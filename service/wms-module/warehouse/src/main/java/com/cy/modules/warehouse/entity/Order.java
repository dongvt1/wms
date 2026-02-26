package com.cy.modules.warehouse.entity;

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
 * Bảng đơn hàng
 */
@Data
@TableName("orders")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Order extends JeecgEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** Mã đơn hàng */
    @Excel(name = "Mã đơn hàng", width = 15)
    private String orderCode;

    /** ID khách hàng */
    @Excel(name = "ID khách hàng", width = 15)
    private String customerId;

    /** Tên khách hàng */
    @Excel(name = "Tên khách hàng", width = 30)
    @Dict(dictTable = "customers", dicCode = "customer_name", dicText = "customer_name")
    private String customerName;

    /** Ngày đặt hàng */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "Ngày đặt hàng", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    private Date orderDate;

    /** Trạng thái đơn hàng */
    @Excel(name = "Trạng thái đơn hàng", width = 15, dicCode = "order_status")
    @Dict(dicCode = "order_status")
    private String status;

    /** Tổng tiền */
    @Excel(name = "Tổng tiền", width = 15)
    private BigDecimal totalAmount;

    /** Tiền giảm giá */
    @Excel(name = "Tiền giảm giá", width = 15)
    private BigDecimal discountAmount;

    /** Tiền thuế */
    @Excel(name = "Tiền thuế", width = 15)
    private BigDecimal taxAmount;

    /** Tiền thanh toán cuối */
    @Excel(name = "Tiền thanh toán cuối", width = 15)
    private BigDecimal finalAmount;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 30)
    private String notes;

    /** Người tạo */
    @Excel(name = "Người tạo", width = 15)
    private String createdBy;

    /** Danh sách sản phẩm trong đơn */
    private List<OrderItem> orderItems;
}