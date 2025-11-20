package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.jeecg.modules.system.entity.SysThirdAppConfig;

import java.util.List;

/**
 * @Description: Third-party configuration table
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
public interface ISysThirdAppConfigService extends IService<SysThirdAppConfig>{

    /**
     * According to tenantidGet DingTalk/Enterprise WeChat configuration
     * @param tenantId
     * @return
     */
    List<SysThirdAppConfig> getThirdConfigListByThirdType(int tenantId);

    /**
     * According to tenantidand third-party categories to get third-party configurations
     * @param tenantId
     * @param thirdType
     * @return
     */
    SysThirdAppConfig getThirdConfigByThirdType(Integer tenantId, String thirdType);

    /**
     * According to applicationkeyGet third-party table configuration
     * @param clientId
     */
    List<SysThirdAppConfig> getThirdAppConfigByClientId(String clientId);
}
