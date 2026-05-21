package com.cy.modules.qms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Description: DTO cho Step Field (nested trong InspectionStepDTO)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "DTO trường dữ liệu trong bước kiểm tra")
public class StepFieldDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID (chỉ dùng khi cập nhật)")
    private String id;

    @NotBlank(message = "Tên trường không được để trống")
    @Schema(description = "Tên trường", required = true)
    private String fieldName;

    @Schema(description = "Mã trường (dùng cho API)")
    private String fieldCode;

    @NotBlank(message = "Kiểu dữ liệu không được để trống")
    @Schema(description = "Kiểu: text, number, boolean, select, measurement", required = true)
    private String fieldType;

    @Schema(description = "Đơn vị đo")
    private String unit;

    @Schema(description = "Giá trị mặc định")
    private String defaultValue;

    @Schema(description = "1=bắt buộc", defaultValue = "1")
    private Integer isRequired;

    @Schema(description = "Thứ tự hiển thị")
    private Integer sortOrder;

    @Schema(description = "Cấu hình theo field_type (JSON object)")
    private Object fieldConfig;

    @Schema(description = "Ghi chú hướng dẫn nhập")
    private String hint;
}
