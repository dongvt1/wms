package com.cy.modules.qms.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Trạng thái của Inspection Execution
 */
public enum ExecutionStatus {
    DRAFT("draft", "Bản nháp"),
    IN_PROGRESS("in_progress", "Đang thực hiện"),
    PENDING_APPROVAL("pending_approval", "Chờ phê duyệt"),
    APPROVED("approved", "Đã phê duyệt"),
    REJECTED("rejected", "Bị từ chối");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    ExecutionStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
