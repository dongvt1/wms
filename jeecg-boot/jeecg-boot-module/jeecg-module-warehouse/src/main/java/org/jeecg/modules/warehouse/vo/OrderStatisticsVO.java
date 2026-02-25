package org.jeecg.modules.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单统计VO
 */
@Data
@Schema(description = "订单统计VO")
public class OrderStatisticsVO {

    @Schema(description = "总订单数")
    private Integer totalOrders;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "待处理订单数")
    private Integer pendingCount;

    @Schema(description = "已确认订单数")
    private Integer confirmedCount;

    @Schema(description = "配送中订单数")
    private Integer shippingCount;

    @Schema(description = "已完成订单数")
    private Integer completedCount;

    @Schema(description = "已取消订单数")
    private Integer cancelledCount;

    @Schema(description = "今日订单数")
    private Integer todayCount;

    @Schema(description = "今日订单金额")
    private BigDecimal todayAmount;

    @Schema(description = "本周订单数")
    private Integer weekCount;

    @Schema(description = "本周订单金额")
    private BigDecimal weekAmount;

    @Schema(description = "本月订单数")
    private Integer monthCount;

    @Schema(description = "本月订单金额")
    private BigDecimal monthAmount;

    @Schema(description = "平均订单金额")
    private BigDecimal averageOrderAmount;

    @Schema(description = "订单完成率")
    private BigDecimal completionRate;

    @Schema(description = "订单取消率")
    private BigDecimal cancellationRate;
}