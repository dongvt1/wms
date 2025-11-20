package org.jeecg.modules.test.sharding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * System log table
 * @author: zyf
 * @date: 2022/04/21
 */
@Data
@TableName("sys_log")
public class ShardingSysLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Creator
     */
    private String createBy;

    /**
     * creation time
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * Updater
     */
    private String updateBy;

    /**
     * Update time
     */
    private Date updateTime;

    /**
     * time consuming
     */
    private Long costTime;

    /**
     * IP
     */
    private String ip;

    /**
     * Request parameters
     */
    private String requestParam;

    /**
     * Request type
     */
    private String requestType;

    /**
     * Request path
     */
    private String requestUrl;
    /**
     * Request method
     */
    private String method;

    /**
     * Operator username
     */
    private String username;
    /**
     * Operator user account
     */
    private String userid;
    /**
     * Operation detailed log
     */
    private String logContent;

    /**
     * Log type（1Login log，2Operation log）
     */
    @Dict(dicCode = "log_type")
    private Integer logType;

    /**
     * Operation type（1Query，2Add to，3Revise，4delete,5import，6Export）
     */
    @Dict(dicCode = "operate_type")
    private Integer operateType;

}
