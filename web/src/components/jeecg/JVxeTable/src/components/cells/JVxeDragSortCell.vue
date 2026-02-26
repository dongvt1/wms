<template>
  <div class="j-vxe-drag-box">
     <span v-if="!isAllowDrag"><span class="not-drag-btn"> <Icon icon="mi:drag" /> </span
      ></span>
    <a-dropdown v-else :trigger="['click']" >
      <span
        ><span class="drag-btn"> <Icon icon="mi:drag" /> </span
      ></span>
      <template #overlay >
        <a-menu>
          <a-menu-item key="0" :disabled="disabledMoveUp" @click="handleRowMoveUp">move up</a-menu-item>
          <a-menu-item key="1" :disabled="disabledMoveDown" @click="handleRowMoveDown">move down</a-menu-item>
          <template v-if="allowInsertRow">
            <a-menu-divider />
            <a-menu-item key="3" @click="handleRowInsertDown">insert a row</a-menu-item>
          </template>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';

  export default defineComponent({
    name: 'JVxeDragSortCell',
    components: { Icon },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { rowIndex, originColumn, fullDataLength, trigger } = useJVxeComponent(props);
      // update-begin--author:liaozhiyang---date:20240417---for:【QQYUN-8785】onlineform column positionidNo restrictions，Drag other columns toidcolumn above，Error when synchronizing database
      const isAllowDrag = computed(() => {
        const notAllowDrag = originColumn.value.notAllowDrag;
        if (notAllowDrag.length) {
          const row = props.params.row;
          const find = notAllowDrag.find((item: any) => {
            const { key, value } = item;
            return row[key] == value;
          });
          return !find;
        } else {
          return true;
        }
      });
      // update-end--author:liaozhiyang---date:20240417---for:【QQYUN-8785】onlineform column positionidNo restrictions，Drag other columns toidcolumn above，Error when synchronizing database
      const disabledMoveUp = computed(() => rowIndex.value === 0);
      const disabledMoveDown = computed(() => rowIndex.value === fullDataLength.value - 1);

      // Whether to allow inserting rows
      const allowInsertRow = computed(() => originColumn.value.insertRow);

      /** move up */
      function handleRowMoveUp() {
        if (!disabledMoveUp.value) {
          trigger('rowResort', {
            oldIndex: rowIndex.value,
            newIndex: rowIndex.value - 1,
          });
        }
      }

      /** move down */
      function handleRowMoveDown() {
        if (!disabledMoveDown.value) {
          trigger('rowResort', {
            oldIndex: rowIndex.value,
            newIndex: rowIndex.value + 1,
          });
        }
      }

      /** insert a row */
      function handleRowInsertDown() {
        trigger('rowInsertDown', rowIndex.value);
      }

      return {
        disabledMoveUp,
        disabledMoveDown,
        handleRowMoveUp,
        handleRowMoveDown,
        handleRowInsertDown,
        isAllowDrag,
        allowInsertRow,
      };
    },
    // 【Component enhancement】See notes for details：JVxeComponent.Enhanced
    enhanced: {
      // 【Function switch】
      switches: {
        editRender: false,
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style lang="less">
  .j-vxe-drag-box {
    .app-iconify {
      cursor: move;
    }
  }

  .vxe-table--fixed-wrapper {
    .j-vxe-drag-box {
      .app-iconify {
        cursor: pointer;
      }
    }
  }
</style>
<style scoped>
  .not-drag-btn {
    opacity: 0.5;
     .app-iconify {
      cursor: not-allowed;
    }
  }
</style>
