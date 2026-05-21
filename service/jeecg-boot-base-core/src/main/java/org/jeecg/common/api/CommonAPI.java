package org.jeecg.common.api;

import org.jeecg.common.api.dto.AiragFlowDTO;
import org.jeecg.common.system.vo.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Universalapi
 * @author: jeecg-boot
 */
public interface CommonAPI {

    /**
     * 1Query user role information
     * @param username
     * @return
     */
    Set<String> queryUserRoles(String username);
    
    /**
     * 1Query user role information
     * @param userId
     * @return
     */
    Set<String> queryUserRolesById(String userId);


    /**
     * 2Query user permission information
     * @param userId
     * @return
     */
    Set<String> queryUserAuths(String userId);

    /**
     * 3according to id Query the data stored in the database DynamicDataSourceModel
     *
     * @param dbSourceId
     * @return
     */
    DynamicDataSourceModel getDynamicDbSourceById(String dbSourceId);

    /**
     * 4according to code Query the data stored in the database DynamicDataSourceModel
     *
     * @param dbSourceCode
     * @return
     */
    DynamicDataSourceModel getDynamicDbSourceByCode(String dbSourceCode);

    /**
     * 5according to用户账号Query user information
     * @param username
     * @return
     */
    public LoginUser getUserByName(String username);
    
    /**
     * 5according to用户账号查询用户Id
     * @param username
     * @return
     */
    public String getUserIdByName(String username);


    /**
     * 6Dictionary translate
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    String translateDictFromTable(String table, String text, String code, String key);

    /**
     * 7普通字典的translate
     * @param code
     * @param key
     * @return
     */
    String translateDict(String code, String key);

    /**
     * 8Query data permissions
     * @param component components
     * @param username username
     * @param requestPath Previous request address
     * @return
     */
    List<SysPermissionDataRuleModel> queryPermissionDataRule(String component, String requestPath, String username);


    /**
     * 9Query user information
     * @param username
     * @return
     */
    SysUserCacheInfo getCacheUser(String username);

    /**
     * 10Get data dictionary
     * @param code
     * @return
     */
    public List<DictModel> queryDictItemsByCode(String code);

    /**
     * Get valid data dictionary entries
     * @param code
     * @return
     */
    public List<DictModel> queryEnableDictItemsByCode(String code);

    /**
     * 13Get table data dictionary
     * @param tableFilterSql
     * @param text
     * @param code
     * @return
     */
    List<DictModel> queryTableDictItemsByCode(String tableFilterSql, String text, String code);

    /**
     * 14 普通字典的translate，according to多个dictCodeand multiple pieces of data，Multiple separated by commas
     * @param dictCodes For example：user_status,sex
     * @param keys For example：1,2,0
     * @return
     */
    Map<String, List<DictModel>> translateManyDict(String dictCodes, String keys);

    //update-begin---author:chenrui ---date:20231221  for：[issues/#5643]Solve the problem that the distributed table dictionary cannot be queried across databases------------
    /**
     * 15 Dictionary translate，Available in batches
     * @param table
     * @param text
     * @param code
     * @param keys Separate multiple with commas
     * @param dataSource data source
     * @return
     */
    List<DictModel> translateDictFromTableByKeys(String table, String text, String code, String keys, String dataSource);
    //update-end---author:chenrui ---date:20231221  for：[issues/#5643]Solve the problem that the distributed table dictionary cannot be queried across databases------------

    /**
     * 16 runAIRagprocess
     * for  [QQYUN-13634]existbaseapiEncapsulation method inside，Convenient for calling other modules
     *
     * @param airagFlowDTO
     * @return process执行结果,may beStringorMap
     * @author chenrui
     * @date 2025/9/2 11:43
     */
    Object runAiragFlow(AiragFlowDTO airagFlowDTO);

}
