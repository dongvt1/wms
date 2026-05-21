-- Create product category table
CREATE TABLE `product_category` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `name` varchar(100) DEFAULT NULL COMMENT 'Category name',
  `description` varchar(500) DEFAULT NULL COMMENT 'Description',
  `parent_id` varchar(36) DEFAULT NULL COMMENT 'Parent category ID',
  `status` int(1) DEFAULT '1' COMMENT 'Status (0: Inactive, 1: Active)',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product category table';

-- Create product table
CREATE TABLE `product` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `code` varchar(50) DEFAULT NULL COMMENT 'Product code',
  `name` varchar(100) DEFAULT NULL COMMENT 'Product name',
  `description` text DEFAULT NULL COMMENT 'Description',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT 'Price',
  `category_id` varchar(36) DEFAULT NULL COMMENT 'Category ID',
  `min_stock_level` int(11) DEFAULT '0' COMMENT 'Minimum stock level',
  `image` varchar(500) DEFAULT NULL COMMENT 'Product image URL',
  `status` int(1) DEFAULT '1' COMMENT 'Status (0: Inactive, 1: Active)',
  `current_stock` int(11) DEFAULT '0' COMMENT 'Current stock quantity',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_category_id` (`category_id`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product table';

-- Create product history table
CREATE TABLE `product_history` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `product_id` varchar(36) DEFAULT NULL COMMENT 'Product ID',
  `action` varchar(50) DEFAULT NULL COMMENT 'Action (CREATE, UPDATE, DELETE)',
  `old_data` text DEFAULT NULL COMMENT 'Old data (JSON format)',
  `new_data` text DEFAULT NULL COMMENT 'New data (JSON format)',
  `user_id` varchar(36) DEFAULT NULL COMMENT 'User ID who performed the action',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_history_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product history table';

-- Insert dictionary data
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`) 
VALUES 
(NULL, 'Product Status', 'product_status', 'Product status', '0', 'admin', NOW(), 'admin', NOW(), '0', '1'),
(NULL, 'Product History Action', 'product_history_action', 'Product history action', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`) 
VALUES 
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'product_status'), 'Inactive', '0', 'Inactive', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'product_status'), 'Active', '1', 'Active', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'product_history_action'), 'Create', 'CREATE', 'Create product', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'product_history_action'), 'Update', 'UPDATE', 'Update product', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'product_history_action'), 'Delete', 'DELETE', 'Delete product', 'admin', NOW(), 'admin', NOW(), '3', '1');

-- Insert sample category data
INSERT INTO `product_category` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `name`, `description`, `parent_id`, `status`) 
VALUES 
('1', 'admin', NOW(), 'admin', NOW(), 'A01', 'Electronics', 'Electronic devices and components', NULL, 1),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', 'Clothing', 'Clothing and accessories', NULL, 1),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', 'Food', 'Food and beverages', NULL, 1),
('4', 'admin', NOW(), 'admin', NOW(), 'A01', 'Mobile Phones', 'Smartphones and accessories', '1', 1),
('5', 'admin', NOW(), 'admin', NOW(), 'A01', 'Laptops', 'Laptop computers', '1', 1),
('6', 'admin', NOW(), 'admin', NOW(), 'A01', 'Men\'s Clothing', 'Clothing for men', '2', 1),
('7', 'admin', NOW(), 'admin', NOW(), 'A01', 'Women\'s Clothing', 'Clothing for women', '2', 1);

-- Insert sample product data
INSERT INTO `product` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `code`, `name`, `description`, `price`, `category_id`, `min_stock_level`, `image`, `status`, `current_stock`) 
VALUES 
('1', 'admin', NOW(), 'admin', NOW(), 'A01', 'PROD001', 'iPhone 13', 'Apple iPhone 13 128GB', 799.99, '4', 10, 'https://example.com/images/iphone13.jpg', 1, 25),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', 'PROD002', 'Samsung Galaxy S21', 'Samsung Galaxy S21 128GB', 699.99, '4', 15, 'https://example.com/images/galaxys21.jpg', 1, 30),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', 'PROD003', 'MacBook Pro', 'Apple MacBook Pro 13-inch', 1299.99, '5', 5, 'https://example.com/images/macbookpro.jpg', 1, 15),
('4', 'admin', NOW(), 'admin', NOW(), 'A01', 'PROD004', 'Men\'s T-Shirt', 'Cotton men\'s t-shirt', 19.99, '6', 50, 'https://example.com/images/men-tshirt.jpg', 1, 100),
('5', 'admin', NOW(), 'admin', NOW(), 'A01', 'PROD005', 'Women\'s Dress', 'Elegant women\'s dress', 49.99, '7', 25, 'https://example.com/images/women-dress.jpg', 1, 75);

-- Insert sample product history data
INSERT INTO `product_history` (`id`, `create_by`, `create_time`, `sys_org_code`, `product_id`, `action`, `old_data`, `new_data`, `user_id`) 
VALUES 
('1', 'admin', NOW(), 'A01', '1', 'CREATE', NULL, '{"code":"PROD001","name":"iPhone 13","price":799.99}', 'admin'),
('2', 'admin', NOW(), 'A01', '2', 'CREATE', NULL, '{"code":"PROD002","name":"Samsung Galaxy S21","price":699.99}', 'admin'),
('3', 'admin', NOW(), 'A01', '1', 'UPDATE', '{"price":799.99}', '{"price":849.99}', 'admin');