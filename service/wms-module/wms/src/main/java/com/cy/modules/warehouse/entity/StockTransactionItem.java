package com.cy.modules.warehouse.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Chi tiết phiếu xuất nhập kho
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Chi tiết phiếu xuất nhập kho")
@TableName("stock_transaction_items")
public class StockTransactionItem extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID phiếu */
    @Excel(name="ID phiếu",width=25)
    @Schema(description = "ID phiếu")
    private java.lang.String transactionId;

    /** ID sản phẩm */
    @Excel(name="ID sản phẩm",width=25)
    @Schema(description = "ID sản phẩm")
    private java.lang.String productId;

    /** Số lượng */
    @Excel(name="Số lượng",width=15)
    @Schema(description = "Số lượng")
    private java.lang.Integer quantity;

    /** Đơn giá */
    @Excel(name="Đơn giá",width=15)
    @Schema(description = "Đơn giá")
    private java.math.BigDecimal unitPrice;

    /** Thành tiền */
    @Excel(name="Thành tiền",width=15)
    @Schema(description = "Thành tiền")
    private java.math.BigDecimal totalPrice;

    /** ID vị trí xuất */
    @Excel(name="Vị trí xuất",width=25)
    @Schema(description = "ID vị trí xuất")
    private java.lang.String fromLocationId;

    /** ID vị trí nhập */
    @Excel(name="Vị trí nhập",width=25)
    @Schema(description = "ID vị trí nhập")
    private java.lang.String toLocationId;

    /** Số lô sản xuất */
    @Excel(name="Số lô",width=25)
    @Schema(description = "Số lô sản xuất")
    private java.lang.String batchNumber;

    /** Ngày hết hạn */
    @Excel(name="Ngày hết hạn",width=25, format = "yyyy-MM-dd")
    @Schema(description = "Ngày hết hạn")
    private java.util.Date expiryDate;
}