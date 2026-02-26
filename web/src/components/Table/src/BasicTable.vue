<template>
  <div ref="wrapRef" :class="getWrapperClass">
    <BasicForm
      :class="{ 'table-search-area-hidden': !getBindValues.formConfig?.schemas?.length }"
      submitOnReset
      v-bind="getFormProps"
      source="table-query"
      v-if="getBindValues.useSearchForm"
      :tableAction="tableAction"
      @register="registerForm"
      @submit="handleSearchInfoChange"
      @advanced-change="redoHeight"
    >
      <template #[replaceFormSlotKey(item)]="data" v-for="item in getFormSlotKeys">
        <slot :name="item" v-bind="data || {}"></slot>
      </template>
    </BasicForm>

    <!-- antd v3 Upgrade compatible，Prevent data collection，Prevent console errors -->
    <!-- https://antdv.com/docs/vue/migration-v3-cn -->
    <a-form-item-rest>
      <!-- 【TV360X-377】Related records are required to be filled in. This affectstableinput box and page number styles -->
      <a-form-item>
        <Table ref="tableElRef" v-bind="getBindValues" :rowClassName="getRowClassName" v-show="getEmptyDataIsShowTable" @resizeColumn="handleResizeColumn" @change="handleTableChange">
          <!-- antdThe native slot is passed directly -->
          <template #[item]="data" v-for="item in slotNamesGroup.native" :key="item">
            <!-- update-begin--author:liaozhiyang---date:20240424---for：【issues/1146】BasicTableuseheaderCellCan't select all boxes -->
            <template v-if="item === 'headerCell'">
              <CustomSelectHeader v-if="isCustomSelection(data.column)" v-bind="selectHeaderProps" />
              <slot v-else :name="item" v-bind="data || {}"></slot>
            </template>
            <slot v-else :name="item" v-bind="data || {}"></slot>
            <!-- update-begin--author:liaozhiyang---date:20240424---for：【issues/1146】BasicTableuseheaderCellCan't select all boxes -->
          </template>
          <template #headerCell="{ column }">
            <!-- update-begin--author:sunjianlei---date:220230630---for：【QQYUN-5571】Self-encapsulating selection column，Solve the problem of stuck data row selection -->
            <CustomSelectHeader v-if="isCustomSelection(column)" v-bind="selectHeaderProps"/>
            <HeaderCell v-else :column="column" />
            <!-- update-end--author:sunjianlei---date:220230630---for：【QQYUN-5571】Self-encapsulating selection column，Solve the problem of stuck data row selection -->
          </template>
          <!-- increase pairantdv3.xcompatible -->
          <template #bodyCell="data">
            <!-- update-begin--author:liaozhiyang---date:220230717---for：【issues-179】antd3 Some warnings and errors(For tables) -->
            <!-- update-begin--author:liusq---date:20230921---for：【issues/770】slotsBakException error reporting problem,Increase judgmentcolumnexists -->
            <template v-if="data.column?.slotsBak?.customRender">
            <!-- update-end--author:liusq---date:20230921---for：【issues/770】slotsBakException error reporting problem,Increase judgmentcolumnexists -->
              <slot :name="data.column.slotsBak.customRender" v-bind="data || {}"></slot>
            </template>
            <template v-else>
              <slot name="bodyCell" v-bind="data || {}"></slot>
            </template>
            <!-- update-begin--author:liaozhiyang---date:22030717---for：【issues-179】antd3 Some warnings and errors(For tables) -->
          </template>
          <!-- update-begin--author:liaozhiyang---date:20240425---for：【pull/1201】Add toantdofTableSummary功能compatible老ofsummary（Total at the end of the table） -->
          <template v-if="showSummaryRef && !getBindValues.showSummary" #summary="data">
            <slot name="summary" v-bind="data || {}">
              <TableSummary :data="data || {}" v-bind="getSummaryProps" />
            </slot>
          </template>
          <!-- update-end--author:liaozhiyang---date:20240425---for：【pull/1201】Add toantdofTableSummary功能compatible老ofsummary（Total at the end of the table） -->
        </Table>
      </a-form-item>
    </a-form-item-rest>
  </div>
