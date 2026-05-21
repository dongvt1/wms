package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.qms.entity.TemplateAssignment;

import java.util.List;

/**
 * Service quản lý gán template cho sản phẩm/nhóm sản phẩm.
 * Đảm bảo chỉ 1 active template per product + stage type.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public interface TemplateAssignmentService extends IService<TemplateAssignment> {

    /**
     * Tạo assignment mới: gán template cho sản phẩm/nhóm SP/default.
     * Validate: chỉ cho phép 1 active template per product (hoặc group) + stage type.
     * Nếu đã tồn tại active assignment cho cùng target + stage type → throw exception.
     *
     * @param templateId     ID template cần gán
     * @param assignmentType Loại gán: "product", "product_group", "default"
     * @param targetId       ID sản phẩm hoặc nhóm SP (null nếu default)
     * @return TemplateAssignment đã tạo
     * @throws IllegalArgumentException nếu template không tồn tại hoặc không active
     * @throws IllegalStateException    nếu đã có active assignment cho cùng target + stage type
     */
    TemplateAssignment createAssignment(String templateId, String assignmentType, String targetId);

    /**
     * Xóa (gỡ) assignment.
     *
     * @param id ID assignment cần xóa
     * @throws IllegalArgumentException nếu assignment không tồn tại
     */
    void deleteAssignment(String id);

    /**
     * Danh sách assignments theo template.
     *
     * @param templateId ID template
     * @return Danh sách assignments của template đó
     */
    List<TemplateAssignment> listAssignmentsByTemplate(String templateId);

    /**
     * Danh sách assignments theo target (sản phẩm/nhóm SP).
     *
     * @param assignmentType Loại gán: "product", "product_group", "default"
     * @param targetId       ID sản phẩm hoặc nhóm SP (null nếu default)
     * @return Danh sách assignments cho target đó
     */
    List<TemplateAssignment> listAssignmentsByTarget(String assignmentType, String targetId);
}
