package org.jeecg.config;

import org.jeecg.config.filter.WebsocketFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @Description: WebSocketConfig
 * @author: jeecg-boot
 */
@Configuration
public class WebSocketConfig {
    /**
     * 	injectionServerEndpointExporter，
     * 	thisbeanIt will be automatically registered and used.@ServerEndpointAnnotatedWebsocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public WebsocketFilter websocketFilter(){
        return new WebsocketFilter();
    }

    @Bean
    public FilterRegistrationBean getFilterRegistrationBean(){
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(websocketFilter());
        //TODO Comment out temporarily，Test offlinesocketTotal problem
        bean.addUrlPatterns("/taskCountSocket/*", "/websocket/*","/eoaSocket/*","/eoaNewChatSocket/*", "/newsWebsocket/*", "/dragChannelSocket/*", "/vxeSocket/*");
        return bean;
    }

}
