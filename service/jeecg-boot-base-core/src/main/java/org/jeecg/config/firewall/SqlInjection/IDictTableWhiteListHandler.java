package org.jeecg.config.firewall.SqlInjection;

/**
 * Dictionary table query :: Whitelist configuration
 *
 * @Author taoYan
 * @Date 2022/3/17 11:21
 **/
public interface IDictTableWhiteListHandler {

    /**
     * check【table name】【Field】Is it legal to query?，Return if allowed true
     *
     * @param sql
     * @return
     */
    boolean isPassBySql(String sql);

    /**
     * check字典是否通过
     *
     * @param dictCodeString Dictionary table configuration
     * @return
     */
    boolean isPassByDict(String dictCodeString);

    boolean isPassByDict(String tableName, String... fields);

    /**
     * Clear cache，Make changes effective
     *
     * @return
     */
    boolean clear();

    String getErrorMsg();

}
