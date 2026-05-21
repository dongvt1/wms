package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.qms.entity.InspectionStep;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.StepField;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.mapper.InspectionStepMapper;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.mapper.StepFieldMapper;
import com.cy.modules.qms.service.impl.InspectionTemplateServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Sort Order Invariant after reordering and deletion.
 *
 * **Validates: Requirements 2.2, 2.4**
 *
 * Property 6: Sort order invariant after reordering.
 * For any sequence of add, remove, or reorder operations on Inspection Steps within a template,
 * the resulting sort_order values SHALL be unique within the template AND form a contiguous
 * sequence starting from 1.
 */
class SortOrderInvariantPropertyTest {

    private static final String TEMPLATE_ID = "template-001";

    /**
     * In-memory store simulating the database for steps within a single template.
     */
    static class InMemoryStepStore {
        private final Map<String, InspectionStep> steps = new LinkedHashMap<>();

        void addStep(InspectionStep step) {
            steps.put(step.getId(), step);
        }

        void removeStep(String stepId) {
            steps.remove(stepId);
        }

        void updateStep(InspectionStep step) {
            InspectionStep existing = steps.get(step.getId());
            if (existing != null && step.getSortOrder() != null) {
                existing.setSortOrder(step.getSortOrder());
            }
        }

        InspectionStep getById(String id) {
            return steps.get(id);
        }

        List<InspectionStep> getByTemplateId(String templateId) {
            return steps.values().stream()
                    .filter(s -> templateId.equals(s.getTemplateId()))
                    .collect(Collectors.toList());
        }

        List<InspectionStep> getByTemplateIdOrderBySortOrder(String templateId) {
            return steps.values().stream()
                    .filter(s -> templateId.equals(s.getTemplateId()))
                    .sorted(Comparator.comparingInt(InspectionStep::getSortOrder))
                    .collect(Collectors.toList());
        }

        List<String> getAllStepIds() {
            return new ArrayList<>(steps.keySet());
        }

        int size() {
            return steps.size();
        }
    }

