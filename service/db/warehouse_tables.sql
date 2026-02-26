-- Create warehouse area table
CREATE TABLE `warehouse_area` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `area_code` varchar(50) DEFAULT NULL COMMENT 'Area code',
  `area_name` varchar(100) DEFAULT NULL COMMENT 'Area name',
  `description` varchar(500) DEFAULT NULL COMMENT 'Description',
  `status` int(1) DEFAULT '1' COMMENT 'Status (0: Inactive, 1: Active)',
  `capacity` int(11) DEFAULT '0' COMMENT 'Capacity (max shelf count)',
  `used_capacity` int(11) DEFAULT '0' COMMENT 'Used capacity (used shelf count)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Warehouse area table';

-- Create warehouse shelf table
CREATE TABLE `warehouse_shelf` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `area_id` varchar(36) DEFAULT NULL COMMENT 'Area ID',
  `shelf_code` varchar(50) DEFAULT NULL COMMENT 'Shelf code',
  `shelf_name` varchar(100) DEFAULT NULL COMMENT 'Shelf name',
  `description` varchar(500) DEFAULT NULL COMMENT 'Description',
  `status` int(1) DEFAULT '1' COMMENT 'Status (0: Inactive, 1: Active)',
  `capacity` int(11) DEFAULT '0' COMMENT 'Capacity (max slot count)',
  `used_capacity` int(11) DEFAULT '0' COMMENT 'Used capacity (used slot count)',
  PRIMARY KEY (`id`),
  KEY `idx_area_id` (`area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Warehouse shelf table';

-- Create warehouse slot table
CREATE TABLE `warehouse_slot` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `shelf_id` varchar(36) DEFAULT NULL COMMENT 'Shelf ID',
  `slot_code` varchar(50) DEFAULT NULL COMMENT 'Slot code',
  `slot_name` varchar(100) DEFAULT NULL COMMENT 'Slot name',
  `position` varchar(50) DEFAULT NULL COMMENT 'Position (row, column)',
  `description` varchar(500) DEFAULT NULL COMMENT 'Description',
  `status` int(1) DEFAULT '0' COMMENT 'Status (0: Empty, 1: Reserved, 2: Occupied)',
  `capacity` int(11) DEFAULT '1' COMMENT 'Capacity (max product count)',
  `used_capacity` int(11) DEFAULT '0' COMMENT 'Used capacity (placed product count)',
  `product_code` varchar(50) DEFAULT NULL COMMENT 'Placed product code',
  PRIMARY KEY (`id`),
  KEY `idx_shelf_id` (`shelf_id`),
  KEY `idx_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Warehouse slot table';

-- Insert dictionary data
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`) 
VALUES 
(NULL, 'Warehouse Status', 'warehouse_status', 'Warehouse status', '0', 'admin', NOW(), 'admin', NOW(), '0', '1'),
(NULL, 'Slot Status', 'slot_status', 'Slot status', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`) 
VALUES 
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'warehouse_status'), 'Inactive', '0', 'Inactive', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'warehouse_status'), 'Active', '1', 'Active', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'slot_status'), 'Empty', '0', 'Empty', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'slot_status'), 'Reserved', '1', 'Reserved', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'slot_status'), 'Occupied', '2', 'Occupied', 'admin', NOW(), 'admin', NOW(), '3', '1');

-- Insert sample data
INSERT INTO `warehouse_area` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `area_code`, `area_name`, `description`, `status`, `capacity`, `used_capacity`) 
VALUES 
('1', 'admin', NOW(), 'admin', NOW(), 'A01', 'AREA001', 'Raw Material Area', 'Store raw materials', 1, 10, 3),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', 'AREA002', 'Finished Product Area', 'Store finished products', 1, 15, 8),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', 'AREA003', 'Semi-finished Product Area', 'Store semi-finished products', 1, 8, 5);

