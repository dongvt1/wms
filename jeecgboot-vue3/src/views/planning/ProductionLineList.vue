<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">Thêm dây chuyền</a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
      </template>
    </BasicTable>
    <ProductionLineModal @register="registerModal" @success="reload" />
  </div>
</template>

<script lang="ts" name="production-line-list" setup>
  import { BasicTable, TableAction, useTable } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { productionLineApi } from '/@/api/warehouse/productionLine';
  import ProductionLineModal from './ProductionLineModal.vue';

  const columns = [
    { title: 'Mã dây chuyền', dataIndex: 'lineCode', width: 140 },
    { title: 'Tên dây chuyền', dataIndex: 'lineName', width: 200 },
    { title: 'Năng suất/ngày', dataIndex: 'capacityPerDay', width: 130 },
    { title: 'Đơn vị', dataIndex: 'unit', width: 90 },
    { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 100 },
    { title: 'Ngày tạo', dataIndex: 'createTime', width: 160 },
  ];

  const searchFormSchema = [
    { field: 'lineCode', label: 'Mã', component: 'Input', colProps: { span: 6 } },
    { field: 'lineName', label: 'Tên', component: 'Input', colProps: { span: 6 } },
    {
      field: 'status', label: 'Trạng thái', component: 'Select', colProps: { span: 6 },
      componentProps: {
        options: [
          { label: 'Hoạt động', value: 'active' },
          { label: 'Ngừng', value: 'inactive' },
          { label: 'Bảo trì', value: 'maintenance' },
        ],
      },
    },
  ];

  const [registerTable, { reload }] = useTable({
    title: 'Dây chuyền sản xuất',
    api: productionLineApi.list,
    columns,
    formConfig: { labelWidth: 90, schemas: searchFormSchema, autoSubmitOnEnter: true },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: { width: 160, title: 'Thao tác', dataIndex: 'action', slots: { customRender: 'action' }, fixed: 'right' },
  });

  const [registerModal, { openModal }] = useModal();

  function getTableAction(record) {
    return [
      { label: 'Sửa', onClick: () => openModal(true, { record, isUpdate: true }) },
      {
        label: 'Xóa', color: 'error',
        popConfirm: { title: 'Xác nhận xóa dây chuyền?', confirm: () => handleDelete(record) },
      },
    ];
  }

  function handleAdd() {
    openModal(true, { isUpdate: false });
  }

  async function handleDelete(record) {
    await productionLineApi.delete({ id: record.id });
    reload();
  }

  function statusColor(status: string) {
    const map = { active: 'green', inactive: 'red', maintenance: 'orange' };
    return map[status] || 'default';
  }

  function statusLabel(status: string) {
    const map = { active: 'Hoạt động', inactive: 'Ngừng', maintenance: 'Bảo trì' };
    return map[status] || status;
  }
</script>
