package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.dto.DashboardUpdate;
import com.cy.modules.planning.agent.enums.NotificationType;

import java.util.List;
import java.util.Map;

/**
 * Service interface cho gửi thông báo từ Planning Agent.
 * Hỗ trợ thông báo đến quản lý sản xuất, chủ đơn hàng, và cập nhật dashboard.
 */
public interface PlanningNotificationService {

    /**
     * Gửi thông báo đến quản lý sản xuất.
     *
     * @param type    loại thông báo
     * @param message nội dung thông báo
     * @param data    dữ liệu bổ sung
     */
    void notifyProductionManager(NotificationType type, String message, Map<String, Object> data);

    /**
     * Gửi thông báo đến chủ sở hữu các đơn hàng.
     *
     * @param orderIds danh sách mã đơn hàng bị ảnh hưởng
     * @param message  nội dung thông báo
     */
    void notifyOrderOwners(List<String> orderIds, String message);

    /**
     * Đẩy cập nhật dashboard qua WebSocket.
     *
     * @param update dữ liệu cập nhật dashboard
     */
    void pushDashboardUpdate(DashboardUpdate update);
}
