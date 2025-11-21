package org.jeecg.modules.warehouse.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 库存统计VO
 */
@Data
@ApiModel(value = "库存统计VO", description = "库存统计VO")
public class InventoryStatisticsVO {

    @ApiModelProperty(value = "产品总数")
    private Integer totalProducts;

    @ApiModelProperty(value = "总库存量")
    private Integer totalQuantity;

    @ApiModelProperty(value = "预留库存量")
    private Integer totalReserved;

    @ApiModelProperty(value = "可用库存量")
    private Integer totalAvailable;

    @ApiModelProperty(value = "总库存价值")
    private Double totalValue;

    @ApiModelProperty(value = "低库存产品数")
    private Integer lowStockCount;

    @ApiModelProperty(value = "缺货产品数")
    private Integer outOfStockCount;

    @ApiModelProperty(value = "今日入库数量")
    private Integer todayInCount;

    @ApiModelProperty(value = "今日出库数量")
    private Integer todayOutCount;

    @ApiModelProperty(value = "本周入库数量")
    private Integer weekInCount;

    @ApiModelProperty(value = "本周出库数量")
    private Integer weekOutCount;

    @ApiModelProperty(value = "本月入库数量")
    private Integer monthInCount;

    @ApiModelProperty(value = "本月出库数量")
    private Integer monthOutCount;
}