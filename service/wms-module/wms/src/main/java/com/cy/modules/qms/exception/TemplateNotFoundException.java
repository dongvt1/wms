package com.cy.modules.qms.exception;

import lombok.Getter;

/**
 * Exception thrown when no suitable template is found during resolution.
 * Error code: NO_TEMPLATE_FOUND
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Getter
public class TemplateNotFoundException extends RuntimeException {

    public static final String ERROR_CODE = "NO_TEMPLATE_FOUND";

    private final String productId;
    private final String stageType;

    public TemplateNotFoundException(String productId, String stageType) {
        super("NO_TEMPLATE_FOUND: Không tìm được template phù hợp cho productId="
                + productId + ", stageType=" + stageType);
        this.productId = productId;
        this.stageType = stageType;
    }
}
