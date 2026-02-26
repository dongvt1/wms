package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.mapper.SysDepartPermissionMapper;
import org.jeecg.modules.system.mapper.SysDepartRolePermissionMapper;
import org.jeecg.modules.system.mapper.SysPermissionMapper;
import org.jeecg.modules.system.mapper.SysRolePermissionMapper;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.ISysPermissionDataRuleService;
import org.jeecg.modules.system.service.ISysPermissionService;
import org.jeecg.modules.system.service.ISysRoleIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * <p>
 * Menu permission table Service implementation class
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

	@Resource
	private SysPermissionMapper sysPermissionMapper;
	
	@Resource
	private ISysPermissionDataRuleService permissionDataRuleService;

	@Resource
	private SysRolePermissionMapper sysRolePermissionMapper;

	@Resource
	private SysDepartPermissionMapper sysDepartPermissionMapper;

	@Resource
	private SysDepartRolePermissionMapper sysDepartRolePermissionMapper;

	@Autowired
	private ISysRoleIndexService roleIndexService;

	@Override
	public void switchVue3Menu() {
		sysPermissionMapper.backupVue2Menu();
		sysPermissionMapper.changeVue3Menu();
	}

	@Override
	public List<TreeModel> queryListByParentId(String parentId) {
		return sysPermissionMapper.queryListByParentId(parentId);
	}

	/**
	  * true deletion
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	public void deletePermission(String id) throws JeecgBootException {
		SysPermission sysPermission = this.getById(id);
		if(sysPermission==null) {
			throw new JeecgBootException("Menu information not found");
		}
		String pid = sysPermission.getParentId();
		if(oConvertUtils.isNotEmpty(pid)) {
			Long count = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, pid));
			if(count==1) {
				//If the parent node has no other child nodes，Then the parent node is a leaf node
				this.sysPermissionMapper.setMenuLeaf(pid, 1);
			}
		}
		sysPermissionMapper.deleteById(id);
		// The node may be a child node but may also be the parent node of other nodes.,So cascade deletion is needed
		this.removeChildrenBy(sysPermission.getId());
		//Association deletion
		Map map = new HashMap(5);
		map.put("permission_id",id);
		//Delete data rules
		this.deletePermRuleByPermId(id);
		//Delete role authorization table
		sysRolePermissionMapper.deleteByMap(map);
		//Delete department permission table
		sysDepartPermissionMapper.deleteByMap(map);
		//Delete department role authorization
		sysDepartRolePermissionMapper.deleteByMap(map);
	}
	
	/**
	 * according to parentidDelete its associated child node data
	 * 
	 * @return
	 */
	public void removeChildrenBy(String parentId) {
		LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<>();
		// Encapsulate query conditionsparentIdprimary key,
		query.eq(SysPermission::getParentId, parentId);
		// Find all children under the primary key
		List<SysPermission> permissionList = this.list(query);
		if (permissionList != null && permissionList.size() > 0) {
            // id
			String id = "";
            // The number of children detected
			Long num = Long.valueOf(0);
			// If the checked set is not empty, then delete all
			this.remove(query);
			// Traverse the collection just found again, According to each object,Find if it still has children
			for (int i = 0, len = permissionList.size(); i < len; i++) {
				id = permissionList.get(i).getId();
				Map map = new HashMap(5);
				map.put("permission_id",id);
				//Delete data rules
				this.deletePermRuleByPermId(id);
				//Delete role authorization table
				sysRolePermissionMapper.deleteByMap(map);
				//Delete department permission table
				sysDepartPermissionMapper.deleteByMap(map);
				//Delete department role authorization
				sysDepartRolePermissionMapper.deleteByMap(map);
				num = this.count(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, id));
				// if there is, then recursion
				if (num > 0) {
					this.removeChildrenBy(id);
				}
			}
		}
	}
	
	/**
	  * tombstone
	 */
	@Override
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	//@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true,condition="#sysPermission.menuType==2")
	public void deletePermissionLogical(String id) throws JeecgBootException {
		SysPermission sysPermission = this.getById(id);
		if(sysPermission==null) {
			throw new JeecgBootException("Menu information not found");
		}
		String pid = sysPermission.getParentId();
		Long count = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, pid));
		if(count==1) {
			//If the parent node has no other child nodes，Then the parent node is a leaf node
			this.sysPermissionMapper.setMenuLeaf(pid, 1);
		}
		sysPermission.setDelFlag(1);
		this.updateById(sysPermission);
	}

	@Override
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	public void addPermission(SysPermission sysPermission) throws JeecgBootException {
		//----------------------------------------------------------------------
		//Determine whether it is a first-level menu，If yes, clear the parent menu
		if(CommonConstant.MENU_TYPE_0.equals(sysPermission.getMenuType())) {
			sysPermission.setParentId(null);
		}
		//----------------------------------------------------------------------
		String pid = sysPermission.getParentId();
		if(oConvertUtils.isNotEmpty(pid)) {
			//Set the parent node not to be a leaf node
			this.sysPermissionMapper.setMenuLeaf(pid, 0);
		}
		sysPermission.setCreateTime(new Date());
		sysPermission.setDelFlag(0);
		sysPermission.setLeaf(true);
		this.save(sysPermission);
	}

	@Override
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	public void editPermission(SysPermission sysPermission) throws JeecgBootException {
		SysPermission p = this.getById(sysPermission.getId());
		//TODO This node determines whether there are any child nodes
		if(p==null) {
			throw new JeecgBootException("Menu information not found");
		}else {
			sysPermission.setUpdateTime(new Date());
			//----------------------------------------------------------------------
			//Step1.Determine whether it is a first-level menu，If yes, clear the parent menuID
			if(CommonConstant.MENU_TYPE_0.equals(sysPermission.getMenuType())) {
				sysPermission.setParentId("");
			}
			//Step2.Determine whether there is a menu below the menu，If none, set to leaf node
			Long count = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, sysPermission.getId()));
			if(count==0) {
				sysPermission.setLeaf(true);
			}
			//----------------------------------------------------------------------
			this.updateById(sysPermission);
			
			//If the parent menu of the current menu changes，You need to modify the new parent menu and the old parent menu.，Leaf node status
			String pid = sysPermission.getParentId();
            boolean flag = (oConvertUtils.isNotEmpty(pid) && !pid.equals(p.getParentId())) || oConvertUtils.isEmpty(pid)&&oConvertUtils.isNotEmpty(p.getParentId());
            if (flag) {
				//a.Set the new parent menu not to be a leaf node
				this.sysPermissionMapper.setMenuLeaf(pid, 0);
				//b.Determine whether there are other submenus under the old menu，If not, set it as a leaf node.
				Long cc = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, p.getParentId()));
				if(cc==0) {
					if(oConvertUtils.isNotEmpty(p.getParentId())) {
						this.sysPermissionMapper.setMenuLeaf(p.getParentId(), 1);
					}
				}
				
			}

			// Change default menu synchronously
			SysRoleIndex defIndexCfg = this.roleIndexService.queryDefaultIndex();
			boolean isDefIndex = defIndexCfg.getUrl().equals(p.getUrl());
			if (isDefIndex) {
				this.roleIndexService.updateDefaultIndex(sysPermission.getUrl(), sysPermission.getComponent(), sysPermission.isRoute());
			}

		}
		
	}

	@Override
	public List<SysPermission> queryByUser(String userId) {
		List<SysPermission> permissionList = this.sysPermissionMapper.queryByUser(userId);
		//================= begin When opening a tenant if nottestRole，Join by defaulttestRole================
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			if (permissionList == null) {
				permissionList = new ArrayList<>();
			}
			List<SysPermission> testRoleList = sysPermissionMapper.queryPermissionByTestRoleId();
			//update-begin-author:liusq date:20230427 for: [QQYUN-5168]【vue3】Why do two menus appear? Menu based onidRemove duplicates
			for (SysPermission permission: testRoleList) {
				boolean hasPerm = permissionList.stream().anyMatch(a->a.getId().equals(permission.getId()));
				if(!hasPerm){
					permissionList.add(permission);
				}
			}
			//update-end-author:liusq date:20230427 for: [QQYUN-5168]【vue3】Why do two menus appear? Menu based onidRemove duplicates
		}
		//================= end When opening a tenant if nottestRole，Join by defaulttestRole================
		return permissionList;
	}

	/**
	 * according topermissionIdDelete its associatedSysPermissionDataRuledata in table
	 */
	@Override
	public void deletePermRuleByPermId(String id) {
		LambdaQueryWrapper<SysPermissionDataRule> query = new LambdaQueryWrapper<>();
		query.eq(SysPermissionDataRule::getPermissionId, id);
		Long countValue = this.permissionDataRuleService.count(query);
		if(countValue > 0) {
			this.permissionDataRuleService.remove(query);	
		}
	}

	/**
	  *   Obtain data permissions for fuzzy matching rulesURL
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE)
	public List<String> queryPermissionUrlWithStar() {
		return this.baseMapper.queryPermissionUrlWithStar();
	}

	@Override
	public boolean hasPermission(String username, SysPermission sysPermission) {
		int count = baseMapper.queryCountByUsername(username,sysPermission);
		if(count>0){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean hasPermission(String username, String url) {
		SysPermission sysPermission = new SysPermission();
		sysPermission.setUrl(url);
		int count = baseMapper.queryCountByUsername(username,sysPermission);
		if(count>0){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public List<SysPermission> queryDepartPermissionList(String departId) {
		return sysPermissionMapper.queryDepartPermissionList(departId);
	}

	@Override
	public boolean checkPermDuplication(String id, String url,Boolean alwaysShow) {
		QueryWrapper<SysPermission> qw=new QueryWrapper();
		qw.lambda().eq(true,SysPermission::getUrl,url).ne(oConvertUtils.isNotEmpty(id),SysPermission::getId,id).eq(true,SysPermission::isAlwaysShow,alwaysShow);
		return count(qw)==0;
	}

}
