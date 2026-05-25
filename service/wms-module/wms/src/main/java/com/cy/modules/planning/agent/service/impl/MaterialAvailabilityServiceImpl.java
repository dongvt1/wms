package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.dto.BomStructure;
import com.cy.modules.planning.agent.dto.MaterialAvailabilityResult;
import com.cy.modules.planning.agent.entity.MaterialAvailability;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.mapper.MaterialAvailabilityMapper;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.mapper.SupplierLeadTimeMapper;
import com.cy.modules.planning.agent.service.InventorySyncService;
import com.cy.modules.planning.agent.service.MaterialAvailabilityService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation của MaterialAvailabilityService.
 * Kiểm tra tình trạng nguyên vật liệu cho đơn hàng dựa trên BOM và tồn kho cache.
 * Retry logic: 3 lần thử với exponential backoff (1s, 2s, 4s) cho truy vấn ERP.
 * Thông báo quản lý sản xuất sau 3 lần retry thất bại.
 */
@Slf4j
@Service
public class MaterialAvailabilityServiceImpl implements MaterialAvailabilityService {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long[] BACKOFF_DELAYS_MS = {1000, 2000, 4000};

    @Autowired
    private InventorySyncService inventorySyncService;

    @Autowired
    private ErpClient erpClient;

    @Autowired
    private MaterialAvailabilityMapper materialAvailabilityMapper;

    @Autowired
    private PlanningOrderMapper planningOrderMapper;

    @Autowired
    private SupplierLeadTimeMapper supplierLeadTimeMapper;

    @Autowired
    private PlanningNotificationService planningNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialAvailabilityResult checkMaterialAvailability(String orderId) {
        log.info("[MaterialAvailability] Bắt đầu kiểm tra nguyên vật liệu cho đơn hàng: {}", orderId);

        // 1. Tải đơn hàng từ DB
        PlanningOrder order = planningOrderMapper.selectById(orderId);
        if (order == null) {
            log.error("[MaterialAvailability] Không tìm thấy đơn hàng: {}", orderId);
            return MaterialAvailabilityResult.builder()
                    .orderId(orderId)
                    .success(false)
                    .allAvailable(false)
                    .errorMessage("Không tìm thấy đơn hàng: " + orderId)
                    .build();
        }

        String productType = order.getProductType();
        BigDecimal orderQuantity = order.getQuantity();
        log.info("[MaterialAvailability] Đơn hàng {} - Sản phẩm: {}, Số lượng: {}",
                orderId, productType, orderQuantity);

        // 2. Lấy BOM từ cache hoặc truy vấn ERP với retry
        BomStructure bom = getBomWithRetry(productType);
        if (bom == null) {
            log.error("[MaterialAvailability] Không thể lấy BOM cho sản phẩm: {}", productType);
            return MaterialAvailabilityResult.builder()
                    .orderId(orderId)
                    .success(false)
                    .allAvailable(false)
                    .errorMessage("Không thể lấy BOM cho sản phẩm: " + productType)
                    .build();
        }

        if (bom.getItems() == null || bom.getItems().isEmpty()) {
            log.warn("[MaterialAvailability] BOM rỗng cho sản phẩm: {}", productType);
            return MaterialAvailabilityResult.builder()
                    .orderId(orderId)
                    .success(true)
                    .allAvailable(true)
                    .atRisk(false)
                    .materials(Collections.emptyList())
                    .shortages(Collections.emptyList())
                    .build();
        }

        // 3. Xóa bản ghi cũ (nếu có) cho đơn hàng này
        LambdaQueryWrapper<MaterialAvailability> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(MaterialAvailability::getOrderId, orderId);
        materialAvailabilityMapper.delete(deleteWrapper);

        // 4. Kiểm tra từng nguyên vật liệu trong BOM
        List<MaterialAvailability> allMaterials = new ArrayList<>();
        List<MaterialAvailability> shortages = new ArrayList<>();
        boolean allAvailable = true;
        boolean atRisk = false;
        Date now = new Date();

        for (BomStructure.BomItem bomItem : bom.getItems()) {
            // Tính số lượng yêu cầu = quantityPerUnit * orderQuantity
            BigDecimal requiredQty = bomItem.getQuantityPerUnit().multiply(orderQuantity);

            // Tính thêm hao hụt nếu có scrapRate
            if (bomItem.getScrapRate() != null && bomItem.getScrapRate().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal scrapFactor = BigDecimal.ONE.add(
                        bomItem.getScrapRate().divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP)
                );
                requiredQty = requiredQty.multiply(scrapFactor)
                        .setScale(3, BigDecimal.ROUND_HALF_UP);
            }

            // Lấy tồn kho từ cache
            BigDecimal availableQty = inventorySyncService.getInventoryLevel(bomItem.getMaterialId());
            if (availableQty == null) {
                availableQty = BigDecimal.ZERO;
            }

            // Tính deficit = max(0, required - available)
            BigDecimal deficitQty = requiredQty.subtract(availableQty).max(BigDecimal.ZERO);

            // Lấy supplier lead time
            Integer supplierLeadDays = getSupplierLeadDays(bomItem.getMaterialId());

            // Xác định trạng thái
            String status;
            if (deficitQty.compareTo(BigDecimal.ZERO) > 0) {
                status = "shortage";
                allAvailable = false;
            } else {
                status = "available";
            }

            // Tạo bản ghi MaterialAvailability
            MaterialAvailability ma = new MaterialAvailability();
            ma.setOrderId(orderId);
            ma.setMaterialId(bomItem.getMaterialId());
            ma.setMaterialName(bomItem.getMaterialName());
            ma.setRequiredQty(requiredQty);
            ma.setAvailableQty(availableQty);
            ma.setDeficitQty(deficitQty);
            ma.setReserved(0);
            ma.setSupplierLeadDays(supplierLeadDays);
            ma.setStatus(status);
            ma.setCheckTime(now);
            ma.setSysOrgCode(order.getSysOrgCode());

            // Lưu vào DB
            materialAvailabilityMapper.insert(ma);
            allMaterials.add(ma);

            if ("shortage".equals(status)) {
                shortages.add(ma);
            }

            // 7. Xác thực delivery date: current_date + lead_time_days > order.deadline → at-risk
            if (supplierLeadDays != null && order.getDeadline() != null) {
                LocalDate currentDate = LocalDate.now();
                LocalDate expectedDelivery = currentDate.plusDays(supplierLeadDays);
                LocalDate deadline = order.getDeadline().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                if (expectedDelivery.isAfter(deadline)) {
                    atRisk = true;
                    log.warn("[MaterialAvailability] Đơn hàng {} at-risk: vật tư {} cần {} ngày, vượt deadline {}",
                            orderId, bomItem.getMaterialId(), supplierLeadDays, deadline);
                }
            }
        }

