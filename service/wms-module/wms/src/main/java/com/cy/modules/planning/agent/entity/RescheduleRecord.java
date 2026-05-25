package com.cy.modules.planning.agent.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * @Description: Lịch sử điều chỉnh kế hoạch (Reschedule Record)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Lịch sử điều chỉnh kế hoạch")
@TableName("ap_reschedule_record")
public class RescheduleRecord extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** FK → ap_weekly_plan (bản gốc) */
    @Schema(description = "ID kế hoạch gốc")
    @TableField("original_plan_id")
    private String originalPlanId;

    /** FK → ap_weekly_plan (phiên bản mới) */
    @Schema(description = "ID kế hoạch mới")
    @TableField("new_plan_id")
    private String newPlanId;

    /** Loại nguyên nhân: deviation, machine_breakdown, material_delay */
    @Schema(description = "Loại nguyên nhân điều chỉnh")
    @TableField("trigger_type")
    private String triggerType;

    /** Chi tiết nguyên nhân điều chỉnh (JSON) */
    @Schema(description = "Chi tiết nguyên nhân")
    @TableField("trigger_details")
    private String triggerDetails;

    /** Danh sách đơn hàng bị ảnh hưởng (JSON) */
    @Schema(description = "Đơn hàng bị ảnh hưởng")
    @TableField("affected_orders")
    private String affectedOrders;

    /** Các phương án điều chỉnh được đề xuất (JSON) */
    @Schema(description = "Phương án điều chỉnh")
    private String options;

    /** Phương án được chọn (1-based) */
    @Schema(description = "Phương án được chọn")
    @TableField("selected_option")
    private Integer selectedOption;

    /** Trạng thái: pending, approved, rejected */
    @Schema(description = "Trạng thái")
    private String status;

    /** Thời điểm phát hiện sai lệch */
    @Schema(description = "Thời điểm phát hiện")
    @TableField("detection_time")
    private Date detectionTime;

    /** Thời điểm đưa ra đề xuất */
    @Schema(description = "Thời điểm đề xuất")
    @TableField("recommendation_time")
    private Date recommendationTime;

    /** Thời điểm quản lý quyết định */
    @Schema(description = "Thời điểm quyết định")
    @TableField("decision_time")
    private Date decisionTime;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
