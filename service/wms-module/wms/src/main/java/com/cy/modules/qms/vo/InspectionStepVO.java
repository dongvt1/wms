package com.cy.modules.qms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: VO bước kiểm tra (output, nested trong InspectionTemplateVO)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO bước kiểm tra")
public class InspectionStepVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "FK → qms_inspection_template")
    private String templateId;

    @Schema(description = "Tên bước kiểm tra")
    private String stepName;

    @Schema(description = "Mô tả bước")
    private String description;

    @Schema(description = "Thứ tự thực hiện")
    private Integer sortOrder;

    @Schema(description = "1=bắt buộc, 0=tùy chọn")
    private Integer isMandatory;

    @Schema(description = "1=cần phê duyệt, 0=không")
    private Integer requiresApproval;

    @Schema(description = "Danh sách trường dữ liệu")
    private List<StepFieldVO> fields;
}
