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

import java.io.Serializable;

/**
 * @Description: Tenant product package user relationship table
 * @Author: jeecg-boot
 * @Date:   2023-02-16
 * @Version: V1.0
 */
@Data
@TableName("sys_tenant_pack_user")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Tenant product package user relationship table")
public class SysTenantPackUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /**id*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private java.lang.String id;
    /**Tenant product packageID*/
    @Excel(name = "Tenant product packageID", width = 15)
    @Schema(description = "Tenant product packageID")
    private java.lang.String packId;
    /**userID*/
    @Excel(name = "userID", width = 15)
    @Schema(description = "userID")
    private java.lang.String userId;
    /**tenantID*/
    @Excel(name = "tenantID", width = 15)
    @Schema(description = "tenantID")
    private java.lang.Integer tenantId;
    /**Creator*/
    @Schema(description = "Creator")
    private java.lang.String createBy;
    /**creation time*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "creation time")
    private java.util.Date createTime;
    /**Updater*/
    @Schema(description = "Updater")
    private java.lang.String updateBy;
    /**Update time*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Update time")
    private java.util.Date updateTime;

    private transient String realname;

    private transient String packName;

    private transient String packCode;

    /**
     * state(申请state0 正常state1)
     */
    private Integer status;

    public SysTenantPackUser(){
        
    }
    public SysTenantPackUser(Integer tenantId, String packId, String userId) {
        this.packId = packId;
        this.userId = userId;
        this.tenantId = tenantId;
        this.status = 1;
    }

    public SysTenantPackUser(SysTenantPackUser param, String userId, String realname) {
        this.userId = userId;
        this.realname = realname;
        this.packId = param.getPackId();
        this.tenantId = param.getTenantId();
        this.packName = param.getPackName();
        this.status = 1;
    }
}
