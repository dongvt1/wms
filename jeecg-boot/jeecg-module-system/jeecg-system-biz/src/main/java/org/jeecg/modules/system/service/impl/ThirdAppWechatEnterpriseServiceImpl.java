package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jeecg.qywx.api.base.JwAccessTokenAPI;
import com.jeecg.qywx.api.core.common.AccessToken;
import com.jeecg.qywx.api.department.JwDepartmentAPI;
import com.jeecg.qywx.api.department.vo.DepartMsgResponse;
import com.jeecg.qywx.api.department.vo.Department;
import com.jeecg.qywx.api.message.JwMessageAPI;
import com.jeecg.qywx.api.message.vo.*;
import com.jeecg.qywx.api.user.JwUserAPI;
import com.jeecg.qywx.api.user.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.RestUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.thirdapp.JwDepartmentTreeVo;
import org.jeecg.modules.system.vo.thirdapp.JwSysUserDepartVo;
import org.jeecg.modules.system.vo.thirdapp.JwUserDepartVo;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * third partyAppdocking：Enterprise WeChat implementation class
 * @author: jeecg-boot
 */
@Slf4j
@Service
public class ThirdAppWechatEnterpriseServiceImpl implements IThirdAppService {

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
    private SysTenantMapper sysTenantMapper;
    @Autowired
    private SysUserTenantMapper sysUserTenantMapper;
    @Autowired
    private SysThirdAccountMapper sysThirdAccountMapper;
    @Autowired
    private SysTenantMapper tenantMapper;
    
    
    /**
     * errcode
     */
    private static final String ERR_CODE = "errcode";

    /**
     * third partyAPPtype，Currently fixed to wechat_enterprise
     */
    public final String THIRD_TYPE = "wechat_enterprise";

    @Override
    public String getAccessToken() {
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        SysThirdAppConfig config = this.getWeChatThirdAppConfig();
        String corpId = config.getClientId();
        String secret = config.getClientSecret();
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        AccessToken accessToken = JwAccessTokenAPI.getAccessToken(corpId, secret);
        if (accessToken != null) {
            return accessToken.getAccesstoken();
        }
        log.warn("GetAccessTokenfail");
        return null;
    }

    /** GetAPPToken，The secret key of the new version of Enterprise WeChat is separate */
    public String getAppAccessToken(SysThirdAppConfig config) {
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        String corpId = config.getClientId();
        // If not configuredAPPsecret key，It means it is an old company，可以通用secret key
        String secret = config.getClientSecret();
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------

        AccessToken accessToken = JwAccessTokenAPI.getAccessToken(corpId, secret);
        if (accessToken != null) {
            return accessToken.getAccesstoken();
        }
        log.warn("GetAccessTokenfail");
        return null;
    }

