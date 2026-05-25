-- ===================================================
-- AI Production Planning Agent – Migration Script
-- Date: 2026-06-20
-- Description: DDL for 12 tables supporting the AI Production Planning Agent
--              (order queue, material availability, procurement, quarterly/monthly/weekly plans,
--               optimization scores, production progress, rescheduling, sync status, supplier lead times)
-- Requirements: All (foundational schema)
-- ===================================================

-- ===================================================
-- 1. ap_planning_order (Đơn hàng kế hoạch)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_planning_order` (
    `id`                 VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `external_order_id`  VARCHAR(100)  NOT NULL COMMENT 'Mã đơn hàng từ OrderHub',
    `product_type`       VARCHAR(100)  NOT NULL COMMENT 'Loại sản phẩm',
    `customer_name`      VARCHAR(200)  NOT NULL COMMENT 'Tên khách hàng',
    `quantity`           DECIMAL(15,2) NOT NULL COMMENT 'Số lượng đặt hàng',
    `deadline`           DATE          NOT NULL COMMENT 'Hạn giao hàng',
    `receipt_timestamp`  DATETIME      NOT NULL COMMENT 'Thời điểm nhận đơn hàng',
    `status`             VARCHAR(30)   NOT NULL DEFAULT 'pending'
                         COMMENT 'Trạng thái: pending, confirmed, in_production, fulfilled, cancelled',
    `validation_status`  VARCHAR(20)   NOT NULL DEFAULT 'valid'
                         COMMENT 'Xác thực: valid, incomplete, invalid',
    `validation_errors`  JSON          NULL     COMMENT 'Chi tiết lỗi xác thực',
    `priority_rank`      INT           NULL     COMMENT 'Vị trí trong hàng đợi ưu tiên',
    `fulfillment_qty`    DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'Số lượng đã hoàn thành',
    `fulfillment_status` VARCHAR(30)   NULL     COMMENT 'in_production, partially_fulfilled, fully_fulfilled',
    `create_by`          VARCHAR(50)   NULL,
    `create_time`        DATETIME      NULL,
    `update_by`          VARCHAR(50)   NULL,
    `update_time`        DATETIME      NULL,
    `sys_org_code`       VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_external_order` (`external_order_id`),
    KEY `idx_order_deadline` (`deadline`),
    KEY `idx_order_status` (`status`),
    KEY `idx_order_product` (`product_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Hàng đợi đơn hàng';

-- ===================================================
-- 2. ap_material_availability (Tình trạng nguyên vật liệu)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_material_availability` (
    `id`                 VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `order_id`           VARCHAR(36)   NOT NULL COMMENT 'FK → ap_planning_order',
    `material_id`        VARCHAR(100)  NOT NULL COMMENT 'Mã nguyên vật liệu từ ERP',
    `material_name`      VARCHAR(200)  NULL     COMMENT 'Tên nguyên vật liệu',
    `required_qty`       DECIMAL(15,3) NOT NULL COMMENT 'Số lượng yêu cầu theo BOM',
    `available_qty`      DECIMAL(15,3) NOT NULL COMMENT 'Số lượng tồn kho hiện có',
    `deficit_qty`        DECIMAL(15,3) NOT NULL DEFAULT 0 COMMENT 'Số lượng thiếu hụt',
    `reserved`           TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '1=đã đặt trước cho đơn hàng này',
    `supplier_lead_days` INT           NULL     COMMENT 'Thời gian giao hàng nhà cung cấp (ngày)',
    `expected_arrival`   DATE          NULL     COMMENT 'Ngày dự kiến nhận hàng',
    `status`             VARCHAR(20)   NOT NULL DEFAULT 'checking'
                         COMMENT 'checking, available, shortage, pr_generated, received',
    `check_time`         DATETIME      NULL     COMMENT 'Thời điểm kiểm tra gần nhất',
    `sys_org_code`       VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    KEY `idx_ma_order` (`order_id`),
    KEY `idx_ma_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Tình trạng nguyên vật liệu';

-- ===================================================
-- 3. ap_purchase_request (Yêu cầu mua hàng)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_purchase_request` (
    `id`                 VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `pr_code`            VARCHAR(50)   NOT NULL COMMENT 'Mã PR (PRyyyyMMddNNN)',
    `order_id`           VARCHAR(36)   NOT NULL COMMENT 'FK → ap_planning_order',
    `material_id`        VARCHAR(100)  NOT NULL COMMENT 'Mã nguyên vật liệu',
    `material_name`      VARCHAR(200)  NULL     COMMENT 'Tên nguyên vật liệu',
    `deficit_qty`        DECIMAL(15,3) NOT NULL COMMENT 'Số lượng cần mua',
    `required_date`      DATE          NOT NULL COMMENT 'Ngày giao hàng yêu cầu',
    `supplier_lead_days` INT           NOT NULL COMMENT 'Thời gian giao hàng nhà cung cấp đã dùng',
    `status`             VARCHAR(20)   NOT NULL DEFAULT 'generated'
                         COMMENT 'generated, submitted, confirmed, received',
    `actual_delivery`    DATE          NULL     COMMENT 'Ngày giao hàng thực tế',
    `alternatives`       JSON          NULL     COMMENT 'Các phương án thay thế (JSON)',
    `create_by`          VARCHAR(50)   NULL,
    `create_time`        DATETIME      NULL,
    `update_by`          VARCHAR(50)   NULL,
    `update_time`        DATETIME      NULL,
    `sys_org_code`       VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pr_code` (`pr_code`),
    KEY `idx_pr_order` (`order_id`),
    KEY `idx_pr_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Yêu cầu mua hàng';

-- ===================================================
-- 4. ap_quarterly_plan (Kế hoạch quý)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_quarterly_plan` (
    `id`                  VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `plan_code`           VARCHAR(50)   NOT NULL COMMENT 'Mã kế hoạch (QPyyyyQN)',
    `year`                INT           NOT NULL COMMENT 'Năm',
    `quarter`             INT           NOT NULL COMMENT 'Quý (1-4)',
    `status`              VARCHAR(20)   NOT NULL DEFAULT 'draft'
                          COMMENT 'draft, active, completed',
    `demand_summary`      JSON          NOT NULL COMMENT 'Nhu cầu theo loại sản phẩm mỗi tháng',
    `capacity_validated`  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '1=đã xác nhận công suất',
    `capacity_gaps`       JSON          NULL     COMMENT 'Chi tiết khoảng cách công suất (nếu có)',
    `create_by`           VARCHAR(50)   NULL,
    `create_time`         DATETIME      NULL,
    `update_by`           VARCHAR(50)   NULL,
    `update_time`         DATETIME      NULL,
    `sys_org_code`        VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_qp_period` (`year`, `quarter`, `sys_org_code`),
    KEY `idx_qp_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Kế hoạch quý';

-- ===================================================
-- 5. ap_monthly_plan (Kế hoạch tháng)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_monthly_plan` (
    `id`                    VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `plan_code`             VARCHAR(50)   NOT NULL COMMENT 'Mã kế hoạch (MPyyyyMMNNN)',
    `quarterly_plan_id`     VARCHAR(36)   NOT NULL COMMENT 'FK → ap_quarterly_plan',
    `year`                  INT           NOT NULL COMMENT 'Năm',
    `month`                 INT           NOT NULL COMMENT 'Tháng (1-12)',
    `option_rank`           INT           NOT NULL COMMENT 'Thứ hạng phương án (1-3)',
    `plan_details`          JSON          NOT NULL COMMENT 'SL theo sản phẩm, timeline, dây chuyền, ngày hoàn thành',
    `total_hours`           DECIMAL(10,2) NULL     COMMENT 'Tổng giờ sản xuất kế hoạch',
    `capacity_utilization`  DECIMAL(5,2)  NULL     COMMENT 'Tỷ lệ sử dụng công suất (%)',
    `status`                VARCHAR(20)   NOT NULL DEFAULT 'suggested'
                            COMMENT 'suggested, approved, rejected',
    `approved_by`           VARCHAR(50)   NULL,
    `approved_time`         DATETIME      NULL,
    `create_by`             VARCHAR(50)   NULL,
    `create_time`           DATETIME      NULL,
    `sys_org_code`          VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    KEY `idx_mp_quarterly` (`quarterly_plan_id`),
    KEY `idx_mp_period` (`year`, `month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Kế hoạch tháng';

-- ===================================================
-- 6. ap_weekly_plan (Kế hoạch tuần)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_weekly_plan` (
    `id`                  VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `plan_code`           VARCHAR(50)   NOT NULL COMMENT 'Mã kế hoạch (WPyyyyWNN-NNN)',
    `monthly_plan_id`     VARCHAR(36)   NOT NULL COMMENT 'FK → ap_monthly_plan',
    `year`                INT           NOT NULL COMMENT 'Năm',
    `week_number`         INT           NOT NULL COMMENT 'Số tuần ISO',
    `start_date`          DATE          NOT NULL COMMENT 'Ngày bắt đầu tuần',
    `end_date`            DATE          NOT NULL COMMENT 'Ngày kết thúc tuần',
    `optimization_score`  DECIMAL(5,2)  NULL     COMMENT 'Điểm tối ưu (0-100)',
    `option_rank`         INT           NOT NULL DEFAULT 1 COMMENT 'Thứ hạng phương án (1-3)',
    `status`              VARCHAR(20)   NOT NULL DEFAULT 'draft'
                          COMMENT 'draft, approved, in_execution, completed, rescheduled',
    `material_verified`   TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '1=đã xác nhận nguyên vật liệu',
    `approved_by`         VARCHAR(50)   NULL,
    `approved_time`       DATETIME      NULL,
    `issued_time`         DATETIME      NULL     COMMENT 'Thời điểm phát lệnh sản xuất',
    `version`             INT           NOT NULL DEFAULT 1 COMMENT 'Phiên bản (tăng khi điều chỉnh)',
    `parent_plan_id`      VARCHAR(36)   NULL     COMMENT 'FK → ap_weekly_plan (bản gốc trước điều chỉnh)',
    `create_by`           VARCHAR(50)   NULL,
    `create_time`         DATETIME      NULL,
    `update_by`           VARCHAR(50)   NULL,
    `update_time`         DATETIME      NULL,
    `sys_org_code`        VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wp_code` (`plan_code`),
    KEY `idx_wp_monthly` (`monthly_plan_id`),
    KEY `idx_wp_week` (`year`, `week_number`),
    KEY `idx_wp_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Kế hoạch tuần';

-- ===================================================
-- 7. ap_weekly_plan_batch (Chi tiết batch trong kế hoạch tuần)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_weekly_plan_batch` (
    `id`                  VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `weekly_plan_id`      VARCHAR(36)   NOT NULL COMMENT 'FK → ap_weekly_plan',
    `order_id`            VARCHAR(36)   NOT NULL COMMENT 'FK → ap_planning_order',
    `product_type`        VARCHAR(100)  NOT NULL COMMENT 'Loại sản phẩm',
    `quantity`            DECIMAL(15,2) NOT NULL COMMENT 'Số lượng kế hoạch',
    `gross_quantity`      DECIMAL(15,2) NULL     COMMENT 'SL gộp (đã điều chỉnh theo yield)',
    `production_line_id`  VARCHAR(36)   NOT NULL COMMENT 'Dây chuyền sản xuất được gán',
    `machine_id`          VARCHAR(36)   NULL     COMMENT 'Máy được gán',
    `planned_start`       DATETIME      NOT NULL COMMENT 'Thời điểm bắt đầu kế hoạch',
    `planned_end`         DATETIME      NOT NULL COMMENT 'Thời điểm kết thúc kế hoạch',
    `sequence_order`      INT           NOT NULL COMMENT 'Thứ tự trên dây chuyền',
    `changeover_minutes`  INT           NOT NULL DEFAULT 0 COMMENT 'Thời gian chuyển đổi trước batch (phút)',
    `actual_start`        DATETIME      NULL     COMMENT 'Thời điểm bắt đầu thực tế',
    `actual_end`          DATETIME      NULL     COMMENT 'Thời điểm kết thúc thực tế',
    `actual_quantity`     DECIMAL(15,2) NULL     COMMENT 'Số lượng sản xuất thực tế',
    `status`              VARCHAR(20)   NOT NULL DEFAULT 'planned'
                          COMMENT 'planned, in_progress, completed, rescheduled, on_hold',
    `material_status`     VARCHAR(20)   NOT NULL DEFAULT 'pending'
                          COMMENT 'pending, verified, shortage',
    `sys_org_code`        VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    KEY `idx_wpb_plan` (`weekly_plan_id`),
    KEY `idx_wpb_order` (`order_id`),
    KEY `idx_wpb_line` (`production_line_id`),
    KEY `idx_wpb_sequence` (`weekly_plan_id`, `production_line_id`, `sequence_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Chi tiết batch kế hoạch tuần';

-- ===================================================
-- 8. ap_optimization_score (Chi tiết điểm tối ưu)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_optimization_score` (
    `id`                    VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `weekly_plan_id`        VARCHAR(36)   NOT NULL COMMENT 'FK → ap_weekly_plan',
    `total_score`           DECIMAL(5,2)  NOT NULL COMMENT 'Tổng điểm tối ưu (0-100)',
    `deadline_score`        DECIMAL(5,2)  NOT NULL COMMENT 'Điểm tuân thủ deadline',
    `deadline_weight`       DECIMAL(3,2)  NOT NULL COMMENT 'Trọng số deadline (>=0.40)',
    `utilization_score`     DECIMAL(5,2)  NOT NULL COMMENT 'Điểm sử dụng máy',
    `utilization_weight`    DECIMAL(3,2)  NOT NULL COMMENT 'Trọng số sử dụng máy',
    `material_score`        DECIMAL(5,2)  NOT NULL COMMENT 'Điểm sẵn sàng nguyên vật liệu',
    `material_weight`       DECIMAL(3,2)  NOT NULL COMMENT 'Trọng số nguyên vật liệu',
    `priority_score`        DECIMAL(5,2)  NOT NULL COMMENT 'Điểm ưu tiên đơn hàng',
    `priority_weight`       DECIMAL(3,2)  NOT NULL COMMENT 'Trọng số ưu tiên',
    `historical_data_used`  TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1=dùng dữ liệu lịch sử, 0=dùng ước tính',
    `constraint_violations` JSON          NULL     COMMENT 'Danh sách vi phạm ràng buộc (nếu có)',
    `create_time`           DATETIME      NULL,
    PRIMARY KEY (`id`),
    KEY `idx_os_plan` (`weekly_plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Chi tiết điểm tối ưu';

-- ===================================================
-- 9. ap_production_progress (Tiến độ sản xuất)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_production_progress` (
    `id`                  VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `weekly_plan_id`      VARCHAR(36)   NOT NULL COMMENT 'FK → ap_weekly_plan',
    `batch_id`            VARCHAR(36)   NOT NULL COMMENT 'FK → ap_weekly_plan_batch',
    `production_line_id`  VARCHAR(36)   NOT NULL COMMENT 'Mã dây chuyền sản xuất',
    `report_date`         DATE          NOT NULL COMMENT 'Ngày báo cáo',
    `planned_qty`         DECIMAL(15,2) NOT NULL COMMENT 'Số lượng kế hoạch trong ngày',
    `actual_qty`          DECIMAL(15,2) NOT NULL COMMENT 'Số lượng sản xuất thực tế',
    `defect_qty`          DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'Số lượng lỗi',
    `defect_rate`         DECIMAL(5,4)  NULL     COMMENT 'Tỷ lệ lỗi (0.0000-1.0000)',
    `deviation_pct`       DECIMAL(5,2)  NULL     COMMENT 'Phần trăm sai lệch so với kế hoạch',
    `completion_pct`      DECIMAL(5,2)  NULL     COMMENT 'Phần trăm hoàn thành',
    `machine_status`      VARCHAR(20)   NULL     COMMENT 'Trạng thái máy tại thời điểm báo cáo',
    `notes`               TEXT          NULL,
    `create_time`         DATETIME      NULL,
    `sys_org_code`        VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    KEY `idx_pp_plan` (`weekly_plan_id`),
    KEY `idx_pp_batch` (`batch_id`),
    KEY `idx_pp_date` (`report_date`),
    UNIQUE KEY `uk_pp_batch_date` (`batch_id`, `report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Tiến độ sản xuất hàng ngày';

-- ===================================================
-- 10. ap_reschedule_record (Lịch sử điều chỉnh kế hoạch)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_reschedule_record` (
    `id`                  VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `original_plan_id`    VARCHAR(36)   NOT NULL COMMENT 'FK → ap_weekly_plan (bản gốc)',
    `new_plan_id`         VARCHAR(36)   NULL     COMMENT 'FK → ap_weekly_plan (phiên bản mới)',
    `trigger_type`        VARCHAR(30)   NOT NULL COMMENT 'deviation, machine_breakdown, material_delay',
    `trigger_details`     JSON          NOT NULL COMMENT 'Chi tiết nguyên nhân điều chỉnh',
    `affected_orders`     JSON          NULL     COMMENT 'Danh sách đơn hàng bị ảnh hưởng và tác động',
    `options`             JSON          NOT NULL COMMENT 'Các phương án điều chỉnh được đề xuất',
    `selected_option`     INT           NULL     COMMENT 'Phương án được chọn (1-based)',
    `status`              VARCHAR(20)   NOT NULL DEFAULT 'pending'
                          COMMENT 'pending, approved, rejected',
    `detection_time`      DATETIME      NOT NULL COMMENT 'Thời điểm phát hiện sai lệch',
    `recommendation_time` DATETIME      NULL     COMMENT 'Thời điểm đưa ra đề xuất',
    `decision_time`       DATETIME      NULL     COMMENT 'Thời điểm quản lý quyết định',
    `create_by`           VARCHAR(50)   NULL,
    `create_time`         DATETIME      NULL,
    `sys_org_code`        VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    KEY `idx_rr_original` (`original_plan_id`),
    KEY `idx_rr_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Lịch sử điều chỉnh kế hoạch';

-- ===================================================
-- 11. ap_sync_status (Trạng thái đồng bộ)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_sync_status` (
    `id`                      VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `system_name`             VARCHAR(50)   NOT NULL COMMENT 'Hệ thống: orderhub, erp, scada, qms',
    `last_sync_time`          DATETIME      NULL     COMMENT 'Thời điểm đồng bộ thành công gần nhất',
    `last_attempt_time`       DATETIME      NULL     COMMENT 'Thời điểm thử đồng bộ gần nhất',
    `status`                  VARCHAR(20)   NOT NULL DEFAULT 'active'
                              COMMENT 'active, failed, stale',
    `consecutive_failures`    INT           NOT NULL DEFAULT 0 COMMENT 'Số lần thất bại liên tiếp',
    `last_error`              TEXT          NULL     COMMENT 'Thông báo lỗi gần nhất',
    `data_staleness_minutes`  INT           NULL     COMMENT 'Số phút kể từ lần đồng bộ thành công',
    `sys_org_code`            VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sync_system` (`system_name`, `sys_org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Trạng thái đồng bộ hệ thống';

-- ===================================================
-- 12. ap_supplier_lead_time (Thời gian giao hàng nhà cung cấp)
-- ===================================================
CREATE TABLE IF NOT EXISTS `ap_supplier_lead_time` (
    `id`                 VARCHAR(36)   NOT NULL COMMENT 'Khóa chính',
    `material_id`        VARCHAR(100)  NOT NULL COMMENT 'Mã nguyên vật liệu',
    `supplier_id`        VARCHAR(100)  NULL     COMMENT 'Mã nhà cung cấp',
    `lead_time_days`     INT           NOT NULL COMMENT 'Thời gian giao hàng hiện tại (ngày)',
    `last_actual_days`   INT           NULL     COMMENT 'Thời gian giao hàng thực tế lần cuối',
    `avg_lead_time_days` DECIMAL(5,1)  NULL     COMMENT 'Thời gian giao hàng trung bình (lịch sử)',
    `last_updated`       DATETIME      NOT NULL COMMENT 'Thời điểm cập nhật gần nhất',
    `update_source`      VARCHAR(30)   NULL     COMMENT 'erp_sync, procurement_cycle',
    `sys_org_code`       VARCHAR(64)   NULL     COMMENT 'Mã tổ chức (multi-tenant)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slt_material_supplier` (`material_id`, `supplier_id`, `sys_org_code`),
    KEY `idx_slt_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Planning Agent - Thời gian giao hàng nhà cung cấp';
