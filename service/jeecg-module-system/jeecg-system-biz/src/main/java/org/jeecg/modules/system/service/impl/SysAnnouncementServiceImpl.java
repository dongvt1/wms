package org.jeecg.modules.system.service.impl;

import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.FileDownloadUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.mapper.SysAnnouncementMapper;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.service.ISysAnnouncementSendService;
import org.jeecg.modules.system.service.ISysAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.net.URLEncoder;
import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: System notification table
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
@Service
@Slf4j
public class SysAnnouncementServiceImpl extends ServiceImpl<SysAnnouncementMapper, SysAnnouncement> implements ISysAnnouncementService {
	/**
	 * Supplement data and change to background mode
	 */
	public static ExecutorService completeNoteThreadPool = new ThreadPoolExecutor(0, 1024, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	
	@Resource
	private SysAnnouncementMapper sysAnnouncementMapper;
	@Resource
	private SysUserMapper sysUserMapper;
	@Resource
	private SysAnnouncementSendMapper sysAnnouncementSendMapper;
	@Autowired
	private ISysAnnouncementSendService sysAnnouncementSendService;
	@Autowired
	private JeecgBaseConfig jeecgBaseConfig;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveAnnouncement(SysAnnouncement sysAnnouncement) {
		if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
			sysAnnouncementMapper.insert(sysAnnouncement);
		}else {
			// 1.Insert notification table record
			sysAnnouncementMapper.insert(sysAnnouncement);
			// 2.Insert user notification reading mark table record
			String userId = sysAnnouncement.getUserIds();
            //update-begin-author:liusq---date:2023-10-31--for:[issues/5503]【announcement】Notifications cannot be received
			if(StringUtils.isNotBlank(userId) && userId.endsWith(",")){
				userId = userId.substring(0, (userId.length()-1));
			}
			String[] userIds = userId.split(",");
            //update-end-author:liusq---date:2023-10-31--for:[issues/5503]【announcement】Notifications cannot be received
			String anntId = sysAnnouncement.getId();
			Date refDate = new Date();
			for(int i=0;i<userIds.length;i++) {
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(anntId);
				announcementSend.setUserId(userIds[i]);
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				announcementSend.setReadTime(refDate);
				sysAnnouncementSendMapper.insert(announcementSend);
			}
		}
	}
	
	/**
	 * @Function：Edit message information
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean upDateAnnouncement(SysAnnouncement sysAnnouncement) {
		// 1.Update system information table data
		sysAnnouncementMapper.updateById(sysAnnouncement);
		String userId = sysAnnouncement.getUserIds();
		if(oConvertUtils.isNotEmpty(userId)&&sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_UESR)) {
			// 2.Add new notification user data
			String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
			String anntId = sysAnnouncement.getId();
			Date refDate = new Date();
			for(int i=0;i<userIds.length;i++) {
				LambdaQueryWrapper<SysAnnouncementSend> queryWrapper = new LambdaQueryWrapper<SysAnnouncementSend>();
				queryWrapper.eq(SysAnnouncementSend::getAnntId, anntId);
				queryWrapper.eq(SysAnnouncementSend::getUserId, userIds[i]);
				List<SysAnnouncementSend> announcementSends=sysAnnouncementSendMapper.selectList(queryWrapper);
				if(announcementSends.size()<=0) {
					SysAnnouncementSend announcementSend = new SysAnnouncementSend();
					announcementSend.setAnntId(anntId);
					announcementSend.setUserId(userIds[i]);
					announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
					announcementSend.setReadTime(refDate);
					sysAnnouncementSendMapper.insert(announcementSend);
				}
			}
			// 3. Delete redundant notification user data
			Collection<String> delUserIds = Arrays.asList(userIds);
			LambdaQueryWrapper<SysAnnouncementSend> queryWrapper = new LambdaQueryWrapper<SysAnnouncementSend>();
			queryWrapper.notIn(SysAnnouncementSend::getUserId, delUserIds);
			queryWrapper.eq(SysAnnouncementSend::getAnntId, anntId);
			sysAnnouncementSendMapper.delete(queryWrapper);
		}
		return true;
	}

    /**
     * Process execution completed save message notification
     * @param title title
     * @param msgContent Information content
     */
	@Override
	public void saveSysAnnouncement(String title, String msgContent) {
		SysAnnouncement announcement = new SysAnnouncement();
		announcement.setTitile(title);
		announcement.setMsgContent(msgContent);
		announcement.setSender("JEECG BOOT");
		announcement.setPriority(CommonConstant.PRIORITY_L);
		announcement.setMsgType(CommonConstant.MSG_TYPE_ALL);
		announcement.setSendStatus(CommonConstant.HAS_SEND);
		announcement.setSendTime(new Date());
		announcement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
		sysAnnouncementMapper.insert(announcement);
	}

	@Override
	public Page<SysAnnouncement> querySysCementPageByUserId(Page<SysAnnouncement> page, String userId, String msgCategory, Integer tenantId, Date beginDate) {
		if (page.getSize() == -1) {
			return page.setRecords(sysAnnouncementMapper.querySysCementListByUserId(null, userId, msgCategory,tenantId,beginDate));
		} else {
			return page.setRecords(sysAnnouncementMapper.querySysCementListByUserId(page, userId, msgCategory,tenantId,beginDate));
		}
	}

	@Override
	public Integer getUnreadMessageCountByUserId(String userId, Date beginDate, String noticeType) {
		return sysAnnouncementMapper.getUnreadMessageCountByUserId(userId, beginDate, noticeType);
	}

