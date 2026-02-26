package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.vo.SysDepartExportVo;
import org.jeecg.modules.system.vo.SysDepartPositionVo;
import org.jeecg.modules.system.vo.SysUserDepVo;
import org.jeecg.modules.system.vo.lowapp.ExportDepartVo;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <p>
 * department Mapper interface
 * <p>
 * 
 * @Author: Steve
 * @Since：   2019-01-22
 */
public interface SysDepartMapper extends BaseMapper<SysDepart> {
	
	/**
	 * According to userIDQuerydepartmentgather
     * @param userId userid
     * @return List<SysDepart>
	 */
	public List<SysDepart> queryUserDeparts(@Param("userId") String userId);

	/**
	 * According to user名Querydepartment
	 *
	 * @param username
	 * @return
	 */
	public List<SysDepart> queryDepartsByUsername(@Param("username") String username);
	
	/**
	 * According to user名Querydepartment
	 *
	 * @param userId
	 * @return
	 */
	public List<String> queryDepartsByUserId(@Param("userId") String userId);

    /**
     * 通过department编码获取departmentid
     * @param orgCode department编码
     * @return String
     */
	@Select("select id from sys_depart where org_code=#{orgCode}")
	public String queryDepartIdByOrgCode(@Param("orgCode") String orgCode);
	
    /**
     * 通过departmentid，Querydepartment下的user的账号
     * @param departIds departmentIDgather
     * @return String
     */
	public List<String> queryUserAccountByDepartIds(@Param("departIds") List<String> departIds);

    /**
     * 通过departmentid Querydepartmentid,fatherid
     * @param departId departmentid
     * @return
     */
	@Select("select id,parent_id from sys_depart where id=#{departId}")
	public SysDepart getParentDepartId(@Param("departId") String departId);

	/**
	 *  according todepartmentIdQuery,当前和下级所有departmentIDS
	 * @param departId
	 * @return
	 */
	List<String> getSubDepIdsByDepId(@Param("departId") String departId);

	/**
	 * according todepartment编码获取department下所有IDS
	 * @param orgCodes
	 * @return
	 */
	List<String> getSubDepIdsByOrgCodes(@org.apache.ibatis.annotations.Param("orgCodes") String[] orgCodes);

    /**
     * according toparent_idQuery下级department
     * @param parentId fatherid
     * @return List<SysDepart>
     */
    List<SysDepart> queryTreeListByPid(@Param("parentId") String parentId);
	/**
	 * according toid下级department数量
	 * @param parentId
	 * @return
	 */
	@Select("SELECT count(*) FROM sys_depart where del_flag ='0' AND parent_id = #{parentId,jdbcType=VARCHAR}")
    Integer queryCountByPid(@Param("parentId")String parentId);
	/**
	 * according toOrgCodQuery所属公司信息
	 * @param orgCode
	 * @return
	 */
	SysDepart queryCompByOrgCode(@Param("orgCode")String orgCode);
	/**
	 * according toid下级department
	 * @param parentId
	 * @return
	 */
	@Select("SELECT * FROM sys_depart where del_flag ='0' AND parent_id = #{parentId,jdbcType=VARCHAR}")
	List<SysDepart> queryDeptByPid(@Param("parentId")String parentId);

	/**
	 * 通过father级idand tenantsidQuerydepartment
	 * @param parentId
	 * @param tenantId
	 * @return
	 */
	@InterceptorIgnore(tenantLine = "true")
	List<SysDepart> queryBookDepTreeSync(@Param("parentId") String parentId, @Param("tenantId") Integer tenantId, @Param("departName") String departName);

	@InterceptorIgnore(tenantLine = "true")
	@Select("SELECT * FROM sys_depart where id = #{id,jdbcType=VARCHAR}")
	SysDepart getDepartById(@Param("id") String id);

	@InterceptorIgnore(tenantLine = "true")
	List<SysDepart> getMaxCodeDepart(@Param("page") Page<SysDepart> page, @Param("parentId") String parentId);

