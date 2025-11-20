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

import java.util.Date;

/**
 * @Description: Coding verification rules
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
@Data
@TableName("sys_check_rule")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Coding verification rules")
public class SysCheckRule {

    /**
     * primary keyid
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary keyid")
    private String id;
    /**
     * Rule name
     */
    @Excel(name = "Rule name", width = 15)
    @Schema(description = "Rule name")
    private String ruleName;
    /**
     * ruleCode
     */
    @Excel(name = "ruleCode", width = 15)
    @Schema(description = "ruleCode")
    private String ruleCode;
    /**
     * ruleJSON
     */
    @Excel(name = "ruleJSON", width = 15)
    @Schema(description = "ruleJSON")
    private String ruleJson;
    /**
     * rule描述
     */
    @Excel(name = "rule描述", width = 15)
    @Schema(description = "rule描述")
    private String ruleDescription;
    /**
     * Updater
     */
    @Excel(name = "Updater", width = 15)
    @Schema(description = "Updater")
    private String updateBy;
    /**
     * Update time
     */
    @Excel(name = "Update time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update time")
    private Date updateTime;
    /**
     * Creator
     */
    @Excel(name = "Creator", width = 15)
    @Schema(description = "Creator")
    private String createBy;
    /**
     * creation time
     */
    @Excel(name = "creation time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "creation time")
    private Date createTime;
}
