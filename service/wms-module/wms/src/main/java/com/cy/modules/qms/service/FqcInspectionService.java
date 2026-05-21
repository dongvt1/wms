package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.qms.entity.FqcInspection;
import com.cy.modules.qms.entity.FqcInspectionResult;

import java.util.List;
import java.util.Map;

/**
 * @Description: FQC Inspection Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface FqcInspectionService extends IService<FqcInspection> {

    /** Sinh mã phiếu FQC theo định dạng FQCyyyyMMddNNN */
    String generateInspectionCode();

    /** Lưu phiếu FQC kèm danh sách kết quả kiểm tra */
    void saveWithResults(FqcInspection inspection, List<FqcInspectionResult> results);

    /** Cập nhật phiếu FQC kèm thay thế danh sách kết quả */
    void updateWithResults(FqcInspection inspection, List<FqcInspectionResult> results);

    /** Nộp phiếu FQC chờ duyệt: in_progress → pending_approval */
    String submitForApproval(String id);

    /** Duyệt phiếu FQC: pending_approval → passed / failed */
    String approveInspection(String id, String status, String notes, String operator);

    /** Lấy danh sách kết quả kiểm tra theo phiếu */
    List<FqcInspectionResult> getResults(String inspectionId);

    /** Lấy chi tiết phiếu FQC kèm kết quả */
    Map<String, Object> getDetail(String inspectionId);

    /** Lấy thống kê tổng hợp phiếu FQC theo trạng thái */
    Map<String, Object> getStatistics();

    /** Kiểm tra đơn hàng xuất có được phép xuất kho không (FQC phải passed) */
    boolean isOutboundAllowed(String orderId);
}
