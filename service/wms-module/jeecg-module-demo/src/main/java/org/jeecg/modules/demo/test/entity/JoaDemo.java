package org.jeecg.modules.demo.test.entity;

import java.io.Serializable;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @Description: Process testing
 * @Author: jeecg-boot
 * @Date:   2019-05-14
 * @Version: V1.0
 */
@Data
@TableName("joa_demo")
public class JoaDemo implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**ID*/
	@TableId(type = IdType.ASSIGN_ID)
	private java.lang.String id;
	/**Leave person*/
	@Excel(name = "Leave person", width = 15)
	private java.lang.String name;
	/**days of leave*/
	@Excel(name = "days of leave", width = 15)
	private java.lang.Integer days;
	/**start time*/
	@Excel(name = "start time", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
	private java.util.Date beginDate;
	/**Leave end time*/
	@Excel(name = "Leave end time", width = 20, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
	private java.util.Date endDate;
	/**Reason for leave*/
	@Excel(name = "Reason for leave", width = 15)
	private java.lang.String reason;
	/**process status*/
	@Excel(name = "process status", width = 15)
	private java.lang.String bpmStatus;
	/**Creatorid*/
	@Excel(name = "Creatorid", width = 15)
	private java.lang.String createBy;
	/**creation time*/
	@Excel(name = "creation time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;
	/**modification time*/
	@Excel(name = "modification time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateTime;
	/**Modifierid*/
	@Excel(name = "Modifierid", width = 15)
	private java.lang.String updateBy;
}
