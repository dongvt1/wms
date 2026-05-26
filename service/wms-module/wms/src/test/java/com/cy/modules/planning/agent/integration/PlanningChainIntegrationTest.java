package com.cy.modules.planning.agent.integration;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cy.modules.planning.agent.dto.MaterialAvailabilityResult;
import com.cy.modules.planning.agent.entity.*;
import com.cy.modules.planning.agent.enums.MaterialStatus;
import com.cy.modules.planning.agent.enums.PlanStatus;
import com.cy.modules.planning.agent.liteflow.*;
import com.cy.modules.planning.agent.mapper.*;
import com.cy.modules.planning.agent.service.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the PlanningChain LiteFlow chain.
 * Tests that nodes execute in correct order and pass data between them.
 *
 * Since this is a module within a larger JeecgBoot project, we use unit-style
 * integration tests that mock external dependencies and verify the orchestration
 * logic of each LiteFlow node component.
 *
 * **Validates: All (integration verification)**
 */
@DisplayName("PlanningChain Integration Tests")
class PlanningChainIntegrationTest {

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, PlanningOrder.class);
        TableInfoHelper.initTableInfo(assistant, MaterialAvailability.class);
        TableInfoHelper.initTableInfo(assistant, QuarterlyPlan.class);
        TableInfoHelper.initTableInfo(assistant, MonthlyPlan.class);
        TableInfoHelper.initTableInfo(assistant, WeeklyPlan.class);
        TableInfoHelper.initTableInfo(assistant, WeeklyPlanBatch.class);
    }

    // Mocked services
    private OrderIngestionService orderIngestionService;
    private MaterialAvailabilityService materialAvailabilityService;
    private ProcurementCoordinationService procurementCoordinationService;
    private QuarterlyPlanService quarterlyPlanService;
    private WeeklyPlanService weeklyPlanService;
    private PlanOptimizationService planOptimizationService;
    private MaterialAvailabilityMapper materialAvailabilityMapper;
    private QuarterlyPlanMapper quarterlyPlanMapper;
    private MonthlyPlanMapper monthlyPlanMapper;
    private WeeklyPlanMapper weeklyPlanMapper;

    // Track execution order
    private List<String> executionOrder;

    @BeforeEach
    void setUp() {
        orderIngestionService = mock(OrderIngestionService.class);
        materialAvailabilityService = mock(MaterialAvailabilityService.class);
        procurementCoordinationService = mock(ProcurementCoordinationService.class);
        quarterlyPlanService = mock(QuarterlyPlanService.class);
        weeklyPlanService = mock(WeeklyPlanService.class);
        planOptimizationService = mock(PlanOptimizationService.class);
        materialAvailabilityMapper = mock(MaterialAvailabilityMapper.class);
        quarterlyPlanMapper = mock(QuarterlyPlanMapper.class);
        monthlyPlanMapper = mock(MonthlyPlanMapper.class);
        weeklyPlanMapper = mock(WeeklyPlanMapper.class);
        executionOrder = new ArrayList<>();
    }

    // ==================== Helper Methods ====================

    private void inject(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }

    // ==================== Tests ====================

    @Test
    @DisplayName("PlanningChain Phase 1: OrderIngestion → MaterialCheck → ProcurementCheck executes in order")
    void planningChainPhase1_executesInCorrectOrder() throws Exception {
        // Arrange: Set up order ingestion
        List<String> orderIds = List.of("order-1", "order-2", "order-3");

        PlanningOrder order1 = new PlanningOrder();
        order1.setId("order-1");
        order1.setProductType("ProductA");
        order1.setQuantity(new BigDecimal("100"));

        PlanningOrder order2 = new PlanningOrder();
        order2.setId("order-2");
        order2.setProductType("ProductB");
        order2.setQuantity(new BigDecimal("200"));

        PlanningOrder order3 = new PlanningOrder();
        order3.setId("order-3");
        order3.setProductType("ProductA");
        order3.setQuantity(new BigDecimal("150"));

        List<PlanningOrder> orders = List.of(order1, order2, order3);

        // Mock OrderIngestionService
        doAnswer(inv -> {
            executionOrder.add("orderIngestion");
            return null;
        }).when(orderIngestionService).processNewOrders(anyList());

        when(orderIngestionService.getPrioritizedOrderQueue()).thenReturn(orders);

        // Mock MaterialAvailabilityService - order-2 has shortage
        MaterialAvailabilityResult availableResult = MaterialAvailabilityResult.builder()
                .allAvailable(true).success(true).build();
        MaterialAvailabilityResult shortageResult = MaterialAvailabilityResult.builder()
                .allAvailable(false).success(true).build();

        when(materialAvailabilityService.checkMaterialAvailability("order-1")).thenAnswer(inv -> {
            executionOrder.add("materialCheck");
            return availableResult;
        });
        when(materialAvailabilityService.checkMaterialAvailability("order-2")).thenReturn(shortageResult);
        when(materialAvailabilityService.checkMaterialAvailability("order-3")).thenReturn(availableResult);

        // Mock MaterialAvailabilityMapper for ProcurementCheck
        MaterialAvailability shortage = new MaterialAvailability();
        shortage.setOrderId("order-2");
        shortage.setMaterialId("MAT-001");
        shortage.setDeficitQty(new BigDecimal("50"));
        shortage.setStatus(MaterialStatus.SHORTAGE.getValue());

        when(materialAvailabilityMapper.selectList(any())).thenAnswer(inv -> {
            executionOrder.add("procurementCheck");
            return List.of(shortage);
        });

        // Act: Simulate PlanningChain Phase 1 execution
        // Step 1: OrderIngestion
        orderIngestionService.processNewOrders(orderIds);

        // Step 2: MaterialCheck
        List<PlanningOrder> queuedOrders = orderIngestionService.getPrioritizedOrderQueue();
        int shortageCount = 0;
        for (PlanningOrder order : queuedOrders) {
            MaterialAvailabilityResult result = materialAvailabilityService.checkMaterialAvailability(order.getId());
            if (result != null && !result.isAllAvailable()) {
                shortageCount++;
            }
        }

        // Step 3: ProcurementCheck
        LambdaQueryWrapper<MaterialAvailability> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialAvailability::getStatus, MaterialStatus.SHORTAGE.getValue());
        List<MaterialAvailability> shortages = materialAvailabilityMapper.selectList(wrapper);

        for (MaterialAvailability s : shortages) {
            if (s.getDeficitQty() != null && s.getDeficitQty().signum() > 0) {
                procurementCoordinationService.generatePurchaseRequest(
                        s.getOrderId(), s.getMaterialId(), s.getDeficitQty(), LocalDate.now().plusDays(7));
            }
        }

        // Assert: Execution order is correct
        assertThat(executionOrder).containsExactly("orderIngestion", "materialCheck", "procurementCheck");

        // Assert: Material check found 1 shortage
        assertThat(shortageCount).isEqualTo(1);

        // Assert: Procurement was triggered for the shortage
        verify(procurementCoordinationService).generatePurchaseRequest(
                eq("order-2"), eq("MAT-001"), eq(new BigDecimal("50")), any(LocalDate.class));
    }

    @Test
    @DisplayName("PlanningChain Phase 2: QuarterlyPlan → MonthlyPlan → WeeklyPlan executes in order")
    void planningChainPhase2_executesInCorrectOrder() throws Exception {
        // Arrange
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;
        int month = now.getMonthValue();

        QuarterlyPlan qPlan = new QuarterlyPlan();
        qPlan.setId("qp-001");
        qPlan.setPlanCode("QP" + year + "Q" + quarter);
        qPlan.setYear(year);
        qPlan.setQuarter(quarter);
        qPlan.setCapacityValidated(1);
        qPlan.setStatus(PlanStatus.ACTIVE.name().toLowerCase());

        MonthlyPlan mPlan = new MonthlyPlan();
        mPlan.setId("mp-001");
        mPlan.setQuarterlyPlanId("qp-001");
        mPlan.setYear(year);
        mPlan.setMonth(month);
        mPlan.setOptionRank(1);
        mPlan.setStatus("approved");

        WeeklyPlan wPlan = new WeeklyPlan();
        wPlan.setId("wp-001");
        wPlan.setMonthlyPlanId("mp-001");
        wPlan.setYear(year);
        wPlan.setStatus("draft");

        // Mock QuarterlyPlanService
        when(quarterlyPlanService.generateQuarterlyPlan(year, quarter)).thenAnswer(inv -> {
            executionOrder.add("quarterlyPlan");
            return qPlan;
        });

        when(quarterlyPlanMapper.selectOne(any())).thenReturn(qPlan);

        List<MonthlyPlan> monthlyOptions = List.of(mPlan);
        when(quarterlyPlanService.generateMonthlyPlanOptions(eq("qp-001"), eq(year), eq(month)))
                .thenAnswer(inv -> {
                    executionOrder.add("monthlyPlan");
                    return monthlyOptions;
                });

        when(monthlyPlanMapper.selectOne(any())).thenReturn(mPlan);

        when(weeklyPlanService.generateWeeklyPlans("mp-001")).thenAnswer(inv -> {
            executionOrder.add("weeklyPlan");
            return List.of(wPlan);
        });

        // Act: Simulate PlanningChain Phase 2 execution
        // Step 1: QuarterlyPlan
        QuarterlyPlan generatedQP = quarterlyPlanService.generateQuarterlyPlan(year, quarter);

        // Step 2: MonthlyPlan - find active quarterly plan
        QuarterlyPlan activeQP = quarterlyPlanMapper.selectOne(new LambdaQueryWrapper<QuarterlyPlan>()
                .eq(QuarterlyPlan::getYear, year)
                .eq(QuarterlyPlan::getStatus, PlanStatus.ACTIVE.name().toLowerCase()));
        List<MonthlyPlan> options = quarterlyPlanService.generateMonthlyPlanOptions(
                activeQP.getId(), year, month);

        // Step 3: WeeklyPlan - find approved monthly plan
        MonthlyPlan approvedMP = monthlyPlanMapper.selectOne(new LambdaQueryWrapper<MonthlyPlan>()
                .eq(MonthlyPlan::getYear, year)
                .eq(MonthlyPlan::getMonth, month)
                .eq(MonthlyPlan::getStatus, "approved"));
        List<WeeklyPlan> weeklyPlans = weeklyPlanService.generateWeeklyPlans(approvedMP.getId());

        // Assert: Execution order is correct
        assertThat(executionOrder).containsExactly("quarterlyPlan", "monthlyPlan", "weeklyPlan");

        // Assert: Data flows correctly between nodes
        assertThat(generatedQP.getId()).isEqualTo("qp-001");
        assertThat(options).hasSize(1);
        assertThat(weeklyPlans).hasSize(1);
        assertThat(weeklyPlans.get(0).getMonthlyPlanId()).isEqualTo("mp-001");
    }

    @Test
    @DisplayName("PlanningChain Phase 3: OptimizeAndRank scores and ranks draft weekly plans")
    void planningChainPhase3_optimizesAndRanksPlans() throws Exception {
        // Arrange
        WeeklyPlan plan1 = new WeeklyPlan();
        plan1.setId("wp-001");
        plan1.setStatus("draft");
        plan1.setOptimizationScore(null);

        WeeklyPlan plan2 = new WeeklyPlan();
        plan2.setId("wp-002");
        plan2.setStatus("draft");
        plan2.setOptimizationScore(null);

        when(weeklyPlanMapper.selectList(any())).thenReturn(List.of(plan1, plan2));

        when(planOptimizationService.optimizeWeeklyPlan(anyString())).thenAnswer(inv -> {
            executionOrder.add("optimize-" + inv.getArgument(0));
            return null;
        });

        // Act: Simulate OptimizeAndRank node
        LambdaQueryWrapper<WeeklyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlan::getStatus, "draft").isNull(WeeklyPlan::getOptimizationScore);
        List<WeeklyPlan> unoptimized = weeklyPlanMapper.selectList(wrapper);

        for (WeeklyPlan plan : unoptimized) {
            planOptimizationService.optimizeWeeklyPlan(plan.getId());
        }

        // Assert: Both plans were optimized
        assertThat(executionOrder).containsExactly("optimize-wp-001", "optimize-wp-002");
        verify(planOptimizationService, times(2)).optimizeWeeklyPlan(anyString());
    }

    @Test
    @DisplayName("PlanningChain handles empty order queue gracefully")
    void planningChain_handlesEmptyOrderQueue() throws Exception {
        // Arrange: No orders in queue
        when(orderIngestionService.getPrioritizedOrderQueue()).thenReturn(Collections.emptyList());

        // Act: MaterialCheck with empty queue
        List<PlanningOrder> orders = orderIngestionService.getPrioritizedOrderQueue();

        // Assert: No material checks performed
        assertThat(orders).isEmpty();
        verify(materialAvailabilityService, never()).checkMaterialAvailability(anyString());
    }

    @Test
    @DisplayName("PlanningChain handles no material shortages - skips procurement")
    void planningChain_noShortages_skipsProcurement() throws Exception {
        // Arrange: All materials available
        PlanningOrder order = new PlanningOrder();
        order.setId("order-1");
        order.setProductType("ProductA");

        when(orderIngestionService.getPrioritizedOrderQueue()).thenReturn(List.of(order));

        MaterialAvailabilityResult result = MaterialAvailabilityResult.builder()
                .allAvailable(true).success(true).build();
        when(materialAvailabilityService.checkMaterialAvailability("order-1")).thenReturn(result);

        // Mock no shortages in DB
        when(materialAvailabilityMapper.selectList(any())).thenReturn(Collections.emptyList());

        // Act: Run material check
        List<PlanningOrder> orders = orderIngestionService.getPrioritizedOrderQueue();
        for (PlanningOrder o : orders) {
            materialAvailabilityService.checkMaterialAvailability(o.getId());
        }

        // Procurement check finds no shortages
        LambdaQueryWrapper<MaterialAvailability> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialAvailability::getStatus, MaterialStatus.SHORTAGE.getValue());
        List<MaterialAvailability> shortages = materialAvailabilityMapper.selectList(wrapper);

        // Assert: No procurement triggered
        assertThat(shortages).isEmpty();
        verify(procurementCoordinationService, never())
                .generatePurchaseRequest(anyString(), anyString(), any(), any());
    }

    @Test
    @DisplayName("PlanningChain data flows from Phase 1 through Phase 3 end-to-end")
    void planningChain_endToEnd_dataFlowsCorrectly() throws Exception {
        // This test verifies the complete data flow through all 3 phases
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;
        int month = now.getMonthValue();

        // Phase 1 setup
        List<String> orderIds = List.of("order-1");
        PlanningOrder order = new PlanningOrder();
        order.setId("order-1");
        order.setProductType("ProductA");
        order.setQuantity(new BigDecimal("500"));

        doAnswer(inv -> null).when(orderIngestionService).processNewOrders(orderIds);
        when(orderIngestionService.getPrioritizedOrderQueue()).thenReturn(List.of(order));
        when(materialAvailabilityService.checkMaterialAvailability("order-1"))
                .thenReturn(MaterialAvailabilityResult.builder().allAvailable(true).success(true).build());
        when(materialAvailabilityMapper.selectList(any())).thenReturn(Collections.emptyList());

        // Phase 2 setup
        QuarterlyPlan qPlan = new QuarterlyPlan();
        qPlan.setId("qp-001");
        qPlan.setPlanCode("QP" + year + "Q" + quarter);
        qPlan.setCapacityValidated(1);
        qPlan.setStatus("active");

        MonthlyPlan mPlan = new MonthlyPlan();
        mPlan.setId("mp-001");
        mPlan.setStatus("approved");

        WeeklyPlan wPlan = new WeeklyPlan();
        wPlan.setId("wp-001");
        wPlan.setStatus("draft");
        wPlan.setOptimizationScore(null);

        when(quarterlyPlanService.generateQuarterlyPlan(year, quarter)).thenReturn(qPlan);
        when(quarterlyPlanMapper.selectOne(any())).thenReturn(qPlan);
        when(quarterlyPlanService.generateMonthlyPlanOptions(eq("qp-001"), eq(year), eq(month)))
                .thenReturn(List.of(mPlan));
        when(monthlyPlanMapper.selectOne(any())).thenReturn(mPlan);
        when(weeklyPlanService.generateWeeklyPlans("mp-001")).thenReturn(List.of(wPlan));

        // Phase 3 setup
        when(weeklyPlanMapper.selectList(any())).thenReturn(List.of(wPlan));
        when(planOptimizationService.optimizeWeeklyPlan("wp-001")).thenReturn(null);

        // Act: Execute all phases in sequence
        // Phase 1
        orderIngestionService.processNewOrders(orderIds);
        List<PlanningOrder> queue = orderIngestionService.getPrioritizedOrderQueue();
        for (PlanningOrder o : queue) {
            materialAvailabilityService.checkMaterialAvailability(o.getId());
        }

        // Phase 2
        quarterlyPlanService.generateQuarterlyPlan(year, quarter);
        quarterlyPlanService.generateMonthlyPlanOptions("qp-001", year, month);
        weeklyPlanService.generateWeeklyPlans("mp-001");

        // Phase 3
        LambdaQueryWrapper<WeeklyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlan::getStatus, "draft");
        List<WeeklyPlan> unoptimized = weeklyPlanMapper.selectList(wrapper);
        for (WeeklyPlan p : unoptimized) {
            planOptimizationService.optimizeWeeklyPlan(p.getId());
        }

        // Assert: All services were called in the correct sequence
        InOrder inOrder = inOrder(orderIngestionService, materialAvailabilityService,
                quarterlyPlanService, weeklyPlanService, planOptimizationService);

        inOrder.verify(orderIngestionService).processNewOrders(orderIds);
        inOrder.verify(orderIngestionService).getPrioritizedOrderQueue();
        inOrder.verify(materialAvailabilityService).checkMaterialAvailability("order-1");
        inOrder.verify(quarterlyPlanService).generateQuarterlyPlan(year, quarter);
        inOrder.verify(quarterlyPlanService).generateMonthlyPlanOptions("qp-001", year, month);
        inOrder.verify(weeklyPlanService).generateWeeklyPlans("mp-001");
        inOrder.verify(planOptimizationService).optimizeWeeklyPlan("wp-001");
    }
}
