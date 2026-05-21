-- ===================================================
-- QMS Inspection Template – Migration Script
-- Date: 2026-03-15
-- Description: DDL for 8 new tables (Inspection Template system)
--              + ALTER TABLE for routing step & production stage integration
-- Requirements: 1.1, 2.1, 3.1, 5.1, 6.1, 7.6, 8.6, 9.1
-- ===================================================

-- ===================================================
-- 1. qms_inspection_template (Mẫu kiểm tra chất lượng)
-- ===================================================
CREATE TABLE IF NOT EXISTS `qms_inspection_template` (
    `id`               VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `template_code`    VARCHAR(50)   NOT NULL COMMENT 'Mã template (TPLyyyyMMddNNN)',
    `template_name`    VARCHAR(200)  NOT NULL COMMENT 'Tên template',
    `description`      TEXT          NULL     COMMENT 'Mô tả',
    `stage_type`       VARCHAR(10)   NOT NULL COMMENT 'Loại giai đoạn: iqc, pqc, fqc',
    `version`          VARCHAR(20)   NOT NULL DEFAULT '1.0' COMMENT 'Phiên bản',
    `status`           VARCHAR(20)   NOT NULL DEFAULT 'draft' COMMENT 'Trạng thái: draft, active, obsolete',
    `notes`            TEXT          NULL     COMMENT 'Ghi chú',
    `create_by`        VARCHAR(50)   NULL,
    `create_time`      DATETIME      NULL,
    `update_by`        VARCHAR(50)   NULL,
    `update_time`      DATETIME      NULL,
    `sys_org_code`     VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tpl_code` (`template_code`),
    KEY `idx_tpl_stage_status` (`stage_type`, `status`),
    KEY `idx_tpl_org` (`sys_org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Mẫu kiểm tra chất lượng (Inspection Template)';

-- ===================================================
-- 2. qms_inspection_step (Bước kiểm tra trong template)
-- ===================================================
CREATE TABLE IF NOT EXISTS `qms_inspection_step` (
    `id`                 VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `template_id`        VARCHAR(36)   NOT NULL COMMENT 'FK → qms_inspection_template',
    `step_name`          VARCHAR(200)  NOT NULL COMMENT 'Tên bước kiểm tra',
    `description`        TEXT          NULL     COMMENT 'Mô tả bước',
    `sort_order`         INT           NOT NULL COMMENT 'Thứ tự thực hiện',
    `is_mandatory`       TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1=bắt buộc, 0=tùy chọn',
    `requires_approval`  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '1=cần phê duyệt, 0=không',
    PRIMARY KEY (`id`),
    KEY `idx_step_template` (`template_id`),
    UNIQUE KEY `uk_step_order` (`template_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bước kiểm tra trong template';

-- ===================================================
-- 3. qms_step_field (Trường dữ liệu trong bước kiểm tra)
-- ===================================================
CREATE TABLE IF NOT EXISTS `qms_step_field` (
    `id`             VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `step_id`        VARCHAR(36)   NOT NULL COMMENT 'FK → qms_inspection_step',
    `field_name`     VARCHAR(200)  NOT NULL COMMENT 'Tên trường',
    `field_code`     VARCHAR(100)  NULL     COMMENT 'Mã trường (dùng cho API)',
    `field_type`     VARCHAR(20)   NOT NULL COMMENT 'Kiểu: text, number, boolean, select, measurement',
    `unit`           VARCHAR(50)   NULL     COMMENT 'Đơn vị đo',
    `default_value`  VARCHAR(500)  NULL     COMMENT 'Giá trị mặc định',
    `is_required`    TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1=bắt buộc',
    `sort_order`     INT           NOT NULL DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    `field_config`   JSON          NULL     COMMENT 'Cấu hình theo field_type (JSON)',
    `hint`           VARCHAR(500)  NULL     COMMENT 'Ghi chú hướng dẫn nhập',
    PRIMARY KEY (`id`),
    KEY `idx_field_step` (`step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Trường dữ liệu trong bước kiểm tra';

-- ===================================================
-- 4. qms_template_assignment (Gán template cho sản phẩm/nhóm SP)
-- ===================================================
CREATE TABLE IF NOT EXISTS `qms_template_assignment` (
    `id`               VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `template_id`      VARCHAR(36)   NOT NULL COMMENT 'FK → qms_inspection_template',
    `assignment_type`  VARCHAR(20)   NOT NULL COMMENT 'Loại gán: product, product_group, default',
    `target_id`        VARCHAR(36)   NULL     COMMENT 'ID sản phẩm hoặc nhóm SP (NULL nếu default)',
    `is_active`        TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1=đang áp dụng',
    `create_by`        VARCHAR(50)   NULL,
    `create_time`      DATETIME      NULL,
    `sys_org_code`     VARCHAR(64)   NULL,
    PRIMARY KEY (`id`),
    KEY `idx_assign_template` (`template_id`),
    KEY `idx_assign_target` (`assignment_type`, `target_id`),
    UNIQUE KEY `uk_assign_active` (`template_id`, `assignment_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Gán template cho sản phẩm/nhóm SP';

-- ===================================================
-- 5. qms_inspection_execution (Phiên kiểm tra chất lượng)
-- ===================================================
CREATE TABLE IF NOT EXISTS `qms_inspection_execution` (
    `id`                  VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `execution_code`      VARCHAR(50)   NOT NULL COMMENT 'Mã phiên (EXCyyyyMMddNNN)',
    `template_id`         VARCHAR(36)   NOT NULL COMMENT 'FK → qms_inspection_template',
    `template_snapshot`   JSON          NULL     COMMENT 'Snapshot cấu hình template tại thời điểm tạo',
    `product_id`          VARCHAR(36)   NOT NULL COMMENT 'FK → product',
    `stage_type`          VARCHAR(10)   NOT NULL COMMENT 'Loại giai đoạn: iqc, pqc, fqc',
    `work_order_id`       VARCHAR(36)   NULL     COMMENT 'FK → pl_work_order',
    `production_stage_id` VARCHAR(36)   NULL     COMMENT 'FK → pl_production_stage',
    `inspector`           VARCHAR(100)  NULL     COMMENT 'Người kiểm tra',
    `inspection_date`     DATE          NULL     COMMENT 'Ngày kiểm tra',
    `overall_result`      VARCHAR(20)   NULL     COMMENT 'Kết quả tổng: pass, fail',
    `status`              VARCHAR(30)   NOT NULL DEFAULT 'draft'
                          COMMENT 'Trạng thái: draft, in_progress, pending_approval, approved, rejected',
    `approved_by`         VARCHAR(100)  NULL     COMMENT 'Người phê duyệt',
    `approved_time`       DATETIME      NULL     COMMENT 'Thời gian phê duyệt',
    `notes`               TEXT          NULL,
    `create_by`           VARCHAR(50)   NULL,
    `create_time`         DATETIME      NULL,
    `update_by`           VARCHAR(50)   NULL,
    `update_time`         DATETIME      NULL,
    `sys_org_code`        VARCHAR(64)   NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exec_code` (`execution_code`),
    KEY `idx_exec_template` (`template_id`),
    KEY `idx_exec_product` (`product_id`),
    KEY `idx_exec_wo` (`work_order_id`),
    KEY `idx_exec_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiên kiểm tra chất lượng';

-- ===================================================
-- 6. qms_step_result (Kết quả bước kiểm tra)
-- ===================================================
CREATE TABLE IF NOT EXISTS `qms_step_result` (
    `id`              VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `execution_id`    VARCHAR(36)   NOT NULL COMMENT 'FK → qms_inspection_execution',
    `step_id`         VARCHAR(36)   NOT NULL COMMENT 'FK → qms_inspection_step (snapshot)',
    `step_name`       VARCHAR(200)  NOT NULL COMMENT 'Tên bước (snapshot)',
    `sort_order`      INT           NOT NULL COMMENT 'Thứ tự (snapshot)',
    `is_mandatory`    TINYINT(1)    NOT NULL DEFAULT 1 COMMENT 'Bắt buộc (snapshot)',
    `result`          VARCHAR(20)   NULL     COMMENT 'Kết quả: pass, fail, pending',
    `status`          VARCHAR(20)   NOT NULL DEFAULT 'pending'
                      COMMENT 'Trạng thái: pending, completed, approved, rejected, re_inspect',
    `completed_time`  DATETIME      NULL     COMMENT 'Thời gian hoàn thành',
    `notes`           TEXT          NULL,
    PRIMARY KEY (`id`),
    KEY `idx_sr_execution` (`execution_id`),
    KEY `idx_sr_step` (`step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kết quả bước kiểm tra';

-- ===================================================
-- 7. qms_field_value (Giá trị trường dữ liệu trong phiên kiểm tra)
-- ===================================================
CREATE TABLE IF NOT EXISTS `qms_field_value` (
    `id`             VARCHAR(36)    NOT NULL COMMENT 'Khóa chính',
    `step_result_id` VARCHAR(36)    NOT NULL COMMENT 'FK → qms_step_result',
    `field_id`       VARCHAR(36)    NOT NULL COMMENT 'FK → qms_step_field (snapshot)',
    `field_name`     VARCHAR(200)   NOT NULL COMMENT 'Tên trường (snapshot)',
    `field_type`     VARCHAR(20)    NOT NULL COMMENT 'Kiểu trường (snapshot)',
    `field_config`   JSON           NULL     COMMENT 'Cấu hình trường (snapshot)',
    `is_required`    TINYINT(1)     NOT NULL DEFAULT 1 COMMENT 'Bắt buộc (snapshot)',
    `actual_value`   VARCHAR(1000)  NULL     COMMENT 'Giá trị thực tế nhập',
    `result`         VARCHAR(20)    NULL     COMMENT 'Kết quả: pass, fail, na',
    `eval_message`   VARCHAR(500)   NULL     COMMENT 'Thông báo đánh giá (vd: "Trong dung sai [4.5, 5.5]")',
    PRIMARY KEY (`id`),
    KEY `idx_fv_step_result` (`step_result_id`),
    KEY `idx_fv_field` (`field_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Giá trị trường dữ liệu trong phiên kiểm tra';

-- ===================================================
-- 8. qms_approval_record (Lịch sử phê duyệt kết quả kiểm tra)
-- ===================================================
CREATE TABLE IF NOT EXISTS `qms_approval_record` (
    `id`             VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `execution_id`   VARCHAR(36)   NOT NULL COMMENT 'FK → qms_inspection_execution',
    `step_result_id` VARCHAR(36)   NULL     COMMENT 'FK → qms_step_result (nếu phê duyệt từng bước)',
    `action`         VARCHAR(20)   NOT NULL COMMENT 'Hành động: approve, reject, re_inspect',
    `approver`       VARCHAR(100)  NOT NULL COMMENT 'Người phê duyệt',
    `reason`         TEXT          NULL     COMMENT 'Lý do (bắt buộc khi reject/re_inspect)',
    `action_time`    DATETIME      NOT NULL COMMENT 'Thời gian thực hiện',
    PRIMARY KEY (`id`),
    KEY `idx_apr_execution` (`execution_id`),
    KEY `idx_apr_step` (`step_result_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử phê duyệt kết quả kiểm tra';

-- ===================================================
-- 9. ALTER TABLE: Tích hợp với Routing Step
-- Thêm cột qc_stage_id vào pl_routing_step để liên kết với QC stage
-- ===================================================
ALTER TABLE `pl_routing_step`
    ADD COLUMN `qc_stage_id` VARCHAR(36) NULL COMMENT 'FK → qms_qc_stage (trigger QC khi hoàn thành bước này)',
    ADD COLUMN `qc_stage_type` VARCHAR(10) NULL COMMENT 'Loại QC: iqc, pqc, fqc';

-- ===================================================
-- 10. ALTER TABLE: Tích hợp với Production Stage
-- Thêm cột qc_status vào pl_production_stage để track trạng thái QC
-- ===================================================
ALTER TABLE `pl_production_stage`
    ADD COLUMN `qc_execution_id` VARCHAR(36) NULL COMMENT 'FK → qms_inspection_execution',
    ADD COLUMN `qc_blocked` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1=đang chờ QC hoàn thành';
