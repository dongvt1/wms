package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jeecg.dingtalk.api.base.JdtBaseAPI;
import com.jeecg.dingtalk.api.core.response.Response;
import com.jeecg.dingtalk.api.core.util.HttpUtil;
import com.jeecg.dingtalk.api.core.vo.AccessToken;
import com.jeecg.dingtalk.api.core.vo.PageResult;
import com.jeecg.dingtalk.api.department.JdtDepartmentAPI;
import com.jeecg.dingtalk.api.department.vo.Department;
import com.jeecg.dingtalk.api.message.JdtMessageAPI;
import com.jeecg.dingtalk.api.message.vo.ActionCardMessage;
import com.jeecg.dingtalk.api.message.vo.MarkdownMessage;
import com.jeecg.dingtalk.api.message.vo.Message;
import com.jeecg.dingtalk.api.message.vo.TextMessage;
import com.jeecg.dingtalk.api.oauth2.JdtOauth2API;
import com.jeecg.dingtalk.api.oauth2.vo.ContactUser;
import com.jeecg.dingtalk.api.user.JdtUserAPI;
import com.jeecg.dingtalk.api.user.body.GetUserListBody;
import com.jeecg.dingtalk.api.user.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.*;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.thirdapp.JdtDepartmentTreeVo;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * third partyAppdocking：DingTalk implementation class
 * @author: jeecg-boot
 */
@Slf4j
@Service
public class ThirdAppDingtalkServiceImpl implements IThirdAppService {

    @Autowired
    JeecgBaseConfig jeecgBaseConfig;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private ISysThirdAccountService sysThirdAccountService;
    @Autowired
    private ISysUserDepartService sysUserDepartService;
    @Autowired
    private ISysPositionService sysPositionService;
    @Autowired
    private SysAnnouncementSendMapper sysAnnouncementSendMapper;
    @Autowired
    private SysThirdAppConfigMapper configMapper;
    @Autowired
    private SysUserTenantMapper userTenantMapper;
    @Autowired
    private SysTenantMapper tenantMapper;

    /**
     * third partyAPPtype，Currently fixed to dingtalk
     */
    public final String THIRD_TYPE = "dingtalk";

    @Override
    public String getAccessToken() {
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        SysThirdAppConfig config = getDingThirdAppConfig();
        if(null != config){
            return getTenantAccessToken(config);
        }
        log.warn("DingTalk is not configured under the tenant");
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        return null;
    }

    // update：2022-1-21，updateBy：sunjianlei; for 【JTC-704】【DingTalk】Department synchronization successful，Didn't actually happen，Background promptipwhitelist
    @Override
    public SyncInfoVo syncLocalDepartmentToThirdApp(String ids) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessTokenFailed to obtain！");
            return syncInfo;
        }
        // Get【DingTalk】all departments
        List<Response<Department>> departments = JdtDepartmentAPI.listAllResponse(accessToken);
        // deleteDingTalk有但本地没有ofdepartment（Mainly based on local department data）（DingTalk不能创建同名department，Can only be deleted first）
        List<SysDepart> sysDepartList = sysDepartService.list();
        for1:
        for (Response<Department> departmentRes : departments) {
            // Determine whether the department query is successful
            if (!departmentRes.isSuccess()) {
                syncInfo.addFailInfo(departmentRes.getErrmsg());
                // 88 yes ip 不在whitelistoferror code，If you encounter this error code，There is no need to perform any subsequent operations.，因for肯定都yesfailof
                if (new Integer(88).equals(departmentRes.getErrcode())) {
                    return syncInfo;
                }
                continue;
            }
            Department department = departmentRes.getResult();
            for (SysDepart depart : sysDepartList) {
                // idsame，Represents existing，Do not delete
                String sourceIdentifier = department.getSource_identifier();
                if (sourceIdentifier != null && sourceIdentifier.equals(depart.getId())) {
                    continue for1;
                }
            }
            // Looping to this point means that there is no local，delete
            int deptId = department.getDept_id();
            // DingTalk不允许delete带有userofdepartment，So you need to judge，Move users from departments with users to the root department
            Response<List<String>> userIdRes = JdtUserAPI.getUserListIdByDeptId(deptId, accessToken);
            if (userIdRes.isSuccess() && userIdRes.getResult().size() > 0) {
                for (String userId : userIdRes.getResult()) {
                    User updateUser = new User();
                    updateUser.setUserid(userId);
                    updateUser.setDept_id_list(1);
                    JdtUserAPI.update(updateUser, accessToken);
                }
            }
            JdtDepartmentAPI.delete(deptId, accessToken);
        }
        // Get本地所有department树结构
        List<SysDepartTreeModel> sysDepartsTree = sysDepartService.queryTreeList();
        // -- DingTalk不能创建新of顶级department，So the new top division'sparentIdjust for1
        Department parent = new Department();
        parent.setDept_id(1);
        // recursive sync department
        departments = JdtDepartmentAPI.listAllResponse(accessToken);
        this.syncDepartmentRecursion(sysDepartsTree, departments, parent, accessToken, syncInfo);
        return syncInfo;
    }

    /**
     * recursive sync department到本地
     * @param sysDepartsTree
     * @param departments
     * @param parent
     * @param accessToken
     * @param syncInfo
     */
    public void syncDepartmentRecursion(List<SysDepartTreeModel> sysDepartsTree, List<Response<Department>> departments, Department parent, String accessToken, SyncInfoVo syncInfo) {
        if (sysDepartsTree != null && sysDepartsTree.size() != 0) {
            for1:
            for (SysDepartTreeModel depart : sysDepartsTree) {
                for (Response<Department> departmentRes : departments) {
                    // Determine whether the department query is successful
                    if (!departmentRes.isSuccess()) {
                        syncInfo.addFailInfo(departmentRes.getErrmsg());
                        continue;
                    }
                    Department department = departmentRes.getResult();
                    // idsame，Represents existing，Perform modification operations
                    String sourceIdentifier = department.getSource_identifier();
                    if (sourceIdentifier != null && sourceIdentifier.equals(depart.getId())) {
                        this.sysDepartToDtDepartment(depart, department, parent.getDept_id());
                        Response<JSONObject> response = JdtDepartmentAPI.update(department, accessToken);
                        if (response.isSuccess()) {
                            // followed by synchronized children
                            this.syncDepartmentRecursion(depart.getChildren(), departments, department, accessToken, syncInfo);
                        }
                        // Collect error information
                        this.syncDepartCollectErrInfo(response, depart, syncInfo);
                        // Break out of outer loop
                        continue for1;
                    }
                }
                // 循环到此说明yes新department，Direct interface creation
                Department newDepartment = this.sysDepartToDtDepartment(depart, parent.getDept_id());
                Response<Integer> response = JdtDepartmentAPI.create(newDepartment, accessToken);
                // Created successfully，will be returnedidbind to local
                if (response.getResult() != null) {
                    Department newParent = new Department();
                    newParent.setDept_id(response.getResult());
                    // followed by synchronized children
                    this.syncDepartmentRecursion(depart.getChildren(), departments, newParent, accessToken, syncInfo);
                }
                // Collect error information
                this.syncDepartCollectErrInfo(response, depart, syncInfo);
            }
        }
    }

