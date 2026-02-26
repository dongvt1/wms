package org.jeecg.common.system.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.constant.ServiceNameConstants;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.constant.enums.EmailTemplateEnum;
import org.jeecg.common.desensitization.annotation.SensitiveDecode;
import org.jeecg.common.system.api.factory.SysBaseAPIFallbackFactory;
import org.jeecg.common.system.vo.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * 1、cloudNumber of interfaces43  local：35 common：9  extra specialqueryAllRoleUse one as two
 *  - comparelocalversion
 *  - Removed some methods：addLog、getDatabaseType、queryAllDepart、queryAllUser(Wrapper wrapper)、queryAllUser(String[] userIds, int pageNo, int pageSize)
 *  - Modified some methods：createLog、sendSysAnnouncement（Only one remains，Kill the rest）
 * 2、@ConditionalOnMissingClass("org.jeecg.modules.system.service.impl.SysBaseApiImpl")=> When there is an implementation class，Not instantiatedFeigninterface
 * @author: jeecg-boot
 */
@Component
@FeignClient(contextId = "sysBaseRemoteApi", value = ServiceNameConstants.SERVICE_SYSTEM, fallbackFactory = SysBaseAPIFallbackFactory.class)
@ConditionalOnMissingClass("org.jeecg.modules.system.service.impl.SysBaseApiImpl")
public interface ISysBaseAPI extends CommonAPI {

    /**
     * 1Send system message
     * @param message Assign parameters using constructor If not setcategory(Message type)The default is2 Send system message
     */
    @PostMapping("/sys/api/sendSysAnnouncement")
    void sendSysAnnouncement(@RequestBody MessageDTO message);

    /**
     * 2Send message With business parameters
     * @param message Assign parameters using constructor
     */
    @PostMapping("/sys/api/sendBusAnnouncement")
    void sendBusAnnouncement(@RequestBody BusMessageDTO message);

    /**
     * 3通过模板Send message
     * @param message Assign parameters using constructor
     */
    @PostMapping("/sys/api/sendTemplateAnnouncement")
    void sendTemplateAnnouncement(@RequestBody TemplateMessageDTO message);

    /**
     * 4通过模板Send message With business parameters
     * @param message Assign parameters using constructor
     */
    @PostMapping("/sys/api/sendBusTemplateAnnouncement")
    void sendBusTemplateAnnouncement(@RequestBody BusTemplateMessageDTO message);

    /**
     * 5Via message center template，Generate push content
     * @param templateDTO Assign parameters using constructor
     * @return
     */
    @PostMapping("/sys/api/parseTemplateByCode")
    String parseTemplateByCode(@RequestBody TemplateDTO templateDTO);

    /**
     * 6According to useridQuery user information
     * @param id
     * @return
     */
    @SensitiveDecode
    @GetMapping("/sys/api/getUserById")
    LoginUser getUserById(@RequestParam("id") String id);

    /**
     * 7Query role collection by user account
     * @param username
     * @return
     */
    @GetMapping("/sys/api/getRolesByUsername")
    List<String> getRolesByUsername(@RequestParam("username") String username);
    
    /**
     * 7Query role collection by user account
     * @param userId
     * @return
     */
    @GetMapping("/sys/api/getRolesByUserId")
    List<String> getRolesByUserId(@RequestParam("userId") String userId);

    /**
     * 8Query department collection through user account
     * @param username
     * @return department id
     */
    @GetMapping("/sys/api/getDepartIdsByUsername")
    List<String> getDepartIdsByUsername(@RequestParam("username") String username);
    
    /**
     * 8Query department collection through user account
     * @param userId
     * @return department id
     */
    @GetMapping("/sys/api/getDepartIdsByUserId")
    List<String> getDepartIdsByUserId(@RequestParam("userId") String userId);

    /**
     * 8.2 通过user账号查询department父IDgather
     * @param username
     * @return department parentIds
     */
    @GetMapping("/sys/api/getDepartParentIdsByUsername")
    Set<String> getDepartParentIdsByUsername(@RequestParam("username")String username);

