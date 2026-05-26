# Implementation Plan: AI Production Planning Agent

## Overview

This implementation plan breaks down the AI Production Planning Agent into incremental coding tasks across the three phases: Data Synchronization & Input, Planning Core, and Execution Layer. Each task builds on previous work, starting with foundational data models and interfaces, progressing through core planning logic, and ending with execution monitoring and integration wiring. The implementation uses Java 21, Spring Boot 3.5.5, JeecgBoot framework, MyBatis-Plus, LiteFlow 2.15.0, and jqwik 1.9.1.

## Tasks

- [x] 1. Set up module structure, database schema, and core entities
  - [x] 1.1 Create module package structure and database migration scripts
    - Create package `com.cy.modules.planning.agent` with sub-packages: `entity`, `mapper`, `service`, `service/impl`, `controller`, `client`, `config`, `event`, `liteflow`, `dto`, `enums`
    - Create Flyway/Liquibase migration script with all 12 database tables (ap_planning_order, ap_material_availability, ap_purchase_request, ap_quarterly_plan, ap_monthly_plan, ap_weekly_plan, ap_weekly_plan_batch, ap_optimization_score, ap_production_progress, ap_reschedule_record, ap_sync_status, ap_supplier_lead_time)
    - _Requirements: All (foundational)_

  - [x] 1.2 Create JeecgBoot entity classes and MyBatis-Plus mappers
    - Create entity classes for all 12 tables extending JeecgEntity with proper annotations (@TableName, @TableField, @TableId)
    - Create corresponding Mapper interfaces extending BaseMapper
    - Create enums for status fields: OrderStatus, ValidationStatus, PlanStatus, BatchStatus, MaterialStatus, SyncStatus, TriggerType, NotificationType
    - _Requirements: All (foundational)_

  - [x] 1.3 Create integration client interfaces and DTOs
    - Define `OrderHubClient` interface with `fetchNewOrders(Instant since)` and `getOrderDetail(String orderId)`
    - Define `ErpClient` interface with inventory, BOM, capacity, production order, material issuance, warehouse receipt, and dispatch methods
    - Define `ScadaClient` interface with `getMachineStatuses` and `getProductionProgress`
    - Define `QmsClient` interface with `getQualityData` and `classifyDefects`
    - Define `PlanningNotificationService` interface with notification methods
    - Create request/response DTOs for all external system interactions
    - _Requirements: 1.1, 2.1, 9.1, 10.1, 12.1-12.3_

- [x] 2. Implement Data Synchronization Layer
  - [x] 2.1 Implement OrderSyncService
    - Create `OrderSyncService` that polls OrderHub every 5 minutes using @Scheduled
    - Transform external orders to internal `PlanningOrder` entities
    - Maintain sync cursor (last sync timestamp) in ap_sync_status
    - Handle deduplication via `external_order_id` unique key
    - Publish `OrdersReceivedEvent` Spring Event after successful sync
    - _Requirements: 1.1, 12.2_

  - [x] 2.2 Implement InventorySyncService
    - Create `InventorySyncService` that polls ERP-MRP-WMS every 15 minutes
    - Cache BOM data, supplier lead times, and stock levels in Redis
    - Track data freshness with `lastSyncTimestamp` in ap_sync_status
    - Implement retry logic: 3 attempts with exponential backoff
    - _Requirements: 2.1, 2.6, 12.1_

  - [x] 2.3 Implement MachineSyncService
    - Create `MachineSyncService` that polls Scada every 5 minutes
    - Cache production line availability and capacity data
    - Detect machine breakdowns via status changes and publish `MachineBreakdownEvent`
    - Handle 2 consecutive failures: notify manager, display last successful timestamp
    - _Requirements: 9.1, 9.5, 12.3_

  - [x] 2.4 Implement QualitySyncService
    - Create `QualitySyncService` that polls QMS every 15 minutes
    - Cache defect rates and inspection results
    - Calculate rolling 30-day and 90-day averages
    - Handle >30 minute data gap: use historical yield rates, display staleness warning
    - _Requirements: 10.1, 10.5, 12.1_

  - [x] 2.5 Implement SyncStatusController and staleness management
    - Create `SyncStatusController` with GET /status and POST /{system}/force-sync endpoints
    - Implement staleness calculation: `now() - lastSyncTime`
    - Implement blocking threshold: prevent new planning decisions if staleness > 60 minutes
    - Implement full data reconciliation within 10 minutes of sync restoration
    - _Requirements: 12.4, 12.5, 12.6_

  - [x] 2.6 Write property test for data staleness blocking (Property 27)
    - **Property 27: Data staleness blocking**
    - Verify system prevents planning decisions when cache staleness > 60 minutes
    - Verify system allows planning decisions when staleness ≤ 60 minutes with warning
    - **Validates: Requirements 12.4**

