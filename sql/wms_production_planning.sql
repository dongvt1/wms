-- ===================================================
-- Epic 5: Production Planning Module
-- Date: 2026-02-25
-- ===================================================

-- Production Line (Dây chuyền sản xuất)
CREATE TABLE IF NOT EXISTS `wh_production_line` (
    `id`                 VARCHAR(36)     NOT NULL COMMENT 'ID',
    `line_code`          VARCHAR(50)     NOT NULL COMMENT 'Mã dây chuyền',
    `line_name`          VARCHAR(200)    NOT NULL COMMENT 'Tên dây chuyền',
    `description`        TEXT            NULL COMMENT 'Mô tả',
    `capacity_per_day`   DECIMAL(10,2)   NULL COMMENT 'Năng suất/ngày',
    `unit`               VARCHAR(20)     NULL COMMENT 'Đơn vị năng suất',
    `status`             VARCHAR(20)     NOT NULL DEFAULT 'active' COMMENT 'Trạng thái: active, inactive, maintenance',
    `create_by`          VARCHAR(50)     NULL COMMENT 'Người tạo',
    `create_time`        DATETIME        NULL COMMENT 'Thời gian tạo',
    `update_by`          VARCHAR(50)     NULL COMMENT 'Người cập nhật',
    `update_time`        DATETIME        NULL COMMENT 'Thời gian cập nhật',
    `sys_org_code`       VARCHAR(64)     NULL COMMENT 'Mã tổ chức',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_line_code` (`line_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dây chuyền sản xuất';

-- BOM Header (Định mức nguyên vật liệu)
CREATE TABLE IF NOT EXISTS `wh_bom` (
    `id`                 VARCHAR(36)     NOT NULL COMMENT 'ID',
    `bom_code`           VARCHAR(50)     NOT NULL COMMENT 'Mã BOM',
    `bom_name`           VARCHAR(200)    NOT NULL COMMENT 'Tên BOM',
    `product_id`         VARCHAR(36)     NOT NULL COMMENT 'ID thành phẩm (FK product)',
    `output_quantity`    DECIMAL(10,3)   NOT NULL DEFAULT 1 COMMENT 'Số lượng thành phẩm đầu ra',
    `unit`               VARCHAR(20)     NULL COMMENT 'Đơn vị thành phẩm',
    `version`            VARCHAR(20)     NOT NULL DEFAULT '1.0' COMMENT 'Phiên bản BOM',
    `status`             VARCHAR(20)     NOT NULL DEFAULT 'active' COMMENT 'Trạng thái: active, inactive',
    `notes`              TEXT            NULL COMMENT 'Ghi chú',
    `create_by`          VARCHAR(50)     NULL COMMENT 'Người tạo',
    `create_time`        DATETIME        NULL COMMENT 'Thời gian tạo',
    `update_by`          VARCHAR(50)     NULL COMMENT 'Người cập nhật',
    `update_time`        DATETIME        NULL COMMENT 'Thời gian cập nhật',
    `sys_org_code`       VARCHAR(64)     NULL COMMENT 'Mã tổ chức',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bom_code` (`bom_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Định mức nguyên vật liệu (BOM)';

-- BOM Items (Chi tiết NVL trong BOM)
CREATE TABLE IF NOT EXISTS `wh_bom_item` (
    `id`                 VARCHAR(36)     NOT NULL COMMENT 'ID',
    `bom_id`             VARCHAR(36)     NOT NULL COMMENT 'ID BOM',
    `material_id`        VARCHAR(36)     NOT NULL COMMENT 'ID nguyên vật liệu (FK product)',
    `quantity`           DECIMAL(10,3)   NOT NULL COMMENT 'Số lượng NVL cần',
    `unit`               VARCHAR(20)     NULL COMMENT 'Đơn vị',
    `notes`              TEXT            NULL COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    KEY `idx_bom_id` (`bom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chi tiết nguyên vật liệu trong BOM';

-- Work Order (Lệnh sản xuất)
CREATE TABLE IF NOT EXISTS `wh_work_order` (
    `id`                 VARCHAR(36)     NOT NULL COMMENT 'ID',
    `order_code`         VARCHAR(50)     NOT NULL COMMENT 'Mã lệnh sản xuất',
    `bom_id`             VARCHAR(36)     NOT NULL COMMENT 'ID BOM sử dụng',
    `production_line_id` VARCHAR(36)     NULL COMMENT 'ID dây chuyền',
    `planned_quantity`   DECIMAL(10,3)   NOT NULL COMMENT 'Số lượng kế hoạch',
    `actual_quantity`    DECIMAL(10,3)   NULL COMMENT 'Số lượng thực tế',
    `planned_start_date` DATE            NULL COMMENT 'Ngày bắt đầu kế hoạch',
    `planned_end_date`   DATE            NULL COMMENT 'Ngày kết thúc kế hoạch',
    `actual_start_date`  DATE            NULL COMMENT 'Ngày bắt đầu thực tế',
    `actual_end_date`    DATE            NULL COMMENT 'Ngày kết thúc thực tế',
    `status`             VARCHAR(30)     NOT NULL DEFAULT 'draft' COMMENT 'Trạng thái: draft, planned, in_progress, completed, cancelled',
    `priority`           VARCHAR(20)     NOT NULL DEFAULT 'normal' COMMENT 'Ưu tiên: low, normal, high, urgent',
    `notes`              TEXT            NULL COMMENT 'Ghi chú',
    `create_by`          VARCHAR(50)     NULL COMMENT 'Người tạo',
    `create_time`        DATETIME        NULL COMMENT 'Thời gian tạo',
    `update_by`          VARCHAR(50)     NULL COMMENT 'Người cập nhật',
    `update_time`        DATETIME        NULL COMMENT 'Thời gian cập nhật',
    `sys_org_code`       VARCHAR(64)     NULL COMMENT 'Mã tổ chức',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_code` (`order_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lệnh sản xuất (Work Order)';

-- Production Stage (Công đoạn sản xuất)
CREATE TABLE IF NOT EXISTS `wh_production_stage` (
    `id`                      VARCHAR(36)     NOT NULL COMMENT 'ID',
    `work_order_id`           VARCHAR(36)     NOT NULL COMMENT 'ID lệnh sản xuất',
    `stage_name`              VARCHAR(200)    NOT NULL COMMENT 'Tên công đoạn',
    `stage_order`             INT             NOT NULL COMMENT 'Thứ tự công đoạn',
    `planned_duration_hours`  DECIMAL(8,2)    NULL COMMENT 'Thời gian kế hoạch (giờ)',
    `actual_duration_hours`   DECIMAL(8,2)    NULL COMMENT 'Thời gian thực tế (giờ)',
    `status`                  VARCHAR(20)     NOT NULL DEFAULT 'pending' COMMENT 'Trạng thái: pending, in_progress, completed, skipped',
    `assignee`                VARCHAR(100)    NULL COMMENT 'Người phụ trách',
    `notes`                   TEXT            NULL COMMENT 'Ghi chú',
    `create_time`             DATETIME        NULL COMMENT 'Thời gian tạo',
    `update_time`             DATETIME        NULL COMMENT 'Thời gian cập nhật',
    PRIMARY KEY (`id`),
    KEY `idx_work_order_id` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Công đoạn sản xuất';

-- Production Log (Nhật ký sản xuất)
CREATE TABLE IF NOT EXISTS `wh_production_log` (
    `id`             VARCHAR(36)     NOT NULL COMMENT 'ID',
    `work_order_id`  VARCHAR(36)     NOT NULL COMMENT 'ID lệnh sản xuất',
    `stage_id`       VARCHAR(36)     NULL COMMENT 'ID công đoạn',
    `log_time`       DATETIME        NOT NULL COMMENT 'Thời gian ghi nhận',
    `action`         VARCHAR(100)    NULL COMMENT 'Hành động',
    `quantity`       DECIMAL(10,3)   NULL COMMENT 'Số lượng',
    `operator`       VARCHAR(100)    NULL COMMENT 'Người thực hiện',
    `notes`          TEXT            NULL COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    KEY `idx_work_order_id` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhật ký sản xuất';
