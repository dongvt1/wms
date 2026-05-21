package com.cy.modules.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: Thông báo đơn hàng
 * @Author: jeecg
 * @Date: 2025-11-21
 * @Version: V1.0
 */
@Data
@TableName("order_notifications")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name="Đối tượng order_notifications", description="Thông báo đơn hàng")
public class OrderNotification implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private String id;

    @Schema(description = "Người tạo")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private Date createTime;

    @Schema(description = "Người cập nhật")
    private String updateBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
    private Date updateTime;

    @Schema(description = "Phòng ban")
    private String sysOrgCode;

    @Excel(name = "ID đơn hàng", width = 15)
    @Schema(description = "ID đơn hàng")
    private String orderId;

    @Excel(name = "Loại", width = 15, dicCode = "notification_type")
    @Dict(dicCode = "notification_type")
    @Schema(description = "Loại thông báo (EMAIL, SMS, SYSTEM)")
    private String type;

    @Excel(name = "Người nhận", width = 30)
    @Schema(description = "Người nhận thông báo (email, điện thoại, user_id)")
    private String recipient;

    @Excel(name = "Tiêu đề", width = 30)
    @Schema(description = "Tiêu đề thông báo")
    private String subject;

    @Excel(name = "Nội dung", width = 50)
    @Schema(description = "Nội dung thông báo")
    private String content;

    @Excel(name = "Trạng thái", width = 15, dicCode = "notification_status")
    @Dict(dicCode = "notification_status")
    @Schema(description = "Trạng thái thông báo (PENDING, SENT, FAILED)")
    private String status;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "Thời gian gửi", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời điểm gửi thông báo")
    private Date sentAt;

    @Excel(name = "Thông báo lỗi", width = 50)
    @Schema(description = "Thông báo lỗi nếu gửi thất bại")
    private String errorMessage;

    @Excel(name = "Số lần thử lại", width = 15)
    @Schema(description = "Số lần thử lại")
    private Integer retryCount;
}