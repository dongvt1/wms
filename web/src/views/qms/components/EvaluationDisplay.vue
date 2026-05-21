<template>
  <div v-if="visible" :class="['evaluation-display', resultClass]">
    <!-- Result badge -->
    <div class="eval-badge">
      <CheckCircleFilled v-if="isPass" class="eval-icon pass-icon" />
      <CloseCircleFilled v-else class="eval-icon fail-icon" />
      <span class="eval-label">{{ isPass ? 'PASS' : 'FAIL' }}</span>
    </div>

    <!-- Value vs limits -->
    <div v-if="showLimits" class="eval-details">
      <span class="actual-value">
        Giá trị: <strong>{{ formattedActualValue }}</strong>
        <span v-if="unit" class="unit">{{ unit }}</span>
      </span>
      <span class="limit-info">
        <template v-if="isMeasurement">
          Dung sai: [{{ lowerLimit }}, {{ upperLimit }}]
          <span v-if="unit" class="unit">{{ unit }}</span>
        </template>
        <template v-else-if="isNumber">
          Giới hạn: [{{ lowerLimit }}, {{ upperLimit }}]
          <span v-if="unit" class="unit">{{ unit }}</span>
        </template>
      </span>
    </div>

    <!-- Eval message from backend -->
    <div v-if="evalMessage" class="eval-message">
      {{ evalMessage }}
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { CheckCircleFilled, CloseCircleFilled } from '@ant-design/icons-vue';

  export interface EvaluationDisplayProps {
    /** Evaluation result: 'pass' or 'fail' */
    result: 'pass' | 'fail' | 'na' | null | undefined;
    /** Actual value entered by the inspector */
    actualValue?: string | number | null;
    /** Field configuration containing limits/tolerances */
    fieldConfig?: Record<string, any> | null;
    /** Field type: measurement, number, boolean, text, select */
    fieldType?: string;
    /** Unit of measurement */
    unit?: string;
    /** Evaluation message from backend */
    evalMessage?: string | null;
  }

  const props = withDefaults(defineProps<EvaluationDisplayProps>(), {
    result: null,
    actualValue: null,
    fieldConfig: null,
    fieldType: '',
    unit: '',
    evalMessage: null,
  });

  // --- Computed ---

  /** Only show when there's a definitive pass/fail result */
  const visible = computed(() => {
    return props.result === 'pass' || props.result === 'fail';
  });

  const isPass = computed(() => props.result === 'pass');

  const resultClass = computed(() => {
    return props.result === 'pass' ? 'eval-pass' : 'eval-fail';
  });

  const isMeasurement = computed(() => props.fieldType === 'measurement');
  const isNumber = computed(() => props.fieldType === 'number');

  const showLimits = computed(() => {
    if (!props.actualValue && props.actualValue !== 0) return false;
    if (isMeasurement.value) {
      const cfg = props.fieldConfig || {};
      return cfg.lowerTolerance != null && cfg.upperTolerance != null;
    }
    if (isNumber.value) {
      const cfg = props.fieldConfig || {};
      return cfg.minValue != null || cfg.maxValue != null;
    }
    return false;
  });

  const formattedActualValue = computed(() => {
    if (props.actualValue === null || props.actualValue === undefined) return '—';
    return String(props.actualValue);
  });

  const lowerLimit = computed(() => {
    const cfg = props.fieldConfig || {};
    if (isMeasurement.value) {
      return cfg.lowerTolerance ?? '—';
    }
    if (isNumber.value) {
      return cfg.minValue ?? '−∞';
    }
    return '—';
  });

  const upperLimit = computed(() => {
    const cfg = props.fieldConfig || {};
    if (isMeasurement.value) {
      return cfg.upperTolerance ?? '—';
    }
    if (isNumber.value) {
      return cfg.maxValue ?? '+∞';
    }
    return '—';
  });
</script>

<style scoped>
  .evaluation-display {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 6px 12px;
    border-radius: 6px;
    font-size: 13px;
    line-height: 1.4;
    flex-wrap: wrap;
  }

  .eval-pass {
    background-color: #f6ffed;
    border: 1px solid #b7eb8f;
  }

  .eval-fail {
    background-color: #fff2f0;
    border: 1px solid #ffccc7;
  }

  .eval-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-weight: 600;
    white-space: nowrap;
  }

  .eval-icon {
    font-size: 16px;
  }

  .pass-icon {
    color: #52c41a;
  }

  .fail-icon {
    color: #ff4d4f;
  }

  .eval-label {
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  .eval-pass .eval-label {
    color: #389e0d;
  }

  .eval-fail .eval-label {
    color: #cf1322;
  }

  .eval-details {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: #595959;
    font-size: 12px;
    flex-wrap: wrap;
  }

  .actual-value {
    white-space: nowrap;
  }

  .limit-info {
    white-space: nowrap;
    color: #8c8c8c;
  }

  .unit {
    color: #8c8c8c;
    font-size: 11px;
  }

  .eval-message {
    font-size: 12px;
    color: #595959;
    font-style: italic;
  }

  .eval-pass .eval-message {
    color: #389e0d;
  }

  .eval-fail .eval-message {
    color: #cf1322;
  }
</style>
