<!--recycle bin-->
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="用户recycle bin" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Batch delete
              </a-menu-item>
              <a-menu-item key="1" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                Batch restore
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

<script lang="ts" setup name="tenant-recycle-bin-modal">
  import { BasicTable, TableAction } from '/@/components/Table';
  import { recycleBinPageList, deleteLogicDeleted, revertTenantLogic } from '../tenant.api';
  import { recycleColumns, searchRecycleFormSchema } from '../tenant.data';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Modal } from 'ant-design-vue';
  import { toRaw, unref } from 'vue';

  const { createMessage: $message } = useMessage();
  const [registerModal] = useModalInner(() => {});
  // List page public parameters、method
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      api: recycleBinPageList,
      columns: recycleColumns,
      size: 'small',
      formConfig: {
        schemas: searchRecycleFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      ellipsis: true,
    },
  });
  const emit = defineEmits(['success', 'register']);
  //registertabledata
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

  //获取Action bar事件
  function getTableAction(record) {
    return [
      {
        label: 'reduction',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: '是否confirmreduction',
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: 'Delete completely',
        icon: 'ant-design:scissor-outlined',
        popConfirm: {
          title: '是否confirmDelete completely',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * reduction
   * @param record
   */
  function handleRevert(record) {
    revertTenantLogic({ ids: record.id }, handleSuccess);
    emit('success');
  }

  /**
   * Successfully refreshed table
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  /**
   * Delete completely
   * @param record
   */
  async function handleDelete(record) {
    await deleteLogicDeleted({ ids: record.id }, handleSuccess);
  }

  /**
   * 批量Delete completely
   */
  function batchHandleDelete() {
    Modal.confirm({
      title: 'Delete completely',
      content: '是否confirmDelete completely',
      okText: 'confirm',
      cancelText: 'Cancel',
      onOk: () => {
        deleteLogicDeleted({ ids: toRaw(unref(selectedRowKeys)).join(',') }, handleSuccess);
      },
    });
  }

  /**
   * Batch restore
   */
  function batchHandleRevert() {
    Modal.confirm({
      title: 'reduction',
      content: '是否confirmreduction',
      okText: 'confirm',
      cancelText: 'Cancel',
      onOk: () => {
        revertTenantLogic({ ids: toRaw(unref(selectedRowKeys)).join(',') }, handleSuccess);
        emit('success');
      },
    });
  }
</script>

<style lang="less" scoped>
  :deep(.ant-popover-inner-content) {
    width: 185px !important;
  }
</style>
