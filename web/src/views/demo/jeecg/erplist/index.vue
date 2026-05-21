<template>
  <div>
    <!--main table table-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
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
    <!--Subtable tabletab-->
    <a-tabs defaultActiveKey="1" style="margin: 10px">
      <a-tab-pane tab="Customer information" key="1">
        <JeecgOrderCustomerList />
      </a-tab-pane>
      <a-tab-pane tab="Air ticket information" key="2" forceRender>
        <JeecgOrderTicketList />
      </a-tab-pane>
    </a-tabs>
  </div>
  <!-- form area -->
  <JeecgOrderModal @register="registerModal" @success="handleSuccess"></JeecgOrderModal>
</template>

<script lang="ts" name="tab-list" setup>
  //tsgrammar
  import { ref, computed, unref, watch, provide } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useModal } from '/@/components/Modal';
  import JeecgOrderModal from './components/JeecgOrderModal.vue';
  import JeecgOrderCustomerList from './JeecgOrderCustomerList.vue';
  import JeecgOrderTicketList from './JeecgOrderTicketList.vue';
  import { columns, searchFormSchema } from './erplist.data';
  import { list, deleteOne, batchDelete } from './erplist.api';

  //Pop-up windowmodel
  const [registerModal, { openModal }] = useModal();

  // List page public parameters、method
  const { tableContext } = useListPage({
    tableProps: {
      api: list,
      tableSetting:{
        cacheKey:'erp_main'
      },
      columns: columns,
      canResize: false,
      rowSelection: { type: 'radio' },
      formConfig: {
        schemas: searchFormSchema,
      },
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
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

  const orderId = computed(() => (unref(selectedRowKeys).length > 0 ? unref(selectedRowKeys)[0] : ''));
  //Issue orderId,Subcomponent receives
  provide('orderId', orderId);

  /**
   * New事件
   */
  function handleCreate() {
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
   * Details
   */
  async function handleDetail(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }

  /**
   * delete事件
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, reload);
  }

  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, () => {
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
