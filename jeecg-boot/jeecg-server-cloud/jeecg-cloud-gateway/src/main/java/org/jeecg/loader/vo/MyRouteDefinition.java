package org.jeecg.loader.vo;

import org.springframework.cloud.gateway.route.RouteDefinition;

/**
 * CustomizeRouteDefinition
 * @author zyf
 */
public class MyRouteDefinition extends RouteDefinition {
    /**
     * routing status
     */
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
