package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.dto.FulfillmentDashboardDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface cho theo dõi thành phẩm và giao hàng (Finished Goods Dispatch).
 * Cập nhật trạng thái hoàn thành đơn hàng trong vòng 5 phút sau khi nhập kho,
 * thông báo giao hàng cho kho bán hàng qua ERP trong vòng 10 phút khi đơn hàng hoàn thành,
 * và duy trì dashboard hoàn thành đơn hàng (refresh ≤15 phút).
 */
public interface FinishedGoodsDispatchService {

    /**
     * Cập nhật trạng thái hoàn thành đơn hàng khi nhận thành phẩm vào kho.
     * Tính toán fulfillment_qty và xác định fulfillment_status:
     * - "in_production" khi R=0 và đơn hàng đang sản xuất
     * - "partially_fulfilled" khi 0 < R < Q (số lượng nhận > 0 nhưng chưa đủ)
     * - "fully_fulfilled" khi R ≥ Q (số lượng nhận đủ hoặc vượt đơn hàng)
     *
     * @param orderId     ID đơn hàng (ap_planning_order.id)
     * @param receivedQty số lượng thành phẩm nhận vào kho
     */
    void updateFulfillmentStatus(String orderId, BigDecimal receivedQty);

    /**
     * Thông báo giao hàng cho kho bán hàng qua ERP khi đơn hàng đã hoàn thành.
     * Retry logic: 3 lần thử. Nếu thất bại sau 3 lần → thông báo quản lý sản xuất.
     *
     * @param orderId ID đơn hàng đã hoàn thành (fully_fulfilled)
     */
    void notifyDispatch(String orderId);

    /**
     * Lấy dữ liệu dashboard hoàn thành đơn hàng.
     * Trả về danh sách DTO bao gồm: số lượng sản xuất, tồn kho, đã giao, % hoàn thành cho mỗi đơn hàng.
     *
     * @return danh sách FulfillmentDashboardDto
     */
    List<FulfillmentDashboardDto> getDashboardData();
}
