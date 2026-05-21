package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * Tenant information
 * @author: jeecg-boot
 */
@Data
@TableName("sys_tenant")
public class SysTenant implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * coding
     */
    private Integer id;
    
    /**
     * name
     */
    private String name;
    

    /**
     * Creator
     */
    @Dict(dictTable ="sys_user",dicText = "realname",dicCode = "username")
    private String createBy;

    /**
     * creation time
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * start time
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date beginDate;

    /**
     * end time
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endDate;

    /**
     * state 1normal 0freeze
     */
    @Dict(dicCode = "tenant_status")
    private Integer status;

    /**
     * Industry
     */
    @Dict(dicCode = "trade")
    private String trade;

    /**
     * Company size
     */
    @Dict(dicCode = "company_size")
    private String companySize;

    /**
     * Company address
     */
    private String companyAddress;

    /**
     * companylogo
     */
    private String companyLogo;

    /**
     * house number
     */
    private String houseNumber;

    /**
     * work place
     */
    private String workPlace;

    /**
     * Second level domain name(Temporarily useless,Reserved fields)
     */
    private String secondaryDomain;

    /**
     * Login background image(Temporarily useless，Reserved fields)
     */
    private String loginBkgdImg;

    /**
     * Rank
     */
    @Dict(dicCode = "company_rank")
    private String position;

    /**
     * department
     */
    @Dict(dicCode = "company_department")
    private String department;
    
    @TableLogic
    private Integer delFlag;

    /**更新人登录name*/
    private String updateBy;
    
    /**Update date*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * Allow application administrators 1allow 0不allow
     */
    private Integer applyStatus;
    
}
