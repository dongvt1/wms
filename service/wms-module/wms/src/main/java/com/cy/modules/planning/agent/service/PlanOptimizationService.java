package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.entity.OptimizationScore;
import com.cy.modules.planning.agent.entity.WeeklyPlan;

import java.util.List;

/**
 * Service interface cho tối ưu hóa kế hoạch sản xuất.
 * Tính toán điểm tối ưu (0-100) dựa trên các yếu tố có trọng số:
 * - Tuân thủ deadline (≥40%)
 * - Sử dụng máy
 * - Sẵn sàng nguyên vật liệu
 * - Ưu tiên đơn hàng
 *
 * Xếp hạng các phương án theo điểm giảm dần, trình bày top 3 lựa chọn.
 * Tích hợp dữ liệu lịch sử 90 ngày (cycle times, defect rates, downtime patterns).
 */
public interface PlanOptimizationService {

    /**
     * Tối ưu hóa kế hoạch tuần: tính toán điểm tối ưu, xác định vi phạm ràng buộc,
     * lưu kết quả vào DB và cập nhật optimization_score trên WeeklyPlan.
     *
     * @param weeklyPlanId ID kế hoạch tuần cần tối ưu
     * @return OptimizationScore chi tiết điểm tối ưu đã lưu
     */
    OptimizationScore optimizeWeeklyPlan(String weeklyPlanId);

    /**
     * Lấy danh sách kế hoạch tuần xếp hạng cao nhất cho một kế hoạch tháng.
     * Sắp xếp theo optimization_score giảm dần, trả về top N.
     *
     * @param monthlyPlanId ID kế hoạch tháng
     * @param topN          số lượng kế hoạch cần trả về (thường là 3)
     * @return danh sách WeeklyPlan xếp hạng cao nhất
     */
    List<WeeklyPlan> getTopRankedPlans(String monthlyPlanId, int topN);
}
