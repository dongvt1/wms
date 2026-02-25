package org.jeecg.modules.warehouse.entity;

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
 * @Description: StockTransactionItem
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="StockTransactionItem")
@TableName("stock_transaction_items")
public class StockTransactionItem extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Transaction ID */
    @Excel(name="Transaction ID",width=25)
    @Schema(description = "Transaction ID")
    private java.lang.String transactionId;

    /** Product ID */
    @Excel(name="Product ID",width=25)
    @Schema(description = "Product ID")
    private java.lang.String productId;

    /** Quantity */
    @Excel(name="Quantity",width=15)
    @Schema(description = "Quantity")
    private java.lang.Integer quantity;

    /** Unit Price */
    @Excel(name="Unit Price",width=15)
    @Schema(description = "Unit Price")
    private java.math.BigDecimal unitPrice;

    /** Total Price */
    @Excel(name="Total Price",width=15)
    @Schema(description = "Total Price")
    private java.math.BigDecimal totalPrice;

    /** From Location ID */
    @Excel(name="From Location",width=25)
    @Schema(description = "From Location ID")
    private java.lang.String fromLocationId;

    /** To Location ID */
    @Excel(name="To Location",width=25)
    @Schema(description = "To Location ID")
    private java.lang.String toLocationId;

    /** Batch Number */
    @Excel(name="Batch Number",width=25)
    @Schema(description = "Batch Number")
    private java.lang.String batchNumber;

    /** Expiry Date */
    @Excel(name="Expiry Date",width=25, format = "yyyy-MM-dd")
    @Schema(description = "Expiry Date")
    private java.util.Date expiryDate;
}