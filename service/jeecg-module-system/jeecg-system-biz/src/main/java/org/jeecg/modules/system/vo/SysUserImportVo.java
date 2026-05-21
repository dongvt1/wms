package org.jeecg.modules.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: Low-code user import
 * @author: wangshuai
 * @date: 2025/8/27 11:58
 */
@Data
public class SysUserImportVo {

    /**
     * Login account
     */
    @Excel(name = "Login account", width = 15)
    private String username;

    /**
     * real name
     */
    @Excel(name = "real name", width = 15)
    private String realname;

    /**
     * avatar
     */
    @Excel(name = "avatar", width = 15, type = 2)
    private String avatar;

    /**
     * Birthday
     */
    @Excel(name = "Birthday", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * gender（1：male 2：female）
     */
    @Excel(name = "gender", width = 15, dicCode = "sex")
    private Integer sex;

    /**
     * e-mail
     */
    @Excel(name = "e-mail", width = 15)
    private String email;

    /**
     * Telephone
     */
    @Excel(name = "Telephone", width = 15)
    private String phone;

    /**
     * state(1：normal  2：freeze ）
     */
    @Excel(name = "state", width = 15, dicCode = "user_status")
    private Integer status;

    /**
     * 删除state（0，normal，1Deleted）
     */
    @Excel(name = "删除state", width = 15, dicCode = "del_flag")
    private Integer delFlag;

    /**
     * Job number，unique key
     */
    @Excel(name = "Job number", width = 15)
    private String workNo;
    
    /**
     * main post
     */
    @Excel(name="main post",width = 15)
    private String mainDepPostId;

    /**
     * Rank
     */
    @Excel(name="Rank", width = 15)
    private String postName;
    
    /**
     * identity（0 ordinary member 1 Superior）
     */
    @Excel(name = "（1ordinary member 2Superior）", width = 15)
    private Integer userIdentity;

    /**
     * Character name
     */
    @Excel(name = "Role", width = 15)
    private String roleNames;

    /**
     * Department name
     */
    @Excel(name = "Department", width = 15)
    private String departNames;

    /**
     * Institution type
     * company(1)、department(2)、post(3)、子company(4)
     */
    @Excel(name = "department类型(1-company,2-department,3-post,4-子company)",width = 15)
    private String orgCategorys;

    /**
     * 负责department
     */
    @Excel(name = "负责department", width = 15)
    private String departIds;
    
    /**
     * Position
     */
    @Excel(name="Position", dicCode = "user_position")
    private String positionType;

}
