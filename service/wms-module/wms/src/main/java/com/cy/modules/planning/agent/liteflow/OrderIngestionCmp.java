package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.service.OrderIngestionService;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * LiteFlow node component for order ingestion.
 * Delegates to OrderIngestionService to process and validate incoming orders.
 *
 * Context data expected:
 * - "orderIds" (List<String>): IDs of orders to process
 */
@Slf4j
@Component("orderIngestion")
public class OrderIngestionCmp extends NodeComponent {

    @Resource
    private OrderIngestionService orderIngestionService;

    @Override
    public void process() throws Exception {
        log.info("[PlanningChain] OrderIngestion node started");

        @SuppressWarnings("unchecked")
        List<String> orderIds = this.getRequestData();

        if (orderIds == null || orderIds.isEmpty()) {
            log.warn("[PlanningChain] No order IDs provided, skipping order ingestion");
            return;
        }

        orderIngestionService.processNewOrders(orderIds);

        log.info("[PlanningChain] OrderIngestion node completed, processed {} orders", orderIds.size());
    }
}