- [x] 3. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Implement Order Ingestion and Validation
  - [x] 4.1 Implement OrderIngestionService
    - Create `OrderIngestionService` that validates incoming orders for completeness (product type, customer name, quantity, deadline)
    - Validate data ranges: quantity > 0, deadline not in the past
    - Flag incomplete orders with `validation_status = 'incomplete'` and store error details in `validation_errors` JSON
    - Reject invalid orders with `validation_status = 'invalid'` and specific error field indication
    - Consolidate valid orders by product type, sort each group by deadline ascending
    - Maintain prioritized order queue: sorted by deadline ASC, then receipt_timestamp ASC for ties
    - Trigger notification within 5 minutes for incomplete/invalid orders
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [x] 4.2 Write property tests for order ingestion (Properties 1, 2, 3)
    - **Property 1: Order data extraction preserves all fields**
    - **Property 2: Order queue maintains sorting invariant**
    - **Property 3: Invalid orders are excluded from the queue**
    - Use jqwik generators for PlanningOrder with random product types, quantities, deadlines, timestamps
    - **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5**

- [x] 5. Implement Material Availability and Procurement
  - [x] 5.1 Implement MaterialAvailabilityService
    - Query cached inventory against BOM requirements for each order
    - Calculate material deficit: `max(0, required_qty - available_qty)` per material
    - Reserve materials when all BOM materials have sufficient inventory
    - Validate supplier delivery dates: flag order as at-risk if `current_date + lead_time > deadline`
    - Implement retry logic: 3 attempts with exponential backoff for ERP queries
    - Return availability result within 60 seconds
    - Notify production manager after 3 failed retries
    - _Requirements: 2.1, 2.2, 2.3, 2.5, 2.6, 2.7_

  - [x] 5.2 Write property tests for material calculations (Properties 4, 5)
    - **Property 4: Material deficit calculation correctness**
    - **Property 5: Material reservation and deadline validation**
    - Use jqwik generators for BomRequirement and InventoryLevel
    - **Validates: Requirements 2.2, 2.3, 2.5, 2.7**

  - [x] 5.3 Implement ProcurementCoordinationService
    - Generate Purchase Requests with `required_delivery_date = production_start_date - supplier_lead_time`
    - Include material type, deficit quantity, and required delivery date in PR
    - Generate at least 2 alternative scenarios when lead time exceeds deadline (expedited shipping, alternative suppliers, production rescheduling)
    - Each alternative includes estimated cost impact and revised delivery date
    - Maintain supplier lead time database updated after each completed procurement cycle
    - Recalculate production feasibility within 15 minutes when materials arrive
    - Notify production manager with earliest feasible date if no alternative meets deadline
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [x] 5.4 Write property tests for procurement (Properties 6, 7)
    - **Property 6: Purchase Request date calculation**
    - **Property 7: Alternative scenarios generation on deadline breach**
    - **Validates: Requirements 2.4, 3.1, 3.2**

- [x] 6. Implement Quarterly and Monthly Planning
  - [x] 6.1 Implement QuarterlyPlanService
    - Classify production demand by product type for each month within the quarter
    - Generate quarterly plan within 10 minutes of input data availability
    - Validate total monthly capacity against planned quantities using standard cycle times
    - Generate at least 2 alternatives when demand exceeds capacity (load redistribution, overtime scheduling)
    - Use cached capacity data when live data unavailable, flag plan as unvalidated
    - _Requirements: 4.1, 4.3, 4.4, 4.5_

  - [x] 6.2 Implement MonthlyPlanService (within QuarterlyPlanService)
    - Generate 1-3 ranked monthly plan options with quantity per product type, timeline, assigned lines, expected completion dates
    - Each option shows capacity utilization percentage
    - Store options in ap_monthly_plan with option_rank
    - _Requirements: 4.2_

  - [x] 6.3 Write property tests for quarterly/monthly planning (Properties 8, 9)
    - **Property 8: Quarterly plan demand aggregation**
    - **Property 9: Monthly plan capacity validation**
    - **Validates: Requirements 4.1, 4.3, 4.4**

