<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate">
          <PlusOutlined /> Tạo phiên kiểm tra
        </a-button>
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
      <template #overallResult="{ record }">
        <a-tag v-if="record.overallResult === 'pass'" color="green">PASS</a-tag>
        <a-tag v-else-if="record.overallResult === 'fail'" color="red">FAIL</a-tag>
        <a-tag v-else color="default">--</a-tag>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="getActions(record)"
        />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="inspection-execution-list" setup>
  import { useRouter } from 'vue-router';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { BasicTable, TableAction, useTable } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { wmsInspectionExecutionApi } from '/@/api/wms/inspectionExecution';

  const router = useRouter();
  const { createMessage } = useMessage();

  const [registerTable, { reload }] = useTable({
    title: 'Danh sách phiên kiểm tra (Inspection Execution)',
    api: wmsInspectionExecutionApi.list,
    columns: [
      { title: 'Mã phiên', dataIndex: 'executionCode', width: 150 },
      { title: 'Template', dataIndex: 'templateName', width: 200 },
      { title: 'Loại QC', dataIndex: 'stageType', slots: { customRender: 'stageType' }, width: 100 },
      { title: 'Người kiểm tra', dataIndex: 'inspector', width: 140 },
      { title: 'Ngày kiểm tra', dataIndex: 'inspectionDate', width: 130 },
      { title: 'Kết quả', dataIndex: 'overallResult', slots: { customRender: 'overallResult' }, width: 100 },
      { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 120 },
      { title: 'Ngày tạo', dataIndex: 'createTime', width: 160 },
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
              { label: 'Đang thực hiện', value: 'in_progress' },
              { label: 'Chờ phê duyệt', value: 'pending_approval' },
              { label: 'Đã phê duyệt', value: 'approved' },
              { label: 'Bị từ chối', value: 'rejected' },
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
            placeholder: 'Mã phiên hoặc template...',
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
    const map: Record<string, string> = {
      draft: 'default',
      in_progress: 'processing',
      pending_approval: 'warning',
      approved: 'success',
      rejected: 'error',
    };
    return map[status] || 'default';
  }

  function statusLabel(status: string) {
    const map: Record<string, string> = {
      draft: 'Nháp',
      in_progress: 'Đang thực hiện',
      pending_approval: 'Chờ phê duyệt',
      approved: 'Đã phê duyệt',
      rejected: 'Bị từ chối',
    };
    return map[status] || status;
  }

  // --- Actions ---
  function handleCreate() {
    router.push({ name: 'QmsInspectionExecutionForm', params: { id: 'new' } });
  }

  function handleOpen(record: any) {
    router.push({ name: 'QmsInspectionExecutionForm', params: { id: record.id } });
  }

  function getActions(record: any) {
    const actions: any[] = [];
    if (record.status === 'draft' || record.status === 'in_progress') {
      actions.push({ label: 'Tiếp tục', onClick: () => handleOpen(record) });
    } else {
      actions.push({ label: 'Xem', onClick: () => handleOpen(record) });
    }
    return actions;
  }
</script>