    /**
     * 8.3 查询department父IDgather
     * @param depIds
     * @return department parentIds
     */
    @GetMapping("/sys/api/getDepartParentIdsByDepIds")
    Set<String> getDepartParentIdsByDepIds(@RequestParam("depIds") Set<String> depIds);
    
    /**
     * 9通过user账号查询department name
     * @param username
     * @return department name
     */
    @GetMapping("/sys/api/getDepartNamesByUsername")
    List<String> getDepartNamesByUsername(@RequestParam("username") String username);

    /**
     * 10Get data dictionary
     * @param code
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryDictItemsByCode")
    List<DictModel> queryDictItemsByCode(@RequestParam("code") String code);

    /**
     * Get valid data dictionary entries
     * @param code
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryEnableDictItemsByCode")
    public List<DictModel> queryEnableDictItemsByCode(@RequestParam("code") String code);

    /** 11Query all parent dictionaries，according tocreate_timesort
     * @return List<DictModel> dictionary值gather
     */
    @GetMapping("/sys/api/queryAllDict")
    List<DictModel> queryAllDict();

    /**
     * 12Query all classification dictionaries
     * @return
     */
    @GetMapping("/sys/api/queryAllSysCategory")
    List<SysCategoryModel> queryAllSysCategory();

    /**
     * 13Get table data dictionary
     * @param tableFilterSql
     * @param text
     * @param code
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryTableDictItemsByCode")
    List<DictModel> queryTableDictItemsByCode(@RequestParam("tableFilterSql") String tableFilterSql, @RequestParam("text") String text, @RequestParam("code") String code);

    /**
     * 14查询所有department as dictionary information id -->value,departName -->text
     * @return
     */
    @GetMapping("/sys/api/queryAllDepartBackDictModel")
    List<DictModel> queryAllDepartBackDictModel();

    /**
     * 15According to business type busType and business busId Modify message read
     * @param busType Business type
     * @param busId businessid
     */
    @GetMapping("/sys/api/updateSysAnnounReadFlag")
    public void updateSysAnnounReadFlag(@RequestParam("busType") String busType, @RequestParam("busId")String busId);

    /**
     * 16lookup table dictionary Support filtering data
     * @param table
     * @param text
     * @param code
     * @param filterSql
     * @return
     */
    @GetMapping("/sys/api/queryFilterTableDictInfo")
    List<DictModel> queryFilterTableDictInfo(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("filterSql") String filterSql);

    /**
     * 17Query specifiedtableof text code Get dictionary，Includetextandvalue
     * @param table
     * @param text
     * @param code
     * @param keyArray
     * @return
     */
    @Deprecated
    @GetMapping("/sys/api/queryTableDictByKeys")
    public List<String> queryTableDictByKeys(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("keyArray") String[] keyArray);

    /**
     * 18Query all users returnComboModel
     * @return
     */
    @GetMapping("/sys/api/queryAllUserBackCombo")
    public List<ComboModel> queryAllUserBackCombo();

    /**
     * 19Query users by page returnJSONObject
     * @param userIds multiple usersid
     * @param pageNo Current page number
     * @param pageSize Number of items per page
     * @return
     */
    @GetMapping("/sys/api/queryAllUser")
    public JSONObject queryAllUser(@RequestParam(name="userIds",required=false)String userIds, @RequestParam(name="pageNo",required=false) Integer pageNo,@RequestParam(name="pageSize",required=false) Integer pageSize);


    /**
     * 20Get all roles With ginseng
     * @param roleIds Role selected by default
     * @return
     */
    @GetMapping("/sys/api/queryAllRole")
    public List<ComboModel> queryAllRole(@RequestParam(name = "roleIds",required = false)String[] roleIds);

    /**
     * 21Query roles by user accountIdgather
     * @param username
     * @return
     */
    @GetMapping("/sys/api/getRoleIdsByUsername")
    public List<String> getRoleIdsByUsername(@RequestParam("username")String username);

