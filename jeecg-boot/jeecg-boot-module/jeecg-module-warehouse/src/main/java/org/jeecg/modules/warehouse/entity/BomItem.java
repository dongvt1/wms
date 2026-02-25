package org.jeecg.modules.warehouse.entity;

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

    /** Số lượng NVL */
    @Excel(name = "Số lượng", width = 12)
    @Schema(description = "Số lượng nguyên vật liệu cần")
    private BigDecimal quantity;

    /** Đơn vị */
    @Excel(name = "Đơn vị", width = 10)
    @Schema(description = "Đơn vị")
    private String unit;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 30)
    @Schema(description = "Ghi chú")
    private String notes;
}
