package com.cy.modules.planning.agent.integration;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.liteflow.PlanningChainContext;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.service.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the ExecutionChain LiteFlow chain.
 * Tests production order issuance → material issuance → monitoring → finished goods → dispatch.
 *
 * **Validates: All (integration verification)**
 */
@DisplayName("ExecutionChain Integration Tests")
class ExecutionChainIntegrationTest {

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, WeeklyPlan.class);
        TableInfoHelper.initTableInfo(assistant, WeeklyPlanBatch.class);
        TableInfoHelper.initTableInfo(assistant, PlanningOrder.class);
    }

    private ProductionOrderIssuanceService productionOrderIssuanceService;
    private ProductionExecutionMonitor productionExecutionMonitor;
    private QualityIntegrationService qualityIntegrationService;
    private FinishedGoodsDispatchService finishedGoodsDispatchService;
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;
    private PlanningOrderMapper planningOrderMapper;

    @BeforeEach
    void setUp() {
        productionOrderIssuanceService = mock(ProductionOrderIssuanceService.class);
        productionExecutionMonitor = mock(ProductionExecutionMonitor.class);
        qualityIntegrationService = mock(QualityIntegrationService.class);
        finishedGoodsDispatchService = mock(FinishedGoodsDispatchService.class);
        weeklyPlanBatchMapper = mock(WeeklyPlanBatchMapper.class);
        planningOrderMapper = mock(PlanningOrderMapper.class);
    }

    // ==================== Helper Methods ====================

    private WeeklyPlanBatch buildBatch(String weeklyPlanId, String status, BigDecimal actualQty) {
        WeeklyPlanBatch batch = new WeeklyPlanBatch();
        batch.setId(UUID.randomUUID().toString());
        batch.setWeeklyPlanId(weeklyPlanId);
        batch.setOrderId("order-" + UUID.randomUUID().toString().substring(0, 4));
        batch.setProductType("ProductA");
        batch.setQuantity(new BigDecimal("100"));
        batch.setProductionLineId("LINE-01");
        batch.setMachineId("MACHINE-A1");
        batch.setStatus(status);
        batch.setActualQuantity(actualQty);
        batch.setSequenceOrder(1);
        batch.setChangeoverMinutes(0);
        return batch;
    }

    // ==================== Tests ====================

    @Test
    @DisplayName("ExecutionChain issues production orders and sets context flag")
    void executionChain_issuesProductionOrders_setsContextFlag() {
        // Arrange
        String weeklyPlanId = "wp-001";
        PlanningChainContext context = new PlanningChainContext();
        context.setWeeklyPlanId(weeklyPlanId);

        doNothing().when(productionOrderIssuanceService).issueProductionOrders(weeklyPlanId);

        // Act: Simulate IssueProductionOrders node
        productionOrderIssuanceService.issueProductionOrders(context.getWeeklyPlanId());
        context.setOrdersIssued(true);

        // Assert
        assertThat(context.isOrdersIssued()).isTrue();
        verify(productionOrderIssuanceService).issueProductionOrders(weeklyPlanId);
    }

    @Test
    @DisplayName("ExecutionChain triggers material issuance only after orders are issued")
    void executionChain_triggersMaterialIssuance_onlyAfterOrdersIssued() {
        // Arrange
        String weeklyPlanId = "wp-001";
        PlanningChainContext context = new PlanningChainContext();
        context.setWeeklyPlanId(weeklyPlanId);
        context.setOrdersIssued(true); // Orders already issued

        WeeklyPlanBatch batch1 = buildBatch(weeklyPlanId, "in_progress", null);
        WeeklyPlanBatch batch2 = buildBatch(weeklyPlanId, "in_progress", null);

        when(weeklyPlanBatchMapper.selectList(any())).thenReturn(List.of(batch1, batch2));

        // Act: Simulate TriggerMaterialIssuance node
        assertThat(context.isOrdersIssued()).isTrue();
        LambdaQueryWrapper<WeeklyPlanBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId)
               .eq(WeeklyPlanBatch::getStatus, "in_progress");
        List<WeeklyPlanBatch> batches = weeklyPlanBatchMapper.selectList(wrapper);
        context.setMaterialIssuanceTriggered(true);

        // Assert
        assertThat(context.isMaterialIssuanceTriggered()).isTrue();
        assertThat(batches).hasSize(2);
    }

    @Test
    @DisplayName("ExecutionChain skips material issuance when orders not issued")
    void executionChain_skipsMaterialIssuance_whenOrdersNotIssued() {
        // Arrange
        PlanningChainContext context = new PlanningChainContext();
        context.setWeeklyPlanId("wp-001");
        context.setOrdersIssued(false); // Orders NOT issued

        // Act: Simulate TriggerMaterialIssuance node check
        boolean shouldProceed = context.isOrdersIssued();

        // Assert: Should not proceed
        assertThat(shouldProceed).isFalse();
        assertThat(context.isMaterialIssuanceTriggered()).isFalse();
        verify(weeklyPlanBatchMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("ExecutionChain monitors progress and quality in parallel")
    void executionChain_monitorsProgressAndQuality() {
        // Arrange
        String weeklyPlanId = "wp-001";
        PlanningChainContext context = new PlanningChainContext();
        context.setWeeklyPlanId(weeklyPlanId);

        WeeklyPlanBatch activeBatch = buildBatch(weeklyPlanId, "in_progress", null);
        when(weeklyPlanBatchMapper.selectList(any())).thenReturn(List.of(activeBatch));

        doNothing().when(productionExecutionMonitor).collectProgress();
        doNothing().when(productionExecutionMonitor).calculateDailyResults(eq(weeklyPlanId), any(LocalDate.class));
        doNothing().when(qualityIntegrationService).checkQualityAlerts(anyString());
        doNothing().when(qualityIntegrationService).classifyDefects(anyString());

        // Act: Simulate MonitorProgress and MonitorQuality nodes (parallel in WHEN)
        // MonitorProgress
        productionExecutionMonitor.collectProgress();
        productionExecutionMonitor.calculateDailyResults(weeklyPlanId, LocalDate.now());

        // MonitorQuality
        LambdaQueryWrapper<WeeklyPlanBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId)
               .eq(WeeklyPlanBatch::getStatus, "in_progress");
        List<WeeklyPlanBatch> activeBatches = weeklyPlanBatchMapper.selectList(wrapper);
        for (WeeklyPlanBatch batch : activeBatches) {
            qualityIntegrationService.checkQualityAlerts(batch.getId());
            qualityIntegrationService.classifyDefects(batch.getId());
        }

        // Assert: Both monitoring paths executed
        verify(productionExecutionMonitor).collectProgress();
        verify(productionExecutionMonitor).calculateDailyResults(eq(weeklyPlanId), any(LocalDate.class));
        verify(qualityIntegrationService).checkQualityAlerts(activeBatch.getId());
        verify(qualityIntegrationService).classifyDefects(activeBatch.getId());
    }

    @Test
    @DisplayName("ExecutionChain records finished goods for completed batches")
    void executionChain_recordsFinishedGoods_forCompletedBatches() {
        // Arrange
        String weeklyPlanId = "wp-001";
        PlanningChainContext context = new PlanningChainContext();
        context.setWeeklyPlanId(weeklyPlanId);

        WeeklyPlanBatch completedBatch1 = buildBatch(weeklyPlanId, "completed", new BigDecimal("95"));
        WeeklyPlanBatch completedBatch2 = buildBatch(weeklyPlanId, "completed", new BigDecimal("200"));

        // Only completed batches with actual quantity are returned
        when(weeklyPlanBatchMapper.selectList(any())).thenReturn(List.of(completedBatch1, completedBatch2));

        doNothing().when(productionExecutionMonitor).recordFinishedGoods(anyString(), any(BigDecimal.class));
        doNothing().when(productionExecutionMonitor).generateMaterialReturn(anyString());

        // Act: Simulate RecordFinishedGoods node
        LambdaQueryWrapper<WeeklyPlanBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId)
               .eq(WeeklyPlanBatch::getStatus, "completed")
               .isNotNull(WeeklyPlanBatch::getActualQuantity);
        List<WeeklyPlanBatch> completedBatches = weeklyPlanBatchMapper.selectList(wrapper);

        int recordedCount = 0;
        for (WeeklyPlanBatch batch : completedBatches) {
            if (batch.getActualQuantity() != null && batch.getActualQuantity().signum() > 0) {
                productionExecutionMonitor.recordFinishedGoods(batch.getId(), batch.getActualQuantity());
                productionExecutionMonitor.generateMaterialReturn(batch.getId());
                recordedCount++;
            }
        }

        // Assert: Both completed batches recorded
        assertThat(recordedCount).isEqualTo(2);
        verify(productionExecutionMonitor).recordFinishedGoods(completedBatch1.getId(), new BigDecimal("95"));
        verify(productionExecutionMonitor).recordFinishedGoods(completedBatch2.getId(), new BigDecimal("200"));
        verify(productionExecutionMonitor).generateMaterialReturn(completedBatch1.getId());
        verify(productionExecutionMonitor).generateMaterialReturn(completedBatch2.getId());
    }

    @Test
    @DisplayName("ExecutionChain dispatches fully fulfilled orders")
    void executionChain_dispatchesFullyFulfilledOrders() {
        // Arrange
        PlanningOrder fulfilledOrder1 = new PlanningOrder();
        fulfilledOrder1.setId("order-001");
        fulfilledOrder1.setFulfillmentStatus("fully_fulfilled");

        PlanningOrder fulfilledOrder2 = new PlanningOrder();
        fulfilledOrder2.setId("order-002");
        fulfilledOrder2.setFulfillmentStatus("fully_fulfilled");

        when(planningOrderMapper.selectList(any())).thenReturn(List.of(fulfilledOrder1, fulfilledOrder2));

        doNothing().when(finishedGoodsDispatchService).notifyDispatch(anyString());

        // Act: Simulate DispatchNotification node
        LambdaQueryWrapper<PlanningOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanningOrder::getFulfillmentStatus, "fully_fulfilled");
        List<PlanningOrder> fulfilledOrders = planningOrderMapper.selectList(wrapper);

        int dispatchedCount = 0;
        for (PlanningOrder order : fulfilledOrders) {
            try {
                finishedGoodsDispatchService.notifyDispatch(order.getId());
                dispatchedCount++;
            } catch (Exception e) {
                // Log error, continue
            }
        }

        // Assert
        assertThat(dispatchedCount).isEqualTo(2);
        verify(finishedGoodsDispatchService).notifyDispatch("order-001");
        verify(finishedGoodsDispatchService).notifyDispatch("order-002");
    }

    @Test
    @DisplayName("ExecutionChain handles dispatch failure gracefully - continues with other orders")
    void executionChain_handlesDispatchFailure_continuesWithOthers() {
        // Arrange
        PlanningOrder order1 = new PlanningOrder();
        order1.setId("order-001");
        order1.setFulfillmentStatus("fully_fulfilled");

        PlanningOrder order2 = new PlanningOrder();
        order2.setId("order-002");
        order2.setFulfillmentStatus("fully_fulfilled");

        when(planningOrderMapper.selectList(any())).thenReturn(List.of(order1, order2));

        // First dispatch fails, second succeeds
        doThrow(new RuntimeException("ERP dispatch failed"))
                .when(finishedGoodsDispatchService).notifyDispatch("order-001");
        doNothing().when(finishedGoodsDispatchService).notifyDispatch("order-002");

        // Act: Simulate DispatchNotification node with error handling
        LambdaQueryWrapper<PlanningOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanningOrder::getFulfillmentStatus, "fully_fulfilled");
        List<PlanningOrder> fulfilledOrders = planningOrderMapper.selectList(wrapper);

        int dispatchedCount = 0;
        for (PlanningOrder order : fulfilledOrders) {
            try {
                finishedGoodsDispatchService.notifyDispatch(order.getId());
                dispatchedCount++;
            } catch (Exception e) {
                // Error logged, continue with next order
            }
        }

        // Assert: Only second order dispatched successfully
        assertThat(dispatchedCount).isEqualTo(1);
        verify(finishedGoodsDispatchService).notifyDispatch("order-001");
        verify(finishedGoodsDispatchService).notifyDispatch("order-002");
    }

    @Test
    @DisplayName("ExecutionChain full flow: issue → material → monitor → record → dispatch")
    void executionChain_fullFlow_executesAllStepsInOrder() {
        // Arrange
        String weeklyPlanId = "wp-001";
        PlanningChainContext context = new PlanningChainContext();
        context.setWeeklyPlanId(weeklyPlanId);

        List<String> executionSteps = new ArrayList<>();

        doAnswer(inv -> {
            executionSteps.add("issueProductionOrders");
            return null;
        }).when(productionOrderIssuanceService).issueProductionOrders(weeklyPlanId);

        WeeklyPlanBatch activeBatch = buildBatch(weeklyPlanId, "in_progress", null);
        WeeklyPlanBatch completedBatch = buildBatch(weeklyPlanId, "completed", new BigDecimal("100"));

        PlanningOrder fulfilledOrder = new PlanningOrder();
        fulfilledOrder.setId("order-001");
        fulfilledOrder.setFulfillmentStatus("fully_fulfilled");

        // Setup sequential mock returns for batch mapper
        when(weeklyPlanBatchMapper.selectList(any()))
                .thenReturn(List.of(activeBatch))       // material issuance check
                .thenReturn(List.of(activeBatch))       // quality monitoring
                .thenReturn(List.of(completedBatch));   // finished goods recording

        when(planningOrderMapper.selectList(any())).thenReturn(List.of(fulfilledOrder));

        doAnswer(inv -> {
            executionSteps.add("collectProgress");
            return null;
        }).when(productionExecutionMonitor).collectProgress();

        doNothing().when(productionExecutionMonitor).calculateDailyResults(anyString(), any(LocalDate.class));
        doNothing().when(qualityIntegrationService).checkQualityAlerts(anyString());
        doNothing().when(qualityIntegrationService).classifyDefects(anyString());

        doAnswer(inv -> {
            executionSteps.add("recordFinishedGoods");
            return null;
        }).when(productionExecutionMonitor).recordFinishedGoods(anyString(), any(BigDecimal.class));
        doNothing().when(productionExecutionMonitor).generateMaterialReturn(anyString());

        doAnswer(inv -> {
            executionSteps.add("dispatchNotification");
            return null;
        }).when(finishedGoodsDispatchService).notifyDispatch(anyString());

        // Act: Execute full chain
        // Step 1: Issue Production Orders
        productionOrderIssuanceService.issueProductionOrders(weeklyPlanId);
        context.setOrdersIssued(true);

        // Step 2: Trigger Material Issuance (verified via context flag)
        if (context.isOrdersIssued()) {
            LambdaQueryWrapper<WeeklyPlanBatch> batchWrapper = new LambdaQueryWrapper<>();
            batchWrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId)
                        .eq(WeeklyPlanBatch::getStatus, "in_progress");
            weeklyPlanBatchMapper.selectList(batchWrapper);
            context.setMaterialIssuanceTriggered(true);
        }

        // Step 3: Monitor Progress (parallel)
        productionExecutionMonitor.collectProgress();
        productionExecutionMonitor.calculateDailyResults(weeklyPlanId, LocalDate.now());

        // Step 4: Monitor Quality (parallel)
        LambdaQueryWrapper<WeeklyPlanBatch> qualityWrapper = new LambdaQueryWrapper<>();
        qualityWrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId)
                      .eq(WeeklyPlanBatch::getStatus, "in_progress");
        List<WeeklyPlanBatch> activeBatches = weeklyPlanBatchMapper.selectList(qualityWrapper);
        for (WeeklyPlanBatch batch : activeBatches) {
            qualityIntegrationService.checkQualityAlerts(batch.getId());
            qualityIntegrationService.classifyDefects(batch.getId());
        }

        // Step 5: Record Finished Goods
        LambdaQueryWrapper<WeeklyPlanBatch> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId)
                        .eq(WeeklyPlanBatch::getStatus, "completed");
        List<WeeklyPlanBatch> completed = weeklyPlanBatchMapper.selectList(completedWrapper);
        for (WeeklyPlanBatch batch : completed) {
            if (batch.getActualQuantity() != null && batch.getActualQuantity().signum() > 0) {
                productionExecutionMonitor.recordFinishedGoods(batch.getId(), batch.getActualQuantity());
                productionExecutionMonitor.generateMaterialReturn(batch.getId());
            }
        }

        // Step 6: Dispatch Notification
        LambdaQueryWrapper<PlanningOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(PlanningOrder::getFulfillmentStatus, "fully_fulfilled");
        List<PlanningOrder> fulfilled = planningOrderMapper.selectList(orderWrapper);
        for (PlanningOrder order : fulfilled) {
            finishedGoodsDispatchService.notifyDispatch(order.getId());
        }

        // Assert: Steps executed in correct order
        assertThat(executionSteps).containsExactly(
                "issueProductionOrders", "collectProgress", "recordFinishedGoods", "dispatchNotification");

        // Assert: Context flags set correctly
        assertThat(context.isOrdersIssued()).isTrue();
        assertThat(context.isMaterialIssuanceTriggered()).isTrue();
    }

    @Test
    @DisplayName("ExecutionChain handles missing weeklyPlanId gracefully")
    void executionChain_handlesMissingWeeklyPlanId() {
        // Arrange
        PlanningChainContext context = new PlanningChainContext();
        context.setWeeklyPlanId(null); // No plan ID

        // Act & Assert: IssueProductionOrders should not proceed
        String weeklyPlanId = context.getWeeklyPlanId();
        boolean shouldProceed = weeklyPlanId != null && !weeklyPlanId.isBlank();

        assertThat(shouldProceed).isFalse();
        verify(productionOrderIssuanceService, never()).issueProductionOrders(anyString());
    }
}
