package com.cy.modules.qms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.modules.qms.dto.InspectionExecutionDTO;
import com.cy.modules.qms.entity.*;
import com.cy.modules.qms.exception.TemplateNotFoundException;
import com.cy.modules.qms.mapper.*;
import com.cy.modules.qms.service.impl.InspectionExecutionServiceImpl;
import com.cy.modules.qms.vo.FieldValueVO;
import com.cy.modules.qms.vo.InspectionExecutionVO;
import com.cy.modules.qms.vo.StepResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InspectionExecutionService.createExecution().
 *
 * Validates: Requirements 6.1, 6.2
 */
@ExtendWith(MockitoExtension.class)
class InspectionExecutionServiceTest {

    @Mock
    private InspectionExecutionMapper inspectionExecutionMapper;

    @Mock
    private StepResultMapper stepResultMapper;

    @Mock
    private FieldValueMapper fieldValueMapper;

    @Mock
    private InspectionStepMapper inspectionStepMapper;

    @Mock
    private StepFieldMapper stepFieldMapper;

    @Mock
    private TemplateResolutionService templateResolutionService;

    @Mock
    private ExecutionCodeGenerator executionCodeGenerator;

    @InjectMocks
    private InspectionExecutionServiceImpl inspectionExecutionService;

    private InspectionTemplate template;
    private InspectionStep step1;
    private InspectionStep step2;
    private StepField field1;
    private StepField field2;
    private StepField field3;

    @BeforeEach
    void setUp() {
        // Setup template
        template = new InspectionTemplate();
        template.setId("tpl-001");
        template.setTemplateCode("TPL20260315001");
        template.setTemplateName("Kiểm tra ngoại quan PCB");
        template.setStageType("pqc");
        template.setVersion("1.0");
        template.setStatus("active");

        // Setup steps
        step1 = new InspectionStep();
        step1.setId("step-001");
        step1.setTemplateId("tpl-001");
        step1.setStepName("Kiểm tra bề mặt");
        step1.setDescription("Kiểm tra bề mặt bo mạch");
        step1.setSortOrder(1);
        step1.setIsMandatory(1);
        step1.setRequiresApproval(0);

        step2 = new InspectionStep();
        step2.setId("step-002");
        step2.setTemplateId("tpl-001");
        step2.setStepName("Kiểm tra kích thước");
        step2.setDescription("Kiểm tra kích thước bo mạch");
        step2.setSortOrder(2);
        step2.setIsMandatory(0);
        step2.setRequiresApproval(0);

        // Setup fields
        field1 = new StepField();
        field1.setId("field-001");
        field1.setStepId("step-001");
        field1.setFieldName("Độ phẳng bề mặt");
        field1.setFieldCode("surface_flatness");
        field1.setFieldType("measurement");
        field1.setUnit("mm");
        field1.setIsRequired(1);
        field1.setSortOrder(1);
        field1.setFieldConfig("{\"nominalValue\":5.0,\"upperTolerance\":5.5,\"lowerTolerance\":4.5}");

        field2 = new StepField();
        field2.setId("field-002");
        field2.setStepId("step-001");
        field2.setFieldName("Tình trạng mối hàn");
        field2.setFieldCode("solder_condition");
        field2.setFieldType("boolean");
        field2.setIsRequired(1);
        field2.setSortOrder(2);
        field2.setFieldConfig("{\"trueLabel\":\"Đạt\",\"falseLabel\":\"Không đạt\"}");

        field3 = new StepField();
        field3.setId("field-003");
        field3.setStepId("step-002");
        field3.setFieldName("Chiều dài");
        field3.setFieldCode("length");
        field3.setFieldType("number");
        field3.setUnit("mm");
        field3.setIsRequired(1);
        field3.setSortOrder(1);
        field3.setFieldConfig("{\"minValue\":90,\"maxValue\":110,\"decimalPlaces\":1}");
    }

    @Nested
    @DisplayName("createExecution - success scenarios")
    class CreateExecutionSuccess {

        @Test
        @DisplayName("Creates execution with correct code and status")
        void createsExecutionWithCorrectCodeAndStatus() {
            setupMocks();

            InspectionExecutionDTO dto = createDTO("product-001", "pqc", "wo-001", null);
            InspectionExecutionVO result = inspectionExecutionService.createExecution(dto);

            assertThat(result).isNotNull();
            assertThat(result.getExecutionCode()).isEqualTo("EXC20260315001");
            assertThat(result.getStatus()).isEqualTo("draft");
            assertThat(result.getTemplateId()).isEqualTo("tpl-001");
            assertThat(result.getTemplateName()).isEqualTo("Kiểm tra ngoại quan PCB");
            assertThat(result.getProductId()).isEqualTo("product-001");
            assertThat(result.getStageType()).isEqualTo("pqc");
            assertThat(result.getWorkOrderId()).isEqualTo("wo-001");
        }

