package com.cy.modules.planning.agent.service;

/**
 * Service interface cho đồng bộ đơn hàng từ OrderHub.
 * Thực hiện polling định kỳ mỗi 5 phút để lấy đơn hàng mới,
 * chuyển đổi sang PlanningOrder và lưu vào hệ thống.
 */
public interface OrderSyncService {

    /**
     * Thực hiện đồng bộ đơn hàng từ OrderHub.
     * Lấy đơn hàng mới kể từ lần đồng bộ thành công gần nhất,
     * chuyển đổi và lưu vào bảng ap_planning_order,
     * cập nhật trạng thái đồng bộ trong ap_sync_status.
     */
    void syncOrders();

    /**
     * Kích hoạt đồng bộ thủ công (manual trigger).
     * Có thể được gọi từ controller hoặc admin endpoint.
     */
    void triggerManualSync();
}
