package com.cy.modules.warehouse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: Sản phẩm – Đã chuyển sang module common
 * @Author: BMad
 * @deprecated Vui lòng dùng {@link com.cy.modules.common.entity.Product}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Sản phẩm")
@Deprecated
public class Product extends com.cy.modules.common.entity.Product {
    private static final long serialVersionUID = 1L;
}