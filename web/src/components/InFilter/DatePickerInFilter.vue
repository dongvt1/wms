<!-- Date picker to use in filtering（Can choose today、This week and other range） -->
<template>
  <a-space :id="formItemId" :class="[prefixCls]" direction="vertical">
    <a-space-compact block>
      <!-- Date range drop down -->
      <a-select v-if="isRange" v-model:value="innerValue">
        <a-select-option v-for="opt of RANGE_OPTIONS" :key="opt.key" :value="opt.key">
          {{ opt.label }}
        </a-select-option>
        <a-select-option key="custom" value="custom">
          Custom date
        </a-select-option>
      </a-select>
      <!-- Custom date选择器 -->
      <DatePicker v-else v-model:value="innerValue" v-model:open="datePickerIsOpen" v-bind="attrs"/>
      <!-- Right drop-down menu -->
      <a-dropdown v-if="allowSelectRange" :trigger="['click']">
        <a-button preIcon="ant-design:menu-unfold"/>
        <template #overlay>
          <a-menu @click="onMenuClick">
            <a-menu-item v-for="opt of RANGE_OPTIONS" :key="opt.key">
              {{ opt.label }}
            </a-menu-item>
            <a-menu-item key="custom">
              Custom date
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </a-space-compact>
  </a-space>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: 'DatePickerInFilter',
  inheritAttrs: false,
})
</script>

<script lang="ts" setup>
import {ref, watch, computed, nextTick, useAttrs, defineProps} from 'vue'
import {DatePicker} from 'ant-design-vue'
import {useDesign} from '/@/hooks/web/useDesign';
import { Form } from 'ant-design-vue';
const formItemContext = Form.useInjectFormItemContext();

// Date range options
const RANGE_OPTIONS = [
  {key: 'TODAY', label: 'today'},
  {key: 'YESTERDAY', label: 'yesterday'},
  {key: 'TOMORROW', label: 'tomorrow'},
  {key: 'THIS_WEEK', label: 'this week'},
  {key: 'LAST_WEEK', label: 'last week'},
  {key: 'NEXT_WEEK', label: 'next week'},
  {key: 'LAST_7_DAYS', label: 'past seven days'},
  {key: 'THIS_MONTH', label: 'this month'},
  {key: 'LAST_MONTH', label: 'last month'},
  {key: 'NEXT_MONTH', label: 'next month'},
];
const RANGE_OPTION_KEYS = RANGE_OPTIONS.map(item => item.key)

const {prefixCls} = useDesign('j-data-picker-in-filter');
const props = defineProps({
  value: {
    type: [String],
    default: ''
  },
  // Whether to allow selection of a preset range
  allowSelectRange: {
    type: Boolean,
    default: true,
  }
})
const emit = defineEmits(['change', 'update:value'])
const attrs = useAttrs()

// componentsid
const formItemId  = computed(() => formItemContext.id.value)

const innerValue = ref(props.value)
// Whether to open the date picker
const datePickerIsOpen = ref(false)

// Determine whether it is a range option
const isRange = computed(() => RANGE_OPTION_KEYS.includes(innerValue.value));

// synchronousvalue
watch(() => props.value, (val) => {
  innerValue.value = val
})

// emit Change
watch(innerValue, (val) => {
  if (val === 'custom') {
    val = ''
    openDatePicker()
  }
  emit('change', val)
  emit('update:value', val)
  // update-begin--author:liaozhiyang---date:20240509---for：【QQYUN-9227】Date verification is not cleared
  formItemContext?.onFieldChange();
  // update-end--author:liaozhiyang---date:20240509---for：【QQYUN-9227】Date verification is not cleared
})

watch(() => props.allowSelectRange, (allow) => {
  // If range selection is not allowed，and the current value is the range option，then clear the value
  if (!allow && isRange.value) {
    innerValue.value = ''
  }
}, {immediate: true});

// 点击Right drop-down menu
function onMenuClick(event: Recordable) {
  if (event.key === 'custom') {
    if (isRange.value) {
      innerValue.value = '';
    }
    openDatePicker()
  } else {
    innerValue.value = event.key
  }
}

// Open date picker dropdown
async function openDatePicker() {
  await nextTick()
  datePickerIsOpen.value = true
}

</script>

<style lang="less">
//noinspection LessUnresolvedVariable
@prefix-cls: ~'@{namespace}-j-data-picker-in-filter';

.@{prefix-cls} {
  width: 100%;
}
</style>
