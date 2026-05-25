package com.cy.modules.planning.agent.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service interface cho giám sát thực thi sản xuất (Production Execution Monitor).
 * Thu thập trạng thái máy từ Scada mỗi ≤5 phút, tính toán kết quả sản xuất hàng ngày,
 * ghi nhận thành phẩm và kích hoạt nhập kho, tạo yêu cầu trả nguyên vật liệu dư.
 */
public interface ProductionExecutionMonitor {

    /**
     * Thu thập tiến độ sản xuất từ Scada cho các batch đang hoạt động.
     * Được gọi tự động mỗi 5 phút qua @Scheduled.
     * Lưu dữ liệu vào bảng ap_production_progress.
     * Cảnh báo khi 2 lần thu thập liên tiếp thất bại.
     */
    void collectProgress();

    /**
     * Tính toán kết quả sản xuất hàng ngày cho kế hoạch tuần.
     * Bao gồm: số lượng sản xuất, tỷ lệ lỗi, phần trăm hoàn thành,
     * và phần trăm sai lệch = ((actual - planned) / planned) × 100.
     *
     * @param weeklyPlanId ID kế hoạch tuần
     * @param date         ngày cần tính toán
     */
    void calculateDailyResults(String weeklyPlanId, LocalDate date);

    /**
     * Ghi nhận thành phẩm và kích hoạt nhập kho trong ERP.
     * Retry logic: 3 lần thử. Sau 3 lần thất bại → thông báo quản lý sản xuất.
     *
     * @param batchId  ID batch đã hoàn thành
     * @param quantity số lượng thành phẩm
     */
    void recordFinishedGoods(String batchId, BigDecimal quantity);

    /**
     * Tạo yêu cầu trả nguyên vật liệu dư khi số lượng còn lại vượt mức tối thiểu có thể trả.
     *
     * @param batchId ID batch đã hoàn thành sản xuất
     */
    void generateMaterialReturn(String batchId);
}
