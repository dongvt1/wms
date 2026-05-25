package com.cy.modules.planning.agent.enums;

import lombok.Getter;

/**
 * Trạng thái xác thực đơn hàng
 */
@Getter
public enum ValidationStatus {
    VALID("valid", "Hợp lệ"),
    INCOMPLETE("incomplete", "Thiếu thông tin"),
    INVALID("invalid", "Không hợp lệ");

    private final String value;
    private final String description;

    ValidationStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static ValidationStatus fromValue(String value) {
        for (ValidationStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ValidationStatus: " + value);
    }
}