    @Override
    public SyncInfoVo syncLocalDepartmentToThirdApp(String ids) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessTokenGetfail！");
            return syncInfo;
        }
        // GetEnterprise WeChat所有ofdepartment
        List<Department> departments = JwDepartmentAPI.getAllDepartment(accessToken);
        if (departments == null) {
            syncInfo.addFailInfo("GetEnterprise WeChat所有departmentfail！");
            return syncInfo;
        }
        // Delete departments that are available on Enterprise WeChat but not locally（Mainly based on local department data）(Thought that Enterprise WeChat could not create a department with the same name，So I can only delete it first）
        List<JwDepartmentTreeVo> departmentTreeList = JwDepartmentTreeVo.listToTree(departments);
        this.deleteDepartRecursion(departmentTreeList, accessToken, true);
        // Get本地所有department树结构
        List<SysDepartTreeModel> sysDepartsTree = sysDepartService.queryTreeList();
        // -- Enterprise WeChat cannot create new top-level departments，So the new top division'sparentIdjust for1
        Department parent = new Department();
        parent.setId("1");
        // recursive sync department
        departments = JwDepartmentAPI.getAllDepartment(accessToken);
        this.syncDepartmentRecursion(sysDepartsTree, departments, parent, accessToken);
        return syncInfo;
    }

    /**
     * Recursively delete departments and subdepartments，Since Enterprise WeChat does not allow deletion of departments with members and sub-departments，So we need to recursively delete the next sub-department.，Then download the department members to the root department on their mobile terminals.
     * @param children
     * @param accessToken
     * @param ifLocal
     */
    private void deleteDepartRecursion(List<JwDepartmentTreeVo> children, String accessToken, boolean ifLocal) {
        for (JwDepartmentTreeVo departmentTree : children) {
            String depId = departmentTree.getId();
            // filter root section
            if (!"1".equals(depId)) {
                // Determine whether the department exists locally
                if (ifLocal) {
                    LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(SysDepart::getQywxIdentifier, depId);
                    SysDepart sysDepart = sysDepartService.getOne(queryWrapper);
                    // This department is available locally，Do not delete
                    if (sysDepart != null) {
                        if (departmentTree.hasChildren()) {
                            this.deleteDepartRecursion(departmentTree.getChildren(), accessToken, true);
                        }
                        continue;
                    }
                }
                // Determine whether there are members，If possible, move to the root department
                List<User> departUserList = JwUserAPI.getUsersByDepartid(depId, "1", null, accessToken);
                if (departUserList != null && departUserList.size() > 0) {
                    for (User user : departUserList) {
                        User updateUser = new User();
                        updateUser.setUserid(user.getUserid());
                        updateUser.setDepartment(new Integer[]{1});
                        JwUserAPI.updateUser(updateUser, accessToken);
                    }
                }
                // If there are sub-departments, delete them first.
                if (departmentTree.hasChildren()) {
                    this.deleteDepartRecursion(departmentTree.getChildren(), accessToken, false);
                }
                // Perform delete operation
                JwDepartmentAPI.deleteDepart(depId, accessToken);
            }
        }
    }

    /**
     * recursive sync department到third partyAPP
     * @param sysDepartsTree
     * @param departments
     * @param parent
     * @param accessToken
     */
    private void syncDepartmentRecursion(List<SysDepartTreeModel> sysDepartsTree, List<Department> departments, Department parent, String accessToken) {
        if (sysDepartsTree != null && sysDepartsTree.size() != 0) {
            for1:
            for (SysDepartTreeModel depart : sysDepartsTree) {
                for (Department department : departments) {
                    // idsame，Represents existing，Perform modification operations
                    if (department.getId().equals(depart.getQywxIdentifier())) {
                        this.sysDepartToQwDepartment(depart, department, parent.getId());
                        JwDepartmentAPI.updateDepart(department, accessToken);
                        // followed by synchronized children
                        this.syncDepartmentRecursion(depart.getChildren(), departments, department, accessToken);
                        // Break out of outer loop
                        continue for1;
                    }
                }
                // Looping to this point indicates that it is a new department，Direct interface creation
                Department newDepartment = this.sysDepartToQwDepartment(depart, parent.getId());
                DepartMsgResponse response = JwDepartmentAPI.createDepartment(newDepartment, accessToken);
                // Created successfully，will be returnedidbind to local
                if (response != null && response.getId() != null) {
                    SysDepart sysDepart = new SysDepart();
                    sysDepart.setId(depart.getId());
                    sysDepart.setQywxIdentifier(response.getId().toString());
                    sysDepartService.updateById(sysDepart);
                    Department newParent = new Department();
                    newParent.setId(response.getId().toString());
                    // followed by synchronized children
                    this.syncDepartmentRecursion(depart.getChildren(), departments, newParent, accessToken);
                }
                // Collect error information
//                this.syncUserCollectErrInfo(errCode, sysUser, errInfo);
            }
        }
    }

    public SyncInfoVo syncThirdAppDepartmentToLocal(Integer tenantId, Map<String,String> map) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessTokenGetfail！");
            return syncInfo;
        }
        // GetEnterprise WeChat所有ofdepartment
        List<Department> departments = JwDepartmentAPI.getAllDepartment(accessToken);
        if (departments == null) {
            syncInfo.addFailInfo("Enterprise WeChatdepartment信息Getfail！");
            return syncInfo;
        }
        String username = JwtUtil.getUserNameByToken(SpringContextUtils.getHttpServletRequest());
        // Willlistconvert totree
        List<JwDepartmentTreeVo> departmentTreeList = JwDepartmentTreeVo.listToTree(departments);
        // recursive sync department
        this.syncDepartmentToLocalRecursion(departmentTreeList, null, username, syncInfo, tenantId, map);
        return syncInfo;
    }

    /**
     * recursive sync department到本地
     */
    private void syncDepartmentToLocalRecursion(List<JwDepartmentTreeVo> departmentTreeList, String sysParentId, String username, SyncInfoVo syncInfo,Integer tenantId, Map<String,String> map) {
        if (departmentTreeList != null && departmentTreeList.size() != 0) {
            for (JwDepartmentTreeVo departmentTree : departmentTreeList) {
                String depId = departmentTree.getId();
                LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
                // according to qywxIdentifier Fields and TenantsidQuery，tenantidDefault is0
                queryWrapper.eq(SysDepart::getQywxIdentifier, depId);
                queryWrapper.eq(SysDepart::getTenantId, tenantId);
                SysDepart sysDepart = sysDepartService.getOne(queryWrapper);
                if (sysDepart != null) {
                    //  Perform update operation
                    SysDepart updateSysDepart = this.qwDepartmentToSysDepart(departmentTree, sysDepart);
                    //update-begin---author:wangshuai---date:2024-04-10---for:【issues/6017】When enterprise WeChat synchronizes departments, there is no top-level department name.，When synchronizing users，The user has no department information---
                    if (sysParentId != null && !"0".equals(sysParentId)) {
                    //update-end---author:wangshuai---date:2024-04-10---for:【issues/6017】When enterprise WeChat synchronizes departments, there is no top-level department name.，When synchronizing users，The user has no department information---
                        updateSysDepart.setParentId(sysParentId);
                    }
                    try {
                        sysDepartService.updateDepartDataById(updateSysDepart, username);
                        String str = String.format("department %s Update successful！", updateSysDepart.getDepartName());
                        syncInfo.addSuccessInfo(str);
                        map.put(depId,updateSysDepart.getId());
                    } catch (Exception e) {
                        this.syncDepartCollectErrInfo(e, departmentTree, syncInfo);
                    }
                    if (departmentTree.hasChildren()) {
                        // followed by synchronized children
                        this.syncDepartmentToLocalRecursion(departmentTree.getChildren(), updateSysDepart.getId(), username, syncInfo, tenantId, map);
                    }
                } else {
                    // Perform new operations
                    SysDepart newSysDepart = this.qwDepartmentToSysDepart(departmentTree, null);
                    if (sysParentId != null && !"0".equals(sysParentId)) {
                        newSysDepart.setParentId(sysParentId);
                        // 2 = Organizational structure
                        newSysDepart.setOrgCategory("2");
                    } else {
                        // 1 = company
                        newSysDepart.setOrgCategory("1");
                    }
                    newSysDepart.setTenantId(tenantId);
                    try {
                        sysDepartService.saveDepartData(newSysDepart, username);
                        String str = String.format("department %s Created successfully！", newSysDepart.getDepartName());
                        syncInfo.addSuccessInfo(str);
                        map.put(depId,newSysDepart.getId());
                    } catch (Exception e) {
                        this.syncDepartCollectErrInfo(e, departmentTree, syncInfo);
                    }
                    // followed by synchronized children
                    if (departmentTree.hasChildren()) {
                        this.syncDepartmentToLocalRecursion(departmentTree.getChildren(), newSysDepart.getId(), username, syncInfo, tenantId, map);
                    }
                }
            }
        }
    }

    @Override
    public SyncInfoVo syncLocalUserToThirdApp(String ids) {
        SyncInfoVo syncInfo = new SyncInfoVo();
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            syncInfo.addFailInfo("accessTokenGetfail！");
            return syncInfo;
        }
        // GetEnterprise WeChat所有ofuser
