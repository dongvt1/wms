package com.cy.modules.planning.agent.properties;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.dto.ProductionLineCapacity;
import com.cy.modules.planning.agent.entity.MonthlyPlan;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.entity.QuarterlyPlan;
import com.cy.modules.planning.agent.mapper.MonthlyPlanMapper;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.mapper.QuarterlyPlanMapper;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.StalenessManagementService;
import com.cy.modules.planning.agent.service.impl.QuarterlyPlanServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
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
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Quarterly and Monthly Planning.
 *
 * **Validates: Requirements 4.1, 4.3, 4.4**
 *
 * Property 8: Quarterly plan demand aggregation — Quarterly plan correctly classifies
 *             and aggregates production demand by product type for each month within the quarter.
 * Property 9: Monthly plan capacity validation — Monthly plan validates that total monthly
 *             capacity can fulfill planned quantities based on standard cycle times.
 */
@Tag("property-test")
@Tag("ai-production-planning")
class QuarterlyMonthlyPlanPropertyTest {

    private static final BigDecimal DEFAULT_CYCLE_TIME_HOURS = new BigDecimal("0.5");
    private static final BigDecimal STANDARD_HOURS_PER_DAY = new BigDecimal("8");
    private static final List<String> DEFAULT_LINE_IDS = Arrays.asList("LINE-01", "LINE-02", "LINE-03");

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initialize MyBatis-Plus lambda cache for entities used in LambdaQueryWrapper/LambdaUpdateWrapper.
     */
    @BeforeContainer
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, PlanningOrder.class);
        TableInfoHelper.initTableInfo(assistant, QuarterlyPlan.class);
        TableInfoHelper.initTableInfo(assistant, MonthlyPlan.class);
    }

    // ==================== Helper methods ====================

    /**
     * Creates a fresh QuarterlyPlanServiceImpl with mocked dependencies.
     */
    private QuarterlyPlanServiceImpl createServiceWithMocks(
            List<PlanningOrder> orders,
            List<ProductionLineCapacity> lineCapacities,
            List<QuarterlyPlan> capturedQuarterlyPlans,
            List<MonthlyPlan> capturedMonthlyPlans) {

        QuarterlyPlanServiceImpl service = new QuarterlyPlanServiceImpl();

        // Mock dependencies
        QuarterlyPlanMapper quarterlyPlanMapper = Mockito.mock(QuarterlyPlanMapper.class);
        MonthlyPlanMapper monthlyPlanMapper = Mockito.mock(MonthlyPlanMapper.class);
        PlanningOrderMapper planningOrderMapper = Mockito.mock(PlanningOrderMapper.class);
        ErpClient erpClient = Mockito.mock(ErpClient.class);
        StalenessManagementService stalenessManagementService = Mockito.mock(StalenessManagementService.class);
        PlanningNotificationService notificationService = Mockito.mock(PlanningNotificationService.class);
        ObjectMapper objMapper = new ObjectMapper();

        // Mock planningOrderMapper.selectList - return provided orders
        when(planningOrderMapper.selectList(any())).thenReturn(orders);

        // Mock erpClient.getLineCapacity - return provided capacities
        if (lineCapacities != null && !lineCapacities.isEmpty()) {
            for (ProductionLineCapacity lc : lineCapacities) {
                when(erpClient.getLineCapacity(eq(lc.getLineId()), any(), any())).thenReturn(lc);
            }
        } else {
            when(erpClient.getLineCapacity(anyString(), any(), any())).thenReturn(null);
        }

        // Mock quarterlyPlanMapper.insert - capture inserted plans
        when(quarterlyPlanMapper.insert(any(QuarterlyPlan.class))).thenAnswer(invocation -> {
            QuarterlyPlan plan = invocation.getArgument(0);
            plan.setId(UUID.randomUUID().toString());
            capturedQuarterlyPlans.add(plan);
            return 1;
        });

        // Mock quarterlyPlanMapper.selectById
        when(quarterlyPlanMapper.selectById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return capturedQuarterlyPlans.stream()
                    .filter(p -> id.equals(p.getId()))
                    .findFirst().orElse(null);
        });

        // Mock monthlyPlanMapper.insert - capture inserted plans
        when(monthlyPlanMapper.insert(any(MonthlyPlan.class))).thenAnswer(invocation -> {
            MonthlyPlan plan = invocation.getArgument(0);
            plan.setId(UUID.randomUUID().toString());
            capturedMonthlyPlans.add(plan);
            return 1;
        });

        // Inject mocks via reflection
        injectField(service, "quarterlyPlanMapper", quarterlyPlanMapper);
        injectField(service, "monthlyPlanMapper", monthlyPlanMapper);
        injectField(service, "planningOrderMapper", planningOrderMapper);
        injectField(service, "erpClient", erpClient);
        injectField(service, "stalenessManagementService", stalenessManagementService);
        injectField(service, "planningNotificationService", notificationService);
        injectField(service, "objectMapper", objMapper);

        return service;
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }

    private PlanningOrder createOrder(String productType, BigDecimal quantity, LocalDate deadline) {
        PlanningOrder order = new PlanningOrder();
        order.setId(UUID.randomUUID().toString());
        order.setExternalOrderId("EXT-" + UUID.randomUUID().toString().substring(0, 8));
        order.setProductType(productType);
        order.setCustomerName("Customer-" + productType);
        order.setQuantity(quantity);
        order.setDeadline(toDate(deadline));
        order.setReceiptTimestamp(new Date());
        order.setStatus("confirmed");
        order.setValidationStatus("valid");
        order.setFulfillmentQty(BigDecimal.ZERO);
        order.setSysOrgCode("ORG001");
        return order;
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Calculate working days in a month (Mon-Fri).
     */
    private int getWorkingDaysInMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        int totalDays = ym.lengthOfMonth();
        int workingDays = 0;
        for (int day = 1; day <= totalDays; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            if (date.getDayOfWeek().getValue() <= 5) {
                workingDays++;
            }
        }
        return workingDays;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<List<OrderSpec>> orderSpecs() {
        Arbitrary<String> productTypes = Arbitraries.of("ProductA", "ProductB", "ProductC", "ProductD");
        Arbitrary<BigDecimal> quantities = Arbitraries.bigDecimals()
                .between(new BigDecimal("10"), new BigDecimal("500"))
                .ofScale(2);
        Arbitrary<Integer> monthOffsets = Arbitraries.integers().between(0, 2); // 3 months in quarter

        Arbitrary<OrderSpec> orderSpec = Combinators.combine(productTypes, quantities, monthOffsets)
                .as(OrderSpec::new);

        return orderSpec.list().ofMinSize(1).ofMaxSize(15);
    }

    @Provide
    Arbitrary<Integer> validYears() {
        return Arbitraries.integers().between(2025, 2030);
    }

    @Provide
    Arbitrary<Integer> validQuarters() {
        return Arbitraries.integers().between(1, 4);
    }

    @Provide
    Arbitrary<BigDecimal> lineCapacityHours() {
        // Total available hours per line for a quarter (e.g., 400-800 hours)
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("400"), new BigDecimal("800"))
                .ofScale(2);
    }

    /**
     * Helper record to represent an order specification for generation.
     */
    private record OrderSpec(String productType, BigDecimal quantity, int monthOffset) {}

    // ==================== Property 8: Quarterly plan demand aggregation ====================

    /**
     * Property 8a: The quarterly plan correctly classifies demand by product type per month.
     * The sum of monthly demands per product type equals the total order quantities for that product type.
     *
     * For any set of confirmed orders, the quarterly plan's demand_summary JSON must contain
     * the correct aggregation: for each month, each product type's total equals the sum of
     * order quantities with deadlines in that month.
     *
     * **Validates: Requirements 4.1**
     */
    @Property(tries = 200)
    void quarterlyPlanAggregatesDemandByProductTypePerMonth(
            @ForAll("validYears") int year,
            @ForAll("validQuarters") int quarter,
            @ForAll("orderSpecs") List<OrderSpec> specs) {

        // Build orders with deadlines distributed across the quarter's months
        int startMonth = (quarter - 1) * 3 + 1;
        List<PlanningOrder> orders = new ArrayList<>();
        for (OrderSpec spec : specs) {
            int month = startMonth + spec.monthOffset();
            // Place deadline in the middle of the target month
            LocalDate deadline = LocalDate.of(year, month, 15);
            orders.add(createOrder(spec.productType(), spec.quantity(), deadline));
        }

        // Calculate expected aggregation
        Map<Integer, Map<String, BigDecimal>> expectedDemand = new LinkedHashMap<>();
        for (OrderSpec spec : specs) {
            int month = startMonth + spec.monthOffset();
            expectedDemand.computeIfAbsent(month, k -> new LinkedHashMap<>())
                    .merge(spec.productType(), spec.quantity(), BigDecimal::add);
        }

        // Execute
        List<QuarterlyPlan> capturedPlans = new ArrayList<>();
        List<MonthlyPlan> capturedMonthlyPlans = new ArrayList<>();
        QuarterlyPlanServiceImpl service = createServiceWithMocks(
                orders, Collections.emptyList(), capturedPlans, capturedMonthlyPlans);

        QuarterlyPlan result = service.generateQuarterlyPlan(year, quarter);

        // Verify demand_summary is correctly aggregated
        assertThat(result).isNotNull();
        assertThat(result.getDemandSummary()).isNotNull();

        Map<String, Map<String, Object>> demandSummary = parseDemandSummary(result.getDemandSummary());

        // Verify each month's demand matches expected
        for (Map.Entry<Integer, Map<String, BigDecimal>> monthEntry : expectedDemand.entrySet()) {
            String monthKey = String.valueOf(monthEntry.getKey());
            assertThat(demandSummary)
                    .as("Demand summary should contain month %s", monthKey)
                    .containsKey(monthKey);

            Map<String, Object> actualMonthDemand = demandSummary.get(monthKey);
            for (Map.Entry<String, BigDecimal> productEntry : monthEntry.getValue().entrySet()) {
                String productType = productEntry.getKey();
                BigDecimal expectedQty = productEntry.getValue();

                assertThat(actualMonthDemand)
                        .as("Month %s should contain product type %s", monthKey, productType)
                        .containsKey(productType);

                BigDecimal actualQty = new BigDecimal(actualMonthDemand.get(productType).toString());
                assertThat(actualQty)
                        .as("Month %s, product %s: aggregated demand should equal sum of order quantities",
                                monthKey, productType)
                        .isEqualByComparingTo(expectedQty);
            }
        }
    }

    /**
     * Property 8b: The total demand across all months in the quarterly plan equals
     * the total of all order quantities. No orders are lost or duplicated during aggregation.
     *
     * **Validates: Requirements 4.1**
     */
    @Property(tries = 200)
    void quarterlyPlanTotalDemandEqualsOrderTotal(
            @ForAll("validYears") int year,
            @ForAll("validQuarters") int quarter,
            @ForAll("orderSpecs") List<OrderSpec> specs) {

        int startMonth = (quarter - 1) * 3 + 1;
        List<PlanningOrder> orders = new ArrayList<>();
        for (OrderSpec spec : specs) {
            int month = startMonth + spec.monthOffset();
            LocalDate deadline = LocalDate.of(year, month, 15);
            orders.add(createOrder(spec.productType(), spec.quantity(), deadline));
        }

        // Calculate expected total
        BigDecimal expectedTotal = specs.stream()
                .map(OrderSpec::quantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Execute
        List<QuarterlyPlan> capturedPlans = new ArrayList<>();
        List<MonthlyPlan> capturedMonthlyPlans = new ArrayList<>();
        QuarterlyPlanServiceImpl service = createServiceWithMocks(
                orders, Collections.emptyList(), capturedPlans, capturedMonthlyPlans);

        QuarterlyPlan result = service.generateQuarterlyPlan(year, quarter);

        // Sum all quantities from demand_summary
        Map<String, Map<String, Object>> demandSummary = parseDemandSummary(result.getDemandSummary());
        BigDecimal actualTotal = BigDecimal.ZERO;
        for (Map<String, Object> monthDemand : demandSummary.values()) {
            for (Object qty : monthDemand.values()) {
                actualTotal = actualTotal.add(new BigDecimal(qty.toString()));
            }
        }

        assertThat(actualTotal)
                .as("Total demand in quarterly plan should equal sum of all order quantities")
                .isEqualByComparingTo(expectedTotal);
    }

    /**
     * Property 8c: Each product type appears only in months where orders with that
     * product type have deadlines. No phantom demand is created.
     *
     * **Validates: Requirements 4.1**
     */
    @Property(tries = 200)
    void quarterlyPlanOnlyContainsProductsWithOrdersInMonth(
            @ForAll("validYears") int year,
            @ForAll("validQuarters") int quarter,
            @ForAll("orderSpecs") List<OrderSpec> specs) {

        int startMonth = (quarter - 1) * 3 + 1;
        List<PlanningOrder> orders = new ArrayList<>();
        for (OrderSpec spec : specs) {
            int month = startMonth + spec.monthOffset();
            LocalDate deadline = LocalDate.of(year, month, 15);
            orders.add(createOrder(spec.productType(), spec.quantity(), deadline));
        }

        // Build expected: which product types appear in which months
        Map<Integer, Set<String>> expectedProductsByMonth = new HashMap<>();
        for (OrderSpec spec : specs) {
            int month = startMonth + spec.monthOffset();
            expectedProductsByMonth.computeIfAbsent(month, k -> new HashSet<>())
                    .add(spec.productType());
        }

        // Execute
        List<QuarterlyPlan> capturedPlans = new ArrayList<>();
        List<MonthlyPlan> capturedMonthlyPlans = new ArrayList<>();
        QuarterlyPlanServiceImpl service = createServiceWithMocks(
                orders, Collections.emptyList(), capturedPlans, capturedMonthlyPlans);

        QuarterlyPlan result = service.generateQuarterlyPlan(year, quarter);

        // Verify no phantom products
        Map<String, Map<String, Object>> demandSummary = parseDemandSummary(result.getDemandSummary());
        for (Map.Entry<String, Map<String, Object>> entry : demandSummary.entrySet()) {
            int month = Integer.parseInt(entry.getKey());
            Set<String> actualProducts = entry.getValue().keySet();
            Set<String> expectedProducts = expectedProductsByMonth.getOrDefault(month, Collections.emptySet());

            assertThat(actualProducts)
                    .as("Month %d should only contain product types that have orders in that month", month)
                    .isSubsetOf(expectedProducts);
        }
    }

    // ==================== Property 9: Monthly plan capacity validation ====================

    /**
     * Property 9a: When monthly demand exceeds available capacity, the quarterly plan
     * is flagged (capacityValidated=0) and capacity_gaps contains at least 2 alternatives
     * (load redistribution and overtime scheduling).
     *
     * **Validates: Requirements 4.3, 4.4**
     */
    @Property(tries = 200)
    void capacityExceedanceFlagsAndGeneratesAlternatives(
            @ForAll("validYears") int year,
            @ForAll("validQuarters") int quarter) {

        int startMonth = (quarter - 1) * 3 + 1;

        // Create orders with very high demand to exceed capacity
        // Default capacity per month = 8 hours/day × workingDays × 3 lines
        // We create demand that exceeds this
        int workingDays = getWorkingDaysInMonth(year, startMonth);
        BigDecimal monthlyCapacity = STANDARD_HOURS_PER_DAY
                .multiply(new BigDecimal(workingDays))
                .multiply(new BigDecimal(DEFAULT_LINE_IDS.size()));

        // Demand in hours = quantity × 0.5 (DEFAULT_CYCLE_TIME_HOURS)
        // To exceed capacity, we need quantity × 0.5 > monthlyCapacity
        // So quantity > monthlyCapacity × 2
        BigDecimal exceedingQuantity = monthlyCapacity.multiply(new BigDecimal("2"))
                .add(new BigDecimal("100"));

        List<PlanningOrder> orders = new ArrayList<>();
        LocalDate deadline = LocalDate.of(year, startMonth, 15);
        orders.add(createOrder("HeavyProduct", exceedingQuantity, deadline));

        // Execute with no line capacities (uses fallback calculation)
        List<QuarterlyPlan> capturedPlans = new ArrayList<>();
        List<MonthlyPlan> capturedMonthlyPlans = new ArrayList<>();
        QuarterlyPlanServiceImpl service = createServiceWithMocks(
                orders, Collections.emptyList(), capturedPlans, capturedMonthlyPlans);

        QuarterlyPlan result = service.generateQuarterlyPlan(year, quarter);

        // Verify plan is flagged as not capacity validated
        assertThat(result.getCapacityValidated())
                .as("Plan should be flagged as not capacity validated when demand exceeds capacity")
                .isEqualTo(0);

        // Verify capacity_gaps contains alternatives
        assertThat(result.getCapacityGaps())
                .as("Capacity gaps JSON should be populated when demand exceeds capacity")
                .isNotNull()
                .isNotEmpty();

        Map<String, Map<String, Object>> capacityGaps = parseCapacityGaps(result.getCapacityGaps());
        assertThat(capacityGaps)
                .as("Capacity gaps should contain at least one month entry")
                .isNotEmpty();

        // Verify at least 2 alternatives per gap
        for (Map.Entry<String, Map<String, Object>> gapEntry : capacityGaps.entrySet()) {
            Map<String, Object> gapDetail = gapEntry.getValue();
            assertThat(gapDetail).containsKey("alternatives");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alternatives =
                    (List<Map<String, Object>>) gapDetail.get("alternatives");

            assertThat(alternatives)
                    .as("Month %s should have at least 2 alternative options", gapEntry.getKey())
                    .hasSizeGreaterThanOrEqualTo(2);

            // Verify alternatives include load_redistribution and overtime_scheduling
            Set<String> altTypes = alternatives.stream()
                    .map(alt -> (String) alt.get("type"))
                    .collect(Collectors.toSet());

            assertThat(altTypes)
                    .as("Alternatives should include load_redistribution")
                    .contains("load_redistribution");
            assertThat(altTypes)
                    .as("Alternatives should include overtime_scheduling")
                    .contains("overtime_scheduling");
        }
    }

    /**
     * Property 9b: When monthly demand is within available capacity, the quarterly plan
     * is marked as capacity validated (capacityValidated=1) and no capacity gaps are reported.
     *
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 200)
    void withinCapacityMarksValidated(
            @ForAll("validYears") int year,
            @ForAll("validQuarters") int quarter) {

        int startMonth = (quarter - 1) * 3 + 1;

        // Create small orders that fit within capacity
        // Default capacity per month = 8 × workingDays × 3 lines
        // Use a small quantity that won't exceed capacity
        BigDecimal smallQuantity = new BigDecimal("10");

        List<PlanningOrder> orders = new ArrayList<>();
        LocalDate deadline = LocalDate.of(year, startMonth, 15);
        orders.add(createOrder("SmallProduct", smallQuantity, deadline));

        // Execute with no line capacities (uses fallback - which gives plenty of capacity)
        List<QuarterlyPlan> capturedPlans = new ArrayList<>();
        List<MonthlyPlan> capturedMonthlyPlans = new ArrayList<>();
        QuarterlyPlanServiceImpl service = createServiceWithMocks(
                orders, Collections.emptyList(), capturedPlans, capturedMonthlyPlans);

        QuarterlyPlan result = service.generateQuarterlyPlan(year, quarter);

        // When using fallback (empty line capacities), capacityValidated is set to 0
        // because usingCachedData=true. So we test with actual line capacities instead.
        // Let's provide line capacities with enough hours.
        int workingDays = getWorkingDaysInMonth(year, startMonth);
        BigDecimal monthlyCapacity = STANDARD_HOURS_PER_DAY
                .multiply(new BigDecimal(workingDays))
                .multiply(new BigDecimal(DEFAULT_LINE_IDS.size()));

        // Demand hours = 10 × 0.5 = 5 hours, well within capacity
        // Provide line capacities that cover the quarter
        List<ProductionLineCapacity> lineCapacities = new ArrayList<>();
        for (String lineId : DEFAULT_LINE_IDS) {
            lineCapacities.add(ProductionLineCapacity.builder()
                    .lineId(lineId)
                    .totalAvailableHours(monthlyCapacity) // plenty of capacity per line
                    .build());
        }

        capturedPlans.clear();
        capturedMonthlyPlans.clear();
        service = createServiceWithMocks(orders, lineCapacities, capturedPlans, capturedMonthlyPlans);

        result = service.generateQuarterlyPlan(year, quarter);

        assertThat(result.getCapacityValidated())
                .as("Plan should be capacity validated when demand is within capacity")
                .isEqualTo(1);

        assertThat(result.getCapacityGaps())
                .as("No capacity gaps should be reported when demand is within capacity")
                .isNull();
    }

    /**
     * Property 9c: Capacity validation uses standard cycle times correctly.
     * Total demand hours = sum(quantity × DEFAULT_CYCLE_TIME_HOURS) for each product type.
     * The plan is flagged when demand hours > capacity hours.
     *
     * **Validates: Requirements 4.3, 4.4**
     */
    @Property(tries = 200)
    void capacityValidationUsesStandardCycleTimes(
            @ForAll("validYears") int year,
            @ForAll("validQuarters") int quarter,
            @ForAll("orderSpecs") List<OrderSpec> specs) {

        int startMonth = (quarter - 1) * 3 + 1;
        List<PlanningOrder> orders = new ArrayList<>();
        for (OrderSpec spec : specs) {
            int month = startMonth + spec.monthOffset();
            LocalDate deadline = LocalDate.of(year, month, 15);
            orders.add(createOrder(spec.productType(), spec.quantity(), deadline));
        }

        // Provide line capacities with known total hours
        BigDecimal perLineHours = new BigDecimal("500"); // 500 hours per line for the quarter
        List<ProductionLineCapacity> lineCapacities = new ArrayList<>();
        for (String lineId : DEFAULT_LINE_IDS) {
            lineCapacities.add(ProductionLineCapacity.builder()
                    .lineId(lineId)
                    .totalAvailableHours(perLineHours)
                    .build());
        }

        // Calculate expected demand per month using standard cycle time
        Map<Integer, BigDecimal> demandHoursByMonth = new HashMap<>();
        for (OrderSpec spec : specs) {
            int month = startMonth + spec.monthOffset();
            BigDecimal hours = spec.quantity().multiply(DEFAULT_CYCLE_TIME_HOURS);
            demandHoursByMonth.merge(month, hours, BigDecimal::add);
        }

        // Monthly capacity = perLineHours / 3 (divided among 3 months) × 3 lines
        // Actually the service divides each line's totalAvailableHours by 3 for monthly
        BigDecimal monthlyCapacity = perLineHours.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(DEFAULT_LINE_IDS.size()));

        // Determine if any month exceeds capacity
        boolean anyMonthExceeds = demandHoursByMonth.values().stream()
                .anyMatch(h -> h.compareTo(monthlyCapacity) > 0);

        // Execute
        List<QuarterlyPlan> capturedPlans = new ArrayList<>();
        List<MonthlyPlan> capturedMonthlyPlans = new ArrayList<>();
        QuarterlyPlanServiceImpl service = createServiceWithMocks(
                orders, lineCapacities, capturedPlans, capturedMonthlyPlans);

        QuarterlyPlan result = service.generateQuarterlyPlan(year, quarter);

        if (anyMonthExceeds) {
            assertThat(result.getCapacityValidated())
                    .as("Plan should NOT be capacity validated when any month's demand (using standard cycle times) exceeds capacity")
                    .isEqualTo(0);
            assertThat(result.getCapacityGaps())
                    .as("Capacity gaps should be reported when demand exceeds capacity")
                    .isNotNull();
        } else {
            assertThat(result.getCapacityValidated())
                    .as("Plan should be capacity validated when all months' demand is within capacity")
                    .isEqualTo(1);
        }
    }

    /**
     * Property 9d: Each alternative in capacity gaps shows the capacity gap resolved
     * and delivery impact information.
     *
     * **Validates: Requirements 4.4**
     */
    @Property(tries = 100)
    void alternativesShowCapacityGapResolvedAndDeliveryImpact(
            @ForAll("validYears") int year,
            @ForAll("validQuarters") int quarter) {

        int startMonth = (quarter - 1) * 3 + 1;

        // Create demand that exceeds capacity
        int workingDays = getWorkingDaysInMonth(year, startMonth);
        BigDecimal monthlyCapacity = STANDARD_HOURS_PER_DAY
                .multiply(new BigDecimal(workingDays))
                .multiply(new BigDecimal(DEFAULT_LINE_IDS.size()));

        BigDecimal exceedingQuantity = monthlyCapacity.multiply(new BigDecimal("2"))
                .add(new BigDecimal("50"));

        List<PlanningOrder> orders = new ArrayList<>();
        LocalDate deadline = LocalDate.of(year, startMonth, 15);
        orders.add(createOrder("OverloadProduct", exceedingQuantity, deadline));

        // Execute
        List<QuarterlyPlan> capturedPlans = new ArrayList<>();
        List<MonthlyPlan> capturedMonthlyPlans = new ArrayList<>();
        QuarterlyPlanServiceImpl service = createServiceWithMocks(
                orders, Collections.emptyList(), capturedPlans, capturedMonthlyPlans);

        QuarterlyPlan result = service.generateQuarterlyPlan(year, quarter);

        // Parse and verify alternatives have required fields
        Map<String, Map<String, Object>> capacityGaps = parseCapacityGaps(result.getCapacityGaps());

        for (Map.Entry<String, Map<String, Object>> gapEntry : capacityGaps.entrySet()) {
            Map<String, Object> gapDetail = gapEntry.getValue();

            // Verify gap detail contains demand and capacity hours
            assertThat(gapDetail).containsKey("demandHours");
            assertThat(gapDetail).containsKey("capacityHours");
            assertThat(gapDetail).containsKey("gapHours");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alternatives =
                    (List<Map<String, Object>>) gapDetail.get("alternatives");

            for (Map<String, Object> alt : alternatives) {
                assertThat(alt)
                        .as("Alternative should contain 'capacityGapResolved' field")
                        .containsKey("capacityGapResolved");
                assertThat(alt)
                        .as("Alternative should contain 'deliveryImpact' field")
                        .containsKey("deliveryImpact");
                assertThat(alt)
                        .as("Alternative should contain 'type' field")
                        .containsKey("type");
            }
        }
    }

    // ==================== Utility methods ====================

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> parseDemandSummary(String json) {
        try {
            return objectMapper.readValue(json,
                    new TypeReference<Map<String, Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse demand summary JSON: " + json, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> parseCapacityGaps(String json) {
        try {
            return objectMapper.readValue(json,
                    new TypeReference<Map<String, Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse capacity gaps JSON: " + json, e);
        }
    }
}
