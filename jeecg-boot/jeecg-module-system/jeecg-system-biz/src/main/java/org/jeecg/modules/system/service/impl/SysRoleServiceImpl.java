package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.mapper.SysRoleMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * character sheet Service implementation class
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    @Autowired
    SysRoleMapper sysRoleMapper;
    @Autowired
    SysUserMapper sysUserMapper;

    
    @Override
    public Page<SysRole> listAllSysRole(Page<SysRole> page, SysRole role) {
        return page.setRecords(sysRoleMapper.listAllSysRole(page,role));
    }

    @Override
    public SysRole getRoleNoTenant(String roleCode) {
        return sysRoleMapper.getRoleNoTenant(roleCode);
    }

    @Override
    public Result importExcelCheckRoleCode(MultipartFile file, ImportParams params) throws Exception {
        List<Object> listSysRoles = ExcelImportUtil.importExcel(file.getInputStream(), SysRole.class, params);
        int totalCount = listSysRoles.size();
        List<String> errorStrs = new ArrayList<>();

        // remove listSysRoles Duplicate data in
        for (int i = 0; i < listSysRoles.size(); i++) {
            String roleCodeI =((SysRole)listSysRoles.get(i)).getRoleCode();
            for (int j = i + 1; j < listSysRoles.size(); j++) {
                String roleCodeJ =((SysRole)listSysRoles.get(j)).getRoleCode();
                // Duplicate data found
                if (roleCodeI.equals(roleCodeJ)) {
                    errorStrs.add("No. " + (j + 1) + " OK roleCode value：" + roleCodeI + " Already exists，Ignore import");
                    listSysRoles.remove(j);
                    break;
                }
            }
        }
        // remove sql Duplicate data in
        Integer errorLines=0;
        Integer successLines=0;
        List<String> list = ImportExcelUtil.importDateSave(listSysRoles, ISysRoleService.class, errorStrs, CommonConstant.SQL_INDEX_UNIQ_SYS_ROLE_CODE);
         errorLines+=list.size();
         successLines+=(listSysRoles.size()-errorLines);
        return ImportExcelUtil.imporReturnRes(errorLines,successLines,list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(String roleid) {
        //1.Delete roles and user relationships
        sysRoleMapper.deleteRoleUserRelation(roleid);
        //2.Delete role and permission relationships
        sysRoleMapper.deleteRolePermissionRelation(roleid);
        //3.Delete role
        this.removeById(roleid);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatchRole(String[] roleIds) {
        //1.Delete roles and user relationships
        sysUserMapper.deleteBathRoleUserRelation(roleIds);
        //2.Delete role and permission relationships
        sysUserMapper.deleteBathRolePermissionRelation(roleIds);
        //3.Delete role
        this.removeByIds(Arrays.asList(roleIds));
        return true;
    }

    @Override
    public Long getRoleCountByTenantId(String id, Integer tenantId) {
        return sysRoleMapper.getRoleCountByTenantId(id,tenantId);
    }

    @Override
    public void checkAdminRoleRejectDel(String ids) {
        LambdaQueryWrapper<SysRole> query = new  LambdaQueryWrapper<>();
        query.in(SysRole::getId,Arrays.asList(ids.split(SymbolConstant.COMMA)));
        query.eq(SysRole::getRoleCode,"admin");
        Long adminRoleCount = sysRoleMapper.selectCount(query);
        if(adminRoleCount>0){
            throw new JeecgBootException("adminRole，Delete not allowed！");
        }
    }
}
