package org.jeecg.config.firewall.SqlInjection.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgSqlInjectionException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.firewall.SqlInjection.IDictTableWhiteListHandler;
import org.jeecg.config.firewall.interceptor.LowCodeModeInterceptor;
import org.jeecg.modules.system.entity.SysTableWhiteList;
import org.jeecg.modules.system.security.DictQueryBlackListHandler;
import org.jeecg.modules.system.service.ISysTableWhiteListService;
import org.jeecgframework.minidao.sqlparser.impl.vo.SelectSqlInfo;
import org.jeecgframework.minidao.util.MiniDaoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.*;

/**
 * Whitelist processing for common situations，If there is a situation that cannot be handled，Implementation classes can be written separately
 */
@Slf4j
@Component("dictTableWhiteListHandlerImpl")
public class DictTableWhiteListHandlerImpl implements IDictTableWhiteListHandler {

    /**
     * key-table name
     * value-Field name，Multiple commas separated
     * Two configuration methods-- Configure all in lowercase
     * whiteTablesRuleMap.put("sys_user", "*")  sys_userAll fields support query
     * whiteTablesRuleMap.put("sys_user", "username,password")  sys_userinusernameandpasswordSupport query
     */
    private static final Map<String, String> whiteTablesRuleMap = new HashMap<>();
    /**
     * LowCode Is it dev model
     */
    private static Boolean LOW_CODE_IS_DEV = null;


    @Autowired
    private ISysTableWhiteListService sysTableWhiteListService;
    @Autowired
    private JeecgBaseConfig jeecgBaseConfig;

    
    /**
     * initialization whiteTablesRuleMap method
     */
    private void init() {
        // If the currentdevmodel，Then query the database every time，Prevent caching
        if (this.isDev()) {
            DictTableWhiteListHandlerImpl.whiteTablesRuleMap.clear();
        }
        // ifmapis empty，Then query from the database
        if (DictTableWhiteListHandlerImpl.whiteTablesRuleMap.isEmpty()) {
            Map<String, String> ruleMap = sysTableWhiteListService.getAllConfigMap();
            log.info("surfacedictionary白名单initialization完成：{}", ruleMap);
            DictTableWhiteListHandlerImpl.whiteTablesRuleMap.putAll(ruleMap);
        }
    }

