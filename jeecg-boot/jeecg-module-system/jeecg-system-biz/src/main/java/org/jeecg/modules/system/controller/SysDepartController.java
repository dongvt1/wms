package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.excelstyle.ExcelExportSysUserStyle;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.vo.SysDepartExportVo;
import org.jeecg.modules.system.vo.SysPositionSelectTreeVo;
import org.jeecg.modules.system.vo.lowapp.ExportDepartVo;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * Department table front controller
 * <p>
 * 
 * @Author: Steve @Since： 2019-01-22
 */
@RestController
@RequestMapping("/sys/sysDepart")
@Slf4j
public class SysDepartController {

	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	public RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysUserDepartService sysUserDepartService;
	@Autowired
	private RedisUtil redisUtil;
	/**
	 * Query data Find out my department,And respond to the front end in tree structure data format
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryMyDeptTreeList", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryMyDeptTreeList() {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			if(oConvertUtils.isNotEmpty(user.getUserIdentity()) && user.getUserIdentity().equals( CommonConstant.USER_IDENTITY_2 )){
				//update-begin--Author:liusq  Date:20210624  for:Department inquiryidsFront-end display problem after being empty issues/I3UD06
				String departIds = user.getDepartIds();
				if(StringUtils.isNotBlank(departIds)){
					List<SysDepartTreeModel> list = sysDepartService.queryMyDeptTreeList(departIds);
					result.setResult(list);
				}
				//update-end--Author:liusq  Date:20210624  for:Department inquiryidsFront-end display problem after being empty issues/I3UD06
				result.setMessage(CommonConstant.USER_IDENTITY_2.toString());
				result.setSuccess(true);
			}else{
				result.setMessage(CommonConstant.USER_IDENTITY_1.toString());
				result.setSuccess(true);
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * Query data Find all departments,And respond to the front end in tree structure data format
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryTreeList(@RequestParam(name = "ids", required = false) String ids) {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		try {
			// read from memory
//			List<SysDepartTreeModel> list =FindsDepartsChildrenUtil.getSysDepartTreeList();
//			if (CollectionUtils.isEmpty(list)) {
//				list = sysDepartService.queryTreeList();
//			}
			if(oConvertUtils.isNotEmpty(ids)){
				List<SysDepartTreeModel> departList = sysDepartService.queryTreeList(ids);
				result.setResult(departList);
			}else{
				List<SysDepartTreeModel> list = sysDepartService.queryTreeList();
				result.setResult(list);
			}
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * Asynchronous query departmentlist
	 * @param parentId parent node Passed when loading asynchronously
	 * @param ids The front-end echo is passed
	 * @param primaryKey primary key field（idororgCode）
	 * @return
	 */
	@RequestMapping(value = "/queryDepartTreeSync", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryDepartTreeSync(@RequestParam(name = "pid", required = false) String parentId,@RequestParam(name = "ids", required = false) String ids, @RequestParam(name = "primaryKey", required = false) String primaryKey) {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		try {
			List<SysDepartTreeModel> list = sysDepartService.queryTreeListByPid(parentId,ids, primaryKey);
			result.setResult(list);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.setSuccess(false);
			result.setMessage("Query failed");
		}
		return result;
	}

    /**
     * Asynchronous query departmentand岗位list
     * @param parentId parent node Passed when loading asynchronously
     * @param ids The front-end echo is passed
     * @param primaryKey primary key field（idororgCode）
     * @return
     */
    @RequestMapping(value = "/queryDepartAndPostTreeSync", method = RequestMethod.GET)
    public Result<List<SysDepartTreeModel>> queryDepartAndPostTreeSync(@RequestParam(name = "pid", required = false) String parentId,
																	   @RequestParam(name = "ids", required = false) String ids,
																	   @RequestParam(name = "primaryKey", required = false) String primaryKey,
                                                                       @RequestParam(name = "departIds", required = false) String departIds,
																	   @RequestParam(name = "name", required = false) String orgName) {
        Result<List<SysDepartTreeModel>> result = new Result<>();
        try {
            List<SysDepartTreeModel> list = sysDepartService.queryDepartAndPostTreeSync(parentId,ids, primaryKey, departIds, orgName);
            result.setResult(list);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            result.setSuccess(false);
            result.setMessage("Query failed");
        }
        return result;
    }

	/**
	 * Get all parent departments of a departmentID
	 *
	 * @param departId according todepartIdcheck
	 * @param orgCode  according toorgCodecheck，departIdandorgCodeThere must be one that is not empty
	 */
	@GetMapping("/queryAllParentId")
	public Result queryParentIds(
			@RequestParam(name = "departId", required = false) String departId,
			@RequestParam(name = "orgCode", required = false) String orgCode) {
		try {
			JSONObject data;
			if (oConvertUtils.isNotEmpty(departId)) {
				data = sysDepartService.queryAllParentIdByDepartId(departId);
			} else if (oConvertUtils.isNotEmpty(orgCode)) {
				data = sysDepartService.queryAllParentIdByOrgCode(orgCode);
			} else {
				return Result.error("departId and orgCode Cannot all be empty！");
			}
			return Result.OK(data);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error(e.getMessage());
		}
	}

	/**
	 * Add new data Add user-created department object data,and save to database
	 * 
	 * @param sysDepart
	 * @return
	 */
    @RequiresPermissions("system:depart:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<SysDepart> add(@RequestBody SysDepart sysDepart, HttpServletRequest request) {
		Result<SysDepart> result = new Result<SysDepart>();
		String username = JwtUtil.getUserNameByToken(request);
		try {
			sysDepart.setCreateBy(username);
			sysDepartService.saveDepartData(sysDepart, username);
			//Clear department tree memory
			// FindsDepartsChildrenUtil.clearSysDepartTreeList();
			// FindsDepartsChildrenUtil.clearDepartIdModel();
			result.success("Added successfully！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Operation failed");
		}
		return result;
	}

	/**
	 * Edit data Edit some department data,and save to database
	 * 
	 * @param sysDepart
	 * @return
	 */
    @RequiresPermissions("system:depart:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<SysDepart> edit(@RequestBody SysDepart sysDepart, HttpServletRequest request) {
		String username = JwtUtil.getUserNameByToken(request);
		sysDepart.setUpdateBy(username);
		Result<SysDepart> result = new Result<SysDepart>();
		SysDepart sysDepartEntity = sysDepartService.getById(sysDepart.getId());
		if (sysDepartEntity == null) {
			result.error500("No corresponding entity found");
		} else {
			boolean ok = sysDepartService.updateDepartDataById(sysDepart, username);
			// TODO returnfalseWhat does it mean？
			if (ok) {
				//Clear department tree memory
				//FindsDepartsChildrenUtil.clearSysDepartTreeList();
				//FindsDepartsChildrenUtil.clearDepartIdModel();
				result.success("Modification successful!");
			}
		}
		return result;
	}
	
	 /**
     *   passiddelete
    * @param id
    * @return
    */
    @RequiresPermissions("system:depart:delete")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
   public Result<SysDepart> delete(@RequestParam(name="id",required=true) String id) {

       Result<SysDepart> result = new Result<SysDepart>();
       SysDepart sysDepart = sysDepartService.getById(id);
       if(sysDepart==null) {
           result.error500("No corresponding entity found");
       }else {
           sysDepartService.deleteDepart(id);
			//Clear department tree memory
		   //FindsDepartsChildrenUtil.clearSysDepartTreeList();
		   // FindsDepartsChildrenUtil.clearDepartIdModel();
		   result.success("delete成功!");
       }
       return result;
   }


	/**
	 * 批量delete according to前端请求的多个ID,对数据库执行delete相关部门数据的操作
	 * 
	 * @param ids
	 * @return
	 */
    @RequiresPermissions("system:depart:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<SysDepart> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {

		Result<SysDepart> result = new Result<SysDepart>();
		if (ids == null || "".equals(ids.trim())) {
			result.error500("Parameter not recognized！");
		} else {
			this.sysDepartService.deleteBatchWithChildren(Arrays.asList(ids.split(",")));
			result.success("delete成功!");
		}
		return result;
	}

	/**
	 * Query data Add or edit the page to initiate a request for this method,Load the names of all departments in a tree structure,User-friendly operation
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryIdTree", method = RequestMethod.GET)
	public Result<List<DepartIdModel>> queryIdTree() {
//		Result<List<DepartIdModel>> result = new Result<List<DepartIdModel>>();
//		List<DepartIdModel> idList;
//		try {
//			idList = FindsDepartsChildrenUtil.wrapDepartIdModel();
//			if (idList != null && idList.size() > 0) {
//				result.setResult(idList);
//				result.setSuccess(true);
//			} else {
//				sysDepartService.queryTreeList();
//				idList = FindsDepartsChildrenUtil.wrapDepartIdModel();
//				result.setResult(idList);
//				result.setSuccess(true);
//			}
//			return result;
//		} catch (Exception e) {
//			log.error(e.getMessage(),e);
//			result.setSuccess(false);
//			return result;
//		}
		Result<List<DepartIdModel>> result = new Result<>();
		try {
			List<DepartIdModel> list = sysDepartService.queryDepartIdTreeList();
			result.setResult(list);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}
	 
	/**
	 * <p>
	 * Department search function method,according to关键字模糊搜索相关部门
	 * </p>
	 * 
	 * @param keyWord
	 * @return
	 */
	@RequestMapping(value = "/searchBy", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> searchBy(@RequestParam(name = "keyWord", required = true) String keyWord,
                                                     @RequestParam(name = "myDeptSearch", required = false) String myDeptSearch,
                                                     @RequestParam(name = "orgCategory", required = false) String orgCategory,
                                                     @RequestParam(name = "departIds", required = false) String depIds) {
		Result<List<SysDepartTreeModel>> result = new Result<List<SysDepartTreeModel>>();
		//Department inquiry，myDeptSearchfor1时for我的Department inquiry，登录用户for上级时check只check负责部门下数据
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String departIds = null;
		if(oConvertUtils.isNotEmpty(user.getUserIdentity()) && user.getUserIdentity().equals( CommonConstant.USER_IDENTITY_2 )){
			departIds = user.getDepartIds();
		}
		List<SysDepartTreeModel> treeList = this.sysDepartService.searchByKeyWord(keyWord,myDeptSearch,departIds,orgCategory,depIds);
		if (treeList == null || treeList.size() == 0) {
			result.setSuccess(false);
			result.setMessage("未check询匹配数据！");
			return result;
		}
		result.setResult(treeList);
		return result;
	}


	/**
     * Exportexcel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysDepart sysDepart,HttpServletRequest request) {
		//------------------------------------------------------------------------------------------------
		//Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysDepart.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		
		//update-begin---author:wangshuai---date:2023-10-19---for:【QQYUN-5482】系统的Department importExport也可以改成敲敲云模式的部门路径---
		//// Step.1 组装check询条件
		//QueryWrapper<SysDepart> queryWrapper = QueryGenerator.initQueryWrapper(sysDepart, request.getParameterMap());
		//Step.1 AutoPoi ExportExcel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        //List<SysDepart> pageList = sysDepartService.list(queryWrapper);
        //Sort dictionary
        //Collections.sort(pageList, new Comparator<SysDepart>() {
            //@Override
			//public int compare(SysDepart arg0, SysDepart arg1) {
				//return arg0.getOrgCode().compareTo(arg1.getOrgCode());
			//}
		//});
		// Filter selected data
		String selections = request.getParameter("selections");
		List<String> idList = new ArrayList<>();
		if (oConvertUtils.isNotEmpty(selections)) {
			idList = Arrays.asList(selections.split(","));
		}
		//step.2 组装Export数据
		Integer tenantId = sysDepart == null ? null : sysDepart.getTenantId();
		//update-begin---author:wangshuai---date:2024-07-05---for:【TV360X-1671】部门管理不支持选中的记录Export---
		List<SysDepartExportVo> sysDepartExportVos = sysDepartService.getExportDepart(tenantId,idList);
		//update-end---author:wangshuai---date:2024-07-05---for:【TV360X-1671】部门管理不支持选中的记录Export---
        //Export文件名称
        mv.addObject(NormalExcelConstants.FILE_NAME, "Department list");
        mv.addObject(NormalExcelConstants.CLASS, SysDepartExportVo.class);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ExportParams exportParams = new ExportParams("Import rules：\n" +
                "1、标题for第三行，部门路径andDepartment name的标题不允许修改，Otherwise, the match will fail；第四行for数据填写范围;\n" +
                "2、Department paths use English characters/segmentation，Department namefor部门路径的最后一位;\n" +
                "3、Departments are created starting from the first-level name，If there are siblings, you need to add one more line，Such as R&D department/R&D Department;R&D Department/R&D 2;\n" +
                "4、Custom department codes need to meet rules before they can be imported。如一级部门编码forA01,那么子部门forA01A01,同级子部门forA01A02,编码固定for三位，首字母forA-Z,后两位for数字0-99，Increasingly;", "Export人:" + user.getRealname(), "Export信息");
        exportParams.setTitleHeight((short)70);
        exportParams.setStyle(ExcelExportSysUserStyle.class);
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, sysDepartExportVos);
		//update-end---author:wangshuai---date:2023-10-19---for:【QQYUN-5482】系统的Department importExport也可以改成敲敲云模式的部门路径---
        
		return mv;
    }

    /**
     * passexcelImport data
	 * Department introduction plan1: pass机构编码来计算出部门的父级ID,Maintain superior-subordinate relationship;
	 * Department introduction plan2: You can also modify the program,Directly import institution codes,Do not set the parent yetID;After importing all,write asql,Make up for the fatherID;
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("system:depart:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<String> errorMessageList = new ArrayList<>();
		//List<SysDepart> listSysDeparts = null;
		List<SysDepartExportVo> listSysDeparts = null;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Get the uploaded file object
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
				//update-begin---author:wangshuai---date:2023-10-20---for: Comment out the original import department logic---
//            	// orgCodeCode length
//            	int codeLength = YouBianCodeUtil.ZHANWEI_LENGTH;
//                listSysDeparts = ExcelImportUtil.importExcel(file.getInputStream(), SysDepart.class, params);
//                //Sort by length
//                Collections.sort(listSysDeparts, new Comparator<SysDepart>() {
//                    @Override
//					public int compare(SysDepart arg0, SysDepart arg1) {
//                    	return arg0.getOrgCode().length() - arg1.getOrgCode().length();
//                    }
//                });
//
//                int num = 0;
//                for (SysDepart sysDepart : listSysDeparts) {
//                	String orgCode = sysDepart.getOrgCode();
//                	if(orgCode.length() > codeLength) {
//                		String parentCode = orgCode.substring(0, orgCode.length()-codeLength);
//                		QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<SysDepart>();
//                		queryWrapper.eq("org_code", parentCode);
//                		try {
//                		SysDepart parentDept = sysDepartService.getOne(queryWrapper);
//                		if(!parentDept.equals(null)) {
//							sysDepart.setParentId(parentDept.getId());
//							//Update parent department not leaf node
//							sysDepartService.updateIzLeaf(parentDept.getId(),CommonConstant.NOT_LEAF);
//						} else {
//							sysDepart.setParentId("");
//						}
//                		}catch (Exception e) {
//                			//没有check找到parentDept
//                		}
//                	}else{
//                		sysDepart.setParentId("");
//					}
//                    //update-begin---author:liusq   Date:20210223  for：After importing departments in batches，Cannot add lower-level departments #2245------------
//					sysDepart.setOrgType(sysDepart.getOrgCode().length()/codeLength+"");
//                    //update-end---author:liusq   Date:20210223  for：After importing departments in batches，Cannot add lower-level departments #2245------------
//					sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
//                    //update-begin---author:wangshuai ---date:20220105  for：[JTC-363]Department import Import fails when the institution category does not exist，Assign default value------------
//					if(oConvertUtils.isEmpty(sysDepart.getOrgCategory())){
//					    sysDepart.setOrgCategory("1");
//                    }
//                    //update-end---author:wangshuai ---date:20220105  for：[JTC-363]Department import Import fails when the institution category does not exist，Assign default value------------
//					ImportExcelUtil.importDateSaveOne(sysDepart, ISysDepartService.class, errorMessageList, num, CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE);
//					num++;
//                }
				//update-end---author:wangshuai---date:2023-10-20---for: Comment out the original import department logic---
				
				//update-begin---author:wangshuai---date:2023-10-19---for:【QQYUN-5482】系统的Department importExport也可以改成敲敲云模式的部门路径---
				listSysDeparts = ExcelImportUtil.importExcel(file.getInputStream(), SysDepartExportVo.class, params);
				sysDepartService.importSysDepart(listSysDeparts,errorMessageList);
				//update-end---author:wangshuai---date:2023-10-19---for:【QQYUN-5482】系统的Department importExport也可以改成敲敲云模式的部门路径---
				
				//Clear department cache
				List<String> keys3 = redisUtil.scan(CacheConstant.SYS_DEPARTS_CACHE + "*");
				List<String> keys4 = redisUtil.scan(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
				redisTemplate.delete(keys3);
				redisTemplate.delete(keys4);
				return ImportExcelUtil.imporReturnRes(errorMessageList.size(), listSysDeparts.size() - errorMessageList.size(), errorMessageList);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                return Result.error("File import failed:"+e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.error("File import failed！");
    }


	/**
	 * check询所有部门信息
	 * @return
	 */
	@GetMapping("listAll")
	public Result<List<SysDepart>> listAll(@RequestParam(name = "id", required = false) String id) {
		Result<List<SysDepart>> result = new Result<>();
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.orderByAsc(SysDepart::getOrgCode);
		if(oConvertUtils.isNotEmpty(id)){
			String[] arr = id.split(",");
			query.in(SysDepart::getId,arr);
		}
		List<SysDepart> ls = this.sysDepartService.list(query);
		result.setSuccess(true);
		result.setResult(ls);
		return result;
	}
	/**
	 * Query data Find all departments,And respond to the front end in tree structure data format
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryTreeByKeyWord", method = RequestMethod.GET)
	public Result<Map<String,Object>> queryTreeByKeyWord(@RequestParam(name = "keyWord", required = false) String keyWord) {
		Result<Map<String,Object>> result = new Result<>();
		try {
			Map<String,Object> map=new HashMap(5);
			List<SysDepartTreeModel> list = sysDepartService.queryTreeByKeyWord(keyWord);
			//according tokeyWordGet user information
			LambdaQueryWrapper<SysUser> queryUser = new LambdaQueryWrapper<SysUser>();
			queryUser.eq(SysUser::getDelFlag,CommonConstant.DEL_FLAG_0);
			queryUser.and(i -> i.like(SysUser::getUsername, keyWord).or().like(SysUser::getRealname, keyWord));
			List<SysUser> sysUsers = this.sysUserService.list(queryUser);
			map.put("userList",sysUsers);
			map.put("departList",list);
			result.setResult(map);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * according to部门编码获取部门信息
	 *
	 * @param orgCode
	 * @return
	 */
	@GetMapping("/getDepartName")
	public Result<SysDepart> getDepartName(@RequestParam(name = "orgCode") String orgCode) {
		Result<SysDepart> result = new Result<>();
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
		query.eq(SysDepart::getOrgCode, orgCode);
		SysDepart sysDepart = sysDepartService.getOne(query);
		result.setSuccess(true);
		result.setResult(sysDepart);
		return result;
	}

	/**
	 * according to部门idGet user information
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/getUsersByDepartId")
	public Result<List<SysUser>> getUsersByDepartId(@RequestParam(name = "id") String id) {
		Result<List<SysUser>> result = new Result<>();
		List<SysUser> sysUsers = sysUserDepartService.queryUserByDepId(id);
		result.setSuccess(true);
		result.setResult(sysUsers);
		return result;
	}

	/**
	 * @Function：according toid 批量check询
	 * @param deptIds
	 * @return
	 */
	@RequestMapping(value = "/queryByIds", method = RequestMethod.GET)
	public Result<Collection<SysDepart>> queryByIds(@RequestParam(name = "deptIds") String deptIds) {
		Result<Collection<SysDepart>> result = new Result<>();
		String[] ids = deptIds.split(",");
		Collection<String> idList = Arrays.asList(ids);
		Collection<SysDepart> deptList = sysDepartService.listByIds(idList);
		// Set department path name
		for (SysDepart depart : deptList) {
			String departPathName = sysDepartService.getDepartPathNameByOrgCode(depart.getOrgCode(),null);
			depart.setDepartPathName(departPathName);
		}
		result.setSuccess(true);
		result.setResult(deptList);
		return result;
	}

	@GetMapping("/getMyDepartList")
    public Result<List<SysDepart>> getMyDepartList(){
        List<SysDepart> list = sysDepartService.getMyDepartList();
        return Result.ok(list);
    }

	/**
	 * Asynchronous query departmentlist
	 * @param parentId parent node Passed when loading asynchronously
	 * @return
	 */
	@RequestMapping(value = "/queryBookDepTreeSync", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryBookDepTreeSync(@RequestParam(name = "pid", required = false) String parentId,
																 @RequestParam(name = "tenantId") Integer tenantId,
																 @RequestParam(name = "departName",required = false) String departName) {
		Result<List<SysDepartTreeModel>> result = new Result<>();
		try {
			List<SysDepartTreeModel> list = sysDepartService.queryBookDepTreeSync(parentId, tenantId, departName);
			result.setResult(list);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}

	/**
	 * pass部门idand租户idGet user 【low code applications: Used to select department heads】
	 * @param departId
	 * @return
	 */
	@GetMapping("/getUsersByDepartTenantId")
	public Result<List<SysUser>> getUsersByDepartTenantId(@RequestParam("departId") String departId){
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		List<SysUser> sysUserList = sysUserDepartService.getUsersByDepartTenantId(departId,tenantId);
		return Result.ok(sysUserList);
	}

	/**
	 * Exportexcel【low code applications: 用于Export部门】
	 *
	 * @param request
	 */
	@RequestMapping(value = "/appExportXls")
	public ModelAndView appExportXls(SysDepart sysDepart,HttpServletRequest request) {
		// Step.1 组装check询条件
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<ExportDepartVo> pageList = sysDepartService.getExcelDepart(tenantId);
		//Step.2 AutoPoi ExportExcel
		//Export文件名称
		mv.addObject(NormalExcelConstants.FILE_NAME, "Department list");
		mv.addObject(NormalExcelConstants.CLASS, ExportDepartVo.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Department list数据", "Export人:"+user.getRealname(), "Export信息"));
		mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
		return mv;
	}

	/**
	 * importexcel【low code applications: 用于Export部门】
	 *
	 * @param request
	 */
	@RequestMapping(value = "/appImportExcel", method = RequestMethod.POST)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<?> appImportExcel(HttpServletRequest request, HttpServletResponse response) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		List<String> errorMessageList = new ArrayList<>();
		List<ExportDepartVo> listSysDeparts = null;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			// Get the uploaded file object
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				listSysDeparts = ExcelImportUtil.importExcel(file.getInputStream(), ExportDepartVo.class, params);
				sysDepartService.importExcel(listSysDeparts,errorMessageList);
				//Clear department cache
				List<String> keys3 = redisUtil.scan(CacheConstant.SYS_DEPARTS_CACHE + "*");
				List<String> keys4 = redisUtil.scan(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
				redisTemplate.delete(keys3);
				redisTemplate.delete(keys4);
				return ImportExcelUtil.imporReturnRes(errorMessageList.size(), listSysDeparts.size() - errorMessageList.size(), errorMessageList);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				return Result.error("File import failed:"+e.getMessage());
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Result.error("File import failed！");
	}

    /**
     * according to部门idand职级idGet job information
     */
    @GetMapping("/getPositionByDepartId")
	public Result<List<SysPositionSelectTreeVo>> getPositionByDepartId(@RequestParam(name = "parentId") String parentId,
                                                                       @RequestParam(name = "departId",required = false) String departId,
                                                                       @RequestParam(name = "positionId") String positionId){
        List<SysPositionSelectTreeVo> positionByDepartId = sysDepartService.getPositionByDepartId(parentId, departId, positionId);
        return Result.OK(positionByDepartId);
    }

    /**
     * Get rank relationship
     * @param departId
     * @return
     */
    @GetMapping("/getRankRelation")
    public Result<List<SysPositionSelectTreeVo>> getRankRelation(@RequestParam(name = "departId") String departId){
        List<SysPositionSelectTreeVo> list = sysDepartService.getRankRelation(departId);
        return Result.ok(list);
    }

    /**
     * according to部门code获取当前and上级Department name
     *
     * @param orgCode
     * @param depId
     * @return String Department name
     */
    @GetMapping("/getDepartPathNameByOrgCode")
    public Result<String> getDepartPathNameByOrgCode(@RequestParam(name = "orgCode", required = false) String orgCode,
                                                 @RequestParam(name = "depId", required = false) String depId) {
        String departName = sysDepartService.getDepartPathNameByOrgCode(orgCode, depId);
        return Result.OK(departName);
    }
}
