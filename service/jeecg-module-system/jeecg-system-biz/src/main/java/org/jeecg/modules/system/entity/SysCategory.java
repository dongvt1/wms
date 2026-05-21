package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: Classification dictionary
 * @Author: jeecg-boot
 * @Date:   2019-05-29
 * @Version: V1.0
 */
@Data
@TableName("sys_category")
public class SysCategory implements Serializable,Comparable<SysCategory>{
    private static final long serialVersionUID = 1L;
    
	/**primary key*/
	@TableId(type = IdType.ASSIGN_ID)
	private java.lang.String id;
	/**parent node*/
	private java.lang.String pid;
	/**Type name*/
	@Excel(name = "Type name", width = 15)
	private java.lang.String name;
	/**type encoding*/
	@Excel(name = "type encoding", width = 15)
	private java.lang.String code;
	/**Creator*/
	private java.lang.String createBy;
	/**Creation date*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;
	/**Updater*/
	private java.lang.String updateBy;
	/**Update date*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateTime;
	/**Department*/
	private java.lang.String sysOrgCode;
	/**Whether there are child nodes*/
	@Excel(name = "Whether there are child nodes(1:have)", width = 15)
	private java.lang.String hasChild;

	/**tenantID*/
	private java.lang.Integer tenantId;

	@Override
	public int compareTo(SysCategory o) {
		//The comparison conditions we set are based oncodelength in ascending order
		// <0：The current object is smaller than the incoming object。
		// =0：The current object is equal to the incoming object。
		// >0：The current object is larger than the incoming object。
		int	 s = this.code.length() - o.code.length();
		return s;
	}
	@Override
	public String toString() {
		return "SysCategory [code=" + code + ", name=" + name + "]";
	}
}
