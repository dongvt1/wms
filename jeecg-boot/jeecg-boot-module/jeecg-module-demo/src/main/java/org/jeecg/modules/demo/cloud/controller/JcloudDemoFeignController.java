//package org.jeecg.modules.demo.cloud.controller;
//
//import com.alibaba.csp.sentinel.annotation.SentinelResource;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import io.swagger.v3.oas.annotations.Operation;
//import lombok.extern.slf4j.Slf4j;
//import org.jeecg.common.api.vo.Result;
//import org.jeecg.common.system.api.ISysBaseAPI;
//import org.jeecg.common.system.vo.DictModel;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import javax.annotation.Resource;
//import java.util.List;
//
///**
// *
// */
//@Slf4j
//@Tag(name = "【microservices】Unit testing")
//@RestController
//@RequestMapping("/test")
//public class JcloudDemoFeignController {
//    @Resource
//    private ISysBaseAPI sysBaseApi;
////    @Autowired
////    private ErpHelloApi erpHelloApi;
//
//    /**
//     * test
//     *
//     * @return
//     */
//    @GetMapping("/callSystem")
//    //@SentinelResource(value = "remoteDict",fallback = "getDefaultHandler")
//    @Operation(summary = "passfeigncallsystemServe")
//    public Result getRemoteDict() {
//        List<DictModel> list = sysBaseApi.queryAllDict();
//        return Result.OK(list);
//    }
//
//
////    /**
////     * testcall erp microservices接口
////     * 【如何test：passarchetype生成microservices模块，快速集成test】
////     *  https://help.jeecg.com/java/springcloud/archetype.html
////     * @return
////     */
////    @GetMapping("/callErp")
////    @Operation(summary = "testfeign erp")
////    public Result callErp() {
////        log.info("call erp Serve");
////        String res = erpHelloApi.callHello();
////        return Result.OK(res);
////    }
//
//    /**
//     * fuse，Default callback function
//     *
//     * @return
//     */
//    public Result<Object> getDefaultHandler() {
//        log.info("testJcloudDemoController-remoteDict fuse降级");
//        return Result.error("testJcloudDemoController-remoteDict fuse降级");
//    }
//
//}
