package com.cy.modules.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: Số dư khách hàng
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@TableName("customer_balances")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Số dư khách hàng")
public class CustomerBalance implements Serializable {
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

    @Schema(description = "ID khách hàng", required = true)
    private String customerId;

    @Schema(description = "Số dư (dương: có công nợ, âm: còn nợ)")
    private BigDecimal balance;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian cập nhật lần cuối")
    private Date lastUpdated;

    @Schema(description = "Người cập nhật lần cuối")
    private String updatedBy;
}