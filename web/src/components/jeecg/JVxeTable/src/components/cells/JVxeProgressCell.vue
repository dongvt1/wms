<template>
  <Progress :class="clazz" :percent="innerValue" size="small" v-bind="cellProps" />
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { Progress } from 'ant-design-vue';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';

  export default defineComponent({
    name: 'JVxeCheckboxCell',
    components: { Progress },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, scrolling } = useJVxeComponent(props);
      const clazz = computed(() => {
        return {
          'j-vxe-progress': true,
          'no-animation': scrolling.value,
        };
      });
      return { innerValue, cellProps, clazz };
    },
    // 【Component enhancement】See notes for details：：JVxeComponent.Enhanced
    enhanced: {
      switches: {
        editRender: false,
      },
      setValue(value) {
        try {
          if (typeof value !== 'number') {
            return Number.parseFloat(value);
          } else {
            return value;
          }
        } catch {
          return 0;
        }
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style scoped lang="less">
  // Turn off progress bar animation，Prevent problems with dynamic assignment during scrolling
  .j-vxe-progress.no-animation {
    :deep(.ant-progress-bg) {
      transition: none !important;
    }
  }
</style>
