<template>
  <JInputPop
    :value="innerValue"
    :width="300"
    :height="210"
    :pop-container="getPopupContainer"
    v-bind="cellProps"
    style="width: 100%"
    @blur="handleBlurCommon"
    @change="handleChangeCommon"
  />
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import JInputPop from '/@/components/Form/src/jeecg/components/JInputPop.vue';
  import { dispatchEvent } from '/@/components/jeecg/JVxeTable/utils';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';

  export default defineComponent({
    name: 'JVxeTextareaCell',
    components: { JInputPop },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, handleChangeCommon, handleBlurCommon } = useJVxeComponent(props);

      function getPopupContainer() {
        return document.body;
      }

      return { innerValue, cellProps, handleChangeCommon, handleBlurCommon, getPopupContainer };
    },
    // 【Component enhancement】See notes for details：JVxeComponent.Enhanced
    enhanced: {
      installOptions: {
        autofocus: '.ant-input',
      },
      aopEvents: {
        editActived({ $event, row, column }) {
          // Whether to open the pop-up window on the right by default
          if (column.params.defaultOpen ?? false) {
            dispatchEvent({
              $event,
              row,
              column,
              props: this.props,
              instance: this,
              className: '.ant-input-suffix .app-iconify',
              isClick: true,
            });
          }
        },
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
