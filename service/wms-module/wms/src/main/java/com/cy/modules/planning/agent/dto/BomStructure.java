package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cấu trúc BOM (Bill of Materials) từ ERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomStructure {

    /** Mã sản phẩm */
    private String productId;

    /** Tên sản phẩm */
    private String productName;

    /** Phiên bản BOM */
    private String bomVersion;

    /** Danh sách thành phần nguyên vật liệu */
    private List<BomItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BomItem {
        /** Mã vật tư */
        private String materialId;

        /** Tên vật tư */
        private String materialName;

        /** Số lượng cần cho 1 đơn vị sản phẩm */
        private BigDecimal quantityPerUnit;

        /** Đơn vị tính */
        private String unit;

        /** Tỷ lệ hao hụt (%) */
        private BigDecimal scrapRate;
    }
}
