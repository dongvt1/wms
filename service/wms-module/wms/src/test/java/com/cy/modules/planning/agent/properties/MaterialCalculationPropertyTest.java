package com.cy.modules.planning.agent.properties;

import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.dto.BomStructure;
import com.cy.modules.planning.agent.dto.MaterialAvailabilityResult;
import com.cy.modules.planning.agent.entity.MaterialAvailability;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.mapper.MaterialAvailabilityMapper;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.mapper.SupplierLeadTimeMapper;
import com.cy.modules.planning.agent.service.InventorySyncService;
import com.cy.modules.planning.agent.service.MaterialAvailabilityService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.impl.MaterialAvailabilityServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Material Calculations.
 *
 * **Validates: Requirements 2.2, 2.3, 2.5, 2.7**
 *
 * Property 4: Material deficit calculation correctness.
 * Property 5: Material reservation and deadline validation.
 */
@Tag("property-test")
@Tag("ai-production-planning")
class MaterialCalculationPropertyTest {

    private static final String[] MATERIAL_IDS = {"MAT-001", "MAT-002", "MAT-003", "MAT-004", "MAT-005"};
    private static final String[] MATERIAL_NAMES = {"Steel Sheet", "Copper Wire", "Plastic Resin", "Aluminum Bar", "Glass Panel"};

    // ==================== Helper methods ====================

