package org.jeecg.common.util.dynamic.db;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.DynamicDataSourceModel;
import org.jeecg.common.util.ReflectHelper;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring JDBC Real-time database access
 *
 * @author chenguobin
 * @version 1.0
 * @date 2014-09-05
 */
@Slf4j
public class DynamicDBUtil {

    /**
     * Get data source【lowest level method，Don't call casually】
     *
     * @param dbSource
     * @return
     */
    private static DruidDataSource getJdbcDataSource(final DynamicDataSourceModel dbSource) {
        DruidDataSource dataSource = new DruidDataSource();

        String driverClassName = dbSource.getDbDriver();
        String url = dbSource.getDbUrl();
        // urlconfigured as “123” will triggerDruidinfinite loop，Keep trying to connect again and again
        if (oConvertUtils.isEmpty(url) || !url.toLowerCase().startsWith("jdbc:")) {
            throw new JeecgBootException("data sourceURLConfiguration format is incorrect！");
        }
        
        String dbUser = dbSource.getDbUsername();
        String dbPassword = dbSource.getDbPassword();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        //dataSource.setValidationQuery("SELECT 1 FROM DUAL");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setBreakAfterAcquireFailure(true);
        //Set timeout60Second
        dataSource.setLoginTimeout(60);
        dataSource.setConnectionErrorRetryAttempts(0);
        dataSource.setUsername(dbUser);
        dataSource.setMaxWait(30000);
        dataSource.setPassword(dbPassword);

        log.info("******************************************");
        log.info("*                                        *");
        log.info("*====【"+dbSource.getCode()+"】=====DruidConnection pooling is enabled ====*");
        log.info("*                                        *");
        log.info("******************************************");
        return dataSource;
    }

    /**
     * pass dbKey ,Get data source
     *
     * @param dbKey
     * @return
     */
    public static DruidDataSource getDbSourceByDbKey(final String dbKey) {
        //获取多data source配置
        DynamicDataSourceModel dbSource = DataSourceCachePool.getCacheDynamicDataSourceModel(dbKey);
        //First determine whether there is a database link in the cache
        DruidDataSource cacheDbSource = DataSourceCachePool.getCacheBasicDataSource(dbKey);
        if (cacheDbSource != null && !cacheDbSource.isClosed()) {
            log.debug("--------getDbSourceBydbKey------------------Get from cacheDBconnect-------------------");
            return cacheDbSource;
        } else {
            DruidDataSource dataSource = getJdbcDataSource(dbSource);
            if(dataSource!=null && dataSource.isEnable()){

                // 【TV360X-2060】Set timeout 6Second
                dataSource.setMaxWait(6000);

                DataSourceCachePool.putCacheBasicDataSource(dbKey, dataSource);
            }else{
                throw new JeecgBootException("动态data sourceconnect失败，dbKey："+dbKey);
            }
            log.info("--------getDbSourceBydbKey------------------createDB数据库connect-------------------");
            return dataSource;
        }
    }

