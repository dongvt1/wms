package org.jeecg.modules.system.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jeecg.modules.system.entity.SysLog;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * System log table Service category
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
public interface ISysLogService extends IService<SysLog> {

	/**
	 * Clear all log records
	 */
	public void removeAll();
	
	/**
	 * Get the total number of visits to the system
	 *
	 * @return Long
	 */
	Long findTotalVisitCount();

	//update-begin--Author:zhangweijian  Date:20190428 for：Incoming start time，end time parameter
	/**
	 * Get the number of visits to the system today
     * @param dayStart
	 * @param dayEnd
	 * @return Long
	 */
	Long findTodayVisitCount(Date dayStart, Date dayEnd);

	/**
	 * Get system access today IPnumber
	 * @param dayStart start time
     * @param dayEnd end time
	 * @return Long
	 */
	Long findTodayIp(Date dayStart, Date dayEnd);
	//update-end--Author:zhangweijian  Date:20190428 for：Incoming start time，end time parameter
	
	/**
	 *   front page：根据时间统计访问number量/ipnumber量
	 * @param dayStart
	 * @param dayEnd
	 * @return
	 */
	List<Map<String,Object>> findVisitCount(Date dayStart, Date dayEnd);
}