    /**
     * Creates a fresh MaterialAvailabilityServiceImpl with mocked dependencies.
     * Uses in-memory stores to simulate database operations.
     */
    private MaterialAvailabilityServiceImpl createServiceWithMocks(
            PlanningOrder order,
            BomStructure bom,
            Map<String, BigDecimal> inventoryLevels,
            Map<String, Integer> supplierLeadDays,
            List<MaterialAvailability> insertedMaterials) {

        MaterialAvailabilityServiceImpl service = new MaterialAvailabilityServiceImpl();

        // Mock dependencies
        InventorySyncService inventorySyncService = Mockito.mock(InventorySyncService.class);
        ErpClient erpClient = Mockito.mock(ErpClient.class);
        MaterialAvailabilityMapper materialAvailabilityMapper = Mockito.mock(MaterialAvailabilityMapper.class);
        PlanningOrderMapper planningOrderMapper = Mockito.mock(PlanningOrderMapper.class);
        SupplierLeadTimeMapper supplierLeadTimeMapper = Mockito.mock(SupplierLeadTimeMapper.class);
        PlanningNotificationService notificationService = Mockito.mock(PlanningNotificationService.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        // Mock planningOrderMapper.selectById
        when(planningOrderMapper.selectById(anyString())).thenReturn(order);

        // Mock inventorySyncService.getBom
        when(inventorySyncService.getBom(anyString())).thenReturn(bom);

        // Mock inventorySyncService.getInventoryLevel
        when(inventorySyncService.getInventoryLevel(anyString())).thenAnswer(invocation -> {
            String materialId = invocation.getArgument(0);
            return inventoryLevels.getOrDefault(materialId, BigDecimal.ZERO);
        });

        // Mock supplierLeadTimeMapper.selectOne - return entity with lead days
        when(supplierLeadTimeMapper.selectOne(any())).thenAnswer(invocation -> {
            // We can't easily extract the materialId from the wrapper,
            // so we return null and let the fallback handle it
            return null;
        });

        // Mock inventorySyncService.getSupplierLeadTime - return DTO list with lead days
        when(inventorySyncService.getSupplierLeadTime(anyString())).thenAnswer(invocation -> {
            String materialId = invocation.getArgument(0);
            Integer days = supplierLeadDays.get(materialId);
            if (days != null) {
                com.cy.modules.planning.agent.dto.SupplierLeadTime lt =
                        com.cy.modules.planning.agent.dto.SupplierLeadTime.builder()
                                .materialId(materialId)
                                .leadTimeDays(days)
                                .build();
                return List.of(lt);
            }
            return Collections.emptyList();
        });

        // Mock materialAvailabilityMapper.delete (no-op)
        when(materialAvailabilityMapper.delete(any())).thenReturn(0);

        // Mock materialAvailabilityMapper.insert - capture inserted records
        when(materialAvailabilityMapper.insert(any(MaterialAvailability.class))).thenAnswer(invocation -> {
            MaterialAvailability ma = invocation.getArgument(0);
            insertedMaterials.add(ma);
            return 1;
        });

        // Mock materialAvailabilityMapper.updateById
        when(materialAvailabilityMapper.updateById(any(MaterialAvailability.class))).thenReturn(1);

        // Inject mocks via reflection
        injectField(service, "inventorySyncService", inventorySyncService);
        injectField(service, "erpClient", erpClient);
        injectField(service, "materialAvailabilityMapper", materialAvailabilityMapper);
        injectField(service, "planningOrderMapper", planningOrderMapper);
        injectField(service, "supplierLeadTimeMapper", supplierLeadTimeMapper);
        injectField(service, "planningNotificationService", notificationService);
        injectField(service, "eventPublisher", eventPublisher);

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

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<BigDecimal> orderQuantities() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("1"), new BigDecimal("1000"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> quantitiesPerUnit() {
        // Quantity per unit in BOM: 0.01 to 50
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("50"))
                .ofScale(3);
    }

    @Provide
    Arbitrary<BigDecimal> inventoryQuantities() {
        // Available inventory: 0 to 100000
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("100000"))
                .ofScale(3);
    }

    @Provide
    Arbitrary<Integer> leadTimeDays() {
        // Supplier lead time: 1 to 90 days
        return Arbitraries.integers().between(1, 90);
    }

    @Provide
    Arbitrary<Integer> deadlineDaysFromNow() {
        // Deadline: 1 to 180 days from now
        return Arbitraries.integers().between(1, 180);
    }

    @Provide
    Arbitrary<List<BomStructure.BomItem>> bomItemLists() {
        Arbitrary<BomStructure.BomItem> itemArb = Combinators.combine(
                Arbitraries.integers().between(0, MATERIAL_IDS.length - 1),
                Arbitraries.bigDecimals().between(new BigDecimal("0.01"), new BigDecimal("50")).ofScale(3)
        ).as((idx, qtyPerUnit) -> BomStructure.BomItem.builder()
                .materialId(MATERIAL_IDS[idx])
                .materialName(MATERIAL_NAMES[idx])
                .quantityPerUnit(qtyPerUnit)
                .unit("kg")
                .scrapRate(null) // No scrap for simpler deficit calculation testing
                .build());

        return itemArb.list().ofMinSize(1).ofMaxSize(5).uniqueElements(BomStructure.BomItem::getMaterialId);
    }

    // ==================== Property 4: Material deficit calculation correctness ====================

    /**
     * Property 4a: For any BOM requirement and inventory level, the material deficit
     * is correctly calculated as max(0, required_qty - available_qty) per material.
     *
     * For each material in the BOM, required_qty = quantityPerUnit * orderQuantity.
     * deficit = max(0, required_qty - available_qty).
     *
     * **Validates: Requirements 2.3**
     */
    @Property(tries = 200)
    void materialDeficitIsMaxZeroRequiredMinusAvailable(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("bomItemLists") List<BomStructure.BomItem> bomItems) {

        String orderId = UUID.randomUUID().toString();
        PlanningOrder order = createOrder(orderId, "Product-X", orderQuantity, toDate(LocalDate.now().plusDays(60)));

        BomStructure bom = BomStructure.builder()
                .productId("Product-X")
                .productName("Product X")
                .bomVersion("1.0")
                .items(bomItems)
                .build();

        // Generate random inventory levels for each material
        Map<String, BigDecimal> inventoryLevels = new HashMap<>();
        Random rng = new Random(orderId.hashCode());
        for (BomStructure.BomItem item : bomItems) {
            // Random inventory between 0 and 2x the max possible required qty
            BigDecimal maxRequired = item.getQuantityPerUnit().multiply(orderQuantity);
            BigDecimal inventory = maxRequired.multiply(BigDecimal.valueOf(rng.nextDouble() * 2))
                    .setScale(3, RoundingMode.HALF_UP);
            inventoryLevels.put(item.getMaterialId(), inventory);
        }

        // No supplier lead times for this property (focus on deficit calculation)
        Map<String, Integer> supplierLeadDays = new HashMap<>();
        List<MaterialAvailability> insertedMaterials = new ArrayList<>();

        MaterialAvailabilityServiceImpl service = createServiceWithMocks(
                order, bom, inventoryLevels, supplierLeadDays, insertedMaterials);

        // Execute
        MaterialAvailabilityResult result = service.checkMaterialAvailability(orderId);

        // Verify deficit calculation for each material
        assertThat(result.isSuccess()).isTrue();
        assertThat(insertedMaterials).hasSize(bomItems.size());

        for (int i = 0; i < bomItems.size(); i++) {
            BomStructure.BomItem bomItem = bomItems.get(i);
            MaterialAvailability ma = insertedMaterials.get(i);

            BigDecimal expectedRequired = bomItem.getQuantityPerUnit().multiply(orderQuantity);
            BigDecimal available = inventoryLevels.getOrDefault(bomItem.getMaterialId(), BigDecimal.ZERO);
            BigDecimal expectedDeficit = expectedRequired.subtract(available).max(BigDecimal.ZERO);

            assertThat(ma.getMaterialId())
                    .as("Material ID should match BOM item")
                    .isEqualTo(bomItem.getMaterialId());

            assertThat(ma.getRequiredQty())
                    .as("Required qty for %s should be quantityPerUnit * orderQuantity", bomItem.getMaterialId())
                    .isEqualByComparingTo(expectedRequired);

            assertThat(ma.getAvailableQty())
                    .as("Available qty for %s should match inventory level", bomItem.getMaterialId())
                    .isEqualByComparingTo(available);

            assertThat(ma.getDeficitQty())
                    .as("Deficit for %s should be max(0, required - available)", bomItem.getMaterialId())
                    .isEqualByComparingTo(expectedDeficit);
        }
    }

    /**
     * Property 4b: The deficit is always non-negative (≥ 0) regardless of input values.
     *
     * **Validates: Requirements 2.3**
     */
    @Property(tries = 200)
    void materialDeficitIsAlwaysNonNegative(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("quantitiesPerUnit") BigDecimal qtyPerUnit,
            @ForAll("inventoryQuantities") BigDecimal availableQty) {

        String orderId = UUID.randomUUID().toString();
        PlanningOrder order = createOrder(orderId, "Product-Y", orderQuantity, toDate(LocalDate.now().plusDays(60)));

        BomStructure.BomItem bomItem = BomStructure.BomItem.builder()
                .materialId("MAT-001")
                .materialName("Steel Sheet")
                .quantityPerUnit(qtyPerUnit)
                .unit("kg")
                .scrapRate(null)
                .build();

        BomStructure bom = BomStructure.builder()
                .productId("Product-Y")
                .productName("Product Y")
                .bomVersion("1.0")
                .items(List.of(bomItem))
                .build();

        Map<String, BigDecimal> inventoryLevels = Map.of("MAT-001", availableQty);
        Map<String, Integer> supplierLeadDays = new HashMap<>();
        List<MaterialAvailability> insertedMaterials = new ArrayList<>();

        MaterialAvailabilityServiceImpl service = createServiceWithMocks(
                order, bom, inventoryLevels, supplierLeadDays, insertedMaterials);

        MaterialAvailabilityResult result = service.checkMaterialAvailability(orderId);

        assertThat(result.isSuccess()).isTrue();
        assertThat(insertedMaterials).hasSize(1);

        MaterialAvailability ma = insertedMaterials.get(0);
        assertThat(ma.getDeficitQty())
                .as("Deficit should always be >= 0 (required=%s, available=%s)",
                        ma.getRequiredQty(), ma.getAvailableQty())
                .isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    /**
     * Property 4c: When available inventory exceeds required quantity, deficit is exactly zero.
     *
     * **Validates: Requirements 2.2, 2.3**
     */
    @Property(tries = 200)
    void deficitIsZeroWhenInventoryExceedsRequired(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("quantitiesPerUnit") BigDecimal qtyPerUnit) {

        String orderId = UUID.randomUUID().toString();
        PlanningOrder order = createOrder(orderId, "Product-Z", orderQuantity, toDate(LocalDate.now().plusDays(60)));

        BigDecimal requiredQty = qtyPerUnit.multiply(orderQuantity);
        // Set available to be strictly more than required
        BigDecimal availableQty = requiredQty.add(new BigDecimal("100"));

        BomStructure.BomItem bomItem = BomStructure.BomItem.builder()
                .materialId("MAT-001")
                .materialName("Steel Sheet")
                .quantityPerUnit(qtyPerUnit)
                .unit("kg")
                .scrapRate(null)
                .build();

        BomStructure bom = BomStructure.builder()
                .productId("Product-Z")
                .productName("Product Z")
                .bomVersion("1.0")
                .items(List.of(bomItem))
                .build();

        Map<String, BigDecimal> inventoryLevels = Map.of("MAT-001", availableQty);
        Map<String, Integer> supplierLeadDays = new HashMap<>();
        List<MaterialAvailability> insertedMaterials = new ArrayList<>();

        MaterialAvailabilityServiceImpl service = createServiceWithMocks(
                order, bom, inventoryLevels, supplierLeadDays, insertedMaterials);

        MaterialAvailabilityResult result = service.checkMaterialAvailability(orderId);

        assertThat(result.isSuccess()).isTrue();
        assertThat(insertedMaterials).hasSize(1);

        MaterialAvailability ma = insertedMaterials.get(0);
        assertThat(ma.getDeficitQty())
                .as("Deficit should be 0 when available (%s) >= required (%s)",
                        availableQty, requiredQty)
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ma.getStatus())
                .as("Status should be 'available' when no deficit")
                .isEqualTo("available");
    }

    // ==================== Property 5: Material reservation and deadline validation ====================

    /**
     * Property 5a: Materials are reserved (reserved=1) when ALL BOM materials have
     * sufficient inventory (deficit = 0 for all materials).
     *
     * **Validates: Requirements 2.2**
     */
    @Property(tries = 200)
    void materialsAreReservedWhenAllSufficient(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("bomItemLists") List<BomStructure.BomItem> bomItems) {

        String orderId = UUID.randomUUID().toString();
        PlanningOrder order = createOrder(orderId, "Product-R", orderQuantity, toDate(LocalDate.now().plusDays(60)));

        BomStructure bom = BomStructure.builder()
                .productId("Product-R")
                .productName("Product R")
                .bomVersion("1.0")
                .items(bomItems)
                .build();

        // Set inventory to be MORE than required for all materials
        Map<String, BigDecimal> inventoryLevels = new HashMap<>();
        for (BomStructure.BomItem item : bomItems) {
            BigDecimal required = item.getQuantityPerUnit().multiply(orderQuantity);
            // Ensure available > required
            inventoryLevels.put(item.getMaterialId(), required.add(new BigDecimal("500")));
        }

        Map<String, Integer> supplierLeadDays = new HashMap<>();
        List<MaterialAvailability> insertedMaterials = new ArrayList<>();

        MaterialAvailabilityServiceImpl service = createServiceWithMocks(
                order, bom, inventoryLevels, supplierLeadDays, insertedMaterials);

        MaterialAvailabilityResult result = service.checkMaterialAvailability(orderId);

        // Verify all materials are available and reserved
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isAllAvailable())
                .as("All materials should be available when inventory exceeds requirements")
                .isTrue();

        // Verify each material has reserved=1
        for (MaterialAvailability ma : insertedMaterials) {
            assertThat(ma.getReserved())
                    .as("Material %s should be reserved (reserved=1) when all BOM materials are sufficient",
                            ma.getMaterialId())
                    .isEqualTo(1);
        }
    }

