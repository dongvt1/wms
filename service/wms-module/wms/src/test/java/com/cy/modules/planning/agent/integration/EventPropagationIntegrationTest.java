package com.cy.modules.planning.agent.integration;

import com.cy.modules.planning.agent.enums.NotificationType;
import com.cy.modules.planning.agent.event.*;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.ProductionOrderIssuanceService;
import com.cy.modules.planning.agent.service.ReschedulingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for event propagation between services.
 * Tests that Spring Events published by services are received by PlanningEventListener
 * and trigger appropriate actions.
 *
 * **Validates: All (integration verification)**
 */
@DisplayName("Event Propagation Integration Tests")
class EventPropagationIntegrationTest {

    private PlanningEventListener planningEventListener;
    private PlanningNotificationService notificationService;
    private ReschedulingService reschedulingService;
    private ProductionOrderIssuanceService productionOrderIssuanceService;

    @BeforeEach
    void setUp() {
        notificationService = mock(PlanningNotificationService.class);
        reschedulingService = mock(ReschedulingService.class);
        productionOrderIssuanceService = mock(ProductionOrderIssuanceService.class);

        planningEventListener = new PlanningEventListener(
                notificationService, reschedulingService, productionOrderIssuanceService);
    }

    // ==================== MaterialShortageEvent Tests ====================

