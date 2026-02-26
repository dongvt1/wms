package org.jeecg.modules.system.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.config.shiro.ShiroRealm;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.model.SysDictTree;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.service.ISysDictItemService;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.vo.SysDictPage;
import org.jeecg.modules.system.vo.lowapp.SysDictVo;
import org.jeecgframework.poi.excel.ExcelImportCheckUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * <p>
 * Dictionary front controller
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@RestController
@RequestMapping("/sys/dict")
@Slf4j
public class SysDictController {

	@Autowired
	private ISysDictService sysDictService;
	@Autowired
	private ISysDictItemService sysDictItemService;
	@Autowired
	public RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private ShiroRealm shiroRealm;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysDict>> queryPageList(
			SysDict sysDict,
			@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			// Query keywords，fuzzy filtercodeandname
			@RequestParam(name = "keywords", required = false) String keywords,
			HttpServletRequest req
	) {
		Result<IPage<SysDict>> result = new Result<IPage<SysDict>>();
		//------------------------------------------------------------------------------------------------
		//Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysDict.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(),0));
		}
		//------------------------------------------------------------------------------------------------
		QueryWrapper<SysDict> queryWrapper = QueryGenerator.initQueryWrapper(sysDict, req.getParameterMap());
		// Query keywords，fuzzy filtercodeandname
		if (oConvertUtils.isNotEmpty(keywords)) {
			queryWrapper.and(i -> i.like("dict_code", keywords).or().like("dict_name", keywords));
		}

