package org.jeecg.modules.system.mapper;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysDataLog;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: System data logMapperinterface
 * @author: jeecg-boot
 */
public interface SysDataLogMapper extends BaseMapper<SysDataLog>{
	/**
	 * Through table name and dataIdGet the largest version
	 * @param tableName
	 * @param dataId
	 * @return
	 */
	public String queryMaxDataVer(@Param("tableName") String tableName,@Param("dataId") String dataId);
	
}
