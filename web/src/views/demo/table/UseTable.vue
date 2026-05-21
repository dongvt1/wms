<template>
  <div class="p-4">
    <div class="mb-4">
      <a-button class="mr-2" @click="reloadTable"> reduction </a-button>
      <a-button class="mr-2" @click="changeLoading"> turn onloading </a-button>
      <a-button class="mr-2" @click="changeColumns"> ChangeColumns </a-button>
      <a-button class="mr-2" @click="getColumn"> GetColumns </a-button>
      <a-button class="mr-2" @click="getTableData"> Get表格数据 </a-button>
      <a-button class="mr-2" @click="getTableRawData"> Get接口原始数据 </a-button>
      <a-button class="mr-2" @click="setPaginationInfo"> Jump to page2Page </a-button>
    </div>
    <div class="mb-4">
      <a-button class="mr-2" @click="getSelectRowList"> Get选中行 </a-button>
      <a-button class="mr-2" @click="getSelectRowKeyList"> Get选中行Key </a-button>
      <a-button class="mr-2" @click="setSelectedRowKeyList"> Set selected row </a-button>
      <a-button class="mr-2" @click="clearSelect"> Clear selected rows </a-button>
      <a-button class="mr-2" @click="getPagination"> Get分Page信息 </a-button>
    </div>
    <BasicTable @register="registerTable" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, ColumnChangeParam, useTable } from '/@/components/Table';
  import { getBasicColumns, getBasicShortColumns } from './tableData';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { demoListApi } from '/@/api/demo/table';
  export default defineComponent({
    components: { BasicTable },
    setup() {
      const { createMessage } = useMessage();
      function onChange() {
        console.log('onChange', arguments);
      }
      const [
        registerTable,
        {
          setLoading,
          setColumns,
          getColumns,
          getDataSource,
          getRawDataSource,
          reload,
          getPaginationRef,
          setPagination,
          getSelectRows,
          getSelectRowKeys,
          setSelectedRowKeys,
          clearSelectedRowKeys,
        },
      ] = useTable({
        canResize: true,
        title: 'useTableExample',
        titleHelpMessage: 'useuseTableCall method in table',
        api: demoListApi,
        columns: getBasicColumns(),
        defSort: {
          field: 'name',
          order: 'ascend',
        },
        rowKey: 'id',
        showTableSetting: true,
        onChange,
        rowSelection: {
          type: 'checkbox',
        },
        onColumnsChange: (data: ColumnChangeParam[]) => {
          console.log('ColumnsChanged', data);
        },
      });

      function changeLoading() {
        setLoading(true);
        setTimeout(() => {
          setLoading(false);
        }, 1000);
      }
      function changeColumns() {
        setColumns(getBasicShortColumns());
      }
      function reloadTable() {
        setColumns(getBasicColumns());

        reload({
          page: 1,
        });
      }
      function getColumn() {
        createMessage.info('Please check in the console！');
        console.log(getColumns());
      }

      function getTableData() {
        createMessage.info('Please check in the console！');
        console.log(getDataSource());
      }

      function getTableRawData() {
        createMessage.info('Please check in the console！');
        console.log(getRawDataSource());
      }

      function getPagination() {
        createMessage.info('Please check in the console！');
        console.log(getPaginationRef());
      }

      function setPaginationInfo() {
        setPagination({
          current: 2,
        });
        reload();
      }
      function getSelectRowList() {
        createMessage.info('Please check in the console！');
        console.log(getSelectRows());
      }
      function getSelectRowKeyList() {
        createMessage.info('Please check in the console！');
        console.log(getSelectRowKeys());
      }
      function setSelectedRowKeyList() {
        setSelectedRowKeys(['0', '1', '2']);
      }
      function clearSelect() {
        clearSelectedRowKeys();
      }

      return {
        registerTable,
        changeLoading,
        changeColumns,
        reloadTable,
        getColumn,
        getTableData,
        getTableRawData,
        getPagination,
        setPaginationInfo,
        getSelectRowList,
        getSelectRowKeyList,
        setSelectedRowKeyList,
        clearSelect,
        onChange,
      };
    },
  });
</script>
