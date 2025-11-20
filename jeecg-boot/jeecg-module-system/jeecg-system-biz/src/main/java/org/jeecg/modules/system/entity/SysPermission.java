package org.jeecg.modules.system.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.system.constant.DefIndexConst;

/**
 * <p>
 * Menu permission table
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysPermission implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;

	/**
	 * fatherid
	 */
	private String parentId;

	/**
	 * Menu name
	 */
	private String name;

	/**
	 * Menu permission encoding，For example：“sys:schedule:list,sys:schedule:info”,Multiple commas separated
	 */
	private String perms;
	/**
	 * Permission policy1show2Disable
	 */
	private String permsType;

	/**
	 * menu icon
	 */
	private String icon;

	/**
	 * components
	 */
	private String component;
	
	/**
	 * components名字
	 */
	private String componentName;

	/**
	 * path
	 */
	private String url;
	/**
	 * First-level menu jump address
	 */
	private String redirect;

	/**
	 * Menu sorting
	 */
	private Double sortNo;

	/**
	 * type（0：First level menu；1：submenu ；2：Button permissions）
	 */
	@Dict(dicCode = "menu_type")
	private Integer menuType;

	/**
	 * Whether it is a leaf node: 1:yes  0:不yes
	 */
	@TableField(value="is_leaf")
	private boolean leaf;
	
	/**
	 * yesno路由菜单: 0:不yes  1:yes（default value1）
	 */
	@TableField(value="is_route")
	private boolean route;


	/**
	 * yesno缓存页面: 0:不yes  1:yes（default value1）
	 */
	@TableField(value="keep_alive")
	private boolean keepAlive;

	/**
	 * describe
	 */
	private String description;

	/**
	 * Creator
	 */
	private String createBy;

	/**
	 * delete status 0normal 1Deleted
	 */
	private Integer delFlag;
	
	/**
	 * yesno配置菜单的数据权限 1yes0no default0
	 */
	private Integer ruleFlag;
	
	/**
	 * yesno隐藏路由菜单: 0no,1yes（default value0）
	 */
	private boolean hidden;

	/**
	 * yesno隐藏Tab: 0no,1yes（default value0）
	 */
	private boolean hideTab;

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
	
	/**Button permissions状态(0invalid1efficient)*/
	private java.lang.String status;
	
	/**alwaysShow*/
    private boolean alwaysShow;

	/*update_begin author:wuxianquan date:20190908 for:Entity adds fields */
    /** How to open external link menu 0/Open internally 1/Open externally */
    private boolean internalOrExternal;
	/*update_end author:wuxianquan date:20190908 for:Entity adds fields */

    public SysPermission() {
    	
    }
    public SysPermission(boolean index) {
    	if(index) {
			this.id = "9502685863ab87f0ad1134142788a385";
			this.name = DefIndexConst.DEF_INDEX_NAME;
			this.component = DefIndexConst.DEF_INDEX_COMPONENT;
			this.componentName = "dashboard-analysis";
			this.url = DefIndexConst.DEF_INDEX_URL;
        	this.icon="home";
        	this.menuType=0;
        	this.sortNo=0.0;
        	this.ruleFlag=0;
        	this.delFlag=0;
        	this.alwaysShow=false;
        	this.route=true;
        	this.keepAlive=true;
        	this.leaf=true;
        	this.hidden=false;
    	}
    	
    }
}
