package org.jeecg.smallTools;


import org.junit.jupiter.api.Test;

/**
 * testsqlsegmentation、Replacement and other operations
 * 
 * @author: scott
 * @date: 2023Year09moon05day 16:13
 */
public class TestSqlHandle {

    /**
     * Where segmentationtest
     */
    @Test
    public void testSqlSplitWhere() {
        String tableFilterSql = " select * from data.sys_user Where name='12312' and age>100";
        String[] arr = tableFilterSql.split(" (?i)where ");
        for (String sql : arr) {
            System.out.println("sqlfragment：" + sql);
        }
    }


    /**
     * Where replace
     */
    @Test
    public void testSqlWhereReplace() {
        String input = " Where name='12312' and age>100";
        String pattern = "(?i)where "; // (?i) Indicates case insensitivity

        String replacedString = input.replaceAll(pattern, "");

        System.out.println("replace前的字符串：" + input);
        System.out.println("replace后的字符串：" + replacedString);
    }
}
