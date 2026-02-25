package org.jeecg.modules.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.warehouse.entity.IqcInspection;
import org.jeecg.modules.warehouse.entity.IqcInspectionResult;

import java.util.List;
import java.util.Map;

/**
 * @Description: IQC Inspection Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface IqcInspectionService extends IService<IqcInspection> {

    String generateInspectionCode();

    boolean isCodeUnique(String code, String excludeId);

    void saveWithResults(IqcInspection inspection, List<IqcInspectionResult> results);

    void updateWithResults(IqcInspection inspection, List<IqcInspectionResult> results);

    /** Duyệt phiếu IQC: status passed / failed / conditional */
    String approveInspection(String id, String status, String notes, String operator);

    List<IqcInspectionResult> getResults(String inspectionId);

    Map<String, Object> getDetail(String inspectionId);

    Map<String, Object> getStatistics();
}
