<template>
  <div class="p-4 inspection-execution-form">
    <a-page-header
      :title="executionData?.executionCode || 'Phiên kiểm tra'"
      :sub-title="executionData?.templateName"
      @back="handleBack"
    >
      <template #extra>
        <a-tag :color="statusColor">{{ statusLabel }}</a-tag>
        <a-button
          :loading="savingDraft"
          :disabled="isReadOnly"
          @click="handleSaveDraft"
        >
          <SaveOutlined /> Lưu nháp
        </a-button>
        <a-button
          type="primary"
          :loading="submitting"
          :disabled="!canSubmitExecution"
          @click="handleSubmitExecution"
        >
          <CheckCircleOutlined /> Nộp kết quả
        </a-button>
      </template>
    </a-page-header>

    <a-spin :spinning="loading">
      <a-row :gutter="24">
        <!-- Left: Steps navigation -->
        <a-col :span="6">
          <a-card title="Các bước kiểm tra" size="small" class="steps-nav-card">
            <a-steps
              direction="vertical"
              :current="currentStepIndex"
              size="small"
              @change="handleStepChange"
            >
              <a-step
                v-for="(step, idx) in steps"
                :key="step.id"
                :title="step.stepName"
                :status="getStepStatus(step, idx)"
                :disabled="isStepDisabled(idx)"
                :description="getStepDescription(step)"
              />
            </a-steps>
          </a-card>
        </a-col>

        <!-- Right: Current step fields -->
        <a-col :span="18">
          <a-card v-if="currentStep" :title="currentStepTitle" class="step-content-card">
            <template #extra>
              <a-space>
                <a-tag v-if="currentStep.isMandatory" color="red">Bắt buộc</a-tag>
                <a-tag v-else color="default">Tùy chọn</a-tag>
                <a-tag v-if="currentStep.result === 'pass'" color="green">PASS</a-tag>
                <a-tag v-else-if="currentStep.result === 'fail'" color="red">FAIL</a-tag>
              </a-space>
            </template>

            <!-- Fields rendering -->
            <div class="step-fields">
              <a-form layout="vertical">
                <div
                  v-for="field in currentStepFields"
                  :key="field.fieldId"
                  class="field-item"
                >
                  <FieldRenderer
                    :field="field"
                    :disabled="isReadOnly"
                    :value="getFieldValue(field.fieldId)"
                    @update:value="(val) => setFieldValue(field.fieldId, val)"
                  />
                </div>
              </a-form>
            </div>

            <!-- Step actions -->
            <a-divider />
            <div class="step-actions">
              <a-space>
                <a-button
                  :disabled="currentStepIndex === 0"
                  @click="handlePrevStep"
                >
                  <LeftOutlined /> Bước trước
                </a-button>
                <a-button
                  type="primary"
                  :loading="submittingStep"
                  :disabled="isReadOnly || !canSubmitCurrentStep"
                  @click="handleSubmitStep"
                >
                  <CheckOutlined /> Hoàn thành bước này
                </a-button>
                <a-button
                  :disabled="!canGoNext"
                  @click="handleNextStep"
                >
                  Bước tiếp <RightOutlined />
                </a-button>
              </a-space>
            </div>
          </a-card>

          <a-empty v-else description="Chọn một bước kiểm tra để bắt đầu" />
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    SaveOutlined,
    CheckCircleOutlined,
    CheckOutlined,
    LeftOutlined,
    RightOutlined,
  } from '@ant-design/icons-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    wmsInspectionExecutionApi,
    type InspectionExecutionModel,
    type StepResultModel,
    type FieldValueModel,
    type FieldValueDTO,
  } from '/@/api/wms/inspectionExecution';
  import FieldRenderer from './components/fieldExecution/FieldRenderer.vue';

  const route = useRoute();
  const router = useRouter();
  const { createMessage, createConfirm } = useMessage();

  // --- State ---
  const loading = ref(false);
  const savingDraft = ref(false);
  const submitting = ref(false);
  const submittingStep = ref(false);
  const currentStepIndex = ref(0);
  const executionData = ref<InspectionExecutionModel | null>(null);

  /** Local field values map: fieldId → value string */
  const fieldValues = reactive<Record<string, string | null>>({});

  // --- Computed ---
  const steps = computed<StepResultModel[]>(() => {
    if (!executionData.value) return [];
    return [...executionData.value.steps].sort((a, b) => a.sortOrder - b.sortOrder);
  });

  const currentStep = computed<StepResultModel | null>(() => {
    return steps.value[currentStepIndex.value] || null;
  });

  const currentStepTitle = computed(() => {
    if (!currentStep.value) return '';
    return `Bước ${currentStepIndex.value + 1}: ${currentStep.value.stepName}`;
  });

  const currentStepFields = computed<FieldValueModel[]>(() => {
    if (!currentStep.value) return [];
    return [...(currentStep.value.fields || [])].sort(
      (a, b) => (a.sortOrder || 0) - (b.sortOrder || 0)
    );
  });

  const isReadOnly = computed(() => {
    if (!executionData.value) return true;
    const status = executionData.value.status;
    return status === 'pending_approval' || status === 'approved' || status === 'rejected';
  });

  const statusColor = computed(() => {
    const map: Record<string, string> = {
      draft: 'default',
      in_progress: 'processing',
      pending_approval: 'warning',
      approved: 'success',
      rejected: 'error',
    };
    return map[executionData.value?.status || ''] || 'default';
  });

  const statusLabel = computed(() => {
    const map: Record<string, string> = {
      draft: 'Nháp',
      in_progress: 'Đang thực hiện',
      pending_approval: 'Chờ phê duyệt',
      approved: 'Đã phê duyệt',
      rejected: 'Bị từ chối',
    };
    return map[executionData.value?.status || ''] || executionData.value?.status || '';
  });

  /** Check if all mandatory steps are completed */
  const canSubmitExecution = computed(() => {
    if (isReadOnly.value) return false;
    if (!executionData.value) return false;
    const mandatorySteps = steps.value.filter((s) => s.isMandatory);
    return mandatorySteps.every((s) => s.status === 'completed' || s.status === 'approved');
  });

  /** Check if current step has all required fields filled */
  const canSubmitCurrentStep = computed(() => {
    if (!currentStep.value) return false;
    if (currentStep.value.status === 'completed' || currentStep.value.status === 'approved') {
      return false;
    }
    const requiredFields = currentStepFields.value.filter((f) => f.isRequired);
    return requiredFields.every((f) => {
      const val = getFieldValue(f.fieldId);
      return val !== null && val !== undefined && val !== '';
    });
  });

  /** Can navigate to next step - current step must be completed for sequential enforcement */
  const canGoNext = computed(() => {
    if (currentStepIndex.value >= steps.value.length - 1) return false;
    const current = currentStep.value;
    if (!current) return false;
    // Allow navigation if current step is completed or if it's optional
    return current.status === 'completed' || current.status === 'approved' || !current.isMandatory;
  });

  // --- Methods ---
  function getFieldValue(fieldId: string): string | null {
    return fieldValues[fieldId] ?? null;
  }

  function setFieldValue(fieldId: string, value: string | null) {
    fieldValues[fieldId] = value;
  }

  function getStepStatus(step: StepResultModel, idx: number): 'wait' | 'process' | 'finish' | 'error' {
    if (step.result === 'fail') return 'error';
    if (step.status === 'completed' || step.status === 'approved') return 'finish';
    if (idx === currentStepIndex.value) return 'process';
    return 'wait';
  }

  function getStepDescription(step: StepResultModel): string {
    if (step.result === 'pass') return '✓ Đạt';
    if (step.result === 'fail') return '✗ Không đạt';
    if (step.status === 'completed') return 'Hoàn thành';
    if (step.status === 're_inspect') return 'Cần kiểm tra lại';
    return step.isMandatory ? 'Bắt buộc' : 'Tùy chọn';
  }

  /** Sequential enforcement: disable steps that come after an incomplete mandatory step */
  function isStepDisabled(idx: number): boolean {
    if (isReadOnly.value) return false; // Allow viewing all steps in read-only mode
    if (idx === 0) return false;
    // Check all previous mandatory steps are completed
    for (let i = 0; i < idx; i++) {
      const prevStep = steps.value[i];
      if (prevStep.isMandatory && prevStep.status !== 'completed' && prevStep.status !== 'approved') {
        return true;
      }
    }
    return false;
  }

  function handleStepChange(idx: number) {
    if (!isStepDisabled(idx)) {
      currentStepIndex.value = idx;
    }
  }

  function handlePrevStep() {
    if (currentStepIndex.value > 0) {
      currentStepIndex.value--;
    }
  }

  function handleNextStep() {
    if (canGoNext.value) {
      currentStepIndex.value++;
    }
  }

  // --- API calls ---
  async function loadExecution(id: string) {
    loading.value = true;
    try {
      const data = await wmsInspectionExecutionApi.queryById(id);
      if (data) {
        executionData.value = data;
        // Populate local field values from loaded data
        for (const step of data.steps || []) {
          for (const field of step.fields || []) {
            fieldValues[field.fieldId] = field.actualValue;
          }
        }
        // Auto-select first incomplete step
        const firstIncomplete = steps.value.findIndex(
          (s) => s.status !== 'completed' && s.status !== 'approved'
        );
        if (firstIncomplete >= 0) {
          currentStepIndex.value = firstIncomplete;
        }
      }
    } catch (e: any) {
      createMessage.error('Không thể tải dữ liệu phiên kiểm tra');
    } finally {
      loading.value = false;
    }
  }

  /** Collect all field values for save draft */
  function collectAllFieldValues(): FieldValueDTO[] {
    const values: FieldValueDTO[] = [];
    for (const step of steps.value) {
      for (const field of step.fields || []) {
        values.push({
          fieldId: field.fieldId,
          value: fieldValues[field.fieldId] ?? null,
        });
      }
    }
    return values;
  }

  /** Collect field values for current step */
  function collectCurrentStepValues(): FieldValueDTO[] {
    if (!currentStep.value) return [];
    return (currentStep.value.fields || []).map((field) => ({
      fieldId: field.fieldId,
      value: fieldValues[field.fieldId] ?? null,
    }));
  }

  async function handleSaveDraft() {
    if (!executionData.value) return;
    savingDraft.value = true;
    try {
      const values = collectAllFieldValues();
      await wmsInspectionExecutionApi.saveDraft(executionData.value.id, { values });
      createMessage.success('Đã lưu nháp thành công');
    } catch (e: any) {
      // Global error handler will show error
    } finally {
      savingDraft.value = false;
    }
  }

  async function handleSubmitStep() {
    if (!executionData.value || !currentStep.value) return;
    submittingStep.value = true;
    try {
      const values = collectCurrentStepValues();
      const result = await wmsInspectionExecutionApi.submitStepValues(
        executionData.value.id,
        currentStep.value.stepId,
        { values }
      );
      // Update local step data with evaluation results
      if (result && currentStep.value) {
        currentStep.value.status = 'completed';
        currentStep.value.result = result.stepResult;
        // Update field results
        for (const fieldResult of result.fieldResults || []) {
          const field = currentStep.value.fields?.find((f) => f.fieldId === fieldResult.fieldId);
          if (field) {
            field.result = fieldResult.result;
            field.evalMessage = fieldResult.message || null;
          }
        }
      }
      if (result?.stepResult === 'pass') {
        createMessage.success('Bước kiểm tra ĐẠT');
      } else if (result?.stepResult === 'fail') {
        createMessage.warning('Bước kiểm tra KHÔNG ĐẠT');
      } else {
        createMessage.info('Đã lưu kết quả bước kiểm tra');
      }
    } catch (e: any) {
      // Global error handler
    } finally {
      submittingStep.value = false;
    }
  }

  async function handleSubmitExecution() {
    if (!executionData.value) return;
    createConfirm({
      iconType: 'info',
      title: 'Xác nhận nộp kết quả',
      content: 'Bạn có chắc chắn muốn nộp kết quả kiểm tra để phê duyệt? Sau khi nộp, bạn không thể chỉnh sửa.',
      onOk: async () => {
        submitting.value = true;
        try {
          await wmsInspectionExecutionApi.submit(executionData.value!.id);
          createMessage.success('Đã nộp kết quả kiểm tra thành công');
          // Reload to get updated status
          await loadExecution(executionData.value!.id);
        } catch (e: any) {
          // Global error handler
        } finally {
          submitting.value = false;
        }
      },
    });
  }

  function handleBack() {
    router.back();
  }

  // --- Init ---
  onMounted(() => {
    const id = route.params.id as string;
    if (id) {
      loadExecution(id);
    }
  });
</script>

<style scoped>
  .inspection-execution-form {
    max-width: 1400px;
    margin: 0 auto;
  }

  .steps-nav-card {
    position: sticky;
    top: 16px;
  }

  .steps-nav-card :deep(.ant-steps-item) {
    cursor: pointer;
  }

  .steps-nav-card :deep(.ant-steps-item-disabled) {
    cursor: not-allowed;
    opacity: 0.5;
  }

  .step-content-card {
    min-height: 400px;
  }

  .field-item {
    margin-bottom: 16px;
    padding: 12px 16px;
    background: #fafafa;
    border-radius: 6px;
    border: 1px solid #f0f0f0;
  }

  .step-actions {
    display: flex;
    justify-content: center;
  }
</style>