        // 5. Nếu tất cả nguyên vật liệu đủ: đặt trước (reserved=1)
        if (allAvailable) {
            for (MaterialAvailability ma : allMaterials) {
                ma.setReserved(1);
                materialAvailabilityMapper.updateById(ma);
            }
            log.info("[MaterialAvailability] Đơn hàng {} - Tất cả nguyên vật liệu đủ, đã đặt trước", orderId);
        } else {
            log.info("[MaterialAvailability] Đơn hàng {} - Thiếu {} nguyên vật liệu",
                    orderId, shortages.size());
        }

        // Thông báo nếu at-risk
        if (atRisk) {
            notifyAtRisk(orderId, order, allMaterials);
        }

        return MaterialAvailabilityResult.builder()
                .orderId(orderId)
                .success(true)
                .allAvailable(allAvailable)
                .atRisk(atRisk)
                .materials(allMaterials)
                .shortages(shortages)
                .build();
    }

    // ==================== Private Methods ====================

    /**
     * Lấy BOM từ cache InventorySyncService, nếu cache miss thì truy vấn ErpClient với retry.
     * Retry logic: 3 lần thử với exponential backoff (1s, 2s, 4s).
     * Sau 3 lần thất bại: thông báo quản lý sản xuất.
     *
     * @param productType mã sản phẩm
     * @return BomStructure hoặc null nếu thất bại
     */
    private BomStructure getBomWithRetry(String productType) {
        // Thử lấy từ cache trước
        BomStructure cachedBom = inventorySyncService.getBom(productType);
        if (cachedBom != null) {
            log.debug("[MaterialAvailability] BOM lấy từ cache cho sản phẩm: {}", productType);
            return cachedBom;
        }

        // Cache miss → truy vấn ERP với retry
        log.info("[MaterialAvailability] BOM cache miss, truy vấn ERP cho sản phẩm: {}", productType);
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                BomStructure bom = erpClient.getBom(productType);
                if (bom != null) {
                    log.info("[MaterialAvailability] Lấy BOM từ ERP thành công (lần thử {})", attempt);
                    return bom;
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("[MaterialAvailability] Lần thử {} lấy BOM thất bại: {}", attempt, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(BACKOFF_DELAYS_MS[attempt - 1]);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[MaterialAvailability] Bị gián đoạn trong khi chờ retry");
                        break;
                    }
                }
            }
        }

        // Tất cả retry đều thất bại → thông báo quản lý sản xuất
        log.error("[MaterialAvailability] Không thể lấy BOM sau {} lần thử cho sản phẩm: {}",
                MAX_RETRY_ATTEMPTS, productType);

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("productType", productType);
        notificationData.put("attempts", MAX_RETRY_ATTEMPTS);
        notificationData.put("lastError", lastException != null ? lastException.getMessage() : "Unknown error");

        planningNotificationService.notifyProductionManager(
                NotificationType.SYSTEM_ERROR,
                String.format("Không thể truy vấn BOM từ ERP cho sản phẩm %s sau %d lần thử",
                        productType, MAX_RETRY_ATTEMPTS),
                notificationData
        );

        return null;
    }

    /**
     * Lấy supplier lead time (ngày) cho một vật tư.
     * Ưu tiên lấy từ bảng ap_supplier_lead_time, fallback sang cache InventorySyncService.
     *
     * @param materialId mã vật tư
     * @return số ngày lead time, null nếu không có dữ liệu
     */
    private Integer getSupplierLeadDays(String materialId) {
        // Thử lấy từ DB (bảng ap_supplier_lead_time)
        LambdaQueryWrapper<com.cy.modules.planning.agent.entity.SupplierLeadTime> wrapper =
                new LambdaQueryWrapper<>();
        wrapper.eq(com.cy.modules.planning.agent.entity.SupplierLeadTime::getMaterialId, materialId)
                .orderByAsc(com.cy.modules.planning.agent.entity.SupplierLeadTime::getLeadTimeDays)
                .last("LIMIT 1");

        com.cy.modules.planning.agent.entity.SupplierLeadTime dbLeadTime =
                supplierLeadTimeMapper.selectOne(wrapper);
        if (dbLeadTime != null && dbLeadTime.getLeadTimeDays() != null) {
            return dbLeadTime.getLeadTimeDays();
        }

        // Fallback: lấy từ cache InventorySyncService
        List<com.cy.modules.planning.agent.dto.SupplierLeadTime> cachedLeadTimes =
                inventorySyncService.getSupplierLeadTime(materialId);
        if (cachedLeadTimes != null && !cachedLeadTimes.isEmpty()) {
            return cachedLeadTimes.stream()
                    .filter(lt -> lt.getLeadTimeDays() != null)
                    .mapToInt(com.cy.modules.planning.agent.dto.SupplierLeadTime::getLeadTimeDays)
                    .min()
                    .orElse(0);
        }

        return null;
    }

    /**
     * Thông báo quản lý sản xuất khi đơn hàng có nguy cơ trễ deadline.
     */
    private void notifyAtRisk(String orderId, PlanningOrder order, List<MaterialAvailability> materials) {
        // Tìm các vật tư gây at-risk
        List<String> atRiskMaterials = materials.stream()
                .filter(ma -> ma.getSupplierLeadDays() != null)
                .filter(ma -> {
                    LocalDate expectedDelivery = LocalDate.now().plusDays(ma.getSupplierLeadDays());
                    LocalDate deadline = order.getDeadline().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    return expectedDelivery.isAfter(deadline);
                })
                .map(ma -> String.format("%s (lead time: %d ngày)", ma.getMaterialName(), ma.getSupplierLeadDays()))
                .collect(Collectors.toList());

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("orderId", orderId);
        notificationData.put("productType", order.getProductType());
        notificationData.put("deadline", order.getDeadline());
        notificationData.put("atRiskMaterials", atRiskMaterials);

        planningNotificationService.notifyProductionManager(
                NotificationType.DEADLINE_AT_RISK,
                String.format("Đơn hàng %s có nguy cơ trễ deadline do thời gian giao hàng vật tư vượt hạn",
                        orderId),
                notificationData
        );
    }
}