    /**
     * 22通过department编号查询departmentid
     * @param orgCode
     * @return
     */
    @GetMapping("/sys/api/getDepartIdsByOrgCode")
    public String getDepartIdsByOrgCode(@RequestParam("orgCode")String orgCode);

    /**
     * 23查询所有department
     * @return
     */
    @GetMapping("/sys/api/getAllSysDepart")
    public List<SysDepartModel> getAllSysDepart();

    /**
     * 24查找父classdepartment
     * @param departId
     * @return
     */
    @GetMapping("/sys/api/getParentDepartId")
    DictModel getParentDepartId(@RequestParam("departId")String departId);

    /**
     * 25according todepartmentId获取department负责人
     * @param deptId
     * @return
     */
    @GetMapping("/sys/api/getDeptHeadByDepId")
    public List<String> getDeptHeadByDepId(@RequestParam("deptId") String deptId);

    /**
     * 26Send message to specified user
     * @param userIds
     * @param cmd
     */
    @GetMapping("/sys/api/sendWebSocketMsg")
    public void sendWebSocketMsg(@RequestParam("userIds")String[] userIds, @RequestParam("cmd") String cmd);

    /**
     * 27according toidGet all participating users
     * @param userIds multiple usersid
     * @return
     */
    @GetMapping("/sys/api/queryAllUserByIds")
    public List<UserAccountInfo> queryAllUserByIds(@RequestParam("userIds") String[] userIds);

    /**
     * 28Push meeting check-in information to preview
     * userIds
     * @return
     * @param userId
     */
    @GetMapping("/sys/api/meetingSignWebsocket")
    void meetingSignWebsocket(@RequestParam("userId")String userId);

    /**
     * 29according tonameGet all participating users
     * @param userNames multiple users账号
     * @return
     */
    @GetMapping("/sys/api/queryUserByNames")
    List<UserAccountInfo> queryUserByNames(@RequestParam("userNames")String[] userNames);


    /**
     * 30获取userof角色gather
     * @param username
     * @return
     */
    @GetMapping("/sys/api/getUserRoleSet")
    Set<String> getUserRoleSet(@RequestParam("username")String username);
    
    /**
     * 30获取userof角色gather
     * @param userId
     * @return
     */
    @GetMapping("/sys/api/getUserRoleSetById")
    Set<String> getUserRoleSetById(@RequestParam("userId")String userId);

    /**
     * 31获取userof权限gather
     * @param userId
     * @return
     */
    @GetMapping("/sys/api/getUserPermissionSet")
    Set<String> getUserPermissionSet(@RequestParam("userId") String userId);

    /**
     * 32Determine whether there isonline访问of权限
     * @param onlineAuthDTO
     * @return
     */
    @PostMapping("/sys/api/hasOnlineAuth")
    boolean hasOnlineAuth(@RequestBody OnlineAuthDTO onlineAuthDTO);

    /**
     * 33通过departmentid获取department全部信息
     * @param id departmentid
     * @return SysDepartModel department信息
     */
    @GetMapping("/sys/api/selectAllById")
    SysDepartModel selectAllById(@RequestParam("id") String id);

    /**
     * 34According to useridQuery all users under the company to which the user belongsids
     * @param userId
     * @return
     */
    @GetMapping("/sys/api/queryDeptUsersByUserId")
    List<String> queryDeptUsersByUserId(@RequestParam("userId") String userId);


    //---

    /**
     * 35Query user role information
     * @param username
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryUserRoles")
    Set<String> queryUserRoles(@RequestParam("username")String username);
    
    /**
     * 35Query user role information
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryUserRolesById")
    Set<String> queryUserRolesById(@RequestParam("userId")String userId);

    /**
     * 36Query user permission information
     * @param userId
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryUserAuths")
    Set<String> queryUserAuths(@RequestParam("userId")String userId);

    /**
     * 37according to id 查询数据库中存储of DynamicDataSourceModel
     *
     * @param dbSourceId
     * @return
     */
    @Override
    @GetMapping("/sys/api/getDynamicDbSourceById")
    DynamicDataSourceModel getDynamicDbSourceById(@RequestParam("dbSourceId") String dbSourceId);