//    @Override
//    public SyncInfoVo syncThirdAppDepartmentToLocal(String ids) {
//        SyncInfoVo syncInfo = new SyncInfoVo();
//        String accessToken = this.getAccessToken();
//        if (accessToken == null) {
//            syncInfo.addFailInfo("accessTokenFailed to obtain！");
//            return syncInfo;
//        }
//        // Get【DingTalk】all departments
//        List<Department> departments = JdtDepartmentAPI.listAll(accessToken);
//        String username = JwtUtil.getUserNameByToken(SpringContextUtils.getHttpServletRequest());
//        List<JdtDepartmentTreeVo> departmentTreeList = JdtDepartmentTreeVo.listToTree(departments);
//        // recursive sync department
//        this.syncDepartmentToLocalRecursion(departmentTreeList, null, username, syncInfo, accessToken,false);
//        return syncInfo;
//    }

    public void syncDepartmentToLocalRecursion(List<JdtDepartmentTreeVo> departmentTreeList, String sysParentId, String username, SyncInfoVo syncInfo, String accessToken,Boolean syncUser,Integer tenantId) {

        if (departmentTreeList != null && departmentTreeList.size() != 0) {
            // Record users who have been synchronizedid，When there are multiple departments，Sync only once
            Set<String> syncedUserIdSet = new HashSet<>();
            for (JdtDepartmentTreeVo departmentTree : departmentTreeList) {
                LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
                // according to source_identifier Field query
                //update-begin---author:wangshuai---date:2024-04-10---for:【issues/6017】DingTalk同步department时没有最顶层ofdepartment名，When synchronizing users，The user has no department information---
                queryWrapper.and(item -> item.eq(SysDepart::getId, departmentTree.getSource_identifier()).or().eq(SysDepart::getDingIdentifier,departmentTree.getDept_id()));
                //update-end---author:wangshuai---date:2024-04-10---for:【issues/6017】DingTalk同步department时没有最顶层ofdepartment名，When synchronizing users，The user has no department information---
                SysDepart sysDepart = sysDepartService.getOne(queryWrapper);
                if (sysDepart != null) {
                    //  Perform update operation
                    SysDepart updateSysDepart = this.dtDepartmentToSysDepart(departmentTree, sysDepart);
                    if (sysParentId != null) {
                        updateSysDepart.setParentId(sysParentId);
                        //更新父级department不yes叶子结点
                        sysDepartService.updateIzLeaf(sysParentId,CommonConstant.NOT_LEAF);
                    }
                    try {
                        sysDepartService.updateDepartDataById(updateSysDepart, username);
                        String str = String.format("department %s Update successful！", updateSysDepart.getDepartName());
                        syncInfo.addSuccessInfo(str);
                    } catch (Exception e) {
                        this.syncDepartCollectErrInfo(e, departmentTree, syncInfo);
                    }
                    if (departmentTree.hasChildren()) {
                        // followed by synchronized children
                        this.syncDepartmentToLocalRecursion(departmentTree.getChildren(), updateSysDepart.getId(), username, syncInfo, accessToken,syncUser,tenantId);
                    }
                    //judgeyes否需要同步user
                    if(syncUser){
                        this.addDepartUser(updateSysDepart.getId(),departmentTree.getDept_id(), accessToken, syncInfo, syncedUserIdSet,tenantId);
                    }
                } else {
                    //  Perform new operations
                    SysDepart newSysDepart = this.dtDepartmentToSysDepart(departmentTree, null);
                    if (sysParentId != null) {
                        newSysDepart.setParentId(sysParentId);
                        // 2 = Organizational structure
                        newSysDepart.setOrgCategory("2");
                    } else {
                        // 1 = company
                        newSysDepart.setOrgCategory("1");
                    }
                    try {
                        if(oConvertUtils.isEmpty(departmentTree.getParent_id())){
                            newSysDepart.setDingIdentifier(departmentTree.getDept_id().toString());
                        }
                        newSysDepart.setTenantId(tenantId);
                        sysDepartService.saveDepartData(newSysDepart, username);
                        // 更新DingTalk source_identifier
                        Department updateDtDepart = new Department();
                        updateDtDepart.setDept_id(departmentTree.getDept_id());
                        updateDtDepart.setSource_identifier(newSysDepart.getId());
                        //fornull说明yes最顶级department，最顶级department不允许修改操作
                        if(oConvertUtils.isNotEmpty(newSysDepart.getParentId())){
                            Response response = JdtDepartmentAPI.update(updateDtDepart, accessToken);
                            if (!response.isSuccess()) {
                                throw new RuntimeException(response.getErrmsg());
                            }
                        }
                        String str = String.format("department %s Created successfully！", newSysDepart.getDepartName());
                        syncInfo.addSuccessInfo(str);
                        //judgeyes否需要同步user
                        if(syncUser){
                            this.addDepartUser(newSysDepart.getId(),departmentTree.getDept_id(), accessToken, syncInfo, syncedUserIdSet,tenantId);
                        }
                    } catch (Exception e) {
                        this.syncDepartCollectErrInfo(e, departmentTree, syncInfo);
                    }
                    // followed by synchronized children
                    if (departmentTree.hasChildren()) {
                        this.syncDepartmentToLocalRecursion(departmentTree.getChildren(), newSysDepart.getId(), username, syncInfo, accessToken,syncUser,tenantId);
                    }
                }
            }
        }
    }

    private boolean syncDepartCollectErrInfo(Exception e, Department department, SyncInfoVo syncInfo) {
        String msg;
        if (e instanceof DuplicateKeyException) {
            msg = e.getCause().getMessage();
        } else {
            msg = e.getMessage();
        }
        String str = String.format("department %s(%s) Sync failed！error message：%s", department.getName(), department.getDept_id(), msg);
        syncInfo.addFailInfo(str);
        return false;
    }

    /**
     * 【同步department】收集同步过程中oferror message
     */
    private boolean syncDepartCollectErrInfo(Response<?> response, SysDepartTreeModel depart, SyncInfoVo syncInfo) {
        if (!response.isSuccess()) {
            String str = String.format("department %s(%s) Sync failed！error code：%s——%s", depart.getDepartName(), depart.getOrgCode(), response.getErrcode(), response.getErrmsg());
            syncInfo.addFailInfo(str);
            return false;
        } else {
            String str = String.format("department户 %s(%s) Synchronization successful！", depart.getDepartName(), depart.getOrgCode());
            syncInfo.addSuccessInfo(str);
            return true;
        }
    }

    @Override
    public SyncInfoVo syncLocalUserToThirdApp(String ids) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessTokenFailed to obtain！");
            return syncInfo;
        }
        List<SysUser> sysUsers;
        if (StringUtils.isNotBlank(ids)) {
            String[] idList = ids.split(",");
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SysUser::getId, (Object[]) idList);
            // Get本地指定user
            sysUsers = userMapper.selectList(queryWrapper);
        } else {
            // Get本地所有user
            sysUsers = userMapper.selectList(Wrappers.emptyWrapper());
        }
        // QueryDingTalkall departments，用于同步user和departmentof关系
        List<Department> allDepartment = JdtDepartmentAPI.listAll(accessToken);

        for (SysUser sysUser : sysUsers) {
            // External simulated login temporary account，Out of sync
            if ("_reserve_user_external".equals(sysUser.getUsername())) {
                continue;
            }
            // DingTalkuser信息，Not fornullIndicates that it has been synchronized
            Response<User> dtUserInfo;
            /*
             * judgeyes否同步过of逻辑：
             * 1. Query sys_third_account（third party账号表）yes否有数据，If a representative is synced
             * 2. There is no local table，Just use your mobile phone number to determine，Do not reuseusername(User account)judge。
             */
            SysThirdAccount sysThirdAccount = sysThirdAccountService.getOneBySysUserId(sysUser.getId(), THIRD_TYPE);
            if (sysThirdAccount != null && oConvertUtils.isNotEmpty(sysThirdAccount.getThirdUserId())) {
                // sys_third_account Table match successful，passthird partyuserIdQuery出third partyuserInfo
                dtUserInfo = JdtUserAPI.getUserById(sysThirdAccount.getThirdUserId(), accessToken);
            } else {
                // Mobile phone number matching
                Response<String> thirdUserId = JdtUserAPI.getUseridByMobile(sysUser.getPhone(), accessToken);
                // Mobile phone number matching成功
                if (thirdUserId.isSuccess() && oConvertUtils.isNotEmpty(thirdUserId.getResult())) {
                    // passQuery到ofuserIdQueryuserDetails
                    dtUserInfo = JdtUserAPI.getUserById(thirdUserId.getResult(), accessToken);
                } else {
                    // Mobile phone number matchingfail，Try usingusernamematch
                    dtUserInfo = JdtUserAPI.getUserById(sysUser.getUsername(), accessToken);
                }
            }
            String dtUserId;
            // api 接口yes否执行成功
            boolean apiSuccess;
            // Update after synchronization，Otherwise create
            if (dtUserInfo != null && dtUserInfo.isSuccess() && dtUserInfo.getResult() != null) {
                User dtUser = dtUserInfo.getResult();
                dtUserId = dtUser.getUserid();
                User updateQwUser = this.sysUserToDtUser(sysUser, dtUser, allDepartment);
                Response<JSONObject> updateRes = JdtUserAPI.update(updateQwUser, accessToken);
                // Collection successful/Failure message
                apiSuccess = this.syncUserCollectErrInfo(updateRes, sysUser, syncInfo);
            } else {
                User newQwUser = this.sysUserToDtUser(sysUser, allDepartment);
                Response<String> createRes = JdtUserAPI.create(newQwUser, accessToken);
                dtUserId = createRes.getResult();
                // Collection successful/Failure message
                apiSuccess = this.syncUserCollectErrInfo(createRes, sysUser, syncInfo);
            }

            // api Interface execution successful，and sys_third_account 表matchfail，just to sys_third_account Insert a piece of data into
            boolean flag = (sysThirdAccount == null || oConvertUtils.isEmpty(sysThirdAccount.getThirdUserId()));
            if (apiSuccess && flag) {
                if (sysThirdAccount == null) {
                    sysThirdAccount = new SysThirdAccount();
                    sysThirdAccount.setSysUserId(sysUser.getId());
                    sysThirdAccount.setStatus(1);
                    sysThirdAccount.setDelFlag(0);
                    sysThirdAccount.setThirdType(THIRD_TYPE);
                }
                // 设置third partyappuserID
                sysThirdAccount.setThirdUserId(dtUserId);
                sysThirdAccountService.saveOrUpdate(sysThirdAccount);
            }
        }
        return syncInfo;
    }

