# Warehouse Management System - Product Requirements Document

**Author:** BMad
**Date:** 2025-11-17
**Version:** 1.0

---

## Executive Summary

Warehouse Management System là một giải pháp web-based được thiết kế đặc biệt cho doanh nghiệp vừa và nhỏ, giúp quản lý hiệu quả hàng tồn kho, đơn hàng và tạo báo cáo trực quan. Hệ thống tập trung vào việc cung cấp giao diện trực quan giúp người dùng không chuyên dễ dàng quản lý kho, với tính năng cảnh báo tự động khi hàng tồn kho thấp.

### What Makes This Special

Điểm khác biệt chính của hệ thống là giao diện trực quan được thiết kế cho người dùng không chuyên, kết hợp với hệ thống cảnh báo thông minh khi hàng tồn kho thấp. Điều này giúp các doanh nghiệp vừa và nhỏ có thể quản lý kho hiệu quả mà không cần kiến thức chuyên sâu về quản lý chuỗi cung ứng.

---

## Project Classification

**Technical Type:** web_app
**Domain:** general
**Complexity:** low

Dự án được phân loại là ứng dụng web (web_app) với độ phức tạp thấp, tập trung vào việc cung cấp các chức năng quản lý kho cơ bản nhưng hiệu quả cho doanh nghiệp vừa và nhỏ.

---

## Success Criteria

Thành công của hệ thống được định nghĩa bằng khả năng doanh nghiệp theo dõi chính xác 100% hàng tồn kho và nhận cảnh báo kịp thời khi có biến động. Điều này đảm bảo doanh nghiệp luôn có cái nhìn rõ ràng về tình hình kho và có thể đưa ra quyết định kinh doanh dựa trên dữ liệu chính xác.

### Business Metrics

- Tỷ lệ chính xác của dữ liệu tồn kho: 100%
- Thời gian cảnh báo khi tồn kho thấp: Ngay lập tức
- Số lượng doanh nghiệp sử dụng hệ thống hàng ngày: Mục tiêu 100 doanh nghiệp
- Tỷ lệ giữ chân người dùng sau 6 tháng: 90%

---

## Product Scope

### MVP - Minimum Viable Product

MVP tập trung vào các chức năng cốt lõi giúp doanh nghiệp quản lý kho hiệu quả ngay lập tức:

- Quản lý sản phẩm cơ bản: Thêm, sửa, xóa thông tin sản phẩm
- Nhập/xuất kho: Ghi nhận giao dịch hàng hóa vào/ra kho
- Theo dõi tồn kho: Hiển thị số lượng tồn kho hiện tại cho từng sản phẩm
- Cảnh báo khi tồn kho thấp: Thông báo tự động khi sản phẩm đạt ngưỡng tồn kho tối thiểu
- Quản lý kho hàng
- Quản lý khu vực trong kho để khai báo các thông tin : mã , tên , Area , số rack, số rác trống , số rác đã đặt hàng, mã hàng đã đặt như nào …
- Chuyển kho nội bộ  có thông tin các trường : mã yêu cầu, từ kho, đến kho , đơn vị lĩnh, lý do xuất / nhập , ngày chứng từ, số chứng từ, số serial , trạng thaí , Pack
- Nhập kho sản xuất
- Xuất hàng theo đơn
### Growth Features (Post-MVP)

Các tính năng mở rộng giúp hệ thống cạnh tranh hơn:

- Quản lý nhà cung cấp: Thông tin nhà cung cấp, lịch sử giao dịch
- Báo cáo trực quan: Biểu đồ, phân tích xu hướng tồn kho, báo cáo xuất/nhập
- Ứng dụng di động: Quản lý kho từ thiết bị di động, quét mã vạch

### Vision (Future)

Tầm nhìn dài hạn cho hệ thống quản lý kho thông minh:

- Hệ thống tự động hoàn toàn với IoT: Tích hợp cảm biến IoT để theo dõi tồn kho real-time
- AI dự báo nhu cầu: Phân tích dữ liệu lịch sử để dự báo nhu cầu tương lai
- Tự động đặt hàng: Tự động tạo đơn hàng khi tồn kho đạt ngưỡng tối thiểu
- Tối ưu hóa không gian kho: Gợi ý sắp xếp kho dựa trên tần suất sử dụng

---

## web_app Specific Requirements

Hệ thống được xây dựng như một ứng dụng web hiện đại với các yêu cầu kỹ thuật sau:

