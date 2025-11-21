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
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单状态历史表
 */
@Data
@TableName("order_status_history")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "订单状态历史表", description = "订单状态历史表")
public class OrderStatusHistory extends JeecgEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;

    /** 订单ID */
    @Excel(name = "订单ID", width = 15)
    @ApiModelProperty(value = "订单ID")
    private String orderId;

    /** 订单编码 */
    @Excel(name = "订单编码", width = 15)
    @ApiModelProperty(value = "订单编码")
    @Dict(dictTable = "orders", dicCode = "order_code", dictText = "order_code")
    private String orderCode;

    /** 原状态 */
    @Excel(name = "原状态", width = 15, dicCode = "order_status")
    @ApiModelProperty(value = "原状态")
    @Dict(dicCode = "order_status")
    private String fromStatus;

    /** 新状态 */
    @Excel(name = "新状态", width = 15, dicCode = "order_status")
    @ApiModelProperty(value = "新状态")
    @Dict(dicCode = "order_status")
    private String toStatus;

    /** 状态变更原因 */
    @Excel(name = "状态变更原因", width = 30)
    @ApiModelProperty(value = "状态变更原因")
    private String reason;

    /** 操作人 */
    @ApiModelProperty(value = "操作人")
    @Excel(name = "操作人", width = 15)
    private String userId;

    /** 状态变更时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "状态变更时间")
    @Excel(name = "状态变更时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}