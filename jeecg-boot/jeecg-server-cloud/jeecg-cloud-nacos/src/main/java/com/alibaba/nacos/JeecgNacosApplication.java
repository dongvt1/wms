package com.alibaba.nacos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpServletResponse;


/**
 * Nacos Startup class
 *
 * @author zyf
 */
@SpringBootApplication(scanBasePackages = "com.alibaba.nacos")
@ServletComponentScan
@EnableScheduling
public class JeecgNacosApplication {

    /** Whether to start in stand-alone mode */
    private static String standalone = "true";
    /** Whether to enable authentication */
    private static String enabled = "false";

    public static void main(String[] args) {
        System.setProperty("nacos.standalone", standalone);
        System.setProperty("nacos.core.auth.enabled", enabled);
//        //onceNacosinitialization，usernamenacoswill not be modified，But you can do it via the console orAPIto change password  https://nacos.io/en/blog/faq/nacos-user-question-history8420       
//        System.setProperty("nacos.core.auth.default.username", "nacos");
//        System.setProperty("nacos.core.auth.default.password", "nacos");
        System.setProperty("server.tomcat.basedir","logs");
        //Custom startup port number
        System.setProperty("server.port","8848");
        SpringApplication.run(JeecgNacosApplication.class, args);
    }

    /**
     * Jump to homepage by default
     *
     * @param model
     * @return
     */
    @GetMapping("/")
    public String index(Model model, HttpServletResponse response) {
        // view redirection - Jump
        return "/nacos";
    }
}
