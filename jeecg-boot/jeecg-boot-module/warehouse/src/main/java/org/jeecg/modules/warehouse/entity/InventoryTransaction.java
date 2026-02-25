package org.jeecg.modules.warehouse.entity;

import java.io.Serializable;
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
 * @Description: InventoryTransaction
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="InventoryTransaction")
@TableName("inventory_transactions")
public class InventoryTransaction extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Product ID */
    @Excel(name="Product ID",width=25)
    @Schema(description = "Product ID")
    private java.lang.String productId;

    /** Transaction type (IN, OUT, ADJUST) */
    @Excel(name="Transaction Type",width=15, dictCode="inventory_transaction_type")
    @Schema(description = "Transaction type")
    private java.lang.String transactionType;

    /** Transaction quantity */
    @Excel(name="Quantity",width=15)
    @Schema(description = "Transaction quantity")
    private java.lang.Integer quantity;

    /** Reference ID (stock transaction, order, etc.) */
    @Excel(name="Reference ID",width=25)
    @Schema(description = "Reference ID")
    private java.lang.String referenceId;

    /** Transaction reason */
    @Excel(name="Reason",width=50)
    @Schema(description = "Transaction reason")
    private java.lang.String reason;

    /** User who performed the transaction */
    @Excel(name="User ID",width=15)
    @Schema(description = "User who performed the transaction")
    private java.lang.String userId;

    /** Transaction timestamp */
    @Excel(name="Transaction Time",width=25, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Transaction timestamp")
    private java.util.Date createdAt;
}