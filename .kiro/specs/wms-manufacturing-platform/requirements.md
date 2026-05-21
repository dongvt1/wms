# Requirements Document

## Introduction

Nền tảng WMS Manufacturing Platform là hệ thống tích hợp dành cho các công ty sản xuất vừa và nhỏ, kết hợp ba module chính: Lập kế hoạch sản xuất (Production Planning), Quản lý chất lượng (QMS - Quality Management System), và Quản lý kho (Warehouse Management). Hệ thống giúp doanh nghiệp quản lý toàn bộ quy trình từ lập kế hoạch sản xuất, theo dõi tiến độ, kiểm soát chất lượng, đến quản lý nguyên vật liệu và thành phẩm trong kho.

## Glossary

- **Hệ_thống_WMS**: Nền tảng quản lý kho tích hợp cho sản xuất
- **Module_Kế_hoạch**: Module lập kế hoạch và theo dõi sản xuất
- **Module_QMS**: Module quản lý chất lượng (đã có nền tảng IQC/PQC/FQC)
- **Module_Kho**: Module quản lý kho nguyên vật liệu và thành phẩm
- **Lệnh_Sản_Xuất**: Đơn vị công việc sản xuất (Work Order) chứa thông tin sản phẩm, số lượng, thời hạn
- **BOM**: Bill of Materials - Danh mục nguyên vật liệu cần thiết để sản xuất một sản phẩm
- **Công_đoạn**: Một bước trong quy trình sản xuất (routing step)
- **Quản_lý_Sản_xuất**: Người có quyền lập kế hoạch và quản lý lệnh sản xuất
- **Thủ_kho**: Người có quyền quản lý nhập/xuất kho và kiểm kê
- **Quản_lý_QC**: Người có quyền cấu hình và phê duyệt kiểm tra chất lượng
- **Nhân_viên_Sản_xuất**: Người thực hiện sản xuất và báo cáo tiến độ
- **Phiếu_Nhập_Kho**: Chứng từ ghi nhận nhập nguyên vật liệu hoặc thành phẩm vào kho
- **Phiếu_Xuất_Kho**: Chứng từ ghi nhận xuất nguyên vật liệu cho sản xuất hoặc xuất thành phẩm
- **Vị_trí_Kho**: Vị trí lưu trữ cụ thể trong kho (kệ, ô, tầng)
- **Lot**: Lô hàng - nhóm sản phẩm/nguyên vật liệu cùng đợt nhập hoặc sản xuất
- **Kế_hoạch_Sản_xuất**: Bản kế hoạch tổng thể phân bổ lệnh sản xuất theo thời gian và nguồn lực
- **Giao_diện_Kế_hoạch**: Màn hình frontend cho phép Quản_lý_Sản_xuất thao tác lập kế hoạch
- **Giao_diện_Kho**: Màn hình frontend cho phép Thủ_kho thao tác quản lý kho
- **Vật_tư_Thay_thế**: Nguyên vật liệu có thể sử dụng thay thế cho một nguyên vật liệu chính trong BOM khi nguyên vật liệu chính không khả dụng
- **Phiên_bản_BOM**: Một version cụ thể của BOM cho sản phẩm, cho phép lựa chọn khi lập kế hoạch thay vì chỉ sử dụng BOM active mặc định

## Requirements

### Requirement 1: Quản lý BOM (Bill of Materials)

**User Story:** Là Quản_lý_Sản_xuất, tôi muốn quản lý danh mục nguyên vật liệu (BOM) cho từng sản phẩm, để biết chính xác cần những gì để sản xuất và tính toán nhu cầu vật tư.

#### Acceptance Criteria

