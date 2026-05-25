package com.cy.modules.planning.agent.client;

import com.cy.modules.planning.agent.dto.ExternalOrder;
import com.cy.modules.planning.agent.dto.OrderDetail;

import java.time.Instant;
import java.util.List;

/**
 * Client interface cho tích hợp với hệ thống OrderHub.
 * Cung cấp khả năng lấy đơn hàng mới và chi tiết đơn hàng.
 */
public interface OrderHubClient {

    /**
     * Lấy danh sách đơn hàng mới từ OrderHub kể từ thời điểm chỉ định.
     *
     * @param since thời điểm bắt đầu lấy đơn hàng (exclusive)
     * @return danh sách đơn hàng mới
     */
    List<ExternalOrder> fetchNewOrders(Instant since);

    /**
     * Lấy chi tiết đơn hàng theo mã đơn hàng.
     *
     * @param orderId mã đơn hàng từ OrderHub
     * @return chi tiết đơn hàng
     */
    OrderDetail getOrderDetail(String orderId);
}
