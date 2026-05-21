-- ============================================================
-- WMS - TOÀN BỘ BẢNG MODULE WAREHOUSE
-- Chuẩn theo entity Java + JeecgBoot (utf8mb4, InnoDB)
-- Ngày tạo: 2026-03-02
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. DANH MỤC SẢN PHẨM (product_category)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id`           VARCHAR(36)  NOT NULL                    COMMENT 'Khóa chính',
  `create_by`    VARCHAR(50)  DEFAULT NULL                COMMENT 'Người tạo',
  `create_time`  DATETIME     DEFAULT NULL                COMMENT 'Ngày tạo',
  `update_by`    VARCHAR(50)  DEFAULT NULL                COMMENT 'Người cập nhật',
  `update_time`  DATETIME     DEFAULT NULL                COMMENT 'Ngày cập nhật',
  `sys_org_code` VARCHAR(64)  DEFAULT NULL                COMMENT 'Mã tổ chức',
  `name`         VARCHAR(100) DEFAULT NULL                COMMENT 'Tên danh mục',
  `description`  VARCHAR(500) DEFAULT NULL                COMMENT 'Mô tả',
  `parent_id`    VARCHAR(36)  DEFAULT NULL                COMMENT 'ID danh mục cha',
  `status`       TINYINT(1)   NOT NULL DEFAULT 1          COMMENT 'Trạng thái (0: Ngưng, 1: Hoạt động)',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Danh mục sản phẩm';

-- ------------------------------------------------------------
-- 2. SẢN PHẨM (product)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id`              VARCHAR(36)    NOT NULL                COMMENT 'Khóa chính',
  `create_by`       VARCHAR(50)    DEFAULT NULL            COMMENT 'Người tạo',
  `create_time`     DATETIME       DEFAULT NULL            COMMENT 'Ngày tạo',
  `update_by`       VARCHAR(50)    DEFAULT NULL            COMMENT 'Người cập nhật',
  `update_time`     DATETIME       DEFAULT NULL            COMMENT 'Ngày cập nhật',
  `sys_org_code`    VARCHAR(64)    DEFAULT NULL            COMMENT 'Mã tổ chức',
  `code`            VARCHAR(50)    NOT NULL                COMMENT 'Mã sản phẩm',
  `name`            VARCHAR(200)   NOT NULL                COMMENT 'Tên sản phẩm',
  `description`     TEXT           DEFAULT NULL            COMMENT 'Mô tả',
  `price`           DECIMAL(15,2)  NOT NULL DEFAULT 0.00  COMMENT 'Giá bán',
  `category_id`     VARCHAR(36)    DEFAULT NULL            COMMENT 'ID danh mục',
  `min_stock_level` INT            NOT NULL DEFAULT 0      COMMENT 'Mức tồn kho tối thiểu',
  `image`           VARCHAR(500)   DEFAULT NULL            COMMENT 'Ảnh sản phẩm (URL)',
  `status`          TINYINT(1)     NOT NULL DEFAULT 1      COMMENT 'Trạng thái (0: Ngưng, 1: Hoạt động)',
  `current_stock`   INT            NOT NULL DEFAULT 0      COMMENT 'Tồn kho hiện tại',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`code`),
  KEY `idx_category_id` (`category_id`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Sản phẩm';

-- ------------------------------------------------------------
-- 3. LỊCH SỬ SẢN PHẨM (product_history)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `product_history`;
CREATE TABLE `product_history` (
  `id`           VARCHAR(36)  NOT NULL        COMMENT 'Khóa chính',
  `create_by`    VARCHAR(50)  DEFAULT NULL    COMMENT 'Người tạo',
  `create_time`  DATETIME     DEFAULT NULL    COMMENT 'Ngày tạo',
  `sys_org_code` VARCHAR(64)  DEFAULT NULL    COMMENT 'Mã tổ chức',
  `product_id`   VARCHAR(36)  DEFAULT NULL    COMMENT 'ID sản phẩm',
  `action`       VARCHAR(50)  DEFAULT NULL    COMMENT 'Hành động (CREATE/UPDATE/DELETE)',
  `old_data`     TEXT         DEFAULT NULL    COMMENT 'Dữ liệu cũ (JSON)',
  `new_data`     TEXT         DEFAULT NULL    COMMENT 'Dữ liệu mới (JSON)',
  `user_id`      VARCHAR(50)  DEFAULT NULL    COMMENT 'Người thực hiện',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_product_history_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử sản phẩm';

-- ------------------------------------------------------------
-- 4. KHU VỰC KHO (warehouse_area)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `warehouse_area`;
CREATE TABLE `warehouse_area` (
  `id`             VARCHAR(36)  NOT NULL           COMMENT 'Khóa chính',
  `create_by`      VARCHAR(50)  DEFAULT NULL       COMMENT 'Người tạo',
  `create_time`    DATETIME     DEFAULT NULL       COMMENT 'Ngày tạo',
  `update_by`      VARCHAR(50)  DEFAULT NULL       COMMENT 'Người cập nhật',
  `update_time`    DATETIME     DEFAULT NULL       COMMENT 'Ngày cập nhật',
  `sys_org_code`   VARCHAR(64)  DEFAULT NULL       COMMENT 'Mã tổ chức',
  `area_code`      VARCHAR(50)  NOT NULL           COMMENT 'Mã khu vực',
  `area_name`      VARCHAR(100) NOT NULL           COMMENT 'Tên khu vực',
  `description`    VARCHAR(500) DEFAULT NULL       COMMENT 'Mô tả',
  `status`         TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Trạng thái (0: Ngưng, 1: Hoạt động)',
  `capacity`       INT          NOT NULL DEFAULT 0 COMMENT 'Sức chứa tối đa (số kệ)',
  `used_capacity`  INT          NOT NULL DEFAULT 0 COMMENT 'Sức chứa đã dùng',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_code` (`area_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Khu vực kho';

-- ------------------------------------------------------------
-- 5. KỆ KHO (warehouse_shelf)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `warehouse_shelf`;
CREATE TABLE `warehouse_shelf` (
  `id`             VARCHAR(36)  NOT NULL           COMMENT 'Khóa chính',
  `create_by`      VARCHAR(50)  DEFAULT NULL       COMMENT 'Người tạo',
  `create_time`    DATETIME     DEFAULT NULL       COMMENT 'Ngày tạo',
  `update_by`      VARCHAR(50)  DEFAULT NULL       COMMENT 'Người cập nhật',
  `update_time`    DATETIME     DEFAULT NULL       COMMENT 'Ngày cập nhật',
  `sys_org_code`   VARCHAR(64)  DEFAULT NULL       COMMENT 'Mã tổ chức',
  `area_id`        VARCHAR(36)  NOT NULL           COMMENT 'ID khu vực',
  `shelf_code`     VARCHAR(50)  NOT NULL           COMMENT 'Mã kệ',
  `shelf_name`     VARCHAR(100) NOT NULL           COMMENT 'Tên kệ',
  `description`    VARCHAR(500) DEFAULT NULL       COMMENT 'Mô tả',
  `status`         TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Trạng thái (0: Ngưng, 1: Hoạt động)',
  `capacity`       INT          NOT NULL DEFAULT 0 COMMENT 'Sức chứa tối đa (số ô)',
  `used_capacity`  INT          NOT NULL DEFAULT 0 COMMENT 'Sức chứa đã dùng',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shelf_code` (`shelf_code`),
  KEY `idx_area_id` (`area_id`),
  CONSTRAINT `fk_shelf_area` FOREIGN KEY (`area_id`) REFERENCES `warehouse_area` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Kệ kho';

-- ------------------------------------------------------------
-- 6. Ô KHO (warehouse_slot)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `warehouse_slot`;
CREATE TABLE `warehouse_slot` (
  `id`             VARCHAR(36)  NOT NULL           COMMENT 'Khóa chính',
  `create_by`      VARCHAR(50)  DEFAULT NULL       COMMENT 'Người tạo',
  `create_time`    DATETIME     DEFAULT NULL       COMMENT 'Ngày tạo',
  `update_by`      VARCHAR(50)  DEFAULT NULL       COMMENT 'Người cập nhật',
  `update_time`    DATETIME     DEFAULT NULL       COMMENT 'Ngày cập nhật',
  `sys_org_code`   VARCHAR(64)  DEFAULT NULL       COMMENT 'Mã tổ chức',
  `shelf_id`       VARCHAR(36)  NOT NULL           COMMENT 'ID kệ',
  `slot_code`      VARCHAR(50)  NOT NULL           COMMENT 'Mã ô',
  `slot_name`      VARCHAR(100) DEFAULT NULL       COMMENT 'Tên ô',
  `position`       VARCHAR(50)  DEFAULT NULL       COMMENT 'Vị trí (hàng-cột)',
  `description`    VARCHAR(500) DEFAULT NULL       COMMENT 'Mô tả',
  `status`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Trạng thái (0: Trống, 1: Đã đặt, 2: Đang chứa)',
  `capacity`       INT          NOT NULL DEFAULT 1 COMMENT 'Sức chứa tối đa',
  `used_capacity`  INT          NOT NULL DEFAULT 0 COMMENT 'Đang sử dụng',
  `product_code`   VARCHAR(50)  DEFAULT NULL       COMMENT 'Mã sản phẩm đang chứa',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slot_code` (`slot_code`),
  KEY `idx_shelf_id` (`shelf_id`),
  KEY `idx_product_code` (`product_code`),
  CONSTRAINT `fk_slot_shelf` FOREIGN KEY (`shelf_id`) REFERENCES `warehouse_shelf` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Ô chứa hàng trong kho';

-- ------------------------------------------------------------
-- 7. NHÀ CUNG CẤP (suppliers)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `suppliers`;
CREATE TABLE `suppliers` (
  `id`              VARCHAR(36)  NOT NULL           COMMENT 'Khóa chính',
  `create_by`       VARCHAR(50)  DEFAULT NULL       COMMENT 'Người tạo',
  `create_time`     DATETIME     DEFAULT NULL       COMMENT 'Ngày tạo',
  `update_by`       VARCHAR(50)  DEFAULT NULL       COMMENT 'Người cập nhật',
  `update_time`     DATETIME     DEFAULT NULL       COMMENT 'Ngày cập nhật',
  `sys_org_code`    VARCHAR(64)  DEFAULT NULL       COMMENT 'Mã tổ chức',
  `supplier_code`   VARCHAR(50)  NOT NULL           COMMENT 'Mã nhà cung cấp',
  `supplier_name`   VARCHAR(200) NOT NULL           COMMENT 'Tên nhà cung cấp',
  `contact_person`  VARCHAR(100) DEFAULT NULL       COMMENT 'Người liên hệ',
  `phone`           VARCHAR(30)  DEFAULT NULL       COMMENT 'Số điện thoại',
  `email`           VARCHAR(100) DEFAULT NULL       COMMENT 'Email',
  `address`         VARCHAR(500) DEFAULT NULL       COMMENT 'Địa chỉ',
  `tax_code`        VARCHAR(50)  DEFAULT NULL       COMMENT 'Mã số thuế',
  `status`          TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Trạng thái (0: Ngưng, 1: Hoạt động)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`),
  KEY `idx_supplier_name` (`supplier_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhà cung cấp';

-- ------------------------------------------------------------
-- 8. KHÁCH HÀNG (customers)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers` (
  `id`              VARCHAR(36)  NOT NULL           COMMENT 'Khóa chính',
  `create_by`       VARCHAR(50)  DEFAULT NULL       COMMENT 'Người tạo',
  `create_time`     DATETIME     DEFAULT NULL       COMMENT 'Ngày tạo',
  `update_by`       VARCHAR(50)  DEFAULT NULL       COMMENT 'Người cập nhật',
  `update_time`     DATETIME     DEFAULT NULL       COMMENT 'Ngày cập nhật',
  `sys_org_code`    VARCHAR(64)  DEFAULT NULL       COMMENT 'Mã tổ chức',
  `customer_code`   VARCHAR(50)  NOT NULL           COMMENT 'Mã khách hàng',
  `customer_name`   VARCHAR(200) NOT NULL           COMMENT 'Tên khách hàng',
  `contact_person`  VARCHAR(100) DEFAULT NULL       COMMENT 'Người liên hệ',
  `phone`           VARCHAR(30)  DEFAULT NULL       COMMENT 'Số điện thoại',
  `email`           VARCHAR(100) DEFAULT NULL       COMMENT 'Email',
  `address`         VARCHAR(500) DEFAULT NULL       COMMENT 'Địa chỉ',
  `tax_code`        VARCHAR(50)  DEFAULT NULL       COMMENT 'Mã số thuế',
  `status`          TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Trạng thái (0: Ngưng, 1: Hoạt động)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_code` (`customer_code`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Khách hàng';

-- ------------------------------------------------------------
-- 9. SỐ DƯ KHÁCH HÀNG (customer_balances)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `customer_balances`;
CREATE TABLE `customer_balances` (
  `id`            VARCHAR(36)    NOT NULL               COMMENT 'Khóa chính',
  `create_by`     VARCHAR(50)    DEFAULT NULL            COMMENT 'Người tạo',
  `create_time`   DATETIME       DEFAULT NULL            COMMENT 'Ngày tạo',
  `update_by`     VARCHAR(50)    DEFAULT NULL            COMMENT 'Người cập nhật',
  `update_time`   DATETIME       DEFAULT NULL            COMMENT 'Ngày cập nhật',
  `sys_org_code`  VARCHAR(64)    DEFAULT NULL            COMMENT 'Mã tổ chức',
  `customer_id`   VARCHAR(36)    NOT NULL                COMMENT 'ID khách hàng',
  `balance`       DECIMAL(15,2)  NOT NULL DEFAULT 0.00  COMMENT 'Số dư (dương: có, âm: nợ)',
  `last_updated`  DATETIME       DEFAULT NULL            COMMENT 'Lần cập nhật cuối',
  `updated_by`    VARCHAR(50)    DEFAULT NULL            COMMENT 'Người cập nhật cuối',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_id` (`customer_id`),
  CONSTRAINT `fk_customer_balance` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Số dư khách hàng';

-- ------------------------------------------------------------
-- 10. TỒN KHO (inventory)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `id`                  VARCHAR(36)  NOT NULL               COMMENT 'Khóa chính',
  `create_by`           VARCHAR(50)  DEFAULT NULL           COMMENT 'Người tạo',
  `create_time`         DATETIME     DEFAULT NULL           COMMENT 'Ngày tạo',
  `update_by`           VARCHAR(50)  DEFAULT NULL           COMMENT 'Người cập nhật',
  `update_time`         DATETIME     DEFAULT NULL           COMMENT 'Ngày cập nhật',
  `sys_org_code`        VARCHAR(64)  DEFAULT NULL           COMMENT 'Mã tổ chức',
  `product_id`          VARCHAR(36)  NOT NULL               COMMENT 'ID sản phẩm',
  `quantity`            INT          NOT NULL DEFAULT 0     COMMENT 'Tổng số lượng',
  `reserved_quantity`   INT          NOT NULL DEFAULT 0     COMMENT 'Số lượng đã đặt trước',
  `available_quantity`  INT          NOT NULL DEFAULT 0     COMMENT 'Số lượng có thể bán',
  `last_updated`        DATETIME     DEFAULT NULL           COMMENT 'Thời gian cập nhật cuối',
  `updated_by`          VARCHAR(50)  DEFAULT NULL           COMMENT 'Người cập nhật cuối',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_product` (`product_id`),
  CONSTRAINT `fk_inventory_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tồn kho';

-- ------------------------------------------------------------
-- 11. GIAO DỊCH TỒN KHO (inventory_transactions)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `inventory_transactions`;
CREATE TABLE `inventory_transactions` (
  `id`                VARCHAR(36)  NOT NULL            COMMENT 'Khóa chính',
  `create_by`         VARCHAR(50)  DEFAULT NULL        COMMENT 'Người tạo',
  `create_time`       DATETIME     DEFAULT NULL        COMMENT 'Ngày tạo',
  `update_by`         VARCHAR(50)  DEFAULT NULL        COMMENT 'Người cập nhật',
  `update_time`       DATETIME     DEFAULT NULL        COMMENT 'Ngày cập nhật',
  `sys_org_code`      VARCHAR(64)  DEFAULT NULL        COMMENT 'Mã tổ chức',
  `product_id`        VARCHAR(36)  NOT NULL            COMMENT 'ID sản phẩm',
  `transaction_type`  VARCHAR(20)  NOT NULL            COMMENT 'Loại giao dịch (IN/OUT/ADJUST)',
  `quantity`          INT          NOT NULL            COMMENT 'Số lượng giao dịch',
  `reference_id`      VARCHAR(36)  DEFAULT NULL        COMMENT 'ID phiếu tham chiếu',
  `reason`            VARCHAR(500) DEFAULT NULL        COMMENT 'Lý do giao dịch',
  `user_id`           VARCHAR(50)  DEFAULT NULL        COMMENT 'Người thực hiện',
  `created_at`        DATETIME     DEFAULT NULL        COMMENT 'Thời gian giao dịch',
  PRIMARY KEY (`id`),
  KEY `idx_invtxn_product` (`product_id`),
  KEY `idx_invtxn_type` (`transaction_type`),
  KEY `idx_invtxn_time` (`created_at`),
  CONSTRAINT `fk_invtxn_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Giao dịch tồn kho';

-- ------------------------------------------------------------
-- 12. ĐIỀU CHỈNH TỒN KHO (inventory_adjustments)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `inventory_adjustments`;
CREATE TABLE `inventory_adjustments` (
  `id`                 VARCHAR(36)  NOT NULL          COMMENT 'Khóa chính',
  `create_by`          VARCHAR(50)  DEFAULT NULL      COMMENT 'Người tạo',
  `create_time`        DATETIME     DEFAULT NULL      COMMENT 'Ngày tạo',
  `update_by`          VARCHAR(50)  DEFAULT NULL      COMMENT 'Người cập nhật',
  `update_time`        DATETIME     DEFAULT NULL      COMMENT 'Ngày cập nhật',
  `sys_org_code`       VARCHAR(64)  DEFAULT NULL      COMMENT 'Mã tổ chức',
  `product_id`         VARCHAR(36)  NOT NULL          COMMENT 'ID sản phẩm',
  `old_quantity`       INT          NOT NULL DEFAULT 0 COMMENT 'Số lượng cũ',
  `new_quantity`       INT          NOT NULL DEFAULT 0 COMMENT 'Số lượng mới',
  `adjustment_reason`  VARCHAR(500) DEFAULT NULL      COMMENT 'Lý do điều chỉnh',
  `user_id`            VARCHAR(50)  DEFAULT NULL      COMMENT 'Người thực hiện',
  `created_at`         DATETIME     DEFAULT NULL      COMMENT 'Thời gian điều chỉnh',
  PRIMARY KEY (`id`),
  KEY `idx_invadj_product` (`product_id`),
  KEY `idx_invadj_time` (`created_at`),
  CONSTRAINT `fk_invadj_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Điều chỉnh tồn kho';

-- ------------------------------------------------------------
-- 13. CẢNH BÁO TỒN KHO (inventory_alerts)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `inventory_alerts`;
CREATE TABLE `inventory_alerts` (
  `id`                VARCHAR(36)  NOT NULL               COMMENT 'Khóa chính',
  `create_by`         VARCHAR(50)  DEFAULT NULL           COMMENT 'Người tạo',
  `create_time`       DATETIME     DEFAULT NULL           COMMENT 'Ngày tạo',
  `update_by`         VARCHAR(50)  DEFAULT NULL           COMMENT 'Người cập nhật',
  `update_time`       DATETIME     DEFAULT NULL           COMMENT 'Ngày cập nhật',
  `sys_org_code`      VARCHAR(64)  DEFAULT NULL           COMMENT 'Mã tổ chức',
  `product_id`        VARCHAR(36)  NOT NULL               COMMENT 'ID sản phẩm',
  `product_name`      VARCHAR(200) DEFAULT NULL           COMMENT 'Tên sản phẩm (denorm)',
  `product_code`      VARCHAR(50)  DEFAULT NULL           COMMENT 'Mã sản phẩm (denorm)',
  `alert_type`        VARCHAR(30)  NOT NULL               COMMENT 'Loại cảnh báo (LOW_STOCK/OUT_OF_STOCK/OVERSTOCK)',
  `current_quantity`  INT          NOT NULL DEFAULT 0     COMMENT 'Số lượng hiện tại',
  `threshold_value`   INT          NOT NULL DEFAULT 0     COMMENT 'Ngưỡng cảnh báo',
  `alert_status`      VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT 'Trạng thái (OPEN/RESOLVED)',
  `resolved_at`       DATETIME     DEFAULT NULL           COMMENT 'Thời gian giải quyết',
  `resolved_by`       VARCHAR(50)  DEFAULT NULL           COMMENT 'Người giải quyết',
  `created_at`        DATETIME     DEFAULT NULL           COMMENT 'Thời gian tạo cảnh báo',
  PRIMARY KEY (`id`),
  KEY `idx_alert_product` (`product_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_alert_status` (`alert_status`),
  CONSTRAINT `fk_alert_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Cảnh báo tồn kho';

-- ------------------------------------------------------------
-- 14. PHIẾU XUẤT NHẬP KHO (stock_transactions)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `stock_transactions`;
CREATE TABLE `stock_transactions` (
  `id`                VARCHAR(36)  NOT NULL                      COMMENT 'Khóa chính',
  `create_by`         VARCHAR(50)  DEFAULT NULL                  COMMENT 'Người tạo',
  `create_time`       DATETIME     DEFAULT NULL                  COMMENT 'Ngày tạo',
  `update_by`         VARCHAR(50)  DEFAULT NULL                  COMMENT 'Người cập nhật',
  `update_time`       DATETIME     DEFAULT NULL                  COMMENT 'Ngày cập nhật',
  `sys_org_code`      VARCHAR(64)  DEFAULT NULL                  COMMENT 'Mã tổ chức',
  `transaction_code`  VARCHAR(50)  NOT NULL                      COMMENT 'Mã phiếu',
  `transaction_type`  VARCHAR(20)  NOT NULL                      COMMENT 'Loại phiếu (IN/OUT/TRANSFER)',
  `transaction_date`  DATETIME     DEFAULT NULL                  COMMENT 'Ngày thực hiện',
  `status`            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'    COMMENT 'Trạng thái (PENDING/APPROVED/CANCELLED)',
  `supplier_id`       VARCHAR(36)  DEFAULT NULL                  COMMENT 'ID nhà cung cấp (nhập kho)',
  `customer_id`       VARCHAR(36)  DEFAULT NULL                  COMMENT 'ID khách hàng (xuất kho)',
  `created_by`        VARCHAR(50)  DEFAULT NULL                  COMMENT 'Người lập phiếu',
  `approved_by`       VARCHAR(50)  DEFAULT NULL                  COMMENT 'Người duyệt',
  `approved_at`       DATETIME     DEFAULT NULL                  COMMENT 'Thời gian duyệt',
  `notes`             TEXT         DEFAULT NULL                  COMMENT 'Ghi chú',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_code` (`transaction_code`),
  KEY `idx_sttxn_date` (`transaction_date`),
  KEY `idx_sttxn_type` (`transaction_type`),
  KEY `idx_sttxn_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phiếu xuất nhập kho';

-- ------------------------------------------------------------
-- 15. CHI TIẾT PHIẾU XUẤT NHẬP KHO (stock_transaction_items)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `stock_transaction_items`;
CREATE TABLE `stock_transaction_items` (
  `id`                VARCHAR(36)    NOT NULL               COMMENT 'Khóa chính',
  `create_by`         VARCHAR(50)    DEFAULT NULL           COMMENT 'Người tạo',
  `create_time`       DATETIME       DEFAULT NULL           COMMENT 'Ngày tạo',
  `update_by`         VARCHAR(50)    DEFAULT NULL           COMMENT 'Người cập nhật',
  `update_time`       DATETIME       DEFAULT NULL           COMMENT 'Ngày cập nhật',
  `sys_org_code`      VARCHAR(64)    DEFAULT NULL           COMMENT 'Mã tổ chức',
  `transaction_id`    VARCHAR(36)    NOT NULL               COMMENT 'ID phiếu',
  `product_id`        VARCHAR(36)    NOT NULL               COMMENT 'ID sản phẩm',
  `quantity`          INT            NOT NULL               COMMENT 'Số lượng',
  `unit_price`        DECIMAL(15,2)  NOT NULL DEFAULT 0.00  COMMENT 'Đơn giá',
  `total_price`       DECIMAL(15,2)  NOT NULL DEFAULT 0.00  COMMENT 'Thành tiền',
  `from_location_id`  VARCHAR(36)    DEFAULT NULL           COMMENT 'Ô kho nguồn',
  `to_location_id`    VARCHAR(36)    DEFAULT NULL           COMMENT 'Ô kho đích',
  `batch_number`      VARCHAR(50)    DEFAULT NULL           COMMENT 'Số lô',
  `expiry_date`       DATE           DEFAULT NULL           COMMENT 'Hạn sử dụng',
  PRIMARY KEY (`id`),
  KEY `idx_stitem_txn` (`transaction_id`),
  KEY `idx_stitem_product` (`product_id`),
  CONSTRAINT `fk_stitem_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `stock_transactions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_stitem_product`     FOREIGN KEY (`product_id`)    REFERENCES `product` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chi tiết phiếu xuất nhập kho';

-- ------------------------------------------------------------
-- 16. ĐƠN HÀNG (orders)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id`               VARCHAR(36)    NOT NULL                    COMMENT 'Khóa chính',
  `create_by`        VARCHAR(50)    DEFAULT NULL                COMMENT 'Người tạo',
  `create_time`      DATETIME       DEFAULT NULL                COMMENT 'Ngày tạo',
  `update_by`        VARCHAR(50)    DEFAULT NULL                COMMENT 'Người cập nhật',
  `update_time`      DATETIME       DEFAULT NULL                COMMENT 'Ngày cập nhật',
  `sys_org_code`     VARCHAR(64)    DEFAULT NULL                COMMENT 'Mã tổ chức',
  `order_code`       VARCHAR(50)    NOT NULL                    COMMENT 'Mã đơn hàng',
  `customer_id`      VARCHAR(36)    DEFAULT NULL                COMMENT 'ID khách hàng',
  `customer_name`    VARCHAR(200)   DEFAULT NULL                COMMENT 'Tên khách hàng (denorm)',
  `order_date`       DATETIME       DEFAULT NULL                COMMENT 'Ngày đặt hàng',
  `status`           VARCHAR(20)    NOT NULL DEFAULT 'PENDING'  COMMENT 'Trạng thái (PENDING/CONFIRMED/PROCESSING/SHIPPED/DELIVERED/CANCELLED)',
  `total_amount`     DECIMAL(15,2)  NOT NULL DEFAULT 0.00       COMMENT 'Tổng tiền',
  `discount_amount`  DECIMAL(15,2)  NOT NULL DEFAULT 0.00       COMMENT 'Tiền giảm giá',
  `tax_amount`       DECIMAL(15,2)  NOT NULL DEFAULT 0.00       COMMENT 'Tiền thuế',
  `final_amount`     DECIMAL(15,2)  NOT NULL DEFAULT 0.00       COMMENT 'Tiền thanh toán cuối',
  `notes`            TEXT           DEFAULT NULL                COMMENT 'Ghi chú',
  `created_by`       VARCHAR(50)    DEFAULT NULL                COMMENT 'Người tạo đơn',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_code` (`order_code`),
  KEY `idx_order_customer` (`customer_id`),
  KEY `idx_order_date` (`order_date`),
  KEY `idx_order_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Đơn hàng';

-- ------------------------------------------------------------
-- 17. CHI TIẾT ĐƠN HÀNG (order_items)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items` (
  `id`               VARCHAR(36)    NOT NULL               COMMENT 'Khóa chính',
  `create_by`        VARCHAR(50)    DEFAULT NULL           COMMENT 'Người tạo',
  `create_time`      DATETIME       DEFAULT NULL           COMMENT 'Ngày tạo',
  `update_by`        VARCHAR(50)    DEFAULT NULL           COMMENT 'Người cập nhật',
  `update_time`      DATETIME       DEFAULT NULL           COMMENT 'Ngày cập nhật',
  `sys_org_code`     VARCHAR(64)    DEFAULT NULL           COMMENT 'Mã tổ chức',
  `order_id`         VARCHAR(36)    NOT NULL               COMMENT 'ID đơn hàng',
  `product_id`       VARCHAR(36)    NOT NULL               COMMENT 'ID sản phẩm',
  `product_name`     VARCHAR(200)   DEFAULT NULL           COMMENT 'Tên sản phẩm (denorm)',
  `product_code`     VARCHAR(50)    DEFAULT NULL           COMMENT 'Mã sản phẩm (denorm)',
  `quantity`         INT            NOT NULL               COMMENT 'Số lượng',
  `unit_price`       DECIMAL(15,2)  NOT NULL DEFAULT 0.00  COMMENT 'Đơn giá',
  `total_price`      DECIMAL(15,2)  NOT NULL DEFAULT 0.00  COMMENT 'Thành tiền',
  `discount_amount`  DECIMAL(15,2)  NOT NULL DEFAULT 0.00  COMMENT 'Tiền giảm giá',
  `final_amount`     DECIMAL(15,2)  NOT NULL DEFAULT 0.00  COMMENT 'Tiền thanh toán cuối',
  PRIMARY KEY (`id`),
  KEY `idx_oitem_order` (`order_id`),
  KEY `idx_oitem_product` (`product_id`),
  CONSTRAINT `fk_oitem_order`   FOREIGN KEY (`order_id`)   REFERENCES `orders`  (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_oitem_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chi tiết đơn hàng';

-- ------------------------------------------------------------
-- 18. LỊCH SỬ TRẠNG THÁI ĐƠN HÀNG (order_status_history)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `order_status_history`;
CREATE TABLE `order_status_history` (
  `id`             VARCHAR(36)  NOT NULL   COMMENT 'Khóa chính',
  `create_by`      VARCHAR(50)  DEFAULT NULL COMMENT 'Người tạo',
  `create_time`    DATETIME     DEFAULT NULL COMMENT 'Ngày tạo',
  `sys_org_code`   VARCHAR(64)  DEFAULT NULL COMMENT 'Mã tổ chức',
  `order_id`       VARCHAR(36)  NOT NULL   COMMENT 'ID đơn hàng',
  `old_status`     VARCHAR(20)  DEFAULT NULL COMMENT 'Trạng thái cũ',
  `new_status`     VARCHAR(20)  NOT NULL   COMMENT 'Trạng thái mới',
  `changed_by`     VARCHAR(50)  DEFAULT NULL COMMENT 'Người thay đổi',
  `changed_at`     DATETIME     DEFAULT NULL COMMENT 'Thời gian thay đổi',
  `notes`          VARCHAR(500) DEFAULT NULL COMMENT 'Ghi chú',
  PRIMARY KEY (`id`),
  KEY `idx_osh_order` (`order_id`),
  CONSTRAINT `fk_osh_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử trạng thái đơn hàng';

-- ------------------------------------------------------------
-- 19. NHẬT KÝ XỬ LÝ ĐƠN HÀNG (order_processing_log)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `order_processing_log`;
CREATE TABLE `order_processing_log` (
  `id`           VARCHAR(36)  NOT NULL   COMMENT 'Khóa chính',
  `create_by`    VARCHAR(50)  DEFAULT NULL COMMENT 'Người tạo',
  `create_time`  DATETIME     DEFAULT NULL COMMENT 'Ngày tạo',
  `sys_org_code` VARCHAR(64)  DEFAULT NULL COMMENT 'Mã tổ chức',
  `order_id`     VARCHAR(36)  NOT NULL   COMMENT 'ID đơn hàng',
  `action`       VARCHAR(100) NOT NULL   COMMENT 'Hành động (RESERVE/RELEASE/CONFIRM...)',
  `detail`       TEXT         DEFAULT NULL COMMENT 'Chi tiết (JSON)',
  `result`       VARCHAR(20)  NOT NULL   COMMENT 'Kết quả (SUCCESS/FAILED)',
  `error_msg`    TEXT         DEFAULT NULL COMMENT 'Thông báo lỗi',
  `processed_by` VARCHAR(50)  DEFAULT NULL COMMENT 'Người xử lý',
  `processed_at` DATETIME     DEFAULT NULL COMMENT 'Thời gian xử lý',
  PRIMARY KEY (`id`),
  KEY `idx_log_order` (`order_id`),
  CONSTRAINT `fk_log_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nhật ký xử lý đơn hàng';

-- ------------------------------------------------------------
-- 20. THÔNG BÁO ĐƠN HÀNG (order_notifications)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `order_notifications`;
CREATE TABLE `order_notifications` (
  `id`            VARCHAR(36)  NOT NULL                   COMMENT 'Khóa chính',
  `create_by`     VARCHAR(50)  DEFAULT NULL               COMMENT 'Người tạo',
  `create_time`   DATETIME     DEFAULT NULL               COMMENT 'Ngày tạo',
  `update_by`     VARCHAR(50)  DEFAULT NULL               COMMENT 'Người cập nhật',
  `update_time`   DATETIME     DEFAULT NULL               COMMENT 'Ngày cập nhật',
  `sys_org_code`  VARCHAR(64)  DEFAULT NULL               COMMENT 'Mã tổ chức',
  `order_id`      VARCHAR(36)  NOT NULL                   COMMENT 'ID đơn hàng',
  `notify_type`   VARCHAR(50)  NOT NULL                   COMMENT 'Loại thông báo (EMAIL/SMS/SYSTEM)',
  `recipient`     VARCHAR(200) DEFAULT NULL               COMMENT 'Người nhận',
  `subject`       VARCHAR(500) DEFAULT NULL               COMMENT 'Tiêu đề',
  `content`       TEXT         DEFAULT NULL               COMMENT 'Nội dung',
  `status`        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái (PENDING/SENT/FAILED)',
  `sent_at`       DATETIME     DEFAULT NULL               COMMENT 'Thời gian gửi',
  `error_msg`     TEXT         DEFAULT NULL               COMMENT 'Thông báo lỗi',
  PRIMARY KEY (`id`),
  KEY `idx_notif_order` (`order_id`),
  CONSTRAINT `fk_notif_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Thông báo đơn hàng';

-- ============================================================
-- DỮ LIỆU DANH MỤC (sys_dict)
-- ============================================================

-- Trạng thái sản phẩm
INSERT IGNORE INTO `sys_dict` (`dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`)
VALUES
  ('Trạng thái sản phẩm',    'product_status',             'Trạng thái sản phẩm',           '0', 'admin', NOW(), '0'),
  ('Hành động lịch sử SP',   'product_history_action',     'Hành động lịch sử sản phẩm',    '0', 'admin', NOW(), '0'),
  ('Loại giao dịch tồn kho', 'inventory_transaction_type', 'Loại giao dịch tồn kho',        '0', 'admin', NOW(), '0'),
  ('Loại phiếu kho',         'stock_transaction_type',     'Loại phiếu xuất nhập kho',      '0', 'admin', NOW(), '0'),
  ('Trạng thái phiếu kho',   'stock_transaction_status',   'Trạng thái phiếu xuất nhập kho','0', 'admin', NOW(), '0'),
  ('Trạng thái kho',         'warehouse_status',           'Trạng thái khu vực kho',        '0', 'admin', NOW(), '0'),
  ('Trạng thái ô kho',       'slot_status',                'Trạng thái ô chứa hàng',        '0', 'admin', NOW(), '0'),
  ('Trạng thái đơn hàng',    'order_status',               'Trạng thái đơn hàng',           '0', 'admin', NOW(), '0'),
  ('Trạng thái khách hàng',  'customer_status',            'Trạng thái khách hàng',         '0', 'admin', NOW(), '0'),
  ('Loại cảnh báo tồn kho',  'inventory_alert_type',       'Loại cảnh báo tồn kho',         '0', 'admin', NOW(), '0'),
  ('Trạng thái cảnh báo TK', 'inventory_alert_status',     'Trạng thái cảnh báo tồn kho',   '0', 'admin', NOW(), '0');

-- Giá trị từng danh mục
INSERT IGNORE INTO `sys_dict_item` (`dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `sort_order`, `status`)
SELECT d.id, v.item_text, v.item_value, v.description, 'admin', NOW(), v.sort_order, '1'
FROM sys_dict d
JOIN (
  SELECT 'product_status' AS dc, 'Ngưng hoạt động' AS item_text, '0' AS item_value, '' AS description, 1 AS sort_order UNION ALL
  SELECT 'product_status', 'Hoạt động', '1', '', 2 UNION ALL
  SELECT 'product_history_action', 'Tạo mới', 'CREATE', '', 1 UNION ALL
  SELECT 'product_history_action', 'Cập nhật', 'UPDATE', '', 2 UNION ALL
  SELECT 'product_history_action', 'Xóa', 'DELETE', '', 3 UNION ALL
  SELECT 'inventory_transaction_type', 'Nhập kho', 'IN', '', 1 UNION ALL
  SELECT 'inventory_transaction_type', 'Xuất kho', 'OUT', '', 2 UNION ALL
  SELECT 'inventory_transaction_type', 'Điều chỉnh', 'ADJUST', '', 3 UNION ALL
  SELECT 'stock_transaction_type', 'Nhập kho', 'IN', '', 1 UNION ALL
  SELECT 'stock_transaction_type', 'Xuất kho', 'OUT', '', 2 UNION ALL
  SELECT 'stock_transaction_type', 'Chuyển kho', 'TRANSFER', '', 3 UNION ALL
  SELECT 'stock_transaction_status', 'Chờ duyệt', 'PENDING', '', 1 UNION ALL
  SELECT 'stock_transaction_status', 'Đã duyệt', 'APPROVED', '', 2 UNION ALL
  SELECT 'stock_transaction_status', 'Đã hủy', 'CANCELLED', '', 3 UNION ALL
  SELECT 'warehouse_status', 'Ngưng hoạt động', '0', '', 1 UNION ALL
  SELECT 'warehouse_status', 'Hoạt động', '1', '', 2 UNION ALL
  SELECT 'slot_status', 'Trống', '0', '', 1 UNION ALL
  SELECT 'slot_status', 'Đã đặt', '1', '', 2 UNION ALL
  SELECT 'slot_status', 'Đang chứa', '2', '', 3 UNION ALL
  SELECT 'order_status', 'Chờ xác nhận', 'PENDING', '', 1 UNION ALL
  SELECT 'order_status', 'Đã xác nhận', 'CONFIRMED', '', 2 UNION ALL
  SELECT 'order_status', 'Đang xử lý', 'PROCESSING', '', 3 UNION ALL
  SELECT 'order_status', 'Đang giao', 'SHIPPED', '', 4 UNION ALL
  SELECT 'order_status', 'Đã giao', 'DELIVERED', '', 5 UNION ALL
  SELECT 'order_status', 'Đã hủy', 'CANCELLED', '', 6 UNION ALL
  SELECT 'customer_status', 'Ngưng hoạt động', '0', '', 1 UNION ALL
  SELECT 'customer_status', 'Hoạt động', '1', '', 2 UNION ALL
  SELECT 'inventory_alert_type', 'Tồn kho thấp', 'LOW_STOCK', '', 1 UNION ALL
  SELECT 'inventory_alert_type', 'Hết hàng', 'OUT_OF_STOCK', '', 2 UNION ALL
  SELECT 'inventory_alert_type', 'Dư thừa', 'OVERSTOCK', '', 3 UNION ALL
  SELECT 'inventory_alert_status', 'Đang mở', 'OPEN', '', 1 UNION ALL
  SELECT 'inventory_alert_status', 'Đã xử lý', 'RESOLVED', '', 2
) v ON d.dict_code = v.dc;

-- ============================================================
-- DỮ LIỆU MẪU
-- ============================================================

-- Danh mục sản phẩm
INSERT INTO `product_category` (`id`,`create_by`,`create_time`,`sys_org_code`,`name`,`description`,`parent_id`,`status`) VALUES
  (UUID(),'admin',NOW(),'A01','Nguyên vật liệu','Nguyên liệu sản xuất',NULL,1),
  (UUID(),'admin',NOW(),'A01','Thành phẩm','Sản phẩm hoàn thiện',NULL,1),
  (UUID(),'admin',NOW(),'A01','Bán thành phẩm','Sản phẩm bán hoàn thiện',NULL,1),
  (UUID(),'admin',NOW(),'A01','Phụ tùng, thiết bị','Phụ tùng và thiết bị hỗ trợ',NULL,1);

-- Nhà cung cấp mẫu
INSERT INTO `suppliers` (`id`,`create_by`,`create_time`,`sys_org_code`,`supplier_code`,`supplier_name`,`contact_person`,`phone`,`email`,`address`,`status`) VALUES
  (UUID(),'admin',NOW(),'A01','NCC001','Công ty TNHH Vật liệu Việt Nam','Nguyễn Văn A','0901234567','a@vatlieu.vn','123 Lê Lợi, Q1, TP.HCM',1),
  (UUID(),'admin',NOW(),'A01','NCC002','Công ty CP Cung ứng Toàn Cầu','Trần Thị B','0902345678','b@toanCAU.vn','456 Điện Biên Phủ, Q3, TP.HCM',1),
  (UUID(),'admin',NOW(),'A01','NCC003','Xí nghiệp Sản xuất Miền Nam','Lê Văn C','0903456789','c@xnmn.vn','789 Nguyễn Huệ, Q1, TP.HCM',1);

-- Khách hàng mẫu
INSERT INTO `customers` (`id`,`create_by`,`create_time`,`sys_org_code`,`customer_code`,`customer_name`,`contact_person`,`phone`,`email`,`address`,`tax_code`,`status`) VALUES
  (UUID(),'admin',NOW(),'A01','KH001','Công ty TNHH ABC','Phạm Thị D','0904567890','d@abc.vn','10 Hai Bà Trưng, Q1, TP.HCM','0101234567',1),
  (UUID(),'admin',NOW(),'A01','KH002','Công ty CP XYZ','Ngô Văn E','0905678901','e@xyz.vn','20 Nam Kỳ Khởi Nghĩa, Q3, TP.HCM','0201234567',1),
  (UUID(),'admin',NOW(),'A01','KH003','Doanh nghiệp tư nhân DEF','Đặng Thị F','0906789012','f@def.vn','30 Lê Duẩn, Q1, TP.HCM','0301234567',1);

-- Khu vực kho mẫu
INSERT INTO `warehouse_area` (`id`,`create_by`,`create_time`,`sys_org_code`,`area_code`,`area_name`,`description`,`status`,`capacity`,`used_capacity`) VALUES
  (UUID(),'admin',NOW(),'A01','KVNVL','Khu vực nguyên vật liệu','Lưu trữ nguyên liệu đầu vào',1,20,0),
  (UUID(),'admin',NOW(),'A01','KVTP', 'Khu vực thành phẩm',       'Lưu trữ sản phẩm hoàn thiện',1,30,0),
  (UUID(),'admin',NOW(),'A01','KVBTP','Khu vực bán thành phẩm',   'Lưu trữ sản phẩm chưa hoàn thiện',1,15,0);

SET FOREIGN_KEY_CHECKS = 1;
