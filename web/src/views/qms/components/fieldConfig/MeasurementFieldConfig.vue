<template>
  <a-row :gutter="16">
    <a-col :span="6">
      <a-form-item label="Giới hạn dưới">
        <a-input-number
          v-model:value="config.lowerTolerance"
          placeholder="Lower"
          style="width: 100%"
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
    <a-col :span="6">
      <a-form-item label="Giá trị danh nghĩa">
        <a-input-number
          v-model:value="config.nominalValue"
          placeholder="Nominal"
          style="width: 100%"
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
    <a-col :span="6">
      <a-form-item label="Giới hạn trên">
        <a-input-number
          v-model:value="config.upperTolerance"
          placeholder="Upper"
          style="width: 100%"
          @change="emitChange"
        />
      </a-form-item>
    </a-col>
    <a-col :span="6">
      <a-form-item label="Đơn vị đo">
        <a-input
          v-model:value="unitValue"
          placeholder="mm, kg, ..."
          @change="emitUnitChange"
        />
      </a-form-item>
    </a-col>
  </a-row>
  <div v-if="hasValidTolerance" class="tolerance-display">
    <a-tag color="blue">
      Dung sai: {{ config.lowerTolerance }} ≤ {{ config.nominalValue }} ≤ {{ config.upperTolerance }}
    </a-tag>
  </div>
</template>

<script lang="ts" setup>
import { reactive, computed, watch, ref } from 'vue';
import type { MeasurementFieldConfigData } from './types';

const props = defineProps<{
  modelValue: MeasurementFieldConfigData;
  unit?: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: MeasurementFieldConfigData): void;
  (e: 'update:unit', value: string): void;
}>();

const config = reactive<MeasurementFieldConfigData>({
  nominalValue: props.modelValue?.nominalValue ?? null,
  upperTolerance: props.modelValue?.upperTolerance ?? null,
  lowerTolerance: props.modelValue?.lowerTolerance ?? null,
});

const unitValue = ref(props.unit || '');

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      config.nominalValue = val.nominalValue ?? null;
      config.upperTolerance = val.upperTolerance ?? null;
      config.lowerTolerance = val.lowerTolerance ?? null;
    }
  },
  { deep: true }
);

watch(
  () => props.unit,
  (val) => {
    unitValue.value = val || '';
  }
);

const hasValidTolerance = computed(() => {
  return (
    config.lowerTolerance != null &&
    config.nominalValue != null &&
    config.upperTolerance != null
  );
});

function emitChange() {
  emit('update:modelValue', { ...config });
}

function emitUnitChange() {
  emit('update:unit', unitValue.value);
}
</script>

<style scoped>
.tolerance-display {
  margin-top: -8px;
  margin-bottom: 12px;
}
</style>
