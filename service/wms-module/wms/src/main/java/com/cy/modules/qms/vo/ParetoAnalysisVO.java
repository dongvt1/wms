package com.cy.modules.qms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: VO Pareto analysis - top fields có tỷ lệ fail cao nhất
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO Pareto analysis")
public class ParetoAnalysisVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID template")
    private String templateId;

    @Schema(description = "Tên template")
    private String templateName;

    @Schema(description = "Top fields có tỷ lệ fail cao nhất (tối đa 5)")
    private List<ParetoItemVO> items;

    /**
     * Một mục trong biểu đồ Pareto
     */
    @Data
    @Schema(description = "Mục Pareto - field có tỷ lệ fail cao")
    public static class ParetoItemVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Thứ hạng (1-5)")
        private int rank;

        @Schema(description = "ID field")
        private String fieldId;

        @Schema(description = "Tên field")
        private String fieldName;

        @Schema(description = "Kiểu field")
        private String fieldType;

        @Schema(description = "Tổng số lần đánh giá")
        private long totalEvaluations;

        @Schema(description = "Số lần FAIL")
        private long failCount;

        @Schema(description = "Tỷ lệ FAIL (%)")
        private double failRate;

        @Schema(description = "Tỷ lệ tích lũy (%)")
        private double cumulativeRate;
    }
}
