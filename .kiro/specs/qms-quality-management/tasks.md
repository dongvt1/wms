# Implementation Plan: QMS Quality Management - New Features

## Overview

This plan implements the new QMS features on top of the existing IQC, PQC, QC Stage/Session, QC Review, and Checklist Template modules. The implementation follows the established JeecgBoot patterns (entity → mapper → service → controller) for backend and Ant Design Vue BasicTable + useTable + useModal for frontend.

**Tech Stack:**
- Backend: Java 21 + Spring Boot 3.5.5 + JeecgBoot 3.8.3 + MyBatis-Plus 3.5.12 + MySQL
- Frontend: Vue 3 + TypeScript + Vite 6 + Ant Design Vue 4 + Pinia
- Testing: JUnit 5 + Mockito + jqwik (property-based)

## Tasks

- [x] 1. Database schema and shared infrastructure
  - [x] 1.1 Create SQL migration script for all new tables
    - Create `/Volumes/DONG/wms/sql/wms_qms_v2.sql` with DDL for: `qms_fqc_inspection`, `qms_fqc_inspection_result`, `qms_ncr`, `qms_attachment`, `qms_notification`
    - Add `ALTER TABLE wh_stock_transaction ADD COLUMN qc_status VARCHAR(20) NULL DEFAULT 'pending'`
    - Update `qms_checklist_template.inspection_type` comment to include 'fqc'
    - _Requirements: 7.1, 7.2, 8.1, 9.1, 9.2, 10.1, 11.1_

  - [x] 1.2 Create shared utility class for code generation
    - Create `com.cy.modules.qms.util.QmsCodeGenerator` with method `generateCode(String prefix, Mapper)` that produces `PREFIXyyyyMMddNNN` format
    - Refactor existing IQC/PQC code generation to use this shared utility
    - _Requirements: 2.2, 3.2, 7.2, 9.2_

- [x] 2. FQC Module - Backend
  - [x] 2.1 Create FQC entity and mapper classes
    - Create `FqcInspection.java` entity with fields: id, inspectionCode, outboundOrderId, productId, customerId, templateId, quantityInspected, quantityPassed, quantityFailed, inspector, inspectionDate, status, notes + JeecgBoot audit fields
    - Create `FqcInspectionResult.java` entity mirroring `IqcInspectionResult` structure
    - Create `FqcInspectionMapper.java` and `FqcInspectionResultMapper.java` interfaces
    - Create corresponding XML mapper files if needed
    - _Requirements: 7.1, 7.2, 7.3_

  - [x] 2.2 Create FQC service layer
    - Create `FqcInspectionService.java` interface extending `IService<FqcInspection>` with methods: generateInspectionCode(), saveWithResults(), updateWithResults(), submitForApproval(), approveInspection(), getResults(), getDetail(), getStatistics(), isOutboundAllowed()
    - Create `FqcInspectionServiceImpl.java` implementing the service
    - Implement state machine: draft → in_progress → pending_approval → passed/failed
    - Implement `isOutboundAllowed(orderId)` that checks if linked FQC is passed
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [x] 2.3 Create FQC controller
    - Create `FqcInspectionController.java` at `/qms/fqc/*` following `IqcInspectionController` pattern
    - Endpoints: list, add, edit, delete, queryById, getResults, submit/{id}, approve/{id}, statistics, checkOutbound/{orderId}
    - Add `@AutoLog` annotations on state-changing endpoints
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [ ]* 2.4 Write property tests for FQC code generation and state machine
    - **Property 1: Inspection code generation format and uniqueness** — verify FQC codes match `FQCyyyyMMddNNN` format
    - **Property 2: State machine transition validity** — verify only valid FQC transitions succeed
    - **Validates: Requirements 7.2, 7.4**

