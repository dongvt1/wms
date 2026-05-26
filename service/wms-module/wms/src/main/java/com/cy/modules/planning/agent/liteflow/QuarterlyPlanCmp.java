package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.QuarterlyPlan;
import com.cy.modules.planning.agent.service.QuarterlyPlanService;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;

/**
 * LiteFlow node component for quarterly plan generation.
 * Delegates to QuarterlyPlanService to classify production demand by product type
 * for each month within the current quarter.
 */
@Slf4j
@Component("quarterlyPlan")
public class QuarterlyPlanCmp extends NodeComponent {

    @Resource
    private QuarterlyPlanService quarterlyPlanService;

    @Override
    public void process() throws Exception {
        log.info("[PlanningChain] QuarterlyPlan node started");

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;

        QuarterlyPlan plan = quarterlyPlanService.generateQuarterlyPlan(year, quarter);

        log.info("[PlanningChain] QuarterlyPlan node completed. Plan code: {}, capacity validated: {}",
                plan.getPlanCode(), plan.getCapacityValidated());
    }
}
