package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.common.entity.ProductionStage;
import com.cy.modules.common.mapper.ProductionStageMapper;
import com.cy.modules.qms.dto.InspectionExecutionDTO;
import com.cy.modules.qms.event.QmsRoutingStepEventListener;
import com.cy.modules.qms.event.RoutingStepCompletedEvent;
import com.cy.modules.qms.vo.InspectionExecutionVO;
import net.jqwik.api.*;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Routing step blocking during inspection.
 *
 * **Validates: Requirements 9.3**
 *
 * Property 17: Routing step blocking during inspection.
 * For any production stage linked to a QC inspection that is not yet approved,
 * attempting to advance to the next routing step SHALL be blocked.
 * The block SHALL be released only when the inspection reaches "approved" status.
 */
class RoutingStepBlockingPropertyTest {

    // ==================== Helper methods ====================

    private QmsRoutingStepEventListener createListenerWithMocks(
            InspectionExecutionService executionService,
            ProductionStageMapper productionStageMapper) throws Exception {

        QmsRoutingStepEventListener listener = new QmsRoutingStepEventListener();

        // Inject mocks via reflection
        java.lang.reflect.Field execServiceField =
                QmsRoutingStepEventListener.class.getDeclaredField("inspectionExecutionService");
        execServiceField.setAccessible(true);
        execServiceField.set(listener, executionService);

        java.lang.reflect.Field stageMapperField =
                QmsRoutingStepEventListener.class.getDeclaredField("productionStageMapper");
        stageMapperField.setAccessible(true);
        stageMapperField.set(listener, productionStageMapper);

        return listener;
    }

    private ProductionStage createProductionStage(String id) {
        ProductionStage stage = new ProductionStage();
        stage.setId(id);
        stage.setStageName("Stage " + id);
        stage.setStageOrder(1);
        stage.setStatus("in_progress");
        stage.setQcBlocked(0);
        stage.setQcExecutionId(null);
        return stage;
    }

    private InspectionExecutionVO createExecutionVO(String executionId) {
        InspectionExecutionVO vo = new InspectionExecutionVO();
        vo.setId(executionId);
        vo.setExecutionCode("EXC20260315001");
        vo.setStatus("draft");
        return vo;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<String> qcStageTypes() {
        return Arbitraries.of("iqc", "pqc", "fqc");
    }

    @Provide
    Arbitrary<String> nonEmptyIds() {
        return Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20)
                .map(s -> "id-" + s);
    }

    @Provide
    Arbitrary<String> overallResults() {
        return Arbitraries.of("pass", "fail");
    }

    // ==================== Property tests ====================

    /**
     * Property 17a: When a routing step with qcStageType completes,
     * the production stage gets qc_blocked = 1.
     *
     * For any routing step completion event with a non-null qcStageType and a valid
     * productionStageId, the listener SHALL set qc_blocked = 1 on the production stage.
     *
     * **Validates: Requirements 9.3**
     */
    @Property(tries = 200)
    void routingStepWithQcStageBlocksProductionStage(
            @ForAll("qcStageTypes") String qcStageType,
            @ForAll("nonEmptyIds") String productId,
            @ForAll("nonEmptyIds") String workOrderId,
            @ForAll("nonEmptyIds") String productionStageId,
            @ForAll("nonEmptyIds") String stepId) throws Exception {

        // Create mocks
        InspectionExecutionService executionService = mock(InspectionExecutionService.class);
        ProductionStageMapper productionStageMapper = mock(ProductionStageMapper.class);

        // Setup execution service to return a valid execution
        String executionId = "exec-" + UUID.randomUUID().toString().substring(0, 8);
        InspectionExecutionVO executionVO = createExecutionVO(executionId);
        when(executionService.createExecution(any(InspectionExecutionDTO.class))).thenReturn(executionVO);

        // Setup production stage mapper
        ProductionStage productionStage = createProductionStage(productionStageId);
        when(productionStageMapper.selectById(productionStageId)).thenReturn(productionStage);
        when(productionStageMapper.updateById(any(ProductionStage.class))).thenReturn(1);

        // Create listener with mocks
        QmsRoutingStepEventListener listener = createListenerWithMocks(executionService, productionStageMapper);

        // Create event with qcStageType (should trigger blocking)
        RoutingStepCompletedEvent event = new RoutingStepCompletedEvent(
                this, stepId, productId, qcStageType, workOrderId, productionStageId);

        // Act
        listener.handleRoutingStepCompleted(event);

        // Assert: production stage was updated with qc_blocked = 1
        ArgumentCaptor<ProductionStage> stageCaptor = ArgumentCaptor.forClass(ProductionStage.class);
        verify(productionStageMapper).updateById(stageCaptor.capture());

        ProductionStage updatedStage = stageCaptor.getValue();
        assertThat(updatedStage.getQcBlocked())
                .as("Production stage should be blocked (qc_blocked=1) when routing step has qcStageType '%s'", qcStageType)
                .isEqualTo(1);
        assertThat(updatedStage.getQcExecutionId())
                .as("Production stage should be linked to the created execution")
                .isEqualTo(executionId);
    }

