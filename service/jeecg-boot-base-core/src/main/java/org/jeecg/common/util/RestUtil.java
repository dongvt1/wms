package org.jeecg.common.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * call Restful interface Util
 *
 * @author sunjianlei
 */
@Slf4j
public class RestUtil {

    private static String domain = null;
    private static String path = null;
    
    private static String getDomain() {
        if (domain == null) {
            domain = SpringContextUtils.getDomain();
            // issues/2959
            // The microservice version integrates enterprise WeChat single sign-in
            // Because the microservice version does not have a port number，lead to SpringContextUtils.getDomain() The port number of the domain name obtained by the method becomes:-1So something went wrong，Just put this-1Just remove it。
            String port=":-1";
            if (domain.endsWith(port)) {
                domain = domain.substring(0, domain.length() - 3);
            }
        }
        return domain;
    }

    private static String getPath() {
        if (path == null) {
            path = SpringContextUtils.getApplicationContext().getEnvironment().getProperty("server.servlet.context-path");
        }
        return oConvertUtils.getString(path);
    }

    public static String getBaseUrl() {
        String basepath = getDomain() + getPath();
        log.info(" RestUtil.getBaseUrl: " + basepath);
        return basepath;
    }

    /**
     * RestAPI call器
     */
    private final static RestTemplate RT;

    static {
        //update-begin---author:chenrui ---date:20251011  for：[issues/8859]onlineformjavaReinforcement failure------------
        // use Apache HttpClient avoid JDK HttpURLConnection of too many bytes written question
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        //update-end---author:chenrui ---date:20251011  for：[issues/8859]onlineformjavaReinforcement failure------------
        requestFactory.setConnectTimeout(30000);
        requestFactory.setReadTimeout(30000);
        RT = new RestTemplate(requestFactory);
        //update-begin---author:chenrui ---date:20251011  for：[issues/8859]onlineformjavaReinforcement failure------------
        // 解决乱码question（replace StringHttpMessageConverter for UTF-8）
        for (int i = 0; i < RT.getMessageConverters().size(); i++) {
            if (RT.getMessageConverters().get(i) instanceof StringHttpMessageConverter) {
                RT.getMessageConverters().set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
                break;
            }
        }
        //update-end---author:chenrui ---date:20251011  for：[issues/8859]onlineformjavaReinforcement failure------------
    }

    public static RestTemplate getRestTemplate() {
        return RT;
    }

    /**
     * send get ask
     */
    public static JSONObject get(String url) {
        return getNative(url, null, null).getBody();
    }

    /**
     * send get ask
     */
    public static JSONObject get(String url, JSONObject variables) {
        return getNative(url, variables, null).getBody();
    }

    /**
     * send get ask
     */
    public static JSONObject get(String url, JSONObject variables, JSONObject params) {
        return getNative(url, variables, params).getBody();
    }