- Kiến trúc SPA (Single Page Application) để trải nghiệm người dùng mượt mà
- Hỗ trợ đa nền tảng: trình duyệt di động và desktop
- Tối ưu cho các trình duyệt hiện đại (Chrome, Firefox, Safari, Edge)
- Không yêu cầu SEO do là hệ thống nội bộ
- Tính năng real-time cho cảnh báo tồn kho và cập nhật dữ liệu
- Đạt chuẩn accessibility WCAG A để đảm bảo người dùng khuyết tật cơ bản có thể sử dụng

### Platform Support

- Desktop: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- Mobile: iOS Safari 14+, Chrome Mobile 90+
- Yêu cầu kết nối internet ổn định
- Hỗ trợ cả màn hình ngang và dọc trên thiết bị di động

---

## User Experience Principles

Thiết kế UX tập trung vào việc tạo ra trải nghiệm thân thiện với người dùng không chuyên:

- Thiết kế hiện đại với giao diện trực quan
- Sử dụng biểu tượng dễ hiểu thay vì văn bản phức tạp
- Màu sắc tươi sáng để phân biệt các trạng thái và hành động
- Luồng làm việc tự nhiên, giảm thiểu các bước không cần thiết
- Phản hồi tức thì cho mọi hành động của người dùng
- Thiết kế responsive hoạt động tốt trên cả desktop và di động

### Key Interactions

Các tương tác chính của hệ thống:

- Thao tác kéo-thả cho việc sắp xếp sản phẩm
- Quét mã vạch (trong phiên bản di động) để nhanh chóng nhập/xuất kho
- Cảnh báo pop-up khi tồn kho đạt ngưỡng nguy hiểm
- Lọc và tìm kiếm sản phẩm theo nhiều tiêu chí
- Xem nhanh chi tiết sản phẩm bằng cách hover/click
- Nhập liệu nhanh với các mẫu có sẵn
- Xác nhận trước khi thực hiện các hành động quan trọng (xóa, xuất kho lớn)

---

## Functional Requirements

### Quản lý sản phẩm

- FR1: Người dùng có thể thêm sản phẩm mới với thông tin cơ bản (tên, mã, mô tả, giá)
- FR2: Người dùng có thể chỉnh sửa thông tin sản phẩm đã tồn tại
- FR3: Người dùng có thể xóa sản phẩm khỏi hệ thống
- FR4: Người dùng có thể tìm kiếm sản phẩm theo tên hoặc mã sản phẩm
- FR5: Người dùng có thể lọc sản phẩm theo danh mục hoặc trạng thái
- FR6: Người dùng có thể xem lịch sử thay đổi thông tin sản phẩm
- FR7: Người dùng có thể quản lý danh mục sản phẩm (thêm, sửa, xóa)
- FR8: Hệ thống hiển thị hình ảnh sản phẩm (nếu có)

### Quản lý tồn kho

- FR9: Hệ thống hiển thị số lượng tồn kho hiện tại cho từng sản phẩm
- FR10: Người dùng có thể ghi nhận giao dịch nhập kho
- FR11: Người dùng có thể ghi nhận giao dịch xuất kho
- FR12: Hệ thống tự động cập nhật số lượng tồn kho sau mỗi giao dịch
- FR13: Hệ thống gửi cảnh báo khi sản phẩm đạt ngưỡng tồn kho tối thiểu
- FR14: Người dùng có thể thiết lập ngưỡng tồn kho tối thiểu cho từng sản phẩm
- FR15: Người dùng có thể xem lịch sử giao dịch nhập/xuất kho
- FR16: Hệ thống hiển thị giá trị tồn kho (số lượng × đơn giá)

### Quản lý vị trí kho

- FR17: Người dùng có thể định nghĩa các vị trí kho (khu vực, kệ, vị trí cụ thể)
- FR18: Người dùng có thể gán sản phẩm đến vị trí cụ thể trong kho
- FR19: Người dùng có thể tìm kiếm sản phẩm theo vị trí kho
- FR20: Hệ thống hiển thị sơ đồ kho với các vị trí đã định nghĩa
- FR21: Người dùng có thể di chuyển sản phẩm giữa các vị trí kho

### Quản lý nhà cung cấp

- FR22: Người dùng có thể thêm thông tin nhà cung cấp (tên, liên hệ, địa chỉ)
- FR23: Người dùng có thể chỉnh sửa thông tin nhà cung cấp
- FR24: Người dùng có thể xóa nhà cung cấp
- FR25: Người dùng có thể gán sản phẩm đến nhà cung cấp cụ thể
- FR26: Người dùng có thể xem lịch sử giao dịch với từng nhà cung cấp
- FR27: Hệ thống hiển thị danh sách nhà cung cấp cho từng sản phẩm

