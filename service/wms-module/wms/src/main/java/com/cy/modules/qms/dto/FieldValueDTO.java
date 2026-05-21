package com.cy.modules.qms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Description: DTO cho giá trị trường dữ liệu khi submit
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "DTO giá trị trường dữ liệu")
public class FieldValueDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "ID trường không được để trống")
    @Schema(description = "FK → qms_step_field", required = true)
    private String fieldId;

    @Schema(description = "Giá trị thực tế nhập")
    private String value;
}
