package org.jeecg.modules.system.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeecg.dingtalk.api.core.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.entity.SysThirdAppConfig;
import org.jeecg.modules.system.service.ISysThirdAccountService;
import org.jeecg.modules.system.service.ISysThirdAppConfigService;
import org.jeecg.modules.system.service.impl.ThirdAppDingtalkServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.jeecg.modules.system.vo.thirdapp.JwSysUserDepartVo;
import org.jeecg.modules.system.vo.thirdapp.JwUserDepartVo;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * third partyAppdocking
 * @author: jeecg-boot
 */
@Slf4j
@RestController("thirdAppController")
@RequestMapping("/sys/thirdApp")
public class ThirdAppController {

    @Autowired
    ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
    @Autowired
    ThirdAppDingtalkServiceImpl dingtalkService;

    @Autowired
    private ISysThirdAppConfigService appConfigService;

    @Autowired
    private ISysThirdAccountService sysThirdAccountService;

    /**
     * Get enabled systems
     */
    @GetMapping("/getEnabledType")
    public Result getEnabledType() {
        Map<String, Boolean> enabledMap = new HashMap(5);
        int tenantId;
        //Whether to enable multi-tenant data isolation of the system management module【SAASMulti-tenant model】
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            tenantId = oConvertUtils.getInt(TenantContext.getTenant(), -1);
        } else {
            tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        }
        //Query当前tenant下的third partyConfiguration
        List<SysThirdAppConfig> list = appConfigService.getThirdConfigListByThirdType(tenantId);
        //Is DingTalk configured?
        boolean dingConfig = false;
        //Is Enterprise WeChat configured?
        boolean qywxConfig = false;
        if(null != list && list.size()>0){
            for (SysThirdAppConfig config:list) {
                if(MessageTypeEnum.DD.getType().equals(config.getThirdType())){
                    dingConfig = true;
                    continue;
                }
                if(MessageTypeEnum.QYWX.getType().equals(config.getThirdType())){
                    qywxConfig = true;
                    continue;
                }
            }
        }
        enabledMap.put("wechatEnterprise", qywxConfig);
        enabledMap.put("dingtalk", dingConfig);
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]Isolation via tenant mode------------
        return Result.OK(enabledMap);
    }

    /**
     * Sync local[user]arrive【Enterprise WeChat】
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/wechatEnterprise/user/toApp")
    public Result syncWechatEnterpriseUserToApp(@RequestParam(value = "ids", required = false) String ids) {
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Isolation via tenant mode ------------
        //获取Enterprise WeChatConfiguration
        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
        if (null != config) {
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]Isolation via tenant mode ------------
            SyncInfoVo syncInfo = wechatEnterpriseService.syncLocalUserToThirdApp(ids);
            if (syncInfo.getFailInfo().size() == 0) {
                return Result.OK("Synchronization successful", syncInfo);
            } else {
                return Result.error("Sync failed", syncInfo);
            }
        }
        return Result.error("Enterprise WeChat尚未Configuration,请ConfigurationEnterprise WeChat");
    }

    /**
     * synchronous【Enterprise WeChat】[user]arrive本地
     *
     * @param ids void
     * @return
     */
    @GetMapping("/sync/wechatEnterprise/user/toLocal")
    public Result syncWechatEnterpriseUserToLocal(@RequestParam(value = "ids", required = false) String ids) {
        return Result.error("由于Enterprise WeChat接口调整，synchronousarrive本地功能已失效");

//        if (thirdAppConfig.isWechatEnterpriseEnabled()) {
//            SyncInfoVo syncInfo = wechatEnterpriseService.syncThirdAppUserToLocal();
//            if (syncInfo.getFailInfo().size() == 0) {
//                return Result.OK("Synchronization successful", syncInfo);
//            } else {
//                return Result.error("Sync failed", syncInfo);
//            }
//        }
//        return Result.error("Enterprise WeChatsynchronous功能已禁用");
    }

    /**
     * Sync local[department]arrive【Enterprise WeChat】
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/wechatEnterprise/depart/toApp")
    public Result syncWechatEnterpriseDepartToApp(@RequestParam(value = "ids", required = false) String ids) {
        //获取Enterprise WeChatConfiguration
        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
        if (null != config) {
            SyncInfoVo syncInfo = wechatEnterpriseService.syncLocalDepartmentToThirdApp(ids);
            if (syncInfo.getFailInfo().size() == 0) {
                return Result.OK("Synchronization successful", null);
            } else {
                return Result.error("Sync failed", syncInfo);
            }
        }
        return Result.error("Enterprise WeChat尚未Configuration,请ConfigurationEnterprise WeChat");
    }

    /**
     * synchronous【Enterprise WeChat】[department]arrive本地
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/wechatEnterprise/depart/toLocal")
    public Result syncWechatEnterpriseDepartToLocal(@RequestParam(value = "ids", required = false) String ids) {
        return Result.error("由于Enterprise WeChat接口调整，Enterprise WeChatSync localdepartment失效");
//        //获取Enterprise WeChatConfiguration
//        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
//        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
//        if (null != config) {
//            SyncInfoVo syncInfo = wechatEnterpriseService.syncThirdAppDepartmentToLocal(ids);
//            if (syncInfo.getFailInfo().size() == 0) {
//                return Result.OK("Synchronization successful", syncInfo);
//            } else {
//                return Result.error("Sync failed", syncInfo);
//            }
//        }
//        return Result.error("Enterprise WeChat尚未Configuration,请ConfigurationEnterprise WeChat");
    }

    /**
     * Sync local[department]arrive【DingTalk】
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/dingtalk/depart/toApp")
    public Result syncDingtalkDepartToApp(@RequestParam(value = "ids", required = false) String ids) {
        //获取DingTalkConfiguration
        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
        if (null != config) {
            SyncInfoVo syncInfo = dingtalkService.syncLocalDepartmentToThirdApp(ids);
            if (syncInfo.getFailInfo().size() == 0) {
                return Result.OK("Synchronization successful", null);
            } else {
                return Result.error("Sync failed", syncInfo);
            }
        }
        return Result.error("DingTalk尚未Configuration,请ConfigurationDingTalk");
    }

//    /**
//     * synchronous【DingTalk】[department]arrive本地
//     *
//     * @param ids
//     * @return
//     */
//   @GetMapping("/sync/dingtalk/depart/toLocal")
//    public Result syncDingtalkDepartToLocal(@RequestParam(value = "ids", required = false) String ids) {
//        //获取DingTalkConfiguration
//        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
//        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
//        if (null!= config) {
//            SyncInfoVo syncInfo = dingtalkService.syncThirdAppDepartmentToLocal(ids);
//            if (syncInfo.getFailInfo().size() == 0) {
//                return Result.OK("Synchronization successful", syncInfo);
//            } else {
//                return Result.error("Sync failed", syncInfo);
//            }
//        }
//        return Result.error("DingTalk尚未Configuration,请ConfigurationDingTalk");
//    }

    /**
     * Sync local[user]arrive【DingTalk】
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/dingtalk/user/toApp")
    public Result syncDingtalkUserToApp(@RequestParam(value = "ids", required = false) String ids) {
        //获取DingTalkConfiguration
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        //According to tenantid和third party类别获取tenant数据
        SysThirdAppConfig appConfig = appConfigService.getThirdConfigByThirdType(tenantId,MessageTypeEnum.DD.getType());
        if(null != appConfig){
            SyncInfoVo syncInfo = dingtalkService.syncLocalUserToThirdApp(ids);
            if (syncInfo.getFailInfo().size() == 0) {
                return Result.OK("Synchronization successful", syncInfo);
            } else {
                return Result.error("Sync failed", syncInfo);
            }
        }
        return Result.error("DingTalk尚未Configuration,请ConfigurationDingTalk");
    }

//    /**
//     * synchronous【DingTalk】[user]arrive本地
//     *
//     * @param ids void
//     * @return
//     */
//    @GetMapping("/sync/dingtalk/user/toLocal")
//    public Result syncDingtalkUserToLocal(@RequestParam(value = "ids", required = false) String ids) {
//        //获取DingTalkConfiguration
//        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
//        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
//        if (null != config) {
//            SyncInfoVo syncInfo = dingtalkService.syncThirdAppUserToLocal();
//            if (syncInfo.getFailInfo().size() == 0) {
//                return Result.OK("Synchronization successful", syncInfo);
//            } else {
//                return Result.error("Sync failed", syncInfo);
//            }
//        }
//        return Result.error("DingTalk尚未Configuration,请ConfigurationDingTalk");
//    }

    /**
     * Send message test
     *
     * @return
     */
    @PostMapping("/sendMessageTest")
    public Result sendMessageTest(@RequestBody JSONObject params, HttpServletRequest request) {
        /* Get the parameters passed by the front desk */
        // third partyapptype
        String app = params.getString("app");
        // Whether to send to everyone
        boolean sendAll = params.getBooleanValue("sendAll");
        // message receiver，passsys_usertableusernameField，Separate multiple with commas
        String receiver = params.getString("receiver");
        // Message content
        String content = params.getString("content");
        // tenantid
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);

        String fromUser = JwtUtil.getUserNameByToken(request);
        String title = "third partyAPPMessage test";
        MessageDTO message = new MessageDTO(fromUser, receiver, title, content);
        message.setToAll(sendAll);
        //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]DingTalk、Enterprise WeChatIsolation via tenant mode ------------
        String weChatType = MessageTypeEnum.QYWX.getType();
        String dingType = MessageTypeEnum.DD.getType();
        if (weChatType.toUpperCase().equals(app)) {
            SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, weChatType);
            if (null != config) {
        //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]DingTalk、Enterprise WeChatIsolation via tenant mode ------------
                JSONObject response = wechatEnterpriseService.sendMessageResponse(message, false);
                return Result.OK(response);
            }
            return Result.error("Enterprise WeChat尚未Configuration,请ConfigurationEnterprise WeChat");
            //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]DingTalk、Enterprise WeChatIsolation via tenant mode ------------
        } else if (dingType.toUpperCase().equals(app)) {
            SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, dingType);
            if (null != config) {
            //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]DingTalk、Enterprise WeChatIsolation via tenant mode ------------
                Response<String> response = dingtalkService.sendMessageResponse(message, false);
                return Result.OK(response);
            }
            return Result.error("DingTalk尚未Configuration,请ConfigurationDingTalk");
        }
        return Result.error("不识别的third partyAPP");
    }

    /**
     * 撤回Message test
     *
     * @return
     */
    @PostMapping("/recallMessageTest")
    public Result recallMessageTest(@RequestBody JSONObject params) {
        /* Get the parameters passed by the front desk */
        // third partyapptype
        String app = params.getString("app");
        // informationid
        String msgTaskId = params.getString("msg_task_id");
        //tenantid
        int tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
        if (CommonConstant.WECHAT_ENTERPRISE.equals(app)) {
            SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
            if (null != config) {
                return Result.error("Enterprise WeChat不支持撤回information");
            }
            return Result.error("Enterprise WeChat尚未Configuration,请ConfigurationEnterprise WeChat");
        } else if (CommonConstant.DINGTALK.equals(app)) {
            SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
            if (null != config) {
                Response<JSONObject> response = dingtalkService.recallMessageResponse(msgTaskId);
                if (response.isSuccess()) {
                    return Result.OK("Withdrawal successful", response);
                } else {
                    return Result.error("Undo failed：" + response.getErrcode() + "——" + response.getErrmsg(), response);
                }
            }
            return Result.error("DingTalk尚未Configuration,请ConfigurationDingTalk");
        }
        return Result.error("不识别的third partyAPP");
    }

    //========================begin 应用低代码DingTalk/Enterprise WeChatsynchronoususerdepartment专用 =============================
    /**
     * 添加third partyappConfiguration
     *
     * @param appConfig
     * @return
     */
    @RequestMapping(value = "/addThirdAppConfig", method = RequestMethod.POST)
    public Result<String> addThirdAppConfig(@RequestBody SysThirdAppConfig appConfig) {
        Result<String> result = new Result<>();
        //according to当前登录tenantid和third party类别判断是否已经创建
        Integer tenantId = oConvertUtils.isNotEmpty(appConfig.getTenantId()) ? appConfig.getTenantId() : oConvertUtils.getInt(TenantContext.getTenant(), 0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, appConfig.getThirdType());
        if (null != config) {
            result.error500("Operation failed,同一个tenant下只允许绑定一个DingTalk或者Enterprise WeChat");
            return result;
        }
        String clientId = appConfig.getClientId();
        //by applicationkey获取third partyConfiguration
        List<SysThirdAppConfig> thirdAppConfigByClientId = appConfigService.getThirdAppConfigByClientId(clientId);
        if(CollectionUtil.isNotEmpty(thirdAppConfigByClientId)){
            result.error500("AppKeyAlready exists，Do not add duplicates");
            return result;
        }
        try {
            appConfig.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(),0));
            appConfigService.save(appConfig);
            result.success("Added successfully！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Operation failed");
        }
        return result;
    }

    /**
     * 编辑third partyappConfiguration
     *
     * @param appConfig
     * @return
     */
    @RequestMapping(value = "/editThirdAppConfig", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> editThirdAppConfig(@RequestBody SysThirdAppConfig appConfig) {
        Result<String> result = new Result<>();
        SysThirdAppConfig config = appConfigService.getById(appConfig.getId());
        if (null == config) {
            result.error500("Data does not exist");
            return result;
        }
        String clientId = appConfig.getClientId();
        //If the edited applicationkey,inconsistent with the database，Need to judge the applicationkey是否Already exists
        if(!clientId.equals(config.getClientId())){
            //by applicationkey获取third partyConfiguration
            List<SysThirdAppConfig> thirdAppConfigByClientId = appConfigService.getThirdAppConfigByClientId(clientId);
            if(CollectionUtil.isNotEmpty(thirdAppConfigByClientId)){
                result.error500("AppKeyAlready exists，Do not add duplicates");
                return result;
            }
        }
        try {
            appConfigService.updateById(appConfig);
            result.success("Modification successful！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Operation failed");
        }
        return result;
    }

    /**
     * according toid删除third partyConfiguration表
     * @param id
     * @return
     */
    @DeleteMapping(value = "/deleteThirdAppConfig")
    @RequiresPermissions("system:third:config:delete")
    public Result<String> deleteThirdAppConfig(@RequestParam(name="id",required=true) String id) {
        Result<String> result = new Result<>();
        SysThirdAppConfig config = appConfigService.getById(id);
        if (null == config) {
            result.error500("Data does not exist");
            return result;
        }
        try {
            appConfigService.removeById(id);
            result.success("Unbinding successfully！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Operation failed");
        }
        return result;
    }
    

    /**
     * According to tenantid和third party类型获取third partyappConfiguration信息
     *
     * @param tenantId
     * @param thirdType
     * @return
     */
    @GetMapping("/getThirdConfigByTenantId")
    public Result<SysThirdAppConfig> getThirdAppByTenantId(@RequestParam(name = "tenantId", required = false) Integer tenantId,
                                                           @RequestParam(name = "thirdType") String thirdType) {
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            if (tenantId == null) {
                return Result.error("开启Multi-tenant model，tenantIDParameters are not allowed to be empty！");
            }
        } else {
            //tenant未pass递，Then use the platform
            if (tenantId == null) {
                tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
            }
        }
        Result<SysThirdAppConfig> result = new Result<>();
        LambdaQueryWrapper<SysThirdAppConfig> query = new LambdaQueryWrapper<>();
        query.eq(SysThirdAppConfig::getThirdType,thirdType);
        query.eq(SysThirdAppConfig::getTenantId,tenantId);
        SysThirdAppConfig sysThirdAppConfig = appConfigService.getOne(query);
        result.setSuccess(true);
        result.setResult(sysThirdAppConfig);
        return result;
    }

    /**
     * synchronous【DingTalk】[department和user]arrive本地
     *
     * @param ids
     * @return
     */
    @GetMapping("/sync/dingtalk/departAndUser/toLocal")
    public Result syncDingTalkDepartAndUserToLocal(@RequestParam(value = "ids", required = false) String ids) {
        Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.DD.getType());
        if (null != config) {
            SyncInfoVo syncInfo = dingtalkService.syncThirdAppDepartmentUserToLocal();
            if (syncInfo.getFailInfo().size() == 0) {
                return Result.OK("Synchronization successful", syncInfo);
            } else {
                return Result.error("Sync failed", syncInfo);
            }
        }
        return Result.error("DingTalk尚未Configuration,请ConfigurationDingTalk");
    }
    //========================end 应用低代码DingTalk/Enterprise WeChatsynchronoususerdepartment专用 ========================


    //========================begin 应用低代码账号设置third party账号绑定 ================================
    /**
     * 获取third party账号
     * @param thirdType
     * @return
     */
    @GetMapping("/getThirdAccountByUserId")
    public Result<List<SysThirdAccount>> getThirdAccountByUserId(@RequestParam(name="thirdType") String thirdType){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
        //according toidQuery
        query.eq(SysThirdAccount::getSysUserId,sysUser.getId());
        //扫码登录只有tenant为0
        query.eq(SysThirdAccount::getTenantId,CommonConstant.TENANT_ID_DEFAULT_VALUE);
        //according tothird party类别Query
        if(oConvertUtils.isNotEmpty(thirdType)){
            query.in(SysThirdAccount::getThirdType, Arrays.asList(thirdType.split(SymbolConstant.COMMA)));
        }
        List<SysThirdAccount> list = sysThirdAccountService.list(query);
        return Result.ok(list);
    }

    /**
     * 绑定third party账号
     * @return
     */
    @PostMapping("/bindThirdAppAccount")
    public Result<SysThirdAccount> bindThirdAppAccount(@RequestBody SysThirdAccount sysThirdAccount){
        SysThirdAccount thirdAccount = sysThirdAccountService.bindThirdAppAccountByUserId(sysThirdAccount);
        return Result.ok(thirdAccount);
    }

    /**
     * 删除third partyuser信息
     * @param sysThirdAccount
     * @return
     */
    @DeleteMapping("/deleteThirdAccount")
    public Result<String> deleteThirdAccountById(@RequestBody SysThirdAccount sysThirdAccount){
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(!sysUser.getId().equals(sysThirdAccount.getSysUserId())){
            return Result.error("No right to modify other people’s information");
        }
        SysThirdAccount thirdAccount = sysThirdAccountService.getById(sysThirdAccount.getId());
        if(null == thirdAccount){
            return Result.error("未找arrive改third party账户信息");
        }
        sysThirdAccountService.removeById(thirdAccount.getId());
        return Result.ok("Unbinding successfully");
    }
    //========================end 应用低代码账号设置third party账号绑定 ================================

    /**
     * 获取Enterprise WeChat绑定的user信息
     * @param request
     * @return
     */
    @GetMapping("/getThirdUserByWechat")
    public Result<JwSysUserDepartVo> getThirdUserByWechat(HttpServletRequest request){
        //获取Enterprise WeChatConfiguration
        Integer tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request),0);
        SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(tenantId, MessageTypeEnum.QYWX.getType());
        if (null != config) {
            JwSysUserDepartVo list = wechatEnterpriseService.getThirdUserByWechat(tenantId);
            return Result.ok(list);
        }
        return Result.error("Enterprise WeChat尚未Configuration,请ConfigurationEnterprise WeChat"); 
    }

    /**
     * synchronousEnterprise WeChatdepartment和userarrive本地
     * @param jwUserDepartJson
     * @param request
     * @return
     */
    @GetMapping("/sync/wechatEnterprise/departAndUser/toLocal")
    public Result<SyncInfoVo> syncWechatEnterpriseDepartAndUserToLocal(@RequestParam(name = "jwUserDepartJson") String jwUserDepartJson,HttpServletRequest request){
        int tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request), 0);
        SyncInfoVo syncInfoVo = wechatEnterpriseService.syncWechatEnterpriseDepartAndUserToLocal(jwUserDepartJson,tenantId);
        return Result.ok(syncInfoVo);
    }

    /**
     * Query被绑定的Enterprise WeChatuser
     * @param request
     * @return
     */
    @GetMapping("/getThirdUserBindByWechat")
    public Result<List<JwUserDepartVo>> getThirdUserBindByWechat(HttpServletRequest request){
        int tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request), 0);
        List<JwUserDepartVo> jwSysUserDepartVos = wechatEnterpriseService.getThirdUserBindByWechat(tenantId);
        return Result.ok(jwSysUserDepartVos);
    }
}
