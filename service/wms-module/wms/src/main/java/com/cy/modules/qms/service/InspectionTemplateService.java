package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.qms.dto.InspectionTemplateDTO;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.vo.InspectionTemplateVO;

import java.util.List;

/**
 * Service quản lý Inspection Template (CRUD + phân trang + filter).
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public interface InspectionTemplateService extends IService<InspectionTemplate> {

    /**
     * Tạo mới template kèm steps + fields trong single transaction.
     *
     * @param dto DTO chứa thông tin template, steps, fields
     * @return VO template đã tạo (bao gồm generated code, id)
     */
    InspectionTemplateVO saveTemplateWithSteps(InspectionTemplateDTO dto);

    /**
     * Cập nhật template kèm diff logic cho steps/fields.
     * - Steps/fields có id → update
     * - Steps/fields không có id → insert mới
     * - Steps/fields cũ không có trong DTO → delete
     *
     * @param id  ID template cần cập nhật
     * @param dto DTO chứa thông tin cập nhật
     * @return VO template đã cập nhật
     */
    InspectionTemplateVO updateTemplateWithSteps(String id, InspectionTemplateDTO dto);

    /**
     * Xóa template sau khi kiểm tra referential integrity.
     * Từ chối xóa nếu có InspectionExecution liên quan.
     *
     * @param id ID template cần xóa
     */
    void deleteTemplate(String id);

    /**
     * Lấy chi tiết template kèm steps + fields.
     *
     * @param id ID template
     * @return VO template đầy đủ (steps + fields)
     */
    InspectionTemplateVO getTemplateDetail(String id);

    /**
     * Danh sách template phân trang + filter.
     *
     * @param page      Thông tin phân trang
     * @param stageType Lọc theo loại giai đoạn (iqc/pqc/fqc), null = tất cả
     * @param status    Lọc theo trạng thái (draft/active/obsolete), null = tất cả
     * @param search    Tìm kiếm theo tên hoặc mã template, null = không lọc
     * @return Trang kết quả chứa danh sách InspectionTemplateVO
     */
    IPage<InspectionTemplateVO> listTemplates(Page<InspectionTemplate> page, String stageType, String status, String search);

    /**
     * Sắp xếp lại thứ tự các Inspection Steps trong template.
     * Nhận danh sách step IDs theo thứ tự mới, tự động cập nhật sort_order liên tục từ 1.
     *
     * @param templateId ID template chứa các steps
     * @param stepIds    Danh sách step IDs theo thứ tự mới
     */
    void reorderSteps(String templateId, List<String> stepIds);

    /**
     * Xóa một Inspection Step kèm toàn bộ Step Fields (cascade delete).
     *
     * @param stepId ID step cần xóa
     */
    void deleteStep(String stepId);

    /**
     * Nhân bản (clone) template kèm toàn bộ steps và fields.
     * Template mới có:
     * - Mã template mới (tự sinh)
     * - Tên = tên cũ + " (Copy)"
     * - Cùng stageType
     * - Version tăng lên
     * - Status = "draft"
     * - Toàn bộ steps và fields được deep clone với ID mới
     *
     * @param id ID template nguồn cần nhân bản
     * @return VO template mới đã tạo (bao gồm steps + fields)
     */
    InspectionTemplateVO cloneTemplate(String id);

    /**
     * Kích hoạt template.
     * 1. Load template kèm steps + fields
     * 2. Validate template qua TemplateValidationService - nếu có lỗi, throw exception kèm danh sách lỗi
     * 3. Chuyển template cũ (cùng stage_type trong cùng org) sang trạng thái obsolete
     * 4. Cập nhật status template mới sang active
     * Tất cả trong single @Transactional.
     *
     * @param id ID template cần kích hoạt
     * @throws IllegalArgumentException nếu template không tồn tại
     * @throws com.cy.modules.qms.exception.TemplateValidationException nếu validation thất bại (kèm danh sách lỗi)
     */
    void activateTemplate(String id);
}
