package com.cy.modules.planning.agent.enums;

import lombok.Getter;

/**
 * Trạng thái đơn hàng kế hoạch
 */
@Getter
public enum OrderStatus {
    PENDING("pending", "Chờ xử lý"),
    CONFIRMED("confirmed", "Đã xác nhận"),
    IN_PRODUCTION("in_production", "Đang sản xuất"),
    FULFILLED("fulfilled", "Đã hoàn thành"),
    CANCELLED("cancelled", "Đã hủy");

    private final String value;
    private final String description;

    OrderStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus: " + value);
    }
}
