package com.cy.modules.planning.agent.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * @Description: Đơn hàng kế hoạch (Planning Order)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Đơn hàng kế hoạch")
@TableName("ap_planning_order")
public class PlanningOrder extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã đơn hàng từ OrderHub */
    @Schema(description = "Mã đơn hàng từ OrderHub")
    @TableField("external_order_id")
    private String externalOrderId;

    /** Loại sản phẩm */
    @Schema(description = "Loại sản phẩm")
    @TableField("product_type")
    private String productType;

    /** Tên khách hàng */
    @Schema(description = "Tên khách hàng")
    @TableField("customer_name")
    private String customerName;

    /** Số lượng đặt hàng */
    @Schema(description = "Số lượng đặt hàng")
    private BigDecimal quantity;

    /** Hạn giao hàng */
    @Schema(description = "Hạn giao hàng")
    private Date deadline;

    /** Thời điểm nhận đơn hàng */
    @Schema(description = "Thời điểm nhận đơn hàng")
    @TableField("receipt_timestamp")
    private Date receiptTimestamp;

    /** Trạng thái: pending, confirmed, in_production, fulfilled, cancelled */
    @Schema(description = "Trạng thái đơn hàng")
    private String status;

    /** Xác thực: valid, incomplete, invalid */
    @Schema(description = "Trạng thái xác thực")
    @TableField("validation_status")
    private String validationStatus;

    /** Chi tiết lỗi xác thực (JSON) */
    @Schema(description = "Chi tiết lỗi xác thực")
    @TableField("validation_errors")
    private String validationErrors;

    /** Vị trí trong hàng đợi ưu tiên */
    @Schema(description = "Vị trí trong hàng đợi ưu tiên")
    @TableField("priority_rank")
    private Integer priorityRank;

    /** Số lượng đã hoàn thành */
    @Schema(description = "Số lượng đã hoàn thành")
    @TableField("fulfillment_qty")
    private BigDecimal fulfillmentQty;

    /** Trạng thái hoàn thành: in_production, partially_fulfilled, fully_fulfilled */
    @Schema(description = "Trạng thái hoàn thành")
    @TableField("fulfillment_status")
    private String fulfillmentStatus;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
