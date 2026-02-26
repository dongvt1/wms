package org.jeecg.modules.demo.test.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.demo.test.entity.JeecgDemo;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @Description: jeecg testdemo
 * @Author: jeecg-boot
 * @Date:  2018-12-29
 * @Version: V1.0
 */
public interface IJeecgDemoService extends JeecgService<JeecgDemo> {

    /**
     * test事务
     */
	public void testTran();

    /**
     * passidpastdemodata，Read first cache，exist读data库
     * @param id data库id
     * @return demoobject
     */
	public JeecgDemo getByIdCacheable(String id);
    /**
     * passidpastdemodata，Read first cache，exist读data库
     * @param id data库id
     * @return demoobject
     */
	public JeecgDemo getByIdCacheableTTL(String id);
	
	/**
	 * 查询列表data existservice中获取data权限sqlinformation
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	IPage<JeecgDemo> queryListWithPermission(int pageSize,int pageNo);

	/**
	 * Get export fields based on user permissions
	 * @return
	 */
	String getExportFields();

	/**
	 * Get the creator
	 * @return
	 */
	List<String> getCreateByList();
}
