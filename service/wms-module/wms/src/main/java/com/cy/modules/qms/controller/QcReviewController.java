package com.cy.modules.qms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import com.cy.modules.qms.entity.QcReview;
import com.cy.modules.qms.service.QcReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "QMS - Review & Approval")
@RestController
@RequestMapping("/qms/review")
public class QcReviewController extends JeecgController<QcReview, QcReviewService> {

    @Autowired
    private QcReviewService reviewService;

    @GetMapping("/list")
    @Operation(summary = "List of reviews")
    public Result<?> list(QcReview review,
                          @RequestParam(defaultValue = "1") Integer pageNo,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<QcReview> qw = QueryGenerator.initQueryWrapper(review, req.getParameterMap());
        qw.orderByDesc("create_time");
        IPage<QcReview> page = reviewService.page(new Page<>(pageNo, pageSize), qw);
        return Result.OK(page);
    }

    @GetMapping("/byWorkOrder")
    @Operation(summary = "Get or create review for WO (aggregate sessions)")
    public Result<?> byWorkOrder(@RequestParam String workOrderId) {
        return Result.OK(reviewService.getOrCreateByWorkOrder(workOrderId));
    }

    @GetMapping("/queryById")
    @Operation(summary = "Review details with list of sessions")
    public Result<?> queryById(@RequestParam String id) {
        return Result.OK(reviewService.getDetail(id));
    }

    @PutMapping("/submit/{id}")
    @AutoLog(value = "Submit review for approval", operateType = 3)
    @Operation(summary = "Submit review for approval (draft → pending_approval)")
    public Result<?> submit(@PathVariable String id,
                            @RequestParam(required = false) String reviewer) {
        return Result.OK(reviewService.submit(id, reviewer));
    }

    @PutMapping("/approve/{id}")
    @AutoLog(value = "Approve review", operateType = 3)
    @Operation(summary = "Approve review (pending_approval → approved)")
    public Result<?> approve(@PathVariable String id,
                             @RequestParam(required = false) String approver,
                             @RequestParam(required = false) String overallResult,
                             @RequestParam(required = false) String notes) {
        return Result.OK(reviewService.approve(id, approver, overallResult, notes));
    }

    @PutMapping("/reject/{id}")
    @AutoLog(value = "Reject review", operateType = 3)
    @Operation(summary = "Reject review (pending_approval → rejected)")
    public Result<?> reject(@PathVariable String id,
                            @RequestParam(required = false) String approver,
                            @RequestParam(required = false) String reason) {
        return Result.OK(reviewService.reject(id, approver, reason));
    }

    @PutMapping("/syncStats/{id}")
    @Operation(summary = "Synchronize session statistics into review")
    public Result<?> syncStats(@PathVariable String id) {
        reviewService.syncStats(id);
        return Result.OK("Statistics synchronized successfully!");
    }

    @RequestMapping(value = "/export")
    public org.springframework.web.servlet.ModelAndView exportXls(jakarta.servlet.http.HttpServletRequest request, QcReview review) {
        return super.exportXls(request, review, QcReview.class, "QC Review Report");
    }
}
