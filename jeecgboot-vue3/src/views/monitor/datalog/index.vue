<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleCompare" style="margin-right: 5px">Data comparison</a-button>
      </template>
    </BasicTable>
    <DataLogCompareModal @register="registerModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="monitor-datalog" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import DataLogCompareModal from './DataLogCompareModal.vue';
  const [registerModal, { openModal }] = useModal();
  import { getDataLogList } from './datalog.api';
  import { columns, searchFormSchema } from './datalog.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  const { createMessage } = useMessage();
  const checkedRows = ref<Array<object | number>>([]);

  // List page public parameters、method
  const { prefixCls, tableContext } = useListPage({
    designScope: 'datalog-template',
    tableProps: {
      title: 'Data log list',
      api: getDataLogList,
      columns: columns,
      formConfig: {
        labelWidth: 120,
        schemas: searchFormSchema,
      },
      actionColumn: false,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  function handleCompare() {
    let obj = selectedRows.value;
    console.info('sfsfsf', obj);
    if (!obj || obj.length != 2) {
      createMessage.warning('Please select two pieces of data!');
      return false;
    }
    if (obj[0].dataId != obj[1].dataId) {
      createMessage.warning('Please select the same database table and dataIDcompare!');
      return false;
    }
    openModal(true, {
      selectedRows,
      isUpdate: true,
    });
  }
</script>
