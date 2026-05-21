package com.cy.modules.qms.controller;

import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.TemplateAssignment;
import com.cy.modules.qms.exception.TemplateNotFoundException;
import com.cy.modules.qms.service.TemplateAssignmentService;
import com.cy.modules.qms.service.TemplateResolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Description: Quản lý gán template cho sản phẩm/nhóm sản phẩm
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "QMS - Gán Template cho Sản phẩm")
@RestController
@RequestMapping("/api/qms/template-assignment")
public class TemplateAssignmentController {

    @Autowired
    private TemplateAssignmentService templateAssignmentService;

    @Autowired
    private TemplateResolutionService templateResolutionService;

    /**
     * Danh sách assignments.
     * Hỗ trợ filter theo templateId hoặc theo assignmentType + targetId.
     */
    @GetMapping("/list")
    @Operation(summary = "Danh sách assignments theo template hoặc target")
    public Result<?> list(@RequestParam(required = false) String templateId,
                          @RequestParam(required = false) String assignmentType,
                          @RequestParam(required = false) String targetId) {
        List<TemplateAssignment> assignments;
        if (templateId != null) {
            assignments = templateAssignmentService.listAssignmentsByTemplate(templateId);
        } else if (assignmentType != null) {
            assignments = templateAssignmentService.listAssignmentsByTarget(assignmentType, targetId);
        } else {
            assignments = templateAssignmentService.list();
        }
        return Result.OK(assignments);
    }

    /**
     * Gán template cho sản phẩm/nhóm SP/default.
     */
    @PostMapping("")
    @AutoLog(value = "Gán template cho sản phẩm")
    @Operation(summary = "Gán template cho sản phẩm/nhóm SP/default")
    public Result<?> create(@RequestBody Map<String, String> body) {
        String templateId = body.get("templateId");
        String assignmentType = body.get("assignmentType");
        String targetId = body.get("targetId");

        if (templateId == null || templateId.isEmpty()) {
            return Result.error("templateId là bắt buộc");
        }
        if (assignmentType == null || assignmentType.isEmpty()) {
            return Result.error("assignmentType là bắt buộc");
        }

        try {
            TemplateAssignment assignment = templateAssignmentService.createAssignment(
                    templateId, assignmentType, targetId);
            return Result.OK("Gán template thành công!", assignment);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Gỡ assignment.
     */
    @DeleteMapping("/{id}")
    @AutoLog(value = "Gỡ assignment template")
    @Operation(summary = "Gỡ assignment (xóa liên kết template - sản phẩm)")
    public Result<?> delete(@PathVariable String id) {
        try {
            templateAssignmentService.deleteAssignment(id);
            return Result.OK("Gỡ assignment thành công!");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Tìm template phù hợp cho sản phẩm + stage type.
     * Áp dụng thứ tự ưu tiên: product-specific → product-group → default.
     */
    @GetMapping("/resolve")
    @Operation(summary = "Tìm template phù hợp cho sản phẩm và stage type")
    public Result<?> resolve(@RequestParam String productId,
                             @RequestParam String stageType,
                             @RequestParam(required = false) String productGroupId) {
        try {
            InspectionTemplate template;
            if (productGroupId != null) {
                template = templateResolutionService.resolveTemplate(productId, productGroupId, stageType);
            } else {
                template = templateResolutionService.resolveTemplate(productId, stageType);
            }
            return Result.OK(template);
        } catch (TemplateNotFoundException e) {
            return Result.error(e.getMessage());
        }
    }
}
