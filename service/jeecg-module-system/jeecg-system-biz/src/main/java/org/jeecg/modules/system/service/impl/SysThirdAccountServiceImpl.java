package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeecg.dingtalk.api.base.JdtBaseAPI;
import com.jeecg.dingtalk.api.core.response.Response;
import com.jeecg.dingtalk.api.core.vo.AccessToken;
import com.jeecg.dingtalk.api.user.JdtUserAPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.mapper.SysRoleMapper;
import org.jeecg.modules.system.mapper.SysThirdAccountMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserRoleMapper;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.ISysThirdAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Description: Third-party login account table
 * @Author: jeecg-boot
 * @Date:   2020-11-17
 * @Version: V1.0
 */
@Service
@Slf4j
public class SysThirdAccountServiceImpl extends ServiceImpl<SysThirdAccountMapper, SysThirdAccount> implements ISysThirdAccountService {
    
    @Autowired
    private  SysThirdAccountMapper sysThirdAccountMapper;
    
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    
    @Value("${justauth.type.DINGTALK.client-id:}")
    private String dingTalkClientId;   
    @Value("${justauth.type.DINGTALK.client-secret:}")
    private String dingTalkClientSecret;
    
    @Override
    public void updateThirdUserId(SysUser sysUser,String thirdUserUuid) {
        //Modify the third-party login account table to add usersid
        LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
        query.eq(SysThirdAccount::getThirdUserUuid,thirdUserUuid);
        //Scan the QR code to log in and update the default tenant that is saved when the user is created.，When updating, you also need to query based on the default tenant.，Under the same companyUUIDis the same，Different applications need to distinguish tenants。
        query.eq(SysThirdAccount::getTenantId,CommonConstant.TENANT_ID_DEFAULT_VALUE);
        SysThirdAccount account = sysThirdAccountMapper.selectOne(query);
        SysThirdAccount sysThirdAccount = new SysThirdAccount();
        sysThirdAccount.setSysUserId(sysUser.getId());
        //According to current useridand login method to query the third-party login form
        LambdaQueryWrapper<SysThirdAccount> thirdQuery = new LambdaQueryWrapper<>();
        thirdQuery.eq(SysThirdAccount::getSysUserId,sysUser.getId());
        thirdQuery.eq(SysThirdAccount::getThirdType,account.getThirdType());
        thirdQuery.eq(SysThirdAccount::getThirdUserUuid,thirdUserUuid);
        thirdQuery.eq(SysThirdAccount::getTenantId,CommonConstant.TENANT_ID_DEFAULT_VALUE);
        SysThirdAccount sysThirdAccounts = sysThirdAccountMapper.selectOne(thirdQuery);
        if(sysThirdAccounts!=null){
            sysThirdAccount.setThirdUserId(sysThirdAccounts.getThirdUserId());
            sysThirdAccountMapper.deleteById(sysThirdAccounts.getId());
        }
        //Update user account tablesys_user_id
        sysThirdAccountMapper.update(sysThirdAccount,query);
    }
    
    @Override
    public SysUser createUser(String phone, String thirdUserUuid, Integer tenantId) {
       //Check with the third party first，Get login method
        LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
        query.eq(SysThirdAccount::getThirdUserUuid,thirdUserUuid);
        query.eq(SysThirdAccount::getTenantId,tenantId);
        SysThirdAccount account = sysThirdAccountMapper.selectOne(query);
        //Query whether the database exists by user name
        SysUser userByName = sysUserMapper.getUserByName(thirdUserUuid);
        if(null!=userByName){
            //If the account exists，will automatically add a timestamp
            String format = DateUtils.yyyymmddhhmmss.get().format(new Date());
            thirdUserUuid = thirdUserUuid + format;
        }
        //Add user
        SysUser user = new SysUser();
        user.setActivitiSync(CommonConstant.ACT_SYNC_1);
        user.setDelFlag(CommonConstant.DEL_FLAG_0);
        user.setStatus(1);
        user.setUsername(thirdUserUuid);
        user.setPhone(phone);
        //Set initial password
        String salt = oConvertUtils.randomGen(8);
        user.setSalt(salt);
        String passwordEncode = PasswordUtil.encrypt(user.getUsername(), "123456", salt);
        user.setPassword(passwordEncode);
        user.setRealname(account.getRealname());
        user.setAvatar(account.getAvatar());
        String s = this.saveThirdUser(user);
        //Update the user's third-party account tableuserId
        SysThirdAccount sysThirdAccount = new SysThirdAccount();
        sysThirdAccount.setSysUserId(s);
        sysThirdAccount.setTenantId(tenantId);
        sysThirdAccountMapper.update(sysThirdAccount,query);
        return user;
    }
    
