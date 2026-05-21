package org.jeecg.modules.system.vo.tenant;

import lombok.Data;
import org.jeecg.modules.system.entity.SysTenant;

import java.util.List;

/**
 * Enter the tenant organization page Query tenant information and operation permissions
 * @Author taoYan
 * @Date 2023/2/16 16:18
 **/
@Data
public class TenantDepartAuthInfo {

    /**
     * Is the current user super administrator
     */
    private boolean superAdmin;

    /**
     * Tenant information
     */
    private SysTenant sysTenant;

    /**
     * Count the number of tenant product package personnel
     */
    private List<TenantPackUserCount> packCountList;

    /**
     * Tenant product package coding(这indivualcoding只有3indivualadminProduct package includes，Easy to distinguish)
     */
    private List<String> packCodes;
    
}
