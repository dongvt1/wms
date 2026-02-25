package org.jeecg.modules.warehouse.entity;

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
 * @Description: Product Category
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Product Category")
@TableName("product_category")
public class ProductCategory extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Category Name */
    @Excel(name="Category Name",width=25)
    @Schema(description = "Category Name")
    private java.lang.String name;

    /** Description */
    @Excel(name="Description",width=50)
    @Schema(description = "Description")
    private java.lang.String description;

    /** Parent Category ID */
    @Excel(name="Parent Category",width=25)
    @Schema(description = "Parent Category ID")
    private java.lang.String parentId;

    /** Status (0: Inactive, 1: Active) */
    @Excel(name="Status",width=15, dicCode="product_status")
    @Schema(description = "Status")
    private java.lang.Integer status;
}