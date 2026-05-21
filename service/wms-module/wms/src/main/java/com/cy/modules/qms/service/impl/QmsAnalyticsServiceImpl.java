package com.cy.modules.qms.service.impl;

import com.cy.modules.qms.service.QmsAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * @Description: QMS Analytics Service Implementation - SQL aggregation queries
 * @Author: BMad
 * @Date: 2026-02-25
 */
@Service
@Slf4j
public class QmsAnalyticsServiceImpl implements QmsAnalyticsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> getDashboardSummary(Map<String, Object> filters) {
        Map<String, Object> summary = new LinkedHashMap<>();

        // IQC pass/fail ratios
        summary.put("iqc", getInspectionRatios("qms_iqc_inspection", filters));

        // PQC pass/fail ratios
        summary.put("pqc", getInspectionRatios("qms_pqc_inspection", filters));

        // FQC pass/fail ratios
        summary.put("fqc", getInspectionRatios("qms_fqc_inspection", filters));

        // Open NCR count
        Long openNcrCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM qms_ncr WHERE status != 'closed'", Long.class);
        summary.put("openNcrCount", openNcrCount != null ? openNcrCount : 0L);

        // Total inspections this month
        Long totalThisMonth = getTotalInspectionsThisMonth();
        summary.put("totalInspectionsThisMonth", totalThisMonth);

