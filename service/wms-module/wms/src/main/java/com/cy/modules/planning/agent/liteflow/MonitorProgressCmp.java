package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.service.ProductionExecutionMonitor;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;

/**
 * LiteFlow node component for monitoring production progress.
 * Delegates to ProductionExecutionMonitor to collect machine status from Scada
 * and calculate daily production results.
 *
 * This node runs in parallel with MonitorQualityCmp (WHEN operator in executionChain).
 */
@Slf4j
@Component("monitorProgress")
public class MonitorProgressCmp extends NodeComponent {

    @Resource
    private ProductionExecutionMonitor productionExecutionMonitor;

    @Override
    public void process() throws Exception {
        log.info("[ExecutionChain] MonitorProgress node started");

        PlanningChainContext context = this.getContextBean(PlanningChainContext.class);
        String weeklyPlanId = context.getWeeklyPlanId();

        // Collect progress data from Scada
        productionExecutionMonitor.collectProgress();

        // Calculate daily results for today
        if (weeklyPlanId != null) {
            productionExecutionMonitor.calculateDailyResults(weeklyPlanId, LocalDate.now());
        }

        log.info("[ExecutionChain] MonitorProgress node completed");
    }
}
