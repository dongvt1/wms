# Requirements Document

## Introduction

Tính năng Cấu hình Bước Kiểm tra QMS (QMS Step Configuration) cho phép các công ty sản xuất tùy biến quy trình kiểm tra chất lượng theo đặc thù riêng. Mỗi công ty có quy trình kiểm tra khác nhau — ví dụ Công ty A cần: Kiểm tra ngoại quan → Kiểm tra nứt vỡ → Phê duyệt, trong khi Công ty B cần: Kiểm tra kích thước → Kiểm tra thông số → Phê duyệt.

Hệ thống hiện tại đã có module QMS cơ bản với các giai đoạn IQC/PQC/FQC và bảng `qms_qc_stage`/`qms_qc_stage_param`. Tính năng mới này nâng cấp thành hệ thống **Inspection Template** cho phép:
- Định nghĩa các bước kiểm tra (steps) trong mỗi giai đoạn QC (IQC/PQC/FQC)
- Cấu hình trường dữ liệu động (dynamic fields) cho mỗi bước với nhiều kiểu dữ liệu
- Gán template cho sản phẩm/nhóm sản phẩm cụ thể
- Thực hiện kiểm tra theo template đã cấu hình
- Đánh giá kết quả tự động dựa trên dung sai và logic pass/fail
- Quy trình phê duyệt kết quả kiểm tra
- Tích hợp với routing steps của WMS Manufacturing Platform

## Glossary

- **Hệ_thống_QMS**: Module quản lý chất lượng trong WMS Manufacturing Platform
- **Inspection_Template**: Mẫu kiểm tra chất lượng, định nghĩa toàn bộ quy trình kiểm tra cho một giai đoạn QC cụ thể (IQC/PQC/FQC)
- **Inspection_Step**: Một bước kiểm tra trong Inspection_Template, thực hiện theo thứ tự tuần tự
- **Step_Field**: Trường dữ liệu cần nhập trong một Inspection_Step, có kiểu dữ liệu và ràng buộc cụ thể
- **Field_Type**: Kiểu dữ liệu của Step_Field, bao gồm: text, number, boolean, select, measurement
- **Measurement_Field**: Kiểu Step_Field đặc biệt cho phép nhập giá trị đo lường kèm dung sai (tolerance) với giá trị danh nghĩa, giới hạn trên, giới hạn dưới
- **Inspection_Execution**: Phiên thực hiện kiểm tra thực tế dựa trên Inspection_Template đã gán
- **Step_Result**: Kết quả của một Inspection_Step trong phiên kiểm tra, bao gồm dữ liệu nhập và đánh giá pass/fail
- **Field_Value**: Giá trị thực tế được nhập cho một Step_Field trong phiên kiểm tra
- **Quản_lý_QC**: Người có quyền cấu hình Inspection_Template, gán template, và phê duyệt kết quả kiểm tra
- **Nhân_viên_QC**: Người thực hiện kiểm tra chất lượng theo template đã cấu hình
- **Giao_diện_Cấu_hình**: Màn hình frontend cho phép Quản_lý_QC quản lý Inspection_Template
- **Giao_diện_Kiểm_tra**: Màn hình frontend cho phép Nhân_viên_QC thực hiện kiểm tra
- **QC_Stage_Type**: Loại giai đoạn kiểm tra: IQC (Incoming), PQC (Process), FQC (Final)
- **Template_Assignment**: Liên kết giữa Inspection_Template và sản phẩm/nhóm sản phẩm
- **Tolerance**: Dung sai cho phép của giá trị đo lường, gồm giá trị danh nghĩa (nominal), giới hạn trên (upper limit), giới hạn dưới (lower limit)
- **Approval_Step**: Bước phê duyệt cuối cùng trong quy trình kiểm tra, yêu cầu Quản_lý_QC xác nhận kết quả
- **Routing_Step**: Công đoạn sản xuất trong WMS Manufacturing Platform, có thể liên kết với QC stage để trigger kiểm tra

## Requirements

### Requirement 1: Quản lý Inspection Template

**User Story:** Là Quản_lý_QC, tôi muốn tạo và quản lý các mẫu kiểm tra (Inspection Template) cho từng giai đoạn QC, để chuẩn hóa quy trình kiểm tra phù hợp với từng loại sản phẩm và công ty.

#### Acceptance Criteria

