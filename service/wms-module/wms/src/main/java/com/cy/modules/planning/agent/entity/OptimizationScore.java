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
 * @Description: Chi tiết điểm tối ưu (Optimization Score)
 * @Author: AI Planning Agent
 * @Date: 2026-06-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Chi tiết điểm tối ưu")
@TableName("ap_optimization_score")
public class OptimizationScore implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính */
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** FK → ap_weekly_plan */
    @Schema(description = "ID kế hoạch tuần")
    @TableField("weekly_plan_id")
    private String weeklyPlanId;

    /** Tổng điểm tối ưu (0-100) */
    @Schema(description = "Tổng điểm tối ưu")
    @TableField("total_score")
    private BigDecimal totalScore;

    /** Điểm tuân thủ deadline */
    @Schema(description = "Điểm deadline")
    @TableField("deadline_score")
    private BigDecimal deadlineScore;

    /** Trọng số deadline (>=0.40) */
    @Schema(description = "Trọng số deadline")
    @TableField("deadline_weight")
    private BigDecimal deadlineWeight;

    /** Điểm sử dụng máy */
    @Schema(description = "Điểm sử dụng máy")
    @TableField("utilization_score")
    private BigDecimal utilizationScore;

    /** Trọng số sử dụng máy */
    @Schema(description = "Trọng số sử dụng máy")
    @TableField("utilization_weight")
    private BigDecimal utilizationWeight;

    /** Điểm sẵn sàng nguyên vật liệu */
    @Schema(description = "Điểm nguyên vật liệu")
    @TableField("material_score")
    private BigDecimal materialScore;

    /** Trọng số nguyên vật liệu */
    @Schema(description = "Trọng số nguyên vật liệu")
    @TableField("material_weight")
    private BigDecimal materialWeight;

    /** Điểm ưu tiên đơn hàng */
    @Schema(description = "Điểm ưu tiên")
    @TableField("priority_score")
    private BigDecimal priorityScore;

    /** Trọng số ưu tiên */
    @Schema(description = "Trọng số ưu tiên")
    @TableField("priority_weight")
    private BigDecimal priorityWeight;

    /** 1=dùng dữ liệu lịch sử, 0=dùng ước tính */
    @Schema(description = "Sử dụng dữ liệu lịch sử")
    @TableField("historical_data_used")
    private Integer historicalDataUsed;

    /** Danh sách vi phạm ràng buộc (JSON) */
    @Schema(description = "Vi phạm ràng buộc")
    @TableField("constraint_violations")
    private String constraintViolations;

    /** Thời điểm tạo */
    @Schema(description = "Thời điểm tạo")
    @TableField("create_time")
    private Date createTime;
}
