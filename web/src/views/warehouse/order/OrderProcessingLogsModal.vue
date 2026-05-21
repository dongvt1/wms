<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Order Processing Logs" :showOkBtn="false" cancelText="Close" :width="1000">
    <BasicTable @register="registerTable" :pagination="false" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { orderApi } from './order.api';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const [registerModal, { setModalProps }] = useModalInner((data) => {
    setModalProps({ confirmLoading: false });
    if (data && data.record) {
      loadProcessingLogs(data.record.id);
    }
  });

  const [registerTable, { reload: tableReload, setTableData }] = useTable({
    title: 'Processing Logs',
    columns: [
      {
        title: 'Action',
        dataIndex: 'action',
        width: 120,
        customRender: ({ text }) => {
          const actionMap: Recordable<string> = {
            'CREATE': 'Create',
            'CONFIRM': 'Confirm',
            'CANCEL': 'Cancel',
            'SHIP': 'Ship',
            'COMPLETE': 'Complete',
            'STATUS_UPDATE': 'Status Update',
          };
          return actionMap[text] || text;
        },
      },
      {
        title: 'Details',
        dataIndex: 'details',
        width: 200,
        ellipsis: true,
      },
      {
        title: 'Status',
        dataIndex: 'status',
        width: 100,
        customRender: ({ text }) => {
          const color = text === 'SUCCESS' ? 'green' : text === 'FAILED' ? 'red' : 'orange';
          return `<a-tag color="${color}">${text}</a-tag>`;
        },
      },
      {
        title: 'Error Message',
        dataIndex: 'errorMessage',
        width: 200,
        ellipsis: true,
      },
      {
        title: 'User ID',
        dataIndex: 'userId',
        width: 100,
      },
      {
        title: 'Processing Time',
        dataIndex: 'processingTime',
        width: 120,
        customRender: ({ text }) => {
          return text ? `${text}ms` : '-';
        },
      },
      {
        title: 'Create Time',
        dataIndex: 'createTime',
        width: 150,
        customRender: ({ text }) => {
          return text ? new Date(text).toLocaleString() : '-';
        },
      },
    ],
    bordered: true,
    showIndexColumn: false,
    pagination: false,
    canResize: false,
  });

  function loadProcessingLogs(orderId: string) {
    orderApi.getProcessingLogs(orderId).then((res) => {
      setTableData(res);
    });
  }
</script>