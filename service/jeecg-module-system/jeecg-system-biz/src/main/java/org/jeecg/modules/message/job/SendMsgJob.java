package org.jeecg.modules.message.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.message.entity.SysMessage;
import org.jeecg.modules.message.handle.enums.SendMsgStatusEnum;
import org.jeecg.modules.message.service.ISysMessageService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Send message task
 * @author: jeecg-boot
 */

@Slf4j
public class SendMsgJob implements Job {

	@Autowired
	private ISysMessageService sysMessageService;

	@Autowired
	private ISysBaseAPI sysBaseAPI;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		log.info(String.format(" Jeecg-Boot Send message task SendMsgJob !  time:" + DateUtils.getTimestamp()));

		// 1.Read message center data，Only query those that have not been sent and those that have failed to be sent no more than once.
		QueryWrapper<SysMessage> queryWrapper = new QueryWrapper<SysMessage>();
		queryWrapper.eq("es_send_status", SendMsgStatusEnum.WAIT.getCode())
				.or(i -> i.eq("es_send_status", SendMsgStatusEnum.FAIL.getCode()).lt("es_send_num", 6));
		List<SysMessage> sysMessages = sysMessageService.list(queryWrapper);
		System.out.println(sysMessages);
		// 2.According to different types, there is no way to send the implementation class
		for (SysMessage sysMessage : sysMessages) {
			//update-begin-author:taoyan date:2022-7-8 for: Template message sending test calling method modification
			Integer sendNum = sysMessage.getEsSendNum();
			try {
				MessageDTO md = new MessageDTO();
				md.setTitle(sysMessage.getEsTitle());
				md.setContent(sysMessage.getEsContent());
				md.setToUser(sysMessage.getEsReceiver());
				md.setType(sysMessage.getEsType());
				md.setToAll(false);
				//update-begin---author:wangshuai---date:2024-11-12---for:【QQYUN-8523】Send email notification via KnockKou Cloud，unstable---
				md.setIsTimeJob(true);
				//update-end---author:wangshuai---date:2024-11-12---for:【QQYUN-8523】Send email notification via KnockKou Cloud，unstable---
				sysBaseAPI.sendTemplateMessage(md);
				//Message sent successfully
				sysMessage.setEsSendStatus(SendMsgStatusEnum.SUCCESS.getCode());
				//update-end-author:taoyan date:2022-7-8 for: Template message sending test calling method modification
			} catch (Exception e) {
				e.printStackTrace();
				// Exception occurred while sending message
				sysMessage.setEsSendStatus(SendMsgStatusEnum.FAIL.getCode());
			}
			sysMessage.setEsSendNum(++sendNum);
			// Send results and write back to database
			sysMessageService.updateById(sysMessage);
		}

	}

}
