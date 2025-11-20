<!--Dictionary drop-down multiple selection-->
<template>
  <a-select
    :value="arrayValue"
    @change="onChange"
    mode="multiple"
    :filter-option="filterOption"
    :disabled="disabled"
    :placeholder="placeholder"
    allowClear
    :getPopupContainer="getParentContainer"
  >
    <a-select-option v-for="(item, index) in dictOptions" :key="index" :getPopupContainer="getParentContainer" :value="item.value">
      <span :class="[useDicColor && item.color ? 'colorText' : '']" :style="{ backgroundColor: `${useDicColor && item.color}` }">{{ item.text || item.label }}</span>
    </a-select-option>
  </a-select>
</template>
<script lang="ts">
  import { computed, defineComponent, onMounted, ref, nextTick, watch } from 'vue';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { getDictItems } from '/@/api/common/api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { setPopContainer } from '/@/utils';

  const { createMessage, createErrorModal } = useMessage();
  export default defineComponent({
    name: 'JSelectMultiple',
    components: {},
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      placeholder: {
        type: String,
        default: 'Please select',
        required: false,
      },
      readOnly: {
        type: Boolean,
        required: false,
        default: false,
      },
      options: {
        type: Array,
        default: () => [],
        required: false,
      },
      triggerChange: {
        type: Boolean,
        required: false,
        default: true,
      },
      spliter: {
        type: String,
        required: false,
        default: ',',
      },
      popContainer: {
        type: String,
        default: '',
        required: false,
      },
      dictCode: {
        type: String,
        required: false,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      useDicColor: {
        type: Boolean,
        default: false,
      },
    },
    emits: ['options-change', 'change', 'input', 'update:value'],
    setup(props, { emit, refs }) {
      //console.info(props);
      const emitData = ref<any[]>([]);
      const arrayValue = ref<any[]>(!props.value ? [] : props.value.split(props.spliter));
      const dictOptions = ref<any[]>([]);
      const attrs = useAttrs();
      const [state, , , formItemContext] = useRuleFormItem(props, 'value', 'change', emitData);

      onMounted(() => {
        if (props.dictCode) {
          loadDictOptions();
        } else {
          dictOptions.value = props.options;
        }
      });

      watch(
          () => props.dictCode,
          () => {
            if (props.dictCode) {
              loadDictOptions();
            } else {
              dictOptions.value = props.options;
            }
          }
      );

      watch(
        () => props.value,
        (val) => {
          if (!val) {
            arrayValue.value = [];
          } else {
            arrayValue.value = props.value.split(props.spliter);
          }
        }
      );

      //Applicable to Dynamically change drop-down options
      watch(()=>props.options, ()=>{
        if (props.dictCode) {
          // nothing to do
        } else {
          dictOptions.value = props.options;
        }
      });

      function onChange(selectedValue) {
        if (props.triggerChange) {
          emit('change', selectedValue.join(props.spliter));
          emit('update:value', selectedValue.join(props.spliter));
        } else {
          emit('input', selectedValue.join(props.spliter));
          emit('update:value', selectedValue.join(props.spliter));
        }
        // update-begin--author:liaozhiyang---date:20240429---for：【QQYUN-9110】The component has value verification and has not disappeared.
        nextTick(() => {
          formItemContext?.onFieldChange();
        });
        // update-end--author:liaozhiyang---date:20240429---for：【QQYUN-9110】The component has value verification and has not disappeared.
      }

      function getParentContainer(node) {
        if (!props.popContainer) {
          return node?.parentNode;
        } else {
          // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9339】There are multiplemodalWhen there are drop-down dictionary multi-select and drop-down search components in the pop-up window，open anothermodaltime componentoptionsDo not display
          return setPopContainer(node, props.popContainer);
          // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9339】There are multiplemodalWhen there are drop-down dictionary multi-select and drop-down search components in the pop-up window，open anothermodaltime componentoptionsDo not display
        }
      }

      // according to dictionarycodeQuery dictionary item
      function loadDictOptions() {
        //update-begin-author:taoyan date:2022-6-21 for: Encode parameters before requesting dictionary data，But it cannot be directly encoded，Because it may have been encoded before
        let temp = props.dictCode || '';
        if (temp.indexOf(',') > 0 && temp.indexOf(' ') > 0) {
          // After encoding does not contain spaces
          temp = encodeURI(temp);
        }
        //update-end-author:taoyan date:2022-6-21 for: Encode parameters before requesting dictionary data，But it cannot be directly encoded，Because it may have been encoded before
        getDictItems(temp).then((res) => {
          if (res) {
            dictOptions.value = res.map((item) => ({ value: item.value, label: item.text, color:item.color }));
            //console.info('res', dictOptions.value);
          } else {
            console.error('getDictItems error: : ', res);
            dictOptions.value = [];
          }
        });
      }

      //update-begin-author:taoyan date:2022-5-31 for: VUEN-1145 Drop-down multiple selection，When searching，No data found
      function filterOption(input, option) {
        return option.children()[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      }
      //update-end-author:taoyan date:2022-5-31 for: VUEN-1145 Drop-down multiple selection，When searching，No data found

      return {
        state,
        attrs,
        dictOptions,
        onChange,
        arrayValue,
        getParentContainer,
        filterOption,
      };
    },
  });
</script>
<style scoped lang='less'>
.colorText{
  display: inline-block;
    height: 20px;
    line-height: 20px;
    padding: 0 6px;
    border-radius: 8px;
    background-color: red;
    color: #fff;
    font-size: 12px;
}
</style>
