package org.jeecg.config.flyway;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Map;

/**
* @Description: initializationflywayConfiguration Support multiple data sources after modification，Print logs when exceptions occur，Does not affect project start
*
* @author: wangshuai
* @date: 2024/3/12 10:03
*/
@Slf4j
@Configuration
public class FlywayConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    /**
     * Whether to turn onflyway
     */
    @Value("${spring.flyway.enabled:false}")
    private Boolean enabled;
    
    /**
     * encoding format，defaultUTF-8
     */
    @Value("${spring.flyway.encoding:UTF-8}")
    private String encoding;

    /**
     * migratesqlScript file storage path，官方defaultdb/migration
     */
    @Value("${spring.flyway.locations:classpath:flyway/sql/mysql}")
    private String locations;

    /**
     * migratesqlPrefix for script file name，defaultV
     */
    @Value("${spring.flyway.sql-migration-prefix:V}")
    private String sqlMigrationPrefix;

    /**
     * migratesqlSeparator for script file names，default2underline__
     */
    @Value("${spring.flyway.sql-migration-separator:__}")
    private String sqlMigrationSeparator;

    /**
     * text prefix
     */
    @Value("${spring.flyway.placeholder-prefix:#(}")
    private String placeholderPrefix;

    /**
     * text suffix
     */
    @Value("${spring.flyway.placeholder-suffix:)}")
    private String placeholderSuffix;

    /**
     * migratesqlScript file name suffix
     */
    @Value("${spring.flyway.sql-migration-suffixes:.sql}")
    private String sqlMigrationSuffixes;

    /**
     * migrate时是否进行校验，defaulttrue
     */
    @Value("${spring.flyway.validate-on-migrate:true}")
    private Boolean validateOnMigrate;

    /**
     * 当migrate发现数据库非空且存在没有元数据的surface时，自动implement基准migrate，Newschema_versionsurface
     */
    @Value("${spring.flyway.baseline-on-migrate:true}")
    private Boolean baselineOnMigrate;

    /**
     * 是否关闭要清除已有库下的surface功能,The production environment must betrue,Otherwise the library will be deleted，very important！！！
     */
    @Value("${spring.flyway.clean-disabled:true}")
    private Boolean cleanDisabled;
    
//    @Bean
//    public void migrate() {
//        if(!enabled){
//            return;
//        }
//
//        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
//        Map<String, DataSource> dataSources = ds.getDataSources();
//        dataSources.forEach((k, v) -> {
//            if("master".equals(k)){
//                String databaseType = environment.getProperty("spring.datasource.dynamic.datasource." + k + ".url");
//                if (databaseType != null && databaseType.contains("mysql")) {
//                    try {
//                            Flyway flyway = Flyway.configure()
//                                    .dataSource(v)
//                                    .locations(locations)
//                                    .encoding(encoding)
//                                    .sqlMigrationPrefix(sqlMigrationPrefix)
//                                    .sqlMigrationSeparator(sqlMigrationSeparator)
//                                    .placeholderPrefix(placeholderPrefix)
//                                    .placeholderSuffix(placeholderSuffix)
//                                    .sqlMigrationSuffixes(sqlMigrationSuffixes)
//                                    .validateOnMigrate(validateOnMigrate)
//                                    .baselineOnMigrate(baselineOnMigrate)
//                                    .cleanDisabled(cleanDisabled)
//                                    .load();
//                            flyway.migrate();
//                            log.info("【Upgrade tips】The platform integratesMySQLlibraryFlyway，Database version automatically upgraded! ");
//                    } catch (FlywayException e) {
//                        log.error("【Upgrade tips】flywayimplementsqlScript failed", e);
//                    }
//                } else {
//                    log.warn("【Upgrade tips】The platform only integratesMySQLlibraryFlyway，实现了Database version automatically upgraded! Other types of databases，You may consider upgrading manually~");
//                }
//            }
//        });
//    }
}