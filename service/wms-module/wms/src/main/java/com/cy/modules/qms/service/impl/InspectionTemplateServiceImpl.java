package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.modules.qms.dto.InspectionStepDTO;
import com.cy.modules.qms.dto.InspectionTemplateDTO;
import com.cy.modules.qms.dto.StepFieldDTO;
import com.cy.modules.qms.entity.InspectionExecution;
import com.cy.modules.qms.entity.InspectionStep;
import com.cy.modules.qms.entity.InspectionTemplate;
import com.cy.modules.qms.entity.StepField;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.mapper.InspectionStepMapper;
import com.cy.modules.qms.mapper.InspectionTemplateMapper;
import com.cy.modules.qms.mapper.StepFieldMapper;
import com.cy.modules.qms.exception.TemplateValidationException;
import com.cy.modules.qms.service.InspectionTemplateService;
import com.cy.modules.qms.service.TemplateCodeGenerator;
import com.cy.modules.qms.service.TemplateValidationService;
import com.cy.modules.qms.vo.InspectionStepVO;
import com.cy.modules.qms.vo.InspectionTemplateVO;
import com.cy.modules.qms.vo.StepFieldVO;
import com.cy.modules.qms.vo.ValidationErrorVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation InspectionTemplateService.
 * Quản lý CRUD template kèm steps + fields trong single transaction.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Service
