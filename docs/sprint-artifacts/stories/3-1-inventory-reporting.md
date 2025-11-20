# Story 3.1: Báo cáo tồn kho

## Story Details

**Epic:** Epic 3: Báo cáo và phân tích
**Story Key:** 3-1-inventory-reporting
**Status:** Ready for Development
**Priority:** Medium
**Effort:** 3 days

## User Story

Là một người quản lý kho, tôi muốn xem báo cáo tồn kho hiện tại và xuất ra các định dạng khác nhau để có thể phân tích và đưa ra quyết định kinh doanh hiệu quả.

## Acceptance Criteria

- [ ] Hệ thống hiển thị báo cáo tồn kho hiện tại với thông tin chi tiết
- [ ] Người dùng có thể xuất báo cáo ra file PDF
- [ ] Người dùng có thể xuất báo cáo ra file Excel
- [ ] Người dùng có thể lọc báo cáo theo khoảng thời gian
- [ ] Hệ thống hiển thị báo cáo tổng hợp (giá trị tồn kho, số lượng sản phẩm)
- [ ] Hệ thống hỗ trợ tạo báo cáo tự động theo lịch định sẵn
- [ ] Hệ thống hiển thị báo cáo tồn kho thấp
- [ ] Người dùng có thể tùy chỉnh các trường hiển thị trong báo cáo
- [ ] Hệ thống hiển thị biểu đồ phân tích tồn kho
- [ ] Hệ thống hỗ trợ so sánh báo cáo giữa các kỳ

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu báo cáo
- [ ] Tạo bảng report_templates với các trường: id, name, description, templateType, fields, createdBy, createdAt, updatedAt
- [ ] Tạo bảng report_schedules với các trường: id, templateId, scheduleType, scheduleConfig, recipients, isActive, createdBy, createdAt, updatedAt
- [ ] Tạo bảng report_history với các trường: id, templateId, generatedBy, generatedAt, parameters, filePath, status
- [ ] Thiết kế quan hệ khóa ngoại giữa report_schedules và report_templates
- [ ] Thiết kế quan hệ khóa ngoại giữa report_history và report_templates
- [ ] Tạo index cho trường templateId và generatedAt để tối ưu truy vấn

### Task 2: Xây dựng API báo cáo tồn kho
- [ ] API lấy báo cáo tồn kho hiện tại (GET /api/reports/inventory/current)
- [ ] API xuất báo cáo tồn kho ra PDF (GET /api/reports/inventory/export/pdf)
- [ ] API xuất báo cáo tồn kho ra Excel (GET /api/reports/inventory/export/excel)
- [ ] API lấy báo cáo tồn kho theo khoảng thời gian (GET /api/reports/inventory/by-period)
- [ ] API lấy báo cáo tổng hợp tồn kho (GET /api/reports/inventory/summary)
- [ ] API lấy báo cáo tồn kho thấp (GET /api/reports/inventory/low-stock)
- [ ] API quản lý template báo cáo (CRUD)
- [ ] API quản lý lịch báo cáo (CRUD)
- [ ] API lấy lịch sử báo cáo (GET /api/reports/history)
- [ ] API tạo báo cáo theo template (POST /api/reports/generate)

### Task 3: Xây dựng giao diện báo cáo tồn kho
- [ ] Trang dashboard báo cáo tồn kho với các chỉ số quan trọng
- [ ] Component hiển thị báo cáo tồn kho hiện tại
- [ ] Component lọc báo cáo theo nhiều tiêu chí
- [ ] Component tùy chỉnh các trường hiển thị trong báo cáo
- [ ] Component xuất báo cáo ra các định dạng khác nhau
- [ ] Component quản lý template báo cáo
- [ ] Component quản lý lịch báo cáo
- [ ] Component hiển thị lịch sử báo cáo
- [ ] Component so sánh báo cáo giữa các kỳ
- [ ] Component hiển thị biểu đồ phân tích tồn kho

### Task 4: Xây dựng hệ thống tạo báo cáo
- [ ] Service tạo báo cáo PDF từ template
- [ ] Service tạo báo cáo Excel từ template
- [ ] Service xử lý dữ liệu cho báo cáo
- [ ] Service tạo báo cáo theo lịch
- [ ] Service gửi báo cáo qua email
- [ ] Service cache báo cáo để tối ưu performance
- [ ] Service xử lý báo cáo lớn bất đồng bộ

### Task 5: Xây dựng hệ thống biểu đồ và phân tích
- [ ] Component biểu đồ tồn kho theo thời gian
- [ ] Component biểu đồ phân bổ tồn kho theo danh mục
- [ ] Component biểu đồ sản phẩm có giá trị tồn kho cao nhất
- [ ] Component biểu đồ sản phẩm có tồn kho thấp nhất
- [ ] Component so sánh tồn kho giữa các kỳ
- [ ] Component phân tích xu hướng tồn kho

### Task 6: Tích hợp với các module khác
- [ ] Tích hợp với module quản lý sản phẩm để lấy thông tin sản phẩm
- [ ] Tích hợp với module quản lý tồn kho để lấy dữ liệu tồn kho
- [ ] Tích hợp với module quản lý giao dịch để lấy dữ liệu lịch sử
- [ ] Tích hợp với module quản lý email để gửi báo cáo
- [ ] Tích hợp với module quản lý notification để thông báo khi báo cáo sẵn sàng

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng iText hoặc JasperReports cho việc tạo PDF
- Sử dụng Apache POI cho việc tạo Excel
- Sử dụng Chart.js hoặc ECharts cho việc tạo biểu đồ
- Sử dụng Quartz Scheduler cho việc tạo báo cáo theo lịch
- Sử dụng Spring Mail cho việc gửi email
- Sử dụng Spring Async cho việc xử lý báo cáo lớn bất đồng bộ
- Sử dụng Spring Cache cho việc cache báo cáo

## Dependencies

- Cần có module quản lý sản phẩm đã được triển khai
- Cần có module quản lý tồn kho đã được triển khai
- Cần có module quản lý giao dịch đã được triển khai
- Cần có hệ thống authentication và authorization được thiết lập
- Cần có module quản lý email được triển khai

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-20
- Story được chọn: 3-1-inventory-reporting
- Trạng thái hiện tại: Ready for Development

### Completion Notes
- Bắt đầu với Task 1: Thiết kế cơ sở dữ liệu báo cáo
- Ưu tiên xây dựng API trước để có dữ liệu cho frontend
- Tập trung vào performance khi xử lý lượng lớn dữ liệu báo cáo
- Đảm bảo tính chính xác của dữ liệu báo cáo
- Triển khai hệ thống tạo báo cáo hiệu quả
- Tối ưu hóa việc tạo báo cáo lớn bằng cách xử lý bất đồng bộ