1. THE Module_Kế_hoạch SHALL cho phép tạo BOM với các trường: mã sản phẩm, tên sản phẩm, phiên bản BOM, trạng thái (draft/active/obsolete), và ngày hiệu lực
2. THE Module_Kế_hoạch SHALL cho phép thêm nhiều dòng nguyên vật liệu vào BOM với các trường: mã vật tư, tên vật tư, số lượng định mức, đơn vị tính, tỷ lệ hao hụt cho phép, và ghi chú
3. WHEN Quản_lý_Sản_xuất kích hoạt BOM mới cho một sản phẩm, THE Module_Kế_hoạch SHALL tự động chuyển BOM cũ sang trạng thái obsolete
4. THE Module_Kế_hoạch SHALL đảm bảo mỗi sản phẩm chỉ có một BOM ở trạng thái active tại một thời điểm
5. IF Quản_lý_Sản_xuất xóa BOM đang được tham chiếu bởi Lệnh_Sản_Xuất, THEN THE Module_Kế_hoạch SHALL từ chối xóa và hiển thị thông báo lỗi
6. THE Module_Kế_hoạch SHALL tính toán tổng nhu cầu nguyên vật liệu dựa trên BOM và số lượng sản xuất yêu cầu
7. THE Module_Kế_hoạch SHALL cho phép định nghĩa danh sách Vật_tư_Thay_thế cho mỗi dòng nguyên vật liệu trong BOM, bao gồm: mã vật tư thay thế, tỷ lệ quy đổi (conversion ratio), và mức ưu tiên thay thế
8. THE Module_Kế_hoạch SHALL cho phép một vật tư thay thế được liên kết với nhiều dòng BOM khác nhau và một dòng BOM có thể có nhiều vật tư thay thế
9. WHEN Quản_lý_Sản_xuất thêm Vật_tư_Thay_thế, THE Module_Kế_hoạch SHALL yêu cầu vật tư thay thế phải có cùng đơn vị tính hoặc có tỷ lệ quy đổi hợp lệ so với vật tư chính

### Requirement 2: Quản lý Lệnh Sản Xuất (Work Order)

**User Story:** Là Quản_lý_Sản_xuất, tôi muốn tạo và quản lý các lệnh sản xuất, để theo dõi tiến độ và phân bổ nguồn lực sản xuất hiệu quả.

#### Acceptance Criteria

1. THE Module_Kế_hoạch SHALL cho phép tạo Lệnh_Sản_Xuất với các trường: mã lệnh (tự sinh theo định dạng WOyyyyMMddNNN), sản phẩm, số lượng yêu cầu, ngày bắt đầu dự kiến, ngày hoàn thành dự kiến, mức ưu tiên (low/medium/high/urgent), và ghi chú
2. THE Module_Kế_hoạch SHALL tự động liên kết BOM active của sản phẩm khi tạo Lệnh_Sản_Xuất; WHEN sản phẩm có nhiều Phiên_bản_BOM, THE Module_Kế_hoạch SHALL cho phép Quản_lý_Sản_xuất lựa chọn phiên bản BOM cụ thể thay vì chỉ sử dụng BOM active mặc định
3. WHEN Quản_lý_Sản_xuất tạo Lệnh_Sản_Xuất, THE Module_Kế_hoạch SHALL tính toán nhu cầu nguyên vật liệu dựa trên BOM và số lượng yêu cầu (bao gồm tỷ lệ hao hụt)
4. THE Module_Kế_hoạch SHALL quản lý trạng thái Lệnh_Sản_Xuất theo luồng: draft → planned → in_progress → completed → closed
5. WHEN Quản_lý_Sản_xuất tạo Lệnh_Sản_Xuất cho một sản phẩm, THE Module_Kế_hoạch SHALL yêu cầu sản phẩm đó phải có cả BOM ở trạng thái active VÀ Routing ở trạng thái active; IF sản phẩm thiếu BOM hoặc Routing active, THEN THE Module_Kế_hoạch SHALL từ chối tạo và hiển thị thông báo lỗi chỉ rõ thành phần còn thiếu
6. WHEN Lệnh_Sản_Xuất chuyển sang trạng thái in_progress, THE Module_Kế_hoạch SHALL kiểm tra tồn kho nguyên vật liệu đủ theo BOM trước khi cho phép bắt đầu
7. IF tồn kho nguyên vật liệu không đủ, THEN THE Module_Kế_hoạch SHALL hiển thị danh sách vật tư thiếu kèm số lượng cần bổ sung
8. IF tồn kho nguyên vật liệu chính không đủ VÀ dòng BOM có định nghĩa Vật_tư_Thay_thế, THEN THE Module_Kế_hoạch SHALL hiển thị danh sách Vật_tư_Thay_thế khả dụng kèm số lượng tồn kho và cho phép Quản_lý_Sản_xuất lựa chọn vật tư thay thế
9. WHEN Quản_lý_Sản_xuất chọn Vật_tư_Thay_thế cho Lệnh_Sản_Xuất, THE Module_Kế_hoạch SHALL ghi nhận vật tư thay thế đã chọn và tính toán lại số lượng cần xuất dựa trên tỷ lệ quy đổi
10. WHEN Lệnh_Sản_Xuất hoàn thành, THE Module_Kế_hoạch SHALL ghi nhận số lượng thực tế sản xuất được, số lượng đạt chất lượng, và số lượng lỗi

