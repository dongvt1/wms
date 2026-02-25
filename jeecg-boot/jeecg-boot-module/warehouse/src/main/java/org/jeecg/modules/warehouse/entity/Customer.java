package org.jeecg.modules.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: Customer Entity
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@TableName("customers")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Customer Entity")
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Primary key")
    private String id;

    @Schema(description = "Created by")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Create date")
    private Date createTime;

    @Schema(description = "Updated by")
    private String updateBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update date")
    private Date updateTime;

    @Schema(description = "Department")
    private String sysOrgCode;

    @Excel(name = "Customer Code", width = 15)
    @Schema(description = "Customer code", required = true)
    private String customerCode;

    @Excel(name = "Customer Name", width = 15)
    @Schema(description = "Customer name", required = true)
    private String customerName;

    @Excel(name = "Contact Person", width = 15)
    @Schema(description = "Contact person")
    private String contactPerson;

    @Excel(name = "Phone", width = 15)
    @Schema(description = "Phone")
    private String phone;

    @Excel(name = "Email", width = 15)
    @Schema(description = "Email")
    private String email;

    @Excel(name = "Address", width = 30)
    @Schema(description = "Address")
    private String address;

    @Excel(name = "Tax Code", width = 15)
    @Schema(description = "Tax code")
    private String taxCode;

    @Dict(dicCode = "customer_status")
    @Excel(name = "Status", width = 15, dicCode = "customer_status")
    @Schema(description = "Status (0: Inactive, 1: Active)")
    private Integer status;
}