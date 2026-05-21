package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.InspectionStep;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.StepField;
import com.cy.modules.qms.service.impl.TemplateValidationServiceImpl;
import com.cy.modules.qms.vo.ValidationErrorVO;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test for TemplateValidationService - Property 9.
 *
 * **Validates: Requirements 4.1, 4.4, 4.6**
 *
 * Property 9: Template activation validation reports all errors.
 * For any Inspection Template with multiple validation issues (missing steps,
 * missing fields in mandatory steps, invalid number ranges, invalid measurement
 * tolerances, invalid select options), the validation response SHALL contain
 * ALL errors rather than stopping at the first error found.
 */
class TemplateValidationAllErrorsPropertyTest {

    private final TemplateValidationService validationService = new TemplateValidationServiceImpl();

    /**
     * Property 9: Template activation validation reports all errors.
     *
     * Generate templates with 1-N intentional errors, verify that the validation
     * service reports ALL of them (error count matches injected error count).
     *
     * **Validates: Requirements 4.1, 4.4, 4.6**
     */
    @Property(tries = 200)
    void validationReportsAllInjectedErrors(
            @ForAll("templatesWithErrors") TemplateWithExpectedErrors templateWithErrors) {

        List<ValidationErrorVO.ValidationErrorItem> errors =
                validationService.validateForActivation(
                        templateWithErrors.template,
                        templateWithErrors.steps,
                        templateWithErrors.fieldsByStep
                );

        assertThat(errors)
                .as("Validation should report exactly %d errors for template with %d intentional errors. " +
                                "Error types injected: %s",
                        templateWithErrors.expectedErrorCount,
                        templateWithErrors.expectedErrorCount,
                        templateWithErrors.errorDescriptions)
                .hasSize(templateWithErrors.expectedErrorCount);
    }

    /**
     * Complementary property: A fully valid template produces zero errors.
     *
     * **Validates: Requirements 4.1, 4.4, 4.6**
     */
    @Property(tries = 100)
    void validTemplateProducesNoErrors(
            @ForAll("validTemplates") TemplateWithExpectedErrors validTemplate) {

        List<ValidationErrorVO.ValidationErrorItem> errors =
                validationService.validateForActivation(
                        validTemplate.template,
                        validTemplate.steps,
                        validTemplate.fieldsByStep
                );

        assertThat(errors)
                .as("A fully valid template should produce zero validation errors")
                .isEmpty();
    }

    // --- Arbitraries ---

    @Provide
    Arbitrary<TemplateWithExpectedErrors> templatesWithErrors() {
        return Combinators.combine(
                Arbitraries.integers().between(1, 5), // number of steps
                Arbitraries.integers().between(1, 4)  // error types to inject per step
        ).flatAs((stepCount, errorVariety) ->
                generateTemplateWithErrors(stepCount, errorVariety)
        );
    }

    @Provide
    Arbitrary<TemplateWithExpectedErrors> validTemplates() {
        return Arbitraries.integers().between(1, 5).flatMap(stepCount ->
                Arbitraries.integers().between(1, 4).map(fieldsPerStep ->
                        generateValidTemplate(stepCount, fieldsPerStep)
                )
        );
    }

    /**
     * Generates a template with a controlled number of intentional errors.
     * Each error type is independently injected so we can precisely predict
     * the expected error count.
     */
    private Arbitrary<TemplateWithExpectedErrors> generateTemplateWithErrors(int stepCount, int errorVariety) {
        // We use a combination of error injection strategies
        return Arbitraries.of(ErrorStrategy.values())
                .list().ofSize(stepCount)
                .map(strategies -> buildTemplateWithStrategies(strategies, stepCount));
    }

