package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.dto.ProductionLineCapacity;
import com.cy.modules.planning.agent.entity.MaterialAvailability;
import com.cy.modules.planning.agent.entity.MonthlyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.mapper.MaterialAvailabilityMapper;
import com.cy.modules.planning.agent.mapper.MonthlyPlanMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.InventorySyncService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.WeeklyPlanService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Triển khai quản lý kế hoạch sản xuất tuần
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class WeeklyPlanServiceImpl implements WeeklyPlanService {

    /** Giờ sản xuất tiêu chuẩn mỗi ngày (8 giờ/ca × 1 ca) */
    private static final BigDecimal STANDARD_HOURS_PER_DAY = new BigDecimal("8");

    /** Số ngày làm việc mỗi tuần */
    private static final int WORKING_DAYS_PER_WEEK = 6;

    /** Tổng giờ khả dụng mỗi tuần mỗi dây chuyền = 8h × 6 ngày = 48h */
    private static final BigDecimal WEEKLY_HOURS_PER_LINE =
            STANDARD_HOURS_PER_DAY.multiply(new BigDecimal(WORKING_DAYS_PER_WEEK));

    /** Giới hạn công suất 90% */
    private static final BigDecimal CAPACITY_CAP = new BigDecimal("0.90");

    /** Cycle time tiêu chuẩn mặc định (giờ/đơn vị sản phẩm) */
    private static final BigDecimal DEFAULT_CYCLE_TIME_HOURS = new BigDecimal("0.5");

    /** Changeover time mặc định giữa sản phẩm khác loại (phút) */
    private static final int DEFAULT_CHANGEOVER_MINUTES = 30;

    /** Changeover time khi cùng loại sản phẩm (phút) */
    private static final int SAME_PRODUCT_CHANGEOVER_MINUTES = 0;

    /** Danh sách dây chuyền sản xuất mặc định */
    private static final List<String> DEFAULT_LINE_IDS = Arrays.asList("LINE-01", "LINE-02", "LINE-03");

    @Resource
    private WeeklyPlanMapper weeklyPlanMapper;

    @Resource
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;

    @Resource
    private MonthlyPlanMapper monthlyPlanMapper;

    @Resource
    private MaterialAvailabilityMapper materialAvailabilityMapper;

    @Resource
    private InventorySyncService inventorySyncService;

    @Resource
    private PlanningNotificationService planningNotificationService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ErpClient erpClient;

    // ======================== generateWeeklyPlans ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WeeklyPlan> generateWeeklyPlans(String monthlyPlanId) {
        log.info("[WeeklyPlan] Bắt đầu tạo kế hoạch tuần từ kế hoạch tháng: {}", monthlyPlanId);

        // 1. Tải kế hoạch tháng đã duyệt
        MonthlyPlan monthlyPlan = monthlyPlanMapper.selectById(monthlyPlanId);
        if (monthlyPlan == null) {
            throw new IllegalArgumentException("Không tìm thấy kế hoạch tháng: " + monthlyPlanId);
        }
        if (!"approved".equals(monthlyPlan.getStatus())) {
            throw new IllegalStateException("Kế hoạch tháng chưa được duyệt: " + monthlyPlanId);
        }

        // 2. Parse plan_details JSON
        List<Map<String, Object>> assignments = parsePlanDetails(monthlyPlan.getPlanDetails());
        if (assignments.isEmpty()) {
            log.warn("[WeeklyPlan] Kế hoạch tháng {} không có assignments", monthlyPlanId);
            return Collections.emptyList();
        }

        // 3. Xác định các tuần trong tháng (ISO week numbers)
        int year = monthlyPlan.getYear();
        int month = monthlyPlan.getMonth();
        List<WeekInfo> weeksInMonth = getWeeksInMonth(year, month);
        log.info("[WeeklyPlan] Tháng {}/{} có {} tuần: {}", month, year, weeksInMonth.size(),
                weeksInMonth.stream().map(w -> "W" + w.weekNumber).collect(Collectors.joining(", ")));

        // 4. Lấy công suất dây chuyền
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = YearMonth.of(year, month).atEndOfMonth();
        List<ProductionLineCapacity> lineCapacities = fetchLineCapacities(monthStart, monthEnd);
        List<String> availableLineIds = getAvailableLineIds(lineCapacities);

        // 5. Phân bổ assignments vào các tuần
        Map<Integer, List<Map<String, Object>>> assignmentsByWeek = distributeAssignmentsToWeeks(
                assignments, weeksInMonth);

        // 6. Tạo WeeklyPlan cho mỗi tuần
        List<WeeklyPlan> weeklyPlans = new ArrayList<>();
        List<WeeklyPlanBatch> flaggedBatches = new ArrayList<>();

        int planSequence = 1;
        for (WeekInfo weekInfo : weeksInMonth) {
            List<Map<String, Object>> weekAssignments = assignmentsByWeek.getOrDefault(
                    weekInfo.weekNumber, Collections.emptyList());
            if (weekAssignments.isEmpty()) {
                continue;
            }

            // Tạo WeeklyPlan record
            WeeklyPlan weeklyPlan = new WeeklyPlan();
            weeklyPlan.setPlanCode(generateWeeklyPlanCode(year, weekInfo.weekNumber, planSequence));
            weeklyPlan.setMonthlyPlanId(monthlyPlanId);
            weeklyPlan.setYear(year);
            weeklyPlan.setWeekNumber(weekInfo.weekNumber);
            weeklyPlan.setStartDate(toDate(weekInfo.startDate));
            weeklyPlan.setEndDate(toDate(weekInfo.endDate));
            weeklyPlan.setStatus("draft");
            weeklyPlan.setVersion(1);
            weeklyPlan.setMaterialVerified(0);
            weeklyPlan.setOptionRank(1);

            weeklyPlanMapper.insert(weeklyPlan);
            log.info("[WeeklyPlan] Đã tạo kế hoạch tuần: id={}, code={}, week={}",
                    weeklyPlan.getId(), weeklyPlan.getPlanCode(), weekInfo.weekNumber);

            // 7. Gán sản phẩm vào dây chuyền và tạo batch records
            List<WeeklyPlanBatch> batches = createBatchesForWeek(
                    weeklyPlan, weekAssignments, availableLineIds, lineCapacities, weekInfo);

            // 8. Sắp xếp batch trên mỗi dây chuyền (greedy nearest-neighbor)
            batches = sequenceBatchesByLine(batches);

            // 9. Tính thời gian planned_start/planned_end cho mỗi batch
            calculateBatchTimelines(batches, weekInfo);

            // 10. Xác minh nguyên vật liệu
            boolean allMaterialsVerified = true;
            for (WeeklyPlanBatch batch : batches) {
                boolean materialOk = verifyMaterialAvailability(batch, weekInfo.startDate);
                if (!materialOk) {
                    batch.setMaterialStatus("shortage");
                    allMaterialsVerified = false;
                    flaggedBatches.add(batch);
                } else {
                    batch.setMaterialStatus("verified");
                }
            }

            // 11. Lưu tất cả batch
            for (WeeklyPlanBatch batch : batches) {
                weeklyPlanBatchMapper.insert(batch);
            }

            // Cập nhật material_verified trên WeeklyPlan
            weeklyPlan.setMaterialVerified(allMaterialsVerified ? 1 : 0);
            weeklyPlanMapper.updateById(weeklyPlan);

            weeklyPlans.add(weeklyPlan);
            planSequence++;
        }

        // 12. Thông báo về batch bị đánh dấu thiếu nguyên vật liệu
        if (!flaggedBatches.isEmpty()) {
            notifyMaterialShortage(flaggedBatches);
        }

        log.info("[WeeklyPlan] Hoàn thành tạo {} kế hoạch tuần từ kế hoạch tháng {}",
                weeklyPlans.size(), monthlyPlanId);
        return weeklyPlans;
    }

    // ======================== approveWeeklyPlan ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeeklyPlan approveWeeklyPlan(String weeklyPlanId) {
        log.info("[WeeklyPlan] Duyệt kế hoạch tuần: id={}", weeklyPlanId);

        WeeklyPlan plan = weeklyPlanMapper.selectById(weeklyPlanId);
        if (plan == null) {
            throw new IllegalArgumentException("Không tìm thấy kế hoạch tuần: " + weeklyPlanId);
        }

        if ("approved".equals(plan.getStatus())) {
            log.warn("[WeeklyPlan] Kế hoạch tuần {} đã được duyệt trước đó", weeklyPlanId);
            return plan;
        }

        // Lấy thông tin người duyệt
        String approvedBy = getCurrentUsername();

        plan.setStatus("approved");
        plan.setApprovedBy(approvedBy);
        plan.setApprovedTime(new Date());
        weeklyPlanMapper.updateById(plan);

        log.info("[WeeklyPlan] Đã duyệt kế hoạch tuần: code={}, approvedBy={}",
                plan.getPlanCode(), approvedBy);

        return plan;
    }

    // ======================== Private Helper Methods ========================

    /**
     * Parse plan_details JSON từ MonthlyPlan.
     * Trả về danh sách assignments (mỗi assignment chứa orderId, productType, quantity, assignedLine, startDate, expectedCompletion).
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parsePlanDetails(String planDetailsJson) {
        try {
            Map<String, Object> planDetails = objectMapper.readValue(planDetailsJson,
                    new TypeReference<Map<String, Object>>() {});
            Object assignmentsObj = planDetails.get("assignments");
            if (assignmentsObj instanceof List) {
                return (List<Map<String, Object>>) assignmentsObj;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("[WeeklyPlan] Lỗi parse plan_details JSON: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Xác định các tuần ISO trong tháng.
     */
    private List<WeekInfo> getWeeksInMonth(int year, int month) {
        List<WeekInfo> weeks = new ArrayList<>();
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = YearMonth.of(year, month).atEndOfMonth();

        LocalDate current = monthStart;
        Set<Integer> processedWeeks = new HashSet<>();

        while (!current.isAfter(monthEnd)) {
            int weekNumber = current.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            if (!processedWeeks.contains(weekNumber)) {
                processedWeeks.add(weekNumber);

                // Tính ngày bắt đầu và kết thúc tuần (Monday-Sunday)
                LocalDate weekStart = current.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate weekEnd = current.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

                // Giới hạn trong phạm vi tháng
                if (weekStart.isBefore(monthStart)) {
                    weekStart = monthStart;
                }
                if (weekEnd.isAfter(monthEnd)) {
                    weekEnd = monthEnd;
                }

                weeks.add(new WeekInfo(weekNumber, weekStart, weekEnd));
            }
            current = current.plusDays(1);
        }

        return weeks;
    }

    /**
     * Phân bổ assignments vào các tuần dựa trên startDate.
     */
    private Map<Integer, List<Map<String, Object>>> distributeAssignmentsToWeeks(
            List<Map<String, Object>> assignments, List<WeekInfo> weeks) {

        Map<Integer, List<Map<String, Object>>> result = new LinkedHashMap<>();

        for (Map<String, Object> assignment : assignments) {
            String startDateStr = (String) assignment.get("startDate");
            LocalDate startDate = LocalDate.parse(startDateStr);

            // Tìm tuần chứa startDate
            int targetWeek = startDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

            // Nếu tuần không nằm trong danh sách tuần của tháng, gán vào tuần gần nhất
            boolean found = false;
            for (WeekInfo week : weeks) {
                if (week.weekNumber == targetWeek) {
                    found = true;
                    break;
                }
            }
            if (!found && !weeks.isEmpty()) {
                // Gán vào tuần đầu tiên hoặc cuối cùng tùy vào vị trí
                if (startDate.isBefore(weeks.get(0).startDate)) {
                    targetWeek = weeks.get(0).weekNumber;
                } else {
                    targetWeek = weeks.get(weeks.size() - 1).weekNumber;
                }
            }

            result.computeIfAbsent(targetWeek, k -> new ArrayList<>()).add(assignment);
        }

        return result;
    }

    /**
     * Tạo batch records cho một tuần, gán vào dây chuyền dựa trên capability và changeover time thấp nhất.
     * Áp dụng giới hạn 90% công suất.
     */
    private List<WeeklyPlanBatch> createBatchesForWeek(
            WeeklyPlan weeklyPlan,
            List<Map<String, Object>> weekAssignments,
            List<String> availableLineIds,
            List<ProductionLineCapacity> lineCapacities,
            WeekInfo weekInfo) {

        List<WeeklyPlanBatch> batches = new ArrayList<>();

        // Tính giờ khả dụng tối đa mỗi dây chuyền (90% cap)
        Map<String, BigDecimal> lineMaxHours = new LinkedHashMap<>();
        Map<String, BigDecimal> lineUsedHours = new LinkedHashMap<>();
        Map<String, String> lineLastProductType = new LinkedHashMap<>();

        for (String lineId : availableLineIds) {
            BigDecimal weeklyCapacity = getWeeklyCapacityForLine(lineId, lineCapacities, weekInfo);
            BigDecimal maxHours = weeklyCapacity.multiply(CAPACITY_CAP)
                    .setScale(2, RoundingMode.HALF_UP);
            lineMaxHours.put(lineId, maxHours);
            lineUsedHours.put(lineId, BigDecimal.ZERO);
            lineLastProductType.put(lineId, null);
        }

        // Gán từng assignment vào dây chuyền tối ưu
        for (Map<String, Object> assignment : weekAssignments) {
            String productType = (String) assignment.get("productType");
            String orderId = (String) assignment.get("orderId");
            BigDecimal quantity = toBigDecimal(assignment.get("quantity"));

            // Tính giờ sản xuất cần thiết
            BigDecimal productionHours = quantity.multiply(DEFAULT_CYCLE_TIME_HOURS);

            // Tìm dây chuyền tối ưu: capability + lowest changeover + còn capacity
            String bestLine = findBestLine(productType, productionHours,
                    availableLineIds, lineCapacities, lineMaxHours, lineUsedHours, lineLastProductType);

            if (bestLine == null) {
                // Không có dây chuyền nào còn đủ capacity → redistribute
                bestLine = redistributeToLeastLoadedLine(productionHours,
                        availableLineIds, lineMaxHours, lineUsedHours);
            }

            // Tính changeover time
            int changeoverMinutes = calculateChangeoverMinutes(
                    lineLastProductType.get(bestLine), productType);

            // Tạo batch record
            WeeklyPlanBatch batch = new WeeklyPlanBatch();
            batch.setWeeklyPlanId(weeklyPlan.getId());
            batch.setOrderId(orderId);
            batch.setProductType(productType);
            batch.setQuantity(quantity);
            batch.setGrossQuantity(calculateGrossQuantity(quantity));
            batch.setProductionLineId(bestLine);
            batch.setChangeoverMinutes(changeoverMinutes);
            batch.setStatus("planned");
            batch.setMaterialStatus("pending");

            batches.add(batch);

            // Cập nhật tracking
            BigDecimal changeoverHours = new BigDecimal(changeoverMinutes)
                    .divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);
            lineUsedHours.put(bestLine,
                    lineUsedHours.get(bestLine).add(productionHours).add(changeoverHours));
            lineLastProductType.put(bestLine, productType);
        }

        return batches;
    }

    /**
     * Tìm dây chuyền tối ưu cho sản phẩm dựa trên:
     * 1. Capability (dây chuyền có thể sản xuất sản phẩm này)
     * 2. Changeover time thấp nhất
     * 3. Còn đủ capacity (90% cap)
     */
    private String findBestLine(
            String productType,
            BigDecimal requiredHours,
            List<String> availableLineIds,
            List<ProductionLineCapacity> lineCapacities,
            Map<String, BigDecimal> lineMaxHours,
            Map<String, BigDecimal> lineUsedHours,
            Map<String, String> lineLastProductType) {

        String bestLine = null;
        int lowestChangeover = Integer.MAX_VALUE;
        BigDecimal lowestUtilization = null;

        for (String lineId : availableLineIds) {
            // Kiểm tra capability
            if (!isLineCapable(lineId, productType, lineCapacities)) {
                continue;
            }

            // Kiểm tra capacity còn đủ
            BigDecimal changeoverHours = new BigDecimal(
                    calculateChangeoverMinutes(lineLastProductType.get(lineId), productType))
                    .divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);
            BigDecimal totalNeeded = lineUsedHours.get(lineId).add(requiredHours).add(changeoverHours);

            if (totalNeeded.compareTo(lineMaxHours.get(lineId)) > 0) {
                continue; // Vượt 90% cap
            }

            // Tính changeover time
            int changeover = calculateChangeoverMinutes(lineLastProductType.get(lineId), productType);

            // Chọn dây chuyền có changeover thấp nhất, nếu bằng nhau thì chọn dây chuyền ít tải hơn
            if (changeover < lowestChangeover ||
                    (changeover == lowestChangeover &&
                            (lowestUtilization == null || lineUsedHours.get(lineId).compareTo(lowestUtilization) < 0))) {
                bestLine = lineId;
                lowestChangeover = changeover;
                lowestUtilization = lineUsedHours.get(lineId);
            }
        }

        return bestLine;
    }

    /**
     * Khi không có dây chuyền nào đủ capacity trong giới hạn 90%,
     * redistribute vào dây chuyền có tải thấp nhất (cho phép vượt nhẹ).
     */
    private String redistributeToLeastLoadedLine(
            BigDecimal requiredHours,
            List<String> availableLineIds,
            Map<String, BigDecimal> lineMaxHours,
            Map<String, BigDecimal> lineUsedHours) {

        String leastLoadedLine = availableLineIds.get(0);
        BigDecimal minUsed = lineUsedHours.get(leastLoadedLine);

        for (String lineId : availableLineIds) {
            BigDecimal used = lineUsedHours.get(lineId);
            if (used.compareTo(minUsed) < 0) {
                minUsed = used;
                leastLoadedLine = lineId;
            }
        }

        log.warn("[WeeklyPlan] Redistribute: gán vào dây chuyền {} (tải hiện tại: {}h, cần thêm: {}h)",
                leastLoadedLine, minUsed, requiredHours);
        return leastLoadedLine;
    }

    /**
     * Sắp xếp batch trên mỗi dây chuyền theo greedy nearest-neighbor
     * để tối thiểu tổng changeover time.
     */
    private List<WeeklyPlanBatch> sequenceBatchesByLine(List<WeeklyPlanBatch> batches) {
        // Nhóm batch theo dây chuyền
        Map<String, List<WeeklyPlanBatch>> batchesByLine = batches.stream()
                .collect(Collectors.groupingBy(WeeklyPlanBatch::getProductionLineId));

        List<WeeklyPlanBatch> sequencedBatches = new ArrayList<>();

        for (Map.Entry<String, List<WeeklyPlanBatch>> entry : batchesByLine.entrySet()) {
            List<WeeklyPlanBatch> lineBatches = new ArrayList<>(entry.getValue());

            if (lineBatches.size() <= 1) {
                // Chỉ có 1 batch, không cần sắp xếp
                if (!lineBatches.isEmpty()) {
                    lineBatches.get(0).setSequenceOrder(1);
                    lineBatches.get(0).setChangeoverMinutes(0); // Batch đầu tiên không có changeover
                }
                sequencedBatches.addAll(lineBatches);
                continue;
            }

            // Greedy nearest-neighbor: bắt đầu từ batch đầu tiên, chọn batch tiếp theo có changeover thấp nhất
            List<WeeklyPlanBatch> ordered = new ArrayList<>();
            List<WeeklyPlanBatch> remaining = new ArrayList<>(lineBatches);

            // Bắt đầu từ batch đầu tiên (hoặc có thể chọn batch tối ưu)
            WeeklyPlanBatch current = remaining.remove(0);
            current.setSequenceOrder(1);
            current.setChangeoverMinutes(0); // Batch đầu tiên không có changeover
            ordered.add(current);

            int sequence = 2;
            while (!remaining.isEmpty()) {
                // Tìm batch tiếp theo có changeover thấp nhất so với batch hiện tại
                WeeklyPlanBatch nearest = null;
                int minChangeover = Integer.MAX_VALUE;

                for (WeeklyPlanBatch candidate : remaining) {
                    int changeover = calculateChangeoverMinutes(
                            current.getProductType(), candidate.getProductType());
                    if (changeover < minChangeover) {
                        minChangeover = changeover;
                        nearest = candidate;
                    }
                }

                remaining.remove(nearest);
                nearest.setSequenceOrder(sequence);
                nearest.setChangeoverMinutes(minChangeover);
                ordered.add(nearest);

                current = nearest;
                sequence++;
            }

            sequencedBatches.addAll(ordered);
        }

        return sequencedBatches;
    }

    /**
     * Tính thời gian planned_start và planned_end cho mỗi batch dựa trên sequence order.
     */
    private void calculateBatchTimelines(List<WeeklyPlanBatch> batches, WeekInfo weekInfo) {
        // Nhóm theo dây chuyền
        Map<String, List<WeeklyPlanBatch>> batchesByLine = batches.stream()
                .collect(Collectors.groupingBy(WeeklyPlanBatch::getProductionLineId));

        for (Map.Entry<String, List<WeeklyPlanBatch>> entry : batchesByLine.entrySet()) {
            List<WeeklyPlanBatch> lineBatches = entry.getValue();
            // Sắp xếp theo sequence_order
            lineBatches.sort(Comparator.comparingInt(WeeklyPlanBatch::getSequenceOrder));

            LocalDateTime currentTime = weekInfo.startDate.atTime(8, 0); // Bắt đầu lúc 8:00 sáng

            for (WeeklyPlanBatch batch : lineBatches) {
                // Cộng changeover time
                if (batch.getChangeoverMinutes() > 0) {
                    currentTime = currentTime.plusMinutes(batch.getChangeoverMinutes());
                }

                // Tính thời gian sản xuất
                BigDecimal productionHours = batch.getQuantity().multiply(DEFAULT_CYCLE_TIME_HOURS);
                long productionMinutes = productionHours.multiply(new BigDecimal("60"))
                        .setScale(0, RoundingMode.CEILING).longValue();

                batch.setPlannedStart(toDatetime(currentTime));

                // Tính end time (chỉ tính giờ làm việc 8:00-16:00)
                LocalDateTime endTime = addWorkingMinutes(currentTime, productionMinutes, weekInfo.endDate);
                batch.setPlannedEnd(toDatetime(endTime));

                // Cập nhật current time cho batch tiếp theo
                currentTime = endTime;
            }
        }
    }

    /**
     * Xác minh nguyên vật liệu cho batch:
     * - Kiểm tra tồn kho cache (InventorySyncService)
     * - Kiểm tra ngày dự kiến nhận hàng ≥ 1 ngày làm việc trước ngày bắt đầu batch
     */
    private boolean verifyMaterialAvailability(WeeklyPlanBatch batch, LocalDate weekStartDate) {
        String orderId = batch.getOrderId();
        if (orderId == null) {
            return true; // Không có order liên kết, bỏ qua kiểm tra
        }

        // Lấy danh sách nguyên vật liệu cho đơn hàng này
        LambdaQueryWrapper<MaterialAvailability> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialAvailability::getOrderId, orderId);
        List<MaterialAvailability> materials = materialAvailabilityMapper.selectList(wrapper);

        if (materials.isEmpty()) {
            // Không có dữ liệu nguyên vật liệu → kiểm tra qua InventorySyncService
            BigDecimal inventoryLevel = inventorySyncService.getInventoryLevel(batch.getProductType());
            if (inventoryLevel == null || inventoryLevel.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[WeeklyPlan] Không có dữ liệu tồn kho cho sản phẩm: {}", batch.getProductType());
                return false;
            }
            return true;
        }

        // Kiểm tra từng nguyên vật liệu
        LocalDate batchStartDate = batch.getPlannedStart() != null
                ? batch.getPlannedStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : weekStartDate;
        LocalDate requiredByDate = subtractBusinessDays(batchStartDate, 1);

        for (MaterialAvailability material : materials) {
            if ("shortage".equals(material.getStatus()) || "checking".equals(material.getStatus())) {
                // Kiểm tra expected_arrival
                if (material.getExpectedArrival() == null) {
                    return false; // Không có ngày dự kiến nhận hàng
                }
                LocalDate arrivalDate = material.getExpectedArrival().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                if (arrivalDate.isAfter(requiredByDate)) {
                    log.warn("[WeeklyPlan] Nguyên vật liệu {} dự kiến đến {} nhưng cần trước {}",
                            material.getMaterialId(), arrivalDate, requiredByDate);
                    return false; // Nguyên vật liệu đến sau deadline
                }
            }
            // Nếu status = 'available' hoặc 'received' → OK
            // Nếu status = 'pr_generated' → kiểm tra expected_arrival
            if ("pr_generated".equals(material.getStatus())) {
                if (material.getExpectedArrival() == null) {
                    return false;
                }
                LocalDate arrivalDate = material.getExpectedArrival().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                if (arrivalDate.isAfter(requiredByDate)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Thông báo quản lý sản xuất về batch bị đánh dấu thiếu nguyên vật liệu.
     * Đề xuất: dời lịch batch hoặc tạo PR cho nguyên vật liệu thiếu.
     */
    private void notifyMaterialShortage(List<WeeklyPlanBatch> flaggedBatches) {
        List<Map<String, Object>> batchDetails = new ArrayList<>();
        for (WeeklyPlanBatch batch : flaggedBatches) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("batchId", batch.getId());
            detail.put("orderId", batch.getOrderId());
            detail.put("productType", batch.getProductType());
            detail.put("quantity", batch.getQuantity());
            detail.put("productionLineId", batch.getProductionLineId());
            detail.put("plannedStart", batch.getPlannedStart());
            detail.put("suggestions", Arrays.asList(
                    "Dời lịch batch sang ngày muộn hơn khi nguyên vật liệu sẵn sàng",
                    "Tạo Purchase Request cho nguyên vật liệu thiếu với yêu cầu giao hàng nhanh"
            ));
            batchDetails.add(detail);
        }

        Map<String, Object> notifData = new HashMap<>();
        notifData.put("flaggedBatchCount", flaggedBatches.size());
        notifData.put("batches", batchDetails);

        planningNotificationService.notifyProductionManager(
                NotificationType.MATERIAL_SHORTAGE,
                String.format("Có %d batch trong kế hoạch tuần bị thiếu nguyên vật liệu. " +
                        "Đề xuất: dời lịch hoặc tạo PR cho nguyên vật liệu thiếu.", flaggedBatches.size()),
                notifData);
    }

    // ======================== Utility Methods ========================

    /**
     * Kiểm tra dây chuyền có khả năng sản xuất sản phẩm không.
     */
    private boolean isLineCapable(String lineId, String productType,
                                   List<ProductionLineCapacity> lineCapacities) {
        for (ProductionLineCapacity lc : lineCapacities) {
            if (lineId.equals(lc.getLineId())) {
                if (lc.getCapableProducts() != null && !lc.getCapableProducts().isEmpty()) {
                    return lc.getCapableProducts().contains(productType);
                }
                // Nếu không có danh sách capability → giả sử có thể sản xuất tất cả
                return true;
            }
        }
        // Không tìm thấy thông tin capacity → giả sử capable
        return true;
    }

    /**
     * Tính changeover time giữa 2 loại sản phẩm.
     */
    private int calculateChangeoverMinutes(String fromProductType, String toProductType) {
        if (fromProductType == null || fromProductType.equals(toProductType)) {
            return SAME_PRODUCT_CHANGEOVER_MINUTES;
        }
        return DEFAULT_CHANGEOVER_MINUTES;
    }

    /**
     * Tính gross quantity (điều chỉnh theo yield rate mặc định 95%).
     */
    private BigDecimal calculateGrossQuantity(BigDecimal netQuantity) {
        BigDecimal defaultYieldRate = new BigDecimal("0.95");
        return netQuantity.divide(defaultYieldRate, 2, RoundingMode.CEILING);
    }

    /**
     * Lấy công suất tuần cho dây chuyền.
     */
    private BigDecimal getWeeklyCapacityForLine(String lineId,
                                                 List<ProductionLineCapacity> lineCapacities,
                                                 WeekInfo weekInfo) {
        for (ProductionLineCapacity lc : lineCapacities) {
            if (lineId.equals(lc.getLineId()) && lc.getTotalAvailableHours() != null) {
                // Ước tính giờ tuần từ tổng giờ (chia theo số tuần trong khoảng thời gian)
                long totalDays = java.time.temporal.ChronoUnit.DAYS.between(lc.getFromDate(), lc.getToDate()) + 1;
                long weekDays = java.time.temporal.ChronoUnit.DAYS.between(weekInfo.startDate, weekInfo.endDate) + 1;
                if (totalDays > 0) {
                    return lc.getTotalAvailableHours()
                            .multiply(new BigDecimal(weekDays))
                            .divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP);
                }
            }
        }
        // Fallback: sử dụng giá trị mặc định
        return WEEKLY_HOURS_PER_LINE;
    }

    /**
     * Lấy công suất dây chuyền từ ERP.
     */
    private List<ProductionLineCapacity> fetchLineCapacities(LocalDate from, LocalDate to) {
        List<ProductionLineCapacity> capacities = new ArrayList<>();
        try {
            for (String lineId : DEFAULT_LINE_IDS) {
                ProductionLineCapacity capacity = erpClient.getLineCapacity(lineId, from, to);
                if (capacity != null) {
                    capacities.add(capacity);
                }
            }
        } catch (Exception e) {
            log.error("[WeeklyPlan] Lỗi lấy dữ liệu công suất từ ERP: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
        return capacities;
    }

    /**
     * Lấy danh sách ID dây chuyền khả dụng.
     */
    private List<String> getAvailableLineIds(List<ProductionLineCapacity> lineCapacities) {
        if (lineCapacities != null && !lineCapacities.isEmpty()) {
            return lineCapacities.stream()
                    .map(ProductionLineCapacity::getLineId)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(DEFAULT_LINE_IDS);
    }

    /**
     * Cộng phút làm việc (chỉ tính 8:00-16:00, bỏ qua cuối tuần).
     */
    private LocalDateTime addWorkingMinutes(LocalDateTime start, long minutes, LocalDate maxDate) {
        LocalDateTime current = start;
        long remainingMinutes = minutes;

        while (remainingMinutes > 0) {
            // Nếu ngoài giờ làm việc, chuyển sang ngày tiếp theo
            if (current.getHour() >= 16) {
                current = current.plusDays(1).withHour(8).withMinute(0);
                // Bỏ qua cuối tuần
                while (current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    current = current.plusDays(1);
                }
                continue;
            }

            // Tính phút còn lại trong ngày làm việc
            LocalDateTime endOfDay = current.withHour(16).withMinute(0);
            long minutesLeftToday = java.time.Duration.between(current, endOfDay).toMinutes();

            if (remainingMinutes <= minutesLeftToday) {
                current = current.plusMinutes(remainingMinutes);
                remainingMinutes = 0;
            } else {
                remainingMinutes -= minutesLeftToday;
                current = current.plusDays(1).withHour(8).withMinute(0);
                // Bỏ qua cuối tuần
                while (current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    current = current.plusDays(1);
                }
            }

            // Giới hạn không vượt quá ngày kết thúc tuần
            if (current.toLocalDate().isAfter(maxDate)) {
                current = maxDate.atTime(16, 0);
                break;
            }
        }

        return current;
    }

    /**
     * Trừ ngày làm việc (bỏ qua cuối tuần).
     */
    private LocalDate subtractBusinessDays(LocalDate date, int days) {
        LocalDate result = date;
        int subtracted = 0;
        while (subtracted < days) {
            result = result.minusDays(1);
            if (result.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    result.getDayOfWeek() != DayOfWeek.SUNDAY) {
                subtracted++;
            }
        }
        return result;
    }

    /**
     * Tạo mã kế hoạch tuần: WPyyyyWNN-NNN
     */
    private String generateWeeklyPlanCode(int year, int weekNumber, int sequence) {
        return String.format("WP%dW%02d-%03d", year, weekNumber, sequence);
    }

    /**
     * Lấy username hiện tại.
     */
    private String getCurrentUsername() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal != null) {
                return principal.toString();
            }
        } catch (Exception e) {
            log.debug("[WeeklyPlan] Không lấy được username hiện tại: {}", e.getMessage());
        }
        return "system";
    }

    /**
     * Chuyển đổi Object sang BigDecimal.
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return new BigDecimal(value.toString());
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Chuyển LocalDate sang java.util.Date.
     */
    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Chuyển LocalDateTime sang java.util.Date.
     */
    private Date toDatetime(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    // ======================== Inner Classes ========================

    /**
     * Thông tin tuần trong tháng.
     */
    private static class WeekInfo {
        final int weekNumber;
        final LocalDate startDate;
        final LocalDate endDate;

        WeekInfo(int weekNumber, LocalDate startDate, LocalDate endDate) {
            this.weekNumber = weekNumber;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
