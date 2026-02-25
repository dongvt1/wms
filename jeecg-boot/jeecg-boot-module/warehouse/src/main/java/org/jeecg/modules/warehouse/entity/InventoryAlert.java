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
 * 库存预警表
 */
@Data
@TableName("inventory_alerts")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "库存预警表", description = "库存预警表")
public class InventoryAlert extends JeecgEntity implements Serializable {

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

    /** 预警类型 */
    @Excel(name = "预警类型", width = 15, dicCode = "inventory_alert_type")
    @ApiModelProperty(value = "预警类型")
    @Dict(dicCode = "inventory_alert_type")
    private String alertType;

    /** 当前数量 */
    @Excel(name = "当前数量", width = 15)
    @ApiModelProperty(value = "当前数量")
    private Integer currentQuantity;

    /** 阈值 */
    @Excel(name = "阈值", width = 15)
    @ApiModelProperty(value = "阈值")
    private Integer thresholdValue;

    /** 预警状态 */
    @Excel(name = "预警状态", width = 15, dicCode = "inventory_alert_status")
    @ApiModelProperty(value = "预警状态")
    @Dict(dicCode = "inventory_alert_status")
    private String alertStatus;

    /** 解决时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "解决时间")
    @Excel(name = "解决时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private Date resolvedAt;

    /** 解决人 */
    @Excel(name = "解决人", width = 15)
    @ApiModelProperty(value = "解决人")
    private String resolvedBy;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    @Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}