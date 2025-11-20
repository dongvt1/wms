package org.jeecg.config.sign.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.SortedMap;

/**
 * Signature tool class
 * 
 * @author jeecg
 * @date 20210621
 */
@Slf4j
public class SignUtil {
    public static final String X_PATH_VARIABLE = "x-path-variable";

    /**
     * @param params
     *            All request parameters will be sorted and encrypted here
     * @return Verify signature result
     */
    public static boolean verifySign(SortedMap<String, String> params,String headerSign) {
        if (params == null || StringUtils.isEmpty(headerSign)) {
            return false;
        }
        // Encrypt parameters
        String paramsSign = getParamsSign(params);
        log.info("Param Sign : {}", paramsSign);
        return !StringUtils.isEmpty(paramsSign) && headerSign.equals(paramsSign);
    }

    /**
     * @param params
     *            All request parameters will be sorted and encrypted here
     * @return get signature
     */
    public static String getParamsSign(SortedMap<String, String> params) {
        //remove Url timestamp in
        params.remove("_t");
        String paramsJsonStr = JSONObject.toJSONString(params);
        log.info("Param paramsJsonStr : {}", paramsJsonStr);
        //Set signing key
        JeecgBaseConfig jeecgBaseConfig = SpringContextUtils.getBean(JeecgBaseConfig.class);
        String signatureSecret = jeecgBaseConfig.getSignatureSecret();
        String curlyBracket = SymbolConstant.DOLLAR + SymbolConstant.LEFT_CURLY_BRACKET;
        if(oConvertUtils.isEmpty(signatureSecret) || signatureSecret.contains(curlyBracket)){
            throw new JeecgBootException("Signing key ${jeecg.signatureSecret} Missing configuration ！！");
        }
        try {
            //【issues/I484RW】2.4.6After deployment，Drop-down search box prompt“signSignature verification failed”
            return DigestUtils.md5DigestAsHex((paramsJsonStr + signatureSecret).getBytes("UTF-8")).toUpperCase();
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(),e);
            return null;
        }
    }
}