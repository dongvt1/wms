package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictModelMany;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.model.DuplicateCheckVo;
import org.jeecg.modules.system.model.TreeSelectModel;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Dictionary Mapper interface
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
public interface SysDictMapper extends BaseMapper<SysDict> {
	
	/**
	 * Repeat checkSQL
     * @param duplicateCheckVo
	 * @return
	 */
	@Deprecated
	public Long duplicateCheckCountSql(DuplicateCheckVo duplicateCheckVo);

    /**
     * Repeat check sqlstatement
     * @param duplicateCheckVo
     * @return
     */
	@Deprecated
	public Long duplicateCheckCountSqlNoDataId(DuplicateCheckVo duplicateCheckVo);

    /**
     * via dictionarycodeGet dictionary data
     * @param code dictionarycode
     * @return  List<DictModel>
     */
	public List<DictModel> queryDictItemsByCode(@Param("code") String code);

	/**
	 * Query有效的数据dictionary项
	 * @param code
	 * @return
	 */
	List<DictModel> queryEnableDictItemsByCode(@Param("code") String code);


	/**
	 * 通过多个dictionarycodeGet dictionary data
	 *
	 * @param dictCodeList
	 * @return
	 */
	public List<DictModelMany> queryDictItemsByCodeList(@Param("dictCodeList") List<String> dictCodeList);

    /**
     * via dictionarycodeGet dictionary data
     * @param code
     * @param key
     * @return
     */
	public String queryDictTextByKey(@Param("code") String code,@Param("key") String key);

	/**
	 * 可通过多个dictionarycodeQuery translated text
	 * @param dictCodeList 多个dictionarycode
	 * @param keys Data list
	 * @return
	 */
	List<DictModelMany> queryManyDictByKeys(@Param("dictCodeList") List<String> dictCodeList, @Param("keys") List<String> keys);

	/**
	 * Query系统所有dictionary项
	 * @return
	 */
	public List<DictModelMany> queryAllDictItems(List<Integer> tenantIdList);
	
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
	
	/**
	  * According to table name、Show field name、Store field name query tree
	 * @param table
	 * @param text
	 * @param code
	 * @param pid
	 * @param hasChildField
     * @param query
     * @param pidField
	 * @return
	 */
	@Deprecated
	List<TreeSelectModel> queryTreeList(@Param("query") Map<String, String> query, @Param("table") String table, @Param("text") String text, @Param("code") String code,
										@Param("pidField") String pidField, @Param("pid") String pid, @Param("hasChildField") String hasChildField,
										@Param("converIsLeafVal") int converIsLeafVal);

	/**
	 * delete
	 * @param id
	 */
	@Select("delete from sys_dict where id = #{id}")
	public void deleteOneById(@Param("id") String id);

	/**
	 * Query被逻辑delete的数据
	 * @return
	 */
	@Select("select * from sys_dict where del_flag = 1")
	public List<SysDict> queryDeleteList();

	/**
	 * Modify status value
	 * @param delFlag
	 * @param id
	 */
	@Update("update sys_dict set del_flag = #{flag,jdbcType=INTEGER} where id = #{id,jdbcType=VARCHAR}")
	public void updateDictDelFlag(@Param("flag") int delFlag, @Param("id") String id);


	/**
	 * PaginationQueryDictionary数据
	 * @param page
	 * @param query
	 * @return
	 */
	@Deprecated
	public Page<DictModel> queryDictTablePageList(Page page, @Param("query") DictQuery query);


	/**
	 * Query Dictionary数据 支持Querycondition Pagination
	 * @param page
	 * @param table
	 * @param text
	 * @param code
	 * @param filterSql
	 * @return
	 */
	@Deprecated
	IPage<DictModel> queryPageTableDictWithFilter(Page<DictModel> page, @Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("filterSql") String filterSql);

	/**
	 * Query Dictionary数据 支持Querycondition Query所有
	 * @param table
	 * @param text
	 * @param code
	 * @param filterSql
	 * @return
	 */
	@Deprecated
	List<DictModel> queryTableDictWithFilter(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("filterSql") String filterSql);

	/**
	 * QueryDictionary的数据
	 * @param table table name
	 * @param text   Show field name
	 * @param code   Store field name
	 * @param filterSql conditionsql
	 * @param codeValues Store field value 作为Queryconditionin
	 * @return
	 */
	@Deprecated
	List<DictModel> queryTableDictByKeysAndFilterSql(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("filterSql") String filterSql,  
													 @Param("codeValues") List<String> codeValues);

	/**
	 * According to applicationid获取dictionary列表和详情
	 * @param lowAppId
	 * @param tenantId
	 * @return
	 */
	@InterceptorIgnore(tenantLine = "true")
    List<SysDict> getDictListByLowAppId(@Param("lowAppId") String lowAppId, @Param("tenantId") Integer tenantId);

	/**
	 * Query被逻辑delete的数据（According to tenantid）
	 * @return
	 */
	@Select("select * from sys_dict where del_flag = 1 and tenant_id = #{tenantId}")
	List<SysDict> queryDeleteListBtTenantId(@Param("tenantId") Integer tenantId);

	/**
	 * 还原被逻辑delete的数据（according toid）
	 * @param ids
	 * @return
	 */
	int revertLogicDeleted(@Param("ids") List<String> ids);

	/**
	 *  彻底delete的数据（according toids）
	 * @param ids
	 * @return
	 */
    int removeLogicDeleted(@Param("ids")List<String> ids);
}
