package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import com.cy.modules.qms.entity.FqcInspection;
import com.cy.modules.qms.entity.FqcInspectionResult;
import com.cy.modules.qms.mapper.FqcInspectionMapper;
import com.cy.modules.qms.mapper.FqcInspectionResultMapper;
import com.cy.modules.qms.service.FqcInspectionService;
import com.cy.modules.qms.service.QmsNotificationService;
import com.cy.modules.qms.util.QmsCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description: FQC Inspection Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
@Slf4j
public class FqcInspectionServiceImpl extends ServiceImpl<FqcInspectionMapper, FqcInspection>
        implements FqcInspectionService {

    @Autowired
    private FqcInspectionResultMapper resultMapper;

    @Autowired
    private QmsNotificationService notificationService;

    @Override
    public String generateInspectionCode() {
        return QmsCodeGenerator.generateCode("FQC", "inspection_code", this.baseMapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithResults(FqcInspection inspection, List<FqcInspectionResult> results) {
        this.save(inspection);
        saveResults(inspection.getId(), results);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithResults(FqcInspection inspection, List<FqcInspectionResult> results) {
        this.updateById(inspection);
        // Xóa kết quả cũ và thay thế bằng kết quả mới
        QueryWrapper<FqcInspectionResult> delQw = new QueryWrapper<>();
        delQw.eq("inspection_id", inspection.getId());
        resultMapper.delete(delQw);
        saveResults(inspection.getId(), results);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitForApproval(String id) {
        FqcInspection inspection = this.getById(id);
        if (inspection == null) {
            return "Không tìm thấy phiếu FQC";
        }
        if (!"in_progress".equals(inspection.getStatus())) {
            return "Chỉ phiếu đang kiểm tra (in_progress) mới được nộp phê duyệt";
        }
        inspection.setStatus("pending_approval");
        this.updateById(inspection);

        // Send approval request notification (failure must not roll back main operation)
        try {
            String title = "Phiếu FQC " + inspection.getInspectionCode() + " cần phê duyệt";
            notificationService.sendApprovalRequest("fqc", id, title, null);
        } catch (Exception e) {
            log.warn("Failed to send approval notification: {}", e.getMessage());
        }

        return "Nộp phiếu FQC chờ phê duyệt thành công";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approveInspection(String id, String status, String notes, String operator) {
        FqcInspection inspection = this.getById(id);
        if (inspection == null) {
            return "Không tìm thấy phiếu FQC";
        }
        if (!"pending_approval".equals(inspection.getStatus())) {
            return "Chỉ phiếu đang chờ phê duyệt (pending_approval) mới được duyệt";
        }
        // FQC chỉ cho phép passed hoặc failed (không có conditional)
        if (!"passed".equals(status) && !"failed".equals(status)) {
            return "Trạng thái duyệt FQC chỉ được là passed hoặc failed";
        }
        inspection.setStatus(status);
        if (notes != null && !notes.isEmpty()) {
            inspection.setNotes(notes);
        }
        inspection.setUpdateBy(operator);
        this.updateById(inspection);

        // Send approval result notification (failure must not roll back main operation)
        try {
            notificationService.sendApprovalResult("fqc", id, status, inspection.getCreateBy());
        } catch (Exception e) {
            log.warn("Failed to send approval result notification: {}", e.getMessage());
        }

        return "Duyệt phiếu FQC thành công: " + status;
    }

    @Override
    public List<FqcInspectionResult> getResults(String inspectionId) {
        QueryWrapper<FqcInspectionResult> qw = new QueryWrapper<>();
        qw.eq("inspection_id", inspectionId);
        return resultMapper.selectList(qw);
    }

    @Override
    public Map<String, Object> getDetail(String inspectionId) {
        Map<String, Object> result = new HashMap<>();
        result.put("inspection", this.getById(inspectionId));
        result.put("results", getResults(inspectionId));
        return result;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInspections", count());
        stats.put("draftCount", count(new QueryWrapper<FqcInspection>().eq("status", "draft")));
        stats.put("inProgressCount", count(new QueryWrapper<FqcInspection>().eq("status", "in_progress")));
        stats.put("pendingApprovalCount", count(new QueryWrapper<FqcInspection>().eq("status", "pending_approval")));
        stats.put("passedCount", count(new QueryWrapper<FqcInspection>().eq("status", "passed")));
        stats.put("failedCount", count(new QueryWrapper<FqcInspection>().eq("status", "failed")));
        return stats;
    }

    @Override
    public boolean isOutboundAllowed(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            return true; // Nếu không có đơn hàng xuất liên kết, cho phép xuất
        }
        QueryWrapper<FqcInspection> qw = new QueryWrapper<>();
        qw.eq("outbound_order_id", orderId);
        qw.eq("status", "passed");
        return count(qw) > 0;
    }

    private void saveResults(String inspectionId, List<FqcInspectionResult> results) {
        if (results == null || results.isEmpty()) return;
        for (FqcInspectionResult r : results) {
            r.setId(UUID.randomUUID().toString());
            r.setInspectionId(inspectionId);
            resultMapper.insert(r);
        }
    }
}
