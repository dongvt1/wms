package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.FieldValue;
import com.cy.modules.qms.entity.StepResult;

import java.util.List;

/**
 * Service đánh giá kết quả kiểm tra.
 * Đánh giá pass/fail cho field, step, và execution.
 *
 * Logic đánh giá field:
 * - Measurement: PASS nếu lower ≤ value ≤ upper
 * - Number: PASS nếu min ≤ value ≤ max
 * - Boolean: sử dụng trực tiếp giá trị (true=PASS, false=FAIL)
 * - Text/Select: luôn PASS (không có auto-evaluation)
 *
 * Logic đánh giá step:
 * - PASS nếu tất cả field bắt buộc (isRequired=1) đều PASS
 * - Field không bắt buộc không ảnh hưởng kết quả step
 *
 * Logic đánh giá execution:
 * - PASS nếu tất cả step bắt buộc (isMandatory=1) đều PASS
 * - Step không bắt buộc không ảnh hưởng kết quả execution
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public interface EvaluationService {

    /**
     * Đánh giá một field value dựa trên fieldType và fieldConfig (snapshot).
     * Cập nhật trực tiếp result và evalMessage trên FieldValue entity.
     *
     * @param fieldValue FieldValue entity chứa actualValue, fieldType, fieldConfig
     */
    void evaluateField(FieldValue fieldValue);

    /**
     * Đánh giá kết quả step dựa trên danh sách field values.
     * PASS nếu tất cả field bắt buộc (isRequired=1) đều có result=pass.
     * Field không bắt buộc (isRequired=0) không ảnh hưởng kết quả.
     *
     * @param fieldValues danh sách FieldValue thuộc step
     * @return "pass" hoặc "fail"
     */
    String evaluateStep(List<FieldValue> fieldValues);

    /**
     * Đánh giá kết quả tổng thể execution dựa trên danh sách step results.
     * PASS nếu tất cả step bắt buộc (isMandatory=1) đều có result=pass.
     * Step không bắt buộc (isMandatory=0) không ảnh hưởng kết quả.
     *
     * @param stepResults danh sách StepResult thuộc execution
     * @return "pass" hoặc "fail"
     */
    String evaluateExecution(List<StepResult> stepResults);
}
