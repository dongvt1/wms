package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.dto.MaterialAvailabilityResult;

/**
 * Service interface cho kiểm tra tình trạng nguyên vật liệu.
 * Truy vấn tồn kho đã cache so với yêu cầu BOM cho từng đơn hàng,
 * tính toán thiếu hụt, đặt trước nguyên vật liệu, và xác thực thời gian giao hàng.
 *
 * Retry logic: 3 lần thử với exponential backoff cho truy vấn ERP.
 * Kết quả trả về trong vòng 60 giây.
 */
public interface MaterialAvailabilityService {

    /**
     * Kiểm tra tình trạng nguyên vật liệu cho một đơn hàng.
     *
     * Quy trình:
     * 1. Tải đơn hàng từ DB, lấy loại sản phẩm
     * 2. Lấy BOM từ cache InventorySyncService (hoặc truy vấn ErpClient với retry)
     * 3. Với mỗi nguyên vật liệu trong BOM: lấy available_qty từ cache, tính deficit = max(0, required - available)
     * 4. Lưu bản ghi MaterialAvailability vào DB
     * 5. Nếu tất cả nguyên vật liệu đủ: đặt trước (reserved=1), kiểm tra thời gian giao hàng
     * 6. Nếu có thiếu hụt: đặt status='shortage', tính deficit
     * 7. Xác thực: nếu current_date + lead_time_days > order.deadline → đánh dấu đơn hàng at-risk
     *
     * @param orderId ID đơn hàng cần kiểm tra
     * @return kết quả kiểm tra tình trạng nguyên vật liệu
     */
    MaterialAvailabilityResult checkMaterialAvailability(String orderId);
}
