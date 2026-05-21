# Epic 5: Kế hoạch sản xuất theo dây chuyền (Production Planning)

## Mục tiêu
Xây dựng module *lập kế hoạch sản xuất* giúp doanh nghiệp quản lý **dây chuyền**, **định mức NVL (BOM)**, **lệnh sản xuất** và **theo dõi tiến độ theo công đoạn**, tích hợp tự động với kho tồn kho.

---

## Stories

### Story 5.1: Quản lý dây chuyền sản xuất
**Mô tả:** Người dùng có thể định nghĩa và quản lý các dây chuyền sản xuất của nhà máy.

**Tiêu chí chấp nhận:**
- Thêm, sửa, xóa dây chuyền sản xuất
- Thông tin: mã, tên, năng suất/ngày, đơn vị, trạng thái (active/inactive/maintenance)
- Danh sách dây chuyền với tìm kiếm và lọc theo trạng thái
- Export Excel

**Ghi chú:** Dây chuyền được chọn khi tạo lệnh sản xuất.

---

### Story 5.2: Quản lý định mức nguyên vật liệu (BOM)
**Mô tả:** Người dùng có thể tạo và quản lý Bill of Materials – danh sách nguyên vật liệu cần thiết để sản xuất ra một loại thành phẩm.

**Tiêu chí chấp nhận:**
- Tạo BOM với: mã, tên, thành phẩm đầu ra, số lượng TP, đơn vị, phiên bản
- Thêm nhiều nguyên vật liệu (NVL) vào mỗi BOM với số lượng định mức
- Sửa BOM (thay thế toàn bộ danh sách NVL khi cập nhật)
- Xóa BOM
- Xem chi tiết BOM kèm danh sách NVL
- Lọc BOM theo thành phẩm hoặc trạng thái

**Ghi chú:** BOM được dùng làm cơ sở tính toán lượng NVL tiêu hao khi hoàn thành lệnh sản xuất.

---

### Story 5.3: Lệnh sản xuất (Work Order)
**Mô tả:** Người dùng có thể tạo và quản lý lệnh sản xuất, điều phối sản xuất theo dây chuyền.

**Tiêu chí chấp nhận:**
- Tạo lệnh sản xuất với: BOM, dây chuyền, số lượng KH, ngày KH, ưu tiên
- Thêm công đoạn sản xuất khi tạo lệnh
- Mã lệnh tự động sinh (WOyyyyMMddNNN)
- Thay đổi trạng thái: draft → planned → in_progress → completed / cancelled
- Bắt đầu sản xuất: ghi nhận ngày bắt đầu thực tế
- Hoàn thành sản xuất: nhập số lượng thực tế, hệ thống **tự động trừ kho NVL** và **nhập kho thành phẩm**
- Hủy lệnh sản xuất
- Thống kê tổng quan (tổng lệnh, đang SX, hoàn thành...)

**Ghi chú:** Tích hợp với module Tồn kho (Inventory) để điều chỉnh số lượng tự động.

---

### Story 5.4: Theo dõi tiến độ sản xuất
**Mô tả:** Người dùng theo dõi tiến độ từng công đoạn của lệnh sản xuất và xem nhật ký hoạt động.

**Tiêu chí chấp nhận:**
- Xem danh sách công đoạn theo thứ tự với trạng thái (pending/in_progress/completed/skipped)
- Cập nhật trạng thái và thời gian thực tế của từng công đoạn
- Nhật ký (log) ghi lại mọi hành động: tạo, bắt đầu, hoàn thành, hủy, cập nhật công đoạn
- Chi tiết lệnh hiển thị dạng timeline nhật ký và steps công đoạn

**Ghi chú:** Nhật ký được ghi tự động bởi hệ thống, không cần can thiệp thủ công.

---

## Technical Architecture

### Database Tables
| Bảng | Mục đích |
|---|---|
| `wh_production_line` | Dây chuyền sản xuất |
| `wh_bom` | BOM header |
| `wh_bom_item` | Chi tiết NVL trong BOM |
| `wh_work_order` | Lệnh sản xuất |
| `wh_production_stage` | Công đoạn sản xuất |
| `wh_production_log` | Nhật ký sản xuất |

### Backend (Java)
- Package: `org.jeecg.modules.warehouse.*`
- Controllers: `ProductionLineController`, `BomController`, `WorkOrderController`
- Services: `ProductionLineService`, `BomService`, `WorkOrderService`

### Frontend (Vue3)
- Views: `production/ProductionLineList.vue`, `BomList.vue`, `WorkOrderList.vue`
- Modals: `ProductionLineModal.vue`, `BomModal.vue`, `WorkOrderModal.vue`, `WorkOrderDetailModal.vue`
- API: `src/api/warehouse/productionLine.ts`, `bom.ts`, `workOrder.ts`

---

_Epic 5 được phát triển thêm vào hệ thống WMS, tích hợp chặt chẽ với các Epic 1-4._
