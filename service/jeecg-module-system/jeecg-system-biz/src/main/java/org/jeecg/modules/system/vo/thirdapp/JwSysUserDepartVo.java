package org.jeecg.modules.system.vo.thirdapp;

import lombok.Data;

import java.util.List;

/**
 * Implementation class of enterprise WeChat
 */
@Data
public class JwSysUserDepartVo {

    /**
     * Mapping class between enterprise WeChat and users
     */
    private List<JwUserDepartVo> jwUserDepartVos;

    /**
     * User list
     */
    private List<JwUserDepartVo> userList;

}
