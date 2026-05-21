package com.cy.modules.qms.service;

/**
 * Service quản lý quy trình phê duyệt kết quả kiểm tra.
 * Hỗ trợ: approve, reject, re-inspect.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public interface ApprovalService {

    /**
     * Phê duyệt kết quả kiểm tra.
     * <p>
     * Logic:
     * 1. Validate execution tồn tại và status = "pending_approval"
     * 2. Chuyển status → "approved" (sử dụng ExecutionStateMachine)
     * 3. Ghi nhận approvedBy, approvedTime trên execution
     * 4. Tạo ApprovalRecord với action = "approve"
     *
     * @param executionId ID phiên kiểm tra
     * @param comment     Ghi chú phê duyệt (tùy chọn)
     * @throws IllegalArgumentException nếu execution không tồn tại
     * @throws IllegalStateException    nếu execution không ở trạng thái pending_approval
     */
    void approve(String executionId, String comment);

    /**
     * Từ chối kết quả kiểm tra.
     * <p>
     * Logic:
     * 1. Validate execution tồn tại và status = "pending_approval"
     * 2. Validate reason không được để trống
     * 3. Chuyển status → "rejected" (sử dụng ExecutionStateMachine)
     * 4. Tạo ApprovalRecord với action = "reject"
     * 5. Gửi notification cho Nhân_viên_QC kèm lý do từ chối
     *
     * @param executionId ID phiên kiểm tra
     * @param reason      Lý do từ chối (bắt buộc)
     * @throws IllegalArgumentException nếu execution không tồn tại hoặc reason trống
     * @throws IllegalStateException    nếu execution không ở trạng thái pending_approval
     */
    void reject(String executionId, String reason);

    /**
     * Yêu cầu kiểm tra lại một bước cụ thể.
     * <p>
     * Logic:
     * 1. Validate execution tồn tại và status = "pending_approval"
     * 2. Validate stepResultId thuộc execution
     * 3. Validate reason không được để trống
     * 4. Reset step cụ thể: status → "re_inspect", result → null
     * 5. Các steps khác giữ nguyên trạng thái (không bị ảnh hưởng)
     * 6. Chuyển execution status → "in_progress" (sử dụng ExecutionStateMachine)
     * 7. Tạo ApprovalRecord với action = "re_inspect", stepResultId
     * 8. Gửi notification cho Nhân_viên_QC kèm lý do yêu cầu kiểm tra lại
     *
     * @param executionId  ID phiên kiểm tra
     * @param stepResultId ID kết quả bước cần kiểm tra lại
     * @param reason       Lý do yêu cầu kiểm tra lại (bắt buộc)
     * @throws IllegalArgumentException nếu execution/stepResult không tồn tại hoặc reason trống
     * @throws IllegalStateException    nếu execution không ở trạng thái pending_approval
     */
    void reInspect(String executionId, String stepResultId, String reason);
}
