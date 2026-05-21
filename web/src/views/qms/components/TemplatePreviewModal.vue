<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="Xem trước mẫu kiểm tra"
    width="900px"
    :footer="null"
    :destroyOnClose="true"
  >
    <a-spin :spinning="loading">
      <template v-if="previewData">
        <!-- Template info header -->
        <a-descriptions bordered size="small" :column="3" class="mb-4">
          <a-descriptions-item label="Tên template">{{ previewData.templateName }}</a-descriptions-item>
          <a-descriptions-item label="Loại QC">
            <a-tag :color="stageTypeColor(previewData.stageType)">
              {{ stageTypeLabel(previewData.stageType) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="Phiên bản">{{ previewData.version }}</a-descriptions-item>
        </a-descriptions>

        <!-- Overall result -->
        <div v-if="hasAnyValue" class="overall-result mb-4">
          <a-alert
            :type="overallResult === 'pass' ? 'success' : overallResult === 'fail' ? 'error' : 'info'"
            show-icon
          >
            <template #message>
              <span v-if="overallResult === 'pass'">✅ Kết quả tổng thể: ĐẠT (PASS)</span>
              <span v-else-if="overallResult === 'fail'">❌ Kết quả tổng thể: KHÔNG ĐẠT (FAIL)</span>
              <span v-else>📝 Nhập dữ liệu thử để xem kết quả đánh giá</span>
            </template>
          </a-alert>
        </div>

        <!-- Steps -->
        <a-collapse v-model:activeKey="activeStepKeys" class="preview-steps">
          <a-collapse-panel
            v-for="(step, stepIdx) in previewData.steps"
            :key="step.id || `step_${stepIdx}`"
            :forceRender="true"
          >
            <template #header>
              <div class="step-header">
                <span class="step-order">Bước {{ stepIdx + 1 }}</span>
                <span class="step-name">{{ step.stepName }}</span>
                <a-tag v-if="step.isMandatory" color="red" size="small">Bắt buộc</a-tag>
                <a-tag v-if="step.requiresApproval" color="blue" size="small">Cần phê duyệt</a-tag>
                <!-- Step result indicator -->
                <a-tag
                  v-if="getStepResult(stepIdx) !== 'pending'"
                  :color="getStepResult(stepIdx) === 'pass' ? 'green' : 'red'"
                  size="small"
                  class="ml-2"
                >
                  {{ getStepResult(stepIdx) === 'pass' ? '✅ PASS' : '❌ FAIL' }}
                </a-tag>
              </div>
            </template>

            <p v-if="step.description" class="step-description">{{ step.description }}</p>

            <!-- Fields -->
            <div
              v-for="(field, fieldIdx) in step.fields"
              :key="field.id || `field_${stepIdx}_${fieldIdx}`"
              class="field-row"
            >
              <a-row :gutter="16" align="middle">
                <a-col :span="6">
                  <span class="field-label">
                    {{ field.fieldName }}
                    <span v-if="field.isRequired" class="required-mark">*</span>
                  </span>
                  <div v-if="field.hint" class="field-hint">{{ field.hint }}</div>
                </a-col>
                <a-col :span="10">
                  <!-- Text field -->
                  <template v-if="field.fieldType === 'text'">
                    <a-textarea
                      v-if="field.fieldConfig?.multiline"
                      v-model:value="fieldValues[stepIdx][fieldIdx]"
                      :placeholder="field.fieldConfig?.placeholder || 'Nhập văn bản...'"
                      :maxlength="field.fieldConfig?.maxLength || undefined"
                      :rows="3"
                      @change="evaluateAll"
                    />
                    <a-input
                      v-else
                      v-model:value="fieldValues[stepIdx][fieldIdx]"
                      :placeholder="field.fieldConfig?.placeholder || 'Nhập văn bản...'"
                      :maxlength="field.fieldConfig?.maxLength || undefined"
                      @change="evaluateAll"
                    />
                  </template>

                  <!-- Number field -->
                  <template v-else-if="field.fieldType === 'number'">
                    <a-input-number
                      v-model:value="fieldValues[stepIdx][fieldIdx]"
                      :min="field.fieldConfig?.minValue ?? undefined"
                      :max="field.fieldConfig?.maxValue ?? undefined"
                      :precision="field.fieldConfig?.decimalPlaces ?? undefined"
                      :placeholder="`Nhập số${getRangeHint(field)}`"
                      style="width: 100%"
                      @change="evaluateAll"
                    />
                    <div v-if="hasRange(field)" class="tolerance-hint">
                      Giới hạn: [{{ field.fieldConfig?.minValue ?? '−∞' }}, {{ field.fieldConfig?.maxValue ?? '+∞' }}]
                      {{ field.unit ? `(${field.unit})` : '' }}
                    </div>
                  </template>

                  <!-- Boolean field -->
                  <template v-else-if="field.fieldType === 'boolean'">
                    <a-switch
                      v-model:checked="fieldValues[stepIdx][fieldIdx]"
                      :checked-children="field.fieldConfig?.trueLabel || 'Đạt'"
                      :un-checked-children="field.fieldConfig?.falseLabel || 'Không đạt'"
                      @change="evaluateAll"
                    />
                  </template>

                  <!-- Select field -->
                  <template v-else-if="field.fieldType === 'select'">
                    <a-select
                      v-model:value="fieldValues[stepIdx][fieldIdx]"
                      :placeholder="'Chọn giá trị...'"
                      style="width: 100%"
                      allowClear
                      @change="evaluateAll"
                    >
                      <a-select-option
                        v-for="opt in (field.fieldConfig?.options || [])"
                        :key="opt"
                        :value="opt"
                      >
                        {{ opt }}
                      </a-select-option>
                    </a-select>
                  </template>

                  <!-- Measurement field -->
                  <template v-else-if="field.fieldType === 'measurement'">
                    <a-input-number
                      v-model:value="fieldValues[stepIdx][fieldIdx]"
                      :placeholder="`Giá trị đo (${field.unit || ''})`"
                      style="width: 100%"
                      @change="evaluateAll"
                    />
                    <div class="tolerance-hint">
                      Danh nghĩa: <strong>{{ field.fieldConfig?.nominalValue }}</strong>
                      {{ field.unit || '' }} |
                      Dung sai: [{{ field.fieldConfig?.lowerTolerance }}, {{ field.fieldConfig?.upperTolerance }}]
                      {{ field.unit || '' }}
                    </div>
                  </template>
                </a-col>
                <a-col :span="8">
                  <!-- Evaluation result -->
                  <div v-if="fieldResults[stepIdx]?.[fieldIdx]" class="eval-result">
                    <a-tag
                      :color="fieldResults[stepIdx][fieldIdx].result === 'pass' ? 'green' : fieldResults[stepIdx][fieldIdx].result === 'fail' ? 'red' : 'default'"
                    >
                      {{ fieldResults[stepIdx][fieldIdx].result === 'pass' ? '✅ PASS' : fieldResults[stepIdx][fieldIdx].result === 'fail' ? '❌ FAIL' : '—' }}
                    </a-tag>
                    <span v-if="fieldResults[stepIdx][fieldIdx].message" class="eval-message">
                      {{ fieldResults[stepIdx][fieldIdx].message }}
                    </span>
                  </div>
                </a-col>
              </a-row>
            </div>
          </a-collapse-panel>
        </a-collapse>

        <!-- Reset button -->
        <div class="mt-4 text-center">
          <a-button @click="resetAll">
            Xóa dữ liệu thử
          </a-button>
        </div>
      </template>

      <a-empty v-else-if="!loading" description="Không có dữ liệu template" />
    </a-spin>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { wmsInspectionTemplateApi } from '/@/api/wms/inspectionTemplate';

  interface FieldEvalResult {
    result: 'pass' | 'fail' | 'na';
    message?: string;
  }

  const loading = ref(false);
  const previewData = ref<any>(null);
  const fieldValues = reactive<any[][]>([]);
  const fieldResults = reactive<FieldEvalResult[][]>([]);
  const activeStepKeys = ref<string[]>([]);

  const [registerModal] = useModalInner(async (data) => {
    resetState();
    if (data?.id) {
      await loadPreview(data.id);
    } else if (data?.template) {
      // Support passing template data directly (for preview from form without saving)
      initFromTemplate(JSON.parse(JSON.stringify(data.template)));
    }
  });

  // --- Load preview data from API ---
  async function loadPreview(id: string) {
    loading.value = true;
    try {
      const res = await wmsInspectionTemplateApi.preview(id);
      if (res) {
        initFromTemplate(res);
      }
    } catch (e: any) {
      // Error handled by global interceptor
    } finally {
      loading.value = false;
    }
  }

  // --- Initialize from template data (isolated copy) ---
  function initFromTemplate(template: any) {
    // Deep clone to ensure isolation from editing state
    previewData.value = JSON.parse(JSON.stringify(template));

    // Sort steps by sortOrder
    if (previewData.value.steps) {
      previewData.value.steps.sort((a: any, b: any) => (a.sortOrder || 0) - (b.sortOrder || 0));
      // Sort fields within each step
      previewData.value.steps.forEach((step: any) => {
        if (step.fields) {
          step.fields.sort((a: any, b: any) => (a.sortOrder || 0) - (b.sortOrder || 0));
        }
      });
    }

    // Initialize field values and results arrays
    initFieldArrays();

    // Expand all steps by default
    activeStepKeys.value = (previewData.value.steps || []).map(
      (step: any, idx: number) => step.id || `step_${idx}`
    );
  }

  function initFieldArrays() {
    fieldValues.length = 0;
    fieldResults.length = 0;

    const steps = previewData.value?.steps || [];
    for (let i = 0; i < steps.length; i++) {
      const fields = steps[i].fields || [];
      const stepValues: any[] = [];
      const stepResults: FieldEvalResult[] = [];
      for (let j = 0; j < fields.length; j++) {
        const field = fields[j];
        // Initialize with appropriate default
        if (field.fieldType === 'boolean') {
          stepValues.push(undefined);
        } else {
          stepValues.push(undefined);
        }
        stepResults.push({ result: 'na' });
      }
      fieldValues.push(stepValues);
      fieldResults.push(stepResults);
    }
  }

  // --- Evaluation logic (client-side, matching backend EvaluationService) ---

  function evaluateAll() {
    const steps = previewData.value?.steps || [];
    for (let i = 0; i < steps.length; i++) {
      const fields = steps[i].fields || [];
      for (let j = 0; j < fields.length; j++) {
        fieldResults[i][j] = evaluateField(fields[j], fieldValues[i][j]);
      }
    }
  }

  function evaluateField(field: any, value: any): FieldEvalResult {
    // If no value entered, return N/A
    if (value === undefined || value === null || value === '') {
      return { result: 'na' };
    }

    const config = field.fieldConfig || {};

    switch (field.fieldType) {
      case 'measurement': {
        const numVal = Number(value);
        if (isNaN(numVal)) return { result: 'na', message: 'Giá trị không hợp lệ' };
        const lower = config.lowerTolerance;
        const upper = config.upperTolerance;
        if (lower != null && upper != null) {
          if (numVal >= lower && numVal <= upper) {
            return { result: 'pass', message: `Trong dung sai [${lower}, ${upper}]` };
          } else {
            return { result: 'fail', message: `Ngoài dung sai [${lower}, ${upper}], giá trị: ${numVal}` };
          }
        }
        return { result: 'na', message: 'Chưa cấu hình dung sai' };
      }

      case 'number': {
        const numVal = Number(value);
        if (isNaN(numVal)) return { result: 'na', message: 'Giá trị không hợp lệ' };
        const min = config.minValue;
        const max = config.maxValue;
        if (min != null && max != null) {
          if (numVal >= min && numVal <= max) {
            return { result: 'pass', message: `Trong giới hạn [${min}, ${max}]` };
          } else {
            return { result: 'fail', message: `Ngoài giới hạn [${min}, ${max}], giá trị: ${numVal}` };
          }
        } else if (min != null) {
          if (numVal >= min) {
            return { result: 'pass', message: `≥ ${min}` };
          } else {
            return { result: 'fail', message: `< ${min}, giá trị: ${numVal}` };
          }
        } else if (max != null) {
          if (numVal <= max) {
            return { result: 'pass', message: `≤ ${max}` };
          } else {
            return { result: 'fail', message: `> ${max}, giá trị: ${numVal}` };
          }
        }
        // No min/max configured - always pass
        return { result: 'pass', message: 'Không có giới hạn' };
      }

      case 'boolean': {
        // Boolean: true = pass, false = fail
        if (value === true) {
          return { result: 'pass', message: config.trueLabel || 'Đạt' };
        } else {
          return { result: 'fail', message: config.falseLabel || 'Không đạt' };
        }
      }

      case 'text':
      case 'select':
        // Text and Select: always pass (no auto-evaluation)
        return { result: 'pass', message: '' };

      default:
        return { result: 'na' };
    }
  }

  // --- Step-level evaluation ---
  function getStepResult(stepIdx: number): 'pass' | 'fail' | 'pending' {
    const steps = previewData.value?.steps || [];
    const fields = steps[stepIdx]?.fields || [];
    const results = fieldResults[stepIdx] || [];

    // Check if any required field has been evaluated
    let hasEvaluated = false;
    let allRequiredPass = true;

    for (let j = 0; j < fields.length; j++) {
      const field = fields[j];
      const result = results[j];
      if (!field.isRequired) continue;

      if (result && result.result !== 'na') {
        hasEvaluated = true;
        if (result.result === 'fail') {
          allRequiredPass = false;
        }
      }
    }

    if (!hasEvaluated) return 'pending';
    return allRequiredPass ? 'pass' : 'fail';
  }

  // --- Overall result ---
  const hasAnyValue = computed(() => {
    return fieldValues.some((stepVals) =>
      stepVals.some((v) => v !== undefined && v !== null && v !== '')
    );
  });

  const overallResult = computed<'pass' | 'fail' | 'pending'>(() => {
    if (!hasAnyValue.value) return 'pending';

    const steps = previewData.value?.steps || [];
    let hasEvaluated = false;
    let allMandatoryPass = true;

    for (let i = 0; i < steps.length; i++) {
      if (!steps[i].isMandatory) continue;
      const stepResult = getStepResult(i);
      if (stepResult !== 'pending') {
        hasEvaluated = true;
        if (stepResult === 'fail') {
          allMandatoryPass = false;
        }
      }
    }

    if (!hasEvaluated) return 'pending';
    return allMandatoryPass ? 'pass' : 'fail';
  });

  // --- Helpers ---
  function stageTypeColor(type: string) {
    const map: Record<string, string> = { iqc: 'blue', pqc: 'orange', fqc: 'green' };
    return map[type] || 'default';
  }

  function stageTypeLabel(type: string) {
    const map: Record<string, string> = { iqc: 'IQC', pqc: 'PQC', fqc: 'FQC' };
    return map[type] || type?.toUpperCase();
  }

  function hasRange(field: any): boolean {
    const config = field.fieldConfig || {};
    return config.minValue != null || config.maxValue != null;
  }

  function getRangeHint(field: any): string {
    const config = field.fieldConfig || {};
    if (config.minValue != null && config.maxValue != null) {
      return ` [${config.minValue} - ${config.maxValue}]`;
    }
    return '';
  }

  // --- Reset ---
  function resetState() {
    previewData.value = null;
    fieldValues.length = 0;
    fieldResults.length = 0;
    activeStepKeys.value = [];
  }

  function resetAll() {
    initFieldArrays();
  }

  defineExpose({ registerModal });
</script>

<style scoped>
  .preview-steps {
    border-radius: 6px;
  }

  .step-header {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .step-order {
    font-weight: 600;
    color: #1890ff;
    min-width: 50px;
  }

  .step-name {
    font-weight: 500;
  }

  .step-description {
    color: #666;
    font-size: 13px;
    margin-bottom: 12px;
    padding-left: 4px;
  }

  .field-row {
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;
  }

  .field-row:last-child {
    border-bottom: none;
  }

  .field-label {
    font-weight: 500;
    font-size: 13px;
  }

  .required-mark {
    color: #ff4d4f;
    margin-left: 2px;
  }

  .field-hint {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 2px;
  }

  .tolerance-hint {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 4px;
  }

  .eval-result {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .eval-message {
    font-size: 12px;
    color: #595959;
  }

  .overall-result {
    margin-top: 8px;
  }

  .ml-2 {
    margin-left: 8px;
  }

  .mb-4 {
    margin-bottom: 16px;
  }

  .mt-4 {
    margin-top: 16px;
  }

  .text-center {
    text-align: center;
  }
</style>
