package org.jeecg.loader.repository;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.loader.repository.MyInMemoryRouteDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Dynamically update routing gatewayservice
 * 1）implement aSpringProvided event push interfaceApplicationEventPublisherAware
 * 2）Provides basic methods for dynamic routing，Available viabeanMethods to operate this class。This class provides new routes、Update route、Delete route，Then implement the published function。
 *
 * @author zyf
 */
@Slf4j
@Service
public class DynamicRouteService implements ApplicationEventPublisherAware {

    @Autowired
    private MyInMemoryRouteDefinitionRepository repository;

    /**
     * publish event
     */

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    /**
     * Delete route
     *
     * @param id
     * @return
     */
    public synchronized void delete(String id) {
        try {
            repository.delete(Mono.just(id)).subscribe();
            this.publisher.publishEvent(new RefreshRoutesEvent(this));
        }catch (Exception e){
            log.warn(e.getMessage(),e);
        }
    }

    /**
     * Update route
     *
     * @param definition
     * @return
     */
    public synchronized String update(RouteDefinition definition) {
        try {
            log.info("gateway update route {}", definition);
        } catch (Exception e) {
            return "update fail,not find route  routeId: " + definition.getId();
        }
        try {
            repository.save(Mono.just(definition)).subscribe();
            this.publisher.publishEvent(new RefreshRoutesEvent(this));
            return "success";
        } catch (Exception e) {
            return "update route fail";
        }
    }

    /**
     * Add route
     *
     * @param definition
     * @return
     */
    public synchronized String add(RouteDefinition definition) {
        log.info("gateway add route {}", definition);
        try {
            repository.save(Mono.just(definition)).subscribe();
        } catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
        return "success";
    }
}