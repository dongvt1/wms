package org.jeecg.modules.warehouse.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Công đoạn sản xuất
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Công đoạn sản xuất")
@TableName("wh_production_stage")
public class ProductionStage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** ID lệnh sản xuất */
    @Schema(description = "ID lệnh sản xuất")
    private String workOrderId;

    /** Tên công đoạn */
    @Excel(name = "Tên công đoạn", width = 25)
    @Schema(description = "Tên công đoạn")
    private String stageName;

    /** Thứ tự công đoạn */
    @Excel(name = "Thứ tự", width = 10)
    @Schema(description = "Thứ tự công đoạn")
    private Integer stageOrder;

    /** Thời gian kế hoạch (giờ) */
    @Excel(name = "Giờ kế hoạch", width = 14)
    @Schema(description = "Thời gian kế hoạch (giờ)")
    private BigDecimal plannedDurationHours;

    /** Thời gian thực tế (giờ) */
    @Excel(name = "Giờ thực tế", width = 12)
    @Schema(description = "Thời gian thực tế (giờ)")
    private BigDecimal actualDurationHours;

    /** Trạng thái: pending, in_progress, completed, skipped */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái: pending, in_progress, completed, skipped")
    private String status;

    /** Người phụ trách */
    @Excel(name = "Người PTrach", width = 20)
    @Schema(description = "Người phụ trách")
    private String assignee;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 30)
    @Schema(description = "Ghi chú")
    private String notes;

    /** Thời gian tạo */
    @Schema(description = "Thời gian tạo")
    private Date createTime;

    /** Thời gian cập nhật */
    @Schema(description = "Thời gian cập nhật")
    private Date updateTime;
}
