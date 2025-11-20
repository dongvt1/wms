package org.jeecg.common.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.desensitization.annotation.SensitiveField;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * Online user information
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LoginUser {

	/**
	 * Login personid
	 */
	@SensitiveField
	private String id;

	/**
	 * Login person账号
	 */
	@SensitiveField
	private String username;

	/**
	 * Login person名字
	 */
	@SensitiveField
	private String realname;

	/**
	 * Login person密码
	 */
	@SensitiveField
	private String password;

     /**
      * Current login departmentcode
      */
	@SensitiveField
    private String orgCode;
	/**
	 * Current login departmentid
	 */
	@SensitiveField
	private String orgId;
	/**
	 * Current login rolecode（Multiple commas separated）
	 */
	@SensitiveField
	private String roleCode;

	/**
	 * avatar
	 */
	@SensitiveField
	private String avatar;

	/**
	 * Job number
	 */
	@SensitiveField
	private String workNo;

	/**
	 * Birthday
	 */
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;

	/**
	 * gender（1：male 2：female）
	 */
	private Integer sex;

	/**
	 * e-mail
	 */
	@SensitiveField
	private String email;

	/**
	 * Telephone
	 */
	@SensitiveField
	private String phone;

	/**
	 * state(1：normal 2：freeze ）
	 */
	private Integer status;
	
	private Integer delFlag;
	/**
     * Sync workflow engine1synchronous0不synchronous
     */
    private Integer activitiSync;

	/**
	 * creation time
	 */
	private Date createTime;

	/**
	 *  identity（1 Ordinary employees 2 Superior）
	 */
	private Integer userIdentity;

	/**
	 * management departmentids
	 */
	@SensitiveField
	private String departIds;

	/**
	 * Position，关联Position表
	 */
	@SensitiveField
	private String post;

	/**
	 * Landline number
	 */
	@SensitiveField
	private String telephone;

	/** multi-tenantidsTemporary use，Not persisting database(Database field does not exist) */
	@SensitiveField
	private String relTenantIds;

	/**equipmentid uniappFor push*/
	private String clientId;

	/**
	 * main post
	 */
	private String mainDepPostId;
}
