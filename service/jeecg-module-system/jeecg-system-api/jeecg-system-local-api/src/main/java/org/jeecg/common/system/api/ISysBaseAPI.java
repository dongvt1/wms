package org.jeecg.common.system.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.constant.enums.EmailTemplateEnum;
import org.jeecg.common.system.vo.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description  Common underlying servicesAPI，Provide other independent module calls
 * @Author  scott
 * @Date 2019-4-20
 * @Version V1.0
 */
public interface ISysBaseAPI extends CommonAPI {

    //=======OLD System message push interface============================
    /**
     * 1Send system message
     * @param message Assign parameters using constructor If not setcategory(Message type)The default is2 Send system message
     */
    void sendSysAnnouncement(MessageDTO message);

    /**
     * 2Send message With business parameters
     * @param message Assign parameters using constructor
     */
    void sendBusAnnouncement(BusMessageDTO message);

    /**
     * 3通过模板Send message
     * @param message Assign parameters using constructor
     */
    void sendTemplateAnnouncement(TemplateMessageDTO message);

    /**
     * 4通过模板Send message With business parameters
     * @param message Assign parameters using constructor
     */
    void sendBusTemplateAnnouncement(BusTemplateMessageDTO message);
    
    /**
     * 5Via message center template，Generate push content
     * @param templateDTO Assign parameters using constructor
     * @return
     */
    String parseTemplateByCode(TemplateDTO templateDTO);
    //=======OLD System message push interface============================

    //=======TY NEW Custom message push interface，mail、DingTalk、Enterprise WeChat、System messages============================
    /**
     * NEWSend template message【new，Support custom push types: mail、DingTalk、Enterprise WeChat、System messages】
     * @param message
     */
    void sendTemplateMessage(MessageDTO message);

    /**
     * NEWGet template content based on template encoding【new，Support custom push types】
     * @param templateCode
     * @return
     */
    String getTemplateContent(String templateCode);
    //=======TY NEW Custom message push interface，mail、DingTalk、Enterprise WeChat、System messages============================
    
    /**
     * 6According to useridQuery user information
     * @param id
     * @return
     */
    LoginUser getUserById(String id);

    /**
     * 7Query role collection by user account
     * @param username
     * @return
     */
    List<String> getRolesByUsername(String username);
    
    /**
     * 7Query role collection by user account
     * @param userId
     * @return
     */
    List<String> getRolesByUserId(String userId);

    /**
     * 8Query department collection through user account
     * @param username
     * @return department id
     */
    List<String> getDepartIdsByUsername(String username);
    /**
     * 8Query department collection through user account
     * @param userId
     * @return department id
     */
    List<String> getDepartIdsByUserId(String userId);

    /**
     * 8.2 通过用户账号查询department父IDgather
     * @param username
     * @return department parentIds
     */
    Set<String> getDepartParentIdsByUsername(String username);

    /**
     * 8.2 查询department父IDgather
     * @param depIds
     * @return department parentIds
     */
    Set<String> getDepartParentIdsByDepIds(Set<String> depIds);

    /**
     * 9通过用户账号查询department name
     * @param username
     * @return department name
     */
    List<String> getDepartNamesByUsername(String username);



    /** 11Query all parent dictionaries，according tocreate_timesort
     * @return List<DictModel> dictionarygather
     */
    public List<DictModel> queryAllDict();

    /**
     * 12Query all classification dictionaries
     * @return
     */
    public List<SysCategoryModel> queryAllSysCategory();


    /**
     * 14查询所有department as dictionary information id -->value,departName -->text
     * @return
     */
    public List<DictModel> queryAllDepartBackDictModel();

    /**
     * 15According to business type and businessidModify message read
     * @param busType
     * @param busId
     */
    public void updateSysAnnounReadFlag(String busType, String busId);

    /**
     * 16lookup table dictionary Support filtering data
     * @param table
     * @param text
     * @param code
     * @param filterSql
     * @return
     */
    public List<DictModel> queryFilterTableDictInfo(String table, String text, String code, String filterSql);

    /**
     * 17Query specifiedtableof text code Get dictionary，Includetextandvalue
     * @param table
     * @param text
     * @param code
     * @param keyArray
     * @return
     */
    @Deprecated
    public List<String> queryTableDictByKeys(String table, String text, String code, String[] keyArray);

    /**
     * 18Query all users returnComboModel
     * @return
     */
    public List<ComboModel> queryAllUserBackCombo();

    /**
     * 19Query users by page returnJSONObject
     * @param userIds multiple usersid
     * @param pageNo Current page number
     * @param pageSize Number of items displayed per page
     * @return
     */
    public JSONObject queryAllUser(String userIds, Integer pageNo, Integer pageSize);

    /**
     * 20Get all roles
     * @return
     */
    public List<ComboModel> queryAllRole();

    /**
     * 21Get all roles With ginseng
     * @param roleIds Role selected by default
     * @return
     */
    public List<ComboModel> queryAllRole(String[] roleIds );

    /**
     * 22Query roles by user accountIdgather
     * @param username
     * @return
     */
    public List<String> getRoleIdsByUsername(String username);

