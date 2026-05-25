package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.entity.PlanningOrder;

import java.util.List;
import java.util.Map;

/**
 * Service interface cho xử lý và xác thực đơn hàng đầu vào.
 * Nhận đơn hàng từ OrderSyncService (qua OrdersReceivedEvent),
 * xác thực tính đầy đủ và hợp lệ, phân loại và duy trì hàng đợi ưu tiên.
 */
public interface OrderIngestionService {

    /**
     * Xử lý danh sách đơn hàng mới: xác thực, phân loại, cập nhật hàng đợi ưu tiên.
     *
     * @param orderIds danh sách ID đơn hàng cần xử lý
     */
    void processNewOrders(List<String> orderIds);

    /**
     * Lấy hàng đợi đơn hàng ưu tiên (chỉ đơn hàng valid + pending),
     * sắp xếp theo deadline ASC, receipt_timestamp ASC.
     *
     * @return danh sách đơn hàng đã sắp xếp theo ưu tiên
     */
    List<PlanningOrder> getPrioritizedOrderQueue();

    /**
     * Lấy đơn hàng đã gom nhóm theo loại sản phẩm,
     * mỗi nhóm sắp xếp theo deadline ASC.
     *
     * @return map với key là product_type, value là danh sách đơn hàng
     */
    Map<String, List<PlanningOrder>> getOrdersGroupedByProductType();
}
