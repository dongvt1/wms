package com.cy.modules.qms.service.impl;

import com.cy.modules.qms.entity.FieldValue;
import com.cy.modules.qms.entity.StepResult;
import com.cy.modules.qms.entity.enums.EvaluationResult;
import com.cy.modules.qms.service.EvaluationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation logic đánh giá kết quả kiểm tra.
 *
 * Đánh giá field-level:
 * - Measurement: PASS nếu lower ≤ value ≤ upper
 * - Number: PASS nếu min ≤ value ≤ max
 * - Boolean: true → PASS, false → FAIL
 * - Text/Select: luôn PASS (không có auto-evaluation)
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Slf4j
@Service
public class EvaluationServiceImpl implements EvaluationService {

    private static final String FIELD_TYPE_MEASUREMENT = "measurement";
    private static final String FIELD_TYPE_NUMBER = "number";
    private static final String FIELD_TYPE_BOOLEAN = "boolean";
    private static final String FIELD_TYPE_TEXT = "text";
    private static final String FIELD_TYPE_SELECT = "select";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void evaluateField(FieldValue fieldValue) {
        if (fieldValue == null) {
            return;
        }

        String actualValue = fieldValue.getActualValue();
        String fieldType = fieldValue.getFieldType();

        // Nếu không có giá trị thực tế, đánh dấu NA
        if (actualValue == null || actualValue.trim().isEmpty()) {
            fieldValue.setResult(EvaluationResult.NA.getValue());
            fieldValue.setEvalMessage("Chưa nhập giá trị");
            return;
        }

        if (fieldType == null || fieldType.trim().isEmpty()) {
            fieldValue.setResult(EvaluationResult.NA.getValue());
            fieldValue.setEvalMessage("Không xác định kiểu trường");
            return;
        }

        switch (fieldType) {
            case FIELD_TYPE_MEASUREMENT:
                evaluateMeasurementField(fieldValue);
                break;
            case FIELD_TYPE_NUMBER:
                evaluateNumberField(fieldValue);
                break;
            case FIELD_TYPE_BOOLEAN:
                evaluateBooleanField(fieldValue);
                break;
            case FIELD_TYPE_TEXT:
            case FIELD_TYPE_SELECT:
                evaluateTextOrSelectField(fieldValue);
                break;
            default:
                fieldValue.setResult(EvaluationResult.NA.getValue());
                fieldValue.setEvalMessage("Kiểu trường không hỗ trợ đánh giá: " + fieldType);
                break;
        }
    }

    @Override
    public String evaluateStep(List<FieldValue> fieldValues) {
        if (fieldValues == null || fieldValues.isEmpty()) {
            // Không có field nào → coi như PASS (step không có yêu cầu)
            return EvaluationResult.PASS.getValue();
        }

        // Chỉ xét các field bắt buộc (isRequired = 1)
        boolean hasRequiredField = false;
        for (FieldValue fv : fieldValues) {
            if (fv.getIsRequired() != null && fv.getIsRequired() == 1) {
                hasRequiredField = true;
                String result = fv.getResult();
                // Nếu bất kỳ required field nào không PASS → step FAIL
                if (!EvaluationResult.PASS.getValue().equals(result)) {
                    return EvaluationResult.FAIL.getValue();
                }
            }
        }

        // Nếu không có required field nào → PASS (chỉ có optional fields)
        // Nếu tất cả required fields đều PASS → PASS
        return EvaluationResult.PASS.getValue();
    }

    @Override
    public String evaluateExecution(List<StepResult> stepResults) {
        if (stepResults == null || stepResults.isEmpty()) {
            // Không có step nào → coi như PASS
            return EvaluationResult.PASS.getValue();
        }

        // Chỉ xét các step bắt buộc (isMandatory = 1)
        for (StepResult sr : stepResults) {
            if (sr.getIsMandatory() != null && sr.getIsMandatory() == 1) {
                String result = sr.getResult();
                // Nếu bất kỳ mandatory step nào không PASS → execution FAIL
                if (!EvaluationResult.PASS.getValue().equals(result)) {
                    return EvaluationResult.FAIL.getValue();
                }
            }
        }

        // Nếu tất cả mandatory steps đều PASS (hoặc không có mandatory step) → PASS
        return EvaluationResult.PASS.getValue();
    }