    /**
     * send get ask，Return to native ResponseEntity object
     */
    public static ResponseEntity<JSONObject> getNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.GET, variables, params);
    }

    /**
     * send Post ask
     */
    public static JSONObject post(String url) {
        return postNative(url, null, null).getBody();
    }

    /**
     * send Post ask
     */
    public static JSONObject post(String url, JSONObject params) {
        return postNative(url, null, params).getBody();
    }

    /**
     * send Post ask
     */
    public static JSONObject post(String url, JSONObject variables, JSONObject params) {
        return postNative(url, variables, params).getBody();
    }

    /**
     * send POST ask，Return to native ResponseEntity object
     */
    public static ResponseEntity<JSONObject> postNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.POST, variables, params);
    }

    /**
     * send put ask
     */
    public static JSONObject put(String url) {
        return putNative(url, null, null).getBody();
    }

    /**
     * send put ask
     */
    public static JSONObject put(String url, JSONObject params) {
        return putNative(url, null, params).getBody();
    }

    /**
     * send put ask
     */
    public static JSONObject put(String url, JSONObject variables, JSONObject params) {
        return putNative(url, variables, params).getBody();
    }

    /**
     * send put ask，Return to native ResponseEntity object
     */
    public static ResponseEntity<JSONObject> putNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.PUT, variables, params);
    }

    /**
     * send delete ask
     */
    public static JSONObject delete(String url) {
        return deleteNative(url, null, null).getBody();
    }

    /**
     * send delete ask
     */
    public static JSONObject delete(String url, JSONObject variables, JSONObject params) {
        return deleteNative(url, variables, params).getBody();
    }

    /**
     * send delete ask，Return to native ResponseEntity object
     */
    public static ResponseEntity<JSONObject> deleteNative(String url, JSONObject variables, JSONObject params) {
        return request(url, HttpMethod.DELETE, null, variables, params, JSONObject.class);
    }

    /**
     * sendask
     */
    public static ResponseEntity<JSONObject> request(String url, HttpMethod method, JSONObject variables, JSONObject params) {
        return request(url, method, getHeaderApplicationJson(), variables, params, JSONObject.class);
    }

    /**
     * sendask
     *
     * @param url          ask地址
     * @param method       ask方式
     * @param headers      ask头  available
     * @param variables    askurlparameter available
     * @param params       askbodyparameter available
     * @param responseType Return type
     * @return ResponseEntity<responseType>
     */
    public static <T> ResponseEntity<T> request(String url, HttpMethod method, HttpHeaders headers, JSONObject variables, Object params, Class<T> responseType) {
        log.info(" RestUtil  --- request ---  url = "+ url);
        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException("url 不能for空");
        }
        if (method == null) {
            throw new RuntimeException("method 不能for空");
        }
        if (headers == null) {
            headers = new HttpHeaders();
        }
        // ask体
        String body = "";
        if (params != null) {
            if (params instanceof JSONObject) {
                body = ((JSONObject) params).toJSONString();

            } else {
                body = params.toString();
            }
        }
        // Splicing url parameter
        if (variables != null && !variables.isEmpty()) {
            url += ("?" + asUrlVariables(variables));
        }
        // sendask
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return RT.exchange(url, method, request, responseType);
    }

    /**
     * sendask（Support custom timeout）
     *
     * @param url          ask地址
     * @param method       ask方式
     * @param headers      ask头  available
     * @param variables    askurlparameter available
     * @param params       askbodyparameter available
     * @param responseType Return type
     * @param timeout      timeout（millisecond），如果for0或负数则use默认超时
     * @return ResponseEntity<responseType>
     */
    public static <T> ResponseEntity<T> request(String url, HttpMethod method, HttpHeaders headers,
                                                JSONObject variables, Object params, Class<T> responseType, int timeout) {
        log.info(" RestUtil  --- request ---  url = "+ url + ", timeout = " + timeout);

        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException("url 不能for空");
        }
        if (method == null) {
            throw new RuntimeException("method 不能for空");
        }
        if (headers == null) {
            headers = new HttpHeaders();
        }

        // Create customRestTemplate（If you need to set a timeout）
        RestTemplate restTemplate = RT;
        if (timeout > 0) {
            //update-begin---author:chenrui ---date:20251011  for：[issues/8859]onlineformjavaReinforcement failure------------
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            //update-end---author:chenrui ---date:20251011  for：[issues/8859]onlineformjavaReinforcement failure------------
            requestFactory.setConnectTimeout(timeout);
            requestFactory.setReadTimeout(timeout);
            restTemplate = new RestTemplate(requestFactory);
            //update-begin---author:chenrui ---date:20251011  for：[issues/8859]onlineformjavaReinforcement failure------------
            // 解决乱码question（replace StringHttpMessageConverter for UTF-8）
            for (int i = 0; i < restTemplate.getMessageConverters().size(); i++) {
                if (restTemplate.getMessageConverters().get(i) instanceof StringHttpMessageConverter) {
                    restTemplate.getMessageConverters().set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
                    break;
                }
            }
            //update-end---author:chenrui ---date:20251011  for：[issues/8859]onlineformjavaReinforcement failure------------
        }

        // ask体
        String body = "";
        if (params != null) {
            if (params instanceof JSONObject) {
                body = ((JSONObject) params).toJSONString();
            } else {
                body = params.toString();
            }
        }

        // Splicing url parameter
        if (variables != null && !variables.isEmpty()) {
            url += ("?" + asUrlVariables(variables));
        }

        // sendask
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, method, request, responseType);
    }
    
    /**
     * GetJSONask头
     */
    public static HttpHeaders getHeaderApplicationJson() {
        return getHeader(MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    /**
     * Getask头
     */
    public static HttpHeaders getHeader(String mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mediaType));
        headers.add("Accept", mediaType);
        return headers;
    }

    /**
     * Will JSONObject 转for a=1&b=2&c=3...&n=n of形式
     */
    public static String asUrlVariables(JSONObject variables) {
        Map<String, Object> source = variables.getInnerMap();
        Iterator<String> it = source.keySet().iterator();
        StringBuilder urlVariables = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            String value = "";
            Object object = source.get(key);
            if (object != null) {
                if (!StringUtils.isEmpty(object.toString())) {
                    value = object.toString();
                }
            }
            urlVariables.append("&").append(key).append("=").append(value);
        }
        // remove the first one&
        return urlVariables.substring(1);
    }

}
