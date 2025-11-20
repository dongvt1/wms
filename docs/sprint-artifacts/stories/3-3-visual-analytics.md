# Story 3.3: Phân tích trực quan

## Story Details

**Epic:** Epic 3: Báo cáo và phân tích
**Story Key:** 3-3-visual-analytics
**Status:** Ready for Development
**Priority:** Medium
**Effort:** 5 days

## User Story

Là một nhà quản lý, tôi muốn xem các biểu đồ và phân tích trực quan về dữ liệu kho bãi để có thể hiểu rõ xu hướng kinh doanh và đưa ra quyết định nhanh chóng.

## Acceptance Criteria

- [ ] Hệ thống hiển thị biểu đồ tồn kho theo thời gian
- [ ] Hệ thống hiển thị biểu đồ nhập/xuất kho theo sản phẩm
- [ ] Hệ thống phân tích xu hướng sử dụng sản phẩm
- [ ] Hệ thống cho phép so sánh hiệu suất giữa các kỳ
- [ ] Hệ thống cung cấp dashboard với các chỉ số quan trọng
- [ ] Người dùng có thể tùy chỉnh các loại biểu đồ và bộ lọc
- [ ] Hệ thống hỗ trợ các loại biểu đồ khác nhau (line, bar, pie, area)
- [ ] Hệ thống cho phép xuất biểu đồ ra hình ảnh hoặc PDF
- [ ] Hệ thống cập nhật dữ liệu biểu đồ theo thời gian thực
- [ ] Hệ thống cung cấp các phân tích dự báo đơn giản

## Tasks/Subtasks

### Task 1: Thiết kế cơ sở dữ liệu phân tích trực quan
- [ ] Tạo bảng analytics_dashboards với các trường: id, name, description, layout, isDefault, createdBy, createdAt, updatedAt
- [ ] Tạo bảng dashboard_widgets với các trường: id, dashboardId, widgetType, title, dataSource, config, position, size
- [ ] Tạo bảng analytics_cache với các trường: id, cacheKey, data, expiryDate, createdAt
- [ ] Tạo bảng user_dashboard_preferences với các trường: id, userId, dashboardId, preferences, createdAt
- [ ] Thiết kế quan hệ khóa ngoại giữa dashboard_widgets và analytics_dashboards
- [ ] Thiết kế quan hệ khóa ngoại giữa user_dashboard_preferences và analytics_dashboards
- [ ] Tạo index cho các trường tìm kiếm và cache

### Task 2: Xây dựng API phân tích trực quan
- [ ] API lấy dữ liệu cho biểu đồ tồn kho theo thời gian (GET /api/analytics/inventory-timeline)
- [ ] API lấy dữ liệu cho biểu đồ nhập/xuất kho theo sản phẩm (GET /api/analytics/stock-movement-by-product)
- [ ] API lấy dữ liệu phân tích xu hướng sử dụng sản phẩm (GET /api/analytics/product-usage-trends)
- [ ] API so sánh hiệu suất giữa các kỳ (POST /api/analytics/period-comparison)
- [ ] API lấy dashboard với các widget (GET /api/analytics/dashboards/:id)
- [ ] API quản lý dashboard (CRUD)
- [ ] API quản lý widget trong dashboard (CRUD)
- [ ] API xuất biểu đồ ra hình ảnh (GET /api/analytics/charts/:id/export-image)
- [ ] API xuất dashboard ra PDF (GET /api/analytics/dashboards/:id/export-pdf)
- [ ] API lấy dữ liệu dự báo (GET /api/analytics/forecast)

### Task 3: Xây dựng service xử lý phân tích trực quan
- [ ] Service tính toán dữ liệu cho biểu đồ tồn kho theo thời gian
- [ ] Service tính toán dữ liệu cho biểu đồ nhập/xuất kho theo sản phẩm
- [ ] Service phân tích xu hướng sử dụng sản phẩm
- [ ] Service so sánh hiệu suất giữa các kỳ
- [ ] Service dự báo đơn giản dựa trên dữ liệu lịch sử
- [ ] Service caching cho dữ liệu phân tích thường xuyên truy cập
- [ ] Service xử lý dữ liệu thời gian thực cho biểu đồ
- [ ] Service xuất biểu đồ ra hình ảnh
- [ ] Service xuất dashboard ra PDF
- [ ] Service xử lý dữ liệu lớn cho phân tích

