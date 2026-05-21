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
 * @Description: Thông báo QMS (in-app notification)
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Thông báo QMS")
@TableName("qms_notification")
public class QmsNotification implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private String id;

    /** Người nhận thông báo */
    @Schema(description = "ID người nhận thông báo")
    private String userId;

    /** Tiêu đề thông báo */
    @Schema(description = "Tiêu đề thông báo")
    private String title;

    /** Nội dung thông báo */
    @Schema(description = "Nội dung chi tiết")
    private String content;

    /** Loại thực thể liên kết */
    @Schema(description = "Loại thực thể: iqc/pqc/fqc/ncr/review")
    private String entityType;

    /** ID thực thể liên kết */
    @Schema(description = "ID thực thể liên kết")
    private String entityId;

    /** Đã đọc */
    @Schema(description = "Đã đọc: 0=chưa, 1=đã đọc")
    private Integer isRead;

    /** Thời gian tạo */
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian tạo thông báo")
    private Date createTime;
}
