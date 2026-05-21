-- Add minimum stock threshold to inventory table
ALTER TABLE `inventory` ADD COLUMN `min_stock_threshold` int(11) DEFAULT '0' COMMENT 'Minimum stock threshold for alerts';

-- Create inventory alerts table
CREATE TABLE `inventory_alerts` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `product_id` varchar(36) NOT NULL COMMENT 'Product ID',
  `alert_type` varchar(20) NOT NULL COMMENT 'Alert type (LOW_STOCK, OUT_OF_STOCK)',
  `current_quantity` int(11) NOT NULL COMMENT 'Current quantity when alert triggered',
  `threshold_value` int(11) NOT NULL COMMENT 'Threshold value that triggered the alert',
  `alert_status` varchar(20) DEFAULT 'ACTIVE' COMMENT 'Alert status (ACTIVE, RESOLVED, DISMISSED)',
  `resolved_at` datetime DEFAULT NULL COMMENT 'When alert was resolved',
  `resolved_by` varchar(50) DEFAULT NULL COMMENT 'Who resolved the alert',
  `created_at` datetime DEFAULT NULL COMMENT 'Alert creation timestamp',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_alert_status` (`alert_status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_inventory_alerts_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory alerts table';

-- Create inventory alert configurations table
CREATE TABLE `inventory_alert_configs` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `product_id` varchar(36) DEFAULT NULL COMMENT 'Product ID (NULL for global config)',
  `alert_type` varchar(20) NOT NULL COMMENT 'Alert type (LOW_STOCK, OUT_OF_STOCK)',
  `enabled` tinyint(1) DEFAULT '1' COMMENT 'Alert enabled (0: No, 1: Yes)',
  `email_enabled` tinyint(1) DEFAULT '1' COMMENT 'Email alert enabled (0: No, 1: Yes)',
  `notification_enabled` tinyint(1) DEFAULT '1' COMMENT 'System notification enabled (0: No, 1: Yes)',
  `email_addresses` text DEFAULT NULL COMMENT 'Comma-separated email addresses',
  `threshold_percentage` int(3) DEFAULT '20' COMMENT 'Threshold percentage for low stock alerts',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_alert_type` (`alert_type`),
  CONSTRAINT `fk_inventory_alert_configs_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory alert configurations table';

-- Insert dictionary data for inventory alerts
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`)
VALUES
(NULL, 'Inventory Alert Type', 'inventory_alert_type', 'Inventory alert type', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data for inventory alerts
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`)
VALUES
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'inventory_alert_type'), 'Low Stock', 'LOW_STOCK', 'Low stock alert', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'inventory_alert_type'), 'Out of Stock', 'OUT_OF_STOCK', 'Out of stock alert', 'admin', NOW(), 'admin', NOW(), '2', '1');

-- Insert dictionary data for inventory alert status
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`)
VALUES
(NULL, 'Inventory Alert Status', 'inventory_alert_status', 'Inventory alert status', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data for inventory alert status
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`)
VALUES
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'inventory_alert_status'), 'Active', 'ACTIVE', 'Active alert', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'inventory_alert_status'), 'Resolved', 'RESOLVED', 'Resolved alert', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'inventory_alert_status'), 'Dismissed', 'DISMISSED', 'Dismissed alert', 'admin', NOW(), 'admin', NOW(), '3', '1');

