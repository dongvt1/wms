package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.model.TreeSelectModel;

import java.util.List;
import java.util.Map;

/**
 * @Description: Classification dictionary
 * @Author: jeecg-boot
 * @Date:   2019-05-29
 * @Version: V1.0
 */
public interface ISysCategoryService extends IService<SysCategory> {

	/**root node parentIDvalue*/
	public static final String ROOT_PID_VALUE = "0";

    /**
     * There are child nodes
     */
    public static final String HAS_CHILD = "1";

    /**
     * 添加Classification dictionary
     * @param sysCategory
     */
	void addSysCategory(SysCategory sysCategory);

    /**
     * 修改Classification dictionary
     * @param sysCategory
     */
	void updateSysCategory(SysCategory sysCategory);
	
	/**
     * according to父级编码加载Classification dictionary的数据
	 * @param pcode
	 * @return
     * @throws JeecgBootException
	 */
	public List<TreeSelectModel> queryListByCode(String pcode) throws JeecgBootException;
	
	/**
	  * according topidQuery the collection of child nodes
	 * @param pid
	 * @return
	 */
	public List<TreeSelectModel> queryListByPid(String pid);

	/**
	 * according topidQuery the collection of child nodes,Support query conditions
	 * @param pid
	 * @param condition
	 * @return
	 */
	public List<TreeSelectModel> queryListByPid(String pid, Map<String,String> condition);

	/**
	 * according tocodeQueryid
	 * @param code
	 * @return
	 */
	public String queryIdByCode(String code);

	/**
	 * When deleting a node, delete the child nodes and modify the parent node at the same time.
	 * @param ids
	 */
	void deleteSysCategory(String ids);

	/**
	 * Classification dictionary控件数据回显[form page]
	 *
	 * @param ids
	 * @return
	 */
	List<String> loadDictItem(String ids);

	/**
	 * Classification dictionary控件数据回显[form page]
	 *
	 * @param ids
	 * @param delNotExist Whether to remove non-existing items，set tofalseIf akeydoes not exist in database，then return directlykeyitself
	 * @return
	 */
	List<String> loadDictItem(String ids, boolean delNotExist);

	/**
	 * 【Import only for use】Classification dictionary控件反向翻译
	 *
	 * @param names
	 * @param delNotExist Whether to remove non-existing items，set tofalseIf akeydoes not exist in database，then return directlykeyitself
	 * @return
	 */
	List<String> loadDictItemByNames(String names, boolean delNotExist);

}
