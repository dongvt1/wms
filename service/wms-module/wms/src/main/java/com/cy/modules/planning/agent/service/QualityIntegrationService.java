package com.cy.modules.planning.agent.service;

import java.math.BigDecimal;

/**
 * Service interface cho tích hợp chất lượng (Quality Integration).
 * Nhận dữ liệu QMS mỗi ≤15 phút, tính toán sản lượng gộp dựa trên yield rate lịch sử 90 ngày,
 * cảnh báo khi tỷ lệ lỗi vượt trung bình 30 ngày >5 điểm phần trăm,
 * phân loại sản phẩm lỗi (sửa chữa/hủy), và kích hoạt lập kế hoạch bổ sung khi cần.
 */
public interface QualityIntegrationService {

    /**
     * Kiểm tra cảnh báo chất lượng cho batch.
     * So sánh tỷ lệ lỗi hiện tại với trung bình 30 ngày,
     * cảnh báo nếu chênh lệch >5 điểm phần trăm.
     * Đề xuất điều chỉnh: tăng sản lượng, chuyển dây chuyền, hoặc tạm dừng sản xuất.
     *
     * @param batchId mã batch cần kiểm tra
     */
    void checkQualityAlerts(String batchId);

    /**
     * Tính toán sản lượng gộp (gross quantity) từ sản lượng ròng (net quantity).
     * Công thức: gross = net / yieldRate (yield rate lịch sử 90 ngày).
     * Sử dụng yield rate gần nhất nếu dữ liệu QMS không khả dụng >30 phút.
     *
     * @param productId   mã sản phẩm
     * @param lineId      mã dây chuyền sản xuất
     * @param netQuantity sản lượng ròng cần đạt
     * @return sản lượng gộp cần sản xuất
     */
    BigDecimal calculateGrossQuantity(String productId, String lineId, BigDecimal netQuantity);

    /**
     * Phân loại sản phẩm lỗi trong batch qua QMS.
     * Phân loại thành: sửa chữa được (repairable) hoặc phải hủy (destroyable).
     * Trừ số lượng phải hủy khỏi sản lượng ròng.
     *
     * @param batchId mã batch cần phân loại
     */
    void classifyDefects(String batchId);

    /**
     * Điều chỉnh kế hoạch do tổn thất yield.
     * Kích hoạt lập kế hoạch sản xuất bổ sung khi sản lượng ròng
     * thấp hơn yêu cầu đơn hàng.
     *
     * @param batchId mã batch cần điều chỉnh
     */
    void adjustForYieldLoss(String batchId);
}
