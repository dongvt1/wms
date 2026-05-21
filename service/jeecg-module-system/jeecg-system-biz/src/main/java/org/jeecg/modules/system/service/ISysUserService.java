package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysUserSysDepPostModel;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.vo.SysUserExportVo;
import org.jeecg.modules.system.vo.lowapp.DepartAndUserInfo;
import org.jeecg.modules.system.vo.lowapp.UpdateDepartInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * User table Service category
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface ISysUserService extends IService<SysUser> {

	/**
	 * Query user data list
	 * 
	 * @param req
	 * @param queryWrapper
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	Result<IPage<SysUser>> queryPageList(HttpServletRequest req, QueryWrapper<SysUser> queryWrapper, Integer pageSize, Integer pageNo);
	
	/**
	 * reset password
	 *
	 * @param username
	 * @param oldpassword
	 * @param newpassword
	 * @param confirmpassword
	 * @return
	 */
	public Result<?> resetPassword(String username, String oldpassword, String newpassword, String confirmpassword);

	/**
	 * Change password
	 *
	 * @param sysUser
	 * @return
	 */
	public Result<?> changePassword(SysUser sysUser);

	/**
	 * Delete user
	 * @param userId
	 * @return
	 */
	public boolean deleteUser(String userId);

	/**
	 * 批量Delete user
	 * @param userIds
	 * @return
	 */
	public boolean deleteBatchUsers(String userIds);

    /**
     * Query based on username
     * @param username username
     * @return SysUser
     */
	public SysUser getUserByName(String username);
	
	/**
	 * Add user and user role relationships
	 * @param user
	 * @param roles
	 */
	public void addUserWithRole(SysUser user,String roles);
	
	
	/**
	 * Modify the relationship between users and user roles
	 * @param user
	 * @param roles
	 */
	public void editUserWithRole(SysUser user,String roles);

	/**
	 * Get the user's authorization role
	 * @param username
	 * @return
	 */
	public List<String> getRole(String username);

	/**
	 * Get the dynamic home page based on the role of the logged in user
	 *
	 * @param username
	 * @param version front endUIVersion
	 * @return
	 */
	public SysRoleIndex getDynamicIndexByUserRole(String username,String version);
	
	/**
	  * Querying user information includes Department information
	 * @param username
	 * @return
	 */
	@Deprecated
	public SysUserCacheInfo getCacheUser(String username);

	/**
	 * According to departmentIdQuery
	 * @param page
     * @param departId departmentid
     * @param username User account name
	 * @return
	 */
	public IPage<SysUser> getUserByDepId(Page<SysUser> page, String departId, String username);

	/**
	 * According to departmentIdsQuery
	 * @param page
     * @param departIds  departmentidgather
     * @param username User account name
	 * @return
	 */
	public IPage<SysUser> getUserByDepIds(Page<SysUser> page, List<String> departIds, String username);

	/**
	 * according to userIdsQuery，Queryuser所属department的名称（多个department名逗号隔开）
	 * @param userIds
	 * @return
	 */
	public Map<String,String> getDepNamesByUserIds(List<String> userIds);

    /**
     * According to department Id and QueryWrapper Query
     *
     * @param page
     * @param departId
     * @param queryWrapper
     * @return
     */
    //update-begin-author:taoyan date:2022-9-13 for: VUEN-2245【loopholes】发现新loopholes待处理20220906 ----sqlinjection Method not used，Note out
    // public IPage<SysUser> getUserByDepartIdAndQueryWrapper(Page<SysUser> page, String departId, QueryWrapper<SysUser> queryWrapper);
	//update-end-author:taoyan date:2022-9-13 for: VUEN-2245【loopholes】发现新loopholes待处理20220906 ----sqlinjection Method not used，Note out

	/**
	 * according to orgCode Queryuser，包括子department下的user
	 *
	 * @param orgCode
	 * @param userParams userQuery条件，Can be null
	 * @param page Paging parameters
	 * @return
	 */
	IPage<SysUserSysDepartModel> queryUserByOrgCode(String orgCode, SysUser userParams, IPage page);

	/**
	 * according toRoleIdQuery
	 * @param page
     * @param roleId Roleid
     * @param username user account
     * @param realname User name
	 * @return
	 */
	public IPage<SysUser> getUserByRoleId(Page<SysUser> page,String roleId, String username, String realname);

	/**
	 * 通过username获取userRolegather
	 *
	 * @param username username
	 * @return Rolegather
	 */
	Set<String> getUserRolesSet(String username);
	
	/**
	 * 通过username获取userRolegather
	 *
	 * @param userId userid
	 * @return Rolegather
	 */
	Set<String> getUserRoleSetById(String userId);

	/**
	 * 通过username获取user权限gather
	 *
	 * @param userId userid
	 * @return 权限gather
	 */
	Set<String> getUserPermissionsSet(String userId);
	
	/**
	 * according tousername设置departmentID
	 * @param username
	 * @param orgCode
	 */
	void updateUserDepart(String username,String orgCode,Integer loginTenantId);
	
	/**
	 * according toPhone number获取usernameand密码
     * @param phone Phone number
     * @return SysUser
	 */
	public SysUser getUserByPhone(String phone);


	/**
	 * according toMail获取user
     * @param email Mail
     * @return SysUser
     */
	public SysUser getUserByEmail(String email);


	/**
	 * 添加useranduserdepartment关系
	 * @param user
	 * @param selectedParts
	 */
	void addUserWithDepart(SysUser user, String selectedParts);

	/**
	 * 编辑useranduserdepartment关系
	 * @param user
	 * @param departs
	 */
	void editUserWithDepart(SysUser user, String departs);
	
	/**
	   * 校验user是否有效
	 * @param sysUser
	 * @return
	 */
	Result checkUserIsEffective(SysUser sysUser);

	/**
	 * Query被逻辑删除的user
     * @return List<SysUser>
	 */
	List<SysUser> queryLogicDeleted();

	/**
	 * Query被逻辑删除的user（可拼装Query条件）
     * @param wrapper
     * @return List<SysUser>
	 */
	List<SysUser> queryLogicDeleted(LambdaQueryWrapper<SysUser> wrapper);

	/**
	 * 还原被逻辑删除的user
     * @param userIds  存放useridgather
     * @param updateEntity
     * @return boolean
	 */
	boolean revertLogicDeleted(List<String> userIds, SysUser updateEntity);

	/**
	 * 彻底删除被逻辑删除的user
     * @param userIds 存放useridgather
     * @return boolean
	 */
	boolean removeLogicDeleted(List<String> userIds);

    /**
     * 更新Phone number、Mail空字符串为 null
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    boolean updateNullPhoneEmail();

	/**
	 * 保存第三方user信息
	 * @param sysUser
	 */
	void saveThirdUser(SysUser sysUser);

	/**
	 * According to departmentIdsQuery
	 * @param departIds departmentidgather
     * @param username User account name
	 * @return
	 */
	List<SysUser> queryByDepIds(List<String> departIds, String username);

	/**
     * 保存user
     *
     * @param user            user
     * @param selectedRoles   选择的Roleid，Multiple separated by commas
     * @param selectedDeparts 选择的departmentid，Multiple separated by commas
     * @param relTenantIds    multiple tenantsid
     * @param izSyncPack Do you need to synchronize tenant packages?
     */
	void saveUser(SysUser user, String selectedRoles, String selectedDeparts, String relTenantIds, boolean izSyncPack);

	/**
	 * 编辑user
	 * @param user user
	 * @param roles 选择的Roleid，Multiple separated by commas
	 * @param departs 选择的departmentid，Multiple separated by commas
	 * @param relTenantIds multiple tenantsid
	 * @param updateFromPage Updated from page [TV360X-1686]
	 */
	void editUser(SysUser user, String roles, String departs, String relTenantIds, String updateFromPage);

	/**
     * userIdconvert tousername
     * @param userIdList
     * @return List<String>
     */
	List<String> userIdToUsername(Collection<String> userIdList);


	/**
	 * 获取user信息 Field information is encrypted 【加密user信息】
	 * @param username
	 * @return
	 */
	LoginUser getEncodeUserInfo(String username);

    /**
     * user离职
     * @param username
     */
    void userQuit(String username);

    /**
     * Get the list of resigned personnel
	 * @param tenantId tenantid
     * @return
     */
    List<SysUser> getQuitList(Integer tenantId);

    /**
     * 更新刪除状态and离职状态
     * @param userIds  存放useridgather
     * @param sysUser
     * @return boolean
     */
    void updateStatusAndFlag(List<String> userIds, SysUser sysUser);

	/**
	 * 设置登录tenant
	 * @param sysUser
	 * @return
	 */
	Result<JSONObject>  setLoginTenant(SysUser sysUser, JSONObject obj, String username, Result<JSONObject> result);

	//--- author:taoyan date:20221231 for: QQYUN-3515【application】application下的组织机构管理功能，Detailed implementation ---
	/**
	 * 批量编辑user信息
	 * @param json
	 */
	void batchEditUsers(JSONObject json);

	/**
	 * according to关键词Queryuseranddepartment
	 * @param keyword
	 * @return
	 */
	DepartAndUserInfo searchByKeyword(String keyword);

	/**
	 * Query department修改的信息
	 * @param departId
	 * @return
	 */
	UpdateDepartInfo getUpdateDepartInfo(String departId);

	/**
	 * 修改department相关信息
	 * @param updateDepartInfo
	 */
	void doUpdateDepartInfo(UpdateDepartInfo updateDepartInfo);

	/**
	 * Set up a person in charge Cancel the person in charge
	 * @param json
	 */
	void changeDepartChargePerson(JSONObject json);
	//--- author:taoyan date:20221231 for: QQYUN-3515【application】application下的组织机构管理功能，Detailed implementation ---
	
	/**
	 * 编辑tenantuser
	 * @param sysUser
	 * @param tenantId
	 * @param departs
	 */
	void editTenantUser(SysUser sysUser, String tenantId, String departs, String roles);

/**
	 * 修改useraccount状态
	 * @param id accountid
	 * @param status account状态
	 */
	void updateStatus(String id, String status);

	/**
	 * 导出application下的userExcel
	 * @param request
	 * @return
	 */
	ModelAndView exportAppUser(HttpServletRequest request);

	/**
	 * 导入application下的user
	 * @param request
	 * @return
	 */
	Result<?> importAppUser(HttpServletRequest request);

	/**
	 * 验证user是否为管理员
	 * @param ids
	 */
	void checkUserAdminRejectDel(String ids);

	/**
	 * 修改Phone number
	 * 
	 * @param json
	 * @param username
	 */
    void changePhone(JSONObject json, String username);

	/**
	 * Send SMS verification code
	 * 
	 * @param jsonObject
	 * @param username username
	 * @param ipAddress ipaddress
	 */
	void sendChangePhoneSms(JSONObject jsonObject, String username, String ipAddress);

	/**
	 * 发送注销userPhone number验证密码[Exclusively for KOKAO Cloud]
	 * @param jsonObject
	 * @param username
	 * @param ipAddress
	 */
	void sendLogOffPhoneSms(JSONObject jsonObject, String username, String ipAddress);

	/**
	 * user注销[Exclusively for KOKAO Cloud]
	 * @param jsonObject
	 * @param username
	 */
	void userLogOff(JSONObject jsonObject, String username);

    /**
     * 获取departmentanduser关系的导出信息
     * @param pageList
     */
    List<SysUserExportVo> getDepartAndRoleExportMsg(List<SysUser> pageList);

    /**
     * 导入user
     *
     * @param request
     */
    Result<?> importSysUser(HttpServletRequest request);

    /**
     * 没有绑定Phone number 直接Change password
     *
     * @param oldPassword
     * @param password
     * @param username
     */
    void updatePasswordNotBindPhone(String oldPassword, String password, String username);

	/**
	 * according tousername称QueryuserandDepartment information
	 * @param userName
	 * @return
	 */
	Map<String, String> queryUserAndDeptByName(String userName);

    /**
     * Querydepartment、岗位下的user 包括子department下的user
     * 
     * @param orgCode
     * @param userParams
     * @param page
     * @return
     */
    IPage<SysUserSysDepPostModel> queryDepartPostUserByOrgCode(String orgCode, SysUser userParams, IPage page);

}
