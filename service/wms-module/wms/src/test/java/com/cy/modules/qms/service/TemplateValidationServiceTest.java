package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.InspectionStep;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.StepField;
import com.cy.modules.qms.service.impl.TemplateValidationServiceImpl;
import com.cy.modules.qms.vo.ValidationErrorVO;
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
 * Unit tests for TemplateValidationServiceImpl.
 * Validates Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6
 */
class TemplateValidationServiceTest {

    private TemplateValidationService validationService;
    private InspectionTemplate template;

    @BeforeEach
    void setUp() {
        validationService = new TemplateValidationServiceImpl();
        template = new InspectionTemplate();
        template.setId("tpl-001");
        template.setTemplateName("Test Template");
    }

    // --- Helper methods ---

    private InspectionStep createStep(String id, String name, int isMandatory) {
        InspectionStep step = new InspectionStep();
        step.setId(id);
        step.setStepName(name);
        step.setIsMandatory(isMandatory);
        step.setSortOrder(1);
        return step;
    }

    private StepField createTextField(String id, String name) {
        StepField field = new StepField();
        field.setId(id);
        field.setFieldName(name);
        field.setFieldType("text");
        return field;
    }

    private StepField createNumberField(String id, String name, String config) {
        StepField field = new StepField();
        field.setId(id);
        field.setFieldName(name);
        field.setFieldType("number");
        field.setFieldConfig(config);
        return field;
    }

    private StepField createMeasurementField(String id, String name, String config) {
        StepField field = new StepField();
        field.setId(id);
        field.setFieldName(name);
        field.setFieldType("measurement");
        field.setFieldConfig(config);
        return field;
    }

    private StepField createSelectField(String id, String name, String config) {
        StepField field = new StepField();
        field.setId(id);
        field.setFieldName(name);
        field.setFieldType("select");
        field.setFieldConfig(config);
        return field;
    }

    // --- Tests ---

    @Nested
    @DisplayName("Rule: Template phải có ≥ 1 step")
    class TemplateStepValidation {

        @Test
        @DisplayName("Template không có step → lỗi")
        void templateWithNoSteps_returnsError() {
            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(template, Collections.emptyList(), Collections.emptyList());

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("ít nhất một bước kiểm tra");
        }

        @Test
        @DisplayName("Template có null steps → lỗi")
        void templateWithNullSteps_returnsError() {
            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(template, null, null);

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("ít nhất một bước kiểm tra");
        }

