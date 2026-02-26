package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.ReflectHelper;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

 /**
 * @Description: Classification dictionary
 * @Author: jeecg-boot
 * @Date:   2019-05-29
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/category")
@Slf4j
public class SysCategoryController {
	@Autowired
	private ISysCategoryService sysCategoryService;

     /**
      * Classification coding0
      */
     private static final String CATEGORY_ROOT_CODE = "0";

	/**
	  * Paginated list query
	 * @param sysCategory
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/rootList")
	public Result<IPage<SysCategory>> queryPageList(SysCategory sysCategory,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		if(oConvertUtils.isEmpty(sysCategory.getPid())){
			sysCategory.setPid("0");
		}
		Result<IPage<SysCategory>> result = new Result<IPage<SysCategory>>();
		//------------------------------------------------------------------------------------------------
		//Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysCategory.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(),0));
		}
		//------------------------------------------------------------------------------------------------
		
		//--author:os_chengtgen---date:20190804 -----for: Classification dictionary页面显示mistake,issues:377--------start
		//--author:liusq---date:20211119 -----for: 【vue3】Classification dictionary页面Query条件配置--------start
		QueryWrapper<SysCategory> queryWrapper = QueryGenerator.initQueryWrapper(sysCategory, req.getParameterMap());
		String name = sysCategory.getName();
		String code = sysCategory.getCode();
		//QueryWrapper<SysCategory> queryWrapper = new QueryWrapper<SysCategory>();
		if(StringUtils.isBlank(name)&&StringUtils.isBlank(code)){
			queryWrapper.eq("pid", sysCategory.getPid());
		}
		//--author:liusq---date:20211119 -----for: Classification dictionary页面Query条件配置--------end
		//--author:os_chengtgen---date:20190804 -----for:【vue3】 Classification dictionary页面显示mistake,issues:377--------end

		Page<SysCategory> page = new Page<SysCategory>(pageNo, pageSize);
		IPage<SysCategory> pageList = sysCategoryService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	@GetMapping(value = "/childList")
	public Result<List<SysCategory>> queryPageList(SysCategory sysCategory,HttpServletRequest req) {
		//------------------------------------------------------------------------------------------------
		//Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysCategory.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		Result<List<SysCategory>> result = new Result<List<SysCategory>>();
		QueryWrapper<SysCategory> queryWrapper = QueryGenerator.initQueryWrapper(sysCategory, req.getParameterMap());
		List<SysCategory> list = sysCategoryService.list(queryWrapper);
		result.setSuccess(true);
		result.setResult(list);
		return result;
	}
	
	
	/**
	  *   Add to
	 * @param sysCategory
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<SysCategory> add(@RequestBody SysCategory sysCategory) {
		Result<SysCategory> result = new Result<SysCategory>();
		try {
			sysCategoryService.addSysCategory(sysCategory);
			result.success("Add to成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Operation failed");
		}
		return result;
	}
	
	/**
	  *  edit
	 * @param sysCategory
	 * @return
	 */
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT,RequestMethod.POST })
	public Result<SysCategory> edit(@RequestBody SysCategory sysCategory) {
		Result<SysCategory> result = new Result<SysCategory>();
		SysCategory sysCategoryEntity = sysCategoryService.getById(sysCategory.getId());
		if(sysCategoryEntity==null) {
			result.error500("No corresponding entity found");
		}else {
			sysCategoryService.updateSysCategory(sysCategory);
			result.success("Modification successful!");
		}
		return result;
	}
	
	/**
	  *   passiddelete
	 * @param id
	 * @return
	 */
	@DeleteMapping(value = "/delete")
	public Result<SysCategory> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysCategory> result = new Result<SysCategory>();
		SysCategory sysCategory = sysCategoryService.getById(id);
		if(sysCategory==null) {
			result.error500("No corresponding entity found");
		}else {
			this.sysCategoryService.deleteSysCategory(id);
			result.success("delete成功!");
		}
		
		return result;
	}
	
	/**
	  *  批量delete
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = "/deleteBatch")
	public Result<SysCategory> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysCategory> result = new Result<SysCategory>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("Parameter not recognized！");
		}else {
			this.sysCategoryService.deleteSysCategory(ids);
			result.success("delete成功!");
		}
		return result;
	}
	
	/**
	  * passidQuery
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<SysCategory> queryById(@RequestParam(name="id",required=true) String id) {
		Result<SysCategory> result = new Result<SysCategory>();
		SysCategory sysCategory = sysCategoryService.getById(id);
		if(sysCategory==null) {
			result.error500("No corresponding entity found");
		}else {
			result.setResult(sysCategory);
			result.setSuccess(true);
		}
		return result;
	}

  /**
      * Exportexcel
   *
   * @param request
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, SysCategory sysCategory) {
	  //------------------------------------------------------------------------------------------------
	  //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
	  if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
		  sysCategory.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
	  }
	  //------------------------------------------------------------------------------------------------
	  
      // Step.1 组装Query条件Query数据
      QueryWrapper<SysCategory> queryWrapper = QueryGenerator.initQueryWrapper(sysCategory, request.getParameterMap());
      List<SysCategory> pageList = sysCategoryService.list(queryWrapper);
      // Step.2 AutoPoi ExportExcel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      // Filter selected data
      String selections = request.getParameter("selections");
      if(oConvertUtils.isEmpty(selections)) {
    	  mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
      }else {
    	  List<String> selectionList = Arrays.asList(selections.split(","));
    	  List<SysCategory> exportList = pageList.stream().filter(item -> selectionList.contains(item.getId())).collect(Collectors.toList());
    	  mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
      }
      //Export文件名称
      mv.addObject(NormalExcelConstants.FILE_NAME, "Classification dictionary列表");
      mv.addObject(NormalExcelConstants.CLASS, SysCategory.class);
      LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Classification dictionary列表数据", "Export人:"+user.getRealname(), "Export信息"));
      return mv;
  }

  /**
      * passexcelImport data
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException{
      MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
      Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
	  // error message
	  List<String> errorMessage = new ArrayList<>();
	  int successLines = 0, errorLines = 0;
	  for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
          // Get the uploaded file object
          MultipartFile file = entity.getValue();
          ImportParams params = new ImportParams();
          params.setTitleRows(2);
          params.setHeadRows(1);
          params.setNeedSave(true);
          try {
              List<SysCategory> listSysCategorys = ExcelImportUtil.importExcel(file.getInputStream(), SysCategory.class, params);
			  //update-begin---author:chenrui ---date:20250721  for：[issues/8612]Classification dictionary导入bug #8612 ------------
			  Set<String> parentCategoryIds = new HashSet<>();
			  //update-end---author:chenrui ---date:20250721  for：[issues/8612]Classification dictionary导入bug #8612 ------------
			 //Sort by encoding length
              Collections.sort(listSysCategorys);
			  log.info("sortedlist====>",listSysCategorys);
              for (int i = 0; i < listSysCategorys.size(); i++) {
				  SysCategory sysCategoryExcel = listSysCategorys.get(i);
				  String code = sysCategoryExcel.getCode();
				  if(code.length()>3){
					  String pCode = sysCategoryExcel.getCode().substring(0,code.length()-3);
					  log.info("pCode====>",pCode);
					  String pId=sysCategoryService.queryIdByCode(pCode);
					  log.info("pId====>",pId);
					  if(StringUtils.isNotBlank(pId)){
						  sysCategoryExcel.setPid(pId);
						  //update-begin---author:chenrui ---date:20250721  for：[issues/8612]Classification dictionary导入bug #8612 ------------
						  parentCategoryIds.add(pId);
						  //update-end---author:chenrui ---date:20250721  for：[issues/8612]Classification dictionary导入bug #8612 ------------
					  }
				  }else{
					  sysCategoryExcel.setPid("0");
				  }
				  try {
					  sysCategoryService.save(sysCategoryExcel);
					  successLines++;
				  } catch (Exception e) {
					  errorLines++;
					  String message = e.getMessage().toLowerCase();
					  int lineNumber = i + 1;
					  // pass索引名判断出错信息
					  if (message.contains(CommonConstant.SQL_INDEX_UNIQ_CATEGORY_CODE)) {
						  errorMessage.add("No. " + lineNumber + " OK：Classification coding已经存在，Ignore import。");
					  }  else {
						  errorMessage.add("No. " + lineNumber + " OK：unknown error，Ignore import");
						  log.error(e.getMessage(), e);
					  }
				  }
              }
			  //update-begin---author:chenrui ---date:20250721  for：[issues/8612]Classification dictionary导入bug #8612 ------------
			  if(oConvertUtils.isObjectNotEmpty(parentCategoryIds)){
				  for (String parentCategoryId : parentCategoryIds) {
					  SysCategory parentCategory = sysCategoryService.getById(parentCategoryId);
					  if(oConvertUtils.isObjectNotEmpty(parentCategory)){
						  parentCategory.setHasChild(CommonConstant.STATUS_1);
						  sysCategoryService.updateById(parentCategory);
					  }
				  }
			  }
			  //update-end---author:chenrui ---date:20250721  for：[issues/8612]Classification dictionary导入bug #8612 ------------
          } catch (Exception e) {
			  errorMessage.add("Exception occurred：" + e.getMessage());
			  log.error(e.getMessage(), e);
          } finally {
              try {
                  file.getInputStream().close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return ImportExcelUtil.imporReturnRes(errorLines,successLines,errorMessage);
  }
  
  
  
  /**
     * Load single data used to echo
   */
    @RequestMapping(value = "/loadOne", method = RequestMethod.GET)
 	public Result<SysCategory> loadOne(@RequestParam(name="field") String field,@RequestParam(name="val") String val) {
 		Result<SysCategory> result = new Result<SysCategory>();
 		try {
			//update-begin-author:taoyan date:2022-5-6 for: issues/3663 sqlInjection problem
			boolean isClassField = ReflectHelper.isClassField(field, SysCategory.class);
			if (!isClassField) {
				return Result.error("Field is invalid，Check, please!");
			}
			//update-end-author:taoyan date:2022-5-6 for: issues/3663 sqlInjection problem
 			QueryWrapper<SysCategory> query = new QueryWrapper<SysCategory>();
 			query.eq(field, val);
 			List<SysCategory> ls = this.sysCategoryService.list(query);
 			if(ls==null || ls.size()==0) {
 				result.setMessage("Query无果");
 	 			result.setSuccess(false);
 			}else if(ls.size()>1) {
 				result.setMessage("Query数据异常,["+field+"]Multiple values ​​exist:"+val);
 	 			result.setSuccess(false);
 			}else {
 				result.setSuccess(true);
 				result.setResult(ls.get(0));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			result.setMessage(e.getMessage());
 			result.setSuccess(false);
 		}
 		return result;
 	}
   
    /**
          * Load node's child data
     */
    @RequestMapping(value = "/loadTreeChildren", method = RequestMethod.GET)
	public Result<List<TreeSelectModel>> loadTreeChildren(@RequestParam(name="pid") String pid) {
		Result<List<TreeSelectModel>> result = new Result<List<TreeSelectModel>>();
		try {
			List<TreeSelectModel> ls = this.sysCategoryService.queryListByPid(pid);
			result.setResult(ls);
			result.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage(e.getMessage());
			result.setSuccess(false);
		}
		return result;
	}
    
    /**
         * Load first level node/If it is synchronized then all data
     */
    @RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
   	public Result<List<TreeSelectModel>> loadTreeRoot(@RequestParam(name="async") Boolean async,@RequestParam(name="pcode") String pcode) {
   		Result<List<TreeSelectModel>> result = new Result<List<TreeSelectModel>>();
   		try {
   			List<TreeSelectModel> ls = this.sysCategoryService.queryListByCode(pcode);
   			if(!async) {
   				loadAllCategoryChildren(ls);
   			}
   			result.setResult(ls);
   			result.setSuccess(true);
   		} catch (Exception e) {
   			e.printStackTrace();
   			result.setMessage(e.getMessage());
   			result.setSuccess(false);
   		}
   		return result;
   	}
  
    /**
         * Find child nodes recursively Used for synchronous loading
     */
  	private void loadAllCategoryChildren(List<TreeSelectModel> ls) {
  		for (TreeSelectModel tsm : ls) {
			List<TreeSelectModel> temp = this.sysCategoryService.queryListByPid(tsm.getKey());
			if(temp!=null && temp.size()>0) {
				tsm.setChildren(temp);
				loadAllCategoryChildren(temp);
			}
		}
  	}

	 /**
	  * Check code
	  * @param pid
	  * @param code
	  * @return
	  */
	 @GetMapping(value = "/checkCode")
	 public Result<?> checkCode(@RequestParam(name="pid",required = false) String pid,@RequestParam(name="code",required = false) String code) {
		if(oConvertUtils.isEmpty(code)){
			return Result.error("mistake,Type encoding is empty!");
		}
		if(oConvertUtils.isEmpty(pid)){
			return Result.ok();
		}
		SysCategory parent = this.sysCategoryService.getById(pid);
		if(code.startsWith(parent.getCode())){
			return Result.ok();
		}else{
			return Result.error("Coding does not comply with specifications,Must be\""+parent.getCode()+"\"beginning!");
		}

	 }


	 /**
	  * Classification dictionary树控件 Load node
	  * @param pid
	  * @param pcode
	  * @param condition
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeData", method = RequestMethod.GET)
	 public Result<List<TreeSelectModel>> loadDict(@RequestParam(name="pid",required = false) String pid,@RequestParam(name="pcode",required = false) String pcode, @RequestParam(name="condition",required = false) String condition) {
		 Result<List<TreeSelectModel>> result = new Result<List<TreeSelectModel>>();
		 //pidIf a value is passed Just ignorepcoderole
		 if(oConvertUtils.isEmpty(pid)){
		 	if(oConvertUtils.isEmpty(pcode)){
				result.setSuccess(false);
				result.setMessage("加载Classification dictionary树Wrong parameter.[null]!");
				return result;
			}else{
		 		if(ISysCategoryService.ROOT_PID_VALUE.equals(pcode)){
					pid = ISysCategoryService.ROOT_PID_VALUE;
				}else{
					pid = this.sysCategoryService.queryIdByCode(pcode);
				}
				if(oConvertUtils.isEmpty(pid)){
					result.setSuccess(false);
					result.setMessage("加载Classification dictionary树Wrong parameter.[code]!");
					return result;
				}
			}
		 }
		 Map<String, String> query = null;
		 if(oConvertUtils.isNotEmpty(condition)) {
			 query = JSON.parseObject(condition, Map.class);
		 }
		 List<TreeSelectModel> ls = sysCategoryService.queryListByPid(pid,query);
		 result.setSuccess(true);
		 result.setResult(ls);
		 return result;
	 }

	 /**
	  * Classification dictionary控件数据回显[form page]
	  *
	  * @param ids
	  * @param delNotExist Whether to remove non-existing items，Default istrue，set tofalseIf akeydoes not exist in database，then return directlykeyitself
	  * @return
	  */
	 @RequestMapping(value = "/loadDictItem", method = RequestMethod.GET)
	 public Result<List<String>> loadDictItem(@RequestParam(name = "ids") String ids, @RequestParam(name = "delNotExist", required = false, defaultValue = "true") boolean delNotExist) {
		 Result<List<String>> result = new Result<>();
		 // non-empty judgment
		 if (StringUtils.isBlank(ids)) {
			 result.setSuccess(false);
			 result.setMessage("ids cannot be empty");
			 return result;
		 }
		 // Query数据
		 List<String> textList = sysCategoryService.loadDictItem(ids, delNotExist);
		 result.setSuccess(true);
		 result.setResult(textList);
		 return result;
	 }

	 /**
	  * [List page]加载Classification dictionary数据 for value substitution
	  * @param code
	  * @return
	  */
	 @RequestMapping(value = "/loadAllData", method = RequestMethod.GET)
	 public Result<List<DictModel>> loadAllData(@RequestParam(name="code",required = true) String code) {
		 Result<List<DictModel>> result = new Result<List<DictModel>>();
		 LambdaQueryWrapper<SysCategory> query = new LambdaQueryWrapper<SysCategory>();
		 if(oConvertUtils.isNotEmpty(code) && !CATEGORY_ROOT_CODE.equals(code)){
			 query.likeRight(SysCategory::getCode,code);
		 }
		 List<SysCategory> list = this.sysCategoryService.list(query);
		 if(list==null || list.size()==0) {
			 result.setMessage("No data,Wrong parameter.[code]");
			 result.setSuccess(false);
			 return result;
		 }
		 List<DictModel> rdList = new ArrayList<DictModel>();
		 for (SysCategory c : list) {
			 rdList.add(new DictModel(c.getId(),c.getName()));
		 }
		 result.setSuccess(true);
		 result.setResult(rdList);
		 return result;
	 }

	 /**
	  * According to parentid批量Query子节点
	  * @param parentIds
	  * @return
	  */
	 @GetMapping("/getChildListBatch")
	 public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
		 try {
			 QueryWrapper<SysCategory> queryWrapper = new QueryWrapper<>();
			 List<String> parentIdList = Arrays.asList(parentIds.split(","));
			 queryWrapper.in("pid", parentIdList);
			 List<SysCategory> list = sysCategoryService.list(queryWrapper);
			 IPage<SysCategory> pageList = new Page<>(1, 10, list.size());
			 pageList.setRecords(list);
			 return Result.OK(pageList);
		 } catch (Exception e) {
			 log.error(e.getMessage(), e);
			 return Result.error("批量Query子节点失败：" + e.getMessage());
		 }
	 }


}