    /**
     * 23通过department编号查询departmentid
     * @param orgCode
     * @return
     */
    public String getDepartIdsByOrgCode(String orgCode);

    /**
     * 24查询所有department
     * @return
     */
    public List<SysDepartModel> getAllSysDepart();

    /**
     * 25查找父classdepartment
     * @param departId
     * @return
     */
    DictModel getParentDepartId(String departId);

    /**
     * 26according todepartmentId获取department负责人
     * @param deptId
     * @return
     */
    public List<String> getDeptHeadByDepId(String deptId);

    /**
     * 27Send message to specified user
     * @param userIds
     * @param cmd
     */
    public void sendWebSocketMsg(String[] userIds, String cmd);

    /**
     * 28according toidGet all participating users
     * @param userIds multiple usersid
     * @return
     */
    public List<UserAccountInfo> queryAllUserByIds(String[] userIds);

    /**
     * 29Push meeting check-in information to preview
     * userIds
     * @return
     * @param userId
     */
    void meetingSignWebsocket(String userId);

    /**
     * 30according tonameGet all participating users
     * @param userNames multiple users账户
     * @return
     */
    List<UserAccountInfo> queryUserByNames(String[] userNames);


    /**
     * according to高class查询条件Query user
     * @param superQuery
     * @param matchType
     * @return
     */
    List<JSONObject> queryUserBySuperQuery(String superQuery,String matchType);


    /**
     * according toIDQuery user
     * @param id
     * @return
     */
    JSONObject queryUserById(String id);


    /**
     * according to高class查询条件查询department
     * @param superQuery
     * @param matchType
     * @return
     */
    List<JSONObject> queryDeptBySuperQuery(String superQuery,String matchType);

    /**
     * according to高class查询条件Query role
     * @param superQuery
     * @param matchType
     * @return
     */
    List<JSONObject> queryRoleBySuperQuery(String superQuery,String matchType);


    /**
     * according totenantIDQuery userID
     * @param tenantId tenantID
     * @return List<String>
     */
    List<String> selectUserIdByTenantId(String tenantId);



    /**
     * 31获取用户of角色gather
     * @param username
     * @return
     */
    Set<String> getUserRoleSet(String username);
    /**
     * 31获取用户of角色gather
     * @param useId
     * @return
     */
    Set<String> getUserRoleSetById(String useId);
    
    /**
     * 32获取用户of权限gather
     * @param userId
     * @return
     */
    Set<String> getUserPermissionSet(String userId);

    /**
     * 33Determine whether there isonline访问of权限
     * @param onlineAuthDTO
     * @return
     */
    boolean hasOnlineAuth(OnlineAuthDTO onlineAuthDTO);

    /**
     * 34通过departmentid获取department全部信息
     * @param id departmentid
     * @return SysDepartModelobject
     */
    SysDepartModel selectAllById(String id);

    /**
     * 35According to useridQuery user所属公司下所有用户ids
     * @param userId
     * @return
     */
    List<String> queryDeptUsersByUserId(String userId);

    /**
     * 36according tomultiple users账号(comma separated)，查询returnmultiple users信息
     * @param usernames
     * @return
     */
    List<JSONObject> queryUsersByUsernames(String usernames);

    /**
     * 37according tomultiple usersID(comma separated)，查询returnmultiple users信息
     * @param ids
     * @return
     */
    List<JSONObject> queryUsersByIds(String ids);

    /**
     * 38according to多个department编码(comma separated)，查询return多个department信息
     * @param orgCodes
     * @return
     */
    List<JSONObject> queryDepartsByOrgcodes(String orgCodes);

    /**
     * 39according to多个departmentid(comma separated)，查询return多个department信息
     * @param ids
     * @return
     */
    List<JSONObject> queryDepartsByIds(String ids);

    /**
     * 40发送mail消息
     * @param email
     * @param title
     * @param content
     */
    void sendEmailMsg(String email,String title,String content);

    /**
     * 40发送模版mail消息
     *
     * @param email             Receive email
     * @param title             mail标题
     * @param emailTemplateEnum mail模版枚举
     * @param params            Template parameters
     */
    void sendHtmlTemplateEmail(String email, String title, EmailTemplateEnum emailTemplateEnum, JSONObject params);
    /**
     * 41 获取公司下classdepartmentand公司下所有用户信息
     * @param orgCode
     * @return List<Map>
     */
    List<Map> getDeptUserByOrgCode(String orgCode);
    /**
     * 42 Send SMS message
     * @param phone Phone number
     * @param param Template parameters
     * @param dySmsEnum SMS template
     */
    void sendSmsMsg(String phone, JSONObject param, DySmsEnum dySmsEnum);
    /**
     * Query classification dictionary translation
     * @param ids Multiple classification dictionariesid
     * @return List<String>
     */
    List<String> loadCategoryDictItem(String ids);

    /**
     * Back translation classification dictionary，for import
     *
     * @param names name，Comma separated
     */
    List<String> loadCategoryDictItemByNames(String names, boolean delNotExist);

