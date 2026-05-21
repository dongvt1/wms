# Implementation Plan: QMS Step Configuration

## Overview

Triển khai tính năng QMS Step Configuration cho WMS Manufacturing Platform. Hệ thống bao gồm:
- Backend: Java (Spring Boot + MyBatis-Plus + jqwik) với 8 bảng mới + 2 ALTER TABLE
- Frontend: Vue 3 + Ant Design Vue + TypeScript
- Tích hợp WMS routing steps qua Spring Events
- Dynamic form rendering với 5 field types
- Template snapshot pattern cho execution isolation
- Approval workflow với state machine

## Tasks

- [x] 1. Thiết lập cấu trúc dự án và database schema
  - [x] 1.1 Tạo migration script cho 8 bảng mới và 2 ALTER TABLE
    - Tạo file SQL migration cho: `qms_inspection_template`, `qms_inspection_step`, `qms_step_field`, `qms_template_assignment`, `qms_inspection_execution`, `qms_step_result`, `qms_field_value`, `qms_approval_record`
    - Tạo ALTER TABLE cho `pl_routing_step` (thêm `qc_stage_id`, `qc_stage_type`) và `pl_production_stage` (thêm `qc_execution_id`, `qc_blocked`)
    - Bao gồm indexes và unique constraints theo design
    - _Requirements: 1.1, 2.1, 3.1, 5.1, 6.1, 7.6, 8.6, 9.1_

  - [x] 1.2 Tạo entity classes và MyBatis-Plus mappers
    - Tạo Java entities: `InspectionTemplate`, `InspectionStep`, `StepField`, `TemplateAssignment`, `InspectionExecution`, `StepResult`, `FieldValue`, `ApprovalRecord`
    - Sử dụng `@TableName`, `@TableId`, `@TableField` annotations theo JeecgBoot pattern
    - Tạo Mapper interfaces extends `BaseMapper<T>` cho mỗi entity
    - Định nghĩa enums: `StageType`, `TemplateStatus`, `FieldType`, `ExecutionStatus`, `EvaluationResult`, `ApprovalAction`
    - _Requirements: 1.1, 2.1, 3.1, 3.2_

  - [x] 1.3 Tạo DTO/VO classes cho API layer
    - Tạo `InspectionTemplateDTO` (input) với nested `InspectionStepDTO` và `StepFieldDTO`
    - Tạo `InspectionTemplateVO` (output) với nested steps và fields
    - Tạo `InspectionExecutionDTO`, `InspectionExecutionVO`
    - Tạo `FieldValueDTO`, `StepResultVO`, `ApprovalDTO`
    - Tạo `ValidationErrorVO` cho error responses
    - _Requirements: 1.1, 6.1, 8.3_

- [x] 2. Implement Template Code Generator và Validation Service
  - [x] 2.1 Implement TemplateCodeGenerator
    - Tạo service sinh mã template theo format `TPLyyyyMMddNNN`
    - Đảm bảo uniqueness bằng cách query counter hiện tại trong ngày
    - Thread-safe với `synchronized` hoặc database sequence
    - _Requirements: 1.1, 1.2_

  - [x] 2.2 Write property test cho Template code generation (Property 1)
    - **Property 1: Template code generation produces unique codes in correct format**
    - Verify format regex `TPL\d{8}\d{3}` và uniqueness cho N codes
    - **Validates: Requirements 1.1, 1.2**

  - [x] 2.3 Implement TemplateValidationService
    - Validate template có ≥ 1 step
    - Validate mỗi mandatory step có ≥ 1 field
    - Validate number field: `min_value ≤ max_value`
    - Validate measurement field: `lower_tolerance < nominal_value < upper_tolerance`
    - Validate select field: options JSON hợp lệ, ≥ 1 mục
    - Trả về ALL errors (không dừng ở lỗi đầu tiên)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

  - [x] 2.4 Write property test cho Numeric range validation (Property 10)
    - **Property 10: Numeric range validation correctness**
    - Generate random min/max/nominal/tolerance values, verify accept/reject
    - **Validates: Requirements 4.2, 4.3**

  - [x] 2.5 Write property test cho Validation reports all errors (Property 9)
    - **Property 9: Template activation validation reports all errors**
    - Generate templates với 1-N intentional errors, verify all reported
    - **Validates: Requirements 4.1, 4.4, 4.6**

