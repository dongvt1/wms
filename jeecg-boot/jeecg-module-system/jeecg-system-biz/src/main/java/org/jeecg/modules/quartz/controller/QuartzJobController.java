package org.jeecg.modules.quartz.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.quartz.entity.QuartzJob;
import org.jeecg.modules.quartz.service.IQuartzJobService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: Online management of scheduled tasks
 * @Author: jeecg-boot
 * @Date: 2019-01-02
 * @Version:V1.0
 */
@RestController
@RequestMapping("/sys/quartzJob")
@Slf4j
@Tag(name = "Scheduled task interface")
public class QuartzJobController {
	@Autowired
	private IQuartzJobService quartzJobService;
	@Autowired
	private Scheduler scheduler;

	/**
	 * Paginated list query
	 * 
	 * @param quartzJob
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<?> queryPageList(QuartzJob quartzJob, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
		QueryWrapper<QuartzJob> queryWrapper = QueryGenerator.initQueryWrapper(quartzJob, req.getParameterMap());
		Page<QuartzJob> page = new Page<QuartzJob>(pageNo, pageSize);
		IPage<QuartzJob> pageList = quartzJobService.page(page, queryWrapper);
        return Result.ok(pageList);

	}

	/**
	 * Add a scheduled task
	 * 
	 * @param quartzJob
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<?> add(@RequestBody QuartzJob quartzJob) {
		quartzJobService.saveAndScheduleJob(quartzJob);
		return Result.ok("Scheduled task created successfully");
	}

	/**
	 * Update scheduled tasks
	 * 
	 * @param quartzJob
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:edit")
	@RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
	public Result<?> eidt(@RequestBody QuartzJob quartzJob) {
		try {
			quartzJobService.editAndScheduleJob(quartzJob);
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
			return Result.error("Update scheduled tasks失败!");
		}
	    return Result.ok("Update scheduled tasks成功!");
	}

	/**
	 * passiddelete
	 * 
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
		QuartzJob quartzJob = quartzJobService.getById(id);
		if (quartzJob == null) {
			return Result.error("No corresponding entity found");
		}
		quartzJobService.deleteAndStopJob(quartzJob);
        return Result.ok("delete成功!");

	}

	/**
	 * 批量delete
	 * 
	 * @param ids
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		if (ids == null || "".equals(ids.trim())) {
			return Result.error("Parameter not recognized！");
		}
		for (String id : Arrays.asList(ids.split(SymbolConstant.COMMA))) {
			QuartzJob job = quartzJobService.getById(id);
			quartzJobService.deleteAndStopJob(job);
		}
        return Result.ok("deletescheduled tasks成功!");
	}

	/**
	 * Pause scheduled tasks
	 * 
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:pause")
	@GetMapping(value = "/pause")
	@Operation(summary = "Stop scheduled tasks")
	public Result<Object> pauseJob(@RequestParam(name = "id") String id) {
		QuartzJob job = quartzJobService.getById(id);
		if (job == null) {
			return Result.error("The scheduled task does not exist！");
		}
		quartzJobService.pause(job);
		return Result.ok("Stop scheduled tasks成功");
	}

	/**
	 * Start a scheduled task
	 * 
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:resume")
	@GetMapping(value = "/resume")
	@Operation(summary = "Start a scheduled task")
	public Result<Object> resumeJob(@RequestParam(name = "id") String id) {
		QuartzJob job = quartzJobService.getById(id);
		if (job == null) {
			return Result.error("The scheduled task does not exist！");
		}
		quartzJobService.resumeJob(job);
		//scheduler.resumeJob(JobKey.jobKey(job.getJobClassName().trim()));
		return Result.ok("Start a scheduled task成功");
	}

	/**
	 * passidQuery
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/queryById", method = RequestMethod.GET)
	public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
		QuartzJob quartzJob = quartzJobService.getById(id);
        return Result.ok(quartzJob);
	}

	/**
	 * Exportexcel
	 * 
	 * @param request
	 * @param quartzJob
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, QuartzJob quartzJob) {
		// Step.1 组装Query条件
		QueryWrapper<QuartzJob> queryWrapper = QueryGenerator.initQueryWrapper(quartzJob, request.getParameterMap());
		// Filter selected data
		String selections = request.getParameter("selections");
		if (oConvertUtils.isNotEmpty(selections)) {
			List<String> selectionList = Arrays.asList(selections.split(","));
			queryWrapper.in("id",selectionList);
		}
		// Step.2 AutoPoi ExportExcel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<QuartzJob> pageList = quartzJobService.list(queryWrapper);
		// Export文件名称
		mv.addObject(NormalExcelConstants.FILE_NAME, "Scheduled task list");
		mv.addObject(NormalExcelConstants.CLASS, QuartzJob.class);
        //Get the currently logged in user
        //update-begin---author:wangshuai ---date:20211227  for：[JTC-116]Export人写死了------------
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Scheduled task list数据", "Export人:"+user.getRealname(), "Export信息"));
        //update-end---author:wangshuai ---date:20211227  for：[JTC-116]Export人写死了------------
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
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		// error message
		List<String> errorMessage = new ArrayList<>();
		int successLines = 0, errorLines = 0;
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Get the uploaded file object
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				List<QuartzJob> listQuartzJobs = ExcelImportUtil.importExcel(file.getInputStream(), QuartzJob.class, params);
				//add-begin-author:taoyan date:20210909 for:Import scheduled tasks，and will not be started and scheduled，Need to manually click to start，will be added to the scheduled task #2986
				for(QuartzJob job: listQuartzJobs){
					job.setStatus(CommonConstant.STATUS_DISABLE);
				}
				List<String> list = ImportExcelUtil.importDateSave(listQuartzJobs, IQuartzJobService.class, errorMessage,CommonConstant.SQL_INDEX_UNIQ_JOB_CLASS_NAME);
				//add-end-author:taoyan date:20210909 for:Import scheduled tasks，and will not be started and scheduled，Need to manually click to start，will be added to the scheduled task #2986
				errorLines+=list.size();
				successLines+=(listQuartzJobs.size()-errorLines);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Result.error("File import failed！");
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ImportExcelUtil.imporReturnRes(errorLines,successLines,errorMessage);
	}

	/**
	 * Execute immediately
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:execute")
	@GetMapping("/execute")
	public Result<?> execute(@RequestParam(name = "id", required = true) String id) {
		QuartzJob quartzJob = quartzJobService.getById(id);
		if (quartzJob == null) {
			return Result.error("No corresponding entity found");
		}
		try {
			quartzJobService.execute(quartzJob);
		} catch (Exception e) {
			//e.printStackTrace();
			log.info("scheduled tasks Execute immediately失败>>"+e.getMessage());
			return Result.error("Execution failed!");
		}
		return Result.ok("Executed successfully!");
	}

}
