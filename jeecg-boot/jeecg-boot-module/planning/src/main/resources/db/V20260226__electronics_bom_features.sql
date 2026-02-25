-- =====================================================
-- Electronics BOM Management – Migration Script
-- Date: 2026-02-26
-- Description: Item Master, RefDes, AVL/AML, Revision & ECN
-- =====================================================

-- =====================================================
-- 1. Item Master / Part Catalog (Danh mục linh kiện)
-- =====================================================
CREATE TABLE IF NOT EXISTS wh_item_master (
    id              VARCHAR(36)   NOT NULL COMMENT 'ID',
    ipn             VARCHAR(50)   NOT NULL COMMENT 'Internal Part Number – Mã nội bộ',
    mpn             VARCHAR(100)  NULL     COMMENT 'Manufacturer Part Number – Mã nhà sản xuất',
    manufacturer_name VARCHAR(200) NULL    COMMENT 'Tên nhà sản xuất',
    category        VARCHAR(50)   NULL     COMMENT 'Danh mục: resistor, capacitor, ic, connector, pcb...',
    value           VARCHAR(50)   NULL     COMMENT 'Trị số: 10k Ohm, 10uF, 3.3V...',
    tolerance       VARCHAR(20)   NULL     COMMENT 'Dung sai: 1%, 5%, 10%...',
    voltage         VARCHAR(20)   NULL     COMMENT 'Điện áp định mức: 16V, 50V...',
    package_type    VARCHAR(30)   NULL     COMMENT 'Kiểu đóng gói: 0402, 0603, QFN-48, SOIC-8...',
    lifecycle_status VARCHAR(20)  DEFAULT 'active' COMMENT 'Vòng đời: active, obsolete, nrnd (not recommended for new design)',
    datasheet_url   VARCHAR(500)  NULL     COMMENT 'URL hoặc đường dẫn file datasheet (PDF)',
    drawing_2d_url  VARCHAR(500)  NULL     COMMENT 'URL bản vẽ 2D',
    drawing_3d_url  VARCHAR(500)  NULL     COMMENT 'URL bản vẽ 3D (STEP/IGES)',
    description     VARCHAR(500)  NULL     COMMENT 'Mô tả chi tiết',
    specifications  TEXT          NULL     COMMENT 'Thông số kỹ thuật bổ sung (JSON)',
    create_by       VARCHAR(50)   NULL     COMMENT 'Người tạo',
    create_time     DATETIME      NULL     COMMENT 'Thời gian tạo',
    update_by       VARCHAR(50)   NULL     COMMENT 'Người cập nhật',
    update_time     DATETIME      NULL     COMMENT 'Thời gian cập nhật',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ipn (ipn),
    KEY idx_mpn (mpn),
    KEY idx_category (category),
    KEY idx_lifecycle (lifecycle_status),
    KEY idx_manufacturer (manufacturer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh mục linh kiện điện tử (Item Master)';

-- =====================================================
-- 2. Reference Designators (Vị trí trên bo mạch)
-- =====================================================
CREATE TABLE IF NOT EXISTS wh_bom_item_refdes (
    id              VARCHAR(36)   NOT NULL COMMENT 'ID',
    bom_item_id     VARCHAR(36)   NOT NULL COMMENT 'FK tới wh_bom_item.id',
    ref_designator  VARCHAR(20)   NOT NULL COMMENT 'Ký hiệu vị trí: C1, R5, U3, Q2...',
    position_x      DECIMAL(10,4) NULL     COMMENT 'Toạ độ X trên PCB (mm)',
    position_y      DECIMAL(10,4) NULL     COMMENT 'Toạ độ Y trên PCB (mm)',
    rotation        DECIMAL(6,2)  NULL     COMMENT 'Góc xoay (độ)',
    layer           VARCHAR(10)   DEFAULT 'top' COMMENT 'Layer: top, bottom',
    PRIMARY KEY (id),
    KEY idx_bom_item (bom_item_id),
    UNIQUE KEY uk_bom_item_refdes (bom_item_id, ref_designator)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vị trí linh kiện trên PCB (Reference Designators)';

-- Quick-access field on bom_item
ALTER TABLE wh_bom_item ADD COLUMN ref_designators TEXT NULL COMMENT 'Danh sách RefDes, phân cách bởi dấu phẩy: C1,C5,C12' AFTER notes;

-- =====================================================
-- 3. Approved Manufacturer List (AML)
-- =====================================================
CREATE TABLE IF NOT EXISTS wh_approved_manufacturer (
    id                  VARCHAR(36)   NOT NULL COMMENT 'ID',
    item_master_id      VARCHAR(36)   NOT NULL COMMENT 'FK tới wh_item_master.id',
    manufacturer_name   VARCHAR(200)  NOT NULL COMMENT 'Tên nhà sản xuất',
    mpn                 VARCHAR(100)  NOT NULL COMMENT 'MPN của hãng này',
    priority            INT           DEFAULT 1 COMMENT 'Thứ tự ưu tiên (1 = cao nhất)',
    status              VARCHAR(20)   DEFAULT 'approved' COMMENT 'Trạng thái: approved, pending, rejected',
    qualification_date  DATE          NULL     COMMENT 'Ngày đạt chứng nhận',
    notes               VARCHAR(500)  NULL     COMMENT 'Ghi chú',
    create_by           VARCHAR(50)   NULL     COMMENT 'Người tạo',
    create_time         DATETIME      NULL     COMMENT 'Thời gian tạo',
    update_by           VARCHAR(50)   NULL     COMMENT 'Người cập nhật',
    update_time         DATETIME      NULL     COMMENT 'Thời gian cập nhật',
    PRIMARY KEY (id),
    KEY idx_item_master (item_master_id),
    KEY idx_priority (item_master_id, priority),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh sách nhà sản xuất được phê duyệt (AML)';

-- =====================================================
-- 4. Approved Vendor List (AVL)
-- =====================================================
CREATE TABLE IF NOT EXISTS wh_approved_vendor (
    id              VARCHAR(36)    NOT NULL COMMENT 'ID',
    item_master_id  VARCHAR(36)    NOT NULL COMMENT 'FK tới wh_item_master.id',
    vendor_name     VARCHAR(200)   NOT NULL COMMENT 'Tên nhà cung cấp',
    vendor_code     VARCHAR(50)    NULL     COMMENT 'Mã nhà cung cấp',
    priority        INT            DEFAULT 1 COMMENT 'Thứ tự ưu tiên (1 = cao nhất)',
    lead_time_days  INT            NULL     COMMENT 'Thời gian giao hàng (ngày)',
    moq             INT            NULL     COMMENT 'Minimum Order Quantity',
    unit_price      DECIMAL(12,4)  NULL     COMMENT 'Đơn giá',
    currency        VARCHAR(10)    DEFAULT 'VND' COMMENT 'Đơn vị tiền tệ',
    status          VARCHAR(20)    DEFAULT 'approved' COMMENT 'Trạng thái: approved, pending, rejected',
    notes           VARCHAR(500)   NULL     COMMENT 'Ghi chú',
    create_by       VARCHAR(50)    NULL     COMMENT 'Người tạo',
    create_time     DATETIME       NULL     COMMENT 'Thời gian tạo',
    update_by       VARCHAR(50)    NULL     COMMENT 'Người cập nhật',
    update_time     DATETIME       NULL     COMMENT 'Thời gian cập nhật',
    PRIMARY KEY (id),
    KEY idx_item_master (item_master_id),
    KEY idx_priority (item_master_id, priority),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh sách nhà cung cấp được phê duyệt (AVL)';

-- =====================================================
-- 5. BOM Revision (Lịch sử phiên bản BOM)
-- =====================================================
CREATE TABLE IF NOT EXISTS wh_bom_revision (
    id              VARCHAR(36)   NOT NULL COMMENT 'ID',
    bom_id          VARCHAR(36)   NOT NULL COMMENT 'FK tới wh_bom.id',
    revision_code   VARCHAR(20)   NOT NULL COMMENT 'Mã phiên bản: v1.0, v1.1, v2.0',
    snapshot_data   LONGTEXT      NULL     COMMENT 'JSON snapshot toàn bộ BOM tại thời điểm lưu',
    reason          VARCHAR(500)  NULL     COMMENT 'Lý do tạo phiên bản',
    status          VARCHAR(20)   DEFAULT 'active' COMMENT 'Trạng thái: active, superseded',
    created_by_ecn  VARCHAR(36)   NULL     COMMENT 'FK tới wh_ecn.id nếu tạo từ ECN',
    create_by       VARCHAR(50)   NULL     COMMENT 'Người tạo',
    create_time     DATETIME      NULL     COMMENT 'Thời gian tạo',
    update_by       VARCHAR(50)   NULL     COMMENT 'Người cập nhật',
    update_time     DATETIME      NULL     COMMENT 'Thời gian cập nhật',
    PRIMARY KEY (id),
    KEY idx_bom (bom_id),
    KEY idx_bom_revision (bom_id, revision_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử phiên bản BOM (Revision Control)';

-- =====================================================
-- 6. Engineering Change Notice (ECN)
-- =====================================================
CREATE TABLE IF NOT EXISTS wh_ecn (
    id              VARCHAR(36)   NOT NULL COMMENT 'ID',
    ecn_code        VARCHAR(50)   NOT NULL COMMENT 'Mã ECN',
    title           VARCHAR(200)  NOT NULL COMMENT 'Tiêu đề thay đổi',
    description     TEXT          NULL     COMMENT 'Mô tả chi tiết',
    bom_id          VARCHAR(36)   NOT NULL COMMENT 'FK tới wh_bom.id',
    from_revision   VARCHAR(20)   NULL     COMMENT 'Phiên bản BOM trước thay đổi',
    to_revision     VARCHAR(20)   NULL     COMMENT 'Phiên bản BOM sau thay đổi',
    status          VARCHAR(20)   DEFAULT 'draft' COMMENT 'Trạng thái: draft, pending, approved, rejected, applied',
    requested_by    VARCHAR(50)   NULL     COMMENT 'Người yêu cầu',
    approved_by     VARCHAR(50)   NULL     COMMENT 'Người phê duyệt cuối cùng',
    approved_date   DATETIME      NULL     COMMENT 'Ngày phê duyệt',
    applied_date    DATETIME      NULL     COMMENT 'Ngày áp dụng vào BOM',
    create_by       VARCHAR(50)   NULL     COMMENT 'Người tạo',
    create_time     DATETIME      NULL     COMMENT 'Thời gian tạo',
    update_by       VARCHAR(50)   NULL     COMMENT 'Người cập nhật',
    update_time     DATETIME      NULL     COMMENT 'Thời gian cập nhật',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ecn_code (ecn_code),
    KEY idx_bom (bom_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Engineering Change Notice (Thông báo thay đổi kỹ thuật)';

-- =====================================================
-- 7. ECN Item (Chi tiết thay đổi)
-- =====================================================
CREATE TABLE IF NOT EXISTS wh_ecn_item (
    id              VARCHAR(36)   NOT NULL COMMENT 'ID',
    ecn_id          VARCHAR(36)   NOT NULL COMMENT 'FK tới wh_ecn.id',
    change_type     VARCHAR(20)   NOT NULL COMMENT 'Loại thay đổi: add, remove, modify',
    bom_item_id     VARCHAR(36)   NULL     COMMENT 'FK tới wh_bom_item.id (cho modify/remove)',
    old_material_id VARCHAR(36)   NULL     COMMENT 'ID linh kiện cũ',
    new_material_id VARCHAR(36)   NULL     COMMENT 'ID linh kiện mới',
    old_quantity    DECIMAL(12,4) NULL     COMMENT 'Số lượng cũ',
    new_quantity    DECIMAL(12,4) NULL     COMMENT 'Số lượng mới',
    reason          VARCHAR(500)  NULL     COMMENT 'Lý do thay đổi dòng này',
    PRIMARY KEY (id),
    KEY idx_ecn (ecn_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chi tiết thay đổi trong ECN';

-- =====================================================
-- 8. ECN Approval (Quy trình phê duyệt)
-- =====================================================
CREATE TABLE IF NOT EXISTS wh_ecn_approval (
    id              VARCHAR(36)   NOT NULL COMMENT 'ID',
    ecn_id          VARCHAR(36)   NOT NULL COMMENT 'FK tới wh_ecn.id',
    department      VARCHAR(50)   NOT NULL COMMENT 'Bộ phận: production, procurement, quality, engineering',
    approver_id     VARCHAR(50)   NULL     COMMENT 'ID người duyệt',
    approver_name   VARCHAR(100)  NULL     COMMENT 'Tên người duyệt',
    status          VARCHAR(20)   DEFAULT 'pending' COMMENT 'Trạng thái: pending, approved, rejected',
    comments        VARCHAR(500)  NULL     COMMENT 'Nhận xét',
    approved_date   DATETIME      NULL     COMMENT 'Ngày phê duyệt',
    create_by       VARCHAR(50)   NULL     COMMENT 'Người tạo',
    create_time     DATETIME      NULL     COMMENT 'Thời gian tạo',
    update_by       VARCHAR(50)   NULL     COMMENT 'Người cập nhật',
    update_time     DATETIME      NULL     COMMENT 'Thời gian cập nhật',
    PRIMARY KEY (id),
    KEY idx_ecn (ecn_id),
    KEY idx_department (ecn_id, department),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phê duyệt ECN theo bộ phận';
