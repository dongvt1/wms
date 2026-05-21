<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="库存历史" :width="1200" :canFullscreen="false">
    <a-tabs v-model:activeKey="activeKey">
      <a-tab-pane key="transactions" tab="交易记录">
        <BasicTable @register="registerTransactionTable" :searchInfo="transactionSearchInfo" />
      </a-tab-pane>
      <a-tab-pane key="adjustments" tab="调整记录">
        <BasicTable @register="registerAdjustmentTable" :searchInfo="adjustmentSearchInfo" />
      </a-tab-pane>
    </a-tabs>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { defineComponent, ref, reactive, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable } from '/@/components/Table';
  import { transactionColumns, adjustmentColumns } from './inventory.data';
  import { inventoryApi } from './inventory.api';

  const emit = defineEmits(['success', 'register']);
  const activeKey = ref('transactions');
  const productId = ref('');
  
  const transactionSearchInfo = reactive({ productId: '' });
  const adjustmentSearchInfo = reactive({ productId: '' });

  const [registerTransactionTable, { reload: reloadTransactions, setProps: setTransactionProps }] = useTable({
    title: '库存交易记录',
    api: () => inventoryApi.getTransactions({ productId: productId.value }),
    columns: transactionColumns,
    formConfig: {
      labelWidth: 120,
      schemas: [
        {
          field: 'transactionType',
          label: '交易类型',
          component: 'Select',
          componentProps: {
            options: [
              { label: '全部', value: '' },
              { label: '入库', value: 'IN' },
              { label: '出库', value: 'OUT' },
              { label: '调整', value: 'ADJUST' },
            ],
            placeholder: '请选择交易类型',
          },
        },
        {
          field: 'dateRange',
          label: '交易日期',
          component: 'RangePicker',
          componentProps: {
            style: { width: '100%' },
          },
        },
      ],
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    pagination: {
      pageSize: 10,
    },
  });

  const [registerAdjustmentTable, { reload: reloadAdjustments, setProps: setAdjustmentProps }] = useTable({
    title: '库存调整记录',
    api: () => inventoryApi.getAdjustments({ productId: productId.value }),
    columns: adjustmentColumns,
    formConfig: {
      labelWidth: 120,
      schemas: [
        {
          field: 'dateRange',
          label: '调整日期',
          component: 'RangePicker',
          componentProps: {
            style: { width: '100%' },
          },
        },
      ],
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    pagination: {
      pageSize: 10,
    },
  });

  const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    productId.value = data?.productId || '';
    
    // Update search info with product ID
    transactionSearchInfo.productId = productId.value;
    adjustmentSearchInfo.productId = productId.value;
    
    // Reload data
    reloadTransactions();
    reloadAdjustments();
  });
</script>

<script lang="ts">
export default defineComponent({
  name: 'InventoryHistoryModal',
});
</script>