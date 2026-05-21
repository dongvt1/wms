package com.cy.modules.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.modules.qms.dto.FieldValueDTO;
import com.cy.modules.qms.dto.InspectionExecutionDTO;
import com.cy.modules.qms.entity.*;
import com.cy.modules.qms.mapper.FieldValueMapper;
import com.cy.modules.qms.mapper.InspectionExecutionMapper;
import com.cy.modules.qms.mapper.InspectionStepMapper;
import com.cy.modules.qms.mapper.StepFieldMapper;
import com.cy.modules.qms.mapper.StepResultMapper;
import com.cy.modules.qms.service.EvaluationService;
import com.cy.modules.qms.service.ExecutionCodeGenerator;
import com.cy.modules.qms.service.InspectionExecutionService;
import com.cy.modules.qms.service.TemplateResolutionService;
import com.cy.modules.qms.vo.FieldValueVO;
import com.cy.modules.qms.vo.InspectionExecutionVO;
import com.cy.modules.qms.vo.StepResultVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Implementation InspectionExecutionService.
 * Quản lý tạo phiên kiểm tra với template snapshot pattern.
 *
 * @Author: QMS
 * @Date: 2026-03-15
 */
@Service
public class InspectionExecutionServiceImpl implements InspectionExecutionService {

    @Autowired
    private InspectionExecutionMapper inspectionExecutionMapper;

    @Autowired
    private StepResultMapper stepResultMapper;

    @Autowired
    private FieldValueMapper fieldValueMapper;

    @Autowired
    private InspectionStepMapper inspectionStepMapper;

    @Autowired
    private StepFieldMapper stepFieldMapper;

    @Autowired
    private TemplateResolutionService templateResolutionService;

    @Autowired
    private ExecutionCodeGenerator executionCodeGenerator;

    @Autowired
    private EvaluationService evaluationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionExecutionVO createExecution(InspectionExecutionDTO dto) {
        // 1. Resolve template phù hợp cho product + stageType
        InspectionTemplate template = templateResolutionService.resolveTemplate(
                dto.getProductId(), dto.getStageType());

        // 2. Load steps + fields từ template
        List<InspectionStep> steps = inspectionStepMapper.selectList(
                new QueryWrapper<InspectionStep>()
                        .eq("template_id", template.getId())
                        .orderByAsc("sort_order")
        );

        Map<String, List<StepField>> fieldsByStepId = new LinkedHashMap<>();
        for (InspectionStep step : steps) {
            List<StepField> fields = stepFieldMapper.selectList(
                    new QueryWrapper<StepField>()
                            .eq("step_id", step.getId())
                            .orderByAsc("sort_order")
            );
            fieldsByStepId.put(step.getId(), fields);
        }

        // 3. Build template snapshot JSON
        String templateSnapshot = buildTemplateSnapshot(template, steps, fieldsByStepId);

        // 4. Create InspectionExecution entity
        InspectionExecution execution = new InspectionExecution();
        execution.setExecutionCode(executionCodeGenerator.generateCode());
        execution.setTemplateId(template.getId());
        execution.setTemplateSnapshot(templateSnapshot);
        execution.setProductId(dto.getProductId());
        execution.setStageType(dto.getStageType());
        execution.setWorkOrderId(dto.getWorkOrderId());
        execution.setProductionStageId(dto.getProductionStageId());
        execution.setStatus("draft");
        execution.setNotes(dto.getNotes());

        inspectionExecutionMapper.insert(execution);

        // 5. Create StepResult records cho mỗi step
        List<StepResult> stepResults = new ArrayList<>();
        List<List<FieldValue>> fieldValuesByStep = new ArrayList<>();

        for (InspectionStep step : steps) {
            StepResult stepResult = new StepResult();
            stepResult.setId(UUID.randomUUID().toString().replace("-", ""));
            stepResult.setExecutionId(execution.getId());
            stepResult.setStepId(step.getId());
            stepResult.setStepName(step.getStepName());
            stepResult.setSortOrder(step.getSortOrder());
            stepResult.setIsMandatory(step.getIsMandatory());
            stepResult.setResult(null); // pending - no result yet
            stepResult.setStatus("pending");

            stepResultMapper.insert(stepResult);
            stepResults.add(stepResult);

            // 6. Create FieldValue records cho mỗi field trong step
            List<StepField> fields = fieldsByStepId.get(step.getId());
            List<FieldValue> fieldValues = new ArrayList<>();

            if (fields != null) {
                for (StepField field : fields) {
                    FieldValue fieldValue = new FieldValue();
                    fieldValue.setId(UUID.randomUUID().toString().replace("-", ""));
                    fieldValue.setStepResultId(stepResult.getId());
                    fieldValue.setFieldId(field.getId());
                    fieldValue.setFieldName(field.getFieldName());
                    fieldValue.setFieldType(field.getFieldType());
                    fieldValue.setFieldConfig(field.getFieldConfig());
                    fieldValue.setIsRequired(field.getIsRequired());
                    fieldValue.setActualValue(null);
                    fieldValue.setResult(null);
                    fieldValue.setEvalMessage(null);

                    fieldValueMapper.insert(fieldValue);
                    fieldValues.add(fieldValue);
                }
            }
            fieldValuesByStep.add(fieldValues);
        }

        // 7. Build and return VO
        return buildExecutionVO(execution, template, stepResults, fieldValuesByStep);
    }

