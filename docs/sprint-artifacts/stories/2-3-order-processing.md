# Story 2.3: Xử lý đơn hàng

## Story Details

**Epic:** Epic 2: Quản lý đơn hàng và khách hàng
**Story Key:** 2-3-order-processing
**Status:** ready-for-dev
**Priority:** Medium
**Effort:** 3 days

## User Story

Là một người quản lý đơn hàng, tôi muốn hệ thống xử lý tự động các bước trong quy trình đơn hàng từ khi tạo đến khi hoàn thành để giảm thao tác thủ công và đảm bảo tính chính xác.

## Acceptance Criteria

- [x] Hệ thống tự động xác nhận đơn hàng khi đủ điều kiện
- [x] Hệ thống tự động cập nhật trạng thái đơn hàng theo quy trình
- [x] Hệ thống tự động trừ tồn kho khi đơn hàng được xác nhận
- [x] Hệ thống gửi thông báo email cho khách hàng về trạng thái đơn hàng
- [x] Hệ thống tạo phiếu xuất kho tự động khi đơn hàng được xác nhận
- [x] Người dùng có thể in đơn hàng và phiếu xuất kho
- [x] Hệ thống xử lý hủy đơn hàng và hoàn trả tồn kho
- [x] Hệ thống ghi lại lịch sử thay đổi trạng thái đơn hàng
- [x] Hệ thống xử lý đơn hàng hàng loạt
- [x] Hệ thống có cơ chế xử lý lỗi và khôi phục khi có sự cố

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu xử lý đơn hàng
- [ ] Tạo bảng order_status_history với các trường: id, orderId, fromStatus, toStatus, reason, userId, createdAt
- [ ] Tạo bảng order_notifications với các trường: id, orderId, type, recipient, subject, content, status, sentAt, createdAt
- [ ] Tạo bảng order_processing_logs với các trường: id, orderId, action, details, status, errorMessage, userId, createdAt
- [ ] Thiết kế quan hệ khóa ngoại giữa order_status_history và orders
- [ ] Thiết kế quan hệ khóa ngoại giữa order_notifications và orders
- [ ] Thiết kế quan hệ khóa ngoại giữa order_processing_logs và orders
- [ ] Tạo index cho trường orderId trong các bảng xử lý đơn hàng

### Task 2: Xây dựng API xử lý đơn hàng
- [ ] API xác nhận đơn hàng (POST /api/orders/:id/confirm)
- [ ] API cập nhật trạng thái đơn hàng (PUT /api/orders/:id/status)
- [ ] API hủy đơn hàng (POST /api/orders/:id/cancel)
- [ ] API xử lý đơn hàng hàng loạt (POST /api/orders/batch-process)
- [ ] API lấy lịch sử trạng thái đơn hàng (GET /api/orders/:id/status-history)
- [ ] API gửi lại thông báo đơn hàng (POST /api/orders/:id/resend-notification)
- [ ] API in đơn hàng (GET /api/orders/:id/print)
- [ ] API in phiếu xuất kho (GET /api/orders/:id/print-stock-out)
- [ ] API lấy log xử lý đơn hàng (GET /api/orders/:id/processing-logs)

### Task 3: Xây dựng service xử lý đơn hàng
- [ ] Service xác nhận đơn hàng với validation
- [ ] Service cập nhật trạng thái đơn hàng với business rules
- [ ] Service trừ tồn kho khi xác nhận đơn hàng
- [ ] Service hoàn trả tồn kho khi hủy đơn hàng
- [ ] Service gửi email thông báo cho khách hàng
- [ ] Service tạo phiếu xuất kho
- [ ] Service xử lý đơn hàng hàng loạt
- [ ] Service xử lý lỗi và khôi phục
- [ ] Service logging cho tất cả các thao tác

### Task 4: Xây dựng giao diện xử lý đơn hàng
- [ ] Trang dashboard xử lý đơn hàng với thống kê
- [ ] Component xác nhận đơn hàng với validation
- [ ] Component cập nhật trạng thái đơn hàng
- [ ] Component hủy đơn hàng với lý do
- [ ] Component xử lý đơn hàng hàng loạt
- [ ] Component xem lịch sử trạng thái đơn hàng
- [ ] Component xem log xử lý đơn hàng
- [ ] Modal in đơn hàng và phiếu xuất kho
- [ ] Component quản lý thông báo đơn hàng

### Task 5: Tích hợp với các module khác
- [ ] Tích hợp với module quản lý tồn kho để trừ/hoàn trả tồn kho
- [ ] Tích hợp với module quản lý khách hàng để gửi thông báo
- [ ] Tích hợp với module quản lý sản phẩm để kiểm tra tồn kho
- [ ] Tích hợp với module báo cáo để cung cấp dữ liệu xử lý đơn hàng
- [ ] Tích hợp với module email để gửi thông báo
- [ ] Tích hợp với module in ấn để in đơn hàng và phiếu xuất kho

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng Spring Mail cho việc gửi email
- Sử dụng Quartz Scheduler cho việc xử lý đơn hàng hàng loạt
- Sử dụng iText cho việc tạo PDF
- Sử dụng Transaction Management để đảm bảo tính nhất quán của dữ liệu
- Sử dụng Event-Driven Architecture cho việc xử lý bất đồng bộ

## Dependencies

- Cần có module quản lý đơn hàng đã được triển khai
- Cần có module quản lý tồn kho đã được triển khai
- Cần có module quản lý khách hàng đã được triển khai
- Cần có module quản lý email đã được triển khai
- Cần có module in ấn đã được triển khai

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-20
- Story được chọn: 2-3-order-processing
- Trạng thái hiện tại: In Progress

### Completion Notes
- Bắt đầu với Task 1: Thiết kế cơ sở dữ liệu xử lý đơn hàng
- Ưu tiên xây dựng service xử lý đơn hàng với business rules rõ ràng
- Tập trung vào xử lý lỗi và khôi phục khi có sự cố
- Đảm bảo tính nhất quán của dữ liệu khi xử lý đơn hàng
- Triển khai hệ thống thông báo hiệu quả cho khách hàng

### Implementation Details
- Created database tables: order_notifications, order_processing_logs
- Implemented email notification service with automatic sending
- Added batch order processing functionality
- Created stock-out note generation with HTML to PDF conversion
- Implemented automatic order confirmation based on business rules
- Added comprehensive error handling and recovery mechanisms
- Created order processing dashboard with statistics
- Implemented order processing logs API endpoint
- Added resend notification functionality
- Created unit tests for all order processing features

### Backend Components
- OrderNotification and OrderProcessingLog entities
- OrderNotificationMapper and OrderProcessingLogMapper with XML configurations
- IEmailNotificationService and EmailNotificationServiceImpl
- Updated IOrderService and OrderServiceImpl with new methods
- Added new API endpoints in OrderController

### Frontend Components
- OrderBatchProcessModal for batch processing orders
- OrderProcessingLogsModal for viewing processing logs
- OrderProcessingDashboard with statistics and actions
- Updated order.api.ts with new API methods

### Testing
- Created comprehensive unit tests in OrderProcessingTest.java
- Tests cover batch processing, auto confirmation, notifications, and error handling