    private TemplateWithExpectedErrors buildTemplateWithStrategies(List<ErrorStrategy> strategies, int stepCount) {
        InspectionTemplate template = new InspectionTemplate();
        template.setId("tpl-test");
        template.setTemplateName("Test Template");

        List<InspectionStep> steps = new ArrayList<>();
        List<List<StepField>> fieldsByStep = new ArrayList<>();
        int expectedErrors = 0;
        List<String> errorDescriptions = new ArrayList<>();

        for (int i = 0; i < strategies.size(); i++) {
            ErrorStrategy strategy = strategies.get(i);
            InspectionStep step = new InspectionStep();
            step.setId("step-" + i);
            step.setStepName("Step " + i);
            step.setSortOrder(i + 1);

            List<StepField> fields = new ArrayList<>();

            switch (strategy) {
                case MANDATORY_STEP_NO_FIELDS:
                    // Mandatory step with no fields → 1 error
                    step.setIsMandatory(1);
                    expectedErrors += 1;
                    errorDescriptions.add("mandatory_step_no_fields[" + i + "]");
                    break;

                case NUMBER_FIELD_MIN_GT_MAX:
                    // Non-mandatory step with a number field where min > max → 1 error
                    step.setIsMandatory(0);
                    StepField numberField = new StepField();
                    numberField.setId("f-num-" + i);
                    numberField.setFieldName("Number " + i);
                    numberField.setFieldType("number");
                    numberField.setFieldConfig("{\"minValue\": 100, \"maxValue\": 50}");
                    fields.add(numberField);
                    expectedErrors += 1;
                    errorDescriptions.add("number_min_gt_max[" + i + "]");
                    break;

                case MEASUREMENT_LOWER_GE_NOMINAL:
                    // Measurement field where lower >= nominal → 1 error
                    step.setIsMandatory(0);
                    StepField measField1 = new StepField();
                    measField1.setId("f-meas-" + i);
                    measField1.setFieldName("Measurement " + i);
                    measField1.setFieldType("measurement");
                    measField1.setFieldConfig("{\"nominalValue\": 5.0, \"upperTolerance\": 6.0, \"lowerTolerance\": 5.5}");
                    fields.add(measField1);
                    expectedErrors += 1;
                    errorDescriptions.add("measurement_lower_ge_nominal[" + i + "]");
                    break;

                case MEASUREMENT_NOMINAL_GE_UPPER:
                    // Measurement field where nominal >= upper → 1 error
                    step.setIsMandatory(0);
                    StepField measField2 = new StepField();
                    measField2.setId("f-meas2-" + i);
                    measField2.setFieldName("Measurement " + i);
                    measField2.setFieldType("measurement");
                    measField2.setFieldConfig("{\"nominalValue\": 5.0, \"upperTolerance\": 4.0, \"lowerTolerance\": 3.0}");
                    fields.add(measField2);
                    expectedErrors += 1;
                    errorDescriptions.add("measurement_nominal_ge_upper[" + i + "]");
                    break;

                case MEASUREMENT_BOTH_INVALID:
                    // Measurement field where lower >= nominal AND nominal >= upper → 2 errors
                    step.setIsMandatory(0);
                    StepField measField3 = new StepField();
                    measField3.setId("f-meas3-" + i);
                    measField3.setFieldName("Measurement " + i);
                    measField3.setFieldType("measurement");
                    measField3.setFieldConfig("{\"nominalValue\": 5.0, \"upperTolerance\": 4.0, \"lowerTolerance\": 6.0}");
                    fields.add(measField3);
                    expectedErrors += 2;
                    errorDescriptions.add("measurement_both_invalid[" + i + "](2 errors)");
                    break;

                case SELECT_EMPTY_OPTIONS:
                    // Select field with empty options → 1 error
                    step.setIsMandatory(0);
                    StepField selectField = new StepField();
                    selectField.setId("f-sel-" + i);
                    selectField.setFieldName("Select " + i);
                    selectField.setFieldType("select");
                    selectField.setFieldConfig("{\"options\": []}");
                    fields.add(selectField);
                    expectedErrors += 1;
                    errorDescriptions.add("select_empty_options[" + i + "]");
                    break;

                case SELECT_NO_CONFIG:
                    // Select field with null config → 1 error
                    step.setIsMandatory(0);
                    StepField selectField2 = new StepField();
                    selectField2.setId("f-sel2-" + i);
                    selectField2.setFieldName("Select " + i);
                    selectField2.setFieldType("select");
                    selectField2.setFieldConfig(null);
                    fields.add(selectField2);
                    expectedErrors += 1;
                    errorDescriptions.add("select_no_config[" + i + "]");
                    break;

                case MEASUREMENT_NO_CONFIG:
                    // Measurement field with null config → 1 error
                    step.setIsMandatory(0);
                    StepField measField4 = new StepField();
                    measField4.setId("f-meas4-" + i);
                    measField4.setFieldName("Measurement " + i);
                    measField4.setFieldType("measurement");
                    measField4.setFieldConfig(null);
                    fields.add(measField4);
                    expectedErrors += 1;
                    errorDescriptions.add("measurement_no_config[" + i + "]");
                    break;

                case MULTIPLE_ERRORS_IN_STEP:
                    // Step with multiple invalid fields → multiple errors
                    step.setIsMandatory(0);
                    StepField numF = new StepField();
                    numF.setId("f-multi-num-" + i);
                    numF.setFieldName("Num " + i);
                    numF.setFieldType("number");
                    numF.setFieldConfig("{\"minValue\": 200, \"maxValue\": 10}");
                    fields.add(numF);

                    StepField selF = new StepField();
                    selF.setId("f-multi-sel-" + i);
                    selF.setFieldName("Sel " + i);
                    selF.setFieldType("select");
                    selF.setFieldConfig("{\"options\": []}");
                    fields.add(selF);

                    expectedErrors += 2; // 1 for number + 1 for select
                    errorDescriptions.add("multiple_errors_in_step[" + i + "](2 errors)");
                    break;
            }

            steps.add(step);
            fieldsByStep.add(fields);
        }

        // Ensure at least 1 error (if all strategies happened to produce valid configs, force one)
        if (expectedErrors == 0) {
            // This shouldn't happen given our strategies, but as a safety net
            // add a mandatory step with no fields
            InspectionStep extraStep = new InspectionStep();
            extraStep.setId("step-extra");
            extraStep.setStepName("Extra Mandatory");
            extraStep.setSortOrder(steps.size() + 1);
            extraStep.setIsMandatory(1);
            steps.add(extraStep);
            fieldsByStep.add(Collections.emptyList());
            expectedErrors += 1;
            errorDescriptions.add("fallback_mandatory_no_fields");
        }

        return new TemplateWithExpectedErrors(template, steps, fieldsByStep, expectedErrors, errorDescriptions);
    }

