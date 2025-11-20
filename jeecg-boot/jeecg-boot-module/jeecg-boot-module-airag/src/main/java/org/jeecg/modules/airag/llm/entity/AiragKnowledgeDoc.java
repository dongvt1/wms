package org.jeecg.modules.airag.llm.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.*;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.util.Date;


import io.swagger.v3.oas.annotations.media.Schema;

import java.io.UnsupportedEncodingException;

/**
 * @Description: airagKnowledge base documentation
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
@Schema(description="airagKnowledge base documentation")
@Data
@TableName("airag_knowledge_doc")
public class AiragKnowledgeDoc implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * primary key
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary key")
    private String id;

    /**
     * Creator
     */
    @Schema(description = "Creator")
    @Dict(dictTable = "sys_user",dicCode = "username",dicText = "realname")
    private String createBy;

    /**
     * Creation date
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation date")
    private Date createTime;

    /**
     * Updater
     */
    @Schema(description = "Updater")
    private String updateBy;

    /**
     * Update date
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update date")
    private Date updateTime;

    /**
     * Department
     */
    @Schema(description = "Department")
    private String sysOrgCode;

    /**
     * tenantid
     */
    @Excel(name = "tenantid", width = 15)
    @Schema(description = "tenantid")
    private String tenantId;

    /**
     * knowledge baseid
     */
    @Schema(description = "knowledge baseid")
    private String knowledgeId;

    /**
     * title
     */
    @Excel(name = "title", width = 15)
    @Schema(description = "title")
    private String title;

    /**
     * type
     */
    @Excel(name = "type", width = 15, dicCode = "know_doc_type")
    @Schema(description = "type")
    private String type;

    /**
     * content
     */
    @Excel(name = "content", width = 15)
    @Schema(description = "content")
    private String content;

    /**
     * Metadata,Storage directory and website where uploaded files are stored <br/>
     * eg. {"filePath":"https://xxxxxx","website":"http://hellp.jeecg.com"}
     */
    @Excel(name = "Metadata", width = 15)
    @Schema(description = "Metadata")
    private String metadata;

    /**
     * state
     */
    @Excel(name = "state", width = 15)
    @Schema(description = "state")
    private String status;

}
