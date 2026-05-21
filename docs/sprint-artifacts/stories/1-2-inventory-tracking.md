# Story 1.2: Theo dõi tồn kho

## Story Details

**Epic:** Epic 1: Quản lý sản phẩm và tồn kho
**Story Key:** 1-2-inventory-tracking
**Status:** Review
**Priority:** High
**Effort:** 4 days

## User Story

Là một người quản lý kho, tôi muốn theo dõi số lượng tồn kho của các sản phẩm theo thời gian thực để có thể quản lý hiệu quả và nhận cảnh báo khi hàng tồn kho thấp.

## Acceptance Criteria

- [ ] Hệ thống hiển thị số lượng tồn kho hiện tại cho từng sản phẩm
- [ ] Hệ thống tự động cập nhật số lượng tồn kho sau mỗi giao dịch nhập/xuất
- [ ] Người dùng có thể xem lịch sử thay đổi tồn kho với timestamps
- [ ] Người dùng có thể thiết lập ngưỡng tồn kho tối thiểu cho từng sản phẩm
- [ ] Hệ thống gửi cảnh báo khi sản phẩm đạt ngưỡng tồn kho tối thiểu
- [ ] Hệ thống hiển thị giá trị tồn kho (số lượng × đơn giá)
- [ ] Người dùng có thể thực hiện điều chỉnh tồn kho thủ công với lý do
- [ ] Hệ thống hiển thị báo cáo tồn kho tổng hợp
- [ ] Hệ thống hỗ trợ xuất báo cáo tồn kho ra file PDF/Excel
- [ ] Hệ thống hiển thị biểu đồ xu hướng tồn kho theo thời gian

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu tồn kho
- [ ] Tạo bảng inventory với các trường: id, productId, quantity, reservedQuantity, availableQuantity, lastUpdated, updatedBy
- [ ] Tạo bảng inventory_transactions với các trường: id, productId, transactionType, quantity, referenceId, reason, userId, createdAt
- [ ] Tạo bảng inventory_adjustments với các trường: id, productId, oldQuantity, newQuantity, adjustmentReason, userId, createdAt
- [ ] Thiết kế quan hệ khóa ngoại giữa inventory và products
- [ ] Thiết kế quan hệ khóa ngoại giữa inventory_transactions và products
- [ ] Tạo index cho trường productId trong các bảng tồn kho để tối ưu truy vấn
- [ ] Tạo trigger để tự động cập nhật bảng inventory khi có giao dịch mới

### Task 2: Xây dựng API quản lý tồn kho
- [ ] API lấy thông tin tồn kho của sản phẩm (GET /api/inventory/:productId)
- [ ] API lấy danh sách tồn kho tất cả sản phẩm (GET /api/inventory)
- [ ] API cập nhật tồn kho (PUT /api/inventory/:productId)
- [ ] API điều chỉnh tồn kho thủ công (POST /api/inventory/adjust)
- [ ] API lấy lịch sử giao dịch tồn kho (GET /api/inventory/transactions)
- [ ] API lấy lịch sử điều chỉnh tồn kho (GET /api/inventory/adjustments)
- [ ] API lấy báo cáo tồn kho tổng hợp (GET /api/inventory/report)
- [ ] API xuất báo cáo tồn kho (GET /api/inventory/export)
- [ ] API lấy sản phẩm có tồn kho thấp (GET /api/inventory/low-stock)
- [ ] API cập nhật ngưỡng tồn kho tối thiểu (PUT /api/inventory/:productId/min-stock)

### Task 3: Xây dựng giao diện quản lý tồn kho
- [ ] Trang dashboard tồn kho với thông tin tổng quan
- [ ] Bảng hiển thị danh sách tồn kho với thông tin chi tiết
- [ ] Modal điều chỉnh tồn kho thủ công
- [ ] Component hiển thị lịch sử giao dịch tồn kho
- [ ] Component hiển thị lịch sử điều chỉnh tồn kho
- [ ] Component báo cáo tồn kho với biểu đồ
- [ ] Component xuất báo cáo tồn kho
- [ ] Component cảnh báo tồn kho thấp
- [ ] Component tìm kiếm và lọc tồn kho

### Task 4: Xây dựng hệ thống cảnh báo
- [ ] Service kiểm tra tồn kho thấp theo lịch
- [ ] Service gửi cảnh báo qua email
- [ ] Service gửi cảnh báo qua notification trong hệ thống
- [ ] Component quản lý cấu hình cảnh báo
- [ ] Component lịch sử cảnh báo
- [ ] API quản lý cấu hình cảnh báo

### Task 5: Tích hợp với các module khác
- [ ] Tích hợp với module quản lý sản phẩm để hiển thị thông tin sản phẩm
- [ ] Tích hợp với module quản lý nhập/xuất kho để tự động cập nhật tồn kho
- [ ] Tích hợp với module quản lý đơn hàng để trừ tồn kho khi xác nhận đơn hàng
- [ ] Tích hợp với module báo cáo để cung cấp dữ liệu tồn kho

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng Quartz Scheduler cho việc kiểm tra tồn kho thấp định kỳ
- Sử dụng Spring Mail cho việc gửi email cảnh báo
- Sử dụng WebSocket cho real-time notifications
- Sử dụng Chart.js cho biểu đồ xu hướng tồn kho
- Sử dụng Apache POI cho xuất Excel
- Sử dụng iText cho xuất PDF

## Dependencies

- Cần có module quản lý sản phẩm đã được triển khai
- Cần có hệ thống authentication và authorization được thiết lập
- Cần có module quản lý email được triển khai
- Cần có module quản lý notification được triển khai

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-20
- Story được chọn: 1-2-inventory-tracking
- Trạng thái hiện tại: Ready for Development

### Completion Notes
- Bắt đầu với Task 1: Thiết kế cơ sở dữ liệu tồn kho
- Ưu tiên xây dựng API trước để có dữ liệu cho frontend
- Tập trung vào performance khi xử lý lượng lớn giao dịch tồn kho
- Đảm bảo tính chính xác của dữ liệu tồn kho
- Triển khai hệ thống cảnh báo hiệu quả