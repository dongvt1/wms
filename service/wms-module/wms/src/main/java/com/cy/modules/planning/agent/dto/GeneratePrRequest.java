package com.cy.modules.planning.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO yêu cầu tạo Purchase Request.
 */
@Data
@Schema(description = "Yêu cầu tạo Purchase Request")
public class GeneratePrRequest {

    @NotBlank(message = "orderId không được để trống")
    @Schema(description = "ID đơn hàng kế hoạch")
    private String orderId;

    @NotBlank(message = "materialId không được để trống")
    @Schema(description = "Mã nguyên vật liệu")
    private String materialId;

    @NotNull(message = "deficitQty không được để trống")
    @Positive(message = "deficitQty phải lớn hơn 0")
    @Schema(description = "Số lượng thiếu hụt cần mua")
    private BigDecimal deficitQty;

    @NotNull(message = "productionStartDate không được để trống")
    @Schema(description = "Ngày bắt đầu sản xuất dự kiến")
    private LocalDate productionStartDate;
}
