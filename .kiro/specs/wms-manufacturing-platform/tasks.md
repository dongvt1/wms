# Implementation Plan: WMS Manufacturing Platform

## Overview

Triển khai nền tảng WMS Manufacturing Platform với ba module tích hợp: Lập kế hoạch sản xuất (Planning), Quản lý kho (Warehouse), và Quản lý chất lượng (QMS). Xây dựng trên nền tảng JeecgBoot hiện có (Spring Boot + MyBatis-Plus + Shiro/JWT), sử dụng Vue 3 + Ant Design Vue cho frontend, MySQL cho database. Tích hợp cross-module qua Spring ApplicationEvent.

## Tasks

- [ ] 1. Thiết lập Database Schema và Master Data
  - [ ] 1.1 Tạo migration scripts cho các bảng Master Data (mdm_material, mdm_product, mdm_supplier, mdm_supplier_material)
    - Tạo file SQL migration cho 4 bảng master data
    - Bao gồm indexes và constraints theo design
    - _Requirements: 12.1, 12.2, 14.1_

  - [ ] 1.2 Tạo migration scripts cho các bảng Planning (pl_bom, pl_bom_item, pl_bom_item_substitute, pl_wo_material_substitution, pl_wo_step, ALTER pl_work_order)
    - Tạo file SQL cho bảng BOM header và line items
    - Tạo bảng `pl_bom_item_substitute` (vật tư thay thế many-to-many) với UNIQUE KEY (bom_item_id, substitute_material_id) và index priority
    - Tạo bảng `pl_wo_material_substitution` (ghi nhận vật tư thay thế đã chọn cho WO) với indexes trên work_order_id, bom_item_id, substitute_material_id
    - ALTER TABLE pl_work_order thêm các cột mới bao gồm `selected_bom_version VARCHAR(20)` cho phép chọn phiên bản BOM cụ thể
    - Tạo bảng pl_wo_step
    - _Requirements: 1.1, 1.7, 1.8, 2.1, 2.2, 2.9, 5.1_

  - [ ] 1.3 Tạo migration scripts cho các bảng Warehouse (wh_warehouse, wh_location, wh_receipt, wh_receipt_item, wh_issue, wh_issue_item, wh_lot, wh_stock, wh_stock_adjustment, wh_alert)
    - Tạo file SQL cho 10 bảng warehouse
    - Bao gồm generated column (available_qty) và indexes
    - _Requirements: 6.1, 7.1, 8.1, 8.5_

- [ ] 2. Implement Master Data Module (Backend)
  - [ ] 2.1 Tạo Entity classes và Mapper cho Master Data
    - Tạo `MdmMaterial`, `MdmProduct`, `MdmSupplier`, `MdmSupplierMaterial` entities
    - Tạo MyBatis-Plus Mapper interfaces
    - Sử dụng JeecgBoot base entity pattern (`JeecgEntity`)
    - _Requirements: 12.1, 12.2, 14.1_

  - [ ] 2.2 Implement MasterDataService và SupplierService
    - CRUD operations cho Material, Product
    - Validation mã trùng lặp (unique code check)
    - Import từ Excel với validation và error reporting
    - Lọc inactive materials khỏi selection lists
    - _Requirements: 12.1, 12.2, 12.4, 12.5, 12.6_

  - [ ]* 2.3 Write property test: Master data code uniqueness (Property 25)
    - **Property 25: Master data code uniqueness**
    - Verify duplicate code rejection cho material, product, supplier
    - **Validates: Requirements 12.4**

  - [ ]* 2.4 Write property test: Inactive material filtering (Property 26)
    - **Property 26: Inactive material filtering**
    - Verify inactive materials không xuất hiện trong selection lists
    - **Validates: Requirements 12.6**

  - [ ] 2.5 Implement SupplierService với performance metrics
    - CRUD operations cho Supplier
    - Tính toán iqc_pass_rate, ncr_count, avg_lead_time
    - Liên kết supplier-material (nhiều NCC cho 1 vật tư)
    - _Requirements: 14.1, 14.2, 14.5_

  - [ ]* 2.6 Write property test: Supplier performance metrics (Property 29)
    - **Property 29: Supplier performance metrics correctness**
    - Verify iqc_pass_rate = (P/K) × 100 cho K inspections, P passed
    - **Validates: Requirements 14.2**

  - [ ]* 2.7 Write property test: Supplier quality alert threshold (Property 30)
    - **Property 30: Supplier quality alert threshold**
    - Verify alert generated khi iqc_pass_rate < 80% trong 3 tháng
    - **Validates: Requirements 14.3**

  - [ ] 2.8 Implement MasterDataController và SupplierController
    - REST endpoints theo design: `/mdm/material/*`, `/mdm/product/*`, `/mdm/supplier/*`
    - Excel import endpoint với multipart upload
    - Supplier performance và history endpoints
    - _Requirements: 12.1, 12.2, 12.5, 14.1, 14.4_

