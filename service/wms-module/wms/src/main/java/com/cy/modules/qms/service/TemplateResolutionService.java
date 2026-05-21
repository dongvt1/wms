package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.exception.TemplateNotFoundException;

/**
 * Service tìm template phù hợp theo thứ tự ưu tiên:
 * 1. Template gán trực tiếp cho sản phẩm cụ thể (assignment_type = "product")
 * 2. Template gán cho nhóm sản phẩm (assignment_type = "product_group")
 * 3. Template mặc định (assignment_type = "default")
 *
 * Chỉ xét các assignment đang active (is_active = 1) liên kết với template active (status = "active").
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public interface TemplateResolutionService {

    /**
     * Tìm template phù hợp nhất cho sản phẩm và loại giai đoạn QC.
     *
     * Logic ưu tiên:
     * 1. product-specific: assignment_type = "product" AND target_id = productId
     * 2. product-group: assignment_type = "product_group" AND target_id = productGroupId
     * 3. default: assignment_type = "default"
     *
     * @param productId   ID sản phẩm cần tìm template
     * @param stageType   Loại giai đoạn QC (iqc, pqc, fqc)
     * @return InspectionTemplate phù hợp nhất
     * @throws TemplateNotFoundException nếu không tìm được template nào phù hợp (NO_TEMPLATE_FOUND)
     */
    InspectionTemplate resolveTemplate(String productId, String stageType);

    /**
     * Tìm template phù hợp nhất cho sản phẩm và loại giai đoạn QC,
     * với productGroupId được cung cấp sẵn (tránh phải lookup product entity).
     *
     * @param productId      ID sản phẩm
     * @param productGroupId ID nhóm sản phẩm (có thể null nếu sản phẩm không thuộc nhóm nào)
     * @param stageType      Loại giai đoạn QC (iqc, pqc, fqc)
     * @return InspectionTemplate phù hợp nhất
     * @throws TemplateNotFoundException nếu không tìm được template nào phù hợp (NO_TEMPLATE_FOUND)
     */
    InspectionTemplate resolveTemplate(String productId, String productGroupId, String stageType);
}
