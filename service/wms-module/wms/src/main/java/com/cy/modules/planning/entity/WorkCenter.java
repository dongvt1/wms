package com.cy.modules.planning.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Trung tâm sản xuất (Work Center)
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Trung tâm sản xuất (Work Center)")
@TableName("pl_work_center")
public class WorkCenter extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã trung tâm sản xuất */
    @Excel(name = "Mã TTSX", width = 20)
    @Schema(description = "Mã trung tâm sản xuất")
    private String centerCode;

    /** Tên trung tâm */
    @Excel(name = "Tên TTSX", width = 30)
    @Schema(description = "Tên trung tâm sản xuất")
    private String centerName;

    /** Loại: machine, labor_team, production_line */
    @Excel(name = "Loại", width = 15)
    @Schema(description = "Loại: machine (máy), labor_team (tổ đội), production_line (dây chuyền)")
    private String centerType;

    /** ID dây chuyền (tuỳ chọn) */
    @Excel(name = "Dây chuyền", width = 25, dictTable = "pl_production_line", dicText = "line_name", dicCode = "id")
    @Schema(description = "ID dây chuyền sản xuất (tuỳ chọn)")
    private String productionLineId;

    /** Năng suất tối đa/giờ */
    @Excel(name = "NS/Giờ", width = 12)
    @Schema(description = "Năng suất tối đa mỗi giờ")
    private BigDecimal capacityPerHour;

    /** Năng suất tối đa/ngày */
    @Excel(name = "NS/Ngày", width = 12)
    @Schema(description = "Năng suất tối đa mỗi ngày")
    private BigDecimal capacityPerDay;

    /** Đơn vị năng suất */
    @Excel(name = "Đơn vị NS", width = 10)
    @Schema(description = "Đơn vị năng suất")
    private String capacityUnit;

    /** Chi phí/giờ */
    @Excel(name = "Chi phí/giờ", width = 15)
    @Schema(description = "Chi phí mỗi giờ")
    private BigDecimal costPerHour;

    /** Thời gian setup máy (phút) */
    @Excel(name = "Setup (phút)", width = 14)
    @Schema(description = "Thời gian chuẩn bị máy (phút)")
    private Integer setupTimeMinutes;

    /** Trạng thái: active, inactive, maintenance */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái: active, inactive, maintenance")
    private String status;

    /** Mô tả */
    @Excel(name = "Mô tả", width = 40)
    @Schema(description = "Mô tả")
    private String description;
}
