-- ===================================================
-- Module QC Stage - Cấu hình và Thực hiện Kiểm tra Công đoạn
-- Date: 2026-02-26 | Prefix: qms_
-- ===================================================

-- -------------------------------------------------------
-- PHẦN 1: CẤU HÌNH
-- -------------------------------------------------------

-- [1] Công đoạn kiểm tra (Stage template)
CREATE TABLE IF NOT EXISTS `qms_qc_stage` (
    `id`               VARCHAR(36)   NOT NULL COMMENT 'ID',
    `stage_code`       VARCHAR(50)   NOT NULL COMMENT 'Mã công đoạn',
    `stage_name`       VARCHAR(200)  NOT NULL COMMENT 'Tên công đoạn',
    `description`      TEXT          NULL     COMMENT 'Mô tả công đoạn',
    `sort_order`       INT           NOT NULL DEFAULT 0 COMMENT 'Thứ tự sắp xếp',
    `status`           VARCHAR(20)   NOT NULL DEFAULT 'active' COMMENT 'active | inactive',
    `create_by`        VARCHAR(50)   NULL,
    `create_time`      DATETIME      NULL,
    `update_by`        VARCHAR(50)   NULL,
    `update_time`      DATETIME      NULL,
    `sys_org_code`     VARCHAR(64)   NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_qc_stage_code` (`stage_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Công đoạn kiểm tra chất lượng';

-- [2] Tham số input của mỗi công đoạn
CREATE TABLE IF NOT EXISTS `qms_qc_stage_param` (
    `id`             VARCHAR(36)   NOT NULL COMMENT 'ID',
    `stage_id`       VARCHAR(36)   NOT NULL COMMENT 'FK → qms_qc_stage',
    `param_name`     VARCHAR(200)  NOT NULL COMMENT 'Tên tham số (vd: Độ dày lớp sơn)',
    `param_code`     VARCHAR(100)  NOT NULL COMMENT 'Mã tham số (vd: paint_thickness)',
    `input_type`     VARCHAR(20)   NOT NULL DEFAULT 'text'
                     COMMENT 'Kiểu nhập: text | number | pass_fail | select | date | list',
    `unit`           VARCHAR(50)   NULL     COMMENT 'Đơn vị (mm, kg, %...)',
    `default_value`  VARCHAR(500)  NULL     COMMENT 'Giá trị mặc định',
    `min_value`      DECIMAL(18,4) NULL     COMMENT 'Giá trị tối thiểu (khi type=number)',
    `max_value`      DECIMAL(18,4) NULL     COMMENT 'Giá trị tối đa (khi type=number)',
    `options_json`   TEXT          NULL     COMMENT 'JSON options khi type=select, vd: ["Trơn","Nhám"]',
    `is_required`    TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1=bắt buộc',
    `sort_order`     INT           NOT NULL DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    `notes`          TEXT          NULL,
    PRIMARY KEY (`id`),
    KEY `idx_qcs_param_stage` (`stage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tham số input của công đoạn kiểm tra';

-- -------------------------------------------------------
-- PHẦN 2: THỰC HIỆN KIỂM TRA
-- -------------------------------------------------------

-- [3] Phiên kiểm tra (1 session = 1 công đoạn trong 1 WO)
CREATE TABLE IF NOT EXISTS `qms_qc_session` (
    `id`               VARCHAR(36)   NOT NULL COMMENT 'ID',
    `session_code`     VARCHAR(50)   NOT NULL COMMENT 'Mã phiên kiểm tra (SKyyyyMMddNNN)',
    `work_order_id`    VARCHAR(36)   NOT NULL COMMENT 'FK → wh_work_order',
    `stage_id`         VARCHAR(36)   NOT NULL COMMENT 'FK → qms_qc_stage',
    `stage_name`       VARCHAR(200)  NULL     COMMENT 'Tên công đoạn (snapshot)',
    `inspector`        VARCHAR(100)  NULL     COMMENT 'Người kiểm tra',
    `inspection_date`  DATE          NULL     COMMENT 'Ngày kiểm tra',
    `status`           VARCHAR(20)   NOT NULL DEFAULT 'draft'
                       COMMENT 'Trạng thái: draft | completed',
    `notes`            TEXT          NULL,
    `create_by`        VARCHAR(50)   NULL,
    `create_time`      DATETIME      NULL,
    `update_by`        VARCHAR(50)   NULL,
    `update_time`      DATETIME      NULL,
    `sys_org_code`     VARCHAR(64)   NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_qc_session_code` (`session_code`),
    KEY `idx_qcs_work_order` (`work_order_id`),
    KEY `idx_qcs_stage` (`stage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiên kiểm tra công đoạn';

-- [4] Giá trị từng tham số trong phiên kiểm tra
CREATE TABLE IF NOT EXISTS `qms_qc_session_value` (
    `id`           VARCHAR(36)   NOT NULL COMMENT 'ID',
    `session_id`   VARCHAR(36)   NOT NULL COMMENT 'FK → qms_qc_session',
    `param_id`     VARCHAR(36)   NULL     COMMENT 'FK → qms_qc_stage_param',
    `param_name`   VARCHAR(200)  NOT NULL COMMENT 'Tên tham số (snapshot)',
    `input_type`   VARCHAR(20)   NOT NULL COMMENT 'Kiểu nhập (snapshot)',
    `unit`         VARCHAR(50)   NULL     COMMENT 'Đơn vị (snapshot)',
    `actual_value` VARCHAR(1000) NULL     COMMENT 'Giá trị thực (null nếu type=list)',
    `result`       VARCHAR(20)   NULL     COMMENT 'passed | failed | na',
    `sort_order`   INT           NOT NULL DEFAULT 0,
    `notes`        TEXT          NULL,
    PRIMARY KEY (`id`),
    KEY `idx_qcsv_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Giá trị tham số trong phiên kiểm tra';

-- [5] Chi tiết nhiều lần đo (khi input_type = list)
CREATE TABLE IF NOT EXISTS `qms_qc_session_value_item` (
    `id`             VARCHAR(36)   NOT NULL COMMENT 'ID',
    `value_id`       VARCHAR(36)   NOT NULL COMMENT 'FK → qms_qc_session_value',
    `seq_no`         INT           NOT NULL DEFAULT 1 COMMENT 'Số thứ tự lần đo',
    `measured_value` VARCHAR(500)  NULL     COMMENT 'Giá trị đo được lần này',
    `result`         VARCHAR(20)   NULL     COMMENT 'passed | failed | na',
    `notes`          VARCHAR(500)  NULL,
    PRIMARY KEY (`id`),
    KEY `idx_qcsvi_value` (`value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chi tiết nhiều lần đo (list input)';

-- -------------------------------------------------------
-- PHẦN 3: REVIEW & PHÊ DUYỆT
-- -------------------------------------------------------

-- [6] Review tổng hợp & phê duyệt toàn bộ WO
CREATE TABLE IF NOT EXISTS `qms_qc_review` (
    `id`               VARCHAR(36)   NOT NULL COMMENT 'ID',
    `review_code`      VARCHAR(50)   NOT NULL COMMENT 'Mã review (RVyyyyMMddNNN)',
    `work_order_id`    VARCHAR(36)   NOT NULL COMMENT 'FK → wh_work_order',
    `total_sessions`   INT           NOT NULL DEFAULT 0 COMMENT 'Tổng số phiên kiểm tra',
    `passed_sessions`  INT           NOT NULL DEFAULT 0 COMMENT 'Số phiên đạt',
    `failed_sessions`  INT           NOT NULL DEFAULT 0 COMMENT 'Số phiên không đạt',
    `overall_result`   VARCHAR(20)   NULL     COMMENT 'passed | failed | conditional',
    `status`           VARCHAR(30)   NOT NULL DEFAULT 'draft'
                       COMMENT 'draft | pending_approval | approved | rejected',
    `reviewer`         VARCHAR(100)  NULL     COMMENT 'Người review',
    `approver`         VARCHAR(100)  NULL     COMMENT 'Người phê duyệt',
    `approval_date`    DATETIME      NULL     COMMENT 'Ngày phê duyệt',
    `rejection_reason` TEXT          NULL     COMMENT 'Lý do từ chối',
    `notes`            TEXT          NULL,
    `create_by`        VARCHAR(50)   NULL,
    `create_time`      DATETIME      NULL,
    `update_by`        VARCHAR(50)   NULL,
    `update_time`      DATETIME      NULL,
    `sys_org_code`     VARCHAR(64)   NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_qc_review_code` (`review_code`),
    UNIQUE KEY `uk_qc_review_wo` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Review và phê duyệt toàn bộ quá trình kiểm tra của WO';
