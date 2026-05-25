package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.dto.BomStructure;
import com.cy.modules.planning.agent.dto.MaterialIssuanceRequest;
import com.cy.modules.planning.agent.dto.ProductionOrderRequest;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.enums.BatchStatus;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.enums.PlanStatus;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.InventorySyncService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.ProductionOrderIssuanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation của ProductionOrderIssuanceService.
 * Phát lệnh sản xuất trong ERP trong vòng 5 phút sau khi kế hoạch tuần được duyệt.
 * Retry logic: 3 lần thử, mỗi lần cách nhau 60 giây.
 * Khi tất cả lệnh được xác nhận → cập nhật trạng thái kế hoạch sang "in_execution".
 */
@Slf4j
@Service
public class ProductionOrderIssuanceServiceImpl implements ProductionOrderIssuanceService {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_INTERVAL_MS = 60000; // 60 giây

    @Autowired
    private ErpClient erpClient;

    @Autowired
    private WeeklyPlanMapper weeklyPlanMapper;

    @Autowired
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;

    @Autowired
    private InventorySyncService inventorySyncService;

    @Autowired
    private PlanningNotificationService planningNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueProductionOrders(String weeklyPlanId) {
        log.info("[ProductionOrderIssuance] Bắt đầu phát lệnh sản xuất cho weeklyPlanId={}", weeklyPlanId);

        // 1. Load kế hoạch tuần đã duyệt
        WeeklyPlan weeklyPlan = weeklyPlanMapper.selectById(weeklyPlanId);
        if (weeklyPlan == null) {
            log.error("[ProductionOrderIssuance] Không tìm thấy kế hoạch tuần: {}", weeklyPlanId);
            return;
        }
        if (!PlanStatus.APPROVED.getValue().equals(weeklyPlan.getStatus())) {
            log.warn("[ProductionOrderIssuance] Kế hoạch tuần chưa được duyệt: status={}", weeklyPlan.getStatus());
            return;
        }

        // 2. Lấy danh sách batch
        LambdaQueryWrapper<WeeklyPlanBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId);
        List<WeeklyPlanBatch> batches = weeklyPlanBatchMapper.selectList(batchWrapper);

        if (batches.isEmpty()) {
            log.warn("[ProductionOrderIssuance] Không có batch nào trong kế hoạch tuần: {}", weeklyPlanId);
            return;
        }

        // 3. Phát lệnh sản xuất cho từng batch
        boolean allSuccess = true;
        for (WeeklyPlanBatch batch : batches) {
            boolean orderSuccess = createProductionOrderWithRetry(batch, weeklyPlanId);
            if (!orderSuccess) {
                allSuccess = false;
                continue;
            }

            // 4. Kích hoạt xuất kho nguyên vật liệu theo BOM
            boolean issuanceSuccess = triggerMaterialIssuanceForBatch(batch);
            if (!issuanceSuccess) {
                allSuccess = false;
            }
        }

        // 5. Khi tất cả lệnh được xác nhận → cập nhật trạng thái
        if (allSuccess) {
            weeklyPlan.setStatus(PlanStatus.IN_EXECUTION.getValue());
            weeklyPlan.setIssuedTime(new Date());
            weeklyPlanMapper.updateById(weeklyPlan);
            log.info("[ProductionOrderIssuance] Tất cả lệnh sản xuất đã được xác nhận. " +
                    "Kế hoạch tuần {} chuyển sang trạng thái 'in_execution'", weeklyPlanId);
        } else {
            log.warn("[ProductionOrderIssuance] Một số batch thất bại. Kế hoạch tuần {} chưa chuyển trạng thái", weeklyPlanId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryFailedOrder(String batchId) {
        log.info("[ProductionOrderIssuance] Thử lại phát lệnh cho batchId={}", batchId);

        WeeklyPlanBatch batch = weeklyPlanBatchMapper.selectById(batchId);
        if (batch == null) {
            log.error("[ProductionOrderIssuance] Không tìm thấy batch: {}", batchId);
            return;
        }

        boolean success = createProductionOrderWithRetry(batch, batch.getWeeklyPlanId());
        if (success) {
            triggerMaterialIssuanceForBatch(batch);
        }
    }

    // ==================== Private Methods ====================

    /**
     * Tạo lệnh sản xuất với retry logic: 3 lần thử, mỗi lần cách nhau 60 giây.
     * Sau 3 lần thất bại: thông báo quản lý sản xuất và đặt batch on_hold.
     */
    private boolean createProductionOrderWithRetry(WeeklyPlanBatch batch, String weeklyPlanId) {
        ProductionOrderRequest request = buildProductionOrderRequest(batch, weeklyPlanId);

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                erpClient.createProductionOrder(request);
                log.info("[ProductionOrderIssuance] Tạo lệnh sản xuất thành công cho batch={} (lần thử {})",
                        batch.getId(), attempt);
                return true;
            } catch (Exception e) {
                log.warn("[ProductionOrderIssuance] Lần thử {} thất bại cho batch={}: {}",
                        attempt, batch.getId(), e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[ProductionOrderIssuance] Bị gián đoạn trong khi chờ retry");
                        break;
                    }
                }
            }
        }

