package com.cy.modules.qms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.qms.dto.InspectionExecutionDTO;
import com.cy.modules.qms.dto.StepValuesRequest;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.exception.TemplateNotFoundException;
import com.cy.modules.qms.service.InspectionExecutionService;
import com.cy.modules.qms.vo.InspectionExecutionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: Inspection Execution Controller - Quản lý phiên kiểm tra chất lượng
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "QMS - Inspection Execution")
@RestController
@RequestMapping("/api/qms/inspection-execution")
public class InspectionExecutionController {

    @Autowired
    private InspectionExecutionService inspectionExecutionService;

    /**
     * Danh sách phiên kiểm tra phân trang + filter
     */
    @Operation(summary = "Danh sách phiên kiểm tra (phân trang + filter)")
    @GetMapping("/list")
    public Result<IPage<InspectionExecutionVO>> list(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "productId", required = false) String productId,
            @RequestParam(name = "stageType", required = false) String stageType) {
        Page<InspectionExecution> page = new Page<>(pageNo, pageSize);
        IPage<InspectionExecutionVO> result = inspectionExecutionService.listExecutions(page, status, productId, stageType);
        return Result.OK(result);
    }

    /**
     * Chi tiết phiên kiểm tra kèm step results và field values
     */
    @Operation(summary = "Chi tiết phiên kiểm tra (kèm results)")
    @GetMapping("/{id}")
    public Result<InspectionExecutionVO> getDetail(@PathVariable("id") String id) {
        InspectionExecutionVO vo = inspectionExecutionService.getExecutionDetail(id);
        if (vo == null) {
            return Result.error("Không tìm thấy phiên kiểm tra với ID: " + id);
        }
        return Result.OK(vo);
    }

    /**
     * Tạo phiên kiểm tra mới
     * - Tự động tìm template phù hợp theo productId + stageType
     * - Tạo snapshot template tại thời điểm tạo
     */
    @PostMapping("")
    @AutoLog(value = "Tạo phiên kiểm tra")
    @Operation(summary = "Tạo phiên kiểm tra mới")
    public Result<InspectionExecutionVO> create(@Valid @RequestBody InspectionExecutionDTO dto) {
        try {
            InspectionExecutionVO vo = inspectionExecutionService.createExecution(dto);
            return Result.OK("Tạo phiên kiểm tra thành công!", vo);
        } catch (TemplateNotFoundException e) {
            log.warn("Không tìm được template phù hợp: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * Lưu nháp giá trị field cho một step (không đánh giá)
     */
    @PutMapping("/{id}/save-draft")
    @AutoLog(value = "Lưu nháp phiên kiểm tra")
    @Operation(summary = "Lưu nháp giá trị cho một step")
    public Result<?> saveDraft(@PathVariable("id") String id,
                               @RequestParam("stepId") String stepId,
                               @Valid @RequestBody StepValuesRequest request) {
        try {
            inspectionExecutionService.saveDraft(id, stepId, request.getValues());
            return Result.OK("Lưu nháp thành công!");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Submit toàn bộ phiên kiểm tra để chuyển sang pending_approval
     */
    @PutMapping("/{id}/submit")
    @AutoLog(value = "Submit phiên kiểm tra")
    @Operation(summary = "Submit phiên kiểm tra")
    public Result<?> submit(@PathVariable("id") String id) {
        try {
            inspectionExecutionService.submitExecution(id);
            return Result.OK("Submit phiên kiểm tra thành công!");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Lưu giá trị field cho một step cụ thể (submit + evaluate)
     */
    @PutMapping("/{id}/step/{stepId}/values")
    @AutoLog(value = "Submit giá trị bước kiểm tra")
    @Operation(summary = "Lưu giá trị cho một bước kiểm tra (submit + evaluate)")
    public Result<?> submitStepValues(@PathVariable("id") String id,
                                      @PathVariable("stepId") String stepId,
                                      @Valid @RequestBody StepValuesRequest request) {
        try {
            inspectionExecutionService.submitStepValues(id, stepId, request.getValues());
            return Result.OK("Lưu giá trị bước kiểm tra thành công!");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }
}
