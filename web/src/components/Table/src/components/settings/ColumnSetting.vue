<template>
  <Tooltip placement="top" v-bind="getBindProps" >
    <template #title>
      <span>{{ t('component.table.settingColumn') }}</span>
    </template>
    <Popover
      v-model:open="popoverVisible"
      placement="bottomLeft"
      trigger="click"
      @open-change="handleVisibleChange"
      :overlayClassName="`${prefixCls}__cloumn-list`"
      :getPopupContainer="getPopupContainer"
    >
      <template #title>
        <div :class="`${prefixCls}__popover-title`">
          <Checkbox :indeterminate="indeterminate" v-model:checked="checkAll" @change="onCheckAllChange">
            {{ t('component.table.settingColumnShow') }}
          </Checkbox>

          <Checkbox :disabled="isTreeTable" v-model:checked="checkIndex" @change="handleIndexCheckChange">
            {{ t('component.table.settingIndexColumnShow') }}
          </Checkbox>

          <!--                    <Checkbox-->
          <!--                            v-model:checked="checkSelect"-->
          <!--                            @change="handleSelectCheckChange"-->
          <!--                            :disabled="!defaultRowSelection"-->
          <!--                    >-->
          <!--                        {{ t('component.table.settingSelectColumnShow') }}-->
          <!--                    </Checkbox>-->
        </div>
      </template>

      <template #content>
        <ScrollContainer>
          <CheckboxGroup v-model:value="checkedList" @change="onChange" ref="columnListRef">
            <template v-for="item in plainOptions" :key="item.value">
              <div :class="`${prefixCls}__check-item`" v-if="!('ifShow' in item && !item.ifShow)">
                <DragOutlined class="table-column-drag-icon" />
                <Checkbox :value="item.value">
                  {{ item.label }}
                </Checkbox>

                <Tooltip placement="bottomLeft" :mouseLeaveDelay="0.4" :getPopupContainer="getPopupContainer">
                  <template #title>
                    {{ t('component.table.settingFixedLeft') }}
                  </template>
                  <Icon
                    icon="line-md:arrow-align-left"
                    :class="[
                      `${prefixCls}__fixed-left`,
                      {
                        active: item.fixed === 'left',
                        disabled: !checkedList.includes(item.value),
                      },
                    ]"
                    @click="handleColumnFixed(item, 'left')"
                  />
                </Tooltip>
                <Divider type="vertical" />
                <Tooltip placement="bottomLeft" :mouseLeaveDelay="0.4" :getPopupContainer="getPopupContainer">
                  <template #title>
                    {{ t('component.table.settingFixedRight') }}
                  </template>
                  <Icon
                    icon="line-md:arrow-align-left"
                    :class="[
                      `${prefixCls}__fixed-right`,
                      {
                        active: item.fixed === 'right',
                        disabled: !checkedList.includes(item.value),
                      },
                    ]"
                    @click="handleColumnFixed(item, 'right')"
                  />
                </Tooltip>
              </div>
            </template>
          </CheckboxGroup>
        </ScrollContainer>
        <div :class="`${prefixCls}__popover-footer`">
          <a-button size="small" @click="reset">
            {{ t('common.resetText') }}
          </a-button>
          <a-button size="small" type="primary" @click="saveSetting"> save </a-button>
        </div>
      </template>
      <SettingOutlined />
    </Popover>
  </Tooltip>
