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
 * @Description: sys_user_tenant_relation
 * @Author: jeecg-boot
 * @Date:   2022-12-23
 * @Version: V1.0
 */
@Data
@TableName("sys_user_tenant")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="sys_user_tenant")
public class SysUserTenant implements Serializable {
    private static final long serialVersionUID = 1L;

	/**primary keyid*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "primary keyid")
    private String id;
	/**userid*/
	@Excel(name = "userid", width = 15)
    @Schema(description = "userid")
    private String userId;
	/**tenantid*/
	@Excel(name = "tenantid", width = 15)
    @Schema(description = "tenantid")
    private Integer tenantId;
	/**state(1 normal 2 freeze 3 Pending review 4 reject)*/
	@Excel(name = "state(1 normal 2 freeze 3 Pending review 4 reject)", width = 15)
    @Schema(description = "state(1 normal 2 freeze 3 Pending review 4 reject)")
    private String status;
	/**Creator login name*/
    @Schema(description = "Creator login name")
    private String createBy;
	/**Creation date*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Creation date")
    private Date createTime;
	/**Updater login name*/
    @Schema(description = "Updater login name")
    private String updateBy;
	/**Update date*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Schema(description = "Update date")
    private Date updateTime;
}