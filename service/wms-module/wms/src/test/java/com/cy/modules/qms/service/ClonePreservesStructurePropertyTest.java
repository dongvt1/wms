package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.qms.entity.InspectionStep;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.StepField;
import com.cy.modules.qms.mapper.InspectionStepMapper;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.mapper.StepFieldMapper;
import com.cy.modules.qms.service.impl.InspectionTemplateServiceImpl;
import com.cy.modules.qms.vo.InspectionStepVO;
import com.cy.modules.qms.vo.InspectionTemplateVO;
import com.cy.modules.qms.vo.StepFieldVO;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Template Clone preserves structure.
 *
 * **Validates: Requirements 1.7**
 *
 * Property 2: Template clone preserves structure.
 * For any Inspection Template with N steps and M total fields, cloning that template
 * SHALL produce a new template with exactly N steps and M fields, where each step and
 * field has identical configuration data (name, type, config, sort_order) but different IDs.
 */
class ClonePreservesStructurePropertyTest {

    private static final String[] FIELD_TYPES = {"text", "number", "boolean", "select", "measurement"};
    private static final String[] STAGE_TYPES = {"iqc", "pqc", "fqc"};

    /**
     * Property 2: Cloning a template with N steps and M fields produces a new template
     * with exactly N steps and M fields, identical configuration but different IDs.
     *
     * **Validates: Requirements 1.7**
     */
    @Property(tries = 100)
    void clonePreservesStructure(
            @ForAll("templates") TemplateTestData testData) {

        // Setup mocks
        InspectionTemplateServiceImpl service = createServiceWithMockedData(testData);

        // Execute clone
        InspectionTemplateVO result = service.cloneTemplate(testData.sourceTemplate.getId());

        // Verify: same number of steps
        assertThat(result.getSteps()).hasSize(testData.sourceSteps.size());

        // Verify: total field count matches
        int expectedTotalFields = testData.fieldsByStep.stream()
                .mapToInt(List::size)
                .sum();
        int actualTotalFields = result.getSteps().stream()
                .mapToInt(s -> s.getFields() != null ? s.getFields().size() : 0)
                .sum();
        assertThat(actualTotalFields).isEqualTo(expectedTotalFields);

        // Verify: template has different ID from source
        assertThat(result.getId()).isNotEqualTo(testData.sourceTemplate.getId());

        // Verify: template status is "draft"
        assertThat(result.getStatus()).isEqualTo("draft");

        // Verify: template name has " (Copy)" suffix
        assertThat(result.getTemplateName())
                .isEqualTo(testData.sourceTemplate.getTemplateName() + " (Copy)");

        // Verify: same stageType
        assertThat(result.getStageType()).isEqualTo(testData.sourceTemplate.getStageType());

        // Verify each step: identical config data but different IDs
        for (int i = 0; i < testData.sourceSteps.size(); i++) {
            InspectionStep sourceStep = testData.sourceSteps.get(i);
            InspectionStepVO clonedStep = result.getSteps().get(i);

            // Different ID
            assertThat(clonedStep.getId()).isNotEqualTo(sourceStep.getId());

            // Same configuration data
            assertThat(clonedStep.getStepName()).isEqualTo(sourceStep.getStepName());
            assertThat(clonedStep.getSortOrder()).isEqualTo(sourceStep.getSortOrder());
            assertThat(clonedStep.getIsMandatory()).isEqualTo(sourceStep.getIsMandatory());
            assertThat(clonedStep.getRequiresApproval()).isEqualTo(sourceStep.getRequiresApproval());

            // Verify fields within this step
            List<StepField> sourceFields = testData.fieldsByStep.get(i);
            List<StepFieldVO> clonedFields = clonedStep.getFields();

            assertThat(clonedFields).hasSize(sourceFields.size());

            for (int j = 0; j < sourceFields.size(); j++) {
                StepField sourceField = sourceFields.get(j);
                StepFieldVO clonedField = clonedFields.get(j);

                // Different ID
                assertThat(clonedField.getId()).isNotEqualTo(sourceField.getId());

                // Same configuration data
                assertThat(clonedField.getFieldName()).isEqualTo(sourceField.getFieldName());
                assertThat(clonedField.getFieldCode()).isEqualTo(sourceField.getFieldCode());
                assertThat(clonedField.getFieldType()).isEqualTo(sourceField.getFieldType());
                assertThat(clonedField.getUnit()).isEqualTo(sourceField.getUnit());
                assertThat(clonedField.getSortOrder()).isEqualTo(sourceField.getSortOrder());
                assertThat(clonedField.getIsRequired()).isEqualTo(sourceField.getIsRequired());
            }
        }
    }

