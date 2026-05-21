package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Tenant product package
 * @Author: jeecg-boot
 * @Date:   2022-12-31
 * @Version: V1.0
 */
@Data
@TableName("sys_tenant_pack")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Tenant product package")
public class SysTenantPack implements Serializable {
    private static final long serialVersionUID = 1L;

	/**primary keyid*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary keyid")
    private String id;
	/**tenantid*/
	@Excel(name = "tenantid", width = 15)
    @Schema(description = "tenantid")
    private Integer tenantId;
	/**Product package name*/
	@Excel(name = "Product package name", width = 15)
    @Schema(description = "Product package name")
    private String packName;
	/**On state(0 Not turned on 1turn on)*/
	@Excel(name = "On state(0 Not turned on 1turn on)", width = 15)
    @Schema(description = "On state(0 Not turned on 1turn on)")
    private String status;
	/**Remark*/
	@Excel(name = "Remark", width = 15)
    @Schema(description = "Remark")
    private String remarks;
	/**Creator*/
    @Schema(description = "Creator")
    private String createBy;
	/**creation time*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "creation time")
    private Date createTime;
	/**Updater*/
    @Schema(description = "Updater")
    private String updateBy;
	/**Update time*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Update time")
    private Date updateTime;
    /**Product package type(default Default product package custom Custom product packages)*/
    @Excel(name = "Product package type", width = 15)
    @Schema(description = "Product package type")
	private String packType;

    /**
     * Whether to automatically assign to users(0 no 1yes)
     */
    @Excel(name = "Whether to automatically assign to users(0 no 1yes)", width = 15)
    @Schema(description = "Whether to automatically assign to users")
    private String izSysn;
    
    /**menuid 临时字段用于新增编辑menuidtransfer*/
    @TableField(exist = false)
    private String permissionIds;
    
    
    /**
     * coding
     */
    private String packCode;
    
    public SysTenantPack(){
        
    }

    public SysTenantPack(Integer tenantId, String packName, String packCode){
        this.tenantId = tenantId;
        this.packCode = packCode;
        this.packName = packName;
        this.status = "1";
    }
}
