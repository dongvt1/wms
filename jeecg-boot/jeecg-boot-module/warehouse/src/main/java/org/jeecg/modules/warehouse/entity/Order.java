package org.jeecg.modules.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单表
 */
@Data
@TableName("orders")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Order extends JeecgEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 订单编码 */
    @Excel(name = "订单编码", width = 15)
    private String orderCode;

    /** 客户ID */
    @Excel(name = "客户ID", width = 15)
    private String customerId;

    /** 客户名称 */
    @Excel(name = "客户名称", width = 30)
    @Dict(dictTable = "customers", dicCode = "customer_name", dictText = "customer_name")
    private String customerName;

    /** 订单日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "订单日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private Date orderDate;

    /** 订单状态 */
    @Excel(name = "订单状态", width = 15, dicCode = "order_status")
    @Dict(dicCode = "order_status")
    private String status;

    /** 总金额 */
    @Excel(name = "总金额", width = 15)
    private BigDecimal totalAmount;

    /** 折扣金额 */
    @Excel(name = "折扣金额", width = 15)
    private BigDecimal discountAmount;

    /** 税额 */
    @Excel(name = "税额", width = 15)
    private BigDecimal taxAmount;

    /** 最终金额 */
    @Excel(name = "最终金额", width = 15)
    private BigDecimal finalAmount;

    /** 备注 */
    @Excel(name = "备注", width = 30)
    private String notes;

    /** 创建人 */
    @Excel(name = "创建人", width = 15)
    private String createdBy;

    /** 订单项列表 */
    private List<OrderItem> orderItems;
}