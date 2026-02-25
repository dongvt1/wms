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
 * @Description: Supplier
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Supplier")
@TableName("suppliers")
public class Supplier extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Supplier Code */
    @Excel(name="Supplier Code",width=25)
    @Schema(description = "Supplier Code")
    private java.lang.String supplierCode;

    /** Supplier Name */
    @Excel(name="Supplier Name",width=50)
    @Schema(description = "Supplier Name")
    private java.lang.String supplierName;

    /** Contact Person */
    @Excel(name="Contact Person",width=50)
    @Schema(description = "Contact Person")
    private java.lang.String contactPerson;

    /** Phone */
    @Excel(name="Phone",width=20)
    @Schema(description = "Phone")
    private java.lang.String phone;

    /** Email */
    @Excel(name="Email",width=50)
    @Schema(description = "Email")
    private java.lang.String email;

    /** Address */
    @Excel(name="Address",width=100)
    @Schema(description = "Address")
    private java.lang.String address;

    /** Status */
    @Excel(name="Status",width=15, dicCode="supplier_status")
    @Schema(description = "Status")
    private java.lang.Integer status;
}