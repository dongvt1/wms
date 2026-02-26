<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">+ Thêm mẫu checklist</a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="record.status === 'active' ? 'green' : 'default'">
          {{ record.status === 'active' ? 'Đang dùng' : 'Ngừng' }}
        </a-tag>
      </template>
      <template #inspectionType="{ record }">
        <a-tag :color="record.inspectionType === 'iqc' ? 'blue' : 'orange'">
          {{ record.inspectionType?.toUpperCase() }}
        </a-tag>
      </template>
    </BasicTable>
    <ChecklistTemplateModal @register="registerModal" @success="reload" />
  </div>
</template>

<script lang="ts" name="checklist-template-list" setup>
  import { BasicTable, TableAction, useTable } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { qmsChecklistApi } from '/@/api/warehouse/qmsChecklist';
  import { useMessage } from '/@/hooks/web/useMessage';
  import ChecklistTemplateModal from './ChecklistTemplateModal.vue';

  const { createMessage } = useMessage();

  const [registerTable, { reload }] = useTable({
    title: 'Mẫu bộ tiêu chí kiểm tra',
    api: qmsChecklistApi.list,
    columns: [
      { title: 'Mã mẫu', dataIndex: 'templateCode', width: 130 },
      { title: 'Tên mẫu', dataIndex: 'templateName', width: 200 },
      { title: 'Loại KT', dataIndex: 'inspectionType', slots: { customRender: 'inspectionType' }, width: 100 },
      { title: 'Sản phẩm', dataIndex: 'productId', width: 140 },
      { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 110 },
      { title: 'Ghi chú', dataIndex: 'notes', width: 200 },
    ],
    formConfig: {
      labelWidth: 100,
      schemas: [
        { field: 'templateCode', label: 'Mã mẫu', component: 'Input', colProps: { span: 6 } },
        { field: 'templateName', label: 'Tên mẫu', component: 'Input', colProps: { span: 6 } },
        {
          field: 'inspectionType', label: 'Loại KT', component: 'Select', colProps: { span: 6 },
          componentProps: {
            options: [
              { label: 'IQC - Đầu vào', value: 'iqc' },
              { label: 'PQC - Sản xuất', value: 'pqc' },
            ],
          },
        },
        {
          field: 'status', label: 'Trạng thái', component: 'Select', colProps: { span: 6 },
          componentProps: {
            options: [
              { label: 'Đang dùng', value: 'active' },
              { label: 'Ngừng', value: 'inactive' },
            ],
          },
        },
      ],
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: { width: 160, title: 'Thao tác', dataIndex: 'action', slots: { customRender: 'action' }, fixed: 'right' },
  });

  const [registerModal, { openModal }] = useModal();

  function handleAdd() {
    openModal(true, { isUpdate: false });
  }

  function getActions(record: any) {
    return [
      { label: 'Sửa', onClick: () => openModal(true, { record, isUpdate: true }) },
      {
        label: 'Xóa', color: 'error',
        popConfirm: {
          title: 'Xác nhận xóa mẫu checklist?',
          confirm: async () => {
            await qmsChecklistApi.delete({ id: record.id });
            createMessage.success('Xóa thành công!');
            reload();
          },
        },
      },
    ];
  }
</script>
