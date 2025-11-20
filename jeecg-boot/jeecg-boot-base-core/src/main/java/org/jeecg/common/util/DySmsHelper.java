package org.jeecg.common.util;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.config.JeecgSmsTemplateConfig;
import org.jeecg.config.StaticConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created on 17/6/7.
 * Short messageAPIproductDEMOprogram,The project contains aSmsDemokind，pass directly
 * implementmain函数That’s it体验Short messageproductAPIFunction(Just need toAKReplaced with cloud communication enabled-Short messageproductFunction的AKThat’s it)
 * The project depends on2indivualjarBag(stored in the projectlibsunder directory)
 * 1:aliyun-java-sdk-core.jar
 * 2:aliyun-java-sdk-dysmsapi.jar
 *
 * Remark:DemoEngineering coding adoptsUTF-8
 * 国际Short message发送请勿参照此DEMO
 * @author: jeecg-boot
 */
public class DySmsHelper {
	
	private final static Logger logger=LoggerFactory.getLogger(DySmsHelper.class);

    /**Product name:云通信Short messageAPIproduct,Developers do not need to replace*/
    static final String PRODUCT = "Dysmsapi";
    /**product域名,Developers do not need to replace*/
    static final String DOMAIN = "dysmsapi.aliyuncs.com";

    /**TODO This needs to be replaced with the developer's ownAK(Find it in the Alibaba Cloud access console)*/
    static  String accessKeyId;
    static  String accessKeySecret;

    public static void setAccessKeyId(String accessKeyId) {
        DySmsHelper.accessKeyId = accessKeyId;
    }

    public static void setAccessKeySecret(String accessKeySecret) {
        DySmsHelper.accessKeySecret = accessKeySecret;
    }

    public static String getAccessKeyId() {
        return accessKeyId;
    }

    public static String getAccessKeySecret() {
        return accessKeySecret;
    }
    
    
    public static boolean sendSms(String phone, JSONObject templateParamJson, DySmsEnum dySmsEnum) throws ClientException {
    	//Self-adjustable timeout
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //update-begin-author：taoyan date:20200811 for:配置kind数据获取
        StaticConfig staticConfig = SpringContextUtils.getBean(StaticConfig.class);
        //logger.info("阿里大鱼Short message秘钥 accessKeyId：" + staticConfig.getAccessKeyId());
        //logger.info("阿里大鱼Short message秘钥 accessKeySecret："+ staticConfig.getAccessKeySecret());
        setAccessKeyId(staticConfig.getAccessKeyId());
        setAccessKeySecret(staticConfig.getAccessKeySecret());
        //update-end-author：taoyan date:20200811 for:配置kind数据获取
        
        //initializationacsClient,Not supported yetregionchange
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", PRODUCT, DOMAIN);
        IAcsClient acsClient = new DefaultAcsClient(profile);
        
        //verifyjsonparameter
        validateParam(templateParamJson,dySmsEnum);

        //update-begin---author:wangshuai---date:2024-11-05---for:【QQYUN-9422】Short message模板管理，Alibaba Cloud---
        String templateCode = dySmsEnum.getTemplateCode();
        JeecgSmsTemplateConfig baseConfig = SpringContextUtils.getBean(JeecgSmsTemplateConfig.class);
        if(baseConfig != null && CollectionUtil.isNotEmpty(baseConfig.getTemplateCode())){
            Map<String, String> smsTemplate = baseConfig.getTemplateCode();
            if(smsTemplate.containsKey(templateCode) && StringUtils.isNotEmpty(smsTemplate.get(templateCode))){
                templateCode = smsTemplate.get(templateCode);   
                logger.info("yml中读取Short messagecode{}",templateCode);
            }
        }
        //Signature name
        String signName = dySmsEnum.getSignName();
        if(baseConfig != null && StringUtils.isNotEmpty(baseConfig.getSignature())){
            logger.info("yml中读取Signature name{}",baseConfig.getSignature());
            signName = baseConfig.getSignature();
        }
        //update-end---author:wangshuai---date:2024-11-05---for:【QQYUN-9422】Short message模板管理，Alibaba Cloud---
        
        //Assemble the request object-See the console for detailed description-Part of the document
        SendSmsRequest request = new SendSmsRequest();
        //Required:Mobile phone number to be sent
        request.setPhoneNumbers(phone);
        //Required:Short message签名-可在Short message控制台中找到
        request.setSignName(signName);
        //Required:Short message模板-可在Short message控制台中找到
        request.setTemplateCode(templateCode);
        //Optional:Variable substitution in templatesJSONstring,If the template content is"Honey${name},您的verify码为${code}"hour,The value here is
        request.setTemplateParam(templateParamJson.toJSONString());
        
        //Optional-上行Short message扩展码(Users without special needs please ignore this field)
        //request.setSmsUpExtendCode("90997");

        //Optional:outIdExtension fields provided to business parties,最终在Short message回执消息中将此值带回给调用者
        //request.setOutId("yourOutId");

        boolean result = false;

        //hint An exception may be thrown here，Noticecatch
        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
        logger.info("Short message接口返回的数据----------------");
        logger.info("{Code:" + sendSmsResponse.getCode()+",Message:" + sendSmsResponse.getMessage()+",RequestId:"+ sendSmsResponse.getRequestId()+",BizId:"+sendSmsResponse.getBizId()+"}");
        String ok = "OK";
        if (ok.equals(sendSmsResponse.getCode())) {
            result = true;
        }
        return result;
        
    }
    
    private static void validateParam(JSONObject templateParamJson,DySmsEnum dySmsEnum) {
    	String keys = dySmsEnum.getKeys();
    	String [] keyArr = keys.split(",");
    	for(String item :keyArr) {
    		if(!templateParamJson.containsKey(item)) {
    			throw new RuntimeException("模板缺少parameter："+item);
    		}
    	}
    }
    

//    public static void main(String[] args) throws ClientException, InterruptedException {
//    	JSONObject obj = new JSONObject();
//    	obj.put("code", "1234");
//    	sendSms("13800138000", obj, DySmsEnum.FORGET_PASSWORD_TEMPLATE_CODE);
//    }
}
