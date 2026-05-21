package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.qms.dto.FieldValueDTO;
import com.cy.modules.qms.dto.InspectionExecutionDTO;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.vo.InspectionExecutionVO;

import java.util.List;

/**
 * Service quản lý Inspection Execution (phiên kiểm tra chất lượng).
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public interface InspectionExecutionService {

    /**
     * Danh sách phiên kiểm tra có phân trang và filter.
     *
     * @param page Thông tin phân trang
     * @param status Filter theo trạng thái (optional)
     * @param productId Filter theo sản phẩm (optional)
     * @param stageType Filter theo loại giai đoạn QC (optional)
     * @return Trang kết quả InspectionExecutionVO
     */
    IPage<InspectionExecutionVO> listExecutions(Page<InspectionExecution> page, String status, String productId, String stageType);

    /**
     * Chi tiết phiên kiểm tra kèm step results và field values.
     *
     * @param id ID phiên kiểm tra
     * @return InspectionExecutionVO đầy đủ hoặc null nếu không tìm thấy
     */
    InspectionExecutionVO getExecutionDetail(String id);

    /**
     * Tạo phiên kiểm tra mới.
     *
     * Logic:
     * 1. Gọi TemplateResolutionService.resolveTemplate(productId, stageType) để tìm template phù hợp
     * 2. Tạo InspectionExecution với:
     *    - Mã phiên tự sinh (EXCyyyyMMddNNN)
     *    - template_snapshot: JSON snapshot toàn bộ cấu hình template (steps + fields)
     *    - status = "draft"
     * 3. Tạo StepResult records cho mỗi step trong template (status = "pending", result = null)
     * 4. Tạo FieldValue records cho mỗi field trong mỗi step (actualValue = null, result = null)
     *    - Copy field_name, field_type, field_config, is_required từ template làm snapshot
     * 5. Trả về InspectionExecutionVO đầy đủ
     *
     * @param dto DTO chứa productId, stageType, workOrderId (optional), productionStageId (optional)
     * @return InspectionExecutionVO đầy đủ kèm steps và fields
     * @throws com.cy.modules.qms.exception.TemplateNotFoundException nếu không tìm được template phù hợp
     */
    InspectionExecutionVO createExecution(InspectionExecutionDTO dto);

    /**
     * Lưu nháp giá trị field cho một step mà không đánh giá.
     *
     * Logic:
     * 1. Validate execution tồn tại và status cho phép chỉnh sửa (draft hoặc in_progress)
     * 2. Validate stepResult thuộc execution
     * 3. Cập nhật actualValue cho mỗi FieldValue theo fieldId
     * 4. Nếu execution đang ở status "draft", chuyển sang "in_progress"
     * 5. KHÔNG gọi EvaluationService
     *
     * @param executionId ID phiên kiểm tra
     * @param stepResultId ID kết quả bước kiểm tra
     * @param values Danh sách giá trị field cần lưu
     * @throws IllegalArgumentException nếu execution/stepResult không tồn tại
     * @throws IllegalStateException nếu execution status không cho phép chỉnh sửa
     */
    void saveDraft(String executionId, String stepResultId, List<FieldValueDTO> values);

    /**
     * Submit giá trị field cho một step, đánh giá và cập nhật kết quả.
     *
     * Logic:
     * 1. Validate execution tồn tại và status cho phép (draft hoặc in_progress)
     * 2. Validate stepResult thuộc execution
     * 3. Enforce sequential step completion: kiểm tra tất cả step trước đã completed
     * 4. Cập nhật actualValue cho mỗi FieldValue theo fieldId
     * 5. Gọi EvaluationService.evaluateField() cho mỗi FieldValue
     * 6. Gọi EvaluationService.evaluateStep() để tính kết quả step
     * 7. Cập nhật StepResult: result, status = "completed", completedTime
     * 8. Nếu execution đang ở status "draft", chuyển sang "in_progress"
     *
     * @param executionId ID phiên kiểm tra
     * @param stepResultId ID kết quả bước kiểm tra
     * @param values Danh sách giá trị field cần submit
     * @throws IllegalArgumentException nếu execution/stepResult không tồn tại
     * @throws IllegalStateException nếu execution status không cho phép hoặc step trước chưa hoàn thành
     */
    void submitStepValues(String executionId, String stepResultId, List<FieldValueDTO> values);

    /**
     * Submit toàn bộ execution để chuyển sang pending_approval.
     *
     * Logic:
     * 1. Validate execution tồn tại và status = "in_progress"
     * 2. Validate tất cả mandatory steps (isMandatory=1) đã completed
     * 3. Gọi EvaluationService.evaluateExecution() để tính overall result
     * 4. Cập nhật execution: overallResult, status = "pending_approval"
     *
     * @param executionId ID phiên kiểm tra
     * @throws IllegalArgumentException nếu execution không tồn tại
     * @throws IllegalStateException nếu execution status không phải in_progress hoặc có mandatory step chưa hoàn thành
     */
    void submitExecution(String executionId);
}
