-- =====================================================
-- ALTER TABLE: thêm các cột kích thước & ảnh vào bảng material
-- Chạy file này nếu bảng material đã tồn tại (tạo từ material_tables.sql cũ)
-- Created: 2026-03-05
-- =====================================================

ALTER TABLE `material`
  ADD COLUMN IF NOT EXISTS `weight` DECIMAL(10,3) DEFAULT NULL COMMENT 'Cân nặng (kg)' AFTER `image`,
  ADD COLUMN IF NOT EXISTS `length` DECIMAL(10,2) DEFAULT NULL COMMENT 'Chiều dài (mm)' AFTER `weight`,
  ADD COLUMN IF NOT EXISTS `width`  DECIMAL(10,2) DEFAULT NULL COMMENT 'Chiều rộng (mm)' AFTER `length`,
  ADD COLUMN IF NOT EXISTS `height` DECIMAL(10,2) DEFAULT NULL COMMENT 'Chiều cao (mm)' AFTER `width`;
