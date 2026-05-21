package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.InspectionStep;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.StepField;
import com.cy.modules.qms.vo.ValidationErrorVO;

import java.util.List;

/**
 * Service validate toàn bộ cấu hình template trước khi kích hoạt.
 * Trả về ALL errors (không dừng ở lỗi đầu tiên).
 *
 * Rules:
 * - Template phải có ≥ 1 step
 * - Mỗi mandatory step phải có ≥ 1 field
 * - Number field: min_value ≤ max_value
 * - Measurement field: lower_tolerance < nominal_value < upper_tolerance
 * - Select field: options JSON hợp lệ, ≥ 1 mục
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public interface TemplateValidationService {

    /**
     * Validate template cho activation.
     * Trả về danh sách tất cả lỗi validation tìm được.
     * Danh sách rỗng nghĩa là template hợp lệ.
     *
     * @param template  InspectionTemplate cần validate
     * @param steps     Danh sách InspectionStep thuộc template
     * @param fieldsByStep Danh sách StepField cho mỗi step (theo thứ tự tương ứng với steps)
     * @return danh sách lỗi validation, rỗng nếu hợp lệ
     */
    List<ValidationErrorVO.ValidationErrorItem> validateForActivation(
            InspectionTemplate template,
            List<InspectionStep> steps,
            List<List<StepField>> fieldsByStep
    );
}