//        List<User> qwUsers = JwUserAPI.getDetailUsersByDepartid("1", null, null, accessToken);
        // GetEnterprise WeChat所有ofuser（只能Getuserid）
        List<User> qwUsers = JwUserAPI.getUserIdList(accessToken);

        if (qwUsers == null) {
            syncInfo.addFailInfo("Enterprise WeChatuser列表Queryfail！");
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

        // Loop to determine new users and users who need to be updated
        for1:
        for (SysUser sysUser : sysUsers) {
            // External simulated login temporary account，Out of sync
            if ("_reserve_user_external".equals(sysUser.getUsername())) {
                continue;
            }
            /*
             * Logic to determine whether it has been synchronized：
             * 1. Query sys_third_account（third party账号表）Is there data，If a representative is synced
             * 2. There is no local table，Just use your mobile phone number to determine，Do not reuseusernamejudge。
             */
            User qwUser;
            SysThirdAccount sysThirdAccount = sysThirdAccountService.getOneBySysUserId(sysUser.getId(), THIRD_TYPE);
            for (User qwUserTemp : qwUsers) {
                if (sysThirdAccount == null || oConvertUtils.isEmpty(sysThirdAccount.getThirdUserId()) || !sysThirdAccount.getThirdUserId().equals(qwUserTemp.getUserid())) {
                    // sys_third_account 表matchfail，Try matching with mobile phone number
                    // The new version of Enterprise WeChat has been adjustedAPI，Now you can only passuserid来judge是否同步过了
//                    String phone = sysUser.getPhone();
//                    if (!(oConvertUtils.isEmpty(phone) || phone.equals(qwUserTemp.getMobile()))) {
                        // 手机号matchfail，try againusernamematch
                        String username = sysUser.getUsername();
                        if (!(oConvertUtils.isEmpty(username) || username.equals(qwUserTemp.getUserid()))) {
                            // username matchfail，Jump directly to the next cycle to continue
                            continue;
                        }
//                    }
                }
                // 循环到此说明usermatch成功，Perform update operation
                qwUser = this.sysUserToQwUser(sysUser, qwUserTemp);
                int errCode = JwUserAPI.updateUser(qwUser, accessToken);
                // Collect error information
                this.syncUserCollectErrInfo(errCode, sysUser, syncInfo);
                this.thirdAccountSaveOrUpdate(sysThirdAccount, sysUser.getId(), qwUser.getUserid(),qwUser.getName(), null);
                // Update completed，Jump directly to the next outer loop to continue
                continue for1;
            }
            // If you loop to this point, you are a new user.，Direct interface creation
            qwUser = this.sysUserToQwUser(sysUser);
            int errCode = JwUserAPI.createUser(qwUser, accessToken);
            // Collect error information
            boolean apiSuccess = this.syncUserCollectErrInfo(errCode, sysUser, syncInfo);
            if (apiSuccess) {
                this.thirdAccountSaveOrUpdate(sysThirdAccount, sysUser.getId(), qwUser.getUserid(),qwUser.getName(), null);
            }
        }
        return syncInfo;
    }

