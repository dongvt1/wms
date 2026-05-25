package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO yêu cầu nhập kho thành phẩm gửi đến ERP-WMS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseReceiptRequest {

    /** Mã lệnh sản xuất */
    private String productionOrderId;

    /** Mã batch */
    private String batchId;

    /** Mã sản phẩm */
    private String productId;

    /** Loại sản phẩm */
    private String productType;

    /** Số lượng thành phẩm */
    private BigDecimal quantity;

    /** Mã dây chuyền sản xuất */
    private String productionLineId;

    /** Thời gian hoàn thành sản xuất */
    private LocalDateTime completionTime;

    /** Kho đích */
    private String targetWarehouse;
}
