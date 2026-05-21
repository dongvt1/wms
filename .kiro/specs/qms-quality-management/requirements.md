# Requirements Document

## Introduction

Module QMS (Quality Management System) cung cấp hệ thống quản lý chất lượng toàn diện cho Warehouse Management System, bao gồm kiểm soát chất lượng đầu vào (IQC), kiểm soát chất lượng trong sản xuất (PQC), kiểm soát chất lượng thành phẩm (FQC), quản lý sự không phù hợp (NCR), và báo cáo phân tích chất lượng. Hệ thống được thiết kế cho doanh nghiệp vừa và nhỏ tại Việt Nam, tích hợp chặt chẽ với module kho và sản xuất.

## Glossary

- **Hệ_thống_QMS**: Module quản lý chất lượng trong Warehouse Management System
- **Checklist_Template**: Mẫu bộ tiêu chí kiểm tra tái sử dụng cho IQC/PQC/FQC
- **IQC_Module**: Module kiểm soát chất lượng đầu vào (Incoming Quality Control)
- **PQC_Module**: Module kiểm soát chất lượng sản xuất (Process Quality Control)
- **FQC_Module**: Module kiểm soát chất lượng thành phẩm trước xuất kho (Final Quality Control)
- **QC_Stage**: Công đoạn kiểm tra với các tham số cấu hình động
- **QC_Session**: Phiên kiểm tra thực tế cho một công đoạn trong một Work Order
- **QC_Review**: Bản tổng hợp và phê duyệt toàn bộ phiên kiểm tra của một Work Order
- **NCR**: Báo cáo sự không phù hợp (Non-Conformance Report)
- **Work_Order**: Lệnh sản xuất từ module kế hoạch sản xuất
- **Nhân_viên_QC**: Người thực hiện kiểm tra chất lượng
- **Quản_lý_QC**: Người phê duyệt kết quả kiểm tra và NCR
- **Stock_Transaction**: Giao dịch nhập/xuất kho từ module kho

## Requirements

### Requirement 1: Quản lý Checklist Template

**User Story:** Là Quản_lý_QC, tôi muốn định nghĩa các mẫu bộ tiêu chí kiểm tra tái sử dụng, để nhân viên QC có thể áp dụng nhanh khi tạo phiếu kiểm tra.

#### Tiêu chí chấp nhận

1. THE Hệ_thống_QMS SHALL cho phép tạo Checklist_Template với các thông tin: mã mẫu (tự sinh), tên mẫu, loại kiểm tra (IQC/PQC/FQC), sản phẩm áp dụng (tùy chọn), và trạng thái (active/inactive)
2. THE Hệ_thống_QMS SHALL cho phép thêm nhiều tiêu chí vào Checklist_Template với các thuộc tính: tên tiêu chí, giá trị tiêu chuẩn, kiểu nhập (pass_fail/text/number/select), danh sách tùy chọn (khi kiểu nhập là select), và cờ bắt buộc
3. WHEN Nhân_viên_QC chọn một Checklist_Template khi tạo phiếu kiểm tra, THE Hệ_thống_QMS SHALL tự động tải toàn bộ danh sách tiêu chí từ mẫu vào phiếu kiểm tra
4. WHEN Checklist_Template có product_id là NULL, THE Hệ_thống_QMS SHALL hiển thị mẫu đó cho tất cả sản phẩm khi tạo phiếu kiểm tra
5. THE Hệ_thống_QMS SHALL cho phép lọc Checklist_Template theo loại kiểm tra và trạng thái

### Yêu cầu 2: Kiểm soát chất lượng đầu vào (IQC)

**User Story:** Là Nhân_viên_QC, tôi muốn tạo và xử lý phiếu kiểm tra chất lượng khi nhận nguyên liệu từ nhà cung cấp, để đảm bảo nguyên liệu đạt tiêu chuẩn trước khi nhập kho.

#### Tiêu chí chấp nhận

1. THE IQC_Module SHALL cho phép tạo phiếu kiểm tra IQC với các thông tin: sản phẩm, nhà cung cấp, phiếu nhập kho liên kết, mẫu checklist, và số lượng nhận
2. WHEN phiếu IQC được tạo, THE Hệ_thống_QMS SHALL tự động sinh mã phiếu theo định dạng IQCyyyyMMddNNN
3. THE IQC_Module SHALL cho phép Nhân_viên_QC điền kết quả từng tiêu chí: giá trị thực đo, kết quả (đạt/không đạt/N/A), và ghi chú
4. THE IQC_Module SHALL quản lý vòng đời phiếu IQC theo trạng thái: draft → in_progress → pending_approval → passed/failed/conditional
5. WHEN phiếu IQC chuyển sang trạng thái pending_approval, THE Hệ_thống_QMS SHALL yêu cầu Quản_lý_QC phê duyệt trước khi chuyển sang trạng thái cuối cùng
6. THE IQC_Module SHALL hiển thị thống kê tổng hợp: tổng phiếu, đang kiểm tra, đạt, không đạt, có điều kiện