//    @Override
//    public SyncInfoVo syncThirdAppUserToLocal() {
//        SyncInfoVo syncInfo = new SyncInfoVo();
//        String accessToken = this.getAccessToken();
//        if (accessToken == null) {
//            syncInfo.addFailInfo("accessTokenFailed to obtain！");
//            return syncInfo;
//        }
//
//        // Get本地user
//        List<SysUser> sysUsersList = userMapper.selectList(Wrappers.emptyWrapper());
//
//        // QueryDingTalkall departments，用于同步user和departmentof关系
//        List<Department> allDepartment = JdtDepartmentAPI.listAll(accessToken);
//        // according toDingTalkdepartmentQuery所有DingTalkuser，For reverse synchronization to local
//        List<User> ddUserList = this.getDtAllUserByDepartment(allDepartment, accessToken);
//        // Record users who have been synchronizedid，When there are multiple departments，Sync only once
//        Set<String> syncedUserIdSet = new HashSet<>();
//
//        for (User dtUserInfo : ddUserList) {
//            if (syncedUserIdSet.contains(dtUserInfo.getUserid())) {
//                continue;
//            }
//            syncedUserIdSet.add(dtUserInfo.getUserid());
//            SysThirdAccount sysThirdAccount = sysThirdAccountService.getOneByThirdUserId(dtUserInfo.getUserid(), THIRD_TYPE);
//            List<SysUser> collect = sysUsersList.stream().filter(user -> (dtUserInfo.getMobile().equals(user.getPhone()) || dtUserInfo.getUserid().equals(user.getUsername()))
//                                                                 ).collect(Collectors.toList());
//            if (collect != null && collect.size() > 0) {
//                SysUser sysUserTemp = collect.get(0);
//                // 循环到此说明usermatch成功，Perform update operation
//                SysUser updateSysUser = this.dtUserToSysUser(dtUserInfo, sysUserTemp);
//                try {
//                    userMapper.updateById(updateSysUser);
//                    String str = String.format("user %s(%s) Update successful！", updateSysUser.getRealname(), updateSysUser.getUsername());
//                    syncInfo.addSuccessInfo(str);
//                } catch (Exception e) {
//                    this.syncUserCollectErrInfo(e, dtUserInfo, syncInfo);
//                }
//                //third party账号关系表
//                this.thirdAccountSaveOrUpdate(sysThirdAccount, updateSysUser.getId(), dtUserInfo.getUserid());
//            }else{
//                // if notmatch到user，Then go through the creation logic
//                SysUser newSysUser = this.dtUserToSysUser(dtUserInfo);
//                try {
//                    userMapper.insert(newSysUser);
//                    String str = String.format("user %s(%s) Created successfully！", newSysUser.getRealname(), newSysUser.getUsername());
//                    syncInfo.addSuccessInfo(str);
//                } catch (Exception e) {
//                    this.syncUserCollectErrInfo(e, dtUserInfo, syncInfo);
//                }
//                //third party账号关系表
//                this.thirdAccountSaveOrUpdate(null, newSysUser.getId(), dtUserInfo.getUserid());
//            }
//        }
//        return syncInfo;
//    }

