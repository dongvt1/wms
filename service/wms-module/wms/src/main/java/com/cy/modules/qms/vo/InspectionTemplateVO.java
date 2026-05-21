package com.cy.modules.qms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: VO Inspection Template (output)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO Inspection Template")
public class InspectionTemplateVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "Mã template (TPLyyyyMMddNNN)")
    private String templateCode;

    @Schema(description = "Tên template")
    private String templateName;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Loại giai đoạn: iqc, pqc, fqc")
    private String stageType;

    @Schema(description = "Phiên bản")
    private String version;

    @Schema(description = "Trạng thái: draft, active, obsolete")
    private String status;

    @Schema(description = "Ghi chú")
    private String notes;

    @Schema(description = "Số bước kiểm tra")
    private Integer stepCount;

    @Schema(description = "Người tạo")
    private String createBy;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private Date createTime;

    @Schema(description = "Người cập nhật")
    private String updateBy;

    @JsonFormat(timezone = "GMT+7", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
    private Date updateTime;

    @Schema(description = "Danh sách bước kiểm tra (kèm fields)")
    private List<InspectionStepVO> steps;
}