        @Test
        @DisplayName("Creates StepResult records for each step with pending status")
        void createsStepResultsForEachStep() {
            setupMocks();

            InspectionExecutionDTO dto = createDTO("product-001", "pqc", null, null);
            InspectionExecutionVO result = inspectionExecutionService.createExecution(dto);

            assertThat(result.getSteps()).hasSize(2);

            StepResultVO stepResult1 = result.getSteps().get(0);
            assertThat(stepResult1.getStepId()).isEqualTo("step-001");
            assertThat(stepResult1.getStepName()).isEqualTo("Kiểm tra bề mặt");
            assertThat(stepResult1.getSortOrder()).isEqualTo(1);
            assertThat(stepResult1.getIsMandatory()).isEqualTo(1);
            assertThat(stepResult1.getResult()).isNull();
            assertThat(stepResult1.getStatus()).isEqualTo("pending");

            StepResultVO stepResult2 = result.getSteps().get(1);
            assertThat(stepResult2.getStepId()).isEqualTo("step-002");
            assertThat(stepResult2.getStepName()).isEqualTo("Kiểm tra kích thước");
            assertThat(stepResult2.getSortOrder()).isEqualTo(2);
            assertThat(stepResult2.getIsMandatory()).isEqualTo(0);
            assertThat(stepResult2.getStatus()).isEqualTo("pending");
        }

        @Test
        @DisplayName("Creates FieldValue records for each field with null values")
        void createsFieldValuesForEachField() {
            setupMocks();

            InspectionExecutionDTO dto = createDTO("product-001", "pqc", null, null);
            InspectionExecutionVO result = inspectionExecutionService.createExecution(dto);

            // Step 1 has 2 fields
            List<FieldValueVO> step1Fields = result.getSteps().get(0).getFields();
            assertThat(step1Fields).hasSize(2);

            FieldValueVO fv1 = step1Fields.get(0);
            assertThat(fv1.getFieldId()).isEqualTo("field-001");
            assertThat(fv1.getFieldName()).isEqualTo("Độ phẳng bề mặt");
            assertThat(fv1.getFieldType()).isEqualTo("measurement");
            assertThat(fv1.getFieldConfig()).isNotNull();
            assertThat(fv1.getIsRequired()).isEqualTo(1);
            assertThat(fv1.getActualValue()).isNull();
            assertThat(fv1.getResult()).isNull();

            FieldValueVO fv2 = step1Fields.get(1);
            assertThat(fv2.getFieldId()).isEqualTo("field-002");
            assertThat(fv2.getFieldName()).isEqualTo("Tình trạng mối hàn");
            assertThat(fv2.getFieldType()).isEqualTo("boolean");
            assertThat(fv2.getIsRequired()).isEqualTo(1);
            assertThat(fv2.getActualValue()).isNull();

            // Step 2 has 1 field
            List<FieldValueVO> step2Fields = result.getSteps().get(1).getFields();
            assertThat(step2Fields).hasSize(1);

            FieldValueVO fv3 = step2Fields.get(0);
            assertThat(fv3.getFieldId()).isEqualTo("field-003");
            assertThat(fv3.getFieldName()).isEqualTo("Chiều dài");
            assertThat(fv3.getFieldType()).isEqualTo("number");
        }

        @Test
        @DisplayName("Inserts correct number of records into database")
        void insertsCorrectNumberOfRecords() {
            setupMocks();

            InspectionExecutionDTO dto = createDTO("product-001", "pqc", null, null);
            inspectionExecutionService.createExecution(dto);

            // 1 execution
            verify(inspectionExecutionMapper, times(1)).insert((InspectionExecution) any());
            // 2 step results
            verify(stepResultMapper, times(2)).insert((StepResult) any());
            // 3 field values (2 for step1 + 1 for step2)
            verify(fieldValueMapper, times(3)).insert((FieldValue) any());
        }

