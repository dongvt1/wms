package org.jeecg.modules.api.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.constant.enums.EmailTemplateEnum;
import org.jeecg.common.desensitization.util.SensitiveInfoUtil;
import org.jeecg.common.system.vo.*;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Servitization systemmodule External interface request class
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/api")
public class SystemApiController {

    @Autowired
    private SysBaseApiImpl sysBaseApi;
    @Autowired
    private ISysUserService sysUserService;

    /**
     * Send system message
     * @param message Assign parameters using constructor If not setcategory(Message type)The default is2 Send system message
     */
    @PostMapping("/sendSysAnnouncement")
    public void sendSysAnnouncement(@RequestBody MessageDTO message){
        sysBaseApi.sendSysAnnouncement(message);
    }

    /**
     * Send message With business parameters
     * @param message Assign parameters using constructor
     */
    @PostMapping("/sendBusAnnouncement")
    public void sendBusAnnouncement(@RequestBody BusMessageDTO message){
        sysBaseApi.sendBusAnnouncement(message);
    }

    /**
     * 通过模板Send message
     * @param message Assign parameters using constructor
     */
    @PostMapping("/sendTemplateAnnouncement")
    public void sendTemplateAnnouncement(@RequestBody TemplateMessageDTO message){
        sysBaseApi.sendTemplateAnnouncement(message);
    }

    /**
     * 通过模板Send message With business parameters
     * @param message Assign parameters using constructor
     */
    @PostMapping("/sendBusTemplateAnnouncement")
    public void sendBusTemplateAnnouncement(@RequestBody BusTemplateMessageDTO message){
        sysBaseApi.sendBusTemplateAnnouncement(message);
    }

    /**
     * Via message center template，Generate push content
     * @param templateDTO Assign parameters using constructor
     * @return
     */
    @PostMapping("/parseTemplateByCode")
    public String parseTemplateByCode(@RequestBody TemplateDTO templateDTO){
        return sysBaseApi.parseTemplateByCode(templateDTO);
    }

    /**
     * According to business typebusTypeand businessbusIdModify message read
     */
    @GetMapping("/updateSysAnnounReadFlag")
    public void updateSysAnnounReadFlag(@RequestParam("busType") String busType, @RequestParam("busId")String busId){
        sysBaseApi.updateSysAnnounReadFlag(busType, busId);
    }

