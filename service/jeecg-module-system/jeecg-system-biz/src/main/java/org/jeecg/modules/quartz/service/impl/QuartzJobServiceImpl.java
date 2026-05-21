package org.jeecg.modules.quartz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.quartz.entity.QuartzJob;
import org.jeecg.modules.quartz.mapper.QuartzJobMapper;
import org.jeecg.modules.quartz.service.IQuartzJobService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Description: Online management of scheduled tasks
 * @Author: jeecg-boot
 * @Date: 2019-04-28
 * @Version: V1.1
 */
@Slf4j
@Service
public class QuartzJobServiceImpl extends ServiceImpl<QuartzJobMapper, QuartzJob> implements IQuartzJobService {
	@Autowired
	private QuartzJobMapper quartzJobMapper;
	@Autowired
	private Scheduler scheduler;

	/**
	 * Immediately executed task grouping
	 */
	private static final String JOB_TEST_GROUP = "test_group";

	@Override
	public List<QuartzJob> findByJobClassName(String jobClassName) {
		return quartzJobMapper.findByJobClassName(jobClassName);
	}

	/**
	 * save&Start a scheduled task
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean saveAndScheduleJob(QuartzJob quartzJob) {
		// DBSetting modification
		quartzJob.setDelFlag(CommonConstant.DEL_FLAG_0);
		boolean success = this.save(quartzJob);
		if (success) {
			if (CommonConstant.STATUS_NORMAL.equals(quartzJob.getStatus())) {
				// Timer added
				this.schedulerAdd(quartzJob.getId(), quartzJob.getJobClassName().trim(), quartzJob.getCronExpression().trim(), quartzJob.getParameter());
			}
		}
		return success;
	}

	/**
	 * Resume scheduled tasks
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean resumeJob(QuartzJob quartzJob) {
		schedulerDelete(quartzJob.getId());
		schedulerAdd(quartzJob.getId(), quartzJob.getJobClassName().trim(), quartzJob.getCronExpression().trim(), quartzJob.getParameter());
		quartzJob.setStatus(CommonConstant.STATUS_NORMAL);
		return this.updateById(quartzJob);
	}

	/**
	 * edit&Start and stop scheduled tasks
	 * @throws SchedulerException 
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean editAndScheduleJob(QuartzJob quartzJob) throws SchedulerException {
		if (CommonConstant.STATUS_NORMAL.equals(quartzJob.getStatus())) {
			schedulerDelete(quartzJob.getId());
			schedulerAdd(quartzJob.getId(), quartzJob.getJobClassName().trim(), quartzJob.getCronExpression().trim(), quartzJob.getParameter());
		}else{
			scheduler.pauseJob(JobKey.jobKey(quartzJob.getId()));
		}
		return this.updateById(quartzJob);
	}

	/**
	 * delete&停止delete定时任务
	 */
	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public boolean deleteAndStopJob(QuartzJob job) {
		schedulerDelete(job.getId());
		boolean ok = this.removeById(job.getId());
		return ok;
	}

	@Override
	public void execute(QuartzJob quartzJob) throws Exception {
		String jobName = quartzJob.getJobClassName().trim();
		Date startDate = new Date();
		String ymd = DateUtils.date2Str(startDate,DateUtils.yyyymmddhhmmss.get());
		String identity =  jobName + ymd;
		//3Execute after seconds Execute only once
		// update-begin--author:sunjianlei ---- date:20210511--- for：Scheduled tasks are executed immediately，Delay3seconds changed to0.1Second-------
		startDate.setTime(startDate.getTime() + 100L);
		// update-end--author:sunjianlei ---- date:20210511--- for：Scheduled tasks are executed immediately，Delay3seconds changed to0.1Second-------
		// define aTrigger
		SimpleTrigger trigger = (SimpleTrigger)TriggerBuilder.newTrigger()
				.withIdentity(identity, JOB_TEST_GROUP)
				.startAt(startDate)
				.build();
		// buildjobinformation
		JobDetail jobDetail = JobBuilder.newJob(getClass(jobName).getClass()).withIdentity(identity).usingJobData("parameter", quartzJob.getParameter()).build();
		// Willtriggerand jobDetail Join this schedule
		scheduler.scheduleJob(jobDetail, trigger);
		// start upscheduler
		scheduler.start();
	}

	@Override
	@Transactional(rollbackFor = JeecgBootException.class)
	public void pause(QuartzJob quartzJob){
		schedulerDelete(quartzJob.getId());
		quartzJob.setStatus(CommonConstant.STATUS_DISABLE);
		this.updateById(quartzJob);
	}

	/**
	 * Add a scheduled task
	 *
	 * @param jobClassName
	 * @param cronExpression
	 * @param parameter
	 */
	private void schedulerAdd(String id, String jobClassName, String cronExpression, String parameter) {
		try {
			// start up调度器
			scheduler.start();

			// buildjobinformation
			JobDetail jobDetail = JobBuilder.newJob(getClass(jobClassName).getClass()).withIdentity(id).usingJobData("parameter", parameter).build();

			// 表达式调度build器(That is, the time of task execution)
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

			// press newcronExpression表达式build一个新的trigger
			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(id).withSchedule(scheduleBuilder).build();

			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			throw new JeecgBootException("Failed to create scheduled task", e);
		} catch (RuntimeException e) {
			throw new JeecgBootException(e.getMessage(), e);
		}catch (Exception e) {
			throw new JeecgBootException("The class name cannot be found in the background：" + jobClassName, e);
		}
	}

	/**
	 * delete定时任务
	 * 
	 * @param id
	 */
	private void schedulerDelete(String id) {
		try {
			scheduler.pauseTrigger(TriggerKey.triggerKey(id));
			scheduler.unscheduleJob(TriggerKey.triggerKey(id));
			scheduler.deleteJob(JobKey.jobKey(id));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JeecgBootException("delete定时任务失败");
		}
	}

	private static Job getClass(String classname) throws Exception {
		Class<?> class1 = Class.forName(classname);
		return (Job) class1.newInstance();
	}

}
