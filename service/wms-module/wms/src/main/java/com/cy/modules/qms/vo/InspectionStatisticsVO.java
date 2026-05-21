package com.cy.modules.qms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: VO thống kê kiểm tra (pass/fail ratio theo template và theo field)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO thống kê kiểm tra")
public class InspectionStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID template")
    private String templateId;

    @Schema(description = "Tên template")
    private String templateName;

    @Schema(description = "Tổng số phiên kiểm tra")
    private long totalExecutions;

    @Schema(description = "Số phiên PASS")
    private long passCount;

    @Schema(description = "Số phiên FAIL")
    private long failCount;

    @Schema(description = "Tỷ lệ PASS (%)")
    private double passRate;

    @Schema(description = "Tỷ lệ FAIL (%)")
    private double failRate;

    @Schema(description = "Thống kê theo field")
    private List<FieldStatisticsVO> fieldStatistics;

    /**
     * VO thống kê theo từng field
     */
    @Data
    @Schema(description = "Thống kê pass/fail theo field")
    public static class FieldStatisticsVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "ID field")
        private String fieldId;

        @Schema(description = "Tên field")
        private String fieldName;

        @Schema(description = "Kiểu field")
        private String fieldType;

        @Schema(description = "Tổng số lần đánh giá")
        private long totalEvaluations;

        @Schema(description = "Số lần PASS")
        private long passCount;

        @Schema(description = "Số lần FAIL")
        private long failCount;

        @Schema(description = "Tỷ lệ PASS (%)")
        private double passRate;

        @Schema(description = "Tỷ lệ FAIL (%)")
        private double failRate;
    }
}
