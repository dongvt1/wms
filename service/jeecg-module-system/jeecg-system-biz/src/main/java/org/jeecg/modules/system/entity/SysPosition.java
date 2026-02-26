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
 * @Description: Job level
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
@Data
@TableName("sys_position")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Job level表")
public class SysPosition {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private java.lang.String id;
    /**
     * Job code
     */
    @Excel(name = "Job code", width = 15)
    @Schema(description = "Job code")
    private java.lang.String code;
    /**
     * Job level名称
     */
    @Excel(name = "Job level名称", width = 15)
    @Schema(description = "Job level名称")
    private java.lang.String name;
    /**
     * Rank
     */
    //@Excel(name = "Rank", width = 15,dicCode ="position_rank")
    @Schema(description = "Job level")
    private java.lang.Integer postLevel;
    /**
     * companyid
     */
    @Schema(description = "companyid")
    private java.lang.String companyId;
    /**
     * Creator
     */
    @Schema(description = "Creator")
    private java.lang.String createBy;
    /**
     * creation time
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "creation time")
    private java.util.Date createTime;
    /**
     * Modifier
     */
    @Schema(description = "Modifier")
    private java.lang.String updateBy;
    /**
     * modification time
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "modification time")
    private java.util.Date updateTime;
    /**
     * Organization code
     */
    @Schema(description = "Organization code")
    private java.lang.String sysOrgCode;

    /**tenantID*/
    @Schema(description = "tenantID")
    private java.lang.Integer tenantId;
}
