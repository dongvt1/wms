package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO phân loại sản phẩm lỗi từ QMS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectClassification {

    /** Mã batch */
    private String batchId;

    /** Tổng số lượng lỗi */
    private BigDecimal totalDefects;

    /** Số lượng có thể sửa chữa */
    private BigDecimal repairableQuantity;

    /** Số lượng phải hủy */
    private BigDecimal destroyableQuantity;

    /** Chi tiết phân loại */
    private List<DefectDetail> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefectDetail {
        /** Loại lỗi */
        private String defectType;

        /** Mô tả lỗi */
        private String description;

        /** Số lượng */
        private BigDecimal quantity;

        /** Phân loại: repairable, destroyable */
        private String classification;

        /** Mức độ nghiêm trọng: low, medium, high, critical */
        private String severity;
    }
}