- [x] 7. Implement Weekly Plan Generation and Optimization
  - [x] 7.1 Implement WeeklyPlanService
    - Decompose approved monthly plan into weekly schedules with product, quantity, daily timeline, line, and machine assignments
    - Assign products to lines based on machine capability, availability, and lowest changeover time
    - Enforce 90% capacity utilization cap per line per week
    - Sequence products on each line to minimize total changeover time
    - Verify material availability: all materials available or arriving ≥1 business day before batch start
    - Flag affected batches and suggest rescheduling or PR generation when material verification fails
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

  - [x] 7.2 Write property tests for weekly plan (Properties 10, 11, 12, 13)
    - **Property 10: Weekly plan preserves monthly totals**
    - **Property 11: Production line utilization cap**
    - **Property 12: Changeover time minimization**
    - **Property 13: Material availability verification for batches**
    - Use jqwik generators for ProductionLineCapacity and WeeklyPlanBatch
    - **Validates: Requirements 5.1, 5.3, 5.4, 5.5**

  - [x] 7.3 Implement PlanOptimizationService
    - Calculate optimization score (0-100) with weighted factors: deadline compliance (≥40%), machine utilization, material availability, order priority
    - All weights sum to 1.0, score bounded within [0, 100]
    - Rank valid sequences by score descending, present top 3 options
    - Incorporate 90-day historical data (cycle times, defect rates, downtime patterns)
    - Use BOM-based standard cycle times when historical data unavailable, indicate estimation to manager
    - When no plan satisfies all deadlines, present plan with fewest violations and delay estimates
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

  - [x] 7.4 Write property tests for optimization (Properties 14, 15, 16)
    - **Property 14: Optimization score calculation and bounds**
    - **Property 15: Plan ranking by optimization score**
    - **Property 16: Minimum-violation plan selection**
    - **Validates: Requirements 6.1, 6.2, 6.4, 6.5**

- [x] 8. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Implement Rescheduling Service
  - [x] 9.1 Implement ReschedulingService
    - Monitor daily production progress: compare actual vs planned quantities at daily intervals
    - Detect deviations >10% from planned quantities (cumulative daily measurement)
    - Generate rescheduling recommendation when threshold exceeded
    - Recalculate affected weekly plan within 30 minutes of machine breakdown or material delay
    - Generate ≥2 rescheduling options ranked by optimization score
    - Each option shows effects on delivery dates, line assignments, and resource utilization
    - Assess downstream order impact and notify affected order owners within 30 minutes
    - Present least-impact option when no option meets all deadlines
    - Create new plan version (immutable snapshot pattern) linked to original via parent_plan_id
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

  - [x] 9.2 Write property tests for rescheduling (Properties 17, 18)
    - **Property 17: Deviation detection threshold**
    - **Property 18: Rescheduling options with downstream impact**
    - **Validates: Requirements 7.2, 7.4, 7.5**

- [x] 10. Implement Production Order Issuance
  - [x] 10.1 Implement ProductionOrderIssuanceService
    - Generate Production Orders in ERP within 5 minutes of weekly plan approval
    - Each order includes: product spec, quantity, assigned line, assigned machine, start time, completion time
    - Trigger material issuance to WMS per BOM (material type, quantity per BOM, target line)
    - Retry logic: 3 attempts at 60-second intervals for failed orders
    - Notify production manager after 3 failed attempts with failed order details
    - Place affected Production Order on hold if material issuance fails
    - Update Weekly Plan status to "In Execution" when all orders acknowledged by ERP
    - Record issuance timestamp
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

  - [x] 10.2 Write property tests for production orders (Properties 19, 20)
    - **Property 19: Production order completeness**
    - **Property 20: Plan status transition on full acknowledgment**
    - **Validates: Requirements 8.2, 8.3, 8.6**

