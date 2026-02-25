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
 * @Description: StockTransaction
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="StockTransaction")
@TableName("stock_transactions")
public class StockTransaction extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Transaction Code */
    @Excel(name="Transaction Code",width=25)
    @Schema(description = "Transaction Code")
    private java.lang.String transactionCode;

    /** Transaction Type */
    @Excel(name="Transaction Type",width=20, dictCode="stock_transaction_type")
    @Schema(description = "Transaction Type")
    private java.lang.String transactionType;

    /** Transaction Date */
    @Excel(name="Transaction Date",width=25, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Transaction Date")
    private java.util.Date transactionDate;

    /** Status */
    @Excel(name="Status",width=20, dictCode="stock_transaction_status")
    @Schema(description = "Status")
    private java.lang.String status;

    /** Created By */
    @Excel(name="Created By",width=15)
    @Schema(description = "Created By")
    private java.lang.String createdBy;

    /** Approved By */
    @Excel(name="Approved By",width=15)
    @Schema(description = "Approved By")
    private java.lang.String approvedBy;

    /** Approved At */
    @Excel(name="Approved At",width=25, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Approved At")
    private java.util.Date approvedAt;

    /** Notes */
    @Excel(name="Notes",width=50)
    @Schema(description = "Notes")
    private java.lang.String notes;
}