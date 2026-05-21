package com.cy.modules.qms.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Loại giai đoạn kiểm tra chất lượng
 */
public enum StageType {
    IQC("iqc", "Incoming Quality Control"),
    PQC("pqc", "Process Quality Control"),
    FQC("fqc", "Final Quality Control");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    StageType(String value, String description) {
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
