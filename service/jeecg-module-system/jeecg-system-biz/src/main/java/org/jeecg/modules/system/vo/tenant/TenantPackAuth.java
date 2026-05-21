package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

/**
 * Tenant product package Associated permission details
 * @Author taoYan
 * @Date 2023/2/16 21:02
 **/
@Data
public class TenantPackAuth {

    /**
     * First level menu
     */
    private String category;

    /**
     * Permissions menu name
     */
    private String authName;


    /**
     * Permissions menu description
     */
    private String authNote;
}