- [x] 3. NCR Module - Backend
  - [x] 3.1 Create NCR entity and mapper classes
    - Create `Ncr.java` entity with fields: id, ncrCode, sourceType, sourceId, productId, supplierId, description, severity, quantityDefective, proposedAction, correctiveAction, status, assignedTo, closedBy, closedDate, notes + JeecgBoot audit fields
    - Create `NcrMapper.java` interface
    - _Requirements: 9.1, 9.2_

  - [x] 3.2 Create NCR service layer
    - Create `NcrService.java` interface with methods: generateNcrCode(), createFromInspection(), transition(), close(), getSupplierHistory(), getStatistics()
    - Create `NcrServiceImpl.java` implementing the service
    - Implement state machine: open → investigating → action_taken → verified → closed
    - Implement `createFromInspection(inspectionId, sourceType)` that auto-links supplier from IQC
    - Implement `close()` requiring corrective action confirmation
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.6_

  - [x] 3.3 Create NCR controller
    - Create `NcrController.java` at `/qms/ncr/*` following existing controller pattern
    - Endpoints: list, add, edit, delete, queryById, transition/{id}, close/{id}, statistics, bySupplier/{supplierId}
    - Add `@AutoLog` annotations on state-changing endpoints
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.6_

  - [ ]* 3.4 Write property tests for NCR state machine and supplier linking
    - **Property 2: State machine transition validity** — verify only valid NCR transitions succeed
    - **Property 11: NCR auto-links supplier from IQC source** — verify supplier_id is populated from IQC
    - **Validates: Requirements 9.3, 9.6**

- [x] 4. Stock Blocking Integration - Backend
  - [x] 4.1 Create StockBlockingService
    - Create `StockBlockingService.java` interface with methods: handleIqcApproval(inspectionId, status), isStockAvailable(stockTransactionId), releaseBlock(stockTransactionId, ncrId)
    - Create `StockBlockingServiceImpl.java` that updates `wh_stock_transaction.qc_status` based on IQC result
    - Mapping: failed → blocked, conditional → conditional_hold, passed → available
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [x] 4.2 Integrate StockBlockingService into existing IQC approval flow
    - Modify `IqcInspectionServiceImpl.approveInspection()` to call `StockBlockingService.handleIqcApproval()` after status update
    - Wrap in `@Transactional` to ensure atomicity
    - Add check in Work Order material allocation to call `isStockAvailable()`
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [ ]* 4.3 Write property tests for stock blocking logic
    - **Property 5: IQC result determines stock transaction state** — verify correct qc_status mapping
    - **Property 7: Blocked stock cannot be allocated to Work Orders** — verify allocation rejection
    - **Validates: Requirements 8.1, 8.2, 8.3, 8.4**

- [x] 5. Checkpoint - Core backend modules
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Attachment Module
  - [x] 6.1 Create Attachment entity, mapper, and service
    - Create `QmsAttachment.java` entity with fields: id, entityType, entityId, fileName, filePath, fileSize, fileType, uploadBy, uploadTime
    - Create `QmsAttachmentMapper.java` interface
    - Create `QmsAttachmentService.java` with methods: upload(), listByEntity(), deleteAttachment(), validateFile(), countByEntity()
    - Implement file validation: format check (JPG/PNG/PDF/DOCX/XLSX), size check (≤10MB), count check (≤10 per entity)
    - Leverage JeecgBoot's `CommonController` upload mechanism for actual file storage
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

  - [x] 6.2 Create Attachment controller
    - Create `QmsAttachmentController.java` at `/qms/attachment/*`
    - Endpoints: upload (POST multipart), list (GET by entityType + entityId), delete (DELETE)
    - Implement image compression for uploads > 10MB from mobile
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

  - [ ]* 6.3 Write property tests for attachment validation
    - **Property 12: Attachment validation constraints** — verify format/size/count rejection rules
    - **Validates: Requirements 10.2, 10.3**

- [x] 7. Notification Module
  - [x] 7.1 Create Notification entity, mapper, and service
    - Create `QmsNotification.java` entity with fields: id, userId, title, content, entityType, entityId, isRead, createTime
    - Create `QmsNotificationMapper.java` interface
    - Create `QmsNotificationService.java` with methods: sendApprovalRequest(), sendApprovalResult(), getUnreadCount(), markRead(), markAllRead(), sendReminder()
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

  - [x] 7.2 Create Notification controller and scheduled job
    - Create `QmsNotificationController.java` at `/qms/notification/*`
    - Endpoints: list (GET), unreadCount (GET), markRead/{id} (PUT), markAllRead (PUT)
    - Create `QmsNotificationScheduler.java` with `@Scheduled` method running hourly to find pending_approval > 24h and send reminders
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

  - [x] 7.3 Integrate notifications into existing approval flows
    - Modify `IqcInspectionServiceImpl.submitForApproval()` to call `notificationService.sendApprovalRequest()`
    - Modify `PqcInspectionServiceImpl.submitForApproval()` similarly
    - Modify `FqcInspectionServiceImpl.submitForApproval()` similarly
    - Modify all `approveInspection()` methods to call `notificationService.sendApprovalResult()`
    - Wrap notification calls in try-catch so failures don't roll back main operation
    - _Requirements: 11.1, 11.4_

