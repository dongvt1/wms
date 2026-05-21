package org.jeecg.config.init;

import com.alibaba.druid.filter.config.ConfigTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecgframework.codegenerate.database.CodegenDatasourceConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: code generator,CustomizeDBConfiguration
 * 【Added this category，butonlinemodelDBconnect，使用平台的Configuration，jeecg_database.propertiesConfiguration无效;
 *  But useGUImodel代码生成，Or gojeecg_database.propertiesConfiguration】
 *  remind： Dameng database needs to modify the following parameters${spring.datasource.dynamic.datasource.master.url:}Configuration
 * @author: scott
 * @date: 2021Year02moon18day 16:30
 * 
 * Important note：Change the path or name of this type，Need to be modified simultaneously
 *  org/jeecg/interceptor/OnlineRepairCodeGenerateDbConfig.javaAnnotations inside
 *  @ConditionalOnMissingClass("org.jeecg.config.init.CodeGenerateDbConfig")
 */
@Slf4j
@Configuration
public class CodeGenerateDbConfig {
    @Value("${spring.datasource.dynamic.datasource.master.url:}")
    private String url;
    @Value("${spring.datasource.dynamic.datasource.master.username:}")
    private String username;
    @Value("${spring.datasource.dynamic.datasource.master.password:}")
    private String password;
    @Value("${spring.datasource.dynamic.datasource.master.driver-class-name:}")
    private String driverClassName;
    @Value("${spring.datasource.dynamic.datasource.master.druid.public-key:}")
    private String publicKey;


    @Bean
    public CodeGenerateDbConfig initCodeGenerateDbConfig() {
        if(StringUtils.isNotBlank(url)){
            if(StringUtils.isNotBlank(publicKey)){
                try {
                    password = ConfigTools.decrypt(publicKey, password);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(" code generator数据库connect，Database password decryption failed！");
                }
            }
            CodegenDatasourceConfig.initDbConfig(driverClassName,url, username, password);
            log.info(" Init CodeGenerate Config [ Get Db Config From application.yml ] ");
        }
        return null;
    }
}
