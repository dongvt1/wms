package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDataLog;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: Data logserviceinterface
 * @author: jeecg-boot
 */
public interface ISysDataLogService extends IService<SysDataLog> {
	
	/**
	 * 添加Data log
	 * @param tableName
	 * @param dataId
	 * @param dataContent
	 */
	public void addDataLog(String tableName, String dataId, String dataContent);

}
