package org.jeecg.modules.message.entity;

import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: Message template
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_sms_template")
public class SysMessageTemplate extends JeecgEntity{
	/**templateCODE*/
	@Excel(name = "templateCODE", width = 15)
	private java.lang.String templateCode;
	/**template标题*/
	@Excel(name = "template标题", width = 30)
	private java.lang.String templateName;
	/**template内容*/
	@Excel(name = "template内容", width = 50)
	private java.lang.String templateContent;
	/**template测试json*/
	@Excel(name = "template测试json", width = 15)
	private java.lang.String templateTestJson;
	/**template类型*/
	@Excel(name = "template类型", width = 15)
	private java.lang.String templateType;
	/**template分类*/
	@Excel(name = "template类型(noticeNotices and Announcements otherother)", width = 15)
	private java.lang.String templateCategory;

	/**Already applied/Not applied  1yes0no*/
	@Excel(name = "Application status", width = 15)
	private String useStatus;

}
