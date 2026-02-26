package org.jeecg.modules.system.service.impl;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.TenantConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.aop.TenantLog;
import org.jeecg.modules.system.entity.SysPackPermission;
import org.jeecg.modules.system.entity.SysTenantPack;
import org.jeecg.modules.system.entity.SysTenantPackUser;
import org.jeecg.modules.system.entity.SysUserTenant;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.service.ISysTenantPackService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: Tenant product package
 * @Author: jeecg-boot
 * @Date: 2022-12-31
 * @Version: V1.0
 */
@Service
public class SysTenantPackServiceImpl extends ServiceImpl<SysTenantPackMapper, SysTenantPack> implements ISysTenantPackService {

    @Autowired
    private SysTenantPackMapper sysTenantPackMapper;

    @Autowired
    private SysTenantPackUserMapper sysTenantPackUserMapper;

    @Autowired
    private SysPackPermissionMapper sysPackPermissionMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserTenantMapper sysUserTenantMapper;
    
    @Override
    public void addPackPermission(SysTenantPack sysTenantPack) {
        //If it is the default tenant package，You need to setcodecoding，Use it when editing the default package to find a custom package
        if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
            String packCode = CommonConstant.TENANT_PACK_DEFAULT + RandomUtil.randomNumbers(4).toLowerCase();
            sysTenantPack.setPackCode(packCode);
        }
        sysTenantPackMapper.insert(sysTenantPack);
        String permissionIds = sysTenantPack.getPermissionIds();
        if (oConvertUtils.isNotEmpty(permissionIds)) {
            String packId = sysTenantPack.getId();
            String[] permissionIdArray = permissionIds.split(SymbolConstant.COMMA);
            for (String permissionId : permissionIdArray) {
                this.addPermission(packId, permissionId);
            }
        }

