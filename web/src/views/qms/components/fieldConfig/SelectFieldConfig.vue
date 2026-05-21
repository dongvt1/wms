<template>
  <div class="select-field-config">
    <a-form-item label="Danh sách tùy chọn">
      <div class="options-list">
        <draggable
          v-model="optionsList"
          item-key="index"
          handle=".drag-handle"
          @end="emitChange"
        >
          <template #item="{ element, index }">
            <div class="option-item">
              <HolderOutlined class="drag-handle" />
              <a-input
                :value="element"
                placeholder="Nhập giá trị tùy chọn..."
                style="flex: 1"
                @input="(e: any) => updateOption(index, e.target.value)"
                @blur="emitChange"
              />
              <a-button
                type="text"
                danger
                size="small"
                :disabled="optionsList.length <= 1"
                @click="removeOption(index)"
              >
                <DeleteOutlined />
              </a-button>
            </div>
          </template>
        </draggable>
      </div>
      <a-button type="dashed" block class="mt-2" @click="addOption">
        <PlusOutlined /> Thêm tùy chọn
      </a-button>
    </a-form-item>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { PlusOutlined, DeleteOutlined, HolderOutlined } from '@ant-design/icons-vue';
import draggable from 'vuedraggable';
import type { SelectFieldConfigData } from './types';

const props = defineProps<{
  modelValue: SelectFieldConfigData;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: SelectFieldConfigData): void;
}>();

const optionsList = ref<string[]>([...(props.modelValue?.options || [''])]);

watch(
  () => props.modelValue,
  (val) => {
    if (val?.options) {
      optionsList.value = [...val.options];
    }
  },
  { deep: true }
);

function addOption() {
  optionsList.value.push('');
}

function removeOption(index: number) {
  optionsList.value.splice(index, 1);
  emitChange();
}

function updateOption(index: number, value: string) {
  optionsList.value[index] = value;
}

function emitChange() {
  emit('update:modelValue', { options: [...optionsList.value] });
}
</script>

<style scoped>
.options-list {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 8px;
  background: #fafafa;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.option-item + .option-item {
  border-top: 1px dashed #f0f0f0;
  padding-top: 8px;
  margin-top: 4px;
}

.drag-handle {
  cursor: grab;
  color: #999;
  font-size: 14px;
}

.drag-handle:active {
  cursor: grabbing;
}
</style>
