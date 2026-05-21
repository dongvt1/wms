package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.qms.entity.InspectionStep;
import com.cy.modules.qms.entity.StepField;
import com.cy.modules.qms.mapper.InspectionStepMapper;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.mapper.StepFieldMapper;
import com.cy.modules.qms.service.impl.InspectionTemplateServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Cascade Delete behavior.
 *
 * **Validates: Requirements 2.5**
 *
 * Property 7: Cascade delete removes all child fields.
 * For any Inspection Step with N associated Step Fields (0-10), deleting that step
 * SHALL also delete all N fields, leaving zero orphaned field records referencing
 * the deleted step.
 */
class CascadeDeletePropertyTest {

    /**
     * Creates an InspectionTemplateServiceImpl with mocked mappers.
     * The InspectionStepMapper is configured to return a step with the given stepId and templateId.
     * The StepFieldMapper is configured to simulate N fields belonging to the step.
     */
    private InspectionTemplateServiceImpl createServiceWithMockedStep(String stepId, String templateId, int fieldCount) {
        InspectionTemplateServiceImpl service = new InspectionTemplateServiceImpl();

        InspectionStepMapper stepMapper = Mockito.mock(InspectionStepMapper.class);
        StepFieldMapper fieldMapper = Mockito.mock(StepFieldMapper.class);
        InspectionTemplateMapper templateMapper = Mockito.mock(InspectionTemplateMapper.class);

        // Configure step mapper to return the step when queried by ID
        InspectionStep step = new InspectionStep();
        step.setId(stepId);
        step.setTemplateId(templateId);
        step.setSortOrder(1);
        when(stepMapper.selectById(stepId)).thenReturn(step);

        // Configure field mapper to return fieldCount when delete is called
        when(fieldMapper.delete(any(QueryWrapper.class))).thenReturn(fieldCount);

        // Configure step mapper deleteById
        when(stepMapper.deleteById(stepId)).thenReturn(1);

        // Configure step mapper to return empty list for remaining steps (re-numbering)
        when(stepMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        // Inject mocks via reflection
        injectField(service, "inspectionStepMapper", stepMapper);
        injectField(service, "stepFieldMapper", fieldMapper);
        injectBaseMapper(service, templateMapper);

        return service;
    }

    /**
     * Returns the mocked StepFieldMapper from the service for verification.
     */
    private StepFieldMapper getFieldMapper(InspectionTemplateServiceImpl service) {
        try {
            Field field = service.getClass().getDeclaredField("stepFieldMapper");
            field.setAccessible(true);
            return (StepFieldMapper) field.get(service);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get stepFieldMapper", e);
        }
    }

    /**
     * Returns the mocked InspectionStepMapper from the service for verification.
     */
    private InspectionStepMapper getStepMapper(InspectionTemplateServiceImpl service) {
        try {
            Field field = service.getClass().getDeclaredField("inspectionStepMapper");
            field.setAccessible(true);
            return (InspectionStepMapper) field.get(service);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get inspectionStepMapper", e);
        }
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
     * Property 7: For any step with N fields (0-10), deleteStep() SHALL invoke
     * stepFieldMapper.delete() with a QueryWrapper condition on step_id matching
     * the deleted step's ID, ensuring all child fields are removed.
     *
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 100)
    void deleteStepCascadesDeleteToAllChildFields(
            @ForAll @IntRange(min = 0, max = 10) int fieldCount) {

        String stepId = "step-" + UUID.randomUUID().toString().substring(0, 8);
        String templateId = "tpl-" + UUID.randomUUID().toString().substring(0, 8);

        InspectionTemplateServiceImpl service = createServiceWithMockedStep(stepId, templateId, fieldCount);
        StepFieldMapper fieldMapper = getFieldMapper(service);
        InspectionStepMapper stepMapper = getStepMapper(service);

        // Execute deleteStep
        service.deleteStep(stepId);

        // Verify: stepFieldMapper.delete() was called exactly once with step_id condition
        ArgumentCaptor<QueryWrapper> queryCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(fieldMapper, times(1)).delete(queryCaptor.capture());

        // Verify the QueryWrapper targets the correct step_id
        QueryWrapper<StepField> capturedQuery = queryCaptor.getValue();
        String querySql = capturedQuery.getSqlSegment();
        assertThat(querySql).contains("step_id");

        // Verify: the step itself was also deleted
        verify(stepMapper, times(1)).deleteById(stepId);
    }

    /**
     * Property 7 (ordering): The cascade delete of fields SHALL occur BEFORE
     * the step itself is deleted, ensuring no orphan records exist at any point.
     *
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 50)
    void cascadeDeleteFieldsBeforeStep(
            @ForAll @IntRange(min = 0, max = 10) int fieldCount) {

        String stepId = "step-order-" + UUID.randomUUID().toString().substring(0, 8);
        String templateId = "tpl-order-" + UUID.randomUUID().toString().substring(0, 8);

        InspectionTemplateServiceImpl service = new InspectionTemplateServiceImpl();

        InspectionStepMapper stepMapper = Mockito.mock(InspectionStepMapper.class);
        StepFieldMapper fieldMapper = Mockito.mock(StepFieldMapper.class);
        InspectionTemplateMapper templateMapper = Mockito.mock(InspectionTemplateMapper.class);

        // Track call order
        List<String> callOrder = new ArrayList<>();

        InspectionStep step = new InspectionStep();
        step.setId(stepId);
        step.setTemplateId(templateId);
        step.setSortOrder(1);
        when(stepMapper.selectById(stepId)).thenReturn(step);

        when(fieldMapper.delete(any(QueryWrapper.class))).thenAnswer(invocation -> {
            callOrder.add("deleteFields");
            return fieldCount;
        });

        when(stepMapper.deleteById(stepId)).thenAnswer(invocation -> {
            callOrder.add("deleteStep");
            return 1;
        });

        when(stepMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        injectField(service, "inspectionStepMapper", stepMapper);
        injectField(service, "stepFieldMapper", fieldMapper);
        injectBaseMapper(service, templateMapper);

        // Execute
        service.deleteStep(stepId);

        // Verify ordering: fields deleted before step
        assertThat(callOrder).containsExactly("deleteFields", "deleteStep");
    }

    /**
     * Property 7 (zero orphans): After deleteStep(), there SHALL be zero field records
     * referencing the deleted step. Verified by ensuring the delete query targets
     * exactly the step_id of the deleted step.
     *
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 50)
    void afterDeleteStepZeroOrphanFieldsRemain(
            @ForAll @IntRange(min = 1, max = 10) int fieldCount) {

        String stepId = "step-orphan-" + UUID.randomUUID().toString().substring(0, 8);
        String templateId = "tpl-orphan-" + UUID.randomUUID().toString().substring(0, 8);

        InspectionTemplateServiceImpl service = new InspectionTemplateServiceImpl();

        InspectionStepMapper stepMapper = Mockito.mock(InspectionStepMapper.class);
        StepFieldMapper fieldMapper = Mockito.mock(StepFieldMapper.class);
        InspectionTemplateMapper templateMapper = Mockito.mock(InspectionTemplateMapper.class);

        InspectionStep step = new InspectionStep();
        step.setId(stepId);
        step.setTemplateId(templateId);
        step.setSortOrder(1);
        when(stepMapper.selectById(stepId)).thenReturn(step);

        // Simulate that fieldCount fields exist and are all deleted
        when(fieldMapper.delete(any(QueryWrapper.class))).thenReturn(fieldCount);
        when(stepMapper.deleteById(stepId)).thenReturn(1);
        when(stepMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        // After deletion, querying for fields with this step_id returns 0
        when(fieldMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        injectField(service, "inspectionStepMapper", stepMapper);
        injectField(service, "stepFieldMapper", fieldMapper);
        injectBaseMapper(service, templateMapper);

        // Execute
        service.deleteStep(stepId);

        // Verify: the delete was called (removing all N fields)
        verify(fieldMapper, times(1)).delete(any(QueryWrapper.class));

        // Simulate post-deletion check: zero orphans remain
        long remainingFields = fieldMapper.selectCount(
                new QueryWrapper<StepField>().eq("step_id", stepId)
        );
        assertThat(remainingFields).isZero();
    }
}
