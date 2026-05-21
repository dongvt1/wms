package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysTenantPack;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysTenantPackUser;

import java.util.List;

/**
 * @Description: Tenant product package
 * @Author: jeecg-boot
 * @Date:   2022-12-31
 * @Version: V1.0
 */
public interface ISysTenantPackService extends IService<SysTenantPack> {

    /**
     * Add a new product package and insert the menu into the relationship table
     * @param sysTenantPack
     */
    void addPackPermission(SysTenantPack sysTenantPack);

    /**
     * Settings menuid
     * @param records
     * @return
     */
    List<SysTenantPack> setPermissions(List<SysTenantPack> records);

    /**
     * Edit the product package and insert the menu into the relationship table
     * @param sysTenantPack
     */
    void editPackPermission(SysTenantPack sysTenantPack);

    /**
     * 删除Tenant product package
     * @param ids
     */
    void deleteTenantPack(String ids);

    /**
     * Exit tenant
     * @param tenantId
     * @param s
     */
    void exitTenant(String tenantId, String s);

    /**
     * Created by default when creating a tenant3indivual product package
     * @param tenantId
     */
    void addDefaultTenantPack(Integer tenantId);

    /**
     * 保存product package
     * @param sysTenantPack
     */
    String saveOne(SysTenantPack sysTenantPack);


    /**
     * 保存product package和人员的关系
     * @param sysTenantPackUser
     */
    void savePackUser(SysTenantPackUser sysTenantPackUser);

    /**
     * According to tenantIDand encoding query
     * @param tenantId
     * @param packCode
     * @return
     */
    SysTenantPack getSysTenantPack(Integer tenantId ,String packCode);
   
    /**
     * 添加由管理员创建的默认product package
     * @param id
     */
    void addTenantDefaultPack(Integer id);

    /**
     * Synchronize default plan
     * for [QQYUN-11032]【jeecg】Added initialization package button to tenant package management
     * @param tenantId
     * @author chenrui
     * @date 2025/2/5 19:08
     */
    void syncDefaultPack(Integer tenantId);

    /**
     * According to useridand current tenantsid获取product package的id
     *
     * @param userId
     * @param tenantId
     * @return
     */
    List<String> getPackIdByUserIdAndTenantId(String userId, Integer tenantId);

    /**
     * According to tenantid获取用户的product package列表
     *
     * @param tenantId
     * @return
     */
    List<SysTenantPack> getPackListByTenantId(String tenantId);
}
