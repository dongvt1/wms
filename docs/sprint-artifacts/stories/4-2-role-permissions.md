# Story 4.2: Phân quyền và vai trò

## Story Details

**Epic:** Epic 4: Quản lý người dùng và phân quyền
**Story Key:** 4-2-role-permissions
**Status:** Ready for Development
**Priority:** High
**Effort:** 5 days

## User Story

Là một quản trị viên hệ thống, tôi muốn định nghĩa các vai trò và phân quyền chi tiết cho từng chức năng trong hệ thống để có thể bảo mật dữ liệu và phân chia trách nhiệm rõ ràng.

## Acceptance Criteria

- [ ] Admin có thể định nghĩa các vai trò: Admin, Manager, Staff, Viewer
- [ ] Admin có thể phân quyền chi tiết cho từng chức năng (xem, thêm, sửa, xóa)
- [ ] Admin có thể gán vai trò cho người dùng
- [ ] Hệ thống hiển thị ma trận phân quyền rõ ràng
- [ ] Admin có thể chỉnh sửa phân quyền khi cần
- [ ] Hệ thống hiển thị lịch sử thay đổi phân quyền
- [ ] Hệ thống kiểm tra quyền truy cập trước khi thực hiện hành động
- [ ] Hệ thống hỗ trợ phân quyền theo cấp độ (tổ chức, phòng ban)
- [ ] Hệ thống hỗ trợ phân quyền tạm thời
- [ ] Hệ thống ghi lại tất cả các hoạt động liên quan đến phân quyền

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu phân quyền
- [ ] Tạo bảng roles với các trường: id, name, code, description, isSystem, createdAt, updatedAt
- [ ] Tạo bảng permissions với các trường: id, name, code, resource, action, description, createdAt, updatedAt
- [ ] Tạo bảng role_permissions với các trường: id, roleId, permissionId, createdAt
- [ ] Tạo bảng user_roles với các trường: id, userId, roleId, assignedBy, assignedAt, expiresAt
- [ ] Tạo bảng permission_change_history với các trường: id, roleId, permissionId, action, changedBy, reason, createdAt
- [ ] Thiết kế quan hệ khóa ngoại giữa role_permissions và roles, permissions
- [ ] Thiết kế quan hệ khóa ngoại giữa user_roles và users, roles
- [ ] Thiết kế quan hệ khóa ngoại giữa permission_change_history và roles, permissions
- [ ] Tạo index cho các trường tìm kiếm và kiểm tra quyền

### Task 2: Xây dựng API quản lý vai trò và phân quyền
- [ ] API tạo vai trò mới (POST /api/roles)
- [ ] API cập nhật thông tin vai trò (PUT /api/roles/:id)
- [ ] API xóa vai trò (DELETE /api/roles/:id)
- [ ] API lấy danh sách vai trò (GET /api/roles)
- [ ] API lấy chi tiết vai trò (GET /api/roles/:id)
- [ ] API tạo quyền mới (POST /api/permissions)
- [ ] API lấy danh sách quyền (GET /api/permissions)
- [ ] API gán quyền cho vai trò (POST /api/roles/:id/permissions)
- [ ] API xóa quyền khỏi vai trò (DELETE /api/roles/:id/permissions/:permissionId)
- [ ] API gán vai trò cho người dùng (POST /api/users/:id/roles)
- [ ] API xóa vai trò khỏi người dùng (DELETE /api/users/:id/roles/:roleId)
- [ ] API kiểm tra quyền của người dùng (GET /api/users/:id/permissions)
- [ ] API lấy lịch sử thay đổi phân quyền (GET /api/roles/:id/permission-history)

### Task 3: Xây dựng service quản lý phân quyền
- [ ] Service quản lý vai trò với validation
- [ ] Service quản lý quyền với validation
- [ ] Service gán/xóa quyền cho vai trò
- [ ] Service gán/xóa vai trò cho người dùng
- [ ] Service kiểm tra quyền truy cập của người dùng
- [ ] Service caching cho quyền người dùng để tối ưu performance
- [ ] Service ghi lại lịch sử thay đổi phân quyền
- [ ] Service xử lý phân quyền tạm thời
- [ ] Service xử lý phân quyền theo cấp độ
- [ ] Service khởi tạo dữ liệu phân quyền mặc định

### Task 4: Xây dựng giao diện quản lý vai trò và phân quyền
- [ ] Trang danh sách vai trò với thông tin cơ bản
- [ ] Form tạo/chỉnh sửa vai trò
- [ ] Component ma trận phân quyền với giao diện trực quan
- [ ] Component gán quyền cho vai trò với checkbox tree
- [ ] Component gán vai trò cho người dùng
- [ ] Component xem lịch sử thay đổi phân quyền
- [ ] Component kiểm tra quyền của người dùng
- [ ] Component quản lý phân quyền tạm thời
- [ ] Component quản lý phân quyền theo cấp độ
- [ ] Component xuất/import cấu hình phân quyền

### Task 5: Xây dựng hệ thống kiểm soát truy cập
- [ ] Interceptor kiểm tra quyền trước khi thực hiện action
- [ ] Annotation-based security cho các method
- [ ] Service kiểm tra quyền động dựa trên context
- [ ] Component hiển thị/ẩn UI elements dựa trên quyền
- [ ] Service xử lý khi quyền bị thay đổi
- [ ] Service refresh cache khi có thay đổi phân quyền
- [ ] Component quản lý session khi quyền thay đổi
- [ ] Service logging cho các hoạt động kiểm tra quyền

### Task 6: Tích hợp với các module khác
- [ ] Tích hợp với module quản lý người dùng để gán vai trò
- [ ] Tích hợp với tất cả các module khác để kiểm tra quyền
- [ ] Tích hợp với module xác thực để xử lý login
- [ ] Tích hợp với module logging để ghi lại hoạt động
- [ ] Tích hợp với module báo cáo để cung cấp dữ liệu phân quyền
- [ ] Tích hợp với module audit để theo dõi thay đổi

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng Spring Security với custom authorization
- Sử dụng Redis cho caching quyền người dùng
- Sử dụng Spring AOP cho aspect-oriented security
- Sử dụng Custom annotations cho method-level security
- Sử dụng Event-driven architecture cho việc refresh cache
- Sử dụng JSON Web Token (JWT) cho stateless authentication

## Dependencies

- Cần có module quản lý người dùng đã được triển khai
- Cần có module xác thực đã được triển khai
- Cần có module logging đã được triển khai
- Cần có tất cả các module chức năng đã được xác định quyền

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-20
- Story được chọn: 4-2-role-permissions
- Trạng thái hiện tại: Ready for Development

### Completion Notes
- Bắt đầu với Task 1: Thiết kế cơ sở dữ liệu phân quyền
- Ưu tiên xây dựng hệ thống kiểm soát truy cập mạnh mẽ
- Tập trung vào performance khi kiểm tra quyền thường xuyên
- Đảm bảo tính linh hoạt của hệ thống phân quyền
- Triển khai giao diện quản lý phân quyền trực quan và dễ sử dụng
- Xây dựng hệ thống caching hiệu quả cho quyền người dùng