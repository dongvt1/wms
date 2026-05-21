package com.cy.modules.qms.service;

import com.cy.modules.qms.entity.ApprovalRecord;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.entity.StepResult;
import com.cy.modules.qms.mapper.ApprovalRecordMapper;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.mapper.StepResultMapper;
import com.cy.modules.qms.service.impl.ApprovalServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Re-inspection isolation.
 *
 * **Validates: Requirements 8.5**
 *
 * Property 16: Re-inspection isolation.
 * For any Inspection Execution with multiple steps where one step is marked for re-inspection,
 * only the re-inspection step SHALL be reset (status → "re_inspect", result → null).
 * All other steps that were previously completed SHALL remain locked and their data unchanged.
 */
class ReInspectionIsolationPropertyTest {

    // ==================== Data classes for generation ====================

    /**
     * Represents a generated step result configuration for testing.
     */
    static class StepResultConfig {
        final String id;
        final String stepName;
        final int sortOrder;
        final boolean isMandatory;
        final String status;
        final String result;
        final Date completedTime;

        StepResultConfig(String id, String stepName, int sortOrder, boolean isMandatory,
                         String status, String result, Date completedTime) {
            this.id = id;
            this.stepName = stepName;
            this.sortOrder = sortOrder;
            this.isMandatory = isMandatory;
            this.status = status;
            this.result = result;
            this.completedTime = completedTime;
        }

        @Override
        public String toString() {
            return String.format("StepResultConfig{id='%s', sortOrder=%d, mandatory=%s, status='%s', result='%s'}",
                    id, sortOrder, isMandatory, status, result);
        }
    }

    // ==================== Helper methods ====================

    private InspectionExecution createExecution(String executionId) {
        InspectionExecution execution = new InspectionExecution();
        execution.setId(executionId);
        execution.setExecutionCode("EXC20260315001");
        execution.setTemplateId("tpl-001");
        execution.setProductId("product-001");
        execution.setStageType("pqc");
        execution.setStatus("pending_approval");
        execution.setOverallResult("pass");
        execution.setInspector("inspector-001");
        return execution;
    }

    private StepResult createStepResult(String executionId, StepResultConfig config) {
        StepResult sr = new StepResult();
        sr.setId(config.id);
        sr.setExecutionId(executionId);
        sr.setStepId("step-" + config.sortOrder);
        sr.setStepName(config.stepName);
        sr.setSortOrder(config.sortOrder);
        sr.setIsMandatory(config.isMandatory ? 1 : 0);
        sr.setStatus(config.status);
        sr.setResult(config.result);
        sr.setCompletedTime(config.completedTime);
        return sr;
    }

