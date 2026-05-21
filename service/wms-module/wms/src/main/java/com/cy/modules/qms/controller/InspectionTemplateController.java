package com.cy.modules.qms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.qms.dto.InspectionTemplateDTO;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.exception.TemplateValidationException;
import com.cy.modules.qms.service.InspectionTemplateService;
import com.cy.modules.qms.vo.InspectionTemplateVO;
import com.cy.modules.qms.vo.ValidationErrorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: Inspection Template Controller - CRUD + Activate + Clone + Preview
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "QMS - Inspection Template")
@RestController
@RequestMapping("/api/qms/inspection-template")
public class InspectionTemplateController {

    @Autowired
    private InspectionTemplateService inspectionTemplateService;

    /**
     * Danh sách template phân trang + filter
     */
    @Operation(summary = "Danh sách Inspection Template (phân trang + filter)")
    @GetMapping("/list")
    public Result<IPage<InspectionTemplateVO>> list(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "stageType", required = false) String stageType,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "search", required = false) String search) {
        Page<InspectionTemplate> page = new Page<>(pageNo, pageSize);
        IPage<InspectionTemplateVO> result = inspectionTemplateService.listTemplates(page, stageType, status, search);
        return Result.OK(result);
    }

    /**
     * Chi tiết template kèm steps + fields
     */
    @Operation(summary = "Chi tiết Inspection Template (kèm steps + fields)")
    @GetMapping("/{id}")
    public Result<InspectionTemplateVO> getDetail(@PathVariable("id") String id) {
        InspectionTemplateVO vo = inspectionTemplateService.getTemplateDetail(id);
        if (vo == null) {
            return Result.error("Không tìm thấy template với ID: " + id);
        }
        return Result.OK(vo);
    }

    /**
     * Tạo mới template kèm steps + fields
     */
    @PostMapping("")
    @AutoLog(value = "Tạo Inspection Template")
    @Operation(summary = "Tạo mới Inspection Template")
    public Result<InspectionTemplateVO> add(@Valid @RequestBody InspectionTemplateDTO dto) {
        InspectionTemplateVO vo = inspectionTemplateService.saveTemplateWithSteps(dto);
        return Result.OK("Tạo template thành công!", vo);
    }

    /**
     * Cập nhật template kèm steps + fields
     */
    @PutMapping("/{id}")
    @AutoLog(value = "Cập nhật Inspection Template")
    @Operation(summary = "Cập nhật Inspection Template")
    public Result<InspectionTemplateVO> update(@PathVariable("id") String id,
                                               @Valid @RequestBody InspectionTemplateDTO dto) {
        InspectionTemplateVO vo = inspectionTemplateService.updateTemplateWithSteps(id, dto);
        return Result.OK("Cập nhật template thành công!", vo);
    }

    /**
     * Xóa template (kiểm tra referential integrity)
     */
    @DeleteMapping("/{id}")
    @AutoLog(value = "Xóa Inspection Template")
    @Operation(summary = "Xóa Inspection Template")
    public Result<?> delete(@PathVariable("id") String id) {
        inspectionTemplateService.deleteTemplate(id);
        return Result.OK("Xóa template thành công!");
    }

    /**
     * Kích hoạt template
     * - Validate template trước khi activate
     * - Chuyển template cũ sang obsolete
     * - Trả 422 nếu validation thất bại
     */
    @PutMapping("/{id}/activate")
    @AutoLog(value = "Kích hoạt Inspection Template")
    @Operation(summary = "Kích hoạt Inspection Template")
    public ResponseEntity<Result<?>> activate(@PathVariable("id") String id) {
        try {
            inspectionTemplateService.activateTemplate(id);
            return ResponseEntity.ok(Result.OK("Template activated successfully"));
        } catch (TemplateValidationException e) {
            log.warn("Template activation validation failed for id={}: {}", id, e.getMessage());
            ValidationErrorVO errorVO = new ValidationErrorVO(e.getErrors());
            Result<ValidationErrorVO> errorResult = Result.error("Validation failed");
            errorResult.setResult(errorVO);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResult);
        }
    }

    /**
     * Nhân bản template (deep clone)
     */
    @PostMapping("/{id}/clone")
    @AutoLog(value = "Nhân bản Inspection Template")
    @Operation(summary = "Nhân bản Inspection Template")
    public Result<InspectionTemplateVO> clone(@PathVariable("id") String id) {
        InspectionTemplateVO vo = inspectionTemplateService.cloneTemplate(id);
        return Result.OK("Nhân bản template thành công!", vo);
    }

    /**
     * Preview template data - trả về chi tiết template để frontend render preview form
     */
    @Operation(summary = "Preview Inspection Template")
    @GetMapping("/{id}/preview")
    public Result<InspectionTemplateVO> preview(@PathVariable("id") String id) {
        InspectionTemplateVO vo = inspectionTemplateService.getTemplateDetail(id);
        if (vo == null) {
            return Result.error("Không tìm thấy template với ID: " + id);
        }
        return Result.OK(vo);
    }
}
