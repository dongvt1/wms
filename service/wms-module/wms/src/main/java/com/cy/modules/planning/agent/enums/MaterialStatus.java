package com.cy.modules.planning.agent.enums;

import lombok.Getter;

/**
 * Trạng thái nguyên vật liệu
 */
@Getter
public enum MaterialStatus {
    CHECKING("checking", "Đang kiểm tra"),
    AVAILABLE("available", "Có sẵn"),
    SHORTAGE("shortage", "Thiếu hụt"),
    PR_GENERATED("pr_generated", "Đã tạo PR"),
    RECEIVED("received", "Đã nhận"),
    PENDING("pending", "Chờ xác nhận"),
    VERIFIED("verified", "Đã xác nhận");

    private final String value;
    private final String description;

    MaterialStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static MaterialStatus fromValue(String value) {
        for (MaterialStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown MaterialStatus: " + value);
    }
}