1. THE Hệ_thống_QMS SHALL cho phép tạo Inspection_Template với các trường: mã template (tự sinh theo định dạng TPLyyyyMMddNNN), tên template (bắt buộc), mô tả, loại giai đoạn QC (IQC/PQC/FQC), phiên bản, trạng thái (draft/active/obsolete), và ghi chú
2. THE Hệ_thống_QMS SHALL đảm bảo mã template là duy nhất trong toàn hệ thống
3. THE Giao_diện_Cấu_hình SHALL hiển thị danh sách Inspection_Template dạng bảng với các cột: mã, tên, loại QC stage, số bước kiểm tra, phiên bản, trạng thái, và ngày cập nhật
4. THE Giao_diện_Cấu_hình SHALL cho phép lọc danh sách theo: loại QC stage (IQC/PQC/FQC), trạng thái (draft/active/obsolete), và tìm kiếm theo tên hoặc mã
5. WHEN Quản_lý_QC kích hoạt Inspection_Template mới cho cùng loại QC stage và cùng sản phẩm, THE Hệ_thống_QMS SHALL tự động chuyển template cũ sang trạng thái obsolete
6. IF Quản_lý_QC xóa Inspection_Template đã được sử dụng trong Inspection_Execution, THEN THE Hệ_thống_QMS SHALL từ chối xóa và hiển thị thông báo lỗi kèm số lượng phiên kiểm tra liên quan
7. THE Hệ_thống_QMS SHALL cho phép nhân bản Inspection_Template để tạo phiên bản mới hoặc template tương tự, bao gồm toàn bộ Inspection_Step và Step_Field

### Requirement 2: Cấu hình Inspection Step trong Template

**User Story:** Là Quản_lý_QC, tôi muốn định nghĩa các bước kiểm tra tuần tự trong mỗi template, để quy trình kiểm tra được thực hiện đúng trình tự và đầy đủ.

#### Acceptance Criteria

1. THE Hệ_thống_QMS SHALL cho phép thêm nhiều Inspection_Step vào một Inspection_Template với các trường: tên bước (bắt buộc), mô tả, thứ tự thực hiện (sort_order), cờ bắt buộc (is_mandatory), và cờ yêu cầu phê duyệt (requires_approval)
2. THE Hệ_thống_QMS SHALL đảm bảo các Inspection_Step trong cùng template có thứ tự thực hiện duy nhất và liên tục
3. THE Giao_diện_Cấu_hình SHALL cho phép sắp xếp lại thứ tự Inspection_Step bằng kéo thả (drag-and-drop)
4. WHEN Quản_lý_QC thay đổi thứ tự bằng kéo thả, THE Hệ_thống_QMS SHALL tự động cập nhật sort_order của tất cả Inspection_Step bị ảnh hưởng
5. THE Giao_diện_Cấu_hình SHALL cho phép xóa Inspection_Step với xác nhận, đồng thời xóa toàn bộ Step_Field thuộc bước đó
6. THE Hệ_thống_QMS SHALL lưu đồng thời Inspection_Template và toàn bộ Inspection_Step trong một transaction duy nhất
7. IF lưu thất bại ở bất kỳ bước nào, THEN THE Hệ_thống_QMS SHALL rollback toàn bộ transaction và hiển thị thông báo lỗi cụ thể

### Requirement 3: Cấu hình Step Field (Trường dữ liệu động)

**User Story:** Là Quản_lý_QC, tôi muốn cấu hình các trường dữ liệu cho mỗi bước kiểm tra với nhiều kiểu dữ liệu khác nhau, để nhân viên QC nhập đúng loại thông tin cần thiết.

#### Acceptance Criteria

1. THE Hệ_thống_QMS SHALL cho phép thêm nhiều Step_Field vào mỗi Inspection_Step với các trường: tên trường (bắt buộc), mã trường, kiểu dữ liệu (Field_Type), đơn vị đo, giá trị mặc định, cờ bắt buộc (is_required), thứ tự hiển thị, và ghi chú hướng dẫn
2. THE Hệ_thống_QMS SHALL hỗ trợ 5 Field_Type: text (văn bản tự do), number (số thực với min/max), boolean (đạt/không đạt), select (chọn từ danh sách tùy chọn), measurement (giá trị đo lường kèm dung sai)
3. WHEN Field_Type là number, THE Giao_diện_Cấu_hình SHALL hiển thị trường cấu hình: giá trị tối thiểu (min_value), giá trị tối đa (max_value), và số chữ số thập phân (decimal_places)
4. WHEN Field_Type là select, THE Giao_diện_Cấu_hình SHALL hiển thị giao diện quản lý danh sách tùy chọn cho phép thêm/xóa/sắp xếp từng mục
5. WHEN Field_Type là measurement, THE Giao_diện_Cấu_hình SHALL hiển thị trường cấu hình: giá trị danh nghĩa (nominal_value), giới hạn trên (upper_tolerance), giới hạn dưới (lower_tolerance), và đơn vị đo
6. WHEN Field_Type là boolean, THE Giao_diện_Cấu_hình SHALL hiển thị tùy chọn cấu hình nhãn cho hai trạng thái (mặc định: "Đạt"/"Không đạt")
7. THE Giao_diện_Cấu_hình SHALL cho phép sắp xếp lại thứ tự Step_Field bằng kéo thả hoặc nút di chuyển lên/xuống
8. WHEN Quản_lý_QC thay đổi Field_Type của Step_Field, THE Giao_diện_Cấu_hình SHALL xóa cấu hình cũ không phù hợp và hiển thị trường cấu hình mới tương ứng

