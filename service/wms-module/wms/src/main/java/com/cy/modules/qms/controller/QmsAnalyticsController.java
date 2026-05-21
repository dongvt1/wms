package com.cy.modules.qms.controller;

import com.cy.modules.qms.service.QmsAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * @Description: QMS Analytics Controller - Báo cáo và phân tích chất lượng
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Slf4j
@Tag(name = "QMS - Báo cáo phân tích chất lượng")
@RestController
@RequestMapping("/qms/analytics")
public class QmsAnalyticsController {

    @Autowired
    private QmsAnalyticsService qmsAnalyticsService;

    /**
     * Dashboard summary: pass/fail ratios by type, open NCR count, total inspections this month
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard tổng hợp chất lượng")
    public Result<?> dashboard(
            @RequestParam(name = "productId", required = false) String productId,
            @RequestParam(name = "supplierId", required = false) String supplierId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> filters = buildFilters(productId, supplierId, startDate, endDate, null);
        Map<String, Object> summary = qmsAnalyticsService.getDashboardSummary(filters);
        return Result.OK(summary);
    }

    /**
     * Quality trend: time-series pass/fail data grouped by week or month
     */
    @GetMapping("/trend")
    @Operation(summary = "Xu hướng chất lượng theo thời gian")
    public Result<?> trend(
            @RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(name = "groupBy", defaultValue = "week") String groupBy) {
        if (!"week".equalsIgnoreCase(groupBy) && !"month".equalsIgnoreCase(groupBy)) {
            return Result.error("groupBy phải là 'week' hoặc 'month'");
        }
        List<Map<String, Object>> trend = qmsAnalyticsService.getTrend(startDate, endDate, groupBy);
        return Result.OK(trend);
    }

    /**
     * Supplier quality report: IQC pass rate, NCR count, ranking
     */
    @GetMapping("/supplier/{id}")
    @Operation(summary = "Báo cáo chất lượng nhà cung cấp")
    public Result<?> supplierReport(@PathVariable("id") String supplierId) {
        Map<String, Object> report = qmsAnalyticsService.getSupplierReport(supplierId);
        return Result.OK(report);
    }

    /**
     * Pareto analysis: top 5 criteria by failure count
     */
    @GetMapping("/pareto")
    @Operation(summary = "Phân tích Pareto - Top 5 tiêu chí lỗi cao nhất")
    public Result<?> pareto(
            @RequestParam(name = "productId", required = false) String productId,
            @RequestParam(name = "inspectionType", required = false) String inspectionType,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> filters = buildFilters(productId, null, startDate, endDate, inspectionType);
        List<Map<String, Object>> pareto = qmsAnalyticsService.getParetoAnalysis(filters);
        return Result.OK(pareto);
    }

    /**
     * Export report as Excel or PDF
     */
    @GetMapping("/export")
    @Operation(summary = "Xuất báo cáo (Excel/PDF)")
    public void export(
            @RequestParam(name = "format", defaultValue = "excel") String format,
            @RequestParam(name = "productId", required = false) String productId,
            @RequestParam(name = "supplierId", required = false) String supplierId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletResponse response) throws IOException {

        Map<String, Object> filters = buildFilters(productId, supplierId, startDate, endDate, null);
        byte[] data = qmsAnalyticsService.exportReport(format, filters);

        if (data == null || data.length == 0) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Lỗi khi xuất báo cáo\"}");
            return;
        }

        String fileName;
        String contentType;
        if ("pdf".equalsIgnoreCase(format)) {
            fileName = "QMS_Report.pdf";
            contentType = "application/pdf";
        } else {
            fileName = "QMS_Report.xlsx";
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    // ==================== Private Helper ====================

    private Map<String, Object> buildFilters(String productId, String supplierId,
                                              Date startDate, Date endDate,
                                              String inspectionType) {
        Map<String, Object> filters = new HashMap<>();
        if (productId != null && !productId.isEmpty()) {
            filters.put("productId", productId);
        }
        if (supplierId != null && !supplierId.isEmpty()) {
            filters.put("supplierId", supplierId);
        }
        if (startDate != null) {
            filters.put("startDate", startDate);
        }
        if (endDate != null) {
            filters.put("endDate", endDate);
        }
        if (inspectionType != null && !inspectionType.isEmpty()) {
            filters.put("inspectionType", inspectionType);
        }
        return filters;
    }
}
