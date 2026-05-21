package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.FieldValue;
import com.cy.modules.qms.entity.StepResult;
import com.cy.modules.qms.entity.enums.EvaluationResult;
import com.cy.modules.qms.service.impl.EvaluationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for step-level and execution-level aggregation in EvaluationService.
 *
 * Validates: Requirements 7.4, 7.5, 7.6
 */
class EvaluationServiceAggregationTest {

    private EvaluationServiceImpl evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new EvaluationServiceImpl();
    }

    // ==================== Helper methods ====================

    private FieldValue createFieldValue(String result, int isRequired) {
        FieldValue fv = new FieldValue();
        fv.setId("fv-" + System.nanoTime());
        fv.setFieldName("Test Field");
        fv.setFieldType("number");
        fv.setIsRequired(isRequired);
        fv.setResult(result);
        return fv;
    }

    private StepResult createStepResult(String result, int isMandatory) {
        StepResult sr = new StepResult();
        sr.setId("sr-" + System.nanoTime());
        sr.setStepName("Test Step");
        sr.setIsMandatory(isMandatory);
        sr.setResult(result);
        return sr;
    }

    // ==================== evaluateStep tests ====================

    @Nested
    @DisplayName("evaluateStep - Step-level aggregation")
    class EvaluateStepTests {

        @Test
        @DisplayName("PASS when all required fields PASS")
        void passWhenAllRequiredFieldsPass() {
            List<FieldValue> fields = Arrays.asList(
                    createFieldValue(EvaluationResult.PASS.getValue(), 1),
                    createFieldValue(EvaluationResult.PASS.getValue(), 1),
                    createFieldValue(EvaluationResult.PASS.getValue(), 1)
            );

            String result = evaluationService.evaluateStep(fields);

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("FAIL when any required field FAILS")
        void failWhenAnyRequiredFieldFails() {
            List<FieldValue> fields = Arrays.asList(
                    createFieldValue(EvaluationResult.PASS.getValue(), 1),
                    createFieldValue(EvaluationResult.FAIL.getValue(), 1),
                    createFieldValue(EvaluationResult.PASS.getValue(), 1)
            );

            String result = evaluationService.evaluateStep(fields);

            assertThat(result).isEqualTo(EvaluationResult.FAIL.getValue());
        }

        @Test
        @DisplayName("PASS when optional fields FAIL but all required fields PASS")
        void passWhenOptionalFieldsFailButRequiredPass() {
            List<FieldValue> fields = Arrays.asList(
                    createFieldValue(EvaluationResult.PASS.getValue(), 1),  // required, pass
                    createFieldValue(EvaluationResult.FAIL.getValue(), 0),  // optional, fail
                    createFieldValue(EvaluationResult.FAIL.getValue(), 0)   // optional, fail
            );

            String result = evaluationService.evaluateStep(fields);

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("PASS when only optional fields exist (no required fields)")
        void passWhenOnlyOptionalFieldsExist() {
            List<FieldValue> fields = Arrays.asList(
                    createFieldValue(EvaluationResult.FAIL.getValue(), 0),
                    createFieldValue(EvaluationResult.FAIL.getValue(), 0)
            );

            String result = evaluationService.evaluateStep(fields);

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("PASS when field list is empty")
        void passWhenFieldListIsEmpty() {
            String result = evaluationService.evaluateStep(Collections.emptyList());

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("PASS when field list is null")
        void passWhenFieldListIsNull() {
            String result = evaluationService.evaluateStep(null);

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("FAIL when required field has pending result")
        void failWhenRequiredFieldHasPendingResult() {
            List<FieldValue> fields = Arrays.asList(
                    createFieldValue(EvaluationResult.PASS.getValue(), 1),
                    createFieldValue(EvaluationResult.PENDING.getValue(), 1)
            );

            String result = evaluationService.evaluateStep(fields);

            assertThat(result).isEqualTo(EvaluationResult.FAIL.getValue());
        }

        @Test
        @DisplayName("FAIL when required field has NA result")
        void failWhenRequiredFieldHasNaResult() {
            List<FieldValue> fields = Arrays.asList(
                    createFieldValue(EvaluationResult.PASS.getValue(), 1),
                    createFieldValue(EvaluationResult.NA.getValue(), 1)
            );

            String result = evaluationService.evaluateStep(fields);

            assertThat(result).isEqualTo(EvaluationResult.FAIL.getValue());
        }

        @Test
        @DisplayName("Mixed required and optional fields - only required affect result")
        void mixedRequiredAndOptionalFields() {
            List<FieldValue> fields = Arrays.asList(
                    createFieldValue(EvaluationResult.PASS.getValue(), 1),   // required, pass
                    createFieldValue(EvaluationResult.PASS.getValue(), 1),   // required, pass
                    createFieldValue(EvaluationResult.FAIL.getValue(), 0),   // optional, fail
                    createFieldValue(EvaluationResult.NA.getValue(), 0)      // optional, na
            );

            String result = evaluationService.evaluateStep(fields);

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }
    }

    // ==================== evaluateExecution tests ====================

    @Nested
    @DisplayName("evaluateExecution - Execution-level aggregation")
    class EvaluateExecutionTests {

        @Test
        @DisplayName("PASS when all mandatory steps PASS")
        void passWhenAllMandatoryStepsPass() {
            List<StepResult> steps = Arrays.asList(
                    createStepResult(EvaluationResult.PASS.getValue(), 1),
                    createStepResult(EvaluationResult.PASS.getValue(), 1),
                    createStepResult(EvaluationResult.PASS.getValue(), 1)
            );

            String result = evaluationService.evaluateExecution(steps);

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("FAIL when any mandatory step FAILS")
        void failWhenAnyMandatoryStepFails() {
            List<StepResult> steps = Arrays.asList(
                    createStepResult(EvaluationResult.PASS.getValue(), 1),
                    createStepResult(EvaluationResult.FAIL.getValue(), 1),
                    createStepResult(EvaluationResult.PASS.getValue(), 1)
            );

            String result = evaluationService.evaluateExecution(steps);

            assertThat(result).isEqualTo(EvaluationResult.FAIL.getValue());
        }

        @Test
        @DisplayName("PASS when optional steps FAIL but all mandatory steps PASS")
        void passWhenOptionalStepsFailButMandatoryPass() {
            List<StepResult> steps = Arrays.asList(
                    createStepResult(EvaluationResult.PASS.getValue(), 1),  // mandatory, pass
                    createStepResult(EvaluationResult.FAIL.getValue(), 0),  // optional, fail
                    createStepResult(EvaluationResult.FAIL.getValue(), 0)   // optional, fail
            );

            String result = evaluationService.evaluateExecution(steps);

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("PASS when only optional steps exist (no mandatory steps)")
        void passWhenOnlyOptionalStepsExist() {
            List<StepResult> steps = Arrays.asList(
                    createStepResult(EvaluationResult.FAIL.getValue(), 0),
                    createStepResult(EvaluationResult.FAIL.getValue(), 0)
            );

            String result = evaluationService.evaluateExecution(steps);

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("PASS when step list is empty")
        void passWhenStepListIsEmpty() {
            String result = evaluationService.evaluateExecution(Collections.emptyList());

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("PASS when step list is null")
        void passWhenStepListIsNull() {
            String result = evaluationService.evaluateExecution(null);

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("FAIL when mandatory step has pending result")
        void failWhenMandatoryStepHasPendingResult() {
            List<StepResult> steps = Arrays.asList(
                    createStepResult(EvaluationResult.PASS.getValue(), 1),
                    createStepResult(EvaluationResult.PENDING.getValue(), 1)
            );

            String result = evaluationService.evaluateExecution(steps);

            assertThat(result).isEqualTo(EvaluationResult.FAIL.getValue());
        }

        @Test
        @DisplayName("Mixed mandatory and optional steps - only mandatory affect result")
        void mixedMandatoryAndOptionalSteps() {
            List<StepResult> steps = Arrays.asList(
                    createStepResult(EvaluationResult.PASS.getValue(), 1),   // mandatory, pass
                    createStepResult(EvaluationResult.PASS.getValue(), 1),   // mandatory, pass
                    createStepResult(EvaluationResult.FAIL.getValue(), 0),   // optional, fail
                    createStepResult(EvaluationResult.PENDING.getValue(), 0) // optional, pending
            );

            String result = evaluationService.evaluateExecution(steps);

            assertThat(result).isEqualTo(EvaluationResult.PASS.getValue());
        }
    }
}
