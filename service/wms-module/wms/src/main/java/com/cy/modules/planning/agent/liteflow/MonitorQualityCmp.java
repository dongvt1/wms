package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.WeeklyPlanBatch;
import com.cy.modules.planning.agent.mapper.WeeklyPlanBatchMapper;
import com.cy.modules.planning.agent.service.QualityIntegrationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * LiteFlow node component for monitoring quality.
 * Delegates to QualityIntegrationService to check quality alerts,
 * classify defects, and adjust for yield loss.
 *
 * This node runs in parallel with MonitorProgressCmp (WHEN operator in executionChain).
 */
@Slf4j
@Component("monitorQuality")
public class MonitorQualityCmp extends NodeComponent {

    @Resource
    private QualityIntegrationService qualityIntegrationService;

    @Resource
    private WeeklyPlanBatchMapper weeklyPlanBatchMapper;

    @Override
    public void process() throws Exception {
        log.info("[ExecutionChain] MonitorQuality node started");

        PlanningChainContext context = this.getContextBean(PlanningChainContext.class);
        String weeklyPlanId = context.getWeeklyPlanId();

        if (weeklyPlanId == null) {
            log.warn("[ExecutionChain] No weekly plan ID for quality monitoring");
            return;
        }

        // Find active batches for quality monitoring
        LambdaQueryWrapper<WeeklyPlanBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyPlanBatch::getWeeklyPlanId, weeklyPlanId)
               .eq(WeeklyPlanBatch::getStatus, "in_progress");

        List<WeeklyPlanBatch> activeBatches = weeklyPlanBatchMapper.selectList(wrapper);

        for (WeeklyPlanBatch batch : activeBatches) {
            qualityIntegrationService.checkQualityAlerts(batch.getId());
            qualityIntegrationService.classifyDefects(batch.getId());
        }

        log.info("[ExecutionChain] MonitorQuality node completed. Checked {} active batches", activeBatches.size());
    }
}
