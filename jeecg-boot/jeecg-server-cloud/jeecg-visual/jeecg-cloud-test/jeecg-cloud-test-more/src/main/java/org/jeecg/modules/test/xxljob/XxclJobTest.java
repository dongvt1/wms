
package org.jeecg.modules.test.xxljob;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *  xxl-jobScheduled task test
 * @author: zyf
 * @date: 2022/04/21
 */
@Component
@Slf4j
public class XxclJobTest {


    /**
     * simple tasks
     *
     * @param params
     * @return
     */

    @XxlJob(value = "xxclJobTest")
    public ReturnT<String> demoJobHandler(String params) {
        log.info("I am jeecg-system Scheduled tasks in services xxclJobTest , I executed...............................");
        return ReturnT.SUCCESS;
    }

    public void init() {
        log.info("init");
    }

    public void destroy() {
        log.info("destory");
    }

}

