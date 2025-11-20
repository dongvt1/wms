package org.jeecg.modules.system.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysLog;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * System log table Mapper interface
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
public interface SysLogMapper extends BaseMapper<SysLog> {

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
	 * @param dayStart start time
     * @param dayEnd end time
	 * @return Long
	 */
	Long findTodayVisitCount(@Param("dayStart") Date dayStart, @Param("dayEnd") Date dayEnd);

	/**
	 * Get system access today IPnumber
	 * @param dayStart start time
     * @param dayEnd end time
	 * @return Long
	 */
	Long findTodayIp(@Param("dayStart") Date dayStart, @Param("dayEnd") Date dayEnd);
	//update-end--Author:zhangweijian  Date:20190428 for：Incoming start time，end time parameter
	
	/**
	 *   front page：根据时间统计访问number量/ipnumber量
	 * @param dayStart
	 * @param dayEnd
     * @param dbType
	 * @return
	 */
	List<Map<String,Object>> findVisitCount(@Param("dayStart") Date dayStart, @Param("dayEnd") Date dayEnd, @Param("dbType") String dbType);
}
