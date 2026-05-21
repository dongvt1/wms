package org.jeecg.config.init;

import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TomcatFactoryConfig
 * @author: scott
 * @date: 2021Year01moon25day 11:40
 */
@Configuration
public class TomcatFactoryConfig {
    /**
     * tomcat-embed-jasperTips after citingjarProblem not found
     */
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
            }
        };
        factory.addConnectorCustomizers(connector -> {
            connector.setProperty("relaxedPathChars", "[]{}");
            connector.setProperty("relaxedQueryChars", "[]{}");
        });
        return factory;
    }
}
