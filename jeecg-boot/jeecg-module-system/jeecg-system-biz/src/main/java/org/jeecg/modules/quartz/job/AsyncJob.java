package org.jeecg.modules.quartz.job;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.quartz.*;

/**
 * @Description: Synchronous scheduled task testing
 *
 * Synchronization here refers to When the execution time of a scheduled task is greater than the task interval
 * Will wait for the first task to complete before starting the second task
 *
 *
 * @author: taoyan
 * @date: 2020Year06moon19day
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Slf4j
public class AsyncJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info(" --- Synchronous task scheduling starts --- ");
        try {
            //Simulate task execution time here 5Second  任务表达式配置为EverySecond执行一次：0/1 * * * * ? *
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Test findings Every5Second执行一次
        log.info(" --- Execution completed，time："+DateUtils.now()+"---");
    }

}
