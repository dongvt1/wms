package org.jeecg.modules.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.system.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * User role table Mapper interface
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * Query role collection by user account
     * @param username User account name
     * @return List<String>
     */
	@Select("select role_code from sys_role where id in (select role_id from sys_user_role where user_id = (select id from sys_user where username=#{username}))")
	List<String> getRoleByUserName(@Param("username") String username);
	
    /**
     * Query role collection by user account
     * @param userId userid
     * @return List<String>
     */
	@Select("select role_code from sys_role where id in (select role_id from sys_user_role where user_id = #{userId})")
	List<String> getRoleCodeByUserId(@Param("userId") String userId);

	/**
     * 通过user账号查询角色Idgather
     * @param username User account name
     * @return List<String>
     */
	@Select("select id from sys_role where id in (select role_id from sys_user_role where user_id = (select id from sys_user where username=#{username}))")
	List<String> getRoleIdByUserName(@Param("username") String username);

}
