package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.entity.*;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.enums.TriggerType;
import com.cy.modules.planning.agent.event.MachineBreakdownEvent;
import com.cy.modules.planning.agent.mapper.*;
import com.cy.modules.planning.agent.service.PlanOptimizationService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.ReschedulingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Triển khai dịch vụ điều chỉnh kế hoạch sản xuất
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class ReschedulingServiceImpl implements ReschedulingService {

    /** Ngưỡng sai lệch kích hoạt điều chỉnh: >10% */
    private static final BigDecimal DEVIATION_THRESHOLD = new BigDecimal("10.00");

    @Resource
    private WeeklyPlanMapper weeklyPlanMapper;

    @Resource
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;

    @Resource
    private ProductionProgressMapper productionProgressMapper;

    @Resource
    private RescheduleRecordMapper rescheduleRecordMapper;

    @Resource
    private PlanningOrderMapper planningOrderMapper;

    @Resource
    private PlanOptimizationService planOptimizationService;

    @Resource
    private PlanningNotificationService planningNotificationService;

    @Resource
    private ObjectMapper objectMapper;

    // ==================== Event Listener ====================

    /**
     * Lắng nghe sự kiện máy hỏng từ MachineSyncService.
     * Tự động kích hoạt quy trình điều chỉnh kế hoạch.
     */
    @EventListener
    public void onMachineBreakdown(MachineBreakdownEvent event) {
        log.info("[Rescheduling] Nhận sự kiện máy hỏng: lineId={}, machineId={}, detectedAt={}",
                event.getLineId(), event.getMachineId(), event.getDetectedAt());
        handleMachineBreakdown(event.getLineId(), event.getMachineId());
    }

    // ==================== Interface Methods ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RescheduleRecord checkDailyDeviation(String weeklyPlanId) {
        log.info("[Rescheduling] Kiểm tra sai lệch hàng ngày cho kế hoạch tuần: {}", weeklyPlanId);

        WeeklyPlan weeklyPlan = weeklyPlanMapper.selectById(weeklyPlanId);
        if (weeklyPlan == null) {
            log.warn("[Rescheduling] Không tìm thấy kế hoạch tuần: {}", weeklyPlanId);
            return null;
        }

        // Lấy tiến độ sản xuất hôm nay cho kế hoạch tuần
        LambdaQueryWrapper<ProductionProgress> progressWrapper = new LambdaQueryWrapper<>();
        progressWrapper.eq(ProductionProgress::getWeeklyPlanId, weeklyPlanId);
        List<ProductionProgress> progressList = productionProgressMapper.selectList(progressWrapper);

        if (progressList.isEmpty()) {
            log.info("[Rescheduling] Chưa có dữ liệu tiến độ cho kế hoạch tuần: {}", weeklyPlanId);
            return null;
        }

        // Tính tổng planned_qty và actual_qty (cumulative)
        BigDecimal totalPlanned = BigDecimal.ZERO;
        BigDecimal totalActual = BigDecimal.ZERO;
        List<Map<String, Object>> deviationDetails = new ArrayList<>();

        for (ProductionProgress progress : progressList) {
            if (progress.getPlannedQty() != null && progress.getActualQty() != null) {
                totalPlanned = totalPlanned.add(progress.getPlannedQty());
                totalActual = totalActual.add(progress.getActualQty());

                // Kiểm tra sai lệch từng batch
                if (progress.getPlannedQty().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal batchDeviation = progress.getPlannedQty().subtract(progress.getActualQty())
                            .abs()
                            .multiply(new BigDecimal("100"))
                            .divide(progress.getPlannedQty(), 2, RoundingMode.HALF_UP);

                    if (batchDeviation.compareTo(DEVIATION_THRESHOLD) > 0) {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("batchId", progress.getBatchId());
                        detail.put("plannedQty", progress.getPlannedQty());
                        detail.put("actualQty", progress.getActualQty());
                        detail.put("deviationPct", batchDeviation);
                        detail.put("reportDate", progress.getReportDate());
                        deviationDetails.add(detail);
                    }
                }
            }
        }

        // Tính sai lệch tổng thể (cumulative)
        if (totalPlanned.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal overallDeviation = totalPlanned.subtract(totalActual)
                .abs()
                .multiply(new BigDecimal("100"))
                .divide(totalPlanned, 2, RoundingMode.HALF_UP);

        if (overallDeviation.compareTo(DEVIATION_THRESHOLD) <= 0 && deviationDetails.isEmpty()) {
            log.info("[Rescheduling] Sai lệch {}% trong ngưỡng cho phép (≤10%)", overallDeviation);
            return null;
        }

        // Sai lệch vượt ngưỡng → tạo RescheduleRecord
        log.warn("[Rescheduling] Phát hiện sai lệch {}% vượt ngưỡng 10% cho kế hoạch tuần: {}",
                overallDeviation, weeklyPlanId);

        RescheduleRecord record = createRescheduleRecord(
                weeklyPlanId,
                TriggerType.DEVIATION,
                buildDeviationTriggerDetails(overallDeviation, deviationDetails)
        );

        // Tạo phương án điều chỉnh
        generateAndSaveOptions(record, weeklyPlan);

        // Thông báo quản lý sản xuất
        notifyDeviation(record, weeklyPlan, overallDeviation);

        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RescheduleRecord handleMachineBreakdown(String lineId, String machineId) {
        log.info("[Rescheduling] Xử lý máy hỏng: lineId={}, machineId={}", lineId, machineId);

        // Tìm các batch đang chạy hoặc chưa chạy trên dây chuyền bị ảnh hưởng
        LambdaQueryWrapper<WeeklyPlanBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(WeeklyPlanBatch::getProductionLineId, lineId)
                .in(WeeklyPlanBatch::getStatus, "planned", "in_progress");
        List<WeeklyPlanBatch> affectedBatches = weeklyPlanBatchMapper.selectList(batchWrapper);

        if (affectedBatches.isEmpty()) {
            log.info("[Rescheduling] Không có batch nào bị ảnh hưởng trên dây chuyền: {}", lineId);
            return null;
        }

        // Lấy weekly plan ID từ batch đầu tiên
        String weeklyPlanId = affectedBatches.get(0).getWeeklyPlanId();
        WeeklyPlan weeklyPlan = weeklyPlanMapper.selectById(weeklyPlanId);
        if (weeklyPlan == null) {
            log.warn("[Rescheduling] Không tìm thấy kế hoạch tuần: {}", weeklyPlanId);
            return null;
        }

        // Tạo trigger details
        Map<String, Object> triggerDetails = new HashMap<>();
        triggerDetails.put("lineId", lineId);
        triggerDetails.put("machineId", machineId);
        triggerDetails.put("affectedBatchCount", affectedBatches.size());
        triggerDetails.put("affectedBatchIds", affectedBatches.stream()
                .map(WeeklyPlanBatch::getId).collect(Collectors.toList()));

        RescheduleRecord record = createRescheduleRecord(
                weeklyPlanId,
                TriggerType.MACHINE_BREAKDOWN,
                triggerDetails
        );

        // Đánh giá ảnh hưởng đến đơn hàng downstream
        assessDownstreamImpact(record, affectedBatches);

        // Tạo phương án điều chỉnh
        generateAndSaveOptions(record, weeklyPlan);

        // Thông báo quản lý sản xuất và chủ đơn hàng
        notifyMachineBreakdown(record, weeklyPlan, lineId, machineId);

        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RescheduleRecord handleMaterialDelay(String materialId) {
        log.info("[Rescheduling] Xử lý chậm nguyên vật liệu: materialId={}", materialId);

        // Tìm các batch có material_status='shortage' hoặc đang chờ nguyên vật liệu này
        LambdaQueryWrapper<WeeklyPlanBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(WeeklyPlanBatch::getMaterialStatus, "shortage")
                .in(WeeklyPlanBatch::getStatus, "planned", "in_progress");
        List<WeeklyPlanBatch> affectedBatches = weeklyPlanBatchMapper.selectList(batchWrapper);

        if (affectedBatches.isEmpty()) {
            log.info("[Rescheduling] Không có batch nào bị ảnh hưởng bởi chậm NVL: {}", materialId);
            return null;
        }

        // Lấy weekly plan ID
        String weeklyPlanId = affectedBatches.get(0).getWeeklyPlanId();
        WeeklyPlan weeklyPlan = weeklyPlanMapper.selectById(weeklyPlanId);
        if (weeklyPlan == null) {
            log.warn("[Rescheduling] Không tìm thấy kế hoạch tuần: {}", weeklyPlanId);
            return null;
        }

        // Tạo trigger details
        Map<String, Object> triggerDetails = new HashMap<>();
        triggerDetails.put("materialId", materialId);
        triggerDetails.put("affectedBatchCount", affectedBatches.size());
        triggerDetails.put("affectedBatchIds", affectedBatches.stream()
                .map(WeeklyPlanBatch::getId).collect(Collectors.toList()));

        RescheduleRecord record = createRescheduleRecord(
                weeklyPlanId,
                TriggerType.MATERIAL_DELAY,
                triggerDetails
        );

        // Đánh giá ảnh hưởng đến đơn hàng downstream
        assessDownstreamImpact(record, affectedBatches);

        // Tạo phương án điều chỉnh
        generateAndSaveOptions(record, weeklyPlan);

        // Thông báo
        notifyMaterialDelay(record, weeklyPlan, materialId);

        return record;
    }

    @Override
    public List<RescheduleRecord> getReschedulingOptions(String rescheduleRecordId) {
        log.info("[Rescheduling] Lấy phương án điều chỉnh cho record: {}", rescheduleRecordId);

        RescheduleRecord record = rescheduleRecordMapper.selectById(rescheduleRecordId);
        if (record == null) {
            return Collections.emptyList();
        }

        // Trả về record chứa options JSON — caller sẽ parse options field
        return Collections.singletonList(record);
    }

    // ==================== Private Methods ====================

    /**
     * Tạo RescheduleRecord mới.
     */
    private RescheduleRecord createRescheduleRecord(String weeklyPlanId, TriggerType triggerType,
                                                     Map<String, Object> triggerDetails) {
        RescheduleRecord record = new RescheduleRecord();
        record.setOriginalPlanId(weeklyPlanId);
        record.setTriggerType(triggerType.getValue());
        record.setDetectionTime(new Date());
        record.setStatus("pending");

        try {
            record.setTriggerDetails(objectMapper.writeValueAsString(triggerDetails));
        } catch (Exception e) {
            log.warn("[Rescheduling] Không thể serialize trigger details: {}", e.getMessage());
            record.setTriggerDetails("{}");
        }

        record.setOptions("[]");
        rescheduleRecordMapper.insert(record);
        return record;
    }

    /**
     * Đánh giá ảnh hưởng downstream đến các đơn hàng.
     */
    private void assessDownstreamImpact(RescheduleRecord record, List<WeeklyPlanBatch> affectedBatches) {
        // Lấy danh sách order IDs bị ảnh hưởng
        Set<String> orderIds = affectedBatches.stream()
                .map(WeeklyPlanBatch::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (orderIds.isEmpty()) {
            return;
        }

        List<PlanningOrder> orders = planningOrderMapper.selectBatchIds(orderIds);
        List<Map<String, Object>> affectedOrders = new ArrayList<>();

        for (PlanningOrder order : orders) {
            Map<String, Object> impact = new HashMap<>();
            impact.put("orderId", order.getId());
            impact.put("externalOrderId", order.getExternalOrderId());
            impact.put("customerName", order.getCustomerName());
            impact.put("deadline", order.getDeadline());
            impact.put("quantity", order.getQuantity());
            impact.put("status", order.getStatus());
            affectedOrders.add(impact);
        }

        try {
            record.setAffectedOrders(objectMapper.writeValueAsString(affectedOrders));
            rescheduleRecordMapper.updateById(record);
        } catch (Exception e) {
            log.warn("[Rescheduling] Không thể serialize affected orders: {}", e.getMessage());
        }
    }

    /**
     * Tạo ≥2 phương án điều chỉnh xếp hạng theo optimization score.
     * Phương án 1: Phân bổ lại sang dây chuyền khác
     * Phương án 2: Kéo dài timeline
     * Nếu không có phương án nào đáp ứng tất cả deadline → chọn phương án ít ảnh hưởng nhất.
     */
    private void generateAndSaveOptions(RescheduleRecord record, WeeklyPlan originalPlan) {
        List<Map<String, Object>> options = new ArrayList<>();

        // Lấy batches của kế hoạch gốc
        LambdaQueryWrapper<WeeklyPlanBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, originalPlan.getId());
        List<WeeklyPlanBatch> batches = weeklyPlanBatchMapper.selectList(batchWrapper);

        // Phương án 1: Phân bổ lại sang dây chuyền khác
        Map<String, Object> option1 = new HashMap<>();
        option1.put("optionNumber", 1);
        option1.put("strategy", "redistribute_to_other_lines");
        option1.put("description", "Phân bổ lại các batch bị ảnh hưởng sang dây chuyền khác có năng lực trống");
        option1.put("deliveryImpact", "Có thể giữ nguyên deadline nếu dây chuyền khác có năng lực");
        option1.put("lineAssignments", "Phân bổ lại dựa trên năng lực trống của các dây chuyền");
        option1.put("resourceUtilization", "Tăng sử dụng dây chuyền thay thế, giảm tải dây chuyền gốc");
        option1.put("optimizationScore", calculateOptionScore(batches, "redistribute"));
        options.add(option1);

        // Phương án 2: Kéo dài timeline
        Map<String, Object> option2 = new HashMap<>();
        option2.put("optionNumber", 2);
        option2.put("strategy", "extend_timeline");
        option2.put("description", "Kéo dài thời gian sản xuất, dời các batch bị ảnh hưởng sang ngày sau");
        option2.put("deliveryImpact", "Có thể trễ 1-3 ngày cho các đơn hàng bị ảnh hưởng");
        option2.put("lineAssignments", "Giữ nguyên phân bổ dây chuyền, điều chỉnh lịch trình");
        option2.put("resourceUtilization", "Giữ nguyên mức sử dụng, kéo dài thời gian");
        option2.put("optimizationScore", calculateOptionScore(batches, "extend"));
        options.add(option2);

        // Sắp xếp theo optimization score giảm dần
        options.sort((a, b) -> {
            BigDecimal scoreA = (BigDecimal) a.get("optimizationScore");
            BigDecimal scoreB = (BigDecimal) b.get("optimizationScore");
            return scoreB.compareTo(scoreA);
        });

        // Lưu options vào record
        try {
            record.setOptions(objectMapper.writeValueAsString(options));
            record.setRecommendationTime(new Date());
            rescheduleRecordMapper.updateById(record);
        } catch (Exception e) {
            log.error("[Rescheduling] Không thể serialize options: {}", e.getMessage());
        }

        // Tạo phiên bản kế hoạch mới (immutable snapshot)
        createNewPlanVersion(originalPlan, record);
    }

    /**
     * Tạo phiên bản kế hoạch mới (immutable snapshot pattern).
     * Phiên bản mới liên kết với bản gốc qua parent_plan_id.
     */
    private void createNewPlanVersion(WeeklyPlan originalPlan, RescheduleRecord record) {
        WeeklyPlan newPlan = new WeeklyPlan();
        newPlan.setPlanCode(generateNewPlanCode(originalPlan));
        newPlan.setMonthlyPlanId(originalPlan.getMonthlyPlanId());
        newPlan.setYear(originalPlan.getYear());
        newPlan.setWeekNumber(originalPlan.getWeekNumber());
        newPlan.setStartDate(originalPlan.getStartDate());
        newPlan.setEndDate(originalPlan.getEndDate());
        newPlan.setOptionRank(1);
        newPlan.setStatus("draft");
        newPlan.setMaterialVerified(originalPlan.getMaterialVerified());
        newPlan.setVersion(originalPlan.getVersion() != null ? originalPlan.getVersion() + 1 : 2);
        newPlan.setParentPlanId(originalPlan.getId());
        newPlan.setSysOrgCode(originalPlan.getSysOrgCode());

        weeklyPlanMapper.insert(newPlan);

        // Cập nhật trạng thái kế hoạch gốc
        originalPlan.setStatus("rescheduled");
        weeklyPlanMapper.updateById(originalPlan);

        // Liên kết reschedule record với kế hoạch mới
        record.setNewPlanId(newPlan.getId());
        rescheduleRecordMapper.updateById(record);

        // Tối ưu hóa kế hoạch mới
        try {
            planOptimizationService.optimizeWeeklyPlan(newPlan.getId());
        } catch (Exception e) {
            log.warn("[Rescheduling] Không thể tối ưu hóa kế hoạch mới: {}", e.getMessage());
        }

        log.info("[Rescheduling] Đã tạo phiên bản kế hoạch mới: id={}, version={}, parentPlanId={}",
                newPlan.getId(), newPlan.getVersion(), newPlan.getParentPlanId());
    }

    /**
     * Tính optimization score cho phương án điều chỉnh.
     */
    private BigDecimal calculateOptionScore(List<WeeklyPlanBatch> batches, String strategy) {
        if (batches.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Score cơ bản dựa trên strategy
        BigDecimal baseScore;
        if ("redistribute".equals(strategy)) {
            // Phân bổ lại thường giữ được deadline tốt hơn
            baseScore = new BigDecimal("75.00");
        } else {
            // Kéo dài timeline ít rủi ro hơn nhưng ảnh hưởng deadline
            baseScore = new BigDecimal("60.00");
        }

        // Điều chỉnh dựa trên số batch bị ảnh hưởng
        int batchCount = batches.size();
        if (batchCount > 5) {
            baseScore = baseScore.subtract(new BigDecimal("10.00"));
        }

        return baseScore.max(BigDecimal.ZERO).min(new BigDecimal("100.00"));
    }

    /**
     * Tạo mã kế hoạch mới cho phiên bản điều chỉnh.
     */
    private String generateNewPlanCode(WeeklyPlan originalPlan) {
        int newVersion = originalPlan.getVersion() != null ? originalPlan.getVersion() + 1 : 2;
        String basePlanCode = originalPlan.getPlanCode();
        // Thêm suffix version: WPyyyyWNN-NNN-v2
        if (basePlanCode != null && basePlanCode.contains("-v")) {
            basePlanCode = basePlanCode.substring(0, basePlanCode.lastIndexOf("-v"));
        }
        return basePlanCode + "-v" + newVersion;
    }

    /**
     * Xây dựng trigger details cho deviation.
     */
    private Map<String, Object> buildDeviationTriggerDetails(BigDecimal overallDeviation,
                                                              List<Map<String, Object>> deviationDetails) {
        Map<String, Object> details = new HashMap<>();
        details.put("overallDeviationPct", overallDeviation);
        details.put("threshold", DEVIATION_THRESHOLD);
        details.put("batchDeviations", deviationDetails);
        details.put("measurementType", "cumulative_daily");
        return details;
    }

    // ==================== Notification Methods ====================

    private void notifyDeviation(RescheduleRecord record, WeeklyPlan weeklyPlan, BigDecimal deviation) {
        Map<String, Object> data = new HashMap<>();
        data.put("rescheduleRecordId", record.getId());
        data.put("weeklyPlanId", weeklyPlan.getId());
        data.put("planCode", weeklyPlan.getPlanCode());
        data.put("deviationPct", deviation);

        planningNotificationService.notifyProductionManager(
                NotificationType.DEVIATION_DETECTED,
                String.format("Phát hiện sai lệch %.1f%% cho kế hoạch tuần %s. Cần xem xét điều chỉnh.",
                        deviation.doubleValue(), weeklyPlan.getPlanCode()),
                data
        );
    }

    private void notifyMachineBreakdown(RescheduleRecord record, WeeklyPlan weeklyPlan,
                                         String lineId, String machineId) {
        Map<String, Object> data = new HashMap<>();
        data.put("rescheduleRecordId", record.getId());
        data.put("weeklyPlanId", weeklyPlan.getId());
        data.put("planCode", weeklyPlan.getPlanCode());
        data.put("lineId", lineId);
        data.put("machineId", machineId);

        planningNotificationService.notifyProductionManager(
                NotificationType.RESCHEDULE_NEEDED,
                String.format("Máy %s trên dây chuyền %s bị hỏng. Kế hoạch tuần %s cần điều chỉnh.",
                        machineId, lineId, weeklyPlan.getPlanCode()),
                data
        );

        // Thông báo chủ đơn hàng bị ảnh hưởng
        notifyAffectedOrderOwners(record);
    }

    private void notifyMaterialDelay(RescheduleRecord record, WeeklyPlan weeklyPlan, String materialId) {
        Map<String, Object> data = new HashMap<>();
        data.put("rescheduleRecordId", record.getId());
        data.put("weeklyPlanId", weeklyPlan.getId());
        data.put("planCode", weeklyPlan.getPlanCode());
        data.put("materialId", materialId);

        planningNotificationService.notifyProductionManager(
                NotificationType.RESCHEDULE_NEEDED,
                String.format("Chậm nguyên vật liệu %s. Kế hoạch tuần %s cần điều chỉnh.",
                        materialId, weeklyPlan.getPlanCode()),
                data
        );

        // Thông báo chủ đơn hàng bị ảnh hưởng
        notifyAffectedOrderOwners(record);
    }

    /**
     * Thông báo chủ đơn hàng bị ảnh hưởng trong vòng 30 phút.
     */
    private void notifyAffectedOrderOwners(RescheduleRecord record) {
        if (record.getAffectedOrders() == null || record.getAffectedOrders().isEmpty()) {
            return;
        }

        try {
            List<Map<String, Object>> affectedOrders = objectMapper.readValue(
                    record.getAffectedOrders(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            List<String> orderIds = affectedOrders.stream()
                    .map(o -> (String) o.get("orderId"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!orderIds.isEmpty()) {
                planningNotificationService.notifyOrderOwners(
                        orderIds,
                        "Đơn hàng của bạn có thể bị ảnh hưởng do điều chỉnh kế hoạch sản xuất. " +
                                "Vui lòng kiểm tra trạng thái đơn hàng."
                );
            }
        } catch (Exception e) {
            log.warn("[Rescheduling] Không thể thông báo chủ đơn hàng: {}", e.getMessage());
        }
    }
}
