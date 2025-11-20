package org.jeecg.modules.system.service;

import java.util.List;

import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.model.AnnouncementSendModel;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: User Announcement Reading Mark Sheet
 * @Author: jeecg-boot
 * @Date:  2019-02-21
 * @Version: V1.0
 */
public interface ISysAnnouncementSendService extends IService<SysAnnouncementSend> {

	/**
	 * get my message
	 * @param announcementSendModel
     * @param page Current page number
	 * @return
	 */
	public Page<AnnouncementSendModel> getMyAnnouncementSendPage(Page<AnnouncementSendModel> page,AnnouncementSendModel announcementSendModel);

	/**
	 * Send records based on messagesIDGet message content
	 * @return
	 */
	AnnouncementSendModel getOne(String sendId);
	

}
