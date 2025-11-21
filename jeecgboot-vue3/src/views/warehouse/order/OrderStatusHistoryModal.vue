<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Order Status History" :showOkBtn="false" cancelText="Close" :width="800">
    <BasicTable @register="registerTable" :pagination="false" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { orderStatusHistoryColumns } from './order.data';
  import { orderApi } from './order.api';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const [registerModal, { setModalProps }] = useModalInner((data) => {
    setModalProps({ confirmLoading: false });
    if (data && data.record) {
      loadStatusHistory(data.record.id);
    }
  });

  const [registerTable, { reload: tableReload, setTableData }] = useTable({
    title: 'Status History',
    columns: orderStatusHistoryColumns,
    bordered: true,
    showIndexColumn: false,
    pagination: false,
    canResize: false,
  });

  function loadStatusHistory(orderId: string) {
    orderApi.getStatusHistory(orderId).then((res) => {
      setTableData(res);
    });
  }
</script>