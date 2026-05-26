package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.MonthlyPlan;
import com.cy.modules.planning.agent.entity.WeeklyPlan;
import com.cy.modules.planning.agent.mapper.MonthlyPlanMapper;
import com.cy.modules.planning.agent.service.WeeklyPlanService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * LiteFlow node component for weekly plan generation.
 * Delegates to WeeklyPlanService to decompose approved monthly plans
 * into detailed weekly schedules with production line and machine assignments.
 */
@Slf4j
@Component("weeklyPlan")
public class WeeklyPlanCmp extends NodeComponent {

    @Resource
    private WeeklyPlanService weeklyPlanService;

    @Resource
    private MonthlyPlanMapper monthlyPlanMapper;

    @Override
    public void process() throws Exception {
        log.info("[PlanningChain] WeeklyPlan node started");

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Find approved monthly plan for current period
        LambdaQueryWrapper<MonthlyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MonthlyPlan::getYear, year)
               .eq(MonthlyPlan::getMonth, month)
               .eq(MonthlyPlan::getStatus, "approved")
               .last("LIMIT 1");

        MonthlyPlan approvedPlan = monthlyPlanMapper.selectOne(wrapper);

        if (approvedPlan == null) {
            log.warn("[PlanningChain] No approved monthly plan found for {}/{}", year, month);
            return;
        }

        List<WeeklyPlan> weeklyPlans = weeklyPlanService.generateWeeklyPlans(approvedPlan.getId());

        log.info("[PlanningChain] WeeklyPlan node completed. Generated {} weekly plans", weeklyPlans.size());
    }
}