### Requirement 3: Lập Kế hoạch Sản xuất

**User Story:** Là Quản_lý_Sản_xuất, tôi muốn lập kế hoạch sản xuất theo tuần/tháng với giao diện trực quan, để phân bổ công việc hợp lý và đảm bảo giao hàng đúng hạn.

#### Acceptance Criteria

1. THE Giao_diện_Kế_hoạch SHALL hiển thị Kế_hoạch_Sản_xuất dạng Gantt chart hoặc calendar view với các Lệnh_Sản_Xuất được phân bổ theo thời gian
2. THE Giao_diện_Kế_hoạch SHALL cho phép kéo thả Lệnh_Sản_Xuất để thay đổi ngày bắt đầu và ngày kết thúc dự kiến
3. WHEN Quản_lý_Sản_xuất thay đổi lịch của Lệnh_Sản_Xuất, THE Module_Kế_hoạch SHALL kiểm tra xung đột về nguồn lực (cùng dây chuyền, cùng thời gian)
4. IF phát hiện xung đột nguồn lực, THEN THE Giao_diện_Kế_hoạch SHALL hiển thị cảnh báo và đề xuất khung thời gian thay thế
5. THE Giao_diện_Kế_hoạch SHALL cho phép lọc kế hoạch theo: sản phẩm, trạng thái, mức ưu tiên, và khoảng thời gian
6. THE Giao_diện_Kế_hoạch SHALL hiển thị tỷ lệ sử dụng năng lực sản xuất (capacity utilization) theo ngày/tuần
7. WHEN tổng công suất vượt quá năng lực sản xuất trong một ngày, THE Giao_diện_Kế_hoạch SHALL đánh dấu ngày đó bằng màu cảnh báo

### Requirement 4: Theo dõi Tiến độ Sản xuất

**User Story:** Là Quản_lý_Sản_xuất, tôi muốn theo dõi tiến độ sản xuất theo thời gian thực, để phát hiện sớm các vấn đề và điều chỉnh kế hoạch kịp thời.

#### Acceptance Criteria

1. THE Module_Kế_hoạch SHALL cho phép Nhân_viên_Sản_xuất báo cáo tiến độ theo từng Công_đoạn: số lượng hoàn thành, số lượng lỗi, thời gian thực hiện
2. WHEN Nhân_viên_Sản_xuất báo cáo tiến độ, THE Module_Kế_hoạch SHALL cập nhật phần trăm hoàn thành của Lệnh_Sản_Xuất dựa trên số lượng đã sản xuất so với số lượng yêu cầu
3. THE Giao_diện_Kế_hoạch SHALL hiển thị dashboard tiến độ với: số lệnh đang thực hiện, tỷ lệ hoàn thành trung bình, số lệnh trễ hạn, và biểu đồ tiến độ theo ngày
4. WHEN Lệnh_Sản_Xuất vượt quá ngày hoàn thành dự kiến mà chưa hoàn thành, THE Module_Kế_hoạch SHALL đánh dấu trạng thái trễ hạn (overdue) và gửi thông báo cho Quản_lý_Sản_xuất
5. THE Module_Kế_hoạch SHALL tính toán OEE (Overall Equipment Effectiveness) dựa trên: availability, performance, và quality rate
6. THE Giao_diện_Kế_hoạch SHALL hiển thị biểu đồ so sánh kế hoạch và thực tế cho mỗi Lệnh_Sản_Xuất

### Requirement 5: Quản lý Công đoạn Sản xuất (Routing)

