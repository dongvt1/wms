package com.cy.modules.planning.entity;

import java.io.Serializable;
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
 * @Description: Nhà sản xuất được phê duyệt (AML – Approved Manufacturer List)
 * @Author: BMad
 * @Date: 2026-02-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Nhà sản xuất được phê duyệt (AML)")
@TableName("wh_approved_manufacturer")
public class ApprovedManufacturer extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** FK tới wh_item_master.id */
    @Schema(description = "FK tới wh_item_master.id")
    private String itemMasterId;

    /** Tên nhà sản xuất */
    @Excel(name = "Nhà sản xuất", width = 25)
    @Schema(description = "Tên nhà sản xuất")
    private String manufacturerName;

    /** MPN của hãng này */
    @Excel(name = "MPN", width = 30)
    @Schema(description = "Manufacturer Part Number")
    private String mpn;

    /** Thứ tự ưu tiên (1 = cao nhất) */
    @Excel(name = "Ưu tiên", width = 8)
    @Schema(description = "Thứ tự ưu tiên (1 = cao nhất)")
    private Integer priority;

    /** Trạng thái: approved, pending, rejected */
    @Excel(name = "Trạng thái", width = 12)
    @Schema(description = "Trạng thái: approved, pending, rejected")
    private String status;

    /** Ngày đạt chứng nhận */
    @Excel(name = "Ngày QC", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày đạt chứng nhận chất lượng")
    private Date qualificationDate;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 30)
    @Schema(description = "Ghi chú")
    private String notes;
}
