package org.jeecg.modules.airag.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: AIRagknowledge base
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
@Schema(description="AIRagknowledge base")
@Data
@TableName("airag_knowledge")
public class AiragKnowledge implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * primary key
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary key")
    private java.lang.String id;

    /**
     * Creator
     */
    @Schema(description = "Creator")
    @Dict(dictTable = "sys_user",dicCode = "username",dicText = "realname")
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
    @Schema(description = "Department")
    private java.lang.String sysOrgCode;

    /**
     * tenantid
     */
    @Excel(name = "tenantid", width = 15)
    @Schema(description = "tenantid")
    private java.lang.String tenantId;

    /**
     * knowledge base名称
     */
    @Excel(name = "knowledge base名称", width = 15)
    @Schema(description = "knowledge base名称")
    private java.lang.String name;

    /**
     * vector modelid
     */
    @Excel(name = "vector modelid", width = 15, dictTable = "airag_model where model_type = 'EMBED'", dicText = "name", dicCode = "id")
    @Dict(dictTable = "airag_model where model_type = 'EMBED'", dicText = "name", dicCode = "id")
    @Schema(description = "vector modelid")
    private java.lang.String embedId;

    /**
     * describe
     */
    @Excel(name = "describe", width = 15)
    @Schema(description = "describe")
    private java.lang.String descr;

    /**
     * state
     */
    @Excel(name = "state", width = 15)
    @Schema(description = "state")
    private java.lang.String status;
}
