package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Prevent swiping SMS interface（Only for binding mobile phone number templates：SMS_175430166）
 * 
 * 1、sameIP，1Sending text messages within minutes is not allowed to exceed5Second-rate（reset every minuteIP请求Second-rate数）
 * 2、sameIP，1Send text messages within minutes for more than20Second-rate，Enter the blacklist，Not allowed to use SMS interface
 * 
 * 3、SMS interface signature and timestamp
 *  Involving interface：
 *  /sys/sms
 *  /desform/api/sendVerifyCode
 *  /sys/sendChangePwdSms
 */
@Slf4j
public class DySmsLimit {

    // 1Maximum number of text messages sent within a minute（singleIP）
    private static final int MAX_MESSAGE_PER_MINUTE = 5;
    // 1minute
    private static final int MILLIS_PER_MINUTE = 60000;
    // 一minute内报警线最大短信数量，Exceeded and blacklisted（singleIP）
    private static final int MAX_TOTAL_MESSAGE_PER_MINUTE = 20;

    private static ConcurrentHashMap<String, Long> ipLastRequestTime = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> ipRequestCount = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Boolean> ipBlacklist = new ConcurrentHashMap<>();

    /**
     * @param ip Request to send text messageIPaddress
     * @return
     */
    public static boolean canSendSms(String ip) {
        long currentTime = System.currentTimeMillis();
        long lastRequestTime = ipLastRequestTime.getOrDefault(ip, 0L);
        int requestCount = ipRequestCount.getOrDefault(ip, 0);
        log.info("IP：{}, Msg requestCount：{} ", ip, requestCount);

        if (ipBlacklist.getOrDefault(ip, false)) {
            // ifIPin blacklist，It is forbidden to send text messages
            log.error("IP：{}, Enter the blacklist，Prohibit sending request messages！", ip);
            return false;
        }

        if (currentTime - lastRequestTime >= MILLIS_PER_MINUTE) {
            // if距离上Second-rate请求已经超过一minute，then reset the count
            ipRequestCount.put(ip, 1);
            ipLastRequestTime.put(ip, currentTime);
            return true;
        } else {
            // if距离上Second-rate请求不到一minute
            ipRequestCount.put(ip, requestCount + 1);
            if (requestCount < MAX_MESSAGE_PER_MINUTE) {
                // if请求Second-rate数小于5Second-rate，Allow text messages to be sent
                return true;
            } else if (requestCount >= MAX_TOTAL_MESSAGE_PER_MINUTE) {
                // if请求Second-rate数超过报警线短信数量，WillIPAdd to blacklist
                ipBlacklist.put(ip, true);
                return false;
            } else {
                log.error("IP：{}, 1minute内请求短信超过5Second-rate，Please try again later！", ip);
                return false;
            }
        }
    }

    /**
     * The quantity will be cleared after the picture QR code is successfully verified.
     * 
     * @param ip IPaddress
     */
    public static void clearSendSmsCount(String ip) {
        long currentTime = System.currentTimeMillis();
        ipRequestCount.put(ip, 0);
        ipLastRequestTime.put(ip, currentTime);
    }
    
//    public static void main(String[] args) {
//        String ip = "192.168.1.1";
//        for (int i = 1; i < 50; i++) {
//            if (canSendSms(ip)) {
//                System.out.println("Send SMS successfully");
//            } else {
//                //System.out.println("Exceed SMS limit for IP " + ip);
//            }
//        }
//
//        System.out.println(ipLastRequestTime);
//        System.out.println(ipRequestCount);
//        System.out.println(ipBlacklist);
//    }
}
