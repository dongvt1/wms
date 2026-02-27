package com.cy.modules.planning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.modules.planning.entity.Routing;
import com.cy.modules.planning.entity.RoutingStep;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Description: Routing Service
 * @Author: BMad
 * @Date: 2026-02-25
 */
public interface RoutingService extends IService<Routing> {

    List<Routing> getByProductId(String productId);

    List<Routing> getByStatus(String status);

    boolean isCodeUnique(String routingCode, String excludeId);

    /**
     * Save routing with steps
     */
    boolean saveRoutingWithSteps(Routing routing, List<RoutingStep> steps);

    /**
     * Update routing with steps (replaces existing steps)
     */
    boolean updateRoutingWithSteps(Routing routing, List<RoutingStep> steps);

    /**
     * Get routing steps
     */
    List<RoutingStep> getRoutingSteps(String routingId);

    /**
     * Get routing detail with steps
     */
    Map<String, Object> getRoutingDetail(String routingId);

    /**
     * Calculate total lead time for a given quantity
     * Returns total production lead time in hours
     */
    BigDecimal calculateTotalLeadTime(String routingId, BigDecimal quantity);
}
