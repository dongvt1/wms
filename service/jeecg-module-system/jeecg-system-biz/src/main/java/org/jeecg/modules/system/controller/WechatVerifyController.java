package org.jeecg.modules.system.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.system.util.XssUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @Description: Enterprise WeChat certificate verification
 * @author: wangshuai
 * @date: 2023/12/6 10:42
 */
@RestController
@Slf4j
public class WechatVerifyController {

    /**
     * Enterprise WeChat verification
     */
    @RequestMapping(value = "/WW_verify_{code}.txt")
    public void mpVerify(@PathVariable("code") String code, HttpServletResponse response) {
        if(StringUtils.isEmpty(code)){
            log.error("Enterprise WeChat certificate verification失败！(codeis empty)");
            return;
        }
        try {
            PrintWriter writer = response.getWriter();
            code = XssUtils.scriptXss(code);
            writer.write(code);
            writer.close();
        } catch (Exception e) {
            log.error("Enterprise WeChat certificate verification失败！");
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }
}

