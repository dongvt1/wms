package com.cy.modules.planning.agent.properties;

import com.cy.modules.planning.agent.entity.ApSyncStatus;
import com.cy.modules.planning.agent.enums.SyncStatus;
import com.cy.modules.planning.agent.mapper.ApSyncStatusMapper;
import com.cy.modules.planning.agent.service.InventorySyncService;
import com.cy.modules.planning.agent.service.MachineSyncService;
import com.cy.modules.planning.agent.service.OrderSyncService;
import com.cy.modules.planning.agent.service.PlanningNotificationService;
import com.cy.modules.planning.agent.service.QualitySyncService;
import com.cy.modules.planning.agent.service.impl.StalenessManagementServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Property-based test for Data staleness blocking.
 *
 * **Validates: Requirements 12.4**
 *
 * Property 27: Data staleness blocking.
 * For any cached data from an integrated system, the system SHALL prevent new planning
 * decisions if the cache staleness exceeds 60 minutes; and SHALL allow planning decisions
 * when staleness is ≤ 60 minutes (using cached data with a warning when the source system
 * is unavailable).
 */
@Tag("property-test")
@Tag("ai-production-planning")
class DataStalenessBlockingPropertyTest {

    private static final long BLOCKING_THRESHOLD_MINUTES = 60;
    private static final String[] SYSTEMS = {"orderhub", "erp", "scada", "qms"};

    // ==================== Helper methods ====================

    /**
     * Creates a fresh StalenessManagementServiceImpl with mocked dependencies.
     * jqwik doesn't support Mockito @Mock annotations directly, so we create mocks manually.
     */
    private StalenessManagementServiceImpl createServiceWithMocks(List<ApSyncStatus> syncStatuses) {
        StalenessManagementServiceImpl service = new StalenessManagementServiceImpl();

        ApSyncStatusMapper mapper = Mockito.mock(ApSyncStatusMapper.class);
        OrderSyncService orderSyncService = Mockito.mock(OrderSyncService.class);
        InventorySyncService inventorySyncService = Mockito.mock(InventorySyncService.class);
        MachineSyncService machineSyncService = Mockito.mock(MachineSyncService.class);
        QualitySyncService qualitySyncService = Mockito.mock(QualitySyncService.class);
        PlanningNotificationService notificationService = Mockito.mock(PlanningNotificationService.class);

        // Mock mapper to return the provided sync statuses
        when(mapper.selectList(any())).thenReturn(syncStatuses);

        // Inject mocks via reflection
        injectField(service, "apSyncStatusMapper", mapper);
        injectField(service, "orderSyncService", orderSyncService);
        injectField(service, "inventorySyncService", inventorySyncService);
        injectField(service, "machineSyncService", machineSyncService);
        injectField(service, "qualitySyncService", qualitySyncService);
        injectField(service, "planningNotificationService", notificationService);

        return service;
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }

