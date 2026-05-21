package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.qms.entity.FieldValue;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.StepResult;
import com.cy.modules.qms.mapper.FieldValueMapper;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.mapper.StepResultMapper;
import com.cy.modules.qms.service.ReportService;
import com.cy.modules.qms.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation ReportService.
 * Cung cấp báo cáo và thống kê kiểm tra chất lượng.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private InspectionExecutionMapper inspectionExecutionMapper;

    @Autowired
    private StepResultMapper stepResultMapper;

    @Autowired
    private FieldValueMapper fieldValueMapper;

    @Autowired
    private InspectionTemplateMapper inspectionTemplateMapper;

    @Override
    public IPage<InspectionHistoryVO> getInspectionHistory(Page<InspectionExecution> page,
                                                            String productId,
                                                            String templateId,
                                                            Date startDate,
                                                            Date endDate,
                                                            String overallResult,
                                                            String inspector) {
        QueryWrapper<InspectionExecution> queryWrapper = new QueryWrapper<>();

        // Chỉ lấy các phiên đã hoàn thành (approved hoặc rejected)
        queryWrapper.in("status", "approved", "rejected");

        if (productId != null && !productId.isEmpty()) {
            queryWrapper.eq("product_id", productId);
        }
        if (templateId != null && !templateId.isEmpty()) {
            queryWrapper.eq("template_id", templateId);
        }
        if (startDate != null) {
            queryWrapper.ge("create_time", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("create_time", endDate);
        }
        if (overallResult != null && !overallResult.isEmpty()) {
            queryWrapper.eq("overall_result", overallResult);
        }
        if (inspector != null && !inspector.isEmpty()) {
            queryWrapper.like("inspector", inspector);
        }

        queryWrapper.orderByDesc("create_time");

        IPage<InspectionExecution> entityPage = inspectionExecutionMapper.selectPage(page, queryWrapper);

        // Convert to VO page
        Page<InspectionHistoryVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<InspectionHistoryVO> voList = new ArrayList<>();

        // Cache template names to avoid repeated queries
        Map<String, String> templateNameCache = new HashMap<>();

        for (InspectionExecution execution : entityPage.getRecords()) {
            InspectionHistoryVO vo = new InspectionHistoryVO();
            BeanUtils.copyProperties(execution, vo);

            // Resolve template name
            String tplName = templateNameCache.computeIfAbsent(execution.getTemplateId(), tplId -> {
                InspectionTemplate tpl = inspectionTemplateMapper.selectById(tplId);
                return tpl != null ? tpl.getTemplateName() : null;
            });
            vo.setTemplateName(tplName);

            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public InspectionStatisticsVO getStatistics(String templateId, Date startDate, Date endDate) {
        // 1. Load template info
        InspectionTemplate template = inspectionTemplateMapper.selectById(templateId);

        InspectionStatisticsVO result = new InspectionStatisticsVO();
        result.setTemplateId(templateId);
        result.setTemplateName(template != null ? template.getTemplateName() : null);

        // 2. Query executions cho template trong dateRange có overall_result != null
        List<InspectionExecution> executions = queryCompletedExecutions(templateId, startDate, endDate);

        // 3. Tính tỷ lệ pass/fail tổng thể
        long totalExecutions = executions.size();
        long passCount = executions.stream()
                .filter(e -> "pass".equals(e.getOverallResult()))
                .count();
        long failCount = executions.stream()
                .filter(e -> "fail".equals(e.getOverallResult()))
                .count();

        result.setTotalExecutions(totalExecutions);
        result.setPassCount(passCount);
        result.setFailCount(failCount);
        result.setPassRate(totalExecutions > 0 ? (double) passCount / totalExecutions * 100.0 : 0.0);
        result.setFailRate(totalExecutions > 0 ? (double) failCount / totalExecutions * 100.0 : 0.0);

        // 4. Tính thống kê theo field
        List<InspectionStatisticsVO.FieldStatisticsVO> fieldStats = calculateFieldStatistics(executions);
        result.setFieldStatistics(fieldStats);

        return result;
    }

    @Override
    public ParetoAnalysisVO getParetoAnalysis(String templateId, Date startDate, Date endDate) {
        // 1. Load template info
        InspectionTemplate template = inspectionTemplateMapper.selectById(templateId);

        ParetoAnalysisVO result = new ParetoAnalysisVO();
        result.setTemplateId(templateId);
        result.setTemplateName(template != null ? template.getTemplateName() : null);

        // 2. Query executions cho template trong dateRange
        List<InspectionExecution> executions = queryCompletedExecutions(templateId, startDate, endDate);

        if (executions.isEmpty()) {
            result.setItems(Collections.emptyList());
            return result;
        }

        // 3. Query tất cả FieldValue thuộc các execution này
        List<String> executionIds = executions.stream()
                .map(InspectionExecution::getId)
                .collect(Collectors.toList());

        List<FieldValue> allFieldValues = queryFieldValuesByExecutionIds(executionIds);

        // 4. Group by fieldId, tính fail_count và total_count
        Map<String, List<FieldValue>> fieldValuesByFieldId = allFieldValues.stream()
                .filter(fv -> fv.getResult() != null && ("pass".equals(fv.getResult()) || "fail".equals(fv.getResult())))
                .collect(Collectors.groupingBy(FieldValue::getFieldId));

        // 5. Tính fail_rate cho mỗi field
        List<ParetoAnalysisVO.ParetoItemVO> paretoItems = new ArrayList<>();
        for (Map.Entry<String, List<FieldValue>> entry : fieldValuesByFieldId.entrySet()) {
            String fieldId = entry.getKey();
            List<FieldValue> fieldValues = entry.getValue();

            long totalEvaluations = fieldValues.size();
            long failCount = fieldValues.stream()
                    .filter(fv -> "fail".equals(fv.getResult()))
                    .count();

            if (failCount == 0) {
                continue; // Skip fields with no failures
            }

            double failRate = (double) failCount / totalEvaluations * 100.0;

            ParetoAnalysisVO.ParetoItemVO item = new ParetoAnalysisVO.ParetoItemVO();
            item.setFieldId(fieldId);
            item.setFieldName(fieldValues.get(0).getFieldName());
            item.setFieldType(fieldValues.get(0).getFieldType());
            item.setTotalEvaluations(totalEvaluations);
            item.setFailCount(failCount);
            item.setFailRate(failRate);

            paretoItems.add(item);
        }

        // 6. Sắp xếp theo fail_rate giảm dần, lấy top 5
        paretoItems.sort((a, b) -> Double.compare(b.getFailRate(), a.getFailRate()));
        List<ParetoAnalysisVO.ParetoItemVO> top5 = paretoItems.stream()
                .limit(5)
                .collect(Collectors.toList());

        // 7. Tính cumulative_rate
        long totalFailsInTop5 = top5.stream().mapToLong(ParetoAnalysisVO.ParetoItemVO::getFailCount).sum();
        long cumulativeFails = 0;
        for (int i = 0; i < top5.size(); i++) {
            ParetoAnalysisVO.ParetoItemVO item = top5.get(i);
            item.setRank(i + 1);
            cumulativeFails += item.getFailCount();
            item.setCumulativeRate(totalFailsInTop5 > 0
                    ? (double) cumulativeFails / totalFailsInTop5 * 100.0
                    : 0.0);
        }

        result.setItems(top5);
        return result;
    }

    @Override
    public InspectionTrendVO getTrend(String templateId, Date startDate, Date endDate, String interval) {
        // 1. Load template info
        InspectionTemplate template = inspectionTemplateMapper.selectById(templateId);

        InspectionTrendVO result = new InspectionTrendVO();
        result.setTemplateId(templateId);
        result.setTemplateName(template != null ? template.getTemplateName() : null);
        result.setInterval(interval);

        // 2. Query executions cho template trong dateRange có overall_result != null
        List<InspectionExecution> executions = queryCompletedExecutions(templateId, startDate, endDate);

        if (executions.isEmpty()) {
            result.setDataPoints(Collections.emptyList());
            return result;
        }

        // 3. Group by period
        SimpleDateFormat periodFormat = getPeriodFormat(interval);
        Map<String, List<InspectionExecution>> groupedByPeriod = executions.stream()
                .collect(Collectors.groupingBy(
                        e -> formatPeriod(e, interval, periodFormat),
                        TreeMap::new, // TreeMap để tự động sắp xếp theo key (thời gian)
                        Collectors.toList()
                ));

        // 4. Tính pass_count, fail_count, total, pass_rate cho mỗi period
        List<InspectionTrendVO.TrendDataPointVO> dataPoints = new ArrayList<>();
        for (Map.Entry<String, List<InspectionExecution>> entry : groupedByPeriod.entrySet()) {
            String period = entry.getKey();
            List<InspectionExecution> periodExecutions = entry.getValue();

            long total = periodExecutions.size();
            long passCount = periodExecutions.stream()
                    .filter(e -> "pass".equals(e.getOverallResult()))
                    .count();
            long failCount = periodExecutions.stream()
                    .filter(e -> "fail".equals(e.getOverallResult()))
                    .count();

            InspectionTrendVO.TrendDataPointVO dataPoint = new InspectionTrendVO.TrendDataPointVO();
            dataPoint.setPeriod(period);
            dataPoint.setTotalExecutions(total);
            dataPoint.setPassCount(passCount);
            dataPoint.setFailCount(failCount);
            dataPoint.setPassRate(total > 0 ? (double) passCount / total * 100.0 : 0.0);

            dataPoints.add(dataPoint);
        }

        result.setDataPoints(dataPoints);
        return result;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Query các InspectionExecution đã hoàn thành (có overall_result) cho template trong dateRange.
     */
    private List<InspectionExecution> queryCompletedExecutions(String templateId, Date startDate, Date endDate) {
        QueryWrapper<InspectionExecution> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("template_id", templateId);
        queryWrapper.isNotNull("overall_result");
        queryWrapper.ne("overall_result", "");

        if (startDate != null) {
            queryWrapper.ge("create_time", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("create_time", endDate);
        }

        queryWrapper.orderByAsc("create_time");
        return inspectionExecutionMapper.selectList(queryWrapper);
    }

    /**
     * Query tất cả FieldValue thuộc các execution IDs (qua StepResult).
     */
    private List<FieldValue> queryFieldValuesByExecutionIds(List<String> executionIds) {
        if (executionIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Lấy tất cả step_result_ids thuộc các execution
        QueryWrapper<StepResult> stepResultQuery = new QueryWrapper<>();
        stepResultQuery.in("execution_id", executionIds);
        List<StepResult> stepResults = stepResultMapper.selectList(stepResultQuery);

        if (stepResults.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> stepResultIds = stepResults.stream()
                .map(StepResult::getId)
                .collect(Collectors.toList());

        // Lấy tất cả field values thuộc các step results
        QueryWrapper<FieldValue> fieldValueQuery = new QueryWrapper<>();
        fieldValueQuery.in("step_result_id", stepResultIds);
        return fieldValueMapper.selectList(fieldValueQuery);
    }

    /**
     * Tính thống kê pass/fail cho từng field từ danh sách executions.
     */
    private List<InspectionStatisticsVO.FieldStatisticsVO> calculateFieldStatistics(
            List<InspectionExecution> executions) {
        if (executions.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> executionIds = executions.stream()
                .map(InspectionExecution::getId)
                .collect(Collectors.toList());

        List<FieldValue> allFieldValues = queryFieldValuesByExecutionIds(executionIds);

        // Group by fieldId, chỉ tính field có result = pass hoặc fail
        Map<String, List<FieldValue>> fieldValuesByFieldId = allFieldValues.stream()
                .filter(fv -> fv.getResult() != null && ("pass".equals(fv.getResult()) || "fail".equals(fv.getResult())))
                .collect(Collectors.groupingBy(FieldValue::getFieldId));

        List<InspectionStatisticsVO.FieldStatisticsVO> fieldStats = new ArrayList<>();
        for (Map.Entry<String, List<FieldValue>> entry : fieldValuesByFieldId.entrySet()) {
            List<FieldValue> fieldValues = entry.getValue();

            long totalEvaluations = fieldValues.size();
            long passCount = fieldValues.stream()
                    .filter(fv -> "pass".equals(fv.getResult()))
                    .count();
            long failCount = fieldValues.stream()
                    .filter(fv -> "fail".equals(fv.getResult()))
                    .count();

            InspectionStatisticsVO.FieldStatisticsVO fieldStat = new InspectionStatisticsVO.FieldStatisticsVO();
            fieldStat.setFieldId(entry.getKey());
            fieldStat.setFieldName(fieldValues.get(0).getFieldName());
            fieldStat.setFieldType(fieldValues.get(0).getFieldType());
            fieldStat.setTotalEvaluations(totalEvaluations);
            fieldStat.setPassCount(passCount);
            fieldStat.setFailCount(failCount);
            fieldStat.setPassRate(totalEvaluations > 0 ? (double) passCount / totalEvaluations * 100.0 : 0.0);
            fieldStat.setFailRate(totalEvaluations > 0 ? (double) failCount / totalEvaluations * 100.0 : 0.0);

            fieldStats.add(fieldStat);
        }

        // Sắp xếp theo failRate giảm dần
        fieldStats.sort((a, b) -> Double.compare(b.getFailRate(), a.getFailRate()));
        return fieldStats;
    }

    /**
     * Format period label cho một execution dựa trên interval.
     */
    private String formatPeriod(InspectionExecution execution, String interval, SimpleDateFormat format) {
        Date date = execution.getInspectionDate() != null ? execution.getInspectionDate() : execution.getCreateTime();
        if (date == null) {
            return "unknown";
        }

        if ("weekly".equals(interval)) {
            // Format tuần: yyyy-'W'ww
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);
            int week = cal.get(Calendar.WEEK_OF_YEAR);
            return String.format("%d-W%02d", year, week);
        }

        return format.format(date);
    }

    /**
     * Lấy SimpleDateFormat phù hợp cho interval.
     */
    private SimpleDateFormat getPeriodFormat(String interval) {
        switch (interval != null ? interval : "daily") {
            case "monthly":
                return new SimpleDateFormat("yyyy-MM");
            case "weekly":
                return new SimpleDateFormat("yyyy-'W'ww"); // Fallback, actual logic in formatPeriod
            case "daily":
            default:
                return new SimpleDateFormat("yyyy-MM-dd");
        }
    }
}