    /**
     * 38according to code 查询数据库中存储of DynamicDataSourceModel
     *
     * @param dbSourceCode
     * @return
     */
    @Override
    @GetMapping("/sys/api/getDynamicDbSourceByCode")
    DynamicDataSourceModel getDynamicDbSourceByCode(@RequestParam("dbSourceCode") String dbSourceCode);

    /**
     * 39According to user账号Query user information CommonAPIin definition
     * @param username
     * @return LoginUser User information
     */
    @Override
    @SensitiveDecode
    @GetMapping("/sys/api/getUserByName")
    LoginUser getUserByName(@RequestParam("username") String username);
    
    /**
     * 39According to user账号查询userID CommonAPIin definition
     * @param username
     * @return userID
     */
    @Override
    @GetMapping("/sys/api/getUserIdByName")
    String getUserIdByName(@RequestParam("username") String username);

    /**
     * 40dictionary表of translate
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    @Override
    @GetMapping("/sys/api/translateDictFromTable")
    String translateDictFromTable(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("key") String key);

    /**
     * 41普通dictionaryoftranslate
     * @param code
     * @param key
     * @return
     */
    @Override
    @GetMapping("/sys/api/translateDict")
    String translateDict(@RequestParam("code") String code, @RequestParam("key") String key);

    /**
     * 42Query data permissions
     * @param component
     * @param requestPath
     * @param username user姓名
     * @return
     */
    @Override
    @GetMapping("/sys/api/queryPermissionDataRule")
    List<SysPermissionDataRuleModel> queryPermissionDataRule(@RequestParam("component") String component, @RequestParam("requestPath")String requestPath, @RequestParam("username") String username);

    /**
     * 43Query user information
     * @param username
     * @return
     */
    @Override
    @GetMapping("/sys/api/getCacheUser")
    SysUserCacheInfo getCacheUser(@RequestParam("username") String username);

    /**
     * 36according tomultiple users账号(comma separated)，查询returnmultiple users信息
     * @param usernames
     * @return
     */
    @GetMapping("/sys/api/queryUsersByUsernames")
    List<JSONObject> queryUsersByUsernames(@RequestParam("usernames") String usernames);

    /**
     * 37according tomultiple usersID(comma separated)，查询returnmultiple users信息
     * @param ids
     * @return
     */
    @RequestMapping("/sys/api/queryUsersByIds")
    List<JSONObject> queryUsersByIds(@RequestParam("ids") String ids);

    /**
     * 38according to多个department编码(comma separated)，查询return多个department信息
     * @param orgCodes
     * @return
     */
    @RequestMapping("/sys/api/queryDepartsByOrgcodes")
    List<JSONObject> queryDepartsByOrgcodes(@RequestParam("orgCodes") String orgCodes);

//    /**
//     * 39according to多个department编码(comma separated)，查询return多个department信息
//     * @param ids
//     * @return
//     */
//    @GetMapping("/sys/api/queryDepartsByOrgIds")
//    List<JSONObject> queryDepartsByOrgIds(@RequestParam("ids") String ids);
    
    /**
     * 40Send email message
     * @param email
     * @param title
     * @param content
     */
    @GetMapping("/sys/api/sendEmailMsg")
    void sendEmailMsg(@RequestParam("email")String email,@RequestParam("title")String title,@RequestParam("content")String content);