- [x] 3. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Implement InspectionTemplateService (CRUD + Activate + Clone)
  - [x] 4.1 Implement Template CRUD operations
    - Tạo `InspectionTemplateService` interface và `InspectionTemplateServiceImpl`
    - Implement `saveTemplateWithSteps()` - lưu template + steps + fields trong single `@Transactional`
    - Implement `updateTemplateWithSteps()` - cập nhật với diff logic (add/update/delete steps/fields)
    - Implement `deleteTemplate()` - kiểm tra referential integrity trước khi xóa
    - Implement `getTemplateDetail()` - load template kèm steps + fields
    - Implement `listTemplates()` - phân trang + filter (stage_type, status, search)
    - _Requirements: 1.1, 1.3, 1.4, 1.6, 2.6, 2.7_

  - [x] 4.2 Write property test cho Referential integrity (Property 5)
    - **Property 5: Referential integrity prevents deletion of used templates**
    - Generate templates với 0-N executions, verify delete behavior
    - **Validates: Requirements 1.6**

  - [x] 4.3 Write property test cho Filter results match (Property 3)
    - **Property 3: Filter results match criteria**
    - Generate list templates với random stage/status, apply random filter combos
    - **Validates: Requirements 1.4**

  - [x] 4.4 Implement Template Activation logic
    - Validate template trước khi activate (gọi TemplateValidationService)
    - Chuyển template cũ (cùng product + stage type) sang `obsolete`
    - Cập nhật status template mới sang `active` trong cùng transaction
    - _Requirements: 1.5, 5.3_

  - [x] 4.5 Write property test cho Activate obsoletes previous (Property 4)
    - **Property 4: Activating a template obsoletes the previous active template**
    - Generate sequence of activate operations cho same product+stage
    - **Validates: Requirements 1.5, 5.3**

  - [x] 4.6 Implement Template Clone
    - Deep clone template + all steps + all fields với new IDs
    - Reset status sang `draft`, tăng version
    - Giữ nguyên toàn bộ cấu hình (name, type, config, sort_order)
    - _Requirements: 1.7_

  - [x] 4.7 Write property test cho Clone preserves structure (Property 2)
    - **Property 2: Template clone preserves structure**
    - Generate random templates (1-10 steps, 1-5 fields/step), verify clone
    - **Validates: Requirements 1.7**

  - [x] 4.8 Implement Step reorder logic
    - Implement drag-and-drop reorder: nhận danh sách step IDs theo thứ tự mới
    - Tự động cập nhật `sort_order` liên tục từ 1
    - Cascade delete step kèm toàn bộ fields
    - _Requirements: 2.2, 2.3, 2.4, 2.5_

  - [x] 4.9 Write property test cho Sort order invariant (Property 6)
    - **Property 6: Sort order invariant after reordering**
    - Generate random sequences of add/remove/reorder operations
    - **Validates: Requirements 2.2, 2.4**

  - [x] 4.10 Write property test cho Cascade delete (Property 7)
    - **Property 7: Cascade delete removes all child fields**
    - Generate steps với 0-10 fields, delete step, verify zero orphans
    - **Validates: Requirements 2.5**