- [ ] 3. Implement BOM Module (Backend)
  - [ ] 3.1 Tạo Entity classes cho BOM (PlBom, PlBomItem)
    - Entity với JeecgBoot annotations
    - Mapper interfaces
    - _Requirements: 1.1, 1.2_

  - [ ] 3.2 Implement BomService - Core BOM logic
    - createBom (draft status)
    - activateBom (auto-obsolete BOM cũ cùng product)
    - deleteBom (kiểm tra WO references)
    - calculateRequirements (line_qty × Q × (1 + wastage_rate))
    - getActiveBomByProduct
    - getBomByVersion(productId, version) - trả về BOM theo phiên bản cụ thể
    - CRUD cho BOM items
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 2.2_

  - [ ] 3.2a Implement BomService - Substitute material CRUD
    - addSubstituteMaterial(bomItemId, substituteMaterialId, conversionRatio, priority): thêm vật tư thay thế, validate đơn vị tính hoặc conversion ratio hợp lệ
    - removeSubstituteMaterial(substituteId): xóa vật tư thay thế
    - getSubstitutesForBomItem(bomItemId): danh sách vật tư thay thế theo priority
    - validateSubstituteMaterial(bomItemId, substituteMaterialId, conversionRatio): kiểm tra cùng đơn vị hoặc ratio > 0
    - Tạo Entity class `PlBomItemSubstitute` với JeecgBoot annotations và Mapper interface
    - _Requirements: 1.7, 1.8, 1.9_

  - [ ]* 3.3 Write property test: BOM active uniqueness invariant (Property 2)
    - **Property 2: BOM active uniqueness invariant**
    - Verify mỗi product chỉ có tối đa 1 BOM active tại mọi thời điểm
    - **Validates: Requirements 1.3, 1.4**

  - [ ]* 3.4 Write property test: BOM deletion protection (Property 3)
    - **Property 3: BOM deletion protection**
    - Verify BOM có WO tham chiếu không thể xóa
    - **Validates: Requirements 1.5**

  - [ ]* 3.5 Write property test: Material requirement calculation (Property 4)
    - **Property 4: Material requirement calculation correctness**
    - Verify requirement = line_qty × Q × (1 + wastage_rate) cho mọi line item
    - **Validates: Requirements 1.6, 2.3**

  - [ ]* 3.5a Write property test: Substitute material many-to-many relationship integrity (Property 32)
    - **Property 32: Substitute material many-to-many relationship integrity**
    - Verify substitute linked to N BOM lines → querying substitutes from each BOM line includes that material; BOM line with M substitutes → querying all BOM lines for each substitute includes original BOM line
    - **Validates: Requirements 1.8**

  - [ ]* 3.5b Write property test: Substitute material unit validation (Property 33)
    - **Property 33: Substitute material unit validation**
    - Verify adding substitute succeeds only if same unit OR conversion ratio > 0; reject if different unit and ratio ≤ 0
    - **Validates: Requirements 1.9**

  - [ ] 3.6 Implement BomController
    - REST endpoints: `/planning/bom/*`
    - Bao gồm activate, calculateRequirements, items CRUD
    - Thêm endpoints cho substitute CRUD: `/planning/bom/items/substitutes/{bomItemId}` (GET), `/planning/bom/items/substitutes/add` (POST), `/planning/bom/items/substitutes/edit` (PUT), `/planning/bom/items/substitutes/delete/{id}` (DELETE)
    - _Requirements: 1.1, 1.2, 1.3, 1.6, 1.7, 1.8, 1.9_

