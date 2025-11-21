# Story 2.4: Quản lý nhà cung cấp

## Story Details

**Epic:** Epic 2: Quản lý đơn hàng và khách hàng
**Story Key:** 2-4-supplier-management
**Status:** ready-for-dev
**Priority:** Low
**Effort:** 3 days

## User Story

Là một quản lý kho, tôi muốn quản lý thông tin nhà cung cấp và theo dõi lịch sử giao dịch với nhà cung cấp để duy trì mối quan hệ tốt với nhà cung cấp và giám sát hoạt động mua hàng.

## Acceptance Criteria

- [ ] Form thêm nhà cung cấp với các trường: tên, mã, thông tin liên hệ, địa chỉ, điện thoại, email
- [ ] Form chỉnh sửa thông tin nhà cung cấp
- [ ] Xác nhận trước khi xóa nhà cung cấp
- [ ] Hiển thị danh sách nhà cung cấp với thông tin cơ bản
- [ ] Tìm kiếm nhà cung cấp theo tên hoặc mã
- [ ] Xem lịch sử giao dịch với từng nhà cung cấp
- [ ] Gán sản phẩm cho nhà cung cấp cụ thể
- [ ] Hiển thị danh sách nhà cung cấp cho từng sản phẩm

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu quản lý nhà cung cấp
- [ ] Tạo bảng suppliers với các trường: id, code, name, contactPerson, phone, email, address, status, createdBy, createdAt, updatedBy, updatedAt
- [ ] Tạo bảng supplier_products với các trường: id, supplierId, productId, supplierProductCode, price, minOrderQuantity, leadTime, isActive, createdAt, updatedAt
- [ ] Tạo bảng supplier_transactions với các trường: id, supplierId, type, referenceId, totalAmount, status, notes, createdBy, createdAt
- [ ] Tạo bảng supplier_transaction_details với các trường: id, transactionId, productId, quantity, unitPrice, totalAmount
- [ ] Thiết kế quan hệ khóa ngoại giữa supplier_products và suppliers, products
- [ ] Thiết kế quan hệ khóa ngoại giữa supplier_transactions và suppliers
- [ ] Thiết kế quan hệ khóa ngoại giữa supplier_transaction_details và supplier_transactions, products
- [ ] Tạo index cho các trường code trong suppliers, supplierId trong supplier_products, supplierId trong supplier_transactions

### Task 2: Xây dựng API quản lý nhà cung cấp
- [ ] API lấy danh sách nhà cung cấp (GET /api/suppliers)
- [ ] API lấy chi tiết nhà cung cấp (GET /api/suppliers/:id)
- [ ] API tạo nhà cung cấp mới (POST /api/suppliers)
- [ ] API cập nhật thông tin nhà cung cấp (PUT /api/suppliers/:id)
- [ ] API xóa nhà cung cấp (DELETE /api/suppliers/:id)
- [ ] API tìm kiếm nhà cung cấp (GET /api/suppliers/search)
- [ ] API lấy danh sách sản phẩm của nhà cung cấp (GET /api/suppliers/:id/products)
- [ ] API gán sản phẩm cho nhà cung cấp (POST /api/suppliers/:id/products)
- [ ] API cập nhật thông tin sản phẩm nhà cung cấp (PUT /api/suppliers/:id/products/:productId)
- [ ] API xóa sản phẩm khỏi nhà cung cấp (DELETE /api/suppliers/:id/products/:productId)
- [ ] API lấy lịch sử giao dịch với nhà cung cấp (GET /api/suppliers/:id/transactions)
- [ ] API lấy chi tiết giao dịch (GET /api/suppliers/transactions/:id)

### Task 3: Xây dựng service quản lý nhà cung cấp
- [ ] Service quản lý nhà cung cấp với validation
- [ ] Service quản lý sản phẩm nhà cung cấp
- [ ] Service quản lý giao dịch nhà cung cấp
- [ ] Service tìm kiếm và lọc nhà cung cấp
- [ ] Service logging cho tất cả các thao tác
- [ ] Service xử lý logic kinh doanh liên quan đến nhà cung cấp

### Task 4: Xây dựng giao diện quản lý nhà cung cấp
- [ ] Trang danh sách nhà cung cấp với tìm kiếm và lọc
- [ ] Component form thêm/sửa nhà cung cấp
- [ ] Component xác nhận xóa nhà cung cấp
- [ ] Component quản lý sản phẩm của nhà cung cấp
- [ ] Component xem lịch sử giao dịch với nhà cung cấp
- [ ] Modal xem chi tiết nhà cung cấp
- [ ] Component gán sản phẩm cho nhà cung cấp

### Task 5: Tích hợp với các module khác
- [ ] Tích hợp với module quản lý sản phẩm để lấy thông tin sản phẩm
- [ ] Tích hợp với module quản lý tồn kho để cập nhật thông tin nhà cung cấp
- [ ] Tích hợp với module báo cáo để cung cấp dữ liệu nhà cung cấp
- [ ] Tích hợp với module nhập kho để tự động gán nhà cung cấp

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng Transaction Management để đảm bảo tính nhất quán của dữ liệu
- Áp dụng các pattern từ story 2-3-order-processing về logging và error handling

## Dependencies

- Cần có module quản lý sản phẩm đã được triển khai
- Cần có module quản lý tồn kho đã được triển khai

### Learnings from Previous Story

**From Story 2-3-order-processing (Status: ready-for-dev)**

- **Database Pattern**: Sử dụng pattern tạo bảng logs cho history tracking (supplier_processing_logs tương tự order_processing_logs)
- **Service Pattern**: Áp dụng pattern logging cho tất cả các thao tác như đã làm trong OrderService
- **API Pattern**: Theo cấu trúc API đã thiết lập trong OrderController cho consistency
- **Frontend Pattern**: Sử dụng similar component structure như OrderList.vue và OrderForm.vue
- **Testing Pattern**: Áp dụng structure unit tests như trong OrderProcessingTest.java
- **Error Handling**: Sử dụng similar error handling and recovery mechanisms

[Source: stories/2-3-order-processing.md#Implementation-Details]

## Dev Agent Record

### Context Reference

- docs/sprint-artifacts/stories/2-4-supplier-management.context.xml

### Agent Model Used

{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

### File List