- [x] 5. Implement TemplateAssignment và TemplateResolutionService
  - [x] 5.1 Implement TemplateAssignmentService
    - CRUD cho template assignments (product, product_group, default)
    - Validate: chỉ 1 active template per product + stage type
    - Giao diện gán/gỡ template từ sản phẩm
    - _Requirements: 5.1, 5.2, 5.3, 5.5, 5.6_

  - [x] 5.2 Implement TemplateResolutionService
    - Logic ưu tiên: product-specific → product-group → default
    - Method `resolveTemplate(productId, stageType)` → InspectionTemplate
    - Trả lỗi `NO_TEMPLATE_FOUND` nếu không tìm được
    - _Requirements: 5.4, 6.1_

  - [x] 5.3 Write property test cho Template resolution priority (Property 11)
    - **Property 11: Template resolution follows priority order**
    - Generate random assignment configurations, verify priority
    - **Validates: Requirements 5.4, 6.1**

- [x] 6. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Implement EvaluationService
  - [x] 7.1 Implement field-level evaluation logic
    - `evaluateField(FieldValue, StepField)` → FieldResult
    - Measurement: PASS if `lower ≤ value ≤ upper`
    - Number: PASS if `min ≤ value ≤ max`
    - Boolean: sử dụng trực tiếp giá trị
    - Text/Select: luôn PASS (không có auto-evaluation)
    - Tạo `eval_message` mô tả kết quả (vd: "Trong dung sai [4.5, 5.5]")
    - _Requirements: 7.1, 7.2, 7.3_

  - [x] 7.2 Implement step-level và execution-level aggregation
    - `evaluateStep(StepResult)`: PASS if ALL required fields PASS
    - `evaluateExecution(InspectionExecution)`: PASS if ALL mandatory steps PASS
    - Optional/non-required items không ảnh hưởng parent result
    - _Requirements: 7.4, 7.5, 7.6_

  - [x] 7.3 Write property test cho Field evaluation correctness (Property 13)
    - **Property 13: Field evaluation correctness**
    - Generate random measurement/number values và tolerance/range, verify PASS/FAIL
    - **Validates: Requirements 7.1, 7.2**

  - [x] 7.4 Write property test cho Hierarchical result aggregation (Property 14)
    - **Property 14: Hierarchical result aggregation**
    - Generate executions với random mandatory/optional steps và field results
    - **Validates: Requirements 7.4, 7.5**

- [x] 8. Implement InspectionExecutionService
  - [x] 8.1 Implement Execution creation với template snapshot
    - Tạo `InspectionExecution` với snapshot toàn bộ template config (JSON)
    - Tạo `StepResult` records cho mỗi step (status = pending)
    - Tạo `FieldValue` records cho mỗi field (value = null)
    - Gọi `TemplateResolutionService` để tìm template phù hợp
    - _Requirements: 6.1, 6.2_

  - [x] 8.2 Implement save draft và submit logic
    - `saveDraft()`: lưu field values mà không evaluate
    - `submitStepValues()`: lưu values + gọi EvaluationService + cập nhật results
    - `submitExecution()`: validate tất cả mandatory steps hoàn thành → chuyển sang `pending_approval`
    - Enforce sequential step completion (phải hoàn thành bước trước)
    - _Requirements: 6.5, 6.6, 6.7_

  - [x] 8.3 Implement state machine cho Execution status
    - Valid transitions: draft→in_progress, in_progress→pending_approval, pending_approval→approved/rejected/in_progress
    - Reject invalid transitions với error message
    - _Requirements: 8.6_

  - [x] 8.4 Write property test cho State machine transition validity (Property 15)
    - **Property 15: State machine transition validity**
    - Generate random state transition sequences, verify only valid ones accepted
    - **Validates: Requirements 8.6**

  - [x] 8.5 Write property test cho Completion validation (Property 12)
    - **Property 12: Completion validation enforces mandatory requirements**
    - Generate executions với random mandatory/optional steps, random field completion
    - **Validates: Requirements 6.5, 6.7**

