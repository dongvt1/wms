package org.jeecg.modules.system.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: Department users and department position usersModel
 * @author: wangshuai
 * @date: 2025/9/5 16:43
 */
@Data
public class SysUserSysDepPostModel {
    /**
     * userID
     */
    private String id;
    
    /**
     * Login account
     */
    @Excel(name = "Login account", width = 15)
    private String username;
    
    /* real name */
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
    @Dict(dicCode = "sex")
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
    @Dict(dicCode = "user_status")
    private Integer status;

    /**
     * 删除state（0，normal，1Deleted）
     */
    @Excel(name = "删除state", width = 15, dicCode = "del_flag")
    @TableLogic
    private Integer delFlag;

    /**
     * Landline number
     */
    @Excel(name = "Landline number", width = 15)
    private String telephone;

    /**
     * identity（0 ordinary member 1 Superior）
     */
    @Excel(name = "（1ordinary member 2Superior）", width = 15)
    private Integer userIdentity;

    /**
     * Responsible department
     */
    @Excel(name = "Responsible department", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    private String departIds;

    /**
     * multi-tenantidsTemporary use，Not persisting database(Database field does not exist)
     */
    private String relTenantIds;

    /**
     * Sync workflow engine(1-synchronous 0-不synchronous)
     */
    private String activitiSync;
    /**
     * main post
     */
    @Excel(name = "main post", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    private String mainDepPostId;

    /**
     * part-time position
     */
    @Excel(name = "part-time position", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @TableField(exist = false)
    private String otherDepPostId;

    /**
     * Department name
     */
    private String departName;
    /**
     * main post
     */
    private String postName;
    
    /**
     * part-time position
     */
    private String otherPostName;

    /**
     * departmenttext
     */
    private String orgCodeTxt;

    /**
     * Position
     */
    private String post;
}
