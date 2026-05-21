package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: System whitelist
 * @Author: jeecg-boot
 * @Date: 2023-09-12
 * @Version: V1.0
 */
@Data
@TableName("sys_table_white_list")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="System whitelist")
public class SysTableWhiteList {

    /**
     * primary keyid
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary keyid")
    private java.lang.String id;
    /**
     * Allowed table names
     */
    @Excel(name = "Allowed table names", width = 15)
    @Schema(description = "Allowed table names")
    private java.lang.String tableName;
    /**
     * Allowed field names，Separate multiple with commas
     */
    @Excel(name = "Allowed field names", width = 15)
    @Schema(description = "Allowed field names")
    private java.lang.String fieldName;
    /**
     * state，1=enable，0=Disable
     */
    @Excel(name = "state", width = 15)
    @Schema(description = "state")
    private java.lang.String status;
    /**
     * Creator
     */
    @Excel(name = "Creator", width = 15)
    @Schema(description = "Creator")
    private java.lang.String createBy;
    /**
     * creation time
     */
    @Excel(name = "creation time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "creation time")
    private java.util.Date createTime;
    /**
     * Updater
     */
    @Excel(name = "Updater", width = 15)
    @Schema(description = "Updater")
    private java.lang.String updateBy;
    /**
     * Update time
     */
    @Excel(name = "Update time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update time")
    private java.util.Date updateTime;
}
