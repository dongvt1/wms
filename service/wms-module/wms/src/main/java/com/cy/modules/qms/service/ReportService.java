package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.vo.InspectionHistoryVO;
import com.cy.modules.qms.vo.InspectionStatisticsVO;
import com.cy.modules.qms.vo.InspectionTrendVO;
import com.cy.modules.qms.vo.ParetoAnalysisVO;

import java.util.Date;

/**
 * Service báo cáo và thống kê kiểm tra chất lượng.
 *
 * Cung cấp:
 * - Tra cứu lịch sử kiểm tra với filter đa tiêu chí
 * - Tính toán thống kê pass/fail theo template và theo field
 * - Pareto analysis: top 5 fields có tỷ lệ fail cao nhất
 * - Xu hướng pass/fail theo thời gian (daily/weekly/monthly)
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
public interface ReportService {

    /**
     * Tra cứu lịch sử kiểm tra có phân trang và filter.
     *
     * Hỗ trợ filter theo:
     * - productId: ID sản phẩm
     * - templateId: ID template kiểm tra
     * - startDate/endDate: khoảng thời gian kiểm tra
     * - overallResult: kết quả tổng (pass/fail)
     * - inspector: người kiểm tra
     *
     * Chỉ trả về các phiên đã hoàn thành (status = approved hoặc rejected).
     *
     * @param page Thông tin phân trang
     * @param productId Filter theo sản phẩm (optional)
     * @param templateId Filter theo template (optional)
     * @param startDate Filter từ ngày (optional)
     * @param endDate Filter đến ngày (optional)
     * @param overallResult Filter theo kết quả: pass, fail (optional)
     * @param inspector Filter theo người kiểm tra (optional)
     * @return Trang kết quả InspectionHistoryVO
     */
    IPage<InspectionHistoryVO> getInspectionHistory(Page<InspectionExecution> page,
                                                     String productId,
                                                     String templateId,
                                                     Date startDate,
                                                     Date endDate,
                                                     String overallResult,
                                                     String inspector);

    /**
     * Tính toán thống kê pass/fail theo template và theo field.
     *
     * Logic:
     * 1. Query tất cả InspectionExecution theo templateId trong dateRange có overall_result != null
     * 2. Tính tỷ lệ pass/fail tổng thể cho template
     * 3. Query tất cả FieldValue liên quan, group by fieldId
     * 4. Tính tỷ lệ pass/fail cho từng field (chỉ tính field có result = pass hoặc fail)
     *
     * @param templateId ID template cần thống kê
     * @param startDate Từ ngày (optional, null = không giới hạn)
     * @param endDate Đến ngày (optional, null = không giới hạn)
     * @return InspectionStatisticsVO chứa thống kê tổng và theo field
     */
    InspectionStatisticsVO getStatistics(String templateId, Date startDate, Date endDate);

    /**
     * Pareto analysis: xác định top 5 fields có tỷ lệ fail cao nhất.
     *
     * Logic:
     * 1. Query tất cả FieldValue thuộc các execution của templateId trong dateRange
     * 2. Group by fieldId, tính fail_count và total_count cho mỗi field
     * 3. Tính fail_rate = fail_count / total_count cho mỗi field
     * 4. Sắp xếp theo fail_rate giảm dần, lấy top 5
     * 5. Tính cumulative_rate (tỷ lệ tích lũy) cho biểu đồ Pareto
     *
     * @param templateId ID template cần phân tích
     * @param startDate Từ ngày (optional)
     * @param endDate Đến ngày (optional)
     * @return ParetoAnalysisVO chứa top 5 fields fail nhiều nhất
     */
    ParetoAnalysisVO getParetoAnalysis(String templateId, Date startDate, Date endDate);

    /**
     * Xu hướng pass/fail theo thời gian.
     *
     * Logic:
     * 1. Query tất cả InspectionExecution theo templateId trong dateRange có overall_result != null
     * 2. Group by period (daily/weekly/monthly) dựa trên inspection_date hoặc create_time
     * 3. Tính pass_count, fail_count, total, pass_rate cho mỗi period
     * 4. Sắp xếp theo thời gian tăng dần
     *
     * Interval:
     * - "daily": group theo ngày (format: yyyy-MM-dd)
     * - "weekly": group theo tuần (format: yyyy-'W'ww)
     * - "monthly": group theo tháng (format: yyyy-MM)
     *
     * @param templateId ID template cần phân tích
     * @param startDate Từ ngày (optional)
     * @param endDate Đến ngày (optional)
     * @param interval Khoảng thời gian: "daily", "weekly", "monthly"
     * @return InspectionTrendVO chứa data points theo thời gian
     */
    InspectionTrendVO getTrend(String templateId, Date startDate, Date endDate, String interval);
}
