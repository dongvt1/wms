package com.cy.modules.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * VO báo cáo tồn kho
 */
@Data
@Schema(name = "VO báo cáo tồn kho", description = "VO báo cáo tồn kho")
public class InventoryReportVO {

    @Schema(description = "Danh sách tồn kho")
    private List<InventoryItemVO> records;

    @Schema(description = "Tổng số bản ghi")
    private Long total;

    @Schema(description = "Số bản ghi mỗi trang")
    private Long size;

    @Schema(description = "Trang hiện tại")
    private Long current;

    @Schema(description = "Tổng số trang")
    private Long pages;

    @Schema(description = "Thông tin tổng hợp")
    private InventorySummaryVO summary;

    @Data
    @Schema(name = "VO mục tồn kho", description = "VO mục tồn kho")
    public static class InventoryItemVO {
            @Schema(description = "ID sản phẩm")
        private String productId;

            @Schema(description = "Mã sản phẩm")
        private String productCode;

            @Schema(description = "Tên sản phẩm")
        private String productName;

            @Schema(description = "Số lượng tồn kho")
        private Integer quantity;

            @Schema(description = "Đơn giá")
        private Double unitPrice;

            @Schema(description = "Tổng giá trị")
        private Double totalValue;

            @Schema(description = "Ngưỡng tồn kho tối thiểu")
        private Integer minStockThreshold;

            @Schema(description = "Trạng thái")
        private String status;
    }

    @Data
    @Schema(name = "VO tổng hợp tồn kho", description = "VO tổng hợp tồn kho")
    public static class InventorySummaryVO {
            @Schema(description = "Tổng số sản phẩm")
        private Integer totalProducts;

            @Schema(description = "Tổng số lượng tồn kho")
        private Integer totalQuantity;

            @Schema(description = "Tổng giá trị")
        private Double totalValue;

            @Schema(description = "Số sản phẩm tồn kho thấp")
        private Integer lowStockProducts;

            @Schema(description = "Số sản phẩm hết hàng")
        private Integer outOfStockProducts;
    }
}