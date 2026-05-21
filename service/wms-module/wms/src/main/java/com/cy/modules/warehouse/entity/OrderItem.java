package com.cy.modules.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Bảng sản phẩm trong đơn hàng
 */
@Data
@TableName("order_items")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "Bảng sản phẩm trong đơn hàng", description = "Bảng sản phẩm trong đơn hàng")
public class OrderItem extends JeecgEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private String id;

    /** ID đơn hàng */
    @Excel(name = "ID đơn hàng", width = 15)
    @Schema(description = "ID đơn hàng")
    private String orderId;

    /** ID sản phẩm */
    @Excel(name = "ID sản phẩm", width = 15)
    @Schema(description = "ID sản phẩm")
    private String productId;

    /** Tên sản phẩm */
    @Excel(name = "Tên sản phẩm", width = 30)
    @Schema(description = "Tên sản phẩm")
    @Dict(dictTable = "product", dicCode = "name", dicText = "product_name")
    private String productName;

    /** Mã sản phẩm */
    @Excel(name = "Mã sản phẩm", width = 15)
    @Schema(description = "Mã sản phẩm")
    @Dict(dictTable = "product", dicCode = "code", dicText = "product_code")
    private String productCode;

    /** Số lượng */
    @Excel(name = "Số lượng", width = 15)
    @Schema(description = "Số lượng")
    private Integer quantity;

    /** Đơn giá */
    @Excel(name = "Đơn giá", width = 15)
    @Schema(description = "Đơn giá")
    private BigDecimal unitPrice;

    /** Tổng giá */
    @Excel(name = "Tổng giá", width = 15)
    @Schema(description = "Tổng giá")
    private BigDecimal totalPrice;

    /** Tiền giảm giá */
    @Excel(name = "Tiền giảm giá", width = 15)
    @Schema(description = "Tiền giảm giá")
    private BigDecimal discountAmount;

    /** Tiền thanh toán cuối */
    @Excel(name = "Tiền thanh toán cuối", width = 15)
    @Schema(description = "Tiền thanh toán cuối")
    private BigDecimal finalAmount;
}