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
 * @Description: Third-party configuration table
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
@Data
@TableName("sys_third_app_config")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Third-party configuration table")
public class SysThirdAppConfig {

    /**serial number*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "serial number")
    private String id;

    /**tenantid*/
    @Excel(name = "tenantid", width = 15)
    @Schema(description = "tenantid")
    private Integer tenantId;

    /**DingTalk/Enterprise WeChat third-party enterprise application logo*/
    @Excel(name = "DingTalk/Enterprise WeChat third-party enterprise application logo", width = 15)
    @Schema(description = "DingTalk/Enterprise WeChat third-party enterprise application logo")
    private String agentId;

    /**DingTalk/Enterprise WeChat applicationid*/
    @Excel(name = "DingTalk/Enterprise WeChat applicationid", width = 15)
    @Schema(description = "DingTalk/Enterprise WeChat applicationid")
    private String clientId;

    /**DingTalk/Enterprise WeChatapplicationidThe corresponding secret key*/
    @Excel(name = "DingTalk/Enterprise WeChatapplicationidThe corresponding secret key", width = 15)
    @Schema(description = "DingTalk/Enterprise WeChatapplicationidThe corresponding secret key")
    private String clientSecret;

    /**DingTalk企业id*/
    @Excel(name = "DingTalk企业id", width = 15)
    @Schema(description = "DingTalk企业id")
    private String corpId;

    /**third party category(dingtalk DingTalk wechat_enterprise Enterprise WeChat)*/
    @Excel(name = "third party category(dingtalk DingTalk wechat_enterprise Enterprise WeChat)", width = 15)
    @Schema(description = "third party category(dingtalk DingTalk wechat_enterprise Enterprise WeChat)")
    private String thirdType;

    /**Whether to enable(0-no,1-yes)*/
    @Excel(name = "Whether to enable(0-no,1-yes)", width = 15)
    @Schema(description = "Whether to enable(0-no,1-yes)")
    private Integer status;

    /**Creation date*/
    @Excel(name = "Creation date", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**Modification date*/
    @Excel(name = "Modification date", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
