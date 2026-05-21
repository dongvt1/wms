<template>
  <a-row :gutter="16">
    <a-col :span="8">
      <a-form-item label="Giá trị tối thiểu">
        <a-input-number
          v-model:value="config.minValue"
          placeholder="Min"
          style="width: 100%"
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
    <a-col :span="8">
      <a-form-item label="Giá trị tối đa">
        <a-input-number
          v-model:value="config.maxValue"
          placeholder="Max"
          style="width: 100%"
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
    <a-col :span="8">
      <a-form-item label="Số thập phân">
        <a-input-number
          v-model:value="config.decimalPlaces"
          :min="0"
          :max="10"
          placeholder="2"
          style="width: 100%"
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup>
import { reactive, watch } from 'vue';
import type { NumberFieldConfigData } from './types';

const props = defineProps<{
  modelValue: NumberFieldConfigData;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: NumberFieldConfigData): void;
}>();

const config = reactive<NumberFieldConfigData>({
  minValue: props.modelValue?.minValue ?? null,
  maxValue: props.modelValue?.maxValue ?? null,
  decimalPlaces: props.modelValue?.decimalPlaces ?? 2,
});

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      config.minValue = val.minValue ?? null;
      config.maxValue = val.maxValue ?? null;
      config.decimalPlaces = val.decimalPlaces ?? 2;
    }
  },
  { deep: true }
);

function emitChange() {
  emit('update:modelValue', { ...config });
}
</script>