        // Sau 3 lần thất bại: thông báo và đặt on_hold
        onOrderCreationFailed(batch);
        return false;
    }

    /**
     * Kích hoạt xuất kho nguyên vật liệu theo BOM cho batch.
     * Nếu thất bại: thông báo quản lý và đặt batch on_hold.
     */
    private boolean triggerMaterialIssuanceForBatch(WeeklyPlanBatch batch) {
        try {
            BomStructure bom = inventorySyncService.getBom(batch.getProductType());
            if (bom == null || bom.getItems() == null || bom.getItems().isEmpty()) {
                log.warn("[ProductionOrderIssuance] Không tìm thấy BOM cho productType={}", batch.getProductType());
                return true; // Không có BOM thì bỏ qua
            }

            // Xây dựng danh sách vật tư cần xuất theo BOM
            List<MaterialIssuanceRequest.MaterialItem> materials = bom.getItems().stream()
                    .map(bomItem -> MaterialIssuanceRequest.MaterialItem.builder()
                            .materialId(bomItem.getMaterialId())
                            .materialName(bomItem.getMaterialName())
                            .quantity(bomItem.getQuantityPerUnit().multiply(batch.getQuantity()))
                            .unit(bomItem.getUnit())
                            .build())
                    .collect(Collectors.toList());

            MaterialIssuanceRequest issuanceRequest = MaterialIssuanceRequest.builder()
                    .productionOrderId(batch.getId())
                    .targetProductionLineId(batch.getProductionLineId())
                    .materials(materials)
                    .build();

            erpClient.triggerMaterialIssuance(issuanceRequest);
            log.info("[ProductionOrderIssuance] Xuất kho nguyên vật liệu thành công cho batch={}", batch.getId());
            return true;

        } catch (Exception e) {
            log.error("[ProductionOrderIssuance] Xuất kho thất bại cho batch={}: {}", batch.getId(), e.getMessage());
            onMaterialIssuanceFailed(batch, e);
            return false;
        }
    }

    /**
     * Xử lý khi tạo lệnh sản xuất thất bại sau 3 lần thử.
     */
    private void onOrderCreationFailed(WeeklyPlanBatch batch) {
        // Đặt batch on_hold
        batch.setStatus(BatchStatus.ON_HOLD.getValue());
        weeklyPlanBatchMapper.updateById(batch);

        // Thông báo quản lý sản xuất
        Map<String, Object> data = new HashMap<>();
        data.put("batchId", batch.getId());
        data.put("productType", batch.getProductType());
        data.put("quantity", batch.getQuantity());
        data.put("productionLineId", batch.getProductionLineId());
        data.put("reason", "Tạo lệnh sản xuất thất bại sau 3 lần thử");

        planningNotificationService.notifyProductionManager(
                NotificationType.SYSTEM_ERROR,
                String.format("Lệnh sản xuất thất bại cho batch %s (sản phẩm: %s, SL: %s)",
                        batch.getId(), batch.getProductType(), batch.getQuantity()),
                data
        );

        log.error("[ProductionOrderIssuance] Batch {} đã được đặt on_hold sau 3 lần thử thất bại", batch.getId());
    }

    /**
     * Xử lý khi xuất kho nguyên vật liệu thất bại.
     */
    private void onMaterialIssuanceFailed(WeeklyPlanBatch batch, Exception e) {
        // Đặt batch on_hold
        batch.setStatus(BatchStatus.ON_HOLD.getValue());
        weeklyPlanBatchMapper.updateById(batch);

        // Thông báo quản lý sản xuất
        Map<String, Object> data = new HashMap<>();
        data.put("batchId", batch.getId());
        data.put("productType", batch.getProductType());
        data.put("productionLineId", batch.getProductionLineId());
        data.put("reason", "Xuất kho nguyên vật liệu thất bại: " + e.getMessage());

        planningNotificationService.notifyProductionManager(
                NotificationType.MATERIAL_SHORTAGE,
                String.format("Xuất kho NVL thất bại cho batch %s (sản phẩm: %s)",
                        batch.getId(), batch.getProductType()),
                data
        );

        log.error("[ProductionOrderIssuance] Batch {} đặt on_hold do xuất kho NVL thất bại", batch.getId());
    }

    /**
     * Xây dựng ProductionOrderRequest từ batch.
     */
    private ProductionOrderRequest buildProductionOrderRequest(WeeklyPlanBatch batch, String weeklyPlanId) {
        LocalDateTime plannedStart = batch.getPlannedStart() != null
                ? batch.getPlannedStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
        LocalDateTime plannedEnd = batch.getPlannedEnd() != null
                ? batch.getPlannedEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;

        return ProductionOrderRequest.builder()
                .weeklyPlanId(weeklyPlanId)
                .batchId(batch.getId())
                .productId(batch.getProductType())
                .productType(batch.getProductType())
                .quantity(batch.getQuantity())
                .productionLineId(batch.getProductionLineId())
                .machineId(batch.getMachineId())
                .plannedStartTime(plannedStart)
                .plannedEndTime(plannedEnd)
                .build();
    }
}