### Yêu cầu 3: Kiểm soát chất lượng sản xuất (PQC)

**User Story:** Là Nhân_viên_QC, tôi muốn kiểm tra chất lượng trong và sau quá trình sản xuất, để phát hiện sớm lỗi và giảm thiểu phế phẩm.

#### Tiêu chí chấp nhận

1. THE PQC_Module SHALL cho phép tạo phiếu kiểm tra PQC với các thông tin: lệnh sản xuất, thành phẩm, công đoạn (tùy chọn), mẫu checklist, và số lượng kiểm tra
2. WHEN phiếu PQC được tạo, THE Hệ_thống_QMS SHALL tự động sinh mã phiếu theo định dạng PQCyyyyMMddNNN
3. THE PQC_Module SHALL cho phép Nhân_viên_QC điền kết quả từng tiêu chí tương tự IQC: giá trị thực đo, kết quả (đạt/không đạt/N/A), và ghi chú
4. THE PQC_Module SHALL quản lý vòng đời phiếu PQC theo trạng thái: draft → in_progress → pending_approval → passed/failed
5. WHEN phiếu PQC chuyển sang trạng thái pending_approval, THE Hệ_thống_QMS SHALL yêu cầu Quản_lý_QC phê duyệt trước khi chuyển sang trạng thái cuối cùng

### Yêu cầu 4: Cấu hình công đoạn kiểm tra (QC Stage)

**User Story:** Là Quản_lý_QC, tôi muốn định nghĩa các công đoạn kiểm tra với tham số cấu hình động, để kiểm soát chất lượng chi tiết theo từng bước sản xuất.

#### Tiêu chí chấp nhận

1. THE Hệ_thống_QMS SHALL cho phép tạo QC_Stage với các thông tin: mã công đoạn, tên công đoạn, mô tả, thứ tự sắp xếp, và trạng thái (active/inactive)
2. THE Hệ_thống_QMS SHALL cho phép thêm nhiều tham số vào QC_Stage với các thuộc tính: tên tham số, mã tham số, kiểu nhập (text/number/pass_fail/select/date/list), đơn vị, giá trị mặc định, giá trị tối thiểu, giá trị tối đa, danh sách tùy chọn JSON, cờ bắt buộc, và thứ tự hiển thị
3. WHEN kiểu nhập của tham số là number, THE Hệ_thống_QMS SHALL cho phép cấu hình giá trị tối thiểu và tối đa để xác định phạm vi chấp nhận được

### Yêu cầu 5: Phiên kiểm tra công đoạn (QC Session)

**User Story:** Là Nhân_viên_QC, tôi muốn thực hiện kiểm tra theo từng công đoạn cho mỗi Work Order, để ghi nhận kết quả chi tiết tại từng bước sản xuất.

#### Tiêu chí chấp nhận

1. THE Hệ_thống_QMS SHALL cho phép tạo QC_Session liên kết với một Work_Order và một QC_Stage cụ thể
2. WHEN QC_Session được tạo, THE Hệ_thống_QMS SHALL tự động sinh mã phiên theo định dạng SKyyyyMMddNNN
3. THE Hệ_thống_QMS SHALL cho phép Nhân_viên_QC nhập giá trị cho từng tham số của công đoạn, bao gồm giá trị thực đo và kết quả (passed/failed/na)
4. WHEN kiểu nhập của tham số là list, THE Hệ_thống_QMS SHALL cho phép nhập nhiều lần đo với số thứ tự, giá trị đo, và kết quả riêng cho mỗi lần
5. THE Hệ_thống_QMS SHALL quản lý trạng thái QC_Session: draft → completed

### Yêu cầu 6: Review và phê duyệt tổng hợp (QC Review)

**User Story:** Là Quản_lý_QC, tôi muốn xem tổng hợp kết quả kiểm tra của toàn bộ Work Order và phê duyệt, để đưa ra quyết định chất lượng cuối cùng cho lô sản xuất.

#### Tiêu chí chấp nhận

1. THE Hệ_thống_QMS SHALL cho phép tạo QC_Review cho mỗi Work_Order với thông tin tổng hợp: tổng số phiên kiểm tra, số phiên đạt, số phiên không đạt, và kết quả tổng thể (passed/failed/conditional)
2. WHEN QC_Review được tạo, THE Hệ_thống_QMS SHALL tự động sinh mã review theo định dạng RVyyyyMMddNNN
3. THE Hệ_thống_QMS SHALL quản lý vòng đời QC_Review theo trạng thái: draft → pending_approval → approved/rejected
4. WHEN Quản_lý_QC từ chối QC_Review, THE Hệ_thống_QMS SHALL yêu cầu nhập lý do từ chối
5. THE Hệ_thống_QMS SHALL đảm bảo mỗi Work_Order chỉ có tối đa một QC_Review