    /**
     * Property 5b: Materials are NOT reserved when ANY BOM material has insufficient
     * inventory (at least one material has deficit > 0).
     *
     * **Validates: Requirements 2.2, 2.3**
     */
    @Property(tries = 200)
    void materialsAreNotReservedWhenAnyInsufficient(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("bomItemLists") List<BomStructure.BomItem> bomItems) {

        Assume.that(bomItems.size() >= 1);

        String orderId = UUID.randomUUID().toString();
        PlanningOrder order = createOrder(orderId, "Product-S", orderQuantity, toDate(LocalDate.now().plusDays(60)));

        BomStructure bom = BomStructure.builder()
                .productId("Product-S")
                .productName("Product S")
                .bomVersion("1.0")
                .items(bomItems)
                .build();

        // Set inventory: first material has LESS than required, rest have enough
        Map<String, BigDecimal> inventoryLevels = new HashMap<>();
        for (int i = 0; i < bomItems.size(); i++) {
            BomStructure.BomItem item = bomItems.get(i);
            BigDecimal required = item.getQuantityPerUnit().multiply(orderQuantity);
            if (i == 0) {
                // First material: insufficient (half of required)
                BigDecimal insufficient = required.divide(BigDecimal.valueOf(2), 3, RoundingMode.HALF_UP);
                inventoryLevels.put(item.getMaterialId(), insufficient);
            } else {
                // Other materials: sufficient
                inventoryLevels.put(item.getMaterialId(), required.add(new BigDecimal("500")));
            }
        }

        Map<String, Integer> supplierLeadDays = new HashMap<>();
        List<MaterialAvailability> insertedMaterials = new ArrayList<>();

        MaterialAvailabilityServiceImpl service = createServiceWithMocks(
                order, bom, inventoryLevels, supplierLeadDays, insertedMaterials);

        MaterialAvailabilityResult result = service.checkMaterialAvailability(orderId);

        // Verify not all available
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isAllAvailable())
                .as("Not all materials should be available when first material is insufficient")
                .isFalse();