	@Override
	public void completeAnnouncementSendInfo() {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String userId = sysUser.getId();
		List<String> announcementIds = this.getNotSendedAnnouncementlist(userId);
		List<SysAnnouncementSend> sysAnnouncementSendList = new ArrayList<>();
		if (!CollectionUtils.isEmpty(announcementIds)) {
			for (String commentId : announcementIds) {
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(commentId);
				announcementSend.setUserId(userId);
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendList.add(announcementSend);
			}
		}
		if (!CollectionUtils.isEmpty(sysAnnouncementSendList)) {
			sysAnnouncementSendService.saveBatch(sysAnnouncementSendList);
		}
	}

	@Override
	public void batchInsertSysAnnouncementSend(String commentId, Integer tenantId) {
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && oConvertUtils.isNotEmpty(tenantId)) {
			log.info("Completeannouncement与用户的关系数据，tenantID = {}", tenantId);
		} else {
			tenantId = null;
		}
		
		List<String> userIdList = sysUserMapper.getTenantUserIdList(tenantId);
		List<SysAnnouncementSend> sysAnnouncementSendList = new ArrayList<>();
		if (!CollectionUtils.isEmpty(userIdList)) {
			for (String userId : userIdList) {
				SysAnnouncementSend announcementSend = new SysAnnouncementSend();
				announcementSend.setAnntId(commentId);
				announcementSend.setUserId(userId);
				announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendList.add(announcementSend);
			}
		}
		if (!CollectionUtils.isEmpty(sysAnnouncementSendList)) {
			log.info("Completeannouncement与用户的关系数据，sysAnnouncementSendList size = {}", sysAnnouncementSendList.size());
			sysAnnouncementSendService.saveBatch(sysAnnouncementSendList);
		}
	}

	@Override
	public List<SysAnnouncement> querySysMessageList(int pageSize, int pageNo, String fromUser, String starFlag, String busType, String msgCategory, Date beginDate, Date endDate, String noticeType) {
//		//1. Completesendtable data
//		completeNoteThreadPool.execute(()->{
//			completeAnnouncementSendInfo();
//		});
		
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		log.info(" Get the login person LoginUser id: {}", sysUser.getId());
		Page<SysAnnouncement> page = new Page<SysAnnouncement>(pageNo,pageSize);
		List<SysAnnouncement> list = baseMapper.queryAllMessageList(page, sysUser.getId(), fromUser, starFlag, busType, msgCategory,beginDate, endDate, noticeType);
		return list;
	}

	@Override
	public void updateReaded(List<String> annoceIdList) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		sysAnnouncementSendMapper.updateReaded(sysUser.getId(), annoceIdList);
	}

	@Override
	public void clearAllUnReadMessage() {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		sysAnnouncementSendMapper.clearAllUnReadMessage(sysUser.getId());
	}

	/**
	 * 查询用户未读的通知announcement，preventSQLInjection writing adjustment
	 * @param userId
	 * @return
	 */

	@Override
	public List<String> getNotSendedAnnouncementlist(String userId) {
		return sysAnnouncementMapper.getNotSendedAnnouncementlist(new Date(), userId);
	}

	/**
	 * Update visits
	 * @param id
	 * @param increaseCount
	 */
	@Override
	public void updateVisitsNum(String id, int increaseCount) {
		SysAnnouncement sysAnnouncement = sysAnnouncementMapper.selectById(id);
		if (oConvertUtils.isNotEmpty(sysAnnouncement)) {
			int visits = oConvertUtils.getInt(sysAnnouncement.getVisitsNum(), 0);
			int totalValue = increaseCount + visits;
			sysAnnouncement.setVisitsNum(totalValue);
			sysAnnouncementMapper.updateById(sysAnnouncement);
			log.info("通知announcement：{} Visits+1，Total visits：{}", sysAnnouncement.getTitile(), sysAnnouncement.getVisitsNum());
		}
	}

	/**
	 * Download files in batches
	 * @param id
	 * @param request
	 * @param response
	 */
	@Override
	public void downLoadFiles(String id, HttpServletRequest request, HttpServletResponse response) {
		// Parameter verification
		if (oConvertUtils.isEmpty(id)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		// Get article information
		SysAnnouncement sysAnnouncement = this.baseMapper.selectById(id);
		if (oConvertUtils.isEmpty(sysAnnouncement)) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		//set upHTTPresponse header：Prepare file download
		response.reset();
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/force-download");
		ZipArchiveOutputStream zous = null;
		try {
			// generateZIPfile name：使用文章title+Timestamp to avoid duplicate names
			String title = sysAnnouncement.getTitile() + new Date().getTime();
			String zipName = URLEncoder.encode( title + ".zip", "UTF-8").replaceAll("\\+", "%20");
			response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + zipName);
			// createZIPoutput stream：output directly toHTTPresponse stream
			zous = new ZipArchiveOutputStream(response.getOutputStream());
			zous.setUseZip64(Zip64Mode.AsNeeded);// Support large files

			// Download files in batches
			String[] fileUrls = sysAnnouncement.getFiles().split(",");
			// Iterate through all filesURL
			for (int i = 0; i < fileUrls.length; i++) {
				String fileUrl = fileUrls[i].trim();
				if (oConvertUtils.isEmpty(fileUrl)) {
					continue;
				}
				// generateZIP内file name：avoid duplication of names，Add serial number
				String fileName = FileDownloadUtils.generateFileName(fileUrl, i, fileUrls.length);
				String uploadUrl = jeecgBaseConfig.getPath().getUpload();
				// Download individual files and add toZIP
				FileDownloadUtils.downLoadSingleFile(fileUrl,fileName,uploadUrl, zous);
			}
			// FinishZIPwrite
			zous.finish();
			// Flush the buffer to ensure data is sent
			response.flushBuffer();
		} catch (IOException e) {
			log.error("File download failed"+e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			// Make sure the stream is closed，prevent资源泄漏
			IoUtil.close(zous);
		}
	}
}