</template>
<script lang="ts">
  import type { BasicColumn, ColumnChangeParam } from '../../types/table';
  import { defineComponent, ref, reactive, toRefs, watchEffect, nextTick, unref, computed, watch } from 'vue';
  import { Tooltip, Popover, Checkbox, Divider } from 'ant-design-vue';
  import type { CheckboxChangeEvent } from 'ant-design-vue/lib/checkbox/interface';
  import { SettingOutlined, DragOutlined } from '@ant-design/icons-vue';
  import { Icon } from '/@/components/Icon';
  import { ScrollContainer } from '/@/components/Container';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useTableContext } from '../../hooks/useTableContext';
  import { useColumnsCache } from '../../hooks/useColumnsCache';
  import { useDesign } from '/@/hooks/web/useDesign';
  // import { useSortable } from '/@/hooks/web/useSortable';
  import { isFunction, isNullAndUnDef } from '/@/utils/is';
  import { getPopupContainer as getParentContainer } from '/@/utils';
  import { cloneDeep, omit } from 'lodash-es';
  import Sortablejs from 'sortablejs';
  import type Sortable from 'sortablejs';
  import { useLocaleStoreWithOut } from '/@/store/modules/locale';

  interface State {
    checkAll: boolean;
    isInit?: boolean;
    checkedList: string[];
    defaultCheckList: string[];
  }

  interface Options {
    label: string;
    value: string;
    fixed?: boolean | 'left' | 'right';
  }

  export default defineComponent({
    name: 'ColumnSetting',
    props: {
      isMobile: Boolean,
    },
    components: {
      SettingOutlined,
      Popover,
      Tooltip,
      Checkbox,
      CheckboxGroup: Checkbox.Group,
      DragOutlined,
      ScrollContainer,
      Divider,
      Icon,
    },
    emits: ['columns-change'],

    setup(props, { emit, attrs }) {
      const { t } = useI18n();
      const table = useTableContext();
      // update-begin--author:liaozhiyang---date:20250526---for：【issues/8301】Tree table serial number column disabled
      const isTreeTable = computed(() => table.getBindValues.value.isTreeTable);
      // update-end--author:liaozhiyang---date:20250526---for：【issues/8301】Tree table serial number column disabled
      const popoverVisible = ref(false);
      // update-begin--author:sunjianlei---date:20221101---for: Fixed the issue where list configuration cannot be dragged and dropped when entering for the first time.
      // nextTick(() => popoverVisible.value = false);
      // update-end--author:sunjianlei---date:20221101---for: Fixed the issue where list configuration cannot be dragged and dropped when entering for the first time.
      const defaultRowSelection = omit(table.getRowSelection(), 'selectedRowKeys');
      // update-begin--author:liaozhiyang---date:20250722---for：【issues/8529】setColumnsThe rear row configuration is not updated in conjunction with each other.
      const getColumnsRef = table.getColumnsRef();
      // update-end--author:liaozhiyang---date:20250722---for：【issues/8529】setColumnsThe rear row configuration is not updated in conjunction with each other.
      let inited = false;

      const cachePlainOptions = ref<Options[]>([]);
      const plainOptions = ref<Options[] | any>([]);

      const plainSortOptions = ref<Options[]>([]);

      const columnListRef = ref<ComponentRef>(null);

      const restAfterOptions = {
        value: null,
      };

      const state = reactive<State>({
        checkAll: true,
        checkedList: [],
        defaultCheckList: [],
      });

      const checkIndex = ref(false);
      const checkSelect = ref(false);

      const { prefixCls } = useDesign('basic-column-setting');

      const getValues = computed(() => {
        return unref(table?.getBindValues) || {};
      });

      const getBindProps = computed(() => {
        let obj = {};
        if (props.isMobile) {
          obj['open'] = false;
        }
        return obj;
      });

      let sortable: Sortable;
      const sortableOrder = ref<string[]>();
      const localeStore = useLocaleStoreWithOut();
      // List field configuration cache
      const { saveSetting, resetSetting, getCache } = useColumnsCache(
        {
          state,
          popoverVisible,
          plainOptions,
          plainSortOptions,
          sortableOrder,
          checkIndex,
          restAfterOptions,
        },
        setColumns,
        handleColumnFixed
      );

      watchEffect(() => {
        setTimeout(() => {
          if (!state.isInit) {
            init();
          }
        }, 0);
      });

      watchEffect(() => {
        const values = unref(getValues);
        checkIndex.value = !!values.showIndexColumn;
        checkSelect.value = !!values.rowSelection;
      });
      // update-begin--author:liaozhiyang---date:20240724---for：【issues/6908】When switching between multiple languages ​​without refreshing，BasicColumnandFormSchemaThe values ​​inside cannot be switched normally
      watch([localeStore], () => {
        const columns = getColumns();
        plainOptions.value = columns;
        plainSortOptions.value = columns;
        cachePlainOptions.value = columns;
      });
      // update-end--author:liaozhiyang---date:20240724---for：【issues/6908】When switching between multiple languages ​​without refreshing，BasicColumnandFormSchemaThe values ​​inside cannot be switched normally
      // update-begin--author:liaozhiyang---date:20250813---for：【issues/8529】setColumnsAfter displaying the originally hidden columns，This column is not checked in the column configuration.
      watch([getColumnsRef], () => {
        init();
      });
      // update-end--author:liaozhiyang---date:20250813---for：【issues/8529】setColumnsAfter displaying the originally hidden columns，This column is not checked in the column configuration.
      function getColumns() {
        const ret: Options[] = [];
        // update-begin--author:liaozhiyang---date:20250403---for：【issues/7996】Table column component cancels all or only checks the middle，Display unexpected
        let t = table.getColumns({ ignoreIndex: true, ignoreAction: true, ignoreAuth: true, ignoreIfShow: true });
        if (!t.length) {
          t = table.getCacheColumns();
        }
        // update-end--author:liaozhiyang---date:20250403---for：【issues/7996】Table column component cancels all or only checks the middle，Display unexpected
        t.forEach((item) => {
          ret.push({
            label: (item.title as string) || (item.customTitle as string),
            value: (item.dataIndex || item.title) as string,
            ...item,
          });
        });
        return ret;
      }

      async function init() {
        const columns = getColumns();

        const checkList = table
          .getColumns({ ignoreAction: true, ignoreIndex: true, ignoreAuth: true, ignoreIfShow: true })
          .map((item) => {
            if (item.defaultHidden) {
              return '';
            }
            return item.dataIndex || item.title;
          })
          .filter(Boolean) as string[];
        // update-begin--author:liaozhiyang---date:20250403---for：【issues/7996】Table column component cancels all or only checks the middle，Display unexpected
        const { sortedList = [] } = getCache() || {};
        await nextTick();
        // update-end--author:liaozhiyang---date:20250403---for：【issues/7996】Table column component cancels all or only checks the middle，Display unexpected
        if (!plainOptions.value.length) {
          // update-begin--author:liaozhiyang---date:20250403---for：【issues/7996】Table column component cancels all or only checks the middle，Display unexpected
          let tmp = columns;
          if (sortedList?.length) {
            tmp = columns.sort((prev, next) => {
              return sortedList.indexOf(prev.value) - sortedList.indexOf(next.value);
            });
          }
          // update-end--author:liaozhiyang---date:20250403---for：【issues/7996】Table column component cancels all or only checks the middle，Display unexpected
          plainOptions.value = tmp;
          plainSortOptions.value = tmp;
          cachePlainOptions.value = tmp;
          state.defaultCheckList = checkList;
        } else {
          // const fixedColumns = columns.filter((item) =>
          //   Reflect.has(item, 'fixed')
          // ) as BasicColumn[];

          unref(plainOptions).forEach((item: BasicColumn) => {
            const findItem = columns.find((col: BasicColumn) => col.dataIndex === item.dataIndex);
            if (findItem) {
              item.fixed = findItem.fixed;
            }
          });
          // update-begin--author:liaozhiyang---date:20250403---for：【issues/7996】Table column component cancels all or only checks the middle，Display unexpected
          if (sortedList?.length) {
            plainOptions.value.sort((prev, next) => {
              return sortedList.indexOf(prev.value) - sortedList.indexOf(next.value);
            });
          }
          // update-end--author:liaozhiyang---date:20250403---for：【issues/7996】Table column component cancels all or only checks the middle，Display unexpected
        }
        state.isInit = true;
        state.checkedList = checkList;
        // update-begin--author:liaozhiyang---date:20240612---for：【TV360X-105】Column display setting issue[Column display if there are unchecked columns，save并刷新后，Column display checkbox styles will be messed up]
        state.checkAll = columns.length === checkList.length;
        // update-end--author:liaozhiyang---date:20240612---for：【TV360X-105】Column display setting issue[Column display if there are unchecked columns，save并刷新后，Column display checkbox styles will be messed up]
      }

      // checkAll change
      function onCheckAllChange(e: CheckboxChangeEvent) {
        const checkList = plainOptions.value.map((item) => item.value);
        if (e.target.checked) {
          state.checkedList = checkList;
          setColumns(checkList);
        } else {
          state.checkedList = [];
          setColumns([]);
        }
      }

      const indeterminate = computed(() => {
        const len = plainOptions.value.length;
        let checkedLen = state.checkedList.length;
        // update-begin--author:liaozhiyang---date:20240612---for：【TV360X-105】Column display setting issue[The column display checkbox should not determine the status of the serial number column checkbox]
        // unref(checkIndex) && checkedLen--;
        // update-end--author:liaozhiyang---date:20240612---for：【TV360X-105】Column display setting issue[The column display checkbox should not determine the status of the serial number column checkbox]
        return checkedLen > 0 && checkedLen < len;
      });

      // Trigger when check/uncheck a column
      function onChange(checkedList: string[]) {
        const len = plainSortOptions.value.length;
        state.checkAll = checkedList.length === len;
        const sortList = unref(plainSortOptions).map((item) => item.value);
        checkedList.sort((prev, next) => {
          return sortList.indexOf(prev) - sortList.indexOf(next);
        });
        setColumns(checkedList);
      }

      // reset columns
      function reset() {
        // update-begin--author:liaozhiyang---date:20240612---for：【TV360X-105】Column display setting issue[It takes two resets to return to the initial state.]
        setColumns(table.getCacheColumns());
        setTimeout(() => {
          const columns = getColumns();
          // state.checkedList = [...state.defaultCheckList];
          // update-begin--author:liaozhiyang---date:20231103---for：【issues/825】tabel的列设置隐藏列save后切换路由问题[Reset is unchecked]
          state.checkedList = table
            .getColumns({ ignoreAction: true, ignoreAuth: true, ignoreIfShow: true })
            .map((item) => {
              return item.dataIndex || item.title;
            })
            .filter(Boolean) as string[];
          // update-end--author:liaozhiyang---date:20231103---for：【issues/825】tabel的列设置隐藏列save后切换路由问题[Reset is unchecked]
          state.checkAll = true;
          plainOptions.value = unref(cachePlainOptions);
          plainSortOptions.value = unref(cachePlainOptions);
          restAfterOptions.value = columns;
          if (sortableOrder.value) {
            sortable.sort(sortableOrder.value);
          }
          resetSetting();
        }, 100);
        // update-end--author:liaozhiyang---date:20240612---for：【TV360X-105】Column display setting issue[It takes two resets to return to the initial state.]
      }

      // Open the pop-up window for drag and drop initialization
      function handleVisibleChange() {
        if (inited) return;
        // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-254】Column settings flash and Safari pop-up window is too long
        setTimeout(() => {
          // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-254】Column settings flash and Safari pop-up window is too long
          const columnListEl = unref(columnListRef);
          if (!columnListEl) return;
          const el = columnListEl.$el as any;
          if (!el) return;
          // Drag and drop sort
          sortable = Sortablejs.create(unref(el), {
            animation: 500,
            delay: 400,
            delayOnTouchOnly: true,
            handle: '.table-column-drag-icon ',
            onEnd: (evt) => {
              const { oldIndex, newIndex } = evt;
              if (isNullAndUnDef(oldIndex) || isNullAndUnDef(newIndex) || oldIndex === newIndex) {
                return;
              }
              // Sort column
              const columns = cloneDeep(plainSortOptions.value);

              if (oldIndex > newIndex) {
                columns.splice(newIndex, 0, columns[oldIndex]);
                columns.splice(oldIndex + 1, 1);
              } else {
                columns.splice(newIndex + 1, 0, columns[oldIndex]);
                columns.splice(oldIndex, 1);
              }

              plainSortOptions.value = columns;
              // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6424】tableAfter the field list setting is not displayed，Then drag the field order，Not originally displayed，Showed again
              // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-108】After refreshing, check the previously unchecked field and drag it. The table column corresponding to the field disappears.
              const cols = columns.map((item) => item.value);
              const arr = cols.filter((cItem) => state.checkedList.find((lItem) => lItem === cItem));
              setColumns(arr);
              // First code
              // setColumns(columns);
              // update-end--author:liaozhiyang---date:20240522---for：【TV360X-108】After refreshing, check the previously unchecked field and drag it. The table column corresponding to the field disappears.
              // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6424】tableAfter the field list setting is not displayed，Then drag the field order，Not originally displayed，Showed again
              // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-105】Column display setting issue[重置之后save的顺序还是上次的]
              restAfterOptions.value = null;
              // update-end--author:liaozhiyang---date:20240611---for：【TV360X-105】Column display setting issue[重置之后save的顺序还是上次的]
            },
          });
          // record original order sequence
          if (!sortableOrder.value) {
            sortableOrder.value = sortable.toArray();
          }
          inited = true;
        }, 2000);
      }

      // Control whether the serial number column is displayed
      function handleIndexCheckChange(e: CheckboxChangeEvent) {
        table.setProps({
          showIndexColumn: e.target.checked,
        });
      }

      // Control whether the check box is displayed
      function handleSelectCheckChange(e: CheckboxChangeEvent) {
        table.setProps({
          rowSelection: e.target.checked ? defaultRowSelection : undefined,
        });
      }

      function handleColumnFixed(item: BasicColumn, fixed?: 'left' | 'right') {
        if (!state.checkedList.includes(item.dataIndex as string)) return;

        const columns = getColumns() as BasicColumn[];
        const isFixed = item.fixed === fixed ? false : fixed;
        const index = columns.findIndex((col) => col.dataIndex === item.dataIndex);
        if (index !== -1) {
          columns[index].fixed = isFixed;
        }
        item.fixed = isFixed;

        if (isFixed && !item.width) {
          item.width = 100;
        }
        table.setCacheColumnsByField?.(item.dataIndex as string, { fixed: isFixed });
        setColumns(columns);
      }

      function setColumns(columns: BasicColumn[] | string[]) {
        table.setColumns(columns);
        const data: ColumnChangeParam[] = unref(plainSortOptions).map((col) => {
          const visible =
            columns.findIndex((c: BasicColumn | string) => c === col.value || (typeof c !== 'string' && c.dataIndex === col.value)) !== -1;
          return { dataIndex: col.value, fixed: col.fixed, visible };
        });

        emit('columns-change', data);
      }

      function getPopupContainer() {
        return isFunction(attrs.getPopupContainer) ? attrs.getPopupContainer() : getParentContainer();
      }

      return {
        getBindProps,
        t,
        ...toRefs(state),
        popoverVisible,
        indeterminate,
        onCheckAllChange,
        onChange,
        plainOptions,
        reset,
        saveSetting,
        prefixCls,
        columnListRef,
        handleVisibleChange,
        checkIndex,
        checkSelect,
        handleIndexCheckChange,
        handleSelectCheckChange,
        defaultRowSelection,
        handleColumnFixed,
        getPopupContainer,
        isTreeTable,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-basic-column-setting';

  .table-column-drag-icon {
    margin: 0 5px;
    cursor: move;
  }

  .@{prefix-cls} {
    &__popover-title {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    /* Card bottom style */
    &__popover-footer {
      position: relative;
      top: 7px;
      text-align: right;
      padding: 4px 0 0;
      border-top: 1px solid #f0f0f0;

      .ant-btn {
        margin-right: 6px;
      }
    }

    &__check-item {
      display: flex;
      align-items: center;
      min-width: 100%;
      padding: 4px 16px 8px 0;

      .ant-checkbox-wrapper {
        width: 100%;

        &:hover {
          color: @primary-color;
        }
      }
    }

    &__fixed-left,
    &__fixed-right {
      color: rgba(0, 0, 0, 0.45);
      cursor: pointer;

      &.active,
      &:hover {
        color: @primary-color;
      }

      &.disabled {
        color: @disabled-color;
        cursor: not-allowed;
      }
    }

    &__fixed-right {
      transform: rotate(180deg);
    }

    &__cloumn-list {
      svg {
        width: 1em !important;
        height: 1em !important;
      }

      .ant-popover-inner-content {
        // max-height: 360px;
        padding-right: 0;
        padding-left: 0;
        // overflow: auto;
      }

      .ant-checkbox-group {
        // update-begin--author:liaozhiyang---date:20240118---for：【QQYUN-7887】Table column width is too long
        // width: 100%;
        min-width: 260px;
        max-width: min-content;
        // update-end--author:liaozhiyang---date:20240118---for：【QQYUN-7887】Table column width is too long
        // flex-wrap: wrap;
      }

      // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-254】Column settings flash and Safari pop-up window is too long
      &.ant-popover,
      .ant-popover-content,
      .ant-popover-inner,
      .ant-popover-inner-content,
      .scroll-container,
      .scrollbar__wrap {
        max-width: min-content;
      }
      // update-end--author:liaozhiyang---date:20240529---for：【TV360X-254】Column settings flash and Safari pop-up window is too long
      .scrollbar {
        height: 220px;
      }
    }
  }
</style>
