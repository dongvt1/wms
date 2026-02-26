package org.jeecg.modules.demo.test.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @Description: jeecg testdemo
 * @Author: jeecg-boot
 * @Date:  2018-12-29
 * @Version: V1.0
 */
public interface JeecgDemoMapper extends BaseMapper<JeecgDemo> {

    /**
     * Search by namedemoList data
     * @param name Name
     * @return demogather
     */
	public List<JeecgDemo> getDemoByName(@Param("name") String name);
	
	/**
	 * 查询List data Direct data transfer permissionsqlPerform data filtering
	 * @param page
	 * @param permissionSql
	 * @return
	 */
	public IPage<JeecgDemo> queryListWithPermission(Page<JeecgDemo> page,@Param("permissionSql")String permissionSql);

	/**
	 * Get all valid permissions based on prefix
	 * @param permsPrefix
	 * @return
	 */
	public List<String> queryAllAuth(@Param("permsPrefix")String permsPrefix);

	/**
	 * Query user authorized fields
	 * @param userId
	 * @param permsPrefix
	 * @return
	 */
	public List<String> queryUserAuth(@Param("userId")String userId,@Param("permsPrefix")String permsPrefix);


	/**
	 * Get the creator
	 * @return
	 */
	List<String> getCreateByList();

}
