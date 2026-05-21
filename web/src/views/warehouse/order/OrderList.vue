<template>
  <div>
    <BasicTable @register="registerTable" :actionColumn="actionColumn">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> New Order </a-button>
        <a-button type="default" @click="handleExport"> Export </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: 'Edit',
                onClick: handleEdit.bind(null, record),
                disabled: !['PENDING'].includes(record.status),
              },
              {
                icon: 'ant-design:close-circle-outlined',
                tooltip: 'Cancel',
                color: 'error',
                onClick: handleCancel.bind(null, record),
                disabled: !['PENDING', 'CONFIRMED'].includes(record.status),
              },
              {
                icon: 'ant-design:check-circle-outlined',
                tooltip: 'Confirm',
                onClick: handleConfirm.bind(null, record),
                disabled: record.status !== 'PENDING',
              },
              {
                icon: 'ant-design:car-outlined',
                tooltip: 'Ship',
                onClick: handleShip.bind(null, record),
                disabled: record.status !== 'CONFIRMED',
              },
              {
                icon: 'ant-design:check-outlined',
                tooltip: 'Complete',
                onClick: handleComplete.bind(null, record),
                disabled: record.status !== 'SHIPPING',
              },
              {
                icon: 'ant-design:printer-outlined',
                tooltip: 'Print',
                onClick: handlePrint.bind(null, record),
              },
              {
                icon: 'ant-design:history-outlined',
                tooltip: 'Status History',
                onClick: handleStatusHistory.bind(null, record),
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>

    <OrderModal @register="registerModal" @success="handleSuccess" />
    <OrderStatusHistoryModal @register="registerStatusHistoryModal" />
    <OrderCancelModal @register="registerCancelModal" @success="handleSuccess" />
    <OrderStatusUpdateModal @register="registerStatusUpdateModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { orderApi } from './order.api';
  import { orderColumns, orderSearchFormSchema } from './order.data';
  import OrderModal from './OrderModal.vue';
  import OrderStatusHistoryModal from './OrderStatusHistoryModal.vue';
  import OrderCancelModal from './OrderCancelModal.vue';
  import OrderStatusUpdateModal from './OrderStatusUpdateModal.vue';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerStatusHistoryModal, { openModal: openStatusHistoryModal }] = useModal();
  const [registerCancelModal, { openModal: openCancelModal }] = useModal();
  const [registerStatusUpdateModal, { openModal: openStatusUpdateModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: 'Order List',
    api: orderApi.list,
    columns: orderColumns,
    formConfig: {
      labelWidth: 120,
      schemas: orderSearchFormSchema,
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

  function handleCancel(record: Recordable) {
    openCancelModal(true, {
      record,
    });
  }

  function handleConfirm(record: Recordable) {
    openStatusUpdateModal(true, {
      record,
      newStatus: 'CONFIRMED',
      title: 'Confirm Order',
      reasonRequired: false,
    });
  }

  function handleShip(record: Recordable) {
    openStatusUpdateModal(true, {
      record,
      newStatus: 'SHIPPING',
      title: 'Ship Order',
      reasonRequired: false,
    });
  }

  function handleComplete(record: Recordable) {
    openStatusUpdateModal(true, {
      record,
      newStatus: 'COMPLETED',
      title: 'Complete Order',
      reasonRequired: false,
    });
  }

  function handlePrint(record: Recordable) {
    orderApi.print(record.id);
  }

  function handleStatusHistory(record: Recordable) {
    openStatusHistoryModal(true, {
      record,
    });
  }

  function handleExport() {
    orderApi.exportXls();
  }

  function handleSuccess() {
    reload();
  }
</script>