package com.cy.modules.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * VO thống kê tồn kho
 */
@Data
@Schema(name = "VO thống kê tồn kho", description = "VO thống kê tồn kho")
public class InventoryStatisticsVO {

    @Schema(description = "Tổng số sản phẩm")
    private Integer totalProducts;

    @Schema(description = "Tổng số lượng tồn kho")
    private Integer totalQuantity;

    @Schema(description = "Số lượng đã đặt trước")
    private Integer totalReserved;

    @Schema(description = "Số lượng khả dụng")
    private Integer totalAvailable;

    @Schema(description = "Tổng giá trị tồn kho")
    private Double totalValue;

    @Schema(description = "Số sản phẩm tồn kho thấp")
    private Integer lowStockCount;

    @Schema(description = "Số sản phẩm hết hàng")
    private Integer outOfStockCount;

    @Schema(description = "Số lượng nhập kho hôm nay")
    private Integer todayInCount;

    @Schema(description = "Số lượng xuất kho hôm nay")
    private Integer todayOutCount;

    @Schema(description = "Số lượng nhập kho tuần này")
    private Integer weekInCount;

    @Schema(description = "Số lượng xuất kho tuần này")
    private Integer weekOutCount;

    @Schema(description = "Số lượng nhập kho tháng này")
    private Integer monthInCount;

    @Schema(description = "Số lượng xuất kho tháng này")
    private Integer monthOutCount;
}