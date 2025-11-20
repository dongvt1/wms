<template>
  <div :class="`${prefixCls}`">
    <div class="content">
      <a-tabs :size="`small`" v-model:activeKey="activeKey">
        <a-tab-pane tab="Second" key="second" v-if="!hideSecond">
          <SecondUI v-model:value="second" :disabled="disabled" />
        </a-tab-pane>
        <a-tab-pane tab="point" key="minute">
          <MinuteUI v-model:value="minute" :disabled="disabled" />
        </a-tab-pane>
        <a-tab-pane tab="hour" key="hour">
          <HourUI v-model:value="hour" :disabled="disabled" />
        </a-tab-pane>
        <a-tab-pane tab="day" key="day">
          <DayUI v-model:value="day" :week="week" :disabled="disabled" />
        </a-tab-pane>
        <a-tab-pane tab="moon" key="month">
          <MonthUI v-model:value="month" :disabled="disabled" />
        </a-tab-pane>
        <a-tab-pane tab="week" key="week">
          <WeekUI v-model:value="week" :day="day" :disabled="disabled" />
        </a-tab-pane>
        <a-tab-pane tab="Year" key="year" v-if="!hideYear && !hideSecond">
          <YearUI v-model:value="year" :disabled="disabled" />
        </a-tab-pane>
      </a-tabs>
      <a-divider />
      <!-- 执行hour间预览 -->
      <div style="overflow: hidden">
        <a-row :gutter="8">
          <a-col :span="18" style="margin-top: 22px">
            <a-row :gutter="8">
              <a-col :span="8" style="margin-bottom: 12px">
                <a-input v-model:value="inputValues.second" @blur="onInputBlur">
                  <template #addonBefore>
                    <span class="allow-click" @click="activeKey = 'second'">Second</span>
                  </template>
                </a-input>
              </a-col>
              <a-col :span="8" style="margin-bottom: 12px">
                <a-input v-model:value="inputValues.minute" @blur="onInputBlur">
                  <template #addonBefore>
                    <span class="allow-click" @click="activeKey = 'minute'">point</span>
                  </template>
                </a-input>
              </a-col>
              <a-col :span="8" style="margin-bottom: 12px">
                <a-input v-model:value="inputValues.hour" @blur="onInputBlur">
                  <template #addonBefore>
                    <span class="allow-click" @click="activeKey = 'hour'">hour</span>
                  </template>
                </a-input>
              </a-col>
              <a-col :span="8" style="margin-bottom: 12px">
                <a-input v-model:value="inputValues.day" @blur="onInputBlur">
                  <template #addonBefore>
                    <span class="allow-click" @click="activeKey = 'day'">day</span>
                  </template>
                </a-input>
              </a-col>
              <a-col :span="8" style="margin-bottom: 12px">
                <a-input v-model:value="inputValues.month" @blur="onInputBlur">
                  <template #addonBefore>
                    <span class="allow-click" @click="activeKey = 'month'">moon</span>
                  </template>
                </a-input>
              </a-col>
              <a-col :span="8" style="margin-bottom: 12px">
                <a-input v-model:value="inputValues.week" @blur="onInputBlur">
                  <template #addonBefore>
                    <span class="allow-click" @click="activeKey = 'week'">week</span>
                  </template>
                </a-input>
              </a-col>
              <a-col :span="8">
                <a-input v-model:value="inputValues.year" @blur="onInputBlur">
                  <template #addonBefore>
                    <span class="allow-click" @click="activeKey = 'year'">Year</span>
                  </template>
                </a-input>
              </a-col>
              <a-col :span="16">
                <a-input v-model:value="inputValues.cron" @blur="onInputCronBlur">
                  <template #addonBefore>
                    <a-tooltip title="Cronexpression">Mode</a-tooltip>
                  </template>
                </a-input>
              </a-col>
            </a-row>
          </a-col>
          <a-col :span="6">
            <div>近十次执行hour间（不含Year）</div>
            <a-textarea type="textarea" :value="preTimeList" :rows="5" />
          </a-col>
        </a-row>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref, watch, provide } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import CronParser from 'cron-parser';
  import SecondUI from './tabs/SecondUI.vue';
  import MinuteUI from './tabs/MinuteUI.vue';
  import HourUI from './tabs/HourUI.vue';
  import DayUI from './tabs/DayUI.vue';
  import MonthUI from './tabs/MonthUI.vue';
  import WeekUI from './tabs/WeekUI.vue';
  import YearUI from './tabs/YearUI.vue';
  import { cronEmits, cronProps } from './easy.cron.data';
  import { dateFormat, simpleDebounce } from '/@/utils/common/compUtils';

  const { prefixCls } = useDesign('easy-cron-inner');
  provide('prefixCls', prefixCls);
  const emit = defineEmits([...cronEmits]);
  const props = defineProps({ ...cronProps });
  const activeKey = ref(props.hideSecond ? 'minute' : 'second');
  const second = ref('*');
  const minute = ref('*');
  const hour = ref('*');
  const day = ref('*');
  const month = ref('*');
  const week = ref('?');
  const year = ref('*');
  const inputValues = reactive({
    second: '',
    minute: '',
    hour: '',
    day: '',
    month: '',
    week: '',
    year: '',
    cron: '',
  });
  const preTimeList = ref('Execute preview，会忽略Year份参数。');

  // cronexpression
  const cronValueInner = computed(() => {
    let result: string[] = [];
    if (!props.hideSecond) {
      result.push(second.value ? second.value : '*');
    }
    result.push(minute.value ? minute.value : '*');
    result.push(hour.value ? hour.value : '*');
    result.push(day.value ? day.value : '*');
    result.push(month.value ? month.value : '*');
    result.push(week.value ? week.value : '?');
    if (!props.hideYear && !props.hideSecond) result.push(year.value ? year.value : '*');
    return result.join(' ');
  });
  // 不含Year
  const cronValueNoYear = computed(() => {
    const v = cronValueInner.value;
    if (props.hideYear || props.hideSecond) return v;
    const vs = v.split(' ');
    if (vs.length >= 6) {
      // Convert to Quartz rules
      vs[5] = convertWeekToQuartz(vs[5]);
    }
    return vs.slice(0, vs.length - 1).join(' ');
  });
  const calTriggerList = simpleDebounce(calTriggerListInner, 500);

  watch(
    () => props.value,
    (newVal) => {
      if (newVal === cronValueInner.value) {
        return;
      }
      formatValue();
    }
  );

  watch(cronValueInner, (newValue) => {
    calTriggerList();
    emitValue(newValue);
    assignInput();
  });

  // watch(minute, () => {
  //   if (second.value === '*') {
  //     second.value = '0'
  //   }
  // })
  // watch(hour, () => {
  //   if (minute.value === '*') {
  //     minute.value = '0'
  //   }
  // })
  // watch(day, () => {
  //   if (day.value !== '?' && hour.value === '*') {
  //     hour.value = '0'
  //   }
  // })
  // watch(week, () => {
  //   if (week.value !== '?' && hour.value === '*') {
  //     hour.value = '0'
  //   }
  // })
  // watch(month, () => {
  //   if (day.value === '?' && week.value === '*') {
  //     week.value = '1'
  //   } else if (week.value === '?' && day.value === '*') {
  //     day.value = '1'
  //   }
  // })
  // watch(year, () => {
  //   if (month.value === '*') {
  //     month.value = '1'
  //   }
  // })

  assignInput();
  formatValue();
  calTriggerListInner();

  function assignInput() {
    inputValues.second = second.value;
    inputValues.minute = minute.value;
    inputValues.hour = hour.value;
    inputValues.day = day.value;
    inputValues.month = month.value;
    inputValues.week = week.value;
    inputValues.year = year.value;
    inputValues.cron = cronValueInner.value;
  }

  function formatValue() {
    if (!props.value) return;
    const values = props.value.split(' ').filter((item) => !!item);
    if (!values || values.length <= 0) return;
    let i = 0;
    if (!props.hideSecond) second.value = values[i++];
    if (values.length > i) minute.value = values[i++];
    if (values.length > i) hour.value = values[i++];
    if (values.length > i) day.value = values[i++];
    if (values.length > i) month.value = values[i++];
    if (values.length > i) week.value = values[i++];
    if (values.length > i) year.value = values[i];
    assignInput();
  }

  // Quartz rules：
  // 1 = weekday，2 = week一，3 = week二，4 = week三，5 = week四，6 = week五，7 = week六
  function convertWeekToQuartz(week: string) {
    let convert = (v: string) => {
      if (v === '0') {
        return '1';
      }
      if (v === '1') {
        return '0';
      }
      return (Number.parseInt(v) - 1).toString();
    };
    // Match example 1-7 or 1/7
    let patten1 = /^([0-7])([-/])([0-7])$/;
    // Match example 1,4,7
    let patten2 = /^([0-7])(,[0-7])+$/;
    if (/^[0-7]$/.test(week)) {
      return convert(week);
    } else if (patten1.test(week)) {
      return week.replace(patten1, ($0, before, separator, after) => {
        if (separator === '/') {
          return convert(before) + separator + after;
        } else {
          return convert(before) + separator + convert(after);
        }
      });
    } else if (patten2.test(week)) {
      return week
        .split(',')
        .map((v) => convert(v))
        .join(',');
    }
    return week;
  }

  function calTriggerListInner() {
    // Callback function set
    if (props.remote) {
      props.remote(cronValueInner.value, +new Date(), (v) => {
        preTimeList.value = v;
      });
      return;
    }
    const format = 'yyyy-MM-dd hh:mm:ss';
    const options = {
      currentDate: dateFormat(new Date(), format),
    };
    const iter = CronParser.parseExpression(cronValueNoYear.value, options);
    const result: string[] = [];
    for (let i = 1; i <= 10; i++) {
      result.push(dateFormat(new Date(iter.next() as any), format));
    }
    preTimeList.value = result.length > 0 ? result.join('\n') : '无执行hour间';
  }

  function onInputBlur() {
    second.value = inputValues.second;
    minute.value = inputValues.minute;
    hour.value = inputValues.hour;
    day.value = inputValues.day;
    month.value = inputValues.month;
    week.value = inputValues.week;
    year.value = inputValues.year;
  }

  function onInputCronBlur(event) {
    emitValue(event.target.value);
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }
</script>
<style lang="less">
  @import 'easy.cron.inner';
</style>
