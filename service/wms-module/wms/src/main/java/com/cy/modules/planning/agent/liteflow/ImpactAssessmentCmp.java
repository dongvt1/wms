package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.RescheduleRecord;
import com.cy.modules.planning.agent.service.ReschedulingService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * LiteFlow node component for impact assessment.
 * Analyzes the impact of detected deviations on downstream orders
 * and determines which orders and plans are affected.
 */
@Slf4j
@Component("impactAssessment")
public class ImpactAssessmentCmp extends NodeComponent {

    @Resource
    private ReschedulingService reschedulingService;

    @Override
    public void process() throws Exception {
        log.info("[ReschedulingChain] ImpactAssessment node started");

        PlanningChainContext context = this.getContextBean(PlanningChainContext.class);
        List<RescheduleRecord> deviations = context.getDeviations();

        if (deviations == null || deviations.isEmpty()) {
            log.info("[ReschedulingChain] No deviations to assess impact for");
            return;
        }

        List<String> allAffectedOrderIds = new ArrayList<>();
        for (RescheduleRecord record : deviations) {
            // Parse affected orders from the record
            if (record.getAffectedOrders() != null) {
                try {
                    JSONArray affectedArray = JSON.parseArray(record.getAffectedOrders());
                    for (int i = 0; i < affectedArray.size(); i++) {
                        String orderId = affectedArray.getJSONObject(i).getString("orderId");
                        if (orderId != null && !allAffectedOrderIds.contains(orderId)) {
                            allAffectedOrderIds.add(orderId);
                        }
                    }
                } catch (Exception e) {
                    log.warn("[ReschedulingChain] Failed to parse affected orders for record {}", record.getId(), e);
                }
            }
        }

        context.setAffectedOrderIds(allAffectedOrderIds);

        log.info("[ReschedulingChain] ImpactAssessment node completed. {} orders affected", allAffectedOrderIds.size());
    }
}
