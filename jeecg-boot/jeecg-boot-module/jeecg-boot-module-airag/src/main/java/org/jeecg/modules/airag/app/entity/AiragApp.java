package org.jeecg.modules.airag.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.util.oConvertUtils;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: AIapplication
 * @Author: jeecg-boot
 * @Date: 2025-02-26
 * @Version: V1.0
 */
@Data
@TableName("airag_app")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="AIapplication")
public class AiragApp implements Serializable {
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
     * application名称
     */
    @Excel(name = "application名称", width = 15)
    @Schema(description = "application名称")
    private java.lang.String name;
    /**
     * application描述
     */
    @Excel(name = "application描述", width = 15)
    @Schema(description = "application描述")
    private java.lang.String descr;
    /**
     * application图标
     */
    @Excel(name = "application图标", width = 15)
    @Schema(description = "application图标")
    private java.lang.String icon;
    /**
     * application类型
     */
    @Excel(name = "application类型", width = 15, dicCode = "ai_app_type")
    @Dict(dicCode = "ai_app_type")
    @Schema(description = "application类型")
    private java.lang.String type;
    /**
     * opening remarks
     */
    @Excel(name = "opening remarks", width = 15)
    @Schema(description = "opening remarks")
    private java.lang.String prologue;
    /**
     * Default questions
     */
    @Excel(name = "Default questions", width = 15)
    @Schema(description = "Default questions")
    private java.lang.String presetQuestion;
    /**
     * prompt word
     */
    @Excel(name = "prompt word", width = 15)
    @Schema(description = "prompt word")
    private java.lang.String prompt;
    /**
     * Model configuration
     */
    @Excel(name = "Model configuration", width = 15, dictTable = "airag_model where model_type = 'LLM' ", dicText = "name", dicCode = "id")
    @Dict(dictTable = "airag_model where model_type = 'LLM' ", dicText = "name", dicCode = "id")
    @Schema(description = "Model configuration")
    private java.lang.String modelId;
    /**
     * Number of historical messages
     */
    @Excel(name = "Number of historical messages", width = 15)
    @Schema(description = "Number of historical messages")
    private java.lang.Integer msgNum;
    /**
     * knowledge base
     */
    @Excel(name = "knowledge base", width = 15, dictTable = "airag_knowledge where status = 'enable'", dicText = "name", dicCode = "id")
    @Dict(dictTable = "airag_knowledge where status = 'enable'", dicText = "name", dicCode = "id")
    @Schema(description = "knowledge base")
    private java.lang.String knowledgeIds;
    /**
     * process
     */
    @Excel(name = "process", width = 15, dictTable = "airag_flow where status = 'enable' ", dicText = "name", dicCode = "id")
    @Dict(dictTable = "airag_flow where status = 'enable' ", dicText = "name", dicCode = "id")
    @Schema(description = "process")
    private java.lang.String flowId;
    /**
     * shortcut command
     */
    @Excel(name = "shortcut command", width = 15)
    @Schema(description = "shortcut command")
    private java.lang.String quickCommand;
    /**
     * state（enable=enable、disable=Disable、release=release）
     */
    @Excel(name = "state", width = 15)
    @Schema(description = "state")
    private java.lang.String status;


    /**
     * Metadata
     */
    @Excel(name = "Metadata", width = 15)
    @Schema(description = "Metadata")
    private java.lang.String metadata;

    /**
     * knowledge baseids
     */
    @TableField(exist = false)
    private List<String> knowIds;

    /**
     * 获取knowledge baseid
     *
     * @return
     * @author chenrui
     * @date 2025/2/28 11:45
     */
    public List<String> getKnowIds() {
        if (oConvertUtils.isNotEmpty(knowledgeIds)) {
            String[] knowIds = knowledgeIds.split(",");
            return Arrays.asList(knowIds);
        } else {
            return new ArrayList<>(0);
        }
    }
}
