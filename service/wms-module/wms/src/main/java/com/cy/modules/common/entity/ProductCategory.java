package com.cy.modules.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Danh mục sản phẩm – Common Entity
 * @Author: BMad
 * @Date: 2026-03-02
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Danh mục sản phẩm")
@TableName("product_category")
public class ProductCategory extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /** Tên danh mục */
    @Excel(name = "Tên danh mục", width = 25)
    @Schema(description = "Tên danh mục")
    private String name;

    /** Mô tả */
    @Excel(name = "Mô tả", width = 50)
    @Schema(description = "Mô tả")
    private String description;

    /** ID danh mục cha (null = root) */
    @Schema(description = "ID danh mục cha")
    private String parentId;

    /** Trạng thái (0: Không hoạt động, 1: Hoạt động) */
    @Excel(name = "Trạng thái", width = 15)
    @Schema(description = "Trạng thái (0: Không hoạt động, 1: Hoạt động)")
    private Integer status;
}
