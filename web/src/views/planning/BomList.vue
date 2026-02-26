<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">Thêm BOM</a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="record.status === 'active' ? 'green' : 'red'">
          {{ record.status === 'active' ? 'Đang dùng' : 'Ngừng' }}
        </a-tag>
      </template>
    </BasicTable>
    <BomModal @register="registerModal" @success="reload" />
  </div>
</template>

<script lang="ts" name="bom-list" setup>
  import { BasicTable, TableAction, useTable } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { bomApi } from '/@/api/warehouse/bom';
  import { useMessage } from '/@/hooks/web/useMessage';
  import BomModal from './BomModal.vue';

  const { createMessage } = useMessage();

  const columns = [
    { title: 'Mã BOM', dataIndex: 'bomCode', width: 130 },
    { title: 'Tên BOM', dataIndex: 'bomName', width: 220 },
    { title: 'Phiên bản', dataIndex: 'version', width: 90 },
    { title: 'SL thành phẩm ĐR', dataIndex: 'outputQuantity', width: 150 },
    { title: 'Đơn vị', dataIndex: 'unit', width: 80 },
    { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 100 },
    { title: 'Ngày tạo', dataIndex: 'createTime', width: 160 },
  ];

  const searchFormSchema = [
    { field: 'bomCode', label: 'Mã BOM', component: 'Input', colProps: { span: 6 } },
    { field: 'bomName', label: 'Tên BOM', component: 'Input', colProps: { span: 6 } },
    {
      field: 'status', label: 'Trạng thái', component: 'Select', colProps: { span: 6 },
      componentProps: {
        options: [
          { label: 'Đang dùng', value: 'active' },
          { label: 'Ngừng', value: 'inactive' },
        ],
      },
    },
  ];

  const [registerTable, { reload }] = useTable({
    title: 'Định mức nguyên vật liệu (BOM)',
    api: bomApi.list,
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
        popConfirm: { title: 'Xác nhận xóa BOM?', confirm: () => handleDelete(record) },
      },
    ];
  }

  function handleAdd() {
    openModal(true, { isUpdate: false });
  }

  async function handleDelete(record) {
    await bomApi.delete({ id: record.id });
    createMessage.success('Xóa BOM thành công!');
    reload();
  }
</script>