- [ ] 4. Checkpoint - Verify Master Data và BOM modules
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement Routing Module (Backend)
  - [ ] 5.1 Tạo Entity classes cho Routing (mở rộng entity hiện có)
    - Mở rộng Routing entity với status field
    - Tạo PlWoStep entity
    - _Requirements: 5.1, 5.2_

  - [ ] 5.2 Implement RoutingService
    - activateRouting (deactivate routing cũ cùng product)
    - getActiveRoutingByProduct
    - instantiateForWorkOrder (tạo WO step instances từ template)
    - Steps CRUD và reorder
    - _Requirements: 5.1, 5.2, 5.3_

  - [ ]* 5.3 Write property test: Routing step instantiation (Property 12)
    - **Property 12: Routing step instantiation**
    - Verify N routing steps → exactly N WO step instances với đúng thứ tự
    - **Validates: Requirements 5.3**

  - [ ]* 5.4 Write property test: Step progression with QC gate (Property 13)
    - **Property 13: Step progression with QC gate**
    - Verify step có qc_required=true không thể completed nếu qc_passed≠true
    - **Validates: Requirements 5.4, 5.5**

  - [ ] 5.5 Implement RoutingController
    - REST endpoints: `/planning/routing/*`
    - Bao gồm activate, steps CRUD, reorder
    - _Requirements: 5.1, 5.2_

- [ ] 6. Implement Work Order Module (Backend)
  - [ ] 6.1 Mở rộng WorkOrder Entity với các cột mới
    - Thêm fields: routing_id, planned_quantity, actual_quantity, passed_quantity, defect_quantity, completion_pct, is_overdue, product_id
    - Mapper interface updates
    - _Requirements: 2.1, 2.8_

  - [ ] 6.2 Implement WorkOrderService - Core logic
    - createWorkOrder(wo): validate BOM active + Routing active, auto-link BOM active, tính NVL
    - createWorkOrder(wo, bomVersion): tạo WO với phiên bản BOM cụ thể (lưu vào selected_bom_version), validate version tồn tại
    - Code generation: WOyyyyMMddNNN format
    - State machine: draft→planned→in_progress→completed→closed, draft/planned→cancelled
    - startProduction: kiểm tra tồn kho, publish WorkOrderStartedEvent
    - completeProduction: publish WorkOrderCompletedEvent
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [ ] 6.2a Implement WorkOrderService - Material substitution logic
    - checkMaterialWithSubstitutes(woId): kiểm tra tồn kho NVL, nếu thiếu trả về danh sách substitutes khả dụng kèm stock levels, ordered by priority
    - selectSubstituteMaterial(woId, bomItemId, substituteMaterialId): ghi nhận vật tư thay thế, tính lại qty = original_qty × conversion_ratio, lưu vào pl_wo_material_substitution
    - getSubstitutions(woId): danh sách vật tư thay thế đã chọn cho WO
    - Tạo Entity class `PlWoMaterialSubstitution` với JeecgBoot annotations và Mapper interface
    - _Requirements: 2.8, 2.9_

  - [ ]* 6.3 Write property test: Document code generation (Property 1)
    - **Property 1: Document code generation format and uniqueness**
    - Verify WO/GRN/GIN codes match PREFIXyyyyMMddNNN format, no duplicates same day
    - **Validates: Requirements 2.1, 6.1, 7.1**

  - [ ]* 6.4 Write property test: Work Order state machine (Property 5)
    - **Property 5: Work Order state machine validity**
    - Verify chỉ transitions hợp lệ được chấp nhận, invalid transitions bị reject
    - **Validates: Requirements 2.4**

  - [ ]* 6.5 Write property test: WO creation requires BOM + Routing (Property 6)
    - **Property 6: Work Order creation requires active BOM and Routing**
    - Verify WO creation fails nếu thiếu BOM hoặc Routing active
    - **Validates: Requirements 2.5**

  - [ ]* 6.5a Write property test: BOM version selection for Work Order (Property 34)
    - **Property 34: BOM version selection for Work Order**
    - Verify WO without version → link active BOM; WO with explicit version → link that specific BOM regardless of status (draft/active/obsolete)
    - **Validates: Requirements 2.2**

  - [ ]* 6.5b Write property test: Substitute material availability display (Property 35)
    - **Property 35: Substitute material availability display when primary insufficient**
    - Verify khi primary material stock < required qty AND BOM line has N substitutes → material check returns all N substitutes with stock levels, ordered by priority ascending
    - **Validates: Requirements 2.8**

  - [ ]* 6.5c Write property test: Substitute material quantity recalculation (Property 36)
    - **Property 36: Substitute material quantity recalculation**
    - Verify substituted_quantity = original_qty × conversion_ratio; substitution record preserves original material, substitute material, ratio, and both quantities
    - **Validates: Requirements 2.9**

  - [ ] 6.6 Implement WorkOrderService - Progress tracking
    - reportProgress: cập nhật step, tính completion percentage
    - calculateCompletion: actual_qty / planned_qty × 100
    - getOverdueOrders: WO có planned_end_date < now AND status ∈ {planned, in_progress}
    - calculateOee: availability × performance × quality
    - _Requirements: 4.1, 4.2, 4.4, 4.5_

  - [ ]* 6.7 Write property test: Completion percentage calculation (Property 9)
    - **Property 9: Completion percentage calculation**
    - Verify completion = (A / P) × 100, clamped to [0, 100]
    - **Validates: Requirements 4.2**

  - [ ]* 6.8 Write property test: Overdue detection (Property 10)
    - **Property 10: Overdue detection**
    - Verify WO flagged overdue khi current_date > planned_end_date AND status ∈ {planned, in_progress}
    - **Validates: Requirements 4.4**

  - [ ]* 6.9 Write property test: OEE calculation (Property 11)
    - **Property 11: OEE calculation correctness**
    - Verify OEE = A × P × Q với đúng công thức
    - **Validates: Requirements 4.5**

  - [ ] 6.10 Implement WorkOrderController
    - REST endpoints: `/planning/workOrder/*`
    - Bao gồm transition, progress, materialCheck, dashboard, overdue
    - Thêm endpoints: `/planning/workOrder/materialCheck/{id}/substitutes` (GET), `/planning/workOrder/selectSubstitute` (POST), `/planning/workOrder/substitutions/{id}` (GET)
    - Hỗ trợ tham số bomVersion khi tạo WO (POST `/planning/workOrder/add`)
    - _Requirements: 2.1, 2.2, 2.4, 2.8, 2.9, 4.1, 4.3_