### Yêu cầu 7: Kiểm soát chất lượng thành phẩm (FQC)

**User Story:** Là Nhân_viên_QC, tôi muốn kiểm tra chất lượng thành phẩm trước khi xuất kho giao cho khách hàng, để đảm bảo sản phẩm đạt tiêu chuẩn khi đến tay khách hàng.

#### Tiêu chí chấp nhận

1. THE FQC_Module SHALL cho phép tạo phiếu kiểm tra FQC với các thông tin: đơn hàng xuất, sản phẩm, mẫu checklist, số lượng kiểm tra, và khách hàng
2. WHEN phiếu FQC được tạo, THE Hệ_thống_QMS SHALL tự động sinh mã phiếu theo định dạng FQCyyyyMMddNNN
3. THE FQC_Module SHALL cho phép Nhân_viên_QC điền kết quả từng tiêu chí: giá trị thực đo, kết quả (đạt/không đạt/N/A), và ghi chú
4. THE FQC_Module SHALL quản lý vòng đời phiếu FQC theo trạng thái: draft → in_progress → pending_approval → passed/failed
5. WHILE phiếu FQC chưa đạt trạng thái passed, THE Hệ_thống_QMS SHALL chặn việc xác nhận xuất kho cho đơn hàng liên kết

### Yêu cầu 8: Tích hợp chặn kho khi IQC không đạt

**User Story:** Là Quản_lý_QC, tôi muốn hệ thống tự động chặn nhập kho khi nguyên liệu không đạt kiểm tra IQC, để ngăn nguyên liệu kém chất lượng vào dây chuyền sản xuất.

#### Tiêu chí chấp nhận

1. WHEN phiếu IQC có kết quả failed, THE Hệ_thống_QMS SHALL tự động đánh dấu Stock_Transaction liên kết ở trạng thái blocked
2. WHEN phiếu IQC có kết quả conditional, THE Hệ_thống_QMS SHALL đánh dấu Stock_Transaction liên kết ở trạng thái conditional_hold và yêu cầu Quản_lý_QC xác nhận trước khi cho phép sử dụng
3. WHEN phiếu IQC có kết quả passed, THE Hệ_thống_QMS SHALL tự động chuyển Stock_Transaction liên kết sang trạng thái available
4. IF Stock_Transaction bị blocked do IQC failed, THEN THE Hệ_thống_QMS SHALL không cho phép sử dụng nguyên liệu đó trong Work_Order cho đến khi có NCR xử lý

### Yêu cầu 9: Quản lý sự không phù hợp (NCR)

**User Story:** Là Quản_lý_QC, tôi muốn tạo và theo dõi báo cáo sự không phù hợp khi phát hiện lỗi chất lượng, để có quy trình xử lý rõ ràng và truy vết được nguyên nhân.

#### Tiêu chí chấp nhận

1. THE Hệ_thống_QMS SHALL cho phép tạo NCR với các thông tin: nguồn phát hiện (IQC/PQC/FQC/khác), phiếu kiểm tra liên kết, sản phẩm, mô tả lỗi, mức độ nghiêm trọng (critical/major/minor), số lượng lỗi, và hành động đề xuất (trả nhà cung cấp/sửa chữa/hủy/chấp nhận có điều kiện)
2. WHEN NCR được tạo, THE Hệ_thống_QMS SHALL tự động sinh mã NCR theo định dạng NCRyyyyMMddNNN
3. THE Hệ_thống_QMS SHALL quản lý vòng đời NCR theo trạng thái: open → investigating → action_taken → verified → closed
4. WHEN NCR chuyển sang trạng thái closed, THE Hệ_thống_QMS SHALL yêu cầu Quản_lý_QC xác nhận hành động khắc phục đã hoàn tất
5. THE Hệ_thống_QMS SHALL cho phép đính kèm bằng chứng (ảnh, tài liệu) vào NCR
6. THE Hệ_thống_QMS SHALL liên kết NCR với nhà cung cấp (khi nguồn là IQC) để theo dõi lịch sử chất lượng nhà cung cấp

### Yêu cầu 10: Đính kèm bằng chứng kiểm tra

**User Story:** Là Nhân_viên_QC, tôi muốn đính kèm ảnh và tài liệu vào phiếu kiểm tra, để lưu trữ bằng chứng trực quan cho kết quả kiểm tra.

#### Tiêu chí chấp nhận