</template>
<script lang="ts">
  import type { BasicTableProps, TableActionType, SizeType, ColumnChangeParam, BasicColumn } from './types/table';

  import { defineComponent, ref, computed, unref, toRaw, inject, watchEffect, watch, onUnmounted, onMounted, nextTick } from 'vue';
  import { Table } from 'ant-design-vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { PageWrapperFixedHeightKey } from '/@/components/Page/injectionKey';
  import CustomSelectHeader from './components/CustomSelectHeader.vue'
  import expandIcon from './components/ExpandIcon';
  import HeaderCell from './components/HeaderCell.vue';
  import TableSummary from './components/TableSummary';
  import { InnerHandlers } from './types/table';
  import { usePagination } from './hooks/usePagination';
  import { useColumns } from './hooks/useColumns';
  import { useDataSource } from './hooks/useDataSource';
  import { useLoading } from './hooks/useLoading';
  import { useRowSelection } from './hooks/useRowSelection';
  import { useTableScroll } from './hooks/useTableScroll';
  import { useCustomRow } from './hooks/useCustomRow';
  import { useTableStyle } from './hooks/useTableStyle';
  import { useTableHeader } from './hooks/useTableHeader';
  import { useTableExpand } from './hooks/useTableExpand';
  import { createTableContext } from './hooks/useTableContext';
  import { useTableFooter } from './hooks/useTableFooter';
  import { useTableForm } from './hooks/useTableForm';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useCustomSelection } from "./hooks/useCustomSelection";

  import { omit, pick } from 'lodash-es';
  import { basicProps } from './props';
  import { isFunction } from '/@/utils/is';
  import { warn } from '/@/utils/log';

  export default defineComponent({
    components: {
      Table,
      BasicForm,
      HeaderCell,
      TableSummary,
      CustomSelectHeader,
    },
    props: basicProps,
    emits: [
      'fetch-success',
      'fetch-error',
      'selection-change',
      'register',
      'row-click',
      'row-dbClick',
      'row-contextmenu',
      'row-mouseenter',
      'row-mouseleave',
      'edit-end',
      'edit-cancel',
      'edit-row-end',
      'edit-change',
      'expanded-rows-change',
      'change',
      'columns-change',
      'table-redo',
    ],
    setup(props, { attrs, emit, slots, expose }) {
      const tableElRef = ref(null);
      const tableData = ref<Recordable[]>([]);

      const wrapRef = ref(null);
      const innerPropsRef = ref<Partial<BasicTableProps>>();

      const { prefixCls } = useDesign('basic-table');
      const [registerForm, formActions] = useForm();

      const getProps = computed(() => {
        return { ...props, ...unref(innerPropsRef) } as BasicTableProps;
      });

      const isFixedHeightPage = inject(PageWrapperFixedHeightKey, false);
      watchEffect(() => {
        unref(isFixedHeightPage) &&
          props.canResize &&
          warn("'canResize' of BasicTable may not work in PageWrapper with 'fixedHeight' (especially in hot updates)");
      });

      const { getLoading, setLoading } = useLoading(getProps);
      const { getPaginationInfo, getPagination, setPagination, setShowPagination, getShowPagination } = usePagination(getProps);

      // update-begin--author:sunjianlei---date:220230630---for：【QQYUN-5571】Self-encapsulating selection column，Solve the problem of stuck data row selection

      // const { getRowSelection, getRowSelectionRef, getSelectRows, clearSelectedRowKeys, getSelectRowKeys, deleteSelectRowByKey, setSelectedRowKeys } =
      //   useRowSelection(getProps, tableData, emit);

      // Child column name
      const childrenColumnName = computed(() => getProps.value.childrenColumnName || 'children');

      // Custom select columns
      const {
        getRowSelection,
        getSelectRows,
        getSelectRowKeys,
        setSelectedRowKeys,
        getRowSelectionRef,
        selectHeaderProps,
        isCustomSelection,
        handleCustomSelectColumn,
        clearSelectedRowKeys,
        deleteSelectRowByKey,
        getExpandIconColumnIndex,
      } = useCustomSelection(
        getProps,
        emit,
        wrapRef,
        getPaginationInfo,
        tableData,
        childrenColumnName
      )
      // update-end--author:sunjianlei---date:220230630---for：【QQYUN-5571】Self-encapsulating selection column，Solve the problem of stuck data row selection

      const {
        handleTableChange: onTableChange,
        getDataSourceRef,
        getDataSource,
        getRawDataSource,
        setTableData,
        updateTableDataRecord,
        deleteTableDataRecord,
        insertTableDataRecord,
        findTableDataRecord,
        fetch,
        getRowKey,
        reload,
        getAutoCreateKey,
        updateTableData,
      } = useDataSource(
        getProps,
        {
          tableData,
          getPaginationInfo,
          setLoading,
          setPagination,
          validate: formActions.validate,
          clearSelectedRowKeys,
        },
        emit
      );

      function handleTableChange(...args) {
        onTableChange.call(undefined, ...args);
        emit('change', ...args);
        // Solved byuseTableregisteronChangehour不起作用of问题
        const { onChange } = unref(getProps);
        onChange && isFunction(onChange) && onChange.call(undefined, ...args);
      }

      const { getViewColumns, getColumns, getRefColumns, setCacheColumnsByField, setColumns, getColumnsRef, getCacheColumns } = useColumns(
        getProps,
        getPaginationInfo,
        // update-begin--author:sunjianlei---date:220230630---for：【QQYUN-5571】Self-encapsulating selection column，Solve the problem of stuck data row selection
        handleCustomSelectColumn,
        // update-end--author:sunjianlei---date:220230630---for：【QQYUN-5571】Self-encapsulating selection column，Solve the problem of stuck data row selection
      );

      const { getScrollRef, redoHeight } = useTableScroll(getProps, tableElRef, getColumnsRef, getRowSelectionRef, getDataSourceRef, slots, getPaginationInfo);

      const { customRow } = useCustomRow(getProps, {
        setSelectedRowKeys,
        getSelectRowKeys,
        clearSelectedRowKeys,
        getAutoCreateKey,
        emit,
      });

      const { getRowClassName } = useTableStyle(getProps, prefixCls);

      const { getExpandOption, expandAll, collapseAll } = useTableExpand(getProps, tableData, emit);

      const handlers: InnerHandlers = {
        onColumnsChange: (data: ColumnChangeParam[]) => {
          emit('columns-change', data);
          // support useTable
          unref(getProps).onColumnsChange?.(data);
        },
      };

      const { getHeaderProps } = useTableHeader(getProps, slots, handlers);
      // update-begin--author:liaozhiyang---date:20240425---for：【pull/1201】Add toantdofTableSummary功能compatible老ofsummary（Total at the end of the table）
      const getSummaryProps = computed(() => {
        // update-begin--author:liaozhiyang---date:20250318---for：【issues/7956】repairshowSummary: falseThe total column is misaligned when there is an embedded subtable
        const result = pick(unref(getProps), ['summaryFunc', 'summaryData', 'hasExpandedRow', 'rowKey']);
        result['hasExpandedRow'] = Object.keys(slots).includes('expandedRowRender');
        // update-end--author:liaozhiyang---date:20250318---for：【issues/7956】repairshowSummary: falseThe total column is misaligned when there is an embedded subtable
        return result;
      });
      const getIsEmptyData = computed(() => {
        return (unref(getDataSourceRef) || []).length === 0;
      });
      const showSummaryRef = computed(() => {
        const summaryProps = unref(getSummaryProps);
        return (summaryProps.summaryFunc || summaryProps.summaryData) && !unref(getIsEmptyData);
      });
      // update-end--author:liaozhiyang---date:20240425---for：【pull/1201】Add toantdofTableSummary功能compatible老ofsummary（Total at the end of the table）

      const { getFooterProps } = useTableFooter(getProps, slots, getScrollRef, tableElRef, getDataSourceRef);

      const { getFormProps, replaceFormSlotKey, getFormSlotKeys, handleSearchInfoChange } = useTableForm(getProps, slots, fetch, getLoading);

      const getBindValues = computed(() => {
        const dataSource = unref(getDataSourceRef);
        let propsData: Recordable = {
          // date-begin--author:liaozhiyang---date:20250716---for：【issues/8564】basicTaleofTableLayoutReplace withautoNot effective
          tableLayout: 'fixed',
          // date-begin--author:liaozhiyang---date:20250716---for：【issues/8564】basicTaleofTableLayoutReplace withautoNot effective
          // ...(dataSource.length === 0 ? { getPopupContainer: () => document.body } : {}),
          ...attrs,
          customRow,
          //树列表展开useAntDesignVue默认of加减图标 author:scott date:20210914
          //expandIcon: slots.expandIcon ? null : expandIcon(),
          ...unref(getProps),
          ...unref(getHeaderProps),
          scroll: unref(getScrollRef),
          loading: unref(getLoading),
          rowSelection: unref(getRowSelectionRef),
          rowKey: unref(getRowKey),
          columns: toRaw(unref(getViewColumns)),
          pagination: toRaw(unref(getPaginationInfo)),
          dataSource,
          footer: unref(getFooterProps),
          ...unref(getExpandOption),
          // 【QQYUN-5837】Dynamic calculation expandIconColumnIndex
          expandIconColumnIndex: getExpandIconColumnIndex.value,
        };

        //update-begin---author:wangshuai ---date:20230214  for：[QQYUN-4237]code generation Embedded subtable mode no scrollbar------------
        //额外of展开行存在插槽hour会将滚动移除掉,Comment out
        /*if (slots.expandedRowRender) {
          propsData = omit(propsData, 'scroll');
        }*/
        //update-end---author:wangshuai ---date:20230214  for：[QQYUN-4237]code generation Embedded subtable mode no scrollbar------------ 

        // update-begin--author:sunjianlei---date:220230630---for：【QQYUN-5571】Self-encapsulating selection column，Solve the problem of stuck data row selection
        // Custom select columns，需要去掉原生of
        delete propsData.rowSelection
        // update-end--author:sunjianlei---date:220230630---for：【QQYUN-5571】Self-encapsulating selection column，Solve the problem of stuck data row selection

        // update-begin--author:liaozhiyang---date:20230919---for：【QQYUN-6387】Expand writing method（Remove error report）
        !propsData.isTreeTable && delete propsData.expandIconColumnIndex;
        propsData.expandedRowKeys === null && delete propsData.expandedRowKeys;
        // update-end--author:liaozhiyang---date:20230919---for：【QQYUN-6387】Expand writing method（Remove error report）
        propsData = omit(propsData, ['class', 'onChange']);
        return propsData;
      });

      // Uniformly set table column widths
      const getMaxColumnWidth = computed(() => {
        const values = unref(getBindValues);
        return values.maxColumnWidth > 0 ? values.maxColumnWidth + 'px' : null;
      });

      const getWrapperClass = computed(() => {
        const values = unref(getBindValues);
        return [
          prefixCls,
          attrs.class,
          {
            [`${prefixCls}-form-container`]: values.useSearchForm,
            [`${prefixCls}--inset`]: values.inset,
            [`${prefixCls}-col-max-width`]: getMaxColumnWidth.value != null,
            // 是否显示Total at the end of the table
            [`${prefixCls}--show-summary`]: values.showSummary,
          },
        ];
      });

      const getEmptyDataIsShowTable = computed(() => {
        const { emptyDataIsShowTable, useSearchForm } = unref(getProps);
        if (emptyDataIsShowTable || !useSearchForm) {
          return true;
        }
        return !!unref(getDataSourceRef).length;
      });

      function setProps(props: Partial<BasicTableProps>) {
        innerPropsRef.value = { ...unref(innerPropsRef), ...props };
      }

      const tableAction: TableActionType = {
        reload,
        getSelectRows,
        clearSelectedRowKeys,
        getSelectRowKeys,
        deleteSelectRowByKey,
        setPagination,
        setTableData,
        updateTableDataRecord,
        deleteTableDataRecord,
        insertTableDataRecord,
        findTableDataRecord,
        redoHeight,
        setSelectedRowKeys,
        setColumns,
        setLoading,
        getDataSource,
        getRawDataSource,
        setProps,
        getRowSelection,
        getPaginationRef: getPagination,
        getColumns,
        // update-begin--author:liaozhiyang---date:20250722---for：【issues/8529】setColumnsThe rear row configuration is not updated in conjunction with each other.
        getColumnsRef: () => getColumnsRef,
        // update-end--author:liaozhiyang---date:20250722---for：【issues/8529】setColumnsThe rear row configuration is not updated in conjunction with each other.
        getCacheColumns,
        emit,
        updateTableData,
        setShowPagination,
        getShowPagination,
        setCacheColumnsByField,
        expandAll,
        collapseAll,
        getSize: () => {
          return unref(getBindValues).size as SizeType;
        },
        // update-begin--author:liaozhiyang---date:20250904---for：【QQYUN-13558】erpThe main style table is5There are also scroll bars when there is data
        getBindValuesRef: () => getBindValues,
        // update-end--author:liaozhiyang---date:20250904---for：【QQYUN-13558】erpThe main style table is5There are also scroll bars when there is data
      };
      createTableContext({ ...tableAction, wrapRef, getBindValues });

      // update-begin--author:sunjianlei---date:220230718---for：【issues/179】compatible新老slotsWriting method，Remove console warning
      // 获取分组之后ofslotname
      const slotNamesGroup = computed<{
        // AntTableNative slot
        native: string[];
        // Column custom slots
        custom: string[];
      }>(() => {
        const native: string[] = [];
        const custom: string[] = [];
        const columns = unref<Recordable[]>(getViewColumns) as BasicColumn[];
        const allCustomRender = columns.map<string>((column) => column.slotsBak?.customRender);
        for (const name of Object.keys(slots)) {
          // 过滤特殊of插槽
          if (['bodyCell'].includes(name)) {
            continue;
          }
          if (allCustomRender.includes(name)) {
            custom.push(name);
          } else {
            native.push(name);
          }
        }
        return { native, custom };
      });
      // update-end--author:sunjianlei---date:220230718---for：【issues/179】compatible新老slotsWriting method，Remove console warning
      // update-begin--author:liaozhiyang---date:20231226---for：【issues/945】BasicTable组件设置默认展开Not effective
      nextTick(() => {
        getProps.value.defaultExpandAllRows && expandAll();
      })
      // update-end--author:sunjianlei---date:20231226---for：【issues/945】BasicTable组件设置默认展开Not effective
      // update-begin--author:liaozhiyang---date:20241225---for：【issues/7588】Automatically refresh table after selection
      expose({ ...tableAction, handleSearchInfoChange });
      // update-end--author:liaozhiyang---date:20241225---for：【issues/7588】Automatically refresh table after selection

      emit('register', tableAction, formActions);


      return {
        tableElRef,
        getBindValues,
        getLoading,
        registerForm,
        handleSearchInfoChange,
        getEmptyDataIsShowTable,
        handleTableChange,
        getRowClassName,
        wrapRef,
        tableAction,
        redoHeight,
        handleResizeColumn: (w, col) => {
          // update-begin--author:liaozhiyang---date:20240903---for：【issues/7101】column configurationresizable: truehour，Total at the end of the tableof列宽没有同步改变
          const columns = getColumns();
          const findItem = columns.find((item) => {
            if (item['dataIndex'] != null) {
              return item['dataIndex'] === col['dataIndex'];
            } else if (item['flag'] != null) {
              return item['flag'] === col['flag'];
            }
            return false;
          });
          if (findItem) {
            findItem.width = w;
            setColumns(columns);
          }
          // update-end--author:liaozhiyang---date:20240903---for：【issues/7101】column configurationresizable: truehour，Total at the end of the tableof列宽没有同步改变
          console.log('col',col);
          col.width = w;
        },
        getFormProps: getFormProps as any,
        replaceFormSlotKey,
        getFormSlotKeys,
        getWrapperClass,
        getMaxColumnWidth,
        columns: getViewColumns,

        // update-begin--author:sunjianlei---date:220230630---for：【QQYUN-5571】Self-encapsulating selection column，Solve the problem of stuck data row selection
        selectHeaderProps,
        isCustomSelection,
        // update-end--author:sunjianlei---date:220230630---for：【QQYUN-5571】Self-encapsulating selection column，Solve the problem of stuck data row selection
        slotNamesGroup,
        // update-begin--author:liaozhiyang---date:20240425---for：【pull/1201】Add toantdofTableSummary功能compatible老ofsummary（Total at the end of the table）
        getSummaryProps,
        showSummaryRef,
        // update-end--author:liaozhiyang---date:20240425---for：【pull/1201】Add toantdofTableSummary功能compatible老ofsummary（Total at the end of the table）
      };
    },
  });