    /**
     * Query user information based on user account
     * @param username
     * @return
     */
    @GetMapping("/getUserByName")
    public LoginUser getUserByName(@RequestParam("username") String username){
        LoginUser loginUser = sysBaseApi.getUserByName(username);
        //User information encryption
        try {
            SensitiveInfoUtil.handlerObject(loginUser, true);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
        return loginUser;
    }
    
    /**
     * Query users based on user accountID
     * @param username
     * @return
     */
    @GetMapping("/getUserIdByName")
    public String getUserIdByName(@RequestParam("username") String username){
        String userId = sysBaseApi.getUserIdByName(username);
        return userId;
    }

    /**
     * According to useridQuery user information
     * @param id
     * @return
     */
    @GetMapping("/getUserById")
    LoginUser getUserById(@RequestParam("id") String id){
        LoginUser loginUser = sysBaseApi.getUserById(id);
        //User information encryption
        try {
            SensitiveInfoUtil.handlerObject(loginUser, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return loginUser;
    }

    /**
     * Query role collection by user account
     * @param username
     * @return
     */
    @GetMapping("/getRolesByUsername")
    List<String> getRolesByUsername(@RequestParam("username") String username){
        return sysBaseApi.getRolesByUsername(username);
    }
    
    /**
     * Query role collection by user account
     * @param userId
     * @return
     */
    @GetMapping("/getRolesByUserId")
    List<String> getRolesByUserId(@RequestParam("userId") String userId){
        return sysBaseApi.getRolesByUserId(userId);
    }

    /**
     * Query department collection through user account
     * @param username
     * @return department id
     */
    @GetMapping("/getDepartIdsByUsername")
    List<String> getDepartIdsByUsername(@RequestParam("username") String username){
        return sysBaseApi.getDepartIdsByUsername(username);
    }
    
    /**
     * Query department collection through user account
     * @param userId
     * @return department id
     */
    @GetMapping("/getDepartIdsByUserId")
    List<String> getDepartIdsByUserId(@RequestParam("userId") String userId){
        return sysBaseApi.getDepartIdsByUserId(userId);
    }

    /**
     * 通过用户账号查询department父IDgather
     * @param username
     * @return department id
     */
    @GetMapping("/getDepartParentIdsByUsername")
    Set<String>  getDepartParentIdsByUsername(@RequestParam("username") String username){
        return sysBaseApi.getDepartParentIdsByUsername(username);
    }

    /**
     * 查询department父IDgather
     * @param depIds
     * @return department id
     */
    @GetMapping("/getDepartParentIdsByDepIds")
    Set<String> getDepartParentIdsByDepIds(@RequestParam("depIds") Set<String> depIds){
        return sysBaseApi.getDepartParentIdsByDepIds(depIds);
    }

    /**
     * 通过用户账号查询department name
     * @param username
     * @return department name
     */
    @GetMapping("/getDepartNamesByUsername")
    List<String> getDepartNamesByUsername(@RequestParam("username") String username){
        return sysBaseApi.getDepartNamesByUsername(username);
    }


    /**
     * Get data dictionary
     * @param code
     * @return
     */
    @GetMapping("/queryDictItemsByCode")
    List<DictModel> queryDictItemsByCode(@RequestParam("code") String code){
        return sysBaseApi.queryDictItemsByCode(code);
    }

    /**
     * Get a valid data dictionary
     * @param code
     * @return
     */
    @GetMapping("/queryEnableDictItemsByCode")
    List<DictModel> queryEnableDictItemsByCode(@RequestParam("code") String code){
        return sysBaseApi.queryEnableDictItemsByCode(code);
    }


    /** Query all parent dictionaries，according tocreate_timesort */
    @GetMapping("/queryAllDict")
    List<DictModel> queryAllDict(){
//        try{
//            //sleep10Second，gatewaygateway5Second超时，Will trigger circuit breaker downgrade operation
//            Thread.sleep(10000);
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        log.info("--I amjeecg-systemservice node，Microservice interfacequeryAllDictcalled--");
        return sysBaseApi.queryAllDict();
    }

    /**
     * Query all classification dictionaries
     * @return
     */
    @GetMapping("/queryAllSysCategory")
    List<SysCategoryModel> queryAllSysCategory(){
        return sysBaseApi.queryAllSysCategory();
    }


    /**
     * 查询所有department as dictionary information id -->value,departName -->text
     * @return
     */
    @GetMapping("/queryAllDepartBackDictModel")
    List<DictModel> queryAllDepartBackDictModel(){
        return sysBaseApi.queryAllDepartBackDictModel();
    }

    /**
     * Get all roles With ginseng
     * roleIds Role selected by default
     * @return
     */
    @GetMapping("/queryAllRole")
    public List<ComboModel> queryAllRole(@RequestParam(name = "roleIds",required = false)String[] roleIds){
        if(roleIds==null || roleIds.length==0){
            return sysBaseApi.queryAllRole();
        }else{
            return sysBaseApi.queryAllRole(roleIds);
        }
    }

    /**
     * Query roles by user accountIdgather
     * @param username
     * @return
     */
    @GetMapping("/getRoleIdsByUsername")
    public List<String> getRoleIdsByUsername(@RequestParam("username")String username){
        return sysBaseApi.getRoleIdsByUsername(username);
    }

    /**
     * 通过department编号查询departmentid
     * @param orgCode
     * @return
     */
    @GetMapping("/getDepartIdsByOrgCode")
    public String getDepartIdsByOrgCode(@RequestParam("orgCode")String orgCode){
        return sysBaseApi.getDepartIdsByOrgCode(orgCode);
    }

    /**
     * 查询所有department
     * @return
     */
    @GetMapping("/getAllSysDepart")
    public List<SysDepartModel> getAllSysDepart(){
        return sysBaseApi.getAllSysDepart();
    }

    /**
     * according to id Query the data stored in the database DynamicDataSourceModel
     *
     * @param dbSourceId
     * @return
     */
    @GetMapping("/getDynamicDbSourceById")
    DynamicDataSourceModel getDynamicDbSourceById(@RequestParam("dbSourceId")String dbSourceId){
        return sysBaseApi.getDynamicDbSourceById(dbSourceId);
    }



    /**
     * according todepartmentId获取department负责人
     * @param deptId
     * @return
     */
    @GetMapping("/getDeptHeadByDepId")
    public List<String> getDeptHeadByDepId(@RequestParam("deptId") String deptId){
        return sysBaseApi.getDeptHeadByDepId(deptId);
    }

    /**
     * 查找父classdepartment
     * @param departId
     * @return
     */
    @GetMapping("/getParentDepartId")
    public DictModel getParentDepartId(@RequestParam("departId")String departId){
        return sysBaseApi.getParentDepartId(departId);
    }

    /**
     * according to code Query the data stored in the database DynamicDataSourceModel
     *
     * @param dbSourceCode
     * @return
     */
    @GetMapping("/getDynamicDbSourceByCode")
    public DynamicDataSourceModel getDynamicDbSourceByCode(@RequestParam("dbSourceCode") String dbSourceCode){
        return sysBaseApi.getDynamicDbSourceByCode(dbSourceCode);
    }

    /**
     * Send message to specified user
     * @param userIds
     * @param cmd
     */
    @GetMapping("/sendWebSocketMsg")
    public void sendWebSocketMsg(String[] userIds, String cmd){
        sysBaseApi.sendWebSocketMsg(userIds, cmd);
    }


    /**
     * according toidGet all participating users
     * userIds
     * @return
     */
    @GetMapping("/queryAllUserByIds")
    public List<UserAccountInfo> queryAllUserByIds(@RequestParam("userIds") String[] userIds){
        return sysBaseApi.queryAllUserByIds(userIds);
    }

    /**
     * Query all users returnComboModel
     * @return
     */
    @GetMapping("/queryAllUserBackCombo")
    public List<ComboModel> queryAllUserBackCombo(){
        return sysBaseApi.queryAllUserBackCombo();
    }

    /**
     * Query users by page returnJSONObject
     * @return
     */
    @GetMapping("/queryAllUser")
    public JSONObject queryAllUser(@RequestParam(name="userIds",required=false)String userIds, @RequestParam(name="pageNo",required=false) Integer pageNo,@RequestParam(name="pageSize",required=false) Integer pageSize){
        return sysBaseApi.queryAllUser(userIds, pageNo, pageSize);
    }



    /**
     * Push meeting check-in information to preview
     * userIds
     * @return
     * @param userId
     */
    @GetMapping("/meetingSignWebsocket")
    public void meetingSignWebsocket(@RequestParam("userId")String userId){
        sysBaseApi.meetingSignWebsocket(userId);
    }

    /**
     * according tonameGet all participating users
     * userNames
     * @return
     */
    @GetMapping("/queryUserByNames")
    public List<UserAccountInfo> queryUserByNames(@RequestParam("userNames")String[] userNames){
        return sysBaseApi.queryUserByNames(userNames);
    }

    /**
     * 获取用户of角色gather
     * @param username
     * @return
     */
    @GetMapping("/getUserRoleSet")
    public Set<String> getUserRoleSet(@RequestParam("username")String username){
        return sysBaseApi.getUserRoleSet(username);
    }
    
    /**
     * 获取用户of角色gather
     * @param userId
     * @return
     */
    @GetMapping("/getUserRoleSetById")
    public Set<String> getUserRoleSetById(@RequestParam("userId")String userId){
        return sysBaseApi.getUserRoleSetById(userId);
    }

    /**
     * 获取用户of权限gather
     * @param userId User tableID
     * @return
     */
    @GetMapping("/getUserPermissionSet")
    public Set<String> getUserPermissionSet(@RequestParam("userId") String userId){
        return sysBaseApi.getUserPermissionSet(userId);
    }

    //-----

    /**
     * Determine whether there isonlineAccess permissions
     * @param onlineAuthDTO
     * @return
     */
    @PostMapping("/hasOnlineAuth")
    public boolean hasOnlineAuth(@RequestBody OnlineAuthDTO onlineAuthDTO){
        return sysBaseApi.hasOnlineAuth(onlineAuthDTO);
    }

    /**
     * Query user role information
     * @param username
     * @return
     */
    @GetMapping("/queryUserRoles")
    public Set<String> queryUserRoles(@RequestParam("username") String username){
        return sysUserService.getUserRolesSet(username);
    }
    
    /**
     * Query user role information
     * @param userId
     * @return
     */
    @GetMapping("/queryUserRolesById")
    public Set<String> queryUserRolesById(@RequestParam("userId") String userId){
        return sysUserService.getUserRoleSetById(userId);
    }


    /**
     * Query user permission information
     * @param userId
     * @return
     */
    @GetMapping("/queryUserAuths")
    public Set<String> queryUserAuths(@RequestParam("userId") String userId){
        return sysUserService.getUserPermissionsSet(userId);
    }

    /**
     * 通过departmentid获取department全部信息
     */
    @GetMapping("/selectAllById")
    public SysDepartModel selectAllById(@RequestParam("id") String id){
        return sysBaseApi.selectAllById(id);
    }

    /**
     * According to useridQuery all users under the company to which the user belongsids
     * @param userId
     * @return
     */
    @GetMapping("/queryDeptUsersByUserId")
    public List<String> queryDeptUsersByUserId(@RequestParam("userId") String userId){
        return sysBaseApi.queryDeptUsersByUserId(userId);
    }


    /**
     * Query data permissions
     * @return
     */
    @GetMapping("/queryPermissionDataRule")
    public List<SysPermissionDataRuleModel> queryPermissionDataRule(@RequestParam("component") String component, @RequestParam("requestPath")String requestPath, @RequestParam("username") String username){
        return sysBaseApi.queryPermissionDataRule(component, requestPath, username);
    }

    /**
     * Query user information
     * @param username
     * @return
     */
    @GetMapping("/getCacheUser")
    public SysUserCacheInfo getCacheUser(@RequestParam("username") String username){
        return sysBaseApi.getCacheUser(username);
    }

    /**
     * General dictionary translation
     * @param code
     * @param key
     * @return
     */
    @GetMapping("/translateDict")
    public String translateDict(@RequestParam("code") String code, @RequestParam("key") String key){
        return sysBaseApi.translateDict(code, key);
    }


    /**
     * 36according to多个用户账号(comma separated)，查询return多个用户信息
     * @param usernames
     * @return
     */
    @RequestMapping("/queryUsersByUsernames")
    List<JSONObject> queryUsersByUsernames(@RequestParam("usernames") String usernames){
        return this.sysBaseApi.queryUsersByUsernames(usernames);
    }

    /**
     * 37according to多个用户id(comma separated)，查询return多个用户信息
     * @param ids
     * @return
     */
    @RequestMapping("/queryUsersByIds")
    List<JSONObject> queryUsersByIds(@RequestParam("ids") String ids){
        return this.sysBaseApi.queryUsersByIds(ids);
    }

    /**
     * 38according to多个department编码(comma separated)，查询return多个department信息
     * @param orgCodes
     * @return
     */
    @GetMapping("/queryDepartsByOrgcodes")
    List<JSONObject> queryDepartsByOrgcodes(@RequestParam("orgCodes") String orgCodes){
        return this.sysBaseApi.queryDepartsByOrgcodes(orgCodes);
    }

    /**
     * 39according to多个departmentID(comma separated)，查询return多个department信息
     * @param ids
     * @return
     */
    @GetMapping("/queryDepartsByIds")
    List<JSONObject> queryDepartsByIds(@RequestParam("ids") String ids){
        return this.sysBaseApi.queryDepartsByIds(ids);
    }

    /**
     * 40Send email message
     * @param email
     * @param title
     * @param content
     */
    @GetMapping("/sendEmailMsg")
    public void sendEmailMsg(@RequestParam("email")String email,@RequestParam("title")String title,@RequestParam("content")String content){
         this.sysBaseApi.sendEmailMsg(email,title,content);
    };
    /**
     * sendhtmlTemplate email message
     * @param email
     * @param title
     * @param emailTemplateEnum Email template enumeration
     * @param params            Template parameters
     */
    @GetMapping("/sendHtmlTemplateEmail")
    public void sendHtmlTemplateEmail(@RequestParam("email")String email, @RequestParam("title")String title, @RequestParam("emailEnum") EmailTemplateEnum emailTemplateEnum, @RequestParam("params") JSONObject params){
         this.sysBaseApi.sendHtmlTemplateEmail(email,title,emailTemplateEnum,params);
    };
    /**
     * send短信消息
     * @param phone  phone number
     * @param params  Template parameters
     * @param dySmsEnum SMS template enumeration
     */
    @GetMapping("/sendSmsMsg")
    public void sendSmsMsg(@RequestParam("phone")String phone, @RequestParam("params") JSONObject params, @RequestParam("dySmsEnum") DySmsEnum dySmsEnum){
         this.sysBaseApi.sendSmsMsg(phone,params,dySmsEnum);
    };
    /**
     * 41 获取公司下classdepartmentand公司下所有用户信息
     * @param orgCode
     */
    @GetMapping("/getDeptUserByOrgCode")
    List<Map> getDeptUserByOrgCode(@RequestParam("orgCode")String orgCode){
       return this.sysBaseApi.getDeptUserByOrgCode(orgCode);
    }

    /**
     * Query classification dictionary translation
     *
     * @param ids Classification dictionary tableid
     * @return
     */
    @GetMapping("/loadCategoryDictItem")
    public List<String> loadCategoryDictItem(@RequestParam("ids") String ids) {
        return sysBaseApi.loadCategoryDictItem(ids);
    }

    /**
     * Back translation classification dictionary，for import
     *
     * @param names name，Comma separated
     * @return
     */
    @GetMapping("/loadCategoryDictItemByNames")
    List<String> loadCategoryDictItemByNames(@RequestParam("names") String names, @RequestParam("delNotExist") boolean delNotExist) {
        return sysBaseApi.loadCategoryDictItemByNames(names, delNotExist);
    }

    /**
     * according todictionarycodeLoad dictionarytext
     *
     * @param dictCode order：tableName,text,code
     * @param keys     To inquirekey
     * @return
     */
    @GetMapping("/loadDictItem")
    public List<String> loadDictItem(@RequestParam("dictCode") String dictCode, @RequestParam("keys") String keys) {
        return sysBaseApi.loadDictItem(dictCode, keys);
    }

    /**
     * Copy all dictionary configurations under the application to the new tenant
     *
     * @param originalAppId Original low-code appID
     * @param appId         New low-code appsID
     * @param tenantId      new tenantID
     * @return Map<String, String>  Map<Original dictionary encoding, new dictionary encoding>
     */
    @GetMapping("/copyLowAppDict")
    Map<String, String> copyLowAppDict(@RequestParam("originalAppId") String originalAppId, @RequestParam("appId") String appId, @RequestParam("tenantId") String tenantId) {
        return sysBaseApi.copyLowAppDict(originalAppId, appId, tenantId);
    }
    
    /**
     * according todictionarycodeQuery dictionary item
     *
     * @param dictCode order：tableName,text,code
     * @param dictCode To inquirekey
     * @return
     */
    @GetMapping("/getDictItems")
    public List<DictModel> getDictItems(@RequestParam("dictCode") String dictCode) {
        return sysBaseApi.getDictItems(dictCode);
    }

    /**
     * according to多个dictionarycodeQuery multiple dictionary items
     *
     * @param dictCodeList
     * @return key = dictCode ； value=corresponding dictionary entry
     */
    @RequestMapping("/getManyDictItems")
    public Map<String, List<DictModel>> getManyDictItems(@RequestParam("dictCodeList") List<String> dictCodeList) {
        return sysBaseApi.getManyDictItems(dictCodeList);
    }

    /**
     * 【Drop down search】
     * Dictionary table with large data volume Go asynchronous loading，That is, the front-end input content filters the data
     *
     * @param dictCode dictionarycodeFormat：table,text,code
     * @param keyword  filter keywords
     * @return
     */
    @GetMapping("/loadDictItemByKeyword")
    public List<DictModel> loadDictItemByKeyword(@RequestParam("dictCode") String dictCode,
                                                 @RequestParam("keyword") String keyword,
                                                 @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo,
                                                 @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return sysBaseApi.loadDictItemByKeyword(dictCode, keyword,pageNo, pageSize);
    }

    /**
     * 48 General dictionary translation，according to多个dictCodeand multiple pieces of data，多个以Comma separated
     * @param dictCodes
     * @param keys
     * @return
     */
    @GetMapping("/translateManyDict")
    public Map<String, List<DictModel>> translateManyDict(@RequestParam("dictCodes") String dictCodes, @RequestParam("keys") String keys){
        return this.sysBaseApi.translateManyDict(dictCodes, keys);
    }


    /**
     * 获取表数据dictionary 【Interface signature verification】
     * @param tableFilterSql Table name can be broughtwherecondition
     * @param text
     * @param code
     * @return
     */
    @GetMapping("/queryTableDictItemsByCode")
    List<DictModel> queryTableDictItemsByCode(@RequestParam("tableFilterSql") String tableFilterSql, @RequestParam("text") String text, @RequestParam("code") String code){
        return sysBaseApi.queryTableDictItemsByCode(tableFilterSql, text, code);
    }

    /**
     * 查询表dictionary Support filtering data 【Interface signature verification】
     * @param table
     * @param text
     * @param code
     * @param filterSql
     * @return
     */
    @GetMapping("/queryFilterTableDictInfo")
    List<DictModel> queryFilterTableDictInfo(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("filterSql") String filterSql){
        return sysBaseApi.queryFilterTableDictInfo(table, text, code, filterSql);
    }

    /**
     * 【Interface signature verification】
     * Query specifiedtableof text code 获取dictionary，Includetextandvalue
     * @param table
     * @param text
     * @param code
     * @param keyArray
     * @return
     */
    @Deprecated
    @GetMapping("/queryTableDictByKeys")
    public List<String> queryTableDictByKeys(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("keyArray") String[] keyArray){
        return sysBaseApi.queryTableDictByKeys(table, text, code, keyArray);
    }


    /**
     * dictionary表of translate【Interface signature verification】
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    @GetMapping("/translateDictFromTable")
    public String translateDictFromTable(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("key") String key){
        return sysBaseApi.translateDictFromTable(table, text, code, key);
    }


    //update-begin---author:chenrui ---date:20231221  for：[issues/#5643]解决分布式下表dictionary跨库无法查询问题------------
    /**
     * 【Interface signature verification】
     * 49 dictionary表of translate，Available in batches
     *
     * @param table
     * @param text
     * @param code
     * @param keys  多个用Comma separated
     * @param ds data source
     * @return
     */
    @GetMapping("/translateDictFromTableByKeys")
    public List<DictModel> translateDictFromTableByKeys(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("keys") String keys, @RequestParam("ds")  String ds) {
        return this.sysBaseApi.translateDictFromTableByKeys(table, text, code, keys, ds);
    }
    //update-end---author:chenrui ---date:20231221  for：[issues/#5643]解决分布式下表dictionary跨库无法查询问题------------

    /**
     * send模板信息
     * @param message
     */
    @PostMapping("/sendTemplateMessage")
    public void sendTemplateMessage(@RequestBody MessageDTO message){
        sysBaseApi.sendTemplateMessage(message);
    }

    /**
     * Get message template content
     * @param code
     * @return
     */
    @GetMapping("/getTemplateContent")
    public String getTemplateContent(@RequestParam("code") String code){
        return this.sysBaseApi.getTemplateContent(code);
    }

    /**
     * Save data log
     * @param dataLogDto
     */
    @PostMapping("/saveDataLog")
    public void saveDataLog(@RequestBody DataLogDTO dataLogDto){
        this.sysBaseApi.saveDataLog(dataLogDto);
    }

    /**
     * Update avatar
     * @param loginUser
     * @return
     */
    @PutMapping("/updateAvatar")
    public void updateAvatar(@RequestBody LoginUser loginUser){
        this.sysBaseApi.updateAvatar(loginUser);
    }

    /**
     * Towardsappend websocketPush chat refresh message
     * @param userId
     * @return
     */
    @GetMapping("/sendAppChatSocket")
    public void sendAppChatSocket(@RequestParam(name="userId") String userId){
        this.sysBaseApi.sendAppChatSocket(userId);
    }

    /**
     * according toroleCodeQuery role information，可comma separated多个
     *
     * @param roleCodes
     * @return
     */
    @GetMapping("/queryRoleDictByCode")
    public List<DictModel> queryRoleDictByCode(@RequestParam(name = "roleCodes") String roleCodes) {
        return this.sysBaseApi.queryRoleDictByCode(roleCodes);
    }

    /**
     * Get message template content
     * @param id
     * @return
     */
    @GetMapping("/getRoleCode")
    public String getRoleCode(@RequestParam("id") String id){
        return this.sysBaseApi.getRoleCodeById(id);
    }
    
    /**
     * VUEN-2584【issue】platformsqlSeveral issues with injection vulnerabilities
     * Some special functions Query results can be mixed with error messages，导致数据库of信息暴露
     * @param e
     * @return
     */
    @ExceptionHandler(java.sql.SQLException.class)
    public Result<?> handleSQLException(Exception e){
        String msg = e.getMessage();
        String extractvalue = "extractvalue";
        String updatexml = "updatexml";
        if(msg!=null && (msg.toLowerCase().indexOf(extractvalue)>=0 || msg.toLowerCase().indexOf(updatexml)>=0)){
            return Result.error("Verification failed，sqlparsing exception！");
        }
        return Result.error("Verification failed，sqlparsing exception！" + msg);
    }

    /**
     * according to高class查询conditionQuery user
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/queryUserBySuperQuery")
    public List<JSONObject> queryUserBySuperQuery(@RequestParam("superQuery")  String superQuery, @RequestParam("matchType") String matchType) {
        return sysBaseApi.queryUserBySuperQuery(superQuery,matchType);
    }

    /**
     * according toidconditionQuery user
     * @param id
     * @return
     */
    @GetMapping("/queryUserById")
    public JSONObject queryUserById(@RequestParam("id")  String id) {
        return sysBaseApi.queryUserById(id);
    }

    /**
     * according to高class查询condition查询department
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/queryDeptBySuperQuery")
    public List<JSONObject> queryDeptBySuperQuery(@RequestParam("superQuery")  String superQuery, @RequestParam("matchType") String matchType) {
        return sysBaseApi.queryDeptBySuperQuery(superQuery,matchType);
    }

    /**
     * according to高class查询condition查询角色
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/queryRoleBySuperQuery")
    public List<JSONObject> queryRoleBySuperQuery(@RequestParam("superQuery")  String superQuery, @RequestParam("matchType") String matchType) {
        return sysBaseApi.queryRoleBySuperQuery(superQuery,matchType);
    }


    /**
     * according totenantIDQuery userID
     * @param tenantId tenantID
     * @return List<String>
     */
    @GetMapping("/selectUserIdByTenantId")
    public List<String> selectUserIdByTenantId(@RequestParam("tenantId")  String tenantId) {
        return sysBaseApi.selectUserIdByTenantId(tenantId);
    }


    /**
     * according todepartmentIDQuery userID
     * @param deptIds
     * @return
     */
    @GetMapping("/queryUserIdsByDeptIds")
    public List<String> queryUserIdsByDeptIds(@RequestParam("deptIds") List<String> deptIds){
        return sysBaseApi.queryUserIdsByDeptIds(deptIds);
    }
    
    /**
     * according todepartmentIDQuery userID
     * @param deptIds
     * @return
     */
    @GetMapping("/queryUserAccountsByDeptIds")
    public List<String> queryUserAccountsByDeptIds(@RequestParam("deptIds") List<String> deptIds){
        return sysBaseApi.queryUserAccountsByDeptIds(deptIds);
    }

    /**
     * according to角色编码 Query userID
     * @param roleCodes
     * @return
     */
    @GetMapping("/queryUserIdsByRoleds")
    public List<String> queryUserIdsByRoleds(@RequestParam("roleCodes")  List<String> roleCodes){
        return sysBaseApi.queryUserIdsByRoleds(roleCodes);
    }

    /**
     * according to职务IDQuery userID
     * @param positionIds
     * @return
     */
    @GetMapping("/queryUserIdsByPositionIds")
    public List<String> queryUserIdsByPositionIds(@RequestParam("positionIds") List<String> positionIds){
        return sysBaseApi.queryUserIdsByPositionIds(positionIds);
    }


    /**
     * according todepartmentand子department下of所有用户账号
     *
     * @param orgCode department编码
     * @return
     */
    @GetMapping("/getUserAccountsByDepCode")
    public List<String> getUserAccountsByDepCode(@RequestParam("orgCode") String orgCode){
        return sysBaseApi.getUserAccountsByDepCode(orgCode);
    }

    /**
     * check querysqlof表and字段是否exist白名单中
     *
     * @param selectSql
     * @return
     */
    @GetMapping("/dictTableWhiteListCheckBySql")
    public boolean dictTableWhiteListCheckBySql(@RequestParam("selectSql") String selectSql) {
        return sysBaseApi.dictTableWhiteListCheckBySql(selectSql);
    }

    /**
     * according todictionary表ordictionary编码，Check whether it is in the whitelist
     *
     * @param tableOrDictCode table name ordictCode
     * @param fields          如果传of是dictCode，Then this parameter must be passednull
     * @return
     */
    @GetMapping("/dictTableWhiteListCheckByDict")
    public boolean dictTableWhiteListCheckByDict(
            @RequestParam("tableOrDictCode") String tableOrDictCode,
            @RequestParam(value = "fields", required = false) String... fields
    ) {
        return sysBaseApi.dictTableWhiteListCheckByDict(tableOrDictCode, fields);
    }
    /**
     * Automatically publish announcements
     *
     * @param dataId noticeID
     * @param currentUserName send人
     * @return
     */
    @GetMapping("/announcementAutoRelease")
    public void announcementAutoRelease(
            @RequestParam("dataId") String dataId,
            @RequestParam(value = "currentUserName", required = false) String currentUserName
    ) {
       sysBaseApi.announcementAutoRelease(dataId, currentUserName);
    }

    /**
     * according todepartment编码查询公司信息
     * @param orgCode department编码
     * @return
     * @author chenrui
     * @date 2025/8/12 14:45
     */
    @GetMapping(value = "/queryCompByOrgCode")
    SysDepartModel queryCompByOrgCode(@RequestParam(name = "sysCode") String orgCode) {
        return sysBaseApi.queryCompByOrgCode(orgCode);
    }

    /**
     * according todepartment编码and层次查询上class公司
     *
     * @param orgCode department编码
     * @param level Can pass empty Default is1class The minimum value is1
     * @return
     */
    @GetMapping(value = "/queryCompByOrgCodeAndLevel")
    SysDepartModel queryCompByOrgCodeAndLevel(@RequestParam("orgCode") String orgCode, @RequestParam("level") Integer level){
        return sysBaseApi.queryCompByOrgCodeAndLevel(orgCode,level);
    }

    /**
     * runAIRagprocess
     * for  [QQYUN-13634]existbaseapiEncapsulation method inside，方便其他module调用
     * @param airagFlowDTO
     * @return process执行结果,may beStringorMap
     * @return
     */
    @PostMapping(value = "/runAiragFlow")
    Object runAiragFlow(@RequestBody AiragFlowDTO airagFlowDTO) {
        return sysBaseApi.runAiragFlow(airagFlowDTO);
    }

    /**
     * according todepartmentcode或departmentid获取departmentname(当前and上classdepartment)
     *
     * @param orgCode department编码
     * @param depId departmentid
     * @return String departmentname
     */
    @GetMapping(value = "/getDepartPathNameByOrgCode")
    String getDepartPathNameByOrgCode(@RequestParam(name = "orgCode", required = false) String orgCode, @RequestParam(name = "depId", required = false) String depId) {
        return sysBaseApi.getDepartPathNameByOrgCode(orgCode, depId);
    }

    /**
     * according todepartmentIDQuery userID
     * @param deptIds
     * @return
     */
    @GetMapping("/queryUserIdsByCascadeDeptIds")
    public List<String> queryUserIdsByCascadeDeptIds(@RequestParam("deptIds") List<String> deptIds){
        return sysBaseApi.queryUserIdsByCascadeDeptIds(deptIds);
    }
}