- [x] 9. Implement ApprovalService và WMS Integration
  - [x] 9.1 Implement ApprovalService
    - `approve(executionId, comment)`: chuyển status → approved, ghi ApprovalRecord
    - `reject(executionId, reason)`: chuyển status → rejected, bắt buộc có reason
    - `reInspect(executionId, stepId, reason)`: reset step cụ thể, giữ nguyên steps đã approved
    - Gửi notification cho Nhân_viên_QC khi reject/re-inspect
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [x] 9.2 Write property test cho Re-inspection isolation (Property 16)
    - **Property 16: Re-inspection isolation**
    - Generate executions với multiple steps, mark one for re-inspect, verify isolation
    - **Validates: Requirements 8.5**

  - [x] 9.3 Implement WMS Routing Step Integration
    - Tạo Spring Event listener cho routing step completion
    - Khi routing step có `qc_stage_id` hoàn thành → auto-create InspectionExecution
    - Set `qc_blocked = 1` trên `pl_production_stage` khi inspection chưa approved
    - Release block khi inspection approved
    - Cập nhật số lượng lỗi trên Work Order khi FAIL
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

  - [x] 9.4 Write property test cho Routing step blocking (Property 17)
    - **Property 17: Routing step blocking during inspection**
    - Generate production stages với pending inspections, verify blocking
    - **Validates: Requirements 9.3**