</script>
<style lang="less">
  @border-color: #cecece4d;

  @prefix-cls: ~'@{namespace}-basic-table';

  [data-theme='dark'] {
    .ant-table-tbody > tr:hover.ant-table-row-selected > td,
    .ant-table-tbody > tr.ant-table-row-selected td {
      background-color: #262626;
    }

    .@{prefix-cls} {
      //Table selection toolbar style
      .alert {
        // background-color: #323232;
        // border-color: #424242;
      }
    }
  }

  .@{prefix-cls} {
    max-width: 100%;

    &-row__striped {
      td {
        background-color: @app-content-background;
      }
    }
    // update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1232】After the query area is hidden, the request does not go away after clicking refresh.(usecsshide)
    > .table-search-area-hidden {
      display: none;
    }
    // update-end--author:liaozhiyang---date:20240613---for：【TV360X-1232】After the query area is hidden, the request does not go away after clicking refresh.(usecsshide)
    &-form-container {
      padding: 10px;

      .ant-form {
        padding: 12px 10px 6px 10px;
        margin-bottom: 8px;
        background-color: @component-background;
        border-radius: 2px;
      }
    }

    .ant-tag {
      margin-right: 0;
    }

    //update-begin-author:liusq---date:20230517--for: [issues/526]RangePicker Problem with setting the preset range button style---
    .ant-picker-preset {
      .ant-tag {
        margin-right: 8px !important;
      }
    }
    //update-end-author:liusq---date:20230517--for: [issues/526]RangePicker Problem with setting the preset range button style---

    .ant-table-wrapper {
      padding: 6px;
      background-color: @component-background;
      border-radius: 2px;

      .ant-table-title {
        min-height: 40px;
        padding: 0 0 8px 0 !important;
      }

      .ant-table.ant-table-bordered .ant-table-title {
        border: none !important;
      }
    }

    .ant-table {
      width: 100%;
      overflow-x: hidden;

      &-title {
        display: flex;
        padding: 8px 6px;
        border-bottom: none;
        justify-content: space-between;
        align-items: center;
      }
      //Define row color
      .trcolor {
        background-color: rgba(255, 192, 203, 0.31);
        color: red;
      }

      //.ant-table-tbody > tr.ant-table-row-selected td {
      //background-color: fade(@primary-color, 8%) !important;
      //}
    }

    .ant-pagination {
      margin: 10px 0 0 0;
    }

    .ant-table-footer {
      padding: 0;

      .ant-table-wrapper {
        padding: 0;
      }

      table {
        border: none !important;
      }

      .ant-table-content {
        overflow-x: hidden !important;
        //  overflow-y: scroll !important;
      }

      td {
        padding: 12px 8px;
      }
    }
    //Table selection toolbar style
    .alert {
      height: 38px;
      // background-color: #e6f7ff;
      // border-color: #91d5ff;
    }
    &--inset {
      .ant-table-wrapper {
        padding: 0;
      }
    }

    // ------ Uniformly set the maximum width of table columns ------
    &-col-max-width {
      .ant-table-thead tr th,
      .ant-table-tbody tr td {
        max-width: v-bind(getMaxColumnWidth);
      }
    }
    // ------ Uniformly set the maximum width of table columns ------

    // update-begin--author:sunjianlei---date:220230718---for：【issues/622】repairTotal at the end of the table错位of问题
    &--show-summary {
      .ant-table > .ant-table-footer {
        padding: 12px 0 0;
      }
      .ant-table > .ant-table-footer {
        // update-begin--author:liaozhiyang---date:20241111---for：【issues/7413】The total rows are a bit misaligned
        padding-left: 0 !important;
        padding-right: 0 !important;
        // update-end--author:liaozhiyang---date:20241111---for：【issues/7413】The total rows are a bit misaligned
      }
      .ant-table.ant-table-bordered > .ant-table-footer {
        border: 0;
      }
    }
    // update-end--author:sunjianlei---date:220230718---for：【issues/622】repairTotal at the end of the table错位of问题
    // update-begin--author:liaozhiyang---date:20240604---for：【TV360X-377】Related records are required to be filled in. This affectstableinput box and page number styles
    > .ant-form-item {
      margin-bottom: 0;
    }
    // update-end--author:liaozhiyang---date:20240604---for：【TV360X-377】Related records are required to be filled in. This affectstableinput box and page number styles
  }
</style>
