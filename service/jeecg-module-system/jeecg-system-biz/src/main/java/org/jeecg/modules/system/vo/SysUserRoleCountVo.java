package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * @Description:
 * @author: wangshuai
 * @date: 2022Year12moon07day 16:41
 */
@Data
public class SysUserRoleCountVo {
    /**
     * Roleid
     */
    private String id;
    /**
     * Role名称
     */
    private String roleName;
    /**
     * Role描述
     */
    private String description;
    /**
     * Role编码
     */
    private String roleCode;
    /**
     * Role下的用户数量
     */
    private Long count;
}
