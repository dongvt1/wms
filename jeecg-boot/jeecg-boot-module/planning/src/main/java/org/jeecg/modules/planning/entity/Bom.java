package org.jeecg.modules.planning.entity;

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
 * @Description: Định mức nguyên vật liệu (BOM - Bill of Materials)
 * @Author: BMad
 * @Date: 2026-02-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Định mức nguyên vật liệu (BOM)")
@TableName("wh_bom")
public class Bom extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã BOM */
    @Excel(name = "Mã BOM", width = 20)
    @Schema(description = "Mã BOM")
    private String bomCode;

    /** Tên BOM */
    @Excel(name = "Tên BOM", width = 30)
    @Schema(description = "Tên BOM")
    private String bomName;

    /** ID thành phẩm */
    @Excel(name = "Thành phẩm", width = 25, dictTable = "product", dicText = "name", dicCode = "id")
    @Schema(description = "ID thành phẩm")
    private String productId;

    /** Số lượng thành phẩm đầu ra */
    @Excel(name = "Số lượng TP", width = 15)
    @Schema(description = "Số lượng thành phẩm đầu ra")
    private BigDecimal outputQuantity;

    /** Đơn vị thành phẩm */
    @Excel(name = "Đơn vị", width = 10)
    @Schema(description = "Đơn vị thành phẩm")
    private String unit;

    /** Phiên bản */
    @Excel(name = "Phiên bản", width = 10)
    @Schema(description = "Phiên bản BOM")
    private String version;

    /** Trạng thái: active, inactive */
    @Excel(name = "Trạng thái", width = 12)
    @Schema(description = "Trạng thái: active, inactive")
    private String status;

    /** Ghi chú */
    @Excel(name = "Ghi chú", width = 40)
    @Schema(description = "Ghi chú")
    private String notes;
}
