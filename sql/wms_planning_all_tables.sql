-- ============================================================
-- WMS - TOÀN BỘ BẢNG MODULE PLANNING (prefix: pl_)
-- FIX: Table 'pl_work_order' doesn't exist
-- Chạy file này để tạo toàn bộ bảng Planning/QMS phụ thuộc
-- Ngày tạo: 2026-03-02
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- NHÓM 1: CƠ SỞ HẠ TẦNG SẢN XUẤT
-- ============================================================

-- ------------------------------------------------------------
-- 1. Dây chuyền sản xuất (pl_production_line)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_production_line` (
    `id`               VARCHAR(36)    NOT NULL                        COMMENT 'Khóa chính',
    `create_by`        VARCHAR(50)    DEFAULT NULL                    COMMENT 'Người tạo',
    `create_time`      DATETIME       DEFAULT NULL                    COMMENT 'Ngày tạo',
    `update_by`        VARCHAR(50)    DEFAULT NULL                    COMMENT 'Người cập nhật',
    `update_time`      DATETIME       DEFAULT NULL                    COMMENT 'Ngày cập nhật',
    `sys_org_code`     VARCHAR(64)    DEFAULT NULL                    COMMENT 'Mã tổ chức',
    `line_code`        VARCHAR(50)    NOT NULL                        COMMENT 'Mã dây chuyền',
    `line_name`        VARCHAR(200)   NOT NULL                        COMMENT 'Tên dây chuyền',
    `description`      TEXT           DEFAULT NULL                    COMMENT 'Mô tả',
    `capacity_per_day` DECIMAL(10,2)  DEFAULT NULL                    COMMENT 'Năng suất/ngày',
    `unit`             VARCHAR(20)    DEFAULT NULL                    COMMENT 'Đơn vị năng suất',
    `status`           VARCHAR(20)    NOT NULL DEFAULT 'active'       COMMENT 'Trạng thái: active | inactive | maintenance',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_production_line_code` (`line_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dây chuyền sản xuất';

-- ------------------------------------------------------------
-- 2. Trung tâm sản xuất / Xưởng (pl_work_center)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_work_center` (
    `id`           VARCHAR(36)   NOT NULL                  COMMENT 'Khóa chính',
    `create_by`    VARCHAR(50)   DEFAULT NULL              COMMENT 'Người tạo',
    `create_time`  DATETIME      DEFAULT NULL              COMMENT 'Ngày tạo',
    `update_by`    VARCHAR(50)   DEFAULT NULL              COMMENT 'Người cập nhật',
    `update_time`  DATETIME      DEFAULT NULL              COMMENT 'Ngày cập nhật',
    `sys_org_code` VARCHAR(64)   DEFAULT NULL              COMMENT 'Mã tổ chức',
    `center_code`  VARCHAR(50)   NOT NULL                  COMMENT 'Mã trung tâm',
    `center_name`  VARCHAR(200)  NOT NULL                  COMMENT 'Tên trung tâm',
    `description`  TEXT          DEFAULT NULL              COMMENT 'Mô tả',
    `capacity`     DECIMAL(10,2) DEFAULT NULL              COMMENT 'Công suất',
    `cost_per_hour` DECIMAL(15,2) DEFAULT 0.00             COMMENT 'Chi phí/giờ',
    `status`       VARCHAR(20)   NOT NULL DEFAULT 'active' COMMENT 'Trạng thái: active | inactive',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_work_center_code` (`center_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Trung tâm sản xuất';

-- ============================================================
-- NHÓM 2: BOM (ĐỊNH MỨC NGUYÊN VẬT LIỆU)
-- ============================================================

-- ------------------------------------------------------------
-- 3. BOM Header (pl_bom)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_bom` (
    `id`              VARCHAR(36)   NOT NULL                   COMMENT 'Khóa chính',
    `create_by`       VARCHAR(50)   DEFAULT NULL               COMMENT 'Người tạo',
    `create_time`     DATETIME      DEFAULT NULL               COMMENT 'Ngày tạo',
    `update_by`       VARCHAR(50)   DEFAULT NULL               COMMENT 'Người cập nhật',
    `update_time`     DATETIME      DEFAULT NULL               COMMENT 'Ngày cập nhật',
    `sys_org_code`    VARCHAR(64)   DEFAULT NULL               COMMENT 'Mã tổ chức',
    `bom_code`        VARCHAR(50)   NOT NULL                   COMMENT 'Mã BOM',
    `bom_name`        VARCHAR(200)  NOT NULL                   COMMENT 'Tên BOM',
    `product_id`      VARCHAR(36)   NOT NULL                   COMMENT 'ID thành phẩm (FK → product)',
    `output_quantity` DECIMAL(10,3) NOT NULL DEFAULT 1         COMMENT 'Số lượng thành phẩm đầu ra',
    `unit`            VARCHAR(20)   DEFAULT NULL               COMMENT 'Đơn vị thành phẩm',
    `version`         VARCHAR(20)   NOT NULL DEFAULT '1.0'     COMMENT 'Phiên bản BOM',
    `is_default`      TINYINT(1)    NOT NULL DEFAULT 0         COMMENT 'Phiên bản mặc định của sản phẩm (1=Có, 0=Không)',
    `status`          VARCHAR(20)   NOT NULL DEFAULT 'active'  COMMENT 'Trạng thái: active | inactive',
    `notes`           TEXT          DEFAULT NULL               COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bom_code` (`bom_code`),
    KEY `idx_bom_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Định mức nguyên vật liệu (BOM Header)';

-- ------------------------------------------------------------
-- 4. BOM Items - Chi tiết NVL (pl_bom_item)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_bom_item` (
    `id`           VARCHAR(36)   NOT NULL   COMMENT 'Khóa chính',
    `bom_id`       VARCHAR(36)   NOT NULL   COMMENT 'ID BOM (FK → pl_bom)',
    `material_id`  VARCHAR(36)   NOT NULL   COMMENT 'ID nguyên vật liệu (FK → product)',
    `quantity`     DECIMAL(10,3) NOT NULL   COMMENT 'Số lượng NVL cần',
    `unit`         VARCHAR(20)   DEFAULT NULL COMMENT 'Đơn vị',
    `wastage_pct`  DECIMAL(5,2)  DEFAULT 0  COMMENT 'Tỷ lệ hao hụt (%)',
    `notes`        TEXT          DEFAULT NULL COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    KEY `idx_bom_item_bom` (`bom_id`),
    KEY `idx_bom_item_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chi tiết NVL trong BOM';

-- ------------------------------------------------------------
-- 5. BOM Revision - Lịch sử phiên bản BOM (pl_bom_revision)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_bom_revision` (
    `id`           VARCHAR(36)  NOT NULL    COMMENT 'Khóa chính',
    `bom_id`       VARCHAR(36)  NOT NULL    COMMENT 'ID BOM (FK → pl_bom)',
    `version`      VARCHAR(20)  NOT NULL    COMMENT 'Phiên bản',
    `change_reason` TEXT        DEFAULT NULL COMMENT 'Lý do thay đổi',
    `changed_by`   VARCHAR(50)  DEFAULT NULL COMMENT 'Người thay đổi',
    `changed_at`   DATETIME     DEFAULT NULL COMMENT 'Thời gian thay đổi',
    `snapshot_json` LONGTEXT    DEFAULT NULL COMMENT 'Snapshot BOM tại thời điểm (JSON)',
    PRIMARY KEY (`id`),
    KEY `idx_bom_rev_bom` (`bom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử phiên bản BOM';

-- ============================================================
-- NHÓM 3: ROUTING (QUY TRÌNH SẢN XUẤT)
-- ============================================================

-- ------------------------------------------------------------
-- 6. Routing Header (pl_routing)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_routing` (
    `id`          VARCHAR(36)  NOT NULL                 COMMENT 'Khóa chính',
    `create_by`   VARCHAR(50)  DEFAULT NULL             COMMENT 'Người tạo',
    `create_time` DATETIME     DEFAULT NULL             COMMENT 'Ngày tạo',
    `update_by`   VARCHAR(50)  DEFAULT NULL             COMMENT 'Người cập nhật',
    `update_time` DATETIME     DEFAULT NULL             COMMENT 'Ngày cập nhật',
    `sys_org_code` VARCHAR(64) DEFAULT NULL             COMMENT 'Mã tổ chức',
    `routing_code` VARCHAR(50) NOT NULL                 COMMENT 'Mã quy trình',
    `routing_name` VARCHAR(200) NOT NULL                COMMENT 'Tên quy trình',
    `product_id`  VARCHAR(36)  DEFAULT NULL             COMMENT 'ID sản phẩm áp dụng',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT 'Trạng thái: active | inactive',
    `notes`       TEXT         DEFAULT NULL             COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_routing_code` (`routing_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Quy trình sản xuất (Routing)';

-- ------------------------------------------------------------
-- 7. Routing Steps - Các bước quy trình (pl_routing_step)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_routing_step` (
    `id`                    VARCHAR(36)   NOT NULL  COMMENT 'Khóa chính',
    `routing_id`            VARCHAR(36)   NOT NULL  COMMENT 'ID Routing (FK → pl_routing)',
    `step_no`               INT           NOT NULL  COMMENT 'Số thứ tự bước',
    `step_name`             VARCHAR(200)  NOT NULL  COMMENT 'Tên bước',
    `work_center_id`        VARCHAR(36)   DEFAULT NULL COMMENT 'Trung tâm SX (FK → pl_work_center)',
    `setup_time_min`        DECIMAL(8,2)  DEFAULT 0 COMMENT 'Thời gian chuẩn bị (phút)',
    `run_time_per_unit_min` DECIMAL(8,2)  DEFAULT 0 COMMENT 'Thời gian chạy/đơn vị (phút)',
    `description`           TEXT          DEFAULT NULL COMMENT 'Mô tả bước',
    PRIMARY KEY (`id`),
    KEY `idx_routing_step_routing` (`routing_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Các bước quy trình sản xuất';

-- ============================================================
-- NHÓM 4: LỆNH SẢN XUẤT (WORK ORDER) ← BẢNG QUAN TRỌNG
-- ============================================================

-- ------------------------------------------------------------
-- 8. Lệnh sản xuất (pl_work_order) ← FIX TABLE MISSING
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_work_order` (
    `id`                  VARCHAR(36)    NOT NULL                       COMMENT 'Khóa chính',
    `create_by`           VARCHAR(50)    DEFAULT NULL                   COMMENT 'Người tạo',
    `create_time`         DATETIME       DEFAULT NULL                   COMMENT 'Ngày tạo',
    `update_by`           VARCHAR(50)    DEFAULT NULL                   COMMENT 'Người cập nhật',
    `update_time`         DATETIME       DEFAULT NULL                   COMMENT 'Ngày cập nhật',
    `sys_org_code`        VARCHAR(64)    DEFAULT NULL                   COMMENT 'Mã tổ chức',
    `order_code`          VARCHAR(50)    NOT NULL                       COMMENT 'Mã lệnh sản xuất',
    `bom_id`              VARCHAR(36)    NOT NULL                       COMMENT 'ID BOM (FK → pl_bom)',
    `production_line_id`  VARCHAR(36)    DEFAULT NULL                   COMMENT 'ID dây chuyền (FK → pl_production_line)',
    `routing_id`          VARCHAR(36)    DEFAULT NULL                   COMMENT 'ID quy trình (FK → pl_routing)',
    `planned_quantity`    DECIMAL(10,3)  NOT NULL                       COMMENT 'Số lượng kế hoạch',
    `actual_quantity`     DECIMAL(10,3)  DEFAULT NULL                   COMMENT 'Số lượng thực tế',
    `planned_start_date`  DATE           DEFAULT NULL                   COMMENT 'Ngày bắt đầu kế hoạch',
    `planned_end_date`    DATE           DEFAULT NULL                   COMMENT 'Ngày kết thúc kế hoạch',
    `actual_start_date`   DATE           DEFAULT NULL                   COMMENT 'Ngày bắt đầu thực tế',
    `actual_end_date`     DATE           DEFAULT NULL                   COMMENT 'Ngày kết thúc thực tế',
    `status`              VARCHAR(30)    NOT NULL DEFAULT 'draft'       COMMENT 'Trạng thái: draft | planned | in_progress | completed | cancelled',
    `priority`            VARCHAR(20)    NOT NULL DEFAULT 'normal'      COMMENT 'Ưu tiên: low | normal | high | urgent',
    `notes`               TEXT           DEFAULT NULL                   COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_work_order_code` (`order_code`),
    KEY `idx_wo_bom` (`bom_id`),
    KEY `idx_wo_line` (`production_line_id`),
    KEY `idx_wo_status` (`status`),
    KEY `idx_wo_planned_start` (`planned_start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lệnh sản xuất (Work Order)';

-- ------------------------------------------------------------
-- 9. Công đoạn trong lệnh SX (pl_production_stage)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_production_stage` (
    `id`                     VARCHAR(36)   NOT NULL                   COMMENT 'Khóa chính',
    `work_order_id`          VARCHAR(36)   NOT NULL                   COMMENT 'ID lệnh SX (FK → pl_work_order)',
    `stage_name`             VARCHAR(200)  NOT NULL                   COMMENT 'Tên công đoạn',
    `stage_order`            INT           NOT NULL                   COMMENT 'Thứ tự công đoạn',
    `work_center_id`         VARCHAR(36)   DEFAULT NULL               COMMENT 'Trung tâm SX',
    `planned_duration_hours` DECIMAL(8,2)  DEFAULT NULL               COMMENT 'Thời gian kế hoạch (giờ)',
    `actual_duration_hours`  DECIMAL(8,2)  DEFAULT NULL               COMMENT 'Thời gian thực tế (giờ)',
    `status`                 VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT 'Trạng thái: pending | in_progress | completed | skipped',
    `assignee`               VARCHAR(100)  DEFAULT NULL               COMMENT 'Người phụ trách',
    `notes`                  TEXT          DEFAULT NULL               COMMENT 'Ghi chú',
    `create_time`            DATETIME      DEFAULT NULL               COMMENT 'Ngày tạo',
    `update_time`            DATETIME      DEFAULT NULL               COMMENT 'Ngày cập nhật',
    PRIMARY KEY (`id`),
    KEY `idx_prod_stage_wo` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Công đoạn trong lệnh sản xuất';

-- ------------------------------------------------------------
-- 10. Nhật ký sản xuất (pl_production_log)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_production_log` (
    `id`             VARCHAR(36)   NOT NULL   COMMENT 'Khóa chính',
    `work_order_id`  VARCHAR(36)   NOT NULL   COMMENT 'ID lệnh SX (FK → pl_work_order)',
    `stage_id`       VARCHAR(36)   DEFAULT NULL COMMENT 'ID công đoạn',
    `log_time`       DATETIME      NOT NULL   COMMENT 'Thời gian ghi nhận',
    `action`         VARCHAR(100)  DEFAULT NULL COMMENT 'Hành động thực hiện',
    `quantity`       DECIMAL(10,3) DEFAULT NULL COMMENT 'Số lượng',
    `operator`       VARCHAR(100)  DEFAULT NULL COMMENT 'Người thực hiện',
    `notes`          TEXT          DEFAULT NULL COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    KEY `idx_prod_log_wo` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhật ký sản xuất';

-- ============================================================
-- NHÓM 5: ECN (ENGINEERING CHANGE NOTICE)
-- ============================================================

-- ------------------------------------------------------------
-- 11. ECN Header (pl_ecn)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_ecn` (
    `id`            VARCHAR(36)  NOT NULL                    COMMENT 'Khóa chính',
    `create_by`     VARCHAR(50)  DEFAULT NULL                COMMENT 'Người tạo',
    `create_time`   DATETIME     DEFAULT NULL                COMMENT 'Ngày tạo',
    `update_by`     VARCHAR(50)  DEFAULT NULL                COMMENT 'Người cập nhật',
    `update_time`   DATETIME     DEFAULT NULL                COMMENT 'Ngày cập nhật',
    `sys_org_code`  VARCHAR(64)  DEFAULT NULL                COMMENT 'Mã tổ chức',
    `ecn_code`      VARCHAR(50)  NOT NULL                    COMMENT 'Mã ECN',
    `title`         VARCHAR(300) NOT NULL                    COMMENT 'Tiêu đề thay đổi',
    `change_reason` TEXT         DEFAULT NULL                COMMENT 'Lý do thay đổi',
    `effective_date` DATE        DEFAULT NULL                COMMENT 'Ngày có hiệu lực',
    `status`        VARCHAR(30)  NOT NULL DEFAULT 'draft'    COMMENT 'Trạng thái: draft | pending | approved | rejected | implemented',
    `priority`      VARCHAR(20)  NOT NULL DEFAULT 'normal'   COMMENT 'Ưu tiên: low | normal | high | critical',
    `initiated_by`  VARCHAR(100) DEFAULT NULL                COMMENT 'Người khởi tạo',
    `approved_by`   VARCHAR(100) DEFAULT NULL                COMMENT 'Người phê duyệt',
    `approved_at`   DATETIME     DEFAULT NULL                COMMENT 'Ngày phê duyệt',
    `notes`         TEXT         DEFAULT NULL                COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ecn_code` (`ecn_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiếu thay đổi kỹ thuật (ECN)';

-- ------------------------------------------------------------
-- 12. ECN Items - Các mục thay đổi (pl_ecn_item)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_ecn_item` (
    `id`              VARCHAR(36)  NOT NULL   COMMENT 'Khóa chính',
    `ecn_id`          VARCHAR(36)  NOT NULL   COMMENT 'ID ECN (FK → pl_ecn)',
    `item_type`       VARCHAR(50)  NOT NULL   COMMENT 'Loại mục thay đổi (BOM/ROUTING/DRAWING...)',
    `ref_id`          VARCHAR(36)  DEFAULT NULL COMMENT 'ID tham chiếu',
    `change_field`    VARCHAR(100) DEFAULT NULL COMMENT 'Trường thay đổi',
    `old_value`       TEXT         DEFAULT NULL COMMENT 'Giá trị cũ',
    `new_value`       TEXT         DEFAULT NULL COMMENT 'Giá trị mới',
    `description`     TEXT         DEFAULT NULL COMMENT 'Mô tả thay đổi',
    PRIMARY KEY (`id`),
    KEY `idx_ecn_item_ecn` (`ecn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chi tiết các mục thay đổi trong ECN';

-- ------------------------------------------------------------
-- 13. ECN Approvals - Luồng phê duyệt (pl_ecn_approval)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_ecn_approval` (
    `id`           VARCHAR(36)  NOT NULL   COMMENT 'Khóa chính',
    `ecn_id`       VARCHAR(36)  NOT NULL   COMMENT 'ID ECN (FK → pl_ecn)',
    `approver`     VARCHAR(100) NOT NULL   COMMENT 'Người phê duyệt',
    `step_order`   INT          NOT NULL   COMMENT 'Thứ tự bước duyệt',
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT 'Trạng thái: pending | approved | rejected',
    `comment`      TEXT         DEFAULT NULL COMMENT 'Nhận xét',
    `decided_at`   DATETIME     DEFAULT NULL COMMENT 'Thời gian quyết định',
    PRIMARY KEY (`id`),
    KEY `idx_ecn_approval_ecn` (`ecn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Luồng phê duyệt ECN';

-- ============================================================
-- NHÓM 6: ITEM MASTER & APPROVED LISTS
-- ============================================================

-- ------------------------------------------------------------
-- 14. Item Master - Danh mục vật tư kỹ thuật (pl_item_master)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_item_master` (
    `id`             VARCHAR(36)  NOT NULL                COMMENT 'Khóa chính',
    `create_by`      VARCHAR(50)  DEFAULT NULL            COMMENT 'Người tạo',
    `create_time`    DATETIME     DEFAULT NULL            COMMENT 'Ngày tạo',
    `update_by`      VARCHAR(50)  DEFAULT NULL            COMMENT 'Người cập nhật',
    `update_time`    DATETIME     DEFAULT NULL            COMMENT 'Ngày cập nhật',
    `sys_org_code`   VARCHAR(64)  DEFAULT NULL            COMMENT 'Mã tổ chức',
    `item_code`      VARCHAR(100) NOT NULL                COMMENT 'Mã vật tư',
    `item_name`      VARCHAR(300) NOT NULL                COMMENT 'Tên vật tư',
    `item_type`      VARCHAR(50)  DEFAULT NULL            COMMENT 'Loại vật tư (RM/WIP/FG/TOOL...)',
    `uom`            VARCHAR(20)  DEFAULT NULL            COMMENT 'Đơn vị đo lường',
    `product_id`     VARCHAR(36)  DEFAULT NULL            COMMENT 'Liên kết với sản phẩm (FK → product)',
    `specifications` TEXT         DEFAULT NULL            COMMENT 'Thông số kỹ thuật',
    `drawing_no`     VARCHAR(100) DEFAULT NULL            COMMENT 'Số bản vẽ',
    `revision`       VARCHAR(20)  DEFAULT NULL            COMMENT 'Phiên bản bản vẽ',
    `status`         VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT 'Trạng thái: active | obsolete',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item_master_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh mục vật tư kỹ thuật (Item Master)';

-- ------------------------------------------------------------
-- 15. Approved Manufacturer - NCC được duyệt (pl_approved_manufacturer)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_approved_manufacturer` (
    `id`              VARCHAR(36)  NOT NULL   COMMENT 'Khóa chính',
    `item_master_id`  VARCHAR(36)  NOT NULL   COMMENT 'ID Item Master (FK → pl_item_master)',
    `manufacturer`    VARCHAR(200) NOT NULL   COMMENT 'Tên nhà sản xuất',
    `mfr_part_no`     VARCHAR(100) DEFAULT NULL COMMENT 'Mã linh kiện của NSX',
    `approved_date`   DATE         DEFAULT NULL COMMENT 'Ngày được duyệt',
    `is_preferred`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Là NSX ưu tiên: 1=Có, 0=Không',
    `notes`           TEXT         DEFAULT NULL COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    KEY `idx_apm_item` (`item_master_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhà sản xuất được duyệt cho vật tư';

-- ------------------------------------------------------------
-- 16. Approved Vendor - NCC được duyệt (pl_approved_vendor)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pl_approved_vendor` (
    `id`             VARCHAR(36)  NOT NULL   COMMENT 'Khóa chính',
    `item_master_id` VARCHAR(36)  NOT NULL   COMMENT 'ID Item Master (FK → pl_item_master)',
    `supplier_id`    VARCHAR(36)  NOT NULL   COMMENT 'ID nhà cung cấp (FK → suppliers)',
    `vendor_part_no` VARCHAR(100) DEFAULT NULL COMMENT 'Mã hàng của NCC',
    `lead_time_days` INT          DEFAULT NULL COMMENT 'Thời gian giao hàng (ngày)',
    `min_order_qty`  DECIMAL(10,3) DEFAULT 1  COMMENT 'Số lượng đặt hàng tối thiểu',
    `approved_date`  DATE         DEFAULT NULL COMMENT 'Ngày được duyệt',
    `is_preferred`   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Là NCC ưu tiên',
    `notes`          TEXT         DEFAULT NULL COMMENT 'Ghi chú',
    PRIMARY KEY (`id`),
    KEY `idx_apv_item` (`item_master_id`),
    KEY `idx_apv_supplier` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhà cung cấp được duyệt cho vật tư';

-- ============================================================
-- DỮ LIỆU DANH MỤC (sys_dict) CHO MODULE PLANNING
-- ============================================================

INSERT IGNORE INTO `sys_dict`
  (`dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`)
VALUES
  ('Trạng thái lệnh SX',     'work_order_status',    'Trạng thái lệnh sản xuất',  '0', 'admin', NOW(), '0'),
  ('Ưu tiên lệnh SX',        'work_order_priority',  'Mức độ ưu tiên lệnh SX',    '0', 'admin', NOW(), '0'),
  ('Trạng thái dây chuyền',  'production_line_status','Trạng thái dây chuyền SX', '0', 'admin', NOW(), '0'),
  ('Trạng thái công đoạn SX','production_stage_status','Trạng thái công đoạn',    '0', 'admin', NOW(), '0'),
  ('Trạng thái BOM',         'bom_status',           'Trạng thái BOM',            '0', 'admin', NOW(), '0'),
  ('Trạng thái ECN',         'ecn_status',           'Trạng thái ECN',            '0', 'admin', NOW(), '0'),
  ('Ưu tiên ECN',            'ecn_priority',         'Mức độ ưu tiên ECN',        '0', 'admin', NOW(), '0'),
  ('Loại item master',       'item_master_type',     'Loại vật tư kỹ thuật',      '0', 'admin', NOW(), '0');

INSERT IGNORE INTO `sys_dict_item`
  (`dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `sort_order`, `status`)
SELECT d.id, v.item_text, v.item_value, v.description, 'admin', NOW(), v.sort_order, '1'
FROM sys_dict d
JOIN (
  -- work_order_status
  SELECT 'work_order_status' dc, 'Bản nháp'       it, 'draft'       iv, '' ds, 1 so UNION ALL
  SELECT 'work_order_status',   'Đã lên kế hoạch','planned',             '',  2 UNION ALL
  SELECT 'work_order_status',   'Đang sản xuất',  'in_progress',         '',  3 UNION ALL
  SELECT 'work_order_status',   'Hoàn thành',     'completed',           '',  4 UNION ALL
  SELECT 'work_order_status',   'Đã hủy',         'cancelled',           '',  5 UNION ALL
  -- work_order_priority
  SELECT 'work_order_priority', 'Thấp',           'low',                 '',  1 UNION ALL
  SELECT 'work_order_priority', 'Bình thường',    'normal',              '',  2 UNION ALL
  SELECT 'work_order_priority', 'Cao',            'high',                '',  3 UNION ALL
  SELECT 'work_order_priority', 'Khẩn',           'urgent',              '',  4 UNION ALL
  -- production_line_status
  SELECT 'production_line_status','Hoạt động',    'active',              '',  1 UNION ALL
  SELECT 'production_line_status','Ngưng',        'inactive',            '',  2 UNION ALL
  SELECT 'production_line_status','Bảo trì',      'maintenance',         '',  3 UNION ALL
  -- production_stage_status
  SELECT 'production_stage_status','Chờ',         'pending',             '',  1 UNION ALL
  SELECT 'production_stage_status','Đang thực hiện','in_progress',       '',  2 UNION ALL
  SELECT 'production_stage_status','Hoàn thành',  'completed',           '',  3 UNION ALL
  SELECT 'production_stage_status','Bỏ qua',      'skipped',             '',  4 UNION ALL
  -- bom_status
  SELECT 'bom_status',          'Hoạt động',      'active',              '',  1 UNION ALL
  SELECT 'bom_status',          'Ngưng',          'inactive',            '',  2 UNION ALL
  -- ecn_status
  SELECT 'ecn_status',          'Bản nháp',       'draft',               '',  1 UNION ALL
  SELECT 'ecn_status',          'Chờ duyệt',      'pending',             '',  2 UNION ALL
  SELECT 'ecn_status',          'Đã duyệt',       'approved',            '',  3 UNION ALL
  SELECT 'ecn_status',          'Từ chối',        'rejected',            '',  4 UNION ALL
  SELECT 'ecn_status',          'Đã thực hiện',   'implemented',         '',  5 UNION ALL
  -- ecn_priority
  SELECT 'ecn_priority',        'Thấp',           'low',                 '',  1 UNION ALL
  SELECT 'ecn_priority',        'Bình thường',    'normal',              '',  2 UNION ALL
  SELECT 'ecn_priority',        'Cao',            'high',                '',  3 UNION ALL
  SELECT 'ecn_priority',        'Nghiêm trọng',   'critical',            '',  4 UNION ALL
  -- item_master_type
  SELECT 'item_master_type',    'Nguyên liệu thô','RM',                  '',  1 UNION ALL
  SELECT 'item_master_type',    'Bán thành phẩm', 'WIP',                 '',  2 UNION ALL
  SELECT 'item_master_type',    'Thành phẩm',     'FG',                  '',  3 UNION ALL
  SELECT 'item_master_type',    'Công cụ/Dụng cụ','TOOL',                '',  4
) v ON d.dict_code = v.dc;

-- ============================================================
-- DỮ LIỆU MẪU
-- ============================================================

-- Dây chuyền sản xuất mẫu
INSERT IGNORE INTO `pl_production_line`
  (`id`, `create_by`, `create_time`, `sys_org_code`, `line_code`, `line_name`, `description`, `capacity_per_day`, `unit`, `status`)
VALUES
  (UUID(), 'admin', NOW(), 'A01', 'DC001', 'Dây chuyền lắp ráp A', 'Lắp ráp sản phẩm điện tử', 500.00, 'Cái', 'active'),
  (UUID(), 'admin', NOW(), 'A01', 'DC002', 'Dây chuyền may B',      'May quần áo',               800.00, 'Cái', 'active'),
  (UUID(), 'admin', NOW(), 'A01', 'DC003', 'Dây chuyền đóng gói C', 'Đóng gói thành phẩm',      1000.00, 'Thùng', 'active');

SET FOREIGN_KEY_CHECKS = 1;
