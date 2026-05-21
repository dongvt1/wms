package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.FieldValue;
import com.cy.modules.qms.entity.StepResult;
import com.cy.modules.qms.entity.enums.EvaluationResult;
import com.cy.modules.qms.service.impl.EvaluationServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for Hierarchical result aggregation.
 *
 * **Validates: Requirements 7.4, 7.5**
 *
 * Property 14: Hierarchical result aggregation.
 * For any Inspection Step, the step result SHALL be PASS if and only if ALL required fields
 * have result PASS. For any Inspection Execution, the overall result SHALL be PASS if and only
 * if ALL mandatory steps have result PASS. Optional/non-required items SHALL NOT affect the
 * parent result.
 */
class HierarchicalResultAggregationPropertyTest {

    private final EvaluationServiceImpl evaluationService = new EvaluationServiceImpl();

    // ==================== Helper methods ====================

    private FieldValue createFieldValue(String result, int isRequired) {
        FieldValue fv = new FieldValue();
        fv.setId("fv-" + System.nanoTime() + "-" + Math.random());
        fv.setFieldName("Test Field");
        fv.setFieldType("number");
        fv.setIsRequired(isRequired);
        fv.setResult(result);
        return fv;
    }

    private StepResult createStepResult(String result, int isMandatory) {
        StepResult sr = new StepResult();
        sr.setId("sr-" + System.nanoTime() + "-" + Math.random());
        sr.setStepName("Test Step");
        sr.setIsMandatory(isMandatory);
        sr.setResult(result);
        return sr;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<String> fieldResults() {
        return Arbitraries.of(
                EvaluationResult.PASS.getValue(),
                EvaluationResult.FAIL.getValue()
        );
    }

    @Provide
    Arbitrary<String> anyFieldResult() {
        return Arbitraries.of(
                EvaluationResult.PASS.getValue(),
                EvaluationResult.FAIL.getValue(),
                EvaluationResult.NA.getValue(),
                EvaluationResult.PENDING.getValue()
        );
    }

    // ==================== Step-level aggregation properties ====================

    /**
     * Property 14a: Step result is PASS iff ALL required fields have result=pass.
     *
     * Generate random combinations of required and optional fields with random pass/fail results.
     * The step result should be PASS only when every required field is PASS.
     * Optional fields (isRequired=0) should NOT affect the step result.
     *
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 1000)
    void stepResultPassIffAllRequiredFieldsPass(
            @ForAll @IntRange(min = 1, max = 8) int requiredFieldCount,
            @ForAll @IntRange(min = 0, max = 5) int optionalFieldCount,
            @ForAll List<@From("fieldResults") String> requiredResults,
            @ForAll List<@From("anyFieldResult") String> optionalResults) {

        // Trim lists to desired sizes
        List<String> reqResults = requiredResults.stream()
                .limit(requiredFieldCount)
                .collect(Collectors.toList());
        // Pad if needed
        while (reqResults.size() < requiredFieldCount) {
            reqResults.add(EvaluationResult.PASS.getValue());
        }

        List<String> optResults = optionalResults.stream()
                .limit(optionalFieldCount)
                .collect(Collectors.toList());
        while (optResults.size() < optionalFieldCount) {
            optResults.add(EvaluationResult.FAIL.getValue());
        }

        // Build field value list
        List<FieldValue> fieldValues = new ArrayList<>();
        for (String result : reqResults) {
            fieldValues.add(createFieldValue(result, 1)); // required
        }
        for (String result : optResults) {
            fieldValues.add(createFieldValue(result, 0)); // optional
        }

        // Evaluate
        String stepResult = evaluationService.evaluateStep(fieldValues);

        // Determine expected result: PASS iff ALL required fields are PASS
        boolean allRequiredPass = reqResults.stream()
                .allMatch(r -> EvaluationResult.PASS.getValue().equals(r));

        if (allRequiredPass) {
            assertThat(stepResult)
                    .as("Step should PASS when all %d required fields PASS (optional fields: %d with results %s)",
                            requiredFieldCount, optionalFieldCount, optResults)
                    .isEqualTo(EvaluationResult.PASS.getValue());
        } else {
            assertThat(stepResult)
                    .as("Step should FAIL when not all required fields PASS (required results: %s)", reqResults)
                    .isEqualTo(EvaluationResult.FAIL.getValue());
        }
    }

    /**
     * Property 14b: Optional fields do NOT affect step result regardless of their results.
     *
     * Generate a step where all required fields PASS, but optional fields have arbitrary
     * (including FAIL) results. The step should always PASS.
     *
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 500)
    void optionalFieldsDoNotAffectStepResult(
            @ForAll @IntRange(min = 1, max = 5) int requiredFieldCount,
            @ForAll @IntRange(min = 1, max = 8) int optionalFieldCount,
            @ForAll List<@From("anyFieldResult") String> optionalResults) {

        List<String> optResults = optionalResults.stream()
                .limit(optionalFieldCount)
                .collect(Collectors.toList());
        while (optResults.size() < optionalFieldCount) {
            optResults.add(EvaluationResult.FAIL.getValue());
        }

        // All required fields PASS
        List<FieldValue> fieldValues = new ArrayList<>();
        for (int i = 0; i < requiredFieldCount; i++) {
            fieldValues.add(createFieldValue(EvaluationResult.PASS.getValue(), 1));
        }
        // Optional fields with arbitrary results
        for (String result : optResults) {
            fieldValues.add(createFieldValue(result, 0));
        }

        String stepResult = evaluationService.evaluateStep(fieldValues);

        assertThat(stepResult)
                .as("Step should PASS when all required fields PASS, regardless of optional field results: %s",
                        optResults)
                .isEqualTo(EvaluationResult.PASS.getValue());
    }

    // ==================== Execution-level aggregation properties ====================

    /**
     * Property 14c: Execution result is PASS iff ALL mandatory steps have result=pass.
     *
     * Generate random combinations of mandatory and optional steps with random pass/fail results.
     * The execution result should be PASS only when every mandatory step is PASS.
     * Optional steps (isMandatory=0) should NOT affect the execution result.
     *
     * **Validates: Requirements 7.5**
     */
    @Property(tries = 1000)
    void executionResultPassIffAllMandatoryStepsPass(
            @ForAll @IntRange(min = 1, max = 8) int mandatoryStepCount,
            @ForAll @IntRange(min = 0, max = 5) int optionalStepCount,
            @ForAll List<@From("fieldResults") String> mandatoryResults,
            @ForAll List<@From("anyFieldResult") String> optionalResults) {

        // Trim lists to desired sizes
        List<String> mandResults = mandatoryResults.stream()
                .limit(mandatoryStepCount)
                .collect(Collectors.toList());
        while (mandResults.size() < mandatoryStepCount) {
            mandResults.add(EvaluationResult.PASS.getValue());
        }

        List<String> optResults = optionalResults.stream()
                .limit(optionalStepCount)
                .collect(Collectors.toList());
        while (optResults.size() < optionalStepCount) {
            optResults.add(EvaluationResult.FAIL.getValue());
        }

        // Build step result list
        List<StepResult> stepResults = new ArrayList<>();
        for (String result : mandResults) {
            stepResults.add(createStepResult(result, 1)); // mandatory
        }
        for (String result : optResults) {
            stepResults.add(createStepResult(result, 0)); // optional
        }

        // Evaluate
        String executionResult = evaluationService.evaluateExecution(stepResults);

        // Determine expected result: PASS iff ALL mandatory steps are PASS
        boolean allMandatoryPass = mandResults.stream()
                .allMatch(r -> EvaluationResult.PASS.getValue().equals(r));

        if (allMandatoryPass) {
            assertThat(executionResult)
                    .as("Execution should PASS when all %d mandatory steps PASS (optional steps: %d with results %s)",
                            mandatoryStepCount, optionalStepCount, optResults)
                    .isEqualTo(EvaluationResult.PASS.getValue());
        } else {
            assertThat(executionResult)
                    .as("Execution should FAIL when not all mandatory steps PASS (mandatory results: %s)", mandResults)
                    .isEqualTo(EvaluationResult.FAIL.getValue());
        }
    }

    /**
     * Property 14d: Optional steps do NOT affect execution result regardless of their results.
     *
     * Generate an execution where all mandatory steps PASS, but optional steps have arbitrary
     * (including FAIL) results. The execution should always PASS.
     *
     * **Validates: Requirements 7.5**
     */
    @Property(tries = 500)
    void optionalStepsDoNotAffectExecutionResult(
            @ForAll @IntRange(min = 1, max = 5) int mandatoryStepCount,
            @ForAll @IntRange(min = 1, max = 8) int optionalStepCount,
            @ForAll List<@From("anyFieldResult") String> optionalResults) {

        List<String> optResults = optionalResults.stream()
                .limit(optionalStepCount)
                .collect(Collectors.toList());
        while (optResults.size() < optionalStepCount) {
            optResults.add(EvaluationResult.FAIL.getValue());
        }

        // All mandatory steps PASS
        List<StepResult> stepResults = new ArrayList<>();
        for (int i = 0; i < mandatoryStepCount; i++) {
            stepResults.add(createStepResult(EvaluationResult.PASS.getValue(), 1));
        }
        // Optional steps with arbitrary results
        for (String result : optResults) {
            stepResults.add(createStepResult(result, 0));
        }

        String executionResult = evaluationService.evaluateExecution(stepResults);

        assertThat(executionResult)
                .as("Execution should PASS when all mandatory steps PASS, regardless of optional step results: %s",
                        optResults)
                .isEqualTo(EvaluationResult.PASS.getValue());
    }
}
