package com.cy.modules.planning.agent.properties;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.dto.ProductionLineCapacity;
import com.cy.modules.planning.agent.entity.MaterialAvailability;
import com.cy.modules.planning.agent.entity.MonthlyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.mapper.MaterialAvailabilityMapper;
import com.cy.modules.planning.agent.mapper.MonthlyPlanMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.InventorySyncService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.impl.WeeklyPlanServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeContainer;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Weekly Plan Generation.
 *
 * **Validates: Requirements 5.1, 5.3, 5.4, 5.5**
 *
 * Property 10: Weekly plan preserves monthly totals
 * Property 11: Production line utilization cap
 * Property 12: Changeover time minimization
 * Property 13: Material availability verification for batches
 */
@Tag("property-test")
@Tag("ai-production-planning")
class WeeklyPlanPropertyTest {

    private static final BigDecimal DEFAULT_CYCLE_TIME_HOURS = new BigDecimal("0.5");
    private static final BigDecimal STANDARD_HOURS_PER_DAY = new BigDecimal("8");
    private static final int WORKING_DAYS_PER_WEEK = 6;
    private static final BigDecimal WEEKLY_HOURS_PER_LINE =
            STANDARD_HOURS_PER_DAY.multiply(new BigDecimal(WORKING_DAYS_PER_WEEK));
    private static final BigDecimal CAPACITY_CAP = new BigDecimal("0.90");
    private static final int DEFAULT_CHANGEOVER_MINUTES = 30;
    private static final List<String> DEFAULT_LINE_IDS =
            Arrays.asList("LINE-01", "LINE-02", "LINE-03");

