package org.jeecg.modules.warehouse.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 库存报告VO
 */
@Data
@ApiModel(value = "库存报告VO", description = "库存报告VO")
public class InventoryReportVO {

    @ApiModelProperty(value = "库存列表")
    private List<InventoryItemVO> records;

    @ApiModelProperty(value = "总数")
    private Long total;

    @ApiModelProperty(value = "页大小")
    private Long size;

    @ApiModelProperty(value = "当前页")
    private Long current;

    @ApiModelProperty(value = "总页数")
    private Long pages;

    @ApiModelProperty(value = "汇总信息")
    private InventorySummaryVO summary;

    @Data
    @ApiModel(value = "库存项VO", description = "库存项VO")
    public static class InventoryItemVO {
        @ApiModelProperty(value = "产品ID")
        private String productId;

        @ApiModelProperty(value = "产品编码")
        private String productCode;

        @ApiModelProperty(value = "产品名称")
        private String productName;

        @ApiModelProperty(value = "库存数量")
        private Integer quantity;

        @ApiModelProperty(value = "单价")
        private Double unitPrice;

        @ApiModelProperty(value = "总价值")
        private Double totalValue;

        @ApiModelProperty(value = "最小库存阈值")
        private Integer minStockThreshold;

        @ApiModelProperty(value = "状态")
        private String status;
    }

    @Data
    @ApiModel(value = "库存汇总VO", description = "库存汇总VO")
    public static class InventorySummaryVO {
        @ApiModelProperty(value = "产品总数")
        private Integer totalProducts;

        @ApiModelProperty(value = "总库存量")
        private Integer totalQuantity;

        @ApiModelProperty(value = "总价值")
        private Double totalValue;

        @ApiModelProperty(value = "低库存产品数")
        private Integer lowStockProducts;

        @ApiModelProperty(value = "缺货产品数")
        private Integer outOfStockProducts;
    }
}