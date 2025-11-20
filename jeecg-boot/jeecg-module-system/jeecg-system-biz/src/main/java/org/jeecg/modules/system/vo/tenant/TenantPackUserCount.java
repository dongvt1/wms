package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

/**
 * for statistics Number of people for the tenant package
 * @Author taoYan
 * @Date 2023/2/16 15:59
 **/
@Data
public class TenantPackUserCount {

    /**
     * Tenant product package encoding
     */
    private String packCode;

    /**
     * number of users
     */
    private String userCount;
    
}
