package org.jeecg.modules.system.util;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.service.ISysRoleIndexService;

import java.util.List;

/**
 * @Author: scott
 * @Date: 2019-04-03
 */
public class PermissionDataUtil {

    /**
     * path：views/
     */
    private static final String PATH_VIEWS = "views/";

    /**
     * path：src/views/
     */
    private static final String PATH_SRC_VIEWS = "src/views/";

    /**
     * .vuesuffix
     */
    private static final String VUE_SUFFIX = ".vue";

	/**
	 * Intelligent handling of erroneous data，Simplify user error operations
	 * 
	 * @param permission
	 */
	public static SysPermission intelligentProcessData(SysPermission permission) {
		if (permission == null) {
			return null;
		}

		// components
		if (oConvertUtils.isNotEmpty(permission.getComponent())) {
			String component = permission.getComponent();
			if (component.startsWith(SymbolConstant.SINGLE_SLASH)) {
				component = component.substring(1);
			}
			if (component.startsWith(PATH_VIEWS)) {
				component = component.replaceFirst(PATH_VIEWS, "");
			}
			if (component.startsWith(PATH_SRC_VIEWS)) {
				component = component.replaceFirst(PATH_SRC_VIEWS, "");
			}
			if (component.endsWith(VUE_SUFFIX)) {
				component = component.replace(VUE_SUFFIX, "");
			}
			permission.setComponent(component);
		}
		
		// askURL
		if (oConvertUtils.isNotEmpty(permission.getUrl())) {
			String url = permission.getUrl();
			if (url.endsWith(VUE_SUFFIX)) {
				url = url.replace(VUE_SUFFIX, "");
			}
			if (!url.startsWith(CommonConstant.STR_HTTP) && !url.startsWith(SymbolConstant.SINGLE_SLASH)&&!url.trim().startsWith(SymbolConstant.DOUBLE_LEFT_CURLY_BRACKET)) {
				url = SymbolConstant.SINGLE_SLASH + url;
			}
			permission.setUrl(url);
		}
		
		// 一级菜单默认components
		if (0 == permission.getMenuType() && oConvertUtils.isEmpty(permission.getComponent())) {
			// 一级菜单默认components
			permission.setComponent("layouts/RouteView");
		}
		return permission;
	}
	
	/**
	 * if notindexpage neednew Put one inlistmiddle
	 * @param metaList
	 */
	public static void addIndexPage(List<SysPermission> metaList) {
		boolean hasIndexMenu = false;
		SysRoleIndex defIndexCfg = PermissionDataUtil.getDefIndexConfig();
		for (SysPermission sysPermission : metaList) {
			if(defIndexCfg.getUrl().equals(sysPermission.getUrl())) {
				hasIndexMenu = true;
				break;
			}
		}
		if(!hasIndexMenu) {
			metaList.add(0,new SysPermission(true));
		}
	}

	/**
	 * Determine whether to authorize the home page
	 * @param metaList
	 * @return
	 */
	public static boolean hasIndexPage(List<SysPermission> metaList, SysRoleIndex defIndexCfg){
		boolean hasIndexMenu = false;
		for (SysPermission sysPermission : metaList) {
			if(defIndexCfg.getUrl().equals(sysPermission.getUrl())) {
				hasIndexMenu = true;
				break;
			}
		}
		return hasIndexMenu;
	}

	/**
	 * passid判断是否授权某个page
	 *
	 * @param metaList
	 * @return
	 */
	public static boolean hasMenuById(List<SysPermission> metaList, String id) {
		for (SysPermission sysPermission : metaList) {
			if (id.equals(sysPermission.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the default homepage configuration
	 */
	public static SysRoleIndex getDefIndexConfig() {
		ISysRoleIndexService sysRoleIndexService = SpringContextUtils.getBean(ISysRoleIndexService.class);
		return sysRoleIndexService.queryDefaultIndex();
	}

}