	/**
	 * 修改department状态字段： Is it a child node?
	 * @param id departmentid
	 * @param leaf leaf node
	 * @return int
	 */
	@Update("UPDATE sys_depart SET iz_leaf=#{leaf} WHERE id = #{id}")
	int setMainLeaf(@Param("id") String id, @Param("leaf") Integer leaf);

	/**
	 * Get tenantsid和departmentfatherid获取的department数据
	 * @param tenantId
	 * @param parentId
	 * @return
	 */
    List<ExportDepartVo> getDepartList(@Param("parentId") String parentId, @Param("tenantId") Integer tenantId);

	/**
	 * according todepartment名称and tenantsid获取department数据
	 * @param departName
	 * @param tenantId
	 * @return
	 */
	List<SysDepart> getDepartByName(@Param("departName")String departName, @Param("tenantId")Integer tenantId,@Param("parentId") String parentId);

	/**
	 * according todepartmentid获取userid和department名称
	 * @param userList
	 * @return
	 */
	List<SysUserDepVo> getUserDepartByTenantUserId(@Param("userList") List<SysUser> userList, @Param("tenantId") Integer tenantId);

	/**
	 * according todepartment名称and tenantsid获取分页department数据
	 * @param page
	 * @param departName
	 * @param tenantId
	 * @param parentId
	 * @return
	 */
	List<SysDepart> getDepartPageByName(@Param("page") Page<SysDepart> page, @Param("departName") String departName, @Param("tenantId") Integer tenantId, @Param("parentId") String parentId);

	/**
	 * Get tenantsid和departmentfatherid获取的department数据
	 * @param tenantId
	 * @param parentId
	 * @return
	 */
    List<SysDepartExportVo> getSysDepartList(@Param("parentId") String parentId,@Param("tenantId") Integer tenantId, List<String> idList);

    /**
     * according to多个departmentid获取department数据
     * 
     * @param departIds
     * @return
     */
    List<SysUserDepVo> getDepartByIds(List<String> departIds);

    /**
     * According to userid获取department数据
     *
     * @param userList
     * @return
     */
    @InterceptorIgnore(tenantLine = "true")
    List<SysUserDepVo> getUserDepartByUserId(@Param("userList")List<SysUser> userList);

    /**
     * according tofather级id/Rank/departmentid获取department岗位信息
     * 
     * @param parentId
     * @param postLevel
     * @param departId
     */
    List<SysDepart> getDepartPositionByParentId(@Param("parentId") String parentId, @Param("postLevel") Integer postLevel, @Param("departId") String departId);

    /**
     * according tofather级id获取department中的数据
     * @param parentId
     * @return
     */
    @Select("select id, depart_name, parent_id, iz_leaf, org_category, org_code from sys_depart where parent_id = #{parentId} order by depart_order,create_time desc")
    List<SysDepart> getDepartByParentId(@Param("parentId") String parentId);

    /**
     * according todepartmentidQuerydepartment信息
     
     * @param departId
     * @return department岗位信息
     */
    SysDepartPositionVo getDepartPostByDepartId(@Param("departId") String departId);

    /**
     * according tofather级departmentidQuerydepartment信息
     
     * @param orgCode
     * @return department岗位信息
     */
    List<SysDepartPositionVo> getDepartPostByOrgCode(@Param("orgCode") String orgCode);

    /**
     * according todepartmentid获取departmentcode
     * @param idList
     * @return
     */
    List<String> getDepCodeByDepIds(@Param("idList") List<String> idList);

    /**
     * according tofather级departmentid和职务名称查找departmentid
     * 
     * @param parentId
     * @param postName
     * @return
     */
    String getDepIdByDepIdAndPostName(@Param("parentId") String parentId, @Param("postName") String postName);

    /**
     * according todepartmentid 获取Rank名称
     * 
     * @param depId
     * @return
     */
    String getPostNameByPostId(@Param("depId") String depId);
}
