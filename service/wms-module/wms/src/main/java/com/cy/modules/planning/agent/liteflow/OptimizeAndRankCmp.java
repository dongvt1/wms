package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.mapper.WeeklyPlanMapper;
import com.cy.modules.planning.agent.service.PlanOptimizationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * LiteFlow node component for plan optimization and ranking.
 * Delegates to PlanOptimizationService to calculate optimization scores
 * and rank weekly plans by score descending, presenting top 3 options.
 */
@Slf4j
@Component("optimizeAndRank")
public class OptimizeAndRankCmp extends NodeComponent {

    @Resource
    private PlanOptimizationService planOptimizationService;

    @Resource
    private WeeklyPlanMapper weeklyPlanMapper;

    @Override
    public void process() throws Exception {
        log.info("[PlanningChain] OptimizeAndRank node started");

        // Find all draft weekly plans that need optimization
        LambdaQueryWrapper<WeeklyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlan::getStatus, "draft")
               .isNull(WeeklyPlan::getOptimizationScore);

        List<WeeklyPlan> unoptimizedPlans = weeklyPlanMapper.selectList(wrapper);

        if (unoptimizedPlans.isEmpty()) {
            log.info("[PlanningChain] No unoptimized weekly plans found");
            return;
        }

        for (WeeklyPlan plan : unoptimizedPlans) {
            planOptimizationService.optimizeWeeklyPlan(plan.getId());
        }

        log.info("[PlanningChain] OptimizeAndRank node completed. Optimized {} plans", unoptimizedPlans.size());
    }
}