### Hệ thống cảnh báo

- FR28: Hệ thống gửi cảnh báo qua email khi tồn kho thấp
- FR29: Người dùng có thể cấu hình địa chỉ email nhận cảnh báo
- FR30: Người dùng có thể tắt/bật cảnh báo cho từng sản phẩm
- FR31: Hệ thống gửi cảnh báo khi có giao dịch lớn bất thường
- FR32: Hệ thống hiển thị lịch sử các cảnh báo đã gửi

### Báo cáo

- FR33: Người dùng có thể xem báo cáo tồn kho hiện tại
- FR34: Người dùng có thể xuất báo cáo tồn kho ra file PDF/Excel
- FR35: Người dùng có thể xem báo cáo lịch sử nhập/xuất kho
- FR36: Người dùng có thể lọc báo cáo theo khoảng thời gian
- FR37: Người dùng có thể xem báo cáo tổng hợp (giá trị tồn kho, số lượng sản phẩm)
- FR38: Hệ thống tạo báo cáo tự động theo lịch định sẵn

### Quản lý người dùng

- FR39: Admin có thể tạo tài khoản người dùng mới
- FR40: Admin có thể phân quyền cho người dùng (xem, thêm, sửa, xóa)
- FR41: Người dùng có thể đổi mật khẩu
- FR42: Hệ thống ghi nhận lịch sử hoạt động của người dùng
- FR43: Người dùng có thể xem thông tin cá nhân và chỉnh sửa

### Quản lý đơn hàng

- FR44: Người dùng có thể tạo đơn hàng mới
- FR45: Người dùng có thể chỉnh sửa thông tin đơn hàng
- FR46: Người dùng có thể hủy đơn hàng
- FR47: Hệ thống tự động trừ tồn kho khi đơn hàng được xác nhận
- FR48: Người dùng có thể xem trạng thái đơn hàng (chờ xác nhận, đã xác nhận, đang giao, hoàn thành)
- FR49: Người dùng có thể tìm kiếm đơn hàng theo mã hoặc khách hàng
- FR50: Hệ thống tạo mã đơn hàng tự động

### Quản lý khách hàng

- FR51: Người dùng có thể thêm thông tin khách hàng mới
- FR52: Người dùng có thể chỉnh sửa thông tin khách hàng
- FR53: Người dùng có thể xóa khách hàng
- FR54: Người dùng có thể xem lịch sử đơn hàng của khách hàng
- FR55: Hệ thống hiển thị thông tin nợ/có của khách hàng
- FR56: Người dùng có thể tìm kiếm khách hàng theo tên hoặc mã

---

## Non-Functional Requirements

### Performance

- Tải trang chính trong vòng 2 giây
- Xử lý tối thiểu 100 giao dịch mỗi giây
- Hiển thị báo cáo trong vòng 5 giây
- Tìm kiếm sản phẩm trả kết quả trong vòng 1 giây
- Hệ thống phản hồi tức thì cho các thao tác người dùng

### Security

- Mật khẩu người dùng được mã hóa bằng bcrypt
- Kết nối HTTPS cho tất cả các trang
- Xác thực người dùng cho mọi chức năng quản lý
- Phân quyền chi tiết cho từng chức năng
- Backup dữ liệu hàng ngày
- Log tất cả các thao tác quan trọng
- Chống tấn công SQL injection và XSS

### Scalability

- Hỗ trợ tối đa 50 người dùng đồng thời cho phiên bản MVP
- Quản lý tối đa 5,000 sản phẩm
- Lưu trữ tối đa 100,000 giao dịch
- Cơ sở dữ liệu có thể mở rộng khi cần
- Kiến trúc cho phép thêm máy chủ khi cần

### Accessibility

- Đạt chuẩn WCAG A (Level A)
- Tương thích với các công cụ đọc màn hình cơ bản
- Tỷ lệ tương phản màu sắc tối thiểu 4.5:1
- Có thể điều hướng bằng bàn phím
- Cung cấp text alternative cho hình ảnh
- Font size có thể phóng to đến 200%

---

_This PRD captures the essence of Warehouse Management System - Hệ thống quản lý kho bãi trực quan giúp doanh nghiệp vừa và nhỏ theo dõi chính xác 100% hàng tồn kho và nhận cảnh báo kịp thời_

_Created through collaborative discovery between BMad and AI facilitator._