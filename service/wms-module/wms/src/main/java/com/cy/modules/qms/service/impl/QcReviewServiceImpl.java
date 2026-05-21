package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.qms.entity.QcReview;
import com.cy.modules.qms.entity.QcSession;
import com.cy.modules.qms.entity.QcSessionValue;
import com.cy.modules.qms.mapper.QcReviewMapper;
import com.cy.modules.qms.mapper.QcSessionMapper;
import com.cy.modules.qms.mapper.QcSessionValueMapper;
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
    @Autowired private QcSessionValueMapper sessionValueMapper;

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
        int passed = 0;
        int failed = 0;

        for (QcSession session : sessions) {
            List<QcSessionValue> values = sessionValueMapper.selectList(
                new QueryWrapper<QcSessionValue>().eq("session_id", session.getId()));

            if (values.isEmpty()) {
                // Session with no values is not counted as passed or failed
                continue;
            }

            boolean hasFailedValue = values.stream()
                .anyMatch(v -> "failed".equals(v.getResult()));
            boolean allPassed = values.stream()
                .allMatch(v -> "passed".equals(v.getResult()) || "na".equals(v.getResult()));

            if (hasFailedValue) {
                failed++;
            } else if (allPassed) {
                passed++;
            }
        }

        review.setTotalSessions(total);
        review.setPassedSessions(passed);
        review.setFailedSessions(failed);
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

    @Override
    public String suggestOverallResult(String reviewId) {
        QcReview review = this.getById(reviewId);
        if (review == null) return null;

        // Ensure stats are up-to-date
        syncStats(reviewId);
        review = this.getById(reviewId);

        List<QcSession> sessions = sessionMapper.selectList(
            new QueryWrapper<QcSession>().eq("work_order_id", review.getWorkOrderId()));

        if (sessions.isEmpty()) {
            return "conditional";
        }

        boolean anyFailed = false;
        boolean allPassed = true;

        for (QcSession session : sessions) {
            List<QcSessionValue> values = sessionValueMapper.selectList(
                new QueryWrapper<QcSessionValue>().eq("session_id", session.getId()));

            if (values.isEmpty()) {
                allPassed = false;
                continue;
            }

            boolean sessionHasFailed = values.stream()
                .anyMatch(v -> "failed".equals(v.getResult()));
            boolean sessionAllPassed = values.stream()
                .allMatch(v -> "passed".equals(v.getResult()) || "na".equals(v.getResult()));

            if (sessionHasFailed) {
                anyFailed = true;
            }
            if (!sessionAllPassed) {
                allPassed = false;
            }
        }

        if (anyFailed) {
            return "failed";
        } else if (allPassed) {
            return "passed";
        } else {
            return "conditional";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String overrideResult(String reviewId, String result, String reason, String operator) {
        QcReview review = this.getById(reviewId);
        if (review == null) return "Không tìm thấy review";

        if (result == null || result.isEmpty()) {
            return "Kết quả không được để trống";
        }
        if (reason == null || reason.isEmpty()) {
            return "Lý do ghi đè không được để trống";
        }

        review.setOverallResult(result);
        // Store override reason in notes field with prefix to distinguish from regular notes
        String overrideNote = "[Override by " + (operator != null ? operator : "unknown") + "] " + reason;
        if (review.getNotes() != null && !review.getNotes().isEmpty()) {
            review.setNotes(review.getNotes() + "\n" + overrideNote);
        } else {
            review.setNotes(overrideNote);
        }
        this.updateById(review);
        return "Đã ghi đè kết quả tổng thể thành công";
    }
}
