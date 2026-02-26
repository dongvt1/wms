package com.cy.modules.warehouse.entity;

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
 * @Description: Sản phẩm
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Sản phẩm")
@TableName("product")
public class Product extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã sản phẩm */
    @Excel(name="Mã sản phẩm",width=25)
    @Schema(description = "Mã sản phẩm")
    private java.lang.String code;

    /** Tên sản phẩm */
    @Excel(name="Tên sản phẩm",width=25)
    @Schema(description = "Tên sản phẩm")
    private java.lang.String name;

    /** Mô tả */
    @Excel(name="Mô tả",width=50)
    @Schema(description = "Mô tả")
    private java.lang.String description;

    /** Giá bán */
    @Excel(name="Giá bán",width=15)
    @Schema(description = "Giá bán")
    private java.math.BigDecimal price;

    /** ID danh mục */
    @Excel(name="Danh mục",width=25, dictTable="product_category", dicText="name", dicCode="id")
    @Schema(description = "ID danh mục")
    private java.lang.String categoryId;

    /** Mức tồn kho tối thiểu */
    @Excel(name="Tồn kho tối thiểu",width=15)
    @Schema(description = "Mức tồn kho tối thiểu")
    private java.lang.Integer minStockLevel;

    /** Đường dẫn ảnh sản phẩm */
    @Excel(name="Ảnh",width=30, type=2)
    @Schema(description = "Đường dẫn ảnh sản phẩm")
    private java.lang.String image;

    /** Trạng thái (0: Không hoạt động, 1: Hoạt động) */
    @Excel(name="Trạng thái",width=15, dicCode="product_status")
    @Schema(description = "Trạng thái")
    private java.lang.Integer status;

    /** Tồn kho hiện tại */
    @Excel(name="Tồn kho hiện tại",width=15)
    @Schema(description = "Tồn kho hiện tại")
    private java.lang.Integer currentStock;
}