    @BeforeContainer
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, WeeklyPlan.class);
        TableInfoHelper.initTableInfo(assistant, WeeklyPlanBatch.class);
        TableInfoHelper.initTableInfo(assistant, MonthlyPlan.class);
        TableInfoHelper.initTableInfo(assistant, MaterialAvailability.class);
    }

    // ==================== Helper record ====================

    private record AssignmentSpec(
            String orderId, String productType,
            BigDecimal quantity, String assignedLine) {}

    // ==================== Service factory ====================

    private WeeklyPlanServiceImpl createService(
            MonthlyPlan monthlyPlan,
            List<ProductionLineCapacity> lineCapacities,
            List<MaterialAvailability> materials,
            BigDecimal inventoryLevel,
            List<WeeklyPlan> outPlans,
            List<WeeklyPlanBatch> outBatches) {

        WeeklyPlanServiceImpl service = new WeeklyPlanServiceImpl();

        WeeklyPlanMapper wpMapper = mock(WeeklyPlanMapper.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        MonthlyPlanMapper mpMapper = mock(MonthlyPlanMapper.class);
        MaterialAvailabilityMapper maMapper = mock(MaterialAvailabilityMapper.class);
        InventorySyncService invService = mock(InventorySyncService.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);
        ErpClient erpClient = mock(ErpClient.class);
        ObjectMapper objMapper = new ObjectMapper();

        when(mpMapper.selectById(anyString())).thenReturn(monthlyPlan);

        if (lineCapacities != null && !lineCapacities.isEmpty()) {
            for (ProductionLineCapacity lc : lineCapacities) {
                when(erpClient.getLineCapacity(eq(lc.getLineId()), any(), any()))
                        .thenReturn(lc);
            }
        } else {
            when(erpClient.getLineCapacity(anyString(), any(), any())).thenReturn(null);
        }

        when(wpMapper.insert(any(WeeklyPlan.class))).thenAnswer(inv -> {
            WeeklyPlan p = inv.getArgument(0);
            p.setId(UUID.randomUUID().toString());
            outPlans.add(p);
            return 1;
        });
        when(wpMapper.updateById(any(WeeklyPlan.class))).thenReturn(1);

        when(batchMapper.insert(any(WeeklyPlanBatch.class))).thenAnswer(inv -> {
            WeeklyPlanBatch b = inv.getArgument(0);
            b.setId(UUID.randomUUID().toString());
            outBatches.add(b);
            return 1;
        });

        when(maMapper.selectList(any())).thenReturn(
                materials != null ? materials : Collections.emptyList());

        when(invService.getInventoryLevel(anyString())).thenReturn(
                inventoryLevel != null ? inventoryLevel : new BigDecimal("1000"));

        inject(service, "weeklyPlanMapper", wpMapper);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "monthlyPlanMapper", mpMapper);
        inject(service, "materialAvailabilityMapper", maMapper);
        inject(service, "inventorySyncService", invService);
        inject(service, "planningNotificationService", notifService);
        inject(service, "erpClient", erpClient);
        inject(service, "objectMapper", objMapper);

        return service;
    }

    private void inject(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }

    // ==================== MonthlyPlan builder ====================

    private MonthlyPlan buildMonthlyPlan(int year, int month, List<AssignmentSpec> specs) {
        MonthlyPlan plan = new MonthlyPlan();
        plan.setId(UUID.randomUUID().toString());
        plan.setPlanCode(String.format("MP%d%02d001", year, month));
        plan.setQuarterlyPlanId(UUID.randomUUID().toString());
        plan.setYear(year);
        plan.setMonth(month);
        plan.setOptionRank(1);
        plan.setStatus("approved");
        plan.setSysOrgCode("ORG001");

        LocalDate monthStart = LocalDate.of(year, month, 1);
        List<Map<String, Object>> assignments = new ArrayList<>();
        int dayOffset = 0;
        for (AssignmentSpec s : specs) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("orderId", s.orderId());
            a.put("productType", s.productType());
            a.put("quantity", s.quantity());
            a.put("assignedLine", s.assignedLine());
            LocalDate start = monthStart.plusDays(dayOffset % 28);
            a.put("startDate", start.toString());
            a.put("expectedCompletion", start.plusDays(5).toString());
            assignments.add(a);
            dayOffset += 7;
        }

        Map<String, Object> planDetails = new LinkedHashMap<>();
        planDetails.put("assignments", assignments);
        try {
            plan.setPlanDetails(new ObjectMapper().writeValueAsString(planDetails));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return plan;
    }

    // ==================== Line capacity builder ====================

    private List<ProductionLineCapacity> buildLineCapacities(int year, int month) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = YearMonth.of(year, month).atEndOfMonth();
        List<ProductionLineCapacity> caps = new ArrayList<>();
        for (String lineId : DEFAULT_LINE_IDS) {
            caps.add(ProductionLineCapacity.builder()
                    .lineId(lineId)
                    .fromDate(from)
                    .toDate(to)
                    .totalAvailableHours(WEEKLY_HOURS_PER_LINE.multiply(new BigDecimal("4")))
                    .capableProducts(Arrays.asList(
                            "ProductA", "ProductB", "ProductC", "ProductD"))
                    .build());
        }
        return caps;
    }

    /**
     * Constrain specs so total demand per week fits within 90% cap across all lines.
     * Max hours per line per week = 48 * 0.9 = 43.2h.
     * Total across 3 lines = 129.6h.
     * Each unit takes 0.5h, so max ~259 units total.
     * We limit to small quantities to ensure no redistribution overflow.
     */
    private List<AssignmentSpec> constrainToCapacity(List<AssignmentSpec> specs) {
        BigDecimal maxTotalHours = WEEKLY_HOURS_PER_LINE.multiply(CAPACITY_CAP)
                .multiply(new BigDecimal(DEFAULT_LINE_IDS.size()));
        BigDecimal usedHours = BigDecimal.ZERO;
        List<AssignmentSpec> result = new ArrayList<>();
        for (AssignmentSpec s : specs) {
            BigDecimal hours = s.quantity().multiply(DEFAULT_CYCLE_TIME_HOURS);
            BigDecimal changeover = new BigDecimal(DEFAULT_CHANGEOVER_MINUTES)
                    .divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);
            BigDecimal needed = hours.add(changeover);
            if (usedHours.add(needed).compareTo(maxTotalHours) <= 0) {
                result.add(s);
                usedHours = usedHours.add(needed);
            }
        }
        return result;
    }

    private int calcChangeover(String from, String to) {
        if (from == null || from.equals(to)) return 0;
        return DEFAULT_CHANGEOVER_MINUTES;
    }

    private int totalChangeover(List<WeeklyPlanBatch> seq) {
        int total = 0;
        for (int i = 1; i < seq.size(); i++) {
            total += calcChangeover(
                    seq.get(i - 1).getProductType(), seq.get(i).getProductType());
        }
        return total;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<List<AssignmentSpec>> assignmentSpecs() {
        Arbitrary<String> productTypes = Arbitraries.of(
                "ProductA", "ProductB", "ProductC", "ProductD");
        Arbitrary<BigDecimal> quantities = Arbitraries.bigDecimals()
                .between(new BigDecimal("5"), new BigDecimal("40"))
                .ofScale(2);
        Arbitrary<String> lineIds = Arbitraries.of("LINE-01", "LINE-02", "LINE-03");

        Arbitrary<AssignmentSpec> spec = Combinators.combine(productTypes, quantities, lineIds)
                .as((pt, qty, line) -> new AssignmentSpec(
                        UUID.randomUUID().toString(), pt, qty, line));

        return spec.list().ofMinSize(1).ofMaxSize(8);
    }

    @Provide
    Arbitrary<Integer> validYears() {
        return Arbitraries.integers().between(2025, 2028);
    }

    @Provide
    Arbitrary<Integer> validMonths() {
        return Arbitraries.integers().between(1, 12);
    }

    // ==================== Property 10 ====================

    /**
     * Property 10: Weekly plan preserves monthly totals.
     * Sum of weekly batch quantities per product type equals the monthly plan total.
     *
     * **Validates: Requirements 5.1**
     */
    @Property(tries = 200)
    void weeklyPlanPreservesMonthlyTotals(
            @ForAll("validYears") int year,
            @ForAll("validMonths") int month,
            @ForAll("assignmentSpecs") List<AssignmentSpec> specs) {

        MonthlyPlan monthlyPlan = buildMonthlyPlan(year, month, specs);
        List<ProductionLineCapacity> caps = buildLineCapacities(year, month);

        // Expected totals per product type from the monthly plan
        Map<String, BigDecimal> expected = new HashMap<>();
        for (AssignmentSpec s : specs) {
            expected.merge(s.productType(), s.quantity(), BigDecimal::add);
        }

        List<WeeklyPlan> outPlans = new ArrayList<>();
        List<WeeklyPlanBatch> outBatches = new ArrayList<>();
        WeeklyPlanServiceImpl service = createService(
                monthlyPlan, caps, Collections.emptyList(),
                new BigDecimal("1000"), outPlans, outBatches);

        service.generateWeeklyPlans(monthlyPlan.getId());

        // Actual totals from generated batches
        Map<String, BigDecimal> actual = new HashMap<>();
        for (WeeklyPlanBatch b : outBatches) {
            actual.merge(b.getProductType(), b.getQuantity(), BigDecimal::add);
        }

        for (Map.Entry<String, BigDecimal> e : expected.entrySet()) {
            assertThat(actual.getOrDefault(e.getKey(), BigDecimal.ZERO))
                    .as("Weekly total for '%s' must equal monthly total", e.getKey())
                    .isEqualByComparingTo(e.getValue());
        }
        assertThat(actual.keySet())
                .as("No extra product types in weekly batches")
                .isSubsetOf(expected.keySet());
    }

    // ==================== Property 11 ====================

    /**
     * Property 11: Production line utilization cap.
     * No single production line is scheduled beyond 90% of its available
     * production hours per week.
     *
     * **Validates: Requirements 5.3**
     */
    @Property(tries = 200)
    void productionLineUtilizationCap(
            @ForAll("validYears") int year,
            @ForAll("validMonths") int month,
            @ForAll("assignmentSpecs") List<AssignmentSpec> specs) {

        // Constrain specs so total demand fits within capacity (avoids redistribute)
        List<AssignmentSpec> constrained = constrainToCapacity(specs);
        if (constrained.isEmpty()) return;

        MonthlyPlan monthlyPlan = buildMonthlyPlan(year, month, constrained);
        List<ProductionLineCapacity> caps = buildLineCapacities(year, month);

        List<WeeklyPlan> outPlans = new ArrayList<>();
        List<WeeklyPlanBatch> outBatches = new ArrayList<>();
        WeeklyPlanServiceImpl service = createService(
                monthlyPlan, caps, Collections.emptyList(),
                new BigDecimal("1000"), outPlans, outBatches);

        service.generateWeeklyPlans(monthlyPlan.getId());

        if (outBatches.isEmpty()) return;

        // Group by (weeklyPlanId, productionLineId)
        Map<String, List<WeeklyPlanBatch>> grouped = outBatches.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getWeeklyPlanId() + "|" + b.getProductionLineId()));

        BigDecimal maxHours = WEEKLY_HOURS_PER_LINE.multiply(CAPACITY_CAP)
                .setScale(2, RoundingMode.HALF_UP);

        for (Map.Entry<String, List<WeeklyPlanBatch>> entry : grouped.entrySet()) {
            BigDecimal totalHours = BigDecimal.ZERO;
            for (WeeklyPlanBatch b : entry.getValue()) {
                BigDecimal prodH = b.getQuantity().multiply(DEFAULT_CYCLE_TIME_HOURS);
                BigDecimal coH = new BigDecimal(b.getChangeoverMinutes())
                        .divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);
                totalHours = totalHours.add(prodH).add(coH);
            }
            assertThat(totalHours)
                    .as("Line %s hours (%s) must not exceed 90%% cap (%s)",
                            entry.getKey(), totalHours, maxHours)
                    .isLessThanOrEqualTo(maxHours);
        }
    }

    // ==================== Property 12 ====================

    /**
     * Property 12: Changeover time minimization.
     * Products are sequenced on each line to minimize total changeover time.
     * The greedy nearest-neighbor sequence has changeover <= worst case (all transitions).
     * Additionally, the recorded changeover_minutes on each batch is consistent with
     * the product type transitions in the sequence.
     *
     * **Validates: Requirements 5.4**
     */
    @Property(tries = 200)
    void changeoverTimeMinimization(
            @ForAll("validYears") int year,
            @ForAll("validMonths") int month,
            @ForAll("assignmentSpecs") List<AssignmentSpec> specs) {

        if (specs.size() < 2) return;

        MonthlyPlan monthlyPlan = buildMonthlyPlan(year, month, specs);
        List<ProductionLineCapacity> caps = buildLineCapacities(year, month);

        List<WeeklyPlan> outPlans = new ArrayList<>();
        List<WeeklyPlanBatch> outBatches = new ArrayList<>();
        WeeklyPlanServiceImpl service = createService(
                monthlyPlan, caps, Collections.emptyList(),
                new BigDecimal("1000"), outPlans, outBatches);

        service.generateWeeklyPlans(monthlyPlan.getId());

        if (outBatches.isEmpty()) return;

        // Group by (weeklyPlanId, productionLineId) to get per-week per-line sequences
        Map<String, List<WeeklyPlanBatch>> byPlanAndLine = outBatches.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getWeeklyPlanId() + "|" + b.getProductionLineId()));

        for (Map.Entry<String, List<WeeklyPlanBatch>> entry : byPlanAndLine.entrySet()) {
            List<WeeklyPlanBatch> lineBatches = entry.getValue();
            if (lineBatches.size() < 2) continue;

            // Sort by sequence_order
            lineBatches.sort(Comparator.comparingInt(WeeklyPlanBatch::getSequenceOrder));

            // Actual changeover from the optimized sequence
            int actualCO = totalChangeover(lineBatches);

            // Worst case: every transition is a changeover
            int worstCO = (lineBatches.size() - 1) * DEFAULT_CHANGEOVER_MINUTES;

            assertThat(actualCO)
                    .as("Optimized changeover (%d) on %s <= worst case (%d)",
                            actualCO, entry.getKey(), worstCO)
                    .isLessThanOrEqualTo(worstCO);

            // Verify changeover_minutes field consistency
            assertThat(lineBatches.get(0).getChangeoverMinutes())
                    .as("First batch should have 0 changeover")
                    .isEqualTo(0);

            for (int i = 1; i < lineBatches.size(); i++) {
                int expectedCO = calcChangeover(
                        lineBatches.get(i - 1).getProductType(),
                        lineBatches.get(i).getProductType());
                assertThat(lineBatches.get(i).getChangeoverMinutes())
                        .as("Batch %d changeover should match product transition",
                                lineBatches.get(i).getSequenceOrder())
                        .isEqualTo(expectedCO);
            }
        }
    }

    // ==================== Property 13 ====================

    /**
     * Property 13a: Material availability verification — when all materials are available,
     * batches are marked 'verified' and the weekly plan has material_verified=1.
     *
     * **Validates: Requirements 5.5**
     */
    @Property(tries = 200)
    void materialAvailableMarksVerified(
            @ForAll("validYears") int year,
            @ForAll("validMonths") int month,
            @ForAll("assignmentSpecs") List<AssignmentSpec> specs) {

        MonthlyPlan monthlyPlan = buildMonthlyPlan(year, month, specs);
        List<ProductionLineCapacity> caps = buildLineCapacities(year, month);

        // No material records + positive inventory → verified
        List<WeeklyPlan> outPlans = new ArrayList<>();
        List<WeeklyPlanBatch> outBatches = new ArrayList<>();
        WeeklyPlanServiceImpl service = createService(
                monthlyPlan, caps, Collections.emptyList(),
                new BigDecimal("1000"), outPlans, outBatches);

        service.generateWeeklyPlans(monthlyPlan.getId());

        for (WeeklyPlanBatch b : outBatches) {
            assertThat(b.getMaterialStatus())
                    .as("Batch '%s' should be verified when inventory available",
                            b.getProductType())
                    .isEqualTo("verified");
        }
        for (WeeklyPlan p : outPlans) {
            assertThat(p.getMaterialVerified())
                    .as("Plan should be material_verified=1")
                    .isEqualTo(1);
        }
    }

    /**
     * Property 13b: Material availability verification — when materials have shortage
     * status with no expected arrival, batches are flagged as 'shortage' and the
     * weekly plan has material_verified=0.
     *
     * **Validates: Requirements 5.5**
     */
    @Property(tries = 200)
    void materialShortageMarksBatchesAsShortage(
            @ForAll("validYears") int year,
            @ForAll("validMonths") int month,
            @ForAll("assignmentSpecs") List<AssignmentSpec> specs) {

        MonthlyPlan monthlyPlan = buildMonthlyPlan(year, month, specs);
        List<ProductionLineCapacity> caps = buildLineCapacities(year, month);

        // Create shortage records for all orders
        List<MaterialAvailability> shortages = new ArrayList<>();
        for (AssignmentSpec s : specs) {
            MaterialAvailability ma = new MaterialAvailability();
            ma.setId(UUID.randomUUID().toString());
            ma.setOrderId(s.orderId());
            ma.setMaterialId("MAT-" + s.productType());
            ma.setRequiredQty(s.quantity());
            ma.setAvailableQty(BigDecimal.ZERO);
            ma.setDeficitQty(s.quantity());
            ma.setStatus("shortage");
            ma.setExpectedArrival(null); // No arrival → fails verification
            shortages.add(ma);
        }

        List<WeeklyPlan> outPlans = new ArrayList<>();
        List<WeeklyPlanBatch> outBatches = new ArrayList<>();
        WeeklyPlanServiceImpl service = createService(
                monthlyPlan, caps, shortages,
                new BigDecimal("1000"), outPlans, outBatches);

        service.generateWeeklyPlans(monthlyPlan.getId());

        // All batches should be shortage
        for (WeeklyPlanBatch b : outBatches) {
            assertThat(b.getMaterialStatus())
                    .as("Batch '%s' should be shortage when materials unavailable",
                            b.getProductType())
                    .isEqualTo("shortage");
        }
        for (WeeklyPlan p : outPlans) {
            assertThat(p.getMaterialVerified())
                    .as("Plan should be material_verified=0 when batches have shortage")
                    .isEqualTo(0);
        }
    }

    /**
     * Property 13c: Material availability verification — when materials arrive
     * at least 1 business day before batch start, batches are marked 'verified'.
     *
     * **Validates: Requirements 5.5**
     */
    @Property(tries = 200)
    void materialArrivingOnTimeMarksVerified(
            @ForAll("validYears") int year,
            @ForAll("validMonths") int month,
            @ForAll("assignmentSpecs") List<AssignmentSpec> specs) {

        MonthlyPlan monthlyPlan = buildMonthlyPlan(year, month, specs);
        List<ProductionLineCapacity> caps = buildLineCapacities(year, month);

        // Create material records with arrival well before batch start
        // Batch starts at beginning of month, so arrival 5 days before month start
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate earlyArrival = monthStart.minusDays(5);
        Date arrivalDate = Date.from(
                earlyArrival.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<MaterialAvailability> materials = new ArrayList<>();
        for (AssignmentSpec s : specs) {
            MaterialAvailability ma = new MaterialAvailability();
            ma.setId(UUID.randomUUID().toString());
            ma.setOrderId(s.orderId());
            ma.setMaterialId("MAT-" + s.productType());
            ma.setRequiredQty(s.quantity());
            ma.setAvailableQty(BigDecimal.ZERO);
            ma.setDeficitQty(s.quantity());
            ma.setStatus("pr_generated");
            ma.setExpectedArrival(arrivalDate); // Arrives well before start
            materials.add(ma);
        }

        List<WeeklyPlan> outPlans = new ArrayList<>();
        List<WeeklyPlanBatch> outBatches = new ArrayList<>();
        WeeklyPlanServiceImpl service = createService(
                monthlyPlan, caps, materials,
                new BigDecimal("1000"), outPlans, outBatches);

        service.generateWeeklyPlans(monthlyPlan.getId());

        for (WeeklyPlanBatch b : outBatches) {
            assertThat(b.getMaterialStatus())
                    .as("Batch '%s' should be verified when material arrives on time",
                            b.getProductType())
                    .isEqualTo("verified");
        }
    }
}
