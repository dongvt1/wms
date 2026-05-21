<template>
  <a-row :gutter="16">
    <a-col :span="12">
      <a-form-item label="Nhãn khi Đạt (true)">
        <a-input
          v-model:value="config.trueLabel"
          placeholder="Đạt"
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
    <a-col :span="12">
      <a-form-item label="Nhãn khi Không đạt (false)">
        <a-input
          v-model:value="config.falseLabel"
          placeholder="Không đạt"
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup>
import { reactive, watch } from 'vue';
import type { BooleanFieldConfigData } from './types';

const props = defineProps<{
  modelValue: BooleanFieldConfigData;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: BooleanFieldConfigData): void;
}>();

const config = reactive<BooleanFieldConfigData>({
  trueLabel: props.modelValue?.trueLabel || 'Đạt',
  falseLabel: props.modelValue?.falseLabel || 'Không đạt',
});

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      config.trueLabel = val.trueLabel || 'Đạt';
      config.falseLabel = val.falseLabel || 'Không đạt';
    }
  },
  { deep: true }
);

function emitChange() {
  emit('update:modelValue', { ...config });
}
</script>
