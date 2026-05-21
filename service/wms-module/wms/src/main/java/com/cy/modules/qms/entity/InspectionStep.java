package com.cy.modules.qms.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Bước kiểm tra trong template
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Bước kiểm tra trong template")
@TableName("qms_inspection_step")
public class InspectionStep implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    /** FK → qms_inspection_template */
    @Schema(description = "FK → qms_inspection_template")
    private String templateId;

    /** Tên bước kiểm tra */
    @Schema(description = "Tên bước kiểm tra")
    private String stepName;

    /** Mô tả bước */
    @Schema(description = "Mô tả bước")
    private String description;

    /** Thứ tự thực hiện */
    @Schema(description = "Thứ tự thực hiện")
    private Integer sortOrder;

    /** 1=bắt buộc, 0=tùy chọn */
    @Schema(description = "1=bắt buộc, 0=tùy chọn")
    private Integer isMandatory;

    /** 1=cần phê duyệt, 0=không */
    @Schema(description = "1=cần phê duyệt, 0=không")
    private Integer requiresApproval;
}
