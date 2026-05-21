package org.jeecg.modules.demo.test.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.demo.test.entity.JeecgDemo;

import java.util.List;

/**
 * @Description: Dynamic data source testing
 * @Author: zyf
 * @Date:2020-04-21
 */
public interface IJeecgDynamicDataService extends JeecgService<JeecgDemo> {

	/**
	 * test fromheaderGet data source
	 * @return
	 */
	public List<JeecgDemo> selectSpelByHeader();

	/**
	 * usespelGet from parameters
	 * @param dsName
	 * @return
	 */
	public  List<JeecgDemo> selectSpelByKey(String dsName);

}
