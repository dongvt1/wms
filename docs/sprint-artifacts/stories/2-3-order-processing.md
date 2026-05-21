# Story 2.3: Xử lý đơn hàng

## Story Details

**Epic:** Epic 2: Quản lý đơn hàng và khách hàng
**Story Key:** 2-3-order-processing
**Status:** review
**Priority:** High
**Effort:** 3 days

## User Story

Là một người quản lý kho, tôi muốn hệ thống tự động xử lý các bước trong quy trình đơn hàng từ khi tạo đến khi hoàn thành để giảm thiểu sai sót và tăng hiệu quả.

## Acceptance Criteria

1. Hệ thống xử lý xác nhận đơn hàng khi người dùng nhấn nút xác nhận
2. Hệ thống tự động cập nhật trạng thái đơn hàng theo quy trình (chờ xác nhận → đã xác nhận → đang giao → hoàn thành)
3. Hệ thống tích hợp với tồn kho để trừ số lượng sản phẩm khi đơn hàng được xác nhận
4. Hệ thống gửi thông báo cho khách hàng về trạng thái đơn hàng qua email hoặc SMS
5. Hệ thống hỗ trợ in đơn hàng khi cần
6. Hệ thống ghi nhận lịch sử thay đổi trạng thái đơn hàng
7. Hệ thống kiểm tra tính hợp lệ của đơn hàng trước khi xử lý (đủ tồn kho, thông tin khách hàng hợp lệ)
8. Hệ thống hỗ trợ hoàn trả tồn kho khi đơn hàng bị hủy

## Tasks / Subtasks

### Task 1: Xây dựng service xử lý đơn hàng
- [ ] Tạo OrderProcessingService với các phương thức xử lý trạng thái
- [ ] Triển khai state machine cho quy trình đơn hàng
- [ ] Tích hợp với InventoryService để trừ/hoàn trả tồn kho
- [ ] Tích hợp với NotificationService để gửi thông báo
- [ ] Triển khai validation logic cho đơn hàng

### Task 2: Xây dựng API cho xử lý đơn hàng
- [ ] API xác nhận đơn hàng (POST /api/orders/{id}/confirm)
- [ ] API cập nhật trạng thái đơn hàng (PUT /api/orders/{id}/status)
- [ ] API hủy đơn hàng (POST /api/orders/{id}/cancel)
- [ ] API lấy lịch sử xử lý đơn hàng (GET /api/orders/{id}/processing-history)
- [ ] API kiểm tra tính hợp lệ của đơn hàng (GET /api/orders/{id}/validate)

### Task 3: Xây dựng giao diện xử lý đơn hàng
- [ ] Component xác nhận đơn hàng với validation feedback
- [ ] Component hiển thị quy trình xử lý đơn hàng (timeline)
- [ ] Component gửi thông báo cho khách hàng
- [ ] Component in đơn hàng với template
- [ ] Dashboard hiển thị đơn hàng cần xử lý

### Task 4: Tích hợp với hệ thống tồn kho và thông báo
- [ ] Tích hợp với InventoryService để kiểm tra và trừ tồn kho
- [ ] Tích hợp với EmailService để gửi thông báo trạng thái
- [ ] Tích hợp với SMSService nếu có
- [ ] Tích hợp với hệ thống in ấn (PDF generation)

## Dev Notes

- Sử dụng Spring State Machine hoặc custom state pattern cho quy trình đơn hàng
- Đảm bảo tính nhất quán dữ liệu với @Transactional
- Sử dụng event-driven architecture cho các hành động sau khi xử lý đơn hàng
- Tuân thủ kiến trúc layered của JEECG (Controller → Service → Mapper → DB)
- Tham khảo docs/dev-story-order-management.md cho kiến trúc và patterns

### Project Structure Notes

- Backend: `jeecg-boot/jeecg-module-system/src/main/java/org/jeecg/modules/wms/order/`
- Frontend: `jeecgboot-vue3/src/views/wms/order/`
- Database: Bảng `wms_order`, `wms_order_item`, `wms_order_status_history`
- Cần align với cấu trúc hiện có từ story 2-2-order-management

### References

- [Source: docs/epic-2.md#Story-2.3-Xử-lý-đơn-hàng]
- [Source: docs/prd.md#Quản-lý-đơn-hàng]
- [Source: docs/dev-story-order-management.md#Architecture-Pattern]
- [Source: docs/sprint-artifacts/stories/2-2-order-management.md#Technical-Notes]

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

deepseek-v3.2

### Debug Log References

### Completion Notes List

### File List
