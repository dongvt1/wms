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
 * @Description: Lịch sử phê duyệt kết quả kiểm tra
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Lịch sử phê duyệt kết quả kiểm tra")
@TableName("qms_approval_record")
public class ApprovalRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    /** FK → qms_inspection_execution */
    @Schema(description = "FK → qms_inspection_execution")
    private String executionId;

    /** FK → qms_step_result (nếu phê duyệt từng bước) */
    @Schema(description = "FK → qms_step_result (nếu phê duyệt từng bước)")
    private String stepResultId;

    /** Hành động: approve, reject, re_inspect */
    @Schema(description = "Hành động: approve, reject, re_inspect")
    private String action;

    /** Người phê duyệt */
    @Schema(description = "Người phê duyệt")
    private String approver;

    /** Lý do (bắt buộc khi reject/re_inspect) */
    @Schema(description = "Lý do (bắt buộc khi reject/re_inspect)")
    private String reason;

    /** Thời gian thực hiện */
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian thực hiện")
    private Date actionTime;
}
