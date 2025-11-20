package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.modules.system.entity.SysAnnouncement;

import java.util.Date;
import java.util.List;

/**
 * @Description: System notification table
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
public interface ISysAnnouncementService extends IService<SysAnnouncement> {

    /**
     * Save system notifications
     * @param sysAnnouncement
     */
	public void saveAnnouncement(SysAnnouncement sysAnnouncement);

    /**
     * Modify system notification
     * @param sysAnnouncement
     * @return
     */
	public boolean upDateAnnouncement(SysAnnouncement sysAnnouncement);

    /**
     * Save system notifications
     * @param title title
     * @param msgContent Information content
     */
	public void saveSysAnnouncement(String title, String msgContent);

    /**
     * Paging query system announcement
     * @param page Current page number
     * @param userId userid
     * @param msgCategory Message type
     * @return Page<SysAnnouncement>
     */
	public Page<SysAnnouncement> querySysCementPageByUserId(Page<SysAnnouncement> page, String userId, String msgCategory, Integer tenantId, Date beginDate);

    /**
     * 获取user未读消息数量
     *
     * @param userId     userid
     * @param noticeType notification type
     * @return
     */
    public Integer getUnreadMessageCountByUserId(String userId, Date beginDate, String noticeType);


    /**
     *  补全当前登录user的消息阅读记录 
     * @Invalid and useless 2023-09-19
     */
    @Deprecated
	void completeAnnouncementSendInfo();

    /**
     * 补全所有user的推送公告关系数据
     *
     * @param commentId
     * @param tenantId
     */
    void batchInsertSysAnnouncementSend(String commentId, Integer tenantId);
    
    /**
     * 分页查询当前登录user的消息， And mark which messages are unread
     */
    List<SysAnnouncement> querySysMessageList(int pageSize, int pageNo, String fromUser, String starFlag, String busType, String msgCategory, Date beginDate, Date endDate, String noticeType);

    /**
     * Modify as read message
     */
    void updateReaded(List<String> annoceIdList);


    /**
     * Clear all unread messages
     */
    void clearAllUnReadMessage();

    /**
     * 查询user未阅读的通知公告
     * @param userId
     * @return
     */
    public List<String> getNotSendedAnnouncementlist(String userId);

    /**
     * Add visits
     * @param id
     * @param count
     */
    void updateVisitsNum(String id, int count);

    /**
     * Download files in batches
     * @param id
     * @param request
     * @param response
     */
    void downLoadFiles(String id, HttpServletRequest request, HttpServletResponse response);
}
