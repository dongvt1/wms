package org.jeecg.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.FillRuleConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.DepartCategoryEnum;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.util.FindsDepartsChildrenUtil;
import org.jeecg.modules.system.vo.SysDepartExportVo;
import org.jeecg.modules.system.vo.SysDepartPositionVo;
import org.jeecg.modules.system.vo.SysPositionSelectTreeVo;
import org.jeecg.modules.system.vo.lowapp.ExportDepartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>
 * Department table Service implementation class
 * <p>
 * 
 * @Author Steve
 * @Since 2019-01-22
 */
@Service
public class SysDepartServiceImpl extends ServiceImpl<SysDepartMapper, SysDepart> implements ISysDepartService {

	@Autowired
	private SysUserDepartMapper userDepartMapper;
	@Autowired
	private SysDepartRoleMapper sysDepartRoleMapper;
	@Autowired
	private SysDepartPermissionMapper departPermissionMapper;
	@Autowired
	private SysDepartRolePermissionMapper departRolePermissionMapper;
	@Autowired
	private SysDepartRoleUserMapper departRoleUserMapper;
	@Autowired
	private SysUserMapper sysUserMapper;
	@Autowired
	private SysDepartMapper departMapper;
    @Autowired
    private SysPositionMapper sysPositionMapper;
    @Autowired
    private RedisUtil redisUtil;
    
