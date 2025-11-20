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
 * @Description: Inventory
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Inventory")
@TableName("inventory")
public class Inventory extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Product ID */
    @Excel(name="Product ID",width=25)
    @Schema(description = "Product ID")
    private java.lang.String productId;

    /** Total quantity */
    @Excel(name="Total Quantity",width=15)
    @Schema(description = "Total quantity")
    private java.lang.Integer quantity;

    /** Reserved quantity */
    @Excel(name="Reserved Quantity",width=15)
    @Schema(description = "Reserved quantity")
    private java.lang.Integer reservedQuantity;

    /** Available quantity */
    @Excel(name="Available Quantity",width=15)
    @Schema(description = "Available quantity")
    private java.lang.Integer availableQuantity;

    /** Last updated timestamp */
    @Excel(name="Last Updated",width=25, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last updated timestamp")
    private java.util.Date lastUpdated;

    /** Last updated by */
    @Excel(name="Updated By",width=15)
    @Schema(description = "Last updated by")
    private java.lang.String updatedBy;
}