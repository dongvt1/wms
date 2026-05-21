package com.cy.modules.qms.exception;

import com.cy.modules.qms.vo.ValidationErrorVO;
import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when template validation fails during activation.
 * Contains the full list of validation errors.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Getter
public class TemplateValidationException extends RuntimeException {

    private final List<ValidationErrorVO.ValidationErrorItem> errors;

    public TemplateValidationException(List<ValidationErrorVO.ValidationErrorItem> errors) {
        super("Template validation failed with " + errors.size() + " error(s)");
        this.errors = errors;
    }

    public TemplateValidationException(String message, List<ValidationErrorVO.ValidationErrorItem> errors) {
        super(message);
        this.errors = errors;
    }
}
