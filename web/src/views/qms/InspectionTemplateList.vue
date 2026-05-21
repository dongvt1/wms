<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">+ Tạo mẫu kiểm tra</a-button>
      </template>
      <template #stageType="{ record }">
        <a-tag :color="stageTypeColor(record.stageType)">
          {{ stageTypeLabel(record.stageType) }}
        </a-tag>
      </template>
      <template #status="{ record }">
        <a-tag :color="statusColor(record.status)">
          {{ statusLabel(record.status) }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" :dropDownActions="getDropDownActions(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="inspection-template-list" setup>
  import { useRouter } from 'vue-router';
  import { BasicTable, TableAction, useTable } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { wmsInspectionTemplateApi } from '/@/api/wms/inspectionTemplate';

  const router = useRouter();
  const { createMessage, createConfirm } = useMessage();

  const [registerTable, { reload }] = useTable({
    title: 'Danh sách mẫu kiểm tra (Inspection Template)',
    api: wmsInspectionTemplateApi.list,
    columns: [
      { title: 'Mã template', dataIndex: 'templateCode', width: 150 },
      { title: 'Tên template', dataIndex: 'templateName', width: 220 },
      { title: 'Loại QC', dataIndex: 'stageType', slots: { customRender: 'stageType' }, width: 110 },
      { title: 'Số bước', dataIndex: 'stepCount', width: 80, align: 'center' },
      { title: 'Phiên bản', dataIndex: 'version', width: 90, align: 'center' },
      { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 110 },
      { title: 'Ngày cập nhật', dataIndex: 'updateTime', width: 160 },
    ],
    formConfig: {
      labelWidth: 100,
      schemas: [
        {
          field: 'stageType',
          label: 'Loại QC',
          component: 'Select',
          colProps: { span: 6 },
          componentProps: {
            options: [
              { label: 'IQC - Đầu vào', value: 'iqc' },
              { label: 'PQC - Sản xuất', value: 'pqc' },
              { label: 'FQC - Thành phẩm', value: 'fqc' },
            ],
            allowClear: true,
            placeholder: 'Tất cả',
          },
        },
        {
          field: 'status',
          label: 'Trạng thái',
          component: 'Select',
          colProps: { span: 6 },
          componentProps: {
            options: [
              { label: 'Nháp', value: 'draft' },
              { label: 'Đang dùng', value: 'active' },
              { label: 'Lỗi thời', value: 'obsolete' },
            ],
            allowClear: true,
            placeholder: 'Tất cả',
          },
        },
        {
          field: 'search',
          label: 'Tìm kiếm',
          component: 'Input',
          colProps: { span: 6 },
          componentProps: {
            placeholder: 'Mã hoặc tên template...',
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
      width: 180,
      title: 'Thao tác',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: 'right',
    },
  });

  // --- Helpers ---

  function stageTypeColor(type: string) {
    const map: Record<string, string> = { iqc: 'blue', pqc: 'orange', fqc: 'green' };
    return map[type] || 'default';
  }

  function stageTypeLabel(type: string) {
    const map: Record<string, string> = { iqc: 'IQC', pqc: 'PQC', fqc: 'FQC' };
    return map[type] || type?.toUpperCase();
  }

  function statusColor(status: string) {
    const map: Record<string, string> = { draft: 'default', active: 'green', obsolete: 'red' };
    return map[status] || 'default';
  }

  function statusLabel(status: string) {
    const map: Record<string, string> = { draft: 'Nháp', active: 'Đang dùng', obsolete: 'Lỗi thời' };
    return map[status] || status;
  }

  // --- Actions ---

  function handleAdd() {
    router.push({ name: 'QmsInspectionTemplateForm', params: { id: 'new' } });
  }

  function handleEdit(record: any) {
    router.push({ name: 'QmsInspectionTemplateForm', params: { id: record.id } });
  }

  async function handleDelete(record: any) {
    createConfirm({
      iconType: 'warning',
      title: 'Xác nhận xóa',
      content: `Bạn có chắc muốn xóa template "${record.templateName}"?`,
      onOk: async () => {
        await wmsInspectionTemplateApi.delete(record.id);
        createMessage.success('Xóa template thành công!');
        reload();
      },
    });
  }

  async function handleClone(record: any) {
    await wmsInspectionTemplateApi.clone(record.id);
    createMessage.success('Nhân bản template thành công!');
    reload();
  }

  async function handleActivate(record: any) {
    try {
      await wmsInspectionTemplateApi.activate(record.id);
      createMessage.success('Kích hoạt template thành công!');
      reload();
    } catch (e: any) {
      // 422 validation errors are handled by global error handler
    }
  }

  function getActions(record: any) {
    return [
      { label: 'Sửa', onClick: () => handleEdit(record) },
      {
        label: 'Xóa',
        color: 'error',
        onClick: () => handleDelete(record),
      },
    ];
  }

  function getDropDownActions(record: any) {
    const actions: any[] = [
      { label: 'Nhân bản', onClick: () => handleClone(record) },
    ];
    if (record.status === 'draft') {
      actions.push({ label: 'Kích hoạt', onClick: () => handleActivate(record) });
    }
    return actions;
  }
</script>
