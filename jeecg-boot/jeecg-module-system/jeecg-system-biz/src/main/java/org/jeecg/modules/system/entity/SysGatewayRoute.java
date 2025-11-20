package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * @Description: gatewayRoute management
 * @Author: jeecg-boot
 * @Date:   2020-05-26
 * @Version: V1.0
 */
@Data
@TableName("sys_gateway_route")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="gatewayRoute management")
public class SysGatewayRoute implements Serializable {
    private static final long serialVersionUID = 1L;

    /**primary key*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary key")
    private String id;

    /**routerKEy*/
    @Schema(description = "routingID")
    private String routerId;

    /**Service name*/
    @Excel(name = "Service name", width = 15)
    @Schema(description = "Service name")
    private String name;

    /**Service address*/
    @Excel(name = "Service address", width = 15)
    @Schema(description = "Service address")
    private String uri;

    /**
     * Assertion configuration
     */
    private String predicates;

    /**
     * Filter configuration
     */
    private String filters;

    /**Whether to ignore prefix0-no 1-yes*/
    @Excel(name = "ignore prefix", width = 15)
    @Schema(description = "ignore prefix")
    @Dict(dicCode = "yn")
    private Integer stripPrefix;

    /**yesno重试0-no 1-yes*/
    @Excel(name = "yesno重试", width = 15)
    @Schema(description = "yesno重试")
    @Dict(dicCode = "yn")
    private Integer retryable;

    /**yesno为retain data:0-no 1-yes*/
    @Excel(name = "retain data", width = 15)
    @Schema(description = "retain data")
    @Dict(dicCode = "yn")
    private Integer persistable;

    /**yesnoShown in interface documentation:0-no 1-yes*/
    @Excel(name = "Shown in interface documentation", width = 15)
    @Schema(description = "Shown in interface documentation")
    @Dict(dicCode = "yn")
    private Integer showApi;

    /**state 1efficient 0invalid*/
    @Excel(name = "state", width = 15)
    @Schema(description = "state")
    @Dict(dicCode = "yn")
    private Integer status;

    /**Creator*/
    @Schema(description = "Creator")
    private String createBy;
    /**Creation date*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation date")
    private Date createTime;

    /**
     * 删除state（0not deleted，1Deleted）
     */
    @TableLogic
    private Integer delFlag;
    /*    *//**Updater*//*
    @Schema(description = "Updater")
    private String updateBy;
    *//**Update date*//*
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update date")
    private Date updateTime;
    *//**Department*//*
    @Schema(description = "Department")
    private String sysOrgCode;*/
}
