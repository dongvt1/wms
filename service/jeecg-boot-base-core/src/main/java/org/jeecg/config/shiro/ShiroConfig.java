package org.jeecg.config.shiro;

import jakarta.annotation.Resource;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.ShiroUrlPathHelper;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.*;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.shiro.filters.CustomShiroFilterFactoryBean;
import org.jeecg.config.shiro.filters.JwtFilter;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.*;

/**
 * @author: Scott
 * @date: 2018/2/7
 * @description: shiro Configuration class
 */

@Slf4j
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ShiroConfig {

    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;
    @Autowired
    private Environment env;
    @Resource
    private JeecgBaseConfig jeecgBaseConfig;
    @Autowired(required = false)
    private RedisProperties redisProperties;
    
    /**
     * Filter ChainDefinition
     *
     * 1、oneURLCan configure multipleFilter，Use commas to separate
     * 2、When setting multiple filters，All verified，is deemed to be passed
     * 3、Some filters can specify parameters，likeperms，roles
     */
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        CustomShiroFilterFactoryBean shiroFilterFactoryBean = new CustomShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // Interceptor
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();

        //supportymlWay，Configure interception exclusions
        if(jeecgBaseConfig!=null && jeecgBaseConfig.getShiro()!=null){
            String shiroExcludeUrls = jeecgBaseConfig.getShiro().getExcludeUrls();
            if(oConvertUtils.isNotEmpty(shiroExcludeUrls)){
                String[] permissionUrl = shiroExcludeUrls.split(",");
                for(String url : permissionUrl){
                    filterChainDefinitionMap.put(url,"anon");
                }
            }
        }

        // Configure links that will not be blocked order judgment
        filterChainDefinitionMap.put("/sys/cas/client/validateLogin", "anon"); //casVerify login
        filterChainDefinitionMap.put("/sys/randomImage/**", "anon"); //Login verification code interface exclusion
        filterChainDefinitionMap.put("/sys/checkCaptcha", "anon"); //Login verification code interface exclusion
        filterChainDefinitionMap.put("/sys/smsCheckCaptcha", "anon"); //Troubleshooting by sending too many verification codes via SMS
        filterChainDefinitionMap.put("/sys/login", "anon"); //Login interface exclusion
        filterChainDefinitionMap.put("/sys/mLogin", "anon"); //Login interface exclusion
        filterChainDefinitionMap.put("/sys/logout", "anon"); //Logout interface exclusion
        filterChainDefinitionMap.put("/sys/thirdLogin/**", "anon"); //Third party login
        filterChainDefinitionMap.put("/sys/getEncryptedString", "anon"); //Get encrypted string
        filterChainDefinitionMap.put("/sys/sms", "anon");//SMS verification code
        filterChainDefinitionMap.put("/sys/phoneLogin", "anon");//Mobile login
        filterChainDefinitionMap.put("/sys/user/checkOnlyUser", "anon");//Verify that the user exists
        filterChainDefinitionMap.put("/sys/user/register", "anon");//User registration
        filterChainDefinitionMap.put("/sys/user/phoneVerification", "anon");//User forgets password to verify mobile phone number
        filterChainDefinitionMap.put("/sys/user/passwordChange", "anon");//User changes password
        filterChainDefinitionMap.put("/auth/2step-code", "anon");//Login verification code
        filterChainDefinitionMap.put("/sys/common/static/**", "anon");//Picture preview &No limit on downloading filestoken
        filterChainDefinitionMap.put("/sys/common/pdf/**", "anon");//pdfPreview

        //filterChainDefinitionMap.put("/sys/common/view/**", "anon");//Picture preview不限制token
        //filterChainDefinitionMap.put("/sys/common/download/**", "anon");//No restrictions on file downloadstoken
        filterChainDefinitionMap.put("/generic/**", "anon");//pdfPreview需要文件

        filterChainDefinitionMap.put("/sys/getLoginQrcode/**", "anon"); //Login QR code
        filterChainDefinitionMap.put("/sys/getQrcodeToken/**", "anon"); //Monitor code scanning
        filterChainDefinitionMap.put("/sys/checkAuth", "anon"); //Authorization interface exclusion
        filterChainDefinitionMap.put("/openapi/call/**", "anon"); // Open platform interface exclusion

        //update-begin--Author:scott Date:20221116 for：Exclude static resource suffixes
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/**/*.js", "anon");
        filterChainDefinitionMap.put("/**/*.css", "anon");
        filterChainDefinitionMap.put("/**/*.html", "anon");
        filterChainDefinitionMap.put("/**/*.svg", "anon");
        filterChainDefinitionMap.put("/**/*.pdf", "anon");
        filterChainDefinitionMap.put("/**/*.jpg", "anon");
        filterChainDefinitionMap.put("/**/*.png", "anon");
        filterChainDefinitionMap.put("/**/*.gif", "anon");
        filterChainDefinitionMap.put("/**/*.ico", "anon");
        filterChainDefinitionMap.put("/**/*.ttf", "anon");
        filterChainDefinitionMap.put("/**/*.woff", "anon");
        filterChainDefinitionMap.put("/**/*.woff2", "anon");

        filterChainDefinitionMap.put("/**/*.glb", "anon");
        filterChainDefinitionMap.put("/**/*.wasm", "anon");
        //update-end--Author:scott Date:20221116 for：Exclude static resource suffixes

        filterChainDefinitionMap.put("/druid/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger**/**", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v3/**", "anon");

        // update-begin--Author:sunjianlei Date:20210510 for：Exclude message notification view details page（for third partiesAPP）
        filterChainDefinitionMap.put("/sys/annountCement/show/**", "anon");
        // update-end--Author:sunjianlei Date:20210510 for：Exclude message notification view details page（for third partiesAPP）

        //Building block report exclusion
        filterChainDefinitionMap.put("/jmreport/**", "anon");
        filterChainDefinitionMap.put("/**/*.js.map", "anon");
        filterChainDefinitionMap.put("/**/*.css.map", "anon");
        
        //Building BlocksBILarge screens and dashboards excluded
        filterChainDefinitionMap.put("/drag/view", "anon");
        filterChainDefinitionMap.put("/drag/page/queryById", "anon");
        filterChainDefinitionMap.put("/drag/page/addVisitsNumber", "anon");
        filterChainDefinitionMap.put("/drag/page/queryTemplateList", "anon");
        filterChainDefinitionMap.put("/drag/share/view/**", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/getAllChartData", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/getTotalData", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/getMapDataByCode", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/getTotalDataByCompId", "anon");
        filterChainDefinitionMap.put("/drag/mock/json/**", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/getDictByCodes", "anon");
        filterChainDefinitionMap.put("/drag/onlDragDatasetHead/queryAllById", "anon");
        filterChainDefinitionMap.put("/jimubi/view", "anon");
        filterChainDefinitionMap.put("/jimubi/share/view/**", "anon");

        //Large screen template example
        filterChainDefinitionMap.put("/test/bigScreen/**", "anon");
        filterChainDefinitionMap.put("/bigscreen/template1/**", "anon");
        filterChainDefinitionMap.put("/bigscreen/template2/**", "anon");
        //filterChainDefinitionMap.put("/test/jeecgDemo/rabbitMqClientTest/**", "anon"); //MQtest
        //filterChainDefinitionMap.put("/test/jeecgDemo/html", "anon"); //template page
        //filterChainDefinitionMap.put("/test/jeecgDemo/redis/**", "anon"); //redistest

        //websocketexclude
        filterChainDefinitionMap.put("/websocket/**", "anon");//System notifications and announcements
        filterChainDefinitionMap.put("/newsWebsocket/**", "anon");//CMSmodule
        filterChainDefinitionMap.put("/vxeSocket/**", "anon");//JVxeTableExample of invisible refresh
        //App vue3Version query version interface
        filterChainDefinitionMap.put("/sys/version/app3version", "anon");
        //Dashboard（Button communication）
        filterChainDefinitionMap.put("/dragChannelSocket/**","anon");
        //App vue3Version query version interface
        filterChainDefinitionMap.put("/sys/version/app3version", "anon");

        //Performance monitoring——Security risks leakedTOEKN（duridThere is also a connection pool）
        //filterChainDefinitionMap.put("/actuator/**", "anon");
        //testmoduleexclude
        filterChainDefinitionMap.put("/test/seata/**", "anon");

        //错误路径exclude
        filterChainDefinitionMap.put("/error", "anon");
        // 企业微信证书exclude
        filterChainDefinitionMap.put("/WW_verify*", "anon");

        // Add your own filter and name itjwt
        Map<String, Filter> filterMap = new HashMap<String, Filter>(1);
        //like果cloudServeris empty It means it is a single body Cross-domain configuration needs to be loaded【Microservice cross-domain switching】
        Object cloudServer = env.getProperty(CommonConstant.CLOUD_SERVER_KEY);
        filterMap.put("jwt", new JwtFilter(cloudServer==null));
        shiroFilterFactoryBean.setFilters(filterMap);
        // <!-- Filter chain definition，Execute sequentially from top to bottom，Generally will/**Put it at the bottom
        filterChainDefinitionMap.put("/**", "jwt");

        // Unauthorized interface returnJSON
        shiroFilterFactoryBean.setUnauthorizedUrl("/sys/common/403");
        shiroFilterFactoryBean.setLoginUrl("/sys/common/403");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    //update-begin---author:chenrui ---date:20240126  for：【QQYUN-7932】AIassistant------------

    /**
     * springfilter decorator <br/>
     * becauseshirooffilter不support异步请求,导致所有of异步请求都会报错. <br/>
     * So it is necessary to usespringofFilterRegistrationBeanAgent againshirooffilter.为他扩展异步support. <br/>
     * 后续所有异步of接口都需要再这里增加registration.addUrlPatterns("/xxx/xxx");
     * @return
     * @author chenrui
     * @date 2024/12/3 19:49
     */
    @Bean
    public FilterRegistrationBean shiroFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new DelegatingFilterProxy("shiroFilterFactoryBean"));
        registration.setEnabled(true);
        //update-begin---author:chenrui ---date:20241202  for：[issues/7491]Running time is so long，Slow efficiency ------------
        registration.addUrlPatterns("/test/ai/chat/send");
        //update-end---author:chenrui ---date:20241202  for：[issues/7491]Running time is so long，Slow efficiency ------------
        registration.addUrlPatterns("/airag/flow/run");
        registration.addUrlPatterns("/airag/flow/debug");
        registration.addUrlPatterns("/airag/chat/send");
        registration.addUrlPatterns("/airag/app/debug");
        registration.addUrlPatterns("/airag/app/prompt/generate");
        registration.addUrlPatterns("/airag/chat/receive/**");
        //support异步
        registration.setAsyncSupported(true);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        return registration;
    }
    //update-end---author:chenrui ---date:20240126  for：【QQYUN-7932】AIassistant------------

    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager(ShiroRealm myRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(myRealm);

        /*
         * closureshiro自带ofsession，See documentation for details
         * http://shiro.apache.org/session-management.html#SessionManagement-
         * StatelessApplications%28Sessionless%29
         */
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        //Custom cache implementation,useredis
        securityManager.setCacheManager(redisCacheManager());
        return securityManager;
    }

    /**
     * 下面of代码是添加注解support
     * @return
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        /**
         * Solving duplicate proxy issues github#994
         * Add prefix judgment no match anyAdvisor
         */
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        defaultAdvisorAutoProxyCreator.setAdvisorBeanNamePrefix("_no_advisor");
        return defaultAdvisorAutoProxyCreator;
    }

    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    /**
     * cacheManager cache redisaccomplish
     * useof是shiro-redisOpen source plug-in
     *
     * @return
     */
    public RedisCacheManager redisCacheManager() {
        log.info("===============(1)createcache管理器RedisCacheManager");
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        //redis中针对不同用户cache(此处ofidNeed to corresponduser实体中ofidField,used to uniquely identify)
        redisCacheManager.setPrincipalIdFieldName("id");
        //用户权限信息cache时间
        redisCacheManager.setExpire(200000);
        return redisCacheManager;
    }

    /**
     * RedisConfigin projectstarterUnder project
     * jeecg-boot-starter-github\jeecg-boot-common\src\main\java\org\jeecg\common\modules\redis\config\RedisConfig.java
     * 
     * Configurationshiro redisManager
     * useof是shiro-redisOpen source plug-in
     *
     * @return
     */
    @Bean
    public IRedisManager redisManager() {
        log.info("===============(2)createRedisManager,connectRedis..");
        IRedisManager manager;
        // sentinel cluster redis（【issues/5569】shirointegrated redis 不support sentinel Way部署ofrediscluster #5569）
        if (Objects.nonNull(redisProperties)
                && Objects.nonNull(redisProperties.getSentinel())
                && !CollectionUtils.isEmpty(redisProperties.getSentinel().getNodes())) {
            RedisSentinelManager sentinelManager = new RedisSentinelManager();
            sentinelManager.setMasterName(redisProperties.getSentinel().getMaster());
            sentinelManager.setHost(String.join(",", redisProperties.getSentinel().getNodes()));
            sentinelManager.setPassword(redisProperties.getPassword());
            sentinelManager.setDatabase(redisProperties.getDatabase());

            return sentinelManager;
        }
        
        // redis 单机support，在clusteris empty，或者cluster无机器时候use add by jzyadmin@163.com
        if (lettuceConnectionFactory.getClusterConfiguration() == null || lettuceConnectionFactory.getClusterConfiguration().getClusterNodes().isEmpty()) {
            RedisManager redisManager = new RedisManager();
            redisManager.setHost(lettuceConnectionFactory.getHostName() + ":" + lettuceConnectionFactory.getPort());
            //(lettuceConnectionFactory.getPort());
            redisManager.setDatabase(lettuceConnectionFactory.getDatabase());
            redisManager.setTimeout(0);
            if (!StringUtils.isEmpty(lettuceConnectionFactory.getPassword())) {
                redisManager.setPassword(lettuceConnectionFactory.getPassword());
            }
            manager = redisManager;
        }else{
            // redisclustersupport，优先useclusterConfiguration
            RedisClusterManager redisManager = new RedisClusterManager();
            Set<HostAndPort> portSet = new HashSet<>();
            lettuceConnectionFactory.getClusterConfiguration().getClusterNodes().forEach(node -> portSet.add(new HostAndPort(node.getHost() , node.getPort())));
            //update-begin--Author:scott Date:20210531 for：修改cluster模式下未设置redis密码ofbug issues/I3QNIC
            if (oConvertUtils.isNotEmpty(lettuceConnectionFactory.getPassword())) {
                JedisCluster jedisCluster = new JedisCluster(portSet, 2000, 2000, 5,
                    lettuceConnectionFactory.getPassword(), new GenericObjectPoolConfig());
                redisManager.setPassword(lettuceConnectionFactory.getPassword());
                redisManager.setJedisCluster(jedisCluster);
            } else {
                JedisCluster jedisCluster = new JedisCluster(portSet);
                redisManager.setJedisCluster(jedisCluster);
            }
            //update-end--Author:scott Date:20210531 for：修改cluster模式下未设置redis密码ofbug issues/I3QNIC
            manager = redisManager;
        }
        return manager;
    }

    /**
     * solve ShiroRequestMappingConfig Get requestMappingHandlerMapping Bean conflict
     * spring-boot-autoconfigure:3.4.5 and spring-boot-actuator-autoconfigure:3.4.5
     */
    @Primary
    @Bean
    public RequestMappingHandlerMapping overridedRequestMappingHandlerMapping() {
        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
        mapping.setUrlPathHelper(new ShiroUrlPathHelper());
        return mapping;
    }

    private List<String> rebuildUrl(String[] bases, String[] uris) {
        List<String> urls = new ArrayList<>();
        for (String base : bases) {
            for (String uri : uris) {
                urls.add(prefix(base)+prefix(uri));
            }
        }
        return urls;
    }

    private String prefix(String seg) {
        return seg.startsWith("/") ? seg : "/"+seg;
    }

}
