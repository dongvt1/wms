package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.qms.entity.QcReview;
import com.cy.modules.qms.entity.QcSession;
import com.cy.modules.qms.mapper.QcReviewMapper;
import com.cy.modules.qms.mapper.QcSessionMapper;
import com.cy.modules.qms.service.QcReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class QcReviewServiceImpl extends ServiceImpl<QcReviewMapper, QcReview>
        implements QcReviewService {

    @Autowired private QcSessionMapper sessionMapper;

    @Override
    public String generateReviewCode() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        QueryWrapper<QcReview> qw = new QueryWrapper<>();
        qw.likeRight("review_code", "RV" + dateStr).orderByDesc("review_code").last("LIMIT 1");
        QcReview last = this.getOne(qw);
        int seq = 1;
        if (last != null) {
            try { seq = Integer.parseInt(last.getReviewCode().substring(last.getReviewCode().length() - 3)) + 1; }
            catch (NumberFormatException e) { seq = 1; }
        }
        return "RV" + dateStr + String.format("%03d", seq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QcReview getOrCreateByWorkOrder(String workOrderId) {
        QcReview existing = this.getOne(new QueryWrapper<QcReview>().eq("work_order_id", workOrderId));
        if (existing != null) {
            syncStats(existing.getId());
            return this.getById(existing.getId());
        }
        QcReview review = new QcReview();
        review.setId(UUID.randomUUID().toString());
        review.setReviewCode(generateReviewCode());
        review.setWorkOrderId(workOrderId);
        review.setStatus("draft");
        review.setTotalSessions(0);
        review.setPassedSessions(0);
        review.setFailedSessions(0);
        this.save(review);
        syncStats(review.getId());
        return this.getById(review.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncStats(String reviewId) {
        QcReview review = this.getById(reviewId);
        if (review == null) return;
        List<QcSession> sessions = sessionMapper.selectList(
            new QueryWrapper<QcSession>().eq("work_order_id", review.getWorkOrderId()));
        int total = sessions.size();
        long passed = sessions.stream().filter(s -> "completed".equals(s.getStatus())).count();
        long failed = sessions.stream().filter(s -> "draft".equals(s.getStatus())).count(); // unfinished
        review.setTotalSessions(total);
        review.setPassedSessions((int) passed);
        review.setFailedSessions(total - (int) passed);
        this.updateById(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submit(String id, String reviewer) {
        QcReview review = this.getById(id);
        if (review == null) return "Không tìm thấy review";
        if (!"draft".equals(review.getStatus()))
            return "Chỉ review ở trạng thái draft mới có thể nộp phê duyệt";
        syncStats(id);
        review = this.getById(id);
        review.setStatus("pending_approval");
        review.setReviewer(reviewer);
        this.updateById(review);
        return "Nộp review chờ phê duyệt thành công";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approve(String id, String approver, String overallResult, String notes) {
        QcReview review = this.getById(id);
        if (review == null) return "Không tìm thấy review";
        if (!"pending_approval".equals(review.getStatus()))
            return "Chỉ review đang chờ phê duyệt mới có thể được duyệt";
        review.setStatus("approved");
        review.setOverallResult(overallResult);
        review.setApprover(approver);
        review.setApprovalDate(new Date());
        if (notes != null && !notes.isEmpty()) review.setNotes(notes);
        this.updateById(review);
        return "Phê duyệt review thành công";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String reject(String id, String approver, String reason) {
        QcReview review = this.getById(id);
        if (review == null) return "Không tìm thấy review";
        if (!"pending_approval".equals(review.getStatus()))
            return "Chỉ review đang chờ phê duyệt mới có thể bị từ chối";
        review.setStatus("rejected");
        review.setApprover(approver);
        review.setApprovalDate(new Date());
        review.setRejectionReason(reason);
        this.updateById(review);
        return "Đã từ chối review";
    }

    @Override
    public Map<String, Object> getDetail(String reviewId) {
        QcReview review = this.getById(reviewId);
        Map<String, Object> result = new HashMap<>();
        result.put("review", review);
        if (review != null) {
            List<QcSession> sessions = sessionMapper.selectList(
                new QueryWrapper<QcSession>().eq("work_order_id", review.getWorkOrderId())
                    .orderByAsc("create_time"));
            result.put("sessions", sessions);
        }
        return result;
    }
}
