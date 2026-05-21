package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysTableWhiteList;

import java.util.Map;

/**
 * @Description: System whitelist
 * @Author: jeecg-boot
 * @Date: 2023-09-12
 * @Version: V1.0
 */
public interface ISysTableWhiteListService extends IService<SysTableWhiteList> {

    /**
     * New
     *
     * @param sysTableWhiteList
     * @return
     */
    boolean add(SysTableWhiteList sysTableWhiteList);

    /**
     * edit
     *
     * @param sysTableWhiteList
     * @return
     */
    boolean edit(SysTableWhiteList sysTableWhiteList);

    /**
     * passiddelete，可批量delete
     *
     * @param ids Use commas to separate multiple
     * @return
     */
    boolean deleteByIds(String ids);

    /**
     * Automatically added to database
     *
     * @param tableName
     * @param fieldName
     * @return
     */
    SysTableWhiteList autoAdd(String tableName, String fieldName);

    /**
     * bymapGet all the data by
     * key=tableName，value=fieldName
     *
     * @return
     */
    Map<String, String> getAllConfigMap();

}
