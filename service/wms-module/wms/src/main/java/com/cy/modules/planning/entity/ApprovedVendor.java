package com.cy.modules.planning.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Nhà cung cấp được phê duyệt (AVL – Approved Vendor List)
 * @Author: BMad
 * @Date: 2026-02-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Nhà cung cấp được phê duyệt (AVL)")
@TableName("wh_approved_vendor")
public class ApprovedVendor extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** FK tới wh_item_master.id */
    @Schema(description = "FK tới wh_item_master.id")
    private String itemMasterId;

    /** Tên nhà cung cấp */
    @Excel(name = "Nhà cung cấp", width = 25)
    @Schema(description = "Tên nhà cung cấp")
    private String vendorName;

    /** Mã nhà cung cấp */
    @Excel(name = "Mã NCC", width = 15)
    @Schema(description = "Mã nhà cung cấp")
    private String vendorCode;

    /** Thứ tự ưu tiên (1 = cao nhất) */
    @Excel(name = "Ưu tiên", width = 8)
    @Schema(description = "Thứ tự ưu tiên (1 = cao nhất)")
    private Integer priority;

    /** Thời gian giao hàng */
    @Excel(name = "Lead Time (ngày)", width = 15)
    @Schema(description = "Thời gian giao hàng (ngày)")
    private Integer leadTimeDays;

    /** Minimum Order Quantity */
    @Excel(name = "MOQ", width = 10)
    @Schema(description = "Minimum Order Quantity")
    private Integer moq;

    /** Đơn giá */
    @Excel(name = "Đơn giá", width = 12)
    @Schema(description = "Đơn giá")
    private BigDecimal unitPrice;

    /** Đơn vị tiền tệ */
    @Excel(name = "Tiền tệ", width = 8)
    @Schema(description = "Đơn vị tiền tệ")
    private String currency;

    /** Trạng thái: approved, pending, rejected */
    @Excel(name = "Trạng thái", width = 12)
    @Schema(description = "Trạng thái: approved, pending, rejected")
    private String status;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 30)
    @Schema(description = "Ghi chú")
    private String notes;
}
