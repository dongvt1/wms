# Story 3.2: Báo cáo nhập/xuất kho

## Story Details

**Epic:** Epic 3: Báo cáo và phân tích
**Story Key:** 3-2-stock-movement-reporting
**Status:** Ready for Development
**Priority:** Medium
**Effort:** 4 days

## User Story

Là một người quản lý kho, tôi muốn xem báo cáo lịch sử giao dịch nhập/xuất kho và phân tích xu hướng để có thể đưa ra quyết định kinh doanh hiệu quả.

## Acceptance Criteria

- [ ] Hệ thống hiển thị báo cáo lịch sử nhập/xuất kho với thông tin chi tiết
- [ ] Hệ thống phân tích xu hướng nhập/xuất theo thời gian
- [ ] Người dùng có thể lọc báo cáo theo sản phẩm hoặc khoảng thời gian
- [ ] Hệ thống cho phép so sánh giữa các kỳ báo cáo
- [ ] Người dùng có thể xuất báo cáo ra file PDF/Excel
- [ ] Hệ thống hiển thị biểu đồ trực quan về nhập/xuất kho
- [ ] Hệ thống tính toán và hiển thị các chỉ số quan trọng (tổng nhập, tổng xuất, giá trị)
- [ ] Hệ thống hỗ trợ báo cáo theo từng kho hoặc tổng hợp tất cả các kho
- [ ] Hệ thống cho phép tạo báo cáo tự động theo lịch định sẵn
- [ ] Hệ thống lưu lịch sử các báo cáo đã tạo

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu báo cáo nhập/xuất kho
- [ ] Tạo bảng stock_movement_reports với các trường: id, reportName, reportType, startDate, endDate, warehouseId, filters, generatedBy, generatedAt, filePath
- [ ] Tạo bảng stock_movement_analytics với các trường: id, reportId, productId, totalIn, totalOut, netMovement, valueIn, valueOut, netValue
- [ ] Tạo bảng report_schedules với các trường: id, reportName, reportType, schedule, recipients, parameters, isActive, createdBy, createdAt
- [ ] Thiết kế quan hệ khóa ngoại giữa stock_movement_reports và warehouses
- [ ] Thiết kế quan hệ khóa ngoại giữa stock_movement_analytics và stock_movement_reports
- [ ] Thiết kế quan hệ khóa ngoại giữa stock_movement_analytics và products
- [ ] Tạo index cho các trường tìm kiếm và lọc trong các bảng báo cáo

### Task 2: Xây dựng API báo cáo nhập/xuất kho
- [ ] API tạo báo cáo nhập/xuất kho (POST /api/reports/stock-movement)
- [ ] API lấy danh sách báo cáo đã tạo (GET /api/reports/stock-movement) với phân trang và tìm kiếm
- [ ] API lấy chi tiết báo cáo (GET /api/reports/stock-movement/:id)
- [ ] API xuất báo cáo ra PDF (GET /api/reports/stock-movement/:id/export-pdf)
- [ ] API xuất báo cáo ra Excel (GET /api/reports/stock-movement/:id/export-excel)
- [ ] API lấy phân tích xu hướng (GET /api/reports/stock-movement/trends)
- [ ] API so sánh các kỳ báo cáo (POST /api/reports/stock-movement/compare)
- [ ] API tạo lịch báo cáo tự động (POST /api/reports/stock-movement/schedules)
- [ ] API quản lý lịch báo cáo tự động (CRUD)
- [ ] API lấy dữ liệu cho biểu đồ (GET /api/reports/stock-movement/charts)

### Task 3: Xây dựng service xử lý báo cáo
- [ ] Service tạo báo cáo nhập/xuất kho với các bộ lọc
- [ ] Service tính toán các chỉ số phân tích (tổng nhập, tổng xuất, giá trị)
- [ ] Service phân tích xu hướng theo thời gian
- [ ] Service so sánh giữa các kỳ báo cáo
- [ ] Service xuất báo cáo ra PDF
- [ ] Service xuất báo cáo ra Excel
- [ ] Service tạo báo cáo tự động theo lịch
- [ ] Service gửi báo cáo qua email
- [ ] Service caching cho báo cáo thường xuyên truy cập
- [ ] Service xử lý báo cáo hàng loạt

### Task 4: Xây dựng giao diện báo cáo nhập/xuất kho
- [ ] Trang dashboard báo cáo nhập/xuất kho với tổng quan
- [ ] Form tạo báo cáo với các bộ lọc chi tiết
- [ ] Bảng hiển thị danh sách báo cáo đã tạo
- [ ] Modal xem chi tiết báo cáo với thông tin đầy đủ
- [ ] Component so sánh các kỳ báo cáo
- [ ] Component hiển thị biểu đồ xu hướng nhập/xuất kho
- [ ] Component xuất báo cáo ra PDF/Excel
- [ ] Component quản lý lịch báo cáo tự động
- [ ] Component xem lịch sử các báo cáo đã tạo
- [ ] Component chia sẻ báo cáo qua email

### Task 5: Tích hợp với các module khác
- [ ] Tích hợp với module quản lý tồn kho để lấy dữ liệu giao dịch
- [ ] Tích hợp với module quản lý sản phẩm để hiển thị thông tin sản phẩm
- [ ] Tích hợp với module quản lý kho để lọc theo kho
- [ ] Tích hợp với module email để gửi báo cáo tự động
- [ ] Tích hợp với module người dùng để theo dõi người tạo báo cáo
- [ ] Tích hợp với module lịch sử để lưu trữ báo cáo đã tạo

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng Apache POI cho xuất Excel
- Sử dụng iText cho xuất PDF
- Sử dụng Chart.js cho biểu đồ trực quan
- Sử dụng Quartz Scheduler cho việc tạo báo cáo tự động
- Sử dụng Spring Mail cho việc gửi email
- Sử dụng Redis cho caching báo cáo thường xuyên truy cập
- Sử dụng Spring Batch cho việc xử lý báo cáo hàng loạt

## Dependencies

- Cần có module quản lý tồn kho đã được triển khai
- Cần có module quản lý sản phẩm đã được triển khai
- Cần có module quản lý kho đã được triển khai
- Cần có module email đã được triển khai
- Cần có module người dùng đã được triển khai

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-20
- Story được chọn: 3-2-stock-movement-reporting
- Trạng thái hiện tại: Ready for Development

### Completion Notes
- Bắt đầu với Task 1: Thiết kế cơ sở dữ liệu báo cáo nhập/xuất kho
- Ưu tiên xây dựng API xử lý báo cáo với hiệu suất cao
- Tập trung vào tối ưu hóa truy vấn cho lượng lớn dữ liệu giao dịch
- Đảm bảo tính chính xác của các chỉ số phân tích
- Triển khai hệ thống caching hiệu quả cho báo cáo thường xuyên truy cập