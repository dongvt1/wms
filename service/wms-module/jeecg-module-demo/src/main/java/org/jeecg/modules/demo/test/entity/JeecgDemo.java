package org.jeecg.modules.demo.test.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.Version;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: jeecg testdemo 
 * @Author: jeecg-boot 
 * @Date:	2018-12-29 
 * @Version:V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="testDEMO")
@TableName("demo")
public class JeecgDemo extends JeecgEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/** Name */
	@Excel(name="Name",width=25)
	@Schema(description = "Name")
	private java.lang.String name;
	/** keywords */
	@Schema(description = "keywords")
	@Excel(name="keywords",width=15)
	private java.lang.String keyWord;
	/** Check in time */
	@Schema(description = "Check in time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name="Check in time",width=20,format="yyyy-MM-dd HH:mm:ss")
	private java.util.Date punchTime;
	/** salary */
	@Schema(description = "salary",example = "0")
	@Excel(name="salary",type = 4,width=15)
	private java.math.BigDecimal salaryMoney;
	/** bonus */
	@Schema(description = "bonus",example = "0")
	@Excel(name="bonus",type = 4,width=15)
	private java.lang.Double bonusMoney;
	/** gender {male:1,female:2} */
	@Schema(description = "gender")
	@Excel(name = "gender", width = 15, dicCode = "sex")
	private java.lang.String sex;
	/** age */
	@Schema(description = "age",example = "0")
	@Excel(name="age",type = 4,width=15)
	private java.lang.Integer age;
	/** Birthday */
	@Schema(description = "Birthday")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Excel(name="Birthday",format="yyyy-MM-dd")
	private java.util.Date birthday;
	/** Mail */
	@Schema(description = "Mail")
	@Excel(name="Mail",width=30)
	private java.lang.String email;
	/** Profile */
	@Schema(description = "Profile")
	private java.lang.String content;
	/** Department code */
	@Excel(name="Department code",width=25)
	@Schema(description = "Department code")
	private java.lang.String sysOrgCode;

	@Schema(description = "tenantID")
	private java.lang.Integer tenantId;
	/** optimistic locking field */
	@Version
	private java.lang.Integer updateCount;

}
