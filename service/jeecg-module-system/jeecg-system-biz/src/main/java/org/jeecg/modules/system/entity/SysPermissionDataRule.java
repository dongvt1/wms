package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Menu permissions rule table
 * </p>
 *
 * @Author huangzhilin
 * @since 2019-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysPermissionDataRule implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;
	
	/**
	 * corresponding menuid
	 */
	private String permissionId;
	
	/**
	 * Rule name
	 */
	private String ruleName;
	
	/**
	 * Field
	 */
	private String ruleColumn;
	
	/**
	 * condition
	 */
	private String ruleConditions;
	
	/**
	 * rule value
	 */
	private String ruleValue;
	
	/**
	 * status value 1efficient 0invalid
	 */
	private String status;
	
	/**
	 * creation time
	 */
	private Date createTime;
	
	/**
	 * Creator
	 */
	private String createBy;
	
	/**
	 * modification time
	 */
	private Date updateTime;
	
	/**
	 * Modifier
	 */
	private String updateBy;
}