    /**
     * sendhtml模version邮件消息
     *
     * @param email
     * @param title
     * @param emailTemplateEnum 邮件模version枚举
     * @param params            模version参数
     */
    @GetMapping("/sys/api/sendHtmlTemplateEmail")
    void sendHtmlTemplateEmail(@RequestParam("email") String email, @RequestParam("title") String title, @RequestParam("emailEnum") EmailTemplateEnum emailTemplateEnum, @RequestParam("params") JSONObject params);
    /**
    /**
     * send短信消息
     *
     * @param phone  phone number
     * @param params  模version参数
     * @param dySmsEnum 短信模version枚举
     */
    @GetMapping("/sys/api/sendSmsMsg")
    void sendSmsMsg(@RequestParam("phone") String phone, @RequestParam("params") JSONObject params,@RequestParam("dySmsEnum") DySmsEnum dySmsEnum);
    /**
     * 41 获取公司下classdepartmentand公司下所有userid
     * @param orgCode department编号
     * @return List<Map>
     */
    @GetMapping("/sys/api/getDeptUserByOrgCode")
    List<Map> getDeptUserByOrgCode(@RequestParam("orgCode")String orgCode);

    /**
     * 42 查询分类dictionarytranslate
     * @param ids Multiple classification dictionariesid
     * @return List<String>
     */
    @GetMapping("/sys/api/loadCategoryDictItem")
    List<String> loadCategoryDictItem(@RequestParam("ids") String ids);

    /**
     * 44 反向translate分类dictionary，for import
     *
     * @param names name，Comma separated
     */
    @GetMapping("/sys/api/loadCategoryDictItemByNames")
    List<String> loadCategoryDictItemByNames(@RequestParam("names") String names, @RequestParam("delNotExist") boolean delNotExist);

    /**
     * 43 according todictionarycodeLoad dictionarytext
     *
     * @param dictCode order：tableName,text,code
     * @param keys     要查询ofkey
     * @return
     */
    @GetMapping("/sys/api/loadDictItem")
    List<String> loadDictItem(@RequestParam("dictCode") String dictCode, @RequestParam("keys") String keys);

    /**
     * 复制应用下of所有dictionary配置到新oftenant下
     *
     * @param originalAppId  Original low-code appID
     * @param appId   新of低代码应用ID
     * @param tenantId  新oftenantID
     * @return  Map<String, String>  Map<Original dictionary encoding, new dictionary encoding> 
     */
    @GetMapping("/sys/api/copyLowAppDict")
    Map<String, String> copyLowAppDict(@RequestParam("originalAppId") String originalAppId, @RequestParam("appId") String appId, @RequestParam("tenantId") String tenantId);
    
    /**
     * 44 according todictionarycodeQuery dictionary item
     *
     * @param dictCode order：tableName,text,code
     * @param dictCode 要查询ofkey
     * @return
     */
    @GetMapping("/sys/api/getDictItems")
    List<DictModel> getDictItems(@RequestParam("dictCode") String dictCode);

    /**
     * 45 according to多个dictionarycodeQuery multiple dictionary items
     *
     * @param dictCodeList
     * @return key = dictCode ； value=对应ofdictionary项
     */
    @RequestMapping("/sys/api/getManyDictItems")
    Map<String, List<DictModel>> getManyDictItems(@RequestParam("dictCodeList") List<String> dictCodeList);

    /**
     * 46 【JSearchSelectTag下拉搜索组件专用interface】
     * 大数据量ofdictionary表 Go asynchronous loading  That is, the front-end input content filters the data
     *
     * @param dictCode dictionarycodeFormat：table,text,code
     * @param keyword  filter keywords
     * @param pageSize Number of items per page
     * @return
     */
    @GetMapping("/sys/api/loadDictItemByKeyword")
    List<DictModel> loadDictItemByKeyword(@RequestParam("dictCode") String dictCode, @RequestParam("keyword") String keyword, @RequestParam(value = "pageNo", defaultValue = "1", required = false) Integer pageNo, @RequestParam(value = "pageSize", required = false) Integer pageSize);

    /**
     * 47 according to多个departmentid(comma separated)，查询return多个department信息
     * @param ids
     * @return
     */
    @GetMapping("/sys/api/queryDepartsByIds")
    List<JSONObject> queryDepartsByIds(@RequestParam("ids") String ids);

    /**
     * 48 普通dictionaryoftranslate，according to多个dictCodeand多条数据，多个以Comma separated
     * @param dictCodes
     * @param keys
     * @return
     */
    @Override
    @GetMapping("/sys/api/translateManyDict")
    Map<String, List<DictModel>> translateManyDict(@RequestParam("dictCodes") String dictCodes, @RequestParam("keys") String keys);