- [ ] 7. Implement Scheduling Module (Backend)
  - [ ] 7.1 Implement ScheduleService
    - getGanttData: WO list với timeline data, filters
    - reschedule: cập nhật lịch, kiểm tra conflict
    - detectConflicts: tìm WO trùng lịch cùng dây chuyền
    - getCapacityUtilization: tính % sử dụng năng lực theo ngày
    - suggestAlternativeSlots: đề xuất khung giờ trống
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [ ]* 7.2 Write property test: Resource conflict detection (Property 7)
    - **Property 7: Resource conflict detection**
    - Verify 2 WO cùng production line với date ranges overlap → conflict detected
    - **Validates: Requirements 3.3**

  - [ ]* 7.3 Write property test: Plan filtering correctness (Property 8)
    - **Property 8: Plan filtering correctness**
    - Verify tất cả WO trả về thỏa mãn ALL filter conditions
    - **Validates: Requirements 3.5**

  - [ ]* 7.4 Write property test: Capacity utilization calculation (Property 31)
    - **Property 31: Capacity utilization calculation**
    - Verify utilization = Σ(planned_hours) / line_capacity, flag khi > 100%
    - **Validates: Requirements 3.6, 3.7**

  - [ ] 7.5 Implement ScheduleController
    - REST endpoints: `/planning/schedule/*`
    - Bao gồm gantt, calendar, reschedule, conflicts, capacity
    - _Requirements: 3.1, 3.2, 3.3, 3.6_