-- Create trigger to automatically update inventory when transaction is added
DELIMITER $$
CREATE TRIGGER `after_inventory_transaction_insert`
AFTER INSERT ON `inventory_transactions`
FOR EACH ROW
BEGIN
  DECLARE current_quantity INT DEFAULT 0;
  DECLARE new_quantity INT DEFAULT 0;
  
  -- Get current quantity
  SELECT COALESCE(quantity, 0) INTO current_quantity
  FROM `inventory`
  WHERE `product_id` = NEW.`product_id`;
  
  -- Calculate new quantity based on transaction type
  IF NEW.`transaction_type` = 'IN' THEN
    SET new_quantity = current_quantity + NEW.`quantity`;
  ELSEIF NEW.`transaction_type` = 'OUT' THEN
    SET new_quantity = current_quantity - NEW.`quantity`;
  ELSEIF NEW.`transaction_type` = 'ADJUST' THEN
    SET new_quantity = NEW.`quantity`;
  END IF;
  
  -- Update inventory table
  INSERT INTO `inventory` (
    `id`,
    `create_by`,
    `create_time`,
    `sys_org_code`,
    `product_id`,
    `quantity`,
    `reserved_quantity`,
    `available_quantity`,
    `last_updated`,
    `updated_by`
  ) VALUES (
    UUID(),
    NEW.`create_by`,
    NOW(),
    NEW.`sys_org_code`,
    NEW.`product_id`,
    new_quantity,
    0,
    new_quantity,
    NOW(),
    NEW.`user_id`
  )
  ON DUPLICATE KEY UPDATE
    `quantity` = new_quantity,
    `available_quantity` = new_quantity,
    `last_updated` = NOW(),
    `updated_by` = NEW.`user_id`,
    `update_time` = NOW();
    
  -- Check for low stock alerts
  IF new_quantity <= (SELECT COALESCE(min_stock_threshold, 0) FROM `inventory` WHERE `product_id` = NEW.`product_id`) THEN
    INSERT INTO `inventory_alerts` (
      `id`,
      `create_by`,
      `create_time`,
      `sys_org_code`,
      `product_id`,
      `alert_type`,
      `current_quantity`,
      `threshold_value`,
      `alert_status`,
      `created_at`
    ) VALUES (
      UUID(),
      'system',
      NOW(),
      NEW.`sys_org_code`,
      NEW.`product_id`,
      IF(new_quantity = 0, 'OUT_OF_STOCK', 'LOW_STOCK'),
      new_quantity,
      (SELECT COALESCE(min_stock_threshold, 0) FROM `inventory` WHERE `product_id` = NEW.`product_id`),
      'ACTIVE',
      NOW()
    );
  END IF;
END$$
DELIMITER ;

-- Create trigger to automatically update inventory when adjustment is made
DELIMITER $$
CREATE TRIGGER `after_inventory_adjustment_insert`
AFTER INSERT ON `inventory_adjustments`
FOR EACH ROW
BEGIN
  -- Update inventory table with new quantity
  INSERT INTO `inventory` (
    `id`,
    `create_by`,
    `create_time`,
    `sys_org_code`,
    `product_id`,
    `quantity`,
    `reserved_quantity`,
    `available_quantity`,
    `last_updated`,
    `updated_by`
  ) VALUES (
    UUID(),
    NEW.`create_by`,
    NOW(),
    NEW.`sys_org_code`,
    NEW.`product_id`,
    NEW.`new_quantity`,
    0,
    NEW.`new_quantity`,
    NOW(),
    NEW.`user_id`
  )
  ON DUPLICATE KEY UPDATE
    `quantity` = NEW.`new_quantity`,
    `available_quantity` = NEW.`new_quantity`,
    `last_updated` = NOW(),
    `updated_by` = NEW.`user_id`,
    `update_time` = NOW();
    
  -- Check for low stock alerts
  IF NEW.`new_quantity` <= (SELECT COALESCE(min_stock_threshold, 0) FROM `inventory` WHERE `product_id` = NEW.`product_id`) THEN
    INSERT INTO `inventory_alerts` (
      `id`,
      `create_by`,
      `create_time`,
      `sys_org_code`,
      `product_id`,
      `alert_type`,
      `current_quantity`,
      `threshold_value`,
      `alert_status`,
      `created_at`
    ) VALUES (
      UUID(),
      'system',
      NOW(),
      NEW.`sys_org_code`,
      NEW.`product_id`,
      IF(NEW.`new_quantity` = 0, 'OUT_OF_STOCK', 'LOW_STOCK'),
      NEW.`new_quantity`,
      (SELECT COALESCE(min_stock_threshold, 0) FROM `inventory` WHERE `product_id` = NEW.`product_id`),
      'ACTIVE',
      NOW()
    );
  END IF;
END$$
DELIMITER ;

-- Insert default alert configuration
INSERT INTO `inventory_alert_configs` (
  `id`,
  `create_by`,
  `create_time`,
  `sys_org_code`,
  `product_id`,
  `alert_type`,
  `enabled`,
  `email_enabled`,
  `notification_enabled`,
  `threshold_percentage`
) VALUES
(UUID(), 'admin', NOW(), 'A01', NULL, 'LOW_STOCK', 1, 1, 1, 20),
(UUID(), 'admin', NOW(), 'A01', NULL, 'OUT_OF_STOCK', 1, 1, 1, 0);