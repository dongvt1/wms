<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="库存报告" :width="1000" :canFullscreen="false">
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="库存概览" :bordered="false">
          <a-statistic title="产品总数" :value="reportData.totalProducts" />
          <a-divider />
          <a-statistic title="总库存量" :value="reportData.totalQuantity" />
          <a-divider />
          <a-statistic title="预留库存" :value="reportData.totalReserved" />
          <a-divider />
          <a-statistic title="可用库存" :value="reportData.totalAvailable" />
          <a-divider />
          <a-statistic title="低库存产品数" :value="reportData.lowStockCount" :value-style="{ color: '#cf1322' }" />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="库存价值" :bordered="false">
          <a-table 
            :columns="valueColumns" 
            :data-source="valueData" 
            :pagination="false" 
            size="small" 
          />
        </a-card>
      </a-col>
    </a-row>
    <a-divider />
    <a-card title="低库存产品" :bordered="false">
      <a-table 
        :columns="lowStockColumns" 
        :data-source="reportData.lowStockProducts" 
        :pagination="false" 
        size="small" 
      />
    </a-card>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { defineComponent, ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { inventoryApi } from './inventory.api';
  import { columns } from './inventory.data';

  const emit = defineEmits(['success', 'register']);
  const reportData = ref<any>({});
  const valueData = ref<any[]>([]);

  const valueColumns = [
    {
      title: '产品名称',
      dataIndex: 'productName',
      key: 'productName',
    },
    {
      title: '库存数量',
      dataIndex: 'quantity',
      key: 'quantity',
    },
    {
      title: '单价',
      dataIndex: 'price',
      key: 'price',
      customRender: ({ text }) => `$${Number(text).toFixed(2)}`,
    },
    {
      title: '总价值',
      dataIndex: 'totalValue',
      key: 'totalValue',
      customRender: ({ text }) => `$${Number(text).toFixed(2)}`,
    },
  ];

  const lowStockColumns = [
    {
      title: '产品ID',
      dataIndex: 'productId',
      key: 'productId',
      width: 120,
    },
    {
      title: '产品名称',
      dataIndex: 'productName',
      key: 'productName',
    },
    {
      title: '当前库存',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 100,
    },
    {
      title: '可用库存',
      dataIndex: 'availableQuantity',
      key: 'availableQuantity',
      width: 100,
      customRender: ({ text }) => {
        return `<span style="color: red;">${text}</span>`;
      },
    },
  ];

  const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    reportData.value = data?.reportData?.summary || {};
    
    // Fetch value report data
    try {
      const valueReport = await inventoryApi.getValueReport();
      valueData.value = valueReport;
    } catch (error) {
      console.error('Failed to fetch value report:', error);
    }
  });
</script>

<script lang="ts">
export default defineComponent({
  name: 'InventoryReportModal',
});
</script>