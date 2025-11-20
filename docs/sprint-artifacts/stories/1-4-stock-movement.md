# Story 1.4: Nhập/Xuất kho

## Story Details

**Epic:** Epic 1: Quản lý sản phẩm và tồn kho
**Story Key:** 1-4-stock-movement
**Status:** Ready for Development
**Priority:** High
**Effort:** 4 days

## User Story

Là một người quản lý kho, tôi muốn ghi nhận các giao dịch nhập kho và xuất kho để hệ thống có thể tự động cập nhật số lượng tồn kho và theo dõi lịch sử giao dịch.

## Acceptance Criteria

- [ ] Người dùng có thể tạo phiếu nhập kho với thông tin: sản phẩm, số lượng, ngày, nhà cung cấp, ghi chú
- [ ] Người dùng có thể tạo phiếu xuất kho với thông tin: sản phẩm, số lượng, ngày, lý do, ghi chú
- [ ] Người dùng có thể thực hiện chuyển kho nội bộ giữa các vị trí
- [ ] Hệ thống hiển thị lịch sử giao dịch nhập/xuất kho
- [ ] Hệ thống tự động cập nhật tồn kho sau mỗi giao dịch
- [ ] Người dùng có thể in phiếu nhập/xuất kho
- [ ] Hệ thống hỗ trợ xử lý hàng loạt nhiều sản phẩm trong một giao dịch
- [ ] Người dùng có thể tìm kiếm và lọc giao dịch theo nhiều tiêu chí
- [ ] Hệ thống hiển thị báo cáo tổng hợp giao dịch theo khoảng thời gian
- [ ] Hệ thống hỗ trợ hủy giao dịch với xác nhận và ghi nhận lý do

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu giao dịch kho
- [ ] Tạo bảng stock_transactions với các trường: id, transactionCode, transactionType, transactionDate, status, createdBy, createdAt, approvedBy, approvedAt, notes
- [ ] Tạo bảng stock_transaction_items với các trường: id, transactionId, productId, quantity, unitPrice, totalPrice, fromLocationId, toLocationId, batchNumber, expiryDate
- [ ] Tạo bảng suppliers với các trường: id, supplierCode, supplierName, contactPerson, phone, email, address, status
- [ ] Thiết kế quan hệ khóa ngoại giữa stock_transactions và stock_transaction_items
- [ ] Thiết kế quan hệ khóa ngoại giữa stock_transaction_items và products
- [ ] Thiết kế quan hệ khóa ngoại giữa stock_transaction_items và warehouse locations
- [ ] Tạo index cho trường transactionCode và transactionDate để tối ưu tìm kiếm

### Task 2: Xây dựng API quản lý giao dịch kho
- [ ] API tạo phiếu nhập kho (POST /api/stock/stock-in)
- [ ] API tạo phiếu xuất kho (POST /api/stock/stock-out)
- [ ] API tạo phiếu chuyển kho nội bộ (POST /api/stock/transfer)
- [ ] API lấy danh sách giao dịch (GET /api/stock/transactions) với phân trang và tìm kiếm
- [ ] API lấy chi tiết giao dịch (GET /api/stock/transactions/:id)
- [ ] API duyệt giao dịch (PUT /api/stock/transactions/:id/approve)
- [ ] API hủy giao dịch (PUT /api/stock/transactions/:id/cancel)
- [ ] API in phiếu giao dịch (GET /api/stock/transactions/:id/print)
- [ ] API quản lý nhà cung cấp (CRUD)
- [ ] API lấy báo cáo tổng hợp giao dịch (GET /api/stock/reports)

### Task 3: Xây dựng giao diện quản lý giao dịch kho
- [ ] Trang danh sách giao dịch với bảng hiển thị thông tin cơ bản
- [ ] Form tạo phiếu nhập kho với validation
- [ ] Form tạo phiếu xuất kho với validation
- [ ] Form tạo phiếu chuyển kho nội bộ với validation
- [ ] Modal xem chi tiết giao dịch
- [ ] Modal duyệt/hủy giao dịch
- [ ] Component tìm kiếm và lọc giao dịch
- [ ] Component phân trang cho danh sách giao dịch
- [ ] Component quản lý nhà cung cấp
- [ ] Component báo cáo tổng hợp giao dịch

### Task 4: Xây dựng hệ thống in ấn
- [ ] Template in phiếu nhập kho
- [ ] Template in phiếu xuất kho
- [ ] Template in phiếu chuyển kho
- [ ] Service tạo PDF từ template
- [ ] Component xem trước và in phiếu
- [ ] API tạo PDF cho phiếu giao dịch

### Task 5: Tích hợp với các module khác
- [ ] Tích hợp với module quản lý sản phẩm để hiển thị thông tin sản phẩm
- [ ] Tích hợp với module quản lý tồn kho để tự động cập nhật số lượng
- [ ] Tích hợp với module quản lý vị trí kho để hiển thị thông tin vị trí
- [ ] Tích hợp với module quản lý nhà cung cấp để hiển thị thông tin nhà cung cấp
- [ ] Tích hợp với module báo cáo để cung cấp dữ liệu giao dịch

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng iText hoặc JasperReports cho việc tạo PDF
- Sử dụng Spring Transaction Management để đảm bảo tính nhất quán của dữ liệu
- Sử dụng Spring Security để kiểm soát quyền truy cập
- Sử dụng Spring Validation cho việc validate dữ liệu đầu vào
- Sử dụng Spring AOP cho việc logging các giao dịch quan trọng

## Dependencies

- Cần có module quản lý sản phẩm đã được triển khai
- Cần có module quản lý tồn kho đã được triển khai
- Cần có module quản lý vị trí kho đã được triển khai
- Cần có hệ thống authentication và authorization được thiết lập

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-20
- Story được chọn: 1-4-stock-movement
- Trạng thái hiện tại: Ready for Development

### Completion Notes
- Bắt đầu với Task 1: Thiết kế cơ sở dữ liệu giao dịch kho
- Ưu tiên xây dựng API trước để có dữ liệu cho frontend
- Tập trung vào tính nhất quán của dữ liệu khi cập nhật tồn kho
- Đảm bảo tính chính xác của các giao dịch
- Triển khai hệ thống in ấn hiệu quả