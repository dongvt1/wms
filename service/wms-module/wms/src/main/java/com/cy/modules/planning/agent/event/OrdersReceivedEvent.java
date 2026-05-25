package com.cy.modules.planning.agent.event;

import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Spring Application Event được phát ra sau khi đồng bộ đơn hàng thành công từ OrderHub.
 * Chứa danh sách ID của các đơn hàng mới được tạo trong hệ thống.
 */
public class OrdersReceivedEvent extends ApplicationEvent {

    private final List<String> newOrderIds;

    /**
     * Tạo event mới với danh sách ID đơn hàng.
     *
     * @param source      nguồn phát event
     * @param newOrderIds danh sách ID đơn hàng mới (ap_planning_order.id)
     */
    public OrdersReceivedEvent(Object source, List<String> newOrderIds) {
        super(source);
        this.newOrderIds = newOrderIds;
    }

    /**
     * Lấy danh sách ID đơn hàng mới được đồng bộ.
     *
     * @return danh sách ID đơn hàng
     */
    public List<String> getNewOrderIds() {
        return newOrderIds;
    }
}
