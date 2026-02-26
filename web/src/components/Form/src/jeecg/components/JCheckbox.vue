<template>
  <a-checkbox-group v-bind="attrs" v-model:value="checkboxArray" :options="checkOptions" @change="handleChange">
    <template #label="{label, value}">
      <span :class="[useDicColor && getDicColor(value) ? 'colorText' : '']" :style="{ backgroundColor: `${getDicColor(value)}` }">{{ label }}</span>
    </template>
  </a-checkbox-group>
</template>

<script lang="ts">
  import { defineComponent, computed, watch, watchEffect, ref, unref } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import {getDictItems} from "@/api/common/api";

  export default defineComponent({
    name: 'JCheckbox',
    props: {
      value:propTypes.oneOfType([propTypes.string, propTypes.number]),
      dictCode: propTypes.string,
      useDicColor: propTypes.bool.def(false),
      options: {
        type: Array,
        default: () => [],
      },
    },
    emits: ['change', 'update:value'],
    setup(props, { emit }) {
      const attrs = useAttrs();
      //checkboxOptions
      const checkOptions = ref<any[]>([]);
      //checkboxnumerical value
      const checkboxArray = ref<any[]>([]);
      /**
       * monitorvalue
       */
      watchEffect(() => {
        //update-begin-author:taoyan date:2022-7-4 for:issues/I5E7YX AUTOAfter entering the functional test, the online form is stuck in the functional test interface.
        let temp = props.value;
        if(!temp && temp!==0){
          checkboxArray.value = []
        }else{
          temp = temp + '';
          checkboxArray.value = temp.split(',')
        }
        //update-end-author:taoyan date:2022-7-4 for:issues/I5E7YX AUTOAfter entering the functional test, the online form is stuck in the functional test interface.
        //update-begin-author:taoyan date:20220401 for: call form resetFieldsCurrent information will not be cleared，The interface displays the last data
        if (props.value === '' || props.value === undefined) {
          checkboxArray.value = [];
        }
        //update-end-author:taoyan date:20220401 for: call form resetFieldsCurrent information will not be cleared，The interface displays the last data
      });
      /**
       * monitor字典code
       */
      watchEffect(() => {
        props && initOptions();
      });

      /**
       * 初始化Options
       */
      async function initOptions() {
        //according tooptions, 初始化Options
        if (props.options && props.options.length > 0) {
          checkOptions.value = props.options;
          return;
        }
        //according to字典Code, 初始化Options
        if (props.dictCode) {
          loadDictOptions()
        }
      }

      // according to字典codeQuery dictionary item
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
            checkOptions.value = res.map((item) => ({value: item.value, label: item.text, color: item.color}));
            //console.info('res', dictOptions.value);
          } else {
            console.error('getDictItems error: : ', res);
            checkOptions.value = [];
          }
        });
      }

      /**
       * changeevent
       * @param $event
       */
      function handleChange($event) {
        emit('update:value', $event.join(','));
        emit('change', $event.join(','));
      }
      const getDicColor = (value) => {
        if (props.useDicColor) {
          const findItem = checkOptions.value.find((item) => item.value == value);
          if (findItem) {
            return findItem.color;
          }
        }
        return null;
      };
      return { checkboxArray, checkOptions, attrs, handleChange, getDicColor };
    },
  });
</script>
<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】dictionary component（Except for native components）Add color configuration
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
  // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】dictionary component（Except for native components）Add color configuration
</style>