- [ ] 8. Checkpoint - Verify Planning modules (BOM, Routing, WO, Schedule)
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. Implement Stock & Location Module (Backend)
  - [ ] 9.1 Tạo Entity classes cho Warehouse (WhWarehouse, WhLocation, WhStock, WhLot, WhStockAdjustment, WhAlert)
    - Entities với JeecgBoot annotations
    - Mapper interfaces
    - WhLocation hỗ trợ phân cấp 4 level (parent_id)
    - _Requirements: 8.1, 8.2_

  - [ ] 9.2 Implement StockService
    - getStockByMaterial, getStockByLocation, getStockByLot
    - increaseStock, decreaseStock (transactional)
    - checkAvailability (trừ blocked và reserved)
    - adjustStock (yêu cầu reason, ghi lịch sử)
    - checkSafetyStock (tạo alert nếu < min_level)
    - calculateKpi (utilization, turnover, value)
    - Optimistic locking cho concurrent updates
    - _Requirements: 8.2, 8.4, 8.5, 8.6, 8.7_

  - [ ]* 9.3 Write property test: Multi-dimensional stock consistency (Property 20)
    - **Property 20: Multi-dimensional stock consistency**
    - Verify Σ(stock_by_location[material]) = total_stock[material]
    - **Validates: Requirements 8.2**

  - [ ]* 9.4 Write property test: Safety stock alert generation (Property 21)
    - **Property 21: Safety stock alert generation**
    - Verify alert generated khi available stock < min_stock_level
    - **Validates: Requirements 8.4**

  - [ ] 9.5 Implement LocationService
    - Quản lý cấu trúc phân cấp (tree)
    - Warehouse map data (pos_x, pos_y)
    - Location CRUD
    - _Requirements: 8.1, 8.3_

  - [ ] 9.6 Implement StockController và LocationController
    - REST endpoints: `/warehouse/stock/*`, `/warehouse/location/*`
    - Bao gồm byMaterial, byLocation, byLot, adjust, alerts, kpi
    - Location tree và map endpoints
    - _Requirements: 8.1, 8.2, 8.3, 8.7_

- [ ] 10. Implement Receipt Module (Backend)
  - [ ] 10.1 Tạo Entity classes cho Receipt (WhReceipt, WhReceiptItem)
    - Entities với JeecgBoot annotations
    - Mapper interfaces
    - _Requirements: 6.1, 6.2_

  - [ ] 10.2 Implement ReceiptService
    - generateCode: GRNyyyyMMddNNN format
    - createReceipt (draft)
    - confirmReceipt: transactional stock update, tạo IQC nếu từ NCC
    - createFromWorkOrder: tạo phiếu nhập thành phẩm từ WO hoàn thành
    - handleIqcResult: unblock stock nếu IQC pass
    - Barcode scan lookup
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

  - [ ]* 10.3 Write property test: Stock transaction correctness - receipt (Property 14)
    - **Property 14: Stock transaction correctness (receipt)**
    - Verify stock tăng đúng bằng receipt item quantity tại mỗi location
    - **Validates: Requirements 6.3**

  - [ ] 10.4 Implement ReceiptController
    - REST endpoints: `/warehouse/receipt/*`
    - Bao gồm confirm, items, scanBarcode
    - _Requirements: 6.1, 6.2, 6.6_

- [ ] 11. Implement Issue Module (Backend)
  - [ ] 11.1 Tạo Entity classes cho Issue (WhIssue, WhIssueItem)
    - Entities với JeecgBoot annotations
    - Mapper interfaces
    - _Requirements: 7.1_

  - [ ] 11.2 Implement IssueService
    - generateCode: GINyyyyMMddNNN format
    - createIssue (draft)
    - confirmIssue: kiểm tra tồn kho đủ, kiểm tra QC status, trừ tồn kho (transactional)
    - suggestMaterialsFromBom: lấy BOM items với số lượng cần xuất
    - suggestLotsFifo: đề xuất lots theo receipt_date tăng dần
    - createFromWorkOrder: tạo phiếu xuất NVL draft khi WO bắt đầu
    - validateQcStatus: kiểm tra lot không bị blocked/conditional_hold
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_

  - [ ]* 11.3 Write property test: Stock transaction correctness - issue (Property 15)
    - **Property 15: Stock transaction correctness (issue)**
    - Verify stock giảm đúng bằng issue item quantity tại mỗi location
    - **Validates: Requirements 7.4**

  - [ ]* 11.4 Write property test: BOM-based issue suggestion (Property 16)
    - **Property 16: BOM-based issue suggestion**
    - Verify suggested list có đúng N materials với qty = bom_line_qty × wo_qty × (1 + wastage)
    - **Validates: Requirements 7.2**

  - [ ]* 11.5 Write property test: FIFO lot ordering (Property 17)
    - **Property 17: FIFO lot ordering**
    - Verify lots ordered by receipt_date ascending
    - **Validates: Requirements 7.3**

  - [ ]* 11.6 Write property test: Issue rejection exceeding stock (Property 18)
    - **Property 18: Issue rejection when exceeding available stock**
    - Verify confirmation rejected khi requested > available
    - **Validates: Requirements 7.5**

  - [ ]* 11.7 Write property test: QC-blocked material rejection (Property 19)
    - **Property 19: QC-blocked material cannot be issued**
    - Verify lot có qc_status ∈ {blocked, conditional_hold} bị reject khi issue
    - **Validates: Requirements 7.6**

  - [ ] 11.8 Implement IssueController
    - REST endpoints: `/warehouse/issue/*`
    - Bao gồm confirm, suggestFromBom, suggestLots
    - _Requirements: 7.1, 7.2, 7.3_

