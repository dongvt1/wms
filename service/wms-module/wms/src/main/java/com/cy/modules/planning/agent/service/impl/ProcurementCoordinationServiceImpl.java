package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cy.modules.planning.agent.entity.MaterialAvailability;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.entity.PurchaseRequest;
import com.cy.modules.planning.agent.entity.SupplierLeadTime;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.mapper.MaterialAvailabilityMapper;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.mapper.PurchaseRequestMapper;
import com.cy.modules.planning.agent.mapper.SupplierLeadTimeMapper;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.ProcurementCoordinationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @Description: Triển khai phối hợp mua sắm nguyên vật liệu.
 * Tạo PR, sinh phương án thay thế, xử lý nhận hàng và cập nhật supplier lead time.
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class ProcurementCoordinationServiceImpl implements ProcurementCoordinationService {

    @Resource
    private PurchaseRequestMapper purchaseRequestMapper;

    @Resource
    private SupplierLeadTimeMapper supplierLeadTimeMapper;

    @Resource
    private MaterialAvailabilityMapper materialAvailabilityMapper;

    @Resource
    private PlanningOrderMapper planningOrderMapper;

    @Resource
    private PlanningNotificationService planningNotificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter PR_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseRequest generatePurchaseRequest(String orderId, String materialId, BigDecimal deficitQty, LocalDate productionStartDate) {
        log.info("[Procurement] Tạo PR cho đơn hàng={}, vật liệu={}, thiếu={}, ngày SX={}",
                orderId, materialId, deficitQty, productionStartDate);

        // 1. Lấy supplier lead time từ bảng ap_supplier_lead_time
        SupplierLeadTime supplierLeadTime = getSupplierLeadTime(materialId);
        int leadTimeDays = supplierLeadTime != null ? supplierLeadTime.getLeadTimeDays() : 14; // mặc định 14 ngày

        // 2. Tính required_delivery_date = productionStartDate - lead_time_days
        LocalDate requiredDeliveryDate = productionStartDate.minusDays(leadTimeDays);

        // 3. Sinh mã PR (format: PRyyyyMMddNNN)
        String prCode = generatePrCode();

        // 4. Lấy tên nguyên vật liệu từ MaterialAvailability
        String materialName = getMaterialName(orderId, materialId);

        // 5. Tạo và lưu PurchaseRequest
        PurchaseRequest pr = new PurchaseRequest();
        pr.setPrCode(prCode);
        pr.setOrderId(orderId);
        pr.setMaterialId(materialId);
        pr.setMaterialName(materialName);
        pr.setDeficitQty(deficitQty);
        pr.setRequiredDate(Date.from(requiredDeliveryDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        pr.setSupplierLeadDays(leadTimeDays);
        pr.setStatus("generated");

        // 6. Nếu required_delivery_date < today → không thể đáp ứng, sinh phương án thay thế
        LocalDate today = LocalDate.now();
        if (requiredDeliveryDate.isBefore(today)) {
            log.warn("[Procurement] required_delivery_date={} < today={}, sinh phương án thay thế", requiredDeliveryDate, today);

            // Lấy deadline từ đơn hàng
            PlanningOrder order = planningOrderMapper.selectById(orderId);
            LocalDate deadline = order != null && order.getDeadline() != null
                    ? order.getDeadline().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    : productionStartDate.plusDays(30);

            String alternatives = generateAlternatives(orderId, deadline);
            pr.setAlternatives(alternatives);
        }

        purchaseRequestMapper.insert(pr);

        // Cập nhật trạng thái MaterialAvailability thành pr_generated
        updateMaterialAvailabilityStatus(orderId, materialId, "pr_generated");

        log.info("[Procurement] Đã tạo PR: code={}, requiredDate={}", prCode, requiredDeliveryDate);
        return pr;
    }

    @Override
    public String generateAlternatives(String orderId, LocalDate deadline) {
        log.info("[Procurement] Sinh phương án thay thế cho đơn hàng={}, deadline={}", orderId, deadline);

        List<Map<String, Object>> alternatives = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Lấy thông tin supplier lead time cho các vật liệu của đơn hàng
        List<MaterialAvailability> materials = getMaterialsByOrder(orderId);
        int maxLeadTimeDays = materials.stream()
                .filter(m -> m.getSupplierLeadDays() != null)
                .mapToInt(MaterialAvailability::getSupplierLeadDays)
                .max()
                .orElse(14);

        // Phương án 1: Vận chuyển nhanh (Expedited Shipping)
        // Lead time giảm 50%, chi phí tăng 50%
        int expeditedLeadDays = (int) Math.ceil(maxLeadTimeDays * 0.5);
        LocalDate expeditedDeliveryDate = today.plusDays(expeditedLeadDays);
        Map<String, Object> expeditedOption = new LinkedHashMap<>();
        expeditedOption.put("type", "expedited_shipping");
        expeditedOption.put("estimated_cost_impact", "Chi phí vận chuyển tăng 50%");
        expeditedOption.put("cost_multiplier", 1.5);
        expeditedOption.put("revised_delivery_date", expeditedDeliveryDate.toString());
        expeditedOption.put("lead_time_days", expeditedLeadDays);
        expeditedOption.put("meets_deadline", !expeditedDeliveryDate.isAfter(deadline));
        expeditedOption.put("description", String.format(
                "Sử dụng vận chuyển nhanh, giảm lead time từ %d xuống %d ngày. " +
                "Ngày giao hàng dự kiến: %s. Chi phí vận chuyển tăng khoảng 50%%.",
                maxLeadTimeDays, expeditedLeadDays, expeditedDeliveryDate));
        alternatives.add(expeditedOption);

        // Phương án 2: Nhà cung cấp thay thế (Alternative Supplier)
        // Giả định nhà cung cấp thay thế có lead time = 70% so với nhà cung cấp chính
        int altSupplierLeadDays = (int) Math.ceil(maxLeadTimeDays * 0.7);
        LocalDate altSupplierDeliveryDate = today.plusDays(altSupplierLeadDays);
        Map<String, Object> altSupplierOption = new LinkedHashMap<>();
        altSupplierOption.put("type", "alternative_supplier");
        altSupplierOption.put("estimated_cost_impact", "Chi phí nguyên vật liệu có thể tăng 10-20%");
        altSupplierOption.put("cost_multiplier", 1.15);
        altSupplierOption.put("revised_delivery_date", altSupplierDeliveryDate.toString());
        altSupplierOption.put("lead_time_days", altSupplierLeadDays);
        altSupplierOption.put("meets_deadline", !altSupplierDeliveryDate.isAfter(deadline));
        altSupplierOption.put("description", String.format(
                "Sử dụng nhà cung cấp thay thế với lead time %d ngày. " +
                "Ngày giao hàng dự kiến: %s. Chi phí nguyên vật liệu có thể tăng 10-20%%.",
                altSupplierLeadDays, altSupplierDeliveryDate));
        alternatives.add(altSupplierOption);

        // Phương án 3: Điều chỉnh lịch sản xuất (Production Rescheduling)
        LocalDate rescheduledStart = today.plusDays(maxLeadTimeDays + 1);
        // Ước tính thời gian sản xuất dựa trên đơn hàng
        PlanningOrder order = planningOrderMapper.selectById(orderId);
        int estimatedProductionDays = 7; // mặc định 7 ngày sản xuất
        LocalDate rescheduledCompletion = rescheduledStart.plusDays(estimatedProductionDays);
        Map<String, Object> reschedulingOption = new LinkedHashMap<>();
        reschedulingOption.put("type", "production_rescheduling");
        reschedulingOption.put("estimated_cost_impact", "Không phát sinh chi phí thêm, nhưng trễ deadline");
        reschedulingOption.put("cost_multiplier", 1.0);
        reschedulingOption.put("revised_delivery_date", rescheduledCompletion.toString());
        reschedulingOption.put("new_production_start", rescheduledStart.toString());
        reschedulingOption.put("meets_deadline", !rescheduledCompletion.isAfter(deadline));
        reschedulingOption.put("description", String.format(
                "Điều chỉnh lịch sản xuất, bắt đầu sản xuất từ %s. " +
                "Ngày hoàn thành dự kiến: %s. Không phát sinh chi phí thêm nhưng giao hàng trễ.",
                rescheduledStart, rescheduledCompletion));
        alternatives.add(reschedulingOption);

        // Kiểm tra nếu không có phương án nào đáp ứng deadline → thông báo quản lý
        boolean anyMeetsDeadline = alternatives.stream()
                .anyMatch(alt -> Boolean.TRUE.equals(alt.get("meets_deadline")));

        if (!anyMeetsDeadline) {
            // Tìm ngày sớm nhất khả thi
            LocalDate earliestFeasible = alternatives.stream()
                    .map(alt -> LocalDate.parse((String) alt.get("revised_delivery_date")))
                    .min(LocalDate::compareTo)
                    .orElse(rescheduledCompletion);

            log.warn("[Procurement] Không có phương án nào đáp ứng deadline={}. Ngày sớm nhất: {}",
                    deadline, earliestFeasible);

            // Thông báo quản lý sản xuất
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("order_id", orderId);
            notificationData.put("deadline", deadline.toString());
            notificationData.put("earliest_feasible_date", earliestFeasible.toString());
            notificationData.put("delay_days", ChronoUnit.DAYS.between(deadline, earliestFeasible));
            notificationData.put("alternatives_count", alternatives.size());
            if (order != null) {
                notificationData.put("customer_name", order.getCustomerName());
                notificationData.put("product_type", order.getProductType());
            }

            planningNotificationService.notifyProductionManager(
                    NotificationType.DEADLINE_AT_RISK,
                    String.format("Không có phương án nào đáp ứng deadline %s cho đơn hàng %s. " +
                            "Ngày sản xuất sớm nhất khả thi: %s (trễ %d ngày).",
                            deadline, orderId, earliestFeasible,
                            ChronoUnit.DAYS.between(deadline, earliestFeasible)),
                    notificationData
            );
        }

        return toJson(alternatives);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMaterialReceived(String materialId, BigDecimal receivedQty) {
        log.info("[Procurement] Nhận hàng: vật liệu={}, số lượng={}", materialId, receivedQty);

        // 1. Cập nhật MaterialAvailability records
        LambdaQueryWrapper<MaterialAvailability> maWrapper = new LambdaQueryWrapper<>();
        maWrapper.eq(MaterialAvailability::getMaterialId, materialId)
                .in(MaterialAvailability::getStatus, "shortage", "pr_generated");
        List<MaterialAvailability> affectedRecords = materialAvailabilityMapper.selectList(maWrapper);

        BigDecimal remainingQty = receivedQty;
        List<String> affectedOrderIds = new ArrayList<>();

        for (MaterialAvailability ma : affectedRecords) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal deficit = ma.getDeficitQty();
            BigDecimal allocated = remainingQty.min(deficit);

            // Cập nhật available_qty và deficit_qty
            BigDecimal newAvailable = ma.getAvailableQty().add(allocated);
            BigDecimal newDeficit = deficit.subtract(allocated);

            ma.setAvailableQty(newAvailable);
            ma.setDeficitQty(newDeficit.max(BigDecimal.ZERO));
            ma.setCheckTime(new Date());

            if (newDeficit.compareTo(BigDecimal.ZERO) <= 0) {
                ma.setStatus("received");
            }

            materialAvailabilityMapper.updateById(ma);
            remainingQty = remainingQty.subtract(allocated);
            affectedOrderIds.add(ma.getOrderId());

            log.debug("[Procurement] Cập nhật MA: order={}, material={}, newAvailable={}, newDeficit={}",
                    ma.getOrderId(), materialId, newAvailable, newDeficit);
        }

        // 2. Cập nhật PurchaseRequest status thành 'received'
        LambdaUpdateWrapper<PurchaseRequest> prWrapper = new LambdaUpdateWrapper<>();
        prWrapper.eq(PurchaseRequest::getMaterialId, materialId)
                .in(PurchaseRequest::getStatus, "generated", "submitted", "confirmed")
                .set(PurchaseRequest::getStatus, "received")
                .set(PurchaseRequest::getActualDelivery, new Date());
        purchaseRequestMapper.update(null, prWrapper);

        // 3. Tính lại khả thi sản xuất cho các đơn hàng bị ảnh hưởng
        Set<String> uniqueOrderIds = new LinkedHashSet<>(affectedOrderIds);
        for (String orderId : uniqueOrderIds) {
            recalculateProductionFeasibility(orderId);
        }

        // 4. Cập nhật supplier lead time database
        updateSupplierLeadTimeDatabase(materialId);

        log.info("[Procurement] Hoàn tất xử lý nhận hàng cho vật liệu={}, ảnh hưởng {} đơn hàng",
                materialId, uniqueOrderIds.size());
    }

    @Override
    public List<PurchaseRequest> getPurchaseRequestsByOrder(String orderId) {
        LambdaQueryWrapper<PurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseRequest::getOrderId, orderId)
                .orderByDesc(PurchaseRequest::getCreateTime);
        return purchaseRequestMapper.selectList(wrapper);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Lấy supplier lead time cho nguyên vật liệu.
     * Ưu tiên bản ghi mới nhất.
     */
    private SupplierLeadTime getSupplierLeadTime(String materialId) {
        LambdaQueryWrapper<SupplierLeadTime> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupplierLeadTime::getMaterialId, materialId)
                .orderByDesc(SupplierLeadTime::getLastUpdated)
                .last("LIMIT 1");
        return supplierLeadTimeMapper.selectOne(wrapper);
    }

    /**
     * Lấy tên nguyên vật liệu từ MaterialAvailability.
     */
    private String getMaterialName(String orderId, String materialId) {
        LambdaQueryWrapper<MaterialAvailability> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialAvailability::getOrderId, orderId)
                .eq(MaterialAvailability::getMaterialId, materialId)
                .last("LIMIT 1");
        MaterialAvailability ma = materialAvailabilityMapper.selectOne(wrapper);
        return ma != null ? ma.getMaterialName() : materialId;
    }

    /**
     * Sinh mã PR theo format PRyyyyMMddNNN.
     * NNN là số thứ tự trong ngày, tăng dần.
     */
    private String generatePrCode() {
        String dateStr = LocalDate.now().format(PR_DATE_FORMAT);
        String prefix = "PR" + dateStr;

        // Đếm số PR đã tạo trong ngày
        LambdaQueryWrapper<PurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(PurchaseRequest::getPrCode, prefix);
        Long count = purchaseRequestMapper.selectCount(wrapper);

        int sequence = (count != null ? count.intValue() : 0) + 1;
        return String.format("%s%03d", prefix, sequence);
    }

    /**
     * Cập nhật trạng thái MaterialAvailability.
     */
    private void updateMaterialAvailabilityStatus(String orderId, String materialId, String status) {
        LambdaUpdateWrapper<MaterialAvailability> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MaterialAvailability::getOrderId, orderId)
                .eq(MaterialAvailability::getMaterialId, materialId)
                .set(MaterialAvailability::getStatus, status)
                .set(MaterialAvailability::getCheckTime, new Date());
        materialAvailabilityMapper.update(null, wrapper);
    }

    /**
     * Lấy danh sách MaterialAvailability theo đơn hàng.
     */
    private List<MaterialAvailability> getMaterialsByOrder(String orderId) {
        LambdaQueryWrapper<MaterialAvailability> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialAvailability::getOrderId, orderId);
        return materialAvailabilityMapper.selectList(wrapper);
    }

    /**
     * Tính lại khả thi sản xuất cho đơn hàng.
     * Feasibility = tất cả nguyên vật liệu BOM đều available trước ngày bắt đầu sản xuất.
     */
    private void recalculateProductionFeasibility(String orderId) {
        log.info("[Procurement] Tính lại khả thi sản xuất cho đơn hàng={}", orderId);

        List<MaterialAvailability> materials = getMaterialsByOrder(orderId);
        if (materials.isEmpty()) {
            return;
        }

        // Kiểm tra tất cả nguyên vật liệu đã đủ chưa
        boolean allAvailable = materials.stream()
                .allMatch(m -> m.getDeficitQty() == null
                        || m.getDeficitQty().compareTo(BigDecimal.ZERO) <= 0
                        || "received".equals(m.getStatus())
                        || "available".equals(m.getStatus()));

        if (allAvailable) {
            log.info("[Procurement] Đơn hàng {} đủ nguyên vật liệu, sẵn sàng sản xuất", orderId);

            // Cập nhật trạng thái đơn hàng nếu đang pending
            PlanningOrder order = planningOrderMapper.selectById(orderId);
            if (order != null && "pending".equals(order.getStatus())) {
                order.setStatus("confirmed");
                planningOrderMapper.updateById(order);
            }

            // Thông báo quản lý
            Map<String, Object> data = new HashMap<>();
            data.put("order_id", orderId);
            data.put("feasible", true);
            if (order != null) {
                data.put("product_type", order.getProductType());
                data.put("deadline", order.getDeadline());
            }

            planningNotificationService.notifyProductionManager(
                    NotificationType.PLAN_GENERATED,
                    String.format("Đơn hàng %s đã đủ nguyên vật liệu, sẵn sàng lên kế hoạch sản xuất.", orderId),
                    data
            );
        } else {
            // Vẫn còn thiếu, log chi tiết
            long shortageCount = materials.stream()
                    .filter(m -> m.getDeficitQty() != null && m.getDeficitQty().compareTo(BigDecimal.ZERO) > 0
                            && !"received".equals(m.getStatus()) && !"available".equals(m.getStatus()))
                    .count();
            log.info("[Procurement] Đơn hàng {} vẫn còn {} nguyên vật liệu thiếu", orderId, shortageCount);
        }
    }

    /**
     * Cập nhật supplier lead time database sau mỗi chu kỳ mua sắm hoàn tất.
     * So sánh thời gian giao hàng thực tế với thời gian đã ghi nhận.
     */
    private void updateSupplierLeadTimeDatabase(String materialId) {
        // Lấy PR đã nhận gần nhất cho vật liệu này
        LambdaQueryWrapper<PurchaseRequest> prWrapper = new LambdaQueryWrapper<>();
        prWrapper.eq(PurchaseRequest::getMaterialId, materialId)
                .eq(PurchaseRequest::getStatus, "received")
                .isNotNull(PurchaseRequest::getActualDelivery)
                .orderByDesc(PurchaseRequest::getActualDelivery)
                .last("LIMIT 1");
        PurchaseRequest latestPr = purchaseRequestMapper.selectOne(prWrapper);

        if (latestPr == null || latestPr.getActualDelivery() == null || latestPr.getCreateTime() == null) {
            return;
        }

        // Tính số ngày giao hàng thực tế
        LocalDate createDate = latestPr.getCreateTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate deliveryDate = latestPr.getActualDelivery().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        int actualDays = (int) ChronoUnit.DAYS.between(createDate, deliveryDate);

        if (actualDays <= 0) {
            actualDays = 1;
        }

        // Cập nhật supplier lead time
        LambdaQueryWrapper<SupplierLeadTime> sltWrapper = new LambdaQueryWrapper<>();
        sltWrapper.eq(SupplierLeadTime::getMaterialId, materialId)
                .orderByDesc(SupplierLeadTime::getLastUpdated)
                .last("LIMIT 1");
        SupplierLeadTime slt = supplierLeadTimeMapper.selectOne(sltWrapper);

        if (slt != null) {
            slt.setLastActualDays(actualDays);

            // Cập nhật trung bình: avg = (old_avg + actual) / 2 (đơn giản hóa)
            BigDecimal oldAvg = slt.getAvgLeadTimeDays() != null
                    ? slt.getAvgLeadTimeDays()
                    : BigDecimal.valueOf(slt.getLeadTimeDays());
            BigDecimal newAvg = oldAvg.add(BigDecimal.valueOf(actualDays))
                    .divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP);
            slt.setAvgLeadTimeDays(newAvg);

            // Cập nhật lead_time_days dựa trên trung bình mới
            slt.setLeadTimeDays(newAvg.setScale(0, RoundingMode.CEILING).intValue());
            slt.setLastUpdated(new Date());
            slt.setUpdateSource("procurement_cycle");

            supplierLeadTimeMapper.updateById(slt);

            log.info("[Procurement] Cập nhật supplier lead time: material={}, actualDays={}, newAvg={}, newLeadTime={}",
                    materialId, actualDays, newAvg, slt.getLeadTimeDays());
        } else {
            log.warn("[Procurement] Không tìm thấy supplier lead time cho vật liệu={}", materialId);
        }
    }

    /**
     * Chuyển đổi object sang JSON string.
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("[Procurement] Lỗi chuyển đổi JSON: {}", e.getMessage(), e);
            return "[]";
        }
    }
}
