package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.model.DuplicateCheckVo;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.vo.lowapp.SysDictVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Dictionary Service category
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
public interface ISysDictService extends IService<SysDict> {

	/**
	 *  Check whether the data is available，There is no duplicate data
	 *  
	 * @param duplicateCheckVo
	 * @return
	 */
	@Deprecated
	public boolean duplicateCheckData(DuplicateCheckVo duplicateCheckVo);

    /**
     * via dictionarycodeGet dictionary data
     * @param code
     * @return
     */
    public List<DictModel> queryDictItemsByCode(String code);

	/**
	 * Query valid data dictionary entries
	 * @param code
	 * @return
	 */
	List<DictModel> queryEnableDictItemsByCode(String code);

	/**
	 * through multiple dictionariescodeGet dictionary data
	 *
	 * @param dictCodeList
	 * @return key = dictionarycode，value=对应ofdictionary选项
	 */
	Map<String, List<DictModel>> queryDictItemsByCodeList(List<String> dictCodeList);

    /**
     * 登录加载系统dictionary
     * @return
     */
    public Map<String,List<DictModel>> queryAllDictItems();

    /**
     * Check specified by querytableof text code 获取dictionary
     * @param tableFilterSql
     * @param text
     * @param code
     * @return
     */
    @Deprecated
    List<DictModel> queryTableDictItemsByCode(String tableFilterSql, String text, String code);

    /**
     * Specified by querytableof text code 获取dictionary（Specify query conditions）
     * @param table
     * @param text
     * @param code
     * @param filterSql
     * @return
     */
    @Deprecated
	public List<DictModel> queryTableDictItemsByCodeAndFilter(String table, String text, String code, String filterSql);

    /**
     * via dictionarycode及dictionary项ofvalue获取dictionary文本
     * @param code
     * @param key
     * @return
     */
    public String queryDictTextByKey(String code, String key);

	/**
	 * 可through multiple dictionariescodeQuery translated text
	 * @param dictCodeList 多个dictionarycode
	 * @param keys Data list
	 * @return
	 */
	Map<String, List<DictModel>> queryManyDictByKeys(List<String> dictCodeList, List<String> keys);

    /**
     * Specified by querytableof text code key 获取dictionary值
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    @Deprecated
	String queryTableDictTextByKey(String table, String text, String code, String key);

	//update-begin---author:chenrui ---date:20231221  for：[issues/#5643]解决分布式下表dictionary跨库无法查询问题------------
	/**
	 * Specified by querytableof text code key 获取dictionary值，Can be queried in batches
	 *
	 * @param table
	 * @param text
	 * @param code
	 * @param keys
	 * @param dataSource data source
	 * @return
	 */
	List<DictModel> queryTableDictTextByKeys(String table, String text, String code, List<String> keys, String dataSource);
	//update-end---author:chenrui ---date:20231221  for：[issues/#5643]解决分布式下表dictionary跨库无法查询问题------------

    /**
     * Specified by querytableof text code key 获取dictionary值，Includevalue
     * @param table table name
     * @param text
     * @param code
     * @param keys
     * @return
     */
	@Deprecated
	List<String> queryTableDictByKeys(String table, String text, String code, String keys);

    /**
     * Specified by querytableof text code key 获取dictionary值，Includevalue
     * @param table
     * @param text
     * @param code
     * @param keys
     * @param delNotExist
     * @return
     */
	@Deprecated
	List<String> queryTableDictByKeys(String table, String text, String code, String keys,boolean delNotExist);

    /**
     * 根据dictionary类型删除关联表中其对应of数据
     *
     * @param sysDict
     * @return
     */
    boolean deleteByDictId(SysDict sysDict);

    /**
     * Add one to many
     * @param sysDict
     * @param sysDictItemList
     * @return Integer
     */
    public Integer saveMain(SysDict sysDict, List<SysDictItem> sysDictItemList);

    /**
	 * Search all departments 作为dictionary信息 id -->value,departName -->text
	 * @return
	 */
	public List<DictModel> queryAllDepartBackDictModel();

	/**
	 * Query all users  作为dictionary信息 username -->value,realname -->text
	 * @return
	 */
	public List<DictModel> queryAllUserBackDictModel();

//	/**
//	 * 通过关键字查询Dictionary
//	 * @param table
//	 * @param text
//	 * @param code
//	 * @param keyword
//	 * @return
//	 */
//	@Deprecated
//	public List<DictModel> queryTableDictItems(String table, String text, String code,String keyword);

	/**
	 * 查询Dictionary数据 Only query before10strip
	 * @param table
	 * @param text
	 * @param code
	 * @param keyword
     * @param condition
     * @param pageSize 每页strip数
	 * @return
	 */
	@Deprecated
	public List<DictModel> queryLittleTableDictItems(String table, String text, String code, String condition, String keyword, int pageNo, int pageSize);

	/**
	 * 查询Dictionary所有数据
	 * @param table
	 * @param text
	 * @param code
	 * @param condition
	 * @param keyword
	 * @return
	 */
	@Deprecated
	public List<DictModel> queryAllTableDictItems(String table, String text, String code, String condition, String keyword);
	/**
	  * 根据table name、Show field name、Store field name query tree
	 * @param table
	 * @param text
	 * @param code
	 * @param pidField
	 * @param pid
	 * @param hasChildField
     * @param query
	 * @return
	 */
	@Deprecated
	List<TreeSelectModel> queryTreeList(Map<String, String> query,String table, String text, String code, String pidField,String pid,String hasChildField,int converIsLeafVal);

	/**
	 * true deletion
	 * @param id
	 */
	public void deleteOneDictPhysically(String id);

	/**
	 * RevisedelFlag
	 * @param delFlag
	 * @param id
	 */
	public void updateDictDelFlag(int delFlag,String id);

	/**
	 * 查询被逻辑删除of数据
	 * @return
	 */
	public List<SysDict> queryDeleteList(String tenantId);

	/**
	 * Page query
	 * @param query
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	@Deprecated
	public List<DictModel> queryDictTablePageList(DictQuery query,int pageSize, int pageNo);

    /**
     * Get dictionary data
     * @param dictCode dictionarycode
     * @param dictCode table name,text field,codeField  | Example：sys_user,realname,id
     * @return
     */
    List<DictModel> getDictItems(String dictCode);

    /**
     * 【JSearchSelectTagDrop-down search component dedicated interface】
     * 大数据量ofDictionary Go asynchronous loading  That is, the front-end input content filters the data
     *
     * @param dictCode dictionarycodeFormat：table,text,code
     * @param keyword
     * @param pageNo
     * @param pageSize 每页strip数
     * @return
     */
    List<DictModel> loadDict(String dictCode, String keyword, Integer pageNo, Integer pageSize);

	/**
	 * According to applicationid获取dictionary列表和详情
	 * @param lowAppId
	 * @return
	 */
	List<SysDictVo> getDictListByLowAppId(String lowAppId);

	/**
	 * 创建dictionary
	 * @param sysDictVo
	 */
	String addDictByLowAppId(SysDictVo sysDictVo);

	/**
	 * 编辑dictionary
	 * @param sysDictVo
	 */
	void editDictByLowAppId(SysDictVo sysDictVo);

	/**
	 * Undo tombstone
	 * @param ids
	 */
	boolean revertLogicDeleted(List<String> ids);

	/**
	 * Completely delete data
	 * @param ids
	 */
	boolean removeLogicDeleted(List<String> ids);
}
