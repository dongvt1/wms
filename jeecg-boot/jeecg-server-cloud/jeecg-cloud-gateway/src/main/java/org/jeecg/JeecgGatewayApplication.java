package org.jeecg;

import org.jeecg.loader.DynamicRouteLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import jakarta.annotation.Resource;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author jeecg
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class JeecgGatewayApplication  implements CommandLineRunner {
    @Resource
    private DynamicRouteLoader dynamicRouteLoader;

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(JeecgGatewayApplication.class, args);
        //String userName = applicationContext.getEnvironment().getProperty("jeecg.test");
        //System.err.println("user name :" +userName);
    }

    /**
     * Load routes after container initialization
     * @param strings
     */
    @Override
    public void run(String... strings) {
        dynamicRouteLoader.refresh(null);
    }

    /**
     * interface address（pass9999Port direct access）
     * Already usedknife4j-gatewaySupport this feature
     * @param indexHtml
     * @return
     */
    @Bean
    public RouterFunction<ServerResponse> indexRouter(@Value("classpath:/META-INF/resources/doc.html") final org.springframework.core.io.Resource indexHtml) {
        return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).syncBody(indexHtml));
    }
}
