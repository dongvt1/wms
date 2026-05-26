package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.dto.MaterialAvailabilityResult;
import com.cy.modules.planning.agent.entity.PlanningOrder;
import com.cy.modules.planning.agent.service.MaterialAvailabilityService;
import com.cy.modules.planning.agent.service.OrderIngestionService;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * LiteFlow node component for material availability check.
 * Delegates to MaterialAvailabilityService to check inventory against BOM requirements.
 *
 * Processes all orders in the prioritized queue and checks material availability for each.
 */
@Slf4j
@Component("materialCheck")
public class MaterialCheckCmp extends NodeComponent {

    @Resource
    private MaterialAvailabilityService materialAvailabilityService;

    @Resource
    private OrderIngestionService orderIngestionService;

    @Override
    public void process() throws Exception {
        log.info("[PlanningChain] MaterialCheck node started");

        List<PlanningOrder> orders = orderIngestionService.getPrioritizedOrderQueue();

        if (orders.isEmpty()) {
            log.warn("[PlanningChain] No orders in queue for material check");
            return;
        }

        int shortageCount = 0;
        for (PlanningOrder order : orders) {
            MaterialAvailabilityResult result = materialAvailabilityService.checkMaterialAvailability(order.getId());
            if (result != null && !result.isAllAvailable()) {
                shortageCount++;
            }
        }

        log.info("[PlanningChain] MaterialCheck node completed. Checked {} orders, {} with shortages",
                orders.size(), shortageCount);
    }
}
