package com.cy.modules.planning.agent.properties;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.enums.ValidationStatus;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.impl.OrderIngestionServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Order Ingestion.
 *
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**
 *
 * Property 1: Order data extraction preserves all fields.
 * Property 2: Order queue maintains sorting invariant.
 * Property 3: Invalid orders are excluded from the queue.
 */
@Tag("property-test")
@Tag("ai-production-planning")
class OrderIngestionPropertyTest {

    private static final String[] PRODUCT_TYPES = {"Widget-A", "Widget-B", "Gadget-X", "Component-Y", "Assembly-Z"};
    private static final String[] CUSTOMER_NAMES = {"Acme Corp", "Global Inc", "Tech Solutions", "MegaCo", "StarLabs"};

    // ==================== Helper methods ====================

    /**
     * Creates a fresh OrderIngestionServiceImpl with mocked dependencies.
     * Uses an in-memory store to simulate database operations.
     */
    private OrderIngestionServiceImpl createServiceWithMocks(Map<String, PlanningOrder> orderStore) {
        OrderIngestionServiceImpl service = new OrderIngestionServiceImpl();

        PlanningOrderMapper mapper = Mockito.mock(PlanningOrderMapper.class);
        PlanningNotificationService notificationService = Mockito.mock(PlanningNotificationService.class);

        // Mock selectById: return from in-memory store
        when(mapper.selectById(any(String.class))).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return orderStore.get(id);
        });

        // Mock updateById: update in-memory store
        when(mapper.updateById(any(PlanningOrder.class))).thenAnswer(invocation -> {
            PlanningOrder order = invocation.getArgument(0);
            orderStore.put(order.getId(), order);
            return 1;
        });

        // Mock selectList: return valid+pending orders sorted by deadline, receipt_timestamp
        when(mapper.selectList(any())).thenAnswer(invocation -> {
            return orderStore.values().stream()
                    .filter(o -> ValidationStatus.VALID.getValue().equals(o.getValidationStatus()))
                    .filter(o -> "pending".equals(o.getStatus()))
                    .sorted(Comparator.comparing(PlanningOrder::getDeadline)
                            .thenComparing(PlanningOrder::getReceiptTimestamp))
                    .collect(Collectors.toList());
        });

        // Inject mocks via reflection
        injectField(service, "planningOrderMapper", mapper);
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

    /**
     * Creates a valid PlanningOrder with all required fields populated.
     */
    private PlanningOrder createValidOrder(String id, String productType, String customerName,
                                           BigDecimal quantity, Date deadline, Date receiptTimestamp) {
        PlanningOrder order = new PlanningOrder();
        order.setId(id);
        order.setExternalOrderId("EXT-" + id);
        order.setProductType(productType);
        order.setCustomerName(customerName);
        order.setQuantity(quantity);
        order.setDeadline(deadline);
        order.setReceiptTimestamp(receiptTimestamp);
        order.setStatus("pending");
        order.setValidationStatus(ValidationStatus.VALID.getValue());
        order.setFulfillmentQty(BigDecimal.ZERO);
        return order;
    }

    /**
     * Converts a LocalDate to a Date at start of day in system timezone.
     */
    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<String> productTypes() {
        return Arbitraries.of(PRODUCT_TYPES);
    }

    @Provide
    Arbitrary<String> customerNames() {
        return Arbitraries.of(CUSTOMER_NAMES);
    }

    @Provide
    Arbitrary<BigDecimal> validQuantities() {
        // Positive quantities: 1 to 99999
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("1"), new BigDecimal("99999"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<Date> futureDeadlines() {
        // Deadlines from today to 365 days in the future
        LocalDate today = LocalDate.now();
        return Arbitraries.integers().between(0, 365)
                .map(daysAhead -> toDate(today.plusDays(daysAhead)));
    }

    @Provide
    Arbitrary<Date> pastDeadlines() {
        // Deadlines from 1 to 365 days in the past
        LocalDate today = LocalDate.now();
        return Arbitraries.integers().between(1, 365)
                .map(daysBack -> toDate(today.minusDays(daysBack)));
    }

    @Provide
    Arbitrary<Date> receiptTimestamps() {
        // Receipt timestamps within the last 30 days
        long now = System.currentTimeMillis();
        long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);
        return Arbitraries.longs().between(thirtyDaysAgo, now)
                .map(Date::new);
    }

    @Provide
    Arbitrary<BigDecimal> invalidQuantities() {
        // Zero or negative quantities
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("-9999"), BigDecimal.ZERO)
                .ofScale(2);
    }

    @Provide
    Arbitrary<List<PlanningOrder>> validOrderLists() {
        LocalDate today = LocalDate.now();
        long now = System.currentTimeMillis();
        long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);

        Arbitrary<PlanningOrder> orderArb = Combinators.combine(
                Arbitraries.of(PRODUCT_TYPES),
                Arbitraries.of(CUSTOMER_NAMES),
                Arbitraries.bigDecimals().between(new BigDecimal("1"), new BigDecimal("99999")).ofScale(2),
                Arbitraries.integers().between(0, 365),
                Arbitraries.longs().between(thirtyDaysAgo, now)
        ).as((productType, customerName, quantity, daysAhead, receiptMillis) -> {
            String id = UUID.randomUUID().toString();
            Date deadline = toDate(today.plusDays(daysAhead));
            Date receiptTimestamp = new Date(receiptMillis);
            return createValidOrder(id, productType, customerName, quantity, deadline, receiptTimestamp);
        });

        return orderArb.list().ofMinSize(2).ofMaxSize(20);
    }

    // ==================== Property 1: Order data extraction preserves all fields ====================

    /**
     * Property 1: When orders are ingested, all fields (product type, customer name,
     * quantity, deadline) are preserved in the order entity after processing.
     *
     * For any valid order with non-null product type, customer name, positive quantity,
     * and future deadline, after ingestion the stored order retains all original field values.
     *
     * **Validates: Requirements 1.1**
     */
    @Property(tries = 200)
    void orderDataExtractionPreservesAllFields(
            @ForAll("productTypes") String productType,
            @ForAll("customerNames") String customerName,
            @ForAll("validQuantities") BigDecimal quantity,
            @ForAll("futureDeadlines") Date deadline,
            @ForAll("receiptTimestamps") Date receiptTimestamp) {

        String orderId = UUID.randomUUID().toString();
        PlanningOrder order = createValidOrder(orderId, productType, customerName, quantity, deadline, receiptTimestamp);

        // Store original values before processing
        String originalProductType = order.getProductType();
        String originalCustomerName = order.getCustomerName();
        BigDecimal originalQuantity = order.getQuantity();
        Date originalDeadline = order.getDeadline();
        Date originalReceiptTimestamp = order.getReceiptTimestamp();

        // Set up in-memory store and process
        Map<String, PlanningOrder> orderStore = new ConcurrentHashMap<>();
        orderStore.put(orderId, order);

        OrderIngestionServiceImpl service = createServiceWithMocks(orderStore);
        service.processNewOrders(List.of(orderId));

        // Verify all fields are preserved after processing
        PlanningOrder processedOrder = orderStore.get(orderId);
        assertThat(processedOrder).isNotNull();
        assertThat(processedOrder.getProductType())
                .as("Product type should be preserved after ingestion")
                .isEqualTo(originalProductType);
        assertThat(processedOrder.getCustomerName())
                .as("Customer name should be preserved after ingestion")
                .isEqualTo(originalCustomerName);
        assertThat(processedOrder.getQuantity())
                .as("Quantity should be preserved after ingestion")
                .isEqualByComparingTo(originalQuantity);
        assertThat(processedOrder.getDeadline())
                .as("Deadline should be preserved after ingestion")
                .isEqualTo(originalDeadline);
        assertThat(processedOrder.getReceiptTimestamp())
                .as("Receipt timestamp should be preserved after ingestion")
                .isEqualTo(originalReceiptTimestamp);
        assertThat(processedOrder.getValidationStatus())
                .as("Valid order should have validation_status = 'valid'")
                .isEqualTo(ValidationStatus.VALID.getValue());
    }

    // ==================== Property 2: Order queue maintains sorting invariant ====================

    /**
     * Property 2: The prioritized order queue is always sorted by deadline ASC,
     * then receipt_timestamp ASC for ties.
     *
     * For any set of valid orders, the queue returned by getPrioritizedOrderQueue()
     * maintains the sorting invariant: for any two consecutive orders in the queue,
     * the first order's deadline is ≤ the second's; and if deadlines are equal,
     * the first order's receipt_timestamp is ≤ the second's.
     *
     * **Validates: Requirements 1.2, 1.4**
     */
    @Property(tries = 200)
    void orderQueueMaintainsSortingInvariant(
            @ForAll("validOrderLists") List<PlanningOrder> orders) {

        // Set up in-memory store with all orders
        Map<String, PlanningOrder> orderStore = new ConcurrentHashMap<>();
        List<String> orderIds = new ArrayList<>();
        for (PlanningOrder order : orders) {
            orderStore.put(order.getId(), order);
            orderIds.add(order.getId());
        }

        OrderIngestionServiceImpl service = createServiceWithMocks(orderStore);
        service.processNewOrders(orderIds);

        // Get the prioritized queue
        List<PlanningOrder> queue = service.getPrioritizedOrderQueue();

        // Verify sorting invariant: deadline ASC, then receipt_timestamp ASC
        for (int i = 0; i < queue.size() - 1; i++) {
            PlanningOrder current = queue.get(i);
            PlanningOrder next = queue.get(i + 1);

            int deadlineComparison = current.getDeadline().compareTo(next.getDeadline());
            assertThat(deadlineComparison)
                    .as("Order at position %d (deadline=%s) should have deadline ≤ order at position %d (deadline=%s)",
                            i, current.getDeadline(), i + 1, next.getDeadline())
                    .isLessThanOrEqualTo(0);

            // If deadlines are equal, check receipt_timestamp ordering
            if (deadlineComparison == 0) {
                assertThat(current.getReceiptTimestamp().compareTo(next.getReceiptTimestamp()))
                        .as("When deadlines are equal, order at position %d (receipt=%s) should have receipt_timestamp ≤ order at position %d (receipt=%s)",
                                i, current.getReceiptTimestamp(), i + 1, next.getReceiptTimestamp())
                        .isLessThanOrEqualTo(0);
            }
        }
    }

    /**
     * Property 2b: After processing, all valid orders in the queue have consecutive
     * priority_rank values starting from 1, reflecting the sorting order.
     *
     * **Validates: Requirements 1.4**
     */
    @Property(tries = 200)
    void orderQueueHasConsecutivePriorityRanks(
            @ForAll("validOrderLists") List<PlanningOrder> orders) {

        // Set up in-memory store with all orders
        Map<String, PlanningOrder> orderStore = new ConcurrentHashMap<>();
        List<String> orderIds = new ArrayList<>();
        for (PlanningOrder order : orders) {
            orderStore.put(order.getId(), order);
            orderIds.add(order.getId());
        }

        OrderIngestionServiceImpl service = createServiceWithMocks(orderStore);
        service.processNewOrders(orderIds);

        // Get the prioritized queue
        List<PlanningOrder> queue = service.getPrioritizedOrderQueue();

        // Verify priority ranks are consecutive starting from 1
        for (int i = 0; i < queue.size(); i++) {
            PlanningOrder order = queue.get(i);
            assertThat(order.getPriorityRank())
                    .as("Order at position %d should have priority_rank = %d", i, i + 1)
                    .isEqualTo(i + 1);
        }
    }

    // ==================== Property 3: Invalid orders are excluded from the queue ====================

    /**
     * Property 3a: Orders with quantity ≤ 0 are excluded from the prioritized queue
     * and marked as invalid.
     *
     * **Validates: Requirements 1.5**
     */
    @Property(tries = 200)
    void invalidQuantityOrdersAreExcludedFromQueue(
            @ForAll("productTypes") String productType,
            @ForAll("customerNames") String customerName,
            @ForAll("invalidQuantities") BigDecimal invalidQuantity,
            @ForAll("futureDeadlines") Date deadline,
            @ForAll("receiptTimestamps") Date receiptTimestamp) {

        String orderId = UUID.randomUUID().toString();
        PlanningOrder order = createValidOrder(orderId, productType, customerName, invalidQuantity, deadline, receiptTimestamp);

        Map<String, PlanningOrder> orderStore = new ConcurrentHashMap<>();
        orderStore.put(orderId, order);

        OrderIngestionServiceImpl service = createServiceWithMocks(orderStore);
        service.processNewOrders(List.of(orderId));

        // Verify order is marked as invalid
        PlanningOrder processedOrder = orderStore.get(orderId);
        assertThat(processedOrder.getValidationStatus())
                .as("Order with quantity %s should be marked as invalid", invalidQuantity)
                .isEqualTo(ValidationStatus.INVALID.getValue());

        // Verify order is NOT in the prioritized queue
        List<PlanningOrder> queue = service.getPrioritizedOrderQueue();
        assertThat(queue)
                .as("Invalid order should not appear in the prioritized queue")
                .noneMatch(o -> o.getId().equals(orderId));

        // Verify priority_rank is null
        assertThat(processedOrder.getPriorityRank())
                .as("Invalid order should have null priority_rank")
                .isNull();
    }

    /**
     * Property 3b: Orders with deadline in the past are excluded from the prioritized queue
     * and marked as invalid.
     *
     * **Validates: Requirements 1.5**
     */
    @Property(tries = 200)
    void pastDeadlineOrdersAreExcludedFromQueue(
            @ForAll("productTypes") String productType,
            @ForAll("customerNames") String customerName,
            @ForAll("validQuantities") BigDecimal quantity,
            @ForAll("pastDeadlines") Date pastDeadline,
            @ForAll("receiptTimestamps") Date receiptTimestamp) {

        String orderId = UUID.randomUUID().toString();
        PlanningOrder order = createValidOrder(orderId, productType, customerName, quantity, pastDeadline, receiptTimestamp);

        Map<String, PlanningOrder> orderStore = new ConcurrentHashMap<>();
        orderStore.put(orderId, order);

        OrderIngestionServiceImpl service = createServiceWithMocks(orderStore);
        service.processNewOrders(List.of(orderId));

        // Verify order is marked as invalid
        PlanningOrder processedOrder = orderStore.get(orderId);
        assertThat(processedOrder.getValidationStatus())
                .as("Order with past deadline %s should be marked as invalid", pastDeadline)
                .isEqualTo(ValidationStatus.INVALID.getValue());

        // Verify order is NOT in the prioritized queue
        List<PlanningOrder> queue = service.getPrioritizedOrderQueue();
        assertThat(queue)
                .as("Order with past deadline should not appear in the prioritized queue")
                .noneMatch(o -> o.getId().equals(orderId));
    }

    /**
     * Property 3c: Orders with missing required fields (incomplete) are excluded from
     * the prioritized queue and marked as incomplete.
     *
     * **Validates: Requirements 1.3**
     */
    @Property(tries = 200)
    void incompleteOrdersAreExcludedFromQueue(
            @ForAll("futureDeadlines") Date deadline,
            @ForAll("receiptTimestamps") Date receiptTimestamp,
            @ForAll @IntRange(min = 0, max = 3) int missingFieldIndex) {

        String orderId = UUID.randomUUID().toString();
        PlanningOrder order = createValidOrder(orderId, "Widget-A", "Acme Corp",
                new BigDecimal("100"), deadline, receiptTimestamp);

        // Remove one required field based on index
        switch (missingFieldIndex) {
            case 0 -> order.setProductType(null);
            case 1 -> order.setCustomerName(null);
            case 2 -> order.setQuantity(null);
            case 3 -> order.setDeadline(null);
        }

        Map<String, PlanningOrder> orderStore = new ConcurrentHashMap<>();
        orderStore.put(orderId, order);

        OrderIngestionServiceImpl service = createServiceWithMocks(orderStore);
        service.processNewOrders(List.of(orderId));

        // Verify order is marked as incomplete
        PlanningOrder processedOrder = orderStore.get(orderId);
        assertThat(processedOrder.getValidationStatus())
                .as("Order with missing field (index=%d) should be marked as incomplete", missingFieldIndex)
                .isEqualTo(ValidationStatus.INCOMPLETE.getValue());

        // Verify order is NOT in the prioritized queue
        List<PlanningOrder> queue = service.getPrioritizedOrderQueue();
        assertThat(queue)
                .as("Incomplete order should not appear in the prioritized queue")
                .noneMatch(o -> o.getId().equals(orderId));

        // Verify priority_rank is null
        assertThat(processedOrder.getPriorityRank())
                .as("Incomplete order should have null priority_rank")
                .isNull();
    }

    /**
     * Property 3d: In a mixed batch of valid and invalid orders, only valid orders
     * appear in the prioritized queue, and the queue size equals the number of valid orders.
     *
     * **Validates: Requirements 1.3, 1.4, 1.5**
     */
    @Property(tries = 200)
    void mixedBatchOnlyValidOrdersInQueue(
            @ForAll @IntRange(min = 1, max = 5) int validCount,
            @ForAll @IntRange(min = 1, max = 5) int invalidCount) {

        LocalDate today = LocalDate.now();
        long now = System.currentTimeMillis();
        Map<String, PlanningOrder> orderStore = new ConcurrentHashMap<>();
        List<String> allOrderIds = new ArrayList<>();
        Set<String> validOrderIds = new HashSet<>();

        // Create valid orders
        for (int i = 0; i < validCount; i++) {
            String id = UUID.randomUUID().toString();
            Date deadline = toDate(today.plusDays(10 + i));
            Date receipt = new Date(now - (i * 60000L));
            PlanningOrder order = createValidOrder(id, PRODUCT_TYPES[i % PRODUCT_TYPES.length],
                    CUSTOMER_NAMES[i % CUSTOMER_NAMES.length],
                    new BigDecimal(100 + i), deadline, receipt);
            orderStore.put(id, order);
            allOrderIds.add(id);
            validOrderIds.add(id);
        }

        // Create invalid orders (quantity <= 0)
        for (int i = 0; i < invalidCount; i++) {
            String id = UUID.randomUUID().toString();
            Date deadline = toDate(today.plusDays(5 + i));
            Date receipt = new Date(now - ((validCount + i) * 60000L));
            PlanningOrder order = createValidOrder(id, PRODUCT_TYPES[i % PRODUCT_TYPES.length],
                    CUSTOMER_NAMES[i % CUSTOMER_NAMES.length],
                    new BigDecimal(-1 * (i + 1)), deadline, receipt);
            orderStore.put(id, order);
            allOrderIds.add(id);
        }

        OrderIngestionServiceImpl service = createServiceWithMocks(orderStore);
        service.processNewOrders(allOrderIds);

        // Get the prioritized queue
        List<PlanningOrder> queue = service.getPrioritizedOrderQueue();

        // Verify only valid orders are in the queue
        assertThat(queue.size())
                .as("Queue should contain exactly %d valid orders", validCount)
                .isEqualTo(validCount);

        Set<String> queueIds = queue.stream().map(PlanningOrder::getId).collect(Collectors.toSet());
        assertThat(queueIds)
                .as("Queue should contain only the valid order IDs")
                .isEqualTo(validOrderIds);
    }
}
