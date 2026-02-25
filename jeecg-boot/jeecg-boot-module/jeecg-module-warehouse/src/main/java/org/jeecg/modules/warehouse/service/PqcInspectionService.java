package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.PqcInspection;
import org.jeecg.modules.warehouse.entity.PqcInspectionResult;

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

    /** Duyệt phiếu PQC: status passed / failed */
    String approveInspection(String id, String status, String notes, String operator);

    List<PqcInspectionResult> getResults(String inspectionId);

    Map<String, Object> getDetail(String inspectionId);

    Map<String, Object> getStatistics();
}