    /**
     * according todictionarycodeLoad dictionarytext
     *
     * @param dictCode order：tableName,text,code
     * @param keys     要查询ofkey
     * @return
     */
    List<String> loadDictItem(String dictCode, String keys);

    /**
     * 复制应用下of所有dictionary配置到newoftenant下
     * 
     * @param originalAppId  Original low-code appID
     * @param appId   newof低代码应用ID
     * @param tenantId  newoftenantID
     * @return  Map<String, String>  Map<Original dictionary encoding, newdictionary编码> 
     */
    Map<String, String> copyLowAppDict(String originalAppId, String appId, String tenantId);

    /**
     * according todictionarycodeQuery dictionary item
     *
     * @param dictCode order：tableName,text,code
     * @param dictCode 要查询ofkey
     * @return
     */
    List<DictModel> getDictItems(String dictCode);

    /**
     *  according to多个dictionarycodeQuery multiple dictionary items
     * @param dictCodeList
     * @return key = dictCode ； value=对应ofdictionary项
     */
    Map<String, List<DictModel>> getManyDictItems(List<String> dictCodeList);

    /**
     * 【JSearchSelectTagDrop-down search component dedicated interface】
     * 大数据量ofdictionary表 Go asynchronous loading  That is, the front-end input content filters the data
     *
     * @param dictCode dictionarycodeFormat：table,text,code
     * @param keyword filter keywords
     * @param pageSize Number of pagination items
     * @return
     */
    List<DictModel> loadDictItemByKeyword(String dictCode, String keyword, Integer pageNo, Integer pageSize);

    /**
     * new增数据日志
     * @param dataLogDto
     */
    void saveDataLog(DataLogDTO dataLogDto);
    /**
     * 更new头像
     * @param loginUser
     */
    void updateAvatar(LoginUser loginUser);

    /**
     * Towardsappend websocket推送聊天刷new消息
     * @param userId
     */
    void sendAppChatSocket(String userId);

    /**
     * according to角色idQuery rolecode
     * @param id
     * @return
     */
    String getRoleCodeById(String id);

    /**
     * according toroleCodeQuery role信息，可comma separated多个
     *
     * @param roleCodes
     * @return
     */
    List<DictModel> queryRoleDictByCode(String roleCodes);

    /**
     * according todepartmentIDQuery userID
     * @param deptIds
     * @return
     */
    List<String> queryUserIdsByDeptIds(List<String> deptIds);


    /**
     * according todepartmentID查询department及其子department下用户ID <br/>
     * @param deptIds
     * @return
     */
    List<String> queryUserIdsByCascadeDeptIds(List<String> deptIds);
    
    /**
     * according todepartmentIDQuery user账号
     * @param deptIds
     * @return
     */
    List<String> queryUserAccountsByDeptIds(List<String> deptIds);

    /**
     * according to角色编码 Query userID
     * @param roleCodes
     * @return
     */
    List<String> queryUserIdsByRoleds(List<String> roleCodes);

    /**
     * according to职务IDQuery userID
     * @param positionIds
     * @return
     */
    List<String> queryUserIdsByPositionIds(List<String> positionIds);

    /**
     * according todepartmentand子department下of所有用户账号
     *
     * @param orgCode department编码
     * @return
     */
    public List<String> getUserAccountsByDepCode(String orgCode);

    /**
     * check querysqlof表and字段是否exist白名单中
     *
     * @param selectSql
     * @return
     */
    boolean dictTableWhiteListCheckBySql(String selectSql);

    /**
     * according todictionary表ordictionary编码，Check whether it is in the whitelist
     *
     * @param tableOrDictCode table name ordictCode
     * @param fields          如果传of是dictCode，Then this parameter must be passednull
     * @return
     */
    boolean dictTableWhiteListCheckByDict(String tableOrDictCode, String... fields);

    /**
     * Automatically publish messages
     * @param dataId
     * @param currentUserName
     */
    void announcementAutoRelease(String dataId, String currentUserName);

    /**
     * according todepartment编码查询公司信息
     * @param orgCode department编码
     * @return
     * @author chenrui
     * @date 2025/8/12 14:53
     */
    SysDepartModel queryCompByOrgCode(@RequestParam(name = "sysCode") String orgCode);

    /**
     * according todepartment编码and层次查询上class公司
     * 
     * @param orgCode department编码
     * @param level Can pass empty Default is1class The minimum value is1
     * @return
     */
    SysDepartModel queryCompByOrgCodeAndLevel(String orgCode, Integer level);

    /**
     * 16 runAIRagprocess
     * for [QQYUN-13634]existbaseapiEncapsulation method inside，Convenient for calling other modules
     *
     * @param airagFlowDTO
     * @return process执行结果,may beStringorMap
     * @author chenrui
     * @date 2025/9/2 11:43
     */
    Object runAiragFlow(AiragFlowDTO airagFlowDTO);

    /**
     * according todepartmentcode或departmentid获取departmentname(当前and上classdepartment)
     *
     * @param orgCode department编码
     * @param depId   departmentid
     * @return String departmentname
     */
    String getDepartPathNameByOrgCode(String orgCode, String depId);
}
