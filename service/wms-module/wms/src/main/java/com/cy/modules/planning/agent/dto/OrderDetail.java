package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO chi tiết đơn hàng từ OrderHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    /** Mã đơn hàng */
    private String orderId;

    /** Loại sản phẩm */
    private String productType;

    /** Tên khách hàng */
    private String customerName;

    /** Số lượng đặt hàng */
    private BigDecimal quantity;

    /** Hạn giao hàng */
    private LocalDate deadline;

    /** Thời điểm nhận đơn hàng */
    private Instant receiptTimestamp;

    /** Trạng thái đơn hàng */
    private String status;

    /** Ghi chú đơn hàng */
    private String notes;

    /** Thông tin bổ sung */
    private Map<String, Object> metadata;

    /** Danh sách sản phẩm chi tiết */
    private List<OrderLineItem> lineItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineItem {
        private String productId;
        private String productName;
        private BigDecimal quantity;
        private String unit;
    }
}
