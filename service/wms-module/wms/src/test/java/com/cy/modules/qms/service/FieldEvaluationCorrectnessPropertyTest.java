package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.FieldValue;
import com.cy.modules.qms.entity.enums.EvaluationResult;
import com.cy.modules.qms.service.impl.EvaluationServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for Field evaluation correctness.
 *
 * **Validates: Requirements 7.1, 7.2**
 *
 * Property 13: Field evaluation correctness.
 * For any measurement field value V with configured tolerance [lower, upper],
 * the evaluation SHALL return PASS if and only if lower ≤ V ≤ upper.
 * For any number field value V with configured range [min, max],
 * the evaluation SHALL return PASS if and only if min ≤ V ≤ max.
 */
class FieldEvaluationCorrectnessPropertyTest {

    private final EvaluationServiceImpl evaluationService = new EvaluationServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== Helper methods ====================

    private FieldValue createMeasurementFieldValue(double actualValue, double lowerTolerance, double upperTolerance) {
        FieldValue fv = new FieldValue();
        fv.setId("fv-prop-test");
        fv.setFieldName("Measurement Field");
        fv.setFieldType("measurement");
        fv.setActualValue(String.valueOf(actualValue));
        fv.setIsRequired(1);

        Map<String, Object> config = new HashMap<>();
        config.put("lowerTolerance", lowerTolerance);
        config.put("upperTolerance", upperTolerance);
        config.put("nominalValue", (lowerTolerance + upperTolerance) / 2.0);

        try {
            fv.setFieldConfig(objectMapper.writeValueAsString(config));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return fv;
    }

    private FieldValue createNumberFieldValue(double actualValue, double minValue, double maxValue) {
        FieldValue fv = new FieldValue();
        fv.setId("fv-prop-test");
        fv.setFieldName("Number Field");
        fv.setFieldType("number");
        fv.setActualValue(String.valueOf(actualValue));
        fv.setIsRequired(1);

        Map<String, Object> config = new HashMap<>();
        config.put("minValue", minValue);
        config.put("maxValue", maxValue);
        config.put("decimalPlaces", 2);

        try {
            fv.setFieldConfig(objectMapper.writeValueAsString(config));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return fv;
    }

    // ==================== Measurement field property tests ====================

    /**
     * Property 13a: Measurement field PASS when value is within tolerance [lower, upper].
     *
     * For any value V where lower ≤ V ≤ upper, evaluation SHALL return PASS.
     *
     * **Validates: Requirements 7.1**
     */
    @Property(tries = 500)
    void measurementFieldPassesWhenValueWithinTolerance(
            @ForAll @DoubleRange(min = -1000.0, max = 1000.0) double lower,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double rangeSize,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double fraction) {

        double upper = lower + rangeSize;
        // Generate value within [lower, upper] using linear interpolation
        double value = lower + fraction * rangeSize;

        FieldValue fv = createMeasurementFieldValue(value, lower, upper);
        evaluationService.evaluateField(fv);

        assertThat(fv.getResult())
                .as("Measurement value %f within tolerance [%f, %f] should PASS", value, lower, upper)
                .isEqualTo(EvaluationResult.PASS.getValue());
    }

    /**
     * Property 13b: Measurement field FAIL when value is below lower tolerance.
     *
     * For any value V where V < lower, evaluation SHALL return FAIL.
     *
     * **Validates: Requirements 7.1**
     */
    @Property(tries = 500)
    void measurementFieldFailsWhenValueBelowLower(
            @ForAll @DoubleRange(min = -500.0, max = 500.0) double lower,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double rangeSize,
            @ForAll @DoubleRange(min = 0.01, max = 500.0) double belowOffset) {

        double upper = lower + rangeSize;
        double value = lower - belowOffset; // value < lower guaranteed

        FieldValue fv = createMeasurementFieldValue(value, lower, upper);
        evaluationService.evaluateField(fv);

        assertThat(fv.getResult())
                .as("Measurement value %f below lower tolerance %f should FAIL", value, lower)
                .isEqualTo(EvaluationResult.FAIL.getValue());
    }

    /**
     * Property 13c: Measurement field FAIL when value is above upper tolerance.
     *
     * For any value V where V > upper, evaluation SHALL return FAIL.
     *
     * **Validates: Requirements 7.1**
     */
    @Property(tries = 500)
    void measurementFieldFailsWhenValueAboveUpper(
            @ForAll @DoubleRange(min = -500.0, max = 500.0) double lower,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double rangeSize,
            @ForAll @DoubleRange(min = 0.01, max = 500.0) double aboveOffset) {

        double upper = lower + rangeSize;
        double value = upper + aboveOffset; // value > upper guaranteed

        FieldValue fv = createMeasurementFieldValue(value, lower, upper);
        evaluationService.evaluateField(fv);

        assertThat(fv.getResult())
                .as("Measurement value %f above upper tolerance %f should FAIL", value, upper)
                .isEqualTo(EvaluationResult.FAIL.getValue());
    }

    // ==================== Number field property tests ====================

    /**
     * Property 13d: Number field PASS when value is within range [min, max].
     *
     * For any value V where min ≤ V ≤ max, evaluation SHALL return PASS.
     *
     * **Validates: Requirements 7.2**
     */
    @Property(tries = 500)
    void numberFieldPassesWhenValueWithinRange(
            @ForAll @DoubleRange(min = -1000.0, max = 1000.0) double min,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double rangeSize,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double fraction) {

        double max = min + rangeSize;
        // Generate value within [min, max] using linear interpolation
        double value = min + fraction * rangeSize;

        FieldValue fv = createNumberFieldValue(value, min, max);
        evaluationService.evaluateField(fv);

        assertThat(fv.getResult())
                .as("Number value %f within range [%f, %f] should PASS", value, min, max)
                .isEqualTo(EvaluationResult.PASS.getValue());
    }

    /**
     * Property 13e: Number field FAIL when value is below min.
     *
     * For any value V where V < min, evaluation SHALL return FAIL.
     *
     * **Validates: Requirements 7.2**
     */
    @Property(tries = 500)
    void numberFieldFailsWhenValueBelowMin(
            @ForAll @DoubleRange(min = -500.0, max = 500.0) double min,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double rangeSize,
            @ForAll @DoubleRange(min = 0.01, max = 500.0) double belowOffset) {

        double max = min + rangeSize;
        double value = min - belowOffset; // value < min guaranteed

        FieldValue fv = createNumberFieldValue(value, min, max);
        evaluationService.evaluateField(fv);

        assertThat(fv.getResult())
                .as("Number value %f below min %f should FAIL", value, min)
                .isEqualTo(EvaluationResult.FAIL.getValue());
    }

    /**
     * Property 13f: Number field FAIL when value is above max.
     *
     * For any value V where V > max, evaluation SHALL return FAIL.
     *
     * **Validates: Requirements 7.2**
     */
    @Property(tries = 500)
    void numberFieldFailsWhenValueAboveMax(
            @ForAll @DoubleRange(min = -500.0, max = 500.0) double min,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double rangeSize,
            @ForAll @DoubleRange(min = 0.01, max = 500.0) double aboveOffset) {

        double max = min + rangeSize;
        double value = max + aboveOffset; // value > max guaranteed

        FieldValue fv = createNumberFieldValue(value, min, max);
        evaluationService.evaluateField(fv);

        assertThat(fv.getResult())
                .as("Number value %f above max %f should FAIL", value, max)
                .isEqualTo(EvaluationResult.FAIL.getValue());
    }
}
