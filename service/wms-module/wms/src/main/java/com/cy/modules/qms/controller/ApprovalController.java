package com.cy.modules.qms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.qms.dto.ApprovalDTO;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: Quản lý quy trình phê duyệt kết quả kiểm tra
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "QMS - Phê duyệt kết quả kiểm tra")
@RestController
@RequestMapping("/api/qms/approval")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private InspectionExecutionMapper inspectionExecutionMapper;

    /**
     * Danh sách phiên kiểm tra chờ phê duyệt (status = "pending_approval").
     */
    @GetMapping("/pending")
    @Operation(summary = "Danh sách phiên kiểm tra chờ phê duyệt")
    public Result<IPage<InspectionExecution>> pending(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<InspectionExecution> page = new Page<>(pageNo, pageSize);
        QueryWrapper<InspectionExecution> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "pending_approval");
        queryWrapper.orderByDesc("create_time");
        IPage<InspectionExecution> result = inspectionExecutionMapper.selectPage(page, queryWrapper);
        return Result.OK(result);
    }

    /**
     * Phê duyệt kết quả kiểm tra.
     */
    @PutMapping("/{executionId}/approve")
    @AutoLog(value = "Phê duyệt kết quả kiểm tra")
    @Operation(summary = "Phê duyệt kết quả kiểm tra")
    public Result<?> approve(@PathVariable("executionId") String executionId,
                             @RequestBody(required = false) ApprovalDTO dto) {
        try {
            String comment = (dto != null) ? dto.getComment() : null;
            approvalService.approve(executionId, comment);
            return Result.OK("Phê duyệt thành công!");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Từ chối kết quả kiểm tra (bắt buộc có lý do).
     */
    @PutMapping("/{executionId}/reject")
    @AutoLog(value = "Từ chối kết quả kiểm tra")
    @Operation(summary = "Từ chối kết quả kiểm tra")
    public Result<?> reject(@PathVariable("executionId") String executionId,
                            @RequestBody ApprovalDTO dto) {
        if (dto == null || dto.getReason() == null || dto.getReason().isBlank()) {
            return Result.error("Lý do từ chối là bắt buộc");
        }
        try {
            approvalService.reject(executionId, dto.getReason());
            return Result.OK("Đã từ chối kết quả kiểm tra!");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * Yêu cầu kiểm tra lại một bước cụ thể (bắt buộc có stepId và lý do).
     */
    @PutMapping("/{executionId}/re-inspect")
    @AutoLog(value = "Yêu cầu kiểm tra lại")
    @Operation(summary = "Yêu cầu kiểm tra lại bước cụ thể")
    public Result<?> reInspect(@PathVariable("executionId") String executionId,
                               @RequestBody ApprovalDTO dto) {
        if (dto == null || dto.getReason() == null || dto.getReason().isBlank()) {
            return Result.error("Lý do yêu cầu kiểm tra lại là bắt buộc");
        }
        if (dto.getStepId() == null || dto.getStepId().isBlank()) {
            return Result.error("stepId là bắt buộc khi yêu cầu kiểm tra lại");
        }
        try {
            approvalService.reInspect(executionId, dto.getStepId(), dto.getReason());
            return Result.OK("Đã yêu cầu kiểm tra lại!");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }
}