    @Test
    @DisplayName("MaterialShortageEvent triggers notification to production manager")
    void materialShortageEvent_triggersNotification() {
        // Arrange
        Map<String, BigDecimal> deficits = Map.of(
                "MAT-001", new BigDecimal("50.5"),
                "MAT-002", new BigDecimal("120.0")
        );
        MaterialShortageEvent event = new MaterialShortageEvent(this, "order-001", deficits);

        // Act
        planningEventListener.onMaterialShortage(event);

        // Assert: Notification sent with correct type and data
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.MATERIAL_SHORTAGE),
                contains("order-001"),
                argThat(data -> {
                    assertThat(data.get("orderId")).isEqualTo("order-001");
                    assertThat(data.get("deficitCount")).isEqualTo(2);
                    return true;
                })
        );
    }

    @Test
    @DisplayName("MaterialShortageEvent includes all deficit materials in notification data")
    void materialShortageEvent_includesAllDeficits() {
        // Arrange
        Map<String, BigDecimal> deficits = Map.of(
                "MAT-001", new BigDecimal("10"),
                "MAT-002", new BigDecimal("20"),
                "MAT-003", new BigDecimal("30")
        );
        MaterialShortageEvent event = new MaterialShortageEvent(this, "order-002", deficits);

        // Act
        planningEventListener.onMaterialShortage(event);

        // Assert
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.MATERIAL_SHORTAGE),
                contains("3"),
                argThat(data -> {
                    @SuppressWarnings("unchecked")
                    Map<String, BigDecimal> materialDeficits =
                            (Map<String, BigDecimal>) data.get("materialDeficits");
                    assertThat(materialDeficits).hasSize(3);
                    assertThat(materialDeficits).containsKeys("MAT-001", "MAT-002", "MAT-003");
                    return true;
                })
        );
    }

    // ==================== DeviationDetectedEvent Tests ====================

    @Test
    @DisplayName("DeviationDetectedEvent triggers rescheduling service check")
    void deviationDetectedEvent_triggersReschedulingCheck() {
        // Arrange
        DeviationDetectedEvent event = new DeviationDetectedEvent(
                this, "wp-001", "batch-001", new BigDecimal("15.5"));

        when(reschedulingService.checkDailyDeviation("wp-001")).thenReturn(null);

        // Act
        planningEventListener.onDeviationDetected(event);

        // Assert: Rescheduling service is called
        verify(reschedulingService).checkDailyDeviation("wp-001");
    }

    @Test
    @DisplayName("DeviationDetectedEvent handles rescheduling service error gracefully")
    void deviationDetectedEvent_handlesServiceError() {
        // Arrange
        DeviationDetectedEvent event = new DeviationDetectedEvent(
                this, "wp-001", "batch-001", new BigDecimal("20.0"));

        when(reschedulingService.checkDailyDeviation("wp-001"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        planningEventListener.onDeviationDetected(event);

        // Assert: Error notification sent to production manager
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.SYSTEM_ERROR),
                contains("Database connection failed"),
                argThat(data -> {
                    assertThat(data.get("weeklyPlanId")).isEqualTo("wp-001");
                    assertThat(data.get("batchId")).isEqualTo("batch-001");
                    return true;
                })
        );
    }

    // ==================== PlanApprovedEvent Tests ====================

    @Test
    @DisplayName("PlanApprovedEvent for weekly plan triggers production order issuance")
    void planApprovedEvent_weeklyPlan_triggersOrderIssuance() {
        // Arrange
        PlanApprovedEvent event = new PlanApprovedEvent(
                this, "weekly", "wp-001", "manager-001");

        doNothing().when(productionOrderIssuanceService).issueProductionOrders("wp-001");

        // Act
        planningEventListener.onPlanApproved(event);

        // Assert: Production orders issued for weekly plan
        verify(productionOrderIssuanceService).issueProductionOrders("wp-001");

        // Assert: Notification sent about plan approval
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.PLAN_GENERATED),
                contains("weekly"),
                argThat(data -> {
                    assertThat(data.get("planType")).isEqualTo("weekly");
                    assertThat(data.get("planId")).isEqualTo("wp-001");
                    assertThat(data.get("approvedBy")).isEqualTo("manager-001");
                    return true;
                })
        );
    }

    @Test
    @DisplayName("PlanApprovedEvent for monthly plan does NOT trigger production order issuance")
    void planApprovedEvent_monthlyPlan_doesNotTriggerOrderIssuance() {
        // Arrange
        PlanApprovedEvent event = new PlanApprovedEvent(
                this, "monthly", "mp-001", "manager-001");

        // Act
        planningEventListener.onPlanApproved(event);

        // Assert: Production orders NOT issued for monthly plan
        verify(productionOrderIssuanceService, never()).issueProductionOrders(anyString());

        // Assert: Notification still sent about plan approval
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.PLAN_GENERATED),
                contains("monthly"),
                any()
        );
    }

    @Test
    @DisplayName("PlanApprovedEvent handles production order issuance failure")
    void planApprovedEvent_handlesIssuanceFailure() {
        // Arrange
        PlanApprovedEvent event = new PlanApprovedEvent(
                this, "weekly", "wp-001", "manager-001");

        doThrow(new RuntimeException("ERP unavailable"))
                .when(productionOrderIssuanceService).issueProductionOrders("wp-001");

        // Act
        planningEventListener.onPlanApproved(event);

        // Assert: Error notification sent
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.SYSTEM_ERROR),
                contains("ERP unavailable"),
                argThat(data -> {
                    assertThat(data.get("planId")).isEqualTo("wp-001");
                    return true;
                })
        );
    }

    // ==================== QualityAlertEvent Tests ====================

    @Test
    @DisplayName("QualityAlertEvent triggers notification with defect rate details")
    void qualityAlertEvent_triggersNotificationWithDetails() {
        // Arrange
        QualityAlertEvent event = new QualityAlertEvent(
                this, "batch-001", "ProductA", "LINE-01",
                new BigDecimal("12.5"), new BigDecimal("5.0"));

        // Act
        planningEventListener.onQualityAlert(event);

        // Assert: Notification includes quality details and suggestions
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.QUALITY_ALERT),
                contains("batch-001"),
                argThat(data -> {
                    assertThat(data.get("batchId")).isEqualTo("batch-001");
                    assertThat(data.get("productId")).isEqualTo("ProductA");
                    assertThat(data.get("lineId")).isEqualTo("LINE-01");
                    assertThat(data.get("currentDefectRate")).isEqualTo(new BigDecimal("12.5"));
                    assertThat(data.get("averageDefectRate")).isEqualTo(new BigDecimal("5.0"));
                    // Verify difference is calculated
                    assertThat(data.get("difference")).isEqualTo(new BigDecimal("7.5"));
                    // Verify suggestions are included
                    assertThat(data.get("suggestions")).isNotNull();
                    return true;
                })
        );
    }

    // ==================== SyncFailureEvent Tests ====================

    @Test
    @DisplayName("SyncFailureEvent triggers notification with system details")
    void syncFailureEvent_triggersNotification() {
        // Arrange
        Instant lastSuccess = Instant.now().minusSeconds(3600); // 1 hour ago
        SyncFailureEvent event = new SyncFailureEvent(
                this, "erp", 3, "Connection timeout", lastSuccess);

        // Act
        planningEventListener.onSyncFailure(event);

        // Assert
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.SYNC_FAILURE),
                argThat(msg -> msg.contains("erp") && msg.contains("3")),
                argThat(data -> {
                    assertThat(data.get("systemName")).isEqualTo("erp");
                    assertThat(data.get("consecutiveFailures")).isEqualTo(3);
                    assertThat(data.get("errorMessage")).isEqualTo("Connection timeout");
                    assertThat(data.get("lastSuccessTime")).isEqualTo(lastSuccess);
                    return true;
                })
        );
    }

    @Test
    @DisplayName("SyncFailureEvent for different systems sends correct system name")
    void syncFailureEvent_differentSystems_correctSystemName() {
        // Arrange & Act: Test each system type
        String[] systems = {"orderhub", "erp", "scada", "qms"};
        Instant lastSuccess = Instant.now();

        for (String system : systems) {
            reset(notificationService);
            SyncFailureEvent event = new SyncFailureEvent(
                    this, system, 2, "Timeout", lastSuccess);

            planningEventListener.onSyncFailure(event);

            verify(notificationService).notifyProductionManager(
                    eq(NotificationType.SYNC_FAILURE),
                    contains(system),
                    argThat((Map<String, Object> data) -> data.get("systemName").equals(system))
            );
        }
    }

    // ==================== Event Flow Integration Tests ====================

    @Test
    @DisplayName("Multiple events in sequence are handled independently")
    void multipleEvents_handledIndependently() {
        // Arrange
        MaterialShortageEvent shortageEvent = new MaterialShortageEvent(
                this, "order-001", Map.of("MAT-001", new BigDecimal("50")));

        DeviationDetectedEvent deviationEvent = new DeviationDetectedEvent(
                this, "wp-001", "batch-001", new BigDecimal("15"));

        QualityAlertEvent qualityEvent = new QualityAlertEvent(
                this, "batch-002", "ProductB", "LINE-02",
                new BigDecimal("10"), new BigDecimal("4"));

        when(reschedulingService.checkDailyDeviation("wp-001")).thenReturn(null);

        // Act: Process events in sequence
        planningEventListener.onMaterialShortage(shortageEvent);
        planningEventListener.onDeviationDetected(deviationEvent);
        planningEventListener.onQualityAlert(qualityEvent);

        // Assert: Each event triggered its own notification
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.MATERIAL_SHORTAGE), anyString(), any());
        verify(notificationService, never()).notifyProductionManager(
                eq(NotificationType.DEVIATION_DETECTED), anyString(), any());
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.QUALITY_ALERT), anyString(), any());

        // Assert: Rescheduling service called for deviation
        verify(reschedulingService).checkDailyDeviation("wp-001");
    }

    @Test
    @DisplayName("PlanApprovedEvent triggers full execution flow: approve → issue orders → notify")
    void planApprovedEvent_triggersFullExecutionFlow() {
        // Arrange
        PlanApprovedEvent event = new PlanApprovedEvent(
                this, "weekly", "wp-001", "admin");

        doNothing().when(productionOrderIssuanceService).issueProductionOrders("wp-001");

        // Act
        planningEventListener.onPlanApproved(event);

        // Assert: Full flow executed
        // 1. Production orders issued
        verify(productionOrderIssuanceService).issueProductionOrders("wp-001");
        // 2. Approval notification sent
        verify(notificationService).notifyProductionManager(
                eq(NotificationType.PLAN_GENERATED),
                anyString(),
                argThat(data -> "weekly".equals(data.get("planType"))
                        && "wp-001".equals(data.get("planId")))
        );
    }
}
