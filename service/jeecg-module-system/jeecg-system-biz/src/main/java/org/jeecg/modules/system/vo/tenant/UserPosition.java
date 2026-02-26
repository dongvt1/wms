package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

/**
 * User and job information
 * @Author taoYan
 * @Date 2023/2/17 10:10
 **/
@Data
public class UserPosition {

    /**
     * userID
     */
    private String userId;

    /**
     * Job title
     */
    private String positionName;
}
