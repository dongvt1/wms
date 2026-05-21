package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.qms.entity.QmsNotification;

/**
 * @Description: QMS Notification Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface QmsNotificationService extends IService<QmsNotification> {

    /**
     * Gửi thông báo yêu cầu phê duyệt đến Quản_lý_QC
     *
     * @param entityType loại phiếu: iqc/pqc/fqc/review
     * @param entityId   ID phiếu kiểm tra
     * @param title      tiêu đề thông báo
     * @param targetUserId ID người nhận (Quản_lý_QC)
     */
    void sendApprovalRequest(String entityType, String entityId, String title, String targetUserId);

    /**
     * Gửi thông báo kết quả phê duyệt đến Nhân_viên_QC đã tạo phiếu
     *
     * @param entityType loại phiếu: iqc/pqc/fqc/review
     * @param entityId   ID phiếu kiểm tra
     * @param result     kết quả: passed/failed/conditional/rejected
     * @param targetUserId ID người nhận (Nhân_viên_QC tạo phiếu)
     */
    void sendApprovalResult(String entityType, String entityId, String result, String targetUserId);

    /**
     * Lấy số lượng thông báo chưa đọc cho badge hiển thị
     *
     * @param userId ID người dùng
     * @return số thông báo chưa đọc
     */
    long getUnreadCount(String userId);

    /**
     * Đánh dấu một thông báo là đã đọc
     *
     * @param id ID thông báo
     */
    void markRead(String id);

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc
     *
     * @param userId ID người dùng
     */
    void markAllRead(String userId);

    /**
     * Gửi thông báo nhắc nhở cho các phiếu pending_approval > 24h.
     * Được gọi bởi scheduled job chạy mỗi giờ.
     */
    void sendReminder();
}
