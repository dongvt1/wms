<template>
  <div class="p-4">
    <a-page-header
      :title="isEdit ? 'Chỉnh sửa mẫu kiểm tra' : 'Tạo mẫu kiểm tra mới'"
      @back="handleBack"
    >
      <template #extra>
        <a-button @click="handleBack">Hủy</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          <SaveOutlined /> Lưu template
        </a-button>
      </template>
    </a-page-header>

    <a-spin :spinning="loading">
      <!-- Template basic info -->
      <a-card title="Thông tin cơ bản" class="mb-4">
        <a-form
          ref="formRef"
          :model="formState"
          :rules="formRules"
          layout="vertical"
        >
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="Tên template" name="templateName">
                <a-input
                  v-model:value="formState.templateName"
                  placeholder="Nhập tên mẫu kiểm tra..."
                />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="Loại giai đoạn QC" name="stageType">
                <a-select
                  v-model:value="formState.stageType"
                  placeholder="Chọn loại QC..."
                >
                  <a-select-option value="iqc">IQC - Kiểm tra đầu vào</a-select-option>
                  <a-select-option value="pqc">PQC - Kiểm tra sản xuất</a-select-option>
                  <a-select-option value="fqc">FQC - Kiểm tra thành phẩm</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="4">
              <a-form-item label="Phiên bản" name="version">
                <a-input v-model:value="formState.version" placeholder="1.0" />
              </a-form-item>
            </a-col>
            <a-col :span="4">
              <a-form-item label="Trạng thái">
                <a-tag :color="statusColor(formState.status)">
                  {{ statusLabel(formState.status) }}
                </a-tag>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="Mô tả" name="description">
                <a-textarea
                  v-model:value="formState.description"
                  placeholder="Mô tả mẫu kiểm tra..."
                  :rows="2"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="Ghi chú" name="notes">
                <a-textarea
                  v-model:value="formState.notes"
                  placeholder="Ghi chú thêm..."
                  :rows="2"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-card>

      <!-- Steps configuration -->
      <a-card title="Các bước kiểm tra (Inspection Steps)" class="mb-4">
        <template #extra>
          <a-button type="primary" ghost @click="handleAddStep">
            <PlusOutlined /> Thêm bước
          </a-button>
        </template>

        <a-empty
          v-if="formState.steps.length === 0"
          description="Chưa có bước kiểm tra nào. Nhấn 'Thêm bước' để bắt đầu."
        />

        <draggable
          v-model="formState.steps"
          item-key="_uid"
          handle=".drag-handle"
          ghost-class="drag-ghost"
          @end="onDragEnd"
        >
          <template #item="{ element, index }">
            <a-collapse
              :activeKey="expandedSteps"
              @change="onCollapseChange"
              class="step-collapse"
            >
              <StepConfigPanel
                :step="element"
                :index="index"
                @remove="handleRemoveStep"
                @change="onStepChange"
              >
                <template #fields="{ step }">
                  <slot name="stepFields" :step="step" :stepIndex="index" />
                </template>
              </StepConfigPanel>
            </a-collapse>
          </template>
        </draggable>
      </a-card>
    </a-spin>
  </div>
</template>

