package org.jeecg.modules.system.mapper;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysAnnouncement;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @Description: System notification table
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
public interface SysAnnouncementMapper extends BaseMapper<SysAnnouncement> {

    /**
     * By message type and useridGet system notifications
     * @param page
     * @param userId userid
     * @param msgCategory Message type
     * @return
     */
	List<SysAnnouncement> querySysCementListByUserId(Page<SysAnnouncement> page, @Param("userId")String userId,@Param("msgCategory")String msgCategory,
                                                     @Param("tenantId")Integer tenantId, @Param("beginDate")Date beginDate);

    /**
     * 获取user未读消息数量
     *
     * @param userId userid
     * @param noticeType
     * @return
     */
    Integer getUnreadMessageCountByUserId(@Param("userId") String userId, @Param("beginDate") Date beginDate, @Param("noticeType") String noticeType);

    /**
     * Query all message list by page
     * @param page
     * @param userId
     * @param fromUser
     * @param beginDate
     * @param endDate
     * @param noticeType
     * @return
     */
	List<SysAnnouncement> queryAllMessageList(Page<SysAnnouncement> page, @Param("userId")String userId, @Param("fromUser")String fromUser, @Param("starFlag")String starFlag, @Param("busType")String busType, @Param("msgCategory")String msgCategory, @Param("beginDate")Date beginDate, @Param("endDate")Date endDate, @Param("noticeType") String noticeType);
   
    /**
     * 查询user未阅读的通知公告
     * @param currDate
     * @param userId
     * @return
     */
    List<String> getNotSendedAnnouncementlist(@Param("currDate") Date currDate, @Param("userId")String userId);
}
