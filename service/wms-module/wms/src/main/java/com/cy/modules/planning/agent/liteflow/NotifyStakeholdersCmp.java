package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.RescheduleRecord;
import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LiteFlow node component for notifying stakeholders.
 * Delegates to PlanningNotificationService to notify the production manager
 * and affected order owners about rescheduling recommendations.
 */
@Slf4j
@Component("notifyStakeholders")
public class NotifyStakeholdersCmp extends NodeComponent {

    @Resource
    private PlanningNotificationService notificationService;

    @Override
    public void process() throws Exception {
        log.info("[ReschedulingChain] NotifyStakeholders node started");

        PlanningChainContext context = this.getContextBean(PlanningChainContext.class);
        List<RescheduleRecord> deviations = context.getDeviations();
        List<String> affectedOrderIds = context.getAffectedOrderIds();

        if (deviations == null || deviations.isEmpty()) {
            log.info("[ReschedulingChain] No deviations to notify about");
            return;
        }

        // Notify production manager about rescheduling recommendations
        Map<String, Object> data = new HashMap<>();
        data.put("deviationCount", deviations.size());
        data.put("affectedOrderCount", affectedOrderIds != null ? affectedOrderIds.size() : 0);

        notificationService.notifyProductionManager(
                NotificationType.RESCHEDULE_NEEDED,
                String.format("Detected %d production deviations requiring rescheduling", deviations.size()),
                data
        );

        // Notify affected order owners
        if (affectedOrderIds != null && !affectedOrderIds.isEmpty()) {
            notificationService.notifyOrderOwners(
                    affectedOrderIds,
                    "Your order may be affected by a production schedule adjustment. The production team is evaluating alternatives."
            );
        }

        log.info("[ReschedulingChain] NotifyStakeholders node completed. Notified manager and {} order owners",
                affectedOrderIds != null ? affectedOrderIds.size() : 0);
    }
}
