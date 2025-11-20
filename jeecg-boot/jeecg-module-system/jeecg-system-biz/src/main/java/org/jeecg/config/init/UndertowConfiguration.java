//package org.jeecg.config.init;
//
//import io.undertow.UndertowOptions;
//import io.undertow.server.DefaultByteBufferPool;
//import io.undertow.server.handlers.BlockingHandler;
//import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
//import org.jeecg.modules.monitor.actuator.undertow.CustomUndertowMetricsHandler;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
//import org.springframework.boot.web.server.WebServerFactoryCustomizer;
//import org.springframework.context.annotation.Configuration;
//
///**
// * UndertowConfiguration
// *
// * Solve startup prompts： WARN  io.undertow.websockets.jsr:68 - UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used
// */
//@Configuration
//public class UndertowConfiguration implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {
//
//    /**
//     * CustomizeundertowMonitoring indicator tools
//     * for [QQYUN-11902]tomcat replaceundertow The functions here have not been modified yet
//     */
//    @Autowired
//    private CustomUndertowMetricsHandler customUndertowMetricsHandler;
//
//    @Override
//    public void customize(UndertowServletWebServerFactory factory) {
//        // set up Undertow Server parameters（底层网络Configuration）
//        factory.addBuilderCustomizers(builder -> {
//            builder.setServerOption(UndertowOptions.MAX_HEADER_SIZE, 65536);      // header maximum64KB
//            builder.setServerOption(UndertowOptions.MAX_PARAMETERS, 10000);       // maximum参数数
//        });
//        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
//
//            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
//
//            // set up合理的参数
//            webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(true, 8192));
//
//            deploymentInfo.addServletContextAttribute("io.undertow.websockets.jsr.WebSocketDeploymentInfo", webSocketDeploymentInfo);
//
//            // 添加Customize monitor handler
//            deploymentInfo.addInitialHandlerChainWrapper(next -> new BlockingHandler(customUndertowMetricsHandler.wrap(next)));
//        });
//    }
//}