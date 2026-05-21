package com.cy.modules.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * VO báo cáo đơn hàng
 */
@Data
@Schema(description = "VO báo cáo đơn hàng")
public class OrderReportVO {

    @Schema(description = "Danh sách bản ghi")
    private List<OrderItemVO> records;

    @Schema(description = "Tổng số bản ghi")
    private Long total;

    @Schema(description = "Số bản ghi mỗi trang")
    private Long size;

    @Schema(description = "Trang hiện tại")
    private Long current;

    @Schema(description = "Tổng số trang")
    private Long pages;

    @Schema(description = "Thông tin tổng hợp")
    private OrderSummaryVO summary;

    /**
     * VO mục đơn hàng
     */
    @Data
    @Schema(description = "VO mục đơn hàng")
    public static class OrderItemVO {
        @Schema(description = "ID đơn hàng")
        private String orderId;

        @Schema(description = "Mã đơn hàng")
        private String orderCode;

        @Schema(description = "ID khách hàng")
        private String customerId;

        @Schema(description = "Tên khách hàng")
        private String customerName;

        @Schema(description = "Ngày đặt hàng")
        private Date orderDate;

        @Schema(description = "Trạng thái đơn hàng")
        private String status;

        @Schema(description = "Tổng tiền")
        private BigDecimal totalAmount;

        @Schema(description = "Tiền giảm giá")
        private BigDecimal discountAmount;

        @Schema(description = "Tiền thuế")
        private BigDecimal taxAmount;

        @Schema(description = "Tiền thanh toán cuối")
        private BigDecimal finalAmount;

        @Schema(description = "Ghi chú")
        private String notes;

        @Schema(description = "Người tạo")
        private String createdBy;
    }

    /**
     * VO tổng hợp đơn hàng
     */
    @Data
    @Schema(description = "VO tổng hợp đơn hàng")
    public static class OrderSummaryVO {
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
    }
}