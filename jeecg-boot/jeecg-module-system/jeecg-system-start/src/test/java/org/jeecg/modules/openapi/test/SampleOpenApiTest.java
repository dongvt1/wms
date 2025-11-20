package org.jeecg.modules.openapi.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;


public class SampleOpenApiTest {
    private final String base_url = "http://localhost:8080/jeecg-boot";
    private final String appKey = "ak-pFjyNHWRsJEFWlu6";
    private final String searchKey = "4hV5dBrZtmGAtPdbA5yseaeKRYNpzGsS";
    
    @Test
    public void test() throws Exception {
        // According to departmentIDQuery user
        String url = base_url+"/openapi/call/TEwcXBlr?id=6d35e179cd814e3299bd588ea7daed3f";
        JSONObject header = genTimestampAndSignature();
        HttpGet httpGet = new HttpGet(url);
        // Set request header
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("appkey",appKey);
        httpGet.setHeader("signature",header.get("signature").toString());
        httpGet.setHeader("timestamp",header.get("timestamp").toString());
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpGet);) {
            // Get response status code
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("[debug] Response status code: " + statusCode);

            HttpEntity entity = response.getEntity();
            System.out.println(entity);
            // Get response content
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("[debug] Response content: " + responseBody);

            // parseJSONresponse
            JSONObject res = JSON.parseObject(responseBody);
            //Error log judgment
            if(res.containsKey("success")){
                Boolean success = res.getBoolean("success");
                if(success){
                    System.out.println("[info] Call successful： " + res.toJSONString());  
                }else{
                    System.out.println("[error] call failed： " + res.getString("message"));
                }
            }else{
                System.out.println("[error] call failed： " + res.getString("message"));
            }
        }

    }
    private JSONObject genTimestampAndSignature(){
        JSONObject jsonObject = new JSONObject();
        long timestamp = System.currentTimeMillis();
        jsonObject.put("timestamp",timestamp);
        jsonObject.put("signature", md5(appKey + searchKey + timestamp));
        return jsonObject;
    }

    /**
     * generatemd5
     * @param sourceStr
     * @return
     */
    protected String md5(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes("utf-8"));
            byte[] hash = md.digest();
            int i;
            StringBuffer buf = new StringBuffer(32);
            for (int offset = 0; offset < hash.length; offset++) {
                i = hash[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (Exception e) {
            throw new RuntimeException("signSignature error", e);
        }
        return result;
    }
}
