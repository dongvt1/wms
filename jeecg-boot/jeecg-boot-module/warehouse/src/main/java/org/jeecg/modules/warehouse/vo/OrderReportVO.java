package org.jeecg.modules.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单报告VO
 */
@Data
@Schema(description = "订单报告VO")
public class OrderReportVO {

    @Schema(description = "记录列表")
    private List<OrderItemVO> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "每页记录数")
    private Long size;

    @Schema(description = "当前页码")
    private Long current;

    @Schema(description = "总页数")
    private Long pages;

    @Schema(description = "汇总信息")
    private OrderSummaryVO summary;

    /**
     * 订单项VO
     */
    @Data
    @Schema(description = "订单项VO")
    public static class OrderItemVO {
        @Schema(description = "订单ID")
        private String orderId;

        @Schema(description = "订单编码")
        private String orderCode;

        @Schema(description = "客户ID")
        private String customerId;

        @Schema(description = "客户名称")
        private String customerName;

        @Schema(description = "订单日期")
        private Date orderDate;

        @Schema(description = "订单状态")
        private String status;

        @Schema(description = "总金额")
        private BigDecimal totalAmount;

        @Schema(description = "折扣金额")
        private BigDecimal discountAmount;

        @Schema(description = "税额")
        private BigDecimal taxAmount;

        @Schema(description = "最终金额")
        private BigDecimal finalAmount;

        @Schema(description = "备注")
        private String notes;

        @Schema(description = "创建人")
        private String createdBy;
    }

    /**
     * 订单汇总VO
     */
    @Data
    @Schema(description = "订单汇总VO")
    public static class OrderSummaryVO {
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
    }
}