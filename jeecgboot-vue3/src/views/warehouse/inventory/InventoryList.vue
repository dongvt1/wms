<template>
  <div>
    <!-- 搜索区域 -->
    <BasicTable @register="registerTable" :searchInfo="searchInfo">
      <template #toolbar>
        <a-button type="primary" @click="handleAdd"> 新增 </a-button>
        <a-button type="primary" @click="handleExport"> 导出 </a-button>
        <a-button type="primary" @click="handleLowStock"> 低库存预警 </a-button>
        <a-button type="primary" @click="handleReport"> 库存报告 </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: '编辑',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:adjustment-outlined',
                tooltip: '调整库存',
                onClick: handleAdjust.bind(null, record),
              },
              {
                icon: 'ant-design:history-outlined',
                tooltip: '查看历史',
                onClick: handleHistory.bind(null, record),
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>

    <!-- 库存调整弹窗 -->
    <InventoryAdjustmentModal @register="registerAdjustmentModal" @success="handleSuccess" />
    
    <!-- 库存历史弹窗 -->
    <InventoryHistoryModal @register="registerHistoryModal" />
    
    <!-- 库存报告弹窗 -->
    <InventoryReportModal @register="registerReportModal" />
    
    <!-- 低库存预警弹窗 -->
    <LowStockModal @register="registerLowStockModal" />
  </div>
</template>

<script lang="ts" setup>
  import { defineComponent, reactive, ref, onMounted } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './inventory.data';
  import { inventoryApi } from './inventory.api';
  import InventoryAdjustmentModal from './InventoryAdjustmentModal.vue';
  import InventoryHistoryModal from './InventoryHistoryModal.vue';
  import InventoryReportModal from './InventoryReportModal.vue';
  import LowStockModal from './LowStockModal.vue';
  import { productApi } from '../product/product.api';

  const [registerTable, { reload, getForm }] = useTable({
    title: '库存管理',
    api: inventoryApi.list,
    columns,
    formConfig: {
      labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: {
      width: 180,
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
    },
  });

  const [registerAdjustmentModal, { openModal: openAdjustmentModal }] = useModal();
  const [registerHistoryModal, { openModal: openHistoryModal }] = useModal();
  const [registerReportModal, { openModal: openReportModal }] = useModal();
  const [registerLowStockModal, { openModal: openLowStockModal }] = useModal();

  const { createMessage } = useMessage();
  const searchInfo = reactive({});
  const productOptions = ref([]);

  onMounted(() => {
    fetchProductOptions();
  });

  // 获取产品选项
  async function fetchProductOptions() {
    try {
      const result = await productApi.list({ pageSize: 999 });
      productOptions.value = result.records.map((item) => ({
        label: `${item.code} - ${item.name}`,
        value: item.id,
      }));
    } catch (error) {
      console.error('Failed to fetch product options:', error);
    }
  }

  // 新增
  function handleAdd() {
    openAdjustmentModal(true, {
      isUpdate: false,
      productOptions: productOptions.value,
    });
  }

  // 编辑
  function handleEdit(record: any) {
    openAdjustmentModal(true, {
      record,
      isUpdate: true,
      productOptions: productOptions.value,
    });
  }

  // 调整库存
  function handleAdjust(record: any) {
    openAdjustmentModal(true, {
      record,
      isUpdate: true,
      isAdjustment: true,
      productOptions: productOptions.value,
    });
  }

  // 查看历史
  function handleHistory(record: any) {
    openHistoryModal(true, {
      productId: record.productId,
    });
  }

  // 导出
  function handleExport() {
    const form = getForm();
    inventoryApi.export(form.getFieldsValue());
  }

  // 低库存预警
  async function handleLowStock() {
    try {
      const result = await inventoryApi.getLowStock();
      openLowStockModal(true, {
        lowStockProducts: result,
      });
    } catch (error) {
      createMessage.error('获取低库存产品失败');
    }
  }

  // 库存报告
  async function handleReport() {
    try {
      const result = await inventoryApi.getReport();
      openReportModal(true, {
        reportData: result,
      });
    } catch (error) {
      createMessage.error('获取库存报告失败');
    }
  }

  // 操作成功
  function handleSuccess() {
    reload();
  }
</script>

<script lang="ts">
export default {
  name: 'InventoryList',
};
</script>