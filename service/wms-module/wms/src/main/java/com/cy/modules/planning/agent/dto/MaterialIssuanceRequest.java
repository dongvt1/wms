package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO yêu cầu xuất kho nguyên vật liệu gửi đến WMS
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialIssuanceRequest {

    /** Mã lệnh sản xuất */
    private String productionOrderId;

    /** Mã dây chuyền sản xuất đích */
    private String targetProductionLineId;

    /** Danh sách vật tư cần xuất */
    private List<MaterialItem> materials;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialItem {
        /** Mã vật tư */
        private String materialId;

        /** Tên vật tư */
        private String materialName;

        /** Số lượng cần xuất */
        private BigDecimal quantity;

        /** Đơn vị tính */
        private String unit;
    }
}
