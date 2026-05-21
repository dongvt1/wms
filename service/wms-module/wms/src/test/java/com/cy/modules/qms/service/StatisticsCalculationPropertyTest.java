package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.qms.entity.FieldValue;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.StepResult;
import com.cy.modules.qms.mapper.FieldValueMapper;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.mapper.StepResultMapper;
import com.cy.modules.qms.service.impl.ReportServiceImpl;
import com.cy.modules.qms.vo.InspectionStatisticsVO;
import com.cy.modules.qms.vo.ParetoAnalysisVO;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Property-based test for Statistics calculation correctness.
 *
 * **Validates: Requirements 11.3, 11.4**
 *
 * Property 18: Statistics calculation correctness.
 * For any set of Inspection Execution results, the computed pass/fail ratio SHALL equal
 * the count of PASS results divided by total results. The Pareto analysis SHALL correctly
 * identify and rank the top 5 fields with highest fail rates in descending order.
 */
class StatisticsCalculationPropertyTest {

    private static final String TEMPLATE_ID = "template-001";
    private static final String TEMPLATE_NAME = "Test Template";

    // ==================== Helper methods ====================

    private ReportServiceImpl createServiceWithMockedData(
            List<InspectionExecution> executions,
            List<StepResult> stepResults,
            List<FieldValue> fieldValues) {

        ReportServiceImpl service = new ReportServiceImpl();

        InspectionExecutionMapper executionMapper = Mockito.mock(InspectionExecutionMapper.class);
        StepResultMapper stepResultMapper = Mockito.mock(StepResultMapper.class);
        FieldValueMapper fieldValueMapper = Mockito.mock(FieldValueMapper.class);
        InspectionTemplateMapper templateMapper = Mockito.mock(InspectionTemplateMapper.class);

        // Mock template lookup
        InspectionTemplate template = new InspectionTemplate();
        template.setId(TEMPLATE_ID);
        template.setTemplateName(TEMPLATE_NAME);
        when(templateMapper.selectById(TEMPLATE_ID)).thenReturn(template);

        // Mock execution query - return all executions for the template
        when(executionMapper.selectList(any(QueryWrapper.class))).thenReturn(executions);

        // Mock step result query - return all step results
        when(stepResultMapper.selectList(any(QueryWrapper.class))).thenReturn(stepResults);

        // Mock field value query - return all field values
        when(fieldValueMapper.selectList(any(QueryWrapper.class))).thenReturn(fieldValues);

        // Inject mocks via reflection
        injectField(service, "inspectionExecutionMapper", executionMapper);
        injectField(service, "stepResultMapper", stepResultMapper);
        injectField(service, "fieldValueMapper", fieldValueMapper);
        injectField(service, "inspectionTemplateMapper", templateMapper);

        return service;
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }

    private InspectionExecution createExecution(String id, String overallResult) {
        InspectionExecution execution = new InspectionExecution();
        execution.setId(id);
        execution.setTemplateId(TEMPLATE_ID);
        execution.setOverallResult(overallResult);
        execution.setStatus("approved");
        execution.setProductId("product-001");
        execution.setStageType("pqc");
        return execution;
    }

    private StepResult createStepResult(String id, String executionId) {
        StepResult sr = new StepResult();
        sr.setId(id);
        sr.setExecutionId(executionId);
        sr.setStepName("Test Step");
        sr.setIsMandatory(1);
        sr.setResult("pass");
        sr.setStatus("completed");
        return sr;
    }

