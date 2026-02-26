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
 * @Description: Nhà cung cấp
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Nhà cung cấp")
@TableName("suppliers")
public class Supplier extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã nhà cung cấp */
    @Excel(name="Mã nhà cung cấp",width=25)
    @Schema(description = "Mã nhà cung cấp")
    private java.lang.String supplierCode;

    /** Tên nhà cung cấp */
    @Excel(name="Tên nhà cung cấp",width=50)
    @Schema(description = "Tên nhà cung cấp")
    private java.lang.String supplierName;

    /** Người liên hệ */
    @Excel(name="Người liên hệ",width=50)
    @Schema(description = "Người liên hệ")
    private java.lang.String contactPerson;

    /** Điện thoại */
    @Excel(name="Điện thoại",width=20)
    @Schema(description = "Điện thoại")
    private java.lang.String phone;

    /** Email */
    @Excel(name="Email",width=50)
    @Schema(description = "Email")
    private java.lang.String email;

    /** Địa chỉ */
    @Excel(name="Địa chỉ",width=100)
    @Schema(description = "Địa chỉ")
    private java.lang.String address;

    /** Trạng thái */
    @Excel(name="Trạng thái",width=15, dicCode="supplier_status")
    @Schema(description = "Trạng thái")
    private java.lang.Integer status;
}