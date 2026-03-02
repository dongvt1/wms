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
 * @Description: Dây chuyền sản xuất (Production Line)
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Dây chuyền sản xuất")
@TableName("pl_production_line")
public class ProductionLine extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã dây chuyền */
    @Excel(name = "Mã dây chuyền", width = 20)
    @Schema(description = "Mã dây chuyền")
    private String lineCode;

    /** Tên dây chuyền */
    @Excel(name = "Tên dây chuyền", width = 30)
    @Schema(description = "Tên dây chuyền")
    private String lineName;

    /** Mô tả */
    @Excel(name = "Mô tả", width = 40)
    @Schema(description = "Mô tả")
    private String description;

    /** Năng suất/ngày */
    @Excel(name = "Năng suất/ngày", width = 15)
    @Schema(description = "Năng suất mỗi ngày")
    private BigDecimal capacityPerDay;

    /** Đơn vị năng suất */
    @Excel(name = "Đơn vị", width = 10)
    @Schema(description = "Đơn vị năng suất")
    private String unit;

    /** Trạng thái: active, inactive, maintenance */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái: active, inactive, maintenance")
    private String status;
}