**User Story:** Là Quản_lý_Sản_xuất, tôi muốn định nghĩa quy trình sản xuất (routing) cho từng sản phẩm, để chuẩn hóa các bước sản xuất và theo dõi tiến độ từng công đoạn.

#### Acceptance Criteria

1. THE Module_Kế_hoạch SHALL cho phép tạo quy trình sản xuất (routing) cho mỗi sản phẩm với danh sách các Công_đoạn theo thứ tự
2. THE Module_Kế_hoạch SHALL lưu trữ cho mỗi Công_đoạn: tên công đoạn, mô tả, thời gian chuẩn (cycle time), dây chuyền/máy thực hiện, và yêu cầu kiểm tra chất lượng (liên kết QMS stage)
3. WHEN Lệnh_Sản_Xuất bắt đầu, THE Module_Kế_hoạch SHALL tạo danh sách công đoạn cần thực hiện dựa trên routing của sản phẩm
4. THE Module_Kế_hoạch SHALL cho phép Nhân_viên_Sản_xuất đánh dấu hoàn thành từng Công_đoạn và chuyển sang công đoạn tiếp theo
5. IF Công_đoạn có yêu cầu kiểm tra chất lượng, THEN THE Module_Kế_hoạch SHALL yêu cầu hoàn thành kiểm tra QC trước khi cho phép chuyển sang công đoạn tiếp theo
6. THE Module_Kế_hoạch SHALL ghi nhận thời gian bắt đầu và kết thúc thực tế của mỗi Công_đoạn để phân tích hiệu suất

### Requirement 6: Quản lý Nhập Kho

**User Story:** Là Thủ_kho, tôi muốn quản lý việc nhập kho nguyên vật liệu và thành phẩm, để ghi nhận chính xác số lượng và vị trí lưu trữ.

#### Acceptance Criteria

1. THE Module_Kho SHALL cho phép tạo Phiếu_Nhập_Kho với các trường: mã phiếu (tự sinh theo định dạng GRNyyyyMMddNNN), loại nhập (mua hàng/sản xuất/trả hàng/điều chuyển), nhà cung cấp (nếu mua hàng), Lệnh_Sản_Xuất liên kết (nếu nhập thành phẩm), ngày nhập, và ghi chú
2. THE Module_Kho SHALL cho phép thêm nhiều dòng chi tiết vào Phiếu_Nhập_Kho: mã vật tư/sản phẩm, số lượng, đơn vị, số Lot, ngày sản xuất, hạn sử dụng, và Vị_trí_Kho đích
3. WHEN Thủ_kho xác nhận Phiếu_Nhập_Kho, THE Module_Kho SHALL cập nhật tồn kho tại Vị_trí_Kho tương ứng trong một transaction duy nhất
4. IF nhập kho nguyên vật liệu từ nhà cung cấp, THEN THE Module_Kho SHALL tạo yêu cầu kiểm tra IQC cho lô hàng nhập; hàng hóa chỉ được ghi nhận nhập kho chính thức (cập nhật tồn kho) SAU KHI kết quả IQC là "pass"; WHILE chờ kết quả IQC, hàng hóa SHALL ở trạng thái "blocked" (chờ kiểm tra) và chưa khả dụng để xuất kho
5. WHEN nhập kho thành phẩm từ sản xuất, THE Module_Kho SHALL liên kết với Lệnh_Sản_Xuất và cập nhật số lượng hoàn thành
6. THE Module_Kho SHALL hỗ trợ quét mã vạch (barcode) hoặc QR code để nhập nhanh thông tin vật tư và vị trí kho

### Requirement 7: Quản lý Xuất Kho

**User Story:** Là Thủ_kho, tôi muốn quản lý việc xuất kho nguyên vật liệu cho sản xuất và xuất thành phẩm, để đảm bảo cấp phát đúng số lượng và truy xuất nguồn gốc.

#### Acceptance Criteria

