package com.cy.modules.qms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Tham số input của công đoạn kiểm tra")
@TableName("qms_qc_stage_param")
public class QcStageParam implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    @Schema(description = "FK → wh_qc_stage")
    private String stageId;

    @Schema(description = "Tên tham số")
    private String paramName;

    @Schema(description = "Mã tham số")
    private String paramCode;

    @Schema(description = "Kiểu nhập: text | number | pass_fail | select | date | list")
    private String inputType;

    @Schema(description = "Đơn vị đo")
    private String unit;

    @Schema(description = "Giá trị mặc định")
    private String defaultValue;

    @Schema(description = "Giá trị tối thiểu (type=number)")
    private BigDecimal minValue;

    @Schema(description = "Giá trị tối đa (type=number)")
    private BigDecimal maxValue;

    @Schema(description = "JSON options khi type=select")
    private String optionsJson;

    @Schema(description = "Bắt buộc: 1=có, 0=không")
    private Integer isRequired;

    @Schema(description = "Thứ tự hiển thị")
    private Integer sortOrder;

    @Schema(description = "Ghi chú")
    private String notes;
}
