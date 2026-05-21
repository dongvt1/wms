package com.cy.modules.qms.service.impl;

import com.cy.modules.qms.entity.InspectionStep;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.StepField;
import com.cy.modules.qms.service.TemplateValidationService;
import com.cy.modules.qms.vo.ValidationErrorVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation validate toàn bộ cấu hình template trước khi kích hoạt.
 * Collect ALL errors (không dừng ở lỗi đầu tiên).
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Service
public class TemplateValidationServiceImpl implements TemplateValidationService {

    private static final String FIELD_TYPE_NUMBER = "number";
    private static final String FIELD_TYPE_MEASUREMENT = "measurement";
    private static final String FIELD_TYPE_SELECT = "select";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ValidationErrorVO.ValidationErrorItem> validateForActivation(
            InspectionTemplate template,
            List<InspectionStep> steps,
            List<List<StepField>> fieldsByStep) {

        List<ValidationErrorVO.ValidationErrorItem> errors = new ArrayList<>();

        // Rule 1: Template phải có ≥ 1 step
        if (steps == null || steps.isEmpty()) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    "steps",
                    "steps",
                    "Template phải có ít nhất một bước kiểm tra"
            ));
            return errors; // Không thể validate tiếp nếu không có step
        }

        // Validate từng step
        for (int i = 0; i < steps.size(); i++) {
            InspectionStep step = steps.get(i);
            List<StepField> fields = (fieldsByStep != null && i < fieldsByStep.size())
                    ? fieldsByStep.get(i) : null;

            validateStep(step, fields, i, errors);
        }

        return errors;
    }

    /**
     * Validate một step và các fields của nó.
     */
    private void validateStep(InspectionStep step, List<StepField> fields, int stepIndex,
                              List<ValidationErrorVO.ValidationErrorItem> errors) {

        String stepPath = "steps[" + stepIndex + "]";
        boolean isMandatory = step.getIsMandatory() != null && step.getIsMandatory() == 1;

        // Rule 2: Mỗi mandatory step phải có ≥ 1 field
        if (isMandatory && (fields == null || fields.isEmpty())) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    stepPath + ".fields",
                    step.getStepName(),
                    "Bước bắt buộc '" + step.getStepName() + "' phải có ít nhất một trường"
            ));
            return; // Không thể validate fields nếu không có
        }

        // Validate từng field
        if (fields != null) {
            for (int j = 0; j < fields.size(); j++) {
                StepField field = fields.get(j);
                validateField(field, stepIndex, j, errors);
            }
        }
    }

    /**
     * Validate một field dựa trên field_type và field_config.
     */
    private void validateField(StepField field, int stepIndex, int fieldIndex,
                               List<ValidationErrorVO.ValidationErrorItem> errors) {

        String fieldPath = "steps[" + stepIndex + "].fields[" + fieldIndex + "].fieldConfig";
        String fieldType = field.getFieldType();

        if (fieldType == null) {
            return; // Không validate nếu không có field type
        }

        switch (fieldType) {
            case FIELD_TYPE_NUMBER:
                validateNumberField(field, fieldPath, errors);
                break;
            case FIELD_TYPE_MEASUREMENT:
                validateMeasurementField(field, fieldPath, errors);
                break;
            case FIELD_TYPE_SELECT:
                validateSelectField(field, fieldPath, errors);
                break;
            default:
                // text, boolean: không cần validate field_config đặc biệt
                break;
        }
    }

    /**
     * Validate number field: min_value ≤ max_value
     */
    private void validateNumberField(StepField field, String fieldPath,
                                     List<ValidationErrorVO.ValidationErrorItem> errors) {

        Map<String, Object> config = parseFieldConfig(field.getFieldConfig());
        if (config == null) {
            // Không có config thì không validate (config là optional cho number)
            return;
        }

        Double minValue = getDoubleValue(config, "minValue");
        Double maxValue = getDoubleValue(config, "maxValue");

        if (minValue != null && maxValue != null && minValue > maxValue) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    fieldPath,
                    field.getFieldName(),
                    "Giá trị tối thiểu (" + minValue + ") phải nhỏ hơn hoặc bằng giá trị tối đa (" + maxValue + ")"
            ));
        }
    }

    /**
     * Validate measurement field: lower_tolerance < nominal_value < upper_tolerance
     */
    private void validateMeasurementField(StepField field, String fieldPath,
                                          List<ValidationErrorVO.ValidationErrorItem> errors) {

        Map<String, Object> config = parseFieldConfig(field.getFieldConfig());
        if (config == null) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    fieldPath,
                    field.getFieldName(),
                    "Trường measurement '" + field.getFieldName() + "' phải có cấu hình dung sai"
            ));
            return;
        }

        Double nominalValue = getDoubleValue(config, "nominalValue");
        Double upperTolerance = getDoubleValue(config, "upperTolerance");
        Double lowerTolerance = getDoubleValue(config, "lowerTolerance");

        if (nominalValue == null || upperTolerance == null || lowerTolerance == null) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    fieldPath,
                    field.getFieldName(),
                    "Trường measurement '" + field.getFieldName() + "' phải có đầy đủ: nominalValue, upperTolerance, lowerTolerance"
            ));
            return;
        }

        if (lowerTolerance >= nominalValue) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    fieldPath,
                    field.getFieldName(),
                    "Giới hạn dưới (" + lowerTolerance + ") phải nhỏ hơn giá trị danh nghĩa (" + nominalValue + ")"
            ));
        }

        if (nominalValue >= upperTolerance) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    fieldPath,
                    field.getFieldName(),
                    "Giá trị danh nghĩa (" + nominalValue + ") phải nhỏ hơn giới hạn trên (" + upperTolerance + ")"
            ));
        }
    }

    /**
     * Validate select field: options JSON hợp lệ, ≥ 1 mục
     */
    private void validateSelectField(StepField field, String fieldPath,
                                     List<ValidationErrorVO.ValidationErrorItem> errors) {

        String fieldConfig = field.getFieldConfig();
        if (fieldConfig == null || fieldConfig.trim().isEmpty()) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    fieldPath,
                    field.getFieldName(),
                    "Trường select '" + field.getFieldName() + "' phải có cấu hình danh sách tùy chọn"
            ));
            return;
        }

        Map<String, Object> config = parseFieldConfig(fieldConfig);
        if (config == null) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    fieldPath,
                    field.getFieldName(),
                    "Cấu hình trường select '" + field.getFieldName() + "' không phải JSON hợp lệ"
            ));
            return;
        }

        Object optionsObj = config.get("options");
        if (optionsObj == null) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    fieldPath,
                    field.getFieldName(),
                    "Trường select '" + field.getFieldName() + "' phải có danh sách tùy chọn (options)"
            ));
            return;
        }

        if (!(optionsObj instanceof List)) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    fieldPath,
                    field.getFieldName(),
                    "Danh sách tùy chọn của trường '" + field.getFieldName() + "' phải là mảng JSON"
            ));
            return;
        }

        List<?> options = (List<?>) optionsObj;
        if (options.isEmpty()) {
            errors.add(new ValidationErrorVO.ValidationErrorItem(
                    fieldPath,
                    field.getFieldName(),
                    "Trường select '" + field.getFieldName() + "' phải có ít nhất một tùy chọn"
            ));
        }
    }

    /**
     * Parse field_config JSON string thành Map.
     * Trả về null nếu JSON không hợp lệ hoặc input null/empty.
     */
    private Map<String, Object> parseFieldConfig(String fieldConfig) {
        if (fieldConfig == null || fieldConfig.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(fieldConfig, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * Lấy giá trị Double từ config map, hỗ trợ cả Integer và Double.
     */
    private Double getDoubleValue(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
