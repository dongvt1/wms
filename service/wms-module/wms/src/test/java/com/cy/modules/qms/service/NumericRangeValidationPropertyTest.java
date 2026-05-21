package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.InspectionStep;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.StepField;
import com.cy.modules.qms.service.impl.TemplateValidationServiceImpl;
import com.cy.modules.qms.vo.ValidationErrorVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for Numeric range validation correctness.
 *
 * **Validates: Requirements 4.2, 4.3**
 *
 * Property 10: Numeric range validation correctness.
 * For any number field where min_value > max_value, OR any measurement field where
 * lower_tolerance ≥ nominal_value OR nominal_value ≥ upper_tolerance, the validation
 * SHALL reject the configuration. Conversely, for any valid configuration
 * (min ≤ max, lower < nominal < upper), validation SHALL accept it.
 */
class NumericRangeValidationPropertyTest {

    private final TemplateValidationServiceImpl validationService = new TemplateValidationServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ========== Helper methods ==========

    private InspectionTemplate createTemplate() {
        InspectionTemplate template = new InspectionTemplate();
        template.setId("tpl-test");
        template.setTemplateName("Test Template");
        template.setStageType("pqc");
        template.setStatus("draft");
        return template;
    }

    private InspectionStep createMandatoryStep() {
        InspectionStep step = new InspectionStep();
        step.setId("step-test");
        step.setTemplateId("tpl-test");
        step.setStepName("Test Step");
        step.setSortOrder(1);
        step.setIsMandatory(1);
        return step;
    }