    @Override
    public boolean isPassBySql(String sql) {
        Map<String, SelectSqlInfo> parsedMap = null;
        try {
            parsedMap = MiniDaoUtil.parseAllSelectTable(sql);
        } catch (Exception e) {
            log.warn("checksqlstatement，Parsing error：{}", e.getMessage());
        }
        // ifsqlThere is a problem，It will definitely not be implemented，So return directlytrue
        if (parsedMap == null) {
            return true;
        }
        log.info("Getselect sqlinformation ：{} ", parsedMap);
        // Traverse the currentsqlin所有table name，if有其中一个surface或surface的FieldNot here白名单中，will not pass
        for (Map.Entry<String, SelectSqlInfo> entry : parsedMap.entrySet()) {
            SelectSqlInfo sqlInfo = entry.getValue();
            if (sqlInfo.isSelectAll()) {
                log.warn("查询statement中包含 * Field，Pass for now");
                continue;
            }
            Set<String> queryFields = sqlInfo.getAllRealSelectFields();
            // checktable nameandField是否允许查询
            String tableName = entry.getKey();
            if (!this.checkWhiteList(tableName, queryFields)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isPassByDict(String dictCodeString) {
        if (oConvertUtils.isEmpty(dictCodeString)) {
            return true;
        }
        try {
            // Decode escape characters
            dictCodeString = URLDecoder.decode(dictCodeString, "UTF-8");
        } catch (Exception e) {
            log.warn(e.getMessage());
            //this.throwException("dictionarycodeDecoding failed，Possibly illegal characters were used，Check, please！");
        }
        dictCodeString = dictCodeString.trim();
        String[] arr = dictCodeString.split(SymbolConstant.COMMA);
        // Gettable name
        String tableName = this.getTableName(arr[0]);
        // Get查询Field
        arr = Arrays.copyOfRange(arr, 1, arr.length);
        // distinctThe function is to remove weight，Equivalent to Set<String>
        String[] fields = Arrays.stream(arr).map(String::trim).distinct().toArray(String[]::new);
        // checktable nameandField是否允许查询
        return this.isPassByDict(tableName, fields);
    }

    @Override
    public boolean isPassByDict(String tableName, String... fields) {
        if (oConvertUtils.isEmpty(tableName)) {
            return true;
        }
        if (fields == null || fields.length == 0) {
            fields = new String[]{"*"};
        }
        String sql = "select " + String.join(",", fields) + " from " + tableName;
        log.info("dictionary拼接的查询SQL：{}", sql);
        try {
            // conductSQLparse
            MiniDaoUtil.parseSelectSqlInfo(sql);
        } catch (Exception e) {
            // ifSQLparse失败，则passField nameandtable nameconductcheck
            return checkWhiteList(tableName, new HashSet<>(Arrays.asList(fields)));
        }
        // passSQLparseconductcheck，Can preventSQLinjection
        return this.isPassBySql(sql);
    }

    /**
     * checktable nameandField是否在白名单内
     *
     * @param tableName
     * @param queryFields
     * @return
     */
    public boolean checkWhiteList(String tableName, Set<String> queryFields) {
        this.init();
        // 1、judge“table name”是否passcheck，ifis empty则未passcheck
        if (oConvertUtils.isEmpty(tableName)) {
            log.error("白名单check：table nameis empty");
            this.throwException();
        }
        // Convert to lowercase uniformly
        tableName = tableName.toLowerCase();
        String allowFieldStr = DictTableWhiteListHandlerImpl.whiteTablesRuleMap.get(tableName);
        log.info("checkWhiteList tableName: {}", tableName);
        if (oConvertUtils.isEmpty(allowFieldStr)) {
            // if是devmodel，Automatically add data to the database
            if (this.isDev()) {
                this.autoAddWhiteList(tableName, String.join(",", queryFields));
                allowFieldStr = DictTableWhiteListHandlerImpl.whiteTablesRuleMap.get(tableName);
            } else {
                // prodmodel下，Throw an exception directly
                log.error("白名单check：surface\"{}\"未passcheck", tableName);
                this.throwException();
            }
        }
        // 2、judge“Field name”是否passcheck
        // Convert to lowercase uniformly
        allowFieldStr = allowFieldStr.toLowerCase();
        Set<String> allowFields = new HashSet<>(Arrays.asList(allowFieldStr.split(",")));
        // 需要合并的Field
        Set<String> waitMergerFields = new HashSet<>();
        for (String field : queryFields) {
            if(oConvertUtils.isEmpty(field)){
                continue;
            }
            // Convert to lowercase uniformly
            field = field.toLowerCase();
            // if允许的Field里不包含查询的Field，则Throw an exception directly
            if (!allowFields.contains(field)) {
                // if是devmodel，记录需要合并的Field
                if (this.isDev()) {
                    waitMergerFields.add(field);
                } else {
                    log.error("白名单check：Field {} Not here {} within range，access denied！", field, allowFields);
                    this.throwException();
                }
            }
        }
        // 自动向数据库中合并未pass的Field
        if (!waitMergerFields.isEmpty()) {
            this.autoAddWhiteList(tableName, String.join(",", waitMergerFields));
        }
        log.info("白名单check：查询surface\"{}\"，查询Field {} passcheck", tableName, queryFields);
        return true;
    }

    /**
     * Automatically add whitelist，if数据库已有，则Field会自动合并
     *
     * @param tableName
     * @param allowFieldStr
     */
    private void autoAddWhiteList(String tableName, String allowFieldStr) {
        try {
            SysTableWhiteList entity = sysTableWhiteListService.autoAdd(tableName, allowFieldStr);
            DictTableWhiteListHandlerImpl.whiteTablesRuleMap.put(tableName, entity.getFieldName());
            log.warn("surface\"{}\"未passcheck，and is currently dev model，Whitelist data has been automatically added to the database。查询Field：{}", tableName, allowFieldStr);
        } catch (Exception e) {
            log.error("surface\"{}\"未passcheck，and is currently dev model，But automatically adding whitelist data to the database failed，Please check and try again。Error reason：{}", tableName, e.getMessage(), e);
            this.throwException();
        }
    }

    /**
     * judge当前 LowCode Is it dev model
     */
    private boolean isDev() {
        if (DictTableWhiteListHandlerImpl.LOW_CODE_IS_DEV == null) {
            if (this.jeecgBaseConfig.getFirewall() != null) {
                String lowCodeMode = this.jeecgBaseConfig.getFirewall().getLowCodeMode();
                DictTableWhiteListHandlerImpl.LOW_CODE_IS_DEV = LowCodeModeInterceptor.LOW_CODE_MODE_DEV.equals(lowCodeMode);
            } else {
                // if没有 firewall Configuration，The default is false
                DictTableWhiteListHandlerImpl.LOW_CODE_IS_DEV = false;
            }
        }
        return DictTableWhiteListHandlerImpl.LOW_CODE_IS_DEV;
    }

    @Override
    public boolean clear() {
        DictTableWhiteListHandlerImpl.whiteTablesRuleMap.clear();
        return true;
    }

    
    /**
     * PickwhereThe previous one is：table name
     *
     * @param str
     * @see DictQueryBlackListHandler#getTableName(String)
     */
    @SuppressWarnings("JavadocReference")
    private String getTableName(String str) {
        String[] arr = str.split("\\s+(?i)where\\s+");
        String tableName = arr[0].trim();
        //【20230814】Solve using parameterstableName=sys_user t&Retest，Vulnerabilities still exist
        if (tableName.contains(".")) {
            tableName = tableName.substring(tableName.indexOf(".") + 1, tableName.length()).trim();
        }
        if (tableName.contains(" ")) {
            tableName = tableName.substring(0, tableName.indexOf(" ")).trim();
        }

        //【issues/4393】 sys_user , (sys_user), sys_user%20, %60sys_user%60
        String reg = "\\s+|\\(|\\)|`";
        return tableName.replaceAll(reg, "");
    }

    private void throwException() throws JeecgSqlInjectionException {
        this.throwException(this.getErrorMsg());
    }

    private void throwException(String message) throws JeecgSqlInjectionException {
        if (oConvertUtils.isEmpty(message)) {
            message = this.getErrorMsg();
        }
        log.error(message);
        throw new JeecgSqlInjectionException(message);
    }

    @Override
    public String getErrorMsg() {
        return "白名单check未pass！";
    }

}
