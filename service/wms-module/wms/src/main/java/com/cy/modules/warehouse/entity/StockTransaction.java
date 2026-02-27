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
 * @Description: Phiếu xuất nhập kho
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Phiếu xuất nhập kho")
@TableName("stock_transactions")
public class StockTransaction extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã phiếu */
    @Excel(name="Mã phiếu",width=25)
    @Schema(description = "Mã phiếu")
    private java.lang.String transactionCode;

    /** Loại phiếu */
    @Excel(name="Loại phiếu",width=20, dicCode="stock_transaction_type")
    @Schema(description = "Loại phiếu")
    private java.lang.String transactionType;

    /** Ngày thực hiện */
    @Excel(name="Ngày thực hiện",width=25, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày thực hiện")
    private java.util.Date transactionDate;

    /** Trạng thái */
    @Excel(name="Trạng thái",width=20, dicCode="stock_transaction_status")
    @Schema(description = "Trạng thái")
    private java.lang.String status;

    /** Người tạo */
    @Excel(name="Người tạo",width=15)
    @Schema(description = "Người tạo")
    private java.lang.String createdBy;

    /** Người duyệt */
    @Excel(name="Người duyệt",width=15)
    @Schema(description = "Người duyệt")
    private java.lang.String approvedBy;

    /** Thời gian duyệt */
    @Excel(name="Thời gian duyệt",width=25, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian duyệt")
    private java.util.Date approvedAt;

    /** Ghi chú */
    @Excel(name="Ghi chú",width=50)
    @Schema(description = "Ghi chú")
    private java.lang.String notes;
}