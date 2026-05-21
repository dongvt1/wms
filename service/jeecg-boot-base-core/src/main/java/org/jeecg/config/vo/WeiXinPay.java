package org.jeecg.config.vo;

import lombok.Data;

@Data
public class WeiXinPay {
    /**
     * WeChat public accountid
     */
    private String appId;
    /**
     * Merchant numberid
     */
    private String mchId;
    /**
     * Merchant number秘钥
     */
    private String apiKey;
    /**
     * callback address
     */
    private String notifyUrl;
    /**
     * Whether to enable member authentication
     */
    private Boolean openVipLimit;
    /**
     * Certificate path
     */
    private String certPath;
}