- [x] 8. Enhanced QC Review Statistics
  - [x] 8.1 Fix syncStats and add suggestion/override to QcReviewService
    - Fix `QcReviewServiceImpl.syncStats()` to correctly count passed/failed based on `QcSessionValue.result` field
    - Add `suggestOverallResult(reviewId)` method that returns suggested result based on session outcomes
    - Add `overrideResult(reviewId, result, reason, operator)` method allowing manager override with reason
    - Add new endpoints to `QcReviewController`: `/qms/review/suggest/{id}` (GET), `/qms/review/override/{id}` (PUT)
    - _Requirements: 13.1, 13.2, 13.3, 13.4_

  - [ ]* 8.2 Write property tests for QC Review statistics
    - **Property 8: QC Review session statistics correctness** — verify total/passed/failed counts
    - **Property 9: QC Review overall result suggestion** — verify suggestion logic
    - **Property 10: Work Order has at most one QC Review** — verify uniqueness constraint
    - **Validates: Requirements 13.1, 13.2, 13.3, 6.5**

- [x] 9. Checkpoint - All backend services complete
  - Ensure all tests pass, ask the user if questions arise.

- [x] 10. Quality Analytics Dashboard - Backend
  - [x] 10.1 Create Analytics service and controller
    - Create `QmsAnalyticsService.java` with methods: getDashboardSummary(), getTrend(), getSupplierReport(), getParetoAnalysis(), exportReport()
    - Create `QmsAnalyticsServiceImpl.java` with SQL queries for aggregation
    - Create `QmsAnalyticsController.java` at `/qms/analytics/*`
    - Endpoints: dashboard (GET), trend (GET with startDate/endDate/groupBy), supplier/{id} (GET), pareto (GET), export (GET with format param)
    - Implement PDF/Excel export using JeecgBoot's export utilities
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6_

  - [ ]* 10.2 Write property tests for analytics computations
    - **Property 13: Inspection statistics computation** — verify status count sums equal total
    - **Property 14: Analytics report filtering** — verify all results satisfy filter criteria
    - **Property 15: Supplier quality metrics correctness** — verify pass rate = P/K
    - **Property 16: Pareto analysis ranking** — verify descending order and max 5 items
    - **Validates: Requirements 12.1, 12.2, 12.4, 12.6**

- [x] 11. Permission/Authorization - Backend
  - [x] 11.1 Add permission annotations to all QMS controllers
    - Add `@RequiresPermissions("qms:inspection:add")` to IQC/PQC/FQC add endpoints
    - Add `@RequiresPermissions("qms:inspection:edit")` to IQC/PQC/FQC edit endpoints
    - Add `@RequiresPermissions("qms:inspection:approve")` to all approve endpoints
    - Add `@RequiresPermissions("qms:ncr:add")` to NCR add endpoint
    - Add `@RequiresPermissions("qms:ncr:close")` to NCR close endpoint
    - Add `@RequiresPermissions("qms:template:manage")` to template/stage management endpoints
    - Add `@RequiresPermissions("qms:analytics:view")` to analytics endpoints
    - Ensure `@AutoLog` is present on all state-changing endpoints for audit trail
    - _Requirements: 14.1, 14.3_

- [x] 12. FQC Module - Frontend
  - [x] 12.1 Create FQC frontend API and views
    - Create `web/src/api/wms/fqcInspection.ts` with defHttp calls for all FQC endpoints following `qcInspection.ts` pattern
    - Create `web/src/views/qms/FqcInspectionList.vue` — paginated table with filters (status, product, date range), action buttons (view, edit, submit, approve)
    - Create `web/src/views/qms/FqcInspectionModal.vue` — form for create/edit with template selection and criteria results grid
    - Create `web/src/views/qms/FqcInspectionDetailModal.vue` — read-only detail view with results and attachments
    - Add `v-auth="'qms:inspection:approve'"` directive on approve/reject buttons
    - Register routes in router config
    - _Requirements: 7.1, 7.3, 7.4, 14.2_

