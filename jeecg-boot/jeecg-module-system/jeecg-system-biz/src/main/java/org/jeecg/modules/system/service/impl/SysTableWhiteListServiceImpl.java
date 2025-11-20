package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.firewall.SqlInjection.IDictTableWhiteListHandler;
import org.jeecg.modules.system.entity.SysTableWhiteList;
import org.jeecg.modules.system.mapper.SysTableWhiteListMapper;
import org.jeecg.modules.system.service.ISysTableWhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description: System whitelist
 * @Author: jeecg-boot
 * @Date: 2023-09-12
 * @Version: V1.0
 */
@Slf4j
@Service
public class SysTableWhiteListServiceImpl extends ServiceImpl<SysTableWhiteListMapper, SysTableWhiteList> implements ISysTableWhiteListService {

    @Lazy
    @Autowired
    IDictTableWhiteListHandler whiteListHandler;

    @Override
    public boolean add(SysTableWhiteList sysTableWhiteList) {
        this.checkEntity(sysTableWhiteList);
        if (super.save(sysTableWhiteList)) {
            // Clear cache
            whiteListHandler.clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean edit(SysTableWhiteList sysTableWhiteList) {
        this.checkEntity(sysTableWhiteList);
        if (super.updateById(sysTableWhiteList)) {
            // Clear cache
            whiteListHandler.clear();
            return true;
        }
        return false;
    }

    /**
     * Check whether the entities that need to be added or updated comply with the specifications
     *
     * @param sysTableWhiteList
     */
    private void checkEntity(SysTableWhiteList sysTableWhiteList) {
        if (sysTableWhiteList == null) {
            throw new JeecgBootException("Operation failed，Entity is empty！");
        }
        if (oConvertUtils.isEmpty(sysTableWhiteList.getTableName())) {
            throw new JeecgBootException("Operation failed，Table name cannot be empty！");
        }
        if (oConvertUtils.isEmpty(sysTableWhiteList.getFieldName())) {
            throw new JeecgBootException("Operation failed，Field name cannot be empty！");
        }
        // Convert table and field names to lowercase
        sysTableWhiteList.setTableName(sysTableWhiteList.getTableName().toLowerCase());
        sysTableWhiteList.setFieldName(sysTableWhiteList.getFieldName().toLowerCase());
        // ifstatusis empty，is enabled by default
        if (oConvertUtils.isEmpty(sysTableWhiteList.getStatus())) {
            sysTableWhiteList.setStatus(CommonConstant.STATUS_1);
        }
    }

    @Override
    public boolean deleteByIds(String ids) {
        if (oConvertUtils.isEmpty(ids)) {
            return false;
        }
        List<String> idList = Arrays.asList(ids.split(","));
        if (super.removeByIds(idList)) {
            // Clear cache
            whiteListHandler.clear();
            return true;
        }
        return false;
    }

    @Override
    public SysTableWhiteList autoAdd(String tableName, String fieldName) {
        if (oConvertUtils.isEmpty(tableName)) {
            throw new JeecgBootException("Operation failed，Table name cannot be empty！");
        }
        if (oConvertUtils.isEmpty(fieldName)) {
            throw new JeecgBootException("Operation failed，Field name cannot be empty！");
        }
        // Convert to lowercase uniformly
        tableName = tableName.toLowerCase();
        fieldName = fieldName.toLowerCase();
        // Check if it already exists
        LambdaQueryWrapper<SysTableWhiteList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTableWhiteList::getTableName, tableName);
        SysTableWhiteList getEntity = super.getOne(queryWrapper);
        if (getEntity != null) {
            // if已经存在，and disabled，throws an exception
            if (CommonConstant.STATUS_0.equals(getEntity.getStatus())) {
                throw new JeecgBootException("[whitelist] table name already exists，but is disabled，Please enable first！tableName=" + tableName);
            }
            // merge fields
            Set<String> oldFieldSet = new HashSet<>(Arrays.asList(getEntity.getFieldName().split(",")));
            Set<String> newFieldSet = new HashSet<>(Arrays.asList(fieldName.split(",")));
            oldFieldSet.addAll(newFieldSet);
            getEntity.setFieldName(String.join(",", oldFieldSet));
            this.checkEntity(getEntity);
            super.updateById(getEntity);
            log.info("修改表单whitelist项，table name：{}，oldFieldSet： {}，newFieldSet：{}", tableName, oldFieldSet.toArray(), newFieldSet.toArray());
            return getEntity;
        } else {
            // 新增whitelist项
            SysTableWhiteList saveEntity = new SysTableWhiteList();
            saveEntity.setTableName(tableName);
            saveEntity.setFieldName(fieldName);
            saveEntity.setStatus(CommonConstant.STATUS_1);
            this.checkEntity(saveEntity);
            super.save(saveEntity);
            log.info("新增表单whitelist项: table name：{}，Configuration > {}", tableName, saveEntity.toString());
            return saveEntity;
        }
    }

    @Override
    public Map<String, String> getAllConfigMap() {
        Map<String, String> map = new HashMap<>();
        List<SysTableWhiteList> allData = super.list();
        for (SysTableWhiteList item : allData) {
            // Only put in enabled onesmap
            if (CommonConstant.STATUS_1.equals(item.getStatus())) {
                // table name和字段名都转成小写，Prevent case inconsistency
                map.put(item.getTableName().toLowerCase(), item.getFieldName().toLowerCase());
            }
        }
        return map;
    }

}