    private FieldValue createFieldValue(String stepResultId, String fieldId,
                                         String fieldName, String fieldType, String result) {
        FieldValue fv = new FieldValue();
        fv.setId(UUID.randomUUID().toString());
        fv.setStepResultId(stepResultId);
        fv.setFieldId(fieldId);
        fv.setFieldName(fieldName);
        fv.setFieldType(fieldType);
        fv.setIsRequired(1);
        fv.setResult(result);
        return fv;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<String> overallResults() {
        return Arbitraries.of("pass", "fail");
    }

    @Provide
    Arbitrary<String> fieldResults() {
        return Arbitraries.of("pass", "fail");
    }

    // ==================== Property 18a: Pass/fail ratio calculation ====================

    /**
     * Property 18a: For N executions with random pass/fail results,
     * passRate = passCount / total * 100 and failRate = failCount / total * 100.
     *
     * **Validates: Requirements 11.3**
     */
    @Property(tries = 200)
    void passFailRatioCalculationIsCorrect(
            @ForAll @IntRange(min = 1, max = 50) int totalExecutions,
            @ForAll List<@From("overallResults") String> results) {

        // Trim/pad results to desired size
        List<String> executionResults = results.stream()
                .limit(totalExecutions)
                .collect(Collectors.toList());
        while (executionResults.size() < totalExecutions) {
            executionResults.add("pass");
        }

        // Create execution entities
        List<InspectionExecution> executions = new ArrayList<>();
        List<StepResult> stepResults = new ArrayList<>();
        List<FieldValue> fieldValues = new ArrayList<>();

        for (int i = 0; i < executionResults.size(); i++) {
            String execId = "exec-" + i;
            executions.add(createExecution(execId, executionResults.get(i)));

            String srId = "sr-" + i;
            stepResults.add(createStepResult(srId, execId));
        }

        // Create service with mocked data
        ReportServiceImpl service = createServiceWithMockedData(executions, stepResults, fieldValues);

        // Call getStatistics
        InspectionStatisticsVO stats = service.getStatistics(TEMPLATE_ID, null, null);

        // Calculate expected values
        long expectedPassCount = executionResults.stream().filter("pass"::equals).count();
        long expectedFailCount = executionResults.stream().filter("fail"::equals).count();
        double expectedPassRate = (double) expectedPassCount / totalExecutions * 100.0;
        double expectedFailRate = (double) expectedFailCount / totalExecutions * 100.0;

        // Verify
        assertThat(stats.getTotalExecutions())
                .as("Total executions should match")
                .isEqualTo(totalExecutions);
        assertThat(stats.getPassCount())
                .as("Pass count should match")
                .isEqualTo(expectedPassCount);
        assertThat(stats.getFailCount())
                .as("Fail count should match")
                .isEqualTo(expectedFailCount);
        assertThat(stats.getPassRate())
                .as("Pass rate should be passCount/total*100")
                .isCloseTo(expectedPassRate, org.assertj.core.data.Offset.offset(0.001));
        assertThat(stats.getFailRate())
                .as("Fail rate should be failCount/total*100")
                .isCloseTo(expectedFailRate, org.assertj.core.data.Offset.offset(0.001));

        // Verify pass + fail rates sum to 100%
        assertThat(stats.getPassRate() + stats.getFailRate())
                .as("Pass rate + fail rate should equal 100%%")
                .isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.001));
    }

    // ==================== Property 18b: Pareto ranking correctness ====================

    /**
     * Property 18b: The Pareto analysis correctly identifies and ranks the top 5 fields
     * with highest fail rates in descending order.
     *
     * **Validates: Requirements 11.4**
     */
    @Property(tries = 200)
    void paretoRankingIsCorrectlyOrderedByFailRate(
            @ForAll @IntRange(min = 2, max = 10) int fieldCount,
            @ForAll @IntRange(min = 5, max = 20) int evaluationsPerField) {

        // Generate random field data with varying fail rates
        List<InspectionExecution> executions = new ArrayList<>();
        List<StepResult> stepResults = new ArrayList<>();
        List<FieldValue> fieldValues = new ArrayList<>();

        // Create executions and step results
        for (int i = 0; i < evaluationsPerField; i++) {
            String execId = "exec-" + i;
            executions.add(createExecution(execId, "pass"));

            String srId = "sr-" + i;
            stepResults.add(createStepResult(srId, execId));
        }

        // Create field values with different fail rates per field
        // Field i has fail rate = (i+1) / evaluationsPerField * some factor
        Random random = new Random(fieldCount * 31L + evaluationsPerField * 17L);
        Map<String, Double> expectedFailRates = new HashMap<>();

        for (int f = 0; f < fieldCount; f++) {
            String fieldId = "field-" + f;
            String fieldName = "Field " + f;
            // Each field gets a different fail probability
            double failProbability = (f + 1.0) / (fieldCount + 1.0);
            int failCount = 0;

            for (int i = 0; i < evaluationsPerField; i++) {
                String srId = "sr-" + i;
                boolean isFail = random.nextDouble() < failProbability;
                if (isFail) failCount++;
                String result = isFail ? "fail" : "pass";
                fieldValues.add(createFieldValue(srId, fieldId, fieldName, "measurement", result));
            }

            if (failCount > 0) {
                expectedFailRates.put(fieldId, (double) failCount / evaluationsPerField * 100.0);
            }
        }

        // Create service with mocked data
        ReportServiceImpl service = createServiceWithMockedData(executions, stepResults, fieldValues);

        // Call getParetoAnalysis
        ParetoAnalysisVO pareto = service.getParetoAnalysis(TEMPLATE_ID, null, null);

        // Verify: items are ordered by fail rate descending
        List<ParetoAnalysisVO.ParetoItemVO> items = pareto.getItems();
        assertThat(items).isNotNull();
        assertThat(items.size()).isLessThanOrEqualTo(5);

        // Verify descending order of fail rates
        for (int i = 0; i < items.size() - 1; i++) {
            assertThat(items.get(i).getFailRate())
                    .as("Item at rank %d should have fail rate >= item at rank %d", i + 1, i + 2)
                    .isGreaterThanOrEqualTo(items.get(i + 1).getFailRate());
        }

        // Verify ranks are 1-based and sequential
        for (int i = 0; i < items.size(); i++) {
            assertThat(items.get(i).getRank())
                    .as("Rank should be %d", i + 1)
                    .isEqualTo(i + 1);
        }

        // Verify that the top items are indeed the ones with highest fail rates
        if (!expectedFailRates.isEmpty() && items.size() > 0) {
            // Sort expected fail rates descending
            List<Map.Entry<String, Double>> sortedExpected = expectedFailRates.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .limit(5)
                    .collect(Collectors.toList());

            // The top items should match the top expected fields
            for (int i = 0; i < Math.min(items.size(), sortedExpected.size()); i++) {
                assertThat(items.get(i).getFieldId())
                        .as("Pareto item %d should be field with highest fail rate", i + 1)
                        .isEqualTo(sortedExpected.get(i).getKey());
            }
        }
    }

    // ==================== Property 18c: Cumulative rate calculation ====================

    /**
     * Property 18c: The cumulative rate in Pareto analysis is correctly calculated.
     * Cumulative rate at position i = sum of fail counts from position 1 to i / total fail counts in top 5.
     *
     * **Validates: Requirements 11.4**
     */
    @Property(tries = 200)
    void paretoCumulativeRateIsCorrect(
            @ForAll @IntRange(min = 3, max = 8) int fieldCount,
            @ForAll @IntRange(min = 10, max = 30) int evaluationsPerField) {

        // Generate field values with guaranteed failures
        List<InspectionExecution> executions = new ArrayList<>();
        List<StepResult> stepResults = new ArrayList<>();
        List<FieldValue> fieldValues = new ArrayList<>();

        for (int i = 0; i < evaluationsPerField; i++) {
            String execId = "exec-" + i;
            executions.add(createExecution(execId, "pass"));

            String srId = "sr-" + i;
            stepResults.add(createStepResult(srId, execId));
        }

        // Create fields with deterministic fail counts (field f has f+1 failures)
        for (int f = 0; f < fieldCount; f++) {
            String fieldId = "field-" + f;
            String fieldName = "Field " + f;
            int failsForThisField = f + 1; // Deterministic: field 0 has 1 fail, field 1 has 2 fails, etc.

            for (int i = 0; i < evaluationsPerField; i++) {
                String srId = "sr-" + i;
                String result = (i < failsForThisField) ? "fail" : "pass";
                fieldValues.add(createFieldValue(srId, fieldId, fieldName, "number", result));
            }
        }

        // Create service with mocked data
        ReportServiceImpl service = createServiceWithMockedData(executions, stepResults, fieldValues);

        // Call getParetoAnalysis
        ParetoAnalysisVO pareto = service.getParetoAnalysis(TEMPLATE_ID, null, null);

        List<ParetoAnalysisVO.ParetoItemVO> items = pareto.getItems();
        assertThat(items).isNotNull();

        if (items.isEmpty()) {
            return; // No failures, nothing to verify
        }

        // Verify cumulative rate calculation
        long totalFailsInTop5 = items.stream()
                .mapToLong(ParetoAnalysisVO.ParetoItemVO::getFailCount)
                .sum();

        long cumulativeFails = 0;
        for (ParetoAnalysisVO.ParetoItemVO item : items) {
            cumulativeFails += item.getFailCount();
            double expectedCumulativeRate = totalFailsInTop5 > 0
                    ? (double) cumulativeFails / totalFailsInTop5 * 100.0
                    : 0.0;

            assertThat(item.getCumulativeRate())
                    .as("Cumulative rate at rank %d should be correct", item.getRank())
                    .isCloseTo(expectedCumulativeRate, org.assertj.core.data.Offset.offset(0.001));
        }

        // The last item's cumulative rate should be 100%
        if (!items.isEmpty()) {
            assertThat(items.get(items.size() - 1).getCumulativeRate())
                    .as("Last item cumulative rate should be 100%%")
                    .isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.001));
        }
    }
}
