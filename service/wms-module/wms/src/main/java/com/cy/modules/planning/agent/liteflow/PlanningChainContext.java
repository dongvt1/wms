package com.cy.modules.planning.agent.liteflow;

import com.cy.modules.planning.agent.entity.RescheduleRecord;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Context bean shared between LiteFlow nodes within a chain execution.
 * Used to pass data between sequential nodes in the rescheduling and execution chains.
 */
@Data
public class PlanningChainContext {

    /**
     * Deviations detected by DeviationDetectionCmp, consumed by ImpactAssessmentCmp.
     */
    private List<RescheduleRecord> deviations = new ArrayList<>();

    /**
     * Weekly plan ID being processed in the execution chain.
     */
    private String weeklyPlanId;

    /**
     * List of affected order IDs identified during impact assessment.
     */
    private List<String> affectedOrderIds = new ArrayList<>();

    /**
     * Flag indicating whether production orders were successfully issued.
     */
    private boolean ordersIssued = false;

    /**
     * Flag indicating whether material issuance was triggered.
     */
    private boolean materialIssuanceTriggered = false;
}
