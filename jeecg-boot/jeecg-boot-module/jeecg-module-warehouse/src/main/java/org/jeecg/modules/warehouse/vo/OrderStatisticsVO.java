package org.jeecg.modules.warehouse.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单统计VO
 */
@Data
@ApiModel(value = "订单统计VO", description = "订单统计VO")
public class OrderStatisticsVO {

    @ApiModelProperty(value = "总订单数")
    private Integer totalOrders;

    @ApiModelProperty(value = "总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "待处理订单数")
    private Integer pendingCount;

    @ApiModelProperty(value = "已确认订单数")
    private Integer confirmedCount;

    @ApiModelProperty(value = "配送中订单数")
    private Integer shippingCount;

    @ApiModelProperty(value = "已完成订单数")
    private Integer completedCount;

    @ApiModelProperty(value = "已取消订单数")
    private Integer cancelledCount;

    @ApiModelProperty(value = "今日订单数")
    private Integer todayCount;

    @ApiModelProperty(value = "今日订单金额")
    private BigDecimal todayAmount;

    @ApiModelProperty(value = "本周订单数")
    private Integer weekCount;

    @ApiModelProperty(value = "本周订单金额")
    private BigDecimal weekAmount;

    @ApiModelProperty(value = "本月订单数")
    private Integer monthCount;

    @ApiModelProperty(value = "本月订单金额")
    private BigDecimal monthAmount;

    @ApiModelProperty(value = "平均订单金额")
    private BigDecimal averageOrderAmount;

    @ApiModelProperty(value = "订单完成率")
    private BigDecimal completionRate;

    @ApiModelProperty(value = "订单取消率")
    private BigDecimal cancellationRate;
}