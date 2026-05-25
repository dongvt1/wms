package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO báo cáo chất lượng từ QMS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityReport {

    /** Mã sản phẩm */
    private String productId;

    /** Mã dây chuyền sản xuất */
    private String lineId;

    /** Ngày bắt đầu */
    private LocalDate fromDate;

    /** Ngày kết thúc */
    private LocalDate toDate;

    /** Tỷ lệ lỗi trung bình (%) */
    private BigDecimal averageDefectRate;

    /** Tỷ lệ lỗi trung bình 30 ngày (%) */
    private BigDecimal rollingThirtyDayDefectRate;

    /** Tỷ lệ lỗi trung bình 90 ngày (%) */
    private BigDecimal rollingNinetyDayDefectRate;

    /** Tỷ lệ yield (%) */
    private BigDecimal yieldRate;

    /** Tổng số lượng kiểm tra */
    private BigDecimal totalInspected;

    /** Tổng số lượng lỗi */
    private BigDecimal totalDefects;

    /** Chi tiết kết quả kiểm tra */
    private List<InspectionResult> inspectionResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspectionResult {
        /** Ngày kiểm tra */
        private LocalDate inspectionDate;

        /** Số lượng kiểm tra */
        private BigDecimal inspectedQuantity;

        /** Số lượng đạt */
        private BigDecimal passedQuantity;

        /** Số lượng lỗi */
        private BigDecimal defectQuantity;

        /** Tỷ lệ lỗi (%) */
        private BigDecimal defectRate;
    }
}
