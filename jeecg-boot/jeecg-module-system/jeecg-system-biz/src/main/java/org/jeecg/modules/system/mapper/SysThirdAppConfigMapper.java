package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.entity.SysThirdAppConfig;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Description: Third-party configuration table
 * @Author: jeecg-boot
 * @Date:   2023-02-03
 * @Version: V1.0
 */
public interface SysThirdAppConfigMapper  extends BaseMapper<SysThirdAppConfig> {

    /**
     * According to tenantidGet DingTalk/Enterprise WeChat configuration
     * @param tenantId
     * @return
     */
    List<SysThirdAppConfig> getThirdConfigListByThirdType(@Param("tenantId") int tenantId);

    /**
     * According to tenantidand third-party categories to get third-party configurations
     * @param tenantId
     * @param thirdType
     * @return
     */
    SysThirdAppConfig getThirdConfigByThirdType(@Param("tenantId") int tenantId, @Param("thirdType") String thirdType);
}
