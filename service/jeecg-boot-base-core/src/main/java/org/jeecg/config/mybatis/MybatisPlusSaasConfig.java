package org.jeecg.config.mybatis;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.log.Log;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.TenantConstant;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Single data source configuration（jeecg.datasource.open = falseeffective when）
 * @Author zhoujf
 *
 */
@Slf4j
@Configuration
@MapperScan(value={"org.jeecg.**.mapper*"})
public class MybatisPlusSaasConfig {
    @Autowired
    private DataSource dataSource;
    
    /**
     * Whether to enable tenant isolation for system modules
     *  span of control：user、Role、department、我的department、dictionary、分类dictionary、Multiple data sources、Position、Notices and Announcements
     *  
     *  Implement function
     *  1.user表通过硬编码accomplish租户IDisolation
     *  2.Role、department、我的department、dictionary、分类dictionary、Multiple data sources、Position、Notices and Announcements除了硬编码还加入的 TENANT_TABLE Configuring，accomplish租户isolation更安全
     *  3.menu table、租户表不做租户isolation
     *  4.via interceptorMybatisInterceptoraccomplish，Add, delete, modify and check data Automatically inject tenantsID
     */
    public static final Boolean OPEN_SYSTEM_TENANT_CONTROL = false;
    
    /**
     * Which tables need to be multi-tenant? The table needs to add a field tenant_id
     */
    public static final List<String> TENANT_TABLE = new ArrayList<String>();

    static {
        //1.需要租户isolation的表请exist此配置
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            //a.System management table
            //TENANT_TABLE.add("sys_role");
            //TENANT_TABLE.add("sys_user_role");
            TENANT_TABLE.add("sys_depart");
            TENANT_TABLE.add("sys_category");
            TENANT_TABLE.add("sys_data_source");
            TENANT_TABLE.add("sys_position");
            //b-2.Dashboard
            TENANT_TABLE.add("onl_drag_page");
            TENANT_TABLE.add("onl_drag_dataset_head");
            TENANT_TABLE.add("jimu_report_data_source");
            TENANT_TABLE.add("jimu_report");
            TENANT_TABLE.add("jimu_dict");
            //b-4.AIRAG
            TENANT_TABLE.add("airag_app");
            TENANT_TABLE.add("airag_flow");
            TENANT_TABLE.add("airag_knowledge");
            TENANT_TABLE.add("airag_knowledge_doc");
            TENANT_TABLE.add("airag_model");
        }

        //2.Sample test
        //TENANT_TABLE.add("demo");
        //3.online租户isolation测试
        //TENANT_TABLE.add("ceapp_issue");
    }


    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // First add TenantLineInnerInterceptor Again add PaginationInnerInterceptor
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String tenantId = TenantContext.getTenant();
                //If you get the tenant through a threadIDis empty，then through the current requestrequestGet tenants（shiroRequests that exclude interceptors will not be able to obtain tenantsID）
                if(oConvertUtils.isEmpty(tenantId)){
                    try {
                        tenantId = TokenUtils.getTenantIdByRequest(SpringContextUtils.getHttpServletRequest());
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
                if(oConvertUtils.isEmpty(tenantId)){
                    tenantId = "0";
                }
                return new LongValue(tenantId);
            }

            @Override
            public String getTenantIdColumn(){
                return TenantConstant.TENANT_ID_TABLE;
            }

            // return true Indicates that tenant logic is not followed
            @Override
            public boolean ignoreTable(String tableName) {
                for(String temp: TENANT_TABLE){
                    if(temp.equalsIgnoreCase(tableName)){
                        return false;
                    }
                }
                return true;
            }
        }));
        //update-begin-author:zyf date:20220425 for:【VUEN-606】Inject dynamic table name adaptation interceptor to solve the problem of multiple table names
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor());
        //update-end-author:zyf date:20220425 for:【VUEN-606】Inject dynamic table name adaptation interceptor to solve the problem of multiple table names
        
        //update-begin---author:scott ---date:2025-08-02  for：【issues/8666】upgrademybatisPlusbackSqlServerUse pagingOFFSET ？ ROWS FETCH NEXT ？ ROWS ONLY，lead toonlineReport error---
        DbType dbType = null;
        try {
             dbType = JdbcUtils.getDbType(dataSource.getConnection().getMetaData().getURL());
             log.info("Current database type: {}", dbType);
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }
        if (dbType!=null && (dbType == DbType.SQL_SERVER || dbType == DbType.SQL_SERVER2005)) {
            // in the case ofSQL ServerThen the coverage is2005Paging mode
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.SQL_SERVER2005));
        } else {
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        }
        //update-end---author:scott ---date::2025-08-02  for：【issues/8666】upgrademybatisPlusbackSqlServerUse pagingOFFSET ？ ROWS FETCH NEXT ？ ROWS ONLY，lead toonlineReport error---
        
        //【jeecg-boot/issues/3847】Increase@VersionOptimistic locking support 
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * Dynamic table name switching interceptor,for adaptationvue2andvue3There are multiple instances of the same table,likesys_role_indexexistvue3In this case the table name issys_role_index_v3
     * @return
     */
    private DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor() {
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            //Get the table name that needs to be dynamically parsed
            String dynamicTableName = ThreadLocalDataHelper.get(CommonConstant.DYNAMIC_TABLE_NAME);
            //whendynamicTableName不is empty时才走动态表名处理逻辑,否则returnOriginal table name
            if (ObjectUtil.isNotEmpty(dynamicTableName) && dynamicTableName.equals(tableName)) {
                // Get the version number identifier passed by the front end
                Object version = ThreadLocalDataHelper.get(CommonConstant.VERSION);
                if (ObjectUtil.isNotEmpty(version)) {
                    //Splicing table name rules(Original table name+Underline+The version number passed by the front end)
                    return tableName + "_" + version;
                }
            }
            return tableName;
        });
        return dynamicTableNameInnerInterceptor;
    }

//    /**
//     * It will be deleted in the next version，现exist为了避免缓存出现问题不得不配置
//     * @return
//     */
//    @Bean
//    public ConfigurationCustomizer configurationCustomizer() {
//        return configuration -> configuration.setUseDeprecatedExecutor(false);
//    }
//    /**
//     * mybatis-plus SQLExecution efficiency plug-in【The production environment can be shut down】
//     */
//    @Bean
//    public PerformanceInterceptor performanceInterceptor() {
//        return new PerformanceInterceptor();
//    }

}
