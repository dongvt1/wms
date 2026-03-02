package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import com.cy.modules.qms.entity.IqcInspection;
import com.cy.modules.qms.entity.IqcInspectionResult;
import com.cy.modules.qms.mapper.IqcInspectionMapper;
import com.cy.modules.qms.mapper.IqcInspectionResultMapper;
import com.cy.modules.qms.service.IqcInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
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

    @Override
    public String generateInspectionCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        QueryWrapper<IqcInspection> qw = new QueryWrapper<>();
        qw.likeRight("inspection_code", "IQC" + dateStr).orderByDesc("inspection_code").last("LIMIT 1");
        IqcInspection last = this.getOne(qw);
        int seq = 1;
        if (last != null) {
            try {
                String code = last.getInspectionCode();
                seq = Integer.parseInt(code.substring(code.length() - 3)) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return "IQC" + dateStr + String.format("%03d", seq);
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
