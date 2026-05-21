package org.jeecg.config;

import java.io.IOException;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot3.autoconfigure.properties.DruidStatProperties;
import jakarta.servlet.*;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.util.Utils;

/**
 * @Description: DruidConfigConfiguration class
 * @author: jeecg-boot
 */
@Configuration
@AutoConfigureAfter(DruidDataSourceAutoConfigure.class)
public class DruidConfig {

    /**
     * with advertisingcommon.jsfull path，druid-1.1.14
     */
    private static final String FILE_PATH = "support/http/resources/js/common.js";
    /**
     * original script，Statements that trigger building ads
     */
    private static final String ORIGIN_JS = "this.buildFooter();";
    /**
     * Replaced script
     */
    private static final String NEW_JS = "//this.buildFooter();";

    /**
     * removeDruidMonitor page for ads
     *
     * @param properties DruidStatPropertiesProperty collection
     * @return {@link FilterRegistrationBean}
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "spring.datasource.druid.stat-view-servlet.enabled", havingValue = "true")
    public FilterRegistrationBean<RemoveAdFilter> removeDruidAdFilter(
            DruidStatProperties properties) throws IOException {
        // GetwebMonitor page parameters
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
        // extractcommon.jsconfiguration path
        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
        // Getcommon.js
        String text = Utils.readFromResource(FILE_PATH);
        // shield this.buildFooter(); Don't build ads
        final String newJs = text.replace(ORIGIN_JS, NEW_JS);
        FilterRegistrationBean<RemoveAdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RemoveAdFilter(newJs));
        registration.addUrlPatterns(commonJsPattern);
        return registration;
    }

    /**
     * deletedruidad filter
     *
     * @author BBF
     */
    private class RemoveAdFilter implements Filter {

        private final String newJs;

        public RemoveAdFilter(String newJs) {
            this.newJs = newJs;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(request, response);
            // reset buffer，Response headers are not reset
            response.resetBuffer();
            response.getWriter().write(newJs);
        }
    }
}
