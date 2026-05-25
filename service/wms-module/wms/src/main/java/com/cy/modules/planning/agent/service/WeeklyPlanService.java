package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.entity.WeeklyPlan;

import java.util.List;

/**
 * Service quản lý kế hoạch sản xuất tuần.
 * <p>
 * Chức năng chính:
 * - Phân rã kế hoạch tháng đã duyệt thành kế hoạch tuần chi tiết
 * - Gán sản phẩm vào dây chuyền dựa trên năng lực, khả dụng, và changeover time thấp nhất
 * - Giới hạn công suất 90% mỗi dây chuyền mỗi tuần
 * - Sắp xếp batch trên mỗi dây chuyền để tối thiểu tổng changeover time
 * - Xác minh nguyên vật liệu cho từng batch
 * - Đánh dấu batch thiếu nguyên vật liệu và thông báo quản lý sản xuất
 */
public interface WeeklyPlanService {

    /**
     * Tạo kế hoạch tuần từ kế hoạch tháng đã duyệt.
     * <p>
     * Quy trình:
     * 1. Tải kế hoạch tháng đã duyệt, parse plan_details JSON
     * 2. Xác định các tuần trong tháng (ISO week numbers)
     * 3. Phân bổ assignments vào các tuần
     * 4. Gán sản phẩm vào dây chuyền (capability + changeover time thấp nhất)
     * 5. Giới hạn 90% công suất mỗi dây chuyền
     * 6. Sắp xếp batch tối ưu changeover (greedy nearest-neighbor)
     * 7. Xác minh nguyên vật liệu
     * 8. Đánh dấu batch thiếu nguyên vật liệu
     *
     * @param monthlyPlanId ID kế hoạch tháng đã duyệt
     * @return danh sách kế hoạch tuần đã tạo
     */
    List<WeeklyPlan> generateWeeklyPlans(String monthlyPlanId);

    /**
     * Duyệt kế hoạch tuần: đặt status='approved', ghi nhận approved_by và approved_time.
     *
     * @param weeklyPlanId ID kế hoạch tuần cần duyệt
     * @return kế hoạch tuần đã duyệt
     */
    WeeklyPlan approveWeeklyPlan(String weeklyPlanId);
}
