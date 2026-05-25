package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.entity.OptimizationScore;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.mapper.OptimizationScoreMapper;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.PlanOptimizationService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.QualitySyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Triển khai tối ưu hóa kế hoạch sản xuất
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Slf4j
@Service
public class PlanOptimizationServiceImpl implements PlanOptimizationService {

    // ==================== Trọng số mặc định (tổng = 1.0) ====================
    private static final BigDecimal WEIGHT_DEADLINE = new BigDecimal("0.40");
    private static final BigDecimal WEIGHT_UTILIZATION = new BigDecimal("0.25");
    private static final BigDecimal WEIGHT_MATERIAL = new BigDecimal("0.20");
    private static final BigDecimal WEIGHT_PRIORITY = new BigDecimal("0.15");

    private static final BigDecimal SCORE_MIN = BigDecimal.ZERO;
    private static final BigDecimal SCORE_MAX = new BigDecimal("100");

    /** Mục tiêu sử dụng dây chuyền: 70-90% */
    private static final BigDecimal UTILIZATION_TARGET_LOW = new BigDecimal("70");
    private static final BigDecimal UTILIZATION_TARGET_HIGH = new BigDecimal("90");

    @Resource
    private WeeklyPlanMapper weeklyPlanMapper;

    @Resource
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;

    @Resource
    private OptimizationScoreMapper optimizationScoreMapper;

    @Resource
    private PlanningOrderMapper planningOrderMapper;

    @Resource
    private QualitySyncService qualitySyncService;

    @Resource
    private PlanningNotificationService planningNotificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OptimizationScore optimizeWeeklyPlan(String weeklyPlanId) {
        log.info("[Optimization] Bắt đầu tối ưu hóa kế hoạch tuần: {}", weeklyPlanId);

        // 1. Load weekly plan
        WeeklyPlan weeklyPlan = weeklyPlanMapper.selectById(weeklyPlanId);
        if (weeklyPlan == null) {
            throw new IllegalArgumentException("Không tìm thấy kế hoạch tuần: " + weeklyPlanId);
        }

        // 2. Load batches cho kế hoạch tuần
        LambdaQueryWrapper<WeeklyPlanBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId);
        List<WeeklyPlanBatch> batches = weeklyPlanBatchMapper.selectList(batchWrapper);

        if (batches == null || batches.isEmpty()) {
            log.warn("[Optimization] Kế hoạch tuần {} không có batch nào", weeklyPlanId);
            return saveEmptyScore(weeklyPlan);
        }

        // 3. Tính các điểm thành phần
        BigDecimal deadlineScore = calculateDeadlineScore(batches);
        BigDecimal utilizationScore = calculateUtilizationScore(batches, weeklyPlan);
        BigDecimal materialScore = calculateMaterialScore(batches);
        BigDecimal priorityScore = calculatePriorityScore(batches);

        // 4. Tính tổng điểm = deadline*0.40 + utilization*0.25 + material*0.20 + priority*0.15
        BigDecimal totalScore = deadlineScore.multiply(WEIGHT_DEADLINE)
                .add(utilizationScore.multiply(WEIGHT_UTILIZATION))
                .add(materialScore.multiply(WEIGHT_MATERIAL))
                .add(priorityScore.multiply(WEIGHT_PRIORITY))
                .setScale(2, RoundingMode.HALF_UP);

        // 5. Bound total_score within [0, 100]
        totalScore = boundScore(totalScore);

        // 6. Kiểm tra dữ liệu lịch sử có được sử dụng không
        boolean historicalDataUsed = checkHistoricalDataUsed(batches);

        // 7. Xác định vi phạm ràng buộc
        List<Map<String, Object>> violations = identifyConstraintViolations(batches, weeklyPlan);

        // 8. Lưu OptimizationScore record
        OptimizationScore score = new OptimizationScore();
        score.setWeeklyPlanId(weeklyPlanId);
        score.setTotalScore(totalScore);
        score.setDeadlineScore(deadlineScore);
        score.setDeadlineWeight(WEIGHT_DEADLINE);
        score.setUtilizationScore(utilizationScore);
        score.setUtilizationWeight(WEIGHT_UTILIZATION);
        score.setMaterialScore(materialScore);
        score.setMaterialWeight(WEIGHT_MATERIAL);
        score.setPriorityScore(priorityScore);
        score.setPriorityWeight(WEIGHT_PRIORITY);
        score.setHistoricalDataUsed(historicalDataUsed ? 1 : 0);
        score.setCreateTime(new Date());

