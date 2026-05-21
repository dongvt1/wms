package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.FieldValue;
import com.cy.modules.qms.entity.enums.EvaluationResult;
import com.cy.modules.qms.service.impl.EvaluationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EvaluationService field-level evaluation logic.
 *
 * Validates: Requirements 7.1, 7.2, 7.3
 */
class EvaluationServiceTest {

    private EvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new EvaluationServiceImpl();
    }

    // ==================== Measurement Field Tests ====================

    @Nested
    @DisplayName("Measurement field evaluation")
    class MeasurementFieldTests {

        @Test
        @DisplayName("PASS when value is within tolerance range")
        void passWhenValueWithinTolerance() {
            FieldValue fv = createFieldValue("measurement",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 5.5, \"lowerTolerance\": 4.5}",
                    "5.0");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
            assertThat(fv.getEvalMessage()).contains("Trong dung sai");
            assertThat(fv.getEvalMessage()).contains("4.5");
            assertThat(fv.getEvalMessage()).contains("5.5");
        }

        @Test
        @DisplayName("PASS when value equals lower tolerance (boundary)")
        void passWhenValueEqualsLowerTolerance() {
            FieldValue fv = createFieldValue("measurement",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 5.5, \"lowerTolerance\": 4.5}",
                    "4.5");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("PASS when value equals upper tolerance (boundary)")
        void passWhenValueEqualsUpperTolerance() {
            FieldValue fv = createFieldValue("measurement",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 5.5, \"lowerTolerance\": 4.5}",
                    "5.5");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("FAIL when value is below lower tolerance")
        void failWhenValueBelowLowerTolerance() {
            FieldValue fv = createFieldValue("measurement",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 5.5, \"lowerTolerance\": 4.5}",
                    "4.4");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.FAIL.getValue());
            assertThat(fv.getEvalMessage()).contains("Ngoài dung sai");
        }

        @Test
        @DisplayName("FAIL when value is above upper tolerance")
        void failWhenValueAboveUpperTolerance() {
            FieldValue fv = createFieldValue("measurement",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 5.5, \"lowerTolerance\": 4.5}",
                    "5.6");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.FAIL.getValue());
            assertThat(fv.getEvalMessage()).contains("Ngoài dung sai");
        }

        @Test
        @DisplayName("FAIL when value is not a valid number")
        void failWhenValueNotNumeric() {
            FieldValue fv = createFieldValue("measurement",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 5.5, \"lowerTolerance\": 4.5}",
                    "abc");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.FAIL.getValue());
            assertThat(fv.getEvalMessage()).contains("không hợp lệ");
        }

        @Test
        @DisplayName("NA when fieldConfig is missing")
        void naWhenConfigMissing() {
            FieldValue fv = createFieldValue("measurement", null, "5.0");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.NA.getValue());
        }
    }

    // ==================== Number Field Tests ====================

    @Nested
    @DisplayName("Number field evaluation")
    class NumberFieldTests {

        @Test
        @DisplayName("PASS when value is within min/max range")
        void passWhenValueWithinRange() {
            FieldValue fv = createFieldValue("number",
                    "{\"minValue\": 0, \"maxValue\": 100, \"decimalPlaces\": 2}",
                    "50");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
            assertThat(fv.getEvalMessage()).contains("Trong khoảng");
        }

        @Test
        @DisplayName("PASS when value equals min (boundary)")
        void passWhenValueEqualsMin() {
            FieldValue fv = createFieldValue("number",
                    "{\"minValue\": 0, \"maxValue\": 100}",
                    "0");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("PASS when value equals max (boundary)")
        void passWhenValueEqualsMax() {
            FieldValue fv = createFieldValue("number",
                    "{\"minValue\": 0, \"maxValue\": 100}",
                    "100");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("FAIL when value is below min")
        void failWhenValueBelowMin() {
            FieldValue fv = createFieldValue("number",
                    "{\"minValue\": 0, \"maxValue\": 100}",
                    "-1");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.FAIL.getValue());
            assertThat(fv.getEvalMessage()).contains("Ngoài khoảng");
        }

        @Test
        @DisplayName("FAIL when value is above max")
        void failWhenValueAboveMax() {
            FieldValue fv = createFieldValue("number",
                    "{\"minValue\": 0, \"maxValue\": 100}",
                    "101");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.FAIL.getValue());
        }

        @Test
        @DisplayName("PASS when no config (no constraints)")
        void passWhenNoConfig() {
            FieldValue fv = createFieldValue("number", null, "999");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("PASS when only min is set and value is above min")
        void passWhenOnlyMinSetAndValueAbove() {
            FieldValue fv = createFieldValue("number",
                    "{\"minValue\": 10}",
                    "15");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("FAIL when only min is set and value is below min")
        void failWhenOnlyMinSetAndValueBelow() {
            FieldValue fv = createFieldValue("number",
                    "{\"minValue\": 10}",
                    "5");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.FAIL.getValue());
        }
    }

    // ==================== Boolean Field Tests ====================

    @Nested
    @DisplayName("Boolean field evaluation")
    class BooleanFieldTests {

        @Test
        @DisplayName("PASS when value is true")
        void passWhenTrue() {
            FieldValue fv = createFieldValue("boolean",
                    "{\"trueLabel\": \"Đạt\", \"falseLabel\": \"Không đạt\"}",
                    "true");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
            assertThat(fv.getEvalMessage()).isEqualTo("Đạt");
        }

        @Test
        @DisplayName("PASS when value is 1")
        void passWhenOne() {
            FieldValue fv = createFieldValue("boolean", null, "1");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("FAIL when value is false")
        void failWhenFalse() {
            FieldValue fv = createFieldValue("boolean",
                    "{\"trueLabel\": \"Đạt\", \"falseLabel\": \"Không đạt\"}",
                    "false");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.FAIL.getValue());
            assertThat(fv.getEvalMessage()).isEqualTo("Không đạt");
        }

        @Test
        @DisplayName("FAIL when value is 0")
        void failWhenZero() {
            FieldValue fv = createFieldValue("boolean", null, "0");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.FAIL.getValue());
        }

        @Test
        @DisplayName("NA when value is invalid boolean")
        void naWhenInvalidBoolean() {
            FieldValue fv = createFieldValue("boolean", null, "maybe");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.NA.getValue());
        }
    }

    // ==================== Text/Select Field Tests ====================

    @Nested
    @DisplayName("Text and Select field evaluation")
    class TextSelectFieldTests {

        @Test
        @DisplayName("Text field always PASS")
        void textFieldAlwaysPass() {
            FieldValue fv = createFieldValue("text", null, "any text value");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
        }

        @Test
        @DisplayName("Select field always PASS")
        void selectFieldAlwaysPass() {
            FieldValue fv = createFieldValue("select",
                    "{\"options\": [\"Tốt\", \"Trung bình\", \"Kém\"]}",
                    "Tốt");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.PASS.getValue());
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("NA when actualValue is null")
        void naWhenActualValueNull() {
            FieldValue fv = createFieldValue("measurement",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 5.5, \"lowerTolerance\": 4.5}",
                    null);

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.NA.getValue());
            assertThat(fv.getEvalMessage()).contains("Chưa nhập giá trị");
        }

        @Test
        @DisplayName("NA when actualValue is empty")
        void naWhenActualValueEmpty() {
            FieldValue fv = createFieldValue("number",
                    "{\"minValue\": 0, \"maxValue\": 100}",
                    "");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.NA.getValue());
        }

        @Test
        @DisplayName("Does nothing when fieldValue is null")
        void doesNothingWhenFieldValueNull() {
            evaluationService.evaluateField(null);
            // No exception thrown
        }

        @Test
        @DisplayName("NA when fieldType is null")
        void naWhenFieldTypeNull() {
            FieldValue fv = createFieldValue(null,
                    "{\"minValue\": 0, \"maxValue\": 100}",
                    "50");

            evaluationService.evaluateField(fv);

            assertThat(fv.getResult()).isEqualTo(EvaluationResult.NA.getValue());
        }
    }

    // ==================== Helper Methods ====================

    private FieldValue createFieldValue(String fieldType, String fieldConfig, String actualValue) {
        FieldValue fv = new FieldValue();
        fv.setId("fv-test-001");
        fv.setFieldName("Test Field");
        fv.setFieldType(fieldType);
        fv.setFieldConfig(fieldConfig);
        fv.setActualValue(actualValue);
        fv.setIsRequired(1);
        return fv;
    }
}
