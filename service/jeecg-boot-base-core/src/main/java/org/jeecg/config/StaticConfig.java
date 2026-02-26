package org.jeecg.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Set static parameter initialization
 * @author: jeecg-boot
 */
@Lazy(false)
@Component
@Data
public class StaticConfig {

    @Value("${jeecg.oss.accessKey:}")
    private String accessKeyId;

    @Value("${jeecg.oss.secretKey:}")
    private String accessKeySecret;

    @Value(value = "${spring.mail.username:}")
    private String emailFrom;

    /**
     * Whether to enable scheduled sending
     */
    @Value(value = "${spring.mail.timeJobSend:false}")
    private Boolean timeJobSend;

//    /**
//     * Signing key string
//     */
//    @Value(value = "${jeecg.signatureSecret}")
//    private String signatureSecret;


    /*@Bean
    public void initStatic() {
       DySmsHelper.setAccessKeyId(accessKeyId);
       DySmsHelper.setAccessKeySecret(accessKeySecret);
       EmailSendMsgHandle.setEmailFrom(emailFrom);
    }*/

}
