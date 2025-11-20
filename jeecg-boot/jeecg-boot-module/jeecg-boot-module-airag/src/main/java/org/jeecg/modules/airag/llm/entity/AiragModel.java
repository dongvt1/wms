package org.jeecg.modules.airag.llm.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: AiRagModel configuration
 * @Author: jeecg-boot
 * @Date: 2025-02-17
 * @Version: V1.0
 */
@Data
@TableName("airag_model")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="AiRagModel configuration")
public class AiragModel implements Serializable {
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
     * name
     */
    @Excel(name = "name", width = 15)
    @Schema(description = "name")
    private String name;
    /**
     * supplier
     */
    @Excel(name = "supplier", width = 15, dicCode = "model_provider")
    @Dict(dicCode = "model_provider")
    @Schema(description = "supplier")
    private String provider;
    /**
     * Model type
     */
    @Excel(name = "Model type", width = 15, dicCode = "model_type")
    @Dict(dicCode = "model_type")
    @Schema(description = "Model type")
    private String modelType;
    /**
     * 模型name
     */
    @Excel(name = "模型name", width = 15)
    @Schema(description = "模型name")
    private String modelName;
    /**
     * APIdomain name
     */
    @Excel(name = "APIdomain name", width = 15)
    @Schema(description = "APIdomain name")
    private String baseUrl;
    /**
     * Credential information
     */
    @Excel(name = "Credential information", width = 15)
    @Schema(description = "Credential information")
    private String credential;
    /**
     * Model parameters
     */
    @Excel(name = "Model parameters", width = 15)
    @Schema(description = "Model parameters")
    private String modelParams;

    /**
     * Whether to activate(0=Not activated,1=Activated)
     */
    @Excel(name = "Whether to activate", width = 15)
    @Schema(description = "Whether to activate")
    private Integer activateFlag;
}
