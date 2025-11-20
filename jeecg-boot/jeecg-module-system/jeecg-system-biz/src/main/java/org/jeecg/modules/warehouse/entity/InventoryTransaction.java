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
 * 库存交易表
 */
@Data
@TableName("inventory_transactions")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "库存交易表", description = "库存交易表")
public class InventoryTransaction extends JeecgEntity implements Serializable {

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

    /** 交易类型 */
    @Excel(name = "交易类型", width = 15, dicCode = "inventory_transaction_type")
    @ApiModelProperty(value = "交易类型")
    @Dict(dicCode = "inventory_transaction_type")
    private String transactionType;

    /** 交易数量 */
    @Excel(name = "交易数量", width = 15)
    @ApiModelProperty(value = "交易数量")
    private Integer quantity;

    /** 参考ID */
    @Excel(name = "参考ID", width = 15)
    @ApiModelProperty(value = "参考ID")
    private String referenceId;

    /** 交易原因 */
    @Excel(name = "交易原因", width = 30)
    @ApiModelProperty(value = "交易原因")
    private String reason;

    /** 用户ID */
    @ApiModelProperty(value = "用户ID")
    private String userId;

    /** 用户名 */
    @Excel(name = "用户名", width = 15)
    @ApiModelProperty(value = "用户名")
    private String userName;

    /** 交易时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "交易时间")
    @Excel(name = "交易时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}