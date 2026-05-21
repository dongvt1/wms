package com.cy.modules.qms.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: Kết quả bước kiểm tra
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Kết quả bước kiểm tra")
@TableName("qms_step_result")
public class StepResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    /** FK → qms_inspection_execution */
    @Schema(description = "FK → qms_inspection_execution")
    private String executionId;

    /** FK → qms_inspection_step (snapshot) */
    @Schema(description = "FK → qms_inspection_step (snapshot)")
    private String stepId;

    /** Tên bước (snapshot) */
    @Schema(description = "Tên bước (snapshot)")
    private String stepName;

    /** Thứ tự (snapshot) */
    @Schema(description = "Thứ tự (snapshot)")
    private Integer sortOrder;

    /** Bắt buộc (snapshot) */
    @Schema(description = "Bắt buộc (snapshot)")
    private Integer isMandatory;

    /** Kết quả: pass, fail, pending */
    @Schema(description = "Kết quả: pass, fail, pending")
    private String result;

    /** Trạng thái: pending, completed, approved, rejected, re_inspect */
    @Schema(description = "Trạng thái: pending, completed, approved, rejected, re_inspect")
    private String status;

    /** Thời gian hoàn thành */
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian hoàn thành")
    private Date completedTime;

    /** Ghi chú */
    @Schema(description = "Ghi chú")
    private String notes;
}