        //If it is a customized package, the package and user relationship will be added.
        if(!CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())) {
            //If you need to automatically assign it to users, add the relationship data between users and packages.
            if(oConvertUtils.isNotEmpty(sysTenantPack.getIzSysn()) && CommonConstant.STATUS_1.equals(sysTenantPack.getIzSysn())) {
                //According to tenantidand packagesidAdd user and package relationship data
                this.addPackUserByPackTenantId(sysTenantPack.getTenantId(), sysTenantPack.getId());
            }
        }
    }

    /**
     * According to tenantidand packagesidAdd user and package relationship data
     *
     * @param tenantId
     * @param packId
     */
    private void addPackUserByPackTenantId(Integer tenantId, String packId) {
        if (null != tenantId && tenantId != 0) {
            List<String> userIds = sysUserTenantMapper.getUserIdsByTenantId(tenantId);
            if (CollectionUtil.isNotEmpty(userIds)) {
                //update-begin---author:wangshuai---date:2025-09-03---for: When editing, you need to check if there are any unassigned users.---
                // Query existing users
                LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<>();
                query.eq(SysTenantPackUser::getTenantId, tenantId);
                query.eq(SysTenantPackUser::getPackId, packId);
                query.in(SysTenantPackUser::getUserId, userIds);
                List<SysTenantPackUser> existingUsers = sysTenantPackUserMapper.selectList(query);
                // Extract existing usersID
                List<String> existingUserIds = existingUsers.stream()
                        .map(SysTenantPackUser::getUserId)
                        .toList();
                // Filter out users who need to be addedID
                List<String> newUserIds = userIds.stream()
                        .filter(userId -> !existingUserIds.contains(userId))
                        .toList();
                for (String userId : newUserIds) {
                //update-end---author:wangshuai---date:2025-09-03---for: When editing, you need to check if there are any unassigned users.---
                    SysTenantPackUser tenantPackUser = new SysTenantPackUser(tenantId, packId, userId);
                    sysTenantPackUserMapper.insert(tenantPackUser);
                }
            }
        }
    }

    @Override
    public List<SysTenantPack> setPermissions(List<SysTenantPack> records) {
        for (SysTenantPack pack : records) {
            List<String> permissionIds = sysPackPermissionMapper.getPermissionsByPackId(pack.getId());
            if (null != permissionIds && permissionIds.size() > 0) {
                String ids = String.join(SymbolConstant.COMMA, permissionIds);
                pack.setPermissionIds(ids);
            }
        }
        return records;
    }

    @Override
    public void editPackPermission(SysTenantPack sysTenantPack) {
        //Database summaryid
        List<String> oldPermissionIds = sysPackPermissionMapper.getPermissionsByPackId(sysTenantPack.getId());
        //The information sent from the front desk needs to be modified.id
        String permissionIds = sysTenantPack.getPermissionIds();
        //If the menu passedidis empty，Then delete all menus in the database
        if (oConvertUtils.isEmpty(permissionIds)) {
            this.deletePackPermission(sysTenantPack.getId(), null);
            //If it is the default package，It is necessary to delete the relationship between roles and menus under other associated default product packages.
            if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
                this.deleteDefaultPackPermission(sysTenantPack.getPackCode(), null);
            }
        } else if (oConvertUtils.isNotEmpty(permissionIds) && oConvertUtils.isEmpty(oldPermissionIds)) {
            //If the menu passedid不is empty但是数据库的菜单idis empty，Then add
            this.addPermission(sysTenantPack.getId(), permissionIds);
            //If it is the default package，It is necessary to add other relationships between roles and menus under the default product package.
            if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
                this.addDefaultPackPermission(sysTenantPack.getPackCode(), permissionIds);
            }
        } else {
            //都不is empty，Need to compare，Add or delete
            if (oConvertUtils.isNotEmpty(oldPermissionIds)) {
                //Find new tenantsidwith original tenantidDifferences，Delete
                List<String> permissionList = oldPermissionIds.stream().filter(item -> !permissionIds.contains(item)).collect(Collectors.toList());
                if (permissionList.size() > 0) {
                    for (String permission : permissionList) {
                        this.deletePackPermission(sysTenantPack.getId(), permission);
                        //If it is the default package，It is necessary to delete the relationship between roles and menus under other associated default product packages.
                        if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
                            this.deleteDefaultPackPermission(sysTenantPack.getPackCode(), permission);
                        }
                    }
                }

                //Find the original menuidwith new menuidDifferences,Add new
                List<String> permissionAddList = Arrays.stream(permissionIds.split(SymbolConstant.COMMA)).filter(item -> !oldPermissionIds.contains(item)).collect(Collectors.toList());
                if (permissionAddList.size() > 0) {
                    for (String permission : permissionAddList) {
                        this.addPermission(sysTenantPack.getId(), permission);
                        //If it is the default package，It is necessary to add other relationships between roles and menus under the default product package.
                        if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
                            this.addDefaultPackPermission(sysTenantPack.getPackCode(), permission);
                        }
                    }
                }
            }
        }
        sysTenantPackMapper.updateById(sysTenantPack);
        //If it is the default package，Then update the data matching the current matching default package.
        if(CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())){
            //Synchronize with packCode Related package data under
            this.syncRelatedPackDataByDefaultPack(sysTenantPack);
        }

        //If it is a customized package, the package and user relationship will be added.
        if(!CommonConstant.TENANT_PACK_DEFAULT.equals(sysTenantPack.getPackType())) {
            //If you need to automatically assign it to users, add the relationship data between users and packages.
            if(oConvertUtils.isNotEmpty(sysTenantPack.getIzSysn()) && CommonConstant.STATUS_1.equals(sysTenantPack.getIzSysn())) {
                //According to tenantidand packagesidAdd user and package relationship data
                this.addPackUserByPackTenantId(sysTenantPack.getTenantId(), sysTenantPack.getId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenantPack(String ids) {
        String[] idsArray = ids.split(SymbolConstant.COMMA);
        for (String id : idsArray) {
            this.deletePackPermission(id,null);
            //Delete users under product packages
            this.deletePackUser(id);
            sysTenantPackMapper.deleteById(id);
        }
    }

    @Override
    public void exitTenant(String tenantId, String userId) {
        this.getById(tenantId);
    }

    @Override
    public void addDefaultTenantPack(Integer tenantId) {
        ISysTenantPackService currentService = SpringContextUtils.getApplicationContext().getBean(ISysTenantPackService.class);
        // Create a tenant super administrator
        SysTenantPack superAdminPack = new SysTenantPack(tenantId, "super administrator", TenantConstant.SUPER_ADMIN);
        superAdminPack.setIzSysn(CommonConstant.STATUS_0);
        //step.1 Create a tenant package（super administrator）
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPack::getTenantId,tenantId);
        query.eq(SysTenantPack::getPackCode, TenantConstant.SUPER_ADMIN);
        SysTenantPack sysTenantPackSuperAdmin = currentService.getOne(query);
        String packId = "";
        if(null == sysTenantPackSuperAdmin){
            packId = currentService.saveOne(superAdminPack);
        }else{
            packId = sysTenantPackSuperAdmin.getId();
        }
        //step.1.2 Supplement the relationship data between personnel and packages
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        SysTenantPackUser packUser = new SysTenantPackUser(tenantId, packId, sysUser.getId());
        packUser.setRealname(sysUser.getRealname());
        packUser.setPackName(superAdminPack.getPackName());
        currentService.savePackUser(packUser);
        
        //step.2 Create a tenant package(Organization Account Administrator)and Add personnel relationship data
        query.eq(SysTenantPack::getTenantId,tenantId);
        query.eq(SysTenantPack::getPackCode, TenantConstant.ACCOUNT_ADMIN);
        SysTenantPack sysTenantPackAccountAdmin = currentService.getOne(query);
        if(null == sysTenantPackAccountAdmin){
            // 创建super administrator
            SysTenantPack accountAdminPack = new SysTenantPack(tenantId, "Organization Account Administrator", TenantConstant.ACCOUNT_ADMIN);
            accountAdminPack.setIzSysn(CommonConstant.STATUS_0);
            currentService.saveOne(accountAdminPack);
        }

        //step.3 Create a tenant package(Organization application administrator)
        query.eq(SysTenantPack::getTenantId,tenantId);
        query.eq(SysTenantPack::getPackCode, TenantConstant.APP_ADMIN);
        SysTenantPack sysTenantPackAppAdmin = currentService.getOne(query);
        if(null == sysTenantPackAppAdmin){
            // 创建super administrator
            SysTenantPack appAdminPack = new SysTenantPack(tenantId, "Organization application administrator", TenantConstant.APP_ADMIN);
            appAdminPack.setIzSysn(CommonConstant.STATUS_0);
            currentService.saveOne(appAdminPack);
        }
        
    }

    @TenantLog(2)
    @Override
    public String saveOne(SysTenantPack sysTenantPack) {
        sysTenantPackMapper.insert(sysTenantPack);
        return sysTenantPack.getId();
    }

    @TenantLog(2)
    @Override
    public void savePackUser(SysTenantPackUser sysTenantPackUser) {
        sysTenantPackUser.setStatus(1);
        sysTenantPackUserMapper.insert(sysTenantPackUser);
    }

    @Override
    public SysTenantPack getSysTenantPack(Integer tenantId, String packCode) {
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<SysTenantPack>()
                .eq(SysTenantPack::getPackCode, packCode)
                .eq(SysTenantPack::getTenantId, tenantId);
        List<SysTenantPack> list = baseMapper.selectList(query);
        if(list!=null && list.size()>0){
            SysTenantPack pack = list.get(0);
            if(pack!=null && pack.getId()!=null){
                return pack;
            }
        }
        return null;
    }

    /**
     * Add menu
     *
     * @param packId
     * @param permissionId
     */
    public void addPermission(String packId, String permissionId) {
        SysPackPermission permission = new SysPackPermission();
        permission.setPermissionId(permissionId);
        permission.setPackId(packId);
        sysPackPermissionMapper.insert(permission);
    }

    /**
     * According to package nameidand菜单idDelete relational table
     *
     * @param packId
     * @param permissionId
     */
    public void deletePackPermission(String packId, String permissionId) {
        LambdaQueryWrapper<SysPackPermission> query = new LambdaQueryWrapper<>();
        query.eq(SysPackPermission::getPackId, packId);
        if (oConvertUtils.isNotEmpty(permissionId)) {
            query.eq(SysPackPermission::getPermissionId, permissionId);
        }
        sysPackPermissionMapper.delete(query);
    }

    @Override
    public void addTenantDefaultPack(Integer tenantId) {
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPack::getPackType,"default");
        List<SysTenantPack> sysTenantPacks = sysTenantPackMapper.selectList(query);
        for (SysTenantPack sysTenantPack: sysTenantPacks) {
            syncDefaultPack2CurrentTenant(tenantId, sysTenantPack);
        }
    }

    @Override
    public void syncDefaultPack(Integer tenantId) {
        // Query the default package
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPack::getPackType,"default");
        List<SysTenantPack> sysDefaultTenantPacks = sysTenantPackMapper.selectList(query);
        // Query current tenant packages
        query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPack::getPackType,"custom");
        query.eq(SysTenantPack::getTenantId, tenantId);
        List<SysTenantPack> currentTenantPacks = sysTenantPackMapper.selectList(query);
        Map<String, SysTenantPack> currentTenantPackMap = new HashMap<String, SysTenantPack>();
        if (oConvertUtils.listIsNotEmpty(currentTenantPacks)) {
            currentTenantPackMap = currentTenantPacks.stream().collect(Collectors.toMap(SysTenantPack::getPackName, o -> o, (existing, replacement) -> existing));
        }
        // Add a package that does not exist
        for (SysTenantPack defaultPacks : sysDefaultTenantPacks) {
            if(!currentTenantPackMap.containsKey(defaultPacks.getPackName())){
                syncDefaultPack2CurrentTenant(tenantId, defaultPacks);
            }
        }
    }

    /**
     * Synchronize the default package to the current tenant
     * for [QQYUN-11032]【jeecg】Added initialization package button to tenant package management
     * @param tenantId target tenant
     * @param defaultPacks Default package
     * @author chenrui
     * @date 2025/2/5 19:41
     */
    private void syncDefaultPack2CurrentTenant(Integer tenantId, SysTenantPack defaultPacks) {
        SysTenantPack pack = new SysTenantPack();
        BeanUtils.copyProperties(defaultPacks,pack);
        pack.setTenantId(tenantId);
        pack.setPackType("custom");
        pack.setId("");
        sysTenantPackMapper.insert(pack);
        List<String> permissionsByPackId = sysPackPermissionMapper.getPermissionsByPackId(defaultPacks.getId());
        for (String permission:permissionsByPackId) {
            SysPackPermission packPermission = new SysPackPermission();
            packPermission.setPackId(pack.getId());
            packPermission.setPermissionId(permission);
            sysPackPermissionMapper.insert(packPermission);
        }
        //If you need to automatically assign it to users, add the relationship data between users and packages.
        if(oConvertUtils.isNotEmpty(defaultPacks.getIzSysn()) && CommonConstant.STATUS_1.equals(defaultPacks.getIzSysn())) {
            //Query users under the current tenant
            List<String> userIds = sysUserTenantMapper.getUserIdsByTenantId(tenantId);
            if (oConvertUtils.isNotEmpty(userIds)) {
                for (String userId : userIds) {
                    //According to tenantidand packagesidAdd user and package relationship data
                    SysTenantPackUser tenantPackUser = new SysTenantPackUser(tenantId, pack.getId(), userId);
                    sysTenantPackUserMapper.insert(tenantPackUser);
                }
            }
        }
    }

    /**
     * Delete users under product packages
     * @param packId
     */
    private void deletePackUser(String packId) {
        LambdaQueryWrapper<SysTenantPackUser> query = new LambdaQueryWrapper<>();
        query.eq(SysTenantPackUser::getPackId, packId);
        sysTenantPackUserMapper.delete(query);
    }

    @Override
    public List<String> getPackIdByUserIdAndTenantId(String userId, Integer tenantId) {
        return sysTenantPackUserMapper.getPackIdByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public List<SysTenantPack> getPackListByTenantId(String tenantId) {
        return sysTenantPackUserMapper.getPackListByTenantId(oConvertUtils.getInt(tenantId));
    }

    /**
     * According to the packagecode 新增其他关联默认产品包下的角色与菜单的关系
     *
     * @param packCode
     * @param permission
     */
    private void addDefaultPackPermission(String packCode, String permission) {
        if (oConvertUtils.isEmpty(packCode)) {
            return;
        }
        //查询当前匹配非Default package的其他Default package
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.ne(SysTenantPack::getPackType, CommonConstant.TENANT_PACK_DEFAULT);
        query.eq(SysTenantPack::getPackCode, packCode);
        List<SysTenantPack> otherDefaultPacks = sysTenantPackMapper.selectList(query);
        for (SysTenantPack pack : otherDefaultPacks) {
            //Added menu permissions for package package users
            this.addPermission(pack.getId(), permission);
        }
    }

    /**
     * According to the packagecode 删除其他关联Default package下的角色与菜单的关系
     *
     * @param packCode
     * @param permissionId
     */
    private void deleteDefaultPackPermission(String packCode, String permissionId) {
        if (oConvertUtils.isEmpty(packCode)) {
            return;
        }
        //查询当前匹配非Default package的其他Default package
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.ne(SysTenantPack::getPackType, CommonConstant.TENANT_PACK_DEFAULT);
        query.eq(SysTenantPack::getPackCode, packCode);
        List<SysTenantPack> defaultPacks = sysTenantPackMapper.selectList(query);
        for (SysTenantPack pack : defaultPacks) {
            //Delete package permissions
            deletePackPermission(pack.getId(), permissionId);
        }
    }

    /**
     * Synchronize with packCode Related package data under
     *
     * @param sysTenantPack
     */
    private void syncRelatedPackDataByDefaultPack(SysTenantPack sysTenantPack) {
        //The query is the same as the default packagecodepackage
        LambdaQueryWrapper<SysTenantPack> query = new LambdaQueryWrapper<>();
        query.ne(SysTenantPack::getPackType, CommonConstant.TENANT_PACK_DEFAULT);
        query.eq(SysTenantPack::getPackCode, sysTenantPack.getPackCode());
        List<SysTenantPack> relatedPacks = sysTenantPackMapper.selectList(query);
        for (SysTenantPack pack : relatedPacks) {
            //Update custom package
            pack.setPackName(sysTenantPack.getPackName());
            pack.setStatus(sysTenantPack.getStatus());
            pack.setRemarks(sysTenantPack.getRemarks());
            pack.setIzSysn(sysTenantPack.getIzSysn());
            sysTenantPackMapper.updateById(pack);
            //Synchronize all users under the default package
            if (oConvertUtils.isNotEmpty(sysTenantPack.getIzSysn()) && CommonConstant.STATUS_1.equals(sysTenantPack.getIzSysn())) {
                this.addPackUserByPackTenantId(pack.getTenantId(), pack.getId());
            }
        }
    }
}
