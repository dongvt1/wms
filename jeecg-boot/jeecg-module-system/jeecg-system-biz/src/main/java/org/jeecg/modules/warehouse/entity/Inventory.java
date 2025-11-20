package org.jeecg.modules.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存表
 */
@Data
@TableName("inventory")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "库存表", description = "库存表")
public class Inventory extends JeecgEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;

    /** 产品ID */
    @Excel(name = "产品ID", width = 15)
    @ApiModelProperty(value = "产品ID")
    private String productId;

    /** 产品名称 */
    @Excel(name = "产品名称", width = 30)
    @ApiModelProperty(value = "产品名称")
    @Dict(dictTable = "product", dicCode = "name", dictText = "product_name")
    private String productName;

    /** 产品编码 */
    @Excel(name = "产品编码", width = 15)
    @ApiModelProperty(value = "产品编码")
    @Dict(dictTable = "product", dicCode = "code", dictText = "product_code")
    private String productCode;

    /** 总库存数量 */
    @Excel(name = "总库存数量", width = 15)
    @ApiModelProperty(value = "总库存数量")
    private Integer quantity;

    /** 预留库存数量 */
    @Excel(name = "预留库存数量", width = 15)
    @ApiModelProperty(value = "预留库存数量")
    private Integer reservedQuantity;

    /** 可用库存数量 */
    @Excel(name = "可用库存数量", width = 15)
    @ApiModelProperty(value = "可用库存数量")
    private Integer availableQuantity;

    /** 最小库存阈值 */
    @Excel(name = "最小库存阈值", width = 15)
    @ApiModelProperty(value = "最小库存阈值")
    private Integer minStockThreshold;

    /** 最后更新时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "最后更新时间")
    @Excel(name = "最后更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdated;

    /** 更新人 */
    @ApiModelProperty(value = "更新人")
    @Excel(name = "更新人", width = 15)
    private String updatedBy;
}