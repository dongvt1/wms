-- ===================================================
-- QMS Quality Management - New Features (v2)
-- Date: 2026-03-01
-- Description: DDL for FQC, NCR, Attachment, Notification tables
--              + ALTER TABLE for stock blocking integration
--              + Update checklist_template inspection_type comment
-- Requirements: 7.1, 7.2, 8.1, 9.1, 9.2, 10.1, 11.1
-- ===================================================

-- 1. FQC Inspection (Phiếu kiểm tra chất lượng thành phẩm)
CREATE TABLE IF NOT EXISTS `qms_fqc_inspection` (
    `id`                    VARCHAR(36)    NOT NULL COMMENT 'ID',
    `inspection_code`       VARCHAR(50)    NOT NULL COMMENT 'Mã phiếu FQC (FQCyyyyMMddNNN)',
    `outbound_order_id`     VARCHAR(36)    NULL     COMMENT 'FK → outbound order (đơn hàng xuất)',
    `product_id`            VARCHAR(36)    NOT NULL COMMENT 'FK → product (thành phẩm)',
    `customer_id`           VARCHAR(36)    NULL     COMMENT 'FK → customer (khách hàng)',
    `template_id`           VARCHAR(36)    NULL     COMMENT 'FK → qms_checklist_template',
    `quantity_inspected`    DECIMAL(10,3)  NOT NULL DEFAULT 0 COMMENT 'Số lượng kiểm tra',
    `quantity_passed`       DECIMAL(10,3)  NULL     COMMENT 'Số lượng đạt',
    `quantity_failed`       DECIMAL(10,3)  NULL     COMMENT 'Số lượng không đạt',
    `inspector`             VARCHAR(100)   NULL     COMMENT 'Người kiểm tra',
    `inspection_date`       DATE           NULL     COMMENT 'Ngày kiểm tra',
    `status`                VARCHAR(20)    NOT NULL DEFAULT 'draft' COMMENT 'draft, in_progress, pending_approval, passed, failed',
    `notes`                 TEXT           NULL     COMMENT 'Ghi chú',
    `create_by`             VARCHAR(50)    NULL,
    `create_time`           DATETIME       NULL,
    `update_by`             VARCHAR(50)    NULL,
    `update_time`           DATETIME       NULL,
    `sys_org_code`          VARCHAR(64)    NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fqc_code` (`inspection_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiếu kiểm tra chất lượng thành phẩm (FQC)';

-- 2. FQC Inspection Result (Kết quả từng tiêu chí FQC)
CREATE TABLE IF NOT EXISTS `qms_fqc_inspection_result` (
    `id`                 VARCHAR(36)   NOT NULL COMMENT 'ID',
    `inspection_id`      VARCHAR(36)   NOT NULL COMMENT 'FK → qms_fqc_inspection',
    `checklist_item_id`  VARCHAR(36)   NULL     COMMENT 'FK → qms_checklist_item',
    `criterion_name`     VARCHAR(200)  NOT NULL COMMENT 'Tên tiêu chí (copy từ template)',
    `standard_value`     VARCHAR(200)  NULL     COMMENT 'Giá trị tiêu chuẩn (copy)',
    `actual_value`       VARCHAR(500)  NULL     COMMENT 'Giá trị thực đo',
    `result`             VARCHAR(20)   NULL     COMMENT 'passed, failed, na',
    `notes`              TEXT          NULL     COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    KEY `idx_fqc_inspection_id` (`inspection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kết quả từng tiêu chí kiểm tra FQC';

-- 3. NCR - Non-Conformance Report (Báo cáo sự không phù hợp)
CREATE TABLE IF NOT EXISTS `qms_ncr` (
    `id`                 VARCHAR(36)    NOT NULL COMMENT 'ID',
    `ncr_code`           VARCHAR(50)    NOT NULL COMMENT 'Mã NCR (NCRyyyyMMddNNN)',
    `source_type`        VARCHAR(20)    NOT NULL COMMENT 'Nguồn phát hiện: iqc, pqc, fqc, other',
    `source_id`          VARCHAR(36)    NULL     COMMENT 'FK → phiếu kiểm tra nguồn',
    `product_id`         VARCHAR(36)    NULL     COMMENT 'FK → product',
    `supplier_id`        VARCHAR(36)    NULL     COMMENT 'FK → supplier (tự động từ IQC)',
    `description`        TEXT           NOT NULL COMMENT 'Mô tả lỗi',
    `severity`           VARCHAR(20)    NOT NULL COMMENT 'Mức độ: critical, major, minor',
    `quantity_defective` DECIMAL(10,3)  NULL     COMMENT 'Số lượng lỗi',
    `proposed_action`    VARCHAR(50)    NULL     COMMENT 'Hành động đề xuất: return, repair, scrap, accept_conditional',
    `corrective_action`  TEXT           NULL     COMMENT 'Hành động khắc phục thực tế',
    `status`             VARCHAR(30)    NOT NULL DEFAULT 'open' COMMENT 'open, investigating, action_taken, verified, closed',
    `assigned_to`        VARCHAR(100)   NULL     COMMENT 'Người được giao xử lý',
    `closed_by`          VARCHAR(100)   NULL     COMMENT 'Người đóng NCR',
    `closed_date`        DATETIME       NULL     COMMENT 'Ngày đóng',
    `notes`              TEXT           NULL     COMMENT 'Ghi chú',
    `create_by`          VARCHAR(50)    NULL,
    `create_time`        DATETIME       NULL,
    `update_by`          VARCHAR(50)    NULL,
    `update_time`        DATETIME       NULL,
    `sys_org_code`       VARCHAR(64)    NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ncr_code` (`ncr_code`),
    KEY `idx_ncr_source` (`source_type`, `source_id`),
    KEY `idx_ncr_supplier` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Báo cáo sự không phù hợp (NCR)';

-- 4. QMS Attachment (Tệp đính kèm cho các thực thể QMS)
CREATE TABLE IF NOT EXISTS `qms_attachment` (
    `id`            VARCHAR(36)   NOT NULL COMMENT 'ID',
    `entity_type`   VARCHAR(20)   NOT NULL COMMENT 'Loại thực thể: iqc, pqc, fqc, ncr',
    `entity_id`     VARCHAR(36)   NOT NULL COMMENT 'FK → thực thể nguồn',
    `file_name`     VARCHAR(255)  NOT NULL COMMENT 'Tên tệp gốc',
    `file_path`     VARCHAR(500)  NOT NULL COMMENT 'Đường dẫn lưu trữ',
    `file_size`     BIGINT        NOT NULL COMMENT 'Dung lượng (bytes)',
    `file_type`     VARCHAR(20)   NOT NULL COMMENT 'Định dạng: jpg, png, pdf, docx, xlsx',
    `upload_by`     VARCHAR(50)   NULL     COMMENT 'Người tải lên',
    `upload_time`   DATETIME      NULL     COMMENT 'Thời gian tải lên',
    PRIMARY KEY (`id`),
    KEY `idx_attach_entity` (`entity_type`, `entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tệp đính kèm cho các thực thể QMS';

-- 5. QMS Notification (Thông báo trong hệ thống)
CREATE TABLE IF NOT EXISTS `qms_notification` (
    `id`            VARCHAR(36)   NOT NULL COMMENT 'ID',
    `user_id`       VARCHAR(36)   NOT NULL COMMENT 'Người nhận thông báo',
    `title`         VARCHAR(200)  NOT NULL COMMENT 'Tiêu đề thông báo',
    `content`       TEXT          NULL     COMMENT 'Nội dung chi tiết',
    `entity_type`   VARCHAR(20)   NULL     COMMENT 'Loại thực thể liên quan: iqc, pqc, fqc, ncr, review',
    `entity_id`     VARCHAR(36)   NULL     COMMENT 'FK → thực thể liên quan',
    `is_read`       TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '0=chưa đọc, 1=đã đọc',
    `create_time`   DATETIME      NOT NULL COMMENT 'Thời gian tạo',
    PRIMARY KEY (`id`),
    KEY `idx_notif_user` (`user_id`, `is_read`),
    KEY `idx_notif_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Thông báo trong hệ thống QMS';

-- ===================================================
-- ALTER TABLE: Add qc_status to wh_stock_transaction
-- for stock blocking integration (Requirement 8.1)
-- ===================================================
ALTER TABLE `wh_stock_transaction`
    ADD COLUMN `qc_status` VARCHAR(20) NULL DEFAULT 'pending'
    COMMENT 'Trạng thái QC: pending, available, blocked, conditional_hold';

-- ===================================================
-- Update qms_checklist_template.inspection_type comment
-- to include 'fqc' as a valid value
-- ===================================================
ALTER TABLE `qms_checklist_template`
    MODIFY COLUMN `inspection_type` VARCHAR(10) NOT NULL
    COMMENT 'Loại kiểm tra: iqc, pqc, fqc';
