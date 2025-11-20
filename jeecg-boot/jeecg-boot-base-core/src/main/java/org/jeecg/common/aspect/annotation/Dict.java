package org.jeecg.common.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * dictionary annotation
 * @author: dangzhenghui
 * @date: 2019Year03moon17day-afternoon9:37:16
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dict {
    /**
     * Method description:  datacode
     * do    who： dangzhenghui
     * day    Expect： 2019Year03moon17day-afternoon9:37:16
     *
     * @return Return type： String
     */
    String dicCode();

    /**
     * Method description:  dataText
     * do    who： dangzhenghui
     * day    Expect： 2019Year03moon17day-afternoon9:37:16
     *
     * @return Return type： String
     */
    String dicText() default "";

    /**
     * Method description: data字典表
     * do    who： dangzhenghui
     * day    Expect： 2019Year03moon17day-afternoon9:37:16
     *
     * @return Return type： String
     */
    String dictTable() default "";


    //update-begin---author:chenrui ---date:20231221  for：[issues/#5643]Solve the problem that the distributed table dictionary cannot be queried across databases------------
    /**
     * Method description: data字典表所在data源名称
     * do    who： chenrui
     * day    Expect： 2023Year12moon20day-afternoon4:58
     *
     * @return Return type： String
     */
    String ds() default "";
    //update-end---author:chenrui ---date:20231221  for：[issues/#5643]Solve the problem that the distributed table dictionary cannot be queried across databases------------
}
