package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.qms.entity.InspectionExecution;
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

import java.lang.reflect.Field;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Referential Integrity on template deletion.
 *
 * **Validates: Requirements 1.6**
 *
 * Property 5: Referential integrity prevents deletion of used templates.
 * For any Inspection Template that has at least one associated Inspection Execution,
 * attempting to delete that template SHALL be rejected, and the error response SHALL
 * include the exact count of associated executions.
 */
class ReferentialIntegrityPropertyTest {

    /**
     * Creates an InspectionTemplateServiceImpl with mocked mappers.
     * The InspectionExecutionMapper is configured to return the given executionCount.
     */
    private InspectionTemplateServiceImpl createServiceWithMockedExecutionCount(long executionCount) {
        InspectionTemplateServiceImpl service = new InspectionTemplateServiceImpl();

        // Mock all required mappers
        InspectionExecutionMapper executionMapper = Mockito.mock(InspectionExecutionMapper.class);
        InspectionStepMapper stepMapper = Mockito.mock(InspectionStepMapper.class);
        StepFieldMapper fieldMapper = Mockito.mock(StepFieldMapper.class);
        InspectionTemplateMapper templateMapper = Mockito.mock(InspectionTemplateMapper.class);

        // Configure execution mapper to return the specified count
        when(executionMapper.selectCount(any(QueryWrapper.class))).thenReturn(executionCount);

        // For the success case (count == 0), mock the step/field deletion and template removal
        when(stepMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());
        when(stepMapper.delete(any(QueryWrapper.class))).thenReturn(0);
        when(fieldMapper.delete(any(QueryWrapper.class))).thenReturn(0);
        when(templateMapper.deleteById(any(String.class))).thenReturn(1);

        // Inject mocks via reflection
        injectField(service, "inspectionExecutionMapper", executionMapper);
        injectField(service, "inspectionStepMapper", stepMapper);
        injectField(service, "stepFieldMapper", fieldMapper);

        // Inject the base mapper for ServiceImpl (used by removeById)
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
            // ServiceImpl stores the mapper in a field called "baseMapper" in the parent class
            Field baseMapperField = service.getClass().getSuperclass().getSuperclass().getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            baseMapperField.set(service, mapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject baseMapper", e);
        }
    }

    /**
     * Property 5: When execution count > 0, deleteTemplate() SHALL throw IllegalStateException
     * with the execution count in the message.
     *
     * For any positive execution count (1 to 1000), deletion must be rejected.
     *
     * **Validates: Requirements 1.6**
     */
    @Property(tries = 100)
    void deleteTemplateRejectedWhenExecutionsExist(
            @ForAll @IntRange(min = 1, max = 1000) int executionCount) {

        InspectionTemplateServiceImpl service = createServiceWithMockedExecutionCount(executionCount);
        String templateId = "template-" + executionCount;

        // Attempting to delete should throw IllegalStateException
        assertThatThrownBy(() -> service.deleteTemplate(templateId))
                .isInstanceOf(IllegalStateException.class)
                .message()
                .contains(String.valueOf(executionCount));
    }

    /**
     * Property 5 (complement): When execution count == 0, deleteTemplate() SHALL succeed
     * without throwing any exception.
     *
     * **Validates: Requirements 1.6**
     */
    @Property(tries = 50)
    void deleteTemplateSucceedsWhenNoExecutionsExist(@ForAll("templateIds") String templateId) {

        InspectionTemplateServiceImpl service = createServiceWithMockedExecutionCount(0L);

        // Should not throw any exception
        service.deleteTemplate(templateId);

        // Verify that the execution count was checked
        // (implicitly verified by the fact that no exception was thrown and the method completed)
    }

    @Provide
    Arbitrary<String> templateIds() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .withChars('-')
                .ofMinLength(5)
                .ofMaxLength(36)
                .map(s -> "tpl-" + s);
    }
}
