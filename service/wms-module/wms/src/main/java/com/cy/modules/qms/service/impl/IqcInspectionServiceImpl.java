package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import com.cy.modules.qms.entity.IqcInspection;
import com.cy.modules.qms.entity.IqcInspectionResult;
import com.cy.modules.qms.mapper.IqcInspectionMapper;
import com.cy.modules.qms.mapper.IqcInspectionResultMapper;
import com.cy.modules.qms.service.IqcInspectionService;
import com.cy.modules.qms.service.QmsNotificationService;
import com.cy.modules.qms.service.StockBlockingService;
import com.cy.modules.qms.util.QmsCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description: IQC Inspection Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
@Slf4j
public class IqcInspectionServiceImpl extends ServiceImpl<IqcInspectionMapper, IqcInspection>
        implements IqcInspectionService {

    @Autowired
    private IqcInspectionResultMapper resultMapper;

    @Autowired
    private QmsNotificationService notificationService;

    @Autowired
    private StockBlockingService stockBlockingService;

    @Override
    public String generateInspectionCode() {
        return QmsCodeGenerator.generateCode("IQC", "inspection_code", this.baseMapper);
    }

    @Override
    public boolean isCodeUnique(String code, String excludeId) {
        QueryWrapper<IqcInspection> qw = new QueryWrapper<>();
        qw.eq("inspection_code", code);
        if (excludeId != null) qw.ne("id", excludeId);
        return count(qw) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithResults(IqcInspection inspection, List<IqcInspectionResult> results) {
        this.save(inspection);
        saveResults(inspection.getId(), results);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithResults(IqcInspection inspection, List<IqcInspectionResult> results) {
        this.updateById(inspection);
        // Replace results
        QueryWrapper<IqcInspectionResult> delQw = new QueryWrapper<>();
        delQw.eq("inspection_id", inspection.getId());
        resultMapper.delete(delQw);
        saveResults(inspection.getId(), results);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitForApproval(String id) {
        IqcInspection inspection = this.getById(id);
        if (inspection == null) return "Không tìm thấy phiếu IQC";
        if (!"in_progress".equals(inspection.getStatus())) {
            return "Chỉ phiếu đang kiểm tra (in_progress) mới được nộp phê duyệt";
        }
        inspection.setStatus("pending_approval");
        this.updateById(inspection);

        // Send approval request notification (failure must not roll back main operation)
        try {
            String title = "Phiếu IQC " + inspection.getInspectionCode() + " cần phê duyệt";
            notificationService.sendApprovalRequest("iqc", id, title, null);
        } catch (Exception e) {
            log.warn("Failed to send approval notification: {}", e.getMessage());
        }

        return "Nộp phiếu IQC chờ phê duyệt thành công";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approveInspection(String id, String status, String notes, String operator) {
        IqcInspection inspection = this.getById(id);
        if (inspection == null) return "Không tìm thấy phiếu IQC";
        if (!"pending_approval".equals(inspection.getStatus())) {
            return "Chỉ phiếu đang chờ phê duyệt (pending_approval) mới được duyệt";
        }
        inspection.setStatus(status);
        if (notes != null && !notes.isEmpty()) {
            inspection.setNotes(notes);
        }
        inspection.setUpdateBy(operator);
        this.updateById(inspection);

        // Update stock transaction qc_status based on IQC result (Requirements 8.1, 8.2, 8.3)
        // This must be atomic with the status update — if it fails, the whole transaction rolls back
        stockBlockingService.handleIqcApproval(id, status);

        // Send approval result notification (failure must not roll back main operation)
        try {
            notificationService.sendApprovalResult("iqc", id, status, inspection.getCreateBy());
        } catch (Exception e) {
            log.warn("Failed to send approval result notification: {}", e.getMessage());
        }

        return "Duyệt phiếu IQC thành công: " + status;
    }

    @Override
    public List<IqcInspectionResult> getResults(String inspectionId) {
        QueryWrapper<IqcInspectionResult> qw = new QueryWrapper<>();
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
        stats.put("draftCount", count(new QueryWrapper<IqcInspection>().eq("status", "draft")));
        stats.put("inProgressCount", count(new QueryWrapper<IqcInspection>().eq("status", "in_progress")));
        stats.put("pendingApprovalCount", count(new QueryWrapper<IqcInspection>().eq("status", "pending_approval")));
        stats.put("passedCount", count(new QueryWrapper<IqcInspection>().eq("status", "passed")));
        stats.put("failedCount", count(new QueryWrapper<IqcInspection>().eq("status", "failed")));
        stats.put("conditionalCount", count(new QueryWrapper<IqcInspection>().eq("status", "conditional")));
        return stats;
    }

    private void saveResults(String inspectionId, List<IqcInspectionResult> results) {
        if (results == null || results.isEmpty()) return;
        for (IqcInspectionResult r : results) {
            r.setId(UUID.randomUUID().toString());
            r.setInspectionId(inspectionId);
            resultMapper.insert(r);
        }
    }
}
