# Epic 6: Quản lý Chất lượng QMS (Quality Management System)

## Mục tiêu
Xây dựng module **kiểm soát chất lượng** cho nguyên liệu đầu vào (IQC) và sản phẩm sản xuất (PQC), với hệ thống cấu hình động các tiêu chí kiểm tra (Checklist Template), tích hợp với kho tồn kho và lệnh sản xuất.

---

## Stories

### Story 6.1: Cấu hình mẫu tiêu chí kiểm tra (Checklist Template)
**Mô tả:** Quản trị viên chất lượng định nghĩa các bộ tiêu chí kiểm tra tái sử dụng.

**Tiêu chí chấp nhận:**
- Tạo mẫu với: mã, tên, loại (IQC/PQC), sản phẩm áp dụng (tuỳ chọn), trạng thái
- Thêm các tiêu chí vào mẫu: tên tiêu chí, giá trị chuẩn, kiểu nhập (pass_fail / text / number / select), bắt buộc / không
- Sửa mẫu (cập nhật toàn bộ danh sách tiêu chí)
- Xóa mẫu, lọc theo loại và trạng thái
- Khi tạo phiếu IQC/PQC, chọn mẫu → tự động load danh sách tiêu chí

**Ghi chú:** Mẫu để NULL `product_id` = áp dụng chung cho mọi sản phẩm.

---

### Story 6.2: Kiểm tra chất lượng đầu vào (IQC – Incoming Quality Control)
**Mô tả:** Nhân viên QC tạo và xử lý phiếu kiểm tra khi nhận nguyên liệu từ nhà cung cấp.

**Tiêu chí chấp nhận:**
- Tạo phiếu IQC: chọn sản phẩm, nhà cung cấp, phiếu nhập kho liên kết, mẫu checklist, SL nhận
- Mã phiếu tự động sinh (IQCyyyyMMddNNN)
- Điền kết quả từng tiêu chí: giá trị thực đo, đạt/không đạt/N/A
- Vòng đời: `draft` → `in_progress` → `passed` / `failed` / `conditional`
- Xem chi tiết phiếu + kết quả từng tiêu chí
- Thống kê: tổng phiếu, đang KT, đạt, không đạt, có điều kiện

---

### Story 6.3: Kiểm tra chất lượng sản xuất (PQC – Process Quality Control)
**Mô tả:** Nhân viên QC kiểm tra chất lượng trong/sau quá trình sản xuất, liên kết với lệnh sản xuất.

**Tiêu chí chấp nhận:**
- Tạo phiếu PQC: chọn lệnh sản xuất, thành phẩm, công đoạn (tuỳ chọn), mẫu checklist, SL kiểm tra
- Mã phiếu tự động sinh (PQCyyyyMMddNNN)
- Điền kết quả từng tiêu chí tương tự IQC
- Vòng đời: `draft` → `in_progress` → `passed` / `failed`
- Thống kê riêng cho PQC

---

## Technical Architecture

### Database Tables
| Bảng | Mục đích |
|---|---|
| `wh_qms_checklist_template` | Mẫu bộ tiêu chí kiểm tra |
| `wh_qms_checklist_item` | Chi tiết tiêu chí trong mẫu |
| `wh_iqc_inspection` | Phiếu kiểm tra IQC |
| `wh_iqc_inspection_result` | Kết quả từng tiêu chí IQC |
| `wh_pqc_inspection` | Phiếu kiểm tra PQC |
| `wh_pqc_inspection_result` | Kết quả từng tiêu chí PQC |

### Backend (Java)
- Package: `org.jeecg.modules.warehouse.*`
- Controllers: `QmsChecklistTemplateController`, `IqcInspectionController`, `PqcInspectionController`
- API endpoints:
  - `/warehouse/qms/checklist/*`
  - `/warehouse/qms/iqc/*`
  - `/warehouse/qms/pqc/*`

### Frontend (Vue3)
- Views: `views/warehouse/quality/`
  - `ChecklistTemplateList.vue`, `ChecklistTemplateModal.vue`
  - `IqcInspectionList.vue`, `IqcInspectionModal.vue`, `IqcInspectionDetailModal.vue`
  - `PqcInspectionList.vue`, `PqcInspectionModal.vue`, `PqcInspectionDetailModal.vue`
- API: `src/api/warehouse/qmsChecklist.ts`, `iqcInspection.ts`, `pqcInspection.ts`

---

_Epic 6 tích hợp với Epic 3 (Nhập/Xuất kho) và Epic 5 (Kế hoạch sản xuất)._
