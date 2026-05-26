package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.MonthlyPlan;
import com.cy.modules.planning.agent.entity.QuarterlyPlan;
import com.cy.modules.planning.agent.enums.PlanStatus;
import com.cy.modules.planning.agent.mapper.QuarterlyPlanMapper;
import com.cy.modules.planning.agent.service.QuarterlyPlanService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * LiteFlow node component for monthly plan generation.
 * Delegates to QuarterlyPlanService to generate 1-3 ranked monthly plan options
 * from the active quarterly plan.
 */
@Slf4j
@Component("monthlyPlan")
public class MonthlyPlanCmp extends NodeComponent {

    @Resource
    private QuarterlyPlanService quarterlyPlanService;

    @Resource
    private QuarterlyPlanMapper quarterlyPlanMapper;

    @Override
    public void process() throws Exception {
        log.info("[PlanningChain] MonthlyPlan node started");

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Find the active quarterly plan for the current period
        LambdaQueryWrapper<QuarterlyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuarterlyPlan::getYear, year)
               .eq(QuarterlyPlan::getStatus, PlanStatus.ACTIVE.name().toLowerCase())
               .last("LIMIT 1");

        QuarterlyPlan quarterlyPlan = quarterlyPlanMapper.selectOne(wrapper);

        if (quarterlyPlan == null) {
            log.warn("[PlanningChain] No active quarterly plan found for year {}", year);
            return;
        }

        List<MonthlyPlan> options = quarterlyPlanService.generateMonthlyPlanOptions(
                quarterlyPlan.getId(), year, month);

        log.info("[PlanningChain] MonthlyPlan node completed. Generated {} options for {}/{}",
                options.size(), year, month);
    }
}
