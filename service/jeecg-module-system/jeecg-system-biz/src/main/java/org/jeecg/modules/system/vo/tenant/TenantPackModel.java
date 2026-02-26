package org.jeecg.modules.system.vo.tenant;

import lombok.Data;

import java.util.List;

/**
 * Tenant product package information
 *  include+ User information + Permission information
 * @Author taoYan
 * @Date 2023/2/16 21:01
 **/
@Data
public class TenantPackModel {

    /**
     * tenantId
     */
    private Integer tenantId;
    /**
     * Product package code
     */
    private String packCode;

    /**
     * product packageID
     */
    private String packId;

    /**
     * product package名称
     */
    private String packName;

    /**
     * product package Permission information
     */
    private List<TenantPackAuth> authList;

    /**
     * product package User list
     */
    private List<TenantPackUser> userList;

    /**
     * state 正常state1 申请state0
     */
    private Integer packUserStatus;
    
    public Integer getPackUserStatus(){
        if(packUserStatus==null){
            return 1;
        }
        return packUserStatus; 
    }
}
