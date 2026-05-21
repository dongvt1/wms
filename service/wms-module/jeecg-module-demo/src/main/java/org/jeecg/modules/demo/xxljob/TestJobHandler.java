//
//package org.jeecg.modules.demo.xxljob;
//
//import com.xxl.job.core.biz.model.ReturnT;
//import com.xxl.job.core.handler.annotation.XxlJob;
//import lombok.extern.slf4j.Slf4j;
//import org.jeecg.common.config.mqtoken.UserTokenContext;
//import org.jeecg.common.constant.CommonConstant;
//import org.jeecg.common.system.api.ISysBaseAPI;
//import org.jeecg.common.system.util.JwtUtil;
//import org.jeecg.common.util.RedisUtil;
//import org.jeecg.common.util.SpringContextUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//
///**
// * xxl-jobScheduled task test
// */
//@Component
//@Slf4j
//public class TestJobHandler {
//    @Autowired
//    ISysBaseAPI sysBaseApi;
//
//    /**
//     * simple tasks
//     *
//     * test：nonetokencallfeigninterface
//     *
//     * @param params
//     * @return
//     */
//
//    @XxlJob(value = "testJob")
//    public ReturnT<String> demoJobHandler(String params) {
//        //1.Generate temporary tokenTokeninto the thread
//        UserTokenContext.setToken(getTemporaryToken());
//
//        log.info("I am jeecg-demo Scheduled tasks in services testJob , I executed...............................");
//        log.info("我call jeecg-system 服务的字典interface：{}",sysBaseApi.queryAllDict());
//        //。。。You can write multiple herefeigninterfacecall
//
//        //2.Finished，Delete temporary tokenToken
//        UserTokenContext.remove();
//        return ReturnT.SUCCESS;
//    }
//
//    public void init() {
//        log.info("init");
//    }
//
//    public void destroy() {
//        log.info("destory");
//    }
//
//    /**
//     * Get temporary token
//     *
//     * 模拟登陆interface，Get simulation Token
//     * @return
//     */
//    public static String getTemporaryToken() {
//        RedisUtil redisUtil = SpringContextUtils.getBean(RedisUtil.class);
//        // Simulated login generationToken
//        String token = JwtUtil.sign("??", "??");
//        // set upTokenThe cache validity time is 5 minute
//        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
//        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, 5 * 60 * 1000);
//        return token;
//    }
//
//}
//
