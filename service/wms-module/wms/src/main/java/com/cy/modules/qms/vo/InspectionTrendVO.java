package com.cy.modules.qms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: VO xu hướng kiểm tra theo thời gian (daily/weekly/monthly)
 * @Author: QMS
 * @Date: 2026-03-15
 * @Version: V1.0
 */
@Data
@Schema(description = "VO xu hướng kiểm tra theo thời gian")
public class InspectionTrendVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID template")
    private String templateId;

    @Schema(description = "Tên template")
    private String templateName;

    @Schema(description = "Khoảng thời gian: daily, weekly, monthly")
    private String interval;

    @Schema(description = "Danh sách data points theo thời gian")
    private List<TrendDataPointVO> dataPoints;

    /**
     * Một điểm dữ liệu trong biểu đồ xu hướng
     */
    @Data
    @Schema(description = "Điểm dữ liệu xu hướng")
    public static class TrendDataPointVO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "Nhãn thời gian (vd: '2026-03-15', '2026-W11', '2026-03')")
        private String period;

        @Schema(description = "Tổng số phiên kiểm tra trong khoảng")
        private long totalExecutions;

        @Schema(description = "Số phiên PASS")
        private long passCount;

        @Schema(description = "Số phiên FAIL")
        private long failCount;

        @Schema(description = "Tỷ lệ PASS (%)")
        private double passRate;
    }
}
