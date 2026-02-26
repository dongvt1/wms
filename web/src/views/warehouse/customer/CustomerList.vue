<template>
  <div>
    <BasicTable @register="registerTable" :actionColumn="actionColumn">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> New Customer </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: 'Edit',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                tooltip: 'Delete',
                color: 'error',
                onClick: handleDelete.bind(null, record),
              },
              {
                icon: 'ant-design:history-outlined',
                tooltip: 'Order History',
                onClick: handleOrderHistory.bind(null, record),
              },
              {
                icon: 'ant-design:bar-chart-outlined',
                tooltip: 'Statistics',
                onClick: handleStatistics.bind(null, record),
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>

    <CustomerModal @register="registerModal" @success="handleSuccess" />
    <CustomerOrderHistoryModal @register="registerOrderHistoryModal" />
    <CustomerStatisticsModal @register="registerStatisticsModal" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { customerApi } from './customer.api';
  import { customerColumns, customerSearchFormSchema } from './customer.data';
  import CustomerModal from './CustomerModal.vue';
  import CustomerOrderHistoryModal from './CustomerOrderHistoryModal.vue';
  import CustomerStatisticsModal from './CustomerStatisticsModal.vue';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerOrderHistoryModal, { openModal: openOrderHistoryModal }] = useModal();
  const [registerStatisticsModal, { openModal: openStatisticsModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: 'Customer List',
    api: customerApi.list,
    columns: customerColumns,
    formConfig: {
      labelWidth: 120,
      schemas: customerSearchFormSchema,
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: false,
    actionColumn: {
      width: 200,
      title: 'Action',
      dataIndex: 'action',
      fixed: 'right',
    },
  });

  const actionColumn = reactive({
    width: 200,
    title: 'Action',
    dataIndex: 'action',
    fixed: 'right' as const,
  });

  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }

  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  async function handleDelete(record: Recordable) {
    try {
      await customerApi.delete(record.id);
      createMessage.success('Customer deleted successfully');
      reload();
    } catch (error) {
      createMessage.error('Failed to delete customer');
    }
  }

  function handleOrderHistory(record: Recordable) {
    openOrderHistoryModal(true, {
      record,
    });
  }

  function handleStatistics(record: Recordable) {
    openStatisticsModal(true, {
      record,
    });
  }

  function handleSuccess() {
    reload();
  }
</script>