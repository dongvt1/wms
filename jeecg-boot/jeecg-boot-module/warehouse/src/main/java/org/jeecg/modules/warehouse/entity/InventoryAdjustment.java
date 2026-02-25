package org.jeecg.modules.warehouse.entity;

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
 * @Description: InventoryAdjustment
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="InventoryAdjustment")
@TableName("inventory_adjustments")
public class InventoryAdjustment extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Product ID */
    @Excel(name="Product ID",width=25)
    @Schema(description = "Product ID")
    private java.lang.String productId;

    /** Old quantity before adjustment */
    @Excel(name="Old Quantity",width=15)
    @Schema(description = "Old quantity before adjustment")
    private java.lang.Integer oldQuantity;

    /** New quantity after adjustment */
    @Excel(name="New Quantity",width=15)
    @Schema(description = "New quantity after adjustment")
    private java.lang.Integer newQuantity;

    /** Reason for adjustment */
    @Excel(name="Adjustment Reason",width=50)
    @Schema(description = "Reason for adjustment")
    private java.lang.String adjustmentReason;

    /** User who performed the adjustment */
    @Excel(name="User ID",width=15)
    @Schema(description = "User who performed the adjustment")
    private java.lang.String userId;

    /** Adjustment timestamp */
    @Excel(name="Adjustment Time",width=25, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Adjustment timestamp")
    private java.util.Date createdAt;
}