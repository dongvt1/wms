package org.jeecg.modules.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.model.TreeModel;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Menu permission table Mapper interface
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
	/**
	   * Via parent menuIDQuery submenu
	 * @param parentId
	 * @return
	 */
	public List<TreeModel> queryListByParentId(@Param("parentId") String parentId);
	
	/**
	 * Query user permissions based on user
     * @param userId userID
     * @return List<SysPermission>
	 */
	public List<SysPermission> queryByUser(@Param("userId") String userId);
	
	/**
	 * Modify menu status field： Is it a child node?
     * @param id menuid
     * @param leaf leaf node
     * @return int
	 */
	@Update("update sys_permission set is_leaf=#{leaf} where id = #{id}")
	public int setMenuLeaf(@Param("id") String id,@Param("leaf") int leaf);

	/**
	 * switchvue3menu
	 */
	@Update("alter table sys_permission rename to sys_permission_v2")
	public void backupVue2Menu();
	@Update("alter table sys_permission_v3 rename to sys_permission")
	public void changeVue3Menu();
	
	/**
	 * Obtain data permissions for fuzzy matching rulesURL
     * @return List<String>
	 */
	@Select("SELECT url FROM sys_permission WHERE del_flag = 0 and menu_type = 2 and url like '%*%'")
    public List<String> queryPermissionUrlWithStar();


	/**
	 * 根据user账号查询menu权限
	 * @param sysPermission
	 * @param username
	 * @return
	 */
	public int queryCountByUsername(@Param("username") String username, @Param("permission") SysPermission sysPermission);


	/**
	 * Query department authority data
	 * @param departId
	 * @return
	 */
	List<SysPermission> queryDepartPermissionList(@Param("departId") String departId);

	/**
	 * 根据user名称和testRoleidQuery permissions
	 * @return
	 */
	@InterceptorIgnore(tenantLine = "true")
    List<SysPermission> queryPermissionByTestRoleId();
}
