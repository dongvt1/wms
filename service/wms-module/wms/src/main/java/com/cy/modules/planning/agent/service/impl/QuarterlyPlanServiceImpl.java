package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.dto.ProductionLineCapacity;
import com.cy.modules.planning.agent.entity.MonthlyPlan;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.entity.QuarterlyPlan;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.mapper.MonthlyPlanMapper;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.mapper.QuarterlyPlanMapper;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.QuarterlyPlanService;
import com.cy.modules.planning.agent.service.StalenessManagementService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Triển khai quản lý kế hoạch sản xuất quý và tháng
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class QuarterlyPlanServiceImpl implements QuarterlyPlanService {

    /** Giờ sản xuất tiêu chuẩn mỗi ngày (8 giờ/ca × 1 ca) */
    private static final BigDecimal STANDARD_HOURS_PER_DAY = new BigDecimal("8");

    /** Cycle time tiêu chuẩn mặc định (giờ/đơn vị sản phẩm) */
    private static final BigDecimal DEFAULT_CYCLE_TIME_HOURS = new BigDecimal("0.5");

    /** Danh sách dây chuyền sản xuất mặc định */
    private static final List<String> DEFAULT_LINE_IDS = Arrays.asList("LINE-01", "LINE-02", "LINE-03");

    @Resource
    private QuarterlyPlanMapper quarterlyPlanMapper;

    @Resource
    private MonthlyPlanMapper monthlyPlanMapper;

    @Resource
    private PlanningOrderMapper planningOrderMapper;

    @Resource
    private ErpClient erpClient;

    @Resource
    private StalenessManagementService stalenessManagementService;

    @Resource
    private PlanningNotificationService planningNotificationService;

    @Resource
    private ObjectMapper objectMapper;

    // ======================== generateQuarterlyPlan ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuarterlyPlan generateQuarterlyPlan(int year, int quarter) {
        log.info("[QuarterlyPlan] Bắt đầu tạo kế hoạch quý: year={}, quarter={}", year, quarter);

        // 1. Lấy tất cả đơn hàng confirmed/pending có deadline trong quý này
        LocalDate quarterStart = getQuarterStartDate(year, quarter);
        LocalDate quarterEnd = getQuarterEndDate(year, quarter);

        List<PlanningOrder> orders = getOrdersForQuarter(quarterStart, quarterEnd);
        log.info("[QuarterlyPlan] Tìm thấy {} đơn hàng trong quý {}/{}", orders.size(), quarter, year);

        // 2. Phân loại nhu cầu theo loại sản phẩm cho từng tháng
        Map<Integer, Map<String, BigDecimal>> demandByMonth = classifyDemandByMonth(orders, quarterStart);

        // 3. Xác nhận công suất
        boolean capacityValidated = true;
        Map<Integer, Map<String, Object>> capacityGaps = new LinkedHashMap<>();

        List<ProductionLineCapacity> lineCapacities = fetchLineCapacities(quarterStart, quarterEnd);
        boolean usingCachedData = lineCapacities.isEmpty();

        if (usingCachedData) {
            // Sử dụng dữ liệu cache khi không lấy được dữ liệu live
            capacityValidated = false;
            log.warn("[QuarterlyPlan] Không lấy được dữ liệu công suất live, sử dụng dữ liệu cache");
        }

        // Tính tổng công suất khả dụng mỗi tháng
        for (int monthOffset = 0; monthOffset < 3; monthOffset++) {
            int month = quarterStart.getMonthValue() + monthOffset;
            Map<String, BigDecimal> monthDemand = demandByMonth.getOrDefault(month, Collections.emptyMap());

            BigDecimal totalDemandHours = calculateTotalDemandHours(monthDemand);
            BigDecimal totalCapacityHours = calculateMonthlyCapacity(year, month, lineCapacities);

            if (totalDemandHours.compareTo(totalCapacityHours) > 0) {
                capacityValidated = false;
                BigDecimal gap = totalDemandHours.subtract(totalCapacityHours);

                // Tạo ít nhất 2 phương án thay thế
                List<Map<String, Object>> alternatives = generateAlternatives(
                        month, monthDemand, totalDemandHours, totalCapacityHours, gap);

                Map<String, Object> gapDetail = new LinkedHashMap<>();
                gapDetail.put("month", month);
                gapDetail.put("demandHours", totalDemandHours);
                gapDetail.put("capacityHours", totalCapacityHours);
                gapDetail.put("gapHours", gap);
                gapDetail.put("alternatives", alternatives);
                capacityGaps.put(month, gapDetail);

                log.warn("[QuarterlyPlan] Tháng {} vượt công suất: demand={}h, capacity={}h, gap={}h",
                        month, totalDemandHours, totalCapacityHours, gap);
            }
        }

        // 4. Tạo và lưu QuarterlyPlan
        QuarterlyPlan plan = new QuarterlyPlan();
        plan.setPlanCode(generateQuarterlyPlanCode(year, quarter));
        plan.setYear(year);
        plan.setQuarter(quarter);
        plan.setStatus("draft");
        plan.setDemandSummary(toJson(demandByMonth));
        plan.setCapacityValidated(capacityValidated ? 1 : 0);
        plan.setCapacityGaps(capacityGaps.isEmpty() ? null : toJson(capacityGaps));
        plan.setSysOrgCode(null); // Sẽ được set bởi framework

        quarterlyPlanMapper.insert(plan);
        log.info("[QuarterlyPlan] Đã tạo kế hoạch quý: id={}, code={}, validated={}",
                plan.getId(), plan.getPlanCode(), capacityValidated);

        // 5. Thông báo nếu plan chưa được xác nhận công suất
        if (!capacityValidated) {
            Map<String, Object> notifData = new HashMap<>();
            notifData.put("planId", plan.getId());
            notifData.put("planCode", plan.getPlanCode());
            notifData.put("usingCachedData", usingCachedData);
            notifData.put("capacityGaps", capacityGaps);

            if (usingCachedData) {
                planningNotificationService.notifyProductionManager(
                        NotificationType.PLAN_GENERATED,
                        String.format("Kế hoạch quý %s đã tạo nhưng CHƯA XÁC NHẬN công suất (dữ liệu cache)", plan.getPlanCode()),
                        notifData);
            } else {
                planningNotificationService.notifyProductionManager(
                        NotificationType.PLAN_GENERATED,
                        String.format("Kế hoạch quý %s: nhu cầu vượt công suất, cần xem xét phương án thay thế", plan.getPlanCode()),
                        notifData);
            }
        } else {
            Map<String, Object> notifData = new HashMap<>();
            notifData.put("planId", plan.getId());
            notifData.put("planCode", plan.getPlanCode());
            planningNotificationService.notifyProductionManager(
                    NotificationType.PLAN_GENERATED,
                    String.format("Kế hoạch quý %s đã tạo và xác nhận công suất thành công", plan.getPlanCode()),
                    notifData);
        }

        return plan;
    }

    // ======================== generateMonthlyPlanOptions ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MonthlyPlan> generateMonthlyPlanOptions(String quarterlyPlanId, int year, int month) {
        log.info("[MonthlyPlan] Tạo phương án kế hoạch tháng: quarterlyPlanId={}, year={}, month={}",
                quarterlyPlanId, year, month);

        // 1. Lấy kế hoạch quý
        QuarterlyPlan quarterlyPlan = quarterlyPlanMapper.selectById(quarterlyPlanId);
        if (quarterlyPlan == null) {
            throw new IllegalArgumentException("Không tìm thấy kế hoạch quý: " + quarterlyPlanId);
        }

        // 2. Lấy đơn hàng cho tháng này
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = YearMonth.of(year, month).atEndOfMonth();

        List<PlanningOrder> monthOrders = getOrdersForPeriod(monthStart, monthEnd);
        log.info("[MonthlyPlan] Tìm thấy {} đơn hàng cho tháng {}/{}", monthOrders.size(), month, year);

        // 3. Phân loại theo loại sản phẩm
        Map<String, List<PlanningOrder>> ordersByProduct = monthOrders.stream()
                .collect(Collectors.groupingBy(PlanningOrder::getProductType));

        // 4. Lấy công suất dây chuyền
        List<ProductionLineCapacity> lineCapacities = fetchLineCapacities(monthStart, monthEnd);
        BigDecimal totalMonthlyCapacity = calculateMonthlyCapacity(year, month, lineCapacities);

        // 5. Tạo 1-3 phương án
        List<MonthlyPlan> options = new ArrayList<>();

        // Phương án 1: Phân bổ đều theo deadline (ưu tiên deadline sớm nhất)
        Map<String, Object> option1Details = buildOptionByDeadlinePriority(
                ordersByProduct, lineCapacities, monthStart, monthEnd);
        BigDecimal option1Hours = calculateOptionTotalHours(option1Details);
        BigDecimal option1Utilization = calculateUtilization(option1Hours, totalMonthlyCapacity);

        MonthlyPlan option1 = createMonthlyPlan(quarterlyPlanId, year, month, 1,
                option1Details, option1Hours, option1Utilization);
        options.add(option1);

        // Phương án 2: Tối ưu hóa sử dụng dây chuyền (nhóm sản phẩm cùng loại)
        Map<String, Object> option2Details = buildOptionByLineOptimization(
                ordersByProduct, lineCapacities, monthStart, monthEnd);
        BigDecimal option2Hours = calculateOptionTotalHours(option2Details);
        BigDecimal option2Utilization = calculateUtilization(option2Hours, totalMonthlyCapacity);

        MonthlyPlan option2 = createMonthlyPlan(quarterlyPlanId, year, month, 2,
                option2Details, option2Hours, option2Utilization);
        options.add(option2);

        // Phương án 3: Cân bằng tải giữa các dây chuyền
        if (lineCapacities.size() > 1 || DEFAULT_LINE_IDS.size() > 1) {
            Map<String, Object> option3Details = buildOptionByLoadBalancing(
                    ordersByProduct, lineCapacities, monthStart, monthEnd);
            BigDecimal option3Hours = calculateOptionTotalHours(option3Details);
            BigDecimal option3Utilization = calculateUtilization(option3Hours, totalMonthlyCapacity);

            MonthlyPlan option3 = createMonthlyPlan(quarterlyPlanId, year, month, 3,
                    option3Details, option3Hours, option3Utilization);
            options.add(option3);
        }

        // 6. Lưu tất cả phương án
        for (MonthlyPlan option : options) {
            monthlyPlanMapper.insert(option);
        }

        log.info("[MonthlyPlan] Đã tạo {} phương án kế hoạch tháng {}/{}",
                options.size(), month, year);

        // 7. Thông báo
        Map<String, Object> notifData = new HashMap<>();
        notifData.put("quarterlyPlanId", quarterlyPlanId);
        notifData.put("year", year);
        notifData.put("month", month);
        notifData.put("optionCount", options.size());
        planningNotificationService.notifyProductionManager(
                NotificationType.PLAN_GENERATED,
                String.format("Đã tạo %d phương án kế hoạch tháng %d/%d, vui lòng xem xét và duyệt",
                        options.size(), month, year),
                notifData);

        return options;
    }

    // ======================== approveMonthlyPlan ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonthlyPlan approveMonthlyPlan(String monthlyPlanId) {
        log.info("[MonthlyPlan] Duyệt kế hoạch tháng: id={}", monthlyPlanId);

        MonthlyPlan plan = monthlyPlanMapper.selectById(monthlyPlanId);
        if (plan == null) {
            throw new IllegalArgumentException("Không tìm thấy kế hoạch tháng: " + monthlyPlanId);
        }

        if ("approved".equals(plan.getStatus())) {
            log.warn("[MonthlyPlan] Kế hoạch tháng {} đã được duyệt trước đó", monthlyPlanId);
            return plan;
        }

        // Duyệt phương án được chọn
        plan.setStatus("approved");
        plan.setApprovedTime(new Date());
        monthlyPlanMapper.updateById(plan);

        // Từ chối các phương án khác cùng tháng, cùng kế hoạch quý
        LambdaUpdateWrapper<MonthlyPlan> rejectWrapper = new LambdaUpdateWrapper<>();
        rejectWrapper.eq(MonthlyPlan::getQuarterlyPlanId, plan.getQuarterlyPlanId())
                .eq(MonthlyPlan::getYear, plan.getYear())
                .eq(MonthlyPlan::getMonth, plan.getMonth())
                .ne(MonthlyPlan::getId, monthlyPlanId)
                .set(MonthlyPlan::getStatus, "rejected");
        monthlyPlanMapper.update(null, rejectWrapper);

        log.info("[MonthlyPlan] Đã duyệt kế hoạch tháng {} (rank={}), từ chối các phương án khác",
                plan.getPlanCode(), plan.getOptionRank());

        return plan;
    }

    // ======================== Private Helper Methods ========================

    /**
     * Lấy đơn hàng có deadline trong khoảng thời gian quý.
     */
    private List<PlanningOrder> getOrdersForQuarter(LocalDate start, LocalDate end) {
        LambdaQueryWrapper<PlanningOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PlanningOrder::getStatus, "pending", "confirmed")
                .eq(PlanningOrder::getValidationStatus, "valid")
                .ge(PlanningOrder::getDeadline, toDate(start))
                .le(PlanningOrder::getDeadline, toDate(end))
                .orderByAsc(PlanningOrder::getDeadline)
                .orderByAsc(PlanningOrder::getReceiptTimestamp);
        return planningOrderMapper.selectList(wrapper);
    }

    /**
     * Lấy đơn hàng có deadline trong khoảng thời gian tháng.
     */
    private List<PlanningOrder> getOrdersForPeriod(LocalDate start, LocalDate end) {
        LambdaQueryWrapper<PlanningOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PlanningOrder::getStatus, "pending", "confirmed")
                .eq(PlanningOrder::getValidationStatus, "valid")
                .ge(PlanningOrder::getDeadline, toDate(start))
                .le(PlanningOrder::getDeadline, toDate(end))
                .orderByAsc(PlanningOrder::getDeadline)
                .orderByAsc(PlanningOrder::getReceiptTimestamp);
        return planningOrderMapper.selectList(wrapper);
    }

    /**
     * Phân loại nhu cầu theo tháng và loại sản phẩm.
     * Trả về: Map<month, Map<productType, totalQuantity>>
     */
    private Map<Integer, Map<String, BigDecimal>> classifyDemandByMonth(
            List<PlanningOrder> orders, LocalDate quarterStart) {
        Map<Integer, Map<String, BigDecimal>> result = new LinkedHashMap<>();

        for (PlanningOrder order : orders) {
            LocalDate deadline = order.getDeadline().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            int month = deadline.getMonthValue();

            result.computeIfAbsent(month, k -> new LinkedHashMap<>())
                    .merge(order.getProductType(), order.getQuantity(), BigDecimal::add);
        }

        return result;
    }

    /**
     * Lấy công suất dây chuyền từ ERP. Nếu thất bại, trả về danh sách rỗng (sẽ dùng cache).
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
            log.error("[QuarterlyPlan] Lỗi lấy dữ liệu công suất từ ERP: {}", e.getMessage(), e);
            // Trả về rỗng để kích hoạt fallback sang cached data
            return Collections.emptyList();
        }
        return capacities;
    }

    /**
     * Tính tổng giờ nhu cầu sản xuất dựa trên cycle time tiêu chuẩn.
     */
    private BigDecimal calculateTotalDemandHours(Map<String, BigDecimal> demandByProduct) {
        BigDecimal totalHours = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : demandByProduct.entrySet()) {
            // Giờ = số lượng × cycle time tiêu chuẩn
            totalHours = totalHours.add(entry.getValue().multiply(DEFAULT_CYCLE_TIME_HOURS));
        }
        return totalHours;
    }

    /**
     * Tính tổng công suất khả dụng trong tháng (tất cả dây chuyền).
     */
    private BigDecimal calculateMonthlyCapacity(int year, int month,
                                                 List<ProductionLineCapacity> lineCapacities) {
        if (!lineCapacities.isEmpty()) {
            // Sử dụng dữ liệu live
            BigDecimal total = BigDecimal.ZERO;
            for (ProductionLineCapacity lc : lineCapacities) {
                if (lc.getTotalAvailableHours() != null) {
                    // Chia tỷ lệ cho tháng cụ thể (nếu capacity là cho cả quý)
                    total = total.add(lc.getTotalAvailableHours().divide(
                            new BigDecimal("3"), 2, RoundingMode.HALF_UP));
                }
            }
            return total;
        }

        // Fallback: tính dựa trên số ngày làm việc × giờ/ngày × số dây chuyền
        int workingDays = getWorkingDaysInMonth(year, month);
        return STANDARD_HOURS_PER_DAY
                .multiply(new BigDecimal(workingDays))
                .multiply(new BigDecimal(DEFAULT_LINE_IDS.size()));
    }

    /**
     * Tạo ít nhất 2 phương án thay thế khi nhu cầu vượt công suất.
     * Phương án 1: Phân bổ lại tải sang tháng liền kề
     * Phương án 2: Lên lịch tăng ca (overtime)
     */
    private List<Map<String, Object>> generateAlternatives(
            int month, Map<String, BigDecimal> demand,
            BigDecimal demandHours, BigDecimal capacityHours, BigDecimal gap) {

        List<Map<String, Object>> alternatives = new ArrayList<>();

        // Phương án 1: Phân bổ lại tải sang tháng liền kề (load redistribution)
        Map<String, Object> alt1 = new LinkedHashMap<>();
        alt1.put("type", "load_redistribution");
        alt1.put("description", "Phân bổ lại tải sản xuất sang tháng liền kề");
        alt1.put("redistributeHours", gap);
        alt1.put("targetMonth", month > 1 ? month - 1 : month + 1);
        alt1.put("capacityGapResolved", gap);
        alt1.put("deliveryImpact", "Một số đơn hàng có thể bị trễ 1-2 tuần");
        alternatives.add(alt1);

        // Phương án 2: Tăng ca (overtime scheduling)
        BigDecimal overtimeHoursPerDay = new BigDecimal("4"); // 4 giờ tăng ca/ngày
        BigDecimal overtimeDaysNeeded = gap.divide(
                overtimeHoursPerDay.multiply(new BigDecimal(DEFAULT_LINE_IDS.size())),
                0, RoundingMode.CEILING);

        Map<String, Object> alt2 = new LinkedHashMap<>();
        alt2.put("type", "overtime_scheduling");
        alt2.put("description", "Lên lịch tăng ca để bù đắp công suất thiếu hụt");
        alt2.put("overtimeHoursPerDay", overtimeHoursPerDay);
        alt2.put("overtimeDaysNeeded", overtimeDaysNeeded);
        alt2.put("capacityGapResolved", gap);
        alt2.put("deliveryImpact", "Không ảnh hưởng deadline nếu tăng ca đủ");
        alt2.put("costImpact", "Chi phí tăng ca ước tính tăng 50%");
        alternatives.add(alt2);

        return alternatives;
    }

    /**
     * Phương án 1: Phân bổ theo ưu tiên deadline (deadline sớm nhất được gán trước).
     */
    private Map<String, Object> buildOptionByDeadlinePriority(
            Map<String, List<PlanningOrder>> ordersByProduct,
            List<ProductionLineCapacity> lineCapacities,
            LocalDate monthStart, LocalDate monthEnd) {

        Map<String, Object> details = new LinkedHashMap<>();
        List<Map<String, Object>> assignments = new ArrayList<>();

        // Gom tất cả đơn hàng, sắp xếp theo deadline
        List<PlanningOrder> allOrders = ordersByProduct.values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(PlanningOrder::getDeadline)
                        .thenComparing(PlanningOrder::getReceiptTimestamp))
                .collect(Collectors.toList());

        List<String> lineIds = getAvailableLineIds(lineCapacities);
        int lineIndex = 0;

        for (PlanningOrder order : allOrders) {
            String assignedLine = lineIds.get(lineIndex % lineIds.size());
            LocalDate deadline = order.getDeadline().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            // Ngày bắt đầu = deadline - thời gian sản xuất ước tính
            BigDecimal productionDays = order.getQuantity().multiply(DEFAULT_CYCLE_TIME_HOURS)
                    .divide(STANDARD_HOURS_PER_DAY, 0, RoundingMode.CEILING);
            LocalDate startDate = deadline.minusDays(productionDays.longValue());
            if (startDate.isBefore(monthStart)) {
                startDate = monthStart;
            }

            Map<String, Object> assignment = new LinkedHashMap<>();
            assignment.put("orderId", order.getId());
            assignment.put("productType", order.getProductType());
            assignment.put("quantity", order.getQuantity());
            assignment.put("assignedLine", assignedLine);
            assignment.put("startDate", startDate.toString());
            assignment.put("expectedCompletion", deadline.toString());
            assignments.add(assignment);

            lineIndex++;
        }

        details.put("strategy", "deadline_priority");
        details.put("description", "Ưu tiên theo deadline sớm nhất");
        details.put("assignments", assignments);
        return details;
    }

    /**
     * Phương án 2: Tối ưu hóa sử dụng dây chuyền (nhóm sản phẩm cùng loại trên cùng dây chuyền).
     */
    private Map<String, Object> buildOptionByLineOptimization(
            Map<String, List<PlanningOrder>> ordersByProduct,
            List<ProductionLineCapacity> lineCapacities,
            LocalDate monthStart, LocalDate monthEnd) {

        Map<String, Object> details = new LinkedHashMap<>();
        List<Map<String, Object>> assignments = new ArrayList<>();

        List<String> lineIds = getAvailableLineIds(lineCapacities);
        int lineIndex = 0;

        // Gán mỗi loại sản phẩm cho một dây chuyền cố định (giảm changeover)
        for (Map.Entry<String, List<PlanningOrder>> entry : ordersByProduct.entrySet()) {
            String productType = entry.getKey();
            List<PlanningOrder> orders = entry.getValue();
            String assignedLine = lineIds.get(lineIndex % lineIds.size());

            LocalDate currentStart = monthStart;
            for (PlanningOrder order : orders) {
                BigDecimal productionDays = order.getQuantity().multiply(DEFAULT_CYCLE_TIME_HOURS)
                        .divide(STANDARD_HOURS_PER_DAY, 0, RoundingMode.CEILING);
                LocalDate endDate = currentStart.plusDays(productionDays.longValue());
                if (endDate.isAfter(monthEnd)) {
                    endDate = monthEnd;
                }

                Map<String, Object> assignment = new LinkedHashMap<>();
                assignment.put("orderId", order.getId());
                assignment.put("productType", productType);
                assignment.put("quantity", order.getQuantity());
                assignment.put("assignedLine", assignedLine);
                assignment.put("startDate", currentStart.toString());
                assignment.put("expectedCompletion", endDate.toString());
                assignments.add(assignment);

                currentStart = endDate.plusDays(1);
            }
            lineIndex++;
        }

        details.put("strategy", "line_optimization");
        details.put("description", "Tối ưu hóa dây chuyền - nhóm sản phẩm cùng loại");
        details.put("assignments", assignments);
        return details;
    }

    /**
     * Phương án 3: Cân bằng tải giữa các dây chuyền.
     */
    private Map<String, Object> buildOptionByLoadBalancing(
            Map<String, List<PlanningOrder>> ordersByProduct,
            List<ProductionLineCapacity> lineCapacities,
            LocalDate monthStart, LocalDate monthEnd) {

        Map<String, Object> details = new LinkedHashMap<>();
        List<Map<String, Object>> assignments = new ArrayList<>();

        List<String> lineIds = getAvailableLineIds(lineCapacities);
        // Theo dõi tải mỗi dây chuyền
        Map<String, BigDecimal> lineLoad = new LinkedHashMap<>();
        Map<String, LocalDate> lineNextAvailable = new LinkedHashMap<>();
        for (String lineId : lineIds) {
            lineLoad.put(lineId, BigDecimal.ZERO);
            lineNextAvailable.put(lineId, monthStart);
        }

        // Gom tất cả đơn hàng, sắp xếp theo số lượng giảm dần (đơn lớn gán trước)
        List<PlanningOrder> allOrders = ordersByProduct.values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(PlanningOrder::getQuantity).reversed())
                .collect(Collectors.toList());

        for (PlanningOrder order : allOrders) {
            // Tìm dây chuyền có tải thấp nhất
            String bestLine = lineIds.get(0);
            BigDecimal minLoad = lineLoad.get(bestLine);
            for (String lineId : lineIds) {
                if (lineLoad.get(lineId).compareTo(minLoad) < 0) {
                    minLoad = lineLoad.get(lineId);
                    bestLine = lineId;
                }
            }

            BigDecimal productionHours = order.getQuantity().multiply(DEFAULT_CYCLE_TIME_HOURS);
            BigDecimal productionDays = productionHours
                    .divide(STANDARD_HOURS_PER_DAY, 0, RoundingMode.CEILING);

            LocalDate startDate = lineNextAvailable.get(bestLine);
            LocalDate endDate = startDate.plusDays(productionDays.longValue());
            if (endDate.isAfter(monthEnd)) {
                endDate = monthEnd;
            }

            Map<String, Object> assignment = new LinkedHashMap<>();
            assignment.put("orderId", order.getId());
            assignment.put("productType", order.getProductType());
            assignment.put("quantity", order.getQuantity());
            assignment.put("assignedLine", bestLine);
            assignment.put("startDate", startDate.toString());
            assignment.put("expectedCompletion", endDate.toString());
            assignments.add(assignment);

            lineLoad.put(bestLine, lineLoad.get(bestLine).add(productionHours));
            lineNextAvailable.put(bestLine, endDate.plusDays(1));
        }

        details.put("strategy", "load_balancing");
        details.put("description", "Cân bằng tải giữa các dây chuyền");
        details.put("assignments", assignments);
        return details;
    }

    /**
     * Tạo đối tượng MonthlyPlan.
     */
    private MonthlyPlan createMonthlyPlan(String quarterlyPlanId, int year, int month,
                                           int optionRank, Map<String, Object> planDetails,
                                           BigDecimal totalHours, BigDecimal capacityUtilization) {
        MonthlyPlan plan = new MonthlyPlan();
        plan.setPlanCode(generateMonthlyPlanCode(year, month, optionRank));
        plan.setQuarterlyPlanId(quarterlyPlanId);
        plan.setYear(year);
        plan.setMonth(month);
        plan.setOptionRank(optionRank);
        plan.setPlanDetails(toJson(planDetails));
        plan.setTotalHours(totalHours);
        plan.setCapacityUtilization(capacityUtilization);
        plan.setStatus("suggested");
        return plan;
    }

    /**
     * Tính tổng giờ sản xuất từ chi tiết phương án.
     */
    @SuppressWarnings("unchecked")
    private BigDecimal calculateOptionTotalHours(Map<String, Object> optionDetails) {
        BigDecimal totalHours = BigDecimal.ZERO;
        List<Map<String, Object>> assignments =
                (List<Map<String, Object>>) optionDetails.get("assignments");
        if (assignments != null) {
            for (Map<String, Object> assignment : assignments) {
                Object qty = assignment.get("quantity");
                BigDecimal quantity = qty instanceof BigDecimal ? (BigDecimal) qty
                        : new BigDecimal(qty.toString());
                totalHours = totalHours.add(quantity.multiply(DEFAULT_CYCLE_TIME_HOURS));
            }
        }
        return totalHours;
    }

    /**
     * Tính tỷ lệ sử dụng công suất (%).
     */
    private BigDecimal calculateUtilization(BigDecimal usedHours, BigDecimal totalCapacity) {
        if (totalCapacity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return usedHours.multiply(new BigDecimal("100"))
                .divide(totalCapacity, 2, RoundingMode.HALF_UP);
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
        return DEFAULT_LINE_IDS;
    }

    /**
     * Tính số ngày làm việc trong tháng (ước tính: tổng ngày - weekends).
     */
    private int getWorkingDaysInMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        int totalDays = ym.lengthOfMonth();
        int workingDays = 0;
        for (int day = 1; day <= totalDays; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            if (date.getDayOfWeek().getValue() <= 5) { // Mon-Fri
                workingDays++;
            }
        }
        return workingDays;
    }

    /**
     * Lấy ngày bắt đầu quý.
     */
    private LocalDate getQuarterStartDate(int year, int quarter) {
        int startMonth = (quarter - 1) * 3 + 1;
        return LocalDate.of(year, startMonth, 1);
    }

    /**
     * Lấy ngày kết thúc quý.
     */
    private LocalDate getQuarterEndDate(int year, int quarter) {
        int endMonth = quarter * 3;
        return YearMonth.of(year, endMonth).atEndOfMonth();
    }

    /**
     * Tạo mã kế hoạch quý: QPyyyyQN (ví dụ: QP2026Q1).
     */
    private String generateQuarterlyPlanCode(int year, int quarter) {
        return String.format("QP%dQ%d", year, quarter);
    }

    /**
     * Tạo mã kế hoạch tháng: MPyyyyMMNNN (ví dụ: MP20260100 1).
     */
    private String generateMonthlyPlanCode(int year, int month, int optionRank) {
        return String.format("MP%d%02d%03d", year, month, optionRank);
    }

    /**
     * Chuyển LocalDate sang java.util.Date.
     */
    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Chuyển đổi object sang JSON string.
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("[QuarterlyPlan] Lỗi chuyển đổi JSON: {}", e.getMessage(), e);
            return "{}";
        }
    }
}
