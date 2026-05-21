package com.cy.modules.qms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: Request body cho lưu/submit giá trị field của một step
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "Request body cho lưu/submit giá trị field")
public class StepValuesRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Danh sách giá trị không được để trống")
    @Valid
    @Schema(description = "Danh sách giá trị field cần lưu/submit", required = true)
    private List<FieldValueDTO> values;
}
