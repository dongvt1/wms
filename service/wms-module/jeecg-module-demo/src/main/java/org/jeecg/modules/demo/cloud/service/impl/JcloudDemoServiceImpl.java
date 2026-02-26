package org.jeecg.modules.demo.cloud.service.impl;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.demo.cloud.service.JcloudDemoService;
import org.springframework.stereotype.Service;

/**
 * @Description: JcloudDemoServiceImplImplementation class
 * @author: jeecg-boot
 */
@Service
public class JcloudDemoServiceImpl implements JcloudDemoService {
    @Override
    public String getMessage(String name) {
        String resMsg = "Hello，I amjeecg-demoservice node，received your message：【 "+ name +" 】";
        return resMsg;
    }
}