- [x] 11. Implement Execution Monitoring and Quality Integration
  - [x] 11.1 Implement ProductionExecutionMonitor
    - Collect machine status from Scada every ≤5 minutes
    - Calculate daily production results: quantities produced, defect rates, completion percentage
    - Calculate deviation_percentage = ((actual - planned) / planned) × 100
    - Record finished goods and trigger warehouse receipt in ERP
    - Generate material return request when remaining materials exceed minimum returnable quantity
    - Alert on 2 consecutive Scada collection failures with last successful timestamp
    - Retry warehouse receipt up to 3 times, notify manager on failure
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

  - [x] 11.2 Write property tests for execution monitoring (Properties 21, 22)
    - **Property 21: Daily production metrics calculation**
    - **Property 22: Material return threshold**
    - **Validates: Requirements 9.2, 9.4**

  - [x] 11.3 Implement QualityIntegrationService
    - Receive QMS data every ≤15 minutes
    - Calculate gross production quantity: `net_quantity / yield_rate` using 90-day historical yield
    - Alert when defect rate exceeds 30-day average by >5 percentage points
    - Suggest adjustments: increased quantity, line reassignment, or production pause
    - Classify defective products as repairable or destroyable via QMS
    - Subtract destroyable quantities from net output
    - Trigger additional production scheduling when net output falls below order requirements
    - Use most recent historical yield rate when QMS data unavailable >30 minutes
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

  - [x] 11.4 Write property tests for quality integration (Properties 23, 24, 25)
    - **Property 23: Quality alert threshold**
    - **Property 24: Gross production quantity calculation**
    - **Property 25: Defect impact on net output**
    - **Validates: Requirements 10.2, 10.3, 10.4**

- [x] 12. Implement Finished Goods and Dispatch
  - [x] 12.1 Implement FinishedGoodsDispatchService
    - Update order fulfillment status within 5 minutes of warehouse receipt: "In Production" (R=0, orders exist), "Partially Fulfilled" (0 < R < Q), "Fully Fulfilled" (R ≥ Q)
    - Calculate remaining quantities per customer order
    - Notify sales warehouse for dispatch via ERP within 10 minutes of full fulfillment
    - Maintain fulfillment dashboard: produced qty, warehouse stock, dispatched qty, fulfillment % per order
    - Refresh dashboard every ≤15 minutes
    - Retry warehouse receipt/dispatch notification up to 3 times, notify manager on failure
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

  - [x] 12.2 Write property test for fulfillment status (Property 26)
    - **Property 26: Order fulfillment status determination**
    - **Validates: Requirements 11.1, 11.2, 11.5**

- [x] 13. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 14. Implement REST Controllers and LiteFlow Orchestration
  - [x] 14.1 Implement PlanningAgentController
    - `POST /api/planning-agent/orders/sync` — Trigger manual order sync
    - `GET /api/planning-agent/orders/queue` — Get prioritized order queue with pagination
    - `GET /api/planning-agent/orders/{id}/material-check` — Check material availability for order
    - `POST /api/planning-agent/procurement/pr` — Generate Purchase Request
    - `GET /api/planning-agent/procurement/alternatives/{orderId}` — Get alternative scenarios
    - Apply JeecgBoot security annotations and sys_org_code data isolation
    - _Requirements: 1.1, 2.1, 3.1, 3.2_

  - [x] 14.2 Implement PlanController
    - `POST /api/planning-agent/plans/quarterly` — Generate quarterly plan
    - `GET /api/planning-agent/plans/quarterly/{id}` — Get quarterly plan details
    - `POST /api/planning-agent/plans/monthly` — Generate monthly plan options
    - `PUT /api/planning-agent/plans/monthly/{id}/approve` — Approve monthly plan
    - `POST /api/planning-agent/plans/weekly` — Generate weekly plans from approved monthly
    - `PUT /api/planning-agent/plans/weekly/{id}/approve` — Approve weekly plan
    - `GET /api/planning-agent/plans/weekly/{id}/optimization` — Get optimization details
    - `GET /api/planning-agent/plans/weekly/{id}/reschedule-options` — Get rescheduling options
    - _Requirements: 4.1, 4.2, 5.1, 6.2, 7.5_

  - [x] 14.3 Implement ExecutionController
    - `POST /api/planning-agent/execution/production-orders/{weeklyPlanId}` — Issue production orders
    - `GET /api/planning-agent/execution/progress/{weeklyPlanId}` — Get execution progress
    - `GET /api/planning-agent/execution/daily-results/{date}` — Get daily production results
    - `GET /api/planning-agent/execution/fulfillment/dashboard` — Get fulfillment dashboard
    - _Requirements: 8.1, 9.2, 11.3_

  - [x] 14.4 Implement LiteFlow chains and node components
    - Create LiteFlow node components: `OrderIngestionCmp`, `MaterialCheckCmp`, `ProcurementCheckCmp`, `QuarterlyPlanCmp`, `MonthlyPlanCmp`, `WeeklyPlanCmp`, `OptimizeAndRankCmp`
    - Create `DeviationDetectionCmp`, `ImpactAssessmentCmp`, `AlternativeGenerationCmp`, `NotifyStakeholdersCmp`
    - Create `IssueProductionOrdersCmp`, `TriggerMaterialIssuanceCmp`, `MonitorProgressCmp`, `MonitorQualityCmp`, `RecordFinishedGoodsCmp`, `DispatchNotificationCmp`
    - Define chain XML: planningChain, reschedulingChain, executionChain
    - _Requirements: All (orchestration layer)_

