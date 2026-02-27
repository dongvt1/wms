package qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import qms.entity.PqcInspection;
import qms.entity.PqcInspectionResult;
import qms.mapper.PqcInspectionMapper;
import qms.mapper.PqcInspectionResultMapper;
import qms.service.PqcInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description: PQC Inspection Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
@Slf4j
public class PqcInspectionServiceImpl extends ServiceImpl<PqcInspectionMapper, PqcInspection>
        implements PqcInspectionService {

    @Autowired
    private PqcInspectionResultMapper resultMapper;

    @Override
    public String generateInspectionCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        QueryWrapper<PqcInspection> qw = new QueryWrapper<>();
        qw.likeRight("inspection_code", "PQC" + dateStr).orderByDesc("inspection_code").last("LIMIT 1");
        PqcInspection last = this.getOne(qw);
        int seq = 1;
        if (last != null) {
            try {
                String code = last.getInspectionCode();
                seq = Integer.parseInt(code.substring(code.length() - 3)) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return "PQC" + dateStr + String.format("%03d", seq);
    }

    @Override
    public boolean isCodeUnique(String code, String excludeId) {
        QueryWrapper<PqcInspection> qw = new QueryWrapper<>();
        qw.eq("inspection_code", code);
        if (excludeId != null) qw.ne("id", excludeId);
        return count(qw) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithResults(PqcInspection inspection, List<PqcInspectionResult> results) {
        this.save(inspection);
        saveResults(inspection.getId(), results);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithResults(PqcInspection inspection, List<PqcInspectionResult> results) {
        this.updateById(inspection);
        QueryWrapper<PqcInspectionResult> delQw = new QueryWrapper<>();
        delQw.eq("inspection_id", inspection.getId());
        resultMapper.delete(delQw);
        saveResults(inspection.getId(), results);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitForApproval(String id) {
        PqcInspection inspection = this.getById(id);
        if (inspection == null) return "Không tìm thấy phiếu PQC";
        if (!"in_progress".equals(inspection.getStatus())) {
            return "Chỉ phiếu đang kiểm tra (in_progress) mới được nộp phê duyệt";
        }
        inspection.setStatus("pending_approval");
        this.updateById(inspection);
        return "Nộp phiếu PQC chờ phê duyệt thành công";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approveInspection(String id, String status, String notes, String operator) {
        PqcInspection inspection = this.getById(id);
        if (inspection == null) return "Không tìm thấy phiếu PQC";
        if (!"pending_approval".equals(inspection.getStatus())) {
            return "Chỉ phiếu đang chờ phê duyệt (pending_approval) mới được duyệt";
        }
        inspection.setStatus(status);
        if (notes != null && !notes.isEmpty()) {
            inspection.setNotes(notes);
        }
        inspection.setUpdateBy(operator);
        this.updateById(inspection);
        return "Duyệt phiếu PQC thành công: " + status;
    }

    @Override
    public List<PqcInspectionResult> getResults(String inspectionId) {
        QueryWrapper<PqcInspectionResult> qw = new QueryWrapper<>();
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
        stats.put("draftCount", count(new QueryWrapper<PqcInspection>().eq("status", "draft")));
        stats.put("inProgressCount", count(new QueryWrapper<PqcInspection>().eq("status", "in_progress")));
        stats.put("pendingApprovalCount", count(new QueryWrapper<PqcInspection>().eq("status", "pending_approval")));
        stats.put("passedCount", count(new QueryWrapper<PqcInspection>().eq("status", "passed")));
        stats.put("failedCount", count(new QueryWrapper<PqcInspection>().eq("status", "failed")));
        return stats;
    }

    private void saveResults(String inspectionId, List<PqcInspectionResult> results) {
        if (results == null || results.isEmpty()) return;
        for (PqcInspectionResult r : results) {
            r.setId(UUID.randomUUID().toString());
            r.setInspectionId(inspectionId);
            resultMapper.insert(r);
        }
    }
}
