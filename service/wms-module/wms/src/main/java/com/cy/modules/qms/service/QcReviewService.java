package qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import qms.entity.QcReview;

import java.util.Map;

public interface QcReviewService extends IService<QcReview> {

    String generateReviewCode();

    /** Lấy hoặc tạo review cho WO, tổng hợp các sessions */
    QcReview getOrCreateByWorkOrder(String workOrderId);

    /** Đồng bộ thống kê sessions vào review */
    void syncStats(String reviewId);

    /** Nộp chờ phê duyệt: draft → pending_approval */
    String submit(String id, String reviewer);

    /** Phê duyệt: pending_approval → approved */
    String approve(String id, String approver, String overallResult, String notes);

    /** Từ chối: pending_approval → rejected */
    String reject(String id, String approver, String reason);

    Map<String, Object> getDetail(String reviewId);
}
