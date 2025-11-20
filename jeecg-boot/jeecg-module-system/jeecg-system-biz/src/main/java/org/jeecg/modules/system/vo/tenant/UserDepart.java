package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

/**
 * User and department information
 * @Author taoYan
 * @Date 2023/2/17 10:10
 **/
@Data
public class UserDepart {

    /**
     * userID
     */
    private String userId;

    /**
     * Department name
     */
    private String departName;
}