    /**
     * Đánh giá measurement field: PASS nếu lower ≤ value ≤ upper.
     * fieldConfig: {"nominalValue": 5.0, "upperTolerance": 5.5, "lowerTolerance": 4.5}
     */
    private void evaluateMeasurementField(FieldValue fieldValue) {
        Double value = parseDouble(fieldValue.getActualValue());
        if (value == null) {
            fieldValue.setResult(EvaluationResult.FAIL.getValue());
            fieldValue.setEvalMessage("Giá trị không hợp lệ: " + fieldValue.getActualValue());
            return;
        }

        Map<String, Object> config = parseFieldConfig(fieldValue.getFieldConfig());
        if (config == null) {
            fieldValue.setResult(EvaluationResult.NA.getValue());
            fieldValue.setEvalMessage("Không có cấu hình dung sai");
            return;
        }

        Double lowerTolerance = getDoubleValue(config, "lowerTolerance");
        Double upperTolerance = getDoubleValue(config, "upperTolerance");

        if (lowerTolerance == null || upperTolerance == null) {
            fieldValue.setResult(EvaluationResult.NA.getValue());
            fieldValue.setEvalMessage("Cấu hình dung sai không đầy đủ");
            return;
        }

        if (value >= lowerTolerance && value <= upperTolerance) {
            fieldValue.setResult(EvaluationResult.PASS.getValue());
            fieldValue.setEvalMessage("Trong dung sai [" + lowerTolerance + ", " + upperTolerance + "]");
        } else {
            fieldValue.setResult(EvaluationResult.FAIL.getValue());
            fieldValue.setEvalMessage("Ngoài dung sai [" + lowerTolerance + ", " + upperTolerance + "], giá trị: " + value);
        }
    }

    /**
     * Đánh giá number field: PASS nếu min ≤ value ≤ max.
     * fieldConfig: {"minValue": 0, "maxValue": 100, "decimalPlaces": 2}
     */
    private void evaluateNumberField(FieldValue fieldValue) {
        Double value = parseDouble(fieldValue.getActualValue());
        if (value == null) {
            fieldValue.setResult(EvaluationResult.FAIL.getValue());
            fieldValue.setEvalMessage("Giá trị không hợp lệ: " + fieldValue.getActualValue());
            return;
        }

        Map<String, Object> config = parseFieldConfig(fieldValue.getFieldConfig());
        if (config == null) {
            // Không có config → không có ràng buộc min/max → luôn PASS
            fieldValue.setResult(EvaluationResult.PASS.getValue());
            fieldValue.setEvalMessage("Giá trị hợp lệ (không có giới hạn)");
            return;
        }

        Double minValue = getDoubleValue(config, "minValue");
        Double maxValue = getDoubleValue(config, "maxValue");

        // Nếu không có cả min và max → luôn PASS
        if (minValue == null && maxValue == null) {
            fieldValue.setResult(EvaluationResult.PASS.getValue());
            fieldValue.setEvalMessage("Giá trị hợp lệ (không có giới hạn)");
            return;
        }

        boolean passMin = (minValue == null) || (value >= minValue);
        boolean passMax = (maxValue == null) || (value <= maxValue);

        if (passMin && passMax) {
            fieldValue.setResult(EvaluationResult.PASS.getValue());
            fieldValue.setEvalMessage("Trong khoảng [" + formatBound(minValue) + ", " + formatBound(maxValue) + "]");
        } else {
            fieldValue.setResult(EvaluationResult.FAIL.getValue());
            fieldValue.setEvalMessage("Ngoài khoảng [" + formatBound(minValue) + ", " + formatBound(maxValue) + "], giá trị: " + value);
        }
    }

    /**
     * Đánh giá boolean field: true → PASS, false → FAIL.
     */
    private void evaluateBooleanField(FieldValue fieldValue) {
        String actualValue = fieldValue.getActualValue().trim().toLowerCase();

        if ("true".equals(actualValue) || "1".equals(actualValue)) {
            fieldValue.setResult(EvaluationResult.PASS.getValue());
            fieldValue.setEvalMessage("Đạt");
        } else if ("false".equals(actualValue) || "0".equals(actualValue)) {
            fieldValue.setResult(EvaluationResult.FAIL.getValue());
            fieldValue.setEvalMessage("Không đạt");
        } else {
            fieldValue.setResult(EvaluationResult.NA.getValue());
            fieldValue.setEvalMessage("Giá trị boolean không hợp lệ: " + fieldValue.getActualValue());
        }
    }

    /**
     * Text và Select: luôn PASS (không có auto-evaluation logic).
     */
    private void evaluateTextOrSelectField(FieldValue fieldValue) {
        fieldValue.setResult(EvaluationResult.PASS.getValue());
        fieldValue.setEvalMessage("Đã nhập giá trị");
    }

    // ==================== Utility methods ====================

    /**
     * Parse field_config JSON string thành Map.
     */
    private Map<String, Object> parseFieldConfig(String fieldConfig) {
        if (fieldConfig == null || fieldConfig.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(fieldConfig, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Không thể parse fieldConfig JSON: {}", fieldConfig, e);
            return null;
        }
    }

    /**
     * Parse string thành Double.
     */
    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
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

    /**
     * Format giá trị bound cho message (hiển thị "-∞" hoặc "+∞" nếu null).
     */
    private String formatBound(Double value) {
        if (value == null) {
            return "∞";
        }
        // Hiển thị số nguyên nếu không có phần thập phân
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value.doubleValue());
        }
        return String.valueOf(value);
    }
}