    private ApSyncStatus createSyncStatus(String systemName, long stalenessMinutes) {
        ApSyncStatus status = new ApSyncStatus();
        status.setId(UUID.randomUUID().toString());
        status.setSystemName(systemName);
        status.setConsecutiveFailures(0);
        status.setStatus(SyncStatus.ACTIVE.getValue());

        if (stalenessMinutes >= 0) {
            // Set lastSyncTime to (now - stalenessMinutes)
            long lastSyncMillis = System.currentTimeMillis() - (stalenessMinutes * 60 * 1000);
            status.setLastSyncTime(new Date(lastSyncMillis));
        } else {
            // null lastSyncTime means never synced (infinite staleness)
            status.setLastSyncTime(null);
        }

        return status;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<String> systemNames() {
        return Arbitraries.of(SYSTEMS);
    }

    @Provide
    Arbitrary<Long> stalenessAboveThreshold() {
        // Staleness values strictly above 60 minutes (61 to 1440 minutes = 24 hours)
        return Arbitraries.longs().between(61, 1440);
    }

    @Provide
    Arbitrary<Long> stalenessAtOrBelowThreshold() {
        // Staleness values at or below 60 minutes (0 to 60)
        return Arbitraries.longs().between(0, 60);
    }

    // ==================== Property 27a: Planning blocked when staleness > 60 minutes ====================

    /**
     * Property 27a: For any system with cache staleness exceeding 60 minutes,
     * the system SHALL prevent new planning decisions (isPlanningBlocked returns true).
     *
     * **Validates: Requirements 12.4**
     */
    @Property(tries = 200)
    void planningIsBlockedWhenAnyStalenessExceedsSixtyMinutes(
            @ForAll("systemNames") String staleSystem,
            @ForAll("stalenessAboveThreshold") long stalenessMinutes) {

        // Create sync statuses where one system exceeds the threshold
        List<ApSyncStatus> statuses = new ArrayList<>();
        for (String system : SYSTEMS) {
            if (system.equals(staleSystem)) {
                statuses.add(createSyncStatus(system, stalenessMinutes));
            } else {
                // Other systems are fresh (10 minutes staleness)
                statuses.add(createSyncStatus(system, 10));
            }
        }

        StalenessManagementServiceImpl service = createServiceWithMocks(statuses);

        // Verify planning is blocked
        assertThat(service.isPlanningBlocked())
                .as("Planning should be blocked when system '%s' has staleness of %d minutes (> %d threshold)",
                        staleSystem, stalenessMinutes, BLOCKING_THRESHOLD_MINUTES)
                .isTrue();
    }

    // ==================== Property 27b: Planning allowed when staleness ≤ 60 minutes ====================

    /**
     * Property 27b: For all systems with cache staleness at or below 60 minutes,
     * the system SHALL allow planning decisions (isPlanningBlocked returns false).
     *
     * **Validates: Requirements 12.4**
     */
    @Property(tries = 200)
    void planningIsAllowedWhenAllStalenessAtOrBelowSixtyMinutes(
            @ForAll @IntRange(min = 0, max = 60) int staleness1,
            @ForAll @IntRange(min = 0, max = 60) int staleness2,
            @ForAll @IntRange(min = 0, max = 60) int staleness3,
            @ForAll @IntRange(min = 0, max = 60) int staleness4) {

        // Create sync statuses where all systems are within threshold
        List<ApSyncStatus> statuses = new ArrayList<>();
        statuses.add(createSyncStatus("orderhub", staleness1));
        statuses.add(createSyncStatus("erp", staleness2));
        statuses.add(createSyncStatus("scada", staleness3));
        statuses.add(createSyncStatus("qms", staleness4));

        StalenessManagementServiceImpl service = createServiceWithMocks(statuses);

        // Verify planning is NOT blocked
        assertThat(service.isPlanningBlocked())
                .as("Planning should be allowed when all systems have staleness ≤ %d minutes " +
                                "(orderhub=%d, erp=%d, scada=%d, qms=%d)",
                        BLOCKING_THRESHOLD_MINUTES, staleness1, staleness2, staleness3, staleness4)
                .isFalse();
    }

    // ==================== Property 27c: Null lastSyncTime blocks planning ====================

    /**
     * Property 27c: For any system that has never synced (lastSyncTime is null),
     * the system SHALL prevent planning decisions since staleness is effectively infinite.
     *
     * **Validates: Requirements 12.4**
     */
    @Property(tries = 100)
    void planningIsBlockedWhenAnySystemNeverSynced(
            @ForAll("systemNames") String neverSyncedSystem) {

        // Create sync statuses where one system has never synced
        List<ApSyncStatus> statuses = new ArrayList<>();
        for (String system : SYSTEMS) {
            if (system.equals(neverSyncedSystem)) {
                // Never synced: lastSyncTime = null → infinite staleness
                statuses.add(createSyncStatus(system, -1));
            } else {
                // Other systems are fresh
                statuses.add(createSyncStatus(system, 5));
            }
        }

        StalenessManagementServiceImpl service = createServiceWithMocks(statuses);

        // Verify planning is blocked
        assertThat(service.isPlanningBlocked())
                .as("Planning should be blocked when system '%s' has never synced (null lastSyncTime)",
                        neverSyncedSystem)
                .isTrue();
    }

    // ==================== Property 27d: Multiple stale systems still block ====================

    /**
     * Property 27d: For any combination where multiple systems exceed the staleness threshold,
     * planning SHALL still be blocked.
     *
     * **Validates: Requirements 12.4**
     */
    @Property(tries = 200)
    void planningIsBlockedWhenMultipleSystemsAreStale(
            @ForAll @IntRange(min = 61, max = 500) int staleness1,
            @ForAll @IntRange(min = 61, max = 500) int staleness2,
            @ForAll @IntRange(min = 0, max = 60) int staleness3,
            @ForAll @IntRange(min = 0, max = 60) int staleness4) {

        // Create sync statuses where at least 2 systems exceed threshold
        List<ApSyncStatus> statuses = new ArrayList<>();
        statuses.add(createSyncStatus("orderhub", staleness1));
        statuses.add(createSyncStatus("erp", staleness2));
        statuses.add(createSyncStatus("scada", staleness3));
        statuses.add(createSyncStatus("qms", staleness4));

        StalenessManagementServiceImpl service = createServiceWithMocks(statuses);

        // Verify planning is blocked
        assertThat(service.isPlanningBlocked())
                .as("Planning should be blocked when multiple systems exceed staleness threshold " +
                                "(orderhub=%d, erp=%d, scada=%d, qms=%d)",
                        staleness1, staleness2, staleness3, staleness4)
                .isTrue();
    }

    // ==================== Property 27e: Boundary condition at exactly 60 minutes ====================

    /**
     * Property 27e: At exactly 60 minutes staleness, planning SHALL still be allowed
     * (the threshold is strictly greater than 60 minutes to block).
     *
     * **Validates: Requirements 12.4**
     */
    @Property(tries = 100)
    void planningIsAllowedAtExactlySixtyMinutesStaleness(
            @ForAll("systemNames") String boundarySystem) {

        // Create sync statuses where one system is at exactly 60 minutes
        List<ApSyncStatus> statuses = new ArrayList<>();
        for (String system : SYSTEMS) {
            if (system.equals(boundarySystem)) {
                statuses.add(createSyncStatus(system, 60));
            } else {
                statuses.add(createSyncStatus(system, 10));
            }
        }

        StalenessManagementServiceImpl service = createServiceWithMocks(statuses);

        // Verify planning is NOT blocked at exactly 60 minutes
        // The implementation uses > (strictly greater than) for blocking
        assertThat(service.isPlanningBlocked())
                .as("Planning should be allowed when system '%s' has exactly 60 minutes staleness " +
                                "(threshold is strictly > 60)",
                        boundarySystem)
                .isFalse();
    }

    // ==================== Property 27f: Sync status DTO reflects blocking state ====================

    /**
     * Property 27f: The sync status DTO for each system correctly reflects whether
     * that system's staleness causes blocking (isBlocked field).
     *
     * **Validates: Requirements 12.4**
     */
    @Property(tries = 200)
    void syncStatusDtoCorrectlyReflectsBlockingState(
            @ForAll("systemNames") String systemName,
            @ForAll @IntRange(min = 0, max = 200) int stalenessMinutes) {

        // Create a single system with the given staleness
        List<ApSyncStatus> statuses = new ArrayList<>();
        statuses.add(createSyncStatus(systemName, stalenessMinutes));

        StalenessManagementServiceImpl service = createServiceWithMocks(statuses);

        // Get all sync statuses as DTOs
        var dtoMap = service.getAllSyncStatuses();

        // Verify the system's DTO has correct isBlocked value
        var dto = dtoMap.get(systemName);
        assertThat(dto).isNotNull();

        boolean expectedBlocked = stalenessMinutes > BLOCKING_THRESHOLD_MINUTES;
        assertThat(dto.getIsBlocked())
                .as("System '%s' with staleness %d minutes should have isBlocked=%b (threshold=%d)",
                        systemName, stalenessMinutes, expectedBlocked, BLOCKING_THRESHOLD_MINUTES)
                .isEqualTo(expectedBlocked);
    }
}
