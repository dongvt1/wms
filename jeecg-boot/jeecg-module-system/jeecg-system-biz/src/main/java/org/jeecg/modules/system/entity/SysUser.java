package org.jeecg.modules.system.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * User table
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

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
     * password
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * md5password盐
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String salt;

    /**
     * avatar
     */
    @Excel(name = "avatar", width = 15,type = 2)
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
    @Excel(name = "gender", width = 15,dicCode="sex")
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
     * Log in and select department code
     */
    private String orgCode;
    /**
     * Log in and select tenantID
     */
    private Integer loginTenantId;

    /**Department name*/
    private transient String orgCodeTxt;

    /**
     * state(1：normal  2：freeze ）
     */
    @Excel(name = "state", width = 15,dicCode="user_status")
    @Dict(dicCode = "user_status")
    private Integer status;

    /**
     * 删除state（0，normal，1Deleted）
     */
    @Excel(name = "删除state", width = 15,dicCode="del_flag")
    @TableLogic
    private Integer delFlag;

    /**
     * Job number，unique key
     */
    @Excel(name = "Job number", width = 15)
    private String workNo;

    /**
     * Position，关联Position表
     */
    @Excel(name = "Position", width = 15)
    @Dict(dictTable ="sys_position",dicText = "name",dicCode = "id")
    @TableField(exist = false)
    private String post;

    /**
     * Landline number
     */
    @Excel(name = "Landline number", width = 15)
    private String telephone;

    /**
     * Creator
     */
    private String createBy;

    /**
     * creation time
     */
    private Date createTime;

    /**
     * Updater
     */
    private String updateBy;

    /**
     * Update time
     */
    private Date updateTime;
    /**
     * Sync workflow engine1synchronous0不synchronous
     */
    private Integer activitiSync;

    /**
     * identity（0 ordinary member 1 Superior）
     */
    @Excel(name="（1ordinary member 2Superior）",width = 15)
    private Integer userIdentity;

    /**
     * Responsible department
     */
    @Excel(name="Responsible department",width = 15,dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    private String departIds;

    /**
     * multi-tenantidsTemporary use，Not persisting database(Database field does not exist)
     */
    @TableField(exist = false)
    private String relTenantIds;

    /**equipmentid uniappFor push*/
    private String clientId;

    /**
     * Login homepage address
     */
    @TableField(exist = false)
    private String homePath;

    /**
     * Job title
     */
    @TableField(exist = false)
    private String postText;

    /**
     * 流程state
     */
    private String bpmStatus;

    /**
     * Whether it has been bound to a third party
     */
    @TableField(exist = false)
    private boolean izBindThird;

    /**
     * Personalized signature
     */
    private String sign;

    /**
     * 是否开启Personalized signature
     */
    private Integer signEnable;

    /**
     * main post
     */
    @Excel(name="main post",width = 15,dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    private String mainDepPostId;

    /**
     * part-time position
     */
    @Excel(name="part-time position",width = 15,dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @TableField(exist = false)
    private String otherDepPostId;

    /**
     * Position(dictionary)
     */
    @Excel(name = "Position", width = 15, dicCode = "position_type")
    @Dict(dicCode = "position_type")
    private String positionType;
}