1. THE Module_Kho SHALL cho phép tạo Phiếu_Xuất_Kho với các trường: mã phiếu (tự sinh theo định dạng GINyyyyMMddNNN), loại xuất (sản xuất/bán hàng/trả nhà cung cấp/điều chuyển), Lệnh_Sản_Xuất liên kết (nếu xuất cho sản xuất), ngày xuất, và ghi chú
2. WHEN xuất kho cho sản xuất, THE Module_Kho SHALL tự động đề xuất danh sách vật tư cần xuất dựa trên BOM của Lệnh_Sản_Xuất
3. THE Module_Kho SHALL áp dụng nguyên tắc FIFO (First In First Out) khi đề xuất Lot xuất kho, ưu tiên Lot có ngày nhập sớm nhất
4. WHEN Thủ_kho xác nhận Phiếu_Xuất_Kho, THE Module_Kho SHALL trừ tồn kho tại Vị_trí_Kho tương ứng trong một transaction duy nhất
5. IF số lượng xuất vượt quá tồn kho khả dụng, THEN THE Module_Kho SHALL từ chối xác nhận và hiển thị số lượng tồn kho hiện tại
6. THE Module_Kho SHALL không cho phép xuất nguyên vật liệu có trạng thái QC hàng hóa là "blocked" (chờ kiểm tra/xử lý) hoặc "conditional_hold" (giữ có điều kiện); trạng thái "blocked" là trạng thái QC gắn trên hàng hóa/Lot, khác với trạng thái "available" của Vị_trí_Kho (chỉ thể hiện vị trí có sẵn sàng nhận hàng hay không)
7. WHEN xuất thành phẩm cho bán hàng, THE Module_Kho SHALL kiểm tra kết quả FQC đạt yêu cầu trước khi cho phép xuất

### Requirement 8: Quản lý Tồn Kho và Vị trí

**User Story:** Là Thủ_kho, tôi muốn theo dõi tồn kho theo vị trí và lot, để biết chính xác hàng hóa ở đâu và quản lý hiệu quả không gian kho.

#### Acceptance Criteria

1. THE Module_Kho SHALL quản lý cấu trúc kho theo phân cấp: Kho → Khu vực → Kệ → Ô (tối đa 4 cấp)
2. THE Module_Kho SHALL hiển thị tồn kho theo nhiều chiều: theo vật tư (tổng hợp tất cả vị trí), theo vị trí (tất cả vật tư tại vị trí), và theo Lot
3. THE Giao_diện_Kho SHALL hiển thị bản đồ kho (warehouse map) dạng sơ đồ 2D cho phép xem nhanh tình trạng sử dụng từng vị trí
4. WHEN tồn kho của một vật tư giảm xuống dưới mức tồn kho tối thiểu (safety stock), THE Module_Kho SHALL gửi cảnh báo cho Thủ_kho và Quản_lý_Sản_xuất
5. THE Module_Kho SHALL hỗ trợ kiểm kê (stock count) theo vị trí hoặc theo vật tư, cho phép ghi nhận chênh lệch và điều chỉnh tồn kho
6. WHEN phát hiện chênh lệch kiểm kê, THE Module_Kho SHALL yêu cầu Thủ_kho nhập lý do điều chỉnh và ghi nhận lịch sử thay đổi
7. THE Module_Kho SHALL tính toán và hiển thị các chỉ số: tỷ lệ sử dụng kho, vòng quay tồn kho, và giá trị tồn kho

### Requirement 9: Truy xuất Nguồn gốc (Traceability)

**User Story:** Là Quản_lý_QC, tôi muốn truy xuất nguồn gốc sản phẩm từ thành phẩm ngược về nguyên vật liệu, để xử lý nhanh khi phát hiện lỗi chất lượng và đáp ứng yêu cầu audit.

#### Acceptance Criteria

1. THE Hệ_thống_WMS SHALL duy trì liên kết truy xuất: Thành phẩm (Lot) → Lệnh_Sản_Xuất → Nguyên vật liệu (Lot) → Nhà cung cấp
2. WHEN Quản_lý_QC tra cứu một Lot thành phẩm, THE Hệ_thống_WMS SHALL hiển thị toàn bộ chuỗi truy xuất bao gồm: nguyên vật liệu đã sử dụng, nhà cung cấp, ngày nhập, kết quả IQC, lệnh sản xuất, kết quả PQC/FQC
3. WHEN Quản_lý_QC tra cứu một Lot nguyên vật liệu, THE Hệ_thống_WMS SHALL hiển thị danh sách tất cả Lệnh_Sản_Xuất và thành phẩm đã sử dụng Lot đó
4. THE Hệ_thống_WMS SHALL cho phép tra cứu truy xuất theo: mã Lot, mã sản phẩm, mã lệnh sản xuất, hoặc mã phiếu nhập/xuất
5. IF phát hiện lỗi chất lượng trên một Lot nguyên vật liệu, THEN THE Hệ_thống_WMS SHALL xác định tất cả thành phẩm bị ảnh hưởng và hiển thị danh sách cần thu hồi

