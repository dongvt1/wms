package org.jeecg.modules.planning.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Danh mục linh kiện điện tử (Item Master / Part Catalog)
 * @Author: BMad
 * @Date: 2026-02-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Danh mục linh kiện điện tử (Item Master)")
@TableName("wh_item_master")
public class ItemMaster extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã linh kiện nội bộ (Internal Part Number) */
    @Excel(name = "Mã IPN", width = 20)
    @Schema(description = "Internal Part Number – Mã nội bộ")
    private String ipn;

    /** Mã nhà sản xuất (Manufacturer Part Number) */
    @Excel(name = "Mã MPN", width = 30)
    @Schema(description = "Manufacturer Part Number – Mã nhà sản xuất")
    private String mpn;

    /** Tên nhà sản xuất */
    @Excel(name = "Nhà sản xuất", width = 25)
    @Schema(description = "Tên nhà sản xuất")
    private String manufacturerName;

    /** Danh mục: resistor, capacitor, ic, connector, pcb... */
    @Excel(name = "Danh mục", width = 15)
    @Schema(description = "Danh mục: resistor, capacitor, ic, connector, pcb...")
    private String category;

    /** Trị số: 10k Ohm, 10uF, 3.3V... */
    @Excel(name = "Trị số", width = 15)
    @Schema(description = "Trị số: 10k Ohm, 10uF, 3.3V...")
    private String value;

    /** Dung sai */
    @Excel(name = "Dung sai", width = 10)
    @Schema(description = "Dung sai: 1%, 5%, 10%...")
    private String tolerance;

    /** Điện áp định mức */
    @Excel(name = "Điện áp", width = 10)
    @Schema(description = "Điện áp định mức: 16V, 50V...")
    private String voltage;

    /** Kiểu đóng gói */
    @Excel(name = "Package", width = 15)
    @Schema(description = "Kiểu đóng gói: 0402, 0603, QFN-48, SOIC-8...")
    private String packageType;

    /** Trạng thái vòng đời */
    @Excel(name = "Lifecycle", width = 12)
    @Schema(description = "Vòng đời: active, obsolete, nrnd")
    private String lifecycleStatus;

    /** URL Datasheet */
    @Schema(description = "URL hoặc đường dẫn file datasheet (PDF)")
    private String datasheetUrl;

    /** URL bản vẽ 2D */
    @Schema(description = "URL bản vẽ 2D")
    private String drawing2dUrl;

    /** URL bản vẽ 3D */
    @Schema(description = "URL bản vẽ 3D (STEP/IGES)")
    private String drawing3dUrl;

    /** Mô tả */
    @Excel(name = "Mô tả", width = 40)
    @Schema(description = "Mô tả chi tiết")
    private String description;

    /** Thông số kỹ thuật bổ sung (JSON) */
    @Schema(description = "Thông số kỹ thuật bổ sung (JSON)")
    private String specifications;
}