        return summary;
    }

    @Override
    public List<Map<String, Object>> getTrend(Date startDate, Date endDate, String groupBy) {
        String dateFormat;
        if ("month".equalsIgnoreCase(groupBy)) {
            dateFormat = "%Y-%m";
        } else {
            // Default to week
            dateFormat = "%x-W%v";
        }

        String sql = """
                SELECT period, SUM(pass_count) AS passCount, SUM(fail_count) AS failCount,
                       CASE WHEN SUM(pass_count) + SUM(fail_count) = 0 THEN 0
                            ELSE ROUND(SUM(pass_count) * 100.0 / (SUM(pass_count) + SUM(fail_count)), 2)
                       END AS passRate
                FROM (
                    SELECT DATE_FORMAT(inspection_date, ?) AS period,
                           SUM(CASE WHEN status = 'passed' THEN 1 ELSE 0 END) AS pass_count,
                           SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) AS fail_count
                    FROM qms_iqc_inspection
                    WHERE inspection_date BETWEEN ? AND ?
                      AND status IN ('passed', 'failed', 'conditional')
                    GROUP BY DATE_FORMAT(inspection_date, ?)
                    UNION ALL
                    SELECT DATE_FORMAT(inspection_date, ?) AS period,
                           SUM(CASE WHEN status = 'passed' THEN 1 ELSE 0 END) AS pass_count,
                           SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) AS fail_count
                    FROM qms_pqc_inspection
                    WHERE inspection_date BETWEEN ? AND ?
                      AND status IN ('passed', 'failed')
                    GROUP BY DATE_FORMAT(inspection_date, ?)
                    UNION ALL
                    SELECT DATE_FORMAT(inspection_date, ?) AS period,
                           SUM(CASE WHEN status = 'passed' THEN 1 ELSE 0 END) AS pass_count,
                           SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) AS fail_count
                    FROM qms_fqc_inspection
                    WHERE inspection_date BETWEEN ? AND ?
                      AND status IN ('passed', 'failed')
                    GROUP BY DATE_FORMAT(inspection_date, ?)
                ) combined
                GROUP BY period
                ORDER BY period
                """;

        return jdbcTemplate.queryForList(sql,
                dateFormat, startDate, endDate, dateFormat,
                dateFormat, startDate, endDate, dateFormat,
                dateFormat, startDate, endDate, dateFormat);
    }

    @Override
    public Map<String, Object> getSupplierReport(String supplierId) {
        Map<String, Object> report = new LinkedHashMap<>();

        // IQC pass rate for this supplier
        String iqcSql = """
                SELECT COUNT(*) AS total,
                       SUM(CASE WHEN status = 'passed' THEN 1 ELSE 0 END) AS passed
                FROM qms_iqc_inspection
                WHERE supplier_id = ?
                  AND status IN ('passed', 'failed', 'conditional')
                """;
        Map<String, Object> iqcStats = jdbcTemplate.queryForMap(iqcSql, supplierId);
        long total = ((Number) iqcStats.getOrDefault("total", 0L)).longValue();
        long passed = ((Number) iqcStats.getOrDefault("passed", 0L)).longValue();
        double passRate = total > 0 ? Math.round(passed * 10000.0 / total) / 100.0 : 0.0;

        report.put("supplierId", supplierId);
        report.put("iqcTotal", total);
        report.put("iqcPassed", passed);
        report.put("iqcPassRate", passRate);

        // NCR count for this supplier
        Long ncrCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM qms_ncr WHERE supplier_id = ?", Long.class, supplierId);
        report.put("ncrCount", ncrCount != null ? ncrCount : 0L);

        // Ranking among all suppliers (by pass rate descending)
        String rankingSql = """
                SELECT supplier_id,
                       COUNT(*) AS total,
                       SUM(CASE WHEN status = 'passed' THEN 1 ELSE 0 END) AS passed,
                       ROUND(SUM(CASE WHEN status = 'passed' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS pass_rate
                FROM qms_iqc_inspection
                WHERE status IN ('passed', 'failed', 'conditional')
                  AND supplier_id IS NOT NULL
                GROUP BY supplier_id
                HAVING COUNT(*) > 0
                ORDER BY pass_rate DESC
                """;
        List<Map<String, Object>> rankings = jdbcTemplate.queryForList(rankingSql);
        int rank = 1;
        int totalSuppliers = rankings.size();
        for (Map<String, Object> row : rankings) {
            if (supplierId.equals(row.get("supplier_id"))) {
                break;
            }
            rank++;
        }
        report.put("ranking", rank);
        report.put("totalSuppliers", totalSuppliers);

        return report;
    }

    @Override
    public List<Map<String, Object>> getParetoAnalysis(Map<String, Object> filters) {
        List<Object> params = new ArrayList<>();
        StringBuilder filterClause = new StringBuilder();

        if (filters != null) {
            if (filters.containsKey("startDate") && filters.get("startDate") != null) {
                filterClause.append(" AND i.inspection_date >= ?");
                params.add(filters.get("startDate"));
            }
            if (filters.containsKey("endDate") && filters.get("endDate") != null) {
                filterClause.append(" AND i.inspection_date <= ?");
                params.add(filters.get("endDate"));
            }
            if (filters.containsKey("productId") && filters.get("productId") != null) {
                filterClause.append(" AND i.product_id = ?");
                params.add(filters.get("productId"));
            }
        }

        String filterStr = filterClause.toString();

        // Query across IQC, PQC, FQC result tables — each subquery filters independently
        String sql = "SELECT criterion_name AS criterionName, COUNT(*) AS failureCount FROM ("
                + " SELECT r.criterion_name FROM qms_iqc_inspection_result r"
                + " JOIN qms_iqc_inspection i ON r.inspection_id = i.id"
                + " WHERE r.result = 'failed'" + filterStr
                + " UNION ALL"
                + " SELECT r.criterion_name FROM qms_pqc_inspection_result r"
                + " JOIN qms_pqc_inspection i ON r.inspection_id = i.id"
                + " WHERE r.result = 'failed'" + filterStr
                + " UNION ALL"
                + " SELECT r.criterion_name FROM qms_fqc_inspection_result r"
                + " JOIN qms_fqc_inspection i ON r.inspection_id = i.id"
                + " WHERE r.result = 'failed'" + filterStr
                + ") AS pareto_data"
                + " GROUP BY criterion_name"
                + " ORDER BY failureCount DESC"
                + " LIMIT 5";

        // Params are repeated 3 times (once per UNION ALL subquery)
        List<Object> allParams = new ArrayList<>();
        allParams.addAll(params);
        allParams.addAll(params);
        allParams.addAll(params);

        return jdbcTemplate.queryForList(sql, allParams.toArray());
    }

    @Override
    public byte[] exportReport(String format, Map<String, Object> filters) {
        if ("excel".equalsIgnoreCase(format)) {
            return exportExcel(filters);
        }
        // PDF export - TODO for future implementation
        // For MVP, return a simple text-based PDF placeholder
        return exportExcel(filters);
    }

    // ==================== Private Helper Methods ====================

    private Map<String, Object> getInspectionRatios(String tableName, Map<String, Object> filters) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT COUNT(*) AS total, ");
        sql.append("SUM(CASE WHEN status = 'passed' THEN 1 ELSE 0 END) AS passed, ");
        sql.append("SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) AS failed ");
        sql.append("FROM ").append(tableName);
        sql.append(" WHERE status IN ('passed', 'failed', 'conditional')");

        if (filters != null) {
            if (filters.containsKey("productId") && filters.get("productId") != null) {
                sql.append(" AND product_id = ?");
                params.add(filters.get("productId"));
            }
            if (filters.containsKey("supplierId") && filters.get("supplierId") != null
                    && tableName.contains("iqc")) {
                sql.append(" AND supplier_id = ?");
                params.add(filters.get("supplierId"));
            }
            if (filters.containsKey("startDate") && filters.get("startDate") != null) {
                sql.append(" AND inspection_date >= ?");
                params.add(filters.get("startDate"));
            }
            if (filters.containsKey("endDate") && filters.get("endDate") != null) {
                sql.append(" AND inspection_date <= ?");
                params.add(filters.get("endDate"));
            }
        }

        Map<String, Object> row = jdbcTemplate.queryForMap(sql.toString(), params.toArray());
        long total = ((Number) row.getOrDefault("total", 0L)).longValue();
        long passed = ((Number) row.getOrDefault("passed", 0L)).longValue();
        long failed = ((Number) row.getOrDefault("failed", 0L)).longValue();
        double passRate = total > 0 ? Math.round(passed * 10000.0 / total) / 100.0 : 0.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("passed", passed);
        result.put("failed", failed);
        result.put("passRate", passRate);
        return result;
    }

    private Long getTotalInspectionsThisMonth() {
        String sql = """
                SELECT (
                    (SELECT COUNT(*) FROM qms_iqc_inspection
                     WHERE DATE_FORMAT(inspection_date, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m'))
                    +
                    (SELECT COUNT(*) FROM qms_pqc_inspection
                     WHERE DATE_FORMAT(inspection_date, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m'))
                    +
                    (SELECT COUNT(*) FROM qms_fqc_inspection
                     WHERE DATE_FORMAT(inspection_date, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m'))
                ) AS total
                """;
        Long total = jdbcTemplate.queryForObject(sql, Long.class);
        return total != null ? total : 0L;
    }

    private byte[] exportExcel(Map<String, Object> filters) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Dashboard Summary
            Sheet summarySheet = workbook.createSheet("Dashboard Summary");
            Map<String, Object> summary = getDashboardSummary(filters);
            writeDashboardSheet(summarySheet, summary);

            // Sheet 2: Pareto Analysis
            Sheet paretoSheet = workbook.createSheet("Pareto Analysis");
            List<Map<String, Object>> pareto = getParetoAnalysis(filters);
            writeParetoSheet(paretoSheet, pareto);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting analytics report to Excel", e);
            return new byte[0];
        }
    }

    private void writeDashboardSheet(Sheet sheet, Map<String, Object> summary) {
        // Header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Loại kiểm tra");
        headerRow.createCell(1).setCellValue("Tổng");
        headerRow.createCell(2).setCellValue("Đạt");
        headerRow.createCell(3).setCellValue("Không đạt");
        headerRow.createCell(4).setCellValue("Tỷ lệ đạt (%)");

        int rowIdx = 1;
        String[] types = {"iqc", "pqc", "fqc"};
        String[] labels = {"IQC", "PQC", "FQC"};

        for (int i = 0; i < types.length; i++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) summary.get(types[i]);
            if (data != null) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(labels[i]);
                row.createCell(1).setCellValue(((Number) data.getOrDefault("total", 0)).longValue());
                row.createCell(2).setCellValue(((Number) data.getOrDefault("passed", 0)).longValue());
                row.createCell(3).setCellValue(((Number) data.getOrDefault("failed", 0)).longValue());
                row.createCell(4).setCellValue(((Number) data.getOrDefault("passRate", 0.0)).doubleValue());
            }
        }

        // NCR and total info
        rowIdx++;
        Row ncrRow = sheet.createRow(rowIdx++);
        ncrRow.createCell(0).setCellValue("NCR mở");
        ncrRow.createCell(1).setCellValue(((Number) summary.getOrDefault("openNcrCount", 0)).longValue());

        Row totalRow = sheet.createRow(rowIdx);
        totalRow.createCell(0).setCellValue("Tổng phiếu tháng này");
        totalRow.createCell(1).setCellValue(((Number) summary.getOrDefault("totalInspectionsThisMonth", 0)).longValue());

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void writeParetoSheet(Sheet sheet, List<Map<String, Object>> pareto) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Tiêu chí");
        headerRow.createCell(1).setCellValue("Số lỗi");

        int rowIdx = 1;
        for (Map<String, Object> item : pareto) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(String.valueOf(item.getOrDefault("criterionName", "")));
            row.createCell(1).setCellValue(((Number) item.getOrDefault("failureCount", 0)).longValue());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
}
