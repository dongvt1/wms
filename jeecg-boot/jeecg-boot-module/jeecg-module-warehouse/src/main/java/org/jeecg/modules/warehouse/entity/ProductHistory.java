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
 * @Description: Product History
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Product History")
@TableName("product_history")
public class ProductHistory extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Product ID */
    @Excel(name="Product ID",width=25)
    @Schema(description = "Product ID")
    private java.lang.String productId;

    /** Action (CREATE, UPDATE, DELETE) */
    @Excel(name="Action",width=15, dicCode="product_history_action")
    @Schema(description = "Action")
    private java.lang.String action;

    /** Old Data (JSON format) */
    @Excel(name="Old Data",width=50)
    @Schema(description = "Old Data")
    private java.lang.String oldData;

    /** New Data (JSON format) */
    @Excel(name="New Data",width=50)
    @Schema(description = "New Data")
    private java.lang.String newData;

    /** User ID who performed the action */
    @Excel(name="User ID",width=25)
    @Schema(description = "User ID")
    private java.lang.String userId;
}