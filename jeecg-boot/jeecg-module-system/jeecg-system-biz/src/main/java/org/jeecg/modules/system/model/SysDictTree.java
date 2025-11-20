package org.jeecg.modules.system.model;

import java.io.Serializable;
import java.util.Date;

import org.jeecg.modules.system.entity.SysDict;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

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
public class SysDictTree implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
	
	private String title;
	
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
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
    
    public SysDictTree(SysDict node) {
    	this.id = node.getId();
		this.key = node.getId();
		this.title = node.getDictName();
		this.dictCode = node.getDictCode();
		this.description = node.getDescription();
		this.delFlag = node.getDelFlag();
		this.type = node.getType();
	}
    
}
