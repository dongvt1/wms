package org.jeecg.common.util.dynamic.db;

import com.baomidou.mybatisplus.annotation.DbType;
import org.jeecg.common.constant.DataBaseConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * Database type judgment
 * 【Some database engines are the same，for the purpose of reuse】
 * @author: jeecg-boot
 */
public class DbTypeUtils {

    public static Map<String, String> dialectMap = new HashMap<String, String>();
    static{
        dialectMap.put("mysql", "org.hibernate.dialect.MySQL5InnoDBDialect");
        // mariadbdatabase 1  --
        dialectMap.put("mariadb", "org.hibernate.dialect.MariaDBDialect");
        //oracledatabase 1
        dialectMap.put("oracle", "org.hibernate.dialect.OracleDialect");
        // TODO not found not sure
        dialectMap.put("oracle12c", "org.hibernate.dialect.OracleDialect");
        // db2database 1xx
        dialectMap.put("db2", "org.hibernate.dialect.DB2390Dialect");
        // H2database
        dialectMap.put("h2", "org.hibernate.dialect.HSQLDialect");
        // HSQLdatabase  1
        dialectMap.put("hsql", "org.hibernate.dialect.HSQLDialect");
        //SQLitedatabase Application platformmobile
        dialectMap.put("sqlite", "org.jeecg.modules.online.config.dialect.SQLiteDialect");
        //PostgreSQLdatabase1  --
        dialectMap.put("postgresql", "org.hibernate.dialect.PostgreSQLDialect");
        dialectMap.put("sqlserver2005", "org.hibernate.dialect.SQLServer2005Dialect");
        //sqlserverdatabase1
        dialectMap.put("sqlserver", "org.hibernate.dialect.SQLServerDialect");
        //达梦database [Domestic] 1--
        dialectMap.put("dm", "org.hibernate.dialect.DmDialect");
        //虚谷database
        dialectMap.put("xugu", "org.hibernate.dialect.HSQLDialect");
        //Renmin University of Finance and Economics [Domestic] 1
        dialectMap.put("kingbasees", "org.hibernate.dialect.PostgreSQLDialect");
        // Phoenix HBasedatabase
        dialectMap.put("phoenix", "org.hibernate.dialect.HSQLDialect");
        // Gauss database
        dialectMap.put("zenith", "org.hibernate.dialect.PostgreSQLDialect");
        //Alibaba CloudPolarDB
        dialectMap.put("clickhouse", "org.hibernate.dialect.MySQLDialect");
        // 南大通用database TODO not found not sure
        dialectMap.put("gbase", "org.hibernate.dialect.PostgreSQLDialect");
        //神通database [Domestic] TODO not found not sure
        dialectMap.put("oscar", "org.hibernate.dialect.PostgreSQLDialect");
        //Sybase ASE database
        dialectMap.put("sybase", "org.hibernate.dialect.SybaseDialect");
        dialectMap.put("oceanbase", "org.hibernate.dialect.PostgreSQLDialect");
        dialectMap.put("Firebird", "org.hibernate.dialect.FirebirdDialect");
        //瀚高database
        dialectMap.put("highgo", "org.hibernate.dialect.HSQLDialect");
        dialectMap.put("other", "org.hibernate.dialect.PostgreSQLDialect");
    }

    public static boolean dbTypeIsMySql(DbType dbType) {
        return dbTypeIf(dbType, DbType.MYSQL, DbType.MARIADB, DbType.CLICK_HOUSE, DbType.SQLITE);
    }

    public static boolean dbTypeIsOracle(DbType dbType) {
        return dbTypeIf(dbType, DbType.ORACLE, DbType.ORACLE_12C, DbType.DM);
    }

    /**
     * Is it Dameng?
     */
    public static boolean dbTypeIsDm(DbType dbType) {
        return dbTypeIf(dbType, DbType.DM);
    }

    public static boolean dbTypeIsSqlServer(DbType dbType) {
        return dbTypeIf(dbType, DbType.SQL_SERVER, DbType.SQL_SERVER2005);
    }

    public static boolean dbTypeIsPostgre(DbType dbType) {
        return dbTypeIf(dbType, DbType.POSTGRE_SQL, DbType.KINGBASE_ES, DbType.GAUSS);
    }



    /**
     *  According to enumeration class 获取database类型的字符串
     * @param dbType
     * @return
     */
    public static String getDbTypeString(DbType dbType){
        if(DbType.DB2.equals(dbType)){
            return DataBaseConstant.DB_TYPE_DB2;
        }else if(DbType.HSQL.equals(dbType)){
            return DataBaseConstant.DB_TYPE_HSQL;
        }else if(dbTypeIsOracle(dbType)){
            return DataBaseConstant.DB_TYPE_ORACLE;
        }else if(dbTypeIsSqlServer(dbType)){
            return DataBaseConstant.DB_TYPE_SQLSERVER;
        }else if(dbTypeIsPostgre(dbType)){
            return DataBaseConstant.DB_TYPE_POSTGRESQL;
        }
        return DataBaseConstant.DB_TYPE_MYSQL;
    }

    /**
     *  According to enumeration class 获取database方言字符串
     * @param dbType
     * @return
     */
    public static String getDbDialect(DbType dbType){
        return dialectMap.get(dbType.getDb());
    }

    /**
     * 判断database类型
     */
    public static boolean dbTypeIf(DbType dbType, DbType... correctTypes) {
        for (DbType type : correctTypes) {
            if (type.equals(dbType)) {
                return true;
            }
        }
        return false;
    }

}