<script lang="ts" name="inspection-template-form" setup>
  import { ref, reactive, onMounted, computed } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { SaveOutlined, PlusOutlined } from '@ant-design/icons-vue';
  import draggable from 'vuedraggable';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { wmsInspectionTemplateApi } from '/@/api/wms/inspectionTemplate';
  import StepConfigPanel from './StepConfigPanel.vue';
  import type { InspectionStepModel } from './StepConfigPanel.vue';

  const route = useRoute();
  const router = useRouter();
  const { createMessage } = useMessage();

  const formRef = ref();
  const loading = ref(false);
  const saving = ref(false);
  const expandedSteps = ref<string[]>([]);

  const isEdit = computed(() => route.params.id && route.params.id !== 'new');

  // --- Form state ---
  const formState = reactive<{
    id?: string;
    templateName: string;
    description: string;
    stageType: string | undefined;
    version: string;
    status: string;
    notes: string;
    steps: InspectionStepModel[];
  }>({
    templateName: '',
    description: '',
    stageType: undefined,
    version: '1.0',
    status: 'draft',
    notes: '',
    steps: [],
  });

  const formRules = {
    templateName: [{ required: true, message: 'Vui lòng nhập tên template', trigger: 'blur' }],
    stageType: [{ required: true, message: 'Vui lòng chọn loại giai đoạn QC', trigger: 'change' }],
    version: [{ required: true, message: 'Vui lòng nhập phiên bản', trigger: 'blur' }],
  };

  // --- Helpers ---
  let uidCounter = 0;
  function generateUid(): string {
    return `step_${Date.now()}_${++uidCounter}`;
  }

  function statusColor(status: string) {
    const map: Record<string, string> = { draft: 'default', active: 'green', obsolete: 'red' };
    return map[status] || 'default';
  }

  function statusLabel(status: string) {
    const map: Record<string, string> = { draft: 'Nháp', active: 'Đang dùng', obsolete: 'Lỗi thời' };
    return map[status] || status;
  }

  // --- Step management ---
  function handleAddStep() {
    const uid = generateUid();
    const newStep: InspectionStepModel = {
      _uid: uid,
      stepName: '',
      description: '',
      sortOrder: formState.steps.length + 1,
      isMandatory: true,
      requiresApproval: false,
      fields: [],
    };
    formState.steps.push(newStep);
    expandedSteps.value.push(uid);
  }

  function handleRemoveStep(index: number) {
    const removed = formState.steps.splice(index, 1);
    // Remove from expanded list
    if (removed[0]) {
      const key = removed[0].id || removed[0]._uid;
      expandedSteps.value = expandedSteps.value.filter((k) => k !== key);
    }
    // Recalculate sort orders
    recalculateSortOrders();
  }

  function onDragEnd() {
    recalculateSortOrders();
  }

  function recalculateSortOrders() {
    formState.steps.forEach((step, idx) => {
      step.sortOrder = idx + 1;
    });
  }

  function onCollapseChange(keys: string[] | string) {
    expandedSteps.value = Array.isArray(keys) ? keys : [keys];
  }

  function onStepChange() {
    // Placeholder for future reactivity needs (e.g., dirty tracking)
  }

  // --- Load existing template ---
  async function loadTemplate(id: string) {
    loading.value = true;
    try {
      const data = await wmsInspectionTemplateApi.queryById(id);
      if (data) {
        formState.id = data.id;
        formState.templateName = data.templateName || '';
        formState.description = data.description || '';
        formState.stageType = data.stageType;
        formState.version = data.version || '1.0';
        formState.status = data.status || 'draft';
        formState.notes = data.notes || '';
        formState.steps = (data.steps || []).map((s: any) => ({
          id: s.id,
          _uid: s.id || generateUid(),
          stepName: s.stepName || '',
          description: s.description || '',
          sortOrder: s.sortOrder,
          isMandatory: s.isMandatory ?? true,
          requiresApproval: s.requiresApproval ?? false,
          fields: s.fields || [],
        }));
        // Expand all steps by default when editing
        expandedSteps.value = formState.steps.map((s) => s.id || s._uid || '');
      }
    } catch (e: any) {
      createMessage.error('Không thể tải dữ liệu template');
    } finally {
      loading.value = false;
    }
  }

  // --- Save ---
  async function handleSave() {
    try {
      await formRef.value?.validate();
    } catch {
      createMessage.warning('Vui lòng điền đầy đủ thông tin bắt buộc');
      return;
    }

    // Validate steps have names
    const invalidSteps = formState.steps.filter((s) => !s.stepName?.trim());
    if (invalidSteps.length > 0) {
      createMessage.warning('Vui lòng nhập tên cho tất cả các bước kiểm tra');
      return;
    }

    saving.value = true;
    try {
      const payload = {
        templateName: formState.templateName,
        description: formState.description,
        stageType: formState.stageType,
        version: formState.version,
        notes: formState.notes,
        steps: formState.steps.map((step, idx) => ({
          id: step.id,
          stepName: step.stepName,
          description: step.description,
          sortOrder: idx + 1,
          isMandatory: step.isMandatory,
          requiresApproval: step.requiresApproval,
          fields: step.fields,
        })),
      };

      if (isEdit.value && formState.id) {
        await wmsInspectionTemplateApi.edit(formState.id, payload);
        createMessage.success('Cập nhật template thành công!');
      } else {
        await wmsInspectionTemplateApi.add(payload);
        createMessage.success('Tạo template thành công!');
      }
      router.back();
    } catch (e: any) {
      // Error handling is done by global interceptor (422 validation errors, etc.)
    } finally {
      saving.value = false;
    }
  }

  // --- Navigation ---
  function handleBack() {
    router.back();
  }

  // --- Init ---
  onMounted(() => {
    const id = route.params.id as string;
    if (id && id !== 'new') {
      loadTemplate(id);
    }
  });
</script>

<style scoped>
  .step-collapse {
    margin-bottom: 12px;
    border-radius: 6px;
    overflow: hidden;
  }

  .step-collapse :deep(.ant-collapse-header) {
    padding: 12px 16px !important;
    background: #fafafa;
  }

  .drag-ghost {
    opacity: 0.5;
    background: #e6f7ff;
    border: 1px dashed #1890ff;
  }
</style>
