package org.jeecg.modules.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: Order notifications
 * @Author: jeecg
 * @Date: 2025-11-21
 * @Version: V1.0
 */
@Data
@TableName("order_notifications")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="order_notifications对象", description="Order notifications")
public class OrderNotification implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;

    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;

    @Excel(name = "Order ID", width = 15)
    @ApiModelProperty(value = "Order ID")
    private String orderId;

    @Excel(name = "Type", width = 15, dicCode = "notification_type")
    @Dict(dicCode = "notification_type")
    @ApiModelProperty(value = "Notification type (EMAIL, SMS, SYSTEM)")
    private String type;

    @Excel(name = "Recipient", width = 30)
    @ApiModelProperty(value = "Notification recipient (email, phone, user_id)")
    private String recipient;

    @Excel(name = "Subject", width = 30)
    @ApiModelProperty(value = "Notification subject")
    private String subject;

    @Excel(name = "Content", width = 50)
    @ApiModelProperty(value = "Notification content")
    private String content;

    @Excel(name = "Status", width = 15, dicCode = "notification_status")
    @Dict(dicCode = "notification_status")
    @ApiModelProperty(value = "Notification status (PENDING, SENT, FAILED)")
    private String status;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "Sent At", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "Timestamp when notification was sent")
    private Date sentAt;

    @Excel(name = "Error Message", width = 50)
    @ApiModelProperty(value = "Error message if notification failed")
    private String errorMessage;

    @Excel(name = "Retry Count", width = 15)
    @ApiModelProperty(value = "Number of retry attempts")
    private Integer retryCount;
}