INSERT INTO `warehouse_shelf` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `area_id`, `shelf_code`, `shelf_name`, `description`, `status`, `capacity`, `used_capacity`) 
VALUES 
('1', 'admin', NOW(), 'admin', NOW(), 'A01', '1', 'SHELF001', 'Area A - Shelf 1', 'Raw material area shelf 1', 1, 20, 8),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', '1', 'SHELF002', 'Area A - Shelf 2', 'Raw material area shelf 2', 1, 20, 5),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', '2', 'SHELF003', 'Area B - Shelf 1', 'Finished product area shelf 1', 1, 30, 15),
('4', 'admin', NOW(), 'admin', NOW(), 'A01', '2', 'SHELF004', 'Area B - Shelf 2', 'Finished product area shelf 2', 1, 30, 10),
('5', 'admin', NOW(), 'admin', NOW(), 'A01', '3', 'SHELF005', 'Area C - Shelf 1', 'Semi-finished product area shelf 1', 1, 25, 12);

INSERT INTO `warehouse_slot` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `shelf_id`, `slot_code`, `slot_name`, `position`, `description`, `status`, `capacity`, `used_capacity`, `product_code`) 
VALUES 
('1', 'admin', NOW(), 'admin', NOW(), 'A01', '1', 'SLOT001', 'Area A - Shelf 1 - Slot 1', 'A1-1', 'Raw material area shelf 1 slot 1', 0, 1, 0, NULL),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', '1', 'SLOT002', 'Area A - Shelf 1 - Slot 2', 'A1-2', 'Raw material area shelf 1 slot 2', 0, 1, 0, NULL),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', '1', 'SLOT003', 'Area A - Shelf 1 - Slot 3', 'A1-3', 'Raw material area shelf 1 slot 3', 2, 1, 1, 'PROD001'),
('4', 'admin', NOW(), 'admin', NOW(), 'A01', '2', 'SLOT004', 'Area B - Shelf 1 - Slot 1', 'B1-1', 'Finished product area shelf 1 slot 1', 2, 1, 1, 'PROD002'),
('5', 'admin', NOW(), 'admin', NOW(), 'A01', '3', 'SLOT005', 'Area C - Shelf 1 - Slot 1', 'C1-1', 'Semi-finished product area shelf 1 slot 1', 1, 1, 1, 'PROD003');

-- Create inventory table
CREATE TABLE `inventory` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `product_id` varchar(36) NOT NULL COMMENT 'Product ID',
  `quantity` int(11) DEFAULT '0' COMMENT 'Total quantity',
  `reserved_quantity` int(11) DEFAULT '0' COMMENT 'Reserved quantity',
  `available_quantity` int(11) DEFAULT '0' COMMENT 'Available quantity',
  `last_updated` datetime DEFAULT NULL COMMENT 'Last updated timestamp',
  `updated_by` varchar(50) DEFAULT NULL COMMENT 'Last updated by',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_inventory_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory table';

