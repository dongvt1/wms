package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description: Third-party login account table
 * @Author: jeecg-boot
 * @Date:   2020-11-17
 * @Version: V1.0
 */
@Data
@TableName("sys_third_account")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="Third-party login account table")
public class SysThirdAccount {
 
	/**serial number*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "serial number")
	private java.lang.String id;
	/**Third party loginid*/
	@Excel(name = "Third party loginid", width = 15)
	@Schema(description = "Third party loginid")
	private java.lang.String sysUserId;
	/**Login source*/
	@Excel(name = "Login source", width = 15)
	@Schema(description = "Login source")
	private java.lang.String thirdType;
	/**avatar*/
	@Excel(name = "avatar", width = 15)
	@Schema(description = "avatar")
	private java.lang.String avatar;
	/**state(1-normal,2-freeze)*/
	@Excel(name = "state(1-normal,2-freeze)", width = 15)
	@Schema(description = "state(1-normal,2-freeze)")
	private java.lang.Integer status;
	/**删除state(0-normal,1-Deleted)*/
	@Excel(name = "删除state(0-normal,1-Deleted)", width = 15)
	@Schema(description = "删除state(0-normal,1-Deleted)")
	private java.lang.Integer delFlag;
	/**real name*/
	@Excel(name = "real name", width = 15)
	@Schema(description = "real name")
	private java.lang.String realname;
	/**third party usersuuid*/
	@Excel(name = "third party usersuuid", width = 15)
	@Schema(description = "third party usersuuid")
	private java.lang.String thirdUserUuid;
	/**third party users账号*/
	@Excel(name = "third party users账号", width = 15)
	@Schema(description = "third party users账号")
	private java.lang.String thirdUserId;
    /**Creator*/
    @Excel(name = "Creator", width = 15)
    private java.lang.String createBy;
    /**Creation date*/
    @Excel(name = "Creation date", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;
    /**Modifier*/
    @Excel(name = "Modifier", width = 15)
    private java.lang.String updateBy;
    /**Modification date*/
    @Excel(name = "Modification date", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;

	/**tenantid*/
	private java.lang.Integer tenantId;
}
