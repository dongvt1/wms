<template>
  <MarkDown v-bind="bindProps" @change="onChange" @get="onGetVditor" />
</template>

<script lang="ts">
  import { computed, defineComponent, watch, nextTick } from 'vue';
  import { MarkDown } from '/@/components/Markdown';
  import { propTypes } from '/@/utils/propTypes';
  import { Form } from 'ant-design-vue';
  export default defineComponent({
    name: 'JMarkdownEditor',
    // will not attrs properties bound to html on the label
    inheritAttrs: false,
    components: { MarkDown },
    props: {
      value: propTypes.string.def(''),
      disabled: propTypes.bool.def(false),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, attrs }) {
      // markdown Component instance
      let mdRef: any = null;
      // vditor Component instance
      let vditorRef: any = null;
      // merge props and attrs
      const bindProps = computed(() => Object.assign({}, props, attrs));
      const formItemContext = Form.useInjectFormItemContext();
      // Equivalent to onMounted
      function onGetVditor(instance) {
        mdRef = instance;
        vditorRef = mdRef.getVditor();

        // Listening disabled，Toggle editor disabled state
        watch(
          () => props.disabled,
          (disabled) => (disabled ? vditorRef.disabled() : vditorRef.enable()),
          { immediate: true }
        );
      }

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
        onGetVditor,
      };
    },
  });
</script>

<style lang="less" scoped></style>
