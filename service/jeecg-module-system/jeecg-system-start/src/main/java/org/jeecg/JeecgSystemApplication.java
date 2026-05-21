package org.jeecg;

import com.xkcoding.justauth.autoconfigure.JustAuthAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
* Single startup class（Use this type of startup to standalone mode）
* Error reminder: Not integratedmongoReport an error，You can open the comments on the startup class exclude={MongoAutoConfiguration.class}
*/
@Slf4j
@SpringBootApplication(exclude = MongoAutoConfiguration.class,scanBasePackages = {"org.jeecg", "com.cy"})
@ImportAutoConfiguration(JustAuthAutoConfiguration.class)  // spring boot 3.x justauth Compatibility processing
@MapperScan(basePackages = {"org.jeecg.modules.**.mapper","com.cy.**.mapper"})
public class JeecgSystemApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(JeecgSystemApplication.class);
    }

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(JeecgSystemApplication.class);
        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("management.health.elasticsearch.enabled", false);
        app.setDefaultProperties(defaultProperties);
        log.info("[JEECG] Elasticsearch Health Check Enabled: false" );
        
        ConfigurableApplicationContext application = app.run(args);;
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = oConvertUtils.getString(env.getProperty("server.servlet.context-path"));
        log.info("\n----------------------------------------------------------\n\t" +
                "Application Jeecg-Boot is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/doc.html\n\t" +
                "Swaggerdocument: \thttp://" + ip + ":" + port + path + "/doc.html\n" +
                "----------------------------------------------------------");

    }

}