package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO đại diện cho đơn hàng từ OrderHub
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalOrder {

    /** Mã đơn hàng từ OrderHub */
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

    /** Trạng thái đơn hàng từ hệ thống nguồn */
    private String sourceStatus;
}
