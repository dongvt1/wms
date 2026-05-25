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
 * @Description: Chi tiết batch trong kế hoạch tuần (Weekly Plan Batch)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Chi tiết batch kế hoạch tuần")
@TableName("ap_weekly_plan_batch")
public class WeeklyPlanBatch implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** FK → ap_weekly_plan */
    @Schema(description = "ID kế hoạch tuần")
    @TableField("weekly_plan_id")
    private String weeklyPlanId;

    /** FK → ap_planning_order */
    @Schema(description = "ID đơn hàng")
    @TableField("order_id")
    private String orderId;

    /** Loại sản phẩm */
    @Schema(description = "Loại sản phẩm")
    @TableField("product_type")
    private String productType;

    /** Số lượng kế hoạch */
    @Schema(description = "Số lượng kế hoạch")
    private BigDecimal quantity;

    /** SL gộp (đã điều chỉnh theo yield) */
    @Schema(description = "Số lượng gộp")
    @TableField("gross_quantity")
    private BigDecimal grossQuantity;

    /** Dây chuyền sản xuất được gán */
    @Schema(description = "ID dây chuyền sản xuất")
    @TableField("production_line_id")
    private String productionLineId;

    /** Máy được gán */
    @Schema(description = "ID máy")
    @TableField("machine_id")
    private String machineId;

    /** Thời điểm bắt đầu kế hoạch */
    @Schema(description = "Thời điểm bắt đầu kế hoạch")
    @TableField("planned_start")
    private Date plannedStart;

    /** Thời điểm kết thúc kế hoạch */
    @Schema(description = "Thời điểm kết thúc kế hoạch")
    @TableField("planned_end")
    private Date plannedEnd;

    /** Thứ tự trên dây chuyền */
    @Schema(description = "Thứ tự trên dây chuyền")
    @TableField("sequence_order")
    private Integer sequenceOrder;

    /** Thời gian chuyển đổi trước batch (phút) */
    @Schema(description = "Thời gian chuyển đổi (phút)")
    @TableField("changeover_minutes")
    private Integer changeoverMinutes;

    /** Thời điểm bắt đầu thực tế */
    @Schema(description = "Thời điểm bắt đầu thực tế")
    @TableField("actual_start")
    private Date actualStart;

    /** Thời điểm kết thúc thực tế */
    @Schema(description = "Thời điểm kết thúc thực tế")
    @TableField("actual_end")
    private Date actualEnd;

    /** Số lượng sản xuất thực tế */
    @Schema(description = "Số lượng sản xuất thực tế")
    @TableField("actual_quantity")
    private BigDecimal actualQuantity;

    /** Trạng thái: planned, in_progress, completed, rescheduled, on_hold */
    @Schema(description = "Trạng thái batch")
    private String status;

    /** Trạng thái nguyên vật liệu: pending, verified, shortage */
    @Schema(description = "Trạng thái nguyên vật liệu")
    @TableField("material_status")
    private String materialStatus;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
