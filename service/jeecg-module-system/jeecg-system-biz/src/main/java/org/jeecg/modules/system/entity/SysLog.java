package org.jeecg.modules.system.entity;

import java.util.Date;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * System log table
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysLog implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	/**
	 * Creator
	 */
	private String createBy;

	/**
	 * creation time
	 */
	@Excel(name = "creation time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * Updater
	 */
	private String updateBy;

	/**
	 * Update time
	 */
	private Date updateTime;

	/**
	 * time consuming
	 */
	@Excel(name = "time consuming（millisecond）", width = 15)
	private Long costTime;

	/**
	 * IP
	 */
	@Excel(name = "IP", width = 15)
	private String ip;

	/**
	 * Request parameters
	 */
	private String requestParam;

	/**
	 * Request type
	 */
	private String requestType;

	/**
	 * Request path
	 */
	private String requestUrl;
	/**
	 * Request method
	 */
	private String method;

	/**
	 * Operator username
	 */
	private String username;
	/**
	 * Operator user account
	 */
	@Excel(name = "operator", width = 15)
	private String userid;
	/**
	 * Operation detailed log
	 */
	@Excel(name = "Log content", width = 50)
	private String logContent;

	/**
	 * Log type（1Login log，2Operation log）
	 */
	@Dict(dicCode = "log_type")
	private Integer logType;

	/**
	 * Operation type（1Query，2Add to，3Revise，4delete,5import，6Export）
	 */
	@Dict(dicCode = "operate_type")
	private Integer operateType;
	
	/**
	 * Client terminal type pc:PC app:Mobile version h5:Mobile web client
	 */
	@Excel(name = "client type", width = 15, dicCode = "client_type")
	@Dict(dicCode = "client_type")
	private String clientType;

	/**
	 * tenantID
	 */
	private Integer tenantId;

}
