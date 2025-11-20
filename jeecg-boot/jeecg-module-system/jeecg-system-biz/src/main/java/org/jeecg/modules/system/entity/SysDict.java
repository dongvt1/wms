package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Dictionary
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysDict implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * [Reserved fields，Temporarily useless]
     * dictionary type,0 string,1 numbertype,2 boolean
     * front endjsrightstirngtype和numbertype boolean type敏感，Need to distinguish。existselect It will be used when matching tags
     * Default isstringtype
     */
    private Integer type;
    
    /**
     * Dictionary name
     */
    private String dictName;

    /**
     * dictionary encoding
     */
    private String dictCode;

    /**
     * describe
     */
    private String description;

    /**
     * delete status
     */
    @TableLogic
    private Integer delFlag;

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

    /**tenantID*/
    private java.lang.Integer tenantId;
    
    /** Relevant low-code applicationsID */
    private java.lang.String lowAppId;

}
