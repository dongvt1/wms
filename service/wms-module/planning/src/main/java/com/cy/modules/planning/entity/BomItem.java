package com.cy.modules.planning.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Chi tiết nguyên vật liệu trong BOM
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Chi tiết nguyên vật liệu trong BOM")
@TableName("wh_bom_item")
public class BomItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    /** ID BOM */
    @Schema(description = "ID BOM")
    private String bomId;

    /** ID nguyên vật liệu */
    @Excel(name = "Nguyên vật liệu", width = 25, dictTable = "product", dicText = "name", dicCode = "id")
    @Schema(description = "ID nguyên vật liệu")
    private String materialId;

    /** ID BOM con (nếu vật tư là bán thành phẩm) */
    @Schema(description = "ID BOM con – tham chiếu tới wh_bom.id nếu vật tư là bán thành phẩm")
    private String childBomId;

    /** Loại vật tư: raw_material, sub_assembly */
    @Excel(name = "Loại vật tư", width = 15)
    @Schema(description = "Loại vật tư: raw_material (NVL), sub_assembly (bán thành phẩm)")
    private String itemType;

    /** Số lượng NVL */
    @Excel(name = "Số lượng", width = 12)
    @Schema(description = "Số lượng nguyên vật liệu cần")
    private BigDecimal quantity;

    /** Đơn vị */
    @Excel(name = "Đơn vị", width = 10)
    @Schema(description = "Đơn vị")
    private String unit;

    /** Thời gian mua hàng (ngày) */
    @Excel(name = "Lead Time (ngày)", width = 15)
    @Schema(description = "Thời gian mua hàng (ngày)")
    private Integer purchaseLeadTimeDays;

    /** Tỷ lệ hao hụt (%) */
    @Excel(name = "Hao hụt (%)", width = 12)
    @Schema(description = "Tỷ lệ hao hụt (%)")
    private BigDecimal wastageRate;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 30)
    @Schema(description = "Ghi chú")
    private String notes;

    /** Danh sách RefDes phân cách bởi dấu phẩy: C1,C5,C12 */
    @Excel(name = "RefDes", width = 30)
    @Schema(description = "Danh sách RefDes, phân cách bởi dấu phẩy: C1,C5,C12")
    private String refDesignators;
}
