package com.cy.modules.qms.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Kiểu dữ liệu của Step Field
 */
public enum FieldType {
    TEXT("text", "Văn bản"),
    NUMBER("number", "Số"),
    BOOLEAN("boolean", "Đạt/Không đạt"),
    SELECT("select", "Chọn từ danh sách"),
    MEASUREMENT("measurement", "Giá trị đo lường");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    FieldType(String value, String description) {
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
