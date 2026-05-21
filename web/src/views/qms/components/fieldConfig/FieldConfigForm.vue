<template>
  <div class="field-config-form">
    <a-card size="small" :bordered="true" class="field-card">
      <!-- Common field properties -->
      <a-row :gutter="16">
        <a-col :span="6">
          <a-form-item label="Tên trường" :required="true">
            <a-input
              v-model:value="fieldModel.fieldName"
              placeholder="Nhập tên trường..."
              @change="emitChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="4">
          <a-form-item label="Mã trường">
            <a-input
              v-model:value="fieldModel.fieldCode"
              placeholder="field_code"
              @change="emitChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="5">
          <a-form-item label="Kiểu dữ liệu" :required="true">
            <a-select
              v-model:value="fieldModel.fieldType"
              :options="FIELD_TYPE_OPTIONS"
              placeholder="Chọn kiểu..."
              @change="handleFieldTypeChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="3">
          <a-form-item label="Đơn vị">
            <a-input
              v-model:value="fieldModel.unit"
              placeholder="mm, kg..."
              @change="emitChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="3">
          <a-form-item label="Bắt buộc">
            <a-switch
              v-model:checked="fieldModel.isRequired"
              checked-children="Có"
              un-checked-children="Không"
              @change="emitChange"
            />
          </a-form-item>
        </a-col>
        <a-col :span="3">
          <a-form-item label=" " :colon="false">
            <a-button type="text" danger @click="$emit('remove')">
              <DeleteOutlined /> Xóa
            </a-button>
          </a-form-item>
        </a-col>
      </a-row>

      <!-- Hint -->
      <a-row :gutter="16">
        <a-col :span="24">
          <a-form-item label="Ghi chú hướng dẫn">
            <a-input
              v-model:value="fieldModel.hint"
              placeholder="Hướng dẫn nhập liệu cho nhân viên QC..."
              @change="emitChange"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- Dynamic config sub-form based on fieldType -->
      <a-divider v-if="fieldModel.fieldType" dashed style="margin: 8px 0 16px">
        <span class="config-divider-text">Cấu hình {{ fieldTypeLabel }}</span>
      </a-divider>

      <NumberFieldConfig
        v-if="fieldModel.fieldType === 'number'"
        v-model="fieldModel.fieldConfig"
        @update:model-value="handleConfigChange"
      />

      <MeasurementFieldConfig
        v-if="fieldModel.fieldType === 'measurement'"
        v-model="fieldModel.fieldConfig"
        :unit="fieldModel.unit"
        @update:model-value="handleConfigChange"
        @update:unit="handleUnitChange"
      />

      <SelectFieldConfig
        v-if="fieldModel.fieldType === 'select'"
        v-model="fieldModel.fieldConfig"
        @update:model-value="handleConfigChange"
      />

      <BooleanFieldConfig
        v-if="fieldModel.fieldType === 'boolean'"
        v-model="fieldModel.fieldConfig"
        @update:model-value="handleConfigChange"
      />

      <TextFieldConfig
        v-if="fieldModel.fieldType === 'text'"
        v-model="fieldModel.fieldConfig"
        @update:model-value="handleConfigChange"
      />
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { reactive, computed, watch } from 'vue';
import { DeleteOutlined } from '@ant-design/icons-vue';
import NumberFieldConfig from './NumberFieldConfig.vue';
import MeasurementFieldConfig from './MeasurementFieldConfig.vue';
import SelectFieldConfig from './SelectFieldConfig.vue';
import BooleanFieldConfig from './BooleanFieldConfig.vue';
import TextFieldConfig from './TextFieldConfig.vue';
import { FIELD_TYPE_OPTIONS, getDefaultConfig } from './types';
import type { StepFieldModel, FieldType, FieldConfigData } from './types';

const props = defineProps<{
  modelValue: StepFieldModel;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: StepFieldModel): void;
  (e: 'remove'): void;
}>();

const fieldModel = reactive<StepFieldModel>({
  id: props.modelValue?.id,
  fieldName: props.modelValue?.fieldName || '',
  fieldCode: props.modelValue?.fieldCode || '',
  fieldType: props.modelValue?.fieldType || ('' as FieldType),
  unit: props.modelValue?.unit || '',
  isRequired: props.modelValue?.isRequired ?? true,
  sortOrder: props.modelValue?.sortOrder || 0,
  hint: props.modelValue?.hint || '',
  defaultValue: props.modelValue?.defaultValue || '',
  fieldConfig: props.modelValue?.fieldConfig || {},
});

// Sync from parent when modelValue changes externally
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      fieldModel.id = val.id;
      fieldModel.fieldName = val.fieldName || '';
      fieldModel.fieldCode = val.fieldCode || '';
      fieldModel.fieldType = val.fieldType || ('' as FieldType);
      fieldModel.unit = val.unit || '';
      fieldModel.isRequired = val.isRequired ?? true;
      fieldModel.sortOrder = val.sortOrder || 0;
      fieldModel.hint = val.hint || '';
      fieldModel.defaultValue = val.defaultValue || '';
      fieldModel.fieldConfig = val.fieldConfig || {};
    }
  },
  { deep: true }
);

const fieldTypeLabel = computed(() => {
  const found = FIELD_TYPE_OPTIONS.find((o) => o.value === fieldModel.fieldType);
  return found?.label || '';
});

/**
 * When field type changes, clear incompatible config and set defaults for new type.
 * This implements Property 8: Field type change clears incompatible configuration.
 */
function handleFieldTypeChange(newType: FieldType) {
  fieldModel.fieldType = newType;
  // Clear old config and set new defaults
  fieldModel.fieldConfig = getDefaultConfig(newType);
  emitChange();
}

function handleConfigChange(newConfig: FieldConfigData) {
  fieldModel.fieldConfig = newConfig;
  emitChange();
}

function handleUnitChange(newUnit: string) {
  fieldModel.unit = newUnit;
  emitChange();
}

function emitChange() {
  emit('update:modelValue', { ...fieldModel, fieldConfig: { ...fieldModel.fieldConfig } });
}
</script>

<style scoped>
.field-config-form {
  margin-bottom: 12px;
}

.field-card {
  background: #fafbfc;
}

.field-card:hover {
  border-color: #1890ff;
}

.config-divider-text {
  font-size: 12px;
  color: #8c8c8c;
}
</style>
