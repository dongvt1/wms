package com.cy.modules.warehouse.entity;

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
 * @Description: Giao dịch tồn kho
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Giao dịch tồn kho")
@TableName("inventory_transactions")
public class InventoryTransaction extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID sản phẩm */
    @Excel(name="ID sản phẩm",width=25)
    @Schema(description = "ID sản phẩm")
    private java.lang.String productId;

    /** Loại giao dịch (NHẬP, XUẤT, ĐIỀU CHỈNH) */
    @Excel(name="Loại giao dịch",width=15, dicCode="inventory_transaction_type")
    @Schema(description = "Loại giao dịch")
    private java.lang.String transactionType;

    /** Số lượng giao dịch */
    @Excel(name="Số lượng",width=15)
    @Schema(description = "Số lượng giao dịch")
    private java.lang.Integer quantity;

    /** ID tham chiếu (phiếu xuất nhập, đơn hàng, v.v.) */
    @Excel(name="ID tham chiếu",width=25)
    @Schema(description = "ID tham chiếu")
    private java.lang.String referenceId;

    /** Lý do giao dịch */
    @Excel(name="Lý do",width=50)
    @Schema(description = "Lý do giao dịch")
    private java.lang.String reason;

    /** Người thực hiện giao dịch */
    @Excel(name="ID người dùng",width=15)
    @Schema(description = "Người thực hiện giao dịch")
    private java.lang.String userId;

    /** Thời gian giao dịch */
    @Excel(name="Thời gian giao dịch",width=25, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian giao dịch")
    private java.util.Date createdAt;
}