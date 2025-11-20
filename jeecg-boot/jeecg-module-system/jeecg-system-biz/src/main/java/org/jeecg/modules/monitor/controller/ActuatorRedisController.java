package org.jeecg.modules.monitor.controller;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.monitor.domain.RedisInfo;
import org.jeecg.modules.monitor.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: ActuatorRedisController
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/actuator/redis")
public class ActuatorRedisController {

    @Autowired
    private RedisService redisService;

    /**
     * RedisDetails
     * @return
     * @throws Exception
     */
    @GetMapping("/info")
    public Result<?> getRedisInfo() throws Exception {
        List<RedisInfo> infoList = this.redisService.getRedisInfo();
        //log.info(infoList.toString());
        return Result.ok(infoList);
    }

	//update-begin---author:chenrui ---date:20240514  for：[QQYUN-9247]System monitoring function optimization------------
	/**
	 * RedisHistorical performance indicator query(past hour)
	 * @return
	 * @throws Exception
	 * @author chenrui
	 * @date 2024/5/14 14:56
	 */
	@GetMapping(value = "/metrics/history")
	public Result<?> getMetricsHistory() throws Exception {
		Map<String,List<Map<String,Object>>> metricsHistory = this.redisService.getMetricsHistory();
	    return Result.OK(metricsHistory);
	}
	//update-end---author:chenrui ---date:20240514  for：[QQYUN-9247]System monitoring function optimization------------

    @GetMapping("/keysSize")
    public Map<String, Object> getKeysSize() throws Exception {
        return redisService.getKeysSize();
    }

    /**
     * Getredis keyquantity for Report
     * @return
     * @throws Exception
     */
    @GetMapping("/keysSizeForReport")
    public Map<String, JSONArray> getKeysSizeReport() throws Exception {
		return redisService.getMapForReport("1");
    }
    /**
     * Getredis Memory for Report
     *
     * @return
     * @throws Exception
     */
    @GetMapping("/memoryForReport")
    public Map<String, JSONArray> memoryForReport() throws Exception {
		return redisService.getMapForReport("2");
    }
    /**
     * Getredis All information for Report
     * @return
     * @throws Exception
     */
    @GetMapping("/infoForReport")
    public Map<String, JSONArray> infoForReport() throws Exception {
		return redisService.getMapForReport("3");
    }

    @GetMapping("/memoryInfo")
    public Map<String, Object> getMemoryInfo() throws Exception {
        return redisService.getMemoryInfo();
    }
    
  //update-begin--Author:zhangweijian  Date:20190425 for：Getdisk信息
  	/**
  	 * @Function：Getdisk信息
  	 * @param request
  	 * @param response
  	 * @return
  	 */
  	@GetMapping("/queryDiskInfo")
  	public Result<List<Map<String,Object>>> queryDiskInfo(HttpServletRequest request, HttpServletResponse response){
  		Result<List<Map<String,Object>>> res = new Result<>();
  		try {
  			// Current file system class
  	        FileSystemView fsv = FileSystemView.getFileSystemView();
  	        // list allwindows disk
  	        File[] fs = File.listRoots();
  	        log.info("查询disk信息:"+fs.length+"indivual");
  	        List<Map<String,Object>> list = new ArrayList<>();
  	        
  	        for (int i = 0; i < fs.length; i++) {
  	        	if(fs[i].getTotalSpace()==0) {
  	        		continue;
  	        	}
  	        	Map<String,Object> map = new HashMap(5);
  	        	map.put("name", fsv.getSystemDisplayName(fs[i]));
  	        	map.put("max", fs[i].getTotalSpace());
  	        	map.put("rest", fs[i].getFreeSpace());
  	        	map.put("restPPT", (fs[i].getTotalSpace()-fs[i].getFreeSpace())*100/fs[i].getTotalSpace());
  	        	list.add(map);
  	        	log.info(map.toString());
  	        }
  	        res.setResult(list);
  	        res.success("Query successful");
  		} catch (Exception e) {
  			res.error500("Query failed"+e.getMessage());
  		}
  		return res;
  	}
  	//update-end--Author:zhangweijian  Date:20190425 for：Getdisk信息
}
