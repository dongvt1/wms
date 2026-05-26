package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.RescheduleRecord;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.ReschedulingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * LiteFlow node component for deviation detection.
 * Delegates to ReschedulingService to check daily production deviations
 * for all weekly plans currently in execution.
 *
 * Stores detected deviations in the LiteFlow context slot for downstream nodes.
 */
@Slf4j
@Component("deviationDetection")
public class DeviationDetectionCmp extends NodeComponent {

    @Resource
    private ReschedulingService reschedulingService;

    @Resource
    private WeeklyPlanMapper weeklyPlanMapper;

    @Override
    public void process() throws Exception {
        log.info("[ReschedulingChain] DeviationDetection node started");

        // Find all weekly plans in execution
        LambdaQueryWrapper<WeeklyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlan::getStatus, "in_execution");

        List<WeeklyPlan> executingPlans = weeklyPlanMapper.selectList(wrapper);

        if (executingPlans.isEmpty()) {
            log.info("[ReschedulingChain] No plans in execution to check for deviations");
            return;
        }

        List<RescheduleRecord> deviations = new ArrayList<>();
        for (WeeklyPlan plan : executingPlans) {
            RescheduleRecord record = reschedulingService.checkDailyDeviation(plan.getId());
            if (record != null) {
                deviations.add(record);
            }
        }

        // Store deviations in context for downstream nodes
        this.getContextBean(PlanningChainContext.class).setDeviations(deviations);

        log.info("[ReschedulingChain] DeviationDetection node completed. Found {} deviations", deviations.size());
    }
}
