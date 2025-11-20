# Epic 2: Quản lý đơn hàng và khách hàng

## Mục tiêu
Xây dựng các chức năng giúp người dùng quản lý đơn hàng và thông tin khách hàng hiệu quả.

## Stories

### Story 2.1: Quản lý thông tin khách hàng
**Mô tả:** Người dùng có thể thêm, sửa, xóa và xem thông tin cơ bản của khách hàng trong hệ thống.

**Tiêu chí chấp nhận:**
- Form thêm khách hàng với các trường: tên, mã, địa chỉ, điện thoại, email
- Form chỉnh sửa thông tin khách hàng
- Xác nhận trước khi xóa khách hàng
- Hiển thị danh sách khách hàng với thông tin cơ bản
- Tìm kiếm khách hàng theo tên hoặc mã
- Xem lịch sử đơn hàng của khách hàng

**Ghi chú:** Cần tích hợp với quản lý đơn hàng để hiển thị thông tin liên quan.

### Story 2.2: Quản lý đơn hàng
**Mô tả:** Người dùng có thể tạo, chỉnh sửa, hủy và xem trạng thái của đơn hàng trong hệ thống.

**Tiêu chí chấp nhận:**
- Form tạo đơn hàng với thông tin: khách hàng, sản phẩm, số lượng, giá
- Form chỉnh sửa thông tin đơn hàng
- Hủy đơn hàng với xác nhận
- Hiển thị trạng thái đơn hàng (chờ xác nhận, đã xác nhận, đang giao, hoàn thành)
- Tìm kiếm đơn hàng theo mã hoặc khách hàng
- Tự động tạo mã đơn hàng
- Tự động trừ tồn kho khi đơn hàng được xác nhận

**Ghi chú:** Cần tích hợp với quản lý khách hàng, sản phẩm và tồn kho để cập nhật thông tin liên quan.

### Story 2.3: Xử lý đơn hàng
**Mô tả:** Hệ thống xử lý các bước trong quy trình đơn hàng từ khi tạo đến khi hoàn thành.

**Tiêu chí chấp nhận:**
- Xử lý xác nhận đơn hàng
- Cập nhật trạng thái đơn hàng tự động
- Tích hợp với tồn kho để trừ số lượng
- Gửi thông báo cho khách hàng về trạng thái đơn hàng
- In đơn hàng khi cần

**Ghi chú:** Cần tích hợp với quản lý tồn kho và báo cáo để theo dõi quy trình.