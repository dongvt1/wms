package org.jeecg.modules.message.entity;

import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: information
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_sms")
public class SysMessage extends JeecgEntity {
	/**push content*/
	@Excel(name = "push content", width = 15)
	private java.lang.String esContent;
	/**Push required parametersJsonFormat*/
	@Excel(name = "Push required parametersJsonFormat", width = 15)
	private java.lang.String esParam;
	/**recipient*/
	@Excel(name = "recipient", width = 15)
	private java.lang.String esReceiver;
	/**Reason for push failure*/
	@Excel(name = "Reason for push failure", width = 15)
	private java.lang.String esResult;
	/**Send times*/
	@Excel(name = "Send times", width = 15)
	private java.lang.Integer esSendNum;
	/**push status 0Not pushed 1Push successful 2Push failed*/
	@Excel(name = "push status 0Not pushed 1Push successful 2Push failed", width = 15)
	@Dict(dicCode = "msgSendStatus")
	private java.lang.String esSendStatus;
	/**Push time*/
	@Excel(name = "Push time", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date esSendTime;
	/**information标题*/
	@Excel(name = "information标题", width = 15)
	private java.lang.String esTitle;
	/**
	 * Push method：Reference enum classMessageTypeEnum
	 */
	@Excel(name = "Push method", width = 15)
	@Dict(dicCode = "messageType")
	private java.lang.String esType;
	/**Remark*/
	@Excel(name = "Remark", width = 15)
	private java.lang.String remark;
}
