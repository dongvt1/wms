<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">+ Thêm vật tư</a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'green' : 'red'">
          {{ record.status === 1 ? 'Đang dùng' : 'Ngừng' }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <MaterialModal @register="registerModal" @success="reload" />
  </div>
</template>

<script lang="ts" name="material-list" setup>
import { BasicTable, TableAction, useTable } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { materialApi } from '/@/api/common/material';
import MaterialModal from './MaterialModal.vue';

const { createMessage } = useMessage();

const columns = [
  { title: 'Mã vật tư', dataIndex: 'code', width: 130 },
  { title: 'Tên vật tư', dataIndex: 'name', width: 200 },
  { title: 'Đơn vị', dataIndex: 'unit', width: 80 },
  { title: 'Giá', dataIndex: 'price', width: 110 },
  { title: 'Tồn kho', dataIndex: 'currentStock', width: 90 },
  { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 110 },
  { title: 'Ngày tạo', dataIndex: 'createTime', width: 150 },
];

const searchFormSchema = [
  { field: 'code', label: 'Mã vật tư', component: 'Input', colProps: { span: 6 } },
  { field: 'name', label: 'Tên vật tư', component: 'Input', colProps: { span: 6 } },
  {
    field: 'status', label: 'Trạng thái', component: 'Select', colProps: { span: 6 },
    componentProps: {
      options: [
        { label: 'Đang dùng', value: 1 },
        { label: 'Ngừng', value: 0 },
      ],
    },
  },
];

const [registerTable, { reload }] = useTable({
  title: 'Danh sách nguyên vật liệu',
  api: materialApi.list,
  columns,
  formConfig: { labelWidth: 90, schemas: searchFormSchema, autoSubmitOnEnter: true },
  useSearchForm: true,
  showTableSetting: true,
  bordered: true,
  showIndexColumn: true,
  actionColumn: {
    width: 140, title: 'Thao tác', dataIndex: 'action',
    slots: { customRender: 'action' }, fixed: 'right',
  },
});

const [registerModal, { openModal }] = useModal();

function getActions(record: any) {
  return [
    { label: 'Sửa', onClick: () => openModal(true, { record, isUpdate: true }) },
    {
      label: 'Xóa', color: 'error',
      popConfirm: { title: 'Xác nhận xóa vật tư?', confirm: () => handleDelete(record) },
    },
  ];
}

function handleAdd() {
  openModal(true, { isUpdate: false });
}

async function handleDelete(record: any) {
  await materialApi.delete({ id: record.id });
  createMessage.success('Xóa vật tư thành công!');
  reload();
}
</script>
