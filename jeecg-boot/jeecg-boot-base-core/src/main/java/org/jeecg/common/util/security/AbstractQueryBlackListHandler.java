package org.jeecg.common.util.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.exception.JeecgSqlInjectionException;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * lookup table/Field Blacklist processing
 * @Author taoYan
 * @Date 2022/3/17 11:21
 **/
@Slf4j
public abstract class AbstractQueryBlackListHandler {

    /**
     * key-table name
     * value-Field名，Multiple commas separated
     * Two configuration methods-- Configure all in lowercase
     * ruleMap.put("sys_user", "*")sys_user所有的FieldQuery is not supported
     * ruleMap.put("sys_user", "username,password")sys_userinusernameandpasswordQuery is not supported
     */
    public static Map<String, String> ruleMap = new HashMap<>();

    /**
     * 以下字符不能出现在table name中或是Field名中
     */
    public static final Pattern ILLEGAL_NAME_REG = Pattern.compile("[-]{2,}");

    static {
        ruleMap.put("sys_user", "password,salt");
    }


    /**
     * according to sqlstatement GetsurfaceandFieldinformation，You need to override this method in a specific implementation class-
     * different scenes The processing may be different Need to customize，But the return value is determined
     * @param sql
     * @return
     */
    protected abstract List<QueryTable> getQueryTableInfo(String sql);


    /**
     * checksqlstatement Return successfullytrue
     * @param sql
     * @return
     */
    public boolean isPass(String sql) {
        List<QueryTable> list = null;
        //【jeecg-boot/issues/4040】Online reports do not support subqueries，Parsing error #4040
        try {
            list = this.getQueryTableInfo(sql.toLowerCase());
        } catch (Exception e) {
            log.warn("checksqlstatement，Parsing error：{}",e.getMessage());
        }
        
        if(list==null){
            return true;
        }
        log.info("  Getsqlinformation ：{} ", list.toString());
        boolean flag = checkTableAndFieldsName(list);
        if(flag == false){
            return false;
        }
        Set<String> xssTableSet = new HashSet<>(Arrays.asList(SqlInjectionUtil.XSS_STR_TABLE.split("\\|")));

        for (QueryTable table : list) {
            String name = table.getName();
            String fieldRule = ruleMap.get(name);
            // Have you configured this table?
            if (fieldRule != null) {
                if ("*".equals(fieldRule) || table.isAll()) {
                    flag = false;
                    log.warn("sql黑名单check，surface【"+name+"】Forbidden query");
                    break;
                } else if (table.existSameField(fieldRule)) {
                    flag = false;
                    break;
                }

            }
            // Determine whether the blacklist database is called
            String dbName = table.getDbName();
            if (oConvertUtils.isNotEmpty(dbName)) {
                dbName = dbName.toLowerCase().trim();
                if (xssTableSet.contains(dbName)) {
                    flag = false;
                    log.warn("sql黑名单check，database【" + dbName + "】Forbidden query");
                    break;
                }
            }
        }

        // 返回黑名单check结果（If it is illegal, an exception will be thrown directly.）
        if(!flag){
            log.error(this.getError());
            throw new JeecgSqlInjectionException(this.getError());
        }
        return flag;
    }

    /**
     * checktable nameandField名是否有效，Or will it bring some special strings?sqlinjection
     * issues/4983 SQL Injection in 3.5.1 #4983
     * @return
     */
    private boolean checkTableAndFieldsName(List<QueryTable> list){
        boolean flag = true;
        for(QueryTable queryTable: list){
            String tableName = queryTable.getName();
            if(hasSpecialString(tableName)){
                flag = false;
                log.warn("sql黑名单check，table name【"+tableName+"】Contains special characters");
                break;
            }
            Set<String> fields = queryTable.getFields();
            for(String name: fields){
                if(hasSpecialString(name)){
                    flag = false;
                    log.warn("sql黑名单check，Field名【"+name+"】Contains special characters");
                    break;
                } 
            }
        }
        return flag;
    }

    /**
     * Whether it contains a special string
     * @param name
     * @return
     */
    private boolean hasSpecialString(String name){
        Matcher m = ILLEGAL_NAME_REG.matcher(name);
        if (m.find()) {
            return true;
        }
        return false;
    }
    

    /**
     * 查询的surface的information
     */
    protected class QueryTable {
        //database名
        private String dbName;
        //table name
        private String name;
        //surface的别名
        private String alias;
        // Field名集合
        private Set<String> fields;
        // 是否查询所有Field
        private boolean all;

        public QueryTable() {
        }

        public QueryTable(String name, String alias) {
            this.name = name;
            this.alias = alias;
            this.all = false;
            this.fields = new HashSet<>();
        }

        public void addField(String field) {
            this.fields.add(field);
        }

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public String getName() {
            return name;
        }

        public Set<String> getFields() {
            return new HashSet<>(fields);
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFields(Set<String> fields) {
            this.fields = fields;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public boolean isAll() {
            return all;
        }

        public void setAll(boolean all) {
            this.all = all;
        }

        /**
         * 判断是否有相同Field
         *
         * @param fieldString
         * @return
         */
        public boolean existSameField(String fieldString) {
            String[] controlFields = fieldString.split(",");
            for (String sqlField : fields) {
                for (String controlField : controlFields) {
                    if (sqlField.equals(controlField)) {
                        // Very specific column direct comparison
                        log.warn("sql黑名单check，surface【"+name+"】中Field【"+controlField+"】Forbidden query");
                        return true;
                    } else {
                        // 使用surface达式的列 It can only be interpreted that the string contains
                        String aliasColumn = controlField;
                        if (StringUtils.isNotBlank(alias)) {
                            aliasColumn = alias + "." + controlField;
                        }
                        if (sqlField.indexOf(aliasColumn) != -1) {
                            log.warn("sql黑名单check，surface【"+name+"】中Field【"+controlField+"】Forbidden query");
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "QueryTable{" +
                    "name='" + name + '\'' +
                    ", alias='" + alias + '\'' +
                    ", fields=" + fields +
                    ", all=" + all +
                    '}';
        }
    }

    public String getError(){
        // TODO
        return "The system has set security rules，敏感surfaceand敏感FieldForbidden query，Contact the administrator for authorization!";
    }

}
