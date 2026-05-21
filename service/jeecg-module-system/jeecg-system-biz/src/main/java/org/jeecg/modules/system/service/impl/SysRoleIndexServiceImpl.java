package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.constant.DefIndexConst;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.mapper.SysRoleIndexMapper;
import org.jeecg.modules.system.service.ISysRoleIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
/**
 * @Description: Role homepage configuration
 * @Author: jeecg-boot
 * @Date: 2022-03-25
 * @Version: V1.0
 */
@Service("sysRoleIndexServiceImpl")
public class SysRoleIndexServiceImpl extends ServiceImpl<SysRoleIndexMapper, SysRoleIndex> implements ISysRoleIndexService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Cacheable(cacheNames = DefIndexConst.CACHE_KEY, key = "'" + DefIndexConst.DEF_INDEX_ALL + "'")
    public SysRoleIndex queryDefaultIndex() {
        LambdaQueryWrapper<SysRoleIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleIndex::getRoleCode, DefIndexConst.DEF_INDEX_ALL);
        SysRoleIndex entity = super.getOne(queryWrapper);
        // Guaranteed not to be empty
        if (entity == null) {
            entity = this.initDefaultIndex();
        }
        return entity;
    }

    @Override
    public boolean updateDefaultIndex(String url, String component, boolean isRoute) {
        // 1. First query the configuration information
        LambdaQueryWrapper<SysRoleIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleIndex::getRoleCode, DefIndexConst.DEF_INDEX_ALL);
        SysRoleIndex entity = super.getOne(queryWrapper);
        boolean success = false;
        // 2. If it does not exist, add it
        if (entity == null) {
            entity = this.newDefIndexConfig(url, component, isRoute);
            success = super.save(entity);
        } else {
            // 3. update if exists
            entity.setUrl(url);
            entity.setComponent(component);
            entity.setRoute(isRoute);
            entity.setRelationType(CommonConstant.HOME_RELATION_DEFAULT);
            success = super.updateById(entity);
        }
        // 4. clear cache
        if (success) {
            this.cleanDefaultIndexCache();
        }
        return success;
    }

    @Override
    public SysRoleIndex initDefaultIndex() {
        return this.newDefIndexConfig(DefIndexConst.DEF_INDEX_URL, DefIndexConst.DEF_INDEX_COMPONENT, true);
    }

    /**
     * Create a default home page configuration
     *
     * @param indexComponent
     * @return
     */
    private SysRoleIndex newDefIndexConfig(String indexUrl, String indexComponent, boolean isRoute) {
        SysRoleIndex entity = new SysRoleIndex();
        entity.setRoleCode(DefIndexConst.DEF_INDEX_ALL);
        entity.setUrl(indexUrl);
        entity.setComponent(indexComponent);
        entity.setRoute(isRoute);
        entity.setStatus(CommonConstant.STATUS_1);
        entity.setRelationType(CommonConstant.HOME_RELATION_DEFAULT);
        return entity;
    }

    @Override
    public void cleanDefaultIndexCache() {
        redisUtil.del(DefIndexConst.CACHE_KEY + "::" + DefIndexConst.DEF_INDEX_ALL);
    }

    /**
     * Switch default portal
     * @param sysRoleIndex
     */
    @Override
    public void changeDefHome(SysRoleIndex sysRoleIndex) {
        // 1. First query the configuration information
        String username = sysRoleIndex.getRoleCode();
        //Current status(1:workbench/portal 0：Menu default)
        String status = sysRoleIndex.getStatus();
        LambdaQueryWrapper<SysRoleIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleIndex::getRoleCode, username);
        queryWrapper.eq(SysRoleIndex::getRelationType,CommonConstant.HOME_RELATION_USER);
        queryWrapper.orderByAsc(SysRoleIndex::getPriority);
        List<SysRoleIndex> list = super.list(queryWrapper);
        boolean success = false;
        if(CommonConstant.STATUS_1.equalsIgnoreCase(status)){
            // 2. Edit if exists
            if (!CollectionUtils.isEmpty(list)) {
                sysRoleIndex.setId(list.get(0).getId());
                sysRoleIndex.setStatus(CommonConstant.STATUS_1);
                sysRoleIndex.setRoute(true);
                success = super.updateById(sysRoleIndex);
            } else {
                // 3. If it does not exist, add it
                sysRoleIndex.setRelationType(CommonConstant.HOME_RELATION_USER);
                sysRoleIndex.setStatus(CommonConstant.STATUS_1);
                sysRoleIndex.setRoute(true);
                success = super.save(sysRoleIndex);
            }
        }else {
            // 0：Menu default，则是Menu default首页
            if (!CollectionUtils.isEmpty(list)) {
                //Set the user-level home page configuration status to0
                for (int i = 0; i < list.size(); i++) {
                    SysRoleIndex roleIndex = list.get(i);
                    roleIndex.setStatus(CommonConstant.STATUS_0);
                    success = super.updateById(roleIndex);
                }
            }
        }
        // 4. clear cache
        if (success) {
            this.cleanDefaultIndexCache();
            redisUtil.del(DefIndexConst.CACHE_TYPE + username);
        }
        // 5. cache type
        //current address
        String url = sysRoleIndex.getUrl();
        //Home page type(Default homepage)
        String type = DefIndexConst.HOME_TYPE_MENU;
        if(oConvertUtils.isNotEmpty(url) && CommonConstant.STATUS_1.equalsIgnoreCase(status)){
           type = url.contains(DefIndexConst.HOME_TYPE_SYSTEM) ? DefIndexConst.HOME_TYPE_SYSTEM : DefIndexConst.HOME_TYPE_PERSONAL;
        }
        redisUtil.set(DefIndexConst.CACHE_TYPE + username,type);
    }

}
