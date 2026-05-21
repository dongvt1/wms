package com.cy.modules.qms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: DTO cho submit giá trị của một bước kiểm tra
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "DTO submit giá trị bước kiểm tra")
public class StepValuesDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Valid
    @NotEmpty(message = "Danh sách giá trị không được rỗng")
    @Schema(description = "Danh sách giá trị trường dữ liệu")
    private List<FieldValueDTO> values;
}