-- Create inventory transactions table
CREATE TABLE `inventory_transactions` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `product_id` varchar(36) NOT NULL COMMENT 'Product ID',
  `transaction_type` varchar(20) NOT NULL COMMENT 'Transaction type (IN, OUT, ADJUST)',
  `quantity` int(11) NOT NULL COMMENT 'Transaction quantity',
  `reference_id` varchar(36) DEFAULT NULL COMMENT 'Reference ID (stock transaction, order, etc.)',
  `reason` varchar(500) DEFAULT NULL COMMENT 'Transaction reason',
  `user_id` varchar(50) DEFAULT NULL COMMENT 'User who performed the transaction',
  `created_at` datetime DEFAULT NULL COMMENT 'Transaction timestamp',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_transaction_type` (`transaction_type`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_inventory_transactions_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory transactions table';

-- Create inventory adjustments table
CREATE TABLE `inventory_adjustments` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `product_id` varchar(36) NOT NULL COMMENT 'Product ID',
  `old_quantity` int(11) DEFAULT '0' COMMENT 'Old quantity before adjustment',
  `new_quantity` int(11) DEFAULT '0' COMMENT 'New quantity after adjustment',
  `adjustment_reason` varchar(500) DEFAULT NULL COMMENT 'Reason for adjustment',
  `user_id` varchar(50) DEFAULT NULL COMMENT 'User who performed the adjustment',
  `created_at` datetime DEFAULT NULL COMMENT 'Adjustment timestamp',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_inventory_adjustments_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Inventory adjustments table';

-- Insert dictionary data for inventory
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`)
VALUES
(NULL, 'Inventory Transaction Type', 'inventory_transaction_type', 'Inventory transaction type', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data for inventory
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`)
VALUES
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'inventory_transaction_type'), 'Stock In', 'IN', 'Stock in transaction', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'inventory_transaction_type'), 'Stock Out', 'OUT', 'Stock out transaction', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'inventory_transaction_type'), 'Adjustment', 'ADJUST', 'Inventory adjustment', 'admin', NOW(), 'admin', NOW(), '3', '1');

-- Create stock transactions table
CREATE TABLE `stock_transactions` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `transaction_code` varchar(50) NOT NULL COMMENT 'Transaction code',
  `transaction_type` varchar(20) NOT NULL COMMENT 'Transaction type (IN, OUT, TRANSFER)',
  `transaction_date` datetime DEFAULT NULL COMMENT 'Transaction date',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'Status (PENDING, APPROVED, CANCELLED)',
  `created_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `approved_by` varchar(50) DEFAULT NULL COMMENT 'Approved by',
  `approved_at` datetime DEFAULT NULL COMMENT 'Approved at',
  `notes` varchar(500) DEFAULT NULL COMMENT 'Notes',
  PRIMARY KEY (`id`),
  KEY `idx_transaction_code` (`transaction_code`),
  KEY `idx_transaction_date` (`transaction_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Stock transactions table';

-- Create stock transaction items table
CREATE TABLE `stock_transaction_items` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `transaction_id` varchar(36) NOT NULL COMMENT 'Transaction ID',
  `product_id` varchar(36) NOT NULL COMMENT 'Product ID',
  `quantity` int(11) NOT NULL COMMENT 'Quantity',
  `unit_price` decimal(10,2) DEFAULT '0.00' COMMENT 'Unit price',
  `total_price` decimal(10,2) DEFAULT '0.00' COMMENT 'Total price',
  `from_location_id` varchar(36) DEFAULT NULL COMMENT 'From location ID',
  `to_location_id` varchar(36) DEFAULT NULL COMMENT 'To location ID',
  `batch_number` varchar(50) DEFAULT NULL COMMENT 'Batch number',
  `expiry_date` datetime DEFAULT NULL COMMENT 'Expiry date',
  PRIMARY KEY (`id`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_stock_transaction_items_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `stock_transactions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_stock_transaction_items_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_stock_transaction_items_from_location` FOREIGN KEY (`from_location_id`) REFERENCES `warehouse_slot` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_stock_transaction_items_to_location` FOREIGN KEY (`to_location_id`) REFERENCES `warehouse_slot` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Stock transaction items table';

-- Create suppliers table
CREATE TABLE `suppliers` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `supplier_code` varchar(50) NOT NULL COMMENT 'Supplier code',
  `supplier_name` varchar(100) NOT NULL COMMENT 'Supplier name',
  `contact_person` varchar(50) DEFAULT NULL COMMENT 'Contact person',
  `phone` varchar(20) DEFAULT NULL COMMENT 'Phone',
  `email` varchar(100) DEFAULT NULL COMMENT 'Email',
  `address` varchar(500) DEFAULT NULL COMMENT 'Address',
  `status` int(1) DEFAULT '1' COMMENT 'Status (0: Inactive, 1: Active)',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Suppliers table';

-- Insert dictionary data for stock transactions
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`)
VALUES
(NULL, 'Stock Transaction Type', 'stock_transaction_type', 'Stock transaction type', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data for stock transactions
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`)
VALUES
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'stock_transaction_type'), 'Stock In', 'IN', 'Stock in transaction', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'stock_transaction_type'), 'Stock Out', 'OUT', 'Stock out transaction', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'stock_transaction_type'), 'Transfer', 'TRANSFER', 'Stock transfer transaction', 'admin', NOW(), 'admin', NOW(), '3', '1');

-- Insert dictionary data for stock transaction status
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`)
VALUES
(NULL, 'Stock Transaction Status', 'stock_transaction_status', 'Stock transaction status', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data for stock transaction status
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`)
VALUES
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'stock_transaction_status'), 'Pending', 'PENDING', 'Pending approval', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'stock_transaction_status'), 'Approved', 'APPROVED', 'Approved transaction', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'stock_transaction_status'), 'Cancelled', 'CANCELLED', 'Cancelled transaction', 'admin', NOW(), 'admin', NOW(), '3', '1');