### Requirement 4: Validation cấu hình Template

**User Story:** Là Quản_lý_QC, tôi muốn hệ thống kiểm tra tính hợp lệ của toàn bộ cấu hình template trước khi kích hoạt, để tránh lỗi khi nhân viên QC thực hiện kiểm tra.

#### Acceptance Criteria

1. WHEN Quản_lý_QC kích hoạt Inspection_Template (chuyển sang active), THE Hệ_thống_QMS SHALL validate: template có ít nhất một Inspection_Step, mỗi Inspection_Step bắt buộc có ít nhất một Step_Field
2. WHEN Field_Type là number, THE Hệ_thống_QMS SHALL validate giá trị tối thiểu nhỏ hơn hoặc bằng giá trị tối đa
3. WHEN Field_Type là measurement, THE Hệ_thống_QMS SHALL validate giới hạn dưới nhỏ hơn giá trị danh nghĩa VÀ giá trị danh nghĩa nhỏ hơn giới hạn trên
4. WHEN Field_Type là select, THE Hệ_thống_QMS SHALL validate danh sách tùy chọn có ít nhất một mục và JSON hợp lệ
5. THE Hệ_thống_QMS SHALL thực hiện validation cả ở frontend (hiển thị lỗi tức thì khi nhập) và backend (trả về danh sách lỗi cụ thể khi lưu/kích hoạt)
6. IF validation thất bại khi kích hoạt, THEN THE Hệ_thống_QMS SHALL trả về danh sách tất cả lỗi (không dừng ở lỗi đầu tiên) để Quản_lý_QC sửa một lần

### Requirement 5: Gán Template cho Sản phẩm/Nhóm sản phẩm

**User Story:** Là Quản_lý_QC, tôi muốn gán Inspection Template cho sản phẩm hoặc nhóm sản phẩm cụ thể, để khi kiểm tra sản phẩm đó hệ thống tự động áp dụng đúng template.

#### Acceptance Criteria

1. THE Hệ_thống_QMS SHALL cho phép gán Inspection_Template cho: một sản phẩm cụ thể, một nhóm sản phẩm, hoặc đánh dấu là template mặc định (áp dụng khi không có template riêng)
2. THE Hệ_thống_QMS SHALL hỗ trợ gán nhiều template cho cùng sản phẩm nếu thuộc các QC_Stage_Type khác nhau (ví dụ: một template IQC và một template FQC cho cùng sản phẩm)
3. IF cùng sản phẩm và cùng QC_Stage_Type có nhiều template active, THEN THE Hệ_thống_QMS SHALL chỉ cho phép một template active duy nhất và yêu cầu Quản_lý_QC chọn template thay thế
4. WHEN tìm template cho một sản phẩm, THE Hệ_thống_QMS SHALL áp dụng thứ tự ưu tiên: template gán cho sản phẩm cụ thể → template gán cho nhóm sản phẩm → template mặc định
5. THE Giao_diện_Cấu_hình SHALL hiển thị danh sách sản phẩm/nhóm sản phẩm đã được gán cho mỗi template
6. THE Giao_diện_Cấu_hình SHALL cho phép gán/gỡ template từ sản phẩm bằng giao diện chọn sản phẩm có tìm kiếm

### Requirement 6: Thực hiện Kiểm tra theo Template (Inspection Execution)

**User Story:** Là Nhân_viên_QC, tôi muốn thực hiện kiểm tra chất lượng theo template đã cấu hình với giao diện nhập liệu phù hợp từng bước, để đảm bảo kiểm tra đầy đủ và chính xác.

#### Acceptance Criteria