        @Test
        @DisplayName("Creates execution with template snapshot JSON")
        void createsExecutionWithTemplateSnapshot() {
            setupMocks();

            InspectionExecutionDTO dto = createDTO("product-001", "pqc", null, null);
            inspectionExecutionService.createExecution(dto);

            verify(inspectionExecutionMapper).insert((InspectionExecution) argThat(execution -> {
                InspectionExecution exec = (InspectionExecution) execution;
                String snapshot = exec.getTemplateSnapshot();
                assertThat(snapshot).isNotNull();
                assertThat(snapshot).contains("\"templateId\":\"tpl-001\"");
                assertThat(snapshot).contains("\"templateName\":\"Kiểm tra ngoại quan PCB\"");
                assertThat(snapshot).contains("\"stepName\":\"Kiểm tra bề mặt\"");
                assertThat(snapshot).contains("\"fieldName\":\"Độ phẳng bề mặt\"");
                assertThat(snapshot).contains("\"fieldType\":\"measurement\"");
                return true;
            }));
        }

        @Test
        @DisplayName("Creates execution with optional workOrderId and productionStageId")
        void createsExecutionWithOptionalFields() {
            setupMocks();

            InspectionExecutionDTO dto = createDTO("product-001", "pqc", "wo-001", "stage-001");
            InspectionExecutionVO result = inspectionExecutionService.createExecution(dto);

            assertThat(result.getWorkOrderId()).isEqualTo("wo-001");
            assertThat(result.getProductionStageId()).isEqualTo("stage-001");
        }
    }

    @Nested
    @DisplayName("createExecution - template with no fields")
    class CreateExecutionNoFields {

        @Test
        @DisplayName("Creates execution for step with no fields")
        void createsExecutionForStepWithNoFields() {
            when(templateResolutionService.resolveTemplate("product-001", "iqc"))
                    .thenReturn(template);
            when(executionCodeGenerator.generateCode()).thenReturn("EXC20260315001");

            InspectionStep emptyStep = new InspectionStep();
            emptyStep.setId("step-empty");
            emptyStep.setTemplateId("tpl-001");
            emptyStep.setStepName("Bước trống");
            emptyStep.setSortOrder(1);
            emptyStep.setIsMandatory(0);
            emptyStep.setRequiresApproval(0);

            when(inspectionStepMapper.selectList(any(QueryWrapper.class)))
                    .thenReturn(Collections.singletonList(emptyStep));
            when(stepFieldMapper.selectList(any(QueryWrapper.class)))
                    .thenReturn(Collections.emptyList());
            when(inspectionExecutionMapper.insert((InspectionExecution) any())).thenReturn(1);
            when(stepResultMapper.insert((StepResult) any())).thenReturn(1);

            InspectionExecutionDTO dto = createDTO("product-001", "iqc", null, null);
            InspectionExecutionVO result = inspectionExecutionService.createExecution(dto);

            assertThat(result.getSteps()).hasSize(1);
            assertThat(result.getSteps().get(0).getFields()).isEmpty();
            verify(fieldValueMapper, never()).insert((FieldValue) any());
        }
    }

    @Nested
    @DisplayName("createExecution - error scenarios")
    class CreateExecutionErrors {

        @Test
        @DisplayName("Throws TemplateNotFoundException when no template found")
        void throwsWhenNoTemplateFound() {
            when(templateResolutionService.resolveTemplate("product-999", "fqc"))
                    .thenThrow(new TemplateNotFoundException("product-999", "fqc"));

            InspectionExecutionDTO dto = createDTO("product-999", "fqc", null, null);

            assertThatThrownBy(() -> inspectionExecutionService.createExecution(dto))
                    .isInstanceOf(TemplateNotFoundException.class)
                    .hasMessageContaining("product-999")
                    .hasMessageContaining("fqc");
        }
    }

    // ==================== Helper Methods ====================

    private void setupMocks() {
        when(templateResolutionService.resolveTemplate("product-001", "pqc"))
                .thenReturn(template);
        when(executionCodeGenerator.generateCode()).thenReturn("EXC20260315001");

        when(inspectionStepMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(step1, step2));

        // First call for step1 fields, second call for step2 fields
        when(stepFieldMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Arrays.asList(field1, field2))
                .thenReturn(Collections.singletonList(field3));

        when(inspectionExecutionMapper.insert((InspectionExecution) any())).thenReturn(1);
        when(stepResultMapper.insert((StepResult) any())).thenReturn(1);
        when(fieldValueMapper.insert((FieldValue) any())).thenReturn(1);
    }

    private InspectionExecutionDTO createDTO(String productId, String stageType,
                                              String workOrderId, String productionStageId) {
        InspectionExecutionDTO dto = new InspectionExecutionDTO();
        dto.setProductId(productId);
        dto.setStageType(stageType);
        dto.setWorkOrderId(workOrderId);
        dto.setProductionStageId(productionStageId);
        return dto;
    }
}
