//package org.jeecg.fallback;
//
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
///**
// * Response timeout fuse processor【upgradespringboot2.6.6back，Such void】
// *
// * @author zyf
// */
//@RestController
//public class FallbackController {
//
//    /**
//     * Global circuit breaker processing
//     * @return
//     */
//    @RequestMapping("/fallback")
//    public Mono<String> fallback() {
//        return Mono.just("Access timeout，请稍back再试!");
//    }
//
//    /**
//     * demofuse processing
//     * @return
//     */
//    @RequestMapping("/demo/fallback")
//    public Mono<String> fallback2() {
//        return Mono.just("Access timeout，请稍back再试!");
//    }
//}