- [ ] 12. Checkpoint - Verify Warehouse modules (Stock, Receipt, Issue)
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. Implement Cross-Module Integration (Spring Events)
  - [ ] 13.1 Implement Event classes và Event Listeners
    - WorkOrderStartedEvent, WorkOrderCompletedEvent
    - WarehouseEventListener: onWorkOrderStarted → tạo Issue draft, onWorkOrderCompleted → tạo Receipt draft
    - QmsEventListener: onPqcFailed → cập nhật defect count trên WO
    - Sử dụng @TransactionalEventListener(phase = AFTER_COMMIT)
    - _Requirements: 10.1, 10.2, 10.3, 10.6_

  - [ ] 13.2 Implement AlertService
    - Safety stock alerts
    - Overdue WO alerts
    - Supplier quality alerts
    - Material shortage early warning
    - _Requirements: 8.4, 4.4, 14.3, 10.5_

  - [ ]* 13.3 Write property test: Material shortage early warning (Property 24)
    - **Property 24: Material shortage early warning**
    - Verify warning generated khi required qty > available stock cho planned WO
    - **Validates: Requirements 10.5**

- [ ] 14. Implement Traceability Module (Backend)
  - [ ] 14.1 Implement TraceabilityService
    - traceForward: NVL lot → Issue → WO → Receipt → Thành phẩm lot
    - traceBackward: Thành phẩm lot → Receipt → WO → Issue → NVL lots → Supplier
    - search: tìm theo lot code, product code, WO code, receipt/issue code
    - identifyAffectedProducts: tìm tất cả thành phẩm sử dụng lot NVL
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

  - [ ]* 14.2 Write property test: Traceability chain integrity (Property 22)
    - **Property 22: Traceability chain integrity**
    - Verify backward trace trả về all raw material lots, suppliers, QC results
    - **Validates: Requirements 9.1, 9.2**

  - [ ]* 14.3 Write property test: Reverse traceability completeness (Property 23)
    - **Property 23: Reverse traceability completeness**
    - Verify forward trace trả về ALL finished goods lots từ WOs sử dụng material lot
    - **Validates: Requirements 9.3, 9.5**

  - [ ] 14.4 Implement TraceabilityController
    - REST endpoints: `/traceability/*`
    - Bao gồm forward, backward, search, recall
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [ ] 15. Implement Report Module (Backend)
  - [ ] 15.1 Implement ReportService
    - getProductionOutput: sản lượng theo ngày/tuần/tháng/SP/dây chuyền
    - calculateOee: OEE = availability × performance × quality
    - getOnTimeDeliveryRate: % WO hoàn thành đúng hạn
    - getScrapRate: % phế phẩm
    - getInventorySummary: giá trị tồn, vòng quay, slow-moving, expiring
    - exportReport: tạo file PDF/Excel (Apache POI + iText/OpenPDF)
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

  - [ ] 15.2 Implement ReportController
    - REST endpoints: `/report/*`
    - Bao gồm production output, kpi, trend, inventory, export, dashboard
    - Performance: response within 3 seconds cho 10,000+ records
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_

- [ ] 16. Implement Authorization & Audit (Backend)
  - [ ] 16.1 Implement Permission configuration và Audit logging
    - Cấu hình Shiro/JWT permissions cho các vai trò: Quản_lý_Sản_xuất, Thủ_kho, Quản_lý_QC, Nhân_viên_Sản_xuất, Admin
    - Implement AuditLogAspect (AOP) ghi nhận: operator, timestamp, action, data before/after
    - Permission annotations trên controllers
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

  - [ ]* 16.2 Write property test: Authorization enforcement (Property 27)
    - **Property 27: Authorization enforcement**
    - Verify request không có quyền → HTTP 403, không modify data
    - **Validates: Requirements 13.2, 13.4**

  - [ ]* 16.3 Write property test: Audit log completeness (Property 28)
    - **Property 28: Audit log completeness**
    - Verify mọi data modification → audit log entry với đầy đủ thông tin
    - **Validates: Requirements 13.3**