### Task 4: Xây dựng giao diện phân tích trực quan
- [ ] Trang dashboard chính với các widget tùy chỉnh
- [ ] Component biểu đồ tồn kho theo thời gian
- [ ] Component biểu đồ nhập/xuất kho theo sản phẩm
- [ ] Component phân tích xu hướng sử dụng sản phẩm
- [ ] Component so sánh hiệu suất giữa các kỳ
- [ ] Component quản lý dashboard (tạo, sửa, xóa)
- [ ] Component quản lý widget trong dashboard
- [ ] Component tùy chỉnh biểu đồ (loại, màu sắc, bộ lọc)
- [ ] Component xuất biểu đồ và dashboard
- [ ] Component xem dữ liệu dự báo

### Task 5: Xây dựng hệ thống dashboard tùy chỉnh
- [ ] Drag-and-drop interface cho việc sắp xếp widget
- [ ] Component tạo widget mới với các loại biểu đồ khác nhau
- [ ] Component cấu hình nguồn dữ liệu cho widget
- [ ] Component lưu và chia sẻ dashboard
- [ ] Component quản lý template dashboard
- [ ] Component đặt dashboard làm mặc định
- [ ] Component quản lý quyền truy cập dashboard
- [ ] Component lịch sử thay đổi dashboard

### Task 6: Tích hợp với các module khác
- [ ] Tích hợp với module quản lý tồn kho để lấy dữ liệu tồn kho
- [ ] Tích hợp với module quản lý sản phẩm để lấy thông tin sản phẩm
- [ ] Tích hợp với module quản lý nhập/xuất kho để lấy dữ liệu giao dịch
- [ ] Tích hợp với module quản lý đơn hàng để lấy dữ liệu bán hàng
- [ ] Tích hợp với module người dùng để quản lý quyền truy cập
- [ ] Tích hợp với module báo cáo để đồng bộ dữ liệu

## Technical Notes

- Sử dụng JEECG framework conventions cho backend development
- Sử dụng Vue.js 3 với Composition API cho frontend development
- Sử dụng Ant Design Vue cho UI components
- Sử dụng Spring Boot với JPA/Hibernate cho backend
- Sử dụng MySQL cho cơ sở dữ liệu
- Sử dụng Chart.js hoặc ECharts cho biểu đồ trực quan
- Sử dụng Redis cho caching dữ liệu phân tích
- Sử dụng WebSocket cho cập nhật dữ liệu thời gian thực
- Sử dụng Apache PDFBox cho xuất PDF
- Sử dụng Spring Batch cho xử lý dữ liệu lớn
- Sử dụng Machine Learning libraries đơn giản cho dự báo

## Dependencies

- Cần có module quản lý tồn kho đã được triển khai
- Cần có module quản lý sản phẩm đã được triển khai
- Cần có module quản lý nhập/xuất kho đã được triển khai
- Cần có module quản lý đơn hàng đã được triển khai
- Cần có module người dùng đã được triển khai

## Dev Agent Record

### Debug Log
- Ngày bắt đầu: 2025-11-20
- Story được chọn: 3-3-visual-analytics
- Trạng thái hiện tại: Ready for Development

### Completion Notes
- Bắt đầu với Task 1: Thiết kế cơ sở dữ liệu phân tích trực quan
- Ưu tiên xây dựng các API tính toán dữ liệu cho biểu đồ
- Tập trung vào tối ưu hóa hiệu suất cho việc xử lý dữ liệu lớn
- Đảm bảo tính tương tác và trực quan của các biểu đồ
- Triển khai hệ thống caching hiệu quả cho dữ liệu phân tích
- Xây dựng giao diện drag-and-drop thân thiện với người dùng