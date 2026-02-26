package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: Department position user
 * @author: wangshuai
 * @date: 2025/9/5 11:45
 */
@Data
@TableName("sys_user_dep_post")
public class SysUserDepPost implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * primary keyid
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary keyid")
    private String id;
    /**
     * userid
     */
    @Schema(description = "userid")
    private String userId;
    /**
     * Department positionsid
     */
    @Schema(description = "Department positionsid")
    private String depId;

    /**
     * Creator
     */
    @Schema(description = "Creator")
    private String createBy;

    /**
     * creation time
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "creation time")
    private Date createTime;
    /**
     * Updater
     */
    @Schema(description = "Updater")
    private String updateBy;
    /**
     * Update time
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update time")
    private Date updateTime;
    /**
     * Institution code
     */
    @Excel(name = "Institution code", width = 15)
    @Schema(description = "Institution code")
    private String orgCode;

    public SysUserDepPost(String id, String userId, String depId) {
        super();
        this.id = id;
        this.userId = userId;
        this.depId = depId;
    }

    public SysUserDepPost(String userId, String departId) {
        this.userId = userId;
        this.depId = departId;
    }
}
