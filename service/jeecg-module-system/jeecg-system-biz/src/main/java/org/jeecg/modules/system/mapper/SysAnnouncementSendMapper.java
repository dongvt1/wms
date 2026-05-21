package org.jeecg.modules.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.model.AnnouncementSendModel;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @Description: User Announcement Reading Mark Sheet
 * @Author: jeecg-boot
 * @Date:  2019-02-21
 * @Version: V1.0
 */
public interface SysAnnouncementSendMapper extends BaseMapper<SysAnnouncementSend> {

	/**
	 * get my message
	 * @param announcementSendModel
	 * @param page
	 * @return
	 */
	public List<AnnouncementSendModel> getMyAnnouncementSendList(Page<AnnouncementSendModel> page,@Param("announcementSendModel") AnnouncementSendModel announcementSendModel);

	/**
	 * Get a record
	 * @param sendId
	 * @return
	 */
	AnnouncementSendModel getOne(@Param("sendId") String sendId);


	/**
	 * Modify as read message
	 */
	void updateReaded(@Param("userId") String userId, @Param("annoceIdList") List<String> annoceIdList);

	/**
	 * Clear all unread messages
	 * @param userId
	 */
	void clearAllUnReadMessage(@Param("userId") String userId);
}
