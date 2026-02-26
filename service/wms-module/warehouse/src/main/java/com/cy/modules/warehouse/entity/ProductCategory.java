package com.cy.modules.warehouse.entity;

import java.io.Serializable;
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
 * @Description: Danh mục sản phẩm
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Danh mục sản phẩm")
@TableName("product_category")
public class ProductCategory extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Tên danh mục */
    @Excel(name="Tên danh mục",width=25)
    @Schema(description = "Tên danh mục")
    private java.lang.String name;

    /** Mô tả */
    @Excel(name="Mô tả",width=50)
    @Schema(description = "Mô tả")
    private java.lang.String description;

    /** ID danh mục cha */
    @Excel(name="Danh mục cha",width=25)
    @Schema(description = "ID danh mục cha")
    private java.lang.String parentId;

    /** Trạng thái (0: Không hoạt động, 1: Hoạt động) */
    @Excel(name="Trạng thái",width=15, dicCode="product_status")
    @Schema(description = "Trạng thái")
    private java.lang.Integer status;
}