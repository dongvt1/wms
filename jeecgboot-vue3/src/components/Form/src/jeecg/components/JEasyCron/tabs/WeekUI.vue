<template>
  <div :class="`${prefixCls}-config-list`">
    <a-radio-group v-model:value="type">
      <div class="item">
        <a-radio :value="TypeEnum.unset" v-bind="beforeRadioAttrs">Not set</a-radio>
        <span class="tip-info">Only one of day and week can be set</span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.range" v-bind="beforeRadioAttrs">interval</a-radio>
        <span> from </span>
        <a-select v-model:value="valueRange.start" :options="weekOptions" v-bind="typeRangeSelectAttrs" />
        <span> to </span>
        <a-select v-model:value="valueRange.end" :options="weekOptions" v-bind="typeRangeSelectAttrs" />
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.loop" v-bind="beforeRadioAttrs">cycle</a-radio>
        <span> from </span>
        <a-select v-model:value="valueLoop.start" :options="weekOptions" v-bind="typeLoopSelectAttrs" />
        <span> start，interval </span>
        <InputNumber v-model:value="valueLoop.interval" v-bind="typeLoopAttrs" />
        <span> sky </span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.specify" v-bind="beforeRadioAttrs">Specify</a-radio>
        <div class="list list-cn">
          <a-checkbox-group v-model:value="valueList">
            <template v-for="opt in weekOptions" :key="i">
              <a-checkbox :value="opt.value" v-bind="typeSpecifyAttrs">{{ opt.label }}</a-checkbox>
            </template>
          </a-checkbox-group>
        </div>
      </div>
    </a-radio-group>
  </div>
</template>

<script lang="ts">
  import { computed, watch, defineComponent } from 'vue';
  import { InputNumber } from 'ant-design-vue';
  import { useTabProps, useTabEmits, useTabSetup, TypeEnum } from './useTabMixin';

  const WEEK_MAP_EN = {
    '1': 'SUN',
    '2': 'MON',
    '3': 'TUE',
    '4': 'WED',
    '5': 'THU',
    '6': 'FRI',
    '7': 'SAT',
  };

  const WEEK_MAP_CN = {
    '1': 'Sunday',
    '2': 'on Monday',
    '3': 'Tuesday',
    '4': 'Wednesday',
    '5': 'Thursday',
    '6': 'Friday',
    '7': 'Saturday',
  };

  export default defineComponent({
    name: 'WeekUI',
    components: { InputNumber },
    props: useTabProps({
      defaultValue: '?',
      props: {
        day: { type: String, default: '*' },
      },
    }),
    emits: useTabEmits(),
    setup(props, context) {
      const disabledChoice = computed(() => {
        return (props.day && props.day !== '?') || props.disabled;
      });
      const setup = useTabSetup(props, context, {
        defaultType: TypeEnum.unset,
        defaultValue: '?',
        minValue: 1,
        maxValue: 7,
        // 0,7表示Sunday 1表示on Monday
        valueRange: { start: 1, end: 7 },
        valueLoop: { start: 2, interval: 1 },
        disabled: disabledChoice,
      });
      const weekOptions = computed(() => {
        let options: { label: string; value: number }[] = [];
        for (let weekKey of Object.keys(WEEK_MAP_CN)) {
          let weekName: string = WEEK_MAP_CN[weekKey];
          options.push({
            value: Number.parseInt(weekKey),
            label: weekName,
          });
        }
        return options;
      });

      const typeRangeSelectAttrs = computed(() => ({
        class: ['w80'],
        disabled: setup.typeRangeAttrs.value.disabled,
      }));

      const typeLoopSelectAttrs = computed(() => ({
        class: ['w80'],
        disabled: setup.typeLoopAttrs.value.disabled,
      }));

      watch(
        () => props.day,
        () => {
          setup.updateValue(disabledChoice.value ? '?' : setup.computeValue.value);
        }
      );

      return {
        ...setup,
        weekOptions,
        typeLoopSelectAttrs,
        typeRangeSelectAttrs,
        WEEK_MAP_CN,
        WEEK_MAP_EN,
      };
    },
  });
</script>
