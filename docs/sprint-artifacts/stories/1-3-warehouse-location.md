# Story 1.3: Quản lý vị trí kho

## Story Details

**Epic:** Epic 1: Quản lý sản phẩm và tồn kho
**Story Key:** 1-3-warehouse-location
**Status:** Completed

## Acceptance Criteria

- [ ] Người dùng có thể định nghĩa các vị trí kho (khu vực, kệ, vị trí cụ thể)
- [ ] Người dùng có thể gán sản phẩm đến vị trí cụ thể trong kho
- [ ] Người dùng có thể tìm kiếm sản phẩm theo vị trí kho
- [ ] Hệ thống hiển thị sơ đồ kho với các vị trí đã định nghĩa
- [ ] Người dùng có thể di chuyển sản phẩm giữa các vị trí kho

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu vị trí kho
- [ ] Tạo bảng positions với các trường: id, name, parent_id, type (area/rack/shelf/slot)
- [ ] Tạo bảng areas với các trường: id, name, description
- [ ] Tạo bảng shelves với các trường: id, area_id, name, description
- [ ] Tạo bảng slots với các trường: id, shelf_id, position, capacity, status
- [ ] Thiết kế quan hệ khóa chính: area -> shelf -> slot

### Task 2: Xây dựng API quản lý vị trí kho
- [ ] API tạo vị trí kho (POST /api/positions)
- [ ] API cập nhật vị trí kho (PUT /api/positions/:id)
- [ ] API xóa vị trí kho (DELETE /api/positions/:id)
- [ ] API lấy danh sách vị trí kho (GET /api/positions)
- [ ] API tìm kiếm vị trí kho (GET /api/positions/search)

### Task 3: Xây dựng giao diện quản lý vị trí kho
- [ ] Trang danh sách vị trí kho với phân cấp (khu vực -> kệ -> vị trí)
- [ ] Form tạo/chỉnh sửa vị trí kho
- [ ] Nút xóa vị trí kho
- [ ] Tìm kiếm vị trí kho theo tên
- [ ] Hiển thị sơ đồ kho trực quan

### Task 4: Tích hợp quản lý vị trí với quản lý sản phẩm
- [ ] Thêm trường position_id vào bảng products
- [ ] API gán sản phẩm đến vị trí (POST /api/products/:id/assign-position)
- [ ] API lấy vị trí của sản phẩm (GET /api/products/:id/positions)
- [ ] Cập nhật UI hiển thị vị trí hiện tại của sản phẩm

## Technical Notes

- Sử dụng cấu trúc cây (tree structure) để quản lý vị trí kho hiệu quả
- Mỗi vị trí có thể chứa nhiều sản phẩm
- Cần xử lý trường hợp di chuyển sản phẩm giữa các vị trí
- Cân nhắc capacity của từng vị trí để tránh quá tải

## Dependencies

- Cần có API quản lý sản phẩm đã được triển khai trước
- Cần có cơ sở dữ liệu sản phẩm đã được thiết lập

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-17
- Story được chọn: 1-3-warehouse-location
- Trạng thái hiện tại: In Progress

### Completion Notes
- Bắt đầu với task 1: Thiết kế cơ sở dữ liệu vị trí kho
- Ưu tiên xây dựng API trước để có dữ liệu cho frontend