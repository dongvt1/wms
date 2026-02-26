package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * character sheet
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * Character name
     */
    @Excel(name="Character name",width=15)
    private String roleName;
    
    /**
     * role coding
     */
    @Excel(name="role coding",width=15)
    private String roleCode;
    
    /**
          * describe
     */
    @Excel(name="describe",width=60)
    private String description;

    /**
     * Creator
     */
    private String createBy;

    /**
     * creation time
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * Updater
     */
    private String updateBy;

    /**
     * Update time
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**tenantID*/
    private java.lang.Integer tenantId;
}
