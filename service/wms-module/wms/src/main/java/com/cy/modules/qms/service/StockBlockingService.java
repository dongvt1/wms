package com.cy.modules.qms.service;

/**
 * @Description: Stock Blocking Service - Quản lý trạng thái QC của phiếu nhập kho
 *               Tích hợp chặn kho khi IQC không đạt (Requirements 8.1, 8.2, 8.3, 8.4)
 * @Author: BMad
 * @Date: 2026-03-01
 */
public interface StockBlockingService {

    /**
     * Xử lý kết quả phê duyệt IQC và cập nhật qc_status của stock transaction.
     * Mapping: failed → blocked, conditional → conditional_hold, passed → available
     *
     * @param inspectionId ID phiếu IQC
     * @param status       Kết quả phê duyệt: passed, failed, conditional
     * @return Thông báo kết quả
     */
    String handleIqcApproval(String inspectionId, String status);

    /**
     * Kiểm tra nguyên liệu có sẵn sàng để sử dụng trong Work Order hay không.
     * Trả về true nếu qc_status là 'available' hoặc 'pending' (chưa kiểm tra QC).
     * Trả về false nếu qc_status là 'blocked' hoặc 'conditional_hold'.
     *
     * @param stockTransactionId ID phiếu nhập kho
     * @return true nếu nguyên liệu có thể sử dụng
     */
    boolean isStockAvailable(String stockTransactionId);

    /**
     * Giải phóng chặn kho sau khi NCR được xử lý xong.
     * Cập nhật qc_status sang 'available' và ghi nhận NCR ID.
     *
     * @param stockTransactionId ID phiếu nhập kho
     * @param ncrId              ID NCR đã xử lý
     * @return Thông báo kết quả
     */
    String releaseBlock(String stockTransactionId, String ncrId);
}
