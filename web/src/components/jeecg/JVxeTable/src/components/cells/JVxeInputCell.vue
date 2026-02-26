<template>
  <a-input ref="input" :value="innerValue" v-bind="cellProps" @blur="handleBlur" @change="handleChange" />
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { isString } from '/@/utils/is';
  import { JVxeComponent, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';

  const NumberRegExp = /^-?\d+\.?\d*$/;
  export default defineComponent({
    name: 'JVxeInputCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, handleChangeCommon, handleBlurCommon } = useJVxeComponent(props);

      /** deal withchangeevent */
      function handleChange(event) {
        let { target } = event;
        let { value, selectionStart } = target;
        let change = true;
        if (props.type === JVxeTypes.inputNumber) {
          // Determine whether the input value matches a numeric regular expression，If it doesn’t match, restore it.
          if (!NumberRegExp.test(value) && value !== '' && value !== '-') {
            change = false;
            value = innerValue.value;
            target.value = value || '';
            if (typeof selectionStart === 'number') {
              target.selectionStart = selectionStart - 1;
              target.selectionEnd = selectionStart - 1;
            }
          } else {
            // update-begin--author:liaozhiyang---date:20240227---for：【QQYUN-8347】There are more than two decimal places and the last digit is0，The input box is unavailable for input
            // For example：41.1 -> 41.10, 100.1 -> 100.10 Not executedhandleChangeCommon function。
            if (value.indexOf('.') != -1) {
              const result = value.split('.').pop();
              if (result && result.length >= 2 && result.substr(-1) === '0') {
                change = false;
                innerValue.value = value;
              }
            }
            // update-end--author:liaozhiyang---date:20240227---for：【QQYUN-8347】There are more than two decimal places and the last digit is0，The input box is unavailable for input
          }
        }
        // 触发event，Store the entered value
        if (change) {
          handleChangeCommon(value);
        }
      }

      /** deal withblur失去焦点event */
      function handleBlur(event) {
        let { target } = event;
        // Determine whether the input value matches a numeric regular expression，If there is no match, leave it blank.
        if (props.type === JVxeTypes.inputNumber) {
          if (!NumberRegExp.test(target.value)) {
            target.value = '';
          } else {
            target.value = Number.parseFloat(target.value);
          }
        }
        handleChangeCommon(target.value, true);
        handleBlurCommon(target.value);
      }

      return {
        innerValue,
        cellProps,
        handleChange,
        handleBlur,
      };
    },
    enhanced: {
      installOptions: {
        autofocus: '.ant-input',
      },
      getValue(value, ctx) {
        if (ctx?.props?.type === JVxeTypes.inputNumber && isString(value)) {
          if (NumberRegExp.test(value)) {
            // 【issues/I5IHN7】Fixed the problem of not being able to enter decimal pointsbug
            if (/\.0*$/.test(value)) {
              return value;
            }
            return Number.parseFloat(value);
          }
        }
        return value;
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
