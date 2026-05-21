package org.jeecg.modules.monitor.service.impl;

import java.util.*;

import jakarta.annotation.Resource;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.monitor.domain.RedisInfo;
import org.jeecg.modules.monitor.exception.RedisConnectException;
import org.jeecg.modules.monitor.service.RedisService;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis Obtain monitoring information
 *
 * @Author MrBird
 */
@Service("redisService")
@Slf4j
public class RedisServiceImpl implements RedisService {

	@Resource
	private RedisConnectionFactory redisConnectionFactory;

    /**
     * redisinformation
     */
    private static final String REDIS_MESSAGE = "3";

	/**
	 * redis性能information记录
	 */
	private static final Map<String,List<Map<String, Object>>> REDIS_METRICS = new HashMap<>(2);

	/**
	 * Redis详细information
	 */
	@Override
	public List<RedisInfo> getRedisInfo() throws RedisConnectException {
		Properties info = redisConnectionFactory.getConnection().info();
		List<RedisInfo> infoList = new ArrayList<>();
		RedisInfo redisInfo = null;
		for (Map.Entry<Object, Object> entry : info.entrySet()) {
			redisInfo = new RedisInfo();
			redisInfo.setKey(oConvertUtils.getString(entry.getKey()));
			redisInfo.setValue(oConvertUtils.getString(entry.getValue()));
			infoList.add(redisInfo);
		}
		return infoList;
	}

	@Override
	public Map<String, Object> getKeysSize() throws RedisConnectException {
		Long dbSize = redisConnectionFactory.getConnection().dbSize();
		Map<String, Object> map = new HashMap(5);
		map.put("create_time", System.currentTimeMillis());
		map.put("dbSize", dbSize);

		log.debug("--getKeysSize--: " + map.toString());
		return map;
	}

	@Override
	public Map<String, Object> getMemoryInfo() throws RedisConnectException {
		Map<String, Object> map = null;
		Properties info = redisConnectionFactory.getConnection().info();
		for (Map.Entry<Object, Object> entry : info.entrySet()) {
			String key = oConvertUtils.getString(entry.getKey());
			if ("used_memory".equals(key)) {
				map = new HashMap(5);
				map.put("used_memory", entry.getValue());
				map.put("create_time", System.currentTimeMillis());
			}
		}
		log.debug("--getMemoryInfo--: " + map.toString());
		return map;
	}

    /**
     * QueryredisinformationforReport
     * @param type 1redis keyquantity 2 Taking up memory 3redisinformation
     * @return
     * @throws RedisConnectException
     */
	@Override
	public Map<String, JSONArray> getMapForReport(String type)  throws RedisConnectException {
		Map<String,JSONArray> mapJson=new HashMap(5);
		JSONArray json = new JSONArray();
		if(REDIS_MESSAGE.equals(type)){
			List<RedisInfo> redisInfo = getRedisInfo();
			for(RedisInfo info:redisInfo){
				Map<String, Object> map= Maps.newHashMap();
				BeanMap beanMap = BeanMap.create(info);
				for (Object key : beanMap.keySet()) {
					map.put(key+"", beanMap.get(key));
				}
				json.add(map);
			}
			mapJson.put("data",json);
			return mapJson;
		}
		int length = 5;
		for(int i = 0; i < length; i++){
			JSONObject jo = new JSONObject();
			Map<String, Object> map;
			if("1".equals(type)){
				map= getKeysSize();
				jo.put("value",map.get("dbSize"));
			}else{
				map = getMemoryInfo();
				Integer usedMemory = Integer.valueOf(map.get("used_memory").toString());
				jo.put("value",usedMemory/1000);
			}
			String createTime = DateUtil.formatTime(DateUtil.date((Long) map.get("create_time")-(4-i)*1000));
			jo.put("name",createTime);
			json.add(jo);
		}
		mapJson.put("data",json);
		return mapJson;
	}

	//update-begin---author:chenrui ---date:20240514  for：[QQYUN-9247]System monitoring function optimization------------
	/**
	 * Get historical performance metrics
	 * @return
	 * @author chenrui
	 * @date 2024/5/14 14:57
	 */
	@Override
	public Map<String, List<Map<String, Object>>> getMetricsHistory() {
		return REDIS_METRICS;
	}

	/**
	 * Recorded for nearly an hourredisMonitoring data <br/>
	 * 60sonce,,record storagekeysizeand memory
	 * @throws RedisConnectException
	 * @author chenrui
	 * @date 2024/5/14 14:09
	 */
	@Scheduled(fixedRate = 60000)
	public void recordCustomMetric() throws RedisConnectException {
		List<Map<String, Object>> list= new ArrayList<>();
		if(REDIS_METRICS.containsKey("dbSize")){
			list = REDIS_METRICS.get("dbSize");
		}else{
			REDIS_METRICS.put("dbSize",list);
		}
		if(list.size()>60){
			list.remove(0);
		}
		list.add(getKeysSize());
		list= new ArrayList<>();
		if(REDIS_METRICS.containsKey("memory")){
			list = REDIS_METRICS.get("memory");
		}else{
			REDIS_METRICS.put("memory",list);
		}
		if(list.size()>60){
			list.remove(0);
		}
		list.add(getMemoryInfo());
	}
	//update-end---author:chenrui ---date:20240514  for：[QQYUN-9247]System monitoring function optimization------------
}
