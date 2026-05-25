package com.cy.modules.planning.agent.service;

import com.cy.modules.planning.agent.entity.RescheduleRecord;

import java.util.List;

/**
 * Service interface cho điều chỉnh kế hoạch sản xuất (Rescheduling).
 * Giám sát tiến độ sản xuất hàng ngày, phát hiện sai lệch >10%,
 * xử lý sự cố máy hỏng và chậm nguyên vật liệu,
 * tạo phương án điều chỉnh và thông báo các bên liên quan.
 */
public interface ReschedulingService {

    /**
     * Kiểm tra sai lệch sản xuất hàng ngày cho kế hoạch tuần.
     * So sánh actual_qty vs planned_qty cho mỗi batch.
     * Nếu sai lệch >10% → tạo RescheduleRecord với trigger_type='deviation'.
     *
     * @param weeklyPlanId ID kế hoạch tuần cần kiểm tra
     * @return RescheduleRecord nếu phát hiện sai lệch, null nếu không
     */
    RescheduleRecord checkDailyDeviation(String weeklyPlanId);

    /**
     * Xử lý sự kiện máy hỏng: tìm các batch bị ảnh hưởng trên dây chuyền,
     * tạo RescheduleRecord với trigger_type='machine_breakdown',
     * tạo phương án điều chỉnh và thông báo.
     *
     * @param lineId    ID dây chuyền sản xuất bị ảnh hưởng
     * @param machineId ID máy bị hỏng
     * @return RescheduleRecord đã tạo
     */
    RescheduleRecord handleMachineBreakdown(String lineId, String machineId);

    /**
     * Xử lý sự kiện chậm nguyên vật liệu: tìm các batch bị ảnh hưởng,
     * tạo RescheduleRecord với trigger_type='material_delay',
     * tạo phương án điều chỉnh và thông báo.
     *
     * @param materialId ID nguyên vật liệu bị chậm
     * @return RescheduleRecord đã tạo
     */
    RescheduleRecord handleMaterialDelay(String materialId);

    /**
     * Lấy danh sách phương án điều chỉnh cho một reschedule record.
     * Trả về ≥2 phương án xếp hạng theo optimization score.
     *
     * @param rescheduleRecordId ID bản ghi điều chỉnh
     * @return danh sách phương án điều chỉnh (JSON parsed)
     */
    List<RescheduleRecord> getReschedulingOptions(String rescheduleRecordId);
}
