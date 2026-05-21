<template>
  <a-collapse-panel :key="step.id || step._uid" :forceRender="true">
    <template #header>
      <div class="step-panel-header">
        <HolderOutlined class="drag-handle" @mousedown.stop />
        <span class="step-order">Bước {{ index + 1 }}</span>
        <span class="step-name">{{ step.stepName || '(Chưa đặt tên)' }}</span>
        <a-tag v-if="step.isMandatory" color="red" size="small">Bắt buộc</a-tag>
        <a-tag v-if="step.requiresApproval" color="blue" size="small">Cần phê duyệt</a-tag>
      </div>
    </template>
    <template #extra>
      <a-popconfirm
        title="Xóa bước này sẽ xóa toàn bộ trường dữ liệu bên trong. Bạn chắc chắn?"
        ok-text="Xóa"
        cancel-text="Hủy"
        @confirm.stop="handleRemove"
      >
        <a-button type="link" danger size="small" @click.stop>
          <DeleteOutlined /> Xóa
        </a-button>
      </a-popconfirm>
    </template>

    <a-form layout="vertical" class="step-form">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="Tên bước kiểm tra" required>
            <a-input
              v-model:value="step.stepName"
              placeholder="Nhập tên bước kiểm tra..."
              @change="emitChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="Mô tả">
            <a-input
              v-model:value="step.description"
              placeholder="Mô tả bước kiểm tra..."
              @change="emitChange"
            />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="6">
          <a-form-item label="Bắt buộc">
            <a-switch
              v-model:checked="step.isMandatory"
              checked-children="Có"
              un-checked-children="Không"
              @change="emitChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="Cần phê duyệt">
            <a-switch
              v-model:checked="step.requiresApproval"
              checked-children="Có"
              un-checked-children="Không"
              @change="emitChange"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- Fields section -->
      <a-divider orientation="left" orientation-margin="0">
        <span style="font-size: 13px">Trường dữ liệu (Fields)</span>
      </a-divider>

      <div class="fields-section">
        <slot name="fields" :step="step" />
      </div>
    </a-form>
  </a-collapse-panel>
</template>

<script lang="ts" setup>
  import { HolderOutlined, DeleteOutlined } from '@ant-design/icons-vue';

  export interface InspectionStepModel {
    id?: string;
    _uid?: string;
    stepName: string;
    description: string;
    sortOrder: number;
    isMandatory: boolean;
    requiresApproval: boolean;
    fields: any[];
  }

  const props = defineProps<{
    step: InspectionStepModel;
    index: number;
  }>();

  const emit = defineEmits<{
    (e: 'remove', index: number): void;
    (e: 'change'): void;
  }>();

  function handleRemove() {
    emit('remove', props.index);
  }

  function emitChange() {
    emit('change');
  }
</script>

<style scoped>
  .step-panel-header {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .drag-handle {
    cursor: grab;
    color: #999;
    font-size: 14px;
  }

  .drag-handle:active {
    cursor: grabbing;
  }

  .step-order {
    font-weight: 600;
    color: #1890ff;
    min-width: 50px;
  }

  .step-name {
    font-weight: 500;
  }

  .step-form {
    padding: 8px 0;
  }

  .fields-section {
    min-height: 40px;
  }
</style>
