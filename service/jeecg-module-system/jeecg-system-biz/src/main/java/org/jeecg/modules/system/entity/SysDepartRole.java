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
 * @Description: Department role
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Data
@TableName("sys_depart_role")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Department role")
public class SysDepartRole {
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
	private java.lang.String id;
	/**departmentid*/
	@Excel(name = "departmentid", width = 15)
	@Schema(description = "departmentid")
	@Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
	private java.lang.String departId;
	/**Department role名称*/
	@Excel(name = "Department role名称", width = 15)
    @Schema(description = "Department role名称")
	private java.lang.String roleName;
	/**Department role编码*/
	@Excel(name = "Department role编码", width = 15)
    @Schema(description = "Department role编码")
	private java.lang.String roleCode;
	/**describe*/
	@Excel(name = "describe", width = 15)
    @Schema(description = "describe")
	private java.lang.String description;
	/**Creator*/
	@Excel(name = "Creator", width = 15)
    @Schema(description = "Creator")
	private java.lang.String createBy;
	/**creation time*/
	@Excel(name = "creation time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "creation time")
	private java.util.Date createTime;
	/**Updater*/
	@Excel(name = "Updater", width = 15)
    @Schema(description = "Updater")
	private java.lang.String updateBy;
	/**Update time*/
	@Excel(name = "Update time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Update time")
	private java.util.Date updateTime;


}
