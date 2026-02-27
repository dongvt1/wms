package com.cy.modules.planning.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: Nhật ký sản xuất
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Nhật ký sản xuất")
@TableName("wh_production_log")
public class ProductionLog implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** ID lệnh sản xuất */
    @Schema(description = "ID lệnh sản xuất")
    private String workOrderId;

    /** ID công đoạn */
    @Schema(description = "ID công đoạn")
    private String stageId;

    /** Thời gian ghi nhận */
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian ghi nhận")
    private Date logTime;

    /** Hành động */
    @Schema(description = "Hành động thực hiện")
    private String action;

    /** Số lượng */
    @Schema(description = "Số lượng liên quan")
    private BigDecimal quantity;

    /** Người thực hiện */
    @Schema(description = "Người thực hiện")
    private String operator;

    /** Ghi chú */
    @Schema(description = "Ghi chú")
    private String notes;
}