    // ==================== Arbitrary Providers ====================

    @Provide
    Arbitrary<TemplateTestData> templates() {
        return Combinators.combine(
                Arbitraries.integers().between(1, 10),   // number of steps
                Arbitraries.integers().between(1, 5)     // fields per step
        ).flatAs((numSteps, fieldsPerStep) -> {
            return Combinators.combine(
                    templateArbitrary(),
                    stepsArbitrary(numSteps),
                    fieldsArbitrary(numSteps, fieldsPerStep)
            ).as((template, steps, fieldsByStep) -> {
                // Wire up template_id references
                for (InspectionStep step : steps) {
                    step.setTemplateId(template.getId());
                }
                return new TemplateTestData(template, steps, fieldsByStep);
            });
        });
    }

    private Arbitrary<InspectionTemplate> templateArbitrary() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(30),
                Arbitraries.of(STAGE_TYPES),
                Arbitraries.of("1.0", "2.0", "3.0", "1.5", "2.1")
        ).as((name, stageType, version) -> {
            InspectionTemplate t = new InspectionTemplate();
            t.setId(UUID.randomUUID().toString().replace("-", ""));
            t.setTemplateCode("TPL20260315001");
            t.setTemplateName(name);
            t.setDescription("Test template description");
            t.setStageType(stageType);
            t.setVersion(version);
            t.setStatus("active");
            t.setNotes("Some notes");
            return t;
        });
    }

    private Arbitrary<List<InspectionStep>> stepsArbitrary(int numSteps) {
        return Arbitraries.just(numSteps).map(n -> {
            List<InspectionStep> steps = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                InspectionStep step = new InspectionStep();
                step.setId(UUID.randomUUID().toString().replace("-", ""));
                step.setStepName("Step " + (i + 1));
                step.setDescription("Description for step " + (i + 1));
                step.setSortOrder(i + 1);
                step.setIsMandatory(i % 2 == 0 ? 1 : 0);
                step.setRequiresApproval(i % 3 == 0 ? 1 : 0);
                steps.add(step);
            }
            return steps;
        });
    }

    private Arbitrary<List<List<StepField>>> fieldsArbitrary(int numSteps, int fieldsPerStep) {
        return Combinators.combine(
                Arbitraries.just(numSteps),
                Arbitraries.just(fieldsPerStep)
        ).as((ns, fps) -> {
            List<List<StepField>> fieldsByStep = new ArrayList<>();
            for (int i = 0; i < ns; i++) {
                List<StepField> fields = new ArrayList<>();
                String stepId = "placeholder"; // will be wired later
                for (int j = 0; j < fps; j++) {
                    StepField field = new StepField();
                    field.setId(UUID.randomUUID().toString().replace("-", ""));
                    field.setStepId(stepId);
                    field.setFieldName("Field " + (j + 1));
                    field.setFieldCode("field_" + (j + 1));
                    field.setFieldType(FIELD_TYPES[j % FIELD_TYPES.length]);
                    field.setUnit(j % 2 == 0 ? "mm" : null);
                    field.setDefaultValue(null);
                    field.setIsRequired(j % 2 == 0 ? 1 : 0);
                    field.setSortOrder(j + 1);
                    field.setFieldConfig(generateFieldConfig(FIELD_TYPES[j % FIELD_TYPES.length]));
                    field.setHint("Hint for field " + (j + 1));
                    fields.add(field);
                }
                fieldsByStep.add(fields);
            }
            return fieldsByStep;
        });
    }

    private String generateFieldConfig(String fieldType) {
        switch (fieldType) {
            case "number":
                return "{\"minValue\":0,\"maxValue\":100,\"decimalPlaces\":2}";
            case "measurement":
                return "{\"nominalValue\":5.0,\"upperTolerance\":5.5,\"lowerTolerance\":4.5}";
            case "select":
                return "{\"options\":[\"Good\",\"Average\",\"Poor\"]}";
            case "boolean":
                return "{\"trueLabel\":\"Pass\",\"falseLabel\":\"Fail\"}";
            case "text":
                return "{\"maxLength\":500,\"multiline\":false}";
            default:
                return null;
        }
    }

    // ==================== Service Setup ====================

    private InspectionTemplateServiceImpl createServiceWithMockedData(TemplateTestData testData) {
        InspectionTemplateServiceImpl service = new InspectionTemplateServiceImpl();

        // Mock mappers
        InspectionTemplateMapper templateMapper = Mockito.mock(InspectionTemplateMapper.class);
        InspectionStepMapper stepMapper = Mockito.mock(InspectionStepMapper.class);
        StepFieldMapper fieldMapper = Mockito.mock(StepFieldMapper.class);
        TemplateCodeGenerator codeGenerator = Mockito.mock(TemplateCodeGenerator.class);

        // Mock getById (via baseMapper.selectById)
        when(templateMapper.selectById(testData.sourceTemplate.getId()))
                .thenReturn(testData.sourceTemplate);

        // Mock save (via baseMapper.insert) - simulate ID generation
        when(templateMapper.insert(any(InspectionTemplate.class))).thenAnswer(invocation -> {
            InspectionTemplate t = invocation.getArgument(0);
            if (t.getId() == null) {
                t.setId(UUID.randomUUID().toString().replace("-", ""));
            }
            return 1;
        });

        // Mock code generator
        when(codeGenerator.generateCode()).thenReturn("TPL" + System.currentTimeMillis());

        // Mock step mapper - return source steps when queried by template_id
        when(stepMapper.selectList(any(QueryWrapper.class))).thenAnswer(invocation -> {
            // First call returns source steps (loading source template steps)
            return testData.sourceSteps;
        });

        // Mock step insert
        when(stepMapper.insert(any(InspectionStep.class))).thenReturn(1);

        // Mock field mapper - return fields for each step
        AtomicInteger fieldQueryCounter = new AtomicInteger(0);
        when(fieldMapper.selectList(any(QueryWrapper.class))).thenAnswer(invocation -> {
            int idx = fieldQueryCounter.getAndIncrement();
            if (idx < testData.fieldsByStep.size()) {
                return testData.fieldsByStep.get(idx);
            }
            return Collections.emptyList();
        });

        // Mock field insert
        when(fieldMapper.insert(any(StepField.class))).thenReturn(1);

        // Inject mocks via reflection
        injectField(service, "inspectionStepMapper", stepMapper);
        injectField(service, "stepFieldMapper", fieldMapper);
        injectField(service, "templateCodeGenerator", codeGenerator);
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

    // ==================== Test Data Holder ====================

    static class TemplateTestData {
        final InspectionTemplate sourceTemplate;
        final List<InspectionStep> sourceSteps;
        final List<List<StepField>> fieldsByStep;

        TemplateTestData(InspectionTemplate sourceTemplate,
                         List<InspectionStep> sourceSteps,
                         List<List<StepField>> fieldsByStep) {
            this.sourceTemplate = sourceTemplate;
            this.sourceSteps = sourceSteps;
            // Wire up step IDs in fields
            for (int i = 0; i < sourceSteps.size() && i < fieldsByStep.size(); i++) {
                String stepId = sourceSteps.get(i).getId();
                for (StepField field : fieldsByStep.get(i)) {
                    field.setStepId(stepId);
                }
            }
            this.fieldsByStep = fieldsByStep;
        }
    }
}
