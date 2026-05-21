<template>
  <div style="width: 100%">
    <div v-if="$slots.headerTop" style="margin: 5px">
      <slot name="headerTop"></slot>
    </div>
    <div :class="`flex items-center ${prefixCls}__table-title-box`">
      <div :class="`${prefixCls}__tableTitle`">
        <slot name="tableTitle" v-if="$slots.tableTitle"></slot>
        <!--Modify title slot position-->
        <TableTitle :helpMessage="titleHelpMessage" :title="title" v-if="!$slots.tableTitle && title" />
      </div>

      <div :class="`${prefixCls}__toolbar`">
        <slot name="toolbar"></slot>
        <Divider type="vertical" v-if="$slots.toolbar && showTableSetting" />
        <TableSetting :class="`${prefixCls}__toolbar-desktop`" style="white-space: nowrap;" :setting="tableSetting" v-if="showTableSetting" @columns-change="handleColumnChange" />
        <a-popover :overlayClassName="`${prefixCls}__toolbar-mobile`" trigger="click" placement="left" :getPopupContainer="(n) => n?.parentElement">
          <template #content>
            <TableSetting mode="mobile" :setting="tableSetting" v-if="showTableSetting" @columns-change="handleColumnChange" />
          </template>
          <a-button :class="`${prefixCls}__toolbar-mobile`" v-if="showTableSetting" type="text" preIcon="ant-design:menu" shape="circle" />
        </a-popover>
      </div>
    </div>
    <!--Add totableTopslot-->
    <div style="margin: -4px 0 -2px; padding-top: 5px">
      <slot name="tableTop">
        <a-alert type="info" show-icon class="alert" v-if="openRowSelection != null">
          <template #message>
            <template v-if="selectRowKeys.length > 0">
              <span>
                <span>selected {{ selectRowKeys.length }} records</span>
                <span v-if="isAcrossPage">(Can spread across pages)</span>
              </span>
              <a-divider type="vertical" />
              <a @click="setSelectedRowKeys([])">Clear</a>
              <slot name="alertAfter" />
            </template>
            <template v-else>
              <span>No data selected</span>
            </template>
          </template>
        </a-alert>
      </slot>
    </div>
    <!--Add totableTopslot-->
  </div>
</template>
<script lang="ts">
  import type { TableSetting, ColumnChangeParam } from '../types/table';
  import type { PropType } from 'vue';
  import { defineComponent, computed } from 'vue';
  import { Divider } from 'ant-design-vue';
  import TableSettingComponent from './settings/index.vue';
  import TableTitle from './TableTitle.vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useTableContext } from '../hooks/useTableContext';

  export default defineComponent({
    name: 'BasicTableHeader',
    components: {
      Divider,
      TableTitle,
      TableSetting: TableSettingComponent,
    },
    props: {
      title: {
        type: [Function, String] as PropType<string | ((data: Recordable) => string)>,
      },
      tableSetting: {
        type: Object as PropType<TableSetting>,
      },
      showTableSetting: {
        type: Boolean,
      },
      titleHelpMessage: {
        type: [String, Array] as PropType<string | string[]>,
        default: '',
      },
    },
    emits: ['columns-change'],
    setup(_, { emit }) {
      const { prefixCls } = useDesign('basic-table-header');

      function handleColumnChange(data: ColumnChangeParam[]) {
        emit('columns-change', data);
      }

      const { getSelectRowKeys, setSelectedRowKeys, getRowSelection } = useTableContext();
      const selectRowKeys = computed(() => getSelectRowKeys());
      const openRowSelection = computed(() => getRowSelection());
      // Whether to allow cross-page selection
      const isAcrossPage = computed(() => openRowSelection.value?.preserveSelectedRowKeys === true);

      return { prefixCls, handleColumnChange, selectRowKeys, setSelectedRowKeys, openRowSelection, isAcrossPage };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-basic-table-header';

  .@{prefix-cls} {
    &__toolbar {
      //flex: 1;
      width: 140px;
      display: flex;
      align-items: center;
      justify-content: flex-end;

      > * {
        margin-right: 8px;
      }

      &-desktop {
        display: block;
      }

      &-mobile {
        display: none;
      }
    }
    &__tableTitle {
      flex: 1;
      display: flex;
      flex-wrap: wrap;
      align-content: flex-start;

      > * {
        margin-right: 4px;
        margin-bottom: 4px;
      }
    }

    @media (max-width: @screen-lg) {
      &__table-title-box {
        align-items: flex-end;
      }

      &__toolbar {
        width: 30px;
        text-align: center;

        > * {
          margin-right: 0;
        }

        .table-settings > * {
          margin-right: 0;
          margin-bottom: 6px;
        }
        &-desktop {
          display: none;
        }

        &-mobile {
          display: block;
          .table-settings > * {
            margin-right: 6px;
            margin-bottom: 0;
          }
        }
      }
    }
  }
</style>