- [ ] 17. Checkpoint - Verify all Backend modules
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 18. Implement Frontend - Master Data & BOM Views
  - [ ] 18.1 Tạo Master Data pages (Material, Product, Supplier)
    - Vue 3 + Ant Design Vue components
    - CRUD forms với validation
    - Excel import UI với progress và error display
    - Supplier performance dashboard
    - _Requirements: 12.1, 12.2, 12.5, 14.1, 14.4_

  - [ ] 18.2 Tạo BOM management pages
    - BOM list với filter/search
    - BOM form (header + line items dynamic table)
    - Activate/obsolete actions
    - Material requirement calculator
    - **Substitute material management UI**: thêm/sửa/xóa vật tư thay thế cho mỗi dòng BOM, hiển thị conversion ratio và priority, validation đơn vị tính
    - _Requirements: 1.1, 1.2, 1.3, 1.6, 1.7, 1.8, 1.9_

- [ ] 19. Implement Frontend - Planning Views
  - [ ] 19.1 Tạo Work Order management pages
    - WO list với status badges, filters
    - WO creation form (auto-link BOM, validate BOM+Routing)
    - **BOM version selection**: dropdown cho phép chọn phiên bản BOM cụ thể khi tạo WO (mặc định active)
    - State transition buttons với confirmation
    - Material check dialog (hiển thị shortages)
    - **Substitute material selection UI**: khi NVL thiếu, hiển thị danh sách vật tư thay thế khả dụng kèm tồn kho, cho phép chọn thay thế, hiển thị số lượng quy đổi
    - **Substitution history**: hiển thị danh sách vật tư thay thế đã chọn cho WO
    - Progress reporting form (per step)
    - _Requirements: 2.1, 2.2, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 4.1_

  - [ ] 19.2 Tạo Routing management pages
    - Routing list và form
    - Steps management (drag-drop reorder)
    - QC stage linking
    - _Requirements: 5.1, 5.2_

  - [ ] 19.3 Tạo Scheduling views (Gantt chart, Calendar)
    - Gantt chart component (sử dụng thư viện như dhtmlx-gantt hoặc vue-gantt)
    - Calendar view theo tuần/tháng
    - Drag-drop reschedule
    - Conflict warning display
    - Capacity utilization bar chart
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.6, 3.7_

  - [ ] 19.4 Tạo Production Dashboard
    - Số lệnh đang thực hiện, tỷ lệ hoàn thành, số lệnh trễ hạn
    - Biểu đồ tiến độ theo ngày
    - So sánh kế hoạch vs thực tế
    - OEE gauge chart
    - _Requirements: 4.3, 4.5, 4.6_

- [ ] 20. Implement Frontend - Warehouse Views
  - [ ] 20.1 Tạo Receipt management pages
    - Receipt list với status filter
    - Receipt form (header + line items)
    - Confirm action với stock update feedback
    - Barcode scan input
    - IQC status indicator
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.6_

  - [ ] 20.2 Tạo Issue management pages
    - Issue list với status filter
    - Issue form (header + line items)
    - BOM-based material suggestion
    - FIFO lot suggestion với override option
    - QC status validation feedback
    - _Requirements: 7.1, 7.2, 7.3, 7.5, 7.6_

  - [ ] 20.3 Tạo Stock & Location management pages
    - Stock views: by material, by location, by lot
    - Warehouse map 2D (canvas/SVG component)
    - Location tree (Ant Design Tree component)
    - Stock adjustment form (yêu cầu reason)
    - Safety stock alerts panel
    - KPI dashboard (utilization, turnover, value)
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

- [ ] 21. Implement Frontend - Traceability & Reports
  - [ ] 21.1 Tạo Traceability pages
    - Search form (lot code, product code, WO code)
    - Forward/backward trace visualization (tree/graph)
    - Recall affected products list
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

  - [ ] 21.2 Tạo Report & Analytics pages
    - Production output report với filters
    - KPI cards (OEE, on-time, scrap rate)
    - Trend charts (ECharts/Chart.js)
    - Inventory reports (summary, slow-moving, expiring)
    - PDF/Excel export buttons
    - Combined dashboard (3 modules)
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 10.4_