- [x] 13. NCR Module - Frontend
  - [x] 13.1 Create NCR frontend API and views
    - Create `web/src/api/wms/ncr.ts` with defHttp calls for all NCR endpoints
    - Create `web/src/views/qms/NcrList.vue` — paginated table with filters (status, severity, source_type, supplier)
    - Create `web/src/views/qms/NcrModal.vue` — form for create/edit with source inspection linking, severity selection, proposed action dropdown
    - Create `web/src/views/qms/NcrDetailModal.vue` — detail view with state transition buttons, attachment list, corrective action section
    - Add `v-auth="'qms:ncr:close'"` directive on close button
    - Register routes in router config
    - _Requirements: 9.1, 9.3, 9.4, 9.5, 14.2_

- [x] 14. Attachment Component - Frontend
  - [x] 14.1 Create reusable attachment upload component
    - Create `web/src/views/qms/components/QmsAttachmentPanel.vue` — reusable component accepting `entityType` and `entityId` props
    - Implement file upload with drag-and-drop, format validation (JPG/PNG/PDF/DOCX/XLSX), size display, delete button
    - Show upload count (X/10) and disable upload when limit reached
    - Integrate into FQC, NCR, IQC, and PQC detail modals
    - _Requirements: 10.1, 10.2, 10.3_

- [x] 15. Notification UI - Frontend
  - [x] 15.1 Create notification badge and panel
    - Create `web/src/api/wms/qmsNotification.ts` with defHttp calls for notification endpoints
    - Create `web/src/views/qms/components/NotificationBadge.vue` — header badge showing unread count, polling every 30 seconds
    - Create `web/src/views/qms/components/NotificationPanel.vue` — dropdown panel listing notifications with mark-read and click-to-navigate
    - Integrate badge into QMS layout/header area
    - _Requirements: 11.1, 11.2_

- [x] 16. Quality Analytics Dashboard - Frontend
  - [x] 16.1 Create analytics dashboard view
    - Create `web/src/api/wms/qmsAnalytics.ts` with defHttp calls for analytics endpoints
    - Create `web/src/views/qms/QmsAnalyticsDashboard.vue` — dashboard with:
      - Summary cards: pass/fail ratios by type (IQC/PQC/FQC), open NCR count
      - Line chart: quality trend by week/month (ECharts)
      - Bar chart: Pareto analysis top 5 failure criteria (ECharts)
      - Filter bar: date range, inspection type, product, supplier
      - Export buttons for PDF and Excel
    - Add `v-auth="'qms:analytics:view'"` on the dashboard route/page
    - Register route in router config
    - _Requirements: 12.1, 12.2, 12.3, 12.5, 12.6, 14.2_

  - [x] 16.2 Create supplier quality report view
    - Create `web/src/views/qms/SupplierQualityReport.vue` — supplier-specific quality page with IQC pass rate, NCR history, and ranking
    - Link from NCR detail and analytics dashboard
    - _Requirements: 12.4_

- [x] 17. Enhanced QC Review - Frontend
  - [x] 17.1 Update QcReviewList.vue with suggestion and override UI
    - Add "Suggested Result" display in QC Review detail showing auto-calculated suggestion
    - Add "Override Result" button (visible only to Quản_lý_QC via `v-auth`) with reason input modal
    - Update statistics display to show corrected counts from fixed syncStats
    - _Requirements: 13.2, 13.3, 13.4, 14.2_

- [x] 18. Final checkpoint - Full integration
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The existing IQC/PQC/QC Stage/Session/Review code is NOT recreated — only new features and modifications are implemented
- Frontend views follow the existing pattern: `BasicTable` + `useTable` + `useModal` with `defHttp` API layer
- All backend services use `@Transactional(rollbackFor = Exception.class)` for state transitions
- Notification failures are caught and logged but do not roll back main operations

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["2.1", "3.1", "6.1", "7.1"] },
    { "id": 2, "tasks": ["2.2", "3.2", "6.2", "7.2"] },
    { "id": 3, "tasks": ["2.3", "3.3", "4.1", "6.3", "8.1"] },
    { "id": 4, "tasks": ["2.4", "3.4", "4.2", "7.3", "8.2"] },
    { "id": 5, "tasks": ["4.3", "10.1", "11.1"] },
    { "id": 6, "tasks": ["10.2", "12.1", "13.1", "14.1"] },
    { "id": 7, "tasks": ["15.1", "16.1", "16.2", "17.1"] }
  ]
}
```