1. THE Hệ_thống_QMS SHALL cho phép đính kèm tệp (ảnh, PDF, tài liệu) vào phiếu IQC, PQC, FQC, và NCR
2. THE Hệ_thống_QMS SHALL hỗ trợ các định dạng tệp: JPG, PNG, PDF, DOCX, XLSX với dung lượng tối đa 10MB mỗi tệp
3. THE Hệ_thống_QMS SHALL cho phép đính kèm tối đa 10 tệp cho mỗi phiếu kiểm tra
4. WHEN Nhân_viên_QC tải ảnh lên từ thiết bị di động, THE Hệ_thống_QMS SHALL tự động nén ảnh nếu dung lượng vượt quá 10MB

### Yêu cầu 11: Thông báo phê duyệt

**User Story:** Là Quản_lý_QC, tôi muốn nhận thông báo khi có phiếu kiểm tra cần phê duyệt, để xử lý kịp thời và không làm chậm quy trình sản xuất.

#### Tiêu chí chấp nhận

1. WHEN phiếu IQC, PQC, FQC, hoặc QC_Review chuyển sang trạng thái pending_approval, THE Hệ_thống_QMS SHALL gửi thông báo đến Quản_lý_QC được chỉ định
2. THE Hệ_thống_QMS SHALL hiển thị thông báo trong hệ thống (in-app notification) với số lượng phiếu chờ phê duyệt
3. WHEN phiếu kiểm tra ở trạng thái pending_approval quá 24 giờ, THE Hệ_thống_QMS SHALL gửi thông báo nhắc nhở đến Quản_lý_QC
4. WHEN Quản_lý_QC phê duyệt hoặc từ chối phiếu kiểm tra, THE Hệ_thống_QMS SHALL gửi thông báo kết quả đến Nhân_viên_QC đã tạo phiếu

### Yêu cầu 12: Báo cáo và phân tích chất lượng

**User Story:** Là Quản_lý_QC, tôi muốn xem báo cáo tổng hợp và xu hướng chất lượng, để đưa ra quyết định cải tiến quy trình dựa trên dữ liệu.

#### Tiêu chí chấp nhận

1. THE Hệ_thống_QMS SHALL cung cấp dashboard hiển thị: tỷ lệ đạt/không đạt theo loại kiểm tra (IQC/PQC/FQC), số NCR mở, và xu hướng chất lượng theo thời gian
2. THE Hệ_thống_QMS SHALL cho phép lọc báo cáo theo khoảng thời gian, loại kiểm tra, sản phẩm, và nhà cung cấp
3. THE Hệ_thống_QMS SHALL hiển thị biểu đồ xu hướng tỷ lệ lỗi theo tuần và tháng
4. THE Hệ_thống_QMS SHALL cung cấp báo cáo chất lượng nhà cung cấp: tỷ lệ IQC đạt, số NCR, và xếp hạng nhà cung cấp
5. THE Hệ_thống_QMS SHALL cho phép xuất báo cáo ra định dạng PDF và Excel
6. THE Hệ_thống_QMS SHALL hiển thị top 5 tiêu chí kiểm tra có tỷ lệ lỗi cao nhất (Pareto analysis)

### Yêu cầu 13: Tính toán thống kê QC Review

**User Story:** Là Quản_lý_QC, tôi muốn hệ thống tự động tính toán thống kê tổng hợp cho QC Review, để có cái nhìn chính xác về chất lượng tổng thể của Work Order.

#### Tiêu chí chấp nhận

1. WHEN QC_Review được tạo hoặc cập nhật, THE Hệ_thống_QMS SHALL tự động tính toán: tổng số phiên kiểm tra, số phiên đạt, số phiên không đạt từ tất cả QC_Session của Work_Order
2. WHEN tất cả QC_Session của Work_Order có kết quả passed, THE Hệ_thống_QMS SHALL đề xuất overall_result là passed
3. WHEN có ít nhất một QC_Session có kết quả failed, THE Hệ_thống_QMS SHALL đề xuất overall_result là failed
4. THE Hệ_thống_QMS SHALL cho phép Quản_lý_QC ghi đè kết quả đề xuất với lý do giải thích

### Yêu cầu 14: Phân quyền module QMS

**User Story:** Là quản trị viên hệ thống, tôi muốn phân quyền chi tiết cho module QMS, để đảm bảo chỉ người có thẩm quyền mới thực hiện được các thao tác quan trọng.

#### Tiêu chí chấp nhận

1. THE Hệ_thống_QMS SHALL phân quyền theo vai trò: Nhân_viên_QC (tạo/sửa phiếu kiểm tra), Quản_lý_QC (phê duyệt/từ chối), và Admin (cấu hình template/stage)
2. WHILE người dùng không có quyền Quản_lý_QC, THE Hệ_thống_QMS SHALL ẩn các nút phê duyệt và từ chối trên giao diện
3. THE Hệ_thống_QMS SHALL ghi nhận lịch sử thao tác (audit log) cho tất cả hành động thay đổi trạng thái phiếu kiểm tra
