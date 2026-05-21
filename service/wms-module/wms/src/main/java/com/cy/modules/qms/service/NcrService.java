package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.qms.entity.Ncr;

import java.util.List;
import java.util.Map;

/**
 * @Description: NCR (Non-Conformance Report) Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface NcrService extends IService<Ncr> {

    /**
     * Sinh mã NCR tự động theo định dạng NCRyyyyMMddNNN
     */
    String generateNcrCode();

    /**
     * Tạo NCR từ phiếu kiểm tra.
     * Nếu sourceType là "iqc", tự động lấy supplierId từ phiếu IQC liên kết.
     *
     * @param ncr          NCR entity với thông tin cơ bản đã điền
     * @param inspectionId ID phiếu kiểm tra nguồn
     * @param sourceType   Loại nguồn: iqc/pqc/fqc/other
     * @return NCR đã được lưu với mã tự sinh và supplier liên kết (nếu IQC)
     */
    Ncr createFromInspection(Ncr ncr, String inspectionId, String sourceType);

    /**
     * Chuyển trạng thái NCR theo state machine.
     * Valid transitions: open→investigating, investigating→action_taken, action_taken→verified, verified→closed
     *
     * @param id           ID của NCR
     * @param targetStatus Trạng thái đích
     * @param notes        Ghi chú chuyển trạng thái
     * @param operator     Người thực hiện
     * @return Thông báo kết quả
     */
    String transition(String id, String targetStatus, String notes, String operator);

    /**
     * Đóng NCR - yêu cầu xác nhận hành động khắc phục đã hoàn tất.
     * NCR phải ở trạng thái 'verified' mới được đóng.
     *
     * @param id                 ID của NCR
     * @param confirmationNotes  Ghi chú xác nhận hành động khắc phục
     * @param operator           Người thực hiện
     * @return Thông báo kết quả
     */
    String close(String id, String confirmationNotes, String operator);

    /**
     * Lấy lịch sử NCR theo nhà cung cấp để theo dõi chất lượng.
     *
     * @param supplierId ID nhà cung cấp
     * @return Danh sách NCR liên kết với nhà cung cấp
     */
    List<Ncr> getSupplierHistory(String supplierId);

    /**
     * Lấy thống kê NCR: số lượng theo trạng thái và mức độ nghiêm trọng.
     *
     * @return Map chứa thống kê
     */
    Map<String, Object> getStatistics();
}
