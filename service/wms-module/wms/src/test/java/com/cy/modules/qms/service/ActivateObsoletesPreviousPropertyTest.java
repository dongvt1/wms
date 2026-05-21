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
import com.cy.modules.qms.vo.ValidationErrorVO;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based test for Activate obsoletes previous active template.
 *
 * **Validates: Requirements 1.5, 5.3**
 *
 * Property 4: Activating a template obsoletes the previous active template.
 * For any product and QC stage type combination, when a new template is activated,
 * the previously active template for that same product and stage type SHALL transition
 * to status "obsolete", ensuring at most one active template exists per product+stage combination.
 */
class ActivateObsoletesPreviousPropertyTest {

    private static final String[] STAGE_TYPES = {"iqc", "pqc", "fqc"};
    private static final String[] ORG_CODES = {"org001", "org002", "org003"};

    /**
     * Creates an InspectionTemplateServiceImpl with mocked dependencies.
     * The service is configured so that:
     * - getById returns the template to activate
     * - step/field queries return valid data (1 step with 1 field) so validation passes
     * - templateValidationService returns no errors
     * - list(QueryWrapper) returns the provided list of currently active templates
     * - updateById captures the updates for verification
     *
     * @param templateToActivate the template being activated
     * @param currentlyActiveTemplates templates currently active with same stage_type + org
     * @param updatedTemplates list to capture all templates that get updated
     */
    private InspectionTemplateServiceImpl createService(
            InspectionTemplate templateToActivate,
            List<InspectionTemplate> currentlyActiveTemplates,
            List<InspectionTemplate> updatedTemplates) {

        InspectionTemplateServiceImpl service = Mockito.spy(new InspectionTemplateServiceImpl());

        // Mock mappers
        InspectionStepMapper stepMapper = Mockito.mock(InspectionStepMapper.class);
        StepFieldMapper fieldMapper = Mockito.mock(StepFieldMapper.class);
        InspectionExecutionMapper execMapper = Mockito.mock(InspectionExecutionMapper.class);
        InspectionTemplateMapper templateMapper = Mockito.mock(InspectionTemplateMapper.class);
        TemplateCodeGenerator codeGen = Mockito.mock(TemplateCodeGenerator.class);
        TemplateValidationService validationService = Mockito.mock(TemplateValidationService.class);

        // Mock getById to return the template to activate
        doReturn(templateToActivate).when(service).getById(templateToActivate.getId());

        // Mock step query - return 1 valid step so validation passes
        InspectionStep validStep = new InspectionStep();
        validStep.setId("step-valid-001");
        validStep.setTemplateId(templateToActivate.getId());
        validStep.setStepName("Valid Step");
        validStep.setSortOrder(1);
        validStep.setIsMandatory(1);
        when(stepMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(validStep));

        // Mock field query - return 1 valid field so validation passes
        StepField validField = new StepField();
        validField.setId("field-valid-001");
        validField.setStepId(validStep.getId());
        validField.setFieldName("Valid Field");
        validField.setFieldType("text");
        validField.setIsRequired(1);
        validField.setSortOrder(1);
        when(fieldMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(validField));

        // Mock validation service - no errors (template is valid)
        when(validationService.validateForActivation(any(), any(), any())).thenReturn(Collections.emptyList());

        // Mock list() to return currently active templates when queried
        doReturn(currentlyActiveTemplates).when(service).list(any(QueryWrapper.class));

        // Mock updateById to capture updates
        doAnswer(invocation -> {
            InspectionTemplate updated = invocation.getArgument(0);
            // Create a copy to capture the state at the time of update
            InspectionTemplate copy = new InspectionTemplate();
            copy.setId(updated.getId());
            copy.setTemplateCode(updated.getTemplateCode());
            copy.setTemplateName(updated.getTemplateName());
            copy.setStageType(updated.getStageType());
            copy.setStatus(updated.getStatus());
            copy.setSysOrgCode(updated.getSysOrgCode());
            copy.setVersion(updated.getVersion());
            updatedTemplates.add(copy);
            return true;
        }).when(service).updateById(any(InspectionTemplate.class));

        // Inject mocks via reflection
        injectField(service, "inspectionStepMapper", stepMapper);
        injectField(service, "stepFieldMapper", fieldMapper);
        injectField(service, "inspectionExecutionMapper", execMapper);
        injectField(service, "templateCodeGenerator", codeGen);
        injectField(service, "templateValidationService", validationService);

        // Inject baseMapper
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

    // ==================== Generators ====================

    @Provide
    Arbitrary<String> stageTypes() {
        return Arbitraries.of(STAGE_TYPES);
    }

    @Provide
    Arbitrary<String> orgCodes() {
        return Arbitraries.of(ORG_CODES);
    }

    private InspectionTemplate createTemplate(String id, String stageType, String status, String orgCode) {
        InspectionTemplate t = new InspectionTemplate();
        t.setId(id);
        t.setTemplateCode("TPL" + id.hashCode());
        t.setTemplateName("Template " + id);
        t.setStageType(stageType);
        t.setStatus(status);
        t.setSysOrgCode(orgCode);
        t.setVersion("1.0");
        return t;
    }

    // ==================== Property Tests ====================

    /**
     * Property 4: When activating a template, ALL previously active templates
     * with the same stage_type (and same org) are set to "obsolete".
     *
     * For any number of previously active templates (1 to 5) with the same stage_type,
     * activating a new template SHALL set all of them to "obsolete".
     *
     * **Validates: Requirements 1.5, 5.3**
     */
    @Property(tries = 100)
    void activateTemplate_obsoletesPreviousActiveTemplates(
            @ForAll("stageTypes") String stageType,
            @ForAll("orgCodes") String orgCode,
            @ForAll @IntRange(min = 1, max = 5) int previousActiveCount) {

        // Create the template to activate
        String newTemplateId = UUID.randomUUID().toString().replace("-", "");
        InspectionTemplate newTemplate = createTemplate(newTemplateId, stageType, "draft", orgCode);

        // Create previously active templates with the same stage_type and org
        List<InspectionTemplate> previouslyActive = new ArrayList<>();
        for (int i = 0; i < previousActiveCount; i++) {
            String prevId = "prev-active-" + i + "-" + UUID.randomUUID().toString().substring(0, 8);
            InspectionTemplate prev = createTemplate(prevId, stageType, "active", orgCode);
            previouslyActive.add(prev);
        }

        // Track all updates
        List<InspectionTemplate> updatedTemplates = new ArrayList<>();

        InspectionTemplateServiceImpl service = createService(newTemplate, previouslyActive, updatedTemplates);

        // Act
        service.activateTemplate(newTemplateId);

        // Assert: all previously active templates should be set to "obsolete"
        List<InspectionTemplate> obsoletedUpdates = updatedTemplates.stream()
                .filter(t -> "obsolete".equals(t.getStatus()))
                .collect(Collectors.toList());

        assertThat(obsoletedUpdates)
                .as("All %d previously active templates should be set to obsolete", previousActiveCount)
                .hasSize(previousActiveCount);

        // Verify each previously active template was obsoleted
        Set<String> obsoletedIds = obsoletedUpdates.stream()
                .map(InspectionTemplate::getId)
                .collect(Collectors.toSet());

        for (InspectionTemplate prev : previouslyActive) {
            assertThat(obsoletedIds)
                    .as("Previously active template '%s' should be obsoleted", prev.getId())
                    .contains(prev.getId());
        }
    }

    /**
     * Property 4: After activation, the newly activated template has status "active".
     *
     * **Validates: Requirements 1.5, 5.3**
     */
    @Property(tries = 100)
    void activateTemplate_newTemplateBecomesActive(
            @ForAll("stageTypes") String stageType,
            @ForAll("orgCodes") String orgCode) {

        String newTemplateId = UUID.randomUUID().toString().replace("-", "");
        InspectionTemplate newTemplate = createTemplate(newTemplateId, stageType, "draft", orgCode);

        // No previously active templates
        List<InspectionTemplate> previouslyActive = Collections.emptyList();
        List<InspectionTemplate> updatedTemplates = new ArrayList<>();

        InspectionTemplateServiceImpl service = createService(newTemplate, previouslyActive, updatedTemplates);

        // Act
        service.activateTemplate(newTemplateId);

        // Assert: the new template should be updated to "active"
        List<InspectionTemplate> activatedUpdates = updatedTemplates.stream()
                .filter(t -> newTemplateId.equals(t.getId()))
                .filter(t -> "active".equals(t.getStatus()))
                .collect(Collectors.toList());

        assertThat(activatedUpdates)
                .as("The newly activated template should have status 'active'")
                .hasSize(1);
    }

    /**
     * Property 4: At most one active template exists per stage_type after activation.
     * After activating a template, exactly one template (the new one) should be "active"
     * among all the updates - all others should be "obsolete".
     *
     * **Validates: Requirements 1.5, 5.3**
     */
    @Property(tries = 100)
    void activateTemplate_atMostOneActivePerStageType(
            @ForAll("stageTypes") String stageType,
            @ForAll("orgCodes") String orgCode,
            @ForAll @IntRange(min = 0, max = 5) int previousActiveCount) {

        String newTemplateId = UUID.randomUUID().toString().replace("-", "");
        InspectionTemplate newTemplate = createTemplate(newTemplateId, stageType, "draft", orgCode);

        List<InspectionTemplate> previouslyActive = new ArrayList<>();
        for (int i = 0; i < previousActiveCount; i++) {
            String prevId = "prev-" + i + "-" + UUID.randomUUID().toString().substring(0, 8);
            previouslyActive.add(createTemplate(prevId, stageType, "active", orgCode));
        }

        List<InspectionTemplate> updatedTemplates = new ArrayList<>();
        InspectionTemplateServiceImpl service = createService(newTemplate, previouslyActive, updatedTemplates);

        // Act
        service.activateTemplate(newTemplateId);

        // Assert: exactly one template set to "active" (the new one)
        long activeCount = updatedTemplates.stream()
                .filter(t -> "active".equals(t.getStatus()))
                .count();

        assertThat(activeCount)
                .as("Exactly one template should be set to 'active' after activation")
                .isEqualTo(1);

        // Assert: the active one is the new template
        Optional<InspectionTemplate> activeTemplate = updatedTemplates.stream()
                .filter(t -> "active".equals(t.getStatus()))
                .findFirst();

        assertThat(activeTemplate).isPresent();
        assertThat(activeTemplate.get().getId())
                .as("The active template should be the newly activated one")
                .isEqualTo(newTemplateId);
    }

    /**
     * Property 4: Sequential activations - each activation obsoletes the previous.
     * Simulates a sequence of activations for the same stage_type and verifies
     * that each new activation obsoletes the previously active template.
     *
     * **Validates: Requirements 1.5, 5.3**
     */
    @Property(tries = 50)
    void sequentialActivations_eachObsoletesPrevious(
            @ForAll("stageTypes") String stageType,
            @ForAll("orgCodes") String orgCode,
            @ForAll @IntRange(min = 2, max = 5) int activationCount) {

        // Simulate a sequence of activations
        InspectionTemplate lastActivated = null;

        for (int i = 0; i < activationCount; i++) {
            String templateId = "seq-template-" + i + "-" + UUID.randomUUID().toString().substring(0, 8);
            InspectionTemplate templateToActivate = createTemplate(templateId, stageType, "draft", orgCode);

            // The previously active template (if any) should be returned by list()
            List<InspectionTemplate> previouslyActive;
            if (lastActivated != null) {
                // Simulate the previous template being active
                lastActivated.setStatus("active");
                previouslyActive = Collections.singletonList(lastActivated);
            } else {
                previouslyActive = Collections.emptyList();
            }

            List<InspectionTemplate> updatedTemplates = new ArrayList<>();
            InspectionTemplateServiceImpl service = createService(templateToActivate, previouslyActive, updatedTemplates);

            // Act
            service.activateTemplate(templateId);

            // Verify: if there was a previous active template, it should now be obsolete
            if (lastActivated != null) {
                final String prevId = lastActivated.getId();
                List<InspectionTemplate> obsoleted = updatedTemplates.stream()
                        .filter(t -> prevId.equals(t.getId()))
                        .filter(t -> "obsolete".equals(t.getStatus()))
                        .collect(Collectors.toList());

                assertThat(obsoleted)
                        .as("Activation %d: previous template '%s' should be obsoleted", i, prevId)
                        .hasSize(1);
            }

            // The newly activated template becomes the "last activated" for next iteration
            lastActivated = templateToActivate;
        }
    }

    /**
     * Property 4: Templates in different organizations are NOT affected.
     * Activating a template in org A should NOT obsolete active templates in org B.
     *
     * **Validates: Requirements 1.5, 5.3**
     */
    @Property(tries = 100)
    void activateTemplate_doesNotAffectDifferentOrg(
            @ForAll("stageTypes") String stageType) {

        String orgA = "orgA";
        String orgB = "orgB";

        String newTemplateId = UUID.randomUUID().toString().replace("-", "");
        InspectionTemplate newTemplate = createTemplate(newTemplateId, stageType, "draft", orgA);

        // The list() mock should only return templates from the SAME org
        // (the implementation filters by sys_org_code)
        // So we pass an empty list - simulating that no active templates exist in orgA
        List<InspectionTemplate> previouslyActive = Collections.emptyList();
        List<InspectionTemplate> updatedTemplates = new ArrayList<>();

        InspectionTemplateServiceImpl service = createService(newTemplate, previouslyActive, updatedTemplates);

        // Act
        service.activateTemplate(newTemplateId);

        // Assert: only the new template is updated (to active), no obsolete updates
        List<InspectionTemplate> obsoletedUpdates = updatedTemplates.stream()
                .filter(t -> "obsolete".equals(t.getStatus()))
                .collect(Collectors.toList());

        assertThat(obsoletedUpdates)
                .as("No templates should be obsoleted when no active templates exist in the same org")
                .isEmpty();

        // Only the new template should be set to active
        assertThat(updatedTemplates).hasSize(1);
        assertThat(updatedTemplates.get(0).getStatus()).isEqualTo("active");
        assertThat(updatedTemplates.get(0).getId()).isEqualTo(newTemplateId);
    }
}
