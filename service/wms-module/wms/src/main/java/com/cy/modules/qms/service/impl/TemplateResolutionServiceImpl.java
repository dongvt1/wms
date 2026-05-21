package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.common.entity.Product;
import com.cy.modules.common.mapper.ProductMapper;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.TemplateAssignment;
import com.cy.modules.qms.exception.TemplateNotFoundException;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.mapper.TemplateAssignmentMapper;
import com.cy.modules.qms.service.TemplateResolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Implementation TemplateResolutionService.
 * Tìm template phù hợp theo thứ tự ưu tiên:
 * 1. product-specific (assignment_type = "product", target_id = productId)
 * 2. product-group (assignment_type = "product_group", target_id = product's categoryId)
 * 3. default (assignment_type = "default")
 *
 * Chỉ xét active assignments (is_active = 1) liên kết với active templates (status = "active").
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Service
public class TemplateResolutionServiceImpl implements TemplateResolutionService {

    @Autowired
    private TemplateAssignmentMapper templateAssignmentMapper;

    @Autowired
    private InspectionTemplateMapper inspectionTemplateMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public InspectionTemplate resolveTemplate(String productId, String stageType) {
        // Lookup product's categoryId as productGroupId
        String productGroupId = null;
        if (StringUtils.hasText(productId)) {
            Product product = productMapper.selectById(productId);
            if (product != null && StringUtils.hasText(product.getCategoryId())) {
                productGroupId = product.getCategoryId();
            }
        }
        return resolveTemplate(productId, productGroupId, stageType);
    }

    @Override
    public InspectionTemplate resolveTemplate(String productId, String productGroupId, String stageType) {
        // Priority 1: product-specific assignment
        if (StringUtils.hasText(productId)) {
            InspectionTemplate template = findActiveTemplate("product", productId, stageType);
            if (template != null) {
                return template;
            }
        }

        // Priority 2: product-group assignment
        if (StringUtils.hasText(productGroupId)) {
            InspectionTemplate template = findActiveTemplate("product_group", productGroupId, stageType);
            if (template != null) {
                return template;
            }
        }

        // Priority 3: default assignment
        InspectionTemplate template = findActiveTemplate("default", null, stageType);
        if (template != null) {
            return template;
        }

        // No template found at any level
        throw new TemplateNotFoundException(productId, stageType);
    }

    /**
     * Tìm active template theo assignment type, target, và stage type.
     * Chỉ trả về template nếu cả assignment lẫn template đều active.
     *
     * @param assignmentType loại gán: product, product_group, default
     * @param targetId       ID target (null cho default)
     * @param stageType      loại giai đoạn QC
     * @return InspectionTemplate nếu tìm thấy, null nếu không
     */
    private InspectionTemplate findActiveTemplate(String assignmentType, String targetId, String stageType) {
        // Query active assignments for the given type and target
        QueryWrapper<TemplateAssignment> qw = new QueryWrapper<>();
        qw.eq("assignment_type", assignmentType)
                .eq("is_active", 1);

        if (targetId != null) {
            qw.eq("target_id", targetId);
        } else {
            qw.isNull("target_id");
        }

        List<TemplateAssignment> assignments = templateAssignmentMapper.selectList(qw);

        // Check each assignment's linked template
        for (TemplateAssignment assignment : assignments) {
            InspectionTemplate template = inspectionTemplateMapper.selectById(assignment.getTemplateId());
            if (template != null
                    && "active".equals(template.getStatus())
                    && stageType.equals(template.getStageType())) {
                return template;
            }
        }

        return null;
    }
}
