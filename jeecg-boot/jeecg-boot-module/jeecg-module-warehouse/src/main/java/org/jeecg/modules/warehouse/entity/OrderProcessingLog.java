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
 * @Description: Order processing logs
 * @Author: jeecg
 * @Date: 2025-11-21
 * @Version: V1.0
 */
@Data
@TableName("order_processing_logs")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="order_processing_logs对象", description="Order processing logs")
public class OrderProcessingLog implements Serializable {
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

    @Excel(name = "Action", width = 20)
    @ApiModelProperty(value = "Processing action (CONFIRM, CANCEL, SHIP, COMPLETE, etc.)")
    private String action;

    @Excel(name = "Details", width = 50)
    @ApiModelProperty(value = "Processing details")
    private String details;

    @Excel(name = "Status", width = 15, dicCode = "processing_log_status")
    @Dict(dicCode = "processing_log_status")
    @ApiModelProperty(value = "Processing status (SUCCESS, FAILED, PENDING)")
    private String status;

    @Excel(name = "Error Message", width = 50)
    @ApiModelProperty(value = "Error message if processing failed")
    private String errorMessage;

    @Excel(name = "User ID", width = 15)
    @ApiModelProperty(value = "User who performed the action")
    private String userId;

    @Excel(name = "Processing Time", width = 15)
    @ApiModelProperty(value = "Processing time in milliseconds")
    private Integer processingTime;
}