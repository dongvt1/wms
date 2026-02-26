package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: Role homepage configuration
 * @Author: liusq
 * @Date:   2022-03-25
 * @Version: V1.0
 */
@Data
@TableName("sys_role_index")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Role homepage configuration")
public class SysRoleIndex {
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
	private java.lang.String id;
	/**role coding*/
	@Excel(name = "role coding", width = 15)
    @Schema(description = "role coding")
	private java.lang.String roleCode;
	/**routing address*/
	@Excel(name = "routing address", width = 15)
    @Schema(description = "routing address")
	private java.lang.String url;
	/**routing address*/
	@Excel(name = "routing address", width = 15)
    @Schema(description = "components")
	private java.lang.String component;
	/**
	 * Whether to route menu: 0:no  1:yes（default value1）
	 */
	@Excel(name = "Whether to route menu", width = 15)
	@Schema(description = "Whether to route menu")
	@TableField(value="is_route")
	private Boolean route;
	/**priority*/
	@Excel(name = "priority", width = 15)
    @Schema(description = "priority")
	private java.lang.Integer priority;
	/**routing address*/
	@Excel(name = "state", width = 15)
	@Schema(description = "state")
	private java.lang.String status;
	/**Creator login name*/
	@Excel(name = "Creator login name", width = 15)
    @Schema(description = "Creator login name")
	private java.lang.String createBy;
	/**Creation date*/
	@Excel(name = "Creation date", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation date")
	private java.util.Date createTime;
	/**Updater login name*/
	@Excel(name = "Updater login name", width = 15)
    @Schema(description = "Updater login name")
	private java.lang.String updateBy;
	/**Update date*/
	@Excel(name = "Update date", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update date")
	private java.util.Date updateTime;
	/**Department*/
	@Excel(name = "Department", width = 15)
    @Schema(description = "Department")
	private java.lang.String sysOrgCode;

	/**Association type(ROLE:Role USER:Represents user)*/
    @Schema(description = "Association type")
	@Excel(name = "Association type", width = 15, dicCode = "relation_type")
	@Dict(dicCode = "relation_type")
	private java.lang.String relationType;


	public SysRoleIndex() {

	}
	public SysRoleIndex(String componentUrl){
		this.component = componentUrl;
	}
}
