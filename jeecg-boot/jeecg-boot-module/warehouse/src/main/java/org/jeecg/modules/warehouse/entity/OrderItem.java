package org.jeecg.modules.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单项表
 */
@Data
@TableName("order_items")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "订单项表", description = "订单项表")
public class OrderItem extends JeecgEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;

    /** 订单ID */
    @Excel(name = "订单ID", width = 15)
    @ApiModelProperty(value = "订单ID")
    private String orderId;

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

    /** 数量 */
    @Excel(name = "数量", width = 15)
    @ApiModelProperty(value = "数量")
    private Integer quantity;

    /** 单价 */
    @Excel(name = "单价", width = 15)
    @ApiModelProperty(value = "单价")
    private BigDecimal unitPrice;

    /** 总价 */
    @Excel(name = "总价", width = 15)
    @ApiModelProperty(value = "总价")
    private BigDecimal totalPrice;

    /** 折扣金额 */
    @Excel(name = "折扣金额", width = 15)
    @ApiModelProperty(value = "折扣金额")
    private BigDecimal discountAmount;

    /** 最终金额 */
    @Excel(name = "最终金额", width = 15)
    @ApiModelProperty(value = "最终金额")
    private BigDecimal finalAmount;
}