<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Resigned personnel information" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                Batch cancellation
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Batch operations
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup name="user-quit-modal">
  import { ref, toRaw, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { recycleColumns } from './user.data';
  import { getQuitList, putCancelQuit, deleteRecycleBin } from './user.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { Modal } from 'ant-design-vue';
  import { defHttp } from '/@/utils/http/axios';

  const { createConfirm } = useMessage();
  // statementEmits
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal] = useModalInner(() => {
    checkedKeys.value = [];
  });
  //registertabledata
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      api: getQuitList,
      columns: recycleColumns,
      rowKey: 'id',
      canResize: false,
      useSearchForm: false,
      actionColumn: {
        width: 120,
      },
    },
  });
  //registertabledata
  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  /**
   * Cancel resignation event
   * @param record
   */
  async function handleCancelQuit(record) {
    await putCancelQuit({ userIds: record.id, usernames: record.username }, reload);
    emit('success');
  }
  /**
   * Batch cancellation离职事件
   */
  function batchHandleRevert() {
    Modal.confirm({
      title: 'Cancel resignation',
      content: 'Cancel resignation交接人也会清空',
      okText: 'confirm',
      cancelText: 'Cancel',
      onOk: () => {
        let rowValue = selectedRows.value;
        let rowData: any = [];
        for (const value of rowValue) {
          rowData.push(value.username);
        }
        handleCancelQuit({ id: toRaw(unref(selectedRowKeys)).join(','), username: rowData.join(',') });
      },
    });
  }

  //获取Action bar事件
  function getTableAction(record) {
    return [
      {
        label: 'Cancel resignation',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: '是否Cancel resignation,Cancel resignation交接人也会清空',
          confirm: handleCancelQuit.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped lang="less">
:deep(.ant-popover-inner-content){
  width: 185px !important;
}
</style>
