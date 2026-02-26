package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysRoleIndex;

/**
 * @Description: Role homepage configuration
 * @Author: jeecg-boot
 * @Date: 2022-03-25
 * @Version: V1.0
 */
public interface ISysRoleIndexService extends IService<SysRoleIndex> {

    /**
     * Query the default home page
     *
     * @return
     */
    SysRoleIndex queryDefaultIndex();

    /**
     * Update default home page
     *
     * @param url
     * @param component
     * @param isRoute   Is it a routing page?
     * @return
     */
    boolean updateDefaultIndex(String url, String component, boolean isRoute);

    /**
     * Create the most original default homepage configuration
     *
     * @return
     */
    SysRoleIndex initDefaultIndex();

    /**
     * Clean up the default home pagerediscache
     */
    void cleanDefaultIndexCache();

    /**
     * Switch default portal
     * @param sysRoleIndex
     */
    void changeDefHome(SysRoleIndex sysRoleIndex);
}