        // Serialize constraint violations to JSON
        if (!violations.isEmpty()) {
            try {
                score.setConstraintViolations(objectMapper.writeValueAsString(violations));
            } catch (Exception e) {
                log.warn("[Optimization] Không thể serialize constraint violations: {}", e.getMessage());
                score.setConstraintViolations("[]");
            }
        }

        optimizationScoreMapper.insert(score);

        // 9. Cập nhật optimization_score trên WeeklyPlan
        weeklyPlan.setOptimizationScore(totalScore);
        weeklyPlanMapper.updateById(weeklyPlan);

        // 10. Nếu không dùng dữ liệu lịch sử, thông báo cho manager
        if (!historicalDataUsed) {
            notifyEstimationUsed(weeklyPlan, score);
        }

        log.info("[Optimization] Hoàn thành tối ưu hóa kế hoạch tuần {}: totalScore={}, " +
                        "deadline={}, utilization={}, material={}, priority={}, violations={}",
                weeklyPlanId, totalScore, deadlineScore, utilizationScore,
                materialScore, priorityScore, violations.size());

        return score;
    }

    @Override
    public List<WeeklyPlan> getTopRankedPlans(String monthlyPlanId, int topN) {
        log.info("[Optimization] Lấy top {} kế hoạch tuần cho monthly plan: {}", topN, monthlyPlanId);

        // Query weekly plans cho monthly plan, sắp xếp theo optimization_score DESC
        LambdaQueryWrapper<WeeklyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlan::getMonthlyPlanId, monthlyPlanId)
                .isNotNull(WeeklyPlan::getOptimizationScore)
                .orderByDesc(WeeklyPlan::getOptimizationScore)
                .last("LIMIT " + topN);

        List<WeeklyPlan> topPlans = weeklyPlanMapper.selectList(wrapper);

        // Nếu không có plan nào thỏa mãn tất cả deadline, tìm plan ít vi phạm nhất
        if (topPlans.isEmpty()) {
            topPlans = findPlansWithFewestViolations(monthlyPlanId, topN);
        }

        return topPlans;
    }

    // ==================== Tính điểm thành phần ====================

    /**
     * Tính deadline_score: % batch hoàn thành trước deadline đơn hàng.
     * Score = (số batch đúng hạn / tổng batch) * 100
     */
    private BigDecimal calculateDeadlineScore(List<WeeklyPlanBatch> batches) {
        if (batches.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Lấy tất cả order IDs liên quan
        Set<String> orderIds = batches.stream()
                .map(WeeklyPlanBatch::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (orderIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Load orders để lấy deadline
        List<PlanningOrder> orders = planningOrderMapper.selectBatchIds(orderIds);
        Map<String, Date> orderDeadlines = orders.stream()
                .collect(Collectors.toMap(PlanningOrder::getId, PlanningOrder::getDeadline,
                        (a, b) -> a));

        // Đếm batch hoàn thành trước deadline
        long onTimeBatches = batches.stream()
                .filter(batch -> {
                    Date deadline = orderDeadlines.get(batch.getOrderId());
                    if (deadline == null || batch.getPlannedEnd() == null) {
                        return false;
                    }
                    return !batch.getPlannedEnd().after(deadline);
                })
                .count();

        return BigDecimal.valueOf(onTimeBatches)
                .multiply(SCORE_MAX)
                .divide(BigDecimal.valueOf(batches.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Tính utilization_score: trung bình sử dụng dây chuyền (mục tiêu 70-90%).
     * Tính dựa trên tổng thời gian sản xuất / tổng thời gian khả dụng trên mỗi line.
     * Score 100 khi utilization nằm trong [70%, 90%], giảm dần khi ngoài khoảng.
     */
    private BigDecimal calculateUtilizationScore(List<WeeklyPlanBatch> batches, WeeklyPlan weeklyPlan) {
        if (batches.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Nhóm batch theo production_line_id
        Map<String, List<WeeklyPlanBatch>> batchesByLine = batches.stream()
                .filter(b -> b.getProductionLineId() != null)
                .collect(Collectors.groupingBy(WeeklyPlanBatch::getProductionLineId));

        if (batchesByLine.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Tính thời gian khả dụng trong tuần (giờ)
        long weekDurationHours = 0;
        if (weeklyPlan.getStartDate() != null && weeklyPlan.getEndDate() != null) {
            long diffMs = weeklyPlan.getEndDate().getTime() - weeklyPlan.getStartDate().getTime();
            weekDurationHours = diffMs / (1000 * 60 * 60);
        }
        if (weekDurationHours <= 0) {
            weekDurationHours = 7 * 24; // Mặc định 1 tuần = 168 giờ
        }

        BigDecimal totalUtilizationScore = BigDecimal.ZERO;
        int lineCount = batchesByLine.size();

        for (Map.Entry<String, List<WeeklyPlanBatch>> entry : batchesByLine.entrySet()) {
            List<WeeklyPlanBatch> lineBatches = entry.getValue();

            // Tính tổng thời gian sản xuất trên line (giờ)
            long totalProductionHours = 0;
            for (WeeklyPlanBatch batch : lineBatches) {
                if (batch.getPlannedStart() != null && batch.getPlannedEnd() != null) {
                    long batchMs = batch.getPlannedEnd().getTime() - batch.getPlannedStart().getTime();
                    totalProductionHours += batchMs / (1000 * 60 * 60);
                }
            }

            // Tính utilization % cho line
            BigDecimal utilization = BigDecimal.valueOf(totalProductionHours)
                    .multiply(SCORE_MAX)
                    .divide(BigDecimal.valueOf(weekDurationHours), 2, RoundingMode.HALF_UP);

            // Score dựa trên khoảng cách đến target [70, 90]
            BigDecimal lineScore = calculateUtilizationLineScore(utilization);
            totalUtilizationScore = totalUtilizationScore.add(lineScore);
        }

        // Trung bình score trên tất cả lines
        return totalUtilizationScore.divide(BigDecimal.valueOf(lineCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * Tính score cho utilization của một line.
     * 100 nếu trong [70%, 90%], giảm tuyến tính khi ngoài khoảng.
     */
    private BigDecimal calculateUtilizationLineScore(BigDecimal utilization) {
        if (utilization.compareTo(UTILIZATION_TARGET_LOW) >= 0
                && utilization.compareTo(UTILIZATION_TARGET_HIGH) <= 0) {
            // Trong khoảng mục tiêu → score = 100
            return SCORE_MAX;
        } else if (utilization.compareTo(UTILIZATION_TARGET_LOW) < 0) {
            // Dưới 70%: score giảm tuyến tính (0% → 0, 70% → 100)
            return utilization.multiply(SCORE_MAX)
                    .divide(UTILIZATION_TARGET_LOW, 2, RoundingMode.HALF_UP);
        } else {
            // Trên 90%: score giảm tuyến tính (90% → 100, 100% → 0)
            BigDecimal overTarget = utilization.subtract(UTILIZATION_TARGET_HIGH);
            BigDecimal maxOver = SCORE_MAX.subtract(UTILIZATION_TARGET_HIGH); // 10
            BigDecimal penalty = overTarget.multiply(SCORE_MAX)
                    .divide(maxOver, 2, RoundingMode.HALF_UP);
            BigDecimal result = SCORE_MAX.subtract(penalty);
            return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
        }
    }

    /**
     * Tính material_score: % batch có material_status='verified'.
     * Score = (số batch verified / tổng batch) * 100
     */
    private BigDecimal calculateMaterialScore(List<WeeklyPlanBatch> batches) {
        if (batches.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long verifiedCount = batches.stream()
                .filter(b -> "verified".equals(b.getMaterialStatus()))
                .count();

        return BigDecimal.valueOf(verifiedCount)
                .multiply(SCORE_MAX)
                .divide(BigDecimal.valueOf(batches.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Tính priority_score: tổng có trọng số dựa trên priority_rank của đơn hàng.
     * Rank cao hơn (số nhỏ hơn) = score cao hơn.
     * Score = trung bình (100 - (rank - 1) * penalty) cho mỗi batch, bounded [0, 100].
     */
    private BigDecimal calculatePriorityScore(List<WeeklyPlanBatch> batches) {
        if (batches.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Lấy order IDs
        Set<String> orderIds = batches.stream()
                .map(WeeklyPlanBatch::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (orderIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Load orders để lấy priority_rank
        List<PlanningOrder> orders = planningOrderMapper.selectBatchIds(orderIds);
        Map<String, Integer> orderRanks = orders.stream()
                .filter(o -> o.getPriorityRank() != null)
                .collect(Collectors.toMap(PlanningOrder::getId, PlanningOrder::getPriorityRank,
                        (a, b) -> a));

        if (orderRanks.isEmpty()) {
            // Không có priority_rank → score mặc định 50
            return new BigDecimal("50.00");
        }

        // Tìm max rank để normalize
        int maxRank = orderRanks.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(1);

        // Tính score cho mỗi batch dựa trên rank (rank 1 = score 100, rank max = score gần 0)
        BigDecimal totalPriorityScore = BigDecimal.ZERO;
        int scoredBatches = 0;

        for (WeeklyPlanBatch batch : batches) {
            Integer rank = orderRanks.get(batch.getOrderId());
            if (rank != null && maxRank > 0) {
                // Score = 100 * (1 - (rank - 1) / maxRank)
                BigDecimal batchScore = SCORE_MAX.multiply(
                        BigDecimal.ONE.subtract(
                                BigDecimal.valueOf(rank - 1)
                                        .divide(BigDecimal.valueOf(maxRank), 4, RoundingMode.HALF_UP)
                        )
                ).setScale(2, RoundingMode.HALF_UP);
                batchScore = boundScore(batchScore);
                totalPriorityScore = totalPriorityScore.add(batchScore);
                scoredBatches++;
            }
        }

        if (scoredBatches == 0) {
            return new BigDecimal("50.00");
        }

        return totalPriorityScore.divide(BigDecimal.valueOf(scoredBatches), 2, RoundingMode.HALF_UP);
    }

    // ==================== Dữ liệu lịch sử & Vi phạm ràng buộc ====================

    /**
     * Kiểm tra xem dữ liệu lịch sử 90 ngày có được sử dụng hay không.
     * Dựa trên QualitySyncService: nếu có yield rate 90 ngày cho ít nhất 1 batch → true.
     * Nếu không có → sử dụng BOM-based standard cycle times (estimation).
     */
    private boolean checkHistoricalDataUsed(List<WeeklyPlanBatch> batches) {
        for (WeeklyPlanBatch batch : batches) {
            if (batch.getProductType() != null && batch.getProductionLineId() != null) {
                BigDecimal yieldRate = qualitySyncService.getYieldRate90Day(
                        batch.getProductType(), batch.getProductionLineId());
                if (yieldRate != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Xác định các vi phạm ràng buộc: deadline misses, over-capacity, material shortages.
     */
    private List<Map<String, Object>> identifyConstraintViolations(
            List<WeeklyPlanBatch> batches, WeeklyPlan weeklyPlan) {

        List<Map<String, Object>> violations = new ArrayList<>();

        // Lấy order deadlines
        Set<String> orderIds = batches.stream()
                .map(WeeklyPlanBatch::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Date> orderDeadlines = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<PlanningOrder> orders = planningOrderMapper.selectBatchIds(orderIds);
            orderDeadlines = orders.stream()
                    .collect(Collectors.toMap(PlanningOrder::getId, PlanningOrder::getDeadline,
                            (a, b) -> a));
        }

        for (WeeklyPlanBatch batch : batches) {
            // 1. Deadline misses
            Date deadline = orderDeadlines.get(batch.getOrderId());
            if (deadline != null && batch.getPlannedEnd() != null
                    && batch.getPlannedEnd().after(deadline)) {
                Map<String, Object> violation = new HashMap<>();
                violation.put("type", "deadline_miss");
                violation.put("batchId", batch.getId());
                violation.put("orderId", batch.getOrderId());
                violation.put("plannedEnd", batch.getPlannedEnd());
                violation.put("deadline", deadline);
                long delayMs = batch.getPlannedEnd().getTime() - deadline.getTime();
                violation.put("delayDays", delayMs / (1000 * 60 * 60 * 24));
                violations.add(violation);
            }

            // 2. Material shortages
            if ("shortage".equals(batch.getMaterialStatus())) {
                Map<String, Object> violation = new HashMap<>();
                violation.put("type", "material_shortage");
                violation.put("batchId", batch.getId());
                violation.put("orderId", batch.getOrderId());
                violation.put("productType", batch.getProductType());
                violations.add(violation);
            }
        }

        // 3. Over-capacity (utilization > 90% trên bất kỳ line nào)
        Map<String, List<WeeklyPlanBatch>> batchesByLine = batches.stream()
                .filter(b -> b.getProductionLineId() != null)
                .collect(Collectors.groupingBy(WeeklyPlanBatch::getProductionLineId));

        long weekDurationHours = 0;
        if (weeklyPlan.getStartDate() != null && weeklyPlan.getEndDate() != null) {
            long diffMs = weeklyPlan.getEndDate().getTime() - weeklyPlan.getStartDate().getTime();
            weekDurationHours = diffMs / (1000 * 60 * 60);
        }
        if (weekDurationHours <= 0) {
            weekDurationHours = 7 * 24;
        }

        for (Map.Entry<String, List<WeeklyPlanBatch>> entry : batchesByLine.entrySet()) {
            long totalProductionHours = 0;
            for (WeeklyPlanBatch batch : entry.getValue()) {
                if (batch.getPlannedStart() != null && batch.getPlannedEnd() != null) {
                    long batchMs = batch.getPlannedEnd().getTime() - batch.getPlannedStart().getTime();
                    totalProductionHours += batchMs / (1000 * 60 * 60);
                }
            }

            BigDecimal utilization = BigDecimal.valueOf(totalProductionHours)
                    .multiply(SCORE_MAX)
                    .divide(BigDecimal.valueOf(weekDurationHours), 2, RoundingMode.HALF_UP);

            if (utilization.compareTo(UTILIZATION_TARGET_HIGH) > 0) {
                Map<String, Object> violation = new HashMap<>();
                violation.put("type", "over_capacity");
                violation.put("lineId", entry.getKey());
                violation.put("utilization", utilization);
                violation.put("threshold", UTILIZATION_TARGET_HIGH);
                violations.add(violation);
            }
        }

        return violations;
    }

    // ==================== Helper Methods ====================

    /**
     * Tìm kế hoạch tuần có ít vi phạm nhất khi không có plan nào thỏa mãn tất cả deadline.
     * Bao gồm delay estimates cho mỗi vi phạm.
     */
    private List<WeeklyPlan> findPlansWithFewestViolations(String monthlyPlanId, int topN) {
        log.info("[Optimization] Không có plan thỏa mãn tất cả deadline, tìm plan ít vi phạm nhất");

        // Lấy tất cả weekly plans cho monthly plan
        LambdaQueryWrapper<WeeklyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlan::getMonthlyPlanId, monthlyPlanId)
                .orderByDesc(WeeklyPlan::getOptimizationScore);

        List<WeeklyPlan> allPlans = weeklyPlanMapper.selectList(wrapper);

        if (allPlans.isEmpty()) {
            return Collections.emptyList();
        }

        // Sắp xếp theo số vi phạm (ít nhất trước), sau đó theo score
        List<WeeklyPlan> sortedPlans = allPlans.stream()
                .sorted((p1, p2) -> {
                    int v1 = countViolations(p1.getId());
                    int v2 = countViolations(p2.getId());
                    if (v1 != v2) {
                        return Integer.compare(v1, v2); // Ít vi phạm trước
                    }
                    // Nếu cùng số vi phạm, ưu tiên score cao hơn
                    BigDecimal s1 = p1.getOptimizationScore() != null ? p1.getOptimizationScore() : BigDecimal.ZERO;
                    BigDecimal s2 = p2.getOptimizationScore() != null ? p2.getOptimizationScore() : BigDecimal.ZERO;
                    return s2.compareTo(s1);
                })
                .limit(topN)
                .collect(Collectors.toList());

        // Thông báo cho manager về deadline violations
        if (!sortedPlans.isEmpty()) {
            notifyDeadlineViolations(sortedPlans.get(0));
        }

        return sortedPlans;
    }

    /**
     * Đếm số vi phạm ràng buộc cho một weekly plan.
     */
    private int countViolations(String weeklyPlanId) {
        LambdaQueryWrapper<OptimizationScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OptimizationScore::getWeeklyPlanId, weeklyPlanId);
        OptimizationScore score = optimizationScoreMapper.selectOne(wrapper);

        if (score == null || score.getConstraintViolations() == null
                || score.getConstraintViolations().isEmpty()) {
            return 0;
        }

        try {
            List<?> violations = objectMapper.readValue(score.getConstraintViolations(), List.class);
            return violations.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Lưu score rỗng khi plan không có batch.
     */
    private OptimizationScore saveEmptyScore(WeeklyPlan weeklyPlan) {
        OptimizationScore score = new OptimizationScore();
        score.setWeeklyPlanId(weeklyPlan.getId());
        score.setTotalScore(BigDecimal.ZERO);
        score.setDeadlineScore(BigDecimal.ZERO);
        score.setDeadlineWeight(WEIGHT_DEADLINE);
        score.setUtilizationScore(BigDecimal.ZERO);
        score.setUtilizationWeight(WEIGHT_UTILIZATION);
        score.setMaterialScore(BigDecimal.ZERO);
        score.setMaterialWeight(WEIGHT_MATERIAL);
        score.setPriorityScore(BigDecimal.ZERO);
        score.setPriorityWeight(WEIGHT_PRIORITY);
        score.setHistoricalDataUsed(0);
        score.setCreateTime(new Date());
        optimizationScoreMapper.insert(score);

        weeklyPlan.setOptimizationScore(BigDecimal.ZERO);
        weeklyPlanMapper.updateById(weeklyPlan);

        return score;
    }

    /**
     * Bound score trong khoảng [0, 100].
     */
    private BigDecimal boundScore(BigDecimal score) {
        if (score.compareTo(SCORE_MIN) < 0) {
            return SCORE_MIN;
        }
        if (score.compareTo(SCORE_MAX) > 0) {
            return SCORE_MAX;
        }
        return score;
    }

    /**
     * Thông báo cho manager khi sử dụng estimation thay vì dữ liệu lịch sử.
     */
    private void notifyEstimationUsed(WeeklyPlan weeklyPlan, OptimizationScore score) {
        Map<String, Object> data = new HashMap<>();
        data.put("weeklyPlanId", weeklyPlan.getId());
        data.put("planCode", weeklyPlan.getPlanCode());
        data.put("totalScore", score.getTotalScore());
        data.put("estimationNote", "Sử dụng BOM-based standard cycle times do không có dữ liệu lịch sử 90 ngày");

        planningNotificationService.notifyProductionManager(
                NotificationType.PLAN_GENERATED,
                String.format("Kế hoạch tuần %s đã được tối ưu (score: %s). " +
                                "Lưu ý: Sử dụng ước tính từ BOM do không có dữ liệu lịch sử 90 ngày.",
                        weeklyPlan.getPlanCode(), score.getTotalScore()),
                data
        );
    }

    /**
     * Thông báo cho manager khi không có plan nào thỏa mãn tất cả deadline.
     * Bao gồm delay estimates.
     */
    private void notifyDeadlineViolations(WeeklyPlan bestPlan) {
        // Lấy optimization score để lấy violations
        LambdaQueryWrapper<OptimizationScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OptimizationScore::getWeeklyPlanId, bestPlan.getId());
        OptimizationScore score = optimizationScoreMapper.selectOne(wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("weeklyPlanId", bestPlan.getId());
        data.put("planCode", bestPlan.getPlanCode());
        data.put("optimizationScore", bestPlan.getOptimizationScore());

        if (score != null && score.getConstraintViolations() != null) {
            data.put("constraintViolations", score.getConstraintViolations());
            try {
                List<?> violations = objectMapper.readValue(score.getConstraintViolations(), List.class);
                long deadlineMisses = violations.stream()
                        .filter(v -> v instanceof Map && "deadline_miss".equals(((Map<?, ?>) v).get("type")))
                        .count();
                data.put("deadlineMissCount", deadlineMisses);
            } catch (Exception e) {
                log.warn("[Optimization] Không thể parse constraint violations: {}", e.getMessage());
            }
        }

        planningNotificationService.notifyProductionManager(
                NotificationType.DEADLINE_AT_RISK,
                String.format("Không có kế hoạch tuần nào thỏa mãn tất cả deadline. " +
                                "Kế hoạch tốt nhất: %s (score: %s) với ít vi phạm nhất.",
                        bestPlan.getPlanCode(), bestPlan.getOptimizationScore()),
                data
        );
    }
}
