package org.jeecg.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.filters.GlobalOpenApiMethodFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author eightmonth
 */
@Slf4j
@Configuration
@PropertySource("classpath:config/default-spring-doc.properties")
public class Swagger3Config implements WebMvcConfigurer {
    // Define a set of paths that do not require injection of security requirements
    Set<String> excludedPaths = new HashSet<>(Arrays.asList(
            "/sys/randomImage/{key}",
            "/sys/login",
            "/sys/phoneLogin",
            "/sys/mLogin",
            "/sys/sms",
            "/sys/cas/client/validateLogin",
            "/test/jeecgDemo/demo3",
            "/sys/thirdLogin/**",
            "/sys/user/register"
    ));

    /**
     *
     * showswagger-ui.htmlDocument display page，must also be injectedswaggerresource：
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public GlobalOpenApiMethodFilter globalOpenApiMethodFilter() {
        return method -> method.isAnnotationPresent(Operation.class);
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            String path = getFullPath(handlerMethod);
            if (!isExcludedPath(path)) {
                operation.addSecurityItem(new SecurityRequirement().addList(CommonConstant.X_ACCESS_TOKEN));
            }else{
                log.info("ignore join X_ACCESS_TOKEN of PATH:" + path);
            }
            return operation;
        };
    }

    private String getFullPath(HandlerMethod handlerMethod) {
        StringBuilder fullPath = new StringBuilder();

        // 获取类级别of路径
        RequestMapping classMapping = handlerMethod.getBeanType().getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            fullPath.append(classMapping.value()[0]);
        }

        // 获取方法级别of路径
        RequestMapping methodMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);
        if (methodMapping != null && methodMapping.value().length > 0) {
            String methodPath = methodMapping.value()[0];
            // Make sure the paths are spliced ​​correctly，handling slashes
            if (!fullPath.toString().endsWith("/") && !methodPath.startsWith("/")) {
                fullPath.append("/");
            }
            fullPath.append(methodPath);
        }

        return fullPath.toString();
    }
    
    
    private boolean isExcludedPath(String path) {
        return excludedPaths.stream()
                .anyMatch(pattern -> {
                    if (pattern.endsWith("/**")) {
                        // Handle wildcard matching
                        String basePath = pattern.substring(0, pattern.length() - 3);
                        return path.startsWith(basePath);
                    }
                    // exact match
                    return pattern.equals(path);
                });
    }
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JeecgBoot Background serviceAPIInterface documentation")
                        .version("3.8.3")
                        .contact(new Contact().name("Beijing Guoju Information Technology Co., Ltd.").url("www.jeccg.com").email("jeecgos@163.com"))
                        .description("BackstageAPIinterface")
                        .termsOfService("NO terms of service")
                        .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList(CommonConstant.X_ACCESS_TOKEN))
                .components(new Components().addSecuritySchemes(CommonConstant.X_ACCESS_TOKEN,
                        new SecurityScheme()
                                .name(CommonConstant.X_ACCESS_TOKEN)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER) // key：designated as header
                ));
    }
}