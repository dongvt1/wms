<template>
  <a-input v-bind="getBindValue" v-model:value="showText" @input="backValue"></a-input>
</template>

<script lang="ts">
  import { defineComponent, PropType, ref, watchEffect, unref, watch, computed } from 'vue';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { propTypes } from '/@/utils/propTypes';
  import { JInputTypeEnum } from '/@/enums/jeecgEnum.ts';
  import { omit } from 'lodash-es';

  export default defineComponent({
    name: 'JInput',
    inheritAttrs: false,
    props: {
      value: propTypes.string.def(''),
      type: propTypes.string.def(JInputTypeEnum.JINPUT_QUERY_LIKE),
      placeholder: propTypes.string.def(''),
      trim: propTypes.bool.def(false),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit }) {
      const attrs = useAttrs();
      //form value
      const showText = ref('');
      // update-begin--author:liaozhiyang---date:20231026---for：【issues/803】JIput updateSchemaNot effective
      //Binding properties
      const getBindValue = computed(() => {
        return omit(Object.assign({}, unref(props), unref(attrs)), ['value']);
      });
      // update-end--author:liaozhiyang---date:20231026---for：【issues/803】JIput updateSchemaNot effective
      //Listening type changes
      watch(
        () => props.type,
        (val) => {
          val && backValue({ target: { value: unref(showText) } });
        }
      );
      //monitorvaluechange
      watch(
        () => props.value,
        () => {
          initVal();
        },
        { immediate: true }
      );

      /**
       * initialization value
       */
      function initVal() {
        if (!props.value) {
          showText.value = '';
        } else {
          let text = props.value;
          switch (props.type) {
            case JInputTypeEnum.JINPUT_QUERY_LIKE:
              //Fix the value of routing parameters to be sent tojinputThe frame is intercepted by one bit each #1336
              if (text.indexOf('*') != -1) {
                text = text.substring(1, text.length - 1);
              }
              break;
            case JInputTypeEnum.JINPUT_QUERY_NE:
              text = text.substring(1);
              break;
            case JInputTypeEnum.JINPUT_QUERY_GE:
              text = text.substring(2);
              break;
            case JInputTypeEnum.JINPUT_QUERY_LE:
              text = text.substring(2);
              break;
            default:
          }
          showText.value = text;
        }
      }

      /**
       * return value
       */
      function backValue(e) {
        let text = e?.target?.value ?? '';
        if (text && !!props.trim) {
          text = text.trim();
        }
        switch (props.type) {
          case JInputTypeEnum.JINPUT_QUERY_LIKE:
            text = '*' + text + '*';
            break;
          case JInputTypeEnum.JINPUT_QUERY_NE:
            text = '!' + text;
            break;
          case JInputTypeEnum.JINPUT_QUERY_GE:
            text = '>=' + text;
            break;
          case JInputTypeEnum.JINPUT_QUERY_LE:
            text = '<=' + text;
            break;
          default:
        }
        emit('change', text);
        emit('update:value', text);
      }

      return { showText, attrs, getBindValue, backValue };
    },
  });
</script>

<style scoped></style>