    /**
     * Creates a fresh ApprovalServiceImpl with mocked dependencies for each test run.
     * jqwik doesn't support Mockito @Mock annotations directly, so we create mocks manually.
     */
    private ApprovalServiceImpl createServiceWithMocks(
            InspectionExecutionMapper executionMapper,
            StepResultMapper stepResultMapper,
            ApprovalRecordMapper approvalRecordMapper) throws Exception {

        ApprovalServiceImpl service = new ApprovalServiceImpl();

        // Inject mocks via reflection
        java.lang.reflect.Field execMapperField =
                ApprovalServiceImpl.class.getDeclaredField("inspectionExecutionMapper");
        execMapperField.setAccessible(true);
        execMapperField.set(service, executionMapper);

        java.lang.reflect.Field stepResultMapperField =
                ApprovalServiceImpl.class.getDeclaredField("stepResultMapper");
        stepResultMapperField.setAccessible(true);
        stepResultMapperField.set(service, stepResultMapper);

        java.lang.reflect.Field approvalRecordMapperField =
                ApprovalServiceImpl.class.getDeclaredField("approvalRecordMapper");
        approvalRecordMapperField.setAccessible(true);
        approvalRecordMapperField.set(service, approvalRecordMapper);

        return service;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<List<StepResultConfig>> multipleCompletedSteps() {
        // Generate 2-8 steps, all completed with pass/fail results
        Arbitrary<Integer> stepCount = Arbitraries.integers().between(2, 8);
        Arbitrary<String> resultArb = Arbitraries.of("pass", "fail");
        Arbitrary<Boolean> mandatoryArb = Arbitraries.of(true, false);

        return Combinators.combine(stepCount, resultArb.list().ofMinSize(2).ofMaxSize(8),
                        mandatoryArb.list().ofMinSize(2).ofMaxSize(8))
                .as((count, results, mandatories) -> {
                    List<StepResultConfig> configs = new ArrayList<>();
                    int actualCount = Math.min(count, Math.min(results.size(), mandatories.size()));
                    actualCount = Math.max(actualCount, 2); // Ensure at least 2 steps

                    for (int i = 0; i < actualCount; i++) {
                        String id = "sr-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                        String result = i < results.size() ? results.get(i) : "pass";
                        boolean mandatory = i < mandatories.size() ? mandatories.get(i) : true;
                        Date completedTime = new Date(System.currentTimeMillis() - (actualCount - i) * 60000L);

                        configs.add(new StepResultConfig(
                                id,
                                "Step " + (i + 1),
                                i + 1,
                                mandatory,
                                "completed",
                                result,
                                completedTime
                        ));
                    }
                    return configs;
                });
    }

    @Provide
    Arbitrary<Integer> targetStepIndex() {
        // Index of the step to mark for re-inspection (0-based)
        return Arbitraries.integers().between(0, 7);
    }

    // ==================== Property tests ====================

    /**
     * Property 16a: Only the targeted step is reset during re-inspection.
     *
     * Generate executions with multiple completed steps, mark one for re-inspect,
     * verify that ONLY the targeted step has its status set to "re_inspect" and result set to null.
     * All other steps remain unchanged.
     *
     * **Validates: Requirements 8.5**
     */
    @Property(tries = 200)
    void onlyTargetedStepIsResetDuringReInspection(
            @ForAll("multipleCompletedSteps") List<StepResultConfig> stepConfigs,
            @ForAll("targetStepIndex") int rawTargetIndex) throws Exception {

        // Ensure target index is within bounds
        int targetIndex = rawTargetIndex % stepConfigs.size();
        String executionId = "exec-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        StepResultConfig targetConfig = stepConfigs.get(targetIndex);
        String targetStepResultId = targetConfig.id;

        // Create mocks
        InspectionExecutionMapper executionMapper = mock(InspectionExecutionMapper.class);
        StepResultMapper stepResultMapper = mock(StepResultMapper.class);
        ApprovalRecordMapper approvalRecordMapper = mock(ApprovalRecordMapper.class);

        // Setup execution mock - must be in pending_approval state
        InspectionExecution execution = createExecution(executionId);
        when(executionMapper.selectById(executionId)).thenReturn(execution);
        when(executionMapper.updateById(any(InspectionExecution.class))).thenReturn(1);

        // Setup target step result mock
        StepResult targetStepResult = createStepResult(executionId, targetConfig);
        when(stepResultMapper.selectById(targetStepResultId)).thenReturn(targetStepResult);
        when(stepResultMapper.updateById(any(StepResult.class))).thenReturn(1);

        // Setup approval record mock
        when(approvalRecordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // Create service with mocks
        ApprovalServiceImpl service = createServiceWithMocks(executionMapper, stepResultMapper, approvalRecordMapper);

        // Act: call reInspect on the target step
        service.reInspect(executionId, targetStepResultId, "Cần kiểm tra lại bước này");

        // Assert: Only the target step was updated via stepResultMapper.updateById
        ArgumentCaptor<StepResult> stepResultCaptor = ArgumentCaptor.forClass(StepResult.class);
        verify(stepResultMapper, times(1)).updateById(stepResultCaptor.capture());

        StepResult updatedStep = stepResultCaptor.getValue();
        assertThat(updatedStep.getId())
                .as("Only the targeted step should be updated")
                .isEqualTo(targetStepResultId);
        assertThat(updatedStep.getStatus())
                .as("Targeted step status should be 're_inspect'")
                .isEqualTo("re_inspect");
        assertThat(updatedStep.getResult())
                .as("Targeted step result should be null (reset)")
                .isNull();
        assertThat(updatedStep.getCompletedTime())
                .as("Targeted step completedTime should be null (reset)")
                .isNull();
    }

    /**
     * Property 16b: Other steps' data remains completely unchanged after re-inspection.
     *
     * Generate executions with multiple completed steps, mark one for re-inspect,
     * verify that no other step's status or result is modified by the operation.
     * The stepResultMapper.updateById should only be called once (for the target step).
     *
     * **Validates: Requirements 8.5**
     */
    @Property(tries = 200)
    void otherStepsRemainUnchangedAfterReInspection(
            @ForAll("multipleCompletedSteps") List<StepResultConfig> stepConfigs,
            @ForAll("targetStepIndex") int rawTargetIndex) throws Exception {

        // Ensure target index is within bounds
        int targetIndex = rawTargetIndex % stepConfigs.size();
        String executionId = "exec-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        StepResultConfig targetConfig = stepConfigs.get(targetIndex);
        String targetStepResultId = targetConfig.id;

        // Create mocks
        InspectionExecutionMapper executionMapper = mock(InspectionExecutionMapper.class);
        StepResultMapper stepResultMapper = mock(StepResultMapper.class);
        ApprovalRecordMapper approvalRecordMapper = mock(ApprovalRecordMapper.class);

        // Setup execution mock
        InspectionExecution execution = createExecution(executionId);
        when(executionMapper.selectById(executionId)).thenReturn(execution);
        when(executionMapper.updateById(any(InspectionExecution.class))).thenReturn(1);

        // Setup target step result mock
        StepResult targetStepResult = createStepResult(executionId, targetConfig);
        when(stepResultMapper.selectById(targetStepResultId)).thenReturn(targetStepResult);
        when(stepResultMapper.updateById(any(StepResult.class))).thenReturn(1);

        // Setup approval record mock
        when(approvalRecordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // Create service with mocks
        ApprovalServiceImpl service = createServiceWithMocks(executionMapper, stepResultMapper, approvalRecordMapper);

        // Act: call reInspect
        service.reInspect(executionId, targetStepResultId, "Cần đo lại với thiết bị đã hiệu chuẩn");

        // Assert: stepResultMapper.updateById was called exactly ONCE (only for target step)
        verify(stepResultMapper, times(1)).updateById(any(StepResult.class));

        // Assert: No other step IDs were passed to selectById (only the target)
        verify(stepResultMapper, times(1)).selectById(targetStepResultId);

        // Assert: Other steps were never fetched or modified
        for (int i = 0; i < stepConfigs.size(); i++) {
            if (i != targetIndex) {
                StepResultConfig otherConfig = stepConfigs.get(i);
                verify(stepResultMapper, never()).selectById(otherConfig.id);
            }
        }
    }

    /**
     * Property 16c: Execution status transitions to in_progress but step data isolation holds.
     *
     * After re-inspection, the execution status changes to "in_progress" and overallResult
     * is cleared, but this does NOT affect individual step results (other than the target).
     *
     * **Validates: Requirements 8.5**
     */
    @Property(tries = 200)
    void executionStatusChangesButStepIsolationHolds(
            @ForAll("multipleCompletedSteps") List<StepResultConfig> stepConfigs,
            @ForAll("targetStepIndex") int rawTargetIndex) throws Exception {

        // Ensure target index is within bounds
        int targetIndex = rawTargetIndex % stepConfigs.size();
        String executionId = "exec-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        StepResultConfig targetConfig = stepConfigs.get(targetIndex);
        String targetStepResultId = targetConfig.id;

        // Create mocks
        InspectionExecutionMapper executionMapper = mock(InspectionExecutionMapper.class);
        StepResultMapper stepResultMapper = mock(StepResultMapper.class);
        ApprovalRecordMapper approvalRecordMapper = mock(ApprovalRecordMapper.class);

        // Setup execution mock
        InspectionExecution execution = createExecution(executionId);
        when(executionMapper.selectById(executionId)).thenReturn(execution);
        when(executionMapper.updateById(any(InspectionExecution.class))).thenReturn(1);

        // Setup target step result mock
        StepResult targetStepResult = createStepResult(executionId, targetConfig);
        when(stepResultMapper.selectById(targetStepResultId)).thenReturn(targetStepResult);
        when(stepResultMapper.updateById(any(StepResult.class))).thenReturn(1);

        // Setup approval record mock
        when(approvalRecordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // Create service with mocks
        ApprovalServiceImpl service = createServiceWithMocks(executionMapper, stepResultMapper, approvalRecordMapper);

        // Act
        service.reInspect(executionId, targetStepResultId, "Giá trị đo không chính xác");

        // Assert: Execution status changed to in_progress
        ArgumentCaptor<InspectionExecution> execCaptor = ArgumentCaptor.forClass(InspectionExecution.class);
        verify(executionMapper).updateById(execCaptor.capture());

        InspectionExecution updatedExecution = execCaptor.getValue();
        assertThat(updatedExecution.getStatus())
                .as("Execution status should transition to 'in_progress'")
                .isEqualTo("in_progress");
        assertThat(updatedExecution.getOverallResult())
                .as("Execution overallResult should be cleared")
                .isNull();

        // Assert: Only ONE step was modified (isolation)
        verify(stepResultMapper, times(1)).updateById(any(StepResult.class));
    }

    /**
     * Property 16d: ApprovalRecord correctly references only the re-inspected step.
     *
     * When re-inspection is triggered, the created ApprovalRecord should reference
     * only the specific stepResultId that was marked for re-inspection.
     *
     * **Validates: Requirements 8.5**
     */
    @Property(tries = 200)
    void approvalRecordReferencesOnlyReInspectedStep(
            @ForAll("multipleCompletedSteps") List<StepResultConfig> stepConfigs,
            @ForAll("targetStepIndex") int rawTargetIndex) throws Exception {

        // Ensure target index is within bounds
        int targetIndex = rawTargetIndex % stepConfigs.size();
        String executionId = "exec-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        StepResultConfig targetConfig = stepConfigs.get(targetIndex);
        String targetStepResultId = targetConfig.id;

        // Create mocks
        InspectionExecutionMapper executionMapper = mock(InspectionExecutionMapper.class);
        StepResultMapper stepResultMapper = mock(StepResultMapper.class);
        ApprovalRecordMapper approvalRecordMapper = mock(ApprovalRecordMapper.class);

        // Setup execution mock
        InspectionExecution execution = createExecution(executionId);
        when(executionMapper.selectById(executionId)).thenReturn(execution);
        when(executionMapper.updateById(any(InspectionExecution.class))).thenReturn(1);

        // Setup target step result mock
        StepResult targetStepResult = createStepResult(executionId, targetConfig);
        when(stepResultMapper.selectById(targetStepResultId)).thenReturn(targetStepResult);
        when(stepResultMapper.updateById(any(StepResult.class))).thenReturn(1);

        // Setup approval record mock
        when(approvalRecordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // Create service with mocks
        ApprovalServiceImpl service = createServiceWithMocks(executionMapper, stepResultMapper, approvalRecordMapper);

        // Act
        service.reInspect(executionId, targetStepResultId, "Cần kiểm tra lại");

        // Assert: ApprovalRecord references the correct step
        ArgumentCaptor<ApprovalRecord> recordCaptor = ArgumentCaptor.forClass(ApprovalRecord.class);
        verify(approvalRecordMapper).insert(recordCaptor.capture());

        ApprovalRecord record = recordCaptor.getValue();
        assertThat(record.getExecutionId())
                .as("ApprovalRecord should reference the correct execution")
                .isEqualTo(executionId);
        assertThat(record.getStepResultId())
                .as("ApprovalRecord should reference only the re-inspected step")
                .isEqualTo(targetStepResultId);
        assertThat(record.getAction())
                .as("ApprovalRecord action should be 're_inspect'")
                .isEqualTo("re_inspect");
        assertThat(record.getReason())
                .as("ApprovalRecord should contain the reason")
                .isEqualTo("Cần kiểm tra lại");
    }
}
