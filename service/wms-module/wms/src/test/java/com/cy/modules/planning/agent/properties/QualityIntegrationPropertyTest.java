package com.cy.modules.planning.agent.properties;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cy.modules.planning.agent.client.QmsClient;
import com.cy.modules.planning.agent.dto.DefectClassification;
import com.cy.modules.planning.agent.dto.QualityReport;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.QualitySyncService;
import com.cy.modules.planning.agent.service.impl.QualityIntegrationServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeContainer;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Quality Integration Service.
 *
 * **Validates: Requirements 10.2, 10.3, 10.4**
 *
 * Property 23: Quality alert threshold — Alert when defect rate exceeds
 *              30-day average by more than 5 percentage points; no alert
 *              when current ≤ average + 5pp.
 * Property 24: Gross production quantity calculation — gross_quantity =
 *              net_quantity / yield_rate using 90-day historical yield.
 * Property 25: Defect impact on net output — Destroyable quantities are
 *              subtracted from net output; additional production triggered
 *              when net output falls below order requirements.
 */
@Tag("property-test")
@Tag("ai-production-planning")
class QualityIntegrationPropertyTest {

    @BeforeContainer
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, WeeklyPlanBatch.class);
        TableInfoHelper.initTableInfo(assistant, PlanningOrder.class);
    }

    // ==================== Service factory ====================

    private QualityIntegrationServiceImpl createServiceForAlerts(
            BigDecimal avg30DayDefectRate,
            BigDecimal currentDefectRate,
            WeeklyPlanBatch batch) {

        QualityIntegrationServiceImpl service = new QualityIntegrationServiceImpl();

        QmsClient qmsClient = mock(QmsClient.class);
        QualitySyncService qualitySyncService = mock(QualitySyncService.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        PlanningOrderMapper orderMapper = mock(PlanningOrderMapper.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);
        org.springframework.context.ApplicationEventPublisher eventPublisher =
                mock(org.springframework.context.ApplicationEventPublisher.class);

        when(batchMapper.selectById(anyString())).thenReturn(batch);
        when(qualitySyncService.getDefectRate30Day(anyString(), anyString()))
                .thenReturn(avg30DayDefectRate);

        QualityReport report = QualityReport.builder()
                .averageDefectRate(currentDefectRate)
                .build();
        when(qmsClient.getQualityData(anyString(), anyString(), any(), any()))
                .thenReturn(report);

        inject(service, "qmsClient", qmsClient);
        inject(service, "qualitySyncService", qualitySyncService);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "planningOrderMapper", orderMapper);
        inject(service, "planningNotificationService", notifService);
        inject(service, "eventPublisher", eventPublisher);

        return service;
    }

    private QualityIntegrationServiceImpl createServiceForGrossCalc(
            BigDecimal yieldRate90Day, boolean isStale) {

        QualityIntegrationServiceImpl service = new QualityIntegrationServiceImpl();

        QmsClient qmsClient = mock(QmsClient.class);
        QualitySyncService qualitySyncService = mock(QualitySyncService.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        PlanningOrderMapper orderMapper = mock(PlanningOrderMapper.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        when(qualitySyncService.getYieldRate90Day(anyString(), anyString()))
                .thenReturn(yieldRate90Day);
        when(qualitySyncService.isDataStale()).thenReturn(isStale);

        inject(service, "qmsClient", qmsClient);
        inject(service, "qualitySyncService", qualitySyncService);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "planningOrderMapper", orderMapper);
        inject(service, "planningNotificationService", notifService);

        return service;
    }

    private QualityIntegrationServiceImpl createServiceForDefectImpact(
            WeeklyPlanBatch batch,
            PlanningOrder order,
            List<WeeklyPlanBatch> relatedBatches,
            DefectClassification classification,
            List<WeeklyPlanBatch> capturedUpdates) {

        QualityIntegrationServiceImpl service = new QualityIntegrationServiceImpl();

        QmsClient qmsClient = mock(QmsClient.class);
        QualitySyncService qualitySyncService = mock(QualitySyncService.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        PlanningOrderMapper orderMapper = mock(PlanningOrderMapper.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        when(batchMapper.selectById(anyString())).thenReturn(batch);
        when(batchMapper.selectList(any())).thenReturn(relatedBatches);
        when(batchMapper.updateById(any(WeeklyPlanBatch.class))).thenAnswer(inv -> {
            WeeklyPlanBatch updated = inv.getArgument(0);
            capturedUpdates.add(copyBatch(updated));
            return 1;
        });
        when(orderMapper.selectById(anyString())).thenReturn(order);
        when(qmsClient.classifyDefects(anyString())).thenReturn(classification);

        inject(service, "qmsClient", qmsClient);
        inject(service, "qualitySyncService", qualitySyncService);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "planningOrderMapper", orderMapper);
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

    private WeeklyPlanBatch copyBatch(WeeklyPlanBatch source) {
        WeeklyPlanBatch copy = new WeeklyPlanBatch();
        copy.setId(source.getId());
        copy.setWeeklyPlanId(source.getWeeklyPlanId());
        copy.setOrderId(source.getOrderId());
        copy.setProductType(source.getProductType());
        copy.setQuantity(source.getQuantity());
        copy.setGrossQuantity(source.getGrossQuantity());
        copy.setProductionLineId(source.getProductionLineId());
        copy.setMachineId(source.getMachineId());
        copy.setSequenceOrder(source.getSequenceOrder());
        copy.setChangeoverMinutes(source.getChangeoverMinutes());
        copy.setActualQuantity(source.getActualQuantity());
        copy.setStatus(source.getStatus());
        copy.setMaterialStatus(source.getMaterialStatus());
        copy.setSysOrgCode(source.getSysOrgCode());
        return copy;
    }

    // ==================== Builders ====================

    private WeeklyPlanBatch buildBatch(String batchId, String orderId,
                                        String productType, String lineId,
                                        BigDecimal quantity, BigDecimal actualQty) {
        WeeklyPlanBatch batch = new WeeklyPlanBatch();
        batch.setId(batchId);
        batch.setWeeklyPlanId("wp-001");
        batch.setOrderId(orderId);
        batch.setProductType(productType);
        batch.setQuantity(quantity);
        batch.setProductionLineId(lineId);
        batch.setMachineId("MACHINE-A1");
        batch.setSequenceOrder(1);
        batch.setChangeoverMinutes(0);
        batch.setActualQuantity(actualQty);
        batch.setStatus("in_progress");
        batch.setMaterialStatus("verified");
        batch.setSysOrgCode("ORG001");
        return batch;
    }

    private PlanningOrder buildOrder(String orderId, BigDecimal quantity,
                                     BigDecimal fulfillmentQty) {
        PlanningOrder order = new PlanningOrder();
        order.setId(orderId);
        order.setExternalOrderId("EXT-" + orderId);
        order.setProductType("ProductA");
        order.setCustomerName("Customer1");
        order.setQuantity(quantity);
        order.setFulfillmentQty(fulfillmentQty);
        order.setStatus("in_production");
        order.setValidationStatus("valid");
        order.setSysOrgCode("ORG001");
        return order;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<BigDecimal> defectRates() {
        // Defect rates as percentages: 0.00 to 50.00
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("50.00"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> yieldRates() {
        // Yield rates as percentages: 50.00 to 99.99 (realistic range)
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("50.00"), new BigDecimal("99.99"))
                .ofScale(2);
    }

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

    // ==================== Property 23 ====================

    /**
     * Property 23: Quality alert threshold — when current defect rate exceeds
     * the 30-day average by MORE than 5 percentage points, an alert IS generated.
     *
     * **Validates: Requirements 10.2**
     */
    @Property(tries = 300)
    void alertGeneratedWhenDefectRateExceedsThreshold(
            @ForAll("defectRates") BigDecimal avg30DayRate,
            @ForAll("defectRates") BigDecimal currentRate) {

        // Ensure current > avg + 5 (strictly greater)
        BigDecimal threshold = new BigDecimal("5.00");
        Assume.that(currentRate.subtract(avg30DayRate).compareTo(threshold) > 0);

        String batchId = UUID.randomUUID().toString();
        WeeklyPlanBatch batch = buildBatch(batchId, "order-001", "ProductA", "LINE-01",
                new BigDecimal("100"), new BigDecimal("50"));

        QualityIntegrationServiceImpl service = createServiceForAlerts(
                avg30DayRate, currentRate, batch);

        // Get the notification service mock
        PlanningNotificationService notifService = getNotifService(service);

        // Execute
        service.checkQualityAlerts(batchId);

        // Verify alert was generated
        ArgumentCaptor<NotificationType> typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(notifService, times(1))
                .notifyProductionManager(typeCaptor.capture(), msgCaptor.capture(), dataCaptor.capture());

        assertThat(typeCaptor.getValue())
                .as("Alert type should be QUALITY_ALERT")
                .isEqualTo(NotificationType.QUALITY_ALERT);

        Map<String, Object> alertData = dataCaptor.getValue();
        assertThat(alertData)
                .as("Alert data should contain batch and rate information")
                .containsKey("batchId")
                .containsKey("currentDefectRate")
                .containsKey("avg30DayDefectRate")
                .containsKey("suggestions");
    }

    /**
     * Property 23b: Quality alert threshold — when current defect rate does NOT exceed
     * the 30-day average by more than 5 percentage points, NO alert is generated.
     *
     * **Validates: Requirements 10.2**
     */
    @Property(tries = 300)
    void noAlertWhenDefectRateWithinThreshold(
            @ForAll("defectRates") BigDecimal avg30DayRate,
            @ForAll("defectRates") BigDecimal currentRate) {

        // Ensure current ≤ avg + 5
        BigDecimal threshold = new BigDecimal("5.00");
        Assume.that(currentRate.subtract(avg30DayRate).compareTo(threshold) <= 0);

        String batchId = UUID.randomUUID().toString();
        WeeklyPlanBatch batch = buildBatch(batchId, "order-001", "ProductA", "LINE-01",
                new BigDecimal("100"), new BigDecimal("50"));

        QualityIntegrationServiceImpl service = createServiceForAlerts(
                avg30DayRate, currentRate, batch);

        PlanningNotificationService notifService = getNotifService(service);

        // Execute
        service.checkQualityAlerts(batchId);

        // Verify NO alert was generated
        verify(notifService, never())
                .notifyProductionManager(any(NotificationType.class), anyString(), any(Map.class));
    }

    // ==================== Property 24 ====================

    /**
     * Property 24: Gross production quantity calculation.
     * For any net quantity and yield rate (> 0), gross_quantity = net_quantity / (yield_rate / 100).
     * The result is bounded and uses CEILING rounding to ensure sufficient production.
     *
     * **Validates: Requirements 10.3**
     */
    @Property(tries = 300)
    void grossQuantityCalculatedCorrectly(
            @ForAll("positiveQuantities") BigDecimal netQuantity,
            @ForAll("yieldRates") BigDecimal yieldRate) {

        QualityIntegrationServiceImpl service = createServiceForGrossCalc(yieldRate, false);

        // Execute
        BigDecimal result = service.calculateGrossQuantity("ProductA", "LINE-01", netQuantity);

        // Expected: gross = net / (yieldRate / 100)
        BigDecimal yieldDecimal = yieldRate.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal expected = netQuantity.divide(yieldDecimal, 2, RoundingMode.CEILING);

        assertThat(result)
                .as("gross_quantity should equal net_quantity / (yield_rate/100) with CEILING rounding")
                .isEqualByComparingTo(expected);

        // Gross should always be >= net (since yield < 100%)
        assertThat(result)
                .as("gross_quantity should be >= net_quantity (yield rate < 100%%)")
                .isGreaterThanOrEqualTo(netQuantity);
    }

    /**
     * Property 24b: When yield rate is null or zero, gross quantity equals net quantity
     * (fallback to 100% yield assumption).
     *
     * **Validates: Requirements 10.3**
     */
    @Property(tries = 100)
    void grossQuantityFallsBackWhenNoYieldRate(
            @ForAll("positiveQuantities") BigDecimal netQuantity) {

        QualityIntegrationServiceImpl service = createServiceForGrossCalc(null, false);

        BigDecimal result = service.calculateGrossQuantity("ProductA", "LINE-01", netQuantity);

        assertThat(result)
                .as("When yield rate is unavailable, gross should equal net (100%% yield assumed)")
                .isEqualByComparingTo(netQuantity);
    }

    // ==================== Property 25 ====================

    /**
     * Property 25: Defect impact on net output — destroyable quantities are subtracted
     * from net output (actual_quantity). The adjusted quantity is max(0, actual - destroyable).
     *
     * **Validates: Requirements 10.4**
     */
    @Property(tries = 300)
    void destroyableQuantitySubtractedFromNetOutput(
            @ForAll("positiveQuantities") BigDecimal actualQty,
            @ForAll("nonNegativeQuantities") BigDecimal destroyableQty) {

        String batchId = UUID.randomUUID().toString();
        String orderId = "order-001";

        WeeklyPlanBatch batch = buildBatch(batchId, orderId, "ProductA", "LINE-01",
                new BigDecimal("1000"), actualQty);

        DefectClassification classification = DefectClassification.builder()
                .batchId(batchId)
                .totalDefects(destroyableQty)
                .repairableQuantity(BigDecimal.ZERO)
                .destroyableQuantity(destroyableQty)
                .build();

        List<WeeklyPlanBatch> capturedUpdates = new ArrayList<>();

        // For classifyDefects, we only need batchMapper and qmsClient
        QualityIntegrationServiceImpl service = new QualityIntegrationServiceImpl();

        QmsClient qmsClient = mock(QmsClient.class);
        QualitySyncService qualitySyncService = mock(QualitySyncService.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        PlanningOrderMapper orderMapper = mock(PlanningOrderMapper.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        when(batchMapper.selectById(anyString())).thenReturn(batch);
        when(batchMapper.updateById(any(WeeklyPlanBatch.class))).thenAnswer(inv -> {
            WeeklyPlanBatch updated = inv.getArgument(0);
            capturedUpdates.add(copyBatch(updated));
            return 1;
        });
        when(qmsClient.classifyDefects(anyString())).thenReturn(classification);

        inject(service, "qmsClient", qmsClient);
        inject(service, "qualitySyncService", qualitySyncService);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "planningOrderMapper", orderMapper);
        inject(service, "planningNotificationService", notifService);

        // Execute
        service.classifyDefects(batchId);

        if (destroyableQty.compareTo(BigDecimal.ZERO) > 0) {
            // Should have updated the batch
            assertThat(capturedUpdates)
                    .as("Batch should be updated when destroyable > 0")
                    .hasSize(1);

            BigDecimal expectedAdjusted = actualQty.subtract(destroyableQty);
            if (expectedAdjusted.compareTo(BigDecimal.ZERO) < 0) {
                expectedAdjusted = BigDecimal.ZERO;
            }

            assertThat(capturedUpdates.get(0).getActualQuantity())
                    .as("Net output should be max(0, actual - destroyable)")
                    .isEqualByComparingTo(expectedAdjusted);
        } else {
            // No update when destroyable is zero
            assertThat(capturedUpdates)
                    .as("No update when destroyable quantity is zero")
                    .isEmpty();
        }
    }

    /**
     * Property 25b: Additional production triggered when net output falls below
     * order requirements. After subtracting destroyable quantities, if the total
     * net output across all batches for an order is less than the order requirement,
     * a notification for additional production is triggered.
     *
     * **Validates: Requirements 10.4**
     */
    @Property(tries = 300)
    void additionalProductionTriggeredWhenNetOutputBelowRequirement(
            @ForAll("positiveQuantities") BigDecimal orderQuantity,
            @ForAll("positiveQuantities") BigDecimal actualQty,
            @ForAll("nonNegativeQuantities") BigDecimal fulfillmentQty) {

        // Ensure net output (actualQty) < remaining requirement (orderQuantity - fulfillmentQty)
        // so additional production is triggered
        BigDecimal remainingReq = orderQuantity.subtract(fulfillmentQty);
        Assume.that(remainingReq.compareTo(BigDecimal.ZERO) > 0);
        Assume.that(actualQty.compareTo(remainingReq) < 0);

        String batchId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();

        WeeklyPlanBatch batch = buildBatch(batchId, orderId, "ProductA", "LINE-01",
                orderQuantity, actualQty);

        PlanningOrder order = buildOrder(orderId, orderQuantity, fulfillmentQty);

        // Related batches — just this one batch for simplicity
        List<WeeklyPlanBatch> relatedBatches = List.of(batch);

        List<WeeklyPlanBatch> capturedUpdates = new ArrayList<>();

        QualityIntegrationServiceImpl service = new QualityIntegrationServiceImpl();

        QmsClient qmsClient = mock(QmsClient.class);
        QualitySyncService qualitySyncService = mock(QualitySyncService.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        PlanningOrderMapper orderMapper = mock(PlanningOrderMapper.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        when(batchMapper.selectById(anyString())).thenReturn(batch);
        when(batchMapper.selectList(any())).thenReturn(relatedBatches);
        when(orderMapper.selectById(anyString())).thenReturn(order);

        inject(service, "qmsClient", qmsClient);
        inject(service, "qualitySyncService", qualitySyncService);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "planningOrderMapper", orderMapper);
        inject(service, "planningNotificationService", notifService);

        // Execute adjustForYieldLoss
        service.adjustForYieldLoss(batchId);

        // Verify notification for additional production was triggered
        ArgumentCaptor<NotificationType> typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(notifService, times(1))
                .notifyProductionManager(typeCaptor.capture(), msgCaptor.capture(), dataCaptor.capture());

        assertThat(typeCaptor.getValue())
                .as("Should trigger RESCHEDULE_NEEDED notification for additional production")
                .isEqualTo(NotificationType.RESCHEDULE_NEEDED);

        Map<String, Object> data = dataCaptor.getValue();
        assertThat(data)
                .containsKey("additionalQuantityNeeded")
                .containsKey("orderId")
                .containsKey("productType");

        BigDecimal additionalNeeded = (BigDecimal) data.get("additionalQuantityNeeded");
        BigDecimal expectedAdditional = orderQuantity.subtract(fulfillmentQty).subtract(actualQty);
        assertThat(additionalNeeded)
                .as("Additional quantity needed = orderQty - fulfillmentQty - totalNetOutput")
                .isEqualByComparingTo(expectedAdditional);
    }

    /**
     * Property 25c: No additional production triggered when net output meets
     * or exceeds order requirements.
     *
     * **Validates: Requirements 10.4**
     */
    @Property(tries = 300)
    void noAdditionalProductionWhenNetOutputMeetsRequirement(
            @ForAll("positiveQuantities") BigDecimal orderQuantity,
            @ForAll("positiveQuantities") BigDecimal actualQty,
            @ForAll("nonNegativeQuantities") BigDecimal fulfillmentQty) {

        // Ensure net output >= remaining requirement
        BigDecimal remainingReq = orderQuantity.subtract(fulfillmentQty);
        Assume.that(remainingReq.compareTo(BigDecimal.ZERO) > 0);
        Assume.that(actualQty.compareTo(remainingReq) >= 0);

        String batchId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();

        WeeklyPlanBatch batch = buildBatch(batchId, orderId, "ProductA", "LINE-01",
                orderQuantity, actualQty);

        PlanningOrder order = buildOrder(orderId, orderQuantity, fulfillmentQty);

        List<WeeklyPlanBatch> relatedBatches = List.of(batch);

        QualityIntegrationServiceImpl service = new QualityIntegrationServiceImpl();

        QmsClient qmsClient = mock(QmsClient.class);
        QualitySyncService qualitySyncService = mock(QualitySyncService.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        PlanningOrderMapper orderMapper = mock(PlanningOrderMapper.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        when(batchMapper.selectById(anyString())).thenReturn(batch);
        when(batchMapper.selectList(any())).thenReturn(relatedBatches);
        when(orderMapper.selectById(anyString())).thenReturn(order);

        inject(service, "qmsClient", qmsClient);
        inject(service, "qualitySyncService", qualitySyncService);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "planningOrderMapper", orderMapper);
        inject(service, "planningNotificationService", notifService);

        // Execute adjustForYieldLoss
        service.adjustForYieldLoss(batchId);

        // Verify NO notification was triggered
        verify(notifService, never())
                .notifyProductionManager(any(NotificationType.class), anyString(), any(Map.class));
    }

    // ==================== Helper ====================

    private PlanningNotificationService getNotifService(QualityIntegrationServiceImpl service) {
        try {
            Field field = service.getClass().getDeclaredField("planningNotificationService");
            field.setAccessible(true);
            return (PlanningNotificationService) field.get(service);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get notifService", e);
        }
    }
}
