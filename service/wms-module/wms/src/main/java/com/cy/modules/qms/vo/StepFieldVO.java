package com.cy.modules.qms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: VO trường dữ liệu (output, nested trong InspectionStepVO)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO trường dữ liệu trong bước kiểm tra")
public class StepFieldVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "FK → qms_inspection_step")
    private String stepId;

    @Schema(description = "Tên trường")
    private String fieldName;

    @Schema(description = "Mã trường (dùng cho API)")
    private String fieldCode;

    @Schema(description = "Kiểu: text, number, boolean, select, measurement")
    private String fieldType;

    @Schema(description = "Đơn vị đo")
    private String unit;

    @Schema(description = "Giá trị mặc định")
    private String defaultValue;

    @Schema(description = "1=bắt buộc")
    private Integer isRequired;

    @Schema(description = "Thứ tự hiển thị")
    private Integer sortOrder;

    @Schema(description = "Cấu hình theo field_type (JSON object)")
    private Object fieldConfig;

    @Schema(description = "Ghi chú hướng dẫn nhập")
    private String hint;
}
