package com.cy.modules.warehouse.entity;

import java.io.Serializable;
import java.util.Date;
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
 * @Description: Tồn kho
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Tồn kho")
@TableName("inventory")
public class Inventory extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID sản phẩm */
    @Excel(name="ID sản phẩm",width=25)
    @Schema(description = "ID sản phẩm")
    private java.lang.String productId;

    /** Tổng số lượng */
    @Excel(name="Tổng số lượng",width=15)
    @Schema(description = "Tổng số lượng")
    private java.lang.Integer quantity;

    /** Số lượng đã đặt trước */
    @Excel(name="Số lượng đặt trước",width=15)
    @Schema(description = "Số lượng đã đặt trước")
    private java.lang.Integer reservedQuantity;

    /** Số lượng có thể bán */
    @Excel(name="Số lượng có thể bán",width=15)
    @Schema(description = "Số lượng có thể bán")
    private java.lang.Integer availableQuantity;

    /** Thời gian cập nhật lần cuối */
    @Excel(name="Cập nhật lần cuối",width=25, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian cập nhật lần cuối")
    private java.util.Date lastUpdated;

    /** Người cập nhật lần cuối */
    @Excel(name="Người cập nhật",width=15)
    @Schema(description = "Người cập nhật lần cuối")
    private java.lang.String updatedBy;

    String productName;
    String productCode;
    String minStockLevel;
    String price;
    String totalValue;
}