package com.cy.modules.planning.agent.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * @Description: Kế hoạch quý (Quarterly Plan)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Kế hoạch quý")
@TableName("ap_quarterly_plan")
public class QuarterlyPlan extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã kế hoạch (QPyyyyQN) */
    @Schema(description = "Mã kế hoạch quý")
    @TableField("plan_code")
    private String planCode;

    /** Năm */
    @Schema(description = "Năm")
    @TableField("year")
    private Integer year;

    /** Quý (1-4) */
    @Schema(description = "Quý (1-4)")
    @TableField("quarter")
    private Integer quarter;

    /** Trạng thái: draft, active, completed */
    @Schema(description = "Trạng thái kế hoạch")
    private String status;

    /** Nhu cầu theo loại sản phẩm mỗi tháng (JSON) */
    @Schema(description = "Tổng hợp nhu cầu")
    @TableField("demand_summary")
    private String demandSummary;

    /** 1=đã xác nhận công suất */
    @Schema(description = "Đã xác nhận công suất")
    @TableField("capacity_validated")
    private Integer capacityValidated;

    /** Chi tiết khoảng cách công suất (JSON) */
    @Schema(description = "Khoảng cách công suất")
    @TableField("capacity_gaps")
    private String capacityGaps;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
