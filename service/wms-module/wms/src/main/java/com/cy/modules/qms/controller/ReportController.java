package com.cy.modules.qms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.service.ReportService;
import com.cy.modules.qms.vo.InspectionHistoryVO;
import com.cy.modules.qms.vo.InspectionStatisticsVO;
import com.cy.modules.qms.vo.ParetoAnalysisVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @Description: Report Controller - Lịch sử kiểm tra, thống kê, Pareto analysis, xuất báo cáo
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "QMS - Report & Analytics")
@RestController
@RequestMapping("/api/qms/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Tra cứu lịch sử kiểm tra có phân trang và filter đa tiêu chí.
     */
    @Operation(summary = "Lịch sử kiểm tra (phân trang + filter)")
    @GetMapping("/history")
    public Result<IPage<InspectionHistoryVO>> history(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "productId", required = false) String productId,
            @RequestParam(name = "templateId", required = false) String templateId,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(name = "overallResult", required = false) String overallResult,
            @RequestParam(name = "inspector", required = false) String inspector) {
        Page<InspectionExecution> page = new Page<>(pageNo, pageSize);
        IPage<InspectionHistoryVO> result = reportService.getInspectionHistory(
                page, productId, templateId, startDate, endDate, overallResult, inspector);
        return Result.OK(result);
    }

    /**
     * Thống kê pass/fail theo template và theo field.
     */
    @Operation(summary = "Thống kê pass/fail theo template")
    @GetMapping("/statistics")
    public Result<InspectionStatisticsVO> statistics(
            @RequestParam(name = "templateId") String templateId,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        InspectionStatisticsVO result = reportService.getStatistics(templateId, startDate, endDate);
        return Result.OK(result);
    }

    /**
     * Pareto analysis: top 5 fields có tỷ lệ fail cao nhất.
     */
    @Operation(summary = "Pareto analysis - top fields fail")
    @GetMapping("/pareto")
    public Result<ParetoAnalysisVO> pareto(
            @RequestParam(name = "templateId") String templateId,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        ParetoAnalysisVO result = reportService.getParetoAnalysis(templateId, startDate, endDate);
        return Result.OK(result);
    }

    /**
     * Xuất báo cáo kiểm tra (PDF/Excel) - placeholder.
     */
    @Operation(summary = "Xuất báo cáo kiểm tra (PDF/Excel)")
    @GetMapping("/export")
    public Result<String> export(
            @RequestParam(name = "templateId", required = false) String templateId,
            @RequestParam(name = "format", defaultValue = "excel") String format,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        log.info("Export report requested: templateId={}, format={}, startDate={}, endDate={}",
                templateId, format, startDate, endDate);
        return Result.OK("Chức năng xuất báo cáo " + format.toUpperCase() + " đang được phát triển. Vui lòng thử lại sau.");
    }
}