1. WHEN tạo Inspection_Execution mới, THE Hệ_thống_QMS SHALL tự động tìm và áp dụng Inspection_Template phù hợp dựa trên sản phẩm và QC_Stage_Type
2. THE Giao_diện_Kiểm_tra SHALL hiển thị các Inspection_Step theo đúng thứ tự đã cấu hình, mỗi bước hiển thị danh sách Step_Field với kiểu input tương ứng
3. THE Giao_diện_Kiểm_tra SHALL render đúng kiểu input cho mỗi Field_Type: ô nhập text, input number với min/max, toggle/checkbox cho boolean, dropdown cho select, input number kèm hiển thị dung sai cho measurement
4. WHEN Field_Type là measurement, THE Giao_diện_Kiểm_tra SHALL hiển thị giá trị danh nghĩa và giới hạn dung sai bên cạnh ô nhập để Nhân_viên_QC tham chiếu
5. THE Hệ_thống_QMS SHALL yêu cầu hoàn thành bước hiện tại (điền đủ trường bắt buộc) trước khi cho phép chuyển sang bước tiếp theo
6. THE Hệ_thống_QMS SHALL cho phép lưu nháp (draft) Inspection_Execution để Nhân_viên_QC tiếp tục sau
7. WHEN Nhân_viên_QC hoàn thành tất cả bước bắt buộc, THE Hệ_thống_QMS SHALL cho phép submit kết quả kiểm tra để đánh giá

### Requirement 7: Đánh giá Kết quả Kiểm tra (Step Result Evaluation)

**User Story:** Là Hệ_thống_QMS, tôi muốn tự động đánh giá kết quả pass/fail cho mỗi bước và toàn bộ phiên kiểm tra dựa trên giá trị nhập và cấu hình dung sai, để giảm thiểu đánh giá chủ quan.

#### Acceptance Criteria

1. WHEN Field_Type là measurement, THE Hệ_thống_QMS SHALL tự động đánh giá: PASS nếu giá trị thực tế nằm trong khoảng [giới hạn dưới, giới hạn trên], FAIL nếu nằm ngoài khoảng
2. WHEN Field_Type là number với min/max, THE Hệ_thống_QMS SHALL tự động đánh giá: PASS nếu giá trị nằm trong khoảng [min_value, max_value], FAIL nếu nằm ngoài khoảng
3. WHEN Field_Type là boolean, THE Hệ_thống_QMS SHALL sử dụng trực tiếp giá trị đạt/không đạt do Nhân_viên_QC chọn làm kết quả
4. THE Hệ_thống_QMS SHALL đánh giá kết quả Inspection_Step: PASS nếu tất cả Step_Field bắt buộc đều PASS, FAIL nếu có bất kỳ Step_Field bắt buộc nào FAIL
5. THE Hệ_thống_QMS SHALL đánh giá kết quả tổng thể Inspection_Execution: PASS nếu tất cả Inspection_Step bắt buộc đều PASS, FAIL nếu có bất kỳ Inspection_Step bắt buộc nào FAIL
6. WHEN đánh giá hoàn tất, THE Hệ_thống_QMS SHALL lưu kết quả chi tiết cho từng Step_Field, từng Inspection_Step, và kết quả tổng thể
7. THE Giao_diện_Kiểm_tra SHALL hiển thị trực quan kết quả đánh giá: màu xanh cho PASS, màu đỏ cho FAIL, kèm giá trị thực tế so với giới hạn cho phép

### Requirement 8: Quy trình Phê duyệt Kết quả Kiểm tra

**User Story:** Là Quản_lý_QC, tôi muốn phê duyệt hoặc từ chối kết quả kiểm tra trước khi chính thức ghi nhận, để đảm bảo kết quả kiểm tra được xác nhận bởi người có thẩm quyền.

#### Acceptance Criteria

1. WHEN Inspection_Step có cờ requires_approval là true, THE Hệ_thống_QMS SHALL yêu cầu Quản_lý_QC phê duyệt kết quả bước đó trước khi chuyển sang bước tiếp theo
2. WHEN Nhân_viên_QC submit kết quả kiểm tra, THE Hệ_thống_QMS SHALL gửi thông báo cho Quản_lý_QC yêu cầu phê duyệt
3. THE Giao_diện_Cấu_hình SHALL cho phép Quản_lý_QC xem chi tiết kết quả kiểm tra và chọn: Phê duyệt (approve), Từ chối (reject kèm lý do), hoặc Yêu cầu kiểm tra lại (re-inspect)
4. WHEN Quản_lý_QC từ chối kết quả, THE Hệ_thống_QMS SHALL gửi thông báo cho Nhân_viên_QC kèm lý do từ chối
5. WHEN Quản_lý_QC yêu cầu kiểm tra lại, THE Hệ_thống_QMS SHALL cho phép Nhân_viên_QC nhập lại dữ liệu cho bước bị từ chối mà không ảnh hưởng đến các bước đã phê duyệt
6. THE Hệ_thống_QMS SHALL quản lý trạng thái Inspection_Execution theo luồng: draft → in_progress → pending_approval → approved/rejected
7. WHEN toàn bộ Inspection_Execution được phê duyệt, THE Hệ_thống_QMS SHALL ghi nhận kết quả chính thức và cập nhật trạng thái QC của lô hàng/sản phẩm liên quan

