# Story 2.2: Quản lý đơn hàng

## Story Details

**Epic:** Epic 2: Quản lý đơn hàng và khách hàng
**Story Key:** 2-2-order-management
**Status:** Ready for Development
**Priority:** Medium
**Effort:** 4 days

## User Story

Là một người quản lý kho, tôi muốn quản lý đơn hàng của khách hàng để có thể theo dõi và xử lý các yêu cầu của khách hàng một cách hiệu quả.

## Acceptance Criteria

- [ ] Người dùng có thể tạo đơn hàng mới với thông tin: khách hàng, sản phẩm, số lượng, giá
- [ ] Người dùng có thể chỉnh sửa thông tin đơn hàng
- [ ] Người dùng có thể hủy đơn hàng với xác nhận
- [ ] Hệ thống hiển thị trạng thái đơn hàng (chờ xác nhận, đã xác nhận, đang giao, hoàn thành, đã hủy)
- [ ] Người dùng có thể tìm kiếm đơn hàng theo mã hoặc khách hàng
- [ ] Hệ thống tự động tạo mã đơn hàng
- [ ] Hệ thống tự động trừ tồn kho khi đơn hàng được xác nhận
- [ ] Hệ thống hiển thị danh sách đơn hàng với phân trang
- [ ] Người dùng có thể xem chi tiết đơn hàng
- [ ] Hệ thống hiển thị tổng giá trị đơn hàng
- [ ] Hệ thống hỗ trợ in đơn hàng
- [ ] Hệ thống hiển thị lịch sử thay đổi trạng thái đơn hàng

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu đơn hàng
- [ ] Tạo bảng orders với các trường: id, orderCode, customerId, orderDate, status, totalAmount, discountAmount, taxAmount, finalAmount, notes, createdBy, createdAt, updatedAt
- [ ] Tạo bảng order_items với các trường: id, orderId, productId, quantity, unitPrice, totalPrice, discountAmount, finalAmount
- [ ] Tạo bảng order_status_history với các trường: id, orderId, fromStatus, toStatus, reason, userId, createdAt
- [ ] Thiết kế quan hệ khóa ngoại giữa orders và customers
- [ ] Thiết kế quan hệ khóa ngoại giữa orders và order_items
- [ ] Thiết kế quan hệ khóa ngoại giữa order_items và products
- [ ] Tạo index cho trường orderCode để tối ưu tìm kiếm
- [ ] Tạo index cho trường customerId để tối ưu truy vấn

### Task 2: Xây dựng API quản lý đơn hàng
- [ ] API tạo đơn hàng mới (POST /api/orders)
- [ ] API cập nhật thông tin đơn hàng (PUT /api/orders/:id)
- [ ] API hủy đơn hàng (PUT /api/orders/:id/cancel)
- [ ] API lấy danh sách đơn hàng (GET /api/orders) với phân trang và tìm kiếm
- [ ] API lấy chi tiết đơn hàng (GET /api/orders/:id)
- [ ] API tìm kiếm đơn hàng (GET /api/orders/search)
- [ ] API cập nhật trạng thái đơn hàng (PUT /api/orders/:id/status)
- [ ] API lấy lịch sử thay đổi trạng thái đơn hàng (GET /api/orders/:id/status-history)
- [ ] API in đơn hàng (GET /api/orders/:id/print)
- [ ] API lấy thống kê đơn hàng (GET /api/orders/statistics)

### Task 3: Xây dựng giao diện quản lý đơn hàng
- [ ] Trang danh sách đơn hàng với bảng hiển thị thông tin cơ bản
- [ ] Form tạo/chỉnh sửa đơn hàng với validation
- [ ] Dialog xác nhận hủy đơn hàng
- [ ] Component tìm kiếm và lọc đơn hàng
- [ ] Component phân trang cho danh sách đơn hàng
- [ ] Modal xem chi tiết đơn hàng
- [ ] Component cập nhật trạng thái đơn hàng
- [ ] Component hiển thị lịch sử thay đổi trạng thái
- [ ] Component in đơn hàng
- [ ] Component thống kê đơn hàng

### Task 4: Xây dựng hệ thống xử lý đơn hàng
- [ ] Service tự động tạo mã đơn hàng
- [ ] Service tính toán tổng giá trị đơn hàng
- [ ] Service kiểm tra và trừ tồn kho khi xác nhận đơn hàng
- [ ] Service hoàn trả tồn kho khi hủy đơn hàng
- [ ] Service cập nhật trạng thái đơn hàng
- [ ] Service gửi thông báo khi thay đổi trạng thái đơn hàng
- [ ] Service xử lý logic kinh doanh cho đơn hàng

### Task 5: Tích hợp với các module khác
- [ ] Tích hợp với module quản lý khách hàng để hiển thị thông tin khách hàng
- [ ] Tích hợp với module quản lý sản phẩm để hiển thị thông tin sản phẩm
- [ ] Tích hợp với module quản lý tồn kho để kiểm tra và trừ tồn kho
- [ ] Tích hợp với module quản lý thanh toán để xử lý thanh toán
- [ ] Tích hợp với module báo cáo để cung cấp dữ liệu đơn hàng
- [ ] Tích hợp với module notification để gửi thông báo

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng Spring Transaction Management để đảm bảo tính nhất quán của dữ liệu
- Sử dụng Spring Security để kiểm soát quyền truy cập
- Sử dụng Spring Validation cho việc validate dữ liệu đầu vào
- Sử dụng Spring AOP cho việc logging các thao tác quan trọng
- Sử dụng iText hoặc JasperReports cho việc tạo PDF
- Sử dụng WebSocket cho real-time notifications

## Dependencies

- Cần có module quản lý khách hàng đã được triển khai
- Cần có module quản lý sản phẩm đã được triển khai
- Cần có module quản lý tồn kho đã được triển khai
- Cần có hệ thống authentication và authorization được thiết lập

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-20
- Story được chọn: 2-2-order-management
- Trạng thái hiện tại: Ready for Development

### Completion Notes
- Bắt đầu với Task 1: Thiết kế cơ sở dữ liệu đơn hàng
- Ưu tiên xây dựng API trước để có dữ liệu cho frontend
- Tập trung vào tính nhất quán của dữ liệu khi cập nhật tồn kho
- Đảm bảo tính chính xác của các giao dịch đơn hàng
- Triển khai hệ thống xử lý đơn hàng hiệu quả