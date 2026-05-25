package com.cy.modules.planning.agent.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Tình trạng nguyên vật liệu (Material Availability)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Tình trạng nguyên vật liệu")
@TableName("ap_material_availability")
public class MaterialAvailability implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** FK → ap_planning_order */
    @Schema(description = "ID đơn hàng")
    @TableField("order_id")
    private String orderId;

    /** Mã nguyên vật liệu từ ERP */
    @Schema(description = "Mã nguyên vật liệu")
    @TableField("material_id")
    private String materialId;

    /** Tên nguyên vật liệu */
    @Schema(description = "Tên nguyên vật liệu")
    @TableField("material_name")
    private String materialName;

    /** Số lượng yêu cầu theo BOM */
    @Schema(description = "Số lượng yêu cầu")
    @TableField("required_qty")
    private BigDecimal requiredQty;

    /** Số lượng tồn kho hiện có */
    @Schema(description = "Số lượng tồn kho")
    @TableField("available_qty")
    private BigDecimal availableQty;

    /** Số lượng thiếu hụt */
    @Schema(description = "Số lượng thiếu hụt")
    @TableField("deficit_qty")
    private BigDecimal deficitQty;

    /** 1=đã đặt trước cho đơn hàng này */
    @Schema(description = "Đã đặt trước")
    private Integer reserved;

    /** Thời gian giao hàng nhà cung cấp (ngày) */
    @Schema(description = "Thời gian giao hàng nhà cung cấp (ngày)")
    @TableField("supplier_lead_days")
    private Integer supplierLeadDays;

    /** Ngày dự kiến nhận hàng */
    @Schema(description = "Ngày dự kiến nhận hàng")
    @TableField("expected_arrival")
    private Date expectedArrival;

    /** Trạng thái: checking, available, shortage, pr_generated, received */
    @Schema(description = "Trạng thái nguyên vật liệu")
    private String status;

    /** Thời điểm kiểm tra gần nhất */
    @Schema(description = "Thời điểm kiểm tra gần nhất")
    @TableField("check_time")
    private Date checkTime;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