### Requirement 10: Tích hợp Module Sản xuất - Kho - QMS

**User Story:** Là Quản_lý_Sản_xuất, tôi muốn ba module hoạt động liên thông với nhau, để dữ liệu tự động đồng bộ và giảm thiểu nhập liệu thủ công.

#### Acceptance Criteria

1. WHEN Lệnh_Sản_Xuất chuyển sang trạng thái in_progress, THE Module_Kho SHALL tự động tạo Phiếu_Xuất_Kho nháp (draft) với danh sách nguyên vật liệu theo BOM; Phiếu_Xuất_Kho nháp này vẫn yêu cầu Thủ_kho xác nhận trước khi thực hiện xuất kho thực tế
2. WHEN Lệnh_Sản_Xuất hoàn thành, THE Module_Kho SHALL tự động tạo Phiếu_Nhập_Kho nháp cho thành phẩm với số lượng đạt chất lượng; Phiếu_Nhập_Kho nháp này vẫn yêu cầu Thủ_kho xác nhận trước khi ghi nhận nhập kho thực tế
3. WHEN kết quả PQC của một Công_đoạn là failed, THE Module_Kế_hoạch SHALL tự động cập nhật số lượng lỗi và tính lại tiến độ Lệnh_Sản_Xuất
4. THE Hệ_thống_WMS SHALL hiển thị dashboard tổng hợp với thông tin từ cả ba module: tiến độ sản xuất, tình trạng kho, và chỉ số chất lượng
5. WHEN tồn kho nguyên vật liệu không đủ cho Lệnh_Sản_Xuất đã lên kế hoạch, THE Hệ_thống_WMS SHALL gửi cảnh báo sớm cho Quản_lý_Sản_xuất với thời gian dự kiến hết hàng
6. THE Hệ_thống_WMS SHALL đảm bảo tính nhất quán dữ liệu giữa các module bằng cách sử dụng transaction khi thao tác liên module

### Requirement 11: Báo cáo và Phân tích Sản xuất

**User Story:** Là Quản_lý_Sản_xuất, tôi muốn xem báo cáo tổng hợp về hiệu suất sản xuất, để đưa ra quyết định cải tiến dựa trên dữ liệu.

#### Acceptance Criteria

1. THE Module_Kế_hoạch SHALL cung cấp báo cáo sản lượng theo: ngày, tuần, tháng, sản phẩm, và dây chuyền sản xuất
2. THE Module_Kế_hoạch SHALL tính toán và hiển thị các KPI: OEE, tỷ lệ hoàn thành đúng hạn (on-time delivery), tỷ lệ phế phẩm (scrap rate), và năng suất trung bình
3. THE Module_Kho SHALL cung cấp báo cáo tồn kho: giá trị tồn kho, vòng quay tồn kho, danh sách hàng tồn lâu (slow-moving), và hàng sắp hết hạn
4. THE Hệ_thống_WMS SHALL cho phép xuất báo cáo dưới dạng PDF và Excel
5. THE Giao_diện_Kế_hoạch SHALL hiển thị biểu đồ xu hướng (trend chart) cho các KPI chính theo thời gian
6. WHEN Quản_lý_Sản_xuất chọn khoảng thời gian báo cáo, THE Hệ_thống_WMS SHALL lọc dữ liệu chính xác theo khoảng thời gian đã chọn và hiển thị kết quả trong vòng 3 giây

### Requirement 12: Quản lý Danh mục Vật tư và Sản phẩm

**User Story:** Là Quản_lý_Sản_xuất, tôi muốn quản lý danh mục vật tư và sản phẩm tập trung, để đảm bảo thông tin nhất quán trên toàn hệ thống.

#### Acceptance Criteria

