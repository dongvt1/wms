package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.entity.StepResult;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.mapper.StepResultMapper;
import com.cy.modules.qms.service.impl.InspectionExecutionServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Completion validation enforces mandatory requirements.
 *
 * **Validates: Requirements 6.5, 6.7**
 *
 * Property 12: Completion validation enforces mandatory requirements.
 * For any Inspection Execution, submission SHALL be rejected if any mandatory Inspection Step
 * has status != "completed". Conversely, submission SHALL be accepted if all mandatory steps
 * have status = "completed" (optional steps don't matter).
 */
class CompletionValidationPropertyTest {

    // ==================== Data classes for generation ====================

    /**
     * Represents a generated step configuration for testing.
     */
    static class StepConfig {
        final boolean isMandatory;
        final String status; // "completed" or "pending"

        StepConfig(boolean isMandatory, String status) {
            this.isMandatory = isMandatory;
            this.status = status;
        }

        @Override
        public String toString() {
            return String.format("StepConfig{mandatory=%s, status='%s'}", isMandatory, status);
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
        execution.setStatus("in_progress");
        return execution;
    }

    private StepResult createStepResult(String executionId, int sortOrder,
                                         boolean isMandatory, String status) {
        StepResult sr = new StepResult();
        sr.setId(UUID.randomUUID().toString().replace("-", ""));
        sr.setExecutionId(executionId);
        sr.setStepId("step-" + sortOrder);
        sr.setStepName("Step " + sortOrder);
        sr.setSortOrder(sortOrder);
        sr.setIsMandatory(isMandatory ? 1 : 0);
        sr.setStatus(status);
        if ("completed".equals(status)) {
            sr.setResult("pass");
            sr.setCompletedTime(new Date());
        }
        return sr;
    }

    /**
     * Creates a fresh InspectionExecutionServiceImpl with mocked dependencies for each test run.
     * jqwik doesn't support Mockito @Mock annotations directly, so we create mocks manually.
     */
    private InspectionExecutionServiceImpl createServiceWithMocks(
            InspectionExecutionMapper executionMapper,
            StepResultMapper stepResultMapper,
            EvaluationService evaluationService) throws Exception {

        InspectionExecutionServiceImpl service = new InspectionExecutionServiceImpl();

        // Inject mocks via reflection
        java.lang.reflect.Field execMapperField =
                InspectionExecutionServiceImpl.class.getDeclaredField("inspectionExecutionMapper");
        execMapperField.setAccessible(true);
        execMapperField.set(service, executionMapper);

        java.lang.reflect.Field stepResultMapperField =
                InspectionExecutionServiceImpl.class.getDeclaredField("stepResultMapper");
        stepResultMapperField.setAccessible(true);
        stepResultMapperField.set(service, stepResultMapper);

        java.lang.reflect.Field evalServiceField =
                InspectionExecutionServiceImpl.class.getDeclaredField("evaluationService");
        evalServiceField.setAccessible(true);
        evalServiceField.set(service, evaluationService);

        return service;
    }

    // ==================== Arbitraries ====================

    @Provide
    Arbitrary<List<StepConfig>> allMandatoryCompleted() {
        // Generate 1-8 mandatory steps all completed + 0-5 optional steps with random status
        Arbitrary<Integer> mandatoryCount = Arbitraries.integers().between(1, 8);
        Arbitrary<Integer> optionalCount = Arbitraries.integers().between(0, 5);
        Arbitrary<String> optionalStatus = Arbitraries.of("completed", "pending");

        return Combinators.combine(mandatoryCount, optionalCount, optionalStatus.list().ofMinSize(0).ofMaxSize(5))
                .as((mCount, oCount, optStatuses) -> {
                    List<StepConfig> configs = new ArrayList<>();
                    // All mandatory steps are completed
                    for (int i = 0; i < mCount; i++) {
                        configs.add(new StepConfig(true, "completed"));
                    }
                    // Optional steps with random status
                    for (int i = 0; i < Math.min(oCount, optStatuses.size()); i++) {
                        configs.add(new StepConfig(false, optStatuses.get(i)));
                    }
                    // Fill remaining optional if needed
                    for (int i = optStatuses.size(); i < oCount; i++) {
                        configs.add(new StepConfig(false, "pending"));
                    }
                    return configs;
                });
    }

    @Provide
    Arbitrary<List<StepConfig>> someMandatoryIncomplete() {
        // Generate 1-8 mandatory steps where at least one is NOT completed + 0-5 optional steps
        Arbitrary<Integer> mandatoryCount = Arbitraries.integers().between(1, 8);
        Arbitrary<Integer> optionalCount = Arbitraries.integers().between(0, 5);
        Arbitrary<String> mandatoryStatus = Arbitraries.of("completed", "pending");
        Arbitrary<String> optionalStatus = Arbitraries.of("completed", "pending");

        return Combinators.combine(mandatoryCount, optionalCount,
                        mandatoryStatus.list().ofMinSize(1).ofMaxSize(8),
                        optionalStatus.list().ofMinSize(0).ofMaxSize(5))
                .as((mCount, oCount, mandStatuses, optStatuses) -> {
                    List<StepConfig> configs = new ArrayList<>();

                    // Generate mandatory steps - ensure at least one is NOT completed
                    List<String> actualMandStatuses = new ArrayList<>();
                    for (int i = 0; i < mCount && i < mandStatuses.size(); i++) {
                        actualMandStatuses.add(mandStatuses.get(i));
                    }
                    while (actualMandStatuses.size() < mCount) {
                        actualMandStatuses.add("completed");
                    }

                    // Ensure at least one mandatory step is NOT completed
                    boolean hasIncomplete = actualMandStatuses.stream()
                            .anyMatch(s -> !"completed".equals(s));
                    if (!hasIncomplete) {
                        // Force the first one to be pending
                        actualMandStatuses.set(0, "pending");
                    }

                    for (String status : actualMandStatuses) {
                        configs.add(new StepConfig(true, status));
                    }

                    // Optional steps with random status
                    for (int i = 0; i < oCount && i < optStatuses.size(); i++) {
                        configs.add(new StepConfig(false, optStatuses.get(i)));
                    }
                    for (int i = optStatuses.size(); i < oCount; i++) {
                        configs.add(new StepConfig(false, "pending"));
                    }

                    return configs;
                });
    }

    // ==================== Property tests ====================

    /**
     * Property 12a: Submission is REJECTED if any mandatory step has status != "completed".
     *
     * Generate executions with random mandatory/optional steps where at least one mandatory
     * step is not completed. Verify that submitExecution() throws IllegalStateException.
     *
     * **Validates: Requirements 6.5, 6.7**
     */
    @Property(tries = 200)
    void submissionRejectedWhenMandatoryStepIncomplete(
            @ForAll("someMandatoryIncomplete") List<StepConfig> stepConfigs) throws Exception {

        String executionId = "exec-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // Create mocks
        InspectionExecutionMapper executionMapper = mock(InspectionExecutionMapper.class);
        StepResultMapper stepResultMapper = mock(StepResultMapper.class);
        EvaluationService evaluationService = mock(EvaluationService.class);

        // Setup execution mock - status must be "in_progress" for submitExecution
        InspectionExecution execution = createExecution(executionId);
        when(executionMapper.selectById(executionId)).thenReturn(execution);

        // Build step results from configs
        List<StepResult> stepResults = new ArrayList<>();
        int sortOrder = 1;
        for (StepConfig config : stepConfigs) {
            stepResults.add(createStepResult(executionId, sortOrder++,
                    config.isMandatory, config.status));
        }

        when(stepResultMapper.selectList(any(QueryWrapper.class))).thenReturn(stepResults);

        // Create service with mocks
        InspectionExecutionServiceImpl service =
                createServiceWithMocks(executionMapper, stepResultMapper, evaluationService);

        // Act & Assert: submission should be rejected
        assertThatThrownBy(() -> service.submitExecution(executionId))
                .isInstanceOf(IllegalStateException.class)
                .satisfies(ex -> {
                    String message = ex.getMessage();
                    assertThat(message).contains("bắt buộc");
                });

        // Verify execution status was NOT updated to pending_approval
        verify(executionMapper, never()).updateById(any(InspectionExecution.class));
    }

    /**
     * Property 12b: Submission is ACCEPTED if all mandatory steps have status = "completed".
     * Optional steps can have any status without affecting submission.
     *
     * Generate executions where all mandatory steps are completed, with random optional steps
     * in any state. Verify that submitExecution() succeeds and transitions to pending_approval.
     *
     * **Validates: Requirements 6.5, 6.7**
     */
    @Property(tries = 200)
    void submissionAcceptedWhenAllMandatoryStepsCompleted(
            @ForAll("allMandatoryCompleted") List<StepConfig> stepConfigs) throws Exception {

        String executionId = "exec-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // Create mocks
        InspectionExecutionMapper executionMapper = mock(InspectionExecutionMapper.class);
        StepResultMapper stepResultMapper = mock(StepResultMapper.class);
        EvaluationService evaluationService = mock(EvaluationService.class);

        // Setup execution mock
        InspectionExecution execution = createExecution(executionId);
        when(executionMapper.selectById(executionId)).thenReturn(execution);

        // Build step results from configs
        List<StepResult> stepResults = new ArrayList<>();
        int sortOrder = 1;
        for (StepConfig config : stepConfigs) {
            stepResults.add(createStepResult(executionId, sortOrder++,
                    config.isMandatory, config.status));
        }

        when(stepResultMapper.selectList(any(QueryWrapper.class))).thenReturn(stepResults);
        when(evaluationService.evaluateExecution(any())).thenReturn("pass");
        when(executionMapper.updateById(any(InspectionExecution.class))).thenReturn(1);

        // Create service with mocks
        InspectionExecutionServiceImpl service =
                createServiceWithMocks(executionMapper, stepResultMapper, evaluationService);

        // Act: submission should succeed
        service.submitExecution(executionId);

        // Assert: execution was updated to pending_approval
        verify(executionMapper).updateById((InspectionExecution) argThat(exec -> {
            InspectionExecution updated = (InspectionExecution) exec;
            return "pending_approval".equals(updated.getStatus());
        }));

        // Assert: evaluateExecution was called
        verify(evaluationService).evaluateExecution(stepResults);
    }

    /**
     * Property 12c: Optional steps with any status do NOT block submission.
     *
     * Generate executions with exactly 1 mandatory completed step and multiple optional
     * steps in "pending" status. Verify submission still succeeds.
     *
     * **Validates: Requirements 6.7**
     */
    @Property(tries = 200)
    void optionalStepsDoNotBlockSubmission(
            @ForAll @IntRange(min = 1, max = 10) int optionalPendingCount) throws Exception {

        String executionId = "exec-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // Create mocks
        InspectionExecutionMapper executionMapper = mock(InspectionExecutionMapper.class);
        StepResultMapper stepResultMapper = mock(StepResultMapper.class);
        EvaluationService evaluationService = mock(EvaluationService.class);

        // Setup execution mock
        InspectionExecution execution = createExecution(executionId);
        when(executionMapper.selectById(executionId)).thenReturn(execution);

        // Build step results: 1 mandatory completed + N optional pending
        List<StepResult> stepResults = new ArrayList<>();
        stepResults.add(createStepResult(executionId, 1, true, "completed"));
        for (int i = 0; i < optionalPendingCount; i++) {
            stepResults.add(createStepResult(executionId, i + 2, false, "pending"));
        }

        when(stepResultMapper.selectList(any(QueryWrapper.class))).thenReturn(stepResults);
        when(evaluationService.evaluateExecution(any())).thenReturn("pass");
        when(executionMapper.updateById(any(InspectionExecution.class))).thenReturn(1);

        // Create service with mocks
        InspectionExecutionServiceImpl service =
                createServiceWithMocks(executionMapper, stepResultMapper, evaluationService);

        // Act: submission should succeed despite optional steps being pending
        service.submitExecution(executionId);

        // Assert: execution was updated to pending_approval
        verify(executionMapper).updateById((InspectionExecution) argThat(exec -> {
            InspectionExecution updated = (InspectionExecution) exec;
            return "pending_approval".equals(updated.getStatus());
        }));
    }
}
