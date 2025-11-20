<template>
  <Tinymce v-bind="bindProps" @change="onChange" />
</template>

<script lang="ts">
  import { computed, defineComponent, nextTick } from 'vue';

  import { Tinymce } from '/@/components/Tinymce';
  import { propTypes } from '/@/utils/propTypes';
  import { Form } from 'ant-design-vue';

  export default defineComponent({
    name: 'JEditor',
    // will not attrs properties bound to html on the label
    inheritAttrs: false,
    components: { Tinymce },
    props: {
      value: propTypes.string.def(''),
      disabled: propTypes.bool.def(false),
      //Focus or not
      autoFocus: propTypes.bool.def(true),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, attrs }) {
      // merge props and attrs
      const bindProps = computed(() => Object.assign({}, props, attrs));
      const formItemContext = Form.useInjectFormItemContext();
      // value change event
      function onChange(value) {
        emit('change', value);
        emit('update:value', value);
        // update-begin--author:liaozhiyang---date:20240429---for：【QQYUN-9110】The component has value verification and has not disappeared.
        nextTick(() => {
          formItemContext?.onFieldChange();
        });
        // update-end--author:liaozhiyang---date:20240429---for：【QQYUN-9110】The component has value verification and has not disappeared.
      }

      return {
        bindProps,
        onChange,
      };
    },
  });
</script>

<style lang="less" scoped></style>