### Requirement 9: Tích hợp với WMS Routing Steps

**User Story:** Là Quản_lý_Sản_xuất, tôi muốn liên kết bước kiểm tra QC với công đoạn sản xuất (routing step), để kiểm tra chất lượng được tự động trigger tại đúng thời điểm trong quy trình sản xuất.

#### Acceptance Criteria

1. THE Hệ_thống_QMS SHALL cho phép liên kết Inspection_Template với Routing_Step thông qua trường qc_stage trên routing step
2. WHEN Nhân_viên_Sản_xuất hoàn thành một Routing_Step có liên kết QC stage, THE Hệ_thống_QMS SHALL tự động tạo Inspection_Execution mới với template phù hợp cho sản phẩm đang sản xuất
3. WHILE Inspection_Execution chưa hoàn thành (chưa approved), THE Hệ_thống_QMS SHALL chặn việc chuyển sang Routing_Step tiếp theo trong Lệnh Sản Xuất
4. WHEN Inspection_Execution được approved với kết quả PASS, THE Hệ_thống_QMS SHALL tự động cho phép chuyển sang Routing_Step tiếp theo
5. WHEN Inspection_Execution được approved với kết quả FAIL, THE Hệ_thống_QMS SHALL cập nhật số lượng lỗi trên Lệnh Sản Xuất và cho phép Quản_lý_Sản_xuất quyết định: tiếp tục sản xuất (với số lượng giảm) hoặc dừng lệnh sản xuất
6. THE Hệ_thống_QMS SHALL hỗ trợ trigger kiểm tra cho cả ba giai đoạn: IQC (khi nhập kho nguyên vật liệu), PQC (tại công đoạn sản xuất), FQC (khi nhập kho thành phẩm)

### Requirement 10: Xem trước và Kiểm thử Template

**User Story:** Là Quản_lý_QC, tôi muốn xem trước giao diện kiểm tra của template sau khi cấu hình, để đảm bảo form hiển thị đúng trước khi đưa vào sử dụng thực tế.

#### Acceptance Criteria

1. THE Giao_diện_Cấu_hình SHALL cung cấp chức năng Xem trước (Preview) hiển thị form kiểm tra giống như khi Nhân_viên_QC thực hiện kiểm tra thực tế
2. WHEN xem trước, THE Giao_diện_Cấu_hình SHALL render đầy đủ: danh sách bước theo thứ tự, các trường nhập liệu đúng kiểu, hiển thị dung sai cho measurement, dropdown cho select, và nhãn cho boolean
3. THE Giao_diện_Cấu_hình SHALL cho phép nhập dữ liệu thử trong chế độ xem trước để kiểm tra logic đánh giá pass/fail hoạt động đúng
4. THE Giao_diện_Cấu_hình SHALL hiển thị xem trước trong modal hoặc panel riêng mà không ảnh hưởng đến dữ liệu cấu hình đang chỉnh sửa

### Requirement 11: Lịch sử và Báo cáo Kiểm tra

**User Story:** Là Quản_lý_QC, tôi muốn xem lịch sử kiểm tra và thống kê kết quả theo template/sản phẩm, để phân tích xu hướng chất lượng và cải tiến quy trình.

#### Acceptance Criteria

1. THE Hệ_thống_QMS SHALL lưu trữ toàn bộ lịch sử Inspection_Execution bao gồm: thời gian thực hiện, người kiểm tra, người phê duyệt, kết quả từng bước, và kết quả tổng thể
2. THE Giao_diện_Cấu_hình SHALL cho phép tra cứu lịch sử kiểm tra theo: sản phẩm, template, khoảng thời gian, kết quả (pass/fail), và người kiểm tra
3. THE Hệ_thống_QMS SHALL tính toán thống kê: tỷ lệ pass/fail theo template, tỷ lệ pass/fail theo Step_Field (xác định trường nào hay fail nhất), và xu hướng theo thời gian
4. WHEN Quản_lý_QC xem thống kê theo Step_Field, THE Hệ_thống_QMS SHALL hiển thị biểu đồ Pareto xác định top 5 trường có tỷ lệ fail cao nhất
5. THE Hệ_thống_QMS SHALL cho phép xuất báo cáo kiểm tra dưới dạng PDF và Excel

