package com.cy.modules.qms.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Kết quả đánh giá kiểm tra
 */
public enum EvaluationResult {
    PASS("pass", "Đạt"),
    FAIL("fail", "Không đạt"),
    PENDING("pending", "Chờ đánh giá"),
    NA("na", "Không áp dụng");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    EvaluationResult(String value, String description) {
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