        @Test
        @DisplayName("Template có ≥ 1 step hợp lệ → không lỗi")
        void templateWithValidStep_noErrors() {
            InspectionStep step = createStep("s1", "Step 1", 1);
            StepField field = createTextField("f1", "Field 1");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).isEmpty();
        }
    }

    @Nested
    @DisplayName("Rule: Mỗi mandatory step phải có ≥ 1 field")
    class MandatoryStepFieldValidation {

        @Test
        @DisplayName("Mandatory step không có field → lỗi")
        void mandatoryStepWithNoFields_returnsError() {
            InspectionStep step = createStep("s1", "Bước bắt buộc", 1);

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.emptyList())
                    );

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("Bước bắt buộc");
            assertThat(errors.get(0).getMessage()).contains("ít nhất một trường");
        }

        @Test
        @DisplayName("Optional step không có field → không lỗi")
        void optionalStepWithNoFields_noErrors() {
            InspectionStep step = createStep("s1", "Bước tùy chọn", 0);

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.emptyList())
                    );

            assertThat(errors).isEmpty();
        }
    }

    @Nested
    @DisplayName("Rule: Number field min_value ≤ max_value")
    class NumberFieldValidation {

        @Test
        @DisplayName("min > max → lỗi")
        void minGreaterThanMax_returnsError() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createNumberField("f1", "Số lượng",
                    "{\"minValue\": 100, \"maxValue\": 50, \"decimalPlaces\": 2}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("nhỏ hơn hoặc bằng");
        }

        @Test
        @DisplayName("min = max → hợp lệ")
        void minEqualsMax_noErrors() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createNumberField("f1", "Số lượng",
                    "{\"minValue\": 50, \"maxValue\": 50, \"decimalPlaces\": 0}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("min < max → hợp lệ")
        void minLessThanMax_noErrors() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createNumberField("f1", "Số lượng",
                    "{\"minValue\": 0, \"maxValue\": 100, \"decimalPlaces\": 2}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("Number field không có config → không lỗi (config optional)")
        void numberFieldWithNoConfig_noErrors() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createNumberField("f1", "Số lượng", null);

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).isEmpty();
        }
    }

    @Nested
    @DisplayName("Rule: Measurement field lower < nominal < upper")
    class MeasurementFieldValidation {

        @Test
        @DisplayName("lower < nominal < upper → hợp lệ")
        void validTolerance_noErrors() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createMeasurementField("f1", "Độ dày",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 5.5, \"lowerTolerance\": 4.5}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("lower >= nominal → lỗi")
        void lowerGreaterThanOrEqualNominal_returnsError() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createMeasurementField("f1", "Độ dày",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 6.0, \"lowerTolerance\": 5.0}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("Giới hạn dưới");
            assertThat(errors.get(0).getMessage()).contains("nhỏ hơn giá trị danh nghĩa");
        }

        @Test
        @DisplayName("nominal >= upper → lỗi")
        void nominalGreaterThanOrEqualUpper_returnsError() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createMeasurementField("f1", "Độ dày",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 5.0, \"lowerTolerance\": 4.0}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("Giá trị danh nghĩa");
            assertThat(errors.get(0).getMessage()).contains("nhỏ hơn giới hạn trên");
        }

        @Test
        @DisplayName("lower > nominal AND nominal > upper → 2 lỗi")
        void bothInvalid_returnsTwoErrors() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createMeasurementField("f1", "Độ dày",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 4.0, \"lowerTolerance\": 6.0}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).hasSize(2);
        }

        @Test
        @DisplayName("Measurement field không có config → lỗi")
        void measurementFieldWithNoConfig_returnsError() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createMeasurementField("f1", "Độ dày", null);

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("cấu hình dung sai");
        }

        @Test
        @DisplayName("Measurement field thiếu trường → lỗi")
        void measurementFieldMissingValues_returnsError() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createMeasurementField("f1", "Độ dày",
                    "{\"nominalValue\": 5.0}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("đầy đủ");
        }
    }

    @Nested
    @DisplayName("Rule: Select field options hợp lệ, ≥ 1 mục")
    class SelectFieldValidation {

        @Test
        @DisplayName("Options hợp lệ với ≥ 1 mục → không lỗi")
        void validOptions_noErrors() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createSelectField("f1", "Tình trạng",
                    "{\"options\": [\"Tốt\", \"Trung bình\", \"Kém\"]}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("Options rỗng → lỗi")
        void emptyOptions_returnsError() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createSelectField("f1", "Tình trạng",
                    "{\"options\": []}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("ít nhất một tùy chọn");
        }

        @Test
        @DisplayName("Không có options key → lỗi")
        void missingOptionsKey_returnsError() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createSelectField("f1", "Tình trạng",
                    "{\"someOtherKey\": \"value\"}");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("danh sách tùy chọn");
        }

        @Test
        @DisplayName("JSON không hợp lệ → lỗi")
        void invalidJson_returnsError() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createSelectField("f1", "Tình trạng",
                    "not valid json {{{");

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("JSON hợp lệ");
        }

        @Test
        @DisplayName("Select field không có config → lỗi")
        void selectFieldWithNoConfig_returnsError() {
            InspectionStep step = createStep("s1", "Step 1", 0);
            StepField field = createSelectField("f1", "Tình trạng", null);

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(
                            template,
                            Collections.singletonList(step),
                            Collections.singletonList(Collections.singletonList(field))
                    );

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).getMessage()).contains("cấu hình danh sách tùy chọn");
        }
    }

    @Nested
    @DisplayName("Rule: Trả về ALL errors (không dừng ở lỗi đầu tiên)")
    class CollectAllErrors {

        @Test
        @DisplayName("Template với nhiều lỗi → trả về tất cả")
        void multipleErrors_returnsAll() {
            // Step 1: mandatory, no fields → 1 error
            InspectionStep step1 = createStep("s1", "Bước 1", 1);

            // Step 2: has invalid number field AND invalid measurement field → 2 errors
            InspectionStep step2 = createStep("s2", "Bước 2", 0);
            StepField numberField = createNumberField("f1", "Số lượng",
                    "{\"minValue\": 100, \"maxValue\": 50}");
            StepField measurementField = createMeasurementField("f2", "Độ dày",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 4.0, \"lowerTolerance\": 6.0}");

            // Step 3: has invalid select field → 1 error
            InspectionStep step3 = createStep("s3", "Bước 3", 0);
            StepField selectField = createSelectField("f3", "Loại",
                    "{\"options\": []}");

            List<InspectionStep> steps = Arrays.asList(step1, step2, step3);
            List<List<StepField>> fieldsByStep = Arrays.asList(
                    Collections.emptyList(),
                    Arrays.asList(numberField, measurementField),
                    Collections.singletonList(selectField)
            );

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(template, steps, fieldsByStep);

            // Expected: 1 (mandatory step no fields) + 1 (number min>max) + 2 (measurement both invalid) + 1 (select empty) = 5
            assertThat(errors).hasSize(5);
        }

        @Test
        @DisplayName("Template hoàn toàn hợp lệ → không lỗi")
        void fullyValidTemplate_noErrors() {
            InspectionStep step1 = createStep("s1", "Kiểm tra ngoại quan", 1);
            StepField textField = createTextField("f1", "Ghi chú");
            StepField numberField = createNumberField("f2", "Số lượng",
                    "{\"minValue\": 0, \"maxValue\": 100, \"decimalPlaces\": 2}");
            StepField measurementField = createMeasurementField("f3", "Độ dày",
                    "{\"nominalValue\": 5.0, \"upperTolerance\": 5.5, \"lowerTolerance\": 4.5}");
            StepField selectField = createSelectField("f4", "Tình trạng",
                    "{\"options\": [\"Tốt\", \"Trung bình\", \"Kém\"]}");

            List<InspectionStep> steps = Collections.singletonList(step1);
            List<List<StepField>> fieldsByStep = Collections.singletonList(
                    Arrays.asList(textField, numberField, measurementField, selectField)
            );

            List<ValidationErrorVO.ValidationErrorItem> errors =
                    validationService.validateForActivation(template, steps, fieldsByStep);

            assertThat(errors).isEmpty();
        }
    }
}
