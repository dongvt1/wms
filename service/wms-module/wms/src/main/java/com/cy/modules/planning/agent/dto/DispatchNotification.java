package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO thông báo giao hàng gửi đến ERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchNotification {

    /** Mã đơn hàng */
    private String orderId;

    /** Tên khách hàng */
    private String customerName;

    /** Danh sách sản phẩm cần giao */
    private List<DispatchItem> items;

    /** Kho xuất hàng */
    private String sourceWarehouse;

    /** Ghi chú giao hàng */
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DispatchItem {
        /** Mã sản phẩm */
        private String productId;

        /** Loại sản phẩm */
        private String productType;

        /** Số lượng giao */
        private BigDecimal quantity;

        /** Đơn vị tính */
        private String unit;
    }
}