    private TemplateWithExpectedErrors generateValidTemplate(int stepCount, int fieldsPerStep) {
        InspectionTemplate template = new InspectionTemplate();
        template.setId("tpl-valid");
        template.setTemplateName("Valid Template");

        List<InspectionStep> steps = new ArrayList<>();
        List<List<StepField>> fieldsByStep = new ArrayList<>();

        for (int i = 0; i < stepCount; i++) {
            InspectionStep step = new InspectionStep();
            step.setId("step-v-" + i);
            step.setStepName("Valid Step " + i);
            step.setSortOrder(i + 1);
            step.setIsMandatory(1); // All mandatory, all with fields → valid

            List<StepField> fields = new ArrayList<>();
            for (int j = 0; j < fieldsPerStep; j++) {
                StepField field = new StepField();
                field.setId("f-v-" + i + "-" + j);
                field.setFieldName("Field " + i + "-" + j);

                // Cycle through valid field types
                switch (j % 4) {
                    case 0:
                        field.setFieldType("text");
                        break;
                    case 1:
                        field.setFieldType("number");
                        field.setFieldConfig("{\"minValue\": 0, \"maxValue\": 100, \"decimalPlaces\": 2}");
                        break;
                    case 2:
                        field.setFieldType("measurement");
                        field.setFieldConfig("{\"nominalValue\": 5.0, \"upperTolerance\": 5.5, \"lowerTolerance\": 4.5}");
                        break;
                    case 3:
                        field.setFieldType("select");
                        field.setFieldConfig("{\"options\": [\"A\", \"B\", \"C\"]}");
                        break;
                }
                fields.add(field);
            }

            steps.add(step);
            fieldsByStep.add(fields);
        }

        return new TemplateWithExpectedErrors(template, steps, fieldsByStep, 0, Collections.emptyList());
    }

    // --- Error injection strategies ---

    enum ErrorStrategy {
        MANDATORY_STEP_NO_FIELDS,
        NUMBER_FIELD_MIN_GT_MAX,
        MEASUREMENT_LOWER_GE_NOMINAL,
        MEASUREMENT_NOMINAL_GE_UPPER,
        MEASUREMENT_BOTH_INVALID,
        SELECT_EMPTY_OPTIONS,
        SELECT_NO_CONFIG,
        MEASUREMENT_NO_CONFIG,
        MULTIPLE_ERRORS_IN_STEP
    }

    // --- Data holder ---

    static class TemplateWithExpectedErrors {
        final InspectionTemplate template;
        final List<InspectionStep> steps;
        final List<List<StepField>> fieldsByStep;
        final int expectedErrorCount;
        final List<String> errorDescriptions;

        TemplateWithExpectedErrors(InspectionTemplate template,
                                   List<InspectionStep> steps,
                                   List<List<StepField>> fieldsByStep,
                                   int expectedErrorCount,
                                   List<String> errorDescriptions) {
            this.template = template;
            this.steps = steps;
            this.fieldsByStep = fieldsByStep;
            this.expectedErrorCount = expectedErrorCount;
            this.errorDescriptions = errorDescriptions;
        }

        @Override
        public String toString() {
            return "TemplateWithExpectedErrors{" +
                    "steps=" + steps.size() +
                    ", expectedErrors=" + expectedErrorCount +
                    ", errors=" + errorDescriptions +
                    '}';
        }
    }
}