        // Verify NO material is reserved (reservation is all-or-nothing)
        for (MaterialAvailability ma : insertedMaterials) {
            assertThat(ma.getReserved())
                    .as("Material %s should NOT be reserved (reserved=0) when any BOM material is insufficient",
                            ma.getMaterialId())
                    .isEqualTo(0);
        }

        // Verify shortages list is non-empty
        assertThat(result.getShortages())
                .as("Shortages list should contain at least one material")
                .isNotEmpty();
    }

    /**
     * Property 5c: An order is flagged as at-risk when current_date + lead_time > deadline
     * for any material. The at-risk flag is set regardless of whether materials are sufficient.
     *
     * **Validates: Requirements 2.5, 2.7**
     */
    @Property(tries = 200)
    void orderIsFlaggedAtRiskWhenLeadTimeExceedsDeadline(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("leadTimeDays") int leadTime,
            @ForAll @IntRange(min = 1, max = 180) int deadlineDays) {

        // Only test cases where lead time exceeds deadline
        Assume.that(leadTime > deadlineDays);

        String orderId = UUID.randomUUID().toString();
        LocalDate deadlineDate = LocalDate.now().plusDays(deadlineDays);
        PlanningOrder order = createOrder(orderId, "Product-AT", orderQuantity, toDate(deadlineDate));

        BomStructure.BomItem bomItem = BomStructure.BomItem.builder()
                .materialId("MAT-001")
                .materialName("Steel Sheet")
                .quantityPerUnit(new BigDecimal("2.000"))
                .unit("kg")
                .scrapRate(null)
                .build();

        BomStructure bom = BomStructure.builder()
                .productId("Product-AT")
                .productName("Product AT")
                .bomVersion("1.0")
                .items(List.of(bomItem))
                .build();

        // Sufficient inventory so reservation logic runs
        BigDecimal required = bomItem.getQuantityPerUnit().multiply(orderQuantity);
        Map<String, BigDecimal> inventoryLevels = Map.of("MAT-001", required.add(new BigDecimal("1000")));
        Map<String, Integer> supplierLeadDaysMap = Map.of("MAT-001", leadTime);
        List<MaterialAvailability> insertedMaterials = new ArrayList<>();

        MaterialAvailabilityServiceImpl service = createServiceWithMocks(
                order, bom, inventoryLevels, supplierLeadDaysMap, insertedMaterials);

        MaterialAvailabilityResult result = service.checkMaterialAvailability(orderId);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isAtRisk())
                .as("Order should be at-risk when lead time (%d days) > deadline (%d days from now)",
                        leadTime, deadlineDays)
                .isTrue();
    }

    /**
     * Property 5d: An order is NOT flagged as at-risk when current_date + lead_time <= deadline
     * for all materials.
     *
     * **Validates: Requirements 2.5, 2.7**
     */
    @Property(tries = 200)
    void orderIsNotAtRiskWhenLeadTimeWithinDeadline(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("leadTimeDays") int leadTime,
            @ForAll @IntRange(min = 1, max = 180) int deadlineDays) {

        // Only test cases where lead time does NOT exceed deadline
        Assume.that(leadTime <= deadlineDays);

        String orderId = UUID.randomUUID().toString();
        LocalDate deadlineDate = LocalDate.now().plusDays(deadlineDays);
        PlanningOrder order = createOrder(orderId, "Product-OK", orderQuantity, toDate(deadlineDate));

        BomStructure.BomItem bomItem = BomStructure.BomItem.builder()
                .materialId("MAT-002")
                .materialName("Copper Wire")
                .quantityPerUnit(new BigDecimal("1.500"))
                .unit("m")
                .scrapRate(null)
                .build();

        BomStructure bom = BomStructure.builder()
                .productId("Product-OK")
                .productName("Product OK")
                .bomVersion("1.0")
                .items(List.of(bomItem))
                .build();

        // Sufficient inventory
        BigDecimal required = bomItem.getQuantityPerUnit().multiply(orderQuantity);
        Map<String, BigDecimal> inventoryLevels = Map.of("MAT-002", required.add(new BigDecimal("1000")));
        Map<String, Integer> supplierLeadDaysMap = Map.of("MAT-002", leadTime);
        List<MaterialAvailability> insertedMaterials = new ArrayList<>();

        MaterialAvailabilityServiceImpl service = createServiceWithMocks(
                order, bom, inventoryLevels, supplierLeadDaysMap, insertedMaterials);

        MaterialAvailabilityResult result = service.checkMaterialAvailability(orderId);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isAtRisk())
                .as("Order should NOT be at-risk when lead time (%d days) <= deadline (%d days from now)",
                        leadTime, deadlineDays)
                .isFalse();
    }
}
