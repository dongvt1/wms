package com.cy.modules.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * VO thống kê đơn hàng
 */
@Data
@Schema(description = "VO thống kê đơn hàng")
public class OrderStatisticsVO {

    @Schema(description = "Tổng số đơn hàng")
    private Integer totalOrders;

    @Schema(description = "Tổng tiền")
    private BigDecimal totalAmount;

    @Schema(description = "Số đơn đang chờ xử lý")
    private Integer pendingCount;

    @Schema(description = "Số đơn đã xác nhận")
    private Integer confirmedCount;

    @Schema(description = "Số đơn đang giao hàng")
    private Integer shippingCount;

    @Schema(description = "Số đơn đã hoàn thành")
    private Integer completedCount;

    @Schema(description = "Số đơn đã huỷ")
    private Integer cancelledCount;

    @Schema(description = "Số đơn hôm nay")
    private Integer todayCount;

    @Schema(description = "Tiền đơn hàng hôm nay")
    private BigDecimal todayAmount;

    @Schema(description = "Số đơn tuần này")
    private Integer weekCount;

    @Schema(description = "Tiền đơn hàng tuần này")
    private BigDecimal weekAmount;

    @Schema(description = "Số đơn tháng này")
    private Integer monthCount;

    @Schema(description = "Tiền đơn hàng tháng này")
    private BigDecimal monthAmount;

    @Schema(description = "Giá trị đơn hàng trung bình")
    private BigDecimal averageOrderAmount;

    @Schema(description = "Tỷ lệ hoàn thành đơn hàng")
    private BigDecimal completionRate;

    @Schema(description = "Tỷ lệ huỷ đơn hàng")
    private BigDecimal cancellationRate;
}