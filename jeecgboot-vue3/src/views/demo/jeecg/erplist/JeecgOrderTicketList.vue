<template>
  <div>
    <!--sheet-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection" :searchInfo="searchInfo">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> New</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                delete
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Batch operations
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!-- form area -->
    <JeecgOrderTicketModal @register="registerModal" @success="handleSuccess"></JeecgOrderTicketModal>
  </div>
</template>

<script lang="ts" setup>
  //tsgrammar
  import type { ComputedRef } from 'vue';
  import { ref, computed, unref, watch, inject } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import JeecgOrderTicketModal from './components/JeecgOrderTicketModal.vue';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useModal } from '/@/components/Modal';
  import { ticketColumns } from './erplist.data';
  import { ticketList, deleteTicket, deleteBatchTicket } from './erplist.api';
  import { isEmpty } from '/@/utils/is';
  import { useMessage } from '/@/hooks/web/useMessage';
  //Receive master tableid
  const orderId = inject<ComputedRef<string>>(
    'orderId',
    computed(() => '')
  );
  //Prompt pop-up window
  const $message = useMessage();
  //Pop-up windowmodel
  const [registerModal, { openModal }] = useModal();
  const searchInfo = {};
  // List page public parameters、method
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      api: getTicketList,
      tableSetting:{
        cacheKey:'ticket'
      },
      columns: ticketColumns,
      canResize: false,
      useSearchForm: false,
      actionColumn: {
        width: 180,
      },
      pagination: {
        current: 1,
        pageSize: 5,
        pageSizeOptions: ['5', '10', '20'],
      },
    },
  });

  //registertabledata
  const [registerTable, { reload, setSelectedRowKeys }, { rowSelection, selectedRowKeys }] = tableContext;

  watch(orderId, () => {
    searchInfo['orderId'] = unref(orderId);
    reload();
    // main tableidWhen changing，Clear the selected state of the subtable
    setSelectedRowKeys([]);
  });

  async function getTicketList(params) {
    let { orderId } = params;
    // main tableIdWhen empty，不查询子表data，Return empty array directly
    if (orderId == null || isEmpty(orderId)) {
      return [];
    }
    return await ticketList(params);
  }

  /**
   * New事件
   */
  function handleCreate() {
    if (isEmpty(unref(orderId))) {
      $message.createMessage.warning('Please select an order information');
      return;
    }
    openModal(true, {
      isUpdate: false,
      showFooter: true,
    });
  }

  /**
   * Edit event
   */
  async function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }

  /**
   * delete事件
   */
  async function handleDelete(record) {
    await deleteTicket({ id: record.id }, reload);
  }

  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    await deleteBatchTicket({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }

  /**
   * successful callback
   */
  function handleSuccess() {
    reload();
  }

  /**
   * Action bar
   */
  function getTableAction(record) {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'delete',
        popConfirm: {
          title: '是否确认delete',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped></style>
