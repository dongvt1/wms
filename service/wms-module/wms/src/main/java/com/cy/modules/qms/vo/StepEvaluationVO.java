package com.cy.modules.qms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: VO kết quả đánh giá sau khi submit giá trị cho một bước
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO kết quả đánh giá bước kiểm tra")
public class StepEvaluationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Kết quả bước: pass, fail")
    private String stepResult;

    @Schema(description = "Danh sách kết quả đánh giá từng trường")
    private List<FieldEvaluationVO> fieldResults;

    /**
     * Kết quả đánh giá cho một trường cụ thể
     */
    @Data
    @Schema(description = "VO kết quả đánh giá trường dữ liệu")
    public static class FieldEvaluationVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "FK → qms_step_field")
        private String fieldId;

        @Schema(description = "Giá trị thực tế nhập")
        private String value;

        @Schema(description = "Kết quả: pass, fail, na")
        private String result;

        @Schema(description = "Thông báo đánh giá (vd: 'Trong dung sai [4.5, 5.5]')")
        private String message;
    }
}
