package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeecg.dingtalk.api.core.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.CommonSendStatus;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.constant.enums.NoticeTypeEnum;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.message.enums.RangeDateEnum;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.service.ISysAnnouncementSendService;
import org.jeecg.modules.system.service.ISysAnnouncementService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.jeecg.modules.system.service.impl.ThirdAppDingtalkServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.jeecg.modules.system.util.XssUtils;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.jeecg.common.constant.CommonConstant.ANNOUNCEMENT_SEND_STATUS_1;

/**
 * @Title: Controller
 * @Description: System notification table
 * @Author: jeecg-boot
 * @Date: 2019-01-02
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/annountCement")
@Slf4j
public class SysAnnouncementController {
	@Autowired
	private ISysAnnouncementService sysAnnouncementService;
	@Autowired
	private ISysAnnouncementSendService sysAnnouncementSendService;
	@Resource
    private WebSocket webSocket;
	@Autowired
    ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
	@Autowired
    ThirdAppDingtalkServiceImpl dingtalkService;
	@Autowired
	private SysBaseApiImpl sysBaseApi;
	@Autowired
	@Lazy
	private RedisUtil redisUtil;
	@Autowired
	public RedisTemplate redisTemplate;
	//General error definition
	private static final String SPECIAL_CHAR_ERROR = "Save failed:The message content contains special characters that are not supported by the database.，Please check and modify the content!";
	private static final String CONTENT_TOO_LONG_ERROR = "Save failed:The message content exceeds the maximum length limit，Please reduce content length!";
	private static final String DEFAULT_ERROR = "Operation failed，Please try again later or contact your administrator!";
	/**
	 * Announcement cache
	 */
	String ANNO_CACHE_KEY = "sys:cache:announcement";
	/**
	 * QQYUN-5072【Performance optimization】Online notification messages are a bit slow to open
	 */
	public static ExecutorService cachedThreadPool = new ThreadPoolExecutor(0, 1024,60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	public static ExecutorService completeNoteThreadPool = new ThreadPoolExecutor(0, 1024,60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

	/**
	  * Paginated list query
	 * @param sysAnnouncement
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysAnnouncement>> queryPageList(SysAnnouncement sysAnnouncement,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		//------------------------------------------------------------------------------------------------
		//Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysAnnouncement.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		Result<IPage<SysAnnouncement>> result = new Result<IPage<SysAnnouncement>>();
		sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
		QueryWrapper<SysAnnouncement> queryWrapper = QueryGenerator.initQueryWrapper(sysAnnouncement, req.getParameterMap());
		Page<SysAnnouncement> page = new Page<SysAnnouncement>(pageNo,pageSize);
		IPage<SysAnnouncement> pageList = sysAnnouncementService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

	/**
	  *   Add to
	 * @param sysAnnouncement
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysAnnouncement> add(@RequestBody SysAnnouncement sysAnnouncement) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		try {
			// update-begin-author:liusq date:20210804 for:Title handlingxssAttack problem
			String title = XssUtils.scriptXss(sysAnnouncement.getTitile());
			sysAnnouncement.setTitile(title);
			// update-end-author:liusq date:20210804 for:Title handlingxssAttack problem
			sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
            //Unpublished
			sysAnnouncement.setSendStatus(CommonSendStatus.UNPUBLISHED_STATUS_0);
            //process status
			sysAnnouncement.setBpmStatus("1");
            sysAnnouncement.setNoticeType(NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue());
			sysAnnouncementService.saveAnnouncement(sysAnnouncement);
			result.success("Add to成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500(determineErrorMessage(e));
		}
		return result;
	}

	/**
	  *  edit
	 * @param sysAnnouncement
	 * @return
	 */
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysAnnouncement> eidt(@RequestBody SysAnnouncement sysAnnouncement) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncementEntity = sysAnnouncementService.getById(sysAnnouncement.getId());
		try{
			if(sysAnnouncementEntity==null) {
				result.error500("No corresponding entity found");
			}else {
				// update-begin-author:liusq date:20210804 for:Title handlingxssAttack problem
				String title = XssUtils.scriptXss(sysAnnouncement.getTitile());
				sysAnnouncement.setTitile(title);
				// update-end-author:liusq date:20210804 for:Title handlingxssAttack problem
				sysAnnouncement.setNoticeType(NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue());
				boolean ok = sysAnnouncementService.upDateAnnouncement(sysAnnouncement);
				//TODO returnfalseWhat does it mean？
				if(ok) {
					result.success("Modification successful!");
				}
			}
		} catch (Exception e) {
			result.error500(determineErrorMessage(e));
		}

		return result;
	}
	/**
	  *  简单edit
	 * @param sysAnnouncement
	 * @return
	 */
	@RequestMapping(value = "/editIzTop", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysAnnouncement> editIzTop(@RequestBody SysAnnouncement sysAnnouncement) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncementEntity = sysAnnouncementService.getById(sysAnnouncement.getId());
		if(sysAnnouncementEntity==null) {
			result.error500("No corresponding entity found");
		}else {
			Integer izTop = sysAnnouncement.getIzTop();
			sysAnnouncementEntity.setIzTop(oConvertUtils.getInt(izTop,CommonConstant.IZ_TOP_0));
			sysAnnouncementService.updateById(sysAnnouncementEntity);
			result.success("Modification successful!");
		}
		return result;
	}

	/**
	  *   passiddelete
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<SysAnnouncement> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("No corresponding entity found");
		}else {
			sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_1.toString());
			boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
			if(ok) {
				result.success("delete成功!");
			}
		}

		return result;
	}

	/**
	  *  批量delete
	 * @param ids
	 * @return
	 */
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysAnnouncement> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("Parameter not recognized！");
		}else {
			String[] id = ids.split(",");
			for(int i=0;i<id.length;i++) {
				SysAnnouncement announcement = sysAnnouncementService.getById(id[i]);
				announcement.setDelFlag(CommonConstant.DEL_FLAG_1.toString());
				sysAnnouncementService.updateById(announcement);
			}
			result.success("delete成功!");
		}
		return result;
	}

	/**
	  * passidQuery
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/queryById", method = RequestMethod.GET)
	public Result<SysAnnouncement> queryById(@RequestParam(name="id",required=true) String id) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("No corresponding entity found");
		}else {
			result.setResult(sysAnnouncement);
			result.setSuccess(true);
		}
		return result;
	}

	/**
	 *	 update publish operation
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/doReleaseData", method = RequestMethod.GET)
	public Result<SysAnnouncement> doReleaseData(@RequestParam(name="id",required=true) String id, HttpServletRequest request) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("No corresponding entity found");
		}else {
            //Publish
			sysAnnouncement.setSendStatus(CommonSendStatus.PUBLISHED_STATUS_1);
			sysAnnouncement.setSendTime(new Date());
			String currentUserName = JwtUtil.getUserNameByToken(request);
			sysAnnouncement.setSender(currentUserName);
			boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
            if(oConvertUtils.isEmpty(sysAnnouncement.getNoticeType())){
                sysAnnouncement.setNoticeType(NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue());
            }
			if(ok) {
				result.success("System notification pushed successfully");
				if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
					// Complete the previous relationship between the announcement and the user
					sysAnnouncementService.batchInsertSysAnnouncementSend(sysAnnouncement.getId(), sysAnnouncement.getTenantId());
					
					// pushwebsocketnotify
					JSONObject obj = new JSONObject();
			    	obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
					obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
					obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
					obj.put(CommonConstant.NOTICE_TYPE, sysAnnouncement.getNoticeType());
			    	webSocket.sendMessage(obj.toJSONString());
				}else {
					// 2.Insert user notification reading mark table record
					String userId = sysAnnouncement.getUserIds();
					String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
					String anntId = sysAnnouncement.getId();
					Date refDate = new Date();
					JSONObject obj = new JSONObject();
			    	obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
					obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
					obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
                    obj.put(CommonConstant.NOTICE_TYPE, sysAnnouncement.getNoticeType());
			    	webSocket.sendMessage(userIds, obj.toJSONString());
				}
				try {
					// Synchronize enterprise WeChat、钉钉的消息notify
					Response<String> dtResponse = dingtalkService.sendActionCardMessage(sysAnnouncement, null, true);
					wechatEnterpriseService.sendTextCardMessage(sysAnnouncement, null,true);

					if (dtResponse != null && dtResponse.isSuccess()) {
						String taskId = dtResponse.getResult();
						sysAnnouncement.setDtTaskId(taskId);
						sysAnnouncementService.updateById(sysAnnouncement);
					}
				} catch (Exception e) {
					log.error("Send to third party simultaneouslyAPPMessage failed：", e);
				}
			}
		}

		return result;
	}

	/**
	 *	 Update undo operation
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/doReovkeData", method = RequestMethod.GET)
	public Result<SysAnnouncement> doReovkeData(@RequestParam(name="id",required=true) String id, HttpServletRequest request) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(id);
		if(sysAnnouncement==null) {
			result.error500("No corresponding entity found");
		}else {
            //Unpublish
			sysAnnouncement.setSendStatus(CommonSendStatus.REVOKE_STATUS_2);
			sysAnnouncement.setCancelTime(new Date());
			boolean ok = sysAnnouncementService.updateById(sysAnnouncement);
			if(ok) {
				result.success("该系统notify撤销成功");
				if (oConvertUtils.isNotEmpty(sysAnnouncement.getDtTaskId())) {
					try {
						dingtalkService.recallMessage(sysAnnouncement.getDtTaskId());
					} catch (Exception e) {
						log.error("third partyAPP撤回Message failed：", e);
					}
				}
			}
		}

		return result;
	}

	/**
	 * @Function：Supplement user data，并returnSystem messages
	 * @return
	 */
	@RequestMapping(value = "/listByUser", method = RequestMethod.GET)
	public Result<Map<String, Object>> listByUser(@RequestParam(required = false, defaultValue = "5") Integer pageSize, HttpServletRequest request) {
		long start = System.currentTimeMillis();
		Result<Map<String,Object>> result = new Result<Map<String,Object>>();
		Map<String,Object> sysMsgMap = new HashMap(5);
		LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		String userId = sysUser.getId();


		//update-begin---author:scott ---date:2024-05-11  for：【Performance optimization】优化系统notify，Only search close2个月的notify---
		// Get the first day of the previous month（Only search close两个月的notify）
		Date lastMonthStartDay = DateRangeUtils.getLastMonthStartDay();
		log.info("-----Queryclose两个月收到的未读notify-----，close2first day of month：{}", lastMonthStartDay);
		//update-end---author:scott ---date::2024-05-11  for：【Performance optimization】优化系统notify，Only search close2个月的notify---
		
//		//补push数据（use户和notify的关系表）
//		completeNoteThreadPool.execute(()->{
//			sysAnnouncementService.completeAnnouncementSendInfo();
//		});
		
		// 2.Queryuse户未读的System messages
		Page<SysAnnouncement> anntMsgList = new Page<SysAnnouncement>(0, pageSize);
        //notify公告消息
		anntMsgList = sysAnnouncementService.querySysCementPageByUserId(anntMsgList,userId,"1",null, lastMonthStartDay);
		sysMsgMap.put("anntMsgList", anntMsgList.getRecords());
		sysMsgMap.put("anntMsgTotal", anntMsgList.getTotal());

		log.info("begin Get userclose2Monthly System Announcements (notify)" + (System.currentTimeMillis() - start) + "millisecond");
		
        //System messages
		Page<SysAnnouncement> sysMsgList = new Page<SysAnnouncement>(0, pageSize);
		sysMsgList = sysAnnouncementService.querySysCementPageByUserId(sysMsgList,userId,"2",null, lastMonthStartDay);
		sysMsgMap.put("sysMsgList", sysMsgList.getRecords());
		sysMsgMap.put("sysMsgTotal", sysMsgList.getTotal());

		log.info("end Get user2Monthly System Announcements (System messages)" + (System.currentTimeMillis() - start) + "millisecond");
		
		result.setSuccess(true);
		result.setResult(sysMsgMap);
		return result;
	}


	/**
	 * 获取未读消息notify数量
	 *
	 * @return
	 */
	@RequestMapping(value = "/getUnreadMessageCount", method = RequestMethod.GET)
	public Result<Map<String, Integer>> getUnreadMessageCount(@RequestParam(required = false, defaultValue = "5") Integer pageSize, HttpServletRequest request) {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String userId = sysUser.getId();

		// Get the first day of the previous month（Only search close两个月的notify）
		Date lastMonthStartDay = DateRangeUtils.getLastMonthStartDay();
		log.info(" ------Queryclose两个月收到的未读notify消息数量------，close2first day of month：{}", lastMonthStartDay);
        //update-begin---author:wangshuai---date:2025-06-26---for:【QQYUN-12162】OAProject transformation，System heavy message splitting，The news is all together now Need to be split by category---
        Map<String,Integer> unreadMessageCount = new HashMap<>();
        //System messages数量
        Integer systemCount = sysAnnouncementService.getUnreadMessageCountByUserId(userId, lastMonthStartDay, NoticeTypeEnum.NOTICE_TYPE_SYSTEM.getValue());
        unreadMessageCount.put("systemCount",systemCount);
        //Number of processes
        Integer flowCount = sysAnnouncementService.getUnreadMessageCountByUserId(userId, lastMonthStartDay, NoticeTypeEnum.NOTICE_TYPE_FLOW.getValue());
        unreadMessageCount.put("flowCount",flowCount);
        //Number of files
        Integer fileCount = sysAnnouncementService.getUnreadMessageCountByUserId(userId, lastMonthStartDay,  NoticeTypeEnum.NOTICE_TYPE_FILE.getValue());
        unreadMessageCount.put("fileCount",fileCount);
        //Schedule quantity
        Integer planCount = sysAnnouncementService.getUnreadMessageCountByUserId(userId, lastMonthStartDay,  NoticeTypeEnum.NOTICE_TYPE_PLAN.getValue());
        unreadMessageCount.put("planCount",planCount);
        Integer count = systemCount + flowCount + fileCount + planCount;
        unreadMessageCount.put("count",count);
        //update-end---author:wangshuai---date:2025-06-26---for:【QQYUN-12162】OAProject transformation，System heavy message splitting，The news is all together now Need to be split by category---
        return Result.ok(unreadMessageCount);
	}


    /**
     * Exportexcel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysAnnouncement sysAnnouncement,HttpServletRequest request) {
        // Step.1 组装Query条件
        LambdaQueryWrapper<SysAnnouncement> queryWrapper = new LambdaQueryWrapper<SysAnnouncement>(sysAnnouncement);
        //Step.2 AutoPoi ExportExcel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		queryWrapper.eq(SysAnnouncement::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
        List<SysAnnouncement> pageList = sysAnnouncementService.list(queryWrapper);
        //Export文件名称
        mv.addObject(NormalExcelConstants.FILE_NAME, "System notification list");
        mv.addObject(NormalExcelConstants.CLASS, SysAnnouncement.class);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("System notification list数据", "Export人:"+user.getRealname(), "Export信息"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    /**
     * passexcelImport data
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Get the uploaded file object
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SysAnnouncement> listSysAnnouncements = ExcelImportUtil.importExcel(file.getInputStream(), SysAnnouncement.class, params);
                for (SysAnnouncement sysAnnouncementExcel : listSysAnnouncements) {
                	if(sysAnnouncementExcel.getDelFlag()==null){
                		sysAnnouncementExcel.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
					}
                	if(oConvertUtils.isEmpty(sysAnnouncementExcel.getIzTop())){
                		sysAnnouncementExcel.setIzTop(CommonConstant.IZ_TOP_0);
					}
                    sysAnnouncementService.save(sysAnnouncementExcel);
                }
                return Result.ok("File imported successfully！Number of data rows：" + listSysAnnouncements.size());
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                return Result.error("File import failed！");
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("File import failed！");
    }
	/**
	 *Sync messages
	 * @param anntId
	 * @return
	 */
	@RequestMapping(value = "/syncNotic", method = RequestMethod.GET)
	public Result<SysAnnouncement> syncNotic(@RequestParam(name="anntId",required=false) String anntId, HttpServletRequest request) {
		Result<SysAnnouncement> result = new Result<SysAnnouncement>();
		JSONObject obj = new JSONObject();
		if(StringUtils.isNotBlank(anntId)){
			SysAnnouncement sysAnnouncement = sysAnnouncementService.getById(anntId);
			if(sysAnnouncement==null) {
				result.error500("No corresponding entity found");
			}else {
				if(sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
					obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
					obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
					obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
					webSocket.sendMessage(obj.toJSONString());
				}else {
					// 2.Insert user notification reading mark table record
					String userId = sysAnnouncement.getUserIds();
					if(oConvertUtils.isNotEmpty(userId)){
						String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
						obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
						obj.put(WebsocketConst.MSG_ID, sysAnnouncement.getId());
						obj.put(WebsocketConst.MSG_TXT, sysAnnouncement.getTitile());
						webSocket.sendMessage(userIds, obj.toJSONString());
					}
				}
			}
		}else{
			obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
			obj.put(WebsocketConst.MSG_TXT, "Batch settings read");
			webSocket.sendMessage(obj.toJSONString());
		}
		return result;
	}

	/**
	 * Notice view details page（use于third partyAPP）
	 * @param modelAndView
	 * @param id
	 * @return
	 */
    @GetMapping("/show/{id}")
    public ModelAndView showContent(ModelAndView modelAndView, @PathVariable("id") String id, HttpServletRequest request) {
        SysAnnouncement announcement = sysAnnouncementService.getById(id);
        if (announcement != null) {
            boolean tokenOk = false;
            try {
                // verifyTokeneffectiveness
                tokenOk = TokenUtils.verifyToken(request, sysBaseApi, redisUtil);
            } catch (Exception ignored) {
            }
            // Determine whether it is deliveredToken，andTokenefficient，If it is uploaded, there will be no viewing restrictions.，直接return
            // ifTokeninvalid，Just make viewing restrictions：Can only view published
            if (tokenOk || ANNOUNCEMENT_SEND_STATUS_1.equals(announcement.getSendStatus())) {
                modelAndView.addObject("data", announcement);
                modelAndView.setViewName("announcement/showContent");
                return modelAndView;
            }
        }
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

	/**
	 * 【vue3use】 消息列表Query
	 * @param fromUser
	 * @param busType
	 * @param starFlag
	 * @param msgCategory
	 * @param beginDate
	 * @param endDate
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/vue3List", method = RequestMethod.GET)
	public Result<List<SysAnnouncement>> vue3List(@RequestParam(name="fromUser", required = false) String fromUser,
												  @RequestParam(name="busType", required = false) String busType,
												  @RequestParam(name="starFlag", required = false) String starFlag,
												  @RequestParam(name="msgCategory", required = false) String msgCategory,
                                                  @RequestParam(name="rangeDateKey", required = false) String rangeDateKey,
                                                  @RequestParam(name="beginDate", required = false) String beginDate, 
												  @RequestParam(name="endDate", required = false) String endDate,
												  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo, 
												  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                  @RequestParam(name= "noticeType", required = false) String noticeType) {
		long calStartTime = System.currentTimeMillis(); // Recording start time
		
		// 1、获取日期Query条件，start time and end time
		Date beginTime = null, endTime = null;
		if (RangeDateEnum.ZDY.getKey().equals(rangeDateKey)) {
			// 自定义日期范围Query
			if (oConvertUtils.isNotEmpty(beginDate)) {
				beginTime = DateUtils.parseDatetime(beginDate);
			}
			if (oConvertUtils.isNotEmpty(endDate)) {
				endTime = DateUtils.parseDatetime(endDate);
			}
		} else {
			// 日期段落Query
			Date[] arr = RangeDateEnum.getRangeArray(rangeDateKey);
			if (arr != null) {
				beginTime = arr[0];
				endTime = arr[1];	
			}
		}
		
		// 2、根据条件Queryuse户的notify消息
		List<SysAnnouncement> ls = this.sysAnnouncementService.querySysMessageList(pageSize, pageNo, fromUser, starFlag,busType, msgCategory, beginTime, endTime, noticeType);

		// 3、Set the message of the current page as read
		if (!CollectionUtils.isEmpty(ls)) {
			// Set read
			String readed = "1";
			List<String> annoceIdList = ls.stream().filter(item -> !readed.equals(item.getReadFlag())).map(item -> item.getId()).collect(Collectors.toList());
			if (!CollectionUtils.isEmpty(annoceIdList)) {
				cachedThreadPool.execute(() -> {
					sysAnnouncementService.updateReaded(annoceIdList);
				});
			}
		}
		
		JSONObject obj = new JSONObject();
		obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		webSocket.sendMessage(sysUser.getId(), obj.toJSONString());

		// 4、Performance statistics take time
		long calEndTime = System.currentTimeMillis(); // Record end time
		long duration = calEndTime - calStartTime; // Calculation time
		log.info("time consuming：" + duration + " millisecond");
		
		return Result.ok(ls);
	}


    /**
     * 根据use户idGet the latest message sending time(creation time)
     * @param userId
     * @return
     */
	@GetMapping("/getLastAnnountTime")
	public Result<Page<SysAnnouncementSend>> getLastAnnountTime(@RequestParam(name = "userId") String userId,@RequestParam(name="noticeType",required = false) String noticeType){
		Result<Page<SysAnnouncementSend>> result = new Result<>();
		//----------------------------------------------------------------------------------------
		// step.1 This interface is too slow，可以采usecache一Hour方案
		String keyString = String.format(CommonConstant.CACHE_KEY_USER_LAST_ANNOUNT_TIME_1HOUR + "_" + noticeType, userId);
		if (redisTemplate.hasKey(keyString)) {
			log.info("[SysAnnouncementSend Redis] passRediscacheQueryuse户最后一Second-rate收到系统notify时间，userId={}", userId);
			Page<SysAnnouncementSend> pageList = (Page<SysAnnouncementSend>) redisTemplate.opsForValue().get(keyString);
			result.setSuccess(true);
			result.setResult(pageList);
			return result;
		}
		//----------------------------------------------------------------------------------------

		Page<SysAnnouncementSend> page = new Page<>(1,1);
        LambdaQueryWrapper<SysAnnouncementSend> query = new LambdaQueryWrapper<>();
        query.eq(SysAnnouncementSend::getUserId,userId);
        //只Query上个月和本月，的notify的数据
		query.ne(SysAnnouncementSend::getCreateTime, DateRangeUtils.getLastMonthStartDay());
        query.select(SysAnnouncementSend::getCreateTime); // 提高Query效率
        query.orderByDesc(SysAnnouncementSend::getCreateTime);
        Page<SysAnnouncementSend> pageList = sysAnnouncementSendService.page(page, query);

		//----------------------------------------------------------------------------------------
		if (pageList != null && pageList.getSize() > 0) {
			// step.3 reserve1Hourrediscache
			redisTemplate.opsForValue().set(keyString, pageList, 3600, TimeUnit.SECONDS);
		}
		//----------------------------------------------------------------------------------------

		result.setSuccess(true);
		result.setResult(pageList);
        return result;
    }

	/**
	 * 清除当前use户所有未读消息
	 * @return
	 */
	@PostMapping("/clearAllUnReadMessage")
    public Result<String> clearAllUnReadMessage(){
		sysAnnouncementService.clearAllUnReadMessage();
		return Result.ok("Clear unread messages successfully");
	}

	/**
	 * Add to访问Second-rate数
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/addVisitsNumber", method = RequestMethod.GET)
	public Result<?> addVisitsNumber(@RequestParam(name="id",required=true) String id) {
		int count = oConvertUtils.getInt(redisUtil.get(ANNO_CACHE_KEY+id),0) + 1;
		redisUtil.set(ANNO_CACHE_KEY+id, count);

		if (count % 5 == 0) {
			cachedThreadPool.execute(() -> {
				sysAnnouncementService.updateVisitsNum(id, count);
			});
			// Reset visits
			redisUtil.del(ANNO_CACHE_KEY+id);
		}
		return Result.ok("Number of visits to announcement messages+1Second-rate");
	}

	/**
	 * Download files in batches
	 * @param id
	 * @param request
	 * @param response
	 */
	@GetMapping("/downLoadFiles")
	public void downLoadFiles(@RequestParam(name="id") String id,
							  HttpServletRequest request,
							  HttpServletResponse response){
		sysAnnouncementService.downLoadFiles(id,request,response);
	}
	/**
	 * Determine friendly error prompts based on exception information
	 */
	private String determineErrorMessage(Exception e) {
		String errorMsg = e.getMessage();
		if (isSpecialCharacterError(errorMsg)) {
			return SPECIAL_CHAR_ERROR;
		} else if (isContentTooLongError(errorMsg)) {
			return CONTENT_TOO_LONG_ERROR;
		} else {
			return DEFAULT_ERROR;
		}
	}
	/**
	 * Determine whether it is a special character error
	 */
	private boolean isSpecialCharacterError(String errorMsg) {
		return errorMsg != null
				&& errorMsg.contains("Incorrect string value")
				&& errorMsg.contains("column 'msg_content'");
	}

	/**
	 * Determine whether the content is too long error
	 */
	private boolean isContentTooLongError(String errorMsg) {
		return errorMsg != null
				&& errorMsg.contains("Data too long for column 'msg_content'");
	}
}
