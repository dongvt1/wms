package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.ResourceUtil;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictModelMany;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.dynamic.db.DbTypeUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.mapper.SysDictItemMapper;
import org.jeecg.modules.system.mapper.SysDictMapper;
import org.jeecg.modules.system.model.DuplicateCheckVo;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.security.DictQueryBlackListHandler;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.vo.lowapp.SysDictVo;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Dictionary Service implementation class
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Service
@Slf4j
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {

    @Autowired
    private SysDictMapper sysDictMapper;
    @Autowired
    private SysDictItemMapper sysDictItemMapper;
	@Autowired
	private DictQueryBlackListHandler dictQueryBlackListHandler;

	@Lazy
	@Autowired
	private ISysBaseAPI sysBaseAPI;
	@Lazy
	@Autowired
	private RedisUtil redisUtil;

	@Override
	public boolean duplicateCheckData(DuplicateCheckVo duplicateCheckVo) {
		Long count = null;

		// 1.Targeting adoption ${}The table names and fields written in the way are escaped andcheck
		String table = SqlInjectionUtil.getSqlInjectTableName(duplicateCheckVo.getTableName());
		String fieldName = SqlInjectionUtil.getSqlInjectField(duplicateCheckVo.getFieldName());
		duplicateCheckVo.setTableName(table);
		duplicateCheckVo.setFieldName(fieldName);
		
		// 2.SQLinjectioncheck（Only restrict illegal modification of the database）
		//Association table dictionary（Example：sys_user,realname,id）
		SqlInjectionUtil.filterContentMulti(table, fieldName);

		String checkSql = table + SymbolConstant.COMMA + fieldName + SymbolConstant.COMMA;
		// 【QQYUN-6533】table dictionary whitelistcheck
		sysBaseAPI.dictTableWhiteListCheckByDict(table, fieldName);
		// 3.table dictionary blacklistcheck
		dictQueryBlackListHandler.isPass(checkSql);

		// 4.implementSQL Query if a value exists
		try{
			//update-begin---author:chenrui ---date:20240715  for：[TV360X-49]postgresdate、Year, month, day, hour, minute and second unique verification error report------------
			if(DbTypeUtils.dbTypeIsPostgre(CommonUtils.getDatabaseTypeEnum())){
				duplicateCheckVo.setFieldName("CAST("+duplicateCheckVo.getFieldName()+" as text)");
			}
			//update-end---author:chenrui ---date:20240715  for：[TV360X-49]postgresdate、Year, month, day, hour, minute and second unique verification error report------------
			if (StringUtils.isNotBlank(duplicateCheckVo.getDataId())) {
				// [1].Edit page verification
				count = sysDictMapper.duplicateCheckCountSql(duplicateCheckVo);
			} else {
				// [2].Add page validation
				count = sysDictMapper.duplicateCheckCountSqlNoDataId(duplicateCheckVo);
			}
		}catch(MyBatisSystemException e){
			log.error(e.getMessage(), e);
			String errorCause = "Query exception,Please check the configuration of unique verification！";
			throw new JeecgBootException(errorCause);
		}

		// 4.Return results
		if (count == null || count == 0) {
			// This value is available
			return true;
		} else {
			// The value is not available
			log.info("The value is not available，already exists in the system！");
			return false;
		}
	}


	/**
	 * Specified by querycode Get dictionary
	 * @param code
	 * @return
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,key = "#code", unless = "#result == null ")
	public List<DictModel> queryDictItemsByCode(String code) {
		log.debug("No cachedictCacheCall here when！");
		return sysDictMapper.queryDictItemsByCode(code);
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_ENABLE_DICT_CACHE,key = "#code", unless = "#result == null ")
	public List<DictModel> queryEnableDictItemsByCode(String code) {
		log.debug("No cachedictCacheCall here when！");
		return sysDictMapper.queryEnableDictItemsByCode(code);
	}

	@Override
	public Map<String, List<DictModel>> queryDictItemsByCodeList(List<String> dictCodeList) {
		List<DictModelMany> list = sysDictMapper.queryDictItemsByCodeList(dictCodeList);
		Map<String, List<DictModel>> dictMap = new HashMap(5);
		for (DictModelMany dict : list) {
			List<DictModel> dictItemList = dictMap.computeIfAbsent(dict.getDictCode(), i -> new ArrayList<>());
			
			//update-begin-author:taoyan date:2023-4-28 for: QQYUN-5183【Simple flow】Multi-field splicing-checkbox、drop down box Wait for fields that need to be translated
			//dict.setDictCode(null);
			//update-end-author:taoyan date:2023-4-28 for: QQYUN-5183【Simple flow】Multi-field splicing-checkbox、drop down box Wait for fields that need to be translated
			
			dictItemList.add(new DictModel(dict.getValue(), dict.getText(), dict.getColor()));
		}
		return dictMap;
	}

	@Override
	public Map<String, List<DictModel>> queryAllDictItems() {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		long start = System.currentTimeMillis();
		Map<String, List<DictModel>> sysAllDictItems = new HashMap(5);
		List<Integer> tenantIds = null;
		//------------------------------------------------------------------------------------------------
		//Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			tenantIds = new ArrayList<>();
			tenantIds.add(0);
			if (TenantContext.getTenant() != null) {
				tenantIds.add(oConvertUtils.getInt(TenantContext.getTenant()));
			}
		}
		//------------------------------------------------------------------------------------------------
		List<DictModelMany> sysDictItemList = sysDictMapper.queryAllDictItems(tenantIds);
		// usegroupingByaccording todictCodeGroup
		sysAllDictItems = sysDictItemList.stream()
				.collect(Collectors.groupingBy(DictModelMany::getDictCode,
						Collectors.mapping(d -> new DictModel(d.getValue(), d.getText(), d.getColor()), Collectors.toList())));
		log.info("      >>> 1 How long does it take to obtain system dictionary entries?（SQL）：" + (System.currentTimeMillis() - start) + "millisecond");

		Map<String, List<DictModel>> enumRes = ResourceUtil.getEnumDictData();
		sysAllDictItems.putAll(enumRes);
		log.info("      >>> 2 How long does it take to obtain system dictionary entries?（Enum）：" + (System.currentTimeMillis() - start) + "millisecond");
		
		log.info("      >>> end The total time taken to obtain the system dictionary library：" + (System.currentTimeMillis() - start) + "millisecond");
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		//log.info("-------Log in to load system dictionary-----" + sysAllDictItems.toString());
		return sysAllDictItems;
	}

	/**
	 * Specified by querycode Get dictionary值text
	 * @param code
	 * @param key
	 * @return
	 */

	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,key = "#code+':'+#key", unless = "#result == null ")
	public String queryDictTextByKey(String code, String key) {
		log.debug("No cachedictTextCall here when！");
		return sysDictMapper.queryDictTextByKey(code, key);
	}

	@Override
	public Map<String, List<DictModel>> queryManyDictByKeys(List<String> dictCodeList, List<String> keys) {
		List<DictModelMany> list = sysDictMapper.queryManyDictByKeys(dictCodeList, keys);
		Map<String, List<DictModel>> dictMap = new HashMap(5);
		for (DictModelMany dict : list) {
			List<DictModel> dictItemList = dictMap.computeIfAbsent(dict.getDictCode(), i -> new ArrayList<>());
			dictItemList.add(new DictModel(dict.getValue(), dict.getText()));
		}
		//update-begin-author:taoyan date:2022-7-8 for: System dictionary data should include customjavakind-enumerate
		Map<String, List<DictModel>> enumRes = ResourceUtil.queryManyDictByKeys(dictCodeList, keys);
		dictMap.putAll(enumRes);
		//update-end-author:taoyan date:2022-7-8 for: System dictionary data should include customjavakind-enumerate
		return dictMap;
	}

	/**
	 * Specified by querytableof text code Get dictionary
	 * dictTableCacheuseredisCache validity period10minute
	 * @param tableFilterSql
	 * @param text
	 * @param code
	 * @return
	 */
	@Override
	@Deprecated
	public List<DictModel> queryTableDictItemsByCode(String tableFilterSql, String text, String code) {
		log.debug("No cachedictTableListCall here when！");
		String str = tableFilterSql+","+text+","+code;
		// 【QQYUN-6533】table dictionary whitelistcheck
		sysBaseAPI.dictTableWhiteListCheckByDict(tableFilterSql, text, code);
		// 1.table dictionary blacklistcheck
		if(!dictQueryBlackListHandler.isPass(str)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}

		// 2.segmentationSQLGet table name and conditions
		String table = null;
		String filterSql = null;
		if(tableFilterSql.toLowerCase().indexOf(DataBaseConstant.SQL_WHERE)>0){
			String[] arr = tableFilterSql.split(" (?i)where ");
			table = arr[0];
			filterSql = oConvertUtils.getString(arr[1], null);
		}else{
			table = tableFilterSql;
		}
		
		// 3.SQLinjectioncheck
		SqlInjectionUtil.filterContentMulti(table, text, code);
		SqlInjectionUtil.specialFilterContentForDictSql(filterSql);
		
		// 4.Targeting adoption ${}The table names and fields written in the way are escaped andcheck
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		
		//return sysDictMapper.queryTableDictItemsByCode(tableFilterSql,text,code);
		table = table.toLowerCase();
		return sysDictMapper.queryTableDictWithFilter(table,text,code,filterSql);
	}

	@Override
	public List<DictModel> queryTableDictItemsByCodeAndFilter(String table, String text, String code, String filterSql) {
		log.debug("No cachedictTableListCall here when！");

		// 1.SQLinjection校验（Only restrict illegal modification of the database）
		SqlInjectionUtil.specialFilterContentForDictSql(table);
		SqlInjectionUtil.filterContentMulti(text, code);
		SqlInjectionUtil.specialFilterContentForDictSql(filterSql);
		
		String str = table+","+text+","+code;
		// 【QQYUN-6533】table dictionary whitelistcheck
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 2.table dictionary blacklist Check
		if(!dictQueryBlackListHandler.isPass(str)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}

		// 3.Targeting adoption ${}The table names and fields written in the way are escaped andcheck
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		table = table.toLowerCase();
		return sysDictMapper.queryTableDictWithFilter(table,text,code,filterSql);
	}
	
	/**
	 * Specified by querytableof text code Get dictionary值text
	 * dictTableCacheuseredisCache validity period10minute
	 * @param table
	 * @param text
	 * @param code
	 * @param key
	 * @return
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_TABLE_CACHE, unless = "#result == null ")
	public String queryTableDictTextByKey(String table,String text,String code, String key) {
		log.debug("No cachedictTableCall here when！");
		
		String str = table+","+text+","+code;
		// 【QQYUN-6533】table dictionary whitelistcheck
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 1.table dictionary blacklistcheck
		if(!dictQueryBlackListHandler.isPass(str)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		// 2.sqlinjectioncheck
		SqlInjectionUtil.filterContentMulti(table, text, code, key);

		// 3.Targeting adoption ${}The table names and fields written in the way are escaped andcheck
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		
		List<DictModel> dictModeList = sysDictMapper.queryTableDictByKeysAndFilterSql(table, text, code, null, Arrays.asList(key));
		if(CollectionUtils.isEmpty(dictModeList)){
			return null;
		}else{
			return dictModeList.get(0).getText();
		}
		
		//This method deletes（20230902）
		//return sysDictMapper.queryTableDictTextByKey(table,text,code,key);
	}

	@Override
	public List<DictModel> queryTableDictTextByKeys(String table, String text, String code, List<String> codeValues, String dataSource) {
		String str = table+","+text+","+code;
		//update-begin---author:chenrui ---date:20231221  for：[issues/#5643]Solve the problem that the distributed table dictionary cannot be queried across databases------------
		// Whether to customize the data source
		boolean isCustomDataSource = oConvertUtils.isNotEmpty(dataSource);
		// 如果是自定义数据源就不检查table dictionary whitelist
		if (!isCustomDataSource) {
			// 【QQYUN-6533】table dictionary whitelistcheck
			sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
			// 1.table dictionary blacklistcheck
			if (!dictQueryBlackListHandler.isPass(str)) {
				log.error(dictQueryBlackListHandler.getError());
				return null;
			}
		}
		//update-end---author:chenrui ---date:20231221  for：[issues/#5643]Solve the problem that the distributed table dictionary cannot be queried across databases------------

		// 2.segmentationSQLGet table name and conditions
		String filterSql = null;
		if(table.toLowerCase().indexOf(DataBaseConstant.SQL_WHERE)>0){
			String[] arr = table.split(" (?i)where ");
			table = arr[0];
			filterSql = arr[1];
		}
		
		// 3.SQLinjectioncheck
		SqlInjectionUtil.filterContentMulti(table, text, code);
		SqlInjectionUtil.specialFilterContentForDictSql(filterSql);

		// 4.Targeting adoption ${}The table names and fields written in the way are escaped andcheck
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);

		//update-begin---author:chenrui ---date:20231221  for：[issues/#5643]Solve the problem that the distributed table dictionary cannot be queried across databases------------
        // 切换为Dictionaryof数据源
        if (isCustomDataSource) {
            DynamicDataSourceContextHolder.push(dataSource);
        }
		List<DictModel> restData = sysDictMapper.queryTableDictByKeysAndFilterSql(table, text, code, filterSql, codeValues);
		// 清理自定义of数据源
		if (isCustomDataSource) {
			DynamicDataSourceContextHolder.clear();
		}
		return restData;
		//update-end---author:chenrui ---date:20231221  for：[issues/#5643]Solve the problem that the distributed table dictionary cannot be queried across databases------------
		//update-end-author:taoyan date:20220113 for: @dictAnnotation support dicttable set upwherecondition
	}

	@Override
	public List<String> queryTableDictByKeys(String table, String text, String code, String keys) {
		String str = table+","+text+","+code;
		// 【QQYUN-6533】table dictionary whitelistcheck
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 1.table dictionary blacklistcheck
		if(!dictQueryBlackListHandler.isPass(str)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		
		return this.queryTableDictByKeys(table, text, code, keys, true);
	}

	/**
	 * Specified by querytableof text code Get dictionary，Includetextandvalue
	 * dictTableCacheuseredisCache validity period10minute
	 * @param table
	 * @param text
	 * @param code
	 * @param codeValuesStr (comma separated)
	 * @param delNotExist 是否移除不存在of项，Default istrue，set tofalseIf akeydoes not exist in database，then return directlykeyitself
	 * @return
	 */
	@Override
	public List<String> queryTableDictByKeys(String table, String text, String code, String codeValuesStr, boolean delNotExist) {
		if(oConvertUtils.isEmpty(codeValuesStr)){
			return null;
		}

		//1.segmentationsqlGet table name and conditionsql
		String filterSql = null;
		if(table.toLowerCase().indexOf("where")!=-1){
			String[] arr = table.split(" (?i)where ");
			table = arr[0];
			filterSql = arr[1];
		}

		// 2.SQLinjectioncheck
		SqlInjectionUtil.filterContentMulti(table, text, code);
		SqlInjectionUtil.specialFilterContentForDictSql(filterSql);

		String str = table+","+text+","+code;
		// 【QQYUN-6533】table dictionary whitelistcheck
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 3.table dictionary blacklistcheck
		if(!dictQueryBlackListHandler.isPass(str)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		
		// 4.Targeting adoption ${}The table names and fields written in the way are escaped andcheck
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		
		//dictionarycondition值
		String[] codeValues = codeValuesStr.split(",");
		// 5.Query dictionary data
		List<DictModel> dicts = sysDictMapper.queryTableDictByKeysAndFilterSql(SqlInjectionUtil.getSqlInjectTableName(table), 
				SqlInjectionUtil.getSqlInjectField(text), SqlInjectionUtil.getSqlInjectField(code), filterSql, Arrays.asList(codeValues));
		
		List<String> texts = new ArrayList<>(dicts.size());
		// 6.Query出来of顺序可能是乱of，Need to sort
		for (String conditionalVal : codeValues) {
			List<DictModel> res = dicts.stream().filter(i -> conditionalVal.equals(i.getValue())).collect(Collectors.toList());
			if (res.size() > 0) {
				texts.add(res.get(0).getText());
			} else if (!delNotExist) {
				texts.add(conditionalVal);
			}
		}
		return texts;
	}

    /**
     * according todictionarykind型id删除关联表中其对应of数据
     */
    @Override
    public boolean deleteByDictId(SysDict sysDict) {
        sysDict.setDelFlag(CommonConstant.DEL_FLAG_1);
        return  this.updateById(sysDict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveMain(SysDict sysDict, List<SysDictItem> sysDictItemList) {
		int insert=0;
    	try{
			 insert = sysDictMapper.insert(sysDict);
			if (sysDictItemList != null) {
				for (SysDictItem entity : sysDictItemList) {
                    //update-begin---author:wangshuai ---date:20220211  for：[JTC-1168]If the dictionary item value is empty，then the dictionary item ignores the import------------
				    if(oConvertUtils.isEmpty(entity.getItemValue())){
				        return -1;
                    }
                    //update-end---author:wangshuai ---date:20220211  for：[JTC-1168]If the dictionary item value is empty，then the dictionary item ignores the import------------
					entity.setDictId(sysDict.getId());
					entity.setStatus(1);
					sysDictItemMapper.insert(entity);
				}
			}
		}catch(Exception e){
			return insert;
		}
		return insert;
    }

	@Override
	public List<DictModel> queryAllDepartBackDictModel() {
		return baseMapper.queryAllDepartBackDictModel();
	}

	@Override
	public List<DictModel> queryAllUserBackDictModel() {
		return baseMapper.queryAllUserBackDictModel();
	}
	
//	@Override
//	public List<DictModel> queryTableDictItems(String table, String text, String code, String keyword) {
//		return baseMapper.queryTableDictItems(table, text, code, "%"+keyword+"%");
//	}

	@Override
	public List<DictModel> queryLittleTableDictItems(String tableSql, String text, String code, String condition, String keyword, int pageNo, int pageSize) {
		int current = oConvertUtils.getInt(pageNo, 1);
		Page<DictModel> page = new Page<DictModel>(current, pageSize);
		page.setSearchCount(false);
		
		//in order to preventsql（jeecg提供了防injectionof方法，Can be spliced SQL Automatically escape parameters when making statements，avoidSQLinjection攻击）
		// 1. Targeting adoption ${}The table names and fields written in the way are escaped andcheck
		String table = SqlInjectionUtil.getSqlInjectTableName(CommonUtils.getTableNameByTableSql(tableSql));
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);

		// 2. QueryconditionSQL (获取conditionsqlMethod containssqlinjection校验)
		String filterSql = getFilterSql(tableSql, text, code, condition, keyword);
		
		// 3. Return table dictionary data
		IPage<DictModel> pageList = baseMapper.queryPageTableDictWithFilter(page, table, text, code, filterSql);
		return pageList.getRecords();
	}

	/**
	 * 获取condition语句 (Drop down search component Supports incoming sorting information Query sorting)
	 * 
	 * @param text
	 * @param code
	 * @param condition
	 * @param keyword
	 * @return
	 */
	private String getFilterSql(String tableSql, String text, String code, String condition, String keyword){
		String filterSql = "";
		String keywordSql = null;
		String sqlWhere = "where ";
		String sqlAnd = " and ";
		
		//【JTC-631】judge if table Brought where condition，那么就use and Query，Prevent error reporting
		boolean tableHasWhere = tableSql.toLowerCase().contains(sqlWhere);
        if (tableHasWhere) {
			sqlWhere = CommonUtils.getFilterSqlByTableSql(tableSql);
		}

		// Drop down search component Supports incoming sorting information Query sorting
		String orderField = "", orderType = "";
		if (oConvertUtils.isNotEmpty(keyword)) {
			// If the keyword is written Sort information xxxxx[orderby:create_time,desc]
			String orderKey = "[orderby";
			if (keyword.indexOf(orderKey) >= 0 && keyword.endsWith("]")) {
				String orderInfo = keyword.substring(keyword.indexOf(orderKey) + orderKey.length() + 1, keyword.length() - 1);
				keyword = keyword.substring(0, keyword.indexOf(orderKey));
				String[] orderInfoArray = orderInfo.split(SymbolConstant.COMMA);
				orderField = orderInfoArray[0];
				orderType = orderInfoArray[1];
			}

			if (oConvertUtils.isNotEmpty(keyword)) {
				// Determine whether it is multiple selection
				if (keyword.contains(SymbolConstant.COMMA)) {
					//update-begin--author:scott--date:20220105--for：JTC-529【form designer】 Edit page error，in参数use双引号导致 ----
					String inKeywords = "'" + String.join("','", keyword.split(",")) + "'";
					//update-end--author:scott--date:20220105--for：JTC-529【form designer】 Edit page error，in参数use双引号导致----
					keywordSql = "(" + text + " in (" + inKeywords + ") or " + code + " in (" + inKeywords + "))";
				} else {
					keywordSql = "("+text + " like '%"+keyword+"%' or "+ code + " like '%"+keyword+"%')";
				}
			}
		}
		
		//Drop down search component Supports incoming sorting information Query sorting
		//update-begin---author:chenrui ---date:20240327  for：[QQYUN-8514]Onlinein form Drop down search box Search Timessqlmistake，生成ofSQLOne more “and" ------------
        if (oConvertUtils.isNotEmpty(condition) && oConvertUtils.isNotEmpty(keywordSql)) {
            filterSql += sqlWhere + (tableHasWhere ? sqlAnd : " ") + condition + sqlAnd + keywordSql;
        } else if (oConvertUtils.isNotEmpty(condition)) {
            filterSql += sqlWhere + (tableHasWhere ? sqlAnd : " ") + condition;
        } else if (oConvertUtils.isNotEmpty(keywordSql)) {
            filterSql += sqlWhere + (tableHasWhere ? sqlAnd : " ") + keywordSql;
        } else if (tableHasWhere) {
            filterSql += sqlWhere;
        }
		//update-end---author:chenrui ---date:20240327  for：[QQYUN-8514]Onlinein form Drop down search box Search Timessqlmistake，生成ofSQLOne more “and" ------------
		// Add sorting logic
		if (oConvertUtils.isNotEmpty(orderField)) {
			filterSql += " order by " + orderField + " " + orderType;
		}

		// 处理返回condition
		// 1.1 返回conditionSQL（去掉开头of where ）
		final String wherePrefix = "(?i)where "; // (?i) Indicates case insensitivity
		String filterSqlString = filterSql.trim().replaceAll(wherePrefix, "");
		// 1.2 conditionSQLexploit check
		SqlInjectionUtil.specialFilterContentForDictSql(filterSqlString);
		// 1.3 判断如何返回condition是 order byAt the beginning, spell it in front of 1=1
		if (oConvertUtils.isNotEmpty(filterSqlString) && filterSqlString.trim().toUpperCase().startsWith("ORDER")) {
			filterSqlString = " 1=1 " + filterSqlString;
		}
		return filterSqlString;
	}
	
	
	@Override
	public List<DictModel> queryAllTableDictItems(String table, String text, String code, String condition, String keyword) {
		// 1.获取conditionsql
		String filterSql = getFilterSql(table, text, code, condition, keyword);

		// in order to preventsql（jeecg提供了防injectionof方法，Can be spliced SQL Automatically escape parameters when making statements，avoidSQLinjection攻击）
		// 2.Targeting adoption ${}The table names and fields written in the way are escaped andcheck
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		
		List<DictModel> ls = baseMapper.queryTableDictWithFilter(table, text, code, filterSql);
    	return ls;
	}

	@Override
	public List<TreeSelectModel> queryTreeList(Map<String, String> query, String table, String text, String code, String pidField, String pid, String hasChildField, int converIsLeafVal) {
		//in order to preventsql（jeecg提供了防injectionof方法，Can be spliced SQL Automatically escape parameters when making statements，avoidSQLinjection攻击）
		// 1.Targeting adoption ${}The table names and fields written in the way are escaped andcheck
		table = SqlInjectionUtil.getSqlInjectTableName(table);
		text = SqlInjectionUtil.getSqlInjectField(text);
		code = SqlInjectionUtil.getSqlInjectField(code);
		pidField = SqlInjectionUtil.getSqlInjectField(pidField);
		hasChildField = SqlInjectionUtil.getSqlInjectField(hasChildField);

		if(oConvertUtils.isEmpty(text) || oConvertUtils.isEmpty(code)){
			log.warn("text={}，code={}", text, code);
			log.warn("Wrong parameters for loading tree dictionary，textandcodeEmpty is not allowed！");
			return null;
		}
		
		// 2.Detection finalSQLexistsSQLinjection风险
		String dictCode = table + "," + text + "," + code;
		SqlInjectionUtil.filterContentMulti(dictCode);

		// 【QQYUN-6533】table dictionary whitelistcheck
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 3.table dictionarySQLTable name blacklist Check
		if(!dictQueryBlackListHandler.isPass(dictCode)){
			log.error("Sqlabnormal：{}", dictQueryBlackListHandler.getError());
			return null;
		}
		// 4.检测QueryconditionexistsSQLinjection
		Map<String, String> queryParams = null;
		if (query != null) {
			queryParams = new HashMap<>(5);
			for (Map.Entry<String, String> searchItem : query.entrySet()) {
				String fieldName = searchItem.getKey();
				queryParams.put(SqlInjectionUtil.getSqlInjectField(fieldName), searchItem.getValue());
			}
		}
		
		return baseMapper.queryTreeList(queryParams, table, text, code, pidField, pid, hasChildField, converIsLeafVal);
	}

	@Override
	public void deleteOneDictPhysically(String id) {
		this.baseMapper.deleteOneById(id);
		this.sysDictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId,id));
	}

	@Override
	public void updateDictDelFlag(int delFlag, String id) {
		baseMapper.updateDictDelFlag(delFlag,id);
	}

	@Override
	public List<SysDict> queryDeleteList(String tenantId) {
		//update-begin---author:wangshuai---date:2024-02-27---for:【QQYUN-8340】When Recycle Bin looks for soft deleted records，Does not determine whether multi-tenancy is enabled，造成可以查找并回收其他租户of数据 #5907---
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			if(oConvertUtils.isEmpty(tenantId)){
				return new ArrayList<>();
			}
			return baseMapper.queryDeleteListBtTenantId(oConvertUtils.getInt(tenantId));
		}
		//update-end---author:wangshuai---date:2024-02-27---for:【QQYUN-8340】When Recycle Bin looks for soft deleted records，Does not determine whether multi-tenancy is enabled，造成可以查找并回收其他租户of数据 #5907---
		return baseMapper.queryDeleteList();
	}

	@Override
	public List<DictModel> queryDictTablePageList(DictQuery query, int pageSize, int pageNo) {
		Page page = new Page(pageNo,pageSize,false);
		
		//in order to preventsql（jeecg提供了防injectionof方法，Can be spliced SQL Automatically escape parameters when making statements，avoidSQLinjection攻击）
		// 1. Targeting adoption ${}The table names and fields written in the way are escaped andcheck
		String table = SqlInjectionUtil.getSqlInjectTableName(query.getTable());
		String text = SqlInjectionUtil.getSqlInjectTableName(query.getText());
		String code = SqlInjectionUtil.getSqlInjectTableName(query.getCode());
		query.setCode(table);
		query.setTable(text);
		query.setText(code);
		
		String dictCode = table+","+text+","+code;
		// 【QQYUN-6533】table dictionary whitelistcheck
		sysBaseAPI.dictTableWhiteListCheckByDict(table, text, code);
		// 2.table dictionary blacklistcheck
		if(!dictQueryBlackListHandler.isPass(dictCode)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		
		// 3.SQLinjectioncheck
		SqlInjectionUtil.filterContentMulti(dictCode);
		
		Page<DictModel> pageList = baseMapper.queryDictTablePageList(page, query);
		return pageList.getRecords();
	}

	@Override
	public List<DictModel> getDictItems(String dictCode) {
		List<DictModel> ls;
		if (dictCode.contains(SymbolConstant.COMMA)) {
			//Association table dictionary（Example：sys_user,realname,id）
			String[] params = dictCode.split(",");
			if (params.length < 3) {
				// dictionaryCodeIncorrect format
				return null;
			}
	
			if (params.length == 4) {
				ls = this.queryTableDictItemsByCodeAndFilter(params[0], params[1], params[2], params[3]);
			} else if (params.length == 3) {
				ls = this.queryTableDictItemsByCode(params[0], params[1], params[2]);
			} else {
				// dictionaryCodeIncorrect format
				return null;
			}
		} else {
			//Dictionary
			ls = this.queryDictItemsByCode(dictCode);
		}
		//update-begin-author:taoyan date:2022-8-30 for: dictionary获取可以获取enumeratekindof数据
		if (ls == null || ls.size() == 0) {
			Map<String, List<DictModel>> map = ResourceUtil.getEnumDictData();
			if (map.containsKey(dictCode)) {
				return map.get(dictCode);
			}
		}
		//update-end-author:taoyan date:2022-8-30 for: dictionary获取可以获取enumeratekindof数据
		return ls;
	}

	@Override
	public List<DictModel> loadDict(String dictCode, String keyword, Integer pageNo, Integer pageSize) {
		// 【QQYUN-6533】table dictionary whitelistcheck
		sysBaseAPI.dictTableWhiteListCheckByDict(dictCode);
		// 1.table dictionary blacklistcheck
		if(!dictQueryBlackListHandler.isPass(dictCode)){
			log.error(dictQueryBlackListHandler.getError());
			return null;
		}
		
		// 2.dictionarySQLinjection风险check
		SqlInjectionUtil.specialFilterContentForDictSql(dictCode);

		if (dictCode.contains(SymbolConstant.COMMA)) {
			//update-begin-author:taoyan date:20210329 for: 下拉搜索不支持表名后加Querycondition
			String[] params = dictCode.split(",");
			String condition = null;
			if (params.length != 3 && params.length != 4) {
				// dictionaryCodeIncorrect format
				return null;
			} else if (params.length == 4) {
				condition = params[3];
				// update-begin-author:taoyan date:20220314 for: online表单Drop down search boxtable dictionary配置#{sys_org_code}Report an error #3500
				if(condition.indexOf(SymbolConstant.SYS_VAR_PREFIX)>=0){
					condition =  QueryGenerator.getSqlRuleValue(condition);
				}
				// update-end-author:taoyan date:20220314 for: online表单Drop down search boxtable dictionary配置#{sys_org_code}Report an error #3500
			}

			// dictionaryCodeIncorrect format [Table name is empty]
			if(oConvertUtils.isEmpty(params[0])){
				return null;
			}
			List<DictModel> ls;
			if (pageSize != null) {
				ls = this.queryLittleTableDictItems(params[0], params[1], params[2], condition, keyword, pageNo,pageSize);
			} else {
				ls = this.queryAllTableDictItems(params[0], params[1], params[2], condition, keyword);
			}
			//update-end-author:taoyan date:20210329 for: 下拉搜索不支持表名后加Querycondition
			return ls;
		} else {
			// dictionaryCodeIncorrect format
			return null;
		}
	}

	@Override
	public List<SysDictVo> getDictListByLowAppId(String lowAppId) {
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		List<SysDict> list =  baseMapper.getDictListByLowAppId(lowAppId,tenantId);
		//Querydictionary下面ofdictionary项
		List<SysDictVo> dictVoList = new ArrayList<>();
		for (SysDict dict:list) {
			SysDictVo dictVo = new SysDictVo();
			BeanUtils.copyProperties(dict,dictVo);
			List<SysDictItem> sysDictItems = sysDictItemMapper.selectItemsByMainId(dict.getId());
			dictVo.setDictItemsList(sysDictItems);
			dictVoList.add(dictVo);
		}
		return dictVoList;
	}

	@Override
	public String addDictByLowAppId(SysDictVo sysDictVo) {
		String[] dictResult = this.addDict(sysDictVo.getDictName(),sysDictVo.getLowAppId(),sysDictVo.getTenantId());
		String id = dictResult[0];
		String code = dictResult[1];
		this.addDictItem(id,sysDictVo.getDictItemsList());
		return code;
	}

	@Override
	public void editDictByLowAppId(SysDictVo sysDictVo) {
		String id = sysDictVo.getId();
		SysDict dict = baseMapper.selectById(id);
		if(null == dict){
			throw new JeecgBootException("dictionary数据不存在");
		}
		//Judgment applicationidand数据库中of是否一致，Do not allow modification if inconsistent
		if(!dict.getLowAppId().equals(sysDictVo.getLowAppId())){
			throw new JeecgBootException("dictionary数据不存在");
		}
		SysDict sysDict = new SysDict();
		sysDict.setDictName(sysDictVo.getDictName());
		sysDict.setId(id);
		baseMapper.updateById(sysDict);
		this.updateDictItem(id,sysDictVo.getDictItemsList());
		// 删除dictionary缓存
		redisUtil.removeAll(CacheConstant.SYS_DICT_CACHE + "::" + dict.getDictCode());
	}

	/**
	 * Undo tombstone
	 * @param ids
	 */
	@Override
	public boolean revertLogicDeleted(List<String> ids) {
		return baseMapper.revertLogicDeleted(ids) > 0;
	}

	/**
	 * Delete completely
	 * @param ids
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeLogicDeleted(List<String> ids) {
		// 1. 删除dictionary
		int line = this.baseMapper.removeLogicDeleted(ids);
		// 2. 删除dictionary选项配置
		line += this.sysDictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>().in(SysDictItem::getDictId, ids));
		return line > 0;
	}

	/**
	 * 添加dictionary
	 * @param dictName
	 */
	private String[] addDict(String dictName,String lowAppId, Integer tenantId) {
		SysDict dict = new SysDict();
		dict.setDictName(dictName);
		dict.setDictCode(RandomUtil.randomString(10));
		dict.setDelFlag(Integer.valueOf(CommonConstant.STATUS_0));
		dict.setLowAppId(lowAppId);
		dict.setTenantId(tenantId);
		baseMapper.insert(dict);
		String[] dictResult = new String[]{dict.getId(), dict.getDictCode()};
		return dictResult;
	}

	/**
	 * 添加dictionary子项
	 * @param id
	 * @param dictItemList
	 */
	private void addDictItem(String id,List<SysDictItem> dictItemList) {
		if(null!=dictItemList && dictItemList.size()>0){
			for (SysDictItem dictItem:dictItemList) {
				SysDictItem sysDictItem = new SysDictItem();
				BeanUtils.copyProperties(dictItem,sysDictItem);
				sysDictItem.setDictId(id);
				sysDictItem.setId("");
				sysDictItem.setStatus(Integer.valueOf(CommonConstant.STATUS_1));
				sysDictItemMapper.insert(sysDictItem);
			}
		}
	}

	/**
	 * 更新dictionary子项
	 * @param id
	 * @param dictItemList
	 */
	private void updateDictItem(String id,List<SysDictItem> dictItemList){
		//Delete first and then add Because the ordering may be inconsistent
		LambdaQueryWrapper<SysDictItem> query = new LambdaQueryWrapper<>();
		query.eq(SysDictItem::getDictId,id);
		sysDictItemMapper.delete(query);
		//Add new subitem
		this.addDictItem(id,dictItemList);
	}
}
