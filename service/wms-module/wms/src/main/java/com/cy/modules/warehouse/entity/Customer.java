package com.cy.modules.warehouse.entity;

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
 * @Description: Khách hàng
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@TableName("customers")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Khách hàng")
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "Khóa chính")
    private String id;

    @Schema(description = "Người tạo")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày tạo")
    private Date createTime;

    @Schema(description = "Người cập nhật")
    private String updateBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày cập nhật")
    private Date updateTime;

    @Schema(description = "Phòng ban")
    private String sysOrgCode;

    @Excel(name = "Mã khách hàng", width = 15)
    @Schema(description = "Mã khách hàng", required = true)
    private String customerCode;

    @Excel(name = "Tên khách hàng", width = 15)
    @Schema(description = "Tên khách hàng", required = true)
    private String customerName;

    @Excel(name = "Người liên hệ", width = 15)
    @Schema(description = "Người liên hệ")
    private String contactPerson;

    @Excel(name = "Điện thoại", width = 15)
    @Schema(description = "Điện thoại")
    private String phone;

    @Excel(name = "Email", width = 15)
    @Schema(description = "Email")
    private String email;

    @Excel(name = "Địa chỉ", width = 30)
    @Schema(description = "Địa chỉ")
    private String address;

    @Excel(name = "Mã số thuế", width = 15)
    @Schema(description = "Mã số thuế")
    private String taxCode;

    @Dict(dicCode = "customer_status")
    @Excel(name = "Trạng thái", width = 15, dicCode = "customer_status")
    @Schema(description = "Trạng thái (0: Không hoạt động, 1: Hoạt động)")
    private Integer status;
}