    /**
     * Property 17b: When the inspection is approved, the production stage gets qc_blocked = 0 (released).
     *
     * For any production stage that was previously blocked (qc_blocked=1) with a linked execution,
     * calling releaseQcBlock SHALL set qc_blocked = 0, regardless of the overall result (pass or fail).
     *
     * **Validates: Requirements 9.3**
     */
    @Property(tries = 200)
    void approvedInspectionReleasesProductionStageBlock(
            @ForAll("nonEmptyIds") String executionId,
            @ForAll("nonEmptyIds") String productionStageId,
            @ForAll("overallResults") String overallResult) throws Exception {

        approvedInspectionReleasesBlockInternal(executionId, productionStageId, overallResult);
    }

    private void approvedInspectionReleasesBlockInternal(
            String executionId, String productionStageId, String overallResult) throws Exception {

        // Create mocks
        InspectionExecutionService executionService = mock(InspectionExecutionService.class);
        ProductionStageMapper productionStageMapper = mock(ProductionStageMapper.class);

        // Setup production stage that is currently blocked
        ProductionStage blockedStage = createProductionStage(productionStageId);
        blockedStage.setQcBlocked(1);
        blockedStage.setQcExecutionId(executionId);

        // Mock the QueryWrapper-based selectOne to return the blocked stage
        when(productionStageMapper.selectOne(any(QueryWrapper.class))).thenReturn(blockedStage);
        when(productionStageMapper.updateById(any(ProductionStage.class))).thenReturn(1);

        // Create listener with mocks
        QmsRoutingStepEventListener listener = createListenerWithMocks(executionService, productionStageMapper);

        // Act: release the QC block
        listener.releaseQcBlock(executionId, overallResult);

        // Assert: production stage was updated with qc_blocked = 0
        ArgumentCaptor<ProductionStage> stageCaptor = ArgumentCaptor.forClass(ProductionStage.class);
        verify(productionStageMapper).updateById(stageCaptor.capture());

        ProductionStage updatedStage = stageCaptor.getValue();
        assertThat(updatedStage.getQcBlocked())
                .as("Production stage should be released (qc_blocked=0) after inspection approved with result '%s'", overallResult)
                .isEqualTo(0);
    }

    /**
     * Property 17c: When the routing step has no qcStageType, no blocking occurs.
     *
     * For any routing step completion event where qcStageType is null or empty,
     * the listener SHALL NOT create an inspection execution and SHALL NOT modify
     * the production stage's qc_blocked flag.
     *
     * **Validates: Requirements 9.3**
     */
    @Property(tries = 200)
    void routingStepWithoutQcStageDoesNotBlock(
            @ForAll("nonEmptyIds") String productId,
            @ForAll("nonEmptyIds") String workOrderId,
            @ForAll("nonEmptyIds") String productionStageId,
            @ForAll("nonEmptyIds") String stepId) throws Exception {

        // Create mocks
        InspectionExecutionService executionService = mock(InspectionExecutionService.class);
        ProductionStageMapper productionStageMapper = mock(ProductionStageMapper.class);

        // Create listener with mocks
        QmsRoutingStepEventListener listener = createListenerWithMocks(executionService, productionStageMapper);

        // Create event with null qcStageType (should NOT trigger blocking)
        RoutingStepCompletedEvent event = new RoutingStepCompletedEvent(
                this, stepId, productId, null, workOrderId, productionStageId);

        // Act
        listener.handleRoutingStepCompleted(event);

        // Assert: No execution was created
        verify(executionService, never()).createExecution(any(InspectionExecutionDTO.class));

        // Assert: Production stage was never modified
        verify(productionStageMapper, never()).selectById(anyString());
        verify(productionStageMapper, never()).updateById(any(ProductionStage.class));
    }

    /**
     * Property 17d: Empty qcStageType string also does not trigger blocking.
     *
     * For any routing step completion event where qcStageType is an empty or whitespace-only string,
     * the listener SHALL NOT create an inspection execution and SHALL NOT block the production stage.
     *
     * **Validates: Requirements 9.3**
     */
    @Property(tries = 100)
    void routingStepWithEmptyQcStageDoesNotBlock(
            @ForAll("nonEmptyIds") String productId,
            @ForAll("nonEmptyIds") String workOrderId,
            @ForAll("nonEmptyIds") String productionStageId,
            @ForAll("nonEmptyIds") String stepId) throws Exception {

        // Create mocks
        InspectionExecutionService executionService = mock(InspectionExecutionService.class);
        ProductionStageMapper productionStageMapper = mock(ProductionStageMapper.class);

        // Create listener with mocks
        QmsRoutingStepEventListener listener = createListenerWithMocks(executionService, productionStageMapper);

        // Create event with empty qcStageType (should NOT trigger blocking)
        RoutingStepCompletedEvent eventEmpty = new RoutingStepCompletedEvent(
                this, stepId, productId, "", workOrderId, productionStageId);

        // Act
        listener.handleRoutingStepCompleted(eventEmpty);

        // Assert: No execution was created
        verify(executionService, never()).createExecution(any(InspectionExecutionDTO.class));

        // Assert: Production stage was never modified
        verify(productionStageMapper, never()).selectById(anyString());
        verify(productionStageMapper, never()).updateById(any(ProductionStage.class));
    }

