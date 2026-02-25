package org.jeecg.modules.warehouse.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Product
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Product")
@TableName("product")
public class Product extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Product Code */
    @Excel(name="Product Code",width=25)
    @Schema(description = "Product Code")
    private java.lang.String code;

    /** Product Name */
    @Excel(name="Product Name",width=25)
    @Schema(description = "Product Name")
    private java.lang.String name;

    /** Description */
    @Excel(name="Description",width=50)
    @Schema(description = "Description")
    private java.lang.String description;

    /** Price */
    @Excel(name="Price",width=15)
    @Schema(description = "Price")
    private java.math.BigDecimal price;

    /** Category ID */
    @Excel(name="Category",width=25, dictTable="product_category", dicText="name", dicCode="id")
    @Schema(description = "Category ID")
    private java.lang.String categoryId;

    /** Minimum Stock Level */
    @Excel(name="Min Stock Level",width=15)
    @Schema(description = "Minimum Stock Level")
    private java.lang.Integer minStockLevel;

    /** Product Image URL */
    @Excel(name="Image",width=30, type=2)
    @Schema(description = "Product Image URL")
    private java.lang.String image;

    /** Status (0: Inactive, 1: Active) */
    @Excel(name="Status",width=15, dicCode="product_status")
    @Schema(description = "Status")
    private java.lang.Integer status;

    /** Current Stock Quantity */
    @Excel(name="Current Stock",width=15)
    @Schema(description = "Current Stock Quantity")
    private java.lang.Integer currentStock;
}