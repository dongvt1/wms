<template>
  <a-radio-group v-if="compType === CompTypeEnum.Radio" v-bind="attrs" v-model:value="state" @change="handleChangeRadio">
    <template v-for="item in dictOptions" :key="`${item.value}`">
      <a-radio :value="item.value">
        <span :class="[useDicColor && item.color ? 'colorText' : '']" :style="{ backgroundColor: `${useDicColor && item.color}` }">
          {{ item.label }}
        </span>
      </a-radio>
    </template>
  </a-radio-group>

  <a-radio-group
    v-else-if="compType === CompTypeEnum.RadioButton"
    v-bind="attrs"
    v-model:value="state"
    buttonStyle="solid"
    @change="handleChangeRadio"
  >
    <template v-for="item in dictOptions" :key="`${item.value}`">
      <a-radio-button :value="item.value">
        {{ item.label }}
      </a-radio-button>
    </template>
  </a-radio-group>

  <template v-else-if="compType === CompTypeEnum.Select">
    <!-- Show loading effect -->
    <a-input v-if="loadingEcho" readOnly placeholder="loading…">
      <template #prefix>
        <LoadingOutlined />
      </template>
    </a-input>
    <a-select
      v-else
      :placeholder="placeholder"
      v-bind="attrs"
      v-model:value="state"
      :filterOption="handleFilterOption"
      :getPopupContainer="getPopupContainer"
      :style="style"
      @change="handleChange"
    >
      <a-select-option v-if="showChooseOption" :value="null">Please select…</a-select-option>
      <template v-for="item in dictOptions" :key="`${item.value}`">
        <a-select-option :value="item.value">
          <span
            :class="[useDicColor && item.color ? 'colorText' : '']"
            :style="{ backgroundColor: `${useDicColor && item.color}` }"
            :title="item.label"
          >
            {{ item.label }}
          </span>
        </a-select-option>
      </template>
    </a-select>
  </template>
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted, nextTick } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { initDictOptions } from '/@/utils/dict';
  import { get, omit } from 'lodash-es';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { CompTypeEnum } from '/@/enums/CompTypeEnum';
  import { LoadingOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'JDictSelectTag',
    inheritAttrs: false,
    components: { LoadingOutlined },
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.number, propTypes.array]),
      dictCode: propTypes.string,
      type: propTypes.string,
      placeholder: propTypes.string,
      stringToNumber: propTypes.bool,
      useDicColor: propTypes.bool.def(false),
      getPopupContainer: {
        type: Function,
        default: (node) => node?.parentNode,
      },
      // Whether to display【Please select】Options
      showChooseOption: propTypes.bool.def(true),
      // drop down item-onlineuse
      options: {
        type: Array,
        default: [],
        required: false,
      },
      style: propTypes.any,
      // Whether to search onlylabel
      onlySearchByLabel: propTypes.bool.def(false),
    },
    emits: ['options-change', 'change','update:value'],
    setup(props, { emit, refs }) {
      const dictOptions = ref<any[]>([]);
      const attrs = useAttrs();
      const [state, , , formItemContext] = useRuleFormItem(props, 'value', 'change');
      const getBindValue = Object.assign({}, unref(props), unref(attrs));
      // Whether the echo data is being loaded
      const loadingEcho = ref<boolean>(false);
      // Whether it is the first time to load the echo，Only first load，will be displayed loading
      let isFirstLoadEcho = true;

      //Component type
      const compType = computed(() => {
        return !props.type || props.type === 'list' ? 'select' : props.type;
      });
      /**
       * listening dictionarycode
       */
      watchEffect(() => {
        if (props.dictCode) {
          loadingEcho.value = isFirstLoadEcho;
          isFirstLoadEcho = false;
          initDictData().finally(() => {
            loadingEcho.value = isFirstLoadEcho;
          });
        }
        //update-begin-author:taoyan date: if not provideddictCode can gooptionsconfiguration--
        if (!props.dictCode) {
          dictOptions.value = props.options;
        }
        //update-end-author:taoyan date: if not provideddictCode can gooptionsconfiguration--
      });

      //update-begin-author:taoyan date:20220404 for: useuseRuleFormItemdefinedvalue，there will be a problem，If it is not the value set by the operation but the control value set by the code, it cannot be triggered.changeevent
      // Add null value herechangeevent,That is, when the component calls the code settingsvaluefor''Can also triggerchangeevent
      watch(
        () => props.value,
        () => {
          if (props.value === '') {
            emit('change', '');
            nextTick(() => formItemContext.onFieldChange());
          }
        }
      );
      //update-end-author:taoyan date:20220404 for: useuseRuleFormItemdefinedvalue，there will be a problem，If it is not the value set by the operation but the control value set by the code, it cannot be triggered.changeevent

      async function initDictData() {
        let { dictCode, stringToNumber } = props;
        //according to dictionaryCode, Initialize dictionary array
        const dictData = await initDictOptions(dictCode);
        dictOptions.value = dictData.reduce((prev, next) => {
          if (next) {
            const value = next['value'];
            prev.push({
              label: next['text'] || next['label'],
              value: stringToNumber ? +value : value,
              color: next['color'],
              ...omit(next, ['text', 'value', 'color']),
            });
          }
          return prev;
        }, []);
      }

      function handleChange(e) {
        const { mode } = unref<Recordable>(getBindValue);
        let changeValue:any;
        // Compatible with multiple selection mode
        
        //update-begin---author:wangshuai ---date:20230216  for：[QQYUN-4290]Issuance of official documents：Report error when selecting agency code,是因for值改变触发Got itchangeevent三次，cause data to change------------
        //take a value，otherwisestateValue transformation triggers multiplechange
        if (mode === 'multiple') {
          changeValue = e?.target?.value ?? e;
          // Filter out null values
          if (changeValue == null || changeValue === '') {
            changeValue = [];
          }
          if (Array.isArray(changeValue)) {
            changeValue = changeValue.filter((item) => item != null && item !== '');
          }
        } else {
          changeValue = e?.target?.value ?? e;
        }
        state.value = changeValue;

        //update-begin---author:wangshuai ---date:20230403  for：【issues/4507】JDictSelectTagcomponentsuse时，Browser gives warning prompt：Expected Function, got Array------------
        emit('update:value',changeValue)
        //update-end---author:wangshuai ---date:20230403  for：【issues/4507】JDictSelectTagcomponentsuse时，Browser gives warning prompt：Expected Function, got Arraydescribe------------
        //update-end---author:wangshuai ---date:20230216  for：[QQYUN-4290]Issuance of official documents：Report error when selecting agency code,是因for值改变触发Got itchangeevent三次，cause data to change------------
        
        // nextTick(() => formItemContext.onFieldChange());
      }

      /** Single choiceradio的值变化event */
      function handleChangeRadio(e) {
        state.value = e?.target?.value ?? e;
        //update-begin---author:wangshuai ---date:20230504  for：【issues/506】JDictSelectTag components type="radio" no return value------------
        emit('update:value',e?.target?.value ?? e)
        //update-end---author:wangshuai ---date:20230504  for：【issues/506】JDictSelectTag components type="radio" no return value------------
      }

      /** Used to search the contents of the drop-down box */
      function handleFilterOption(input, option) {
        // update-begin--author:liaozhiyang---date:20230914---for：【QQYUN-6514】 When configuring，YAxis cannot enter multiple fields.，Console error
        if (typeof option.children === 'function') {
          // exist label Search in
          let labelIf = option.children()[0]?.children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
          if (labelIf) {
            return true;
          }
        }
        // update-end--author:liaozhiyang---date:20230914---for：【QQYUN-6514】 When configuring，YAxis cannot enter multiple fields.，Console error
        if (props.onlySearchByLabel) {
          // 如果开启Got it只exist label Search in，Don’t continue searchingvalueGot it
          return false;
        }
        // exist value Search in
        return (option.value || '').toString().toLowerCase().indexOf(input.toLowerCase()) >= 0;
      }

      return {
        state,
        compType,
        attrs,
        loadingEcho,
        getBindValue,
        dictOptions,
        CompTypeEnum,
        handleChange,
        handleChangeRadio,
        handleFilterOption,
      };
    },
  });
</script>
<style scoped lang="less">
  // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】字典components（原生components除外）Add color configuration
  .colorText {
    display: inline-block;
    height: 20px;
    line-height: 20px;
    padding: 0 6px;
    border-radius: 8px;
    background-color: red;
    color: #fff;
    font-size: 12px;
  }
  // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】字典components（原生components除外）Add color configuration
</style>