-- Insert sample suppliers
INSERT INTO `suppliers` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `supplier_code`, `supplier_name`, `contact_person`, `phone`, `email`, `address`, `status`)
VALUES
('1', 'admin', NOW(), 'admin', NOW(), 'A01', 'SUP001', 'Supplier A', 'John Doe', '1234567890', 'john@supplier-a.com', '123 Supplier St, City, Country', 1),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', 'SUP002', 'Supplier B', 'Jane Smith', '0987654321', 'jane@supplier-b.com', '456 Supplier Ave, Town, Country', 1),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', 'SUP003', 'Supplier C', 'Bob Johnson', '0123456789', 'bob@supplier-c.com', '789 Supplier Rd, Village, Country', 1);

-- Create customers table
CREATE TABLE `customers` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `customer_code` varchar(50) NOT NULL COMMENT 'Customer code',
  `customer_name` varchar(100) NOT NULL COMMENT 'Customer name',
  `contact_person` varchar(50) DEFAULT NULL COMMENT 'Contact person',
  `phone` varchar(20) DEFAULT NULL COMMENT 'Phone',
  `email` varchar(100) DEFAULT NULL COMMENT 'Email',
  `address` varchar(500) DEFAULT NULL COMMENT 'Address',
  `tax_code` varchar(50) DEFAULT NULL COMMENT 'Tax code',
  `status` int(1) DEFAULT '1' COMMENT 'Status (0: Inactive, 1: Active)',
  PRIMARY KEY (`id`),
  KEY `idx_customer_code` (`customer_code`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Customers table';

-- Create customer_balances table
CREATE TABLE `customer_balances` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `customer_id` varchar(36) NOT NULL COMMENT 'Customer ID',
  `balance` decimal(15,2) DEFAULT '0.00' COMMENT 'Balance (positive: credit, negative: debt)',
  `last_updated` datetime DEFAULT NULL COMMENT 'Last updated timestamp',
  `updated_by` varchar(50) DEFAULT NULL COMMENT 'Last updated by',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  CONSTRAINT `fk_customer_balances_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Customer balances table';

-- Create trigger to automatically create customer balance record
DELIMITER $$
CREATE TRIGGER `after_customer_insert`
AFTER INSERT ON `customers`
FOR EACH ROW
BEGIN
  INSERT INTO `customer_balances` (
    `id`,
    `create_by`,
    `create_time`,
    `sys_org_code`,
    `customer_id`,
    `balance`,
    `last_updated`,
    `updated_by`
  ) VALUES (
    UUID(),
    NEW.`create_by`,
    NOW(),
    NEW.`sys_org_code`,
    NEW.`id`,
    0.00,
    NOW(),
    NEW.`create_by`
  );
END$$
DELIMITER ;

-- Insert dictionary data for customer status
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`)
VALUES
(NULL, 'Customer Status', 'customer_status', 'Customer status', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data for customer status
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`)
VALUES
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'customer_status'), 'Inactive', '0', 'Inactive customer', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'customer_status'), 'Active', '1', 'Active customer', 'admin', NOW(), 'admin', NOW(), '2', '1');

-- Insert sample customers
INSERT INTO `customers` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `customer_code`, `customer_name`, `contact_person`, `phone`, `email`, `address`, `tax_code`, `status`)
VALUES
('1', 'admin', NOW(), 'admin', NOW(), 'A01', 'CUST001', 'Customer A', 'Alice Brown', '1234567890', 'alice@customer-a.com', '123 Customer St, City, Country', 'TAX001', 1),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', 'CUST002', 'Customer B', 'Bob Green', '0987654321', 'bob@customer-b.com', '456 Customer Ave, Town, Country', 'TAX002', 1),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', 'CUST003', 'Customer C', 'Charlie White', '0123456789', 'charlie@customer-c.com', '789 Customer Rd, Village, Country', 'TAX003', 1);

-- Insert sample customer balances
INSERT INTO `customer_balances` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `sys_org_code`, `customer_id`, `balance`, `last_updated`, `updated_by`)
VALUES
('1', 'admin', NOW(), 'admin', NOW(), 'A01', '1', 1500.00, NOW(), 'admin'),
('2', 'admin', NOW(), 'admin', NOW(), 'A01', '2', -500.00, NOW(), 'admin'),
('3', 'admin', NOW(), 'admin', NOW(), 'A01', '3', 750.00, NOW(), 'admin');