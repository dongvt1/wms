# Epic 1: Quản lý sản phẩm và tồn kho

## Mục tiêu
Xây dựng các chức năng cốt lõi giúp người dùng quản lý thông tin sản phẩm và theo dõi tồn kho hiệu quả.

## Stories

### Story 1.1: Quản lý thông tin sản phẩm
**Mô tả:** Người dùng có thể thêm, sửa, xóa và xem thông tin cơ bản của sản phẩm trong hệ thống.

**Tiêu chí chấp nhận:**
- Form thêm sản phẩm với các trường: tên, mã, mô tả, giá, danh mục
- Form chỉnh sửa thông tin sản phẩm
- Xác nhận trước khi xóa sản phẩm
- Hiển thị danh sách sản phẩm với thông tin cơ bản
- Tìm kiếm sản phẩm theo tên hoặc mã

**Ghi chú:** Cần tích hợp với quản lý tồn kho để cập nhật số lượng khi thêm/sửa sản phẩm.

### Story 1.2: Theo dõi tồn kho
**Mô tả:** Hệ thống hiển thị số lượng tồn kho hiện tại cho từng sản phẩm và tự động cập nhật sau mỗi giao dịch.

**Tiêu chí chấp nhận:**
- Hiển thị số lượng tồn kho cho từng sản phẩm
- Cập nhật tự động sau mỗi giao dịch nhập/xuất
- Lịch sử thay đổi tồn kho
- Thiết lập ngưỡng tồn kho tối thiểu
- Cảnh báo khi tồn kho thấp

**Ghi chú:** Cần tích hợp với quản lý nhập/xuất kho và hệ thống cảnh báo.

### Story 1.3: Quản lý vị trí kho
**Mô tả:** Người dùng có thể định nghĩa các vị trí kho và gán sản phẩm đến vị trí cụ thể.

**Tiêu chí chấp nhận:**
- Định nghĩa khu vực, kệ, vị trí cụ thể trong kho
- Gán sản phẩm đến vị trí cụ thể
- Tìm kiếm sản phẩm theo vị trí kho
- Hiển thị sơ đồ kho với các vị trí đã định nghĩa
- Di chuyển sản phẩm giữa các vị trí kho

**Ghi chú:** Cần tích hợp với quản lý sản phẩm để hiển thị vị trí của từng sản phẩm.

### Story 1.4: Nhập/Xuất kho
**Mô tả:** Người dùng có thể ghi nhận các giao dịch nhập kho và xuất kho, với hệ thống tự động cập nhật số lượng tồn kho.

**Tiêu chí chấp nhận:**
- Form nhập kho với thông tin: sản phẩm, số lượng, ngày, nhà cung cấp
- Form xuất kho với thông tin: sản phẩm, số lượng, ngày, lý do
- Lịch sử giao dịch nhập/xuất kho
- Tự động cập nhật tồn kho sau mỗi giao dịch
- In phiếu nhập/xuất kho

**Ghi chú:** Cần tích hợp với quản lý sản phẩm và nhà cung cấp để hiển thị thông tin liên quan.