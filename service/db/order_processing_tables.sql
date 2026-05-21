-- Create order_notifications table
CREATE TABLE `order_notifications` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `order_id` varchar(36) NOT NULL COMMENT 'Order ID',
  `type` varchar(50) NOT NULL COMMENT 'Notification type (EMAIL, SMS, SYSTEM)',
  `recipient` varchar(255) NOT NULL COMMENT 'Notification recipient (email, phone, user_id)',
  `subject` varchar(255) DEFAULT NULL COMMENT 'Notification subject',
  `content` text COMMENT 'Notification content',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'Notification status (PENDING, SENT, FAILED)',
  `sent_at` datetime DEFAULT NULL COMMENT 'Timestamp when notification was sent',
  `error_message` text COMMENT 'Error message if notification failed',
  `retry_count` int(11) DEFAULT '0' COMMENT 'Number of retry attempts',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`),
  KEY `idx_sent_at` (`sent_at`),
  CONSTRAINT `fk_order_notifications_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order notifications table';

-- Create order_processing_logs table
CREATE TABLE `order_processing_logs` (
  `id` varchar(36) NOT NULL COMMENT 'Primary key',
  `create_by` varchar(50) DEFAULT NULL COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Create date',
  `update_by` varchar(50) DEFAULT NULL COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update date',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT 'Department',
  `order_id` varchar(36) NOT NULL COMMENT 'Order ID',
  `action` varchar(100) NOT NULL COMMENT 'Processing action (CONFIRM, CANCEL, SHIP, COMPLETE, etc.)',
  `details` text COMMENT 'Processing details',
  `status` varchar(20) DEFAULT 'SUCCESS' COMMENT 'Processing status (SUCCESS, FAILED, PENDING)',
  `error_message` text COMMENT 'Error message if processing failed',
  `user_id` varchar(50) DEFAULT NULL COMMENT 'User who performed the action',
  `processing_time` int(11) DEFAULT NULL COMMENT 'Processing time in milliseconds',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_action` (`action`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_order_processing_logs_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order processing logs table';

-- Insert dictionary data for notification types
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`)
VALUES
(NULL, 'Notification Type', 'notification_type', 'Notification type', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data for notification types
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`)
VALUES
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'notification_type'), 'Email', 'EMAIL', 'Email notification', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'notification_type'), 'SMS', 'SMS', 'SMS notification', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'notification_type'), 'System', 'SYSTEM', 'System notification', 'admin', NOW(), 'admin', NOW(), '3', '1');

-- Insert dictionary data for notification status
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`)
VALUES
(NULL, 'Notification Status', 'notification_status', 'Notification status', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data for notification status
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`)
VALUES
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'notification_status'), 'Pending', 'PENDING', 'Pending to send', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'notification_status'), 'Sent', 'SENT', 'Successfully sent', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'notification_status'), 'Failed', 'FAILED', 'Failed to send', 'admin', NOW(), 'admin', NOW(), '3', '1');

-- Insert dictionary data for processing log status
INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `type`, `sort_order`)
VALUES
(NULL, 'Processing Log Status', 'processing_log_status', 'Processing log status', '0', 'admin', NOW(), 'admin', NOW(), '0', '1');

-- Insert dictionary item data for processing log status
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `sort_order`, `status`)
VALUES
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'processing_log_status'), 'Success', 'SUCCESS', 'Processing successful', 'admin', NOW(), 'admin', NOW(), '1', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'processing_log_status'), 'Failed', 'FAILED', 'Processing failed', 'admin', NOW(), 'admin', NOW(), '2', '1'),
(NULL, (SELECT id FROM sys_dict WHERE dict_code = 'processing_log_status'), 'Pending', 'PENDING', 'Processing pending', 'admin', NOW(), 'admin', NOW(), '3', '1');