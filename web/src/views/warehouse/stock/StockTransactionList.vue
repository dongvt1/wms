<template>
  <div>
    <BasicTable @register="registerTable" :actionColumn="actionColumn">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> New Transaction </a-button>
        <a-button type="primary" @click="handleStockIn"> Stock In </a-button>
        <a-button type="primary" @click="handleStockOut"> Stock Out </a-button>
        <a-button type="primary" @click="handleStockTransfer"> Stock Transfer </a-button>
        <a-button type="primary" @click="handleReport"> Transaction Reports </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: 'Edit',
                onClick: handleEdit.bind(null, record),
                ifShow: record.status === 'PENDING',
              },
              {
                icon: 'ant-design:check-circle-outlined',
                tooltip: 'Approve',
                onClick: handleApprove.bind(null, record),
                ifShow: record.status === 'PENDING',
              },
              {
                icon: 'ant-design:close-circle-outlined',
                tooltip: 'Cancel',
                onClick: handleCancel.bind(null, record),
                ifShow: record.status === 'PENDING',
              },
              {
                icon: 'ant-design:printer-outlined',
                tooltip: 'Print',
                onClick: handlePrint.bind(null, record),
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>

    <StockTransactionModal @register="registerModal" @success="handleSuccess" />
    <TransactionDetailModal @register="registerDetailModal" />
    <TransactionReportModal @register="registerReportModal" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import type { Recordable } from '/@/utils';
  import { stockTransactionApi } from './stockTransaction.api';
  import { stockTransactionColumns, stockTransactionSearchFormSchema } from './stockTransaction.data';
  import StockTransactionModal from './StockTransactionModal.vue';
  import TransactionDetailModal from './TransactionDetailModal.vue';
  import { printTransaction } from './transactionPrintService';

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerDetailModal, { openModal: openDetailModal }] = useModal();
  const [registerReportModal, { openModal: openReportModal }] = useModal();

  const [registerTable, { reload, getForm }] = useTable({
    title: 'Stock Transaction List',
    api: stockTransactionApi.list,
    columns: stockTransactionColumns,
    formConfig: {
      labelWidth: 120,
      schemas: stockTransactionSearchFormSchema,
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: false,
    actionColumn: {
      width: 180,
      title: 'Action',
      dataIndex: 'action',
      fixed: 'right',
    },
  });

  const actionColumn = reactive({
    width: 180,
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

  function handleStockIn() {
    openModal(true, {
      isUpdate: false,
      transactionType: 'IN',
    });
  }

  function handleStockOut() {
    openModal(true, {
      isUpdate: false,
      transactionType: 'OUT',
    });
  }

  function handleStockTransfer() {
    openModal(true, {
      isUpdate: false,
      transactionType: 'TRANSFER',
    });
  }

  function handleReport() {
    openReportModal(true, {});
  }

  async function handleApprove(record: Recordable) {
    try {
      await stockTransactionApi.approve(record.id, 'admin'); // Pass approvedBy parameter
      createMessage.success('Transaction approved successfully');
      reload();
    } catch (error) {
      createMessage.error('Failed to approve transaction');
    }
  }

  async function handleCancel(record: Recordable) {
    try {
      await stockTransactionApi.cancel(record.id, 'Cancelled by user'); // Pass cancelReason parameter
      createMessage.success('Transaction cancelled successfully');
      reload();
    } catch (error) {
      createMessage.error('Failed to cancel transaction');
    }
  }

  async function handlePrint(record: Recordable) {
    try {
      const success = await printTransaction(record.id, { save: true });
      if (success) {
        createMessage.success('Transaction printed successfully');
      } else {
        createMessage.error('Failed to print transaction');
      }
    } catch (error) {
      createMessage.error('Failed to print transaction');
    }
  }

  function handleSuccess() {
    reload();
  }

  onMounted(() => {
    // Initialize any required data
  });
</script>