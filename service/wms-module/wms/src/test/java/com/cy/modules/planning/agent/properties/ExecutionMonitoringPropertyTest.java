package com.cy.modules.planning.agent.properties;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.client.ScadaClient;
import com.cy.modules.planning.agent.dto.WarehouseReceiptRequest;
import com.cy.modules.planning.agent.entity.ProductionProgress;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.enums.BatchStatus;
import com.cy.modules.planning.agent.mapper.ProductionProgressMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.MachineSyncService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.ReschedulingService;
import com.cy.modules.planning.agent.service.impl.ProductionExecutionMonitorImpl;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeContainer;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Production Execution Monitor.
 *
 * **Validates: Requirements 9.2, 9.4**
 *
 * Property 21: Daily production metrics calculation — Daily production results include
 *              quantities produced, defect rates, and completion percentage against plan.
 *              deviation_percentage = ((actual - planned) / planned) × 100
 * Property 22: Material return threshold — When remaining materials after production
 *              exceed minimum returnable quantity, a material return request is generated;
 *              when remaining ≤ minimum, no return request is generated.
 */
@Tag("property-test")
@Tag("ai-production-planning")
class ExecutionMonitoringPropertyTest {

    @BeforeContainer
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, WeeklyPlan.class);
        TableInfoHelper.initTableInfo(assistant, WeeklyPlanBatch.class);
        TableInfoHelper.initTableInfo(assistant, ProductionProgress.class);
    }

    // ==================== Service factory ====================

    /**
     * Create a ProductionExecutionMonitorImpl with mocked dependencies for daily results testing.
     * The progressRecords list simulates existing progress records in the database.
     */
    private ProductionExecutionMonitorImpl createServiceForDailyResults(
            List<WeeklyPlanBatch> batches,
            Map<String, ProductionProgress> existingProgress,
            List<ProductionProgress> capturedInserts,
            List<ProductionProgress> capturedUpdates) {

        ProductionExecutionMonitorImpl service = new ProductionExecutionMonitorImpl();

        WeeklyPlanMapper wpMapper = mock(WeeklyPlanMapper.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        ProductionProgressMapper progressMapper = mock(ProductionProgressMapper.class);
        ScadaClient scadaClient = mock(ScadaClient.class);
        ErpClient erpClient = mock(ErpClient.class);
        MachineSyncService machineSyncService = mock(MachineSyncService.class);
        ReschedulingService reschedulingService = mock(ReschedulingService.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        // Mock batch query — return the provided batches
        when(batchMapper.selectList(any())).thenReturn(batches);

        // Mock progress lookup — return existing progress or null
        when(progressMapper.selectOne(any())).thenAnswer(inv -> {
            // Return existing progress based on batch ID matching
            for (Map.Entry<String, ProductionProgress> entry : existingProgress.entrySet()) {
                return entry.getValue();
            }
            return null;
        });

        // For more precise matching, override per-batch
        for (WeeklyPlanBatch batch : batches) {
            ProductionProgress existing = existingProgress.get(batch.getId());
            // We'll handle this in the test setup
        }

        // Capture inserts
        when(progressMapper.insert(any(ProductionProgress.class))).thenAnswer(inv -> {
            ProductionProgress pp = inv.getArgument(0);
            capturedInserts.add(copyProgress(pp));
            return 1;
        });

        // Capture updates
        when(progressMapper.updateById(any(ProductionProgress.class))).thenAnswer(inv -> {
            ProductionProgress pp = inv.getArgument(0);
            capturedUpdates.add(copyProgress(pp));
            return 1;
        });

        inject(service, "scadaClient", scadaClient);
        inject(service, "erpClient", erpClient);
        inject(service, "weeklyPlanMapper", wpMapper);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "productionProgressMapper", progressMapper);
        inject(service, "machineSyncService", machineSyncService);
        inject(service, "reschedulingService", reschedulingService);
        inject(service, "planningNotificationService", notifService);

        return service;
    }

    /**
     * Create a ProductionExecutionMonitorImpl for material return testing.
     */
    private ProductionExecutionMonitorImpl createServiceForMaterialReturn(
            WeeklyPlanBatch batch,
            List<WarehouseReceiptRequest> capturedReturnRequests) {

        ProductionExecutionMonitorImpl service = new ProductionExecutionMonitorImpl();

        WeeklyPlanMapper wpMapper = mock(WeeklyPlanMapper.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        ProductionProgressMapper progressMapper = mock(ProductionProgressMapper.class);
        ScadaClient scadaClient = mock(ScadaClient.class);
        ErpClient erpClient = mock(ErpClient.class);
        MachineSyncService machineSyncService = mock(MachineSyncService.class);
        ReschedulingService reschedulingService = mock(ReschedulingService.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        // Mock batch lookup
        when(batchMapper.selectById(anyString())).thenReturn(batch);

        // Capture warehouse receipt (material return) requests
        doAnswer(inv -> {
            WarehouseReceiptRequest req = inv.getArgument(0);
            capturedReturnRequests.add(req);
            return null;
        }).when(erpClient).recordWarehouseReceipt(any(WarehouseReceiptRequest.class));

        inject(service, "scadaClient", scadaClient);
        inject(service, "erpClient", erpClient);
        inject(service, "weeklyPlanMapper", wpMapper);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "productionProgressMapper", progressMapper);
        inject(service, "machineSyncService", machineSyncService);
        inject(service, "reschedulingService", reschedulingService);
        inject(service, "planningNotificationService", notifService);

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

    private ProductionProgress copyProgress(ProductionProgress source) {
        ProductionProgress copy = new ProductionProgress();
        copy.setId(source.getId());
        copy.setWeeklyPlanId(source.getWeeklyPlanId());
        copy.setBatchId(source.getBatchId());
        copy.setProductionLineId(source.getProductionLineId());
        copy.setReportDate(source.getReportDate());
        copy.setPlannedQty(source.getPlannedQty());
        copy.setActualQty(source.getActualQty());
        copy.setDefectQty(source.getDefectQty());
        copy.setDefectRate(source.getDefectRate());
        copy.setDeviationPct(source.getDeviationPct());
        copy.setCompletionPct(source.getCompletionPct());
        copy.setMachineStatus(source.getMachineStatus());
        copy.setNotes(source.getNotes());
        copy.setCreateTime(source.getCreateTime());
        copy.setSysOrgCode(source.getSysOrgCode());
        return copy;
    }

    // ==================== Builders ====================

    private WeeklyPlanBatch buildBatch(String batchId, String weeklyPlanId,
                                        BigDecimal plannedQty, BigDecimal grossQty,
                                        BigDecimal actualQty) {
        WeeklyPlanBatch batch = new WeeklyPlanBatch();
        batch.setId(batchId);
        batch.setWeeklyPlanId(weeklyPlanId);
        batch.setOrderId("order-001");
        batch.setProductType("ProductA");
        batch.setQuantity(plannedQty);
        batch.setGrossQuantity(grossQty);
        batch.setActualQuantity(actualQty);
        batch.setProductionLineId("LINE-01");
        batch.setMachineId("MACHINE-A1");
        batch.setSequenceOrder(1);
        batch.setChangeoverMinutes(0);
        batch.setStatus(BatchStatus.COMPLETED.getValue());
        batch.setMaterialStatus("verified");
        batch.setSysOrgCode("ORG001");

        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.FEBRUARY, 3, 8, 0, 0);
        batch.setPlannedStart(cal.getTime());
        cal.add(Calendar.HOUR, 8);
        batch.setPlannedEnd(cal.getTime());
        return batch;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<BigDecimal> positiveQuantities() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("1"), new BigDecimal("10000"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> nonNegativeQuantities() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("10000"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> defectQuantities() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("5000"))
                .ofScale(2);
    }

    // ==================== Property 21 ====================

    /**
     * Property 21: Daily production metrics calculation.
     * For any actual and planned quantities, the daily metrics are correctly calculated:
     * - deviation_percentage = ((actual - planned) / planned) × 100
     * - defect_rate = defect_qty / actual_qty (when actual_qty > 0)
     * - completion_pct = actual_qty / planned_qty × 100 (when planned_qty > 0)
     *
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 300)
    void dailyMetricsCalculatedCorrectly(
            @ForAll("positiveQuantities") BigDecimal plannedQty,
            @ForAll("nonNegativeQuantities") BigDecimal actualQty,
            @ForAll("defectQuantities") BigDecimal defectQty) {

        // Ensure defect_qty ≤ actual_qty (can't have more defects than produced)
        if (defectQty.compareTo(actualQty) > 0) {
            defectQty = actualQty;
        }

        String weeklyPlanId = UUID.randomUUID().toString();
        String batchId = UUID.randomUUID().toString();

        // Build a batch with the planned quantity
        WeeklyPlanBatch batch = buildBatch(batchId, weeklyPlanId, plannedQty, null, null);
        batch.setStatus(BatchStatus.IN_PROGRESS.getValue());

        // Create existing progress record with actual and defect quantities
        ProductionProgress existingProgress = new ProductionProgress();
        existingProgress.setId(UUID.randomUUID().toString());
        existingProgress.setWeeklyPlanId(weeklyPlanId);
        existingProgress.setBatchId(batchId);
        existingProgress.setProductionLineId("LINE-01");
        existingProgress.setReportDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        existingProgress.setPlannedQty(plannedQty);
        existingProgress.setActualQty(actualQty);
        existingProgress.setDefectQty(defectQty);
        existingProgress.setCreateTime(new Date());

        Map<String, ProductionProgress> existingMap = new HashMap<>();
        existingMap.put(batchId, existingProgress);

        List<ProductionProgress> capturedInserts = new ArrayList<>();
        List<ProductionProgress> capturedUpdates = new ArrayList<>();

        // Create service with mocked dependencies
        ProductionExecutionMonitorImpl service = new ProductionExecutionMonitorImpl();

        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        ProductionProgressMapper progressMapper = mock(ProductionProgressMapper.class);

        when(batchMapper.selectList(any())).thenReturn(List.of(batch));
        when(progressMapper.selectOne(any())).thenReturn(existingProgress);
        when(progressMapper.updateById(any(ProductionProgress.class))).thenAnswer(inv -> {
            ProductionProgress pp = inv.getArgument(0);
            capturedUpdates.add(copyProgress(pp));
            return 1;
        });
        when(progressMapper.insert(any(ProductionProgress.class))).thenAnswer(inv -> {
            ProductionProgress pp = inv.getArgument(0);
            capturedInserts.add(copyProgress(pp));
            return 1;
        });

        inject(service, "scadaClient", mock(ScadaClient.class));
        inject(service, "erpClient", mock(ErpClient.class));
        inject(service, "weeklyPlanMapper", mock(WeeklyPlanMapper.class));
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "productionProgressMapper", progressMapper);
        inject(service, "machineSyncService", mock(MachineSyncService.class));
        inject(service, "reschedulingService", mock(ReschedulingService.class));
        inject(service, "planningNotificationService", mock(PlanningNotificationService.class));
        inject(service, "eventPublisher", mock(ApplicationEventPublisher.class));

        // Execute
        service.calculateDailyResults(weeklyPlanId, LocalDate.now());

        // Get the updated progress record
        assertThat(capturedUpdates)
                .as("Should update existing progress record")
                .isNotEmpty();

        ProductionProgress result = capturedUpdates.get(0);

        // Verify deviation_percentage = ((actual - planned) / planned) × 100
        BigDecimal expectedDeviation = actualQty.subtract(plannedQty)
                .divide(plannedQty, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(result.getDeviationPct())
                .as("deviation_pct should equal ((actual - planned) / planned) × 100")
                .isEqualByComparingTo(expectedDeviation);

        // Verify defect_rate = defect_qty / actual_qty
        BigDecimal expectedDefectRate;
        if (actualQty.compareTo(BigDecimal.ZERO) > 0) {
            expectedDefectRate = defectQty.divide(actualQty, 4, RoundingMode.HALF_UP);
        } else {
            expectedDefectRate = BigDecimal.ZERO;
        }

        assertThat(result.getDefectRate())
                .as("defect_rate should equal defect_qty / actual_qty")
                .isEqualByComparingTo(expectedDefectRate);

        // Verify completion_pct = actual_qty / planned_qty × 100
        BigDecimal expectedCompletionPct = actualQty.divide(plannedQty, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(result.getCompletionPct())
                .as("completion_pct should equal actual_qty / planned_qty × 100")
                .isEqualByComparingTo(expectedCompletionPct);
    }

    /**
     * Property 21b: When actual_qty is zero, defect_rate should be zero and
     * completion_pct should be zero.
     *
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 100)
    void dailyMetricsWithZeroActualQuantity(
            @ForAll("positiveQuantities") BigDecimal plannedQty) {

        BigDecimal actualQty = BigDecimal.ZERO;
        BigDecimal defectQty = BigDecimal.ZERO;

        String weeklyPlanId = UUID.randomUUID().toString();
        String batchId = UUID.randomUUID().toString();

        WeeklyPlanBatch batch = buildBatch(batchId, weeklyPlanId, plannedQty, null, null);
        batch.setStatus(BatchStatus.IN_PROGRESS.getValue());

        ProductionProgress existingProgress = new ProductionProgress();
        existingProgress.setId(UUID.randomUUID().toString());
        existingProgress.setWeeklyPlanId(weeklyPlanId);
        existingProgress.setBatchId(batchId);
        existingProgress.setProductionLineId("LINE-01");
        existingProgress.setReportDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        existingProgress.setPlannedQty(plannedQty);
        existingProgress.setActualQty(actualQty);
        existingProgress.setDefectQty(defectQty);
        existingProgress.setCreateTime(new Date());

        List<ProductionProgress> capturedUpdates = new ArrayList<>();

        ProductionExecutionMonitorImpl service = new ProductionExecutionMonitorImpl();

        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        ProductionProgressMapper progressMapper = mock(ProductionProgressMapper.class);

        when(batchMapper.selectList(any())).thenReturn(List.of(batch));
        when(progressMapper.selectOne(any())).thenReturn(existingProgress);
        when(progressMapper.updateById(any(ProductionProgress.class))).thenAnswer(inv -> {
            ProductionProgress pp = inv.getArgument(0);
            capturedUpdates.add(copyProgress(pp));
            return 1;
        });

        inject(service, "scadaClient", mock(ScadaClient.class));
        inject(service, "erpClient", mock(ErpClient.class));
        inject(service, "weeklyPlanMapper", mock(WeeklyPlanMapper.class));
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "productionProgressMapper", progressMapper);
        inject(service, "machineSyncService", mock(MachineSyncService.class));
        inject(service, "reschedulingService", mock(ReschedulingService.class));
        inject(service, "planningNotificationService", mock(PlanningNotificationService.class));
        inject(service, "eventPublisher", mock(org.springframework.context.ApplicationEventPublisher.class));

        service.calculateDailyResults(weeklyPlanId, LocalDate.now());

        assertThat(capturedUpdates).isNotEmpty();
        ProductionProgress result = capturedUpdates.get(0);

        // When actual is zero, defect rate should be zero
        assertThat(result.getDefectRate())
                .as("defect_rate should be zero when actual_qty is zero")
                .isEqualByComparingTo(BigDecimal.ZERO);

        // When actual is zero, completion should be zero
        assertThat(result.getCompletionPct())
                .as("completion_pct should be zero when actual_qty is zero")
                .isEqualByComparingTo(BigDecimal.ZERO);

        // Deviation should be -100% when actual is zero
        BigDecimal expectedDeviation = new BigDecimal("-100.00");
        assertThat(result.getDeviationPct())
                .as("deviation_pct should be -100% when actual_qty is zero")
                .isEqualByComparingTo(expectedDeviation);
    }

    // ==================== Property 22 ====================

    /**
     * Property 22: Material return threshold — when remaining > minimum returnable quantity,
     * a material return request IS generated.
     *
     * remaining = gross_quantity - actual_quantity
     * minimum returnable quantity = 1 (default in implementation)
     *
     * **Validates: Requirements 9.4**
     */
    @Property(tries = 300)
    void materialReturnGeneratedWhenRemainingExceedsMinimum(
            @ForAll("positiveQuantities") BigDecimal grossQty,
            @ForAll("positiveQuantities") BigDecimal actualQty) {

        // Ensure remaining > 1 (minimum returnable quantity)
        BigDecimal minimumReturnable = BigDecimal.ONE;
        BigDecimal remaining = grossQty.subtract(actualQty);

        // Only test cases where remaining > minimum
        Assume.that(remaining.compareTo(minimumReturnable) > 0);

        String batchId = UUID.randomUUID().toString();
        WeeklyPlanBatch batch = buildBatch(batchId, "wp-001", actualQty, grossQty, actualQty);

        List<WarehouseReceiptRequest> capturedReturnRequests = new ArrayList<>();
        ProductionExecutionMonitorImpl service = createServiceForMaterialReturn(batch, capturedReturnRequests);

        // Execute
        service.generateMaterialReturn(batchId);

        // Assert: a material return request was generated
        assertThat(capturedReturnRequests)
                .as("Material return request should be generated when remaining (%s) > minimum (%s)",
                        remaining, minimumReturnable)
                .hasSize(1);

        WarehouseReceiptRequest returnReq = capturedReturnRequests.get(0);

        // Assert: return quantity equals remaining
        assertThat(returnReq.getQuantity())
                .as("Return quantity should equal remaining materials (gross - actual)")
                .isEqualByComparingTo(remaining);

        // Assert: target warehouse is MATERIAL_RETURN
        assertThat(returnReq.getTargetWarehouse())
                .as("Target warehouse should be MATERIAL_RETURN")
                .isEqualTo("MATERIAL_RETURN");

        // Assert: batch ID is correct
        assertThat(returnReq.getBatchId())
                .as("Return request should reference the correct batch")
                .isEqualTo(batchId);
    }

    /**
     * Property 22b: Material return threshold — when remaining ≤ minimum returnable quantity,
     * NO material return request is generated.
     *
     * **Validates: Requirements 9.4**
     */
    @Property(tries = 300)
    void noMaterialReturnWhenRemainingBelowOrEqualMinimum(
            @ForAll("positiveQuantities") BigDecimal grossQty,
            @ForAll("positiveQuantities") BigDecimal actualQty) {

        BigDecimal minimumReturnable = BigDecimal.ONE;
        BigDecimal remaining = grossQty.subtract(actualQty);

        // Only test cases where remaining ≤ minimum (including negative remaining)
        Assume.that(remaining.compareTo(minimumReturnable) <= 0);

        String batchId = UUID.randomUUID().toString();
        WeeklyPlanBatch batch = buildBatch(batchId, "wp-001", actualQty, grossQty, actualQty);

        List<WarehouseReceiptRequest> capturedReturnRequests = new ArrayList<>();
        ProductionExecutionMonitorImpl service = createServiceForMaterialReturn(batch, capturedReturnRequests);

        // Execute
        service.generateMaterialReturn(batchId);

        // Assert: NO material return request was generated
        assertThat(capturedReturnRequests)
                .as("No material return request should be generated when remaining (%s) ≤ minimum (%s)",
                        remaining, minimumReturnable)
                .isEmpty();
    }

    /**
     * Property 22c: When gross_quantity is null, the implementation falls back to
     * using the planned quantity. Material return is generated only when
     * (planned_qty - actual_qty) > minimum.
     *
     * **Validates: Requirements 9.4**
     */
    @Property(tries = 100)
    void materialReturnUsesPlannedQtyWhenGrossIsNull(
            @ForAll("positiveQuantities") BigDecimal plannedQty,
            @ForAll("positiveQuantities") BigDecimal actualQty) {

        BigDecimal minimumReturnable = BigDecimal.ONE;
        BigDecimal remaining = plannedQty.subtract(actualQty);

        // Only test cases where remaining > minimum
        Assume.that(remaining.compareTo(minimumReturnable) > 0);

        String batchId = UUID.randomUUID().toString();
        // Build batch with null gross quantity
        WeeklyPlanBatch batch = buildBatch(batchId, "wp-001", plannedQty, null, actualQty);

        List<WarehouseReceiptRequest> capturedReturnRequests = new ArrayList<>();
        ProductionExecutionMonitorImpl service = createServiceForMaterialReturn(batch, capturedReturnRequests);

        // Execute
        service.generateMaterialReturn(batchId);

        // Assert: return request generated with remaining = planned - actual
        assertThat(capturedReturnRequests)
                .as("Material return should be generated using planned_qty when gross is null")
                .hasSize(1);

        assertThat(capturedReturnRequests.get(0).getQuantity())
                .as("Return quantity should be planned_qty - actual_qty when gross is null")
                .isEqualByComparingTo(remaining);
    }
}
