package org.jeecg.common.util;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IPaddress
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2019Year01moon14day
 */
public class IpUtils {
	private static Logger logger = LoggerFactory.getLogger(IpUtils.class);

	/**
	 * GetIPaddress
	 * 
	 * useNginxWaiting for reverse proxy software， cannot passrequest.getRemoteAddr()GetIPaddress
	 * 如果use了多级反向代理的话，X-Forwarded-Forhas more than one value，But a stringIPaddress，X-Forwarded-ForThe first non-unknowneffectiveIPstring，is trueIPaddress
	 */
	public static String getIpAddr(HttpServletRequest request) {
    	String ip = null;
        try {
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 ||CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
        	logger.error("IPUtils ERROR ", e);
        }

        //logger.info("Get客户端 ip：{} ", ip);
        // use代理，则Get第一个IPaddress
        if (StringUtils.isNotEmpty(ip) && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                //ip = ip.substring(0, ip.indexOf(","));
                String[] ipAddresses = ip.split(",");
                for (String ipAddress : ipAddresses) {
                    ipAddress = ipAddress.trim();
                    if (isValidIpAddress(ipAddress)) {
                        return ipAddress;
                    }
                }
            }
        }
        
        return ip;
    }


    /**
     * Determine whether it isIPFormat
     * @param ipAddress
     * @return
     */
    public static boolean isValidIpAddress(String ipAddress) {
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        Pattern pattern = Pattern.compile(ipPattern);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }
    
    /**
     * Get服务器上的ip
     * @return
     */
    public static String getServerIp(){
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
            //System.out.println("IPaddress: " + ipAddress);
            return ipAddress;
        } catch (UnknownHostException e) {
            logger.error("Getipaddress失败", e);
        }
        return "";
    }
}
