package org.jeecg.config;

import org.jeecg.config.vo.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;


/**
 * Load project configuration
 * @author: jeecg-boot
 */
@Component("jeecgBaseConfig")
@ConfigurationProperties(prefix = "jeecg")
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class JeecgBaseConfig {
    /**
     * Signing key string(Sensitive interfaces such as dictionaries)
     * @TODO Reduce the default value of using cost plus,Actually with ymlConfiguration Subject to
     */
    private String signatureSecret = "dd05f1c54d63749eda95f9fa6d49v442a";
    /**
     * Custom background resource prefix，Solve the problem that the form designer cannot pass the front endnginxForward access
     */
    private String customResourcePrefixPath;
    /**
     * List of interfaces that need to be strengthened
     */
    private String signUrls;
    /**
     * Upload mode  
     * local：local\Minio：minio\Alibaba Cloud：alioss
     */
    private String uploadType;
    
    /**
     * 平台安全模式Configuration
     */
    private Firewall firewall;
    
    /**
     * shirointerception exclusion
     */
    private Shiro shiro;
    /**
     * 上传文件Configuration
     */
    private Path path;

    /**
     * Front-end page access address
     * pc: http://localhost:3100
     * app: http://localhost:8051
     */
    private DomainUrl domainUrl;

    /**
     * File preview
     */
    private String fileViewDomain;
     /**
     * ESConfiguration
     */
    private Elasticsearch elasticsearch;

    /**
     * WeChat Pay
     * @return
     */
    private WeiXinPay weiXinPay;

    /**
     * Baidu is openAPIConfiguration
     */
    private BaiduApi baiduApi;    

    public String getCustomResourcePrefixPath() {
        return customResourcePrefixPath;
    }

    public void setCustomResourcePrefixPath(String customResourcePrefixPath) {
        this.customResourcePrefixPath = customResourcePrefixPath;
    }

    public Elasticsearch getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(Elasticsearch elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public Firewall getFirewall() {
        return firewall;
    }

    public void setFirewall(Firewall firewall) {
        this.firewall = firewall;
    }

    public String getSignatureSecret() {
        return signatureSecret;
    }

    public void setSignatureSecret(String signatureSecret) {
        this.signatureSecret = signatureSecret;
    }

    public Shiro getShiro() {
        return shiro;
    }

    public void setShiro(Shiro shiro) {
        this.shiro = shiro;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public DomainUrl getDomainUrl() {
        return domainUrl;
    }

    public void setDomainUrl(DomainUrl domainUrl) {
        this.domainUrl = domainUrl;
    }
    public String getSignUrls() {
        return signUrls;
    }

    public void setSignUrls(String signUrls) {
        this.signUrls = signUrls;
    }


    public String getFileViewDomain() {
        return fileViewDomain;
    }

    public void setFileViewDomain(String fileViewDomain) {
        this.fileViewDomain = fileViewDomain;
    }

    public String getUploadType() {
        return uploadType;
    }

    public void setUploadType(String uploadType) {
        this.uploadType = uploadType;
    }

    public WeiXinPay getWeiXinPay() {
        return weiXinPay;
    }

    public void setWeiXinPay(WeiXinPay weiXinPay) {
        this.weiXinPay = weiXinPay;
    }

    public BaiduApi getBaiduApi() {
        return baiduApi;
    }

    public void setBaiduApi(BaiduApi baiduApi) {
        this.baiduApi = baiduApi;
    }

}