    //update-begin---author:chenrui ---date:20231221  for：[issues/#5643]解决分布式下表dictionary跨库无法查询问题------------
    /**
     * 49 dictionary表of translate，Available in batches
     * @param table
     * @param text
     * @param code
     * @param keys 多个用Comma separated
     * @param ds
     * @return
     */
    @Override
    @GetMapping("/sys/api/translateDictFromTableByKeys")
    List<DictModel> translateDictFromTableByKeys(@RequestParam("table") String table, @RequestParam("text") String text, @RequestParam("code") String code, @RequestParam("keys") String keys, @RequestParam("ds") String ds);
    //update-end---author:chenrui ---date:20231221  for：[issues/#5643]解决分布式下表dictionary跨库无法查询问题------------

    /**
     * send模板消息
     */
    @PostMapping("/sys/api/sendTemplateMessage")
    void sendTemplateMessage(@RequestBody MessageDTO message);

    /**
     * Get template content
     * @param code
     * @return
     */
    @GetMapping("/sys/api/getTemplateContent")
    String getTemplateContent(@RequestParam("code") String code);

    /**
     * Add data log
     * @param dataLogDto
     */
    @PostMapping("/sys/api/saveDataLog")
    void saveDataLog(@RequestBody DataLogDTO dataLogDto);

    /**
     * Update avatar
     * @param loginUser
     * @return
     */
    @PutMapping("/sys/api/updateAvatar")
    void updateAvatar(@RequestBody LoginUser loginUser);

    @GetMapping("/sys/api/sendAppChatSocket")
    void sendAppChatSocket(@RequestParam(name="userId") String userId);

    /**
     * according to角色idQuery rolecode
     * @param id
     * @return
     */
    @GetMapping("/sys/api/getRoleCode")
    String getRoleCodeById(@RequestParam(name = "id") String id);

    /**
     * according toroleCodeQuery role信息，可comma separated多个
     *
     * @param roleCodes
     * @return
     */
    @GetMapping("/sys/api/queryRoleDictByCode")
    List<DictModel> queryRoleDictByCode(@RequestParam(name = "roleCodes") String roleCodes);


    /**
     * according to高class查询条件查询user
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/sys/api/queryUserBySuperQuery")
    List<JSONObject> queryUserBySuperQuery(@RequestParam(name="superQuery")String superQuery,@RequestParam(name="matchType")String matchType);


    /**
     * according toID条件查询user
     * @param id
     * @return JSONObject
     */
    @GetMapping("/sys/api/queryUserById")
    JSONObject queryUserById(@RequestParam(name="id") String id);


    /**
     * according to高class查询条件查询department
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/sys/api/queryDeptBySuperQuery")
    List<JSONObject> queryDeptBySuperQuery(@RequestParam(name="superQuery")String superQuery,@RequestParam(name="matchType")String matchType);

    /**
     * according to高class查询条件Query role
     * @param superQuery
     * @param matchType
     * @return
     */
    @GetMapping("/sys/api/queryRoleBySuperQuery")
    List<JSONObject> queryRoleBySuperQuery(@RequestParam(name="superQuery")String superQuery,@RequestParam(name="matchType")String matchType);


    /**
     * according totenantID查询userID
     * @param tenantId tenantID
     * @return List<String>
     */
    @GetMapping("/sys/api/selectUserIdByTenantId")
    List<String> selectUserIdByTenantId(@RequestParam("tenantId")String tenantId);


    /**
     * according todepartmentID查询userID
     * @param deptIds
     * @return
     */
    @GetMapping("/sys/api/queryUserIdsByDeptIds")
    List<String> queryUserIdsByDeptIds(List<String> deptIds);

    /**
     * according todepartmentID查询user账号
     * @param deptIds
     * @return
     */
    @GetMapping("/sys/api/queryUserAccountsByDeptIds")
    List<String> queryUserAccountsByDeptIds(List<String> deptIds);
    
