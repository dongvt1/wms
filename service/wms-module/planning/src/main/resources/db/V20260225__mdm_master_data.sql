-- =====================================================
-- Master Data Management (MDM) – Migration Script
-- Date: 2026-02-25
-- Description: BOM multi-level tree, Work Center, Routing
-- =====================================================

-- 1. Enhance wh_bom_item for multi-level BOM tree + Lead Time
ALTER TABLE wh_bom_item ADD COLUMN child_bom_id VARCHAR(36) NULL COMMENT 'ID BOM con (bán thành phẩm)' AFTER material_id;
ALTER TABLE wh_bom_item ADD COLUMN item_type VARCHAR(20) DEFAULT 'raw_material' COMMENT 'Loại: raw_material, sub_assembly' AFTER child_bom_id;
ALTER TABLE wh_bom_item ADD COLUMN purchase_lead_time_days INT NULL COMMENT 'Thời gian mua hàng (ngày)' AFTER unit;
ALTER TABLE wh_bom_item ADD COLUMN wastage_rate DECIMAL(8,4) DEFAULT 0 COMMENT 'Tỷ lệ hao hụt (%)' AFTER purchase_lead_time_days;

ALTER TABLE wh_bom_item ADD INDEX idx_bom_item_child_bom (child_bom_id);
ALTER TABLE wh_bom_item ADD INDEX idx_bom_item_type (item_type);

-- 2. Work Center (Trung tâm sản xuất)
CREATE TABLE IF NOT EXISTS wh_work_center (
    id VARCHAR(36) NOT NULL COMMENT 'ID',
    center_code VARCHAR(50) NOT NULL COMMENT 'Mã trung tâm sản xuất',
    center_name VARCHAR(200) NOT NULL COMMENT 'Tên trung tâm sản xuất',
    center_type VARCHAR(20) DEFAULT 'machine' COMMENT 'Loại: machine, labor_team, production_line',
    production_line_id VARCHAR(36) NULL COMMENT 'FK dây chuyền',
    capacity_per_hour DECIMAL(12,4) NULL COMMENT 'Năng suất tối đa/giờ',
    capacity_per_day DECIMAL(12,4) NULL COMMENT 'Năng suất tối đa/ngày',
    capacity_unit VARCHAR(20) NULL COMMENT 'Đơn vị năng suất',
    cost_per_hour DECIMAL(12,2) NULL COMMENT 'Chi phí/giờ',
    setup_time_minutes INT NULL COMMENT 'Thời gian setup (phút)',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'Trạng thái: active, inactive, maintenance',
    description VARCHAR(500) NULL COMMENT 'Mô tả',
    create_by VARCHAR(50) NULL COMMENT 'Người tạo',
    create_time DATETIME NULL COMMENT 'Thời gian tạo',
    update_by VARCHAR(50) NULL COMMENT 'Người cập nhật',
    update_time DATETIME NULL COMMENT 'Thời gian cập nhật',
    PRIMARY KEY (id),
    UNIQUE KEY uk_center_code (center_code),
    KEY idx_production_line (production_line_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Trung tâm sản xuất (Work Center)';

-- 3. Routing (Quy trình công nghệ)
CREATE TABLE IF NOT EXISTS wh_routing (
    id VARCHAR(36) NOT NULL COMMENT 'ID',
    routing_code VARCHAR(50) NOT NULL COMMENT 'Mã quy trình',
    routing_name VARCHAR(200) NOT NULL COMMENT 'Tên quy trình',
    product_id VARCHAR(36) NULL COMMENT 'FK sản phẩm',
    bom_id VARCHAR(36) NULL COMMENT 'FK BOM',
    version VARCHAR(20) DEFAULT '1.0' COMMENT 'Phiên bản',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'Trạng thái: active, inactive, draft',
    total_lead_time_hours DECIMAL(10,2) NULL COMMENT 'Tổng thời gian SX (giờ)',
    notes VARCHAR(500) NULL COMMENT 'Ghi chú',
    create_by VARCHAR(50) NULL COMMENT 'Người tạo',
    create_time DATETIME NULL COMMENT 'Thời gian tạo',
    update_by VARCHAR(50) NULL COMMENT 'Người cập nhật',
    update_time DATETIME NULL COMMENT 'Thời gian cập nhật',
    PRIMARY KEY (id),
    UNIQUE KEY uk_routing_code (routing_code),
    KEY idx_product_id (product_id),
    KEY idx_bom_id (bom_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Quy trình công nghệ (Routing)';

-- 4. Routing Step (Bước trong quy trình)
CREATE TABLE IF NOT EXISTS wh_routing_step (
    id VARCHAR(36) NOT NULL COMMENT 'ID',
    routing_id VARCHAR(36) NOT NULL COMMENT 'FK quy trình',
    step_order INT NOT NULL DEFAULT 1 COMMENT 'Thứ tự bước',
    step_name VARCHAR(200) NOT NULL COMMENT 'Tên bước',
    work_center_id VARCHAR(36) NULL COMMENT 'FK trung tâm sản xuất',
    setup_time_minutes INT DEFAULT 0 COMMENT 'Thời gian chuẩn bị (phút)',
    run_time_minutes INT DEFAULT 0 COMMENT 'Thời gian chạy/đơn vị (phút)',
    wait_time_minutes INT DEFAULT 0 COMMENT 'Thời gian chờ (phút)',
    move_time_minutes INT DEFAULT 0 COMMENT 'Thời gian di chuyển (phút)',
    lead_time_hours DECIMAL(10,2) NULL COMMENT 'Tổng lead time bước (giờ)',
    description VARCHAR(500) NULL COMMENT 'Mô tả',
    PRIMARY KEY (id),
    KEY idx_routing_id (routing_id),
    KEY idx_step_order (routing_id, step_order),
    KEY idx_work_center (work_center_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bước trong quy trình công nghệ (Routing Step)';
