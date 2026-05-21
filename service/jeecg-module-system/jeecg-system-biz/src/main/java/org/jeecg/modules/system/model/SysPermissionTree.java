package org.jeecg.modules.system.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jeecg.modules.system.entity.SysPermission;

/**
 * @Description: menu tree，Encapsulation tree structure
 * @author: jeecg-boot
 */
public class SysPermissionTree implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private String id;

	private String key;
	private String title;

	/**
	 * fatherid
	 */
	private String parentId;

	/**
	 * Menu name
	 */
	private String name;

	/**
	 * Menu permission encoding
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
	 * Jump to web link
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
	private Integer menuType;

	/**
	 * Whether it is a leaf node: 1:yes 0:不yes
	 */
	private boolean isLeaf;
	
	/**
	 * yesno路由菜单: 0:不yes  1:yes（default value1）
	 */
	private boolean route;


	/**
	 * yesno路缓存页面: 0:不yes  1:yes（default value1）
	 */
	private boolean keepAlive;


	/**
	 * describe
	 */
	private String description;

	/**
	 * delete status 0normal 1Deleted
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

	/**alwaysShow*/
    private boolean alwaysShow;
    /**yesno隐藏路由菜单: 0no,1yes（default value0）*/
    private boolean hidden;
    
    /**Button permissions状态(0invalid1efficient)*/
	private java.lang.String status;

	/*update_begin author:wuxianquan date:20190908 for:modelAdd field */
	/** How to open external link menu 0/Open internally 1/Open externally */
	private boolean internalOrExternal;
	/*update_end author:wuxianquan date:20190908 for:modelAdd field */

	/*update_begin author:liusq date:20230601 for:【issues/4986】modelIncreasehideTabField */
	/**
	 * yesno隐藏Tab: 0no,1yes（default value0）
	 */
	private boolean hideTab;
	/*update_end author:liusq date:20230601 for:【issues/4986】modelIncreasehideTabField */

	public SysPermissionTree() {
	}

	public SysPermissionTree(SysPermission permission) {
		this.key = permission.getId();
		this.id = permission.getId();
		this.perms = permission.getPerms();
		this.permsType = permission.getPermsType();
		this.component = permission.getComponent();
		this.componentName = permission.getComponentName();
		this.createBy = permission.getCreateBy();
		this.createTime = permission.getCreateTime();
		this.delFlag = permission.getDelFlag();
		this.description = permission.getDescription();
		this.icon = permission.getIcon();
		this.isLeaf = permission.isLeaf();
		this.menuType = permission.getMenuType();
		this.name = permission.getName();
		this.parentId = permission.getParentId();
		this.sortNo = permission.getSortNo();
		this.updateBy = permission.getUpdateBy();
		this.updateTime = permission.getUpdateTime();
		this.redirect = permission.getRedirect();
		this.url = permission.getUrl();
		this.hidden = permission.isHidden();
		this.route = permission.isRoute();
		this.keepAlive = permission.isKeepAlive();
		this.alwaysShow= permission.isAlwaysShow();
		/*update_begin author:wuxianquan date:20190908 for:Assignment */
		this.internalOrExternal = permission.isInternalOrExternal();
		/*update_end author:wuxianquan date:20190908 for:Assignment */
		this.title=permission.getName();
		/*update_end author:liusq date:20230601 for:【issues/4986】modelIncreasehideTabField */
		this.hideTab = permission.isHideTab();
		/*update_end author:liusq date:20230601 for:【issues/4986】modelIncreasehideTabField */
		if (!permission.isLeaf()) {
			this.children = new ArrayList<SysPermissionTree>();
		}
		this.status = permission.getStatus();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	private List<SysPermissionTree> children;

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean leaf) {
		isLeaf = leaf;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean isAlwaysShow() {
		return alwaysShow;
	}

	public void setAlwaysShow(boolean alwaysShow) {
		this.alwaysShow = alwaysShow;
	}
	public List<SysPermissionTree> getChildren() {
		return children;
	}

	public void setChildren(List<SysPermissionTree> children) {
		this.children = children;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Double getSortNo() {
		return sortNo;
	}

	public void setSortNo(Double sortNo) {
		this.sortNo = sortNo;
	}

	public Integer getMenuType() {
		return menuType;
	}

	public void setMenuType(Integer menuType) {
		this.menuType = menuType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRoute() {
		return route;
	}

	public void setRoute(boolean route) {
		this.route = route;
	}

	public Integer getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(Integer delFlag) {
		this.delFlag = delFlag;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getPerms() {
		return perms;
	}

	public void setPerms(String perms) {
		this.perms = perms;
	}

	public boolean getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public String getPermsType() {
		return permsType;
	}

	public void setPermsType(String permsType) {
		this.permsType = permsType;
	}

	public java.lang.String getStatus() {
		return status;
	}

	public void setStatus(java.lang.String status) {
		this.status = status;
	}

	/*update_begin author:wuxianquan date:20190908 for:get setmethod */
	public boolean isInternalOrExternal() {
		return internalOrExternal;
	}

	public void setInternalOrExternal(boolean internalOrExternal) {
		this.internalOrExternal = internalOrExternal;
	}
	/*update_end author:wuxianquan date:20190908 for:get set method */

	public boolean isHideTab() {
		return hideTab;
	}

	public void setHideTab(boolean hideTab) {
		this.hideTab = hideTab;
	}
}
