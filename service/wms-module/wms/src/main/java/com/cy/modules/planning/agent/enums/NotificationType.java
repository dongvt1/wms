package com.cy.modules.planning.agent.enums;

import lombok.Getter;

/**
 * Loại thông báo của Planning Agent
 */
@Getter
public enum NotificationType {
    ORDER_INCOMPLETE("ORDER_INCOMPLETE", "Đơn hàng thiếu thông tin"),
    ORDER_INVALID("ORDER_INVALID", "Đơn hàng không hợp lệ"),
    MATERIAL_SHORTAGE("MATERIAL_SHORTAGE", "Thiếu nguyên vật liệu"),
    DEADLINE_AT_RISK("DEADLINE_AT_RISK", "Nguy cơ trễ deadline"),
    PLAN_GENERATED("PLAN_GENERATED", "Kế hoạch đã được tạo"),
    DEVIATION_DETECTED("DEVIATION_DETECTED", "Phát hiện sai lệch"),
    RESCHEDULE_NEEDED("RESCHEDULE_NEEDED", "Cần điều chỉnh kế hoạch"),
    QUALITY_ALERT("QUALITY_ALERT", "Cảnh báo chất lượng"),
    SYNC_FAILURE("SYNC_FAILURE", "Lỗi đồng bộ"),
    SYSTEM_ERROR("SYSTEM_ERROR", "Lỗi hệ thống");

    private final String value;
    private final String description;

    NotificationType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static NotificationType fromValue(String value) {
        for (NotificationType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown NotificationType: " + value);
    }
}
