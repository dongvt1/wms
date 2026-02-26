package com.cy.modules.planning.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Phiên bản BOM (BOM Revision)
 * @Author: BMad
 * @Date: 2026-02-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Phiên bản BOM (BOM Revision)")
@TableName("wh_bom_revision")
public class BomRevision extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** FK tới wh_bom.id */
    @Schema(description = "FK tới wh_bom.id")
    private String bomId;

    /** Mã phiên bản: v1.0, v1.1, v2.0 */
    @Excel(name = "Phiên bản", width = 12)
    @Schema(description = "Mã phiên bản: v1.0, v1.1, v2.0")
    private String revisionCode;

    /** JSON snapshot toàn bộ BOM tại thời điểm lưu */
    @Schema(description = "JSON snapshot toàn bộ BOM")
    private String snapshotData;

    /** Lý do tạo phiên bản */
    @Excel(name = "Lý do", width = 40)
    @Schema(description = "Lý do tạo phiên bản")
    private String reason;

    /** Trạng thái: active, superseded */
    @Excel(name = "Trạng thái", width = 12)
    @Schema(description = "Trạng thái: active, superseded")
    private String status;

    /** FK tới wh_ecn.id nếu tạo từ ECN */
    @Schema(description = "FK tới wh_ecn.id nếu tạo từ ECN")
    private String createdByEcn;
}