		Page<SysDict> page = new Page<>(pageNo, pageSize);
		IPage<SysDict> pageList = sysDictService.page(page, queryWrapper);
		log.debug("Query the current page："+pageList.getCurrent());
		log.debug("Query the current page数量："+pageList.getSize());
		log.debug("Number of query results："+pageList.getRecords().size());
		log.debug("Total data："+pageList.getTotal());
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}

	/**
	 * @Function：Get tree dictionary data
	 * @param sysDict
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/treeList", method = RequestMethod.GET)
	public Result<List<SysDictTree>> treeList(SysDict sysDict,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req) {
		Result<List<SysDictTree>> result = new Result<>();
		LambdaQueryWrapper<SysDict> query = new LambdaQueryWrapper<>();
		// Construct query conditions
		String dictName = sysDict.getDictName();
		if(oConvertUtils.isNotEmpty(dictName)) {
			query.like(true, SysDict::getDictName, dictName);
		}
		query.orderByDesc(true, SysDict::getCreateTime);
		List<SysDict> list = sysDictService.list(query);
		List<SysDictTree> treeList = new ArrayList<>();
		for (SysDict node : list) {
			treeList.add(new SysDictTree(node));
		}
		result.setSuccess(true);
		result.setResult(treeList);
		return result;
	}

	/**
	 * Get all dictionary data
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryAllDictItems", method = RequestMethod.GET)
	public Result<?> queryAllDictItems(HttpServletRequest request) {
		Map<String, List<DictModel>> res = new HashMap(5);
		res = sysDictService.queryAllDictItems();
		return Result.ok(res);
	}

	/**
	 * Get dictionary data
	 * @param dictCode
	 * @return
	 */
	@RequestMapping(value = "/getDictText/{dictCode}/{key}", method = RequestMethod.GET)
	public Result<String> getDictText(@PathVariable("dictCode") String dictCode, @PathVariable("key") String key) {
		log.info(" dictCode : "+ dictCode);
		Result<String> result = new Result<String>();
		String text = null;
		try {
			text = sysDictService.queryDictTextByKey(dictCode, key);
			 result.setSuccess(true);
			 result.setResult(text);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Operation failed");
			return result;
		}
		return result;
	}


	/**
	 * Get dictionary data 【Interface signature verification】
	 * @param dictCode dictionarycode
	 * @param dictCode table name,text field,codeField  | Example：sys_user,realname,id
	 * @return
	 */
	@RequestMapping(value = "/getDictItems/{dictCode}", method = RequestMethod.GET)
	public Result<List<DictModel>> getDictItems(@PathVariable("dictCode") String dictCode, @RequestParam(value = "sign",required = false) String sign,HttpServletRequest request) {
		log.info(" dictCode : "+ dictCode);
		Result<List<DictModel>> result = new Result<List<DictModel>>();
		try {
			List<DictModel> ls = sysDictService.getDictItems(dictCode);
			if (ls == null) {
				result.error500("dictionaryCodeIncorrect format！");
				return result;
			}
			result.setSuccess(true);
			result.setResult(ls);
			log.debug(result.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Operation failed");
			return result;
		}
		return result;
	}

	/**
	 * 【Interface signature verification】
	 * 【JSearchSelectTagDrop-down search component dedicated interface】
	 * 大数据量的Dictionary Go asynchronous loading  That is, the front-end input content filters the data
	 * @param dictCode dictionarycodeFormat：table,text,code
	 * @return
	 */
	@RequestMapping(value = "/loadDict/{dictCode}", method = RequestMethod.GET)
	public Result<List<DictModel>> loadDict(@PathVariable("dictCode") String dictCode,
			@RequestParam(name="keyword",required = false) String keyword,
			@RequestParam(value = "sign",required = false) String sign,
			@RequestParam(name = "pageNo", defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		
		//update-begin-author:taoyan date:2023-5-22 for: /issues/4905 because of the square brackets(%5)caused by problems 表单生成器Field配置时，选择关联Field，When doing advanced configuration，Unable to load database list，hint SginSignature verification error！ #4905 RouteToRequestUrlFilter
		if(keyword!=null && keyword.indexOf("%5")>=0){
			try {
				keyword = URLDecoder.decode(keyword, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error("Drop-down search keyword decoding failed", e);
			}
		}
		//update-end-author:taoyan date:2023-5-22 for: /issues/4905 because of the square brackets(%5)caused by problems  表单生成器Field配置时，选择关联Field，When doing advanced configuration，Unable to load database list，hint SginSignature verification error！ #4905
		
		log.info(" 加载Dictionary数据,Load keywords: "+ keyword);
		Result<List<DictModel>> result = new Result<List<DictModel>>();
		try {
			List<DictModel> ls = sysDictService.loadDict(dictCode, keyword, pageNo,pageSize);
			if (ls == null) {
				result.error500("dictionaryCodeIncorrect format！");
				return result;
			}
			result.setSuccess(true);
			result.setResult(ls);
			log.info(result.toString());
			return result;
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Operation failed：" + e.getMessage());
			return result;
		}
	}

	/**
	 * 【Interface signature verification】
	 * 【给表单设计器的表dictionary使用】Drop down search mode，Dynamically splice data when there is a value
	 * @param dictCode
	 * @param keyword The value of the current control，Can be separated by commas
	 * @param sign
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/loadDictOrderByValue/{dictCode}", method = RequestMethod.GET)
	public Result<List<DictModel>> loadDictOrderByValue(
			@PathVariable("dictCode") String dictCode,
			@RequestParam(name = "keyword") String keyword,
			@RequestParam(value = "sign", required = false) String sign,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {
		// The first query finds the value selected by the user，and no pagination
		Result<List<DictModel>> firstRes = this.loadDict(dictCode, keyword, sign,null, null);
		if (!firstRes.isSuccess()) {
			return firstRes;
		}
		// Then query the data on the first page
		Result<List<DictModel>> result = this.loadDict(dictCode, "", sign,1, pageSize);
		if (!result.isSuccess()) {
			return result;
		}
		// Merge data from two queries
		List<DictModel> firstList = firstRes.getResult();
		List<DictModel> list = result.getResult();
		for (DictModel firstItem : firstList) {
			// anyMatch express：in the conditions of judgment，Any element matches successfully，returntrue
			// allMatch express：Determine the elements in the condition，All matched successfully，returntrue
			// noneMatch and allMatch on the contrary，express：Determine the elements in the condition，All failed to match，returntrue
			boolean none = list.stream().noneMatch(item -> item.getValue().equals(firstItem.getValue()));
			// when the element does not exist，Add to collection
			if (none) {
				list.add(0, firstItem);
			}
		}
		return result;
	}

	/**
	 * 【Interface signature verification】
	 * 根据dictionarycode加载dictionarytext return
	 * @param dictCode order：tableName,text,code
	 * @param keys To inquirekey
	 * @param sign
	 * @param delNotExist Whether to remove non-existing items，Default istrue，set tofalseIf akeydoes not exist in database，则直接returnkeyitself
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loadDictItem/{dictCode}", method = RequestMethod.GET)
	public Result<List<String>> loadDictItem(@PathVariable("dictCode") String dictCode,@RequestParam(name="key") String keys, @RequestParam(value = "sign",required = false) String sign,@RequestParam(value = "delNotExist",required = false,defaultValue = "true") boolean delNotExist,HttpServletRequest request) {
		Result<List<String>> result = new Result<>();
		try {
			if(dictCode.indexOf(SymbolConstant.COMMA)!=-1) {
				String[] params = dictCode.split(SymbolConstant.COMMA);
				if(params.length!=3) {
					result.error500("dictionaryCodeIncorrect format！");
					return result;
				}
				List<String> texts = sysDictService.queryTableDictByKeys(params[0], params[1], params[2], keys, delNotExist);

				result.setSuccess(true);
				result.setResult(texts);
				log.info(result.toString());
			}else {
				result.error500("dictionaryCodeIncorrect format！");
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Operation failed");
			return result;
		}

		return result;
	}

	/**
	 * 【Interface signature verification】
	 * 根据table name——显示Field-存储Field pid Load tree data
	 * @param hasChildField 是否叶子节点Field
	 * @param converIsLeafVal Is system conversion required? Whether the value of the leaf node (0Identity not converted、1Automatic conversion of standard systems)
	 * @param tableName table name
	 * @param text labelField
	 * @param code value Field
	 * @param condition  Query conditions  ？
	 *            
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/loadTreeData", method = RequestMethod.GET)
	public Result<List<TreeSelectModel>> loadTreeData(@RequestParam(name="pid",required = false) String pid,@RequestParam(name="pidField") String pidField,
												  @RequestParam(name="tableName") String tableName,
												  @RequestParam(name="text") String text,
												  @RequestParam(name="code") String code,
												  @RequestParam(name="hasChildField") String hasChildField,
												  @RequestParam(name="converIsLeafVal",defaultValue ="1") int converIsLeafVal,
												  @RequestParam(name="condition") String condition,
												  @RequestParam(value = "sign",required = false) String sign,HttpServletRequest request) {
		Result<List<TreeSelectModel>> result = new Result<List<TreeSelectModel>>();

		// 【QQYUN-9207】Prevent errors caused by empty parameters
		if (oConvertUtils.isEmpty(tableName) || oConvertUtils.isEmpty(text) || oConvertUtils.isEmpty(code)) {
			result.error500("dictionaryCodeIncorrect format！");
			return result;
		}

		// 1.获取Query conditions参数
		Map<String, String> query = null;
		if(oConvertUtils.isNotEmpty(condition)) {
			query = JSON.parseObject(condition, Map.class);
		}
		
		// 2.return查询结果
		List<TreeSelectModel> ls = sysDictService.queryTreeList(query,tableName, text, code, pidField, pid,hasChildField,converIsLeafVal);
		result.setSuccess(true);
		result.setResult(ls);
		return result;
	}

	/**
	 * 【APPinterface】根据dictionary配置查询表dictionary数据（No calling place has been found yet）
	 * @param query
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@Deprecated
	@GetMapping("/queryTableData")
	public Result<List<DictModel>> queryTableData(DictQuery query,
												  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
												  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
												  @RequestParam(value = "sign",required = false) String sign,HttpServletRequest request){
		Result<List<DictModel>> res = new Result<List<DictModel>>();
		List<DictModel> ls = this.sysDictService.queryDictTablePageList(query,pageSize,pageNo);
		res.setResult(ls);
		res.setSuccess(true);
		return res;
	}

	/**
	 * @Function：New
	 * @param sysDict
	 * @return
	 */
    @RequiresPermissions("system:dict:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysDict> add(@RequestBody SysDict sysDict) {
		Result<SysDict> result = new Result<SysDict>();
		try {
			sysDict.setCreateTime(new Date());
			sysDict.setDelFlag(CommonConstant.DEL_FLAG_0);
			sysDictService.save(sysDict);
			result.success("Saved successfully！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Operation failed");
		}
		return result;
	}

	/**
	 * @Function：edit
	 * @param sysDict
	 * @return
	 */
    @RequiresPermissions("system:dict:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT,RequestMethod.POST })
	public Result<SysDict> edit(@RequestBody SysDict sysDict) {
		Result<SysDict> result = new Result<SysDict>();
		SysDict sysdict = sysDictService.getById(sysDict.getId());
		if(sysdict==null) {
			result.error500("No corresponding entity found");
		}else {
			sysDict.setUpdateTime(new Date());
			boolean ok = sysDictService.updateById(sysDict);
			if(ok) {
				result.success("edit成功!");
			}
		}
		return result;
	}

	/**
	 * @Function：delete
	 * @param id
	 * @return
	 */
    @RequiresPermissions("system:dict:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@CacheEvict(value={CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDict> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysDict> result = new Result<SysDict>();
		boolean ok = sysDictService.removeById(id);
		if(ok) {
			result.success("delete成功!");
		}else{
			result.error500("delete失败!");
		}
		return result;
	}

	/**
	 * @Function：批量delete
	 * @param ids
	 * @return
	 */
    @RequiresPermissions("system:dict:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	@CacheEvict(value= {CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDict> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysDict> result = new Result<SysDict>();
		if(oConvertUtils.isEmpty(ids)) {
			result.error500("Parameter not recognized！");
		}else {
			sysDictService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("delete成功!");
		}
		return result;
	}

	/**
	 * @Function：refresh cache
	 * @return
	 */
	@RequestMapping(value = "/refleshCache")
	public Result<?> refleshCache() {
		Result<?> result = new Result<SysDict>();
		//清空dictionary缓存
//		Set keys = redisTemplate.keys(CacheConstant.SYS_DICT_CACHE + "*");
//		Set keys7 = redisTemplate.keys(CacheConstant.SYS_ENABLE_DICT_CACHE + "*");
//		Set keys2 = redisTemplate.keys(CacheConstant.SYS_DICT_TABLE_CACHE + "*");
//		Set keys21 = redisTemplate.keys(CacheConstant.SYS_DICT_TABLE_BY_KEYS_CACHE + "*");
//		Set keys3 = redisTemplate.keys(CacheConstant.SYS_DEPARTS_CACHE + "*");
//		Set keys4 = redisTemplate.keys(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
//		Set keys5 = redisTemplate.keys( "jmreport:cache:dict*");
//		Set keys6 = redisTemplate.keys( "jmreport:cache:dictTable*");
//		redisTemplate.delete(keys);
//		redisTemplate.delete(keys2);
//		redisTemplate.delete(keys21);
//		redisTemplate.delete(keys3);
//		redisTemplate.delete(keys4);
//		redisTemplate.delete(keys5);
//		redisTemplate.delete(keys6);
//		redisTemplate.delete(keys7);

		//update-begin-author:liusq date:20230404 for:  [issue/4358]springCacheThe cache clearing operation in“keys”
		redisUtil.removeAll(CacheConstant.SYS_DICT_CACHE);
		redisUtil.removeAll(CacheConstant.SYS_ENABLE_DICT_CACHE);
		redisUtil.removeAll(CacheConstant.SYS_DICT_TABLE_CACHE);
		redisUtil.removeAll(CacheConstant.SYS_DICT_TABLE_BY_KEYS_CACHE);
		redisUtil.removeAll(CacheConstant.SYS_DEPARTS_CACHE);
		redisUtil.removeAll(CacheConstant.SYS_DEPART_IDS_CACHE);
		redisUtil.removeAll("jmreport:cache:dict");
		redisUtil.removeAll("jmreport:cache:dictTable");
		//update-end-author:liusq date:20230404 for:  [issue/4358]springCacheThe cache clearing operation in“keys”
		
		//update-begin---author:scott ---date:2024-06-18  for：【TV360X-1320】To assign permissions, you must log out and log in again to take effect.，Causes a lot of trouble for users---
		// Clear the current user's authorization cache information
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser.isAuthenticated()) {
			shiroRealm.clearCache(currentUser.getPrincipals());
		}
		//update-end---author:scott ---date::2024-06-18  for：【TV360X-1320】To assign permissions, you must log out and log in again to take effect.，Causes a lot of trouble for users---
		return result;
	}

	/**
	 * Exportexcel
	 *
	 * @param request
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(SysDict sysDict,HttpServletRequest request) {
		//------------------------------------------------------------------------------------------------
		//Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysDict.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		
		// Step.1 组装Query conditions
		QueryWrapper<SysDict> queryWrapper = QueryGenerator.initQueryWrapper(sysDict, request.getParameterMap());
		//Step.2 AutoPoi ExportExcel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		String selections = request.getParameter("selections");
		if(!oConvertUtils.isEmpty(selections)){
			queryWrapper.in("id",selections.split(","));
		}
		List<SysDictPage> pageList = new ArrayList<SysDictPage>();

		List<SysDict> sysDictList = sysDictService.list(queryWrapper);
		for (SysDict dictMain : sysDictList) {
			SysDictPage vo = new SysDictPage();
			BeanUtils.copyProperties(dictMain, vo);
			// Check air tickets
			List<SysDictItem> sysDictItemList = sysDictItemService.selectItemsByMainId(dictMain.getId());
			vo.setSysDictItemList(sysDictItemList);
			pageList.add(vo);
		}

		// Export文件名称
		mv.addObject(NormalExcelConstants.FILE_NAME, "数据dictionary");
		// annotation objectClass
		mv.addObject(NormalExcelConstants.CLASS, SysDictPage.class);
		// Custom table parameters
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("数据dictionary列表", "Export人:"+user.getRealname(), "数据dictionary"));
		// Export数据列表
		mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
		return mv;
	}

	/**
	 * passexcelImport data
	 *
	 * @param request
	 * @param
	 * @return
	 */
    @RequiresPermissions("system:dict:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
 		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Get the uploaded file object
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(2);
			params.setNeedSave(true);
			try {
				//importExcelFormat校验，看匹配的Field文本概率
				Boolean t = ExcelImportCheckUtil.check(file.getInputStream(), SysDictPage.class, params);
				if(t!=null && !t){
					throw new RuntimeException("importExcelVerification failed ！");
				}
				List<SysDictPage> list = ExcelImportUtil.importExcel(file.getInputStream(), SysDictPage.class, params);
				// error message
				List<String> errorMessage = new ArrayList<>();
				int successLines = 0, errorLines = 0;
				for (int i=0;i< list.size();i++) {
					SysDict po = new SysDict();
					BeanUtils.copyProperties(list.get(i), po);
					po.setDelFlag(CommonConstant.DEL_FLAG_0);
					try {
						Integer integer = sysDictService.saveMain(po, list.get(i).getSysDictItemList());
						if(integer>0){
							successLines++;
                        //update-begin---author:wangshuai ---date:20220211  for：[JTC-1168]如果dictionary项值为空，则dictionary项忽略import------------
						}else if(integer == -1){
                            errorLines++;
                            errorMessage.add("dictionary名称：" + po.getDictName() + "，对应dictionary列表的dictionary项值不能为空，忽略import。");
                        }else{
                        //update-end---author:wangshuai ---date:20220211  for：[JTC-1168]如果dictionary项值为空，则dictionary项忽略import------------
							errorLines++;
							int lineNumber = i + 1;
                            //update-begin---author:wangshuai ---date:20220209  for：[JTC-1168]dictionary编号不能为空------------
                            if(oConvertUtils.isEmpty(po.getDictCode())){
                                errorMessage.add("No. " + lineNumber + " OK：dictionary编码不能为空，忽略import。");
                            }else{
                                errorMessage.add("No. " + lineNumber + " OK：dictionary编码已经存在，忽略import。");
                            }
                            //update-end---author:wangshuai ---date:20220209  for：[JTC-1168]dictionary编号不能为空------------
                        }
					}  catch (Exception e) {
						errorLines++;
						int lineNumber = i + 1;
						errorMessage.add("No. " + lineNumber + " OK：dictionary编码已经存在，忽略import。");
					}
				}
				return ImportExcelUtil.imporReturnRes(errorLines,successLines,errorMessage);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				return Result.error("文件import失败:"+e.getMessage());
			} finally {
				try {
					file.getInputStream().close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return Result.error("文件import失败！");
	}


	/**
	 * 查询被delete的列表
	 * @return
	 */
	@RequestMapping(value = "/deleteList", method = RequestMethod.GET)
	public Result<List<SysDict>> deleteList(HttpServletRequest request) {
		Result<List<SysDict>> result = new Result<List<SysDict>>();
		String tenantId = TokenUtils.getTenantIdByRequest(request);
		List<SysDict> list = this.sysDictService.queryDeleteList(tenantId);
		result.setSuccess(true);
		result.setResult(list);
		return result;
	}

	/**
	 * 物理delete
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/deletePhysic/{id}", method = RequestMethod.DELETE)
	public Result<?> deletePhysic(@PathVariable("id") String id) {
		try {
			sysDictService.deleteOneDictPhysically(id);
			return Result.ok("delete成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("delete失败!");
		}
	}

	/**
	 * 逻辑delete的Field，进OK取回
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/back/{id}", method = RequestMethod.PUT)
	public Result<?> back(@PathVariable("id") String id) {
		try {
			sysDictService.updateDictDelFlag(0,id);
			return Result.ok("Operation successful!");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("Operation failed!");
		}
	}
	/**
	 * 还原被逻辑delete的用户
	 *
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "/putRecycleBin", method = RequestMethod.PUT)
	public Result putRecycleBin(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
		try {
			String ids = jsonObject.getString("ids");
			if (StringUtils.isNotBlank(ids)) {
				sysDictService.revertLogicDeleted(Arrays.asList(ids.split(",")));
				return Result.ok("Operation successful!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("Operation failed!");
		}
		return Result.ok("Restore successful");
	}
	/**
	 * 彻底deletedictionary
	 *
	 * @param ids 被delete的dictionaryID，MultipleidSeparate with half-width commas
	 * @return
	 */
	@RequiresPermissions("system:dict:deleteRecycleBin")
	@RequestMapping(value = "/deleteRecycleBin", method = RequestMethod.DELETE)
	public Result deleteRecycleBin(@RequestParam("ids") String ids) {
		try {
			if (StringUtils.isNotBlank(ids)) {
				sysDictService.removeLogicDeleted(Arrays.asList(ids.split(",")));
			}
			return Result.ok("delete成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("delete失败!");
		}
	}

	/**
	 * VUEN-2584【issue】platformsqlSeveral issues with injection vulnerabilities
	 * Some special functions 可以将查询结果混夹在error message中，Exposing database information
	 * @param e
	 * @return
	 */
	@ExceptionHandler(java.sql.SQLException.class)
	public Result<?> handleSQLException(Exception e){
		String msg = e.getMessage();
		String extractvalue = "extractvalue";
		String updatexml = "updatexml";
		if(msg!=null && (msg.toLowerCase().indexOf(extractvalue)>=0 || msg.toLowerCase().indexOf(updatexml)>=0)){
			return Result.error("Verification failed，sqlparsing exception！");
		}
		return Result.error("Verification failed，sqlparsing exception！" + msg);
	}

	/**
	 * According to applicationid获取dictionary列表and详情
	 * @param request
	 */
	@GetMapping("/getDictListByLowAppId")
	public Result<List<SysDictVo>> getDictListByLowAppId(HttpServletRequest request){
		String lowAppId = oConvertUtils.getString(TokenUtils.getLowAppIdByRequest(request));
		List<SysDictVo> list = sysDictService.getDictListByLowAppId(lowAppId);
		return Result.ok(list);
	}

	/**
	 * 添加dictionary
	 * @param sysDictVo
	 * @param request
	 * @return
	 */
	@PostMapping("/addDictByLowAppId")
	public Result<String> addDictByLowAppId(@RequestBody SysDictVo sysDictVo,HttpServletRequest request){
		String lowAppId = oConvertUtils.getString(TokenUtils.getLowAppIdByRequest(request));
		String tenantId = oConvertUtils.getString(TokenUtils.getTenantIdByRequest(request));
		sysDictVo.setLowAppId(lowAppId);
		sysDictVo.setTenantId(oConvertUtils.getInteger(tenantId, null));
		sysDictService.addDictByLowAppId(sysDictVo);
		return Result.ok("Added successfully");
	}

	@PutMapping("/editDictByLowAppId")
	public Result<String> editDictByLowAppId(@RequestBody SysDictVo sysDictVo,HttpServletRequest request){
		String lowAppId = oConvertUtils.getString(TokenUtils.getLowAppIdByRequest(request));
		sysDictVo.setLowAppId(lowAppId);
		sysDictService.editDictByLowAppId(sysDictVo);
		return Result.ok("edit成功");
	}
}
