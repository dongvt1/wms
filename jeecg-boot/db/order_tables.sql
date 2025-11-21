-- Create orders table
CREATE TABLE `orders` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `order_code` varchar(50) NOT NULL COMMENT 'Order code',
  `customer_id` varchar(36) NOT NULL COMMENT 'Customer ID',
  `order_date` datetime DEFAULT NULL COMMENT 'Order date',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'Order status (PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED)',
  `total_amount` decimal(15,2) DEFAULT '0.00' COMMENT 'Total amount',
  `discount_amount` decimal(15,2) DEFAULT '0.00' COMMENT 'Discount amount',
  `tax_amount` decimal(15,2) DEFAULT '0.00' COMMENT 'Tax amount',
  `final_amount` decimal(15,2) DEFAULT '0.00' COMMENT 'Final amount',
  `notes` varchar(500) DEFAULT NULL COMMENT 'Notes',
  `created_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_code` (`order_code`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_order_date` (`order_date`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_orders_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Orders table';

-- Create order_items table
CREATE TABLE `order_items` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `order_id` varchar(36) NOT NULL COMMENT 'Order ID',
  `product_id` varchar(36) NOT NULL COMMENT 'Product ID',
  `quantity` int(11) NOT NULL COMMENT 'Quantity',
  `unit_price` decimal(10,2) DEFAULT '0.00' COMMENT 'Unit price',
  `total_price` decimal(15,2) DEFAULT '0.00' COMMENT 'Total price',
  `discount_amount` decimal(15,2) DEFAULT '0.00' COMMENT 'Discount amount',
  `final_amount` decimal(15,2) DEFAULT '0.00' COMMENT 'Final amount',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order items table';

-- Create order_status_history table
CREATE TABLE `order_status_history` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `order_id` varchar(36) NOT NULL COMMENT 'Order ID',
  `from_status` varchar(20) DEFAULT NULL COMMENT 'From status',
  `to_status` varchar(20) NOT NULL COMMENT 'To status',
  `reason` varchar(500) DEFAULT NULL COMMENT 'Reason for status change',
  `user_id` varchar(50) DEFAULT NULL COMMENT 'User who changed the status',
  `created_at` datetime DEFAULT NULL COMMENT 'Status change timestamp',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_order_status_history_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order status history table';

-- Insert dictionary data for order status
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`)
VALUES
(NULL, 'Order Status', 'order_status', 'Order status', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data for order status
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`)
VALUES
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'order_status'), 'Pending', 'PENDING', 'Pending confirmation', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'order_status'), 'Confirmed', 'CONFIRMED', 'Order confirmed', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'order_status'), 'Shipping', 'SHIPPING', 'Order is being shipped', 'admin', NOW(), 'admin', NOW(), '3', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'order_status'), 'Completed', 'COMPLETED', 'Order completed', 'admin', NOW(), 'admin', NOW(), '4', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'order_status'), 'Cancelled', 'CANCELLED', 'Order cancelled', 'admin', NOW(), 'admin', NOW(), '5', '1');

-- Insert sample orders
INSERT INTO `orders` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `order_code`, `customer_id`, `order_date`, `status`, `total_amount`, `discount_amount`, `tax_amount`, `final_amount`, `notes`, `created_by`)
VALUES
('1', 'admin', NOW(), 'admin', NOW(), 'A01', 'ORD001', '1', NOW(), 'CONFIRMED', 819.98, 0.00, 81.99, 901.97, 'Urgent delivery', 'admin'),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', 'ORD002', '2', NOW(), 'SHIPPING', 1319.98, 50.00, 126.99, 1396.97, 'Standard delivery', 'admin'),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', 'ORD003', '3', NOW(), 'PENDING', 69.98, 0.00, 6.99, 76.97, 'Gift wrap required', 'admin');

-- Insert sample order items
INSERT INTO `order_items` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `order_id`, `product_id`, `quantity`, `unit_price`, `total_price`, `discount_amount`, `final_amount`)
VALUES
('1', 'admin', NOW(), 'admin', NOW(), 'A01', '1', '1', 1, 799.99, 799.99, 0.00, 799.99),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', '1', '2', 1, 19.99, 19.99, 0.00, 19.99),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', '2', '3', 1, 1299.99, 1299.99, 50.00, 1249.99),
('4', 'admin', NOW(), 'admin', NOW(), 'A01', '2', '4', 1, 19.99, 19.99, 0.00, 19.99),
('5', 'admin', NOW(), 'admin', NOW(), 'A01', '3', '5', 1, 49.99, 49.99, 0.00, 49.99),
('6', 'admin', NOW(), 'admin', NOW(), 'A01', '3', '4', 1, 19.99, 19.99, 0.00, 19.99);

-- Insert sample order status history
INSERT INTO `order_status_history` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `order_id`, `from_status`, `to_status`, `reason`, `user_id`, `created_at`)
VALUES
('1', 'admin', NOW(), 'admin', NOW(), 'A01', '1', NULL, 'PENDING', 'Order created', 'admin', NOW()),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', '1', 'PENDING', 'CONFIRMED', 'Payment received', 'admin', NOW() + INTERVAL 1 HOUR),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', '2', NULL, 'PENDING', 'Order created', 'admin', NOW()),
('4', 'admin', NOW(), 'admin', NOW(), 'A01', '2', 'PENDING', 'CONFIRMED', 'Payment received', 'admin', NOW() + INTERVAL 1 HOUR),
('5', 'admin', NOW(), 'admin', NOW(), 'A01', '2', 'CONFIRMED', 'SHIPPING', 'Order shipped', 'admin', NOW() + INTERVAL 2 HOUR),
('6', 'admin', NOW(), 'admin', NOW(), 'A01', '3', NULL, 'PENDING', 'Order created', 'admin', NOW());