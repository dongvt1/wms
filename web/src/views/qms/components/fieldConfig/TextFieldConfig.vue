<template>
  <a-row :gutter="16">
    <a-col :span="8">
      <a-form-item label="Placeholder">
        <a-input
          v-model:value="config.placeholder"
          placeholder="Gợi ý nhập liệu..."
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
    <a-col :span="8">
      <a-form-item label="Độ dài tối đa">
        <a-input-number
          v-model:value="config.maxLength"
          :min="1"
          :max="10000"
          placeholder="500"
          style="width: 100%"
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
    <a-col :span="8">
      <a-form-item label="Nhiều dòng">
        <a-switch
          v-model:checked="config.multiline"
          checked-children="Textarea"
          un-checked-children="Input"
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup>
import { reactive, watch } from 'vue';
import type { TextFieldConfigData } from './types';

const props = defineProps<{
  modelValue: TextFieldConfigData;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: TextFieldConfigData): void;
}>();

const config = reactive<TextFieldConfigData>({
  placeholder: props.modelValue?.placeholder || '',
  maxLength: props.modelValue?.maxLength ?? null,
  multiline: props.modelValue?.multiline || false,
});

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      config.placeholder = val.placeholder || '';
      config.maxLength = val.maxLength ?? null;
      config.multiline = val.multiline || false;
    }
  },
  { deep: true }
);

function emitChange() {
  emit('update:modelValue', { ...config });
}
</script>