- [ ] 22. Implement Frontend - Permission-based UI
  - [ ] 22.1 Implement permission-based menu và button visibility
    - Route guards dựa trên user roles
    - v-permission directive cho buttons/actions
    - Menu filtering theo role
    - _Requirements: 13.1, 13.2_

- [ ] 23. Checkpoint - Verify Frontend integration
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 24. Integration Testing và Final Wiring
  - [ ] 24.1 Write integration tests cho cross-module flows
    - WO lifecycle end-to-end: create → plan → start → progress → complete
    - WO creation with explicit BOM version selection (selected_bom_version)
    - Material substitution flow: primary stock insufficient → show substitutes → select substitute → recalculate qty → create issue with substitute material
    - BOM substitute CRUD: add/edit/delete substitutes, verify many-to-many links
    - Receipt confirmation → stock update → IQC trigger
    - Issue confirmation → stock deduction → FIFO enforcement
    - WO start → auto-create draft issue (event-driven)
    - WO complete → auto-create draft receipt (event-driven)
    - PQC failure → WO defect count update
    - Traceability chain: full chain forward/backward queries
    - _Requirements: 1.7, 1.8, 1.9, 2.2, 2.8, 2.9, 10.1, 10.2, 10.3, 10.6_

  - [ ] 24.2 Wire all modules together và verify end-to-end flows
    - Verify Spring Event propagation
    - Verify transaction boundaries across modules
    - Verify error handling và rollback scenarios
    - _Requirements: 10.6_

- [ ] 25. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties (36 properties defined in design)
- Unit tests validate specific examples and edge cases
- Backend sử dụng Java (JeecgBoot: Spring Boot + MyBatis-Plus + Shiro/JWT)
- Frontend sử dụng Vue 3 + Ant Design Vue + TypeScript
- Database: MySQL với InnoDB engine
- Property-based tests sử dụng jqwik library
- Properties 32-36 cover material substitution feature (many-to-many integrity, unit validation, BOM version selection, substitute availability display, quantity recalculation)
- Cross-module integration qua Spring ApplicationEvent (synchronous, same JVM)
- Tất cả stock operations PHẢI trong @Transactional
- Code generation sử dụng SELECT ... FOR UPDATE cho concurrency safety

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3"] },
    { "id": 1, "tasks": ["2.1", "9.1"] },
    { "id": 2, "tasks": ["2.2", "2.5", "3.1", "5.1", "9.5"] },
    { "id": 3, "tasks": ["2.3", "2.4", "2.6", "2.7", "2.8", "3.2", "3.2a", "5.2", "9.2"] },
    { "id": 4, "tasks": ["3.3", "3.4", "3.5", "3.5a", "3.5b", "3.6", "5.3", "5.4", "5.5", "9.3", "9.4", "9.6"] },
    { "id": 5, "tasks": ["6.1", "10.1", "11.1"] },
    { "id": 6, "tasks": ["6.2", "6.2a", "6.6", "10.2", "11.2"] },
    { "id": 7, "tasks": ["6.3", "6.4", "6.5", "6.5a", "6.5b", "6.5c", "6.7", "6.8", "6.9", "6.10", "10.3", "10.4", "11.3", "11.4", "11.5", "11.6", "11.7", "11.8"] },
    { "id": 8, "tasks": ["7.1", "13.1", "13.2"] },
    { "id": 9, "tasks": ["7.2", "7.3", "7.4", "7.5", "13.3"] },
    { "id": 10, "tasks": ["14.1", "15.1", "16.1"] },
    { "id": 11, "tasks": ["14.2", "14.3", "14.4", "15.2", "16.2", "16.3"] },
    { "id": 12, "tasks": ["18.1", "18.2", "19.1", "19.2"] },
    { "id": 13, "tasks": ["19.3", "19.4", "20.1", "20.2"] },
    { "id": 14, "tasks": ["20.3", "21.1", "21.2", "22.1"] },
    { "id": 15, "tasks": ["24.1"] },
    { "id": 16, "tasks": ["24.2"] }
  ]
}
```
