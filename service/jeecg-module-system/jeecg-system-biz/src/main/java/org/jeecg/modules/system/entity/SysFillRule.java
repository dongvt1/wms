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
 * @Description: Filling rules
 * @Author: jeecg-boot
 * @Date: 2019-11-07
 * @Version: V1.0
 */
@Data
@TableName("sys_fill_rule")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Filling rules")
public class SysFillRule {

    /**
     * primary keyID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary keyID")
    private java.lang.String id;
    /**
     * Rule name
     */
    @Excel(name = "Rule name", width = 15)
    @Schema(description = "Rule name")
    private java.lang.String ruleName;
    /**
     * ruleCode
     */
    @Excel(name = "ruleCode", width = 15)
    @Schema(description = "ruleCode")
    private java.lang.String ruleCode;
    /**
     * rule实现类
     */
    @Excel(name = "rule实现类", width = 15)
    @Schema(description = "rule实现类")
    private java.lang.String ruleClass;
    /**
     * rule参数
     */
    @Excel(name = "rule参数", width = 15)
    @Schema(description = "rule参数")
    private java.lang.String ruleParams;
    /**
     * Modifier
     */
    @Excel(name = "Modifier", width = 15)
    @Schema(description = "Modifier")
    private java.lang.String updateBy;
    /**
     * modification time
     */
    @Excel(name = "modification time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "modification time")
    private java.util.Date updateTime;
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
}
