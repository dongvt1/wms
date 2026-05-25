package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.entity.MonthlyPlan;
import com.cy.modules.planning.agent.entity.QuarterlyPlan;

import java.util.List;

/**
 * Service quản lý kế hoạch sản xuất quý và tháng.
 * <p>
 * Chức năng chính:
 * - Phân loại nhu cầu sản xuất theo loại sản phẩm cho từng tháng trong quý
 * - Xác nhận công suất tổng hàng tháng so với số lượng kế hoạch
 * - Tạo phương án thay thế khi nhu cầu vượt công suất
 * - Tạo 1-3 phương án kế hoạch tháng xếp hạng
 * - Duyệt kế hoạch tháng
 */
public interface QuarterlyPlanService {

    /**
     * Tạo kế hoạch quý: phân loại nhu cầu theo loại sản phẩm cho từng tháng,
     * xác nhận công suất, tạo phương án thay thế nếu nhu cầu vượt công suất.
     *
     * @param year    năm kế hoạch
     * @param quarter quý (1-4)
     * @return kế hoạch quý đã tạo
     */
    QuarterlyPlan generateQuarterlyPlan(int year, int quarter);

    /**
     * Tạo 1-3 phương án kế hoạch tháng xếp hạng với số lượng theo loại sản phẩm,
     * timeline, dây chuyền được gán, ngày hoàn thành dự kiến.
     * Mỗi phương án hiển thị tỷ lệ sử dụng công suất.
     *
     * @param quarterlyPlanId ID kế hoạch quý
     * @param year            năm
     * @param month           tháng (1-12)
     * @return danh sách phương án kế hoạch tháng (1-3 phương án)
     */
    List<MonthlyPlan> generateMonthlyPlanOptions(String quarterlyPlanId, int year, int month);

    /**
     * Duyệt kế hoạch tháng: đặt status='approved', từ chối các phương án khác
     * cùng tháng.
     *
     * @param monthlyPlanId ID kế hoạch tháng được duyệt
     * @return kế hoạch tháng đã duyệt
     */
    MonthlyPlan approveMonthlyPlan(String monthlyPlanId);
}
