package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.qms.entity.PqcInspection;
import com.cy.modules.qms.entity.PqcInspectionResult;

import java.util.List;
import java.util.Map;

/**
 * @Description: PQC Inspection Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface PqcInspectionService extends IService<PqcInspection> {

    String generateInspectionCode();

    boolean isCodeUnique(String code, String excludeId);

    void saveWithResults(PqcInspection inspection, List<PqcInspectionResult> results);

    void updateWithResults(PqcInspection inspection, List<PqcInspectionResult> results);

    /** Nộp phiếu PQC chờ duyệt: in_progress → pending_approval */
    String submitForApproval(String id);

    /** Duyệt phiếu PQC: pending_approval → passed / failed */
    String approveInspection(String id, String status, String notes, String operator);

    List<PqcInspectionResult> getResults(String inspectionId);

    Map<String, Object> getDetail(String inspectionId);

    Map<String, Object> getStatistics();
}
