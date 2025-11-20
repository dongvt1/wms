//package org.jeecg.config;
//
//
//import io.swagger.v3.oas.annotations.Operation;
//import org.jeecg.common.constant.CommonConstant;
//import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.config.BeanPostProcessor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//import org.springframework.util.ReflectionUtils;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
//import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.ParameterBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.schema.ModelRef;
//import springfox.documentation.service.*;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spi.service.contexts.SecurityContext;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
//import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * @Author scott
// */
//@Configuration
//@EnableSwagger2WebMvc
//@Import(BeanValidatorPluginsConfiguration.class)
//public class Swagger2Config implements WebMvcConfigurer {
//
//    /**
//     *
//     * showswagger-ui.htmlDocument display page，must also be injectedswaggerresource：
//     *
//     * @param registry
//     */
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
//
//    /**
//     * swagger2configuration file，You can configure it hereswagger2some basic content，For example, scanned packages, etc.
//     *
//     * @return Docket
//     */
//    @Bean(value = "defaultApi2")
//    public Docket defaultApi2() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(apiInfo())
//                .select()
//                //Classes under this package path，Generate interface document
//                .apis(RequestHandlerSelectors.basePackage("org.jeecg"))
//                //addApiOperationannotated class，Generate interface document
//                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
//                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
//                .paths(PathSelectors.any())
//                .build()
//                .securitySchemes(Collections.singletonList(securityScheme()))
//                .securityContexts(securityContexts())
//                .globalOperationParameters(setHeaderToken());
//    }
//
//    /***
//     * oauth2Configuration
//     * need to increaseswaggerAuthorization callback address
//     * http://localhost:8888/webjars/springfox-swagger-ui/o2c.html
//     * @return
//     */
//    @Bean
//    SecurityScheme securityScheme() {
//        return new ApiKey(CommonConstant.X_ACCESS_TOKEN, CommonConstant.X_ACCESS_TOKEN, "header");
//    }
//    /**
//     * JWT token
//     * @return
//     */
//    private List<Parameter> setHeaderToken() {
//        ParameterBuilder tokenPar = new ParameterBuilder();
//        List<Parameter> pars = new ArrayList<>();
//        tokenPar.name(CommonConstant.X_ACCESS_TOKEN).description("token").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
//        pars.add(tokenPar.build());
//        //update-begin-author:liusq---date:2024-08-15--for:  When multi-tenancy is enabled，Add global parameters to tenantid
//        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
//            ParameterBuilder tenantPar = new ParameterBuilder();
//            tenantPar.name(CommonConstant.TENANT_ID).description("tenantID").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
//            pars.add(tenantPar.build());
//        }
//        //update-end-author:liusq---date:2024-08-15--for: When multi-tenancy is enabled，Add global parameters to tenantid
//
//        return pars;
//    }
//
//    /**
//     * apiDocument details function,Note which annotation here refers to
//     *
//     * @return
//     */
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder()
//                // //headline
//                .title("JeecgBoot Background serviceAPIInterface documentation")
//                // version number
//                .version("1.0")
////				.termsOfServiceUrl("NO terms of service")
//                // describe
//                .description("BackstageAPIinterface")
//                // author
//                .contact(new Contact("Beijing Guoju Information Technology Co., Ltd.","www.jeccg.com","jeecgos@163.com"))
//                .license("The Apache License, Version 2.0")
//                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
//                .build();
//    }
//
//    /**
//     * New securityContexts Keep me logged in
//     */
//    private List<SecurityContext> securityContexts() {
//        return new ArrayList(
//                Collections.singleton(SecurityContext.builder()
//                        .securityReferences(defaultAuth())
//                        .forPaths(PathSelectors.regex("^(?!auth).*$"))
//                        .build())
//        );
//    }
//
//    private List<SecurityReference> defaultAuth() {
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        authorizationScopes[0] = authorizationScope;
//        return new ArrayList(
//                Collections.singleton(new SecurityReference(CommonConstant.X_ACCESS_TOKEN, authorizationScopes)));
//    }
//
//    /**
//     * solvespringboot2.6 andspringfoxIncompatibility issues
//     * @return
//     */
//    @Bean
//    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
//        return new BeanPostProcessor() {
//
//            @Override
//            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//                if (bean instanceof WebMvcRequestHandlerProvider) {
//                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
//                }
//                return bean;
//            }
//
//            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
//                List<T> copy = mappings.stream()
//                        .filter(mapping -> mapping.getPatternParser() == null)
//                        .collect(Collectors.toList());
//                mappings.clear();
//                mappings.addAll(copy);
//            }
//
//            @SuppressWarnings("unchecked")
//            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
//                try {
//                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
//                    field.setAccessible(true);
//                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
//                } catch (IllegalArgumentException | IllegalAccessException e) {
//                    throw new IllegalStateException(e);
//                }
//            }
//        };
//    }
//
//
//}
