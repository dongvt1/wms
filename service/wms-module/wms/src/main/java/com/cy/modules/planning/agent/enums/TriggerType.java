package com.cy.modules.planning.agent.enums;

import lombok.Getter;

/**
 * Loại nguyên nhân điều chỉnh kế hoạch
 */
@Getter
public enum TriggerType {
    DEVIATION("deviation", "Sai lệch sản xuất"),
    MACHINE_BREAKDOWN("machine_breakdown", "Hỏng máy"),
    MATERIAL_DELAY("material_delay", "Chậm nguyên vật liệu");

    private final String value;
    private final String description;

    TriggerType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static TriggerType fromValue(String value) {
        for (TriggerType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown TriggerType: " + value);
    }
}
