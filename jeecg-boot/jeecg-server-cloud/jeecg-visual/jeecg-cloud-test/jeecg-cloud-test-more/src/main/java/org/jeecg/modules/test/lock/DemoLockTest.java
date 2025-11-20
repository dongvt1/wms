package org.jeecg.modules.test.lock;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.boot.starter.lock.annotation.JLock;
import org.jeecg.boot.starter.lock.client.RedissonLockClient;
import org.jeecg.modules.test.constant.CloudConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Distributed lock testdemo
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@Component
public class DemoLockTest {
    @Autowired
    RedissonLockClient redissonLock;
//    @Autowired
//    RabbitMqClient rabbitMqClient;

    /**
     * Test method：
     *    @Scheduled(cron = "0/5 * * * * ?") means every5Execute once per second
     *    @JLock(lockKey = CloudConstant.REDISSON_DEMO_LOCK_KEY1)Distributed lock，10Released in seconds
     *    result：Every10Output once per second “implement Distributed lock business logic1” It means the lock is successful
     *
     * 测试Distributed lock【Annotation method】
     */
    @Scheduled(cron = "0/5 * * * * ?")
    @JLock(lockKey = CloudConstant.REDISSON_DEMO_LOCK_KEY1)
    public void execute() throws InterruptedException {
        log.info("implementexecuteMission begins，Sleep starts after ten seconds，Current system timestamp（Second）："+ System.currentTimeMillis()/1000);
        Thread.sleep(10000);
        log.info("========implement Distributed lock business logic1=============");
//        Map map = new BaseMap();
//        map.put("orderId", "BJ0001");
//        rabbitMqClient.sendMessage(CloudConstant.MQ_JEECG_PLACE_ORDER, map);
//        //Delay10Second发送
//        map.put("orderId", "NJ0002");
//        rabbitMqClient.sendMessage(CloudConstant.MQ_JEECG_PLACE_ORDER, map, 10000);

        log.info("executeMission ended，休眠十Second完成，Current system timestamp（Second）："+ System.currentTimeMillis()/1000);
    }


    /**
     * 测试Distributed lock【encoding method】
     * @Scheduled(cron = "0/5 * * * * ?")
     */
    public void execute2() throws InterruptedException {
        int expireSeconds=6000;
        if (redissonLock.tryLock(CloudConstant.REDISSON_DEMO_LOCK_KEY2, -1, expireSeconds)) {
            log.info("implement任务execute2start，休眠十Second");
            Thread.sleep(10000);
           log.info("=============business logic2===================");
            log.info("timingexecute2Finish，休眠十Second");

            redissonLock.unlock(CloudConstant.REDISSON_DEMO_LOCK_KEY2);
        } else {
            log.info("execute2Failed to acquire lock");
        }
    }

}
