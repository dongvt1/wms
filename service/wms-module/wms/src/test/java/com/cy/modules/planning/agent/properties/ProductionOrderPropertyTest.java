package com.cy.modules.planning.agent.properties;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.dto.BomStructure;
import com.cy.modules.planning.agent.dto.MaterialIssuanceRequest;
import com.cy.modules.planning.agent.dto.ProductionOrderRequest;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.enums.PlanStatus;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.InventorySyncService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.impl.ProductionOrderIssuanceServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeContainer;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Production Order Issuance Service.
 *
 * **Validates: Requirements 8.2, 8.3, 8.6**
 *
 * Property 19: Production order completeness — Each Production Order includes
 *              product spec, quantity, assigned line, assigned machine, start time,
 *              and completion time.
 * Property 20: Plan status transition on full acknowledgment — When all Production
 *              Orders for a Weekly Plan are acknowledged by ERP, the plan status
 *              transitions to "In Execution" and issuance timestamp is recorded.
 */
@Tag("property-test")
@Tag("ai-production-planning")
class ProductionOrderPropertyTest {

    @BeforeContainer
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, WeeklyPlan.class);
        TableInfoHelper.initTableInfo(assistant, WeeklyPlanBatch.class);
    }

    // ==================== Service factory ====================

    /**
     * Create a ProductionOrderIssuanceServiceImpl with mocked dependencies.
     * When allErpSuccess=true, ERP calls succeed; when false, they throw exceptions.
     */
    private ProductionOrderIssuanceServiceImpl createService(
            WeeklyPlan weeklyPlan,
            List<WeeklyPlanBatch> batches,
            boolean allErpSuccess,
            List<ProductionOrderRequest> capturedRequests,
            List<WeeklyPlan> capturedPlanUpdates) {

        ProductionOrderIssuanceServiceImpl service = new ProductionOrderIssuanceServiceImpl();

        WeeklyPlanMapper wpMapper = mock(WeeklyPlanMapper.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        ErpClient erpClient = mock(ErpClient.class);
        InventorySyncService inventorySyncService = mock(InventorySyncService.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        // Mock weekly plan lookup
        when(wpMapper.selectById(anyString())).thenReturn(weeklyPlan);
        when(wpMapper.updateById(any(WeeklyPlan.class))).thenAnswer(inv -> {
            WeeklyPlan wp = inv.getArgument(0);
            capturedPlanUpdates.add(copyWeeklyPlan(wp));
            return 1;
        });

        // Mock batch query
        when(batchMapper.selectList(any())).thenReturn(batches);
        when(batchMapper.updateById(any(WeeklyPlanBatch.class))).thenReturn(1);

        // Mock ERP client — capture production order requests
        if (allErpSuccess) {
            doAnswer(inv -> {
                ProductionOrderRequest req = inv.getArgument(0);
                capturedRequests.add(req);
                return null;
            }).when(erpClient).createProductionOrder(any(ProductionOrderRequest.class));
            doNothing().when(erpClient).triggerMaterialIssuance(any(MaterialIssuanceRequest.class));
        } else {
            doThrow(new RuntimeException("ERP connection failed"))
                    .when(erpClient).createProductionOrder(any(ProductionOrderRequest.class));
        }

        // Mock inventory sync — return a simple BOM for material issuance
        BomStructure bom = BomStructure.builder()
                .productId("PRODUCT-A")
                .productName("Product A")
                .bomVersion("1.0")
                .items(List.of(
                        BomStructure.BomItem.builder()
                                .materialId("MAT-001")
                                .materialName("Material 1")
                                .quantityPerUnit(new BigDecimal("2.5"))
                                .unit("kg")
                                .scrapRate(new BigDecimal("0.05"))
                                .build()
                ))
                .build();
        when(inventorySyncService.getBom(anyString())).thenReturn(bom);

        inject(service, "erpClient", erpClient);
        inject(service, "weeklyPlanMapper", wpMapper);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "inventorySyncService", inventorySyncService);
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

    private WeeklyPlan copyWeeklyPlan(WeeklyPlan source) {
        WeeklyPlan copy = new WeeklyPlan();
        copy.setId(source.getId());
        copy.setPlanCode(source.getPlanCode());
        copy.setMonthlyPlanId(source.getMonthlyPlanId());
        copy.setYear(source.getYear());
        copy.setWeekNumber(source.getWeekNumber());
        copy.setStartDate(source.getStartDate());
        copy.setEndDate(source.getEndDate());
        copy.setOptimizationScore(source.getOptimizationScore());
        copy.setOptionRank(source.getOptionRank());
        copy.setStatus(source.getStatus());
        copy.setMaterialVerified(source.getMaterialVerified());
        copy.setApprovedBy(source.getApprovedBy());
        copy.setApprovedTime(source.getApprovedTime());
        copy.setIssuedTime(source.getIssuedTime());
        copy.setVersion(source.getVersion());
        copy.setParentPlanId(source.getParentPlanId());
        copy.setSysOrgCode(source.getSysOrgCode());
        return copy;
    }

    // ==================== Builders ====================

    private WeeklyPlan buildApprovedWeeklyPlan(String id) {
        WeeklyPlan wp = new WeeklyPlan();
        wp.setId(id);
        wp.setPlanCode("WP2025W05-001");
        wp.setMonthlyPlanId("monthly-001");
        wp.setYear(2025);
        wp.setWeekNumber(5);
        wp.setOptionRank(1);
        wp.setStatus(PlanStatus.APPROVED.getValue());
        wp.setVersion(1);
        wp.setMaterialVerified(1);
        wp.setSysOrgCode("ORG001");
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.FEBRUARY, 3, 0, 0, 0);
        wp.setStartDate(cal.getTime());
        cal.set(2025, Calendar.FEBRUARY, 9, 23, 59, 59);
        wp.setEndDate(cal.getTime());
        return wp;
    }

    private WeeklyPlanBatch buildBatch(String weeklyPlanId, int index,
                                        String productType, BigDecimal quantity,
                                        String lineId, String machineId,
                                        Date plannedStart, Date plannedEnd) {
        WeeklyPlanBatch batch = new WeeklyPlanBatch();
        batch.setId(UUID.randomUUID().toString());
        batch.setWeeklyPlanId(weeklyPlanId);
        batch.setOrderId("order-" + index);
        batch.setProductType(productType);
        batch.setQuantity(quantity);
        batch.setProductionLineId(lineId);
        batch.setMachineId(machineId);
        batch.setPlannedStart(plannedStart);
        batch.setPlannedEnd(plannedEnd);
        batch.setSequenceOrder(index);
        batch.setChangeoverMinutes(index == 0 ? 0 : 15);
        batch.setStatus("planned");
        batch.setMaterialStatus("verified");
        batch.setSysOrgCode("ORG001");
        return batch;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<Integer> batchCounts() {
        return Arbitraries.integers().between(1, 8);
    }

    @Provide
    Arbitrary<BigDecimal> quantities() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("1"), new BigDecimal("10000"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<String> productTypes() {
        return Arbitraries.of("ProductA", "ProductB", "ProductC", "ProductD", "ProductE");
    }

    @Provide
    Arbitrary<String> lineIds() {
        return Arbitraries.of("LINE-01", "LINE-02", "LINE-03", "LINE-04");
    }

    @Provide
    Arbitrary<String> machineIds() {
        return Arbitraries.of("MACHINE-A1", "MACHINE-A2", "MACHINE-B1", "MACHINE-B2", "MACHINE-C1");
    }

    // ==================== Property 19 ====================

    /**
     * Property 19: Production order completeness.
     * Each Production Order includes product spec (productType/productId), quantity,
     * assigned production line, assigned machine, start time, and completion time.
     *
     * **Validates: Requirements 8.2**
     */
    @Property(tries = 200)
    void eachProductionOrderContainsAllRequiredFields(
            @ForAll("batchCounts") int batchCount,
            @ForAll("quantities") BigDecimal quantity,
            @ForAll("productTypes") String productType,
            @ForAll("lineIds") String lineId,
            @ForAll("machineIds") String machineId) {

        String weeklyPlanId = UUID.randomUUID().toString();
        WeeklyPlan weeklyPlan = buildApprovedWeeklyPlan(weeklyPlanId);

        // Build batches with the generated values
        List<WeeklyPlanBatch> batches = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.FEBRUARY, 3, 8, 0, 0);

        for (int i = 0; i < batchCount; i++) {
            Date start = cal.getTime();
            cal.add(Calendar.HOUR, 8);
            Date end = cal.getTime();
            cal.add(Calendar.MINUTE, 30); // gap between batches

            batches.add(buildBatch(weeklyPlanId, i, productType, quantity, lineId, machineId, start, end));
        }

        List<ProductionOrderRequest> capturedRequests = new ArrayList<>();
        List<WeeklyPlan> capturedPlanUpdates = new ArrayList<>();
        ProductionOrderIssuanceServiceImpl service = createService(
                weeklyPlan, batches, true, capturedRequests, capturedPlanUpdates);

        service.issueProductionOrders(weeklyPlanId);

        // Assert: one production order request per batch
        assertThat(capturedRequests)
                .as("Should create one production order per batch")
                .hasSize(batchCount);

        // Assert: each production order contains all required fields
        for (int i = 0; i < capturedRequests.size(); i++) {
            ProductionOrderRequest request = capturedRequests.get(i);

            // Product specification (productType or productId)
            assertThat(request.getProductType())
                    .as("Production order %d must include product type", i)
                    .isNotNull()
                    .isNotEmpty();
            assertThat(request.getProductId())
                    .as("Production order %d must include product ID", i)
                    .isNotNull()
                    .isNotEmpty();

            // Quantity
            assertThat(request.getQuantity())
                    .as("Production order %d must include quantity", i)
                    .isNotNull()
                    .isGreaterThan(BigDecimal.ZERO);

            // Assigned production line
            assertThat(request.getProductionLineId())
                    .as("Production order %d must include assigned production line", i)
                    .isNotNull()
                    .isNotEmpty();

            // Assigned machine
            assertThat(request.getMachineId())
                    .as("Production order %d must include assigned machine", i)
                    .isNotNull()
                    .isNotEmpty();

            // Start time
            assertThat(request.getPlannedStartTime())
                    .as("Production order %d must include start time", i)
                    .isNotNull();

            // Completion time
            assertThat(request.getPlannedEndTime())
                    .as("Production order %d must include completion time", i)
                    .isNotNull();

            // Additional: start time must be before end time
            assertThat(request.getPlannedStartTime())
                    .as("Production order %d start time must be before end time", i)
                    .isBefore(request.getPlannedEndTime());

            // Verify the values match the batch input
            assertThat(request.getProductType())
                    .as("Production order %d product type must match batch", i)
                    .isEqualTo(productType);
            assertThat(request.getQuantity())
                    .as("Production order %d quantity must match batch", i)
                    .isEqualByComparingTo(quantity);
            assertThat(request.getProductionLineId())
                    .as("Production order %d line must match batch", i)
                    .isEqualTo(lineId);
            assertThat(request.getMachineId())
                    .as("Production order %d machine must match batch", i)
                    .isEqualTo(machineId);
        }
    }

    /**
     * Property 19b: Production order completeness — weekly plan ID is included
     * in each production order request for traceability.
     *
     * **Validates: Requirements 8.2**
     */
    @Property(tries = 100)
    void eachProductionOrderIncludesWeeklyPlanReference(
            @ForAll("batchCounts") int batchCount,
            @ForAll("quantities") BigDecimal quantity) {

        String weeklyPlanId = UUID.randomUUID().toString();
        WeeklyPlan weeklyPlan = buildApprovedWeeklyPlan(weeklyPlanId);

        List<WeeklyPlanBatch> batches = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.FEBRUARY, 3, 8, 0, 0);

        for (int i = 0; i < batchCount; i++) {
            Date start = cal.getTime();
            cal.add(Calendar.HOUR, 8);
            Date end = cal.getTime();
            cal.add(Calendar.MINUTE, 30);

            batches.add(buildBatch(weeklyPlanId, i, "ProductA", quantity,
                    "LINE-01", "MACHINE-A1", start, end));
        }

        List<ProductionOrderRequest> capturedRequests = new ArrayList<>();
        List<WeeklyPlan> capturedPlanUpdates = new ArrayList<>();
        ProductionOrderIssuanceServiceImpl service = createService(
                weeklyPlan, batches, true, capturedRequests, capturedPlanUpdates);

        service.issueProductionOrders(weeklyPlanId);

        // Assert: each production order references the weekly plan
        for (int i = 0; i < capturedRequests.size(); i++) {
            ProductionOrderRequest request = capturedRequests.get(i);
            assertThat(request.getWeeklyPlanId())
                    .as("Production order %d must reference the weekly plan", i)
                    .isEqualTo(weeklyPlanId);
            assertThat(request.getBatchId())
                    .as("Production order %d must reference the batch", i)
                    .isNotNull()
                    .isNotEmpty();
        }
    }

    // ==================== Property 20 ====================

    /**
     * Property 20: Plan status transition on full acknowledgment.
     * When all Production Orders for a Weekly Plan are acknowledged by ERP
     * (all succeed), the plan status transitions to "in_execution" and
     * issuance timestamp is recorded.
     *
     * **Validates: Requirements 8.6**
     */
    @Property(tries = 200)
    void allOrdersAcknowledgedTransitionsPlanToInExecution(
            @ForAll("batchCounts") int batchCount,
            @ForAll("quantities") BigDecimal quantity,
            @ForAll("productTypes") String productType,
            @ForAll("lineIds") String lineId,
            @ForAll("machineIds") String machineId) {

        String weeklyPlanId = UUID.randomUUID().toString();
        WeeklyPlan weeklyPlan = buildApprovedWeeklyPlan(weeklyPlanId);

        // Build batches
        List<WeeklyPlanBatch> batches = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.FEBRUARY, 3, 8, 0, 0);

        for (int i = 0; i < batchCount; i++) {
            Date start = cal.getTime();
            cal.add(Calendar.HOUR, 8);
            Date end = cal.getTime();
            cal.add(Calendar.MINUTE, 30);

            batches.add(buildBatch(weeklyPlanId, i, productType, quantity, lineId, machineId, start, end));
        }

        List<ProductionOrderRequest> capturedRequests = new ArrayList<>();
        List<WeeklyPlan> capturedPlanUpdates = new ArrayList<>();
        ProductionOrderIssuanceServiceImpl service = createService(
                weeklyPlan, batches, true, capturedRequests, capturedPlanUpdates);

        // Record time before execution
        Date beforeExecution = new Date();

        service.issueProductionOrders(weeklyPlanId);

        // Assert: plan status was updated
        assertThat(capturedPlanUpdates)
                .as("Plan should be updated when all orders succeed")
                .isNotEmpty();

        WeeklyPlan updatedPlan = capturedPlanUpdates.get(capturedPlanUpdates.size() - 1);

        // Assert: status transitions to "in_execution"
        assertThat(updatedPlan.getStatus())
                .as("Plan status should transition to 'in_execution' when all orders acknowledged")
                .isEqualTo(PlanStatus.IN_EXECUTION.getValue());

        // Assert: issuance timestamp is recorded
        assertThat(updatedPlan.getIssuedTime())
                .as("Issuance timestamp should be recorded")
                .isNotNull();

        // Assert: issuance timestamp is reasonable (not before we started)
        assertThat(updatedPlan.getIssuedTime())
                .as("Issuance timestamp should be at or after execution start")
                .isAfterOrEqualTo(beforeExecution);
    }

    /**
     * Property 20b: When any Production Order fails (material issuance not acknowledged),
     * the plan status does NOT transition to "in_execution".
     *
     * **Validates: Requirements 8.6**
     */
    @Property(tries = 100)
    void failedMaterialIssuancePreventsStatusTransition(
            @ForAll("batchCounts") int batchCount,
            @ForAll("quantities") BigDecimal quantity) {

        String weeklyPlanId = UUID.randomUUID().toString();
        WeeklyPlan weeklyPlan = buildApprovedWeeklyPlan(weeklyPlanId);

        // Build batches
        List<WeeklyPlanBatch> batches = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.FEBRUARY, 3, 8, 0, 0);

        for (int i = 0; i < batchCount; i++) {
            Date start = cal.getTime();
            cal.add(Calendar.HOUR, 8);
            Date end = cal.getTime();
            cal.add(Calendar.MINUTE, 30);

            batches.add(buildBatch(weeklyPlanId, i, "ProductA", quantity,
                    "LINE-01", "MACHINE-A1", start, end));
        }

        // Create service where ERP order creation succeeds but material issuance fails
        ProductionOrderIssuanceServiceImpl service = new ProductionOrderIssuanceServiceImpl();

        WeeklyPlanMapper wpMapper = mock(WeeklyPlanMapper.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        ErpClient erpClient = mock(ErpClient.class);
        InventorySyncService inventorySyncService = mock(InventorySyncService.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        when(wpMapper.selectById(anyString())).thenReturn(weeklyPlan);
        List<WeeklyPlan> capturedPlanUpdates = new ArrayList<>();
        when(wpMapper.updateById(any(WeeklyPlan.class))).thenAnswer(inv -> {
            WeeklyPlan wp = inv.getArgument(0);
            capturedPlanUpdates.add(copyWeeklyPlan(wp));
            return 1;
        });
        when(batchMapper.selectList(any())).thenReturn(batches);
        when(batchMapper.updateById(any(WeeklyPlanBatch.class))).thenReturn(1);

        // ERP order creation succeeds
        doNothing().when(erpClient).createProductionOrder(any(ProductionOrderRequest.class));
        // Material issuance fails
        doThrow(new RuntimeException("WMS material issuance failed"))
                .when(erpClient).triggerMaterialIssuance(any(MaterialIssuanceRequest.class));

        // BOM with items (needed for material issuance to be attempted)
        BomStructure bom = BomStructure.builder()
                .productId("ProductA")
                .productName("Product A")
                .bomVersion("1.0")
                .items(List.of(
                        BomStructure.BomItem.builder()
                                .materialId("MAT-001")
                                .materialName("Material 1")
                                .quantityPerUnit(new BigDecimal("2.0"))
                                .unit("kg")
                                .scrapRate(BigDecimal.ZERO)
                                .build()
                ))
                .build();
        when(inventorySyncService.getBom(anyString())).thenReturn(bom);

        inject(service, "erpClient", erpClient);
        inject(service, "weeklyPlanMapper", wpMapper);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "inventorySyncService", inventorySyncService);
        inject(service, "planningNotificationService", notifService);

        service.issueProductionOrders(weeklyPlanId);

        // Assert: plan status was NOT updated to in_execution
        boolean transitionedToInExecution = capturedPlanUpdates.stream()
                .anyMatch(wp -> PlanStatus.IN_EXECUTION.getValue().equals(wp.getStatus()));

        assertThat(transitionedToInExecution)
                .as("Plan should NOT transition to 'in_execution' when material issuance fails")
                .isFalse();

        // Assert: issuance timestamp should NOT be set on the original plan
        assertThat(weeklyPlan.getIssuedTime())
                .as("Issuance timestamp should NOT be set when not all orders are acknowledged")
                .isNull();
    }

    /**
     * Property 20c: Material issuance is triggered for each successfully created
     * production order (validates Requirement 8.3).
     *
     * **Validates: Requirements 8.3**
     */
    @Property(tries = 100)
    void materialIssuanceTriggeredForEachSuccessfulOrder(
            @ForAll("batchCounts") int batchCount,
            @ForAll("quantities") BigDecimal quantity,
            @ForAll("lineIds") String lineId) {

        String weeklyPlanId = UUID.randomUUID().toString();
        WeeklyPlan weeklyPlan = buildApprovedWeeklyPlan(weeklyPlanId);

        List<WeeklyPlanBatch> batches = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.FEBRUARY, 3, 8, 0, 0);

        for (int i = 0; i < batchCount; i++) {
            Date start = cal.getTime();
            cal.add(Calendar.HOUR, 8);
            Date end = cal.getTime();
            cal.add(Calendar.MINUTE, 30);

            batches.add(buildBatch(weeklyPlanId, i, "ProductA", quantity,
                    lineId, "MACHINE-A1", start, end));
        }

        // Create service with a spy on ErpClient to verify material issuance calls
        ProductionOrderIssuanceServiceImpl service = new ProductionOrderIssuanceServiceImpl();

        WeeklyPlanMapper wpMapper = mock(WeeklyPlanMapper.class);
        WeeklyPlanBatchMapper batchMapper = mock(WeeklyPlanBatchMapper.class);
        ErpClient erpClient = mock(ErpClient.class);
        InventorySyncService inventorySyncService = mock(InventorySyncService.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        when(wpMapper.selectById(anyString())).thenReturn(weeklyPlan);
        when(wpMapper.updateById(any(WeeklyPlan.class))).thenReturn(1);
        when(batchMapper.selectList(any())).thenReturn(batches);
        when(batchMapper.updateById(any(WeeklyPlanBatch.class))).thenReturn(1);

        // ERP succeeds
        doNothing().when(erpClient).createProductionOrder(any(ProductionOrderRequest.class));

        // Capture material issuance requests
        ArgumentCaptor<MaterialIssuanceRequest> issuanceCaptor =
                ArgumentCaptor.forClass(MaterialIssuanceRequest.class);
        doNothing().when(erpClient).triggerMaterialIssuance(any(MaterialIssuanceRequest.class));

        // BOM with items
        BomStructure bom = BomStructure.builder()
                .productId("ProductA")
                .productName("Product A")
                .bomVersion("1.0")
                .items(List.of(
                        BomStructure.BomItem.builder()
                                .materialId("MAT-001")
                                .materialName("Material 1")
                                .quantityPerUnit(new BigDecimal("2.0"))
                                .unit("kg")
                                .scrapRate(BigDecimal.ZERO)
                                .build()
                ))
                .build();
        when(inventorySyncService.getBom(anyString())).thenReturn(bom);

        inject(service, "erpClient", erpClient);
        inject(service, "weeklyPlanMapper", wpMapper);
        inject(service, "weeklyPlanBatchMapper", batchMapper);
        inject(service, "inventorySyncService", inventorySyncService);
        inject(service, "planningNotificationService", notifService);

        service.issueProductionOrders(weeklyPlanId);

        // Verify material issuance was triggered for each batch
        verify(erpClient, times(batchCount)).triggerMaterialIssuance(issuanceCaptor.capture());

        List<MaterialIssuanceRequest> issuanceRequests = issuanceCaptor.getAllValues();
        assertThat(issuanceRequests)
                .as("Material issuance should be triggered for each batch")
                .hasSize(batchCount);

        // Verify each issuance request has correct target production line
        for (MaterialIssuanceRequest req : issuanceRequests) {
            assertThat(req.getTargetProductionLineId())
                    .as("Material issuance must specify target production line")
                    .isEqualTo(lineId);

            assertThat(req.getMaterials())
                    .as("Material issuance must include material items from BOM")
                    .isNotNull()
                    .isNotEmpty();

            // Verify material quantity = BOM quantity per unit × batch quantity
            for (MaterialIssuanceRequest.MaterialItem item : req.getMaterials()) {
                BigDecimal expectedQty = new BigDecimal("2.0").multiply(quantity);
                assertThat(item.getQuantity())
                        .as("Material quantity should be BOM qty per unit × batch quantity")
                        .isEqualByComparingTo(expectedQty);
            }
        }
    }
}
