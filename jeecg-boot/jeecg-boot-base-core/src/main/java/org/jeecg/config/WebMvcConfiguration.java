package org.jeecg.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Spring Boot 2.0 Solve cross-domain issues
 *
 * @Author qinfeng
 *
 */
@Slf4j
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Resource
    JeecgBaseConfig jeecgBaseConfig;
    @Value("${spring.resource.static-locations:}")
    private String staticLocations;

    @Autowired(required = false)
    private PrometheusMeterRegistry prometheusMeterRegistry;

    /**
     * meterRegistryPostProcessor
     * for [QQYUN-12558]【monitor】系统monitorof头两个tabNot easy to use，interface404
     */
    @Autowired(required = false)
    @Qualifier("meterRegistryPostProcessor")
    private BeanPostProcessor meterRegistryPostProcessor;

    /**
     * Configuration of static resources - Makes it possible to read from disk Html、picture、video、Audio etc.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        ResourceHandlerRegistration resourceHandlerRegistration = registry.addResourceHandler("/**");
        if (jeecgBaseConfig.getPath() != null && jeecgBaseConfig.getPath().getUpload() != null) {
            resourceHandlerRegistration
                    .addResourceLocations("file:" + jeecgBaseConfig.getPath().getUpload() + "//")
                    .addResourceLocations("file:" + jeecgBaseConfig.getPath().getWebapp() + "//");
        }
        resourceHandlerRegistration.addResourceLocations(staticLocations.split(","));
        // Set cache control headers Cache-ControlValid for30sky
        resourceHandlerRegistration.setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS));
    }

    /**
     * Option 1： Default access root path jump doc.htmlpage （swagger文档page）
     * Option 2： Change the access root path to jump index.htmlpage （Simplify deployment solutions： You can package the front-end and put it directly into the project webapp，The above configuration）
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/doc.html");
    }

    @Bean
    @Conditional(CorsFilterCondition.class)
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        //Whether to allow requests with verification information
        corsConfiguration.setAllowCredentials(true);
        // Client domain name allowed to access
        corsConfiguration.addAllowedOriginPattern("*");
        // Client request headers that allow server access
        corsConfiguration.addAllowedHeader("*");
        // Method names allowed to be accessed,GET POSTwait
        corsConfiguration.addAllowedMethod("*");
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper());
        converters.add(jackson2HttpMessageConverter);
    }

    /**
     * CustomizeObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        //deal withbigDecimal
        objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        //deal with失败
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
        //默认ofdeal with日期时间格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

    //update-begin---author:chenrui ---date:20240514  for：[QQYUN-9247]系统monitor功能优化------------
//    /**
//     * SpringBootAdminofHttptraceGone
//     * https://blog.csdn.net/u013810234/article/details/110097201
//     */
//    @Bean
//    public InMemoryHttpTraceRepository getInMemoryHttpTrace(){
//        return new InMemoryHttpTraceRepository();
//    }
    //update-end---author:chenrui ---date:20240514  for：[QQYUN-9247]系统monitor功能优化------------


    /**
     * existBeanConfigure immediately after initialization is completedPrometheusMeterRegistry，避免existMeterConfigure after registrationMeterFilter
     * for [QQYUN-12558]【monitor】系统monitorof头两个tabNot easy to use，interface404
     * @author chenrui
     * @date 2025/5/26 16:46
     */
    @PostConstruct
    public void initPrometheusMeterRegistry() {
        // 确保exist应用启动早期就配置MeterFilter，avoid warnings
        if (null != meterRegistryPostProcessor && null != prometheusMeterRegistry) {
            meterRegistryPostProcessor.postProcessAfterInitialization(prometheusMeterRegistry, "prometheusMeterRegistry");
            log.info("PrometheusMeterRegistryConfiguration completed");
        }
    }

//    /**
//     * Register interceptor【Interceptor interception parameters，Automatically switch data sources——Implement multi-tenant switching data source function later】
//     * @param registry
//     */
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new DynamicDatasourceInterceptor()).addPathPatterns("/test/dynamic/**");
//    }

}
