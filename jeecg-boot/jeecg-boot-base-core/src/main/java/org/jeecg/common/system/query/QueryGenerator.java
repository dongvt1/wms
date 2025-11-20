package org.jeecg.common.system.query;

import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import org.apache.commons.beanutils.PropertyUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.util.JeecgDataAutorUtils;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.util.SqlConcatUtil;
import org.jeecg.common.system.vo.SysPermissionDataRuleModel;
import org.jeecg.common.util.*;
import org.springframework.util.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: query builder
 * @author: jeecg-boot
 */
@Slf4j
public class QueryGenerator {
	public static final String SQL_RULES_COLUMN = "SQL_RULES_COLUMN";

	private static final String BEGIN = "_begin";
	private static final String END = "_end";
	/**
	 * Numeric type field，Splice this suffix Accepts multi-valued parameters
	 */
	private static final String MULTI = "_MultiString";
	private static final String STAR = "*";
	private static final String COMMA = ",";
	/**
	 * Query comma escape character Equivalent to a comma【void】
	 */
	public static final String QUERY_COMMA_ESCAPE = "++";
	private static final String NOT_EQUAL = "!";
	/**页面带有rule值Query，Space as separator*/
	private static final String QUERY_SEPARATE_KEYWORD = " ";
	/**高级Query前端传来的parameter名*/
	private static final String SUPER_QUERY_PARAMS = "superQueryParams";
	/** 高级Query前端传来的拼接方式parameter名 */
	private static final String SUPER_QUERY_MATCH_TYPE = "superQueryMatchType";
	/** single quote */
	public static final String SQL_SQ = "'";
	/**sort column*/
	private static final String ORDER_COLUMN = "column";
	/**sort by*/
	private static final String ORDER_TYPE = "order";
	private static final String ORDER_TYPE_ASC = "ASC";

	/**mysql 模糊Query之特殊字符下划线 （_、\）*/
	public static final String LIKE_MYSQL_SPECIAL_STRS = "_,%";

	/**date formattingyyyy-MM-dd*/
	public static final String YYYY_MM_DD = "yyyy-MM-dd";

	/**to_date*/
	public static final String TO_DATE = "to_date";

	/**time formatting */
	private static final ThreadLocal<SimpleDateFormat> LOCAL = new ThreadLocal<SimpleDateFormat>();
	private static SimpleDateFormat getTime(){
		SimpleDateFormat time = LOCAL.get();
		if(time == null){
			time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LOCAL.set(time);
		}
		return time;
	}
	
	/**
	 * GetQuery条件构造器QueryWrapperExample 通用Query条件已被封装完成
	 * @param searchObj Query实体
	 * @param parameterMap request.getParameterMap()
	 * @return QueryWrapperExample
	 */
	public static <T> QueryWrapper<T> initQueryWrapper(T searchObj,Map<String, String[]> parameterMap){
		long start = System.currentTimeMillis();
		QueryWrapper<T> queryWrapper = new QueryWrapper<T>();
		installMplus(queryWrapper, searchObj, parameterMap, null);
		log.debug("---Query条件构造器初始化完成,time consuming:"+(System.currentTimeMillis()-start)+"millisecond----");
		return queryWrapper;
	}
	
	//update-begin---author:chenrui ---date:20240527  for：[TV360X-378]增加自定义FieldQueryrule功能------------
	/**
	 * GetQuery条件构造器QueryWrapperExample 通用Query条件已被封装完成
	 * @param searchObj Query实体
	 * @param parameterMap request.getParameterMap()
	 * @param customRuleMap 自定义FieldQueryrule {field:QueryRuleEnum}
	 * @return QueryWrapperExample
	 */
	public static <T> QueryWrapper<T> initQueryWrapper(T searchObj,Map<String, String[]> parameterMap, Map<String, QueryRuleEnum> customRuleMap){
		long start = System.currentTimeMillis();
		QueryWrapper<T> queryWrapper = new QueryWrapper<T>();
		installMplus(queryWrapper, searchObj, parameterMap, customRuleMap);
		log.debug("---Query条件构造器初始化完成,time consuming:"+(System.currentTimeMillis()-start)+"millisecond----");
		return queryWrapper;
	}
	//update-end---author:chenrui ---date:20240527  for：[TV360X-378]增加自定义FieldQueryrule功能------------

	/**
	 * AssembleMybatis Plus Query条件
	 * <p>use this method The following points need to be noted::   
	 * <br>1.useQueryWrapper rather thanLambdaQueryWrapper;
	 * <br>2.Example化QueryWrapperEntities cannot be passed into parameters when   
	 * <br>Error example:likeQueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>(jeecgDemo);
	 * <br>Correct example:QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>();
	 * <br>3.也可以不usethis方法直接调用 {@link #initQueryWrapper}直接GetExample
	 */
	private static void installMplus(QueryWrapper<?> queryWrapper, Object searchObj, Map<String, String[]> parameterMap, Map<String, QueryRuleEnum> customRuleMap) {
		/*
		 * Notice:权限Query由前端配置数据rule When a person has multiple departments You can include conditions in the rule configuration orgCode Include #{sys_org_code}
		However, customization is not supportedSQLChinese writingorgCode in #{sys_org_code} 
		When a person has only one department Just configure the equal condition directly: orgCode equal #{sys_org_code} Or configure customSQL: orgCode = '#{sys_org_code}'
		*/
		
		//区间条件Assemble 模糊Query 高级QueryAssemble Simple sorting 权限Query
		PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(searchObj);
		Map<String,SysPermissionDataRuleModel> ruleMap = getRuleMap();
		
		//Customize permission rulesSQLexpression
		for (String c : ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
				queryWrapper.and(i ->i.apply(getSqlRuleValue(ruleMap.get(c).getRuleValue())));
			}
		}
		