- [x] 10. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Implement REST Controllers
  - [x] 11.1 Implement InspectionTemplateController
    - `GET /api/qms/inspection-template/list` - phân trang + filter
    - `GET /api/qms/inspection-template/{id}` - chi tiết kèm steps + fields
    - `POST /api/qms/inspection-template` - tạo mới
    - `PUT /api/qms/inspection-template/{id}` - cập nhật
    - `DELETE /api/qms/inspection-template/{id}` - xóa
    - `PUT /api/qms/inspection-template/{id}/activate` - kích hoạt
    - `POST /api/qms/inspection-template/{id}/clone` - nhân bản
    - `GET /api/qms/inspection-template/{id}/preview` - preview data
    - Sử dụng JeecgBoot `Result<T>` wrapper cho responses
    - _Requirements: 1.1, 1.3, 1.4, 1.5, 1.6, 1.7, 10.1_

  - [x] 11.2 Implement TemplateAssignmentController
    - `GET /api/qms/template-assignment/list` - danh sách assignments
    - `POST /api/qms/template-assignment` - gán template
    - `DELETE /api/qms/template-assignment/{id}` - gỡ assignment
    - `GET /api/qms/template-assignment/resolve` - tìm template phù hợp
    - _Requirements: 5.1, 5.4, 5.5, 5.6_

  - [x] 11.3 Implement InspectionExecutionController
    - `GET /api/qms/inspection-execution/list` - danh sách phiên kiểm tra
    - `GET /api/qms/inspection-execution/{id}` - chi tiết kèm results
    - `POST /api/qms/inspection-execution` - tạo mới
    - `PUT /api/qms/inspection-execution/{id}/save-draft` - lưu nháp
    - `PUT /api/qms/inspection-execution/{id}/submit` - submit
    - `PUT /api/qms/inspection-execution/{id}/step/{stepId}/values` - lưu giá trị
    - _Requirements: 6.1, 6.2, 6.5, 6.6, 6.7_

  - [x] 11.4 Implement ApprovalController
    - `GET /api/qms/approval/pending` - danh sách chờ phê duyệt
    - `PUT /api/qms/approval/{executionId}/approve` - phê duyệt
    - `PUT /api/qms/approval/{executionId}/reject` - từ chối
    - `PUT /api/qms/approval/{executionId}/re-inspect` - yêu cầu kiểm tra lại
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 12. Implement Frontend - Template Configuration
  - [x] 12.1 Tạo InspectionTemplateList.vue
    - Bảng danh sách template với Ant Design Table
    - Filter: stage type (IQC/PQC/FQC), status (draft/active/obsolete), search text
    - Actions: Tạo mới, Sửa, Xóa (confirm), Clone, Activate
    - Phân trang server-side
    - _Requirements: 1.3, 1.4_

  - [x] 12.2 Tạo InspectionTemplateForm.vue và StepConfigPanel.vue
    - Form chính: tên, mô tả, stage type, version, notes
    - Nested StepConfigPanel: expandable/collapsible step cards
    - Drag-and-drop reorder steps (sử dụng vuedraggable hoặc @vueuse/integrations)
    - Add/remove steps với confirmation
    - Lưu toàn bộ template + steps + fields trong 1 API call
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [x] 12.3 Tạo FieldConfigForm.vue với dynamic rendering
    - Component factory pattern: render đúng config form theo field_type
    - `NumberFieldConfig`: min, max, decimal_places (a-input-number)
    - `MeasurementFieldConfig`: nominal, upper, lower, unit
    - `SelectFieldConfig`: options list management (add/remove/reorder)
    - `BooleanFieldConfig`: custom labels (trueLabel, falseLabel)
    - `TextFieldConfig`: placeholder, max_length, multiline toggle
    - Clear config khi đổi field_type
    - Drag-and-drop reorder fields
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_

  - [x] 12.4 Write property test cho Field type change clears config (Property 8)
    - **Property 8: Field type change clears incompatible configuration**
    - Test tất cả 5×4=20 field type transitions, verify config cleanup
    - **Validates: Requirements 3.8**

  - [x] 12.5 Tạo TemplatePreviewModal.vue
    - Modal xem trước template dưới dạng form kiểm tra
    - Render đầy đủ steps + fields theo đúng field type
    - Cho phép nhập dữ liệu thử và hiển thị kết quả đánh giá (pass/fail)
    - Không ảnh hưởng dữ liệu cấu hình đang chỉnh sửa
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [x] 13. Implement Frontend - Inspection Execution và Approval
  - [x] 13.1 Tạo InspectionExecutionForm.vue
    - Step-by-step wizard navigation (Ant Design Steps)
    - Dynamic field rendering based on field_type sử dụng component factory
    - Hiển thị tolerance/range bên cạnh measurement/number fields
    - Enforce sequential step completion (disable bước tiếp nếu chưa hoàn thành)
    - Save draft functionality
    - Submit button khi tất cả mandatory steps hoàn thành
    - _Requirements: 6.2, 6.3, 6.4, 6.5, 6.6, 6.7_

  - [x] 13.2 Tạo EvaluationDisplay component
    - Hiển thị trực quan kết quả: màu xanh (PASS), màu đỏ (FAIL)
    - Hiển thị giá trị thực tế so với giới hạn cho phép
    - Hiển thị eval_message từ backend
    - _Requirements: 7.7_

  - [x] 13.3 Tạo ApprovalPanel.vue
    - Danh sách phiên chờ phê duyệt
    - Chi tiết kết quả kiểm tra (read-only)
    - Actions: Approve, Reject (bắt buộc nhập lý do), Re-inspect (chọn step + lý do)
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [x] 13.4 Tạo TemplateAssignmentPanel.vue
    - Danh sách sản phẩm/nhóm đã gán cho template
    - Giao diện chọn sản phẩm có tìm kiếm (a-select với search)
    - Gán/gỡ template
    - _Requirements: 5.5, 5.6_