    private StepField createNumberField(Double minValue, Double maxValue) {
        StepField field = new StepField();
        field.setId("field-number");
        field.setStepId("step-test");
        field.setFieldName("Test Number Field");
        field.setFieldCode("test_number");
        field.setFieldType("number");
        field.setIsRequired(1);
        field.setSortOrder(1);

        Map<String, Object> config = new HashMap<>();
        if (minValue != null) {
            config.put("minValue", minValue);
        }
        if (maxValue != null) {
            config.put("maxValue", maxValue);
        }
        config.put("decimalPlaces", 2);

        try {
            field.setFieldConfig(objectMapper.writeValueAsString(config));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return field;
    }

    private StepField createMeasurementField(Double nominalValue, Double upperTolerance, Double lowerTolerance) {
        StepField field = new StepField();
        field.setId("field-measurement");
        field.setStepId("step-test");
        field.setFieldName("Test Measurement Field");
        field.setFieldCode("test_measurement");
        field.setFieldType("measurement");
        field.setUnit("mm");
        field.setIsRequired(1);
        field.setSortOrder(1);

        Map<String, Object> config = new HashMap<>();
        if (nominalValue != null) {
            config.put("nominalValue", nominalValue);
        }
        if (upperTolerance != null) {
            config.put("upperTolerance", upperTolerance);
        }
        if (lowerTolerance != null) {
            config.put("lowerTolerance", lowerTolerance);
        }

        try {
            field.setFieldConfig(objectMapper.writeValueAsString(config));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return field;
    }

    private List<ValidationErrorVO.ValidationErrorItem> validate(StepField field) {
        InspectionTemplate template = createTemplate();
        InspectionStep step = createMandatoryStep();
        List<InspectionStep> steps = Collections.singletonList(step);
        List<List<StepField>> fieldsByStep = Collections.singletonList(Collections.singletonList(field));
        return validationService.validateForActivation(template, steps, fieldsByStep);
    }

    // ========== Property tests for Number field (min ≤ max) ==========

    /**
     * Property 10a: Number field with min > max SHALL be rejected.
     *
     * For any number field where min_value > max_value, validation SHALL reject.
     *
     * **Validates: Requirements 4.2**
     */
    @Property(tries = 200)
    void numberFieldWithMinGreaterThanMaxIsRejected(
            @ForAll @DoubleRange(min = -1000.0, max = 1000.0) double baseValue,
            @ForAll @DoubleRange(min = 0.01, max = 500.0) double offset) {

        double minValue = baseValue + offset; // min > max guaranteed
        double maxValue = baseValue;

        StepField field = createNumberField(minValue, maxValue);
        List<ValidationErrorVO.ValidationErrorItem> errors = validate(field);

        assertThat(errors)
                .as("Number field with min(%f) > max(%f) should produce validation error", minValue, maxValue)
                .isNotEmpty();

        assertThat(errors).anyMatch(e ->
                e.getMessage().contains("tối thiểu") || e.getMessage().contains("min"));
    }

    /**
     * Property 10b: Number field with min ≤ max SHALL be accepted.
     *
     * For any number field where min_value ≤ max_value, validation SHALL accept.
     *
     * **Validates: Requirements 4.2**
     */
    @Property(tries = 200)
    void numberFieldWithMinLessOrEqualMaxIsAccepted(
            @ForAll @DoubleRange(min = -1000.0, max = 1000.0) double baseValue,
            @ForAll @DoubleRange(min = 0.0, max = 500.0) double offset) {

        double minValue = baseValue;
        double maxValue = baseValue + offset; // max ≥ min guaranteed

        StepField field = createNumberField(minValue, maxValue);
        List<ValidationErrorVO.ValidationErrorItem> errors = validate(field);

        assertThat(errors)
                .as("Number field with min(%f) ≤ max(%f) should produce no validation errors", minValue, maxValue)
                .isEmpty();
    }

    // ========== Property tests for Measurement field (lower < nominal < upper) ==========

    /**
     * Property 10c: Measurement field with lower ≥ nominal SHALL be rejected.
     *
     * For any measurement field where lower_tolerance ≥ nominal_value,
     * validation SHALL reject.
     *
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 200)
    void measurementFieldWithLowerGreaterOrEqualNominalIsRejected(
            @ForAll @DoubleRange(min = -500.0, max = 500.0) double nominal,
            @ForAll @DoubleRange(min = 0.0, max = 200.0) double lowerOffset,
            @ForAll @DoubleRange(min = 0.01, max = 200.0) double upperOffset) {

        double lowerTolerance = nominal + lowerOffset; // lower ≥ nominal guaranteed
        double upperTolerance = nominal + lowerOffset + upperOffset + 1.0; // upper > lower to isolate the error

        StepField field = createMeasurementField(nominal, upperTolerance, lowerTolerance);
        List<ValidationErrorVO.ValidationErrorItem> errors = validate(field);

        assertThat(errors)
                .as("Measurement field with lower(%f) ≥ nominal(%f) should produce validation error",
                        lowerTolerance, nominal)
                .isNotEmpty();

        assertThat(errors).anyMatch(e ->
                e.getMessage().contains("Giới hạn dưới") || e.getMessage().contains("lower"));
    }

    /**
     * Property 10d: Measurement field with nominal ≥ upper SHALL be rejected.
     *
     * For any measurement field where nominal_value ≥ upper_tolerance,
     * validation SHALL reject.
     *
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 200)
    void measurementFieldWithNominalGreaterOrEqualUpperIsRejected(
            @ForAll @DoubleRange(min = -500.0, max = 500.0) double nominal,
            @ForAll @DoubleRange(min = 0.01, max = 200.0) double lowerOffset,
            @ForAll @DoubleRange(min = 0.0, max = 200.0) double upperOffset) {

        double lowerTolerance = nominal - lowerOffset - 1.0; // lower < nominal to isolate the error
        double upperTolerance = nominal - upperOffset; // upper ≤ nominal guaranteed

        StepField field = createMeasurementField(nominal, upperTolerance, lowerTolerance);
        List<ValidationErrorVO.ValidationErrorItem> errors = validate(field);

        assertThat(errors)
                .as("Measurement field with nominal(%f) ≥ upper(%f) should produce validation error",
                        nominal, upperTolerance)
                .isNotEmpty();

        assertThat(errors).anyMatch(e ->
                e.getMessage().contains("danh nghĩa") || e.getMessage().contains("nominal"));
    }

    /**
     * Property 10e: Measurement field with lower < nominal < upper SHALL be accepted.
     *
     * For any measurement field where lower_tolerance < nominal_value < upper_tolerance,
     * validation SHALL accept.
     *
     * **Validates: Requirements 4.3**
     */
    @Property(tries = 200)
    void measurementFieldWithValidTolerancesIsAccepted(
            @ForAll @DoubleRange(min = -500.0, max = 500.0) double nominal,
            @ForAll @DoubleRange(min = 0.01, max = 200.0) double lowerOffset,
            @ForAll @DoubleRange(min = 0.01, max = 200.0) double upperOffset) {

        double lowerTolerance = nominal - lowerOffset; // lower < nominal guaranteed
        double upperTolerance = nominal + upperOffset; // upper > nominal guaranteed

        StepField field = createMeasurementField(nominal, upperTolerance, lowerTolerance);
        List<ValidationErrorVO.ValidationErrorItem> errors = validate(field);

        assertThat(errors)
                .as("Measurement field with lower(%f) < nominal(%f) < upper(%f) should produce no errors",
                        lowerTolerance, nominal, upperTolerance)
                .isEmpty();
    }
}