    /**
     * Creates an InspectionTemplateServiceImpl with mocked mappers backed by an in-memory store.
     */
    private InspectionTemplateServiceImpl createServiceWithStore(InMemoryStepStore store) {
        InspectionTemplateServiceImpl service = new InspectionTemplateServiceImpl();

        InspectionStepMapper stepMapper = Mockito.mock(InspectionStepMapper.class);
        StepFieldMapper fieldMapper = Mockito.mock(StepFieldMapper.class);
        InspectionTemplateMapper templateMapper = Mockito.mock(InspectionTemplateMapper.class);
        InspectionExecutionMapper executionMapper = Mockito.mock(InspectionExecutionMapper.class);

        // Mock template lookup (getById uses baseMapper.selectById)
        InspectionTemplate template = new InspectionTemplate();
        template.setId(TEMPLATE_ID);
        template.setTemplateName("Test Template");
        when(templateMapper.selectById(TEMPLATE_ID)).thenReturn(template);

        // Mock stepMapper.selectList - returns steps for the template
        when(stepMapper.selectList(any(QueryWrapper.class))).thenAnswer(invocation -> {
            // Return steps ordered by sort_order for deleteStep's re-numbering query
            return store.getByTemplateIdOrderBySortOrder(TEMPLATE_ID);
        });

        // Mock stepMapper.selectById - returns a specific step
        when(stepMapper.selectById(any(String.class))).thenAnswer(invocation -> {
            String stepId = invocation.getArgument(0);
            return store.getById(stepId);
        });

        // Mock stepMapper.updateById - updates sort_order in store
        when(stepMapper.updateById(any(InspectionStep.class))).thenAnswer(invocation -> {
            InspectionStep step = invocation.getArgument(0);
            store.updateStep(step);
            return 1;
        });

        // Mock stepMapper.deleteById - removes step from store
        when(stepMapper.deleteById(any(String.class))).thenAnswer(invocation -> {
            String stepId = invocation.getArgument(0);
            store.removeStep(stepId);
            return 1;
        });

        // Mock fieldMapper.delete - no-op for this test (we focus on sort_order)
        when(fieldMapper.delete(any(QueryWrapper.class))).thenReturn(0);

        // Inject mocks via reflection
        injectField(service, "inspectionStepMapper", stepMapper);
        injectField(service, "stepFieldMapper", fieldMapper);
        injectField(service, "inspectionExecutionMapper", executionMapper);
        injectBaseMapper(service, templateMapper);

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

    private void injectBaseMapper(InspectionTemplateServiceImpl service, InspectionTemplateMapper mapper) {
        try {
            Field baseMapperField = service.getClass().getSuperclass().getSuperclass().getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            baseMapperField.set(service, mapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject baseMapper", e);
        }
    }

    /**
     * Creates an initial set of steps with contiguous sort_order starting from 1.
     */
    private InMemoryStepStore createInitialStore(int stepCount) {
        InMemoryStepStore store = new InMemoryStepStore();
        for (int i = 1; i <= stepCount; i++) {
            InspectionStep step = new InspectionStep();
            step.setId("step-" + i);
            step.setTemplateId(TEMPLATE_ID);
            step.setStepName("Step " + i);
            step.setSortOrder(i);
            step.setIsMandatory(1);
            step.setRequiresApproval(0);
            store.addStep(step);
        }
        return store;
    }

    /**
     * Verifies the sort_order invariant: values are unique and form a contiguous sequence from 1.
     */
    private void assertSortOrderInvariant(InMemoryStepStore store) {
        List<InspectionStep> steps = store.getByTemplateIdOrderBySortOrder(TEMPLATE_ID);
        if (steps.isEmpty()) {
            return; // Empty is valid (no steps)
        }

        List<Integer> sortOrders = steps.stream()
                .map(InspectionStep::getSortOrder)
                .collect(Collectors.toList());

        // Check uniqueness
        Set<Integer> uniqueOrders = new HashSet<>(sortOrders);
        assertThat(uniqueOrders).as("Sort orders must be unique").hasSameSizeAs(sortOrders);

        // Check contiguous from 1
        for (int i = 0; i < sortOrders.size(); i++) {
            assertThat(sortOrders.get(i))
                    .as("Sort order at position %d should be %d", i, i + 1)
                    .isEqualTo(i + 1);
        }
    }

    // ==================== Property Tests ====================

    /**
     * Property 6: After reordering steps with a random permutation, sort_order values
     * are unique and contiguous starting from 1.
     *
     * **Validates: Requirements 2.2, 2.4**
     */
    @Property(tries = 100)
    void sortOrderContiguousAfterReorder(
            @ForAll @IntRange(min = 2, max = 10) int stepCount,
            @ForAll("randomPermutationSeed") long seed) {

        InMemoryStepStore store = createInitialStore(stepCount);
        InspectionTemplateServiceImpl service = createServiceWithStore(store);

        // Generate a random permutation of step IDs
        List<String> stepIds = store.getAllStepIds();
        Collections.shuffle(stepIds, new Random(seed));

        // Execute reorder
        service.reorderSteps(TEMPLATE_ID, stepIds);

        // Verify invariant
        assertSortOrderInvariant(store);
    }

    /**
     * Property 6: After deleting a step, remaining sort_order values are unique
     * and contiguous starting from 1.
     *
     * **Validates: Requirements 2.2, 2.4**
     */
    @Property(tries = 100)
    void sortOrderContiguousAfterDelete(
            @ForAll @IntRange(min = 2, max = 10) int stepCount,
            @ForAll @IntRange(min = 0, max = 9) int deleteIndex) {

        // Ensure deleteIndex is within bounds
        int actualDeleteIndex = deleteIndex % stepCount;

        InMemoryStepStore store = createInitialStore(stepCount);
        InspectionTemplateServiceImpl service = createServiceWithStore(store);

        // Pick a step to delete
        List<String> stepIds = store.getAllStepIds();
        String stepToDelete = stepIds.get(actualDeleteIndex);

        // Execute delete
        service.deleteStep(stepToDelete);

        // Verify invariant: remaining steps have contiguous sort_order from 1
        assertSortOrderInvariant(store);

        // Also verify the count decreased by 1
        assertThat(store.size()).isEqualTo(stepCount - 1);
    }

    /**
     * Property 6: After a sequence of reorder then delete operations, sort_order
     * values remain unique and contiguous starting from 1.
     *
     * **Validates: Requirements 2.2, 2.4**
     */
    @Property(tries = 50)
    void sortOrderContiguousAfterReorderThenDelete(
            @ForAll @IntRange(min = 3, max = 8) int stepCount,
            @ForAll("randomPermutationSeed") long seed,
            @ForAll @IntRange(min = 0, max = 7) int deleteIndex) {

        int actualDeleteIndex = deleteIndex % stepCount;

        InMemoryStepStore store = createInitialStore(stepCount);
        InspectionTemplateServiceImpl service = createServiceWithStore(store);

        // Step 1: Reorder with random permutation
        List<String> stepIds = store.getAllStepIds();
        Collections.shuffle(stepIds, new Random(seed));
        service.reorderSteps(TEMPLATE_ID, stepIds);

        // Verify invariant after reorder
        assertSortOrderInvariant(store);

        // Step 2: Delete a step
        List<String> currentStepIds = store.getAllStepIds();
        String stepToDelete = currentStepIds.get(actualDeleteIndex);
        service.deleteStep(stepToDelete);

        // Verify invariant after delete
        assertSortOrderInvariant(store);
        assertThat(store.size()).isEqualTo(stepCount - 1);
    }

    /**
     * Property 6: After multiple sequential delete operations, sort_order values
     * remain unique and contiguous starting from 1.
     *
     * **Validates: Requirements 2.2, 2.4**
     */
    @Property(tries = 50)
    void sortOrderContiguousAfterMultipleDeletes(
            @ForAll @IntRange(min = 4, max = 10) int stepCount,
            @ForAll @IntRange(min = 1, max = 3) int deleteCount,
            @ForAll("randomPermutationSeed") long seed) {

        int actualDeleteCount = Math.min(deleteCount, stepCount - 1); // Keep at least 1 step

        InMemoryStepStore store = createInitialStore(stepCount);
        InspectionTemplateServiceImpl service = createServiceWithStore(store);

        Random random = new Random(seed);

        // Perform multiple deletes
        for (int i = 0; i < actualDeleteCount; i++) {
            List<String> currentStepIds = store.getAllStepIds();
            if (currentStepIds.isEmpty()) break;

            int indexToDelete = random.nextInt(currentStepIds.size());
            String stepToDelete = currentStepIds.get(indexToDelete);
            service.deleteStep(stepToDelete);

            // Verify invariant after each delete
            assertSortOrderInvariant(store);
        }

        // Final verification
        assertThat(store.size()).isEqualTo(stepCount - actualDeleteCount);
    }

    /**
     * Property 6: After a mixed sequence of reorder and delete operations,
     * sort_order values remain unique and contiguous starting from 1.
     *
     * **Validates: Requirements 2.2, 2.4**
     */
    @Property(tries = 50)
    void sortOrderContiguousAfterMixedOperations(
            @ForAll @IntRange(min = 4, max = 8) int stepCount,
            @ForAll @IntRange(min = 2, max = 5) int operationCount,
            @ForAll("randomPermutationSeed") long seed) {

        InMemoryStepStore store = createInitialStore(stepCount);
        InspectionTemplateServiceImpl service = createServiceWithStore(store);

        Random random = new Random(seed);
        int deletesPerformed = 0;

        for (int op = 0; op < operationCount; op++) {
            List<String> currentStepIds = store.getAllStepIds();
            if (currentStepIds.size() <= 1) break; // Need at least 1 step for reorder, 2 for delete

            // Randomly choose: reorder (0) or delete (1)
            boolean doDelete = random.nextBoolean() && currentStepIds.size() > 1;

            if (doDelete) {
                // Delete a random step
                int indexToDelete = random.nextInt(currentStepIds.size());
                String stepToDelete = currentStepIds.get(indexToDelete);
                service.deleteStep(stepToDelete);
                deletesPerformed++;
            } else {
                // Reorder with random permutation
                List<String> shuffled = new ArrayList<>(currentStepIds);
                Collections.shuffle(shuffled, random);
                service.reorderSteps(TEMPLATE_ID, shuffled);
            }

            // Verify invariant after each operation
            assertSortOrderInvariant(store);
        }

        // Final size check
        assertThat(store.size()).isEqualTo(stepCount - deletesPerformed);
    }

    // ==================== Providers ====================

    @Provide
    Arbitrary<Long> randomPermutationSeed() {
        return Arbitraries.longs().between(0, Long.MAX_VALUE);
    }
}
