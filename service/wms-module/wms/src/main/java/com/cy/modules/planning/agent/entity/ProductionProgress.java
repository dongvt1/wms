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
 * @Description: Tiến độ sản xuất (Production Progress)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Tiến độ sản xuất hàng ngày")
@TableName("ap_production_progress")
public class ProductionProgress implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** FK → ap_weekly_plan */
    @Schema(description = "ID kế hoạch tuần")
    @TableField("weekly_plan_id")
    private String weeklyPlanId;

    /** FK → ap_weekly_plan_batch */
    @Schema(description = "ID batch")
    @TableField("batch_id")
    private String batchId;

    /** Mã dây chuyền sản xuất */
    @Schema(description = "ID dây chuyền sản xuất")
    @TableField("production_line_id")
    private String productionLineId;

    /** Ngày báo cáo */
    @Schema(description = "Ngày báo cáo")
    @TableField("report_date")
    private Date reportDate;

    /** Số lượng kế hoạch trong ngày */
    @Schema(description = "Số lượng kế hoạch")
    @TableField("planned_qty")
    private BigDecimal plannedQty;

    /** Số lượng sản xuất thực tế */
    @Schema(description = "Số lượng thực tế")
    @TableField("actual_qty")
    private BigDecimal actualQty;

    /** Số lượng lỗi */
    @Schema(description = "Số lượng lỗi")
    @TableField("defect_qty")
    private BigDecimal defectQty;

    /** Tỷ lệ lỗi (0.0000-1.0000) */
    @Schema(description = "Tỷ lệ lỗi")
    @TableField("defect_rate")
    private BigDecimal defectRate;

    /** Phần trăm sai lệch so với kế hoạch */
    @Schema(description = "Phần trăm sai lệch")
    @TableField("deviation_pct")
    private BigDecimal deviationPct;

    /** Phần trăm hoàn thành */
    @Schema(description = "Phần trăm hoàn thành")
    @TableField("completion_pct")
    private BigDecimal completionPct;

    /** Trạng thái máy tại thời điểm báo cáo */
    @Schema(description = "Trạng thái máy")
    @TableField("machine_status")
    private String machineStatus;

    /** Ghi chú */
    @Schema(description = "Ghi chú")
    private String notes;

    /** Thời điểm tạo */
    @Schema(description = "Thời điểm tạo")
    @TableField("create_time")
    private Date createTime;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
