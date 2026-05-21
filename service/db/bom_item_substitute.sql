-- =====================================================
-- BOM Item Substitute – override linh kiện thay thế per BOM
-- Created: 2026-03-05
-- =====================================================

CREATE TABLE IF NOT EXISTS `pl_bom_item_substitute` (
  `id`                      VARCHAR(36)   NOT NULL,
  `bom_item_id`             VARCHAR(36)   NOT NULL COMMENT 'FK → pl_bom_item.id',
  `substitute_material_id`  VARCHAR(36)   NOT NULL COMMENT 'FK → material.id',
  `priority`                INT           DEFAULT 1 COMMENT '1 = ưu tiên cao nhất',
  `notes`                   VARCHAR(500)  DEFAULT NULL COMMENT 'Ghi chú',
  `create_time`             DATETIME      DEFAULT NULL,
  `update_time`             DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_bis_bom_item_id` (`bom_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Override linh kiện thay thế trong BOM Item';
