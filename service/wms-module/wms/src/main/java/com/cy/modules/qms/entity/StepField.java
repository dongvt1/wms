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
 * @Description: Trường dữ liệu trong bước kiểm tra
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Trường dữ liệu trong bước kiểm tra")
@TableName("qms_step_field")
public class StepField implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    /** FK → qms_inspection_step */
    @Schema(description = "FK → qms_inspection_step")
    private String stepId;

    /** Tên trường */
    @Schema(description = "Tên trường")
    private String fieldName;

    /** Mã trường (dùng cho API) */
    @Schema(description = "Mã trường (dùng cho API)")
    private String fieldCode;

    /** Kiểu: text, number, boolean, select, measurement */
    @Schema(description = "Kiểu: text, number, boolean, select, measurement")
    private String fieldType;

    /** Đơn vị đo */
    @Schema(description = "Đơn vị đo")
    private String unit;

    /** Giá trị mặc định */
    @Schema(description = "Giá trị mặc định")
    private String defaultValue;

    /** 1=bắt buộc */
    @Schema(description = "1=bắt buộc")
    private Integer isRequired;

    /** Thứ tự hiển thị */
    @Schema(description = "Thứ tự hiển thị")
    private Integer sortOrder;

    /** Cấu hình theo field_type (JSON) */
    @Schema(description = "Cấu hình theo field_type (JSON)")
    private String fieldConfig;

    /** Ghi chú hướng dẫn nhập */
    @Schema(description = "Ghi chú hướng dẫn nhập")
    private String hint;
}
