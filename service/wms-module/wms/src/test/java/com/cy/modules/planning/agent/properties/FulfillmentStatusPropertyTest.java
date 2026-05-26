package com.cy.modules.planning.agent.properties;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.impl.FinishedGoodsDispatchServiceImpl;
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
 * Property-based tests for Finished Goods Dispatch Service — Fulfillment Status.
 *
 * **Validates: Requirements 11.1, 11.2, 11.5**
 *
 * Property 26: Order fulfillment status determination
 * - For any order quantity Q and received quantity R:
 *   - When R = 0 and production orders exist: status = "in_production"
 *   - When 0 < R < Q: status = "partially_fulfilled"
 *   - When R ≥ Q: status = "fully_fulfilled"
 * - The remaining quantity = Q - R is correctly calculated
 */
@Tag("property-test")
@Tag("ai-production-planning")
class FulfillmentStatusPropertyTest {

    @BeforeContainer
    static void initMybatisPlusLambdaCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, PlanningOrder.class);
    }

    // ==================== Service factory ====================

    private FinishedGoodsDispatchServiceImpl createService(PlanningOrder order,
                                                            List<PlanningOrder> capturedUpdates) {
        FinishedGoodsDispatchServiceImpl service = new FinishedGoodsDispatchServiceImpl();

        PlanningOrderMapper orderMapper = mock(PlanningOrderMapper.class);
        ErpClient erpClient = mock(ErpClient.class);
        PlanningNotificationService notifService = mock(PlanningNotificationService.class);

        when(orderMapper.selectById(anyString())).thenReturn(order);
        when(orderMapper.updateById(any(PlanningOrder.class))).thenAnswer(inv -> {
            PlanningOrder updated = inv.getArgument(0);
            capturedUpdates.add(copyOrder(updated));
            return 1;
        });

        inject(service, "planningOrderMapper", orderMapper);
        inject(service, "erpClient", erpClient);
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

    private PlanningOrder copyOrder(PlanningOrder source) {
        PlanningOrder copy = new PlanningOrder();
        copy.setId(source.getId());
        copy.setExternalOrderId(source.getExternalOrderId());
        copy.setProductType(source.getProductType());
        copy.setCustomerName(source.getCustomerName());
        copy.setQuantity(source.getQuantity());
        copy.setFulfillmentQty(source.getFulfillmentQty());
        copy.setFulfillmentStatus(source.getFulfillmentStatus());
        copy.setStatus(source.getStatus());
        copy.setValidationStatus(source.getValidationStatus());
        copy.setSysOrgCode(source.getSysOrgCode());
        return copy;
    }

    // ==================== Builders ====================

    private PlanningOrder buildOrder(String orderId, BigDecimal orderQuantity,
                                     BigDecimal currentFulfillmentQty) {
        PlanningOrder order = new PlanningOrder();
        order.setId(orderId);
        order.setExternalOrderId("EXT-" + orderId);
        order.setProductType("ProductA");
        order.setCustomerName("Customer1");
        order.setQuantity(orderQuantity);
        order.setFulfillmentQty(currentFulfillmentQty);
        order.setStatus("in_production");
        order.setValidationStatus("valid");
        order.setFulfillmentStatus("in_production");
        order.setSysOrgCode("ORG001");
        return order;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<BigDecimal> orderQuantities() {
        // Order quantities: 1 to 10000
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("1"), new BigDecimal("10000"))
                .ofScale(2);
    }

    @Provide
    Arbitrary<BigDecimal> positiveReceivedQuantities() {
        // Received quantities > 0
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("10000"))
                .ofScale(2);
    }

    // ==================== Property 26a: In Production status ====================

    /**
     * Property 26a: When received quantity is zero (R=0) and the order is in production,
     * the fulfillment status should be "in_production".
     *
     * **Validates: Requirements 11.1**
     */
    @Property(tries = 300)
    void statusIsInProductionWhenReceivedQuantityIsZero(
            @ForAll("orderQuantities") BigDecimal orderQuantity) {

        String orderId = UUID.randomUUID().toString();
        // Order starts with zero fulfillment
        PlanningOrder order = buildOrder(orderId, orderQuantity, BigDecimal.ZERO);

        List<PlanningOrder> capturedUpdates = new ArrayList<>();
        FinishedGoodsDispatchServiceImpl service = createService(order, capturedUpdates);

        // Receive zero quantity — simulates the case where fulfillment is checked
        // but nothing new has been received yet. The method adds receivedQty to current.
        // With currentFulfillment=0 and receivedQty=0, newFulfillment=0 → in_production
        service.updateFulfillmentStatus(orderId, BigDecimal.ZERO);

        assertThat(capturedUpdates)
                .as("Order should be updated")
                .hasSize(1);

        PlanningOrder updated = capturedUpdates.get(0);
        assertThat(updated.getFulfillmentStatus())
                .as("Status should be 'in_production' when received quantity is zero")
                .isEqualTo("in_production");

        assertThat(updated.getFulfillmentQty())
                .as("Fulfillment quantity should remain zero")
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ==================== Property 26b: Partially Fulfilled status ====================

    /**
     * Property 26b: When 0 < R < Q (received quantity is positive but less than order quantity),
     * the fulfillment status should be "partially_fulfilled".
     *
     * **Validates: Requirements 11.1, 11.5**
     */
    @Property(tries = 300)
    void statusIsPartiallyFulfilledWhenReceivedBetweenZeroAndOrderQty(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("positiveReceivedQuantities") BigDecimal receivedQty) {

        // Ensure 0 < receivedQty < orderQuantity (strictly less)
        Assume.that(receivedQty.compareTo(BigDecimal.ZERO) > 0);
        Assume.that(receivedQty.compareTo(orderQuantity) < 0);

        String orderId = UUID.randomUUID().toString();
        // Order starts with zero fulfillment
        PlanningOrder order = buildOrder(orderId, orderQuantity, BigDecimal.ZERO);

        List<PlanningOrder> capturedUpdates = new ArrayList<>();
        FinishedGoodsDispatchServiceImpl service = createService(order, capturedUpdates);

        // Receive partial quantity
        service.updateFulfillmentStatus(orderId, receivedQty);

        assertThat(capturedUpdates)
                .as("Order should be updated")
                .hasSize(1);

        PlanningOrder updated = capturedUpdates.get(0);
        assertThat(updated.getFulfillmentStatus())
                .as("Status should be 'partially_fulfilled' when 0 < R < Q")
                .isEqualTo("partially_fulfilled");

        assertThat(updated.getFulfillmentQty())
                .as("Fulfillment quantity should equal received quantity")
                .isEqualByComparingTo(receivedQty);

        // Verify remaining quantity = Q - R
        BigDecimal remaining = orderQuantity.subtract(receivedQty);
        assertThat(orderQuantity.subtract(updated.getFulfillmentQty()))
                .as("Remaining quantity should be Q - R")
                .isEqualByComparingTo(remaining);
    }

    // ==================== Property 26c: Fully Fulfilled status ====================

    /**
     * Property 26c: When R ≥ Q (received quantity meets or exceeds order quantity),
     * the fulfillment status should be "fully_fulfilled".
     *
     * **Validates: Requirements 11.1, 11.2**
     */
    @Property(tries = 300)
    void statusIsFullyFulfilledWhenReceivedMeetsOrExceedsOrderQty(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("positiveReceivedQuantities") BigDecimal receivedQty) {

        // Ensure receivedQty >= orderQuantity
        Assume.that(receivedQty.compareTo(orderQuantity) >= 0);

        String orderId = UUID.randomUUID().toString();
        // Order starts with zero fulfillment
        PlanningOrder order = buildOrder(orderId, orderQuantity, BigDecimal.ZERO);

        List<PlanningOrder> capturedUpdates = new ArrayList<>();
        FinishedGoodsDispatchServiceImpl service = createService(order, capturedUpdates);

        // Receive full or excess quantity
        service.updateFulfillmentStatus(orderId, receivedQty);

        assertThat(capturedUpdates)
                .as("Order should be updated")
                .hasSize(1);

        PlanningOrder updated = capturedUpdates.get(0);
        assertThat(updated.getFulfillmentStatus())
                .as("Status should be 'fully_fulfilled' when R >= Q")
                .isEqualTo("fully_fulfilled");

        assertThat(updated.getStatus())
                .as("Order status should be set to 'fulfilled'")
                .isEqualTo("fulfilled");

        assertThat(updated.getFulfillmentQty())
                .as("Fulfillment quantity should equal received quantity")
                .isEqualByComparingTo(receivedQty);
    }

    // ==================== Property 26d: Remaining quantity calculation ====================

    /**
     * Property 26d: The remaining quantity is always correctly calculated as Q - R,
     * and is non-negative when R < Q, or zero/negative when R >= Q.
     *
     * **Validates: Requirements 11.1, 11.5**
     */
    @Property(tries = 300)
    void remainingQuantityCorrectlyCalculated(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("positiveReceivedQuantities") BigDecimal receivedQty) {

        String orderId = UUID.randomUUID().toString();
        PlanningOrder order = buildOrder(orderId, orderQuantity, BigDecimal.ZERO);

        List<PlanningOrder> capturedUpdates = new ArrayList<>();
        FinishedGoodsDispatchServiceImpl service = createService(order, capturedUpdates);

        service.updateFulfillmentStatus(orderId, receivedQty);

        assertThat(capturedUpdates)
                .as("Order should be updated")
                .hasSize(1);

        PlanningOrder updated = capturedUpdates.get(0);
        BigDecimal remaining = orderQuantity.subtract(updated.getFulfillmentQty());

        if (receivedQty.compareTo(orderQuantity) < 0) {
            // Partially fulfilled: remaining > 0
            assertThat(remaining)
                    .as("Remaining quantity should be positive when partially fulfilled")
                    .isGreaterThan(BigDecimal.ZERO);
            assertThat(remaining)
                    .as("Remaining = Q - R")
                    .isEqualByComparingTo(orderQuantity.subtract(receivedQty));
        } else {
            // Fully fulfilled: remaining <= 0
            assertThat(remaining)
                    .as("Remaining quantity should be zero or negative when fully fulfilled")
                    .isLessThanOrEqualTo(BigDecimal.ZERO);
        }
    }

    // ==================== Property 26e: Cumulative fulfillment ====================

    /**
     * Property 26e: Fulfillment is cumulative — multiple receipts accumulate correctly.
     * Starting with existing fulfillment F, receiving additional R results in total = F + R.
     * Status transitions correctly based on the cumulative total vs order quantity.
     *
     * **Validates: Requirements 11.1, 11.5**
     */
    @Property(tries = 300)
    void cumulativeFulfillmentAccumulatesCorrectly(
            @ForAll("orderQuantities") BigDecimal orderQuantity,
            @ForAll("positiveReceivedQuantities") BigDecimal existingFulfillment,
            @ForAll("positiveReceivedQuantities") BigDecimal additionalReceived) {

        // Ensure existing fulfillment is less than order quantity (order still in progress)
        Assume.that(existingFulfillment.compareTo(orderQuantity) < 0);

        String orderId = UUID.randomUUID().toString();
        // Order already has some fulfillment
        PlanningOrder order = buildOrder(orderId, orderQuantity, existingFulfillment);

        List<PlanningOrder> capturedUpdates = new ArrayList<>();
        FinishedGoodsDispatchServiceImpl service = createService(order, capturedUpdates);

        // Receive additional quantity
        service.updateFulfillmentStatus(orderId, additionalReceived);

        assertThat(capturedUpdates)
                .as("Order should be updated")
                .hasSize(1);

        PlanningOrder updated = capturedUpdates.get(0);
        BigDecimal expectedTotal = existingFulfillment.add(additionalReceived);

        assertThat(updated.getFulfillmentQty())
                .as("Fulfillment quantity should be cumulative: existing + additional")
                .isEqualByComparingTo(expectedTotal);

        // Verify status based on cumulative total
        if (expectedTotal.compareTo(BigDecimal.ZERO) == 0) {
            assertThat(updated.getFulfillmentStatus())
                    .isEqualTo("in_production");
        } else if (expectedTotal.compareTo(orderQuantity) >= 0) {
            assertThat(updated.getFulfillmentStatus())
                    .as("Status should be 'fully_fulfilled' when cumulative >= order qty")
                    .isEqualTo("fully_fulfilled");
        } else {
            assertThat(updated.getFulfillmentStatus())
                    .as("Status should be 'partially_fulfilled' when 0 < cumulative < order qty")
                    .isEqualTo("partially_fulfilled");
        }
    }
}
