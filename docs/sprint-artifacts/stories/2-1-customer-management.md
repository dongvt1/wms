# Story 2.1: Quản lý thông tin khách hàng

## Story Details

**Epic:** Epic 2: Quản lý đơn hàng và khách hàng
**Story Key:** 2-1-customer-management
**Status:** Ready for Development
**Priority:** Medium
**Effort:** 3 days

## User Story

Là một người quản lý kho, tôi muốn quản lý thông tin khách hàng trong hệ thống để có thể tạo và theo dõi đơn hàng một cách hiệu quả.

## Acceptance Criteria

- [ ] Người dùng có thể thêm khách hàng mới với các trường: tên, mã, địa chỉ, điện thoại, email
- [ ] Người dùng có thể chỉnh sửa thông tin khách hàng đã tồn tại
- [ ] Người dùng có thể xóa khách hàng khỏi hệ thống với xác nhận
- [ ] Hệ thống hiển thị danh sách khách hàng với thông tin cơ bản và phân trang
- [ ] Người dùng có thể tìm kiếm khách hàng theo tên hoặc mã
- [ ] Người dùng có thể xem lịch sử đơn hàng của khách hàng
- [ ] Hệ thống hiển thị thông tin nợ/có của khách hàng
- [ ] Mã khách hàng phải là duy nhất trong hệ thống
- [ ] Email khách hàng phải có định dạng hợp lệ
- [ ] Số điện thoại khách hàng phải có định dạng hợp lệ
- [ ] Hệ thống hiển thị thống kê đơn hàng của khách hàng (tổng số đơn, tổng giá trị)

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu khách hàng
- [ ] Tạo bảng customers với các trường: id, customerCode, customerName, address, phone, email, contactPerson, taxCode, status, createdAt, updatedAt
- [ ] Tạo bảng customer_balances với các trường: id, customerId, balance, lastUpdated, updatedBy
- [ ] Thiết kế index cho trường customerCode để tối ưu tìm kiếm
- [ ] Thiết kế index cho trường phone và email để tối ưu tìm kiếm
- [ ] Tạo trigger để tự động tạo bản ghi trong customer_balances khi thêm khách hàng mới

### Task 2: Xây dựng API quản lý khách hàng
- [ ] API tạo khách hàng mới (POST /api/customers)
- [ ] API cập nhật thông tin khách hàng (PUT /api/customers/:id)
- [ ] API xóa khách hàng (DELETE /api/customers/:id)
- [ ] API lấy danh sách khách hàng (GET /api/customers) với phân trang và tìm kiếm
- [ ] API lấy chi tiết khách hàng (GET /api/customers/:id)
- [ ] API tìm kiếm khách hàng (GET /api/customers/search)
- [ ] API lấy lịch sử đơn hàng của khách hàng (GET /api/customers/:id/orders)
- [ ] API lấy thông tin nợ/có của khách hàng (GET /api/customers/:id/balance)
- [ ] API cập nhật thông tin nợ/có của khách hàng (PUT /api/customers/:id/balance)
- [ ] API lấy thống kê đơn hàng của khách hàng (GET /api/customers/:id/statistics)

### Task 3: Xây dựng giao diện quản lý khách hàng
- [ ] Trang danh sách khách hàng với bảng hiển thị thông tin cơ bản
- [ ] Form tạo/chỉnh sửa khách hàng với validation
- [ ] Dialog xác nhận xóa khách hàng
- [ ] Component tìm kiếm và lọc khách hàng
- [ ] Component phân trang cho danh sách khách hàng
- [ ] Modal xem chi tiết khách hàng
- [ ] Component hiển thị lịch sử đơn hàng của khách hàng
- [ ] Component hiển thị thông tin nợ/có của khách hàng
- [ ] Component hiển thị thống kê đơn hàng của khách hàng

### Task 4: Tích hợp với các module khác
- [ ] Tích hợp với module quản lý đơn hàng để hiển thị lịch sử đơn hàng
- [ ] Tích hợp với module quản lý thanh toán để cập nhật thông tin nợ/có
- [ ] Tích hợp với module báo cáo để cung cấp dữ liệu khách hàng
- [ ] Tích hợp với module notification để gửi thông báo cho khách hàng

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng Spring Validation cho việc validate dữ liệu đầu vào
- Sử dụng Spring Security để kiểm soát quyền truy cập
- Sử dụng Spring AOP cho việc logging các thao tác quan trọng
- Sử dụng Spring Mail cho việc gửi email cho khách hàng

## Dependencies

- Cần có hệ thống authentication và authorization được thiết lập
- Cần có module quản lý đơn hàng được triển khai (để tích hợp)
- Cần có module quản lý thanh toán được triển khai (để tích hợp)

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-20
- Story được chọn: 2-1-customer-management
- Trạng thái hiện tại: Ready for Development

### Completion Notes
- Bắt đầu với Task 1: Thiết kế cơ sở dữ liệu khách hàng
- Ưu tiên xây dựng API trước để có dữ liệu cho frontend
- Tập trung vào validation và error handling
- Đảm bảo tính bảo mật của thông tin khách hàng
- Tối ưu performance khi xử lý danh sách khách hàng lớn