    // ==================== List & Detail Methods ====================

    @Override
    public IPage<InspectionExecutionVO> listExecutions(Page<InspectionExecution> page, String status, String productId, String stageType) {
        QueryWrapper<InspectionExecution> queryWrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (productId != null && !productId.isEmpty()) {
            queryWrapper.eq("product_id", productId);
        }
        if (stageType != null && !stageType.isEmpty()) {
            queryWrapper.eq("stage_type", stageType);
        }
        queryWrapper.orderByDesc("create_time");

        IPage<InspectionExecution> entityPage = inspectionExecutionMapper.selectPage(page, queryWrapper);

        // Convert to VO page
        Page<InspectionExecutionVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<InspectionExecutionVO> voList = new ArrayList<>();
        for (InspectionExecution execution : entityPage.getRecords()) {
            InspectionExecutionVO vo = new InspectionExecutionVO();
            BeanUtils.copyProperties(execution, vo);
            vo.setTemplateName(getTemplateNameFromExecution(execution));
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public InspectionExecutionVO getExecutionDetail(String id) {
        InspectionExecution execution = inspectionExecutionMapper.selectById(id);
        if (execution == null) {
            return null;
        }

        // Load step results
        List<StepResult> stepResults = stepResultMapper.selectList(
                new QueryWrapper<StepResult>()
                        .eq("execution_id", id)
                        .orderByAsc("sort_order"));

        // Load field values for each step result
        List<List<FieldValue>> fieldValuesByStep = new ArrayList<>();
        for (StepResult stepResult : stepResults) {
            List<FieldValue> fieldValues = fieldValueMapper.selectList(
                    new QueryWrapper<FieldValue>()
                            .eq("step_result_id", stepResult.getId()));
            fieldValuesByStep.add(fieldValues);
        }

        // Build VO
        InspectionExecutionVO vo = new InspectionExecutionVO();
        BeanUtils.copyProperties(execution, vo);
        vo.setTemplateName(getTemplateNameFromExecution(execution));

        List<StepResultVO> stepVOs = new ArrayList<>();
        for (int i = 0; i < stepResults.size(); i++) {
            StepResult stepResult = stepResults.get(i);
            StepResultVO stepVO = new StepResultVO();
            BeanUtils.copyProperties(stepResult, stepVO);

            List<FieldValueVO> fieldVOs = new ArrayList<>();
            if (i < fieldValuesByStep.size()) {
                for (FieldValue fieldValue : fieldValuesByStep.get(i)) {
                    FieldValueVO fieldVO = new FieldValueVO();
                    BeanUtils.copyProperties(fieldValue, fieldVO);
                    fieldVO.setFieldConfig(deserializeFieldConfig(fieldValue.getFieldConfig()));
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
     * Get template name from execution (from snapshot JSON or template table).
     */
    private String getTemplateNameFromExecution(InspectionExecution execution) {
        // Try to extract from snapshot first
        String snapshot = execution.getTemplateSnapshot();
        if (snapshot != null && !snapshot.isEmpty()) {
            try {
                Map<?, ?> snapshotMap = objectMapper.readValue(snapshot, Map.class);
                Object templateName = snapshotMap.get("templateName");
                if (templateName != null) {
                    return templateName.toString();
                }
            } catch (JsonProcessingException e) {
                // Fall through to query template table
            }
        }
        return null;
    }

    // ==================== Save Draft & Submit Methods ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDraft(String executionId, String stepResultId, List<FieldValueDTO> values) {
        // 1. Validate execution tồn tại
        InspectionExecution execution = inspectionExecutionMapper.selectById(executionId);
        if (execution == null) {
            throw new IllegalArgumentException("Không tìm thấy phiên kiểm tra với ID: " + executionId);
        }

        // 2. Validate status cho phép chỉnh sửa (draft hoặc in_progress)
        String status = execution.getStatus();
        if (!"draft".equals(status) && !"in_progress".equals(status)) {
            throw new IllegalStateException(
                    "Không thể lưu nháp khi phiên kiểm tra ở trạng thái: " + status);
        }

        // 3. Validate stepResult thuộc execution
        StepResult stepResult = stepResultMapper.selectById(stepResultId);
        if (stepResult == null || !executionId.equals(stepResult.getExecutionId())) {
            throw new IllegalArgumentException(
                    "Step result không tồn tại hoặc không thuộc phiên kiểm tra này");
        }

        // 4. Cập nhật actualValue cho mỗi FieldValue theo fieldId
        if (values != null && !values.isEmpty()) {
            List<FieldValue> fieldValues = fieldValueMapper.selectList(
                    new QueryWrapper<FieldValue>().eq("step_result_id", stepResultId));

            Map<String, FieldValue> fieldValueMap = new HashMap<>();
            for (FieldValue fv : fieldValues) {
                fieldValueMap.put(fv.getFieldId(), fv);
            }

            for (FieldValueDTO dto : values) {
                FieldValue fv = fieldValueMap.get(dto.getFieldId());
                if (fv != null) {
                    fv.setActualValue(dto.getValue());
                    fieldValueMapper.updateById(fv);
                }
            }
        }

        // 5. Nếu execution đang ở status "draft", chuyển sang "in_progress"
        if ("draft".equals(status)) {
            execution.setStatus("in_progress");
            inspectionExecutionMapper.updateById(execution);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitStepValues(String executionId, String stepResultId, List<FieldValueDTO> values) {
        // 1. Validate execution tồn tại
        InspectionExecution execution = inspectionExecutionMapper.selectById(executionId);
        if (execution == null) {
            throw new IllegalArgumentException("Không tìm thấy phiên kiểm tra với ID: " + executionId);
        }

        // 2. Validate status cho phép (draft hoặc in_progress)
        String status = execution.getStatus();
        if (!"draft".equals(status) && !"in_progress".equals(status)) {
            throw new IllegalStateException(
                    "Không thể submit khi phiên kiểm tra ở trạng thái: " + status);
        }

        // 3. Validate stepResult thuộc execution
        StepResult stepResult = stepResultMapper.selectById(stepResultId);
        if (stepResult == null || !executionId.equals(stepResult.getExecutionId())) {
            throw new IllegalArgumentException(
                    "Step result không tồn tại hoặc không thuộc phiên kiểm tra này");
        }

        // 4. Enforce sequential step completion: kiểm tra tất cả step trước đã completed
        List<StepResult> allStepResults = stepResultMapper.selectList(
                new QueryWrapper<StepResult>()
                        .eq("execution_id", executionId)
                        .orderByAsc("sort_order"));

        for (StepResult sr : allStepResults) {
            // Chỉ kiểm tra các step có sort_order nhỏ hơn step hiện tại
            if (sr.getSortOrder() < stepResult.getSortOrder()) {
                if (!"completed".equals(sr.getStatus())) {
                    throw new IllegalStateException(
                            "Phải hoàn thành bước '" + sr.getStepName() + "' (thứ tự " + sr.getSortOrder()
                                    + ") trước khi submit bước hiện tại");
                }
            }
        }

        // 5. Cập nhật actualValue cho mỗi FieldValue theo fieldId
        List<FieldValue> fieldValues = fieldValueMapper.selectList(
                new QueryWrapper<FieldValue>().eq("step_result_id", stepResultId));

        Map<String, FieldValue> fieldValueMap = new HashMap<>();
        for (FieldValue fv : fieldValues) {
            fieldValueMap.put(fv.getFieldId(), fv);
        }

        if (values != null) {
            for (FieldValueDTO dto : values) {
                FieldValue fv = fieldValueMap.get(dto.getFieldId());
                if (fv != null) {
                    fv.setActualValue(dto.getValue());
                }
            }
        }

        // 6. Gọi EvaluationService.evaluateField() cho mỗi FieldValue
        for (FieldValue fv : fieldValues) {
            evaluationService.evaluateField(fv);
            fieldValueMapper.updateById(fv);
        }

        // 7. Gọi EvaluationService.evaluateStep() để tính kết quả step
        String stepEvalResult = evaluationService.evaluateStep(fieldValues);

        // 8. Cập nhật StepResult: result, status = "completed", completedTime
        stepResult.setResult(stepEvalResult);
        stepResult.setStatus("completed");
        stepResult.setCompletedTime(new Date());
        stepResultMapper.updateById(stepResult);

        // 9. Nếu execution đang ở status "draft", chuyển sang "in_progress"
        if ("draft".equals(status)) {
            execution.setStatus("in_progress");
            inspectionExecutionMapper.updateById(execution);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExecution(String executionId) {
        // 1. Validate execution tồn tại
        InspectionExecution execution = inspectionExecutionMapper.selectById(executionId);
        if (execution == null) {
            throw new IllegalArgumentException("Không tìm thấy phiên kiểm tra với ID: " + executionId);
        }

        // 2. Validate status = "in_progress"
        if (!"in_progress".equals(execution.getStatus())) {
            throw new IllegalStateException(
                    "Chỉ có thể submit phiên kiểm tra ở trạng thái 'in_progress', hiện tại: " + execution.getStatus());
        }

        // 3. Validate tất cả mandatory steps (isMandatory=1) đã completed
        List<StepResult> allStepResults = stepResultMapper.selectList(
                new QueryWrapper<StepResult>()
                        .eq("execution_id", executionId)
                        .orderByAsc("sort_order"));

        List<String> incompleteSteps = new ArrayList<>();
        for (StepResult sr : allStepResults) {
            if (sr.getIsMandatory() != null && sr.getIsMandatory() == 1) {
                if (!"completed".equals(sr.getStatus())) {
                    incompleteSteps.add(sr.getStepName());
                }
            }
        }

        if (!incompleteSteps.isEmpty()) {
            throw new IllegalStateException(
                    "Các bước bắt buộc chưa hoàn thành: " + String.join(", ", incompleteSteps));
        }

        // 4. Gọi EvaluationService.evaluateExecution() để tính overall result
        String overallResult = evaluationService.evaluateExecution(allStepResults);

        // 5. Cập nhật execution: overallResult, status = "pending_approval"
        execution.setOverallResult(overallResult);
        execution.setStatus("pending_approval");
        inspectionExecutionMapper.updateById(execution);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Build template snapshot JSON chứa toàn bộ cấu hình template tại thời điểm tạo execution.
     */
    private String buildTemplateSnapshot(InspectionTemplate template,
                                          List<InspectionStep> steps,
                                          Map<String, List<StepField>> fieldsByStepId) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("templateId", template.getId());
        snapshot.put("templateCode", template.getTemplateCode());
        snapshot.put("templateName", template.getTemplateName());
        snapshot.put("stageType", template.getStageType());
        snapshot.put("version", template.getVersion());

        List<Map<String, Object>> stepSnapshots = new ArrayList<>();
        for (InspectionStep step : steps) {
            Map<String, Object> stepSnapshot = new LinkedHashMap<>();
            stepSnapshot.put("stepId", step.getId());
            stepSnapshot.put("stepName", step.getStepName());
            stepSnapshot.put("description", step.getDescription());
            stepSnapshot.put("sortOrder", step.getSortOrder());
            stepSnapshot.put("isMandatory", step.getIsMandatory());
            stepSnapshot.put("requiresApproval", step.getRequiresApproval());

            List<Map<String, Object>> fieldSnapshots = new ArrayList<>();
            List<StepField> fields = fieldsByStepId.get(step.getId());
            if (fields != null) {
                for (StepField field : fields) {
                    Map<String, Object> fieldSnapshot = new LinkedHashMap<>();
                    fieldSnapshot.put("fieldId", field.getId());
                    fieldSnapshot.put("fieldName", field.getFieldName());
                    fieldSnapshot.put("fieldCode", field.getFieldCode());
                    fieldSnapshot.put("fieldType", field.getFieldType());
                    fieldSnapshot.put("unit", field.getUnit());
                    fieldSnapshot.put("defaultValue", field.getDefaultValue());
                    fieldSnapshot.put("isRequired", field.getIsRequired());
                    fieldSnapshot.put("sortOrder", field.getSortOrder());
                    fieldSnapshot.put("fieldConfig", deserializeFieldConfig(field.getFieldConfig()));
                    fieldSnapshot.put("hint", field.getHint());
                    fieldSnapshots.add(fieldSnapshot);
                }
            }
            stepSnapshot.put("fields", fieldSnapshots);
            stepSnapshots.add(stepSnapshot);
        }
        snapshot.put("steps", stepSnapshots);

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không thể serialize template snapshot: " + e.getMessage(), e);
        }
    }

    /**
     * Build InspectionExecutionVO từ execution entity và related data.
     */
    private InspectionExecutionVO buildExecutionVO(InspectionExecution execution,
                                                    InspectionTemplate template,
                                                    List<StepResult> stepResults,
                                                    List<List<FieldValue>> fieldValuesByStep) {
        InspectionExecutionVO vo = new InspectionExecutionVO();
        BeanUtils.copyProperties(execution, vo);
        vo.setTemplateName(template.getTemplateName());

        List<StepResultVO> stepVOs = new ArrayList<>();
        for (int i = 0; i < stepResults.size(); i++) {
            StepResult stepResult = stepResults.get(i);
            StepResultVO stepVO = new StepResultVO();
            BeanUtils.copyProperties(stepResult, stepVO);

            List<FieldValueVO> fieldVOs = new ArrayList<>();
            if (i < fieldValuesByStep.size()) {
                for (FieldValue fieldValue : fieldValuesByStep.get(i)) {
                    FieldValueVO fieldVO = new FieldValueVO();
                    BeanUtils.copyProperties(fieldValue, fieldVO);
                    fieldVO.setFieldConfig(deserializeFieldConfig(fieldValue.getFieldConfig()));
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
     * Deserialize JSON string from DB to Object for VO/snapshot.
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
