<template>
  <div class="field-renderer">
    <a-form-item
      :label="fieldLabel"
      :required="field.isRequired"
      :validate-status="evaluationStatus"
      :help="evaluationHelp"
    >
      <!-- Text field -->
      <template v-if="field.fieldType === 'text'">
        <a-textarea
          v-if="textConfig.multiline"
          :value="value"
          :disabled="disabled"
          :placeholder="textConfig.placeholder || 'Nhập văn bản...'"
          :maxlength="textConfig.maxLength || undefined"
          :rows="3"
          :show-count="!!textConfig.maxLength"
          @update:value="emitValue"
        />
        <a-input
          v-else
          :value="value"
          :disabled="disabled"
          :placeholder="textConfig.placeholder || 'Nhập văn bản...'"
          :maxlength="textConfig.maxLength || undefined"
          @update:value="emitValue"
        />
      </template>

      <!-- Number field -->
      <template v-else-if="field.fieldType === 'number'">
        <div class="number-field-wrapper">
          <a-input-number
            :value="numericValue"
            :disabled="disabled"
            :min="numberConfig.minValue ?? undefined"
            :max="numberConfig.maxValue ?? undefined"
            :precision="numberConfig.decimalPlaces ?? undefined"
            placeholder="Nhập giá trị..."
            style="width: 200px"
            @update:value="handleNumberChange"
          />
          <span v-if="hasNumberRange" class="range-info">
            <a-tag color="blue" size="small">
              Phạm vi: {{ numberConfig.minValue }} ~ {{ numberConfig.maxValue }}
            </a-tag>
          </span>
        </div>
      </template>

      <!-- Measurement field -->
      <template v-else-if="field.fieldType === 'measurement'">
        <div class="measurement-field-wrapper">
          <a-input-number
            :value="numericValue"
            :disabled="disabled"
            placeholder="Nhập giá trị đo..."
            :precision="3"
            style="width: 200px"
            @update:value="handleNumberChange"
          />
          <span v-if="field.unit" class="unit-label">{{ field.unit }}</span>
          <div class="tolerance-info">
            <a-tooltip title="Giới hạn dưới">
              <a-tag color="orange" size="small">
                Min: {{ measurementConfig.lowerTolerance }}
              </a-tag>
            </a-tooltip>
            <a-tooltip title="Giá trị danh nghĩa">
              <a-tag color="blue" size="small">
                Nominal: {{ measurementConfig.nominalValue }}
              </a-tag>
            </a-tooltip>
            <a-tooltip title="Giới hạn trên">
              <a-tag color="orange" size="small">
                Max: {{ measurementConfig.upperTolerance }}
              </a-tag>
            </a-tooltip>
          </div>
        </div>
      </template>

      <!-- Boolean field -->
      <template v-else-if="field.fieldType === 'boolean'">
        <a-switch
          :checked="booleanValue"
          :disabled="disabled"
          :checked-children="booleanConfig.trueLabel || 'Đạt'"
          :un-checked-children="booleanConfig.falseLabel || 'Không đạt'"
          @update:checked="handleBooleanChange"
        />
      </template>

      <!-- Select field -->
      <template v-else-if="field.fieldType === 'select'">
        <a-select
          :value="value || undefined"
          :disabled="disabled"
          placeholder="Chọn giá trị..."
          style="width: 300px"
          allow-clear
          @update:value="emitValue"
        >
          <a-select-option
            v-for="opt in selectOptions"
            :key="opt"
            :value="opt"
          >
            {{ opt }}
          </a-select-option>
        </a-select>
      </template>

      <!-- Fallback -->
      <template v-else>
        <a-input
          :value="value"
          :disabled="disabled"
          placeholder="Nhập giá trị..."
          @update:value="emitValue"
        />
      </template>
    </a-form-item>

    <!-- Evaluation result display -->
    <div v-if="field.result && field.result !== 'na'" class="evaluation-result">
      <a-tag :color="field.result === 'pass' ? 'green' : 'red'" size="small">
        {{ field.result === 'pass' ? '✓ PASS' : '✗ FAIL' }}
      </a-tag>
      <span v-if="field.evalMessage" class="eval-message">
        {{ field.evalMessage }}
      </span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import type { FieldValueModel } from '/@/api/wms/inspectionExecution';
  import type {
    NumberFieldConfigData,
    MeasurementFieldConfigData,
    SelectFieldConfigData,
    BooleanFieldConfigData,
    TextFieldConfigData,
  } from '../fieldConfig/types';

  const props = defineProps<{
    field: FieldValueModel;
    value: string | null;
    disabled?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:value', value: string | null): void;
  }>();

  // --- Computed configs ---
  const fieldConfig = computed(() => props.field.fieldConfig || {});

  const textConfig = computed<TextFieldConfigData>(() => fieldConfig.value as TextFieldConfigData);
  const numberConfig = computed<NumberFieldConfigData>(() => fieldConfig.value as NumberFieldConfigData);
  const measurementConfig = computed<MeasurementFieldConfigData>(() => fieldConfig.value as MeasurementFieldConfigData);
  const booleanConfig = computed<BooleanFieldConfigData>(() => fieldConfig.value as BooleanFieldConfigData);
  const selectConfig = computed<SelectFieldConfigData>(() => fieldConfig.value as SelectFieldConfigData);

  const fieldLabel = computed(() => {
    let label = props.field.fieldName;
    if (props.field.unit && props.field.fieldType !== 'measurement') {
      label += ` (${props.field.unit})`;
    }
    return label;
  });

  const numericValue = computed(() => {
    if (props.value === null || props.value === undefined || props.value === '') return null;
    const num = parseFloat(props.value);
    return isNaN(num) ? null : num;
  });

  const booleanValue = computed(() => {
    if (props.value === null || props.value === undefined || props.value === '') return false;
    return props.value === 'true' || props.value === '1';
  });

  const selectOptions = computed<string[]>(() => {
    return selectConfig.value?.options || [];
  });

  const hasNumberRange = computed(() => {
    const cfg = numberConfig.value;
    return cfg.minValue != null || cfg.maxValue != null;
  });

  const evaluationStatus = computed<'' | 'success' | 'error'>(() => {
    if (!props.field.result || props.field.result === 'na') return '';
    return props.field.result === 'pass' ? 'success' : 'error';
  });

  const evaluationHelp = computed(() => {
    if (!props.field.evalMessage) return undefined;
    return props.field.evalMessage;
  });

  // --- Handlers ---
  function emitValue(val: string | null | undefined) {
    emit('update:value', val ?? null);
  }

  function handleNumberChange(val: number | null) {
    emit('update:value', val !== null && val !== undefined ? String(val) : null);
  }

  function handleBooleanChange(checked: boolean) {
    emit('update:value', String(checked));
  }
</script>

<style scoped>
  .field-renderer {
    position: relative;
  }

  .number-field-wrapper,
  .measurement-field-wrapper {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .range-info {
    display: inline-flex;
    align-items: center;
  }

  .tolerance-info {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    margin-top: 4px;
  }

  .unit-label {
    font-size: 13px;
    color: #666;
    font-weight: 500;
  }

  .evaluation-result {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: -8px;
    margin-bottom: 4px;
  }

  .eval-message {
    font-size: 12px;
    color: #666;
  }
</style>
