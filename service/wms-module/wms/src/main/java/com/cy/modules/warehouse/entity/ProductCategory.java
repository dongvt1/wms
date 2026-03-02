package com.cy.modules.warehouse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @Description: Danh mục sản phẩm – Đã chuyển sang module common
 * @Author: BMad
 * @deprecated Vui lòng dùng {@link com.cy.modules.common.entity.ProductCategory}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Danh mục sản phẩm")
@Deprecated
public class ProductCategory extends com.cy.modules.common.entity.ProductCategory {
    private static final long serialVersionUID = 1L;
}