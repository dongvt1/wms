package com.cy.modules.qms.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Hành động phê duyệt
 */
public enum ApprovalAction {
    APPROVE("approve", "Phê duyệt"),
    REJECT("reject", "Từ chối"),
    RE_INSPECT("re_inspect", "Yêu cầu kiểm tra lại");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    ApprovalAction(String value, String description) {
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
