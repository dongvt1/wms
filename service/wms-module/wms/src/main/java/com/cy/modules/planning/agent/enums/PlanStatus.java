package com.cy.modules.planning.agent.enums;

import lombok.Getter;

/**
 * Trạng thái kế hoạch sản xuất (quarterly, monthly, weekly)
 */
@Getter
public enum PlanStatus {
    DRAFT("draft", "Bản nháp"),
    ACTIVE("active", "Đang hoạt động"),
    COMPLETED("completed", "Hoàn thành"),
    SUGGESTED("suggested", "Đề xuất"),
    APPROVED("approved", "Đã duyệt"),
    REJECTED("rejected", "Từ chối"),
    IN_EXECUTION("in_execution", "Đang thực hiện"),
    RESCHEDULED("rescheduled", "Đã điều chỉnh");

    private final String value;
    private final String description;

    PlanStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static PlanStatus fromValue(String value) {
        for (PlanStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PlanStatus: " + value);
    }
}
