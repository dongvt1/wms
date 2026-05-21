package com.cy.modules.qms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Description: DTO tạo Inspection Execution (input)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "DTO tạo phiên kiểm tra")
public class InspectionExecutionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "ID sản phẩm không được để trống")
    @Schema(description = "FK → product", required = true)
    private String productId;

    @NotBlank(message = "Loại giai đoạn QC không được để trống")
    @Schema(description = "Loại giai đoạn: iqc, pqc, fqc", required = true)
    private String stageType;

    @Schema(description = "FK → pl_work_order (tùy chọn)")
    private String workOrderId;

    @Schema(description = "FK → pl_production_stage (tùy chọn - link to routing step)")
    private String productionStageId;

    @Schema(description = "Ghi chú")
    private String notes;
}
