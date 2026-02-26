//package org.jeecg.modules.monitor.actuator.undertow;
//
//import io.micrometer.core.instrument.MeterRegistry;
//import io.undertow.server.HttpHandler;
//import io.undertow.server.HttpServerExchange;
//import io.undertow.server.session.*;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.LongAdder;
//
///**
// * CustomizeundertowMonitoring indicator tools
// * for [QQYUN-11902]tomcat replaceundertow The functions here have not been modified yet
// * @author chenrui
// * @date 2025/4/8 19:06
// */
//@Component("jeecgCustomUndertowMetricsHandler")
//public class CustomUndertowMetricsHandler {
//
//    // Used to count the created session quantity
//    private final LongAdder sessionsCreated = new LongAdder();
//
//    // Used to count destroyed session quantity
//    private final LongAdder sessionsExpired = new LongAdder();
//
//    // currently active session quantity
//    private final AtomicInteger activeSessions = new AtomicInteger();
//
//    // Most active in history session number
//    private final AtomicInteger maxActiveSessions = new AtomicInteger();
//
//    // Undertow Memory session Manager（for creating and managing session）
//    private final InMemorySessionManager sessionManager = new InMemorySessionManager("undertow-session-manager");
//
//    // use Cookie storage session ID
//    private final SessionConfig sessionConfig = new SessionCookieConfig();
//
//    /**
//     * 构造函number
//     * @param meterRegistry
//     * @author chenrui
//     * @date 2025/4/8 19:07
//     */
//    public CustomUndertowMetricsHandler(MeterRegistry meterRegistry) {
//        // register Micrometer index
//        meterRegistry.gauge("undertow.sessions.created", sessionsCreated, LongAdder::longValue);
//        meterRegistry.gauge("undertow.sessions.expired", sessionsExpired, LongAdder::longValue);
//        meterRegistry.gauge("undertow.sessions.active.current", activeSessions, AtomicInteger::get);
//        meterRegistry.gauge("undertow.sessions.active.max", maxActiveSessions, AtomicInteger::get);
//
//        // Add to session life cycle listener，statistics session Create and destroy
//        sessionManager.registerSessionListener(new SessionListener() {
//            @Override
//            public void sessionCreated(Session session, HttpServerExchange exchange) {
//                sessionsCreated.increment();
//                int now = activeSessions.incrementAndGet();
//                maxActiveSessions.getAndUpdate(max -> Math.max(max, now));
//            }
//
//            @Override
//            public void sessionDestroyed(Session session, HttpServerExchange exchange, SessionDestroyedReason reason) {
//                sessionsExpired.increment();
//                activeSessions.decrementAndGet();
//            }
//        });
//    }
//
//    /**
//     * Package Undertow of HttpHandler，accomplish session Automatically create logic
//     * @param next
//     * @return
//     * @author chenrui
//     * @date 2025/4/8 19:07
//     */
//    public HttpHandler wrap(HttpHandler next) {
//        return exchange -> {
//            // Get current session，Create if does not exist
//            Session session = sessionManager.getSession(exchange, sessionConfig);
//            if (session == null) {
//                sessionManager.createSession(exchange, sessionConfig);
//            }
//
//            // execute next Handler
//            next.handleRequest(exchange);
//        };
//    }
//}