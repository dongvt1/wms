# Story 4.1: Quản lý tài khoản người dùng

## Story Details

**Epic:** Epic 4: Quản lý người dùng và phân quyền
**Story Key:** 4-1-user-account-management
**Status:** Ready for Development
**Priority:** High
**Effort:** 4 days

## User Story

Là một quản trị viên hệ thống, tôi muốn tạo, quản lý và xóa tài khoản người dùng trong hệ thống để có thể kiểm soát truy cập và phân chia trách nhiệm rõ ràng.

## Acceptance Criteria

- [ ] Admin có thể tạo tài khoản mới với thông tin: tên đăng nhập, email, vai trò
- [ ] Admin có thể chỉnh sửa thông tin tài khoản người dùng
- [ ] Admin có thể xóa tài khoản người dùng với xác nhận
- [ ] Hệ thống hiển thị danh sách người dùng với thông tin cơ bản
- [ ] Admin có thể tìm kiếm người dùng theo tên hoặc email
- [ ] Admin có thể khóa/mở khóa tài khoản người dùng
- [ ] Hệ thống hiển thị lịch sử hoạt động của người dùng
- [ ] Hệ thống ghi lại lịch sử thay đổi thông tin tài khoản
- [ ] Hệ thống kiểm tra trùng lặp tên đăng nhập và email
- [ ] Hệ thống hỗ trợ đặt lại mật khẩu cho người dùng

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu người dùng
- [ ] Tạo bảng users với các trường: id, username, email, password, firstName, lastName, phone, avatar, status, lastLoginAt, createdAt, updatedAt
- [ ] Tạo bảng user_profiles với các trường: id, userId, department, position, bio, preferences, createdAt, updatedAt
- [ ] Tạo bảng user_activity_logs với các trường: id, userId, action, resource, details, ipAddress, userAgent, createdAt
- [ ] Tạo bảng user_change_history với các trường: id, userId, changedBy, field, oldValue, newValue, reason, createdAt
- [ ] Thiết kế quan hệ khóa ngoại giữa user_profiles và users
- [ ] Thiết kế quan hệ khóa ngoại giữa user_activity_logs và users
- [ ] Thiết kế quan hệ khóa ngoại giữa user_change_history và users
- [ ] Tạo index cho trường username và email trong bảng users
- [ ] Tạo index cho trường userId trong các bảng liên quan

### Task 2: Xây dựng API quản lý người dùng
- [ ] API tạo người dùng mới (POST /api/users)
- [ ] API cập nhật thông tin người dùng (PUT /api/users/:id)
- [ ] API xóa người dùng (DELETE /api/users/:id)
- [ ] API lấy danh sách người dùng (GET /api/users) với phân trang và tìm kiếm
- [ ] API lấy chi tiết người dùng (GET /api/users/:id)
- [ ] API khóa/mở khóa người dùng (PUT /api/users/:id/status)
- [ ] API đặt lại mật khẩu người dùng (POST /api/users/:id/reset-password)
- [ ] API lấy lịch sử hoạt động người dùng (GET /api/users/:id/activity-logs)
- [ ] API lấy lịch sử thay đổi người dùng (GET /api/users/:id/change-history)
- [ ] API cập nhật thông tin cá nhân (PUT /api/users/profile)

### Task 3: Xây dựng service quản lý người dùng
- [ ] Service tạo người dùng với validation và mã hóa mật khẩu
- [ ] Service cập nhật thông tin người dùng với validation
- [ ] Service xóa người dùng với kiểm tra ràng buộc
- [ ] Service khóa/mở khóa người dùng
- [ ] Service đặt lại mật khẩu và gửi email thông báo
- [ ] Service ghi lại hoạt động của người dùng
- [ ] Service ghi lại lịch sử thay đổi thông tin
- [ ] Service kiểm tra trùng lặp thông tin người dùng
- [ ] Service xử lý upload avatar người dùng
- [ ] Service quản lý thông tin cá nhân người dùng

### Task 4: Xây dựng giao diện quản lý người dùng
- [ ] Trang danh sách người dùng với bảng hiển thị thông tin cơ bản
- [ ] Form tạo người dùng mới với validation
- [ ] Form chỉnh sửa thông tin người dùng
- [ ] Dialog xác nhận xóa người dùng
- [ ] Component tìm kiếm và lọc người dùng
- [ ] Component khóa/mở khóa người dùng
- [ ] Component đặt lại mật khẩu người dùng
- [ ] Component xem lịch sử hoạt động người dùng
- [ ] Component xem lịch sử thay đổi thông tin
- [ ] Form cập nhật thông tin cá nhân

### Task 5: Xây dựng hệ thống logging và audit
- [ ] Interceptor ghi lại hoạt động người dùng
- [ ] Service logging cho các thao tác quan trọng
- [ ] Component hiển thị lịch sử hoạt động
- [ ] Component hiển thị lịch sử thay đổi
- [ ] API xuất lịch sử hoạt động ra file
- [ ] Service lọc và tìm kiếm lịch sử hoạt động
- [ ] Component quản lý retention policy cho logs
- [ ] Service xóa logs cũ theo policy

### Task 6: Tích hợp với các module khác
- [ ] Tích hợp với module phân quyền để gán vai trò cho người dùng
- [ ] Tích hợp với module xác thực để xử lý đăng nhập
- [ ] Tích hợp với module email để gửi thông báo
- [ ] Tích hợp với module quản lý file để xử lý avatar
- [ ] Tích hợp với tất cả các module khác để ghi lại hoạt động
- [ ] Tích hợp với module báo cáo để cung cấp dữ liệu người dùng

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng BCrypt cho mã hóa mật khẩu
- Sử dụng Spring Security cho authentication và authorization
- Sử dụng Spring Mail cho việc gửi email
- Sử dụng Spring AOP cho logging hoạt động người dùng
- Sử dụng JWT cho session management
- Sử dụng Spring Validation cho input validation

## Dependencies

- Cần có module phân quyền đã được triển khai
- Cần có module xác thực đã được triển khai
- Cần có module email đã được triển khai
- Cần có module quản lý file đã được triển khai

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-20
- Story được chọn: 4-1-user-account-management
- Trạng thái hiện tại: Ready for Development

### Completion Notes
- Bắt đầu với Task 1: Thiết kế cơ sở dữ liệu người dùng
- Ưu tiên xây dựng API với validation và security mạnh mẽ
- Tập trung vào việc mã hóa mật khẩu và bảo mật thông tin
- Đảm bảo logging đầy đủ cho audit trail
- Triển khai hệ thống quản lý người dùng thân thiện và hiệu quả