package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.service.ProductionOrderIssuanceService;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * LiteFlow node component for issuing production orders.
 * Delegates to ProductionOrderIssuanceService to generate Production Orders
 * in ERP for each planned batch in the approved weekly plan.
 */
@Slf4j
@Component("issueProductionOrders")
public class IssueProductionOrdersCmp extends NodeComponent {

    @Resource
    private ProductionOrderIssuanceService productionOrderIssuanceService;

    @Override
    public void process() throws Exception {
        log.info("[ExecutionChain] IssueProductionOrders node started");

        PlanningChainContext context = this.getContextBean(PlanningChainContext.class);
        String weeklyPlanId = context.getWeeklyPlanId();

        if (weeklyPlanId == null || weeklyPlanId.isBlank()) {
            log.warn("[ExecutionChain] No weekly plan ID provided for production order issuance");
            return;
        }

        productionOrderIssuanceService.issueProductionOrders(weeklyPlanId);
        context.setOrdersIssued(true);

        log.info("[ExecutionChain] IssueProductionOrders node completed for plan: {}", weeklyPlanId);
    }
}
