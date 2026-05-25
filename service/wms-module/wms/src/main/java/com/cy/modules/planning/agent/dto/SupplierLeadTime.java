package com.cy.modules.planning.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO thời gian giao hàng của nhà cung cấp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierLeadTime {

    /** Mã vật tư */
    private String materialId;

    /** Tên vật tư */
    private String materialName;

    /** Mã nhà cung cấp */
    private String supplierId;

    /** Tên nhà cung cấp */
    private String supplierName;

    /** Thời gian giao hàng (ngày) */
    private Integer leadTimeDays;

    /** Số lượng tối thiểu đặt hàng */
    private java.math.BigDecimal minimumOrderQuantity;

    /** Đơn vị tính */
    private String unit;
}
