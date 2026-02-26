package org.jeecg.smallTools;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

/**
 * String processing test
 *
 * @author: scott
 * @date: 2023Year03moon30day 15:27
 */
public class TestStr {

    /**
     * Test parameter formatting issues，There is a problem with the numeric value
     */
    @Test
    public void testParameterFormat() {
        String url = "/pages/lowApp/process/taskDetail?tenantId={0}&procInsId={1}&taskId={2}&taskDefKey={3}";
        String cc = MessageFormat.format(url, "6364", "111", "22", "333");
        System.out.println("Parameter is a string：" + cc);

        String cc2 = MessageFormat.format(url, 6364, 111, 22, 333);
        System.out.println("Parameters are numbers（something went wrong）：" + cc2);
    }


    @Test
    public void testStringSplitError() {
        String conditionValue = "qweqwe";
        String[] conditionValueArray = conditionValue.split(",");
        System.out.println("length = "+ conditionValueArray.length);
        Arrays.stream(conditionValueArray).forEach(System.out::println);
    }
    
    
    @Test
    public void getThisDate() {
        LocalDate d = DateUtils.getLocalDate();
        System.out.println(d); 
    }
    
    
    @Test
    public void firstDayOfLastSixMonths() {
        LocalDate today = LocalDate.now(); // 获取当前day期
        LocalDate firstDayOfLastSixMonths = today.minusMonths(6).withDayOfMonth(1); // 获取近半Year的第一天
        LocalDateTime firstDateTime = LocalDateTime.of(firstDayOfLastSixMonths, LocalTime.MIN); // Set the time to the minimum time of the day（00:00:00）
        Date date = Date.from(firstDateTime.atZone(ZoneId.systemDefault()).toInstant()); // Will LocalDateTime Convert to Date
        System.out.println("近半Year的第一天的 00:00:00 Timestamp：" + date);
    }

    @Test
    public void testJSONArrayJoin() {
        JSONArray valArray = new JSONArray();
        valArray.add("123");
        valArray.add("qwe");
        System.out.println("value: " + StringUtils.join(valArray, ","));
    }
    
    @Test
    public void testSql() {
        String sql = "select * from sys_user where sex = ${sex}";
        sql = sql.replaceAll("'?\\$\\{sex}'?","1");
        System.out.println(sql);
    }

    @Test
    public void base64(){
        String encodedString = "5L+d5a2Y5aSx6LSl77yM5YWN6LS554mI5pyA5aSa5Yib5bu6ezB95p2h6L+e5o6l77yM6K+35Y2H57qn5ZWG5Lia54mI77yB";
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        String tipMsg = MessageFormat.format(decodedString, 10);
        System.out.println(tipMsg);
    }

    /**
     * The regular test string only saves Chinese characters, numbers and letters
     */
    @Test
    public void testSpecialChar() {
        String str = "Hello, World! Hello！This is a test of special symbols，This is__ a test string with special characters: @#$%^&*";
        // Use regular expressions to replace special characters
        String replacedStr = str.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
        System.out.println("Replaced String: " + replacedStr);
    }

}
