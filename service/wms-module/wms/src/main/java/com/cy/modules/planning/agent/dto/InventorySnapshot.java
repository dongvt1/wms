package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO snapshot tồn kho từ ERP-MRP-WMS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySnapshot {

    /** Thời điểm lấy dữ liệu */
    private Instant snapshotTime;

    /** Danh sách mức tồn kho theo vật tư */
    private List<MaterialStock> materials;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialStock {
        /** Mã vật tư */
        private String materialId;

        /** Tên vật tư */
        private String materialName;

        /** Số lượng tồn kho hiện tại */
        private BigDecimal availableQuantity;

        /** Số lượng đã đặt trước */
        private BigDecimal reservedQuantity;

        /** Đơn vị tính */
        private String unit;

        /** Vị trí kho */
        private String warehouseLocation;
    }
}
