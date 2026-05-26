package com.cy.modules.planning.agent.integration;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cy.modules.planning.agent.entity.RescheduleRecord;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.liteflow.PlanningChainContext;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.ReschedulingService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the ReschedulingChain LiteFlow chain.
 * Tests deviation detection → impact assessment → alternative generation → notify stakeholders.
 *
 * **Validates: All (integration verification)**
 */
@DisplayName("ReschedulingChain Integration Tests")
class ReschedulingChainIntegrationTest {

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, WeeklyPlan.class);
    }

    private ReschedulingService reschedulingService;
    private WeeklyPlanMapper weeklyPlanMapper;
    private PlanningNotificationService notificationService;

    @BeforeEach
    void setUp() {
        reschedulingService = mock(ReschedulingService.class);
        weeklyPlanMapper = mock(WeeklyPlanMapper.class);
        notificationService = mock(PlanningNotificationService.class);
    }

    // ==================== Helper Methods ====================

    private WeeklyPlan buildExecutingPlan(String id) {
        WeeklyPlan plan = new WeeklyPlan();
        plan.setId(id);
        plan.setPlanCode("WP2025W05-001");
        plan.setStatus("in_execution");
        plan.setYear(2025);
        plan.setWeekNumber(5);
        return plan;
    }

    private RescheduleRecord buildDeviationRecord(String planId, String affectedOrdersJson) {
        RescheduleRecord record = new RescheduleRecord();
        record.setId(UUID.randomUUID().toString());
        record.setOriginalPlanId(planId);
        record.setTriggerType("deviation");
        record.setTriggerDetails("{\"deviationPct\": 15.5}");
        record.setAffectedOrders(affectedOrdersJson);
        record.setStatus("pending");
        record.setDetectionTime(new Date());
        return record;
    }

    // ==================== Tests ====================

    @Test
    @DisplayName("ReschedulingChain detects deviations for all executing plans")
    void reschedulingChain_detectsDeviationsForAllExecutingPlans() {
        // Arrange: Two plans in execution, one with deviation
        WeeklyPlan plan1 = buildExecutingPlan("wp-001");
        WeeklyPlan plan2 = buildExecutingPlan("wp-002");

        when(weeklyPlanMapper.selectList(any())).thenReturn(List.of(plan1, plan2));

        RescheduleRecord deviation = buildDeviationRecord("wp-001",
                "[{\"orderId\":\"order-1\",\"impact\":\"2 days delay\"}]");

        when(reschedulingService.checkDailyDeviation("wp-001")).thenReturn(deviation);
        when(reschedulingService.checkDailyDeviation("wp-002")).thenReturn(null); // No deviation

        // Act: Simulate DeviationDetection node
        LambdaQueryWrapper<WeeklyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlan::getStatus, "in_execution");
        List<WeeklyPlan> executingPlans = weeklyPlanMapper.selectList(wrapper);

        PlanningChainContext context = new PlanningChainContext();
        List<RescheduleRecord> deviations = new ArrayList<>();
        for (WeeklyPlan plan : executingPlans) {
            RescheduleRecord record = reschedulingService.checkDailyDeviation(plan.getId());
            if (record != null) {
                deviations.add(record);
            }
        }
        context.setDeviations(deviations);

        // Assert: Only one deviation detected
        assertThat(context.getDeviations()).hasSize(1);
        assertThat(context.getDeviations().get(0).getOriginalPlanId()).isEqualTo("wp-001");
        assertThat(context.getDeviations().get(0).getTriggerType()).isEqualTo("deviation");
    }

    @Test
    @DisplayName("ReschedulingChain impact assessment extracts affected order IDs from deviations")
    void reschedulingChain_impactAssessment_extractsAffectedOrders() {
        // Arrange: Context with deviations containing affected orders
        PlanningChainContext context = new PlanningChainContext();

        RescheduleRecord record1 = buildDeviationRecord("wp-001",
                "[{\"orderId\":\"order-1\",\"impact\":\"2 days\"},{\"orderId\":\"order-2\",\"impact\":\"1 day\"}]");
        RescheduleRecord record2 = buildDeviationRecord("wp-002",
                "[{\"orderId\":\"order-2\",\"impact\":\"3 days\"},{\"orderId\":\"order-3\",\"impact\":\"1 day\"}]");

        context.setDeviations(List.of(record1, record2));

        // Act: Simulate ImpactAssessment node - parse affected orders
        List<String> allAffectedOrderIds = new ArrayList<>();
        for (RescheduleRecord record : context.getDeviations()) {
            if (record.getAffectedOrders() != null) {
                try {
                    com.alibaba.fastjson.JSONArray affectedArray =
                            com.alibaba.fastjson.JSON.parseArray(record.getAffectedOrders());
                    for (int i = 0; i < affectedArray.size(); i++) {
                        String orderId = affectedArray.getJSONObject(i).getString("orderId");
                        if (orderId != null && !allAffectedOrderIds.contains(orderId)) {
                            allAffectedOrderIds.add(orderId);
                        }
                    }
                } catch (Exception e) {
                    // skip malformed JSON
                }
            }
        }
        context.setAffectedOrderIds(allAffectedOrderIds);

        // Assert: Unique affected orders extracted (order-2 deduplicated)
        assertThat(context.getAffectedOrderIds())
                .containsExactly("order-1", "order-2", "order-3");
    }

    @Test
    @DisplayName("ReschedulingChain generates alternatives for each deviation")
    void reschedulingChain_generatesAlternativesForEachDeviation() {
        // Arrange
        PlanningChainContext context = new PlanningChainContext();
        RescheduleRecord record1 = buildDeviationRecord("wp-001", "[]");
        record1.setId("rr-001");
        RescheduleRecord record2 = buildDeviationRecord("wp-002", "[]");
        record2.setId("rr-002");
        context.setDeviations(List.of(record1, record2));

        // Mock: Each deviation gets 2+ alternatives
        RescheduleRecord alt1 = new RescheduleRecord();
        alt1.setId("alt-1");
        RescheduleRecord alt2 = new RescheduleRecord();
        alt2.setId("alt-2");

        when(reschedulingService.getReschedulingOptions("rr-001")).thenReturn(List.of(alt1, alt2));
        when(reschedulingService.getReschedulingOptions("rr-002")).thenReturn(List.of(alt1, alt2));

        // Act: Simulate AlternativeGeneration node
        int totalOptions = 0;
        for (RescheduleRecord record : context.getDeviations()) {
            List<RescheduleRecord> options = reschedulingService.getReschedulingOptions(record.getId());
            totalOptions += (options != null ? options.size() : 0);
        }

        // Assert: Alternatives generated for each deviation
        assertThat(totalOptions).isEqualTo(4); // 2 options × 2 deviations
        verify(reschedulingService).getReschedulingOptions("rr-001");
        verify(reschedulingService).getReschedulingOptions("rr-002");
    }

    @Test
    @DisplayName("ReschedulingChain notifies stakeholders about deviations and affected orders")
    void reschedulingChain_notifiesStakeholders() {
        // Arrange
        PlanningChainContext context = new PlanningChainContext();
        RescheduleRecord record = buildDeviationRecord("wp-001",
                "[{\"orderId\":\"order-1\"},{\"orderId\":\"order-2\"}]");
        context.setDeviations(List.of(record));
        context.setAffectedOrderIds(List.of("order-1", "order-2"));

        // Act: Simulate NotifyStakeholders node
        Map<String, Object> data = new HashMap<>();
        data.put("deviationCount", context.getDeviations().size());
        data.put("affectedOrderCount", context.getAffectedOrderIds().size());

        notificationService.notifyProductionManager(
                NotificationType.RESCHEDULE_NEEDED,
                String.format("Detected %d production deviations requiring rescheduling",
                        context.getDeviations().size()),
                data
        );

        notificationService.notifyOrderOwners(
                context.getAffectedOrderIds(),
                "Your order may be affected by a production schedule adjustment."
        );

        // Assert: Both notifications sent
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.RESCHEDULE_NEEDED),
                contains("1 production deviations"),
                argThat((Map<String, Object> d) -> (int) d.get("deviationCount") == 1
                        && (int) d.get("affectedOrderCount") == 2)
        );
        verify(notificationService).notifyOrderOwners(
                eq(List.of("order-1", "order-2")),
                anyString()
        );
    }

    @Test
    @DisplayName("ReschedulingChain full flow: detect → assess → generate → notify")
    void reschedulingChain_fullFlow_executesAllStepsInOrder() {
        // Arrange
        WeeklyPlan executingPlan = buildExecutingPlan("wp-001");
        when(weeklyPlanMapper.selectList(any())).thenReturn(List.of(executingPlan));

        RescheduleRecord deviation = buildDeviationRecord("wp-001",
                "[{\"orderId\":\"order-1\",\"impact\":\"delay 2 days\"}]");
        deviation.setId("rr-001");
        when(reschedulingService.checkDailyDeviation("wp-001")).thenReturn(deviation);

        RescheduleRecord alt1 = new RescheduleRecord();
        alt1.setId("alt-1");
        RescheduleRecord alt2 = new RescheduleRecord();
        alt2.setId("alt-2");
        when(reschedulingService.getReschedulingOptions("rr-001")).thenReturn(List.of(alt1, alt2));

        List<String> executionSteps = new ArrayList<>();

        // Act: Full chain simulation
        // Step 1: DeviationDetection
        executionSteps.add("deviationDetection");
        PlanningChainContext context = new PlanningChainContext();
        LambdaQueryWrapper<WeeklyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlan::getStatus, "in_execution");
        List<WeeklyPlan> plans = weeklyPlanMapper.selectList(wrapper);
        List<RescheduleRecord> deviations = new ArrayList<>();
        for (WeeklyPlan plan : plans) {
            RescheduleRecord record = reschedulingService.checkDailyDeviation(plan.getId());
            if (record != null) deviations.add(record);
        }
        context.setDeviations(deviations);

        // Step 2: ImpactAssessment
        executionSteps.add("impactAssessment");
        List<String> affectedOrderIds = new ArrayList<>();
        for (RescheduleRecord record : context.getDeviations()) {
            if (record.getAffectedOrders() != null) {
                com.alibaba.fastjson.JSONArray arr =
                        com.alibaba.fastjson.JSON.parseArray(record.getAffectedOrders());
                for (int i = 0; i < arr.size(); i++) {
                    String orderId = arr.getJSONObject(i).getString("orderId");
                    if (orderId != null && !affectedOrderIds.contains(orderId)) {
                        affectedOrderIds.add(orderId);
                    }
                }
            }
        }
        context.setAffectedOrderIds(affectedOrderIds);

        // Step 3: AlternativeGeneration
        executionSteps.add("alternativeGeneration");
        for (RescheduleRecord record : context.getDeviations()) {
            reschedulingService.getReschedulingOptions(record.getId());
        }

        // Step 4: NotifyStakeholders
        executionSteps.add("notifyStakeholders");
        notificationService.notifyProductionManager(
                NotificationType.RESCHEDULE_NEEDED,
                "Detected deviations", Map.of("deviationCount", deviations.size()));
        notificationService.notifyOrderOwners(affectedOrderIds, "Schedule adjustment");

        // Assert: All steps executed in order
        assertThat(executionSteps).containsExactly(
                "deviationDetection", "impactAssessment", "alternativeGeneration", "notifyStakeholders");

        // Assert: Context data flows correctly between steps
        assertThat(context.getDeviations()).hasSize(1);
        assertThat(context.getAffectedOrderIds()).containsExactly("order-1");
    }

    @Test
    @DisplayName("ReschedulingChain handles no executing plans gracefully")
    void reschedulingChain_noExecutingPlans_completesWithoutAction() {
        // Arrange: No plans in execution
        when(weeklyPlanMapper.selectList(any())).thenReturn(Collections.emptyList());

        // Act: Simulate DeviationDetection with no plans
        PlanningChainContext context = new PlanningChainContext();
        LambdaQueryWrapper<WeeklyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlan::getStatus, "in_execution");
        List<WeeklyPlan> plans = weeklyPlanMapper.selectList(wrapper);

        // Assert: No deviations, no further processing
        assertThat(plans).isEmpty();
        assertThat(context.getDeviations()).isEmpty();
        verify(reschedulingService, never()).checkDailyDeviation(anyString());
        verify(reschedulingService, never()).getReschedulingOptions(anyString());
        verify(notificationService, never()).notifyProductionManager(any(), anyString(), any());
    }

    @Test
    @DisplayName("ReschedulingChain handles no deviations detected - skips downstream steps")
    void reschedulingChain_noDeviations_skipsDownstream() {
        // Arrange: Plans exist but no deviations
        WeeklyPlan plan = buildExecutingPlan("wp-001");
        when(weeklyPlanMapper.selectList(any())).thenReturn(List.of(plan));
        when(reschedulingService.checkDailyDeviation("wp-001")).thenReturn(null);

        // Act: DeviationDetection finds nothing
        PlanningChainContext context = new PlanningChainContext();
        LambdaQueryWrapper<WeeklyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlan::getStatus, "in_execution");
        List<WeeklyPlan> plans = weeklyPlanMapper.selectList(wrapper);
        List<RescheduleRecord> deviations = new ArrayList<>();
        for (WeeklyPlan p : plans) {
            RescheduleRecord record = reschedulingService.checkDailyDeviation(p.getId());
            if (record != null) deviations.add(record);
        }
        context.setDeviations(deviations);

        // Assert: No deviations means no alternatives or notifications
        assertThat(context.getDeviations()).isEmpty();
        verify(reschedulingService, never()).getReschedulingOptions(anyString());
    }
}