    /**
     * Property 17e: Whitespace-only qcStageType does not trigger blocking.
     *
     * **Validates: Requirements 9.3**
     */
    @Property(tries = 100)
    void routingStepWithWhitespaceQcStageDoesNotBlock(
            @ForAll("nonEmptyIds") String productId,
            @ForAll("nonEmptyIds") String workOrderId,
            @ForAll("nonEmptyIds") String productionStageId,
            @ForAll("nonEmptyIds") String stepId) throws Exception {

        // Create mocks
        InspectionExecutionService executionService = mock(InspectionExecutionService.class);
        ProductionStageMapper productionStageMapper = mock(ProductionStageMapper.class);

        // Create listener with mocks
        QmsRoutingStepEventListener listener = createListenerWithMocks(executionService, productionStageMapper);

        // Create event with whitespace-only qcStageType
        RoutingStepCompletedEvent eventWhitespace = new RoutingStepCompletedEvent(
                this, stepId, productId, "   ", workOrderId, productionStageId);

        // Act
        listener.handleRoutingStepCompleted(eventWhitespace);

        // Assert: No execution was created
        verify(executionService, never()).createExecution(any(InspectionExecutionDTO.class));

        // Assert: Production stage was never modified
        verify(productionStageMapper, never()).selectById(anyString());
        verify(productionStageMapper, never()).updateById(any(ProductionStage.class));
    }

    /**
     * Property 17f: The created InspectionExecution receives correct parameters from the event.
     *
     * For any routing step completion event with a valid qcStageType, the InspectionExecutionDTO
     * passed to createExecution SHALL contain the correct productId, stageType, workOrderId,
     * and productionStageId from the event.
     *
     * **Validates: Requirements 9.3**
     */
    @Property(tries = 200)
    void executionCreatedWithCorrectParametersFromEvent(
            @ForAll("qcStageTypes") String qcStageType,
            @ForAll("nonEmptyIds") String productId,
            @ForAll("nonEmptyIds") String workOrderId,
            @ForAll("nonEmptyIds") String productionStageId,
            @ForAll("nonEmptyIds") String stepId) throws Exception {

        // Create mocks
        InspectionExecutionService executionService = mock(InspectionExecutionService.class);
        ProductionStageMapper productionStageMapper = mock(ProductionStageMapper.class);

        // Setup execution service
        String executionId = "exec-" + UUID.randomUUID().toString().substring(0, 8);
        InspectionExecutionVO executionVO = createExecutionVO(executionId);
        when(executionService.createExecution(any(InspectionExecutionDTO.class))).thenReturn(executionVO);

        // Setup production stage mapper
        ProductionStage productionStage = createProductionStage(productionStageId);
        when(productionStageMapper.selectById(productionStageId)).thenReturn(productionStage);
        when(productionStageMapper.updateById(any(ProductionStage.class))).thenReturn(1);

        // Create listener with mocks
        QmsRoutingStepEventListener listener = createListenerWithMocks(executionService, productionStageMapper);

        // Create event
        RoutingStepCompletedEvent event = new RoutingStepCompletedEvent(
                this, stepId, productId, qcStageType, workOrderId, productionStageId);

        // Act
        listener.handleRoutingStepCompleted(event);

        // Assert: createExecution was called with correct parameters
        ArgumentCaptor<InspectionExecutionDTO> dtoCaptor = ArgumentCaptor.forClass(InspectionExecutionDTO.class);
        verify(executionService).createExecution(dtoCaptor.capture());

        InspectionExecutionDTO capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getProductId())
                .as("DTO productId should match event productId")
                .isEqualTo(productId);
        assertThat(capturedDto.getStageType())
                .as("DTO stageType should match event qcStageType")
                .isEqualTo(qcStageType);
        assertThat(capturedDto.getWorkOrderId())
                .as("DTO workOrderId should match event workOrderId")
                .isEqualTo(workOrderId);
        assertThat(capturedDto.getProductionStageId())
                .as("DTO productionStageId should match event productionStageId")
                .isEqualTo(productionStageId);
    }
}
