<template>
  <div class="p-4">
    <!-- Statistics Bar -->
    <a-row :gutter="16" class="mb-4" v-if="stats">
      <a-col :span="4" v-for="item in statItems" :key="item.key">
        <a-statistic :title="item.label" :value="stats[item.key]" class="stat-card" />
      </a-col>
    </a-row>

    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">Tạo lệnh sản xuất</a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
      </template>
      <template #priority="{ record }">
        <a-tag :color="priorityColor(record.priority)">{{ priorityLabel(record.priority) }}</a-tag>
      </template>
    </BasicTable>

    <WorkOrderModal @register="registerModal" @success="handleSuccess" />
    <WorkOrderDetailModal @register="registerDetailModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="work-order-list" setup>
  import { ref, onMounted } from 'vue';
  import { BasicTable, TableAction, useTable } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { workOrderApi } from '/@/api/warehouse/workOrder';
  import { useMessage } from '/@/hooks/web/useMessage';
  import WorkOrderModal from './WorkOrderModal.vue';
  import WorkOrderDetailModal from './WorkOrderDetailModal.vue';

  const { createMessage, createConfirm } = useMessage();
  const stats = ref<any>(null);

  const statItems = [
    { key: 'totalOrders', label: 'Tổng lệnh' },
    { key: 'draftCount', label: 'Nháp' },
    { key: 'plannedCount', label: 'Đã lên kế hoạch' },
    { key: 'inProgressCount', label: 'Đang sản xuất' },
    { key: 'completedCount', label: 'Hoàn thành' },
    { key: 'cancelledCount', label: 'Đã hủy' },
  ];

  const columns = [
    { title: 'Mã lệnh', dataIndex: 'orderCode', width: 140 },
    { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 120 },
    { title: 'Ưu tiên', dataIndex: 'priority', slots: { customRender: 'priority' }, width: 100 },
    { title: 'SL kế hoạch', dataIndex: 'plannedQuantity', width: 120 },
    { title: 'SL thực tế', dataIndex: 'actualQuantity', width: 110 },
    { title: 'Ngày BĐ KH', dataIndex: 'plannedStartDate', width: 130 },
    { title: 'Ngày KT KH', dataIndex: 'plannedEndDate', width: 130 },
    { title: 'Ghi chú', dataIndex: 'notes', width: 180 },
  ];

  const searchFormSchema = [
    { field: 'orderCode', label: 'Mã lệnh', component: 'Input', colProps: { span: 6 } },
    {
      field: 'status', label: 'Trạng thái', component: 'Select', colProps: { span: 6 },
      componentProps: {
        options: [
          { label: 'Nháp', value: 'draft' },
          { label: 'Đã lên kế hoạch', value: 'planned' },
          { label: 'Đang sản xuất', value: 'in_progress' },
          { label: 'Hoàn thành', value: 'completed' },
          { label: 'Đã hủy', value: 'cancelled' },
        ],
      },
    },
    {
      field: 'priority', label: 'Ưu tiên', component: 'Select', colProps: { span: 6 },
      componentProps: {
        options: [
          { label: 'Thấp', value: 'low' },
          { label: 'Bình thường', value: 'normal' },
          { label: 'Cao', value: 'high' },
          { label: 'Khẩn cấp', value: 'urgent' },
        ],
      },
    },
  ];

  const [registerTable, { reload }] = useTable({
    title: 'Lệnh sản xuất',
    api: workOrderApi.list,
    columns,
    formConfig: { labelWidth: 90, schemas: searchFormSchema, autoSubmitOnEnter: true },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: { width: 220, title: 'Thao tác', dataIndex: 'action', slots: { customRender: 'action' }, fixed: 'right' },
  });

  const [registerModal, { openModal }] = useModal();
  const [registerDetailModal, { openModal: openDetailModal }] = useModal();

  function getTableAction(record) {
    const actions: any[] = [
      { label: 'Chi tiết', onClick: () => openDetailModal(true, { id: record.id }) },
    ];
    if (record.status === 'draft' || record.status === 'planned') {
      actions.push({ label: 'Sửa', onClick: () => openModal(true, { record, isUpdate: true }) });
      actions.push({
        label: 'Bắt đầu SX', type: 'primary',
        onClick: () => handleStart(record),
      });
    }
    if (record.status === 'in_progress') {
      actions.push({
        label: 'Hoàn thành', type: 'primary',
        onClick: () => handleComplete(record),
      });
    }
    if (!['completed', 'cancelled'].includes(record.status)) {
      actions.push({
        label: 'Hủy', color: 'error',
        popConfirm: { title: 'Xác nhận hủy lệnh sản xuất?', confirm: () => handleCancel(record) },
      });
    }
    return actions;
  }

  function handleAdd() {
    openModal(true, { isUpdate: false });
  }

  async function handleStart(record) {
    const res: any = await workOrderApi.start(record.id);
    if (res) {
      createMessage.success('Bắt đầu sản xuất thành công!');
      handleSuccess();
    }
  }

  async function handleComplete(record) {
    createConfirm({
      iconType: 'info',
      title: 'Hoàn thành sản xuất',
      content: `Nhập số lượng thực tế sản xuất được (kế hoạch: ${record.plannedQuantity}):`,
      onOk: async () => {
        const qty = prompt(`Số lượng thực tế (kế hoạch: ${record.plannedQuantity}):`, record.plannedQuantity);
        if (qty && !isNaN(Number(qty))) {
          await workOrderApi.complete(record.id, Number(qty));
          createMessage.success('Hoàn thành sản xuất thành công! Kho đã được cập nhật.');
          handleSuccess();
        }
      },
    });
  }

  async function handleCancel(record) {
    await workOrderApi.cancel(record.id, 'Hủy theo yêu cầu');
    createMessage.success('Đã hủy lệnh sản xuất');
    handleSuccess();
  }

  function handleSuccess() {
    reload();
    loadStats();
  }

  async function loadStats() {
    try {
      const result: any = await workOrderApi.getStatistics();
      stats.value = result;
    } catch (e) {}
  }

  onMounted(() => loadStats());

  function statusColor(status: string) {
    const map: Record<string, string> = {
      draft: 'default', planned: 'blue', in_progress: 'orange',
      completed: 'green', cancelled: 'red',
    };
    return map[status] || 'default';
  }

  function statusLabel(status: string) {
    const map: Record<string, string> = {
      draft: 'Nháp', planned: 'Kế hoạch', in_progress: 'Đang SX',
      completed: 'Hoàn thành', cancelled: 'Đã hủy',
    };
    return map[status] || status;
  }

  function priorityColor(p: string) {
    const map: Record<string, string> = { low: 'default', normal: 'blue', high: 'orange', urgent: 'red' };
    return map[p] || 'default';
  }

  function priorityLabel(p: string) {
    const map: Record<string, string> = { low: 'Thấp', normal: 'Bình thường', high: 'Cao', urgent: 'Khẩn cấp' };
    return map[p] || p;
  }
</script>

<style scoped>
.stat-card {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}
</style>
