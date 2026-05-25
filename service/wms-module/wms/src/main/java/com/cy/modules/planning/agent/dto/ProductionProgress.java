package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO tiến độ sản xuất từ Scada
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionProgress {

    /** Mã dây chuyền sản xuất */
    private String lineId;

    /** Ngày báo cáo */
    private LocalDate reportDate;

    /** Thời điểm lấy dữ liệu */
    private Instant collectedAt;

    /** Tổng số lượng sản xuất trong ngày */
    private BigDecimal totalProduced;

    /** Số lượng lỗi */
    private BigDecimal defectCount;

    /** Tỷ lệ hoàn thành (%) */
    private BigDecimal completionPercentage;

    /** Chi tiết theo batch */
    private List<BatchProgress> batches;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchProgress {
        /** Mã batch (nếu có) */
        private String batchId;

        /** Mã sản phẩm */
        private String productId;

        /** Số lượng đã sản xuất */
        private BigDecimal producedQuantity;

        /** Trạng thái: in_progress, completed, paused */
        private String status;
    }
}
