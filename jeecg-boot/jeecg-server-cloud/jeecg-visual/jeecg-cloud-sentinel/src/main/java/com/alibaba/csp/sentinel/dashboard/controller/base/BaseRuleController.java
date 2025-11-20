package com.alibaba.csp.sentinel.dashboard.controller.base;

import java.util.concurrent.TimeUnit;


/**
 * NacosPersistence general processing class
 *
 * @author zyf
 * @date 2022-04-13
 */
public class BaseRuleController {
    /**
     * delay
     *
     * explain：When loading data into the list，NacosPersistence is not finished yet，As a result, loading data is incorrect.
     */
    public static void delayTime(){
        try {
            TimeUnit.MILLISECONDS.sleep(100);
            System.out.println("-------------sleep100millisecond-----------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