//    @Override
//    public SyncInfoVo syncThirdAppUserToLocal() {
//        SyncInfoVo syncInfo = new SyncInfoVo();
//        String accessToken = this.getAccessToken();
//        if (accessToken == null) {
//            syncInfo.addFailInfo("accessTokenGetfail！");
//            return syncInfo;
//        }
//        // GetEnterprise WeChat所有ofuser
//        List<User> qwUsersList = JwUserAPI.getDetailUsersByDepartid("1", null, null, accessToken);
//        if (qwUsersList == null) {
//            syncInfo.addFailInfo("Enterprise WeChatuser列表Queryfail！");
//            return syncInfo;
//        }
//        //Query本地user
//        List<SysUser> sysUsersList = userMapper.selectList(Wrappers.emptyWrapper());
//        // Loop to determine new users and users who need to be updated
//        for (User qwUser : qwUsersList) {
//            /*
//             * Logic to determine whether it has been synchronized：
//             * 1. Query sys_third_account（third party账号表）Is there data，If a representative is synced
//             * 2. There is no local table，Just use your mobile phone number to determine，Do not reuseusernamejudge。
//             */
//            SysThirdAccount sysThirdAccount = sysThirdAccountService.getOneByThirdUserId(qwUser.getUserid(), THIRD_TYPE);
//            List<SysUser> collect = sysUsersList.stream().filter(user -> (qwUser.getMobile().equals(user.getPhone()) || qwUser.getUserid().equals(user.getUsername()))
//                                                                ).collect(Collectors.toList());
//
//            if (collect != null && collect.size() > 0) {
//                SysUser sysUserTemp = collect.get(0);
//                // 循环到此说明usermatch成功，Perform update operation
//                SysUser updateSysUser = this.qwUserToSysUser(qwUser, sysUserTemp);
//                try {
//                    userMapper.updateById(updateSysUser);
//                    String str = String.format("user %s(%s) Update successful！", updateSysUser.getRealname(), updateSysUser.getUsername());
//                    syncInfo.addSuccessInfo(str);
//                } catch (Exception e) {
//                    this.syncUserCollectErrInfo(e, qwUser, syncInfo);
//                }
//
//                this.thirdAccountSaveOrUpdate(sysThirdAccount, updateSysUser.getId(), qwUser.getUserid());
//                // Update completed，Jump directly to the next outer loop to continue
//            }else{
//                // 没match到user则走新增逻辑
//                SysUser newSysUser = this.qwUserToSysUser(qwUser);
//                try {
//                    userMapper.insert(newSysUser);
//                    String str = String.format("user %s(%s) Created successfully！", newSysUser.getRealname(), newSysUser.getUsername());
//                    syncInfo.addSuccessInfo(str);
//                } catch (Exception e) {
//                    this.syncUserCollectErrInfo(e, qwUser, syncInfo);
//                }
//                this.thirdAccountSaveOrUpdate(sysThirdAccount, newSysUser.getId(), qwUser.getUserid());
//            }
//        }
//        return syncInfo;
//    }

    /**
     * 保存或修改third partyLog in表
     *
     * @param sysThirdAccount third party账户表object，fornullJust add data，Otherwise, modify
     * @param sysUserId       本地系统userID
     * @param qwUserId        Enterprise WeChatuserID
     * @param wechatRealName  Enterprise WeChatuser真实姓名
     */
    private void thirdAccountSaveOrUpdate(SysThirdAccount sysThirdAccount, String sysUserId, String qwUserId, String wechatRealName, Integer tenantId) {
        if (sysThirdAccount == null) {
            sysThirdAccount = new SysThirdAccount();
            sysThirdAccount.setSysUserId(sysUserId);
            sysThirdAccount.setStatus(1);
            sysThirdAccount.setDelFlag(0);
            sysThirdAccount.setThirdType(THIRD_TYPE);
            if(oConvertUtils.isNotEmpty(tenantId)){
                sysThirdAccount.setTenantId(tenantId);
            }
        }
        sysThirdAccount.setThirdUserId(qwUserId);
        sysThirdAccount.setThirdUserUuid(qwUserId);
        sysThirdAccount.setRealname(wechatRealName);
        sysThirdAccountService.saveOrUpdate(sysThirdAccount);
    }

    /**
     * 【同步user】Collect error information during synchronization
     */
    private boolean syncUserCollectErrInfo(int errCode, SysUser sysUser, SyncInfoVo syncInfo) {
        if (errCode != 0) {
            String msg = "";
            // https://open.work.weixin.qq.com/api/doc/90000/90139/90313
            switch (errCode) {
                case 40003:
                    msg = "InvalidUserID";
                    break;
                case 60129:
                    msg = "手机和邮箱不能都fornull";
                    break;
                case 60102:
                    msg = "UserIDAlready exists";
                    break;
                case 60103:
                    msg = "Mobile phone number is illegal";
                    break;
                case 60104:
                    msg = "手机号码Already exists";
                    break;
                default:
            }
            String str = String.format("user %s(%s) 同步fail！error code：%s——%s", sysUser.getUsername(), sysUser.getRealname(), errCode, msg);
            syncInfo.addFailInfo(str);
            return false;
        } else {
            String str = String.format("user %s(%s) Synchronization successful！", sysUser.getUsername(), sysUser.getRealname());
            syncInfo.addSuccessInfo(str);
            return true;
        }
    }

    private boolean syncUserCollectErrInfo(Exception e, User qwUser, SyncInfoVo syncInfo) {
        String msg;
        if (e instanceof DuplicateKeyException) {
            msg = e.getCause().getMessage();
        } else {
            msg = e.getMessage();
        }
        String str = String.format("user %s(%s) 同步fail！error message：%s", qwUser.getUserid(), qwUser.getName(), msg);
        syncInfo.addFailInfo(str);
        return false;
    }

    private boolean syncDepartCollectErrInfo(Exception e, Department department, SyncInfoVo syncInfo) {
        String msg;
        if (e instanceof DuplicateKeyException) {
            msg = e.getCause().getMessage();
        } else {
            msg = e.getMessage();
        }
        String str = String.format("department %s(%s) 同步fail！error message：%s", department.getName(), department.getId(), msg);
        syncInfo.addFailInfo(str);
        return false;
    }

    /**
     * 【同步user】WillSysUserconvert toEnterprise WeChatofUserobject（创建新user）
     */
    private User sysUserToQwUser(SysUser sysUser) {
        User user = new User();
        // pass username to relate
        user.setUserid(sysUser.getUsername());
        return this.sysUserToQwUser(sysUser, user);
    }

    /**
     * 【同步user】WillSysUserconvert toEnterprise WeChatofUserobject（更新旧user）
     */
    private User sysUserToQwUser(SysUser sysUser, User user) {
        user.setName(sysUser.getRealname());
        user.setMobile(sysUser.getPhone());
        // Query并同步userdepartment关系
        List<SysDepart> departList = this.getUserDepart(sysUser);
        if (departList != null) {
            List<Integer> departmentIdList = new ArrayList<>();
            // Enterprise WeChat 1表示for上级，0Indicates non-superior
            List<Integer> isLeaderInDept = new ArrayList<>();
            // 当前user管理ofdepartment
            List<String> manageDepartIdList = new ArrayList<>();
            if (oConvertUtils.isNotEmpty(sysUser.getDepartIds())) {
                manageDepartIdList = Arrays.asList(sysUser.getDepartIds().split(","));
            }
            for (SysDepart sysDepart : departList) {
                // Enterprise WeChatofdepartmentid
                if (oConvertUtils.isNotEmpty(sysDepart.getQywxIdentifier())) {
                    try {
                        departmentIdList.add(Integer.parseInt(sysDepart.getQywxIdentifier()));
                    } catch (NumberFormatException ignored) {
                        continue;
                    }
                    // judgeuser身份，是否for上级
                    if (CommonConstant.USER_IDENTITY_2.equals(sysUser.getUserIdentity())) {
                        // judge当前department是否for该user管理ofdepartment
                        isLeaderInDept.add(manageDepartIdList.contains(sysDepart.getId()) ? 1 : 0);
                    } else {
                        isLeaderInDept.add(0);
                    }
                }
            }
            user.setDepartment(departmentIdList.toArray(new Integer[]{}));
            // The number must be the same as the parameterdepartmentThe number of，表示在所在ofdepartment内是否for上级。1表示for上级，0Indicates non-superior。It can be used to identify superior approvers in applications such as approval.
            user.setIs_leader_in_dept(isLeaderInDept.toArray(new Integer[]{}));
        }
        if (user.getDepartment() == null || user.getDepartment().length == 0) {
            // 没有找到matchdepartment，同步到根department下
            user.setDepartment(new Integer[]{1});
            user.setIs_leader_in_dept(new Integer[]{0});
        }
        // job translation
        //update-begin---author:wangshuai ---date:20230220  for：[QQYUN-3980]Under organization management Position function 职位表加tenantid Add position-user关联表------------
        List<SysPosition> positionList = sysPositionService.getPositionList(sysUser.getId());
        if(null != positionList && positionList.size()>0){
            String positionName = positionList.stream().map(SysPosition::getName).collect(Collectors.joining(SymbolConstant.COMMA));
            user.setPosition(positionName);
        }
        //update-end---author:wangshuai ---date:20230220  for：[QQYUN-3980]Under organization management Position function 职位表加tenantid Add position-user关联表------------
        if (sysUser.getSex() != null) {
            user.setGender(sysUser.getSex().toString());
        }
        user.setEmail(sysUser.getEmail());
        // enable/Disable member（state），rules are different，Need to convert
        // Enterprise WeChatrule：1表示enable成员，0表示Disable member
        // JEECGrule：1normal，2freeze
        if (sysUser.getStatus() != null) {
            if (CommonConstant.USER_UNFREEZE.equals(sysUser.getStatus()) || CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
                user.setEnable(sysUser.getStatus() == 1 ? 1 : 0);
            } else {
                user.setEnable(1);
            }
        }
        // Landline number
        user.setTelephone(sysUser.getTelephone());
        // --- Enterprise WeChat没有逻辑删除of功能
        // update-begin--Author:sunjianlei Date:20210520 for：本地逻辑删除ofuser，在Enterprise WeChat里禁用 -----
        if (CommonConstant.DEL_FLAG_1.equals(sysUser.getDelFlag())) {
            user.setEnable(0);
        }
        // update-end--Author:sunjianlei Date:20210520 for：本地逻辑删除ofuser，在Enterprise WeChat里freeze -----

        return user;
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
     * 【同步user】WillEnterprise WeChatofUserobjectconvert toSysUser（创建新user）
     */
    private SysUser qwUserToSysUser(User user) {
        SysUser sysUser = new SysUser();
        sysUser.setDelFlag(0);
        sysUser.setStatus(1);
        // pass username to relate
        sysUser.setUsername(user.getUserid());
        // 密码Default is “123456”，Randomly add salt
        String password = "123456", salt = oConvertUtils.randomGen(8);
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
        sysUser.setSalt(salt);
        sysUser.setPassword(passwordEncode);
        return this.qwUserToSysUser(user, sysUser);
    }

    /**
     * 【同步user】WillEnterprise WeChatofUserobjectconvert toSysUser（更新旧user）
     */
    private SysUser qwUserToSysUser(User qwUser, SysUser oldSysUser) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(oldSysUser, sysUser);
        sysUser.setRealname(qwUser.getName());
        sysUser.setPost(qwUser.getPosition());
        // Set employee number，由于Enterprise WeChat没有工号of概念，So you can only use userId replace
        if (oConvertUtils.isEmpty(sysUser.getWorkNo())) {
            sysUser.setWorkNo(qwUser.getUserid());
        }
        try {
            sysUser.setSex(Integer.parseInt(qwUser.getGender()));
        } catch (NumberFormatException ignored) {
        }
        // 因for唯一键约束of原因，如果原数据和旧数据same，Will not update
        if (oConvertUtils.isNotEmpty(qwUser.getEmail()) && !qwUser.getEmail().equals(sysUser.getEmail())) {
            sysUser.setEmail(qwUser.getEmail());
        } else {
            sysUser.setEmail(null);
        }
        // 因for唯一键约束of原因，如果原数据和旧数据same，Will not update
        if (oConvertUtils.isNotEmpty(qwUser.getMobile()) && !qwUser.getMobile().equals(sysUser.getPhone())) {
            sysUser.setPhone(qwUser.getMobile());
        } else {
            sysUser.setPhone(null);
        }

        // enable/Disable member（state），rules are different，Need to convert
        // Enterprise WeChatrule：1表示enable成员，0表示Disable member
        // JEECGrule：1normal，2freeze
        if (qwUser.getEnable() != null) {
            sysUser.setStatus(qwUser.getEnable() == 1 ? 1 : 2);
        }
        // Landline number
        sysUser.setTelephone(qwUser.getTelephone());

        // --- Enterprise WeChat没有逻辑删除of功能
        // sysUser.setDelFlag()
        return sysUser;
    }

    /**
     * 【同步department】WillSysDepartTreeModelconvert toEnterprise WeChatofDepartmentobject（创建新department）
     */
    private Department sysDepartToQwDepartment(SysDepartTreeModel departTree, String parentId) {
        Department department = new Department();
        return this.sysDepartToQwDepartment(departTree, department, parentId);
    }

    /**
     * 【同步department】WillSysDepartTreeModelconvert toEnterprise WeChatofDepartmentobject
     */
    private Department sysDepartToQwDepartment(SysDepartTreeModel departTree, Department department, String parentId) {
        department.setName(departTree.getDepartName());
        department.setParentid(parentId);
        if (departTree.getDepartOrder() != null) {
            department.setOrder(departTree.getDepartOrder().toString());
        }
        return department;
    }


    /**
     * 【同步department】WillEnterprise WeChatofDepartmentobjectconvert toSysDepart
     */
    private SysDepart qwDepartmentToSysDepart(Department department, SysDepart oldSysDepart) {
        SysDepart sysDepart = new SysDepart();
        if (oldSysDepart != null) {
            BeanUtils.copyProperties(oldSysDepart, sysDepart);
        }
        sysDepart.setQywxIdentifier(department.getId());
        sysDepart.setDepartName(department.getName());
        try {
            sysDepart.setDepartOrder(Integer.parseInt(department.getOrder()));
        } catch (NumberFormatException ignored) {
        }
        return sysDepart;
    }

    @Override
    public int removeThirdAppUser(List<String> userIdList) {
        // judgeenablestate
        SysThirdAppConfig config = this.getWeChatThirdAppConfig();
        if (null == config) {
            return -1;
        }
        int count = 0;
        if (userIdList != null && userIdList.size() > 0) {
            String accessToken = this.getAccessToken();
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
                    // There is no interface for batch deletion
                    int err = JwUserAPI.deleteUser(thirdUserId, accessToken);
                    if (err == 0) {
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

    @Override
    public boolean sendMessage(MessageDTO message, boolean verifyConfig) {
        JSONObject response;
        if (message.isMarkdown()) {
            response = this.sendMarkdownResponse(message, verifyConfig);
        } else {
            response = this.sendMessageResponse(message, verifyConfig);
        }
        if (response != null) {
            return response.getIntValue("errcode") == 0;
        }
        return false;
    }

    public JSONObject sendMessageResponse(MessageDTO message, boolean verifyConfig) {
        SysThirdAppConfig config = this.getWeChatThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getAppAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        Text text = new Text();
        text.setMsgtype("text");
        text.setTouser(this.getTouser(message.getToUser(), message.getToAll()));
        TextEntity entity = new TextEntity();
        entity.setContent(message.getContent());
        text.setText(entity);
        text.setAgentid(Integer.parseInt(config.getAgentId()));
        return JwMessageAPI.sendTextMessage(text, accessToken);
    }

    public JSONObject sendMarkdownResponse(MessageDTO message, boolean verifyConfig) {
        SysThirdAppConfig config = this.getWeChatThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getAppAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        Markdown markdown = new Markdown();
        markdown.setTouser(this.getTouser(message.getToUser(), message.getToAll()));
        MarkdownEntity entity = new MarkdownEntity();
        entity.setContent(message.getContent());
        markdown.setMarkdown(entity);
        markdown.setAgentid(Integer.parseInt(config.getAgentId()));
        return JwMessageAPI.sendMarkdownMessage(markdown, accessToken);
    }

    /**
     * Send text card message（SysAnnouncementcustom made）
     *
     * @param announcement
     * @param verifyConfig Whether to verify the configuration（未enableofAPPWill refuse to send）
     * @return
     */
    public JSONObject sendTextCardMessage(SysAnnouncement announcement,String mobileOpenUrl, boolean verifyConfig) {
        SysThirdAppConfig config = this.getWeChatThirdAppConfig();
        if (verifyConfig && null == config) {
            return null;
        }
        String accessToken = this.getAppAccessToken(config);
        if (accessToken == null) {
            return null;
        }
        TextCard textCard = new TextCard();
        textCard.setAgentid(Integer.parseInt(config.getAgentId()));
        boolean isToAll = CommonConstant.MSG_TYPE_ALL.equals(announcement.getMsgType());
        String usernameString = "";
        if (!isToAll) {
            // WilluserIdconvert tousername
            String userId = announcement.getUserIds();
            String[] userIds = null;
            if(oConvertUtils.isNotEmpty(userId)){
                userIds = userId.substring(0, (userId.length() - 1)).split(",");
            }else{
                LambdaQueryWrapper<SysAnnouncementSend> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(SysAnnouncementSend::getAnntId, announcement.getId());
                SysAnnouncementSend sysAnnouncementSend = sysAnnouncementSendMapper.selectOne(queryWrapper);
                userIds = new String[] {sysAnnouncementSend.getUserId()};
            }

            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SysUser::getId, userIds);
            List<SysUser> userList = userMapper.selectList(queryWrapper);
            List<String> usernameList = userList.stream().map(SysUser::getUsername).collect(Collectors.toList());
            usernameString = String.join(",", usernameList);
        }

        textCard.setTouser(this.getTouser(usernameString, isToAll));
        TextCardEntity entity = new TextCardEntity();
        entity.setTitle(announcement.getTitile());
        
        //update-begin---author:scott ---date:2025-08-05  for：【QQYUN-13257】【h5】urge、Cc message，在Enterprise WeChat中显示jsonGarbled characters---
        // judgeannouncement.getMsgAbstract()value isjsonFormat
        if(oConvertUtils.isJson(announcement.getMsgAbstract()) && oConvertUtils.isNotEmpty(mobileOpenUrl)){
            entity.setDescription(announcement.getMsgContent());
            entity.setUrl(mobileOpenUrl);
        }else{
            entity.setDescription(oConvertUtils.getString(announcement.getMsgAbstract(),"null"));
            entity.setUrl(geQywxtAnnouncementUrl(announcement));
        }
        //update-end---author:scott ---date::2025-08-05  for：【QQYUN-13257】【h5】urge、Cc message，在Enterprise WeChat中显示jsonGarbled characters---
        
        textCard.setTextcard(entity);
        return JwMessageAPI.sendTextCardMessage(textCard, accessToken);
    }

    
    /**
     * GetEnterprise WeChatof公告链接
     *
     * @return
     */
    private String geQywxtAnnouncementUrl(SysAnnouncement announcement){
        String baseUrl = null;
        //优先pass请求Getbasepath，Get不到读取 jeecg.domainUrl.pc
        try {
            baseUrl = RestUtil.getBaseUrl();
        } catch (Exception e) {
            log.warn(e.getMessage());
            baseUrl =  jeecgBaseConfig.getDomainUrl().getPc();
            //e.printStackTrace();
        }
       return baseUrl + "/sys/annountCement/show/" + announcement.getId();
    }

    private String getTouser(String origin, boolean toAll) {
        if (toAll) {
            return "@all";
        } else {
            String[] toUsers = origin.split(",");
            // passthird party账号表Query出third partyuserId
            int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), CommonConstant.TENANT_ID_DEFAULT_VALUE);
            List<SysThirdAccount> thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(toUsers, THIRD_TYPE,tenantId);
            List<String> toUserList = thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
            // For multiple recipients‘|’separate
            return String.join("|", toUserList);
        }
    }

    /**
     * according tothird partyLog inGet到ofcode来Getthird partyappofuserID
     *
     * @param code
     * @return
     */
    public Map<String,String> getUserIdByThirdCode(String code, String accessToken) {
        JSONObject response = JwUserAPI.getUserInfoByCode(code, accessToken);
        if (response != null) {
            Map<String,String> map = new HashMap<>(5);
            log.info("response: " + response.toJSONString());
            if (response.getIntValue(ERR_CODE) == 0) {
                //WilluserTicketalso return，用于Get手机号
                String userTicket = response.getString("user_ticket");
                String appUserId = response.getString("UserId");
                map.put("userTicket",userTicket);
                map.put("appUserId",appUserId);
                return map;
            }
        }
        return null;
    }

    /**
     * OAuth2Log in，成功返回Log inofSysUser，fail返回null
     */
    public SysUser oauth2Login(String code,Integer tenantId) {
        Long count = tenantMapper.tenantIzExist(tenantId);
        if(ObjectUtil.isEmpty(count) || 0 == count){
            throw new JeecgBootException("tenantdoes not exist！");
        }
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        SysThirdAppConfig config = configMapper.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
        String accessToken = this.getAppAccessToken(config);
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]Create a new corporate WeChat and DingTalk configuration table，Isolation via tenant mode------------
        if (accessToken == null) {
            return null;
        }
        Map<String,String> map = this.getUserIdByThirdCode(code, accessToken);
        if (null != map) {
            //Enterprise WeChat需要passuserTicketGetuser信息
            String appUserId = map.get("appUserId");
            String userTicket = map.get("userTicket");
            // judgethird partyuser表有没有这个人
            LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysThirdAccount::getThirdUserId, appUserId);
            queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            queryWrapper.eq(SysThirdAccount::getTenantId, tenantId);
            SysThirdAccount thirdAccount = sysThirdAccountService.getOne(queryWrapper);
            if (thirdAccount != null) {
                return this.getSysUserByThird(thirdAccount, null, appUserId, accessToken, userTicket,tenantId);
            } else {
                throw new JeecgBootException("该user尚未同步，请同步后再次Log in！");
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
     * @param userTicket Get访问user敏感信息
     * @return
     */
    private SysUser getSysUserByThird(SysThirdAccount thirdAccount, User appUser, String appUserId, String accessToken, String userTicket,Integer tenantId) {
        String sysUserId = thirdAccount.getSysUserId();
        if (oConvertUtils.isNotEmpty(sysUserId)) {
            return userMapper.selectById(sysUserId);
        } else {
            // if not sysUserId ，It means there is no account bound，Get到手机号之后进行绑定
            if (appUser == null) {
                appUser = this.getUserByUserTicket(userTicket, accessToken);
            }
            // judge系统里是否有这个手机号ofuser
            SysUser sysUser = userMapper.getUserByPhone(appUser.getMobile());
            if (sysUser != null) {
                thirdAccount.setAvatar(appUser.getAvatar());
                thirdAccount.setRealname(appUser.getName());
                thirdAccount.setThirdUserId(appUser.getUserid());
                thirdAccount.setThirdUserUuid(appUser.getUserid());
                thirdAccount.setSysUserId(sysUser.getId());
                sysThirdAccountService.updateById(thirdAccount);
                return sysUser;
            } else {
                // If not, just create the logic
                return sysThirdAccountService.createUser(appUser.getMobile(), appUser.getUserid(),tenantId);
            }

        }
    }

    /**
     * according totype和tenantidGetEnterprise WeChat配置
     * @return
     */
    private SysThirdAppConfig getWeChatThirdAppConfig(){
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        return configMapper.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
    }

    /**
     * GetEnterprise WeChatthird partyuser信息
     * @param userTicket
     * @param accessToken
     * @return
     */
    private User getUserByUserTicket(String userTicket, String accessToken){
        Map<String, String> map = new HashMap<>(5);
        map.put("user_ticket",userTicket);
        //Establish connection
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://qyapi.weixin.qq.com/cgi-bin/auth/getuserdetail?access_token="+accessToken);
            RequestConfig requestConfig = RequestConfig.custom()
             .setConnectTimeout(10000).setConnectionRequestTimeout(10000).setSocketTimeout(10000)
             .build();
            httpPost.setConfig(requestConfig);
            httpPost.setEntity(new StringEntity(JSONObject.toJSONString(map), ContentType.create("application/json", "utf-8")));
            httpResponse = httpClient.execute(httpPost);
            // 从响应object中Get响应内容
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            JSONObject jsonObject = JSONObject.parseObject(result);
            Integer errcode = jsonObject.getInteger("errcode");
            if(0 == errcode){
                return JSONObject.toJavaObject(jsonObject, User.class);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * GetEnterprise WeChat绑定ofuser信息
     * @return
     */
    public JwSysUserDepartVo getThirdUserByWechat(Integer tenantId) {
        JwSysUserDepartVo sysUserDepartVo = new JwSysUserDepartVo();
        //step1 Getuserid和departmentid
        String accessToken = this.getAccessToken();
        if (accessToken == null) {
            throw new JeecgBootException("accessTokenGetfail！");
        }
        //Get当前tenant下ofuser
        List<JwUserDepartVo> userList = sysUserTenantMapper.getUsersByTenantIdAndName(tenantId);
        // GetEnterprise WeChat所有ofuser（只能Getuserid）
        List<User> qwUsers = JwUserAPI.getUsersByDepartid("1","1",null,accessToken);
        if(oConvertUtils.isEmpty(qwUsers)){
            throw new JeecgBootException("Enterprise WeChat下没Query到user！");
        }
        List<String> userIds = new ArrayList<>();
        List<JwUserDepartVo> userWechatList = new ArrayList<>();

        for (int i = 0; i < qwUsers.size(); i++) {
            User user = qwUsers.get(i);
            String userId = qwUsers.get(i).getUserid();
            //保证user唯一
            if(!userIds.contains(userId)){
                //step2 Check whether it has been synchronized,Synchronized items will not be processed.
                SysThirdAccount oneBySysUserId = sysThirdAccountService.getOneByUuidAndThirdType(userId, THIRD_TYPE,tenantId, userId);
                if(null != oneBySysUserId){
                    userIds.add(qwUsers.get(i).getUserid());
                    userList = userList.stream().filter(item -> !item.getUserId().equals(oneBySysUserId.getSysUserId())).collect(Collectors.toList());;
                    continue;
                }
                AtomicBoolean excludeUser = new AtomicBoolean(false);
                if(ObjectUtil.isNotEmpty(qwUsers)){
                    //step3 pass名称match敲敲云
                    userList.forEach(item ->{
                        if(item.getRealName().equals(user.getName())){
                            item.setWechatUserId(user.getUserid());
                            item.setWechatRealName(user.getName());
                            if(ObjectUtil.isNotEmpty(user.getDepartment())){
                                item.setWechatDepartId(Arrays.toString(user.getDepartment()));
                            }
                            excludeUser.set(true);
                        }
                    });
                    userIds.add(user.getUserid());
                }
                if(!excludeUser.get()){
                    JwUserDepartVo userDepartVo = new JwUserDepartVo();
                    userDepartVo.setWechatRealName(user.getName());
                    userDepartVo.setWechatUserId(user.getUserid());
                    if(ObjectUtil.isNotEmpty(user.getDepartment())){
                        userDepartVo.setWechatDepartId(Arrays.toString(user.getDepartment()));
                    }
                    userWechatList.add(userDepartVo);
                }
            }
        }
        //step4 返回user信息
        sysUserDepartVo.setUserList(userWechatList);
        sysUserDepartVo.setJwUserDepartVos(userList);
        return sysUserDepartVo;
    }

    /**
     * 同步Enterprise WeChat和department
     * @param jwUserDepartJson
     * @return
     */
    public SyncInfoVo syncWechatEnterpriseDepartAndUserToLocal(String jwUserDepartJson, Integer tenantId) {
        //step 1 同步department
        //存放departmentidofmap
        Map<String,String> idsMap = new HashMap<>();
        SyncInfoVo syncInfoVo = this.syncThirdAppDepartmentToLocal(tenantId, idsMap);
        //step 2 同步user及userdepartment
        this.syncDepartAndUser(syncInfoVo, tenantId, idsMap, jwUserDepartJson);
        //step 3 返回Synchronization successful或者同步failof消息
        return syncInfoVo;
    }

    /**
     * 同步user和department
     *  @param syncInfoVo 存放error messageof日志
     * @param tenantId tenantid
     * @param idsMap departmentidgather keyforEnterprise WeChatofid value for系统departmentofid
     * @param jwUserDepartJson
     */
    private void syncDepartAndUser(SyncInfoVo syncInfoVo, Integer tenantId, Map<String, String> idsMap, String jwUserDepartJson) {
        if (oConvertUtils.isNotEmpty(jwUserDepartJson)) {
            JSONArray jsonArray = JSONObject.parseArray(jwUserDepartJson);
            for (Object object : jsonArray) {
                JSONObject jsonObject = JSONObject.parseObject(object.toString());
                Object userId = jsonObject.get("userId");
                String wechatUserId = jsonObject.getString("wechatUserId");
                String wechatRealName = jsonObject.getString("wechatRealName");
                Object wechatDepartId = jsonObject.get("wechatDepartId");
                String sysUserId = "";
                //step 1 新建或更新user
                //useridfornull说明需要创建user
                if (null == userId) {
                    SysTenant sysTenant = sysTenantMapper.selectById(tenantId);
                    String houseNumber = "";
                    //null说明没有tenant直接用user名
                    if (null != sysTenant) {
                        houseNumber = sysTenant.getHouseNumber();
                    }
                    //user名和密码用门牌号+useridofFormat，避免user名重复
                    String username = houseNumber + wechatUserId;
                    //新建user
                    sysUserId = this.saveUser(username, wechatRealName, syncInfoVo, wechatUserId);
                } else {
                    //according toidQueryuser
                    SysUser sysUser = userMapper.selectById(userId.toString());
                    if (null != sysUser) {
                        sysUserId = sysUser.getId();
                        //如果真实姓名fornullof情况下，Will change my real name
                        if(oConvertUtils.isEmpty(sysUser.getRealname())){
                            sysUser.setRealname(wechatRealName);
                            //更新user
                            userMapper.updateById(sysUser);
                        }
                        String str = String.format("user %s(%s) Update successful！", sysUser.getRealname(), sysUser.getUsername());
                        syncInfoVo.addSuccessInfo(str);
                    }else{
                       syncInfoVo.addFailInfo("Enterprise WeChatuser "+wechatRealName+" 对应of组织user没有match到！");
                       continue; 
                    }
                }
                if (oConvertUtils.isNotEmpty(sysUserId)) {
                    //step 2 新增tenantuser表
                    this.createUserTenant(sysUserId,false,tenantId);
                    //step 3 新建或更新third party账号表
                    SysThirdAccount sysThirdAccount = sysThirdAccountService.getOneByUuidAndThirdType(wechatUserId, THIRD_TYPE, tenantId, wechatUserId);
                    this.thirdAccountSaveOrUpdate(sysThirdAccount,sysUserId,wechatUserId,wechatRealName,tenantId);
                    //step 4 新建或更新userdepartment关系表
                    if(oConvertUtils.isNotEmpty(wechatDepartId)){
                        String wechatDepartIds = wechatDepartId.toString();
                        String[] departIds = wechatDepartIds.substring(1, wechatDepartIds.length() - 1).split(",");
                        this.userDepartSaveOrUpdate(idsMap,sysUserId,departIds);
                    }
                }
            }
        } else {
            syncInfoVo.addFailInfo("user同同步fail，请查看Enterprise WeChat是否存在user！");
        }

    }
    
    /**
     * 保存user
     *
     * @param username user名
     * @param wechatRealName Enterprise WeChatuser真实姓名
     * @param syncInfo 存放成功或failof信息
     * @param wechatUserId wechatUserId Enterprise WeChat对应ofid
     * @return
     */
    private String saveUser(String username, String wechatRealName, SyncInfoVo syncInfo, String wechatUserId) {
        SysUser sysUser = new SysUser();
        sysUser.setRealname(wechatRealName);
        sysUser.setPassword(username);
        sysUser.setUsername(username);
        sysUser.setDelFlag(CommonConstant.DEL_FLAG_0);
        //Set creation time
        sysUser.setCreateTime(new Date());
        String salt = oConvertUtils.randomGen(8);
        sysUser.setSalt(salt);
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), sysUser.getPassword(), salt);
        sysUser.setPassword(passwordEncode);
        sysUser.setStatus(1);
        sysUser.setDelFlag(CommonConstant.DEL_FLAG_0);
        //user表字段org_code不能在这里设置他of值
        sysUser.setOrgCode(null);
        try {
            userMapper.insert(sysUser);
            String str = String.format("user %s(%s) Created successfully！", sysUser.getRealname(), sysUser.getUsername());
            syncInfo.addSuccessInfo(str);
            return sysUser.getId();
        } catch (Exception e) {
            User user = new User();
            user.setUserid(wechatUserId);
            user.setName(wechatRealName);
            this.syncUserCollectErrInfo(e, user, syncInfo);
        }

        return "";
    }

    /**
     * 新增usertenant
     *
     * @param userId
     * @param isUpdate Is it a new addition?
     * @param tenantId
     */
    private void createUserTenant(String userId, Boolean isUpdate, Integer tenantId) {
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            //judge当前user是否已在该tenant下面
            Integer count = sysUserTenantMapper.userTenantIzExist(userId, tenantId);
            //count for0 新增tenantuser,Otherwise, no need to add
            if (count == 0) {
                SysUserTenant userTenant = new SysUserTenant();
                userTenant.setTenantId(tenantId);
                userTenant.setUserId(userId);
                userTenant.setStatus(isUpdate ? CommonConstant.USER_TENANT_UNDER_REVIEW : CommonConstant.USER_TENANT_NORMAL);
                sysUserTenantMapper.insert(userTenant);
            }
        }
    }

    /**
     * 新建或更新userdepartment关系表
     * @param idsMap departmentidgather keyforEnterprise WeChatofid value for系统departmentofid
     * @param sysUserId 系统对应ofuserid
     */
    private void userDepartSaveOrUpdate(Map<String, String> idsMap, String sysUserId, String[] departIds) {
        LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<>();
        query.eq(SysUserDepart::getUserId,sysUserId);
        for (String departId:departIds) {
            departId = departId.trim();
            if(idsMap.containsKey(departId)){
                String value = idsMap.get(departId);
                //Queryuser是否在department里面
                query.eq(SysUserDepart::getDepId,value);
                long count = sysUserDepartService.count(query);
                if(count == 0){
                    //does not exist，则新增departmentuser关系
                    SysUserDepart sysUserDepart = new SysUserDepart(null,sysUserId,value);
                    sysUserDepartService.save(sysUserDepart);
                }
            }    
        }
    }

    public List<JwUserDepartVo> getThirdUserBindByWechat(int tenantId) {
        return sysThirdAccountMapper.getThirdUserBindByWechat(tenantId,THIRD_TYPE);
    }
}
