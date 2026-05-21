package org.jeecg.modules.system.vo.thirdapp;

import lombok.Data;

/**
* @Description: Enterprise WeChat user synchronization tools
*
* @author: wangshuai
* @date: 2023/11/28 18:17
*/
@Data
public class JwUserDepartVo {
    
    /**
     * userid
     */
    private String userId;

    /**
     * user头像
     */
    private String avatar;

    /**
     * real name
     */
    private String realName;

    /**
     * Enterprise WeChat name
     */
    private String wechatRealName;

    /**
     * Departments corresponding to Enterprise WeChat
     */
    private String wechatDepartId;

    /**
     * 企业微信对应的userid
     */
    private String wechatUserId;

    /**
     * third partyid
     */
    private String thirdId;
}
