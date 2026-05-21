package com.cy.modules.qms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: DTO cho Inspection Step (nested trong InspectionTemplateDTO)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "DTO bước kiểm tra")
public class InspectionStepDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID (chỉ dùng khi cập nhật)")
    private String id;

    @NotBlank(message = "Tên bước kiểm tra không được để trống")
    @Schema(description = "Tên bước kiểm tra", required = true)
    private String stepName;

    @Schema(description = "Mô tả bước")
    private String description;

    @Schema(description = "Thứ tự thực hiện")
    private Integer sortOrder;

    @Schema(description = "1=bắt buộc, 0=tùy chọn", defaultValue = "1")
    private Integer isMandatory;

    @Schema(description = "1=cần phê duyệt, 0=không", defaultValue = "0")
    private Integer requiresApproval;

    @Valid
    @Schema(description = "Danh sách trường dữ liệu")
    private List<StepFieldDTO> fields;
}
