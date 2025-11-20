package org.jeecg.modules.quartz.service;

import java.util.List;

import org.jeecg.modules.quartz.entity.QuartzJob;
import org.quartz.SchedulerException;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Online management of scheduled tasks
 * @Author: jeecg-boot
 * @Date: 2019-04-28
 * @Version: V1.1
 */
public interface IQuartzJobService extends IService<QuartzJob> {

    /**
     * Find scheduled tasks by class name
     * @param jobClassName Class name
     * @return List<QuartzJob>
     */
	List<QuartzJob> findByJobClassName(String jobClassName);

    /**
     * Save scheduled tasks
     * @param quartzJob
     * @return boolean
     */
	boolean saveAndScheduleJob(QuartzJob quartzJob);

    /**
     * Edit scheduled tasks
     * @param quartzJob
     * @return boolean
     * @throws SchedulerException
     */
	boolean editAndScheduleJob(QuartzJob quartzJob) throws SchedulerException;

    /**
     * Delete scheduled tasks
     * @param quartzJob
     * @return boolean
     */
	boolean deleteAndStopJob(QuartzJob quartzJob);

    /**
     * Resume scheduled tasks
     * @param quartzJob
     * @return
     */
	boolean resumeJob(QuartzJob quartzJob);

	/**
	 * Execute scheduled tasks
	 * @param quartzJob
     * @throws Exception
	 */
	void execute(QuartzJob quartzJob) throws Exception;

	/**
	 * Pause task
	 * @param quartzJob
	 * @throws SchedulerException
	 */
	void pause(QuartzJob quartzJob);
}
