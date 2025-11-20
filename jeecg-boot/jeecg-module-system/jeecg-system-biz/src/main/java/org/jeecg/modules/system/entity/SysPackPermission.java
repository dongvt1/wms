package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Product package menu relationship table
 * @Author: jeecg-boot
 * @Date:   2022-12-31
 * @Version: V1.0
 */
@Data
@TableName("sys_tenant_pack_perms")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="Product package menu relationship table")
public class SysPackPermission implements Serializable {
    private static final long serialVersionUID = 1L;

	/**primary key number*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary key number")
    private String id;
	/**Tenant product package name*/
	@Excel(name = "Tenant product package name", width = 15)
    @Schema(description = "Tenant product package name")
    private String packId;
	/**menuid*/
	@Excel(name = "menuid", width = 15)
    @Schema(description = "menuid")
    private String permissionId;
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
}
