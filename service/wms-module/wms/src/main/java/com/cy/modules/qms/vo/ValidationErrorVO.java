package com.cy.modules.qms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: VO cho validation error responses (HTTP 422)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "VO danh sách lỗi validation")
public class ValidationErrorVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Danh sách lỗi validation")
    private List<ValidationErrorItem> errors;

    /**
     * Một mục lỗi validation cụ thể
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Chi tiết một lỗi validation")
    public static class ValidationErrorItem implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Đường dẫn đến trường lỗi (vd: steps[0].fields[1].fieldConfig)")
        private String path;

        @Schema(description = "Tên trường lỗi (nếu có)")
        private String field;

        @Schema(description = "Thông báo lỗi chi tiết")
        private String message;
    }
}