public class InspectionTemplateServiceImpl extends ServiceImpl<InspectionTemplateMapper, InspectionTemplate>
        implements InspectionTemplateService {

    @Autowired
    private InspectionStepMapper inspectionStepMapper;

    @Autowired
    private StepFieldMapper stepFieldMapper;

    @Autowired
    private InspectionExecutionMapper inspectionExecutionMapper;

    @Autowired
    private TemplateCodeGenerator templateCodeGenerator;

    @Autowired
    private TemplateValidationService templateValidationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionTemplateVO saveTemplateWithSteps(InspectionTemplateDTO dto) {
        // 1. Create template entity
        InspectionTemplate template = new InspectionTemplate();
        template.setTemplateCode(templateCodeGenerator.generateCode());
        template.setTemplateName(dto.getTemplateName());
        template.setDescription(dto.getDescription());
        template.setStageType(dto.getStageType());
        template.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0");
        template.setStatus("draft");
        template.setNotes(dto.getNotes());

        this.save(template);

        // 2. Save steps + fields
        List<InspectionStep> savedSteps = new ArrayList<>();
        List<List<StepField>> savedFieldsByStep = new ArrayList<>();

        if (dto.getSteps() != null) {
            int sortOrder = 1;
            for (InspectionStepDTO stepDTO : dto.getSteps()) {
                InspectionStep step = new InspectionStep();
                step.setId(UUID.randomUUID().toString().replace("-", ""));
                step.setTemplateId(template.getId());
                step.setStepName(stepDTO.getStepName());
                step.setDescription(stepDTO.getDescription());
                step.setSortOrder(stepDTO.getSortOrder() != null ? stepDTO.getSortOrder() : sortOrder);
                step.setIsMandatory(stepDTO.getIsMandatory() != null ? stepDTO.getIsMandatory() : 1);
                step.setRequiresApproval(stepDTO.getRequiresApproval() != null ? stepDTO.getRequiresApproval() : 0);

                inspectionStepMapper.insert(step);
                savedSteps.add(step);

                // Save fields for this step
                List<StepField> stepFields = new ArrayList<>();
                if (stepDTO.getFields() != null) {
                    int fieldOrder = 1;
                    for (StepFieldDTO fieldDTO : stepDTO.getFields()) {
                        StepField field = new StepField();
                        field.setId(UUID.randomUUID().toString().replace("-", ""));
                        field.setStepId(step.getId());
                        field.setFieldName(fieldDTO.getFieldName());
                        field.setFieldCode(fieldDTO.getFieldCode());
                        field.setFieldType(fieldDTO.getFieldType());
                        field.setUnit(fieldDTO.getUnit());
                        field.setDefaultValue(fieldDTO.getDefaultValue());
                        field.setIsRequired(fieldDTO.getIsRequired() != null ? fieldDTO.getIsRequired() : 1);
                        field.setSortOrder(fieldDTO.getSortOrder() != null ? fieldDTO.getSortOrder() : fieldOrder);
                        field.setFieldConfig(serializeFieldConfig(fieldDTO.getFieldConfig()));
                        field.setHint(fieldDTO.getHint());

                        stepFieldMapper.insert(field);
                        stepFields.add(field);
                        fieldOrder++;
                    }
                }
                savedFieldsByStep.add(stepFields);
                sortOrder++;
            }
        }

        // 3. Build and return VO
        return buildTemplateVO(template, savedSteps, savedFieldsByStep);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionTemplateVO updateTemplateWithSteps(String id, InspectionTemplateDTO dto) {
        // 1. Load existing template
        InspectionTemplate template = this.getById(id);
        if (template == null) {
            throw new IllegalArgumentException("Template không tồn tại: " + id);
        }

        // 2. Update template fields
        template.setTemplateName(dto.getTemplateName());
        template.setDescription(dto.getDescription());
        template.setStageType(dto.getStageType());
        if (dto.getVersion() != null) {
            template.setVersion(dto.getVersion());
        }
        template.setNotes(dto.getNotes());

        this.updateById(template);

        // 3. Diff logic for steps
        List<InspectionStep> existingSteps = inspectionStepMapper.selectList(
                new QueryWrapper<InspectionStep>().eq("template_id", id).orderByAsc("sort_order")
        );
        Set<String> existingStepIds = existingSteps.stream()
                .map(InspectionStep::getId)
                .collect(Collectors.toSet());

        Set<String> incomingStepIds = new HashSet<>();
        List<InspectionStep> resultSteps = new ArrayList<>();
        List<List<StepField>> resultFieldsByStep = new ArrayList<>();

        if (dto.getSteps() != null) {
            int sortOrder = 1;
            for (InspectionStepDTO stepDTO : dto.getSteps()) {
                InspectionStep step;

                if (StringUtils.hasText(stepDTO.getId()) && existingStepIds.contains(stepDTO.getId())) {
                    // Update existing step
                    incomingStepIds.add(stepDTO.getId());
                    step = inspectionStepMapper.selectById(stepDTO.getId());
                    step.setStepName(stepDTO.getStepName());
                    step.setDescription(stepDTO.getDescription());
                    step.setSortOrder(stepDTO.getSortOrder() != null ? stepDTO.getSortOrder() : sortOrder);
                    step.setIsMandatory(stepDTO.getIsMandatory() != null ? stepDTO.getIsMandatory() : 1);
                    step.setRequiresApproval(stepDTO.getRequiresApproval() != null ? stepDTO.getRequiresApproval() : 0);
                    inspectionStepMapper.updateById(step);
                } else {
                    // Insert new step
                    step = new InspectionStep();
                    step.setId(UUID.randomUUID().toString().replace("-", ""));
                    step.setTemplateId(id);
                    step.setStepName(stepDTO.getStepName());
                    step.setDescription(stepDTO.getDescription());
                    step.setSortOrder(stepDTO.getSortOrder() != null ? stepDTO.getSortOrder() : sortOrder);
                    step.setIsMandatory(stepDTO.getIsMandatory() != null ? stepDTO.getIsMandatory() : 1);
                    step.setRequiresApproval(stepDTO.getRequiresApproval() != null ? stepDTO.getRequiresApproval() : 0);
                    inspectionStepMapper.insert(step);
                }

                resultSteps.add(step);

                // Diff logic for fields within this step
                List<StepField> stepFields = diffFields(step.getId(), stepDTO.getFields());
                resultFieldsByStep.add(stepFields);

                sortOrder++;
            }
        }

        // 4. Delete steps that are no longer in the DTO (cascade delete fields)
        for (String existingStepId : existingStepIds) {
            if (!incomingStepIds.contains(existingStepId)) {
                // Delete all fields of this step first
                stepFieldMapper.delete(
                        new QueryWrapper<StepField>().eq("step_id", existingStepId)
                );
                // Delete the step
                inspectionStepMapper.deleteById(existingStepId);
            }
        }

        // 5. Build and return VO
        return buildTemplateVO(template, resultSteps, resultFieldsByStep);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(String id) {
        // 1. Check referential integrity - reject if executions exist
        long executionCount = inspectionExecutionMapper.selectCount(
                new QueryWrapper<InspectionExecution>().eq("template_id", id)
        );
        if (executionCount > 0) {
            throw new IllegalStateException(
                    "Không thể xóa template đã được sử dụng trong " + executionCount + " phiên kiểm tra"
            );
        }

        // 2. Delete all fields of all steps
        List<InspectionStep> steps = inspectionStepMapper.selectList(
                new QueryWrapper<InspectionStep>().eq("template_id", id)
        );
        for (InspectionStep step : steps) {
            stepFieldMapper.delete(
                    new QueryWrapper<StepField>().eq("step_id", step.getId())
            );
        }

        // 3. Delete all steps
        inspectionStepMapper.delete(
                new QueryWrapper<InspectionStep>().eq("template_id", id)
        );

        // 4. Delete template
        this.removeById(id);
    }

    @Override
    public InspectionTemplateVO getTemplateDetail(String id) {
        InspectionTemplate template = this.getById(id);
        if (template == null) {
            return null;
        }

        // Load steps ordered by sort_order
        List<InspectionStep> steps = inspectionStepMapper.selectList(
                new QueryWrapper<InspectionStep>()
                        .eq("template_id", id)
                        .orderByAsc("sort_order")
        );

        // Load fields for each step
        List<List<StepField>> fieldsByStep = new ArrayList<>();
        for (InspectionStep step : steps) {
            List<StepField> fields = stepFieldMapper.selectList(
                    new QueryWrapper<StepField>()
                            .eq("step_id", step.getId())
                            .orderByAsc("sort_order")
            );
            fieldsByStep.add(fields);
        }

        return buildTemplateVO(template, steps, fieldsByStep);
    }

    @Override
    public IPage<InspectionTemplateVO> listTemplates(Page<InspectionTemplate> page, String stageType, String status, String search) {
        QueryWrapper<InspectionTemplate> qw = new QueryWrapper<>();

        // Filter by stageType
        if (StringUtils.hasText(stageType)) {
            qw.eq("stage_type", stageType);
        }

        // Filter by status
        if (StringUtils.hasText(status)) {
            qw.eq("status", status);
        }

        // Search by name or code
        if (StringUtils.hasText(search)) {
            qw.and(wrapper -> wrapper
                    .like("template_name", search)
                    .or()
                    .like("template_code", search)
            );
        }

        qw.orderByDesc("create_time");

        // Execute paginated query
        IPage<InspectionTemplate> templatePage = this.page(page, qw);

        // Convert to VO page
        Page<InspectionTemplateVO> voPage = new Page<>(templatePage.getCurrent(), templatePage.getSize(), templatePage.getTotal());
        List<InspectionTemplateVO> voList = templatePage.getRecords().stream()
                .map(this::buildListItemVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    // ==================== Step Reorder & Delete ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorderSteps(String templateId, List<String> stepIds) {
        if (stepIds == null || stepIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách step IDs không được rỗng");
        }

        // Verify template exists
        InspectionTemplate template = this.getById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template không tồn tại: " + templateId);
        }

        // Verify all step IDs belong to this template
        List<InspectionStep> existingSteps = inspectionStepMapper.selectList(
                new QueryWrapper<InspectionStep>().eq("template_id", templateId)
        );
        Set<String> existingStepIds = existingSteps.stream()
                .map(InspectionStep::getId)
                .collect(Collectors.toSet());

        for (String stepId : stepIds) {
            if (!existingStepIds.contains(stepId)) {
                throw new IllegalArgumentException("Step ID không thuộc template này: " + stepId);
            }
        }

        // Update sort_order starting from 1
        int sortOrder = 1;
        for (String stepId : stepIds) {
            InspectionStep step = new InspectionStep();
            step.setId(stepId);
            step.setSortOrder(sortOrder);
            inspectionStepMapper.updateById(step);
            sortOrder++;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStep(String stepId) {
        // Verify step exists
        InspectionStep step = inspectionStepMapper.selectById(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step không tồn tại: " + stepId);
        }

        // 1. Cascade delete all fields of this step
        stepFieldMapper.delete(
                new QueryWrapper<StepField>().eq("step_id", stepId)
        );

        // 2. Delete the step itself
        inspectionStepMapper.deleteById(stepId);

        // 3. Re-number remaining steps to maintain contiguous sort_order
        List<InspectionStep> remainingSteps = inspectionStepMapper.selectList(
                new QueryWrapper<InspectionStep>()
                        .eq("template_id", step.getTemplateId())
                        .orderByAsc("sort_order")
        );
        int sortOrder = 1;
        for (InspectionStep remainingStep : remainingSteps) {
            if (remainingStep.getSortOrder() != sortOrder) {
                remainingStep.setSortOrder(sortOrder);
                inspectionStepMapper.updateById(remainingStep);
            }
            sortOrder++;
        }
    }

    // ==================== Template Clone ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionTemplateVO cloneTemplate(String id) {
        // 1. Load source template
        InspectionTemplate source = this.getById(id);
        if (source == null) {
            throw new IllegalArgumentException("Template không tồn tại: " + id);
        }

        // 2. Create new template with new code, same name + " (Copy)", incremented version, status = draft
        InspectionTemplate cloned = new InspectionTemplate();
        cloned.setTemplateCode(templateCodeGenerator.generateCode());
        cloned.setTemplateName(source.getTemplateName() + " (Copy)");
        cloned.setDescription(source.getDescription());
        cloned.setStageType(source.getStageType());
        cloned.setVersion(incrementVersion(source.getVersion()));
        cloned.setStatus("draft");
        cloned.setNotes(source.getNotes());

        this.save(cloned);

        // 3. Load all steps of source template
        List<InspectionStep> sourceSteps = inspectionStepMapper.selectList(
                new QueryWrapper<InspectionStep>()
                        .eq("template_id", id)
                        .orderByAsc("sort_order")
        );

        // 4. Deep clone steps and fields
        List<InspectionStep> clonedSteps = new ArrayList<>();
        List<List<StepField>> clonedFieldsByStep = new ArrayList<>();

        for (InspectionStep sourceStep : sourceSteps) {
            // Clone step with new ID, pointing to new template
            InspectionStep clonedStep = new InspectionStep();
            clonedStep.setId(UUID.randomUUID().toString().replace("-", ""));
            clonedStep.setTemplateId(cloned.getId());
            clonedStep.setStepName(sourceStep.getStepName());
            clonedStep.setDescription(sourceStep.getDescription());
            clonedStep.setSortOrder(sourceStep.getSortOrder());
            clonedStep.setIsMandatory(sourceStep.getIsMandatory());
            clonedStep.setRequiresApproval(sourceStep.getRequiresApproval());

            inspectionStepMapper.insert(clonedStep);
            clonedSteps.add(clonedStep);

            // Load and clone all fields of this step
            List<StepField> sourceFields = stepFieldMapper.selectList(
                    new QueryWrapper<StepField>()
                            .eq("step_id", sourceStep.getId())
                            .orderByAsc("sort_order")
            );

            List<StepField> clonedFields = new ArrayList<>();
            for (StepField sourceField : sourceFields) {
                StepField clonedField = new StepField();
                clonedField.setId(UUID.randomUUID().toString().replace("-", ""));
                clonedField.setStepId(clonedStep.getId());
                clonedField.setFieldName(sourceField.getFieldName());
                clonedField.setFieldCode(sourceField.getFieldCode());
                clonedField.setFieldType(sourceField.getFieldType());
                clonedField.setUnit(sourceField.getUnit());
                clonedField.setDefaultValue(sourceField.getDefaultValue());
                clonedField.setIsRequired(sourceField.getIsRequired());
                clonedField.setSortOrder(sourceField.getSortOrder());
                clonedField.setFieldConfig(sourceField.getFieldConfig());
                clonedField.setHint(sourceField.getHint());

                stepFieldMapper.insert(clonedField);
                clonedFields.add(clonedField);
            }
            clonedFieldsByStep.add(clonedFields);
        }

        // 5. Build and return VO
        return buildTemplateVO(cloned, clonedSteps, clonedFieldsByStep);
    }

    // ==================== Template Activation ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateTemplate(String id) {
        // 1. Load template
        InspectionTemplate template = this.getById(id);
        if (template == null) {
            throw new IllegalArgumentException("Template không tồn tại: " + id);
        }

        // 2. Load steps and fields for validation
        List<InspectionStep> steps = inspectionStepMapper.selectList(
                new QueryWrapper<InspectionStep>()
                        .eq("template_id", id)
                        .orderByAsc("sort_order")
        );

        List<List<StepField>> fieldsByStep = new ArrayList<>();
        for (InspectionStep step : steps) {
            List<StepField> fields = stepFieldMapper.selectList(
                    new QueryWrapper<StepField>()
                            .eq("step_id", step.getId())
                            .orderByAsc("sort_order")
            );
            fieldsByStep.add(fields);
        }

        // 3. Validate template via TemplateValidationService
        List<ValidationErrorVO.ValidationErrorItem> errors =
                templateValidationService.validateForActivation(template, steps, fieldsByStep);

        if (errors != null && !errors.isEmpty()) {
            throw new TemplateValidationException(errors);
        }

        // 4. Find currently active templates with same stage_type in same org and set to obsolete
        QueryWrapper<InspectionTemplate> obsoleteQuery = new QueryWrapper<>();
        obsoleteQuery.eq("stage_type", template.getStageType())
                .eq("status", "active")
                .ne("id", id);

        // Multi-tenant: only obsolete templates within the same organization
        if (StringUtils.hasText(template.getSysOrgCode())) {
            obsoleteQuery.eq("sys_org_code", template.getSysOrgCode());
        }

        List<InspectionTemplate> activeTemplates = this.list(obsoleteQuery);
        for (InspectionTemplate activeTemplate : activeTemplates) {
            activeTemplate.setStatus("obsolete");
            this.updateById(activeTemplate);
        }

        // 5. Set current template status to active
        template.setStatus("active");
        this.updateById(template);
    }

    /**
     * Increment version string.
     * Examples: "1.0" → "2.0", "3.5" → "4.5", null → "1.0"
     */
    private String incrementVersion(String version) {
        if (version == null || version.isEmpty()) {
            return "1.0";
        }
        try {
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]);
            String minor = parts.length > 1 ? parts[1] : "0";
            return (major + 1) + "." + minor;
        } catch (NumberFormatException e) {
            // If version is not numeric, just append ".1"
            return version + ".1";
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Diff logic for fields within a step.
     * - Fields with id that exist → update
     * - Fields without id → insert new
     * - Existing fields not in DTO → delete
     */
    private List<StepField> diffFields(String stepId, List<StepFieldDTO> fieldDTOs) {
        List<StepField> existingFields = stepFieldMapper.selectList(
                new QueryWrapper<StepField>().eq("step_id", stepId).orderByAsc("sort_order")
        );
        Set<String> existingFieldIds = existingFields.stream()
                .map(StepField::getId)
                .collect(Collectors.toSet());

        Set<String> incomingFieldIds = new HashSet<>();
        List<StepField> resultFields = new ArrayList<>();

        if (fieldDTOs != null) {
            int fieldOrder = 1;
            for (StepFieldDTO fieldDTO : fieldDTOs) {
                StepField field;

                if (StringUtils.hasText(fieldDTO.getId()) && existingFieldIds.contains(fieldDTO.getId())) {
                    // Update existing field
                    incomingFieldIds.add(fieldDTO.getId());
                    field = stepFieldMapper.selectById(fieldDTO.getId());
                    field.setFieldName(fieldDTO.getFieldName());
                    field.setFieldCode(fieldDTO.getFieldCode());
                    field.setFieldType(fieldDTO.getFieldType());
                    field.setUnit(fieldDTO.getUnit());
                    field.setDefaultValue(fieldDTO.getDefaultValue());
                    field.setIsRequired(fieldDTO.getIsRequired() != null ? fieldDTO.getIsRequired() : 1);
                    field.setSortOrder(fieldDTO.getSortOrder() != null ? fieldDTO.getSortOrder() : fieldOrder);
                    field.setFieldConfig(serializeFieldConfig(fieldDTO.getFieldConfig()));
                    field.setHint(fieldDTO.getHint());
                    stepFieldMapper.updateById(field);
                } else {
                    // Insert new field
                    field = new StepField();
                    field.setId(UUID.randomUUID().toString().replace("-", ""));
                    field.setStepId(stepId);
                    field.setFieldName(fieldDTO.getFieldName());
                    field.setFieldCode(fieldDTO.getFieldCode());
                    field.setFieldType(fieldDTO.getFieldType());
                    field.setUnit(fieldDTO.getUnit());
                    field.setDefaultValue(fieldDTO.getDefaultValue());
                    field.setIsRequired(fieldDTO.getIsRequired() != null ? fieldDTO.getIsRequired() : 1);
                    field.setSortOrder(fieldDTO.getSortOrder() != null ? fieldDTO.getSortOrder() : fieldOrder);
                    field.setFieldConfig(serializeFieldConfig(fieldDTO.getFieldConfig()));
                    field.setHint(fieldDTO.getHint());
                    stepFieldMapper.insert(field);
                }

                resultFields.add(field);
                fieldOrder++;
            }
        }

        // Delete fields no longer in DTO
        for (String existingFieldId : existingFieldIds) {
            if (!incomingFieldIds.contains(existingFieldId)) {
                stepFieldMapper.deleteById(existingFieldId);
            }
        }

        return resultFields;
    }

    /**
     * Build full InspectionTemplateVO with steps and fields.
     */
    private InspectionTemplateVO buildTemplateVO(InspectionTemplate template,
                                                  List<InspectionStep> steps,
                                                  List<List<StepField>> fieldsByStep) {
        InspectionTemplateVO vo = new InspectionTemplateVO();
        BeanUtils.copyProperties(template, vo);
        vo.setStepCount(steps.size());

        List<InspectionStepVO> stepVOs = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            InspectionStep step = steps.get(i);
            InspectionStepVO stepVO = new InspectionStepVO();
            BeanUtils.copyProperties(step, stepVO);

            List<StepFieldVO> fieldVOs = new ArrayList<>();
            if (i < fieldsByStep.size()) {
                for (StepField field : fieldsByStep.get(i)) {
                    StepFieldVO fieldVO = new StepFieldVO();
                    BeanUtils.copyProperties(field, fieldVO);
                    fieldVO.setFieldConfig(deserializeFieldConfig(field.getFieldConfig()));
                    fieldVOs.add(fieldVO);
                }
            }
            stepVO.setFields(fieldVOs);
            stepVOs.add(stepVO);
        }

        vo.setSteps(stepVOs);
        return vo;
    }

    /**
     * Build list item VO (without nested steps/fields, but with stepCount).
     */
    private InspectionTemplateVO buildListItemVO(InspectionTemplate template) {
        InspectionTemplateVO vo = new InspectionTemplateVO();
        BeanUtils.copyProperties(template, vo);

        // Count steps for this template
        long stepCount = inspectionStepMapper.selectCount(
                new QueryWrapper<InspectionStep>().eq("template_id", template.getId())
        );
        vo.setStepCount((int) stepCount);

        return vo;
    }

    /**
     * Serialize fieldConfig Object to JSON string for DB storage.
     */
    private String serializeFieldConfig(Object fieldConfig) {
        if (fieldConfig == null) {
            return null;
        }
        if (fieldConfig instanceof String) {
            return (String) fieldConfig;
        }
        try {
            return objectMapper.writeValueAsString(fieldConfig);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Không thể serialize field_config: " + e.getMessage(), e);
        }
    }

    /**
     * Deserialize JSON string from DB to Object for VO.
     */
    private Object deserializeFieldConfig(String fieldConfigJson) {
        if (fieldConfigJson == null || fieldConfigJson.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(fieldConfigJson, Object.class);
        } catch (JsonProcessingException e) {
            // Return raw string if not valid JSON
            return fieldConfigJson;
        }
    }
}
