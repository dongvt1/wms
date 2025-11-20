package org.jeecg.modules.warehouse.entity;

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
 * @Description: Customer Balance Entity
 * @Author: BMad
 * @Date: 2025-11-20
 * @Version: V1.0
 */
@Data
@TableName("customer_balances")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "Customer Balance Entity")
public class CustomerBalance implements Serializable {
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

    @Schema(description = "Customer ID", required = true)
    private String customerId;

    @Schema(description = "Balance (positive: credit, negative: debt)")
    private BigDecimal balance;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last updated timestamp")
    private Date lastUpdated;

    @Schema(description = "Last updated by")
    private String updatedBy;
}