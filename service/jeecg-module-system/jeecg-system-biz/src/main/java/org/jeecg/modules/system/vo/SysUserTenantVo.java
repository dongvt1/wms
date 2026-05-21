package org.jeecg.modules.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: User tenant class(User data tenant data)
 * @author: wangshuai
 * @date: 2023Year01moon08day 17:27
 */
@Data
public class SysUserTenantVo {

    /**
     * userid
     */
    private String id;

    /**
     * user账号
     */
    private String username;

    /**
     * user昵称
     */
    private String realname;

    /**
     * Job number
     */
    private String workNo;
    
    /**
     * Mail
     */
    private String email; 
    
    /**
     * Phone number
     */
    private String phone;   
    
    /**
     * avatar
     */
    private String avatar;

    /**
     * 创建day期
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    /**
     * Position
     */
    @Dict(dictTable ="sys_position",dicText = "name",dicCode = "id")
    private String post;

    /**
     * Review status
     */
    private String status;

    /**
     * Department name
     */
    private String orgCodeTxt;

    /**
     * departmentcode
     */
    private String orgCode;

    /**
     * tenantid
     */
    private String relTenantIds;

    /**
     * tenant创建人
     */
    private String createBy;

    /**
     * usertenant状态
     */
    private String userTenantStatus;

    /**
     * usertenantid
     */
    private String tenantUserId;

    /**
     * tenant名称
     */
    private String name;

    /**
     * Industry
     */
    @Dict(dicCode = "trade")
    private String trade;
    
    /**
     * house number
     */
    private String houseNumber;

    /**
     * Are you a member?
     */
    private String memberType;

    /**
     * 是否为tenant管理员
     */
    private Boolean tenantAdmin = false;
}
