package org.jeecg.modules.quartz.entity;

import java.io.Serializable;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @Description: Online management of scheduled tasks
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
@Data
@TableName("sys_quartz_job")
public class QuartzJob implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
	private java.lang.String id;
	/**Creator*/
	private java.lang.String createBy;
	/**creation time*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;
	/**delete status*/
	private java.lang.Integer delFlag;
	/**Modifier*/
	private java.lang.String updateBy;
	/**modification time*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateTime;
	/**Task class name*/
	@Excel(name="Task class name",width=40)
	private java.lang.String jobClassName;
	/**cronexpression*/
	@Excel(name="cronexpression",width=30)
	private java.lang.String cronExpression;
	/**parameter*/
	@Excel(name="parameter",width=15)
	private java.lang.String parameter;
	/**describe*/
	@Excel(name="describe",width=40)
	private java.lang.String description;
	/**state 0normal -1stop*/
	@Excel(name="state",width=15,dicCode="quartz_status")
	@Dict(dicCode = "quartz_status")
	private java.lang.Integer status;

}
