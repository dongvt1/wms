package com.cy.modules.qms.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: QMS Analytics Service - Báo cáo và phân tích chất lượng
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface QmsAnalyticsService {

    /**
     * Lấy dữ liệu tổng hợp cho dashboard.
     * Bao gồm: tỷ lệ đạt/không đạt theo loại (IQC/PQC/FQC), số NCR mở, tổng phiếu tháng này.
     *
     * @param filters Map chứa các bộ lọc tùy chọn (productId, supplierId, startDate, endDate)
     * @return Map chứa dữ liệu dashboard
     */
    Map<String, Object> getDashboardSummary(Map<String, Object> filters);

    /**
     * Lấy dữ liệu xu hướng chất lượng theo thời gian.
     *
     * @param startDate Ngày bắt đầu
     * @param endDate   Ngày kết thúc
     * @param groupBy   Nhóm theo: "week" hoặc "month"
     * @return Danh sách time-series: [{period, passCount, failCount, passRate}]
     */
    List<Map<String, Object>> getTrend(Date startDate, Date endDate, String groupBy);

    /**
     * Lấy báo cáo chất lượng nhà cung cấp.
     * Bao gồm: tỷ lệ IQC đạt, số NCR, xếp hạng trong tất cả nhà cung cấp.
     *
     * @param supplierId ID nhà cung cấp
     * @return Map chứa thông tin báo cáo nhà cung cấp
     */
    Map<String, Object> getSupplierReport(String supplierId);

    /**
     * Phân tích Pareto: top 5 tiêu chí có tỷ lệ lỗi cao nhất.
     *
     * @param filters Map chứa các bộ lọc tùy chọn (inspectionType, productId, startDate, endDate)
     * @return Danh sách top 5 tiêu chí sắp xếp theo số lỗi giảm dần
     */
    List<Map<String, Object>> getParetoAnalysis(Map<String, Object> filters);

    /**
     * Xuất báo cáo ra file (Excel hoặc PDF).
     *
     * @param format  Định dạng: "excel" hoặc "pdf"
     * @param filters Map chứa các bộ lọc cho báo cáo
     * @return byte[] nội dung file xuất
     */
    byte[] exportReport(String format, Map<String, Object> filters);
}
