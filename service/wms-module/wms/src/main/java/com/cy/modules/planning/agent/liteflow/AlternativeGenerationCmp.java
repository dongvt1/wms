package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.RescheduleRecord;
import com.cy.modules.planning.agent.service.ReschedulingService;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * LiteFlow node component for alternative rescheduling option generation.
 * Delegates to ReschedulingService to generate ≥2 rescheduling options
 * ranked by optimization score for each detected deviation.
 */
@Slf4j
@Component("alternativeGeneration")
public class AlternativeGenerationCmp extends NodeComponent {

    @Resource
    private ReschedulingService reschedulingService;

    @Override
    public void process() throws Exception {
        log.info("[ReschedulingChain] AlternativeGeneration node started");

        PlanningChainContext context = this.getContextBean(PlanningChainContext.class);
        List<RescheduleRecord> deviations = context.getDeviations();

        if (deviations == null || deviations.isEmpty()) {
            log.info("[ReschedulingChain] No deviations requiring alternative generation");
            return;
        }

        int totalOptions = 0;
        for (RescheduleRecord record : deviations) {
            // getReschedulingOptions generates and returns alternatives for the record
            List<RescheduleRecord> options = reschedulingService.getReschedulingOptions(record.getId());
            totalOptions += (options != null ? options.size() : 0);
        }

        log.info("[ReschedulingChain] AlternativeGeneration node completed. Generated {} total options", totalOptions);
    }
}
