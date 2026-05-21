package com.cy.modules.qms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: VO giá trị trường dữ liệu (output, nested trong StepResultVO)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO giá trị trường dữ liệu")
public class FieldValueVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "FK → qms_step_field (snapshot)")
    private String fieldId;

    @Schema(description = "Tên trường (snapshot)")
    private String fieldName;

    @Schema(description = "Kiểu trường (snapshot)")
    private String fieldType;

    @Schema(description = "Cấu hình trường (snapshot, JSON object)")
    private Object fieldConfig;

    @Schema(description = "Bắt buộc (snapshot)")
    private Integer isRequired;

    @Schema(description = "Giá trị thực tế nhập")
    private String actualValue;

    @Schema(description = "Kết quả: pass, fail, na")
    private String result;

    @Schema(description = "Thông báo đánh giá (vd: 'Trong dung sai [4.5, 5.5]')")
    private String evalMessage;
}