//    private List<User> getDtAllUserByDepartment(List<Department> allDepartment, String accessToken) {
//        // according toDingTalkdepartmentQuery所有DingTalkuser，For reverse synchronization to local
//        List<User> userList = new ArrayList<>();
//        for (Department department : allDepartment) {
//            this.getUserListByDeptIdRecursion(department.getDept_id(), 0, userList, accessToken);
//        }
//        return userList;
//    }

    /**
     * 递归Query所有user
     */
    private void getUserListByDeptIdRecursion(int deptId, int cursor, List<User> userList, String accessToken) {
        // according toDingTalkdepartmentQuery所有DingTalkuser，For reverse synchronization to local
        GetUserListBody getUserListBody = new GetUserListBody(deptId, cursor, 100);
        Response<PageResult<User>> response = JdtUserAPI.getUserListByDeptId(getUserListBody, accessToken);
        if (response.isSuccess()) {
            PageResult<User> page = response.getResult();
            userList.addAll(page.getList());
            if (page.getHas_more()) {
                this.getUserListByDeptIdRecursion(deptId, page.getNext_cursor(), userList, accessToken);
            }
        }
    }

    /**
     * 保存或修改third partyLog in表
     *
     * @param sysThirdAccount third party账户表object，fornullJust add data，Otherwise, modify
     * @param sysUserId       本地系统userID
     * @param user            DingTalkuser
     */
    private void thirdAccountSaveOrUpdate(SysThirdAccount sysThirdAccount, String sysUserId, User user, Integer tenantId) {
        if (sysThirdAccount == null) {
            sysThirdAccount = new SysThirdAccount();
            sysThirdAccount.setSysUserId(sysUserId);
            sysThirdAccount.setThirdUserUuid(user.getUnionid());
            sysThirdAccount.setStatus(1);
            sysThirdAccount.setTenantId(tenantId);
            sysThirdAccount.setDelFlag(0);
            sysThirdAccount.setThirdType(THIRD_TYPE);
        }
        sysThirdAccount.setThirdUserId(user.getUserid());
        if(oConvertUtils.isEmpty(sysThirdAccount.getRealname())){
            sysThirdAccount.setRealname(user.getName());
        }
        sysThirdAccountService.saveOrUpdate(sysThirdAccount);
    }

    /**
     * 【同步user】收集同步过程中oferror message
     */
    private boolean syncUserCollectErrInfo(Response<?> response, SysUser sysUser, SyncInfoVo syncInfo) {
        if (!response.isSuccess()) {
            String str = String.format("user %s(%s) Sync failed！error code：%s——%s", sysUser.getUsername(), sysUser.getRealname(), response.getErrcode(), response.getErrmsg());
            syncInfo.addFailInfo(str);
            return false;
        } else {
            String str = String.format("user %s(%s) Synchronization successful！", sysUser.getUsername(), sysUser.getRealname());
            syncInfo.addSuccessInfo(str);
            return true;
        }
    }

    /**
     * 【同步user】收集同步过程中oferror message
     */
    private boolean syncUserCollectErrInfo(Exception e, User dtUser, SyncInfoVo syncInfo) {
        String msg;
        if (e instanceof DuplicateKeyException) {
            msg = e.getCause().getMessage();
            String emailUniq = "uniq_sys_user_email";
            if(msg.contains(emailUniq)){
                msg = "Duplicate email address，Please change your email address";
            }
            String workNoUniq="uniq_sys_user_work_no";
            if(msg.contains(workNoUniq)){
                msg = "Duplicate job number，Please change your job number";
            }
        } else {
            msg = e.getMessage();
        }
        String str = String.format("user %s(%s) Sync failed！error message：%s", dtUser.getUserid(), dtUser.getName(), msg);
        syncInfo.addFailInfo(str);
        return false;
    }


    /**
     * 【同步user】WillSysUser转for【DingTalk】ofUserobject（创建新user）
     */
    private User sysUserToDtUser(SysUser sysUser, List<Department> allDepartment) {
        User user = new User();
        // pass username to relate
        user.setUserid(sysUser.getUsername());
        return this.sysUserToDtUser(sysUser, user, allDepartment);
    }

    /**
     * 【同步user】WillSysUser转for【DingTalk】ofUserobject（更新旧user）
     */
    private User sysUserToDtUser(SysUser sysUser, User user, List<Department> allDepartment) {
        user.setName(sysUser.getRealname());
        user.setMobile(sysUser.getPhone());
        user.setTelephone(sysUser.getTelephone());
        user.setJob_number(sysUser.getWorkNo());
        // job translation
        //update-begin---author:wangshuai ---date:20230220  for：[QQYUN-3980]Under organization management Position function Job listings plus tenantsid Add position-user关联表------------
        //Getuser职位名称
        List<SysPosition> positionList = sysPositionService.getPositionList(sysUser.getId());
        if(null != positionList && positionList.size()>0){
            String positionName = positionList.stream().map(SysPosition::getName).collect(Collectors.joining(SymbolConstant.COMMA));
            user.setTitle(positionName);
        }
        //update-end---author:wangshuai ---date:20230220  for：[QQYUN-3980]Under organization management Position function Job listings plus tenantsid Add position-user关联表------------
        user.setEmail(sysUser.getEmail());
        // Query并同步userdepartment关系
        List<SysDepart> departList = this.getUserDepart(sysUser);
        if (departList != null) {
            List<Integer> departmentIdList = new ArrayList<>();
            for (SysDepart sysDepart : departList) {
                // 企业微信ofdepartmentid
                Department department = this.getDepartmentByDepartId(sysDepart.getId(), allDepartment);
                if (department != null) {
                    departmentIdList.add(department.getDept_id());
                }
            }
            user.setDept_id_list(departmentIdList.toArray(new Integer[]{}));
            user.setDept_order_list(null);
        }
        if (oConvertUtils.isEmpty(user.getDept_id_list())) {
            // 没有找到matchdepartment，同步到根department下
            user.setDept_id_list(1);
            user.setDept_order_list(null);
        }
        // --- DingTalk没有逻辑delete功能
        // sysUser.getDelFlag()
        // --- DingTalk没有冻结、Enable disabled features
        // sysUser.getStatus()
        return user;
    }


    /**
     * 【同步user】Will【DingTalk】ofUserobject转forSysUser（创建新user）
     */
    private SysUser dtUserToSysUser(User dtUser) {
        SysUser sysUser = new SysUser();
        sysUser.setDelFlag(0);
        // pass username to relate
        sysUser.setUsername(dtUser.getMobile());
        // 密码默认forfor手机号加门牌号，Randomly add salt
        String password = "", salt = oConvertUtils.randomGen(8);
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        if(tenantId>0){
            SysTenant tenant = tenantMapper.selectById(tenantId);
            password = tenant.getHouseNumber()+dtUser.getMobile();
        }else{
            password = dtUser.getMobile();
        }
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
        sysUser.setSalt(salt);
        sysUser.setPassword(passwordEncode);
        // update-begin--Author:liusq Date:20210713 for：DingTalk同步到本地of人员没有状态，As a result, you cannot log in after synchronization. #I3ZC2L
        sysUser.setStatus(1);
        // update-end--Author:liusq Date:20210713 for：DingTalk同步到本地of人员没有状态，As a result, you cannot log in after synchronization. #I3ZC2L
        return this.dtUserToSysUser(dtUser, sysUser);
    }

    /**
     * 【同步user】Will【DingTalk】ofUserobject转forSysUser（更新旧user）
     */
    private SysUser dtUserToSysUser(User dtUser, SysUser oldSysUser) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(oldSysUser, sysUser);
        sysUser.setTelephone(dtUser.getTelephone());
        //如果真实姓名fornullof情况下，Will change my real name
        if(oConvertUtils.isEmpty(oldSysUser.getRealname())){
            sysUser.setRealname(dtUser.getName());
        }
        // 因for唯一键约束of原因，如果原数据和旧数据same，Will not update
        if (oConvertUtils.isNotEmpty(dtUser.getEmail()) && !dtUser.getEmail().equals(sysUser.getEmail())) {
            sysUser.setEmail(dtUser.getEmail());
        } else {
            sysUser.setEmail(null);
        }
        // 因for唯一键约束of原因，如果原数据和旧数据same，Will not update
        if (oConvertUtils.isNotEmpty(dtUser.getMobile()) && !dtUser.getMobile().equals(sysUser.getPhone())) {
            sysUser.setPhone(dtUser.getMobile());
        } else {
            sysUser.setPhone(null);
        }
        // Set employee number，如果工号fornull，then useusername
        if (oConvertUtils.isEmpty(dtUser.getJob_number())) {
            sysUser.setWorkNo(dtUser.getUserid());
        } else {
            sysUser.setWorkNo(dtUser.getJob_number());
        }
        // --- DingTalk没有逻辑delete功能
        // sysUser.getDelFlag()
        // --- DingTalk没有冻结、Enable disabled features
        // sysUser.getStatus()
        return sysUser;
    }


    /**
     * Queryuser和departmentof关系
     */
    private List<SysDepart> getUserDepart(SysUser sysUser) {
        // according touserdepartment关系表Query出userofdepartment
        LambdaQueryWrapper<SysUserDepart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserDepart::getUserId, sysUser.getId());
        List<SysUserDepart> sysUserDepartList = sysUserDepartService.list(queryWrapper);
        if (sysUserDepartList.size() == 0) {
            return null;
        }
        // according touserdepartment
        LambdaQueryWrapper<SysDepart> departQueryWrapper = new LambdaQueryWrapper<>();
        List<String> departIdList = sysUserDepartList.stream().map(SysUserDepart::getDepId).collect(Collectors.toList());
        departQueryWrapper.in(SysDepart::getId, departIdList);
        List<SysDepart> departList = sysDepartService.list(departQueryWrapper);
        return departList.size() == 0 ? null : departList;
    }

    /**
     * according tosysDepartIdQueryDingTalkofdepartment
     */
    private Department getDepartmentByDepartId(String departId, List<Department> allDepartment) {
        for (Department department : allDepartment) {
            if (departId.equals(department.getSource_identifier())) {
                return department;
            }
        }
        return null;
    }


    /**
     * 【同步department】WillSysDepartTreeModel转for【DingTalk】ofDepartmentobject（创建新department）
     */
    private Department sysDepartToDtDepartment(SysDepartTreeModel departTree, Integer parentId) {
        Department department = new Department();
        department.setSource_identifier(departTree.getId());
        return this.sysDepartToDtDepartment(departTree, department, parentId);
    }

    /**
     * 【同步department】WillSysDepartTreeModel转for【DingTalk】ofDepartmentobject
     */
    private Department sysDepartToDtDepartment(SysDepartTreeModel departTree, Department department, Integer parentId) {
        department.setName(departTree.getDepartName());
        department.setParent_id(parentId);
        department.setOrder(departTree.getDepartOrder());
        return department;
    }


    /**
     * 【同步department】Will【DingTalk】ofDepartmentobject转forSysDepartTreeModel
     */
    private SysDepart dtDepartmentToSysDepart(Department department, SysDepart departTree) {
        SysDepart sysDepart = new SysDepart();
        if (departTree != null) {
            BeanUtils.copyProperties(departTree, sysDepart);
        }
        sysDepart.setDepartName(department.getName());
        sysDepart.setDepartOrder(department.getOrder());
        sysDepart.setDingIdentifier(department.getSource_identifier());
        return sysDepart;
    }

    @Override
    public int removeThirdAppUser(List<String> userIdList) {
        // judge启用状态
        SysThirdAppConfig appConfig = getDingThirdAppConfig();
        if (null == appConfig) {
            return -1;
        }
        int count = 0;
        if (userIdList != null && userIdList.size() > 0) {
            //update-begin---author:wangshuai ---date:20230209  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
            String accessToken = this.getTenantAccessToken(appConfig);
            //update-end---author:wangshuai ---date:20230209  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
            if (accessToken == null) {
                return count;
            }
            LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            queryWrapper.in(SysThirdAccount::getSysUserId, userIdList);
            // according touserId，Getthird partyuserofid
            List<SysThirdAccount> thirdAccountList = sysThirdAccountService.list(queryWrapper);
            List<String> thirdUserIdList = thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());

            for (String thirdUserId : thirdUserIdList) {
                if (oConvertUtils.isNotEmpty(thirdUserId)) {
                    // 没有批量deleteof接口
                    Response<JSONObject> response = JdtUserAPI.delete(thirdUserId, accessToken);
                    if (response.getErrcode() == 0) {
                        count++;
                    }
                }
            }
        }
        return count;

    }

    @Override
    public boolean sendMessage(MessageDTO message) {
        return this.sendMessage(message, false);
    }

    /**
     * Send message
     *
     * @param message
     * @param verifyConfig
     * @return
     */
    @Override
    public boolean sendMessage(MessageDTO message, boolean verifyConfig) {
        Response<String> response;
        if (message.isMarkdown()) {
            response = this.sendMarkdownResponse(message, verifyConfig);
        } else {
            response = this.sendMessageResponse(message, verifyConfig);
        }
        if (response != null) {
            return response.isSuccess();
        }
        return false;
    }

    /**
     * sendMarkdowninformation
     * @param message
     * @param verifyConfig
     * @return
     */
    public Response<String> sendMarkdownResponse(MessageDTO message, boolean verifyConfig) {
        SysThirdAppConfig config = this.getDingThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            return null;
        }
        // 封装DingTalkinformation
        String title = message.getTitle();
        String content = message.getContent();
        String agentId = config.getAgentId();
        Message<MarkdownMessage> mdMessage = new Message<>(agentId, new MarkdownMessage(title, content));
        if (message.getToAll()) {
            mdMessage.setTo_all_user(true);
        } else {
            String[] toUsers = message.getToUser().split(",");
            // passthird party账号表Query出third partyuserId
            int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE);
            List<SysThirdAccount> thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(toUsers, THIRD_TYPE,tenantId);
            List<String> dtUserIds = thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
            mdMessage.setUserid_list(dtUserIds);
        }
        return JdtMessageAPI.sendMarkdownMessage(mdMessage, accessToken);
    }

    public Response<String> sendMessageResponse(MessageDTO message, boolean verifyConfig) {
        SysThirdAppConfig config = this.getDingThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            return null;
        }
        // 封装DingTalkinformation
        String content = message.getContent();
        String agentId = config.getAgentId();
        Message<TextMessage> textMessage = new Message<>(agentId, new TextMessage(content));
        if (message.getToAll()) {
            textMessage.setTo_all_user(true);
        } else {
            String[] toUsers = message.getToUser().split(",");
            // passthird party账号表Query出third partyuserId
            int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE);
            List<SysThirdAccount> thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(toUsers, THIRD_TYPE, tenantId);
            List<String> dtUserIds = thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
            textMessage.setUserid_list(dtUserIds);
        }
        return JdtMessageAPI.sendTextMessage(textMessage, accessToken);
    }

    public boolean recallMessage(String msgTaskId) {
        Response<JSONObject> response = this.recallMessageResponse(msgTaskId);
        if (response == null) {
            return false;
        }
        return response.isSuccess();
    }

    /**
     * 撤回information
     *
     * @param msgTaskId
     * @return
     */
    public Response<JSONObject> recallMessageResponse(String msgTaskId) {
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        SysThirdAppConfig config = this.getDingThirdAppConfig();
        String accessToken = this.getTenantAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        String agentId = config.getAgentId();
        return JdtMessageAPI.recallMessage(agentId, msgTaskId, accessToken);
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
    }

    /**
     * send卡片information（SysAnnouncementcustom made）
     *
     * @param announcement
     * @param ddMobileUrl DingTalk打开网页地址
     * @param verifyConfig yes否验证配置（未启用ofAPP会拒绝send）
     * @return
     */
    public Response<String> sendActionCardMessage(SysAnnouncement announcement, String ddMobileUrl, boolean verifyConfig) {
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        SysThirdAppConfig config = this.getDingThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getTenantAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        String agentId = config.getAgentId();
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        String emptySuffix = null;
        if (oConvertUtils.isNotEmpty(announcement.getMsgAbstract())) {
            String msgAbstract = announcement.getMsgAbstract().trim();
            log.info("GetDingTalk通知parameter，msgAbstract: {}", msgAbstract);
            if (msgAbstract.startsWith("{") && msgAbstract.endsWith("}")) {
                //如果摘要存ofyes业务扩展parameterjson，Then take the announcement content
                emptySuffix = announcement.getMsgContent();
            } else {
                //如果摘要Not fornull且yes文本格式，then use摘要
                emptySuffix = msgAbstract;
            }
        } else {
            emptySuffix = "null";
        }
        
        String markdown = "### " + announcement.getTitile() + "\n" + emptySuffix;
        log.info("DingTalk推送parameter, markdown: {}", markdown);
        ActionCardMessage actionCard = new ActionCardMessage(markdown);
        actionCard.setTitle(announcement.getTitile());
        actionCard.setSingle_title("Details");
        String baseUrl = null;
        //优先pass请求Getbasepath，Get不到读取 jeecg.domainUrl.pc
        try {
            baseUrl = RestUtil.getBaseUrl();
        } catch (Exception e) {
            log.warn(e.getMessage());
            baseUrl =  jeecgBaseConfig.getDomainUrl().getPc();
            //e.printStackTrace();
        }

        log.info("GetDingTalk打开网页地址，parameter ddMobileUrl: {}", ddMobileUrl);
        String ddSingleUrl = null;
        if (oConvertUtils.isNotEmpty(ddMobileUrl)) {
            ddSingleUrl = ddMobileUrl;
        } else {
            ddSingleUrl = baseUrl + "/sys/annountCement/show/" + announcement.getId();
        }
        actionCard.setSingle_url(ddSingleUrl);
        log.info("GetDingTalk打开网页地址，final address ddSingleUrl: {}", ddSingleUrl);
        
        Message<ActionCardMessage> actionCardMessage = new Message<>(agentId, actionCard);
        if (CommonConstant.MSG_TYPE_ALL.equals(announcement.getMsgType())) {
            actionCardMessage.setTo_all_user(true);
            return JdtMessageAPI.sendActionCardMessage(actionCardMessage, accessToken);
        } else {
            // WilluserId转forusername
            String[] userIds = null;
            String userId = announcement.getUserIds();
            if(oConvertUtils.isNotEmpty(userId)){
                userIds = userId.substring(0, (userId.length() - 1)).split(",");
            }else{
                LambdaQueryWrapper<SysAnnouncementSend> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysAnnouncementSend::getAnntId, announcement.getId());
                SysAnnouncementSend sysAnnouncementSend = sysAnnouncementSendMapper.selectOne(queryWrapper);
                userIds = new String[] {sysAnnouncementSend.getUserId()};
            }

            if(userIds!=null){
                LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(SysUser::getId, userIds);
                List<SysUser> userList = userMapper.selectList(queryWrapper);
                String[] usernameList = userList.stream().map(SysUser::getUsername).toArray(String[] :: new);

                // passthird party账号表Query出third partyuserId
                int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE);
                List<SysThirdAccount> thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(usernameList, THIRD_TYPE, tenantId);
                List<String> dtUserIds = thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
                actionCardMessage.setUserid_list(dtUserIds);
                return JdtMessageAPI.sendActionCardMessage(actionCardMessage, accessToken);
            }
        }
        return null;
    }

    /**
     * OAuth2Log in，成功返回Log inofSysUser，Return on failurenull
     */
    public SysUser oauth2Login(String authCode,Integer tenantId) {
        this.tenantIzExist(tenantId);
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        SysThirdAppConfig dtConfig = configMapper.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        // 1. according to免登授权码Getuser AccessToken
        String userAccessToken = JdtOauth2API.getUserAccessToken(dtConfig.getClientId(), dtConfig.getClientSecret(), authCode);
        if (userAccessToken == null) {
            log.error("oauth2Login userAccessToken is null");
            throw new JeecgBootException("Please view the applicationkeyand application秘钥yes否正确，organizeIDyes否match");
        }
        // 2. according touser AccessToken Get当前userof基本信息（Not includeduserId）
        ContactUser contactUser = JdtOauth2API.getContactUsers("me", userAccessToken);
        if (contactUser == null) {
            log.error("oauth2Login contactUser is null");
            throw new JeecgBootException("GetDingTalkuser信息fail");
        }
        String unionId = contactUser.getUnionId();
        // 3. according toGet到of unionId 换取user userId
        String accessToken = this.getTenantAccessToken(dtConfig);
        if (accessToken == null) {
            log.error("oauth2Login accessToken is null");
            throw new JeecgBootException("Please view the applicationkeyand application秘钥yes否正确，organizeIDyes否match");
        }
        Response<String> getUserIdRes = JdtUserAPI.getUseridByUnionid(unionId, accessToken);
        if (!getUserIdRes.isSuccess()) {
            log.error("oauth2Login getUseridByUnionid failed: " + JSON.toJSONString(getUserIdRes));
            throw new JeecgBootException("GetDingTalkuser信息fail");
        }
        String appUserId = getUserIdRes.getResult();
        log.info("appUserId: " + appUserId);
        if (appUserId != null) {
            // judgethird partyuser表有没有这个人
            LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            queryWrapper.eq(SysThirdAccount::getTenantId, tenantId);
            //update-begin---author:wangshuai---date:2023-12-04---for: authLog in需要联查一下---
            queryWrapper.and((wrapper)->wrapper.eq(SysThirdAccount::getThirdUserUuid,appUserId).or().eq(SysThirdAccount::getThirdUserId,appUserId));
            //update-end---author:wangshuai---date:2023-12-04---for: authLog in需要联查一下---
            SysThirdAccount thirdAccount = sysThirdAccountService.getOne(queryWrapper);
            if (thirdAccount != null) {
                return this.getSysUserByThird(thirdAccount, null, appUserId, accessToken,tenantId);
            } else {
                // Create a new account directly
                User appUser = JdtUserAPI.getUserById(appUserId, accessToken).getResult();
                //update-begin---author:wangshuai ---date:20230328  for：[QQYUN-4883]DingTalkauthLog in同一个tenant下有同一个userid------------
                //should saveuuid
                ThirdLoginModel tlm = new ThirdLoginModel(THIRD_TYPE, appUser.getUnionid(), appUser.getName(), appUser.getAvatar());
                //update-end---author:wangshuai ---date:20230328  for：[QQYUN-4883]DingTalkauthLog in同一个tenant下有同一个userid------------
                thirdAccount = sysThirdAccountService.saveThirdUser(tlm,tenantId);
                return this.getSysUserByThird(thirdAccount, appUser, null, null,tenantId);
            }
        }
        return null;
    }

    /**
     * according tothird party账号Get本地账号，Create if it does not exist
     *
     * @param thirdAccount
     * @param appUser
     * @param appUserId
     * @param accessToken
     * @param tenantId
     * @return
     */
    private SysUser getSysUserByThird(SysThirdAccount thirdAccount, User appUser, String appUserId, String accessToken, Integer tenantId) {
        String sysUserId = thirdAccount.getSysUserId();
        if (oConvertUtils.isNotEmpty(sysUserId)) {
            return userMapper.selectById(sysUserId);
        } else {
            // if not sysUserId ，It means there is no account bound，Get到手机号之后进行绑定
            if (appUser == null) {
                appUser = JdtUserAPI.getUserById(appUserId, accessToken).getResult();
            }
            // judge系统里yes否有这个手机号ofuser
            SysUser sysUser = userMapper.getUserByPhone(appUser.getMobile());
            if (sysUser != null) {
                thirdAccount.setAvatar(appUser.getAvatar());
                thirdAccount.setRealname(appUser.getName());
                thirdAccount.setThirdUserId(appUser.getUserid());
                //update-begin---author:wangshuai ---date:20230328  for：[QQYUN-4883]DingTalkauthLog in同一个tenant下有同一个userid------------
                thirdAccount.setThirdUserUuid(appUser.getUnionid());
                //update-end---author:wangshuai ---date:20230328  for：[QQYUN-4883]DingTalkauthLog in同一个tenant下有同一个userid------------
                thirdAccount.setSysUserId(sysUser.getId());
                sysThirdAccountService.updateById(thirdAccount);
                return sysUser;
            } else {
                // If not, just create the logic
                return sysThirdAccountService.createUser(appUser.getMobile(), appUser.getUnionid(),tenantId);
            }

        }
    }

    //========================begin 应用低代码DingTalk同步userdepartment专用 ====================

    /**
     * according totype和tenantidGetDingTalk配置
     * @return
     */
    private SysThirdAppConfig getDingThirdAppConfig(){
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        this.tenantIzExist(tenantId);
        return configMapper.getThirdConfigByThirdType(tenantId,MessageTypeEnum.DD.getType());
    }

    /**
     * GetDingTalkaccessToken
     * @param config
     * @return
     */
    private String getTenantAccessToken(SysThirdAppConfig config) {
        if(null == config){
            return null;
        }
        AccessToken accessToken = JdtBaseAPI.getAccessToken(config.getClientId(), config.getClientSecret());
        if (accessToken != null) {
            return accessToken.getAccessToken();
        }
        log.warn("GetAccessTokenfail");
        return null;
    }

    /**
     * 添加或保存usertenant
     * @param userId
     * @param isUpdate yes否yes新增
     */
    private void createUserTenant(String userId,Boolean isUpdate){
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
            //judge当前useryes否已在该tenant下面
            Integer count = userTenantMapper.userTenantIzExist(userId, tenantId);
            //count for0 新增tenantuser,Otherwise, no need to add
            if(count == 0){
                SysUserTenant userTenant = new SysUserTenant();
                userTenant.setTenantId(tenantId);
                userTenant.setUserId(userId);
                userTenant.setStatus(isUpdate?CommonConstant.USER_TENANT_UNDER_REVIEW:CommonConstant.USER_TENANT_NORMAL);
                userTenantMapper.insert(userTenant);
            }
        }
    }

    /**
     * 同步user和department
     * @return
     */
    public SyncInfoVo syncThirdAppDepartmentUserToLocal() {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessTokenFailed to obtain！");
            return syncInfo;
        }
        // Get【DingTalk】all departments
        List<Department> departments = JdtDepartmentAPI.listAll(accessToken);
        //update-begin---author:wangshuai---date:2024-06-25---for:【TV360X-1316】DingTalk同步提示information不正确---
        if(departments.isEmpty()){
            throw new JeecgBootBizTipException("请查看配置parameter和whitelistyes否配置！");
        }
        //update-end---author:wangshuai---date:2024-06-25---for:【TV360X-1316】DingTalk同步提示information不正确---
        String username = JwtUtil.getUserNameByToken(SpringContextUtils.getHttpServletRequest());
        List<JdtDepartmentTreeVo> departmentTreeList = JdtDepartmentTreeVo.listToTree(departments);
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        // recursive sync department
        this.syncDepartmentToLocalRecursion(departmentTreeList, null, username, syncInfo, accessToken,true,tenantId);
        return syncInfo;
    }


    /**
     * 添加user及userdepartment关系
     * @param departId departmentid
     * @param dingDepartId DingTalkdepartmentid
     * @param accessToken
     * @param syncInfo
     * @param syncedUserIdSet
     */
    private void addDepartUser(String departId, Integer dingDepartId, String accessToken, SyncInfoVo syncInfo, Set<String> syncedUserIdSet, Integer tenantId) {
        List<User> userList = new ArrayList<>();
        getUserListByDeptIdRecursion(dingDepartId, 0, userList, accessToken);
        for (User user : userList) {
            if (syncedUserIdSet.contains(user.getUserid())) {
                //需要同步userdepartment
                this.syncAddOrUpdateUserDepart(user.getUserid(),departId);
                continue;
            }
            syncedUserIdSet.add(user.getUserid());
            SysUser userByPhone = userMapper.getUserByPhone(user.getMobile());
            SysThirdAccount sysThirdAccount = sysThirdAccountService.getOneByUuidAndThirdType(user.getUnionid(), THIRD_TYPE,tenantId,user.getUserid());
            if (null != userByPhone) {
                // 循环到此说明usermatch成功，Perform update operation
                SysUser updateSysUser = this.dtUserToSysUser(user, userByPhone);
                try {
                    userMapper.updateById(updateSysUser);
                    String str = String.format("user %s(%s) Update successful！", updateSysUser.getRealname(), updateSysUser.getUsername());
                    //update-begin---author:wangshuai---date:2024-06-24---for:【TV360X-1317】DingTalk同步 Synchronization successful之后 Repeat prompt---
                    if(!syncInfo.getSuccessInfo().contains(str)){
                        syncInfo.addSuccessInfo(str);
                    }
                    //update-end---author:wangshuai---date:2024-06-24---for:【TV360X-1317】DingTalk同步 Synchronization successful之后 Repeat prompt---
                } catch (Exception e) {
                    this.syncUserCollectErrInfo(e, user, syncInfo);
                }
                //third party账号关系表
                this.thirdAccountSaveOrUpdate(sysThirdAccount, updateSysUser.getId(), user, tenantId);
                //Create current tenant
                this.createUserTenant(updateSysUser.getId(),true);
                //需要同步userdepartment
                this.syncAddOrUpdateUserDepart(updateSysUser.getId(),departId);
            } else {
                // if notmatch到user，Then go through the creation logic
                SysUser newSysUser = this.dtUserToSysUser(user);
                try {
                    userMapper.insert(newSysUser);
                    String str = String.format("user %s(%s) Created successfully！", newSysUser.getRealname(), newSysUser.getUsername());
                    syncInfo.addSuccessInfo(str);
                } catch (Exception e) {
                    this.syncUserCollectErrInfo(e, user, syncInfo);
                }
                //third party账号关系表
                this.thirdAccountSaveOrUpdate(sysThirdAccount, newSysUser.getId(), user,tenantId);
                //Create current tenant
                this.createUserTenant(newSysUser.getId(),false);
                //需要同步userdepartment
                this.syncAddOrUpdateUserDepart(newSysUser.getId(),departId);
            }
        }
    }

    /**
     * passuserid和departmentid新增userdepartment关系表
     * @param userId
     * @param departId
     */
    private void syncAddOrUpdateUserDepart(String userId, String departId) {
        //Queryuseryes否在department里面
        LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<>();
        query.eq(SysUserDepart::getDepId,departId);
        query.eq(SysUserDepart::getUserId,userId);
        long count = sysUserDepartService.count(query);
        if(count == 0){
            //does not exist，则新增departmentuser关系
            SysUserDepart sysUserDepart = new SysUserDepart(null,userId,departId);
            sysUserDepartService.save(sysUserDepart);
        }
    }

    //========================end 应用低代码DingTalk同步userdepartment专用 ====================

    /**
     * 验证tenantyes否存在
     * @param tenantId
     */
    public void tenantIzExist(Integer tenantId){
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            Long count = tenantMapper.tenantIzExist(tenantId);
            if(ObjectUtil.isEmpty(count) || 0 == count){
                throw new JeecgBootException("tenantID:" + tenantId + "invalid，平台中does not exist！");
            }
        }
    }

    //=================================== begin 新版DingTalkLog in ============================================
    /**
     * DingTalkLog inGetuser信息
     * 【QQYUN-9421】DingTalkLog in后打开了敲敲云，换其他账号Log in后，再打开敲敲云显示ofyes原来账号of应用
     * @param authCode
     * @param tenantId
     * @return
     */
    public SysUser oauthDingDingLogin(String authCode, Integer tenantId) {
        Long count = tenantMapper.tenantIzExist(tenantId);
        if(ObjectUtil.isEmpty(count) || 0 == count){
            throw new JeecgBootException("tenantdoes not exist！");
        }
        SysThirdAppConfig config = configMapper.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
        String accessToken = this.getTenantAccessToken(config);
        if(StringUtils.isEmpty(accessToken)){
            throw new JeecgBootBizTipException("accessTokenFailed to obtain");
        }
        String getUserInfoUrl = "https://oapi.dingtalk.com/topapi/v2/user/getuserinfo?access_token=" + accessToken;
        Map<String,String> params = new HashMap<>();
        params.put("code",authCode);
        Response<JSONObject> userInfoResponse = HttpUtil.post(getUserInfoUrl, JSON.toJSONString(params));
        if (userInfoResponse.isSuccess()) {
            String userId = userInfoResponse.getResult().getString("userid");
            // judgethird partyuser表有没有这个人
            LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            queryWrapper.eq(SysThirdAccount::getTenantId, tenantId);
            queryWrapper.and((wrapper)->wrapper.eq(SysThirdAccount::getThirdUserUuid,userId).or().eq(SysThirdAccount::getThirdUserId,userId));
            SysThirdAccount thirdAccount = sysThirdAccountService.getOne(queryWrapper);
            if (thirdAccount != null) {
                return this.getSysUserByThird(thirdAccount, null, userId, accessToken, tenantId);
            }else{
                throw new JeecgBootException("该user没有同步，Please sync first！");
            }
        }
        return null;
    }

    /**
     * according totenantidGet企业idand applicationid
     * 【QQYUN-9421】DingTalkLog in后打开了敲敲云，换其他账号Log in后，再打开敲敲云显示ofyes原来账号of应用
     * @param tenantId
     */
    public SysThirdAppConfig getCorpIdClientId(Integer tenantId) {
        LambdaQueryWrapper<SysThirdAppConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysThirdAppConfig::getThirdType, THIRD_TYPE);
        queryWrapper.eq(SysThirdAppConfig::getTenantId, tenantId);
        queryWrapper.select(SysThirdAppConfig::getCorpId,SysThirdAppConfig::getClientId);
        return configMapper.selectOne(queryWrapper);
    }
    //=================================== end 新版DingTalkLog in ============================================
}