- [x] 15. Implement Spring Events and Notification Wiring
  - [x] 15.1 Implement event-driven architecture and notifications
    - Create Spring Events: `OrdersReceivedEvent`, `MaterialShortageEvent`, `MachineBreakdownEvent`, `DeviationDetectedEvent`, `PlanApprovedEvent`, `QualityAlertEvent`, `SyncFailureEvent`
    - Create event listeners that trigger appropriate service methods
    - Implement `PlanningNotificationService` with WebSocket push for real-time alerts
    - Implement notification types: ORDER_INCOMPLETE, ORDER_INVALID, MATERIAL_SHORTAGE, DEADLINE_AT_RISK, PLAN_GENERATED, DEVIATION_DETECTED, RESCHEDULE_NEEDED, QUALITY_ALERT, SYNC_FAILURE, SYSTEM_ERROR
    - Wire all services to publish events at appropriate lifecycle points
    - _Requirements: 1.3, 1.5, 2.6, 2.7, 3.5, 7.4, 9.5, 10.2, 12.4_

  - [x] 15.2 Write integration tests for LiteFlow chains and event flow
    - Test planningChain end-to-end with mocked external clients
    - Test reschedulingChain with deviation detection triggering
    - Test executionChain with production order issuance flow
    - Test event propagation between services
    - _Requirements: All (integration verification)_

- [x] 16. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties (27 properties using jqwik 1.9.1)
- Unit tests validate specific examples and edge cases
- The implementation follows JeecgBoot conventions: sys_org_code for multi-tenancy, BaseMapper for data access, standard controller patterns
- LiteFlow chains orchestrate the multi-step planning workflow with conditional branching
- All external system interactions use cached data with staleness tracking for resilience
- Immutable plan snapshots ensure full audit trail for rescheduling

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2"] },
    { "id": 2, "tasks": ["1.3"] },
    { "id": 3, "tasks": ["2.1", "2.2", "2.3", "2.4"] },
    { "id": 4, "tasks": ["2.5", "2.6"] },
    { "id": 5, "tasks": ["4.1"] },
    { "id": 6, "tasks": ["4.2", "5.1"] },
    { "id": 7, "tasks": ["5.2", "5.3"] },
    { "id": 8, "tasks": ["5.4", "6.1"] },
    { "id": 9, "tasks": ["6.2", "6.3"] },
    { "id": 10, "tasks": ["7.1"] },
    { "id": 11, "tasks": ["7.2", "7.3"] },
    { "id": 12, "tasks": ["7.4", "9.1"] },
    { "id": 13, "tasks": ["9.2", "10.1"] },
    { "id": 14, "tasks": ["10.2", "11.1"] },
    { "id": 15, "tasks": ["11.2", "11.3"] },
    { "id": 16, "tasks": ["11.4", "12.1"] },
    { "id": 17, "tasks": ["12.2"] },
    { "id": 18, "tasks": ["14.1", "14.2", "14.3"] },
    { "id": 19, "tasks": ["14.4", "15.1"] },
    { "id": 20, "tasks": ["15.2"] }
  ]
}
```
