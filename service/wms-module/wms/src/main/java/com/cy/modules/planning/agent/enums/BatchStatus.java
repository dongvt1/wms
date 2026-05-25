package com.cy.modules.planning.agent.enums;

import lombok.Getter;

/**
 * Trạng thái batch trong kế hoạch tuần
 */
@Getter
public enum BatchStatus {
    PLANNED("planned", "Đã lên kế hoạch"),
    IN_PROGRESS("in_progress", "Đang thực hiện"),
    COMPLETED("completed", "Hoàn thành"),
    RESCHEDULED("rescheduled", "Đã điều chỉnh"),
    ON_HOLD("on_hold", "Tạm dừng");

    private final String value;
    private final String description;

    BatchStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static BatchStatus fromValue(String value) {
        for (BatchStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown BatchStatus: " + value);
    }
}
