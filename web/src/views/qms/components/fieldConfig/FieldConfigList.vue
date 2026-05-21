<template>
  <div class="field-config-list">
    <draggable
      v-model="fields"
      item-key="sortOrder"
      handle=".field-drag-handle"
      ghost-class="field-ghost"
      @end="handleReorder"
    >
      <template #item="{ element, index }">
        <div class="field-item-wrapper">
          <div class="field-drag-handle">
            <HolderOutlined />
            <span class="field-index">{{ index + 1 }}</span>
          </div>
          <div class="field-item-content">
            <FieldConfigForm
              :model-value="element"
              @update:model-value="(val) => updateField(index, val)"
              @remove="removeField(index)"
            />
          </div>
        </div>
      </template>
    </draggable>

    <a-button type="dashed" block @click="addField">
      <PlusOutlined /> Thêm trường dữ liệu
    </a-button>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { PlusOutlined, HolderOutlined } from '@ant-design/icons-vue';
import draggable from 'vuedraggable';
import FieldConfigForm from './FieldConfigForm.vue';
import type { StepFieldModel, FieldType } from './types';

const props = defineProps<{
  modelValue: StepFieldModel[];
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: StepFieldModel[]): void;
}>();

const fields = ref<StepFieldModel[]>([...(props.modelValue || [])]);

watch(
  () => props.modelValue,
  (val) => {
    fields.value = [...(val || [])];
  },
  { deep: true }
);

function addField() {
  const newField: StepFieldModel = {
    fieldName: '',
    fieldCode: '',
    fieldType: '' as FieldType,
    unit: '',
    isRequired: true,
    sortOrder: fields.value.length + 1,
    hint: '',
    defaultValue: '',
    fieldConfig: {},
  };
  fields.value.push(newField);
  emitChange();
}

function removeField(index: number) {
  fields.value.splice(index, 1);
  // Recalculate sort orders
  fields.value.forEach((f, i) => {
    f.sortOrder = i + 1;
  });
  emitChange();
}

function updateField(index: number, val: StepFieldModel) {
  fields.value[index] = val;
  emitChange();
}

function handleReorder() {
  // Update sort_order after drag-and-drop
  fields.value.forEach((f, i) => {
    f.sortOrder = i + 1;
  });
  emitChange();
}

function emitChange() {
  emit('update:modelValue', [...fields.value]);
}
</script>

<style scoped>
.field-config-list {
  padding: 8px 0;
}

.field-item-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.field-drag-handle {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 4px;
  cursor: grab;
  color: #999;
  min-width: 28px;
}

.field-drag-handle:active {
  cursor: grabbing;
}

.field-index {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}

.field-item-content {
  flex: 1;
  min-width: 0;
}

.field-ghost {
  opacity: 0.4;
  background: #e6f7ff;
  border-radius: 6px;
}
</style>
