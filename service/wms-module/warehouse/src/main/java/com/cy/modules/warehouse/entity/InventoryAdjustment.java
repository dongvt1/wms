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
 * @Description: Điều chỉnh tồn kho
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Điều chỉnh tồn kho")
@TableName("inventory_adjustments")
public class InventoryAdjustment extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID sản phẩm */
    @Excel(name="ID sản phẩm",width=25)
    @Schema(description = "ID sản phẩm")
    private java.lang.String productId;

    /** Số lượng cũ trước khi điều chỉnh */
    @Excel(name="Số lượng cũ",width=15)
    @Schema(description = "Số lượng cũ trước khi điều chỉnh")
    private java.lang.Integer oldQuantity;

    /** Số lượng mới sau khi điều chỉnh */
    @Excel(name="Số lượng mới",width=15)
    @Schema(description = "Số lượng mới sau khi điều chỉnh")
    private java.lang.Integer newQuantity;

    /** Lý do điều chỉnh */
    @Excel(name="Lý do điều chỉnh",width=50)
    @Schema(description = "Lý do điều chỉnh")
    private java.lang.String adjustmentReason;

    /** Người thực hiện điều chỉnh */
    @Excel(name="ID người dùng",width=15)
    @Schema(description = "Người thực hiện điều chỉnh")
    private java.lang.String userId;

    /** Thời gian điều chỉnh */
    @Excel(name="Thời gian điều chỉnh",width=25, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian điều chỉnh")
    private java.util.Date createdAt;
}