    public String saveThirdUser(SysUser sysUser) {
        //save user
        String userid = UUIDGenerator.generate();
        sysUser.setId(userid);
        sysUserMapper.insert(sysUser);
        //Get third-party roles
        SysRole sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "third_role"));
        //save user角色
        SysUserRole userRole = new SysUserRole();
        userRole.setRoleId(sysRole.getId());
        userRole.setUserId(userid);
        sysUserRoleMapper.insert(userRole);
        return userid;
    }

    @Override
    public SysThirdAccount getOneBySysUserId(String sysUserId, String thirdType) {
        LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
        log.info("getSysUserId: {} ,getThirdType: {}",sysUserId,thirdType);
        queryWrapper.eq(SysThirdAccount::getSysUserId, sysUserId);
        queryWrapper.eq(SysThirdAccount::getThirdType, thirdType);
        return super.getOne(queryWrapper);
    }

    @Override
    public SysThirdAccount getOneByThirdUserId(String thirdUserId, String thirdType) {
        LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysThirdAccount::getThirdUserId, thirdUserId);
        queryWrapper.eq(SysThirdAccount::getThirdType, thirdType);
        return super.getOne(queryWrapper);
    }

    @Override
    public List<SysThirdAccount> listThirdUserIdByUsername(String[] sysUsernameArr, String thirdType, Integer tenantId) {
        return sysThirdAccountMapper.selectThirdIdsByUsername(sysUsernameArr, thirdType,tenantId);
    }

    @Override
    public SysThirdAccount saveThirdUser(ThirdLoginModel tlm, Integer tenantId) {
        SysThirdAccount user = new SysThirdAccount();
        user.setDelFlag(CommonConstant.DEL_FLAG_0);
        user.setStatus(1);
        user.setThirdType(tlm.getSource());
        user.setAvatar(tlm.getAvatar());
        user.setRealname(tlm.getUsername());
        user.setThirdUserUuid(tlm.getUuid());
        user.setTenantId(tenantId);
        //update-begin---author:wangshuai ---date:20230306  for：Determine if it is DingTalk，Third-party users need toidCheck it out，Useful when sending templates------------
        //=============begin Determine if it is DingTalk，Third-party users need toidCheck it out，Useful when sending templates==========
        if(CommonConstant.DINGTALK.toLowerCase().equals(tlm.getSource())){
            AccessToken accessToken = JdtBaseAPI.getAccessToken(dingTalkClientId, dingTalkClientSecret);
            Response<String> getUserIdRes = JdtUserAPI.getUseridByUnionid(tlm.getUuid(), accessToken.getAccessToken());
            if (getUserIdRes.isSuccess()) {
                user.setThirdUserId(getUserIdRes.getResult());
            }else{
                user.setThirdUserId(tlm.getUuid());
            }
        //=============end Determine if it is DingTalk，Third-party users need toidCheck it out，Useful when sending templates==========
        }else{
            user.setThirdUserId(tlm.getUuid());
        }
        //update-end---author:wangshuai ---date:20230306  for：Determine if it is DingTalk，Third-party users need toidCheck it out，Useful when sending templates------------
        super.save(user);
        return user;
    }

    @Override
    public SysThirdAccount bindThirdAppAccountByUserId(SysThirdAccount sysThirdAccount) {
        String thirdUserUuid = sysThirdAccount.getThirdUserUuid();
        String thirdType = sysThirdAccount.getThirdType();
        //Get the currently logged in user
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //The current third-party user has been bound by another user
        SysThirdAccount oneByThirdUserId = this.getOneByUuidAndThirdType(thirdUserUuid, thirdType,CommonConstant.TENANT_ID_DEFAULT_VALUE, null);
        if(null != oneByThirdUserId){
            //if not empty，And the third-party table is consistent with the currently logged in user，Return directly
            if(oConvertUtils.isNotEmpty(oneByThirdUserId.getSysUserId()) && oneByThirdUserId.getSysUserId().equals(sysUser.getId())){
                return oneByThirdUserId;
            }else if(oConvertUtils.isNotEmpty(oneByThirdUserId.getSysUserId())){
                //If the user of the third-party tableidNot empty，That means it has been bound.
                throw new JeecgBootException("This Knockout Cloud account has been bound to another third-party account,Please unbind or bind other Knockout cloud accounts");
            }else{
                //Update third-party table information userid
                oneByThirdUserId.setSysUserId(sysUser.getId());
                oneByThirdUserId.setThirdType(thirdType);
                sysThirdAccountMapper.updateById(oneByThirdUserId);
                return oneByThirdUserId;
            }
        }else{
            throw new JeecgBootException("Account binding failed,Please try again later");
        }
    }

    @Override
    public SysThirdAccount getOneByUuidAndThirdType(String unionid, String thirdType,Integer tenantId,String thirdUserId) {
        LambdaQueryWrapper<SysThirdAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysThirdAccount::getThirdType, thirdType);
        //update-begin---author:wangshuai---date:2023-12-04---for: If third party usersidIf it is empty, the third-party user query logic will not be used.，Because scan the QR code to log inthird_user_idis the only one，no duplicates---
        if(oConvertUtils.isNotEmpty(thirdUserId)){
            queryWrapper.and((wrapper) ->wrapper.eq(SysThirdAccount::getThirdUserUuid,unionid).or().eq(SysThirdAccount::getThirdUserId,thirdUserId));
        }else{
            queryWrapper.eq(SysThirdAccount::getThirdUserUuid, unionid);
        }
        //update-end---author:wangshuai---date:2023-12-04---for:If third party usersidIf it is empty, the third-party user query logic will not be used.，Because scan the QR code to log inthird_user_idis the only one，no duplicates---
        queryWrapper.eq(SysThirdAccount::getTenantId, tenantId);
        return super.getOne(queryWrapper);
    }

}
