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
 * @Description: Thời gian giao hàng nhà cung cấp (Supplier Lead Time)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Thời gian giao hàng nhà cung cấp")
@TableName("ap_supplier_lead_time")
public class SupplierLeadTime implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** Mã nguyên vật liệu */
    @Schema(description = "Mã nguyên vật liệu")
    @TableField("material_id")
    private String materialId;

    /** Mã nhà cung cấp */
    @Schema(description = "Mã nhà cung cấp")
    @TableField("supplier_id")
    private String supplierId;

    /** Thời gian giao hàng hiện tại (ngày) */
    @Schema(description = "Thời gian giao hàng (ngày)")
    @TableField("lead_time_days")
    private Integer leadTimeDays;

    /** Thời gian giao hàng thực tế lần cuối */
    @Schema(description = "Thời gian giao hàng thực tế lần cuối (ngày)")
    @TableField("last_actual_days")
    private Integer lastActualDays;

    /** Thời gian giao hàng trung bình (lịch sử) */
    @Schema(description = "Thời gian giao hàng trung bình")
    @TableField("avg_lead_time_days")
    private BigDecimal avgLeadTimeDays;

    /** Thời điểm cập nhật gần nhất */
    @Schema(description = "Thời điểm cập nhật")
    @TableField("last_updated")
    private Date lastUpdated;

    /** Nguồn cập nhật: erp_sync, procurement_cycle */
    @Schema(description = "Nguồn cập nhật")
    @TableField("update_source")
    private String updateSource;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