		String name, type, column;
		// update-begin--Author:taoyan  Date:20200923 for：issues/1671 like果Field加annotation了@TableField(exist = false),Not leavingDBQuery-------
		//Define mapping of entity fields and database field names 高级Query中 Only entity fields can be obtained like果设置TableFieldannotation 那么Query条件会出问题
		Map<String,String> fieldColumnMap = new HashMap<>(5);
		for (int i = 0; i < origDescriptors.length; i++) {
			//aliasName = origDescriptors[i].getName();  mybatis  Entity attribute does not exist No need to deal with aliases
			name = origDescriptors[i].getName();
			type = origDescriptors[i].getPropertyType().toString();
			try {
				if (judgedIsUselessField(name)|| !PropertyUtils.isReadable(searchObj, name)) {
					continue;
				}

				Object value = PropertyUtils.getSimpleProperty(searchObj, name);
				column = ReflectHelper.getTableFieldName(searchObj.getClass(), name);
				if(column==null){
					//columnfornullThere is only one situation That is Add to了annotation@TableField(exist = false) There is no need to deal with it later.
					continue;
				}
				fieldColumnMap.put(name,column);
				//Data permissionsQuery
				if(ruleMap.containsKey(name)) {
					addRuleToQueryWrapper(ruleMap.get(name), column, origDescriptors[i].getPropertyType(), queryWrapper);
				}
				//区间Query
				doIntervalQuery(queryWrapper, parameterMap, type, name, column);
				//Judgment of single value  Parameters with different identification strings 走不同的Query
				//TODO 这种前后带逗号的support分割后模糊Query(多选FieldQuery生效) Example：,1,3,
				if (null != value && value.toString().startsWith(COMMA) && value.toString().endsWith(COMMA)) {
					String multiLikeval = value.toString().replace(",,", COMMA);
					String[] vals = multiLikeval.substring(1, multiLikeval.length()).split(COMMA);
					final String field = oConvertUtils.camelToUnderline(column);
					if(vals.length>1) {
						queryWrapper.and(j -> {
                            log.info("---Query过滤器，Queryrule---field:{}, rule:{}, value:{}", field, "like", vals[0]);
							j = j.like(field,vals[0]);
							for (int k=1;k<vals.length;k++) {
								j = j.or().like(field,vals[k]);
								log.info("---Query过滤器，Queryrule .or()---field:{}, rule:{}, value:{}", field, "like", vals[k]);
							}
							//return j;
						});
					}else {
						log.info("---Query过滤器，Queryrule---field:{}, rule:{}, value:{}", field, "like", vals[0]);
						queryWrapper.and(j -> j.like(field,vals[0]));
					}
				}else {
					//update-begin---author:chenrui ---date:20240527  for：[TV360X-378]增加自定义FieldQueryrule功能------------
					QueryRuleEnum rule;
					if(null != customRuleMap && customRuleMap.containsKey(name)) {
						// 有自定义rule,use自定义rule.
						rule = customRuleMap.get(name);
					}else {
						//according toparameter值带什么关键字符串judge走什么类型的Query
						 rule = convert2Rule(value);
					}
					//update-end---author:chenrui ---date:20240527  for：[TV360X-378]增加自定义FieldQueryrule功能------------
					value = replaceValue(rule,value);
					// add -begin Add tojudgefor字符串时设for全模糊Query
					//if( (rule==null || QueryRuleEnum.EQ.equals(rule)) && "class java.lang.String".equals(type)) {
						// You can set left and right blur or full blur，Varies from person to person
						//rule = QueryRuleEnum.LIKE;
					//}
					// add -end Add tojudgefor字符串时设for全模糊Query
					addEasyQuery(queryWrapper, column, rule, value);
				}
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		// Sorting logic deal with
		doMultiFieldsOrder(queryWrapper, parameterMap, fieldColumnMap);
				
		//高级Query
		doSuperQuery(queryWrapper, parameterMap, fieldColumnMap);
		// update-end--Author:taoyan  Date:20200923 for：issues/1671 like果Field加annotation了@TableField(exist = false),Not leavingDBQuery-------
		
	}


	/**
	 * 区间Query
	 * @param queryWrapper queryobject
	 * @param parameterMap parametermap
	 * @param type         Field type
	 * @param filedName    Field name
	 * @param columnName   Column name
	 */
	private static void doIntervalQuery(QueryWrapper<?> queryWrapper, Map<String, String[]> parameterMap, String type, String filedName, String columnName) throws ParseException {
		// Add to Determine whether there is an interval value
		String endValue = null,beginValue = null;
		if (parameterMap != null && parameterMap.containsKey(filedName + BEGIN)) {
			beginValue = parameterMap.get(filedName + BEGIN)[0].trim();
			addQueryByRule(queryWrapper, columnName, type, beginValue, QueryRuleEnum.GE);

		}
		if (parameterMap != null && parameterMap.containsKey(filedName + END)) {
			endValue = parameterMap.get(filedName + END)[0].trim();
			addQueryByRule(queryWrapper, columnName, type, endValue, QueryRuleEnum.LE);
		}
		//多值Query
		if (parameterMap != null && parameterMap.containsKey(filedName + MULTI)) {
			endValue = parameterMap.get(filedName + MULTI)[0].trim();
			addQueryByRule(queryWrapper, columnName.replace(MULTI,""), type, endValue, QueryRuleEnum.IN);
		}
	}
	
	private static void doMultiFieldsOrder(QueryWrapper<?> queryWrapper,Map<String, String[]> parameterMap, Map<String,String> fieldColumnMap) {
		Set<String> allFields = fieldColumnMap.keySet();
		String column=null,order=null;
		if(parameterMap!=null&& parameterMap.containsKey(ORDER_COLUMN)) {
			column = parameterMap.get(ORDER_COLUMN)[0];
		}
		if(parameterMap!=null&& parameterMap.containsKey(ORDER_TYPE)) {
			order = parameterMap.get(ORDER_TYPE)[0];
		}
		
		if(oConvertUtils.isNotEmpty(column)){
			log.info("单Fieldsortrule>> column:" + column + ",sort by:" + order);
		}

		// 1. List multi-field sorting priority
		if(parameterMap!=null&& parameterMap.containsKey("sortInfoString")) {
			// Sort by multiple fields
			String sortInfoString = parameterMap.get("sortInfoString")[0];
			log.info("Sort by multiple fieldsrule>> sortInfoString:" + sortInfoString);
			List<OrderItem> orderItemList = SqlConcatUtil.getQueryConditionOrders(column, order, sortInfoString);
			log.info(orderItemList.toString());
			if (orderItemList != null && !orderItemList.isEmpty()) {
				for (OrderItem item : orderItemList) {
					// one、Get sorting database field
					String columnName = item.getColumn();
					// 1.Dictionary fields，Remove dictionary translation text suffix
					if(columnName.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
						columnName = columnName.substring(0, column.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
					}
					// 2.实体驼峰Field转for数据库Field
					columnName = SqlInjectionUtil.getSqlInjectSortField(columnName);

					// two、设置Fieldsortrule
					if (item.isAsc()) {
						queryWrapper.orderByAsc(columnName);
					} else {
						queryWrapper.orderByDesc(columnName);
					}
				}
			}
			return;
		}

		// 2. List form fields sorted by default
		if(oConvertUtils.isEmpty(column) && parameterMap!=null&& parameterMap.containsKey("defSortString")) {
			// Sort by multiple fields
			String sortInfoString = parameterMap.get("defSortString")[0];
			log.info("默认Sort by multiple fieldsrule>> defSortString:" + sortInfoString);
			List<OrderItem> orderItemList = SqlConcatUtil.getQueryConditionOrders(column, order, sortInfoString);
			log.info(orderItemList.toString());
			if (orderItemList != null && !orderItemList.isEmpty()) {
				for (OrderItem item : orderItemList) {
					// one、Get sorting database field
					String columnName = item.getColumn();
					// 1.Dictionary fields，Remove dictionary translation text suffix
					if(columnName.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
						columnName = columnName.substring(0, column.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
					}
					// 2.实体驼峰Field转for数据库Field
					columnName = SqlInjectionUtil.getSqlInjectSortField(columnName);
					
					// two、设置Fieldsortrule
					if (item.isAsc()) {
						queryWrapper.orderByAsc(columnName);
					} else {
						queryWrapper.orderByDesc(columnName);
					}
				}
			}
			return;
		}
		
		//update-begin-author:scott date:2022-11-07 for:Avoid user-defined tables without default fields{creation time}，Cause sorting error
		//TODO Avoid user-defined tables without default fieldscreation time，Cause sorting error
		if(DataBaseConstant.CREATE_TIME.equals(column) && !fieldColumnMap.containsKey(DataBaseConstant.CREATE_TIME)){
			column = "id";
			log.warn("Detected that there are no fields in the entitycreateTime，Change to adoptIDsort！");
		}
		//update-end-author:scott date:2022-11-07 for:Avoid user-defined tables without default fields{creation time}，Cause sorting error
		
		if (oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
			//Dictionary fields，Remove dictionary translation text suffix
			if(column.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
				column = column.substring(0, column.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
			}

			//update-begin-author:taoyan date:2022-5-16 for: issues/3676 When getting the system user list，useSQLInjection takes effect
			//judgecolumnIs it the current entity?
			log.debug("The current fields are："+ allFields);
			if (!allColumnExist(column, allFields)) {
				throw new JeecgBootException("请Notice，将要sort的列Field不存在：" + column);
			}
			//update-end-author:taoyan date:2022-5-16 for: issues/3676 When getting the system user list，useSQLInjection takes effect

			//update-begin-author:scott date:2022-10-10 for:【jeecg-boot/issues/I5FJU6】doMultiFieldsOrder() Sort by multiple fields方法存在问题
			//Sort by multiple fields方法没有读取 MybatisPlus annotation @TableField inside value value
			if (column.contains(",")) {
				List<String> columnList = Arrays.asList(column.split(","));
				String columnStrNew = columnList.stream().map(c -> fieldColumnMap.get(c)).collect(Collectors.joining(","));
				if (oConvertUtils.isNotEmpty(columnStrNew)) {
					column = columnStrNew;
				}
			}else{
				column = fieldColumnMap.get(column);
			}
			//update-end-author:scott date:2022-10-10 for:【jeecg-boot/issues/I5FJU6】doMultiFieldsOrder() Sort by multiple fields方法存在问题

			//SQLinjectioncheck
			SqlInjectionUtil.filterContentMulti(column);

			//update-begin--Author:scott  Date:20210531 for：36 多条件sort无效问题修正-------
			// sortrule修改
			// 将现有sort _ 前端传递sort条件{....,column: 'column1,column2',order: 'desc'} translated intosql "column1,column2 desc"
			// 修改for _ 前端传递sort条件{....,column: 'column1,column2',order: 'desc'} translated intosql "column1 desc,column2 desc"
			if (order.toUpperCase().indexOf(ORDER_TYPE_ASC)>=0) {
				queryWrapper.orderByAsc(SqlInjectionUtil.getSqlInjectSortFields(column.split(",")));
			} else {
				queryWrapper.orderByDesc(SqlInjectionUtil.getSqlInjectSortFields(column.split(",")));
			}
			//update-end--Author:scott  Date:20210531 for：36 多条件sort无效问题修正-------
		}
	}

	//update-begin-author:taoyan date:2022-5-23 for: issues/3676 When getting the system user list，useSQLInjection takes effect
	/**
	 * Sort by multiple fields judge所传Field是否存在
	 * @return
	 */
	private static boolean allColumnExist(String columnStr, Set<String> allFields){
		boolean exist = true;
		if(columnStr.indexOf(COMMA)>=0){
			String[] arr = columnStr.split(COMMA);
			for(String column: arr){
				if(!allFields.contains(column)){
					exist = false;
					break;
				}
			}
		}else{
			exist = allFields.contains(columnStr);
		}
		return exist;
	}
	//update-end-author:taoyan date:2022-5-23 for: issues/3676 When getting the system user list，useSQLInjection takes effect
	
	/**
	 * 高级Query
	 * @param queryWrapper Queryobject
	 * @param parameterMap parameterobject
	 * @param fieldColumnMap Entity fields correspond to database columnsmap
	 */
	private static void doSuperQuery(QueryWrapper<?> queryWrapper,Map<String, String[]> parameterMap, Map<String,String> fieldColumnMap) {
		if(parameterMap!=null&& parameterMap.containsKey(SUPER_QUERY_PARAMS)){
			String superQueryParams = parameterMap.get(SUPER_QUERY_PARAMS)[0];
			String superQueryMatchType = parameterMap.get(SUPER_QUERY_MATCH_TYPE) != null ? parameterMap.get(SUPER_QUERY_MATCH_TYPE)[0] : MatchTypeEnum.AND.getValue();
            MatchTypeEnum matchType = MatchTypeEnum.getByValue(superQueryMatchType);
            // update-begin--Author:sunjianlei  Date:20200325 for：高级Query的条件要用括号括起来，Prevent conflicts with other user conditions -------
            try {
                superQueryParams = URLDecoder.decode(superQueryParams, "UTF-8");
                List<QueryCondition> conditions = JSON.parseArray(superQueryParams, QueryCondition.class);
                if (conditions == null || conditions.size() == 0) {
                    return;
                }
				// update-begin-author:sunjianlei date:20220119 for: 【JTC-573】 过滤空条件Query，prevent sql Splice the excess and
				List<QueryCondition> filterConditions = conditions.stream().filter(
						rule -> (oConvertUtils.isNotEmpty(rule.getField())
													&& oConvertUtils.isNotEmpty(rule.getRule())
													&& oConvertUtils.isNotEmpty(rule.getVal())
												 )
												|| "empty".equals(rule.getRule())
				).collect(Collectors.toList());
				if (filterConditions.size() == 0) {
					return;
				}
				// update-end-author:sunjianlei date:20220119 for: 【JTC-573】 过滤空条件Query，prevent sql Splice the excess and
                log.debug("---高级Queryparameter-->" + filterConditions);

                queryWrapper.and(andWrapper -> {
                    for (int i = 0; i < filterConditions.size(); i++) {
                        QueryCondition rule = filterConditions.get(i);
                        if (
								(
								    oConvertUtils.isNotEmpty(rule.getField()) && oConvertUtils.isNotEmpty(rule.getRule()) && oConvertUtils.isNotEmpty(rule.getVal())
								)
							    || "empty".equals(rule.getRule())
						) {

                            log.debug("SuperQuery ==> " + rule.toString());

                            //update-begin-author:taoyan date:20201228 for: 【高级Query】 oracle 日期equalQueryReport an error
							Object queryValue = rule.getVal();
                            if("date".equals(rule.getType())){
								queryValue = DateUtils.str2Date(rule.getVal(),DateUtils.date_sdf.get());
							}else if("datetime".equals(rule.getType())){
								queryValue = DateUtils.str2Date(rule.getVal(), DateUtils.datetimeFormat.get());
							}
							// update-begin--author:sunjianlei date:20210702 for：【/issues/I3VR8E】高级Query没有类型Convert，Queryparameter都是字符串类型 ----
							String dbType = rule.getDbType();
							if (oConvertUtils.isNotEmpty(dbType)) {
								try {
									String valueStr = String.valueOf(queryValue);
									switch (dbType.toLowerCase().trim()) {
										case "int":
											queryValue = Integer.parseInt(valueStr);
											break;
										case "bigdecimal":
											queryValue = new BigDecimal(valueStr);
											break;
										case "short":
											queryValue = Short.parseShort(valueStr);
											break;
										case "long":
											queryValue = Long.parseLong(valueStr);
											break;
										case "float":
											queryValue = Float.parseFloat(valueStr);
											break;
										case "double":
											queryValue = Double.parseDouble(valueStr);
											break;
										case "boolean":
											queryValue = Boolean.parseBoolean(valueStr);
											break;
                                        default:
									}
								} catch (Exception e) {
									log.error("高级Query值Convert失败：", e);
								}
							}
							// update-begin--author:sunjianlei date:20210702 for：【/issues/I3VR8E】高级Query没有类型Convert，Queryparameter都是字符串类型 ----
                            addEasyQuery(andWrapper, fieldColumnMap.get(rule.getField()), QueryRuleEnum.getByValue(rule.getRule()), queryValue);
							//update-end-author:taoyan date:20201228 for: 【高级Query】 oracle 日期equalQueryReport an error

                            // like果拼接方式是OR，Just spliceOR
                            if (MatchTypeEnum.OR == matchType && i < (filterConditions.size() - 1)) {
                                andWrapper.or();
                            }
                        }
                    }
                    //return andWrapper;
                });
            } catch (UnsupportedEncodingException e) {
                log.error("--高级Queryparameter转码失败：" + superQueryParams, e);
            } catch (Exception e) {
                log.error("--高级Query拼接失败：" + e.getMessage());
                e.printStackTrace();
            }
            // update-end--Author:sunjianlei  Date:20200325 for：高级Query的条件要用括号括起来，Prevent conflicts with other user conditions -------
		}
		//log.info(" superQuery getCustomSqlSegment: "+ queryWrapper.getCustomSqlSegment());
	}
	/**
	 * according to所传value Convert into corresponding comparison method
	 * support><= like in !
	 * @param value
	 * @return
	 */
	public static QueryRuleEnum convert2Rule(Object value) {
		// Avoid empty data
		// update-begin-author:taoyan date:20210629 for: Query条件输入空格导致return null后续judge导致抛出nullabnormal
		if (value == null) {
			return QueryRuleEnum.EQ;
		}
		String val = (value + "").toString().trim();
		if (val.length() == 0) {
			return QueryRuleEnum.EQ;
		}
		// update-end-author:taoyan date:20210629 for: Query条件输入空格导致return null后续judge导致抛出nullabnormal
		QueryRuleEnum rule =null;

		//update-begin--Author:scott  Date:20190724 for：initQueryWrapperAssemblesqlQuery条件错误 #284-------------------
		//TODO 此处rule，only applies to le lt ge gt
		// step 2 .>= =<
        int length2 = 2;
        int length3 = 3;
		if (rule == null && val.length() >= length3) {
			if(QUERY_SEPARATE_KEYWORD.equals(val.substring(length2, length3))){
				rule = QueryRuleEnum.getByValue(val.substring(0, 2));
			}
		}
		// step 1 .> <
		if (rule == null && val.length() >= length2) {
			if(QUERY_SEPARATE_KEYWORD.equals(val.substring(1, length2))){
				rule = QueryRuleEnum.getByValue(val.substring(0, 1));
			}
		}
		//update-end--Author:scott  Date:20190724 for：initQueryWrapperAssemblesqlQuery条件错误 #284---------------------

		// step 3 like
		//update-begin-author:taoyan for: /issues/3382 Default with*Just go blurry，但是like果只有one个*，那么走equalQuery
		if(rule == null && val.equals(STAR)){
			rule = QueryRuleEnum.EQ;
		}
		//update-end-author:taoyan for: /issues/3382  Default with*Just go blurry，但是like果只有one个*，那么走equalQuery
		if (rule == null && val.contains(STAR)) {
			if (val.startsWith(STAR) && val.endsWith(STAR)) {
				rule = QueryRuleEnum.LIKE;
			} else if (val.startsWith(STAR)) {
				rule = QueryRuleEnum.LEFT_LIKE;
			} else if(val.endsWith(STAR)){
				rule = QueryRuleEnum.RIGHT_LIKE;
			}
		}

		// step 4 in
		if (rule == null && val.contains(COMMA)) {
			//TODO in Query这inside应该有个bug  like果oneField本身就是多选 Use at this timeinQuery 未必能Query出来
			rule = QueryRuleEnum.IN;
		}
		// step 5 != 
		if(rule == null && val.startsWith(NOT_EQUAL)){
			rule = QueryRuleEnum.NE;
		}
		// step 6 xx+xx+xx 这种情况适用于like果想要用逗号作精确Query But the system defaults to commain So it can be used++replace【此逻辑void】
		if(rule == null && val.indexOf(QUERY_COMMA_ESCAPE)>0){
			rule = QueryRuleEnum.EQ_WITH_ADD;
		}

		//update-begin--Author:taoyan  Date:20201229 for：initQueryWrapperAssemblesqlQuery条件错误 #284---------------------
		//特殊deal with：Oracle的expressionto_date('xxx','yyyy-MM-dd')Contains commas，会被识别forinQuery，转forequalQuery
		if(rule == QueryRuleEnum.IN && val.indexOf(YYYY_MM_DD)>=0 && val.indexOf(TO_DATE)>=0){
			rule = QueryRuleEnum.EQ;
		}
		//update-end--Author:taoyan  Date:20201229 for：initQueryWrapperAssemblesqlQuery条件错误 #284---------------------

		return rule != null ? rule : QueryRuleEnum.EQ;
	}
	
	/**
	 * replace掉关键字字符
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private static Object replaceValue(QueryRuleEnum rule, Object value) {
		if (rule == null) {
			return null;
		}
		if (! (value instanceof String)){
			return value;
		}
		String val = (value + "").toString().trim();
		//update-begin-author:taoyan date:20220302 for: Query条件valuefor等号（=）bug #3443
		if(QueryRuleEnum.EQ.getValue().equals(val)){
			return val;
		}
		//update-end-author:taoyan date:20220302 for: Query条件valuefor等号（=）bug #3443
		if (rule == QueryRuleEnum.LIKE) {
			value = val.substring(1, val.length() - 1);
			//mysql 模糊Query之特殊字符下划线 （_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.LEFT_LIKE || rule == QueryRuleEnum.NE) {
			value = val.substring(1);
			//mysql 模糊Query之特殊字符下划线 （_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.RIGHT_LIKE) {
			value = val.substring(0, val.length() - 1);
			//mysql 模糊Query之特殊字符下划线 （_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.IN) {
			value = val.split(",");
		} else if (rule == QueryRuleEnum.EQ_WITH_ADD) {
			value = val.replaceAll("\\+\\+", COMMA);
		}else {
			//update-begin--Author:scott  Date:20190724 for：initQueryWrapperAssemblesqlQuery条件错误 #284-------------------
			if(val.startsWith(rule.getValue())){
				//TODO The logic here should be commented out-> like果Query内容中带有Query匹配rule符号，will be intercepted（比like：>=Hello）
				value = val.replaceFirst(rule.getValue(),"");
			}else if(val.startsWith(rule.getCondition()+QUERY_SEPARATE_KEYWORD)){
				value = val.replaceFirst(rule.getCondition()+QUERY_SEPARATE_KEYWORD,"").trim();
			}
			//update-end--Author:scott  Date:20190724 for：initQueryWrapperAssemblesqlQuery条件错误 #284-------------------
		}
		return value;
	}
	
	private static void addQueryByRule(QueryWrapper<?> queryWrapper,String name,String type,String value,QueryRuleEnum rule) throws ParseException {
		if(oConvertUtils.isNotEmpty(value)) {
			//update-begin--Author:sunjianlei  Date:20220104 for：【JTC-409】Fixed the problem of no conversion type when comma delimited，导致类型严格的数据库QueryReport an error -------------------
			// 针对Numeric type field，多值Query
			if(value.contains(COMMA)){
				Object[] temp = Arrays.stream(value.split(COMMA)).map(v -> {
					try {
						return QueryGenerator.parseByType(v, type, rule);
					} catch (ParseException e) {
						e.printStackTrace();
						return v;
					}
				}).toArray();
				addEasyQuery(queryWrapper, name, rule, temp);
				return;
			}
			Object temp = QueryGenerator.parseByType(value, type, rule);
			addEasyQuery(queryWrapper, name, rule, temp);
			//update-end--Author:sunjianlei  Date:20220104 for：【JTC-409】Fixed the problem of no conversion type when comma delimited，导致类型严格的数据库QueryReport an error -------------------
		}
	}

	/**
	 * according to类型Convert给定value
	 * @param value
	 * @param type
	 * @param rule
	 * @return
	 * @throws ParseException
	 */
	private static Object parseByType(String value, String type, QueryRuleEnum rule) throws ParseException {
		Object temp;
		switch (type) {
			case "class java.lang.Integer":
				temp =  Integer.parseInt(value);
				break;
			case "class java.math.BigDecimal":
				temp =  new BigDecimal(value);
				break;
			case "class java.lang.Short":
				temp =  Short.parseShort(value);
				break;
			case "class java.lang.Long":
				temp =  Long.parseLong(value);
				break;
			case "class java.lang.Float":
				temp =   Float.parseFloat(value);
				break;
			case "class java.lang.Double":
				temp =  Double.parseDouble(value);
				break;
			case "class java.util.Date":
				temp = getDateQueryByRule(value, rule);
				break;
			default:
				temp = value;
				break;
		}
		return temp;
	}
	
	/**
	 * Get日期类型value
	 * @param value
	 * @param rule
	 * @return
	 * @throws ParseException
	 */
	private static Date getDateQueryByRule(String value,QueryRuleEnum rule) throws ParseException {
		Date date = null;
		int length = 10;
		if(value.length()==length) {
			if(rule==QueryRuleEnum.GE) {
				//comparatively greater than
				date = getTime().parse(value + " 00:00:00");
			}else if(rule==QueryRuleEnum.LE) {
				//less than
				date = getTime().parse(value + " 23:59:59");
			}
			//TODO The date type is special possibleoracle下不one定好使
		}
		if(date==null) {
			date = getTime().parse(value);
		}
		return date;
	}
	
	/**
	  * according torule走不同的Query
	 * @param queryWrapper QueryWrapper
	 * @param name         Field name
	 * @param rule         Queryrule
	 * @param value        Query条件值
	 */
	public static void addEasyQuery(QueryWrapper<?> queryWrapper, String name, QueryRuleEnum rule, Object value) {
		if (
				(
				   name==null || value == null || rule == null || oConvertUtils.isEmpty(value)
				) 
				  && !QueryRuleEnum.EMPTY.equals(rule)) {
			return;
		}
		name = oConvertUtils.camelToUnderline(name);
		log.debug("---高级Query Queryrule---field:{} , rule:{} , value:{}",name,rule.getValue(),value);
		switch (rule) {
		case GT:
			queryWrapper.gt(name, value);
			break;
		case GE:
			queryWrapper.ge(name, value);
			break;
		case EMPTY:
			queryWrapper.isNull(name);
			break;
		case LT:
			queryWrapper.lt(name, value);
			break;
		case LE:
			queryWrapper.le(name, value);
			break;
		case EQ:
		case EQ_WITH_ADD:
			queryWrapper.eq(name, value);
			break;
		case NE:
			queryWrapper.ne(name, value);
			break;
		case IN:
			if(value instanceof String) {
				queryWrapper.in(name, (Object[])value.toString().split(COMMA));
			}else if(value instanceof String[]) {
				queryWrapper.in(name, (Object[]) value);
			}
			//update-begin-author:taoyan date:20200909 for:【bug】in 类型多值Query Not suitablepostgresql #1671
			else if(value.getClass().isArray()) {
				queryWrapper.in(name, (Object[])value);
			}else {
				queryWrapper.in(name, value);
			}
			//update-end-author:taoyan date:20200909 for:【bug】in 类型多值Query Not suitablepostgresql #1671
			break;
		case LIKE:
			queryWrapper.like(name, value);
			break;
		case LEFT_LIKE:
			queryWrapper.likeLeft(name, value);
			break;
		case NOT_LEFT_LIKE:
			queryWrapper.notLikeLeft(name, value);
			break;
		case RIGHT_LIKE:
			queryWrapper.likeRight(name, value);
			break;
		case NOT_RIGHT_LIKE:
			queryWrapper.notLikeRight(name, value);
			break;
		//update-begin---author:chenrui ---date:20240527  for：[TV360X-378]下拉多框according to条件Query不出来:增加自定义FieldQueryrule功能------------
		case LIKE_WITH_OR:
			final String nameFinal = name;
			Object[] vals;
			if (value instanceof String) {
				vals = value.toString().split(COMMA);
			} else if (value instanceof String[]) {
				vals = (Object[]) value;
			}
			//update-begin-author:taoyan date:20200909 for:【bug】in 类型多值Query Not suitablepostgresql #1671
			else if (value.getClass().isArray()) {
				vals = (Object[]) value;
			} else {
				vals = new Object[]{value};
			}
			queryWrapper.and(j -> {
				log.info("---Query过滤器，Queryrule---field:{}, rule:{}, value:{}", nameFinal, "like", vals[0]);
				j = j.like(nameFinal, vals[0]);
				for (int k = 1; k < vals.length; k++) {
					j = j.or().like(nameFinal, vals[k]);
					log.info("---Query过滤器，Queryrule .or()---field:{}, rule:{}, value:{}", nameFinal, "like", vals[k]);
				}
			});
			break;
		//update-end---author:chenrui ---date:20240527  for：[TV360X-378]下拉多框according to条件Query不出来:增加自定义FieldQueryrule功能------------
		default:
			log.info("--Queryrule未匹配到---");
			break;
		}
	}
	/**
	 * 
	 * @param name
	 * @return
	 */
	private static boolean judgedIsUselessField(String name) {
		return "class".equals(name) || "ids".equals(name)
				|| "page".equals(name) || "rows".equals(name)
//// update-begin--author:sunjianlei date:20240808 for：【TV360X-2009】Cancel filter sort、order Field，prevent前端sortReport an error ------
//// https://github.com/jeecgboot/JeecgBoot/issues/6937
//				|| "sort".equals(name) || "order".equals(name)
//// update-end----author:sunjianlei date:20240808 for：【TV360X-2009】Cancel filter sort、order Field，prevent前端sortReport an error ------
				;
	}

	

	/**
	 * Get请求对应的Data permissionsrule TODO Multiple permissions for the same column There is a problem
	 * @return
	 */
	public static Map<String, SysPermissionDataRuleModel> getRuleMap() {
		Map<String, SysPermissionDataRuleModel> ruleMap = new HashMap<>(5);
		List<SysPermissionDataRuleModel> list = null;
		//update-begin-author:taoyan date:2023-6-1 for:QQYUN-5441 【Simple flow】Get multiple users/department/Role 设置departmentQuery Report an error
		try {
			list = JeecgDataAutorUtils.loadDataSearchConditon();
		}catch (Exception e){
			log.error("according torequestobjectGet权限数据失败，possible是定时任务中执行的。", e);
		}
		//update-end-author:taoyan date:2023-6-1 for:QQYUN-5441 【Simple flow】Get multiple users/department/Role 设置departmentQuery Report an error
		if(list != null&&list.size()>0){
			if(list.get(0)==null){
				return ruleMap;
			}
			for (SysPermissionDataRuleModel rule : list) {
				String column = rule.getRuleColumn();
				if(QueryRuleEnum.SQL_RULES.getValue().equals(rule.getRuleConditions())) {
					column = SQL_RULES_COLUMN+rule.getId();
				}
				ruleMap.put(column, rule);
			}
		}
		return ruleMap;
	}
	
	private static void addRuleToQueryWrapper(SysPermissionDataRuleModel dataRule, String name, Class propertyType, QueryWrapper<?> queryWrapper) {
		QueryRuleEnum rule = QueryRuleEnum.getByValue(dataRule.getRuleConditions());
		if(rule.equals(QueryRuleEnum.IN) && ! propertyType.equals(String.class)) {
			String[] values = dataRule.getRuleValue().split(",");
			Object[] objs = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				objs[i] = NumberUtils.parseNumber(values[i], propertyType);
			}
			addEasyQuery(queryWrapper, name, rule, objs);
		}else {
			if (propertyType.equals(String.class)) {
				addEasyQuery(queryWrapper, name, rule, converRuleValue(dataRule.getRuleValue()));
			}else if (propertyType.equals(Date.class)) {
				String dateStr =converRuleValue(dataRule.getRuleValue());
                int length = 10;
				if(dateStr.length()==length){
					addEasyQuery(queryWrapper, name, rule, DateUtils.str2Date(dateStr,DateUtils.date_sdf.get()));
				}else{
					addEasyQuery(queryWrapper, name, rule, DateUtils.str2Date(dateStr,DateUtils.datetimeFormat.get()));
				}
			}else {
				//update-begin---author:chenrui ---date:20241125  for：[issues/7481]In multi-tenant mode Data permissionsuse变量：#{tenant_id} Report an error------------
				addEasyQuery(queryWrapper, name, rule, NumberUtils.parseNumber(converRuleValue(dataRule.getRuleValue()), propertyType));
				//update-end---author:chenrui ---date:20241125  for：[issues/7481]In multi-tenant mode Data permissionsuse变量：#{tenant_id} Report an error------------
			}
		}
	}
	
	public static String converRuleValue(String ruleValue) {
		String value = JwtUtil.getUserSystemData(ruleValue,null);
		return value!= null ? value : ruleValue;
	}

	/**
	* @author: scott
	* @Description: 去掉值前后single quote
	* @date: 2020/3/19 21:26
	* @param ruleValue: 
	* @Return: java.lang.String
	*/
	public static String trimSingleQuote(String ruleValue) {
		if (oConvertUtils.isEmpty(ruleValue)) {
			return "";
		}
		if (ruleValue.startsWith(QueryGenerator.SQL_SQ)) {
			ruleValue = ruleValue.substring(1);
		}
		if (ruleValue.endsWith(QueryGenerator.SQL_SQ)) {
			ruleValue = ruleValue.substring(0, ruleValue.length() - 1);
		}
		return ruleValue;
	}
	
	public static String getSqlRuleValue(String sqlRule){
		try {
			Set<String> varParams = getSqlRuleParams(sqlRule);
			if (varParams == null || varParams.isEmpty()) {
				return sqlRule;
			}
			for(String var:varParams){
				String tempValue = converRuleValue(var);
				sqlRule = sqlRule.replace("#{"+var+"}",tempValue);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return sqlRule;
	}
	
	/**
	 * Getsqlin#{key} thiskeycomposed ofset
	 */
	public static Set<String> getSqlRuleParams(String sql) {
		if(oConvertUtils.isEmpty(sql)){
			return null;
		}
		Set<String> varParams = new HashSet<String>();
		//update-begin---author:chenrui ---date:20250108  for：[QQYUN-10785]Data permissions，查看自己拥有department的权限中存在问题 #7288------------
		String regex = "#\\{\\[*\\w+]*}";
		//update-end---author:chenrui ---date:20250108  for：[QQYUN-10785]Data permissions，查看自己拥有department的权限中存在问题 #7288------------
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sql);
		while(m.find()){
			String var = m.group();
			varParams.add(var.substring(var.indexOf("{")+1,var.indexOf("}")));
		}
		return varParams;
	}
	
	/**
	 * GetQuery条件 
	 * @param field
	 * @param alias
	 * @param value
	 * @param isString
	 * @return
	 */
	public static String getSingleQueryConditionSql(String field,String alias,Object value,boolean isString) {
		return SqlConcatUtil.getSingleQueryConditionSql(field, alias, value, isString,null);
	}
	
	/**
	 *   according to权限相关配置生成相关的SQL statement
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String installAuthJdbc(Class<?> clazz) {
		StringBuffer sb = new StringBuffer();
		//权限Query
		Map<String,SysPermissionDataRuleModel> ruleMap = getRuleMap();
		PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(clazz);
		String sqlAnd = " and ";
		for (String c : ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
				sb.append(sqlAnd+getSqlRuleValue(ruleMap.get(c).getRuleValue()));
			}
		}
		String name, column;
		for (int i = 0; i < origDescriptors.length; i++) {
			name = origDescriptors[i].getName();
			if (judgedIsUselessField(name)) {
				continue;
			}
			if(ruleMap.containsKey(name)) {
				column = ReflectHelper.getTableFieldName(clazz, name);
				if(column==null){
					continue;
				}
				SysPermissionDataRuleModel dataRule = ruleMap.get(name);
				QueryRuleEnum rule = QueryRuleEnum.getByValue(dataRule.getRuleConditions());
				Class propType = origDescriptors[i].getPropertyType();
				boolean isString = propType.equals(String.class);
				Object value;
				//update-begin---author:chenrui ---date:20240527  for：[TV360X-539]Data permissions，配置日期equal条件时后端报Convert错误------------
				if(isString || Date.class.equals(propType)) {
				//update-end---author:chenrui ---date:20240527  for：[TV360X-539]Data permissions，配置日期equal条件时后端报Convert错误------------
					value = converRuleValue(dataRule.getRuleValue());
				}else {
					value = NumberUtils.parseNumber(dataRule.getRuleValue(),propType);
				}
				String filedSql = SqlConcatUtil.getSingleSqlByRule(rule, oConvertUtils.camelToUnderline(column), value,isString);
				sb.append(sqlAnd+filedSql);
			}
		}
		log.info("query auth sql is:"+sb.toString());
		return sb.toString();
	}
	
	/**
	  * according to权限相关配置 AssemblempRequired permissions
	 * @param queryWrapper
	 * @param clazz
	 * @return
	 */
	public static void installAuthMplus(QueryWrapper<?> queryWrapper,Class<?> clazz) {
		//权限Query
		Map<String,SysPermissionDataRuleModel> ruleMap = getRuleMap();
		PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(clazz);
		for (String c : ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
				queryWrapper.and(i ->i.apply(getSqlRuleValue(ruleMap.get(c).getRuleValue())));
			}
		}
		String name, column;
		for (int i = 0; i < origDescriptors.length; i++) {
			name = origDescriptors[i].getName();
			if (judgedIsUselessField(name)) {
				continue;
			}
			column = ReflectHelper.getTableFieldName(clazz, name);
			if(column==null){
				continue;
			}
			if(ruleMap.containsKey(name)) {
				addRuleToQueryWrapper(ruleMap.get(name), column, origDescriptors[i].getPropertyType(), queryWrapper);
			}
		}
	}

	/**
	 * Convertsqlin系统变量
	 * @param sql
	 * @return
	 */
	public static String convertSystemVariables(String sql){
		return getSqlRuleValue(sql);
	}

	/**
	 * Get系统数据库类型
	 */
	private static String getDbType(){
		return CommonUtils.getDatabaseType();
	}

	/**
	 * mysql 模糊Query之特殊字符下划线 （_、\）
	 *
	 * @param value:
	 * @Return: java.lang.String
	 */
	private static String specialStrConvert(String value) {
		if (DataBaseConstant.DB_TYPE_MYSQL.equals(getDbType()) || DataBaseConstant.DB_TYPE_MARIADB.equals(getDbType())) {
			String[] specialStr = QueryGenerator.LIKE_MYSQL_SPECIAL_STRS.split(",");
			for (String str : specialStr) {
				if (value.indexOf(str) !=-1) {
					value = value.replace(str, "\\" + str);
				}
			}
		}
		return value;
	}
}
