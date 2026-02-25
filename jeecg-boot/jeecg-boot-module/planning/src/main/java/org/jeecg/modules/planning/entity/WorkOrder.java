package org.jeecg.modules.planning.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: Lệnh sản xuất (Work Order)
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Lệnh sản xuất")
@TableName("wh_work_order")
public class WorkOrder extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã lệnh sản xuất */
    @Excel(name = "Mã lệnh SX", width = 20)
    @Schema(description = "Mã lệnh sản xuất")
    private String orderCode;

    /** ID BOM */
    @Excel(name = "BOM", width = 25, dictTable = "wh_bom", dicText = "bom_name", dicCode = "id")
    @Schema(description = "ID BOM")
    private String bomId;

    /** ID dây chuyền */
    @Excel(name = "Dây chuyền", width = 25, dictTable = "wh_production_line", dicText = "line_name", dicCode = "id")
    @Schema(description = "ID dây chuyền sản xuất")
    private String productionLineId;

    /** Số lượng kế hoạch */
    @Excel(name = "SL kế hoạch", width = 15)
    @Schema(description = "Số lượng kế hoạch")
    private BigDecimal plannedQuantity;

    /** Số lượng thực tế */
    @Excel(name = "SL thực tế", width = 15)
    @Schema(description = "Số lượng thực tế sản xuất")
    private BigDecimal actualQuantity;

    /** Ngày bắt đầu kế hoạch */
    @Excel(name = "Ngày BĐ kế hoạch", width = 18, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày bắt đầu kế hoạch")
    private Date plannedStartDate;

    /** Ngày kết thúc kế hoạch */
    @Excel(name = "Ngày KT kế hoạch", width = 18, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày kết thúc kế hoạch")
    private Date plannedEndDate;

    /** Ngày bắt đầu thực tế */
    @Excel(name = "Ngày BĐ thực tế", width = 18, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày bắt đầu thực tế")
    private Date actualStartDate;

    /** Ngày kết thúc thực tế */
    @Excel(name = "Ngày KT thực tế", width = 18, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày kết thúc thực tế")
    private Date actualEndDate;

    /** Trạng thái */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái: draft, planned, in_progress, completed, cancelled")
    private String status;

    /** Độ ưu tiên */
    @Excel(name = "Ưu tiên", width = 12)
    @Schema(description = "Độ ưu tiên: low, normal, high, urgent")
    private String priority;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 40)
    @Schema(description = "Ghi chú")
    private String notes;
}
