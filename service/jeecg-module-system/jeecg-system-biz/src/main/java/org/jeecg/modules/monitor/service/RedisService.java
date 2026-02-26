package org.jeecg.modules.monitor.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import org.jeecg.modules.monitor.domain.RedisInfo;
import org.jeecg.modules.monitor.exception.RedisConnectException;

/**
 * @Description: redisinformationserviceinterface
 * @author: jeecg-boot
 */
public interface RedisService {

	/**
	 * Get redis 的详细information
	 *
	 * @return List
     * @throws RedisConnectException
	 */
	List<RedisInfo> getRedisInfo() throws RedisConnectException;

	/**
	 * Get redis key quantity
	 *
	 * @return Map
     * @throws RedisConnectException
	 */
	Map<String, Object> getKeysSize() throws RedisConnectException;

	/**
	 * Get redis 内存information
	 *
	 * @return Map
     * @throws RedisConnectException
	 */
	Map<String, Object> getMemoryInfo() throws RedisConnectException;
	/**
	 * Get The report requires aredisinformation
	 * @param type
	 * @return Map
     * @throws RedisConnectException
	 */
	Map<String, JSONArray> getMapForReport(String type) throws RedisConnectException ;

	/**
	 * Get历史性能指标
	 * @return
	 * @author chenrui
	 * @date 2024/5/14 14:57
	 */
	Map<String, List<Map<String, Object>>> getMetricsHistory();
}
