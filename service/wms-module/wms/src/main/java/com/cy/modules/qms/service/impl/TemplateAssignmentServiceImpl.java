package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.TemplateAssignment;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.mapper.TemplateAssignmentMapper;
import com.cy.modules.qms.service.TemplateAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Implementation TemplateAssignmentService.
 * Quản lý gán/gỡ template cho sản phẩm/nhóm SP với validation uniqueness.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Service
public class TemplateAssignmentServiceImpl extends ServiceImpl<TemplateAssignmentMapper, TemplateAssignment>
        implements TemplateAssignmentService {

    @Autowired
    private InspectionTemplateMapper inspectionTemplateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateAssignment createAssignment(String templateId, String assignmentType, String targetId) {
        // 1. Validate assignmentType
        if (!isValidAssignmentType(assignmentType)) {
            throw new IllegalArgumentException(
                    "Loại gán không hợp lệ: " + assignmentType + ". Chỉ chấp nhận: product, product_group, default");
        }

        // 2. Validate targetId: required for product/product_group, null for default
        if ("default".equals(assignmentType)) {
            targetId = null;
        } else if (!StringUtils.hasText(targetId)) {
            throw new IllegalArgumentException(
                    "targetId là bắt buộc khi assignmentType là " + assignmentType);
        }

        // 3. Validate template exists and is active
        InspectionTemplate template = inspectionTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template không tồn tại: " + templateId);
        }
        if (!"active".equals(template.getStatus())) {
            throw new IllegalArgumentException(
                    "Chỉ có thể gán template đang active. Template hiện tại có trạng thái: " + template.getStatus());
        }

        // 4. Check duplicate: only 1 active assignment per target + stage type
        // Find if there's already an active assignment for the same target + same stage type
        QueryWrapper<TemplateAssignment> duplicateQuery = new QueryWrapper<>();
        duplicateQuery.eq("assignment_type", assignmentType)
                .eq("is_active", 1);

        if (targetId != null) {
            duplicateQuery.eq("target_id", targetId);
        } else {
            duplicateQuery.isNull("target_id");
        }

        // Join with template to check same stage_type
        List<TemplateAssignment> existingAssignments = this.list(duplicateQuery);
        for (TemplateAssignment existing : existingAssignments) {
            InspectionTemplate existingTemplate = inspectionTemplateMapper.selectById(existing.getTemplateId());
            if (existingTemplate != null
                    && existingTemplate.getStageType().equals(template.getStageType())) {
                throw new IllegalStateException(
                        "Đã tồn tại active assignment cho cùng target và stage type ("
                                + template.getStageType() + "). "
                                + "Vui lòng gỡ assignment cũ trước khi gán template mới.");
            }
        }

        // 5. Check unique constraint: same template + assignment_type + target_id
        QueryWrapper<TemplateAssignment> uniqueQuery = new QueryWrapper<>();
        uniqueQuery.eq("template_id", templateId)
                .eq("assignment_type", assignmentType);
        if (targetId != null) {
            uniqueQuery.eq("target_id", targetId);
        } else {
            uniqueQuery.isNull("target_id");
        }

        long existingCount = this.count(uniqueQuery);
        if (existingCount > 0) {
            throw new IllegalStateException(
                    "Assignment đã tồn tại cho template này với cùng loại gán và target.");
        }

        // 6. Create assignment
        TemplateAssignment assignment = new TemplateAssignment();
        assignment.setTemplateId(templateId);
        assignment.setAssignmentType(assignmentType);
        assignment.setTargetId(targetId);
        assignment.setIsActive(1);
        assignment.setCreateTime(new Date());
        assignment.setSysOrgCode(template.getSysOrgCode());

        this.save(assignment);
        return assignment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAssignment(String id) {
        TemplateAssignment assignment = this.getById(id);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment không tồn tại: " + id);
        }
        this.removeById(id);
    }

    @Override
    public List<TemplateAssignment> listAssignmentsByTemplate(String templateId) {
        QueryWrapper<TemplateAssignment> qw = new QueryWrapper<>();
        qw.eq("template_id", templateId)
                .orderByDesc("create_time");
        return this.list(qw);
    }

    @Override
    public List<TemplateAssignment> listAssignmentsByTarget(String assignmentType, String targetId) {
        QueryWrapper<TemplateAssignment> qw = new QueryWrapper<>();
        qw.eq("assignment_type", assignmentType);

        if (targetId != null) {
            qw.eq("target_id", targetId);
        } else {
            qw.isNull("target_id");
        }

        qw.orderByDesc("create_time");
        return this.list(qw);
    }

    /**
     * Validate assignment type value.
     */
    private boolean isValidAssignmentType(String assignmentType) {
        return "product".equals(assignmentType)
                || "product_group".equals(assignmentType)
                || "default".equals(assignmentType);
    }
}
