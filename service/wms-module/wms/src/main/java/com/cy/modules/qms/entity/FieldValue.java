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
 * @Description: Giá trị trường dữ liệu trong phiên kiểm tra
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Giá trị trường dữ liệu trong phiên kiểm tra")
@TableName("qms_field_value")
public class FieldValue implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    /** FK → qms_step_result */
    @Schema(description = "FK → qms_step_result")
    private String stepResultId;

    /** FK → qms_step_field (snapshot) */
    @Schema(description = "FK → qms_step_field (snapshot)")
    private String fieldId;

    /** Tên trường (snapshot) */
    @Schema(description = "Tên trường (snapshot)")
    private String fieldName;

    /** Kiểu trường (snapshot) */
    @Schema(description = "Kiểu trường (snapshot)")
    private String fieldType;

    /** Cấu hình trường (snapshot) */
    @Schema(description = "Cấu hình trường (snapshot)")
    private String fieldConfig;

    /** Bắt buộc (snapshot) */
    @Schema(description = "Bắt buộc (snapshot)")
    private Integer isRequired;

    /** Giá trị thực tế nhập */
    @Schema(description = "Giá trị thực tế nhập")
    private String actualValue;

    /** Kết quả: pass, fail, na */
    @Schema(description = "Kết quả: pass, fail, na")
    private String result;

    /** Thông báo đánh giá */
    @Schema(description = "Thông báo đánh giá (vd: 'Trong dung sai [4.5, 5.5]')")
    private String evalMessage;
}
