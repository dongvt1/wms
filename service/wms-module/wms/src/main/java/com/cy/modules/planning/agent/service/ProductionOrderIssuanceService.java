package com.cy.modules.planning.agent.service;

/**
 * Service interface cho phát lệnh sản xuất (Production Order Issuance).
 * Tạo lệnh sản xuất trong ERP trong vòng 5 phút sau khi kế hoạch tuần được duyệt,
 * kích hoạt xuất kho nguyên vật liệu theo BOM, và cập nhật trạng thái kế hoạch.
 */
public interface ProductionOrderIssuanceService {

    /**
     * Phát lệnh sản xuất cho tất cả batch trong kế hoạch tuần đã được duyệt.
     * Mỗi lệnh bao gồm: thông số sản phẩm, số lượng, dây chuyền, máy, thời gian bắt đầu/kết thúc.
     * Sau khi tạo lệnh thành công, kích hoạt xuất kho nguyên vật liệu theo BOM.
     * Khi tất cả lệnh được ERP xác nhận, cập nhật trạng thái kế hoạch sang "in_execution".
     *
     * @param weeklyPlanId ID kế hoạch tuần đã duyệt
     */
    void issueProductionOrders(String weeklyPlanId);

    /**
     * Thử lại phát lệnh sản xuất cho batch bị lỗi.
     * Retry logic: 3 lần thử, mỗi lần cách nhau 60 giây.
     * Sau 3 lần thất bại: thông báo quản lý sản xuất và đặt batch ở trạng thái on_hold.
     *
     * @param batchId ID batch cần thử lại
     */
    void retryFailedOrder(String batchId);
}
