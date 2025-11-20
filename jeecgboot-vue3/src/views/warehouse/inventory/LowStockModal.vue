<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="低库存预警" :width="1000" :canFullscreen="false">
    <BasicTable @register="registerTable" :searchInfo="searchInfo">
      <template #toolbar>
        <a-button type="primary" @click="handleResolveAll"> 批量解决 </a-button>
        <a-button type="primary" @click="handleDismissAll"> 批量忽略 </a-button>
        <a-button type="primary" @click="handleExport"> 导出 </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:check-line',
                tooltip: '解决',
                onClick: handleResolve.bind(null, record),
              },
              {
                icon: 'ant-design:close-circle-outlined',
                tooltip: '忽略',
                onClick: handleDismiss.bind(null, record),
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { defineComponent, ref, reactive, onMounted } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { alertColumns } from './inventory.data';
  import { inventoryApi } from './inventory.api';

  const emit = defineEmits(['success', 'register']);
  const searchInfo = reactive({});
  const lowStockProducts = ref([]);

  const [registerTable, { reload, getSelectRows, clearSelectedRowKeys }] = useTable({
    title: '低库存产品',
    api: inventoryApi.getAlerts,
    columns: alertColumns,
    rowKey: 'id',
    formConfig: {
      labelWidth: 120,
      schemas: [
        {
          field: 'productCode',
          label: '产品编码',
          component: 'Input',
          componentProps: {
            placeholder: '请输入产品编码',
          },
        },
        {
          field: 'productName',
          label: '产品名称',
          component: 'Input',
          componentProps: {
            placeholder: '请输入产品名称',
          },
        },
        {
          field: 'alertType',
          label: '预警类型',
          component: 'Select',
          componentProps: {
            options: [
              { label: '全部', value: '' },
              { label: '低库存', value: 'LOW_STOCK' },
              { label: '缺货', value: 'OUT_OF_STOCK' },
            ],
            placeholder: '请选择预警类型',
          },
        },
      ],
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: {
      width: 120,
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
    },
    rowSelection: {
      type: 'checkbox',
    },
  });

  const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    lowStockProducts.value = data?.lowStockProducts || [];
    reload();
  });

  const { createMessage } = useMessage();

  // 解决单个预警
  async function handleResolve(record: any) {
    try {
      await inventoryApi.resolveAlert({ alertId: record.id });
      createMessage.success('预警已解决');
      reload();
    } catch (error) {
      createMessage.error('解决预警失败');
    }
  }

  // 忽略单个预警
  async function handleDismiss(record: any) {
    try {
      await inventoryApi.dismissAlert({ alertId: record.id });
      createMessage.success('预警已忽略');
      reload();
    } catch (error) {
      createMessage.error('忽略预警失败');
    }
  }

  // 批量解决预警
  async function handleResolveAll() {
    const selectedRows = getSelectRows();
    if (selectedRows.length === 0) {
      createMessage.warning('请选择要解决的预警');
      return;
    }

    try {
      const alertIds = selectedRows.map(row => row.id);
      await inventoryApi.resolveAlertsBatch({ alertIds });
      createMessage.success(`已解决 ${alertIds.length} 个预警`);
      clearSelectedRowKeys();
      reload();
    } catch (error) {
      createMessage.error('批量解决预警失败');
    }
  }

  // 批量忽略预警
  async function handleDismissAll() {
    const selectedRows = getSelectRows();
    if (selectedRows.length === 0) {
      createMessage.warning('请选择要忽略的预警');
      return;
    }

    try {
      const alertIds = selectedRows.map(row => row.id);
      await inventoryApi.dismissAlertsBatch({ alertIds });
      createMessage.success(`已忽略 ${alertIds.length} 个预警`);
      clearSelectedRowKeys();
      reload();
    } catch (error) {
      createMessage.error('批量忽略预警失败');
    }
  }

  // 导出预警数据
  function handleExport() {
    const selectedRows = getSelectRows();
    if (selectedRows.length === 0) {
      createMessage.warning('请选择要导出的预警');
      return;
    }

    const alertIds = selectedRows.map(row => row.id);
    // 这里可以调用导出API
    createMessage.success(`导出 ${alertIds.length} 个预警数据`);
  }
</script>

<script lang="ts">
export default defineComponent({
  name: 'LowStockModal',
});
</script>