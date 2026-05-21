<template>
  <div :class="`${prefixCls}-config-list`">
    <a-radio-group v-model:value="type">
      <div class="item">
        <a-radio :value="TypeEnum.unset" v-bind="beforeRadioAttrs">Not set</a-radio>
        <span class="tip-info">Only one of day and week can be set</span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.every" v-bind="beforeRadioAttrs">daily</a-radio>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.range" v-bind="beforeRadioAttrs">interval</a-radio>
        <span> from </span>
        <InputNumber v-model:value="valueRange.start" v-bind="typeRangeAttrs" />
        <span> day to </span>
        <InputNumber v-model:value="valueRange.end" v-bind="typeRangeAttrs" />
        <span> day </span>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.loop" v-bind="beforeRadioAttrs">cycle</a-radio>
        <span> from </span>
        <InputNumber v-model:value="valueLoop.start" v-bind="typeLoopAttrs" />
        <span> day开始，interval </span>
        <InputNumber v-model:value="valueLoop.interval" v-bind="typeLoopAttrs" />
        <span> day </span>
      </div>
<!--       工作day暂不支持，Will report an error，Hidden first -->
<!--      <div class="item">-->
<!--        <a-radio :value="TypeEnum.work" v-bind="beforeRadioAttrs">工作day</a-radio>-->
<!--        <span> this month </span>-->
<!--        <InputNumber v-model:value="valueWork" v-bind="typeWorkAttrs" />-->
<!--        <span> day，最近的工作day </span>-->
<!--      </div>-->
      <div class="item">
        <a-radio :value="TypeEnum.last" v-bind="beforeRadioAttrs">最后一day</a-radio>
      </div>
      <div class="item">
        <a-radio :value="TypeEnum.specify" v-bind="beforeRadioAttrs">Specify</a-radio>
        <div class="list">
          <a-checkbox-group v-model:value="valueList">
            <template v-for="i in specifyRange" :key="i">
              <a-checkbox :value="i" v-bind="typeSpecifyAttrs">{{ i }}</a-checkbox>
            </template>
          </a-checkbox-group>
        </div>
      </div>
    </a-radio-group>
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent, watch } from 'vue';
  import { InputNumber } from 'ant-design-vue';
  import { TypeEnum, useTabEmits, useTabProps, useTabSetup } from './useTabMixin';

  export default defineComponent({
    name: 'DayUI',
    components: { InputNumber },
    props: useTabProps({
      defaultValue: '*',
      props: {
        week: { type: String, default: '?' },
      },
    }),
    emits: useTabEmits(),
    setup(props, context) {
      const disabledChoice = computed(() => {
        return (props.week && props.week !== '?') || props.disabled;
      });
      const setup = useTabSetup(props, context, {
        defaultValue: '*',
        valueWork: 1,
        minValue: 1,
        maxValue: 31,
        valueRange: { start: 1, end: 31 },
        valueLoop: { start: 1, interval: 1 },
        disabled: disabledChoice,
      });
      const typeWorkAttrs = computed(() => ({
        disabled: setup.type.value !== TypeEnum.work || props.disabled || disabledChoice.value,
        ...setup.inputNumberAttrs.value,
      }));

      watch(
        () => props.week,
        () => {
          setup.updateValue(disabledChoice.value ? '?' : setup.computeValue.value);
        }
      );

      return { ...setup, typeWorkAttrs };
    },
  });
</script>
