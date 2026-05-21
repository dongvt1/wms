# Epic 4: Quản lý người dùng và phân quyền

## Mục tiêu
Xây dựng hệ thống quản lý người dùng và phân quyền chi tiết giúp bảo mật dữ liệu và phân chia trách nhiệm rõ ràng.

## Stories

### Story 4.1: Quản lý tài khoản người dùng
**Mô tả:** Admin có thể tạo, quản lý và xóa tài khoản người dùng trong hệ thống.

**Tiêu chí chấp nhận:**
- Form tạo tài khoản với thông tin: tên đăng nhập, email, vai trò
- Form chỉnh sửa thông tin tài khoản
- Xóa tài khoản với xác nhận
- Hiển thị danh sách người dùng với thông tin cơ bản
- Tìm kiếm người dùng theo tên hoặc email
- Khóa/mở khóa tài khoản
- Xem lịch sử hoạt động của người dùng

**Ghi chú:** Cần tích hợp với phân quyền để gán vai trò cho người dùng mới.

### Story 4.2: Phân quyền và vai trò
**Mô tả:** Admin có thể định nghĩa các vai trò và phân quyền chi tiết cho từng chức năng trong hệ thống.

**Tiêu chí chấp nhận:**
- Định nghĩa các vai trò: Admin, Manager, Staff, Viewer
- Phân quyền chi tiết cho từng chức năng (xem, thêm, sửa, xóa)
- Gán vai trò cho người dùng
- Hiển thị ma trận phân quyền
- Chỉnh sửa phân quyền khi cần
- Xem lịch sử thay đổi phân quyền

**Ghi chú:** Cần áp dụng cho tất cả các chức năng trong hệ thống để bảo mật dữ liệu.

### Story 4.3: Xác thực và bảo mật
**Mô tả:** Hệ thống có các cơ chế xác thực và bảo mật giúp bảo vệ dữ liệu người dùng.

**Tiêu chí chấp nhận:**
- Đăng nhập với email và mật khẩu
- Quên mật khẩu và khôi phục
- Xác thực hai yếu tố (tùy chọn)
- Phiên đăng nhập tự động kết thúc
- Lịch sử đăng nhập gần đây
- Đăng nhập từ thiết bị tin cậy
- Chính sách mật khẩu mạnh

**Ghi chú:** Cần tích hợp với tất cả các chức năng để kiểm soát truy cập.