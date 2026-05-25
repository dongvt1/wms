package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO yêu cầu tạo lệnh sản xuất gửi đến ERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderRequest {

    /** Mã kế hoạch tuần */
    private String weeklyPlanId;

    /** Mã batch trong kế hoạch */
    private String batchId;

    /** Mã sản phẩm */
    private String productId;

    /** Loại sản phẩm */
    private String productType;

    /** Số lượng sản xuất */
    private BigDecimal quantity;

    /** Mã dây chuyền sản xuất */
    private String productionLineId;

    /** Mã máy */
    private String machineId;

    /** Thời gian bắt đầu dự kiến */
    private LocalDateTime plannedStartTime;

    /** Thời gian kết thúc dự kiến */
    private LocalDateTime plannedEndTime;

    /** Thông số kỹ thuật sản phẩm */
    private String productSpecification;
}
