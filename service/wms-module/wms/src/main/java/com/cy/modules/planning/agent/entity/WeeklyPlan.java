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
 * @Description: Kế hoạch tuần (Weekly Plan)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Kế hoạch tuần")
@TableName("ap_weekly_plan")
public class WeeklyPlan extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã kế hoạch (WPyyyyWNN-NNN) */
    @Schema(description = "Mã kế hoạch tuần")
    @TableField("plan_code")
    private String planCode;

    /** FK → ap_monthly_plan */
    @Schema(description = "ID kế hoạch tháng")
    @TableField("monthly_plan_id")
    private String monthlyPlanId;

    /** Năm */
    @Schema(description = "Năm")
    @TableField("year")
    private Integer year;

    /** Số tuần ISO */
    @Schema(description = "Số tuần ISO")
    @TableField("week_number")
    private Integer weekNumber;

    /** Ngày bắt đầu tuần */
    @Schema(description = "Ngày bắt đầu tuần")
    @TableField("start_date")
    private Date startDate;

    /** Ngày kết thúc tuần */
    @Schema(description = "Ngày kết thúc tuần")
    @TableField("end_date")
    private Date endDate;

    /** Điểm tối ưu (0-100) */
    @Schema(description = "Điểm tối ưu")
    @TableField("optimization_score")
    private BigDecimal optimizationScore;

    /** Thứ hạng phương án (1-3) */
    @Schema(description = "Thứ hạng phương án")
    @TableField("option_rank")
    private Integer optionRank;

    /** Trạng thái: draft, approved, in_execution, completed, rescheduled */
    @Schema(description = "Trạng thái kế hoạch")
    private String status;

    /** 1=đã xác nhận nguyên vật liệu */
    @Schema(description = "Đã xác nhận nguyên vật liệu")
    @TableField("material_verified")
    private Integer materialVerified;

    /** Người duyệt */
    @Schema(description = "Người duyệt")
    @TableField("approved_by")
    private String approvedBy;

    /** Thời điểm duyệt */
    @Schema(description = "Thời điểm duyệt")
    @TableField("approved_time")
    private Date approvedTime;

    /** Thời điểm phát lệnh sản xuất */
    @Schema(description = "Thời điểm phát lệnh sản xuất")
    @TableField("issued_time")
    private Date issuedTime;

    /** Phiên bản (tăng khi điều chỉnh) */
    @Schema(description = "Phiên bản")
    private Integer version;

    /** FK → ap_weekly_plan (bản gốc trước điều chỉnh) */
    @Schema(description = "ID kế hoạch gốc")
    @TableField("parent_plan_id")
    private String parentPlanId;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
