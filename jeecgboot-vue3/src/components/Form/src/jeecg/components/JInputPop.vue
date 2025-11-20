<template>
  <a-popover
    trigger="contextmenu"
    v-model:open="visible"
    :overlayClassName="`${prefixCls}-popover`"
    :getPopupContainer="getPopupContainer"
    :placement="position"
  >
    <template #title>
      <span :class="title ? 'title' : 'emptyTitle'">{{ title }}</span>
      <span style="float: right" title="closure">
        <Icon icon="ant-design:close-outlined" @click="visible = false" />
      </span>
    </template>
    <template #content>
      <a-textarea ref="textareaRef" :value="innerValue" :disabled="disabled" :style="textareaStyle" v-bind="attrs" @input="onInputChange" @blur="onInputBlur" />
    </template>
    <a-input :class="`${prefixCls}-input`" :value="innerValue" :disabled="disabled" v-bind="attrs" @change="onInputChange" @blur="onInputBlur">
      <template #suffix>
        <Icon icon="ant-design:fullscreen-outlined" @click.stop="onShowPopup" />
      </template>
    </a-input>
  </a-popover>
</template>

<script lang="ts" setup>
  import { computed, nextTick, ref, watch } from 'vue';
  import Icon from '/@/components/Icon/src/Icon.vue';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { propTypes } from '/@/utils/propTypes';
  import { useDesign } from '/@/hooks/web/useDesign';

  const { prefixCls } = useDesign('j-input-popup');
  const props = defineProps({
    // v-model:value
    value: propTypes.string.def(''),
    title: propTypes.string.def(''),
    // Pop-up box display location
    position: propTypes.string.def('right'),
    width: propTypes.number.def(300),
    height: propTypes.number.def(150),
    disabled: propTypes.bool.def(false),
    // Elements mounted by the pop-up boxID
    popContainer: propTypes.oneOfType([propTypes.string, propTypes.func]).def(''),
  });
  const attrs = useAttrs();
  const emit = defineEmits(['change', 'update:value', 'blur']);

  const visible = ref<boolean>(false);
  const innerValue = ref<string>('');
  // textarea refobject
  const textareaRef = ref();
  // textarea style
  const textareaStyle = computed(() => ({
    height: `${props.height}px`,
    width: `${props.width}px`,
  }));

  watch(
    () => props.value,
    (value) => {
      if (value && value.length > 0) {
        innerValue.value = value;
      }
    },
    { immediate: true }
  );

  function onInputChange(event) {
    innerValue.value = event.target.value;
    emitValue(innerValue.value);
  }

  async function onShowPopup() {
    visible.value = true;
    await nextTick();
    textareaRef.value?.focus();
  }

  // 获取Elements mounted by the pop-up box
  function getPopupContainer(node) {
    if (!props.popContainer) {
      return node?.parentNode;
    } else if (typeof props.popContainer === 'function') {
      return props.popContainer(node);
    } else {
      return document.getElementById(props.popContainer);
    }
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }

  const onInputBlur = (event) => {
    emit('blur', event);
  }
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-j-input-popup';

  .@{prefix-cls} {
    &-popover {
      // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-144】jVxetableThe multi-line text component intitleRemove excess lines when not available
      .ant-popover-title:has(.emptyTitle) {
        border-bottom: none;
      }
      // update-end--author:liaozhiyang---date:20240520---for：【TV360X-144】jVxetableThe multi-line text component intitleRemove excess lines when not available
    }

    &-input {
      .app-iconify {
        cursor: pointer;
        color: #666666;
        transition: color 0.3s;

        &:hover {
          color: black;
        }
      }
    }
  }
</style>
