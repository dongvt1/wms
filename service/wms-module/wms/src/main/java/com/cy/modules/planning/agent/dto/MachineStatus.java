package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO trạng thái máy từ Scada
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineStatus {

    /** Mã máy */
    private String machineId;

    /** Tên máy */
    private String machineName;

    /** Mã dây chuyền sản xuất */
    private String lineId;

    /** Trạng thái: running, idle, breakdown, maintenance */
    private String status;

    /** Thời điểm cập nhật trạng thái */
    private Instant lastUpdated;

    /** Tốc độ hiện tại (đơn vị/giờ) */
    private Double currentSpeed;

    /** Nhiệt độ hiện tại (nếu có) */
    private Double temperature;

    /** Mã lỗi (nếu có) */
    private String errorCode;

    /** Mô tả lỗi (nếu có) */
    private String errorDescription;
}
