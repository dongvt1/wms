package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO hiển thị dữ liệu dashboard hoàn thành đơn hàng.
 * Bao gồm: số lượng sản xuất, tồn kho, đã giao, % hoàn thành cho mỗi đơn hàng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentDashboardDto {

    /** Mã đơn hàng */
    private String orderId;

    /** Mã đơn hàng từ OrderHub */
    private String externalOrderId;

    /** Tên khách hàng */
    private String customerName;

    /** Loại sản phẩm */
    private String productType;

    /** Số lượng đặt hàng */
    private BigDecimal orderQuantity;

    /** Số lượng đã sản xuất */
    private BigDecimal producedQty;

    /** Số lượng tồn kho thành phẩm */
    private BigDecimal warehouseStock;

    /** Số lượng đã giao */
    private BigDecimal dispatchedQty;

    /** Phần trăm hoàn thành (0-100) */
    private BigDecimal fulfillmentPercentage;

    /** Trạng thái hoàn thành: in_production, partially_fulfilled, fully_fulfilled */
    private String fulfillmentStatus;
}
