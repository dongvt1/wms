package org.jeecg.handler.swagger;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.*;

/** Already usedknife4j-gatewaySupport this feature
 * aggregation of various servicesswaggerinterface
 * @author zyf
 * @date: 2022/4/21 10:55
 */
@Component
@Slf4j
@Primary
public class MySwaggerResourceProvider implements SwaggerResourcesProvider {
    /**
     * swagger2defaulturlsuffix
     */
    private static final String SWAGGER2URL = "/v3/api-docs";

    /**
     * gateway routing
     */
    private final RouteLocator routeLocator;
    /**
     * Nacosname service
     */
    private NamingService naming;

    /**
     * nacosService address
     */
    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String serverAddr;
    /**
     * nacos namespace
     */
    @Value("${spring.cloud.nacos.discovery.namespace:#{null}}")
    private String namespace;

    /**
     * nacos groupName
     */
    @Value("${spring.cloud.nacos.config.group:DEFAULT_GROUP:#{null}}")
    private String group;

    /**
     * nacos username
     */
    @Value("${spring.cloud.nacos.discovery.username:#{null}}")
    private String username;
    /**
     * nacos password
     */
    @Value("${spring.cloud.nacos.discovery.password:#{null}}")
    private String password;

    /**
     * SwaggerServices that need to be excluded
     */
    private String[] excludeServiceIds=new String[]{"jeecg-cloud-monitor"};


    /**
     * Gateway application name
     */
    @Value("${spring.application.name}")
    private String self;

    @Autowired
    public MySwaggerResourceProvider(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routeHosts = new ArrayList<>();
        // Get all availablehost：serviceId
        routeLocator.getRoutes().filter(route -> route.getUri().getHost() != null)
                .filter(route -> !self.equals(route.getUri().getHost()))
                .subscribe(route ->{
                    //update-begin---author:zyf ---date:20220413 for：Filter out invalid routes,避免interface文档报错无法打开
                    boolean hasRoute=checkRoute(route.getId());
                    if(hasRoute){
                        routeHosts.add(route.getUri().getHost());
                    }
                    //update-end---author:zyf ---date:20220413 for：Filter out invalid routes,避免interface文档报错无法打开
                });

        // Record what has been addedserver，There are multiple services registered for the same application.nacossuperior
        Set<String> dealed = new HashSet<>();
        routeHosts.forEach(instance -> {
            // Splicingurl
            String url = "/" + instance.toLowerCase() + SWAGGER2URL;
            if (!dealed.contains(url)) {
                dealed.add(url);
                log.info(" Gateway add SwaggerResource: {}",url);
                SwaggerResource swaggerResource = new SwaggerResource();
                swaggerResource.setUrl(url);
                swaggerResource.setSwaggerVersion("2.0");
                swaggerResource.setName(instance);
                //SwaggerExclude services from display
                if(!ArrayUtil.contains(excludeServiceIds,instance)){
                    resources.add(swaggerResource);
                }
            }
        });
        return resources;
    }

    /**
     * DetectionnacosIs there a healthy instance in
     * @param routeId
     * @return
     */
    private Boolean checkRoute(String routeId) {
        Boolean hasRoute = false;
        try {
            //Fix starting gateway with namespaceswagger看不到interface文档的问题
            Properties properties=new Properties();
            properties.setProperty("serverAddr",serverAddr);
            if(namespace!=null && !"".equals(namespace)){
                log.info("nacos.discovery.namespace = {}", namespace);
                properties.setProperty("namespace",namespace);
            }
            if(username!=null && !"".equals(username)){
                properties.setProperty("username",username);
            }
            if(password!=null && !"".equals(password)){
                properties.setProperty("password",password);
            }
            //【issues/5115】becauseswaggerDocumentation leads togatewaymemory overflow
            if (this.naming == null) {
                this.naming = NamingFactory.createNamingService(properties);
            }
            log.info(" config.group : {}", group);
            List<Instance> list = this.naming.selectInstances(routeId, group , true);
            if (ObjectUtil.isNotEmpty(list)) {
                hasRoute = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasRoute;
    }
}