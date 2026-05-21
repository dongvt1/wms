package com.cy.modules.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: Nguyên vật liệu – Entity riêng biệt (tách khỏi product)
 * @Author: BMad
 * @Date: 2026-03-05
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Nguyên vật liệu")
@TableName("material")
public class Material extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /** Mã vật tư */
    @Excel(name = "Mã vật tư", width = 20)
    @Schema(description = "Mã vật tư")
    private String code;

    /** Tên vật tư */
    @Excel(name = "Tên vật tư", width = 30)
    @Schema(description = "Tên vật tư")
    private String name;

    /** Mô tả */
    @Excel(name = "Mô tả", width = 50)
    @Schema(description = "Mô tả")
    private String description;

    /** Đơn vị tính */
    @Excel(name = "Đơn vị", width = 10)
    @Schema(description = "Đơn vị tính")
    private String unit;

    /** Giá tham khảo */
    @Excel(name = "Giá", width = 15)
    @Schema(description = "Giá tham khảo")
    private BigDecimal price;

    /** ID danh mục */
    @Schema(description = "ID danh mục")
    private String categoryId;

    /** Tên danh mục (join query, không map vào DB) */
    @TableField(exist = false)
    @Schema(description = "Tên danh mục")
    private String categoryName;

    /** Tồn kho tối thiểu */
    @Excel(name = "Tồn kho tối thiểu", width = 15)
    @Schema(description = "Tồn kho tối thiểu")
    private Integer minStockLevel;

    /** Tồn kho hiện tại */
    @Excel(name = "Tồn kho hiện tại", width = 15)
    @Schema(description = "Tồn kho hiện tại")
    private Integer currentStock;

    /** Ảnh */
    @Excel(name = "Ảnh", width = 30, type = 2)
    @Schema(description = "Đường dẫn ảnh")
    private String image;

    /** Cân nặng (kg) */
    @Excel(name = "Cân nặng (kg)", width = 15)
    @Schema(description = "Cân nặng tính bằng kg")
    private BigDecimal weight;

    /** Chiều dài (mm) */
    @Excel(name = "Dài (mm)", width = 12)
    @Schema(description = "Chiều dài (mm)")
    private BigDecimal length;

    /** Chiều rộng (mm) */
    @Excel(name = "Rộng (mm)", width = 12)
    @Schema(description = "Chiều rộng (mm)")
    private BigDecimal width;

    /** Chiều cao (mm) */
    @Excel(name = "Cao (mm)", width = 12)
    @Schema(description = "Chiều cao (mm)")
    private BigDecimal height;

    /** Trạng thái (1: active, 0: inactive) */
    @Excel(name = "Trạng thái", width = 12)
    @Schema(description = "Trạng thái (1=active, 0=inactive)")
    private Integer status;

    /** Danh sách linh kiện thay thế (không map vào DB) */
    @TableField(exist = false)
    @Schema(description = "Danh sách linh kiện thay thế")
    private List<MaterialSubstitute> substitutes;
}