    /**
     * 关闭数据库connect池
     *
     * @param dbKey
     * @return
     */
    public static void closeDbKey(final String dbKey) {
        DruidDataSource dataSource = getDbSourceByDbKey(dbKey);
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.getConnection().commit();
                dataSource.getConnection().close();
                dataSource.close();
                DataSourceCachePool.removeCache(dbKey);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage(), e);
        }
    }


    private static JdbcTemplate getJdbcTemplate(String dbKey) {
        DruidDataSource dataSource = getDbSourceByDbKey(dbKey);
        return new JdbcTemplate(dataSource);
    }

    /**
     * according todata source获取NamedParameterJdbcTemplate
     * @param dbKey
     * @return
     */
    private static NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(String dbKey) {
        DruidDataSource dataSource = getDbSourceByDbKey(dbKey);
        return new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Executes the SQL statement in this <code>PreparedStatement</code> object,
     * which must be an SQL Data Manipulation Language (DML) statement, such as <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code>; or an SQL statement that returns nothing,
     * such as a DDL statement.
     */
    public static int update(final String dbKey, String sql, Object... param) {
        int effectCount;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        if (ArrayUtils.isEmpty(param)) {
            effectCount = jdbcTemplate.update(sql);
        } else {
            effectCount = jdbcTemplate.update(sql, param);
        }
        return effectCount;
    }

    /**
     * supportminiDaogrammatical operationUpdate
     *
     * @param dbKey data source标识
     * @param sql   implementsqlstatement，sqlsupportminidaogrammatical logic
     * @param data  sqlThe data that needs to be judged in the grammar andsqlData required for splicing injection
     * @return
     */
    public static int updateByHash(final String dbKey, String sql, HashMap<String, Object> data) {
        int effectCount;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        //Get based on templatesql
        sql = FreemarkerParseFactory.parseTemplateContent(sql, data);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        effectCount = namedParameterJdbcTemplate.update(sql, data);
        return effectCount;
    }

    public static Object findOne(final String dbKey, String sql, Object... param) {
        List<Map<String, Object>> list;
        list = findList(dbKey, sql, param);
        if (oConvertUtils.listIsEmpty(list)) {
            log.error("Except one, but not find actually");
            return null;
        }
        if (list.size() > 1) {
            log.error("Except one, but more than one actually");
        }
        return list.get(0);
    }

    /**
     * supportminiDaogrammatical operationQuery returnHashMap
     *
     * @param dbKey data source标识
     * @param sql   implementsqlstatement，sqlsupportminidaogrammatical logic
     * @param data  sqlThe data that needs to be judged in the grammar andsqlData required for splicing injection
     * @return
     */
    public static Object findOneByHash(final String dbKey, String sql, HashMap<String, Object> data) {
        List<Map<String, Object>> list;
        list = findListByHash(dbKey, sql, data);
        if (oConvertUtils.listIsEmpty(list)) {
            log.error("Except one, but not find actually");
        }
        if (list.size() > 1) {
            log.error("Except one, but more than one actually");
        }
        return list.get(0);
    }

    /**
     * directsqlQuery according toclazzreturn单个实例
     *
     * @param dbKey data source标识
     * @param sql   implementsqlstatement
     * @param clazz return实例的Class
     * @param param
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Object findOne(final String dbKey, String sql, Class<T> clazz, Object... param) {
        Map<String, Object> map = (Map<String, Object>) findOne(dbKey, sql, param);
        return ReflectHelper.setAll(clazz, map);
    }

    /**
     * supportminiDaogrammatical operationQuery return单个实例
     *
     * @param dbKey data source标识
     * @param sql   implementsqlstatement，sqlsupportminidaogrammatical logic
     * @param clazz return实例的Class
     * @param data  sqlThe data that needs to be judged in the grammar andsqlData required for splicing injection
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Object findOneByHash(final String dbKey, String sql, Class<T> clazz, HashMap<String, Object> data) {
        Map<String, Object> map = (Map<String, Object>) findOneByHash(dbKey, sql, data);
        return ReflectHelper.setAll(clazz, map);
    }

    public static List<Map<String, Object>> findList(final String dbKey, String sql, Object... param) {
        List<Map<String, Object>> list;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);

        if (ArrayUtils.isEmpty(param)) {
            list = jdbcTemplate.queryForList(sql);
        } else {
            list = jdbcTemplate.queryForList(sql, param);
        }
        return list;
    }

    /**
     * Query数量
     * @param dbKey
     * @param sql
     * @param param
     * @return
     */
    public static Map<String, Object> queryCount(String dbKey, String sql, Map<String, Object> param){
        NamedParameterJdbcTemplate npJdbcTemplate = getNamedParameterJdbcTemplate(dbKey);
        return npJdbcTemplate.queryForMap(sql, param);
    }

    /**
     * Query列表数据
     * @param dbKey
     * @param sql
     * @param param
     * @return
     */
    public static List<Map<String, Object>> findListByNamedParam(final String dbKey, String sql, Map<String, Object> param) {
        NamedParameterJdbcTemplate npJdbcTemplate = getNamedParameterJdbcTemplate(dbKey);
        List<Map<String, Object>> list = npJdbcTemplate.queryForList(sql, param);
        return list;
    }

    /**
     * supportminiDaogrammatical operationQuery
     *
     * @param dbKey data source标识
     * @param sql   implementsqlstatement，sqlsupportminidaogrammatical logic
     * @param data  sqlThe data that needs to be judged in the grammar andsqlData required for splicing injection
     * @return
     */
    public static List<Map<String, Object>> findListByHash(final String dbKey, String sql, HashMap<String, Object> data) {
        List<Map<String, Object>> list;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        //Get based on templatesql
        sql = FreemarkerParseFactory.parseTemplateContent(sql, data);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        list = namedParameterJdbcTemplate.queryForList(sql, data);
        return list;
    }

    /**
     * 此方法只能return单列，不能return实体kind
     * @param dbKey data source的key
     * @param sql sal
     * @param clazz kind
     * @param param parameter
     * @param <T>
     * @return
     */
    public static <T> List<T> findList(final String dbKey, String sql, Class<T> clazz, Object... param) {
        List<T> list;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);

        if (ArrayUtils.isEmpty(param)) {
            list = jdbcTemplate.queryForList(sql, clazz);
        } else {
            list = jdbcTemplate.queryForList(sql, clazz, param);
        }
        return list;
    }

    /**
     * supportminiDaogrammatical operationQuery return单列数据list
     *
     * @param dbKey data source标识
     * @param sql   implementsqlstatement，sqlsupportminidaogrammatical logic
     * @param clazz kind型Long、Stringwait
     * @param data  sqlThe data that needs to be judged in the grammar andsqlData required for splicing injection
     * @return
     */
    public static <T> List<T> findListByHash(final String dbKey, String sql, Class<T> clazz, HashMap<String, Object> data) {
        List<T> list;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        //Get based on templatesql
        sql = FreemarkerParseFactory.parseTemplateContent(sql, data);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        list = namedParameterJdbcTemplate.queryForList(sql, data, clazz);
        return list;
    }

    /**
     * directsqlQuery return实体kind列表
     *
     * @param dbKey data source标识
     * @param sql   implementsqlstatement，sqlsupport minidao grammatical logic
     * @param clazz return实体kind列表的class
     * @param param sqlData required for splicing injection
     * @return
     */
    public static <T> List<T> findListEntities(final String dbKey, String sql, Class<T> clazz, Object... param) {
        List<Map<String, Object>> queryList = findList(dbKey, sql, param);
        return ReflectHelper.transList2Entrys(queryList, clazz);
    }

    /**
     * supportminiDaogrammatical operationQuery return实体kind列表
     *
     * @param dbKey data source标识
     * @param sql   implementsqlstatement，sqlsupportminidaogrammatical logic
     * @param clazz return实体kind列表的class
     * @param data  sqlThe data that needs to be judged in the grammar andsqlData required for splicing injection
     * @return
     */
    public static <T> List<T> findListEntitiesByHash(final String dbKey, String sql, Class<T> clazz, HashMap<String, Object> data) {
        List<Map<String, Object>> queryList = findListByHash(dbKey, sql, data);
        return ReflectHelper.transList2Entrys(queryList, clazz);
    }
}
