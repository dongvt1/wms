package com.cy.modules.qms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: DTO tạo/cập nhật Inspection Template (input)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "DTO tạo/cập nhật Inspection Template")
public class InspectionTemplateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID (chỉ dùng khi cập nhật)")
    private String id;

    @NotBlank(message = "Tên template không được để trống")
    @Schema(description = "Tên template", required = true)
    private String templateName;

    @Schema(description = "Mô tả")
    private String description;

    @NotBlank(message = "Loại giai đoạn QC không được để trống")
    @Schema(description = "Loại giai đoạn: iqc, pqc, fqc", required = true)
    private String stageType;

    @Schema(description = "Phiên bản")
    private String version;

    @Schema(description = "Ghi chú")
    private String notes;

    @Valid
    @NotNull(message = "Danh sách bước kiểm tra không được null")
    @Size(min = 1, message = "Template phải có ít nhất một bước kiểm tra")
    @Schema(description = "Danh sách bước kiểm tra")
    private List<InspectionStepDTO> steps;
}
