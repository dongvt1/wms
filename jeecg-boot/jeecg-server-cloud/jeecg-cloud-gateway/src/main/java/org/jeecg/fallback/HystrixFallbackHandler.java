//package org.jeecg.fallback;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.server.HandlerFunction;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import reactor.core.publisher.Mono;
//
//import java.util.Optional;
//
//import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;
//
///**
// * @author scott
// * @date 2020/05/26
// * Hystrix Downgrade processing
// */
//@Slf4j
//@Component
//public class HystrixFallbackHandler implements HandlerFunction<ServerResponse> {
//    @Override
//    public Mono<ServerResponse> handle(ServerRequest serverRequest) {
//        Optional<Object> originalUris = serverRequest.attribute(GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
//
//        originalUris.ifPresent(originalUri -> log.error("Gateway execution request:{}fail,hystrix服务Downgrade processing", originalUri));
//
//        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                .header("Content-Type","text/plain; charset=utf-8").body(BodyInserters.fromObject("Access timeout,Please try again later"));
//    }
//}
