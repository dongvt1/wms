-- ===================================================
-- SQL INSERT bảng sys_permission
-- Modules: Warehouse, QMS, Planning
-- Date: 2026-03-02
-- ===================================================

-- ===================================================
-- MODULE: QUẢN LÝ KHO (WAREHOUSE)
-- Parent ID: 1991721042972844033 (đã tồn tại)
-- ===================================================

INSERT INTO `sys_permission`
  (`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`,
   `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`,
   `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`,
   `create_by`, `create_time`, `update_by`, `update_time`,
   `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
-- ------------------------------------------------
-- 1. Sản phẩm (Product)
-- ------------------------------------------------
('wms_menu_product',        '1991721042972844033',
 'Sản phẩm',               '/warehouse/product',
 'warehouse/product/ProductList', 1, '', NULL,
 1, NULL, '0', 2.00, 0, 'ant-design:shopping-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('wms_btn_product_add',     'wms_menu_product',
 'Thêm sản phẩm',          NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:product:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_product_edit',    'wms_menu_product',
 'Sửa sản phẩm',           NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:product:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_product_delete',  'wms_menu_product',
 'Xóa sản phẩm',           NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:product:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_product_export',  'wms_menu_product',
 'Xuất Excel sản phẩm',    NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:product:exportXls', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_product_import',  'wms_menu_product',
 'Nhập Excel sản phẩm',    NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:product:importExcel', '1', 5.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- 2. Tồn kho (Inventory)
-- ------------------------------------------------
('wms_menu_inventory',      '1991721042972844033',
 'Tồn kho',                '/warehouse/inventory',
 'warehouse/inventory/InventoryList', 1, '', NULL,
 1, NULL, '0', 3.00, 0, 'ant-design:database-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('wms_btn_inv_add',         'wms_menu_inventory',
 'Thêm tồn kho',           NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventory:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_inv_edit',        'wms_menu_inventory',
 'Sửa tồn kho',            NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventory:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_inv_delete',      'wms_menu_inventory',
 'Xóa tồn kho',            NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventory:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_inv_export',      'wms_menu_inventory',
 'Xuất Excel tồn kho',     NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventory:exportXls', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- 3. Giao dịch kho (Inventory Transaction)
-- ------------------------------------------------
('wms_menu_inv_txn',        '1991721042972844033',
 'Giao dịch kho',          '/warehouse/inventory-transaction',
 'warehouse/inventoryTransaction/InventoryTransactionList', 1, '', NULL,
 1, NULL, '0', 4.00, 0, 'ant-design:swap-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('wms_btn_invtxn_add',      'wms_menu_inv_txn',
 'Thêm giao dịch',         NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventoryTransaction:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_invtxn_edit',     'wms_menu_inv_txn',
 'Sửa giao dịch',          NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventoryTransaction:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_invtxn_delete',   'wms_menu_inv_txn',
 'Xóa giao dịch',          NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventoryTransaction:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_invtxn_export',   'wms_menu_inv_txn',
 'Xuất Excel giao dịch',   NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventoryTransaction:exportXls', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- 4. Điều chỉnh kho (Inventory Adjustment)
-- ------------------------------------------------
('wms_menu_inv_adj',        '1991721042972844033',
 'Điều chỉnh kho',         '/warehouse/inventory-adjustment',
 'warehouse/inventoryAdjustment/InventoryAdjustmentList', 1, '', NULL,
 1, NULL, '0', 5.00, 0, 'ant-design:edit-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('wms_btn_invadj_add',      'wms_menu_inv_adj',
 'Thêm điều chỉnh',        NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventoryAdjustment:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_invadj_edit',     'wms_menu_inv_adj',
 'Sửa điều chỉnh',         NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventoryAdjustment:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_invadj_delete',   'wms_menu_inv_adj',
 'Xóa điều chỉnh',         NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventoryAdjustment:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_invadj_approve',  'wms_menu_inv_adj',
 'Duyệt phiếu điều chỉnh', NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:inventoryAdjustment:approve', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- 5. Nhà cung cấp (Supplier)
-- ------------------------------------------------
('wms_menu_supplier',       '1991721042972844033',
 'Nhà cung cấp',           '/warehouse/supplier',
 'warehouse/supplier/SupplierList', 1, '', NULL,
 1, NULL, '0', 6.00, 0, 'ant-design:shop-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('wms_btn_sup_add',         'wms_menu_supplier',
 'Thêm nhà cung cấp',      NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:supplier:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_sup_edit',        'wms_menu_supplier',
 'Sửa nhà cung cấp',       NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:supplier:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_sup_delete',      'wms_menu_supplier',
 'Xóa nhà cung cấp',       NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:supplier:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- 6. Đơn đặt hàng (Purchase Order)
-- ------------------------------------------------
('wms_menu_purchase',       '1991721042972844033',
 'Đơn đặt hàng (PO)',       '/warehouse/purchase-order',
 'warehouse/purchaseOrder/PurchaseOrderList', 1, '', NULL,
 1, NULL, '0', 7.00, 0, 'ant-design:file-text-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('wms_btn_po_add',          'wms_menu_purchase',
 'Tạo đơn đặt hàng',       NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:purchaseOrder:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_po_edit',         'wms_menu_purchase',
 'Sửa đơn đặt hàng',       NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:purchaseOrder:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_po_delete',       'wms_menu_purchase',
 'Xóa đơn đặt hàng',       NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:purchaseOrder:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_po_approve',      'wms_menu_purchase',
 'Duyệt đơn đặt hàng',     NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:purchaseOrder:approve', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('wms_btn_po_export',       'wms_menu_purchase',
 'Xuất Excel đơn đặt hàng', NULL, NULL, 0, NULL, NULL,
 2, 'warehouse:purchaseOrder:exportXls', '1', 5.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- ===================================================
-- MODULE: KIỂM SOÁT CHẤT LƯỢNG (QMS)
-- ===================================================

INSERT INTO `sys_permission`
  (`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`,
   `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`,
   `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`,
   `create_by`, `create_time`, `update_by`, `update_time`,
   `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
-- ------------------------------------------------
-- QMS Root Menu
-- ------------------------------------------------
('qms_menu_root',           NULL,
 'Quản lý chất lượng (QMS)', '/qms',
 'layouts/default/index', 1, '', NULL,
 0, NULL, '0', 2.00, 0, 'ant-design:safety-certificate-outlined',
 0, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

-- ------------------------------------------------
-- QMS 1. Mẫu checklist
-- ------------------------------------------------
('qms_menu_checklist_tpl',  'qms_menu_root',
 'Mẫu kiểm tra (Checklist)', '/qms/checklist-template',
 'qms/checklistTemplate/ChecklistTemplateList', 1, '', NULL,
 1, NULL, '0', 1.00, 0, 'ant-design:file-done-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('qms_btn_cht_add',         'qms_menu_checklist_tpl',
 'Thêm mẫu checklist',     NULL, NULL, 0, NULL, NULL,
 2, 'qms:checklistTemplate:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_cht_edit',        'qms_menu_checklist_tpl',
 'Sửa mẫu checklist',      NULL, NULL, 0, NULL, NULL,
 2, 'qms:checklistTemplate:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_cht_delete',      'qms_menu_checklist_tpl',
 'Xóa mẫu checklist',      NULL, NULL, 0, NULL, NULL,
 2, 'qms:checklistTemplate:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- QMS 2. Kiểm tra đầu vào (IQC)
-- ------------------------------------------------
('qms_menu_iqc',            'qms_menu_root',
 'Kiểm tra đầu vào (IQC)', '/qms/iqc-inspection',
 'qms/iqcInspection/IqcInspectionList', 1, '', NULL,
 1, NULL, '0', 2.00, 0, 'ant-design:import-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('qms_btn_iqc_add',         'qms_menu_iqc',
 'Tạo phiếu IQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:iqcInspection:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_iqc_edit',        'qms_menu_iqc',
 'Sửa phiếu IQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:iqcInspection:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_iqc_delete',      'qms_menu_iqc',
 'Xóa phiếu IQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:iqcInspection:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_iqc_approve',     'qms_menu_iqc',
 'Duyệt phiếu IQC',        NULL, NULL, 0, NULL, NULL,
 2, 'qms:iqcInspection:approve', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_iqc_export',      'qms_menu_iqc',
 'Xuất Excel IQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:iqcInspection:exportXls', '1', 5.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- QMS 3. Kiểm tra sản xuất (PQC)
-- ------------------------------------------------
('qms_menu_pqc',            'qms_menu_root',
 'Kiểm tra sản xuất (PQC)', '/qms/pqc-inspection',
 'qms/pqcInspection/PqcInspectionList', 1, '', NULL,
 1, NULL, '0', 3.00, 0, 'ant-design:audit-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('qms_btn_pqc_add',         'qms_menu_pqc',
 'Tạo phiếu PQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:pqcInspection:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_pqc_edit',        'qms_menu_pqc',
 'Sửa phiếu PQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:pqcInspection:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_pqc_delete',      'qms_menu_pqc',
 'Xóa phiếu PQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:pqcInspection:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_pqc_approve',     'qms_menu_pqc',
 'Duyệt phiếu PQC',        NULL, NULL, 0, NULL, NULL,
 2, 'qms:pqcInspection:approve', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_pqc_export',      'qms_menu_pqc',
 'Xuất Excel PQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:pqcInspection:exportXls', '1', 5.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- QMS 3b. Kiểm tra thành phẩm (FQC)
-- ------------------------------------------------
('qms_menu_fqc',            'qms_menu_root',
 'Kiểm tra thành phẩm (FQC)', '/qms/fqc-inspection',
 'qms/FqcInspectionList', 1, '', NULL,
 1, NULL, '0', 3.50, 0, 'ant-design:export-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('qms_btn_fqc_add',         'qms_menu_fqc',
 'Tạo phiếu FQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:fqcInspection:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_fqc_edit',        'qms_menu_fqc',
 'Sửa phiếu FQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:fqcInspection:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_fqc_delete',      'qms_menu_fqc',
 'Xóa phiếu FQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:fqcInspection:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_fqc_approve',     'qms_menu_fqc',
 'Duyệt phiếu FQC',        NULL, NULL, 0, NULL, NULL,
 2, 'qms:inspection:approve', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_fqc_export',      'qms_menu_fqc',
 'Xuất Excel FQC',          NULL, NULL, 0, NULL, NULL,
 2, 'qms:fqcInspection:exportXls', '1', 5.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- QMS 4. Báo cáo chất lượng
-- ------------------------------------------------
('qms_menu_report',         'qms_menu_root',
 'Báo cáo chất lượng',     '/qms/report',
 'qms/report/QmsReportList', 1, '', NULL,
 1, NULL, '0', 4.00, 0, 'ant-design:bar-chart-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('qms_btn_rpt_view',        'qms_menu_report',
 'Xem báo cáo chất lượng', NULL, NULL, 0, NULL, NULL,
 2, 'qms:report:view', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('qms_btn_rpt_export',      'qms_menu_report',
 'Xuất báo cáo chất lượng', NULL, NULL, 0, NULL, NULL,
 2, 'qms:report:export', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- ===================================================
-- MODULE: HOẠCH ĐỊNH SẢN XUẤT (PLANNING)
-- ===================================================

INSERT INTO `sys_permission`
  (`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`,
   `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`,
   `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`,
   `create_by`, `create_time`, `update_by`, `update_time`,
   `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
-- ------------------------------------------------
-- Planning Root Menu
-- ------------------------------------------------
('pln_menu_root',           NULL,
 'Hoạch định sản xuất',    '/planning',
 'layouts/default/index', 1, '', NULL,
 0, NULL, '0', 3.00, 0, 'ant-design:project-outlined',
 0, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

-- ------------------------------------------------
-- Planning 1. Kế hoạch sản xuất (MPS)
-- ------------------------------------------------
('pln_menu_mps',            'pln_menu_root',
 'Kế hoạch sản xuất (MPS)', '/planning/production-plan',
 'planning/productionPlan/ProductionPlanList', 1, '', NULL,
 1, NULL, '0', 1.00, 0, 'ant-design:calendar-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('pln_btn_mps_add',         'pln_menu_mps',
 'Tạo kế hoạch sản xuất',  NULL, NULL, 0, NULL, NULL,
 2, 'planning:productionPlan:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_mps_edit',        'pln_menu_mps',
 'Sửa kế hoạch sản xuất',  NULL, NULL, 0, NULL, NULL,
 2, 'planning:productionPlan:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_mps_delete',      'pln_menu_mps',
 'Xóa kế hoạch sản xuất',  NULL, NULL, 0, NULL, NULL,
 2, 'planning:productionPlan:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_mps_approve',     'pln_menu_mps',
 'Duyệt kế hoạch sản xuất', NULL, NULL, 0, NULL, NULL,
 2, 'planning:productionPlan:approve', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_mps_export',      'pln_menu_mps',
 'Xuất Excel kế hoạch',    NULL, NULL, 0, NULL, NULL,
 2, 'planning:productionPlan:exportXls', '1', 5.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- Planning 2. Lệnh sản xuất (Work Order)
-- ------------------------------------------------
('pln_menu_wo',             'pln_menu_root',
 'Lệnh sản xuất (WO)',     '/planning/work-order',
 'planning/workOrder/WorkOrderList', 1, '', NULL,
 1, NULL, '0', 2.00, 0, 'ant-design:tool-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('pln_btn_wo_add',          'pln_menu_wo',
 'Tạo lệnh sản xuất',      NULL, NULL, 0, NULL, NULL,
 2, 'planning:workOrder:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_wo_edit',         'pln_menu_wo',
 'Sửa lệnh sản xuất',      NULL, NULL, 0, NULL, NULL,
 2, 'planning:workOrder:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_wo_delete',       'pln_menu_wo',
 'Xóa lệnh sản xuất',      NULL, NULL, 0, NULL, NULL,
 2, 'planning:workOrder:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_wo_release',      'pln_menu_wo',
 'Phát hành lệnh SX',      NULL, NULL, 0, NULL, NULL,
 2, 'planning:workOrder:release', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_wo_complete',     'pln_menu_wo',
 'Hoàn thành lệnh SX',     NULL, NULL, 0, NULL, NULL,
 2, 'planning:workOrder:complete', '1', 5.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_wo_export',       'pln_menu_wo',
 'Xuất Excel lệnh SX',     NULL, NULL, 0, NULL, NULL,
 2, 'planning:workOrder:exportXls', '1', 6.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- Planning 3. BOM (Bill of Materials)
-- ------------------------------------------------
('pln_menu_bom',            'pln_menu_root',
 'Định mức nguyên liệu (BOM)', '/planning/bom',
 'planning/bom/BomList', 1, '', NULL,
 1, NULL, '0', 3.00, 0, 'ant-design:partition-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('pln_btn_bom_add',         'pln_menu_bom',
 'Thêm BOM',               NULL, NULL, 0, NULL, NULL,
 2, 'planning:bom:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_bom_edit',        'pln_menu_bom',
 'Sửa BOM',                NULL, NULL, 0, NULL, NULL,
 2, 'planning:bom:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_bom_delete',      'pln_menu_bom',
 'Xóa BOM',                NULL, NULL, 0, NULL, NULL,
 2, 'planning:bom:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_bom_export',      'pln_menu_bom',
 'Xuất Excel BOM',         NULL, NULL, 0, NULL, NULL,
 2, 'planning:bom:exportXls', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- Planning 4. Nhu cầu vật tư (MRP)
-- ------------------------------------------------
('pln_menu_mrp',            'pln_menu_root',
 'Nhu cầu vật tư (MRP)',   '/planning/mrp',
 'planning/mrp/MrpList', 1, '', NULL,
 1, NULL, '0', 4.00, 0, 'ant-design:funnel-plot-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('pln_btn_mrp_run',         'pln_menu_mrp',
 'Chạy tính toán MRP',     NULL, NULL, 0, NULL, NULL,
 2, 'planning:mrp:run', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_mrp_view',        'pln_menu_mrp',
 'Xem kết quả MRP',        NULL, NULL, 0, NULL, NULL,
 2, 'planning:mrp:view', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_mrp_export',      'pln_menu_mrp',
 'Xuất Excel MRP',         NULL, NULL, 0, NULL, NULL,
 2, 'planning:mrp:exportXls', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- Planning 5. Báo cáo sản xuất
-- ------------------------------------------------
('pln_menu_report',         'pln_menu_root',
 'Báo cáo sản xuất',       '/planning/report',
 'planning/report/PlanningReportList', 1, '', NULL,
 1, NULL, '0', 5.00, 0, 'ant-design:area-chart-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('pln_btn_rpt_view',        'pln_menu_report',
 'Xem báo cáo sản xuất',   NULL, NULL, 0, NULL, NULL,
 2, 'planning:report:view', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('pln_btn_rpt_export',      'pln_menu_report',
 'Xuất báo cáo sản xuất',  NULL, NULL, 0, NULL, NULL,
 2, 'planning:report:export', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- ===================================================
-- MODULE: QUẢN LÝ VẬT TƯ CHUNG (COMMON MATERIAL)
-- Date: 2026-03-05
-- ===================================================

INSERT INTO `sys_permission`
  (`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`,
   `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`,
   `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`,
   `create_by`, `create_time`, `update_by`, `update_time`,
   `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
-- ------------------------------------------------
-- Common Root Menu
-- ------------------------------------------------
('cmn_menu_root',           NULL,
 'Dữ liệu chung',          '/common',
 'layouts/default/index', 1, '', NULL,
 0, NULL, '0', 1.00, 0, 'ant-design:appstore-outlined',
 0, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

-- ------------------------------------------------
-- Common 1. Quản lý vật tư (Material)
-- ------------------------------------------------
('cmn_menu_material',       'cmn_menu_root',
 'Quản lý vật tư',         '/common/material',
 'common/material/MaterialList', 1, '', NULL,
 1, NULL, '0', 1.00, 0, 'ant-design:gold-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('cmn_btn_mat_add',         'cmn_menu_material',
 'Thêm vật tư',            NULL, NULL, 0, NULL, NULL,
 2, 'common:material:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_mat_edit',        'cmn_menu_material',
 'Sửa vật tư',             NULL, NULL, 0, NULL, NULL,
 2, 'common:material:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_mat_delete',      'cmn_menu_material',
 'Xóa vật tư',             NULL, NULL, 0, NULL, NULL,
 2, 'common:material:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_mat_export',      'cmn_menu_material',
 'Xuất Excel vật tư',      NULL, NULL, 0, NULL, NULL,
 2, 'common:material:exportXls', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_mat_import',      'cmn_menu_material',
 'Nhập Excel vật tư',      NULL, NULL, 0, NULL, NULL,
 2, 'common:material:importExcel', '1', 5.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- Common 2. Vật tư thay thế (Material Substitute)
-- ------------------------------------------------
('cmn_menu_mat_sub',        'cmn_menu_root',
 'Vật tư thay thế',        '/common/material-substitute',
 'common/material/MaterialSubstituteList', 1, '', NULL,
 1, NULL, '0', 2.00, 0, 'ant-design:retweet-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('cmn_btn_matsub_add',      'cmn_menu_mat_sub',
 'Thêm vật tư thay thế',   NULL, NULL, 0, NULL, NULL,
 2, 'common:materialSubstitute:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_matsub_edit',     'cmn_menu_mat_sub',
 'Sửa vật tư thay thế',    NULL, NULL, 0, NULL, NULL,
 2, 'common:materialSubstitute:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_matsub_delete',   'cmn_menu_mat_sub',
 'Xóa vật tư thay thế',    NULL, NULL, 0, NULL, NULL,
 2, 'common:materialSubstitute:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- Common 3. Sản phẩm (Product - chung)
-- ------------------------------------------------
('cmn_menu_product',        'cmn_menu_root',
 'Sản phẩm',               '/common/product',
 'common/product/ProductList', 1, '', NULL,
 1, NULL, '0', 3.00, 0, 'ant-design:shopping-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('cmn_btn_prd_add',         'cmn_menu_product',
 'Thêm sản phẩm',          NULL, NULL, 0, NULL, NULL,
 2, 'common:product:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_prd_edit',        'cmn_menu_product',
 'Sửa sản phẩm',           NULL, NULL, 0, NULL, NULL,
 2, 'common:product:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_prd_delete',      'cmn_menu_product',
 'Xóa sản phẩm',           NULL, NULL, 0, NULL, NULL,
 2, 'common:product:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_prd_export',      'cmn_menu_product',
 'Xuất Excel sản phẩm',    NULL, NULL, 0, NULL, NULL,
 2, 'common:product:exportXls', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

-- ------------------------------------------------
-- Common 4. BOM (Bill of Materials - chung)
-- ------------------------------------------------
('cmn_menu_bom',            'cmn_menu_root',
 'Định mức BOM',           '/common/bom',
 'common/bom/BomList', 1, '', NULL,
 1, NULL, '0', 4.00, 0, 'ant-design:partition-outlined',
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, NULL, 0),

('cmn_btn_bom_add',         'cmn_menu_bom',
 'Thêm BOM',               NULL, NULL, 0, NULL, NULL,
 2, 'common:bom:add', '1', 1.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_bom_edit',        'cmn_menu_bom',
 'Sửa BOM',                NULL, NULL, 0, NULL, NULL,
 2, 'common:bom:edit', '1', 2.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_bom_delete',      'cmn_menu_bom',
 'Xóa BOM',                NULL, NULL, 0, NULL, NULL,
 2, 'common:bom:delete', '1', 3.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),

('cmn_btn_bom_export',      'cmn_menu_bom',
 'Xuất Excel BOM',         NULL, NULL, 0, NULL, NULL,
 2, 'common:bom:exportXls', '1', 4.00, 0, NULL,
 1, 0, 0, 0, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);
