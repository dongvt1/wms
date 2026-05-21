-- =====================================================
-- Material Tables – tách riêng khỏi product
-- Created: 2026-03-05
-- =====================================================

-- Bảng nguyên vật liệu (tách riêng khỏi product)
CREATE TABLE IF NOT EXISTS `material` (
  `id`              VARCHAR(36)    NOT NULL,
  `code`            VARCHAR(50)    NOT NULL COMMENT 'Mã vật tư',
  `name`            VARCHAR(200)   NOT NULL COMMENT 'Tên vật tư',
  `description`     VARCHAR(500)   DEFAULT NULL COMMENT 'Mô tả',
  `unit`            VARCHAR(20)    DEFAULT NULL COMMENT 'Đơn vị tính',
  `price`           DECIMAL(15,2)  DEFAULT 0.00 COMMENT 'Giá tham khảo',
  `category_id`     VARCHAR(36)    DEFAULT NULL COMMENT 'FK → product_category.id',
  `min_stock_level` INT            DEFAULT 0 COMMENT 'Tồn kho tối thiểu',
  `current_stock`   INT            DEFAULT 0 COMMENT 'Tồn kho hiện tại',
  `image`           VARCHAR(500)   DEFAULT NULL COMMENT 'Đường dẫn ảnh',
  `weight`          DECIMAL(10,3)  DEFAULT NULL COMMENT 'Cân nặng (kg)',
  `length`          DECIMAL(10,2)  DEFAULT NULL COMMENT 'Chiều dài (mm)',
  `width`           DECIMAL(10,2)  DEFAULT NULL COMMENT 'Chiều rộng (mm)',
  `height`          DECIMAL(10,2)  DEFAULT NULL COMMENT 'Chiều cao (mm)',
  `status`          INT            DEFAULT 1 COMMENT '1=active, 0=inactive',
  `create_by`       VARCHAR(50)    DEFAULT NULL,
  `create_time`     DATETIME       DEFAULT NULL,
  `update_by`       VARCHAR(50)    DEFAULT NULL,
  `update_time`     DATETIME       DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Nguyên vật liệu (tách riêng khỏi product)';

-- Bảng linh kiện thay thế tại cấp vật tư (toàn cục)
CREATE TABLE IF NOT EXISTS `material_substitute` (
  `id`                      VARCHAR(36)   NOT NULL,
  `material_id`             VARCHAR(36)   NOT NULL COMMENT 'FK → material.id',
  `substitute_material_id`  VARCHAR(36)   NOT NULL COMMENT 'FK → material.id',
  `priority`                INT           DEFAULT 1 COMMENT '1 = ưu tiên cao nhất',
  `notes`                   VARCHAR(500)  DEFAULT NULL COMMENT 'Ghi chú',
  `create_time`             DATETIME      DEFAULT NULL,
  `update_time`             DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ms_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Linh kiện thay thế của nguyên vật liệu';