	@Override
	public List<SysDepartTreeModel> queryMyDeptTreeList(String departIds) {
		//According to departmentidGet the responsible department
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		String[] codeArr = this.getMyDeptParentOrgCode(departIds);
		//update-begin---author:wangshuai---date:2023-12-01---for:【QQYUN-7320】There is no data in the query department，resulting in a null pointer error---
		if(ArrayUtil.isEmpty(codeArr)){
			return null;
		}
		//update-end---author:wangshuai---date:2023-12-01---for:【QQYUN-7320】There is no data in the query department，resulting in a null pointer error---
		for(int i=0;i<codeArr.length;i++){
			query.or().likeRight(SysDepart::getOrgCode,codeArr[i]);
		}
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        query.ne(SysDepart::getOrgCategory,DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
		//------------------------------------------------------------------------------------------------
		//Whether to enable the system management module SASS control
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		
		query.orderByAsc(SysDepart::getDepartOrder);
		//the parent nodeParentIdset tonull
		List<SysDepart> listDepts = this.list(query);
		for(int i=0;i<codeArr.length;i++){
			for(SysDepart dept : listDepts){
				if(dept.getOrgCode().equals(codeArr[i])){
					dept.setParentId(null);
				}
			}
		}
		// callwrapTreeDataToTreeListMethod to generate tree data
		List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(listDepts);
		return listResult;
	}

	/**
	 * queryTreeList correspond queryTreeList Query all department data,Respond to the front end in the form of a tree structure
	 */
	@Override
	//@Cacheable(value = CacheConstant.SYS_DEPARTS_CACHE)
	public List<SysDepartTreeModel> queryTreeList() {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		//------------------------------------------------------------------------------------------------
		//Whether to enable the system management module多tenant数据隔离【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}   
		//------------------------------------------------------------------------------------------------
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        //update-begin---author:wangshuai---date:2025-08-18---for:【QQYUN-13427】Department selection component modification:Need to filter out positions only keep company 子company department---
        query.ne(SysDepart::getOrgCategory, DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
        //update-end---author:wangshuai---date:2025-08-18---for:【QQYUN-13427】Department selection component modification:Need to filter out positions only keep company 子company department---
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = this.list(query);
        //update-begin---author:wangshuai ---date:20220307  for：[JTC-119]在department管理菜单下设置department负责人 No processing is required when creating a user
		//Set up userid,Let the front desk display
        this.setUserIdsByDepList(list);
        //update-begin---author:wangshuai ---date:20220307  for：[JTC-119]在department管理菜单下设置department负责人 No processing is required when creating a user
		// callwrapTreeDataToTreeListMethod to generate tree data
		List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(list);
		return listResult;
	}

	/**
	 * queryTreeList According to departmentidQuery,前端回显call
	 */
	@Override
	public List<SysDepartTreeModel> queryTreeList(String ids) {
		List<SysDepartTreeModel> listResult=new ArrayList<>();
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        //update-begin---author:wangshuai---date:2025-08-18---for:【QQYUN-13427】Department selection component modification:Need to filter out positions only keep company 子company department---
         query.ne(SysDepart::getOrgCategory,DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
        //update-end---author:wangshuai---date:2025-08-18---for:【QQYUN-13427】Department selection component modification:Need to filter out positions only keep company 子company department---
		if(oConvertUtils.isNotEmpty(ids)){
			query.in(true,SysDepart::getId,ids.split(","));
		}
		//------------------------------------------------------------------------------------------------
		//Whether to enable the system management module多tenant数据隔离【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list= this.list(query);
		for (SysDepart depart : list) {
			listResult.add(new SysDepartTreeModel(depart));
		}
		return  listResult;

	}

	//@Cacheable(value = CacheConstant.SYS_DEPART_IDS_CACHE)
	@Override
	public List<DepartIdModel> queryDepartIdTreeList() {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		//------------------------------------------------------------------------------------------------
		//Whether to enable the system management module多tenant数据隔离【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> list = this.list(query);
		// callwrapTreeDataToTreeListMethod to generate tree data
		List<DepartIdModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToDepartIdTreeList(list);
		return listResult;
	}

	/**
	 * saveDepartData correspond add 保存user在页面添加of新ofdepartmentobject数据
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveDepartData(SysDepart sysDepart, String username) {
		if (sysDepart != null && username != null) {
			//update-begin---author:wangshuai ---date:20230216  for：[QQYUN-4163]给Department table加个yes否有子节点------------
			if (oConvertUtils.isEmpty(sysDepart.getParentId())) {
				sysDepart.setParentId("");
			}else{
				//Willfatherdepartmentof设成不yes叶子结点
				departMapper.setMainLeaf(sysDepart.getParentId(),CommonConstant.NOT_LEAF);
			}
			//update-end---author:wangshuai ---date:20230216  for：[QQYUN-4163]给Department table加个yes否有子节点------------
			//String s = UUID.randomUUID().toString().replace("-", "");
			sysDepart.setId(IdWorker.getIdStr(sysDepart));
			// First determine whether the object has a parentID,Some means not the most advanced,Otherwise it means the highest level
			// Get parentID
			String parentId = sysDepart.getParentId();
			//update-begin--Author:baihailong  Date:20191209 for：department编码规则生成器做成公use配置
			JSONObject formData = new JSONObject();
			formData.put("parentId",parentId);
			String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART,formData);
			//update-end--Author:baihailong  Date:20191209 for：department编码规则生成器做成公use配置
			sysDepart.setOrgCode(codeArray[0]);
			String orgType = codeArray[1];
			sysDepart.setOrgType(String.valueOf(orgType));
			sysDepart.setCreateTime(new Date());
			sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
			//新添加ofdepartmentyes叶子节点
			sysDepart.setIzLeaf(CommonConstant.IS_LEAF);
			// 【QQYUN-7172】Database defaults compatible
			if (oConvertUtils.isEmpty(sysDepart.getOrgCategory())) {
				if (oConvertUtils.isEmpty(sysDepart.getParentId())) {
					sysDepart.setOrgCategory("1");
				} else {
					sysDepart.setOrgCategory("2");
				}
			}
			this.save(sysDepart);
            //update-begin---author:wangshuai ---date:20220307  for：[JTC-119]在department管理菜单下设置department负责人 No processing is required when creating a user
			//新增departmentof时候新增负责department
            if(oConvertUtils.isNotEmpty(sysDepart.getDirectorUserIds())){
			    this.addDepartByUserIds(sysDepart,sysDepart.getDirectorUserIds());
            }
            //update-end---author:wangshuai ---date:20220307  for：[JTC-119]在department管理菜单下设置department负责人 No processing is required when creating a user
         }

	}
	
	/**
	 * saveDepartData ofcallmethod,生成department编码anddepartment类型（void logic）
	 * @deprecated
	 * @param parentId
	 * @return
	 */
	private String[] generateOrgCode(String parentId) {	
		//update-begin--Author:Steve  Date:20190201 for：Organizational structure added data code adjustment
				LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
				LambdaQueryWrapper<SysDepart> query1 = new LambdaQueryWrapper<SysDepart>();
				String[] strArray = new String[2];
		        // Create aListgather,存储Query返回of所有SysDepartobject
		        List<SysDepart> departList = new ArrayList<>();
				// Define new encoding string
				String newOrgCode = "";
				// Define old encoding string
				String oldOrgCode = "";
				// 定义department类型
				String orgType = "";
				// If it is the highest level,则Query出同级oforg_code, call工具类生成编码并返回
				if (StringUtil.isNullOrEmpty(parentId)) {
					// Line to determine whether the table in the database is empty,If empty, the initial code will be returned directly.
					query1.eq(SysDepart::getParentId, "").or().isNull(SysDepart::getParentId);
					query1.orderByDesc(SysDepart::getOrgCode);
					departList = this.list(query1);
					if(departList == null || departList.size() == 0) {
						strArray[0] = YouBianCodeUtil.getNextYouBianCode(null);
						strArray[1] = "1";
						return strArray;
					}else {
					SysDepart depart = departList.get(0);
					oldOrgCode = depart.getOrgCode();
					orgType = depart.getOrgType();
					newOrgCode = YouBianCodeUtil.getNextYouBianCode(oldOrgCode);
					}
				} else { // 反之则Query出所有同级ofdepartment,There are two situations after getting the results,With or without siblings
					// 封装Query同级of条件
					query.eq(SysDepart::getParentId, parentId);
					// Sort descending
					query.orderByDesc(SysDepart::getOrgCode);
					// Query出同级departmentofgather
					List<SysDepart> parentList = this.list(query);
					// Query出father级department
					SysDepart depart = this.getById(parentId);
					// Get parentdepartmentofCode
					String parentCode = depart.getOrgCode();
					// according tofather级department类型算出当前departmentof类型
					orgType = String.valueOf(Integer.valueOf(depart.getOrgType()) + 1);
					// 处理同级departmentfornullsituation
					if (parentList == null || parentList.size() == 0) {
						// 直接生成当前ofdepartment编码并返回
						newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, null);
					} else { //处理有同级departmentsituation
						// 获取同级departmentof编码,Utilize tools
						String subCode = parentList.get(0).getOrgCode();
						// 返回生成of当前department编码
						newOrgCode = YouBianCodeUtil.getSubYouBianCode(parentCode, subCode);
					}
				}
				// 返回最终封装了department编码anddepartment类型of数组
				strArray[0] = newOrgCode;
				strArray[1] = orgType;
				return strArray;
		//update-end--Author:Steve  Date:20190201 for：Organizational structure added data code adjustment
	} 

	
	/**
	 * removeDepartDataById correspond deletemethod according toID删除相关department数据
	 * 
	 */
	/*
	 * @Override
	 * 
	 * @Transactional public boolean removeDepartDataById(String id) {
	 * System.out.println("to be deletedID for=============================>>>>>"+id); boolean
	 * flag = this.removeById(id); return flag; }
	 */

	/**
	 * updateDepartDataById correspond edit According to department主键来更新correspondofdepartment数据
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateDepartDataById(SysDepart sysDepart, String username) {
		if (sysDepart != null && username != null) {
			sysDepart.setUpdateTime(new Date());
			sysDepart.setUpdateBy(username);
            //验证department类型
            this.verifyOrgCategory(sysDepart);
			this.updateById(sysDepart);
            //update-begin---author:wangshuai ---date:20220307  for：[JTC-119]在department管理菜单下设置department负责人 No processing is required when creating a user
			//修改department管理of时候，修改负责department
            this.updateChargeDepart(sysDepart);
            //update-begin---author:wangshuai ---date:20220307  for：[JTC-119]在department管理菜单下设置department负责人 No processing is required when creating a user
			return true;
		} else {
			return false;
		}

	}

    /**
     * 验证department类型
     *
     * @param sysDepart
     */
    private void verifyOrgCategory(SysDepart sysDepart) {
        //update-begin---author:wangshuai---date:2025-08-21---for: 当department类型for岗位of时候，Need to check whether there are subordinates，存在subordinate无法变更for岗位---
        //ifyes岗位situation下，No children can exist
        if (oConvertUtils.isNotEmpty(sysDepart.getOrgCategory()) && DepartCategoryEnum.DEPART_CATEGORY_POST.getValue().equals(sysDepart.getOrgCategory())) {
            long count = this.count(new QueryWrapper<SysDepart>().lambda().eq(SysDepart::getParentId, sysDepart.getId()));
            if (count > 0) {
                throw new JeecgBootBizTipException("当前子company/department下存在子级，无法变更for岗位!");
            }
        }
        //ifyes子companysituation下，则Superior不能fordepartmentor岗位
        if (oConvertUtils.isNotEmpty(sysDepart.getOrgCategory()) && DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue().equals(sysDepart.getOrgCategory())
                && oConvertUtils.isNotEmpty(sysDepart.getParentId())) {
            LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysDepart::getId, sysDepart.getParentId());
            queryWrapper.in(SysDepart::getOrgCategory, DepartCategoryEnum.DEPART_CATEGORY_POST.getValue(), DepartCategoryEnum.DEPART_CATEGORY_DEPART.getValue());
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new JeecgBootBizTipException("当前father级fordepartment或岗位，无法变更for子company!");
            }
        }
        //ifyesdepartmentsituation下，subordinate不能for子companyorcompany
        if (oConvertUtils.isNotEmpty(sysDepart.getOrgCategory()) && DepartCategoryEnum.DEPART_CATEGORY_DEPART.getValue().equals(sysDepart.getOrgCategory())) {
            LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysDepart::getParentId, sysDepart.getId());
            queryWrapper.in(SysDepart::getOrgCategory, DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue(), DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue());
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new JeecgBootBizTipException("当前子级存在子company，无法变更fordepartment!");
            }
        }
        //update-end---author:wangshuai---date:2025-08-21---for: 当department类型for岗位of时候，Need to check whether there are subordinates，存在subordinate无法变更for岗位---
    }
    
    @Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteBatchWithChildren(List<String> ids) {
		//Store childrenid
		List<String> idList = new ArrayList<String>();
		//Store the parent'sid
		List<String> parentIdList = new ArrayList<>();
		for(String id: ids) {
			idList.add(id);
			//此步骤yesfor了删除子级
			this.checkChildrenExists(id, idList);
			//update-begin---author:wangshuai ---date:20230712  for：【QQYUN-5757】批量删除department时未正确置for叶子节点 ------------
			SysDepart depart = this.getDepartById(id);
			if (oConvertUtils.isNotEmpty(depart.getParentId())) {
				if (!parentIdList.contains(depart.getParentId())) {
					parentIdList.add(depart.getParentId());
				}
			}
			//update-end---author:wangshuai ---date:20230712  for：【QQYUN-5757】批量删除department时未正确置for叶子节点 ------------
		}
		this.removeByIds(idList);
		//update-begin---author:wangshuai ---date:20230712  for：【QQYUN-5757】批量删除department时未正确置for叶子节点 ------------
		//再删除前需要Get parentid，不然会一直for空
		this.setParentDepartIzLeaf(parentIdList);
		//update-end---author:wangshuai ---date:20230712  for：【QQYUN-5757】批量删除department时未正确置for叶子节点 ------------
		//According to departmentid获取department角色id
		List<String> roleIdList = new ArrayList<>();
		LambdaQueryWrapper<SysDepartRole> query = new LambdaQueryWrapper<>();
		query.select(SysDepartRole::getId).in(SysDepartRole::getDepartId, idList);
		List<SysDepartRole> depRoleList = sysDepartRoleMapper.selectList(query);
		for(SysDepartRole deptRole : depRoleList){
			roleIdList.add(deptRole.getId());
		}
		//According to departmentid删除user与department关系
		userDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getDepId,idList));
		//According to departmentid删除department授权
		departPermissionMapper.delete(new LambdaQueryWrapper<SysDepartPermission>().in(SysDepartPermission::getDepartId,idList));
		//According to departmentid删除department角色
		sysDepartRoleMapper.delete(new LambdaQueryWrapper<SysDepartRole>().in(SysDepartRole::getDepartId,idList));
		if(roleIdList != null && roleIdList.size()>0){
			//according to角色id删除department角色授权
			departRolePermissionMapper.delete(new LambdaQueryWrapper<SysDepartRolePermission>().in(SysDepartRolePermission::getRoleId,roleIdList));
			//according to角色id删除department角色userinformation
			departRoleUserMapper.delete(new LambdaQueryWrapper<SysDepartRoleUser>().in(SysDepartRoleUser::getDroleId,roleIdList));
		}
	}

	@Override
	public List<String> getSubDepIdsByDepId(String departId) {
		return this.baseMapper.getSubDepIdsByDepId(departId);
	}

	@Override
	public List<String> getMySubDepIdsByDepId(String departIds) {
		//According to departmentidGet the responsible department
		String[] codeArr = this.getMyDeptParentOrgCode(departIds);
		if(codeArr==null || codeArr.length==0){
			return null;
		}
		return this.baseMapper.getSubDepIdsByOrgCodes(codeArr);
	}

	/**
	 * <p>
	 * according to关键字搜索相关ofdepartment数据
	 * </p>
	 */
	@Override
	public List<SysDepartTreeModel> searchByKeyWord(String keyWord, String myDeptSearch, String departIds, String orgCategory, String depIds) {
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		List<SysDepartTreeModel> newList = new ArrayList<>();
		//myDeptSearch不for空时for我ofdepartment搜索，只搜索所负责department
		if(!StringUtil.isNullOrEmpty(myDeptSearch)){
			//departIds for空普通user或没有管理department
			if(StringUtil.isNullOrEmpty(departIds)){
				return newList;
			}
			//According to departmentidGet the responsible department
			String[] codeArr = this.getMyDeptParentOrgCode(departIds);
			//update-begin-author:taoyan date:20220104 for:/issues/3311 当user属于两个departmentof时候，且这两个department没有上subordinate关系，我ofdepartment-department名称Query条件模糊搜索失效！
			if (codeArr != null && codeArr.length > 0) {
				query.nested(i -> {
					for (String s : codeArr) {
						i.or().likeRight(SysDepart::getOrgCode, s);
					}
				});
			}
			//update-end-author:taoyan date:20220104 for:/issues/3311 当user属于两个departmentof时候，且这两个department没有上subordinate关系，我ofdepartment-department名称Query条件模糊搜索失效！
			query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		}
		query.like(SysDepart::getDepartName, keyWord);
        //update-begin---author:wangshuai---date:2025-08-20---for:【QQYUN-13428】Add job selection component---
        //需要According to department类型进OK数据筛选
        if(oConvertUtils.isNotEmpty(orgCategory)){
            query.in(SysDepart::getOrgCategory, Arrays.asList(orgCategory.split(SymbolConstant.COMMA)));
        }else{
            query.ne(SysDepart::getOrgCategory,DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
        }
        //if前端传过来ofdepartmentid不for空of时候，说明yes系统useraccording to所属department选择主岗位or兼职岗位，Data filtering is required
        if(oConvertUtils.isNotEmpty(depIds)){
            List<String> codeList = baseMapper.getDepCodeByDepIds(Arrays.asList(depIds.split(SymbolConstant.COMMA)));
            if(CollectionUtil.isNotEmpty(codeList)){
                query.and(i -> {
                    for (String code : codeList) {
                        i.or().likeRight(SysDepart::getOrgCode,code);
                    }
                });
            }
        }
        //update-end---author:wangshuai---date:2025-08-20---for:【QQYUN-13428】Add job selection component---
        //update-begin--Author:huangzhilin  Date:20140417 for：[bugfreeNumber]Organization search echo optimization--------------------
		SysDepartTreeModel model = new SysDepartTreeModel();
		List<SysDepart> departList = this.list(query);
		if(departList.size() > 0) {
			for(SysDepart depart : departList) {
				model = new SysDepartTreeModel(depart);
				model.setChildren(null);
	    //update-end--Author:huangzhilin  Date:20140417 for：[bugfreeNumber]Organization search function echo optimization----------------------
				newList.add(model);
			}
			return newList;
		}
		return null;
	}

	/**
	 * According to departmentid删除并且删除其可能存在of子级任何department
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean delete(String id) {
		List<String> idList = new ArrayList<>();
		idList.add(id);
		this.checkChildrenExists(id, idList);
		//清空department树内存
		//FindsDepartsChildrenUtil.clearDepartIdModel();
		boolean ok = this.removeByIds(idList);
		//According to departmentid获取department角色id
		List<String> roleIdList = new ArrayList<>();
		LambdaQueryWrapper<SysDepartRole> query = new LambdaQueryWrapper<>();
		query.select(SysDepartRole::getId).in(SysDepartRole::getDepartId, idList);
		List<SysDepartRole> depRoleList = sysDepartRoleMapper.selectList(query);
		for(SysDepartRole deptRole : depRoleList){
			roleIdList.add(deptRole.getId());
		}
		//According to departmentid删除user与department关系
		userDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getDepId,idList));
		//According to departmentid删除department授权
		departPermissionMapper.delete(new LambdaQueryWrapper<SysDepartPermission>().in(SysDepartPermission::getDepartId,idList));
		//According to departmentid删除department角色
		sysDepartRoleMapper.delete(new LambdaQueryWrapper<SysDepartRole>().in(SysDepartRole::getDepartId,idList));
		if(roleIdList != null && roleIdList.size()>0){
			//according to角色id删除department角色授权
			departRolePermissionMapper.delete(new LambdaQueryWrapper<SysDepartRolePermission>().in(SysDepartRolePermission::getRoleId,roleIdList));
			//according to角色id删除department角色userinformation
			departRoleUserMapper.delete(new LambdaQueryWrapper<SysDepartRoleUser>().in(SysDepartRoleUser::getDroleId,roleIdList));
		}
		return ok;
	}
	
	/**
	 * delete methodcall
	 * @param id
	 * @param idList
	 */
	private void checkChildrenExists(String id, List<String> idList) {	
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getParentId,id);
		List<SysDepart> departList = this.list(query);
		if(departList != null && departList.size() > 0) {
			for(SysDepart depart : departList) {
				idList.add(depart.getId());
				this.checkChildrenExists(depart.getId(), idList);
			}
		}
	}

	@Override
	public List<SysDepart> queryUserDeparts(String userId) {
		return baseMapper.queryUserDeparts(userId);
	}

	@Override
	public List<SysDepart> queryDepartsByUsername(String username) {
		return baseMapper.queryDepartsByUsername(username);
	}
	
	@Override
	public List<String> queryDepartsByUserId(String userId) {
		List<String> list = baseMapper.queryDepartsByUserId(userId);
		return list;
	}

	/**
	 * according touser所负责departmentidsGet parentdepartment编码
	 * @param departIds
	 * @return
	 */
	private String[] getMyDeptParentOrgCode(String departIds){
		//According to departmentidQuery所负责department
		LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
		query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		if(oConvertUtils.isNotEmpty(departIds)){
			query.in(SysDepart::getId, Arrays.asList(departIds.split(",")));
		}
		
		//------------------------------------------------------------------------------------------------
		//Whether to enable the system management module多tenant数据隔离【SAASMulti-tenant model】
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		query.orderByAsc(SysDepart::getOrgCode);
		List<SysDepart> list = this.list(query);
		//查找根department
		if(list == null || list.size()==0){
			return null;
		}
		String orgCode = this.getMyDeptParentNode(list);
		String[] codeArr = orgCode.split(",");
		return codeArr;
	}

	/**
	 * 获取负责departmentfather节点
	 * @param list
	 * @return
	 */
	private String getMyDeptParentNode(List<SysDepart> list){
		Map<String,String> map = new HashMap(5);
		//1.先Will同一company归类
		for(SysDepart dept : list){
			String code = dept.getOrgCode().substring(0,3);
			if(map.containsKey(code)){
				String mapCode = map.get(code)+","+dept.getOrgCode();
				map.put(code,mapCode);
			}else{
				map.put(code,dept.getOrgCode());
			}
		}
		StringBuffer parentOrgCode = new StringBuffer();
		//2.获取同一companyof根节点
		for(String str : map.values()){
			String[] arrStr = str.split(",");
			parentOrgCode.append(",").append(this.getMinLengthNode(arrStr));
		}
		return parentOrgCode.substring(1);
	}

	/**
	 * 获取同一companymiddledepartmentCode length最小ofdepartment
	 * @param str
	 * @return
	 */
	private String getMinLengthNode(String[] str){
		int min =str[0].length();
		StringBuilder orgCodeBuilder = new StringBuilder(str[0]);
		for(int i =1;i<str.length;i++){
			if(str[i].length()<=min){
				min = str[i].length();
                orgCodeBuilder.append(SymbolConstant.COMMA).append(str[i]);
			}
		}
		return orgCodeBuilder.toString();
	}
    /**
     * 获取department树informationaccording to关键字
     * @param keyWord
     * @return
     */
    @Override
    public List<SysDepartTreeModel> queryTreeByKeyWord(String keyWord) {
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
        query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        query.orderByAsc(SysDepart::getDepartOrder);
        List<SysDepart> list = this.list(query);
        // callwrapTreeDataToTreeListMethod to generate tree data
        List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(list);
        List<SysDepartTreeModel> treelist =new ArrayList<>();
        if(StringUtils.isNotBlank(keyWord)){
            this.getTreeByKeyWord(keyWord,listResult,treelist);
        }else{
            return listResult;
        }
        return treelist;
    }

	/**
	 * according toparentIdQuerydepartment树
	 * @param parentId
	 * @param ids Front-end echo delivery
	 * @param primaryKey primary key field（idororgCode）
	 * @return
	 */
	@Override
	public List<SysDepartTreeModel> queryTreeListByPid(String parentId,String ids, String primaryKey) {
		Consumer<LambdaQueryWrapper<SysDepart>> square = i -> {
			if (oConvertUtils.isNotEmpty(ids)) {
				if (CommonConstant.DEPART_KEY_ORG_CODE.equals(primaryKey)) {
					i.in(SysDepart::getOrgCode, ids.split(SymbolConstant.COMMA));
				} else {
					i.in(SysDepart::getId, ids.split(SymbolConstant.COMMA));
				}
			} else {
				if(oConvertUtils.isEmpty(parentId)){
					i.and(q->q.isNull(true,SysDepart::getParentId).or().eq(true,SysDepart::getParentId,""));
				}else{
					i.eq(true,SysDepart::getParentId,parentId);
				}
			}
		};
		LambdaQueryWrapper<SysDepart> lqw=new LambdaQueryWrapper<>();
		//------------------------------------------------------------------------------------------------
		//Whether to enable the system management module SASS control
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			lqw.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		lqw.eq(true,SysDepart::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
        //update-begin---author:wangshuai---date:2025-08-18---for:【QQYUN-13427】Department selection component modification:Need to filter out positions only keep company 子company department---
        lqw.ne(SysDepart::getOrgCategory,DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
        //update-end---author:wangshuai---date:2025-08-18---for:【QQYUN-13427】Department selection component modification:Need to filter out positions only keep company 子company department---
		lqw.func(square);
        //update-begin---author:wangshuai ---date:20220527  for：[VUEN-1143]Wrong sorting，vue3and2There should be problems，should be sorted in ascending order------------
		lqw.orderByAsc(SysDepart::getDepartOrder);
        //update-end---author:wangshuai ---date:20220527  for：[VUEN-1143]Wrong sorting，vue3and2There should be problems，should be sorted in ascending order--------------
		List<SysDepart> list = list(lqw);
        //update-begin---author:wangshuai ---date:20220316  for：[JTC-119]在department管理菜单下设置department负责人 No processing is required when creating a user
        //Set up userid,Let the front desk display
        this.setUserIdsByDepList(list);
        //update-end---author:wangshuai ---date:20220316  for：[JTC-119]在department管理菜单下设置department负责人 No processing is required when creating a user
		List<SysDepartTreeModel> records = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			SysDepart depart = list.get(i);
            //update-begin---author:wangshuai---date:2025-08-18---for:【QQYUN-13427】Department selection component modification:Need to filter out positions only keep company 子company department---
            long count = getNoDepartPostCount(depart.getId());
            if(count == 0){
                depart.setIzLeaf(CommonConstant.IS_LEAF);
            }
            //update-end---author:wangshuai---date:2025-08-18---for:【QQYUN-13427】Department selection component modification:Need to filter out positions only keep company 子company department---
            SysDepartTreeModel treeModel = new SysDepartTreeModel(depart);
            //TODO Asynchronous tree loadingkeySplicing__+Timestamp,So that the data will be refreshed every time the node is expanded.
			//treeModel.setKey(treeModel.getKey()+"__"+System.currentTimeMillis());
            records.add(treeModel);
        }
		return records;
	}

    /**
     * 获取department数量
     * @param departId
     * @return
     */
    private long getNoDepartPostCount(String departId) {
        LambdaQueryWrapper<SysDepart> queryNoPosition = new LambdaQueryWrapper<>();
        queryNoPosition.ne(SysDepart::getOrgCategory,DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
        queryNoPosition.eq(SysDepart::getParentId,departId);
        return this.count(queryNoPosition);
    }

    /**
     * department管理异步树
     *
     * @param parentId
     * @param ids
     * @param primaryKey
     * @param departIds
     * @return
     */
    @Override
    public List<SysDepartTreeModel> queryDepartAndPostTreeSync(String parentId, String ids, String primaryKey, 
															   String departIds, String orgName) {
        Consumer<LambdaQueryWrapper<SysDepart>> square = i -> {
            if (oConvertUtils.isNotEmpty(ids)) {
                if (CommonConstant.DEPART_KEY_ORG_CODE.equals(primaryKey)) {
                    i.in(SysDepart::getOrgCode, ids.split(SymbolConstant.COMMA));
                } else {
                    i.in(SysDepart::getId, ids.split(SymbolConstant.COMMA));
                }
            } else {
                if(oConvertUtils.isEmpty(parentId)){
                    //update-begin---author:wangshuai---date:2025-08-20---for:if前端传过来ofdepartmentid不for空of时候，说明yes系统useraccording to所属department选择主岗位or兼职岗位---
                    if(oConvertUtils.isNotEmpty(departIds)){
                        i.in(SysDepart::getId,Arrays.asList(departIds.split(SymbolConstant.COMMA)));
                    }else{
						if(oConvertUtils.isEmpty(orgName)){
                        	i.and(q->q.isNull(true,SysDepart::getParentId).or().eq(true,SysDepart::getParentId,""));
						}else{
							i.like(SysDepart::getDepartName, orgName);
						}
                    }
                    //update-end---author:wangshuai---date:2025-08-20---for:if前端传过来ofdepartmentid不for空of时候，说明yes系统useraccording to所属department选择主岗位or兼职岗位---
                }else{
                    i.eq(true,SysDepart::getParentId,parentId);
                }
            }
        };
        LambdaQueryWrapper<SysDepart> lqw=new LambdaQueryWrapper<>();
        //Whether to enable the system management module SASS control
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            lqw.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
        }
        lqw.eq(true,SysDepart::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
        lqw.func(square);
        lqw.orderByAsc(SysDepart::getDepartOrder);
        List<SysDepart> list = list(lqw);
        //Set up userid,Let the front desk display
        this.setUserIdsByDepList(list);
        List<String> departIdList = new ArrayList<>();
        //if前端传过来ofdepartmentid不for空of时候，说明yes系统useraccording to所属department选择主岗位or兼职岗位
        if(oConvertUtils.isNotEmpty(departIds) && oConvertUtils.isEmpty(parentId)){
            departIdList = list.stream().map(SysDepart::getId).toList();
        }
        List<SysDepartTreeModel> records = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            SysDepart depart = list.get(i);
            //ifdepartmentidandfather级departmentidWhen in the same column again，No need to add it to the tree structure
            if(oConvertUtils.isNotEmpty(departIds) && oConvertUtils.isEmpty(parentId)
               && departIdList.contains(depart.getParentId())){
                continue;
            }
            SysDepartTreeModel treeModel = new SysDepartTreeModel(depart);
            records.add(treeModel);
        }
        return records;
    }

	@Override
	public JSONObject queryAllParentIdByDepartId(String departId) {
		JSONObject result = new JSONObject();
		for (String id : departId.split(SymbolConstant.COMMA)) {
			JSONObject all = this.queryAllParentId("id", id);
			result.put(id, all);
		}
		return result;
	}

	@Override
	public JSONObject queryAllParentIdByOrgCode(String orgCode) {
		JSONObject result = new JSONObject();
		for (String code : orgCode.split(SymbolConstant.COMMA)) {
			JSONObject all = this.queryAllParentId("org_code", code);
			result.put(code, all);
		}
		return result;
	}

	/**
	 * Query某个departmentof所有fatherIDinformation
	 *
	 * @param fieldName Field name
	 * @param value     value
	 */
	private JSONObject queryAllParentId(String fieldName, String value) {
		JSONObject data = new JSONObject();
		// fatherIDgather，orderly
		data.put("parentIds", new JSONArray());
		// fatherIDofdepartment数据，keyyesid，valueyes数据
		data.put("parentMap", new JSONObject());
		this.queryAllParentIdRecursion(fieldName, value, data);
		return data;
	}

	/**
	 * 递归callQueryfatherdepartment接口
	 */
	private void queryAllParentIdRecursion(String fieldName, String value, JSONObject data) {
		QueryWrapper<SysDepart> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(fieldName, value);
		SysDepart depart = super.getOne(queryWrapper);
		if (depart != null) {
			data.getJSONArray("parentIds").add(0, depart.getId());
			data.getJSONObject("parentMap").put(depart.getId(), depart);
			if (oConvertUtils.isNotEmpty(depart.getParentId())) {
				this.queryAllParentIdRecursion("id", depart.getParentId(), data);
			}
		}
	}

	@Override
	public SysDepart queryCompByOrgCode(String orgCode) {
		int length = YouBianCodeUtil.ZHANWEI_LENGTH;
		String compyOrgCode = orgCode.substring(0,length);
		return this.baseMapper.queryCompByOrgCode(compyOrgCode);
	}
	/**
	 * according toidQuerysubordinatedepartment
	 * @param pid
	 * @return
	 */
	@Override
	public List<SysDepart> queryDeptByPid(String pid) {
		return this.baseMapper.queryDeptByPid(pid);
	}
	/**
     * according to关键字筛选departmentinformation
     * @param keyWord
     * @return
     */
    public void getTreeByKeyWord(String keyWord,List<SysDepartTreeModel> allResult,List<SysDepartTreeModel>  newResult){
        for (SysDepartTreeModel model:allResult) {
            if (model.getDepartName().contains(keyWord)){
                newResult.add(model);
                continue;
            }else if(model.getChildren()!=null){
                getTreeByKeyWord(keyWord,model.getChildren(),newResult);
            }
        }
    }
    
    //update-begin---author:wangshuai ---date:20200308  for：[JTC-119]在department管理菜单下设置department负责人，新增method添加department负责人、删除负责department负责人、Querydepartmentcorrespondof负责人
    /**
     * by userid设置负责department
     * @param sysDepart SysDepartdepartmentobject
     * @param userIds Multiple responsible usersid
     */
    public void addDepartByUserIds(SysDepart sysDepart, String userIds) {
        //获取departmentid,Save to user
        String departId = sysDepart.getId();
        //recurring usersid
        String[] userIdArray = userIds.split(",");
        for (String userId:userIdArray) {
            //Queryuser表增加负责department
            SysUser sysUser = sysUserMapper.selectById(userId);
            //ifdepartmentid不for空，那么就需要Splicing
            if(oConvertUtils.isNotEmpty(sysUser.getDepartIds())){
                if(!sysUser.getDepartIds().contains(departId)) {
                    sysUser.setDepartIds(sysUser.getDepartIds() + "," + departId);
                }
            }else{
                sysUser.setDepartIds(departId);
            }
            //设置身份forSuperior
            sysUser.setUserIdentity(CommonConstant.USER_IDENTITY_2);
            //Follow new user table
            sysUserMapper.updateById(sysUser);
            //判断当前useryes否Include所属department
            List<SysUserDepart> userDepartList = userDepartMapper.getUserDepartByUid(userId);
            boolean isExistDepId = userDepartList.stream().anyMatch(item -> departId.equals(item.getDepId()));
            //ifdoes not exist需要设置所属department
            if(!isExistDepId){
                userDepartMapper.insert(new SysUserDepart(userId,departId));
            }
        }
    }
    
    /**
     * 修改user负责department
     * @param sysDepart SysDepartobject
     */
    private void updateChargeDepart(SysDepart sysDepart) {
        //new userid
        String directorIds = sysDepart.getDirectorUserIds();
        //old userid（exist in the database）
        String oldDirectorIds = sysDepart.getOldDirectorUserIds();
        String departId = sysDepart.getId();
        //If the useridfor空,那么userof负责departmentidshould be removed
        if(oConvertUtils.isEmpty(directorIds)){
            this.deleteChargeDepId(departId,null);
        }else if(oConvertUtils.isNotEmpty(directorIds) && oConvertUtils.isEmpty(oldDirectorIds)){
            //If the userid不for空但yesuser原来负责departmentofuseridfor空
            this.addDepartByUserIds(sysDepart,directorIds);
        }else{
            //都不for空，Need to compare，Add or delete
            //找到新of负责departmentuserid与原来负责departmentofuserid，Delete
            List<String> userIdList = Arrays.stream(oldDirectorIds.split(",")).filter(item -> !directorIds.contains(item)).collect(Collectors.toList());
            for (String userId:userIdList){
                this.deleteChargeDepId(departId,userId);
            }
            //找到原来负责departmentofuserid与新of负责departmentuserid，Add new
            String addUserIds = Arrays.stream(directorIds.split(",")).filter(item -> !oldDirectorIds.contains(item)).collect(Collectors.joining(","));
            if(oConvertUtils.isNotEmpty(addUserIds)){
                this.addDepartByUserIds(sysDepart,addUserIds); 
            }
        }
    }

    /**
     * 删除user负责department
     * @param departId departmentid
     * @param userId userid
     */
    private void deleteChargeDepId(String departId,String userId){
        //先Query负责departmentofuserid,因for负责departmentofid使use逗NumberSplicing起来of
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.like(SysUser::getDepartIds,departId);
        //删除全部situation下useriddoes not exist
        if(oConvertUtils.isNotEmpty(userId)){
            query.eq(SysUser::getId,userId); 
        }
        List<SysUser> userList = sysUserMapper.selectList(query);
        for (SysUser sysUser:userList) {
            //Willdoes not existofdepartmentiddelete
            String departIds = sysUser.getDepartIds();
            List<String> list = new ArrayList<>(Arrays.asList(departIds.split(",")));
            list.remove(departId);
            //Delete and then add newiduse逗NumberSplicing起来进OK更新
            String newDepartIds = String.join(",",list);
            sysUser.setDepartIds(newDepartIds);
            sysUserMapper.updateById(sysUser);
        }
    }

    /**
     * 通过departmentgatherfordepartmentSet up userid，For front desk display
     * @param departList departmentgather
     */
    private void setUserIdsByDepList(List<SysDepart> departList) {
        //Query负责department不for空situation
        LambdaQueryWrapper<SysUser> query  = new LambdaQueryWrapper<>();
        query.isNotNull(SysUser::getDepartIds);
        List<SysUser> users = sysUserMapper.selectList(query);
        Map<String,Object> map = new HashMap(5);
        //先循环一遍找到不同of负责departmentid
        for (SysUser user:users) {
            String departIds = user.getDepartIds();
            String[] departIdArray = departIds.split(",");
            for (String departId:departIdArray) {
                //maomiddleIncludedepartmentkey，负责user直接Splicing
                if(map.containsKey(departId)){
                    String userIds = map.get(departId) + "," + user.getId();
                    map.put(departId,userIds);
                }else{
                    map.put(departId,user.getId());  
                }
            }
        }
        //循环departmentgather找到departmentidcorrespondof负责user
        for (SysDepart sysDepart:departList) {
            if(map.containsKey(sysDepart.getId())){
                sysDepart.setDirectorUserIds(map.get(sysDepart.getId()).toString()); 
            }
        }
    }
    //update-end---author:wangshuai ---date:20200308  for：[JTC-119]在department管理菜单下设置department负责人，新增method添加department负责人、删除负责department负责人、Querydepartmentcorrespondof负责人

    /**
     * 获取我ofdepartment已加入ofcompany
     * @return
     */
    @Override
    public List<SysDepart> getMyDepartList() {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = user.getId();
        //dictionarycodegather
        List<String> list = new ArrayList<>();
        //Query我加入ofdepartment
        List<SysDepart> sysDepartList = this.baseMapper.queryUserDeparts(userId);
        for (SysDepart sysDepart : sysDepartList) {
            //获取一级department编码
            String orgCode = sysDepart.getOrgCode();
            if (YouBianCodeUtil.ZHANWEI_LENGTH <= orgCode.length()) {
                int length = YouBianCodeUtil.ZHANWEI_LENGTH;
                String companyOrgCode = orgCode.substring(0, length);
                list.add(companyOrgCode);
            }
        }
        //dictionarycodegather不for空
        if (oConvertUtils.isNotEmpty(list)) {
            //Query一级departmentof数据
            LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
            query.select(SysDepart::getDepartName, SysDepart::getId, SysDepart::getOrgCode);
            query.eq(SysDepart::getDelFlag, String.valueOf(CommonConstant.DEL_FLAG_0));
            query.in(SysDepart::getOrgCode, list);
            return this.baseMapper.selectList(query);
        }
        return null;
    }

	@Override
	public void deleteDepart(String id) {
    	//删除department设置father级of叶子结点
		this.setIzLeaf(id);
		this.delete(id);
		//删除departmentuser关系表
		LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<SysUserDepart>()
				.eq(SysUserDepart::getDepId, id);
		this.userDepartMapper.delete(query);
	}

	@Override
	public List<SysDepartTreeModel> queryBookDepTreeSync(String parentId, Integer tenantId, String departName) {
		List<SysDepart> list = departMapper.queryBookDepTreeSync(parentId,tenantId,departName);
		List<SysDepartTreeModel> records = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			SysDepart depart = list.get(i);
			SysDepartTreeModel treeModel = new SysDepartTreeModel(depart);
			records.add(treeModel);
		}
		return records;
	}

	@Override
	public SysDepart getDepartById(String id) {
		return departMapper.getDepartById(id);
	}

	@Override
	public IPage<SysDepart> getMaxCodeDepart(Page<SysDepart> page, String parentId) {
		return page.setRecords(departMapper.getMaxCodeDepart(page,parentId));
	}

	@Override
	public void updateIzLeaf(String id, Integer izLeaf) {
		departMapper.setMainLeaf(id,izLeaf);
	}

	/**
	 * 设置father级节点yes否存在叶子结点
	 * @param id
	 */
	private void setIzLeaf(String id) {
		SysDepart depart = this.getDepartById(id);
		String parentId = depart.getParentId();
		if(oConvertUtils.isNotEmpty(parentId)){
			Long count = this.count(new QueryWrapper<SysDepart>().lambda().eq(SysDepart::getParentId, parentId));
			if(count == 1){
				//若father节点无其他子节点，则该father节点yes叶子节点
				departMapper.setMainLeaf(parentId, CommonConstant.IS_LEAF);
			}
		}
	}

	//========================begin 零代码下department与人员导出 ==================================================================

	@Override
	public List<ExportDepartVo> getExcelDepart(int tenantId) {
		//Get parentdepartment
		List<ExportDepartVo> parentDepart = departMapper.getDepartList("",tenantId);
		//子department
		List<ExportDepartVo> childrenDepart = new ArrayList<>();
		//把一级department名称放在里面
		List<ExportDepartVo> exportDepartVoList = new ArrayList<>();
		//存放department一级idavoid duplication
		List<String> departIdList = new ArrayList<>();
		for (ExportDepartVo departVo:parentDepart) {
			departIdList.add(departVo.getId());
			departVo.setDepartNameUrl(departVo.getDepartName());
			exportDepartVoList.add(departVo);
			//create path
			List<String> path = new ArrayList<>();
			path.add(departVo.getDepartName());
			//创建子department路径
			findPath(departVo, path, tenantId,childrenDepart,departIdList);
			path.clear();
		}
		exportDepartVoList.addAll(childrenDepart);
		childrenDepart.clear();
		departIdList.clear();
		return exportDepartVoList;
	}

	/**
	 * 寻找department路径
	 * @param departVo departmentvo
	 * @param path department路径
	 * @param tenantId tenantid
	 * @param childrenDepart 子department
	 * @param departIdList departmentidgather
	 */
	private void findPath(ExportDepartVo departVo, List<String> path,Integer tenantId,List<ExportDepartVo> childrenDepart,List<String> departIdList) {
		//获取tenantidanddepartmentfatherid获取ofdepartment数据
		List<ExportDepartVo> departList = departMapper.getDepartList(departVo.getId(), tenantId);
		//departmentfor空判断
		if (departList == null || departList.size() <= 0) {
			if(!departIdList.contains(departVo.getId())){
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH,path));
				childrenDepart.add(departVo);
			}
			return;
		}

		for (int i = 0; i < departList.size(); i++) {
			ExportDepartVo exportDepartVo = departList.get(i);
			//Store child path
			List<String> cPath = new ArrayList<>();
			cPath.addAll(path);
			cPath.add(exportDepartVo.getDepartName());
			if(!departIdList.contains(departVo.getId())){
				departIdList.add(departVo.getId());
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH,path));
				childrenDepart.add(departVo);
			}
			findPath(exportDepartVo,cPath ,tenantId, childrenDepart,departIdList);
		}
	}
	//========================end 零代码下department与人员导出 ==================================================================

	//========================begin 零代码下department与人员导入 ==================================================================
	@Override
	public void importExcel(List<ExportDepartVo> listSysDeparts, List<String> errorMessageList) {
		int num = 0;
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		
		//department路径排序
		Collections.sort(listSysDeparts, new Comparator<ExportDepartVo>() {
			@Override
			public int compare(ExportDepartVo o1, ExportDepartVo o2) {
				if(oConvertUtils.isNotEmpty(o1.getDepartNameUrl()) && oConvertUtils.isNotEmpty(o2.getDepartNameUrl())){
					int oldLength = o1.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
					int newLength = o2.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
					return oldLength - newLength;
				}else{
					return 0;
				}
			}
		});
		//存放department数据ofmap
		Map<String,SysDepart> departMap = new HashMap<>();
		//Loop through the second pass to import data
		for (ExportDepartVo exportDepartVo : listSysDeparts) {
			SysDepart sysDepart = new SysDepart();
			// orgCodeCode length
			int codeLength = YouBianCodeUtil.ZHANWEI_LENGTH;
			Boolean izExport = false;
			try {
				izExport = this.addDepartByName(exportDepartVo.getDepartNameUrl(),exportDepartVo.getDepartName(),sysDepart,errorMessageList,tenantId,departMap,num);
			} catch (Exception e) {
				//not foundparentDept
			}
			//Data will be imported only when there are no errors
			if(izExport){
				sysDepart.setOrgType(sysDepart.getOrgCode().length()/codeLength+"");
				sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
				sysDepart.setOrgCategory("1");
				sysDepart.setTenantId(tenantId);
				ImportExcelUtil.importDateSaveOne(sysDepart, ISysDepartService.class, errorMessageList, num, CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE);
				departMap.put(exportDepartVo.getDepartNameUrl(),sysDepart);
			}
			num++;
		}
	}

	/**
	 * 添加department
	 * @param departNameUrl department路径
	 * @param departName department名称
	 * @param sysDepart department类
	 * @param errorMessageList 错误gather
	 * @param tenantId tenantid
	 * @param departMap department数组。避免存在departmentinformation再次Query key 存放department路径 value 存放departmentobject
	 * @param num 判断No.几OK有错误information
	 */
	private Boolean addDepartByName(String departNameUrl,String departName,SysDepart sysDepart,List<String> errorMessageList,Integer tenantId,Map<String,SysDepart> departMap, int num) {
		int lineNumber = num + 1;
		if(oConvertUtils.isEmpty(departNameUrl) && oConvertUtils.isEmpty(departName)){
			//department路径for空
			errorMessageList.add("No. " + lineNumber + " OK：记录department路径ordepartment名称for空禁止导入");
			return false;
		}
		//获取department名称路径
		String name = "";
		if(departNameUrl.contains(SymbolConstant.SINGLE_SLASH)){
			//获取分割ofdepartment名称
			name = departNameUrl.substring(departNameUrl.lastIndexOf(SymbolConstant.SINGLE_SLASH)+1);
		}else{
			name = departNameUrl;
		}
		
		if(!name.equals(departName)){
			//department名称Already exists
			errorMessageList.add("No. " + lineNumber + " OK：记录department路径:”"+departNameUrl+"“"+"anddepartment名称：“"+departName+"“inconsistent，Check, please！");
			return false;
		}else{
			String parentId = "";
			//判断yes否Include“/”
			if(departNameUrl.contains(SymbolConstant.SINGLE_SLASH)){
				//Get the path before the last slash
				String departNames = departNameUrl.substring(0,departNameUrl.lastIndexOf(SymbolConstant.SINGLE_SLASH));
				//判断yes否已经Includedepartment路径
				if(departMap.containsKey(departNames)){
					SysDepart depart = departMap.get(departNames);
					if(null != depart){
						parentId = depart.getId();
					}
				}else{
					//split slash path，查看数据库middleyes否存在此路径
					String[] departNameUrls = departNameUrl.split(SymbolConstant.SINGLE_SLASH);
					String departUrlName = departNameUrls[0];
					//判断yes否for最后一位
					int count = 0;
					SysDepart depart  = new SysDepart();
					depart.setId("");
					String parentIdByName = this.getDepartListByName(departUrlName,tenantId,depart,departNameUrls,count,departNameUrls.length-1,name,departMap);
					//ifparentId不for空
					if(oConvertUtils.isNotEmpty(parentIdByName)){
						parentId = parentIdByName;
					}else{
						//department名称Already exists
						errorMessageList.add("No. " + lineNumber + " OK：记录department名称“"+departName+"”Superiordoes not exist，Check, please！");
						return false;
					}
				}
			}
			//Querydepartment名称yes否Already exists
			SysDepart parentDept = null;
			//update-begin---author:wangshuai ---date:20230721  for：一个tenantdepartment名称可能有多个------------
			List<SysDepart> sysDepartList = departMapper.getDepartByName(departName,tenantId,parentId);
			if(CollectionUtil.isNotEmpty(sysDepartList)){
				parentDept = sysDepartList.get(0);
			}
			//update-end---author:wangshuai ---date:20230721  for：一个tenantdepartment名称可能有多个------------
			if(null != parentDept) {
				//department名称Already exists
				errorMessageList.add("No. " + lineNumber + " OK：记录department名称“"+departName+"”Already exists，Check, please！");
				return false;
			}else{
				Page<SysDepart> page = new Page<>(1,1);
				//需要Get parentid，查看father级yes否已经存在
				//获取一级departmentof最大orgCode
				List<SysDepart> records = departMapper.getMaxCodeDepart(page, parentId);
				String newOrgCode = "";
				if(CollectionUtil.isNotEmpty(records)){
					newOrgCode = YouBianCodeUtil.getNextYouBianCode(records.get(0).getOrgCode());
				}else{
					//Queryfatherid
					if(oConvertUtils.isNotEmpty(parentId)){
						SysDepart departById = departMapper.getDepartById(parentId);
						newOrgCode = YouBianCodeUtil.getSubYouBianCode(departById.getOrgCode(), null);
					}else{
						newOrgCode = YouBianCodeUtil.getNextYouBianCode(null);
					}
				}
				if(oConvertUtils.isNotEmpty(parentId)){
					this.updateIzLeaf(parentId,CommonConstant.NOT_LEAF);
					sysDepart.setParentId(parentId);
				}
				sysDepart.setOrgCode(newOrgCode);
				sysDepart.setDepartName(departName);
				return true;
			}
	
		}
	}

	/**
	 * 获取department名称url（subordinate）
	 * @param departName department名称
	 * @param tenantId tenantid
	 * @param sysDepart departmentobject
	 * @param count department路径下标
	 * @param departNameUrls department路径
	 * @param departNum department路径of数量
	 * @param name department路径of数量
	 * @param departMap 存放departmentof数据 key 存放department路径 value 存放departmentobject
	 */
	private String getDepartListByName(String departName, Integer tenantId, SysDepart sysDepart,String[] departNameUrls, int count, int departNum,String name,Map<String,SysDepart> departMap) {
		//Recursively search for the next level
		//update-begin---author:wangshuai ---date:20230721  for：一个tenantdepartment名称可能有多个------------
		SysDepart parentDept = null;
		List<SysDepart> departList = departMapper.getDepartByName(departName,tenantId,sysDepart.getId());
		if(CollectionUtil.isNotEmpty(departList)){
			parentDept = departList.get(0);
		}
		//update-end---author:wangshuai ---date:20230721  for：一个tenantdepartment名称可能有多个------------
		//判断yes否Include/
		if(oConvertUtils.isNotEmpty(name)){
			name = name + SymbolConstant.SINGLE_SLASH + departName;
		}else{
			name = departName;
		}
		if(null != parentDept){
			//if名称路径keyno longer here，add a，避免再次Query
			if(!departMap.containsKey(name)){
				departMap.put(name,parentDept);
			}
			//Query出来ofdepartment名称anddepartment路径middleofdepartment名称作比较，ifdoes not exist直接返回空
			if(parentDept.getDepartName().equals(departNameUrls[count])){
				count = count + 1;
				//数量anddepartment数量相等说明已经到最后一位了，直接返回departmentid
				if(count == departNum){
					return parentDept.getId();
				}else{
					return this.getDepartListByName(departNameUrls[count],tenantId,parentDept,departNameUrls,count,departNum,name,departMap);
				}
			}else{
				return "";
			}
		}else{
			return "";
		}
	}
	//========================end 零代码下department与人员导入 ==================================================================

	/**
	 * 清空departmentid
	 *
	 * @param parentIdList
	 */
	private void setParentDepartIzLeaf(List<String> parentIdList) {
		if (CollectionUtil.isNotEmpty(parentIdList)) {
			for (String parentId : parentIdList) {
				//Queryfather级id没有子级of时候跟新for叶子节点
				LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
				query.eq(SysDepart::getParentId, parentId);
				Long count = departMapper.selectCount(query);
				//当子级都does not exist时，设置当前departmentfor叶子节点
				if (count == 0) {
					departMapper.setMainLeaf(parentId, CommonConstant.IS_LEAF);
				}
			}
		}
	}

	//========================begin 系统下department与人员导入 ==================================================================
	/**
	 * 系统department导出
	 * @param tenantId
	 * @param idList 需要Querydepartmentsqlofidgather
	 * @return
	 */
	@Override
	public List<SysDepartExportVo> getExportDepart(Integer tenantId, List<String> idList) {
		//Get parentdepartment
		List<SysDepartExportVo> parentDepart = departMapper.getSysDepartList("", tenantId, idList);
		//子department
		List<SysDepartExportVo> childrenDepart = new ArrayList<>();
		//把一级department名称放在里面
		List<SysDepartExportVo> exportDepartVoList = new ArrayList<>();
		//存放department一级idavoid duplication
		List<String> departIdList = new ArrayList<>();
		for (SysDepartExportVo sysDepart : parentDepart) {
			//step 1.添加No.一级department
			departIdList.add(sysDepart.getId());
			sysDepart.setDepartNameUrl(sysDepart.getDepartName());
			exportDepartVoList.add(sysDepart);
			//step 2.添加自己department路径，use/separation
			//create path
			List<String> path = new ArrayList<>();
			path.add(sysDepart.getDepartName());
			//创建子department路径
			findSysDepartPath(sysDepart, path, tenantId, childrenDepart, departIdList, idList);
			path.clear();
		}
		exportDepartVoList.addAll(childrenDepart);
		childrenDepart.clear();
		departIdList.clear();
		return exportDepartVoList;
	}

	/**
	 * 系统department导入
	 * @param listSysDeparts
	 * @param errorMessageList
	 */
	@Override
	public void importSysDepart(List<SysDepartExportVo> listSysDeparts, List<String> errorMessageList) {
		int num = 0;
		int tenantId = 0;
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		}
		//department路径排序
		Collections.sort(listSysDeparts, new Comparator<SysDepartExportVo>() {
			@Override
			public int compare(SysDepartExportVo o1, SysDepartExportVo o2) {
				if(oConvertUtils.isNotEmpty(o1.getDepartNameUrl()) && oConvertUtils.isNotEmpty(o2.getDepartNameUrl())){
					int oldLength = o1.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
					int newLength = o2.getDepartNameUrl().split(SymbolConstant.SINGLE_SLASH).length;
					return oldLength - newLength;
				}else{
					return 0;
				}
			}
		});
		//存放department数据ofmap
		Map<String,SysDepart> departMap = new HashMap<>();
		// orgCodeCode length
		int codeLength = YouBianCodeUtil.ZHANWEI_LENGTH;
		//Loop through the second pass to import data
		for (SysDepartExportVo departExportVo : listSysDeparts) {
			SysDepart sysDepart = new SysDepart();
			boolean izExport = false;
			try {
				izExport = this.addDepartByName(departExportVo.getDepartNameUrl(),departExportVo.getDepartName(),sysDepart,errorMessageList,tenantId,departMap,num);
			} catch (Exception e) {
				//not foundparentDept
			}
			//Data will be imported only when there are no errors
			if(izExport){
				if(oConvertUtils.isNotEmpty(departExportVo.getOrgCode())){
					SysDepart depart = this.baseMapper.queryCompByOrgCode(departExportVo.getOrgCode());
					if(null != depart){
						if(oConvertUtils.isNotEmpty(sysDepart.getParentId())){
							//更新Superiordepartmentfor叶子节点
							this.updateIzLeaf(sysDepart.getParentId(),CommonConstant.IS_LEAF);
						}
						//department名称Already exists
						errorMessageList.add("No. " + num + " OK：记录department名称“"+departExportVo.getDepartName()+"”department编码重复，Check, please！");
						continue;
					}
					String departNameUrl = departExportVo.getDepartNameUrl();
					//Include/说明yes多级
					if(departNameUrl.contains(SymbolConstant.SINGLE_SLASH)){
						//判断添加departmentof规则yes否and生成of一致
						if(!sysDepart.getOrgCode().equals(departExportVo.getOrgCode())){
							if(oConvertUtils.isNotEmpty(sysDepart.getParentId())){
								//更新Superiordepartmentfor叶子节点
								this.updateIzLeaf(sysDepart.getParentId(),CommonConstant.IS_LEAF);
							}
							//department名称Already exists
							errorMessageList.add("No. " + num + " OK：记录department名称“"+departExportVo.getDepartName()+"”department编码规则不匹配，Check, please！");
							continue;
						}
					}
					sysDepart.setOrgCode(departExportVo.getOrgCode());
					if(oConvertUtils.isNotEmpty(sysDepart.getParentId())){
					    //Superior
						sysDepart.setOrgType("2");
					}else{
						//subordinate
						sysDepart.setOrgType("1");
					}
				}else{
					sysDepart.setOrgType(sysDepart.getOrgCode().length()/codeLength+"");
				}
				sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
				sysDepart.setDepartNameEn(departExportVo.getDepartNameEn());
				sysDepart.setDepartOrder(departExportVo.getDepartOrder());
				sysDepart.setOrgCategory(oConvertUtils.getString(departExportVo.getOrgCategory(),"1"));
				sysDepart.setMobile(departExportVo.getMobile());
				sysDepart.setFax(departExportVo.getFax());
				sysDepart.setAddress(departExportVo.getAddress());
				sysDepart.setMemo(departExportVo.getMemo());
                sysDepart.setPositionId(departExportVo.getPositionId());
				ImportExcelUtil.importDateSaveOne(sysDepart, ISysDepartService.class, errorMessageList, num, CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE);
				departMap.put(departExportVo.getDepartNameUrl(),sysDepart);
			}
			num++;
		}
	}

	/**
	 * 寻找department路径
	 *
	 * @param departVo       departmentvo
	 * @param path           department路径
	 * @param tenantId       tenantid
	 * @param childrenDepart 子department
	 * @param departIdList   departmentidgather
	 * @param idList   需要Querysqlofdepartmentidgather
	 */
	private void findSysDepartPath(SysDepartExportVo departVo, List<String> path, Integer tenantId, List<SysDepartExportVo> childrenDepart, List<String> departIdList, List<String> idList) {
		//step 1.Query子departmentof数据
		//获取tenantidanddepartmentfatherid获取ofdepartment数据
		List<SysDepartExportVo> departList = departMapper.getSysDepartList(departVo.getId(), tenantId, idList);
		//departmentfor空判断
		if (departList == null || departList.size() <= 0) {
			//判断最后一个子departmentyes否已Splicing
			if (!departIdList.contains(departVo.getId())) {
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH, path));
				childrenDepart.add(departVo);
			}
			return;
		}

		for (SysDepartExportVo exportDepartVo : departList) {
			//Store child path
			List<String> cPath = new ArrayList<>(path);
			cPath.add(exportDepartVo.getDepartName());
			//step 2.Splicing子department路径
			if (!departIdList.contains(departVo.getId())) {
				departIdList.add(departVo.getId());
				departVo.setDepartNameUrl(String.join(SymbolConstant.SINGLE_SLASH, path));
				childrenDepart.add(departVo);
			}
			//step 3.递归Query子路径，直到找不到for止
			findSysDepartPath(exportDepartVo, cPath, tenantId, childrenDepart, departIdList, idList);
		}
	}
	//========================end 系统下department与人员导入 ==================================================================


    //=========================begin department岗位改造 ==================================================================
    @Override
    public List<SysPositionSelectTreeVo> getPositionByDepartId(String parentId, String departId, String positionId) {
        //step1 according to职级获取当前岗位of级别
        SysPosition sysPosition = sysPositionMapper.selectById(positionId);
        if(null == sysPosition){
            return null;
        }
        Integer postLevel = sysPosition.getPostLevel();
        //先获取Superiordepartmentofinformation
        SysDepart sysDepart = baseMapper.getDepartById(parentId);
        //step2 ifyes总company 即数据for空of时候，则说明没有Superior领导了
        if (null == sysDepart) {
            return null;
        }
        
        //可能yes老数据
        if(null == postLevel){
            throw new JeecgBootBizTipException("当前选择职级of职务等级for空，请前往职务管理进OK修改！");
        }
        
        //step3 查看yes否for董事长
        if (1 == postLevel) {
            //step4 ifyes董事长QuerySuperior子companyor总company下departmentof所有of岗位
            return this.getChairmanDepartPosition(sysDepart, departId);
        } else {
            //step5 if不yes董事长组Query当前father级department、companyand子companyof岗位 and当前department下of岗位，但yes必须比当前职务of级别高
            return this.getNotChairmanDepartPosition(sysDepart, postLevel, departId);
        }
    }

    /**
     * Get the position of Chairman
     *
     * @param sysDepart
     * @param id
     * @return
     */
    private List<SysPositionSelectTreeVo> getChairmanDepartPosition(SysDepart sysDepart, String id) {
        //step1 先递归获取for子companyor总共司ofid
        String departId = getCompanyDepartId(sysDepart.getParentId());
        if (oConvertUtils.isNotEmpty(departId)) {
            SysDepart depart = baseMapper.getDepartById(departId);
            //if当前Superiordepartment就yes子companyor总companysituation下
            if (DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue().equals(sysDepart.getOrgCategory()) || DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue().equals(sysDepart.getOrgCategory())) {
                depart.setParentId(departId);
                List<SysPositionSelectTreeVo> departPosition = getDepartPosition(depart, null, id);
                if (CollectionUtil.isNotEmpty(departPosition)) {
                    if (CollectionUtil.isNotEmpty(departPosition)) {
                        //father级id不for空并且当前department不yes子companyor总company，则需要寻上顶级company
                        return getSuperiorCompany(departPosition);
                    }
                }
                return departPosition;
            } else {
                //step2 获取上一个子companyor总company下of岗位
                String parentId = getCompanyDepartId(depart.getParentId());
                if (oConvertUtils.isNotEmpty(departId)) {
                    depart = baseMapper.getDepartById(parentId);
                    if (null != depart) {
                        depart.setParentId(parentId);
                        List<SysPositionSelectTreeVo> departPosition = getDepartPosition(depart, null, id);
                        //step3 获取Superiordepartmentinformation，一直获取到子companyor总companyfor止
                        if (CollectionUtil.isNotEmpty(departPosition)) {
                            //father级id不for空并且当前department不yes子companyor总company，则需要寻上顶级company
                            return getSuperiorCompany(departPosition);
                        }
                        return departPosition;
                    }
                }
            }

        }
        return null;
    }

    /*
     * 获取不yes董事长of数据
     *
     * @param sysDepart
     * @param postLevel
     * @param id
     * @return
     */
    private List<SysPositionSelectTreeVo> getNotChairmanDepartPosition(SysDepart sysDepart, Integer postLevel, String id) {
        //step1 先获取Superiordepartment下of数据
        List<SysPositionSelectTreeVo> departPosition = getDepartPosition(sysDepart, postLevel, id);
        //step2 获取Superiordepartmentinformation，一直获取到子companyor总companyfor止
        if (CollectionUtil.isNotEmpty(departPosition)) {
            if (CollectionUtil.isNotEmpty(departPosition)) {
                //father级id不for空并且当前department不yes子companyor总company，则需要寻上顶级company
                return getSuperiorCompany(departPosition);
            }
            return departPosition;
        }
        return departPosition;
    }

    /**
     * 获取Superiorcompany
     *
     * @param departPosition
     */
    private List<SysPositionSelectTreeVo> getSuperiorCompany(List<SysPositionSelectTreeVo> departPosition) {
        String parentId = departPosition.get(0).getParentId();
        SysDepart depart = baseMapper.getDepartById(parentId);
        if (null == depart) {
            return departPosition;
        }
        List<SysPositionSelectTreeVo> childrenList = new ArrayList<>();
        SysPositionSelectTreeVo childrenTreeModel = new SysPositionSelectTreeVo(depart);
        childrenTreeModel.setChildren(departPosition);
        childrenList.add(childrenTreeModel);
        if (DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue().equals(depart.getOrgCategory()) || DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue().equals(depart.getOrgCategory())) {
            return childrenList;
        } else {
            return this.getSuperiorCompany(childrenList);
        }
    }

    /**
     * 获取department职务
     *
     * @param sysDepart
     * @param postLevel
     */
    private List<SysPositionSelectTreeVo> getDepartPosition(SysDepart sysDepart, Integer postLevel, String id) {
        //step1 获取department下of所有department
        String parentId = sysDepart.getParentId();
        List<SysDepart> departList = baseMapper.getDepartByParentId(parentId);
        List<SysPositionSelectTreeVo> treeModels = new ArrayList<>();
        for (int i = 0; i < departList.size(); i++) {
            SysDepart depart = departList.get(i);
            //ifyes叶子节点说明没有岗位直接跳出循环
            if (depart.getIzLeaf() == 1) {
                if (DepartCategoryEnum.DEPART_CATEGORY_POST.getValue().equals(depart.getOrgCategory())) {
                    SysPositionSelectTreeVo sysDepartTreeModel = new SysPositionSelectTreeVo(depart);
                    treeModels.add(sysDepartTreeModel);
                }
                continue;
            }
            //step2 查找子department下大于当前职别of数据
            List<SysDepart> departParentPosition = baseMapper.getDepartPositionByParentId(depart.getId(), postLevel, id);
            if (CollectionUtil.isNotEmpty(departParentPosition)) {
                List<SysPositionSelectTreeVo> sysDepartTreeModels = sysDepartToTreeModel(departParentPosition);
                SysPositionSelectTreeVo parentDepartTree = new SysPositionSelectTreeVo(depart);
                parentDepartTree.setChildren(sysDepartTreeModels);
                treeModels.add(parentDepartTree);
            }
        }
        return treeModels;
    }

    /**
     * WillSysDepartmiddleof属性转到SysDepartTreeModelmiddle
     *
     * @return
     */
    private List<SysPositionSelectTreeVo> sysDepartToTreeModel(List<SysDepart> sysDeparts) {
        List<SysPositionSelectTreeVo> records = new ArrayList<>();
        for (int i = 0; i < sysDeparts.size(); i++) {
            SysDepart depart = sysDeparts.get(i);
            SysPositionSelectTreeVo treeModel = new SysPositionSelectTreeVo(depart);
            records.add(treeModel);
        }
        return records;
    }

    /**
     * 获取companyor子companyofid
     *
     * @param parentDepartId
     * @return
     */
    private String getCompanyDepartId(String parentDepartId) {
        SysDepart sysDepart = baseMapper.getDepartById(parentDepartId);
        if (sysDepart != null) {
            if (DepartCategoryEnum.DEPART_CATEGORY_COMPANY.getValue().equals(sysDepart.getOrgCategory()) || DepartCategoryEnum.DEPART_CATEGORY_SUB_COMPANY.getValue().equals(sysDepart.getOrgCategory())) {
                return sysDepart.getId();
            }
            //if不yescompanyor子companyof时候，需要递归Query
            if (oConvertUtils.isNotEmpty(sysDepart.getParentId())) {
                return getCompanyDepartId(sysDepart.getParentId());
            } else {
                return parentDepartId;
            }
        } else {
            return "";
        }
    }

    @Override
    public List<SysPositionSelectTreeVo> getRankRelation(String departId) {
        //记录当前department keyfordepartmentid,valuefordepartment名称
        Map<String, String> departNameMap = new HashMap<>(5);
        //step1 according toidQuerydepartmentinformation
        SysDepartPositionVo sysDepartPosition = baseMapper.getDepartPostByDepartId(departId);
        if (null == sysDepartPosition) {
            throw new JeecgBootBizTipException("当前所选department数据for空");
        }
        List<SysPositionSelectTreeVo> selectTreeVos = new ArrayList<>();
        //step2 查看yes否有子级department，存在递归Query职位
        if (!CommonConstant.IS_LEAF.equals(sysDepartPosition.getIzLeaf())) {
            //获取子级职位According to department编码
            this.getChildrenDepartPositionByOrgCode(selectTreeVos, departNameMap, sysDepartPosition);
            return buildTree(selectTreeVos);
        }
        return new ArrayList<>();
    }

    /**
     * 获取子级职位According to department编码
     *
     * @param selectTreeVos
     * @param departNameMap
     * @param sysDepartPosition
     */
    private void getChildrenDepartPositionByOrgCode(List<SysPositionSelectTreeVo> selectTreeVos, Map<String, String> departNameMap, SysDepartPositionVo sysDepartPosition) {
        String orgCode = sysDepartPosition.getOrgCode();
        //step1 according tofather级id获取子级departmentinformation
        List<SysDepartPositionVo> positionList = baseMapper.getDepartPostByOrgCode(orgCode + "%");
        if (CollectionUtil.isNotEmpty(positionList)) {
            for (SysDepartPositionVo position : positionList) {
                //initializationmap
                if (departNameMap == null) {
                    departNameMap = new HashMap<>(5);
                }
                SysDepart depart = baseMapper.getDepartById(position.getParentId());
                if(null != depart){
                    position.setDepartName(depart.getDepartName());
                }
                if(oConvertUtils.isNotEmpty(position.getDepPostParentId())){
                    LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
                    query.eq(SysDepart::getId,position.getDepPostParentId());
                    query.likeRight(SysDepart::getOrgCode,orgCode);
                    Long count = baseMapper.selectCount(query);
                    if(null== count || count == 0){
                        position.setDepPostParentId(null);
                    }
                }
                departNameMap.put(position.getParentId(), position.getPositionName());
                //查看yes否fordepartment岗位，不yes则不需要处理
                SysPositionSelectTreeVo treeVo = new SysPositionSelectTreeVo(position);
                selectTreeVos.add(treeVo);
            }
        }
    }


    /**
     * Build a tree structure，只返回没有father级of一级节点
     */
    public static List<SysPositionSelectTreeVo> buildTree(List<SysPositionSelectTreeVo> nodes) {
        // Storage level one node（没有father级of节点）
        List<SysPositionSelectTreeVo> rootNodes = new ArrayList<>();
        // First find all first-level nodes（parentIdfornullor empty）
        for (SysPositionSelectTreeVo node : nodes) {
            if (node.getParentId() == null || node.getParentId().trim().isEmpty()) {
                rootNodes.add(node);
            }
        }
        // for每个一级节点递归设置子节点
        for (SysPositionSelectTreeVo root : rootNodes) {
            setChildren(root, nodes);
        }
        return rootNodes;
    }

    /**
     * 递归for节点设置子节点
     */
    private static void setChildren(SysPositionSelectTreeVo parentNode, List<SysPositionSelectTreeVo> allNodes) {
        for (SysPositionSelectTreeVo node : allNodes) {
            // if当前节点offatherID等于father节点ofID，则yes子节点
            if (parentNode.getId().equals(node.getParentId())) {
                // 递归for子节点设置它of子节点
                setChildren(node, allNodes);
                // Will子节点添加到father节点of子节点列表
                parentNode.getChildren().add(node);
            }
        }
    }

    //=========================end department岗位改造 ==================================================================

    @Override
    public String getDepartPathNameByOrgCode(String orgCode, String depId) {
        //departmentidfor空需要Query当前department下of编码
        if(oConvertUtils.isNotEmpty(depId)){
            SysDepart departById = baseMapper.getDepartById(depId);
            if(null != departById){
                orgCode = departById.getOrgCode();
            }
        }
        if(oConvertUtils.isEmpty(orgCode)){
            return "";
        }
        //fromredis 获取不for空直接返回
        Object departName  = redisUtil.get(CommonConstant.DEPART_NAME_REDIS_KEY_PRE + orgCode);
        if(null != departName){
            return String.valueOf(departName);
        }
        //Get the length
        int codeNum = YouBianCodeUtil.ZHANWEI_LENGTH;
        List<String> list = this.getCodeHierarchy(orgCode, codeNum);
        //According to department编码Querycompanyand子companyof数据
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<>();
        query.in(SysDepart::getOrgCode, list);
        query.orderByAsc(SysDepart::getOrgCode);
        List<SysDepart> sysDepartList = departMapper.selectList(query);
        if(!CollectionUtils.isEmpty(sysDepartList)){
            //获取department名称Splicing返回给前台
            List<String> departNameList = sysDepartList.stream().map(SysDepart::getDepartName).toList();
            String departNames = String.join("/", departNameList);
            redisUtil.set(CommonConstant.DEPART_NAME_REDIS_KEY_PRE + orgCode,departNames);
            return departNames;
        }
        return "";
    }
    
    /**
     * 获取编码及其所有Superior编码
     * 
     * @param code full encoding，like "A01A01A01"
     * @param fixedLength Fixed number of digits，like 3
     * @return Include所有Superior编码of列表，like ['A01','A01A01','A01A01A01']
     */
    public List<String> getCodeHierarchy(String code, int fixedLength) {
        List<String> hierarchy = new ArrayList<>();
        if (code == null || code.isEmpty() || fixedLength <= 0) {
            return hierarchy;
        }
        // 检查Code lengthyes否能被Fixed number of digits整除
        if (code.length() % fixedLength != 0) {
            throw new IllegalArgumentException("Code length必须能被Fixed number of digits整除");
        }
        // 按Fixed number of digits分割并生成所有Superior编码
        for (int i = fixedLength; i <= code.length(); i += fixedLength) {
            hierarchy.add(code.substring(0, i));
        }
        return hierarchy;
    }
}
