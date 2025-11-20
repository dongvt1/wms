package org.jeecg.monitor.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * @author scott
 */
@Configuration
public class SecuritySecureConfig {

    private final String adminContextPath;

    public SecuritySecureConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }


    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // Login success processing class
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminContextPath + "/");

        http.authorizeRequests(authorize -> {
                    try {
                        authorize

                                //Static files are allowed access
                                .requestMatchers(adminContextPath + "/assets/**").permitAll()
                                //Login page allows access
                                .requestMatchers(adminContextPath + "/login", "/css/**", "/js/**", "/image/*").permitAll()
                                //All other requests require login
                                .anyRequest().authenticated()
                                .and()
                                //Login page configuration，used to replacesecurity默认页面
                                .formLogin(formLogin -> formLogin.loginPage(adminContextPath + "/login").successHandler(successHandler))
                                //登出页面配置，used to replacesecurity默认页面
                                .logout(logout -> logout.logoutUrl(adminContextPath + "/logout"))
                                .httpBasic(Customizer.withDefaults())
                                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                        .ignoringRequestMatchers(
                                        "/instances",
                                        "/actuator/**")
                                );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        return http.build();

    }

}