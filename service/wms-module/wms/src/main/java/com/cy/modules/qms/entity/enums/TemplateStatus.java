package com.cy.modules.qms.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Trạng thái của Inspection Template
 */
public enum TemplateStatus {
    DRAFT("draft", "Bản nháp"),
    ACTIVE("active", "Đang hoạt động"),
    OBSOLETE("obsolete", "Lỗi thời");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    TemplateStatus(String value, String description) {
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
