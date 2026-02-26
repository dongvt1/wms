package org.jeecg;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.util.RestUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * @Description: TODO
 * @author: scott
 * @date: 2022Year05moon10day 14:02
 */
public class TestMain {
    public static void main(String[] args) {
        // Request address
        String url = "https://api3.boot.jeecg.com/sys/user/list";
        // ask Header （used to passToken）
        HttpHeaders headers = getHeaders();
        // ask方式是 GET Represents obtaining data
        HttpMethod method = HttpMethod.GET;

        System.out.println("Request address：" + url);
        System.out.println("ask方式：" + method);

        // use RestUtil ask该url
        ResponseEntity<JSONObject> result = RestUtil.request(url, method, headers, null, null, JSONObject.class);
        if (result != null && result.getBody() != null) {
            System.out.println("Return results：" + result.getBody().toJSONString());
        } else {
            System.out.println("Query failed");
        }
    }
    private static HttpHeaders getHeaders() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.50h-g6INOZRVnznExiawFb1U6PPjcVVA4POeYRA5a5Q";
        System.out.println("askToken：" + token);

        HttpHeaders headers = new HttpHeaders();
        String mediaType = MediaType.APPLICATION_JSON_VALUE;
        headers.setContentType(MediaType.parseMediaType(mediaType));
        headers.set("Accept", mediaType);
        headers.set("X-Access-Token", token);
        return headers;
    }

}
