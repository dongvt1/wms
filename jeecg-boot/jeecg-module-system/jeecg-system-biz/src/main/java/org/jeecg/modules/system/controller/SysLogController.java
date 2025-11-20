package org.jeecg.modules.system.controller;


import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysLog;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.service.ISysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>
 * System log table front controller
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
@RestController
@RequestMapping("/sys/log")
@Slf4j
public class SysLogController extends JeecgController<SysLog, ISysLogService> {
	
	@Autowired
	private ISysLogService sysLogService;

    /**
     * clear all
     */
	private static final String ALL_ClEAR = "allclear";

	/**
	 * @Function：Query log records
	 * @param syslog
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	//@RequiresPermissions("system:log:list")
	public Result<IPage<SysLog>> queryPageList(SysLog syslog,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req) {
		Result<IPage<SysLog>> result = new Result<IPage<SysLog>>();
		QueryWrapper<SysLog> queryWrapper = QueryGenerator.initQueryWrapper(syslog, req.getParameterMap());
		Page<SysLog> page = new Page<SysLog>(pageNo, pageSize);
		//Log keywords
		String keyWord = req.getParameter("keyWord");
		if(oConvertUtils.isNotEmpty(keyWord)) {
			queryWrapper.like("log_content",keyWord);
		}
		//TODO Filter logic processing
		//TODO begin、endlogical processing
		//TODO 一个强大的Function，The front end passes a field string，The background only returns the fields corresponding to these strings.
		//creation time/Creator's assignment
		IPage<SysLog> pageList = sysLogService.page(page, queryWrapper);
		log.info("Query the current page："+pageList.getCurrent());
		log.info("Query the current page数量："+pageList.getSize());
		log.info("Number of query results："+pageList.getRecords().size());
		log.info("Total data："+pageList.getTotal());
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	 * @Function：Delete a single log record
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	//@RequiresPermissions("system:log:delete")
	public Result<SysLog> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysLog> result = new Result<SysLog>();
		SysLog sysLog = sysLogService.getById(id);
		if(sysLog==null) {
			result.error500("No corresponding entity found");
		}else {
			boolean ok = sysLogService.removeById(id);
			if(ok) {
				result.success("Delete successfully!");
			}
		}
		return result;
	}
	
	/**
	 * @Function：batch，Clear all log records
	 * @param ids
	 * @return
	 */
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	//@RequiresPermissions("system:log:deleteBatch")
	public Result<SysRole> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysRole> result = new Result<SysRole>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("Parameter not recognized！");
		}else {
			if(ALL_ClEAR.equals(ids)) {
				this.sysLogService.removeAll();
				result.success("Clear successfully!");
			}
			this.sysLogService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("Delete successfully!");
		}
		return result;
	}

	/**
	 * Exportexcel
	 * for [QQYUN-13431]【jeecg】日志管理中添加大数据ExportFunction
	 * @param request
	 * @param syslog
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, SysLog syslog) {
		return super.exportXlsForBigData(request, syslog, SysLog.class, "syslog", 10000);
	}
	
	
}