    /**
     * according to角色编码 查询userID
     * @param roleCodes
     * @return
     */
    @GetMapping("/sys/api/queryUserIdsByRoleds")
    List<String> queryUserIdsByRoleds(List<String> roleCodes);

    /**
     * according to职务ID查询userID
     * @param positionIds
     * @return
     */
    @GetMapping("/sys/api/queryUserIdsByPositionIds")
    List<String> queryUserIdsByPositionIds(@RequestParam("positionIds") List<String> positionIds);

    /**
     * according todepartmentand子department下of所有user账号
     *
     * @param orgCode department编码
     * @return
     */
    @GetMapping("/sys/api/getUserAccountsByDepCode")
    public List<String> getUserAccountsByDepCode(@RequestParam("orgCode")String orgCode);

    /**
     * check querysqlof表and字段是否exist白名单中
     *
     * @param selectSql
     * @return
     */
    @GetMapping("/sys/api/dictTableWhiteListCheckBySql")
    boolean dictTableWhiteListCheckBySql(@RequestParam("selectSql") String selectSql);

    /**
     * according todictionary表ordictionary编码，Check whether it is in the whitelist
     *
     * @param tableOrDictCode table name ordictCode
     * @param fields          如果传of是dictCode，Then this parameter must be passednull
     * @return
     */
    @GetMapping("/sys/api/dictTableWhiteListCheckByDict")
    boolean dictTableWhiteListCheckByDict(
            @RequestParam("tableOrDictCode") String tableOrDictCode,
            @RequestParam(value = "fields", required = false) String... fields
    );
    /**
     * Automatically publish announcements
     *
     * @param dataId noticeID
     * @param currentUserName send人
     * @return
     */
    @GetMapping("/sys/api/announcementAutoRelease")
    void announcementAutoRelease(
            @RequestParam("dataId") String dataId,
            @RequestParam(value = "currentUserName") String currentUserName
    );

    /**
     * according todepartment编码查询公司信息
     * @param orgCode department编码
     * @return
     * @author chenrui
     * @date 2025/8/12 14:45
     */
    @GetMapping(value = "/sys/api/queryCompByOrgCode")
    SysDepartModel queryCompByOrgCode(@RequestParam(name = "sysCode") String orgCode);

    /**
     * according todepartment编码and层次查询上class公司
     *
     * @param orgCode department编码
     * @param level Can pass empty Default is1class The minimum value is1
     * @return
     */
    @GetMapping(value = "/sys/api/queryCompByOrgCodeAndLevel")
    SysDepartModel queryCompByOrgCodeAndLevel(@RequestParam("orgCode") String orgCode, @RequestParam("level") Integer level);

    /**
     * 16 runAIRagprocess
     * for  [QQYUN-13634]existbaseapiEncapsulation method inside，Convenient for calling other modules
     *
     * @param airagFlowDTO
     * @return process执行结果,may beStringorMap
     * @author chenrui
     * @date 2025/9/2 11:43
     */
    @PostMapping(value = "/sys/api/runAiragFlow")
    Object runAiragFlow(@RequestBody AiragFlowDTO airagFlowDTO);

    /**
     * according todepartmentcode或departmentid获取departmentname(当前and上classdepartment)
     *
     * @param orgCode department编码
     * @param depId   departmentid
     * @return String departmentname
     */
    @GetMapping("/getDepartPathNameByOrgCode")
    String getDepartPathNameByOrgCode(@RequestParam(name = "orgCode", required = false) String orgCode, @RequestParam(name = "depId", required = false) String depId);


    /**
     * according todepartmentID查询department及其子department下userID <br/>
     * @param deptIds
     * @return
     * @author chenrui
     * @date 2025/09/08 15:28
     */
    @GetMapping("/sys/api/queryUserIdsByCascadeDeptIds")
    List<String> queryUserIdsByCascadeDeptIds(@RequestParam("deptIds") List<String> deptIds);
}
