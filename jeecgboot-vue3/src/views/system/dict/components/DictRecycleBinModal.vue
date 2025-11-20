<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Dictionary Recycle Bin" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Slot: table title-->
      <template #tableTitle>
        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Batch Delete
              </a-menu-item>
              <a-menu-item key="2" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                Batch Restore
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Batch Operations
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Action column-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, toRaw } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { recycleBincolumns } from '../dict.data';
  import { getRecycleBinList, putRecycleBin, deleteRecycleBin, batchPutRecycleBin, batchDeleteRecycleBin } from '../dict.api';
  // Emits declaration
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal, { setModalProps, closeModal }] = useModalInner(() => {
    checkedKeys.value = [];
  });
  //Register table data
  const [registerTable, { reload }] = useTable({
    rowKey: 'id',
    api: getRecycleBinList,
    columns: recycleBincolumns,
    striped: true,
    useSearchForm: false,
    showTableSetting: false,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    pagination: false,
    tableSetting: { fullScreen: true },
    canResize: false,
    actionColumn: {
      width: 100,
      title: 'Actions',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });
  // update-begin--author:liaozhiyang---date:20240709---for:【TV360X-1663】Data dictionary recycling adds batch function
  /**
   * Selection column configuration
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 50,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };
  /**
   * Selection event
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }
  // update-end--author:liaozhiyang---date:20240709---for:【TV360X-1663】Data dictionary recycling adds batch function
  /**
   * Restore event
   */
  async function handleRevert(record) {
    await putRecycleBin(record.id, reload);
    emit('success');
  }
  /**
   * Delete event
   */
  async function handleDelete(record) {
    await deleteRecycleBin(record.id, reload);
  }
  /**
   * Batch restore event
   */
  function batchHandleRevert() {
    batchPutRecycleBin({ ids: toRaw(checkedKeys.value).join(',') }, () => {
      // update-begin--author:liaozhiyang---date:20240709---for:【TV360X-1663】Data dictionary recycling adds batch function
      reload();
      checkedKeys.value = [];
      emit('success');
      // update-end--author:liaozhiyang---date:20240709---for:【TV360X-1663】Data dictionary recycling adds batch function
    });
  }
  /**
   * Batch delete event
   */
  function batchHandleDelete() {
    batchDeleteRecycleBin({ ids: toRaw(checkedKeys.value).join(',') }, () => {
      // update-begin--author:liaozhiyang---date:20240709---for:【TV360X-1663】Data dictionary recycling adds batch function
      checkedKeys.value = [];
      reload();
      // update-end--author:liaozhiyang---date:20240709---for:【TV360X-1663】Data dictionary recycling adds batch function
    });
  }
  //Get action column events
  function getTableAction(record) {
    return [
      {
        label: 'Restore',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: 'Are you sure to restore?',
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: 'Permanently Delete',
        icon: 'ant-design:scissor-outlined',
        color: 'error',
        popConfirm: {
          title: 'Are you sure to delete?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
