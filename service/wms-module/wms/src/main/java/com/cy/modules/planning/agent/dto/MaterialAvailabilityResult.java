package com.cy.modules.planning.agent.dto;

import com.cy.modules.planning.agent.entity.MaterialAvailability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO kết quả kiểm tra tình trạng nguyên vật liệu cho một đơn hàng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialAvailabilityResult {

    /** ID đơn hàng */
    private String orderId;

    /** Tất cả nguyên vật liệu đều đủ */
    private boolean allAvailable;

    /** Đơn hàng có nguy cơ trễ deadline (lead time vượt deadline) */
    private boolean atRisk;

    /** Danh sách chi tiết tình trạng từng nguyên vật liệu */
    private List<MaterialAvailability> materials;

    /** Danh sách nguyên vật liệu thiếu hụt */
    private List<MaterialAvailability> shortages;

    /** Thông báo lỗi nếu kiểm tra thất bại */
    private String errorMessage;

    /** Kiểm tra có thành công hay không */
    private boolean success;
}
