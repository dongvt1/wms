package com.cy.modules.qms.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Mẫu kiểm tra chất lượng (Inspection Template)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Mẫu kiểm tra chất lượng (Inspection Template)")
@TableName("qms_inspection_template")
public class InspectionTemplate extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã template (TPLyyyyMMddNNN) */
    @Excel(name = "Mã template", width = 20)
    @Schema(description = "Mã template (TPLyyyyMMddNNN)")
    private String templateCode;

    /** Tên template */
    @Excel(name = "Tên template", width = 30)
    @Schema(description = "Tên template")
    private String templateName;

    /** Mô tả */
    @Schema(description = "Mô tả")
    private String description;

    /** Loại giai đoạn: iqc, pqc, fqc */
    @Excel(name = "Loại giai đoạn", width = 15)
    @Schema(description = "Loại giai đoạn: iqc, pqc, fqc")
    private String stageType;

    /** Phiên bản */
    @Excel(name = "Phiên bản", width = 10)
    @Schema(description = "Phiên bản")
    private String version;

    /** Trạng thái: draft, active, obsolete */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái: draft, active, obsolete")
    private String status;

    /** Ghi chú */
    @Schema(description = "Ghi chú")
    private String notes;

    /** Mã tổ chức (multi-tenant) */
    @Schema(description = "Mã tổ chức (multi-tenant)")
    @TableField("sys_org_code")
    private String sysOrgCode;
}
