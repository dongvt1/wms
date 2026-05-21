package org.jeecg.modules.system.entity;

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

/**
 * @Description: Multiple data source management
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Data
@TableName("sys_data_source")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Multiple data source management")
public class SysDataSource {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private java.lang.String id;
    /**
     * Data source encoding
     */
    @Excel(name = "Data source encoding", width = 15)
    @Schema(description = "Data source encoding")
    private java.lang.String code;
    /**
     * Data source name
     */
    @Excel(name = "Data source name", width = 15)
    @Schema(description = "Data source name")
    private java.lang.String name;
    /**
     * describe
     */
    @Excel(name = "Remark", width = 15)
    @Schema(description = "Remark")
    private java.lang.String remark;
    /**
     * Database type
     */
    @Dict(dicCode = "database_type")
    @Excel(name = "Database type", width = 15, dicCode = "database_type")
    @Schema(description = "Database type")
    private java.lang.String dbType;
    /**
     * Driver class
     */
    @Excel(name = "Driver class", width = 15)
    @Schema(description = "Driver class")
    private java.lang.String dbDriver;
    /**
     * Data source address
     */
    @Excel(name = "Data source address", width = 15)
    @Schema(description = "Data source address")
    private java.lang.String dbUrl;
    /**
     * Database name
     */
    @Excel(name = "Database name", width = 15)
    @Schema(description = "Database name")
    private java.lang.String dbName;
    /**
     * username
     */
    @Excel(name = "username", width = 15)
    @Schema(description = "username")
    private java.lang.String dbUsername;
    /**
     * password
     */
    @Excel(name = "password", width = 15)
    @Schema(description = "password")
    private java.lang.String dbPassword;
    /**
     * Creator
     */
    @Schema(description = "Creator")
    private java.lang.String createBy;
    /**
     * Creation date
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation date")
    private java.util.Date createTime;
    /**
     * Updater
     */
    @Schema(description = "Updater")
    private java.lang.String updateBy;
    /**
     * Update date
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update date")
    private java.util.Date updateTime;
    /**
     * Department
     */
    @Excel(name = "Department", width = 15)
    @Schema(description = "Department")
    private java.lang.String sysOrgCode;

    /**tenantID*/
    @Schema(description = "tenantID")
    private java.lang.Integer tenantId;
}
