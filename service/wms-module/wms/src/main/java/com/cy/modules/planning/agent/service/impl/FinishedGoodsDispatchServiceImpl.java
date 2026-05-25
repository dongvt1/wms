package com.cy.modules.planning.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.modules.planning.agent.client.ErpClient;
import com.cy.modules.planning.agent.dto.DispatchNotification;
import com.cy.modules.planning.agent.dto.FulfillmentDashboardDto;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.mapper.PlanningOrderMapper;
import com.cy.modules.planning.agent.service.FinishedGoodsDispatchService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation của FinishedGoodsDispatchService.
 * Cập nhật trạng thái hoàn thành đơn hàng, thông báo giao hàng qua ERP,
 * và cung cấp dữ liệu dashboard hoàn thành.
 */
@Slf4j
@Service
public class FinishedGoodsDispatchServiceImpl implements FinishedGoodsDispatchService {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final String STATUS_IN_PRODUCTION = "in_production";
    private static final String STATUS_PARTIALLY_FULFILLED = "partially_fulfilled";
    private static final String STATUS_FULLY_FULFILLED = "fully_fulfilled";

    @Autowired
    private PlanningOrderMapper planningOrderMapper;

    @Autowired
    private ErpClient erpClient;

    @Autowired
    private PlanningNotificationService planningNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFulfillmentStatus(String orderId, BigDecimal receivedQty) {
        log.info("[FinishedGoodsDispatch] Cập nhật fulfillment cho orderId={}, receivedQty={}", orderId, receivedQty);

        PlanningOrder order = planningOrderMapper.selectById(orderId);
        if (order == null) {
            log.error("[FinishedGoodsDispatch] Không tìm thấy đơn hàng: {}", orderId);
            return;
        }

        // Cộng dồn số lượng đã hoàn thành
        BigDecimal currentFulfillment = order.getFulfillmentQty() != null ? order.getFulfillmentQty() : BigDecimal.ZERO;
        BigDecimal newFulfillment = currentFulfillment.add(receivedQty);
        order.setFulfillmentQty(newFulfillment);

        // Xác định trạng thái hoàn thành
        BigDecimal orderQuantity = order.getQuantity();
        if (newFulfillment.compareTo(BigDecimal.ZERO) == 0) {
            order.setFulfillmentStatus(STATUS_IN_PRODUCTION);
        } else if (newFulfillment.compareTo(orderQuantity) >= 0) {
            order.setFulfillmentStatus(STATUS_FULLY_FULFILLED);
            order.setStatus("fulfilled");
        } else {
            order.setFulfillmentStatus(STATUS_PARTIALLY_FULFILLED);
        }

        planningOrderMapper.updateById(order);
        log.info("[FinishedGoodsDispatch] Đơn hàng {} cập nhật: fulfillmentQty={}, status={}",
                orderId, newFulfillment, order.getFulfillmentStatus());

        // Tự động thông báo giao hàng khi đơn hàng hoàn thành
        if (STATUS_FULLY_FULFILLED.equals(order.getFulfillmentStatus())) {
            notifyDispatch(orderId);
        }
    }

    @Override
    public void notifyDispatch(String orderId) {
        log.info("[FinishedGoodsDispatch] Thông báo giao hàng cho orderId={}", orderId);

        PlanningOrder order = planningOrderMapper.selectById(orderId);
        if (order == null) {
            log.error("[FinishedGoodsDispatch] Không tìm thấy đơn hàng: {}", orderId);
            return;
        }

        DispatchNotification notification = DispatchNotification.builder()
                .orderId(order.getExternalOrderId())
                .customerName(order.getCustomerName())
                .items(Collections.singletonList(
                        DispatchNotification.DispatchItem.builder()
                                .productType(order.getProductType())
                                .quantity(order.getFulfillmentQty())
                                .build()
                ))
                .build();

        // Retry logic: 3 lần thử
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                erpClient.notifyDispatch(notification);
                log.info("[FinishedGoodsDispatch] Thông báo giao hàng thành công cho orderId={} (lần thử {})",
                        orderId, attempt);
                return;
            } catch (Exception e) {
                log.warn("[FinishedGoodsDispatch] Lần thử {} thất bại cho orderId={}: {}",
                        attempt, orderId, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(5000); // Chờ 5 giây giữa các lần thử
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // Sau 3 lần thất bại: thông báo quản lý sản xuất
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("externalOrderId", order.getExternalOrderId());
        data.put("customerName", order.getCustomerName());
        data.put("reason", "Thông báo giao hàng thất bại sau 3 lần thử");

        planningNotificationService.notifyProductionManager(
                NotificationType.SYSTEM_ERROR,
                String.format("Thông báo giao hàng thất bại cho đơn hàng %s (KH: %s)",
                        order.getExternalOrderId(), order.getCustomerName()),
                data
        );

        log.error("[FinishedGoodsDispatch] Thông báo giao hàng thất bại sau 3 lần thử cho orderId={}", orderId);
    }

    @Override
    public List<FulfillmentDashboardDto> getDashboardData() {
        log.debug("[FinishedGoodsDispatch] Lấy dữ liệu dashboard hoàn thành đơn hàng");

        // Lấy tất cả đơn hàng đang sản xuất hoặc đã hoàn thành
        LambdaQueryWrapper<PlanningOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PlanningOrder::getStatus, "confirmed", "in_production", "fulfilled");
        List<PlanningOrder> orders = planningOrderMapper.selectList(wrapper);

        return orders.stream()
                .map(this::buildDashboardDto)
                .collect(Collectors.toList());
    }

    // ==================== Private Methods ====================

    private FulfillmentDashboardDto buildDashboardDto(PlanningOrder order) {
        BigDecimal orderQty = order.getQuantity() != null ? order.getQuantity() : BigDecimal.ZERO;
        BigDecimal fulfillmentQty = order.getFulfillmentQty() != null ? order.getFulfillmentQty() : BigDecimal.ZERO;

        // Tính % hoàn thành
        BigDecimal fulfillmentPct = BigDecimal.ZERO;
        if (orderQty.compareTo(BigDecimal.ZERO) > 0) {
            fulfillmentPct = fulfillmentQty
                    .multiply(BigDecimal.valueOf(100))
                    .divide(orderQty, 2, RoundingMode.HALF_UP);
        }

        // Số lượng đã giao = fulfillmentQty nếu fully_fulfilled, ngược lại = 0
        BigDecimal dispatchedQty = STATUS_FULLY_FULFILLED.equals(order.getFulfillmentStatus())
                ? fulfillmentQty : BigDecimal.ZERO;

        // Tồn kho = fulfillmentQty - dispatchedQty
        BigDecimal warehouseStock = fulfillmentQty.subtract(dispatchedQty);

        return FulfillmentDashboardDto.builder()
                .orderId(order.getId())
                .externalOrderId(order.getExternalOrderId())
                .customerName(order.getCustomerName())
                .productType(order.getProductType())
                .orderQuantity(orderQty)
                .producedQty(fulfillmentQty)
                .warehouseStock(warehouseStock)
                .dispatchedQty(dispatchedQty)
                .fulfillmentPercentage(fulfillmentPct)
                .fulfillmentStatus(order.getFulfillmentStatus())
                .build();
    }
}
