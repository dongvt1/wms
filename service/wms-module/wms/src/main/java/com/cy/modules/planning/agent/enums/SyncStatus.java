package com.cy.modules.planning.agent.enums;

import lombok.Getter;

/**
 * Trạng thái đồng bộ hệ thống
 */
@Getter
public enum SyncStatus {
    ACTIVE("active", "Hoạt động"),
    FAILED("failed", "Thất bại"),
    STALE("stale", "Dữ liệu cũ");

    private final String value;
    private final String description;

    SyncStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static SyncStatus fromValue(String value) {
        for (SyncStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown SyncStatus: " + value);
    }
}
