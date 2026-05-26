package com.cy.modules.planning.agent.properties;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cy.modules.planning.agent.entity.MaterialAvailability;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.entity.PurchaseRequest;
import com.cy.modules.planning.agent.entity.SupplierLeadTime;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.mapper.MaterialAvailabilityMapper;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.mapper.PurchaseRequestMapper;
import com.cy.modules.planning.agent.mapper.SupplierLeadTimeMapper;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.impl.ProcurementCoordinationServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeContainer;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Procurement Coordination.
 *
 * **Validates: Requirements 2.4, 3.1, 3.2**
 *
 * Property 6: Purchase Request date calculation — PR required_delivery_date = production_start_date - supplier_lead_time.
 * Property 7: Alternative scenarios generation on deadline breach — When supplier delivery time exceeds
 *             production deadline, at least 2 alternative scenarios are generated.
 */
@Tag("property-test")
@Tag("ai-production-planning")
class ProcurementPropertyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initialize MyBatis-Plus lambda cache for entities used in LambdaQueryWrapper/LambdaUpdateWrapper.
     * This is required because the service uses lambda method references (e.g., MaterialAvailability::getOrderId)
     * which need the entity's TableInfo to be registered.
     */
    @BeforeContainer
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, MaterialAvailability.class);
        TableInfoHelper.initTableInfo(assistant, PurchaseRequest.class);
        TableInfoHelper.initTableInfo(assistant, SupplierLeadTime.class);
        TableInfoHelper.initTableInfo(assistant, PlanningOrder.class);
    }

    // ==================== Helper methods ====================

    /**
     * Creates a fresh ProcurementCoordinationServiceImpl with mocked dependencies.
     */
    private ProcurementCoordinationServiceImpl createServiceWithMocks(
            SupplierLeadTime supplierLeadTime,
            PlanningOrder order,
            List<MaterialAvailability> materialsByOrder,
            List<PurchaseRequest> capturedPRs) {

        ProcurementCoordinationServiceImpl service = new ProcurementCoordinationServiceImpl();

        // Mock dependencies
        PurchaseRequestMapper purchaseRequestMapper = Mockito.mock(PurchaseRequestMapper.class);
        SupplierLeadTimeMapper supplierLeadTimeMapper = Mockito.mock(SupplierLeadTimeMapper.class);
        MaterialAvailabilityMapper materialAvailabilityMapper = Mockito.mock(MaterialAvailabilityMapper.class);
        PlanningOrderMapper planningOrderMapper = Mockito.mock(PlanningOrderMapper.class);
        PlanningNotificationService notificationService = Mockito.mock(PlanningNotificationService.class);

        // Mock supplierLeadTimeMapper.selectOne - return the provided SupplierLeadTime
        when(supplierLeadTimeMapper.selectOne(any())).thenReturn(supplierLeadTime);

        // Mock planningOrderMapper.selectById
        when(planningOrderMapper.selectById(anyString())).thenReturn(order);

        // Mock materialAvailabilityMapper.selectOne - return first matching material
        when(materialAvailabilityMapper.selectOne(any())).thenAnswer(invocation -> {
            if (materialsByOrder != null && !materialsByOrder.isEmpty()) {
                return materialsByOrder.get(0);
            }
            return null;
        });

        // Mock materialAvailabilityMapper.selectList - return materials for order
        when(materialAvailabilityMapper.selectList(any())).thenReturn(
                materialsByOrder != null ? materialsByOrder : Collections.emptyList());

        // Mock materialAvailabilityMapper.update (no-op)
        when(materialAvailabilityMapper.update(any(), any())).thenReturn(1);

        // Mock purchaseRequestMapper.insert - capture inserted PRs
        when(purchaseRequestMapper.insert(any(PurchaseRequest.class))).thenAnswer(invocation -> {
            PurchaseRequest pr = invocation.getArgument(0);
            capturedPRs.add(pr);
            return 1;
        });

        // Mock purchaseRequestMapper.selectCount - return 0 for PR code generation
        when(purchaseRequestMapper.selectCount(any())).thenReturn(0L);

        // Inject mocks via reflection
        injectField(service, "purchaseRequestMapper", purchaseRequestMapper);
        injectField(service, "supplierLeadTimeMapper", supplierLeadTimeMapper);
        injectField(service, "materialAvailabilityMapper", materialAvailabilityMapper);
        injectField(service, "planningOrderMapper", planningOrderMapper);
        injectField(service, "planningNotificationService", notificationService);

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

    private PlanningOrder createOrder(String orderId, String productType, BigDecimal quantity, Date deadline) {
        PlanningOrder order = new PlanningOrder();
        order.setId(orderId);
        order.setExternalOrderId("EXT-" + orderId);
        order.setProductType(productType);
        order.setCustomerName("Test Customer");
        order.setQuantity(quantity);
        order.setDeadline(deadline);
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

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<Integer> supplierLeadTimeDays() {
        // Supplier lead time: 1 to 90 days
        return Arbitraries.integers().between(1, 90);
    }

    @Provide
    Arbitrary<Integer> productionStartDaysFromNow() {
        // Production start: 5 to 180 days from now
        return Arbitraries.integers().between(5, 180);
    }

    @Provide
    Arbitrary<BigDecimal> deficitQuantities() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("1"), new BigDecimal("5000"))
                .ofScale(3);
    }

    @Provide
    Arbitrary<Integer> deadlineDaysFromNow() {
        // Deadline: 1 to 180 days from now
        return Arbitraries.integers().between(1, 180);
    }

    // ==================== Property 6: Purchase Request date calculation ====================

    /**
     * Property 6a: The PR required_delivery_date is always equal to
     * production_start_date - supplier_lead_time_days.
     *
     * This validates the core formula: required_delivery_date = productionStartDate - leadTimeDays.
     *
     * **Validates: Requirements 2.4, 3.1**
     */
    @Property(tries = 200)
    void prRequiredDeliveryDateEqualsProductionStartMinusLeadTime(
            @ForAll("supplierLeadTimeDays") int leadTimeDays,
            @ForAll("productionStartDaysFromNow") int productionStartDays,
            @ForAll("deficitQuantities") BigDecimal deficitQty) {

        String orderId = UUID.randomUUID().toString();
        String materialId = "MAT-" + String.format("%03d", leadTimeDays % 100);
        LocalDate productionStartDate = LocalDate.now().plusDays(productionStartDays);

        // Expected: required_delivery_date = productionStartDate - leadTimeDays
        LocalDate expectedRequiredDate = productionStartDate.minusDays(leadTimeDays);

        // Set up supplier lead time
        SupplierLeadTime slt = new SupplierLeadTime();
        slt.setId(UUID.randomUUID().toString());
        slt.setMaterialId(materialId);
        slt.setLeadTimeDays(leadTimeDays);
        slt.setLastUpdated(new Date());

        // Set up order with a far-future deadline so alternatives are not triggered
        LocalDate deadline = productionStartDate.plusDays(30);
        PlanningOrder order = createOrder(orderId, "Product-PR", new BigDecimal("100"), toDate(deadline));

        // Set up material availability
        MaterialAvailability ma = new MaterialAvailability();
        ma.setOrderId(orderId);
        ma.setMaterialId(materialId);
        ma.setMaterialName("Test Material");
        ma.setSupplierLeadDays(leadTimeDays);

        List<PurchaseRequest> capturedPRs = new ArrayList<>();
        ProcurementCoordinationServiceImpl service = createServiceWithMocks(
                slt, order, List.of(ma), capturedPRs);

        // Execute
        PurchaseRequest result = service.generatePurchaseRequest(orderId, materialId, deficitQty, productionStartDate);

        // Verify the date calculation
        assertThat(result).isNotNull();
        assertThat(result.getRequiredDate()).isNotNull();

        LocalDate actualRequiredDate = toLocalDate(result.getRequiredDate());
        assertThat(actualRequiredDate)
                .as("PR required_delivery_date should equal productionStartDate(%s) - leadTimeDays(%d) = %s",
                        productionStartDate, leadTimeDays, expectedRequiredDate)
                .isEqualTo(expectedRequiredDate);

        // Also verify the supplier lead days is stored correctly
        assertThat(result.getSupplierLeadDays())
                .as("PR should store the supplier lead time used for calculation")
                .isEqualTo(leadTimeDays);
    }

    /**
     * Property 6b: The PR required_delivery_date is always before or equal to the production start date.
     * Since lead time is always >= 1 day, required_delivery_date < production_start_date.
     *
     * **Validates: Requirements 3.1**
     */
    @Property(tries = 200)
    void prRequiredDeliveryDateIsBeforeProductionStart(
            @ForAll("supplierLeadTimeDays") int leadTimeDays,
            @ForAll("productionStartDaysFromNow") int productionStartDays,
            @ForAll("deficitQuantities") BigDecimal deficitQty) {

        String orderId = UUID.randomUUID().toString();
        String materialId = "MAT-BEFORE";
        LocalDate productionStartDate = LocalDate.now().plusDays(productionStartDays);

        // Set up supplier lead time
        SupplierLeadTime slt = new SupplierLeadTime();
        slt.setId(UUID.randomUUID().toString());
        slt.setMaterialId(materialId);
        slt.setLeadTimeDays(leadTimeDays);
        slt.setLastUpdated(new Date());

        // Far-future deadline to avoid alternatives
        LocalDate deadline = productionStartDate.plusDays(60);
        PlanningOrder order = createOrder(orderId, "Product-BF", new BigDecimal("100"), toDate(deadline));

        MaterialAvailability ma = new MaterialAvailability();
        ma.setOrderId(orderId);
        ma.setMaterialId(materialId);
        ma.setMaterialName("Test Material");
        ma.setSupplierLeadDays(leadTimeDays);

        List<PurchaseRequest> capturedPRs = new ArrayList<>();
        ProcurementCoordinationServiceImpl service = createServiceWithMocks(
                slt, order, List.of(ma), capturedPRs);

        // Execute
        PurchaseRequest result = service.generatePurchaseRequest(orderId, materialId, deficitQty, productionStartDate);

        // Verify required date is strictly before production start
        assertThat(result).isNotNull();
        LocalDate actualRequiredDate = toLocalDate(result.getRequiredDate());
        assertThat(actualRequiredDate)
                .as("PR required_delivery_date should be before production_start_date (lead time >= 1 day)")
                .isBefore(productionStartDate);
    }

    /**
     * Property 6c: When no supplier lead time record exists, the default lead time (14 days) is used.
     * required_delivery_date = productionStartDate - 14.
     *
     * **Validates: Requirements 2.4, 3.1**
     */
    @Property(tries = 100)
    void prUsesDefaultLeadTimeWhenNoSupplierRecord(
            @ForAll("productionStartDaysFromNow") int productionStartDays,
            @ForAll("deficitQuantities") BigDecimal deficitQty) {

        String orderId = UUID.randomUUID().toString();
        String materialId = "MAT-NORECORD";
        LocalDate productionStartDate = LocalDate.now().plusDays(productionStartDays);
        int defaultLeadTimeDays = 14;

        LocalDate expectedRequiredDate = productionStartDate.minusDays(defaultLeadTimeDays);

        // No supplier lead time record (null)
        LocalDate deadline = productionStartDate.plusDays(60);
        PlanningOrder order = createOrder(orderId, "Product-DEF", new BigDecimal("100"), toDate(deadline));

        MaterialAvailability ma = new MaterialAvailability();
        ma.setOrderId(orderId);
        ma.setMaterialId(materialId);
        ma.setMaterialName("Unknown Material");

        List<PurchaseRequest> capturedPRs = new ArrayList<>();
        ProcurementCoordinationServiceImpl service = createServiceWithMocks(
                null, order, List.of(ma), capturedPRs); // null supplier lead time

        // Execute
        PurchaseRequest result = service.generatePurchaseRequest(orderId, materialId, deficitQty, productionStartDate);

        // Verify default lead time is used
        assertThat(result).isNotNull();
        assertThat(result.getSupplierLeadDays())
                .as("Should use default lead time of 14 days when no supplier record exists")
                .isEqualTo(defaultLeadTimeDays);

        LocalDate actualRequiredDate = toLocalDate(result.getRequiredDate());
        assertThat(actualRequiredDate)
                .as("PR required_delivery_date should use default 14-day lead time")
                .isEqualTo(expectedRequiredDate);
    }

    /**
     * Property 6d: The PR always stores the correct deficit quantity and material ID.
     *
     * **Validates: Requirements 3.1**
     */
    @Property(tries = 200)
    void prStoresCorrectDeficitAndMaterialInfo(
            @ForAll("supplierLeadTimeDays") int leadTimeDays,
            @ForAll("productionStartDaysFromNow") int productionStartDays,
            @ForAll("deficitQuantities") BigDecimal deficitQty) {

        String orderId = UUID.randomUUID().toString();
        String materialId = "MAT-INFO-" + leadTimeDays;
        LocalDate productionStartDate = LocalDate.now().plusDays(productionStartDays);

        SupplierLeadTime slt = new SupplierLeadTime();
        slt.setId(UUID.randomUUID().toString());
        slt.setMaterialId(materialId);
        slt.setLeadTimeDays(leadTimeDays);
        slt.setLastUpdated(new Date());

        LocalDate deadline = productionStartDate.plusDays(60);
        PlanningOrder order = createOrder(orderId, "Product-INFO", new BigDecimal("100"), toDate(deadline));

        MaterialAvailability ma = new MaterialAvailability();
        ma.setOrderId(orderId);
        ma.setMaterialId(materialId);
        ma.setMaterialName("Material " + materialId);
        ma.setSupplierLeadDays(leadTimeDays);

        List<PurchaseRequest> capturedPRs = new ArrayList<>();
        ProcurementCoordinationServiceImpl service = createServiceWithMocks(
                slt, order, List.of(ma), capturedPRs);

        // Execute
        PurchaseRequest result = service.generatePurchaseRequest(orderId, materialId, deficitQty, productionStartDate);

        // Verify PR contains correct information
        assertThat(result).isNotNull();
        assertThat(result.getOrderId())
                .as("PR should reference the correct order")
                .isEqualTo(orderId);
        assertThat(result.getMaterialId())
                .as("PR should reference the correct material")
                .isEqualTo(materialId);
        assertThat(result.getDeficitQty())
                .as("PR should store the exact deficit quantity")
                .isEqualByComparingTo(deficitQty);
        assertThat(result.getStatus())
                .as("PR should have status 'generated'")
                .isEqualTo("generated");
    }

    // ==================== Property 7: Alternative scenarios generation on deadline breach ====================

    /**
     * Property 7a: When supplier delivery time exceeds production deadline (required_delivery_date < today),
     * at least 2 alternative scenarios are generated.
     *
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 200)
    void atLeastTwoAlternativesGeneratedWhenDeadlineBreached(
            @ForAll("supplierLeadTimeDays") int leadTimeDays,
            @ForAll("deficitQuantities") BigDecimal deficitQty,
            @ForAll @IntRange(min = 1, max = 30) int deadlineDaysFromNow) {

        // Set up scenario where required_delivery_date < today
        // This happens when productionStartDate - leadTimeDays < today
        // So we set productionStartDate to be less than leadTimeDays from now
        // to ensure required_delivery_date is in the past
        int productionStartDays = Math.max(1, leadTimeDays - 5); // Ensure production start is before lead time allows
        Assume.that(productionStartDays < leadTimeDays); // required_delivery_date will be in the past

        String orderId = UUID.randomUUID().toString();
        String materialId = "MAT-ALT";
        LocalDate productionStartDate = LocalDate.now().plusDays(productionStartDays);
        LocalDate deadline = LocalDate.now().plusDays(deadlineDaysFromNow);

        SupplierLeadTime slt = new SupplierLeadTime();
        slt.setId(UUID.randomUUID().toString());
        slt.setMaterialId(materialId);
        slt.setLeadTimeDays(leadTimeDays);
        slt.setLastUpdated(new Date());

        PlanningOrder order = createOrder(orderId, "Product-ALT", new BigDecimal("100"), toDate(deadline));

        MaterialAvailability ma = new MaterialAvailability();
        ma.setOrderId(orderId);
        ma.setMaterialId(materialId);
        ma.setMaterialName("Alternative Material");
        ma.setSupplierLeadDays(leadTimeDays);

        List<PurchaseRequest> capturedPRs = new ArrayList<>();
        ProcurementCoordinationServiceImpl service = createServiceWithMocks(
                slt, order, List.of(ma), capturedPRs);

        // Execute
        PurchaseRequest result = service.generatePurchaseRequest(orderId, materialId, deficitQty, productionStartDate);

        // Verify alternatives are generated (at least 2)
        assertThat(result).isNotNull();
        assertThat(result.getAlternatives())
                .as("Alternatives JSON should be generated when required_delivery_date < today")
                .isNotNull()
                .isNotEmpty();

        // Parse alternatives JSON and verify count >= 2
        List<Map<String, Object>> alternatives = parseAlternatives(result.getAlternatives());
        assertThat(alternatives)
                .as("At least 2 alternative scenarios should be generated when deadline is breached")
                .hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Property 7b: Each generated alternative scenario contains required fields:
     * type, estimated_cost_impact, and revised_delivery_date.
     *
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 200)
    void alternativeScenariosContainRequiredFields(
            @ForAll("supplierLeadTimeDays") int leadTimeDays,
            @ForAll("deficitQuantities") BigDecimal deficitQty,
            @ForAll @IntRange(min = 1, max = 60) int deadlineDaysFromNow) {

        // Ensure required_delivery_date < today
        int productionStartDays = Math.max(1, leadTimeDays - 5);
        Assume.that(productionStartDays < leadTimeDays);

        String orderId = UUID.randomUUID().toString();
        String materialId = "MAT-FIELDS";
        LocalDate productionStartDate = LocalDate.now().plusDays(productionStartDays);
        LocalDate deadline = LocalDate.now().plusDays(deadlineDaysFromNow);

        SupplierLeadTime slt = new SupplierLeadTime();
        slt.setId(UUID.randomUUID().toString());
        slt.setMaterialId(materialId);
        slt.setLeadTimeDays(leadTimeDays);
        slt.setLastUpdated(new Date());

        PlanningOrder order = createOrder(orderId, "Product-FLD", new BigDecimal("100"), toDate(deadline));

        MaterialAvailability ma = new MaterialAvailability();
        ma.setOrderId(orderId);
        ma.setMaterialId(materialId);
        ma.setMaterialName("Field Test Material");
        ma.setSupplierLeadDays(leadTimeDays);

        List<PurchaseRequest> capturedPRs = new ArrayList<>();
        ProcurementCoordinationServiceImpl service = createServiceWithMocks(
                slt, order, List.of(ma), capturedPRs);

        // Execute
        PurchaseRequest result = service.generatePurchaseRequest(orderId, materialId, deficitQty, productionStartDate);

        // Parse and verify each alternative has required fields
        assertThat(result.getAlternatives()).isNotNull();
        List<Map<String, Object>> alternatives = parseAlternatives(result.getAlternatives());

        for (int i = 0; i < alternatives.size(); i++) {
            Map<String, Object> alt = alternatives.get(i);
            assertThat(alt)
                    .as("Alternative %d should contain 'type' field", i + 1)
                    .containsKey("type");
            assertThat(alt)
                    .as("Alternative %d should contain 'estimated_cost_impact' field", i + 1)
                    .containsKey("estimated_cost_impact");
            assertThat(alt)
                    .as("Alternative %d should contain 'revised_delivery_date' field", i + 1)
                    .containsKey("revised_delivery_date");

            // Verify type is one of the expected types
            String type = (String) alt.get("type");
            assertThat(type)
                    .as("Alternative type should be one of: expedited_shipping, alternative_supplier, production_rescheduling")
                    .isIn("expedited_shipping", "alternative_supplier", "production_rescheduling");

            // Verify revised_delivery_date is a valid date string
            String revisedDate = (String) alt.get("revised_delivery_date");
            assertThat(revisedDate)
                    .as("Alternative %d revised_delivery_date should be a non-empty date string", i + 1)
                    .isNotNull()
                    .isNotEmpty();
        }
    }

    /**
     * Property 7c: The alternative scenarios include distinct types (expedited shipping,
     * alternative supplier, or production rescheduling).
     *
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 200)
    void alternativeScenariosHaveDistinctTypes(
            @ForAll("supplierLeadTimeDays") int leadTimeDays,
            @ForAll("deficitQuantities") BigDecimal deficitQty,
            @ForAll @IntRange(min = 1, max = 60) int deadlineDaysFromNow) {

        // Ensure required_delivery_date < today
        int productionStartDays = Math.max(1, leadTimeDays - 5);
        Assume.that(productionStartDays < leadTimeDays);

        String orderId = UUID.randomUUID().toString();
        String materialId = "MAT-TYPES";
        LocalDate productionStartDate = LocalDate.now().plusDays(productionStartDays);
        LocalDate deadline = LocalDate.now().plusDays(deadlineDaysFromNow);

        SupplierLeadTime slt = new SupplierLeadTime();
        slt.setId(UUID.randomUUID().toString());
        slt.setMaterialId(materialId);
        slt.setLeadTimeDays(leadTimeDays);
        slt.setLastUpdated(new Date());

        PlanningOrder order = createOrder(orderId, "Product-TYP", new BigDecimal("100"), toDate(deadline));

        MaterialAvailability ma = new MaterialAvailability();
        ma.setOrderId(orderId);
        ma.setMaterialId(materialId);
        ma.setMaterialName("Type Test Material");
        ma.setSupplierLeadDays(leadTimeDays);

        List<PurchaseRequest> capturedPRs = new ArrayList<>();
        ProcurementCoordinationServiceImpl service = createServiceWithMocks(
                slt, order, List.of(ma), capturedPRs);

        // Execute
        PurchaseRequest result = service.generatePurchaseRequest(orderId, materialId, deficitQty, productionStartDate);

        // Parse and verify distinct types
        assertThat(result.getAlternatives()).isNotNull();
        List<Map<String, Object>> alternatives = parseAlternatives(result.getAlternatives());

        Set<String> types = new HashSet<>();
        for (Map<String, Object> alt : alternatives) {
            types.add((String) alt.get("type"));
        }

        assertThat(types)
                .as("Alternative scenarios should have at least 2 distinct types")
                .hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Property 7d: When no alternative can meet the deadline, the generateAlternatives method
     * is called via generatePurchaseRequest and alternatives are still generated.
     * The alternatives JSON is populated regardless of whether they meet the deadline.
     *
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    void alternativesGeneratedEvenWhenNoneMeetsDeadline(
            @ForAll @IntRange(min = 30, max = 90) int leadTimeDays,
            @ForAll("deficitQuantities") BigDecimal deficitQty) {

        // Set a very tight deadline that no alternative can meet
        // Lead time is 30-90 days, deadline is only 1 day from now
        int deadlineDaysFromNow = 1;

        // Ensure required_delivery_date < today
        int productionStartDays = Math.max(1, leadTimeDays - 5);
        Assume.that(productionStartDays < leadTimeDays);

        String orderId = UUID.randomUUID().toString();
        String materialId = "MAT-TIGHT";
        LocalDate productionStartDate = LocalDate.now().plusDays(productionStartDays);
        LocalDate deadline = LocalDate.now().plusDays(deadlineDaysFromNow);

        SupplierLeadTime slt = new SupplierLeadTime();
        slt.setId(UUID.randomUUID().toString());
        slt.setMaterialId(materialId);
        slt.setLeadTimeDays(leadTimeDays);
        slt.setLastUpdated(new Date());

        PlanningOrder order = createOrder(orderId, "Product-TIGHT", new BigDecimal("100"), toDate(deadline));

        MaterialAvailability ma = new MaterialAvailability();
        ma.setOrderId(orderId);
        ma.setMaterialId(materialId);
        ma.setMaterialName("Tight Deadline Material");
        ma.setSupplierLeadDays(leadTimeDays);

        List<PurchaseRequest> capturedPRs = new ArrayList<>();
        ProcurementCoordinationServiceImpl service = createServiceWithMocks(
                slt, order, List.of(ma), capturedPRs);

        // Execute
        PurchaseRequest result = service.generatePurchaseRequest(orderId, materialId, deficitQty, productionStartDate);

        // Verify alternatives are still generated even when none meets deadline
        assertThat(result).isNotNull();
        assertThat(result.getAlternatives())
                .as("Alternatives should be generated even when none can meet the tight deadline")
                .isNotNull()
                .isNotEmpty();

        List<Map<String, Object>> alternatives = parseAlternatives(result.getAlternatives());
        assertThat(alternatives)
                .as("At least 2 alternatives should be generated regardless of deadline feasibility")
                .hasSizeGreaterThanOrEqualTo(2);
    }

    // ==================== Utility methods ====================

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseAlternatives(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse alternatives JSON: " + json, e);
        }
    }
}
