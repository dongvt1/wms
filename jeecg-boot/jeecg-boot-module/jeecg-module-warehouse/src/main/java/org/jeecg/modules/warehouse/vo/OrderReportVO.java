package org.jeecg.modules.warehouse.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单报告VO
 */
@Data
@ApiModel(value = "订单报告VO", description = "订单报告VO")
public class OrderReportVO {

    @ApiModelProperty(value = "记录列表")
    private List<OrderItemVO> records;

    @ApiModelProperty(value = "总记录数")
    private Long total;

    @ApiModelProperty(value = "每页记录数")
    private Long size;

    @ApiModelProperty(value = "当前页码")
    private Long current;

    @ApiModelProperty(value = "总页数")
    private Long pages;

    @ApiModelProperty(value = "汇总信息")
    private OrderSummaryVO summary;

    /**
     * 订单项VO
     */
    @Data
    @ApiModel(value = "订单项VO", description = "订单项VO")
    public static class OrderItemVO {
        @ApiModelProperty(value = "订单ID")
        private String orderId;

        @ApiModelProperty(value = "订单编码")
        private String orderCode;

        @ApiModelProperty(value = "客户ID")
        private String customerId;

        @ApiModelProperty(value = "客户名称")
        private String customerName;

        @ApiModelProperty(value = "订单日期")
        private Date orderDate;

        @ApiModelProperty(value = "订单状态")
        private String status;

        @ApiModelProperty(value = "总金额")
        private BigDecimal totalAmount;

        @ApiModelProperty(value = "折扣金额")
        private BigDecimal discountAmount;

        @ApiModelProperty(value = "税额")
        private BigDecimal taxAmount;

        @ApiModelProperty(value = "最终金额")
        private BigDecimal finalAmount;

        @ApiModelProperty(value = "备注")
        private String notes;

        @ApiModelProperty(value = "创建人")
        private String createdBy;
    }

    /**
     * 订单汇总VO
     */
    @Data
    @ApiModel(value = "订单汇总VO", description = "订单汇总VO")
    public static class OrderSummaryVO {
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
    }
}