1. THE Hệ_thống_WMS SHALL quản lý danh mục vật tư với các trường: mã vật tư (duy nhất), tên, nhóm vật tư, đơn vị tính, mức tồn kho tối thiểu, mức tồn kho tối đa, nhà cung cấp mặc định, và trạng thái (active/inactive)
2. THE Hệ_thống_WMS SHALL quản lý danh mục sản phẩm với các trường: mã sản phẩm (duy nhất), tên, nhóm sản phẩm, đơn vị tính, quy cách đóng gói, và trạng thái
3. THE Hệ_thống_WMS SHALL cho phép phân loại vật tư theo nhóm (nguyên liệu chính, phụ liệu, bao bì, phụ tùng) và sản phẩm theo nhóm (thành phẩm, bán thành phẩm)
4. WHEN Quản_lý_Sản_xuất tạo mã vật tư hoặc sản phẩm trùng với mã đã tồn tại, THE Hệ_thống_WMS SHALL từ chối tạo và hiển thị thông báo lỗi trùng mã
5. THE Hệ_thống_WMS SHALL cho phép import danh mục vật tư và sản phẩm từ file Excel với validation dữ liệu và báo cáo lỗi chi tiết
6. IF vật tư chuyển sang trạng thái inactive, THEN THE Hệ_thống_WMS SHALL không hiển thị vật tư đó trong danh sách chọn khi tạo BOM hoặc phiếu nhập/xuất mới

### Requirement 13: Phân quyền và Bảo mật

**User Story:** Là quản trị viên hệ thống, tôi muốn phân quyền truy cập theo vai trò, để đảm bảo mỗi người chỉ thao tác được trong phạm vi trách nhiệm.

#### Acceptance Criteria

1. THE Hệ_thống_WMS SHALL phân quyền theo vai trò: Quản_lý_Sản_xuất (toàn quyền module sản xuất), Thủ_kho (toàn quyền module kho), Quản_lý_QC (toàn quyền module QMS), Nhân_viên_Sản_xuất (chỉ báo cáo tiến độ), và Admin (toàn quyền hệ thống)
2. THE Hệ_thống_WMS SHALL kiểm tra quyền truy cập ở cả frontend (ẩn/hiện menu và nút) và backend (từ chối request không có quyền)
3. THE Hệ_thống_WMS SHALL ghi nhận audit log cho tất cả thao tác thay đổi dữ liệu: ai thực hiện, thời gian, hành động, và dữ liệu trước/sau thay đổi
4. IF người dùng không có quyền thực hiện thao tác, THEN THE Hệ_thống_WMS SHALL trả về mã lỗi 403 và hiển thị thông báo "Bạn không có quyền thực hiện thao tác này"
5. THE Hệ_thống_WMS SHALL tích hợp với hệ thống xác thực hiện có của JeecgBoot (Shiro/JWT)

### Requirement 14: Quản lý Nhà cung cấp

**User Story:** Là Quản_lý_Sản_xuất, tôi muốn quản lý thông tin nhà cung cấp và đánh giá hiệu suất cung ứng, để lựa chọn nhà cung cấp phù hợp.

#### Acceptance Criteria

1. THE Hệ_thống_WMS SHALL quản lý danh mục nhà cung cấp với các trường: mã nhà cung cấp (duy nhất), tên, địa chỉ, người liên hệ, số điện thoại, email, danh mục vật tư cung cấp, và trạng thái (active/inactive/blacklisted)
2. THE Hệ_thống_WMS SHALL tự động tính toán chỉ số đánh giá nhà cung cấp dựa trên: tỷ lệ đạt IQC, số lượng NCR, thời gian giao hàng trung bình
3. WHEN tỷ lệ đạt IQC của nhà cung cấp giảm xuống dưới 80% trong 3 tháng gần nhất, THE Hệ_thống_WMS SHALL gửi cảnh báo cho Quản_lý_QC
4. THE Hệ_thống_WMS SHALL hiển thị lịch sử giao dịch với nhà cung cấp: danh sách phiếu nhập, kết quả IQC, và NCR liên quan
5. THE Hệ_thống_WMS SHALL cho phép liên kết nhiều nhà cung cấp với một vật tư và đánh dấu nhà cung cấp ưu tiên