- [x] 14. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 15. Implement Report Service và Frontend
  - [x] 15.1 Implement ReportService
    - Tra cứu lịch sử kiểm tra: filter theo sản phẩm, template, thời gian, kết quả, người kiểm tra
    - Tính toán thống kê: tỷ lệ pass/fail theo template, theo field
    - Pareto analysis: top 5 fields có tỷ lệ fail cao nhất
    - Xu hướng theo thời gian
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

  - [x] 15.2 Write property test cho Statistics calculation (Property 18)
    - **Property 18: Statistics calculation correctness**
    - Generate random execution results, verify pass/fail ratio và Pareto ranking
    - **Validates: Requirements 11.3, 11.4**

  - [x] 15.3 Implement ReportController
    - `GET /api/qms/report/history` - lịch sử kiểm tra có filter + phân trang
    - `GET /api/qms/report/statistics` - thống kê pass/fail
    - `GET /api/qms/report/pareto` - Pareto analysis
    - `GET /api/qms/report/export` - xuất PDF/Excel
    - _Requirements: 11.2, 11.3, 11.4, 11.5_

  - [x] 15.4 Tạo Frontend Report views
    - Bảng lịch sử kiểm tra với filters
    - Biểu đồ thống kê pass/fail (sử dụng ECharts hoặc Ant Design Charts)
    - Biểu đồ Pareto cho top 5 fields fail
    - Export buttons (PDF/Excel)
    - _Requirements: 11.2, 11.3, 11.4, 11.5_

- [x] 16. Integration wiring và Frontend routing
  - [x] 16.1 Cấu hình Vue Router và menu
    - Thêm routes cho: template list, template form, execution list, execution form, approval, reports
    - Cấu hình menu items trong JeecgBoot admin layout
    - Permission guards cho Quản_lý_QC vs Nhân_viên_QC roles
    - _Requirements: 1.3, 6.2, 8.3, 11.2_

  - [x] 16.2 Implement frontend API service layer
    - Tạo `inspectionTemplateApi.ts` - tất cả template API calls
    - Tạo `inspectionExecutionApi.ts` - tất cả execution API calls
    - Tạo `approvalApi.ts` - approval API calls
    - Tạo `reportApi.ts` - report API calls
    - Error handling theo pattern design (422 → inline errors, 409 → modal confirm)
    - _Requirements: 4.5_

  - [x] 16.3 Wire Spring Event listener cho WMS integration
    - Register `RoutingStepCompletedEvent` listener
    - Auto-create InspectionExecution khi routing step có QC stage hoàn thành
    - Update `pl_production_stage.qc_blocked` flag
    - Release block khi inspection approved
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [x] 17. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties defined in design (18 properties total)
- Unit tests validate specific examples and edge cases
- Backend sử dụng JeecgBoot patterns: `Result<T>` wrapper, `@TableName` entities, `BaseMapper<T>`
- Frontend sử dụng Ant Design Vue components và Vue 3 Composition API
- jqwik library cho property-based testing (Java)
- Vitest cho frontend component tests

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2"] },
    { "id": 2, "tasks": ["1.3", "2.1"] },
    { "id": 3, "tasks": ["2.2", "2.3"] },
    { "id": 4, "tasks": ["2.4", "2.5", "4.1"] },
    { "id": 5, "tasks": ["4.2", "4.3", "4.4", "4.6", "4.8"] },
    { "id": 6, "tasks": ["4.5", "4.7", "4.9", "4.10", "5.1"] },
    { "id": 7, "tasks": ["5.2", "7.1"] },
    { "id": 8, "tasks": ["5.3", "7.2", "7.3"] },
    { "id": 9, "tasks": ["7.4", "8.1"] },
    { "id": 10, "tasks": ["8.2", "8.3"] },
    { "id": 11, "tasks": ["8.4", "8.5", "9.1"] },
    { "id": 12, "tasks": ["9.2", "9.3"] },
    { "id": 13, "tasks": ["9.4", "11.1", "11.2"] },
    { "id": 14, "tasks": ["11.3", "11.4", "12.1"] },
    { "id": 15, "tasks": ["12.2", "12.3"] },
    { "id": 16, "tasks": ["12.4", "12.5", "13.1"] },
    { "id": 17, "tasks": ["13.2", "13.3", "13.4"] },
    { "id": 18, "tasks": ["15.1"] },
    { "id": 19, "tasks": ["15.2", "15.3"] },
    { "id": 20, "tasks": ["15.4", "16.1"] },
    { "id": 21, "tasks": ["16.2", "16.3"] }
  ]
}
```
