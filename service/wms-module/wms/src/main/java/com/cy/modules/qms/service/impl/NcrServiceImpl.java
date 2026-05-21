package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.qms.entity.IqcInspection;
import com.cy.modules.qms.entity.Ncr;
import com.cy.modules.qms.mapper.IqcInspectionMapper;
import com.cy.modules.qms.mapper.NcrMapper;
import com.cy.modules.qms.service.NcrService;
import com.cy.modules.qms.util.QmsCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description: NCR (Non-Conformance Report) Service Implementation
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
@Slf4j
public class NcrServiceImpl extends ServiceImpl<NcrMapper, Ncr> implements NcrService {

    /**
     * NCR state machine: defines valid transitions.
     * open → investigating → action_taken → verified → closed
     */
    private static final Map<String, String> VALID_TRANSITIONS = new LinkedHashMap<>();

    static {
        VALID_TRANSITIONS.put("open", "investigating");
        VALID_TRANSITIONS.put("investigating", "action_taken");
        VALID_TRANSITIONS.put("action_taken", "verified");
        VALID_TRANSITIONS.put("verified", "closed");
    }

    @Autowired
    private IqcInspectionMapper iqcInspectionMapper;

    @Override
    public String generateNcrCode() {
        return QmsCodeGenerator.generateCode("NCR", "ncr_code", this.baseMapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Ncr createFromInspection(Ncr ncr, String inspectionId, String sourceType) {
        // Set source info
        ncr.setSourceType(sourceType);
        ncr.setSourceId(inspectionId);

        // Auto-generate NCR code
        ncr.setNcrCode(generateNcrCode());

        // Set initial status
        if (ncr.getStatus() == null) {
            ncr.setStatus("open");
        }

        // If source is IQC, auto-link supplier from the IQC inspection
        if ("iqc".equalsIgnoreCase(sourceType) && inspectionId != null) {
            IqcInspection iqcInspection = iqcInspectionMapper.selectById(inspectionId);
            if (iqcInspection != null && iqcInspection.getSupplierId() != null) {
                ncr.setSupplierId(iqcInspection.getSupplierId());
            }
        }

        this.save(ncr);
        return ncr;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String transition(String id, String targetStatus, String notes, String operator) {
        Ncr ncr = this.getById(id);
        if (ncr == null) {
            return "Không tìm thấy NCR";
        }

        String currentStatus = ncr.getStatus();

        // Validate the transition is allowed
        String allowedTarget = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTarget == null || !allowedTarget.equals(targetStatus)) {
            return "Chỉ phiếu ở trạng thái " + currentStatus + " mới được chuyển sang " + allowedTarget;
        }

        ncr.setStatus(targetStatus);
        if (notes != null && !notes.isEmpty()) {
            ncr.setNotes(notes);
        }
        ncr.setUpdateBy(operator);
        this.updateById(ncr);

        return "Chuyển trạng thái NCR thành công: " + targetStatus;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String close(String id, String confirmationNotes, String operator) {
        Ncr ncr = this.getById(id);
        if (ncr == null) {
            return "Không tìm thấy NCR";
        }

        // NCR must be in 'verified' status to close
        if (!"verified".equals(ncr.getStatus())) {
            return "Chỉ phiếu ở trạng thái verified mới được đóng";
        }

        // Require corrective action confirmation
        if (confirmationNotes == null || confirmationNotes.trim().isEmpty()) {
            return "Vui lòng xác nhận hành động khắc phục đã hoàn tất";
        }

        ncr.setStatus("closed");
        ncr.setCorrectiveAction(confirmationNotes);
        ncr.setClosedBy(operator);
        ncr.setClosedDate(new Date());
        ncr.setUpdateBy(operator);
        this.updateById(ncr);

        return "Đóng NCR thành công";
    }

    @Override
    public List<Ncr> getSupplierHistory(String supplierId) {
        QueryWrapper<Ncr> qw = new QueryWrapper<>();
        qw.eq("supplier_id", supplierId);
        qw.orderByDesc("create_time");
        return this.list(qw);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Count by status
        stats.put("totalCount", count());
        stats.put("openCount", count(new QueryWrapper<Ncr>().eq("status", "open")));
        stats.put("investigatingCount", count(new QueryWrapper<Ncr>().eq("status", "investigating")));
        stats.put("actionTakenCount", count(new QueryWrapper<Ncr>().eq("status", "action_taken")));
        stats.put("verifiedCount", count(new QueryWrapper<Ncr>().eq("status", "verified")));
        stats.put("closedCount", count(new QueryWrapper<Ncr>().eq("status", "closed")));

        // Count by severity
        stats.put("criticalCount", count(new QueryWrapper<Ncr>().eq("severity", "critical")));
        stats.put("majorCount", count(new QueryWrapper<Ncr>().eq("severity", "major")));
        stats.put("minorCount", count(new QueryWrapper<Ncr>().eq("severity", "minor")));

        return stats;
    }
}
