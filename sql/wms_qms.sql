-- ===================================================
-- Epic 6: Quality Management System (QMS)
-- Date: 2026-02-25 | Updated: 2026-02-26 (prefix wh_ → qms_)
-- ===================================================

-- 1. Checklist Template (Mẫu bộ tiêu chí kiểm tra)
CREATE TABLE IF NOT EXISTS `qms_checklist_template` (
    `id`               VARCHAR(36)   NOT NULL COMMENT 'ID',
    `template_code`    VARCHAR(50)   NOT NULL COMMENT 'Mã mẫu',
    `template_name`    VARCHAR(200)  NOT NULL COMMENT 'Tên mẫu checklist',
    `inspection_type`  VARCHAR(10)   NOT NULL COMMENT 'Loại kiểm tra: iqc, pqc',
    `product_id`       VARCHAR(36)   NULL     COMMENT 'ID sản phẩm áp dụng (NULL = dùng chung)',
    `status`           VARCHAR(20)   NOT NULL DEFAULT 'active' COMMENT 'active, inactive',
    `notes`            TEXT          NULL     COMMENT 'Ghi chú',
    `create_by`        VARCHAR(50)   NULL,
    `create_time`      DATETIME      NULL,
    `update_by`        VARCHAR(50)   NULL,
    `update_time`      DATETIME      NULL,
    `sys_org_code`     VARCHAR(64)   NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Mẫu bộ tiêu chí kiểm tra chất lượng';

-- 2. Checklist Item (Chi tiết tiêu chí trong mẫu)
CREATE TABLE IF NOT EXISTS `qms_checklist_item` (
    `id`               VARCHAR(36)   NOT NULL COMMENT 'ID',
    `template_id`      VARCHAR(36)   NOT NULL COMMENT 'FK → qms_checklist_template',
    `item_order`       INT           NOT NULL DEFAULT 1 COMMENT 'Thứ tự tiêu chí',
    `criterion_name`   VARCHAR(200)  NOT NULL COMMENT 'Tên tiêu chí kiểm tra',
    `standard_value`   VARCHAR(200)  NULL     COMMENT 'Giá trị tiêu chuẩn / yêu cầu',
    `input_type`       VARCHAR(20)   NOT NULL DEFAULT 'pass_fail' COMMENT 'text, number, pass_fail, select',
    `options`          VARCHAR(500)  NULL     COMMENT 'JSON options nếu input_type=select',
    `is_required`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '1=bắt buộc, 0=không bắt buộc',
    `notes`            TEXT          NULL,
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chi tiết tiêu chí trong mẫu checklist';

-- 3. IQC Inspection (Phiếu kiểm tra chất lượng đầu vào)
CREATE TABLE IF NOT EXISTS `qms_iqc_inspection` (
    `id`                    VARCHAR(36)    NOT NULL COMMENT 'ID',
    `inspection_code`       VARCHAR(50)    NOT NULL COMMENT 'Mã phiếu IQC (IQCyyyyMMddNNN)',
    `product_id`            VARCHAR(36)    NOT NULL COMMENT 'FK → product',
    `supplier_id`           VARCHAR(36)    NULL     COMMENT 'FK → supplier',
    `stock_transaction_id`  VARCHAR(36)    NULL     COMMENT 'FK → wh_stock_transaction (phiếu nhập kho)',
    `template_id`           VARCHAR(36)    NULL     COMMENT 'FK → qms_checklist_template',
    `quantity_received`     DECIMAL(10,3)  NOT NULL DEFAULT 0 COMMENT 'Số lượng nhận',
    `quantity_passed`       DECIMAL(10,3)  NULL     COMMENT 'Số lượng đạt',
    `quantity_failed`       DECIMAL(10,3)  NULL     COMMENT 'Số lượng không đạt',
    `inspector`             VARCHAR(100)   NULL     COMMENT 'Người kiểm tra',
    `inspection_date`       DATE           NULL     COMMENT 'Ngày kiểm tra',
    `status`                VARCHAR(20)    NOT NULL DEFAULT 'draft' COMMENT 'draft, in_progress, pending_approval, passed, failed, conditional',
    `notes`                 TEXT           NULL,
    `create_by`             VARCHAR(50)    NULL,
    `create_time`           DATETIME       NULL,
    `update_by`             VARCHAR(50)    NULL,
    `update_time`           DATETIME       NULL,
    `sys_org_code`          VARCHAR(64)    NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_iqc_code` (`inspection_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiếu kiểm tra chất lượng đầu vào (IQC)';

-- 4. IQC Inspection Result (Kết quả từng tiêu chí IQC)
CREATE TABLE IF NOT EXISTS `qms_iqc_inspection_result` (
    `id`                 VARCHAR(36)   NOT NULL COMMENT 'ID',
    `inspection_id`      VARCHAR(36)   NOT NULL COMMENT 'FK → qms_iqc_inspection',
    `checklist_item_id`  VARCHAR(36)   NULL     COMMENT 'FK → qms_checklist_item',
    `criterion_name`     VARCHAR(200)  NOT NULL COMMENT 'Tên tiêu chí (copy)',
    `standard_value`     VARCHAR(200)  NULL     COMMENT 'Giá trị tiêu chuẩn (copy)',
    `actual_value`       VARCHAR(500)  NULL     COMMENT 'Giá trị thực đo',
    `result`             VARCHAR(20)   NULL     COMMENT 'passed, failed, na',
    `notes`              TEXT          NULL,
    PRIMARY KEY (`id`),
    KEY `idx_iqc_inspection_id` (`inspection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kết quả từng tiêu chí kiểm tra IQC';

-- 5. PQC Inspection (Phiếu kiểm tra chất lượng sản xuất)
CREATE TABLE IF NOT EXISTS `qms_pqc_inspection` (
    `id`                 VARCHAR(36)    NOT NULL COMMENT 'ID',
    `inspection_code`    VARCHAR(50)    NOT NULL COMMENT 'Mã phiếu PQC (PQCyyyyMMddNNN)',
    `work_order_id`      VARCHAR(36)    NULL     COMMENT 'FK → pl_work_order',
    `product_id`         VARCHAR(36)    NOT NULL COMMENT 'FK → product (thành phẩm)',
    `template_id`        VARCHAR(36)    NULL     COMMENT 'FK → qms_checklist_template',
    `stage_id`           VARCHAR(36)    NULL     COMMENT 'FK → qms_qc_stage (tùy chọn)',
    `quantity_inspected` DECIMAL(10,3)  NOT NULL DEFAULT 0 COMMENT 'SL kiểm tra',
    `quantity_passed`    DECIMAL(10,3)  NULL     COMMENT 'SL đạt',
    `quantity_failed`    DECIMAL(10,3)  NULL     COMMENT 'SL không đạt',
    `inspector`          VARCHAR(100)   NULL,
    `inspection_date`    DATE           NULL,
    `status`             VARCHAR(20)    NOT NULL DEFAULT 'draft' COMMENT 'draft, in_progress, pending_approval, passed, failed',
    `notes`              TEXT           NULL,
    `create_by`          VARCHAR(50)    NULL,
    `create_time`        DATETIME       NULL,
    `update_by`          VARCHAR(50)    NULL,
    `update_time`        DATETIME       NULL,
    `sys_org_code`       VARCHAR(64)    NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pqc_code` (`inspection_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiếu kiểm tra chất lượng sản xuất (PQC)';

-- 6. PQC Inspection Result
CREATE TABLE IF NOT EXISTS `qms_pqc_inspection_result` (
    `id`                 VARCHAR(36)   NOT NULL COMMENT 'ID',
    `inspection_id`      VARCHAR(36)   NOT NULL COMMENT 'FK → qms_pqc_inspection',
    `checklist_item_id`  VARCHAR(36)   NULL     COMMENT 'FK → qms_checklist_item',
    `criterion_name`     VARCHAR(200)  NOT NULL COMMENT 'Tên tiêu chí (copy)',
    `standard_value`     VARCHAR(200)  NULL     COMMENT 'Giá trị tiêu chuẩn (copy)',
    `actual_value`       VARCHAR(500)  NULL     COMMENT 'Giá trị thực đo',
    `result`             VARCHAR(20)   NULL     COMMENT 'passed, failed, na',
    `notes`              TEXT          NULL,
    PRIMARY KEY (`id`),
    KEY `idx_pqc_inspection_id` (`inspection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kết quả từng tiêu chí kiểm tra PQC';
