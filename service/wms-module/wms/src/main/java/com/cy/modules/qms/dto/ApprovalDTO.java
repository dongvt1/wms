package com.cy.modules.qms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: DTO cho hành động phê duyệt/từ chối/yêu cầu kiểm tra lại
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "DTO phê duyệt kết quả kiểm tra")
public class ApprovalDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Nhận xét khi phê duyệt")
    private String comment;

    @Schema(description = "Lý do từ chối (bắt buộc khi reject/re-inspect)")
    private String reason;

    @Schema(description = "ID bước cần kiểm tra lại (dùng cho re-inspect)")
    private String stepId;
}
