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
 * @Description: Kế hoạch tháng (Monthly Plan)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Kế hoạch tháng")
@TableName("ap_monthly_plan")
public class MonthlyPlan extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã kế hoạch (MPyyyyMMNNN) */
    @Schema(description = "Mã kế hoạch tháng")
    @TableField("plan_code")
    private String planCode;

    /** FK → ap_quarterly_plan */
    @Schema(description = "ID kế hoạch quý")
    @TableField("quarterly_plan_id")
    private String quarterlyPlanId;

    /** Năm */
    @Schema(description = "Năm")
    @TableField("year")
    private Integer year;

    /** Tháng (1-12) */
    @Schema(description = "Tháng (1-12)")
    @TableField("month")
    private Integer month;

    /** Thứ hạng phương án (1-3) */
    @Schema(description = "Thứ hạng phương án")
    @TableField("option_rank")
    private Integer optionRank;

    /** SL theo sản phẩm, timeline, dây chuyền, ngày hoàn thành (JSON) */
    @Schema(description = "Chi tiết kế hoạch")
    @TableField("plan_details")
    private String planDetails;

    /** Tổng giờ sản xuất kế hoạch */
    @Schema(description = "Tổng giờ sản xuất")
    @TableField("total_hours")
    private BigDecimal totalHours;

    /** Tỷ lệ sử dụng công suất (%) */
    @Schema(description = "Tỷ lệ sử dụng công suất (%)")
    @TableField("capacity_utilization")
    private BigDecimal capacityUtilization;

    /** Trạng thái: suggested, approved, rejected */
    @Schema(description = "Trạng thái kế hoạch")
    private String status;

    /** Người duyệt */
    @Schema(description = "Người duyệt")
    @TableField("approved_by")
    private String approvedBy;

    /** Thời điểm duyệt */
    @Schema(description = "Thời điểm duyệt")
    @TableField("approved_time")
    private Date approvedTime;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
