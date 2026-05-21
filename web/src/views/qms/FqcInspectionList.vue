<template>
    <div class="p-4">
        <!-- Statistics -->
        <a-row :gutter="16" class="mb-4" v-if="stats">
            <a-col :span="4" v-for="item in statItems" :key="item.key">
                <a-statistic :title="item.label" :value="stats[item.key]" class="stat-card" />
            </a-col>
        </a-row>

        <BasicTable @register="registerTable">
            <template #toolbar>
                <a-button type="primary" @click="handleAdd">+ Tạo phiếu FQC</a-button>
            </template>
            <template #action="{ record }">
                <TableAction :actions="getActions(record)" />
            </template>
            <template #status="{ record }">
                <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
            </template>
        </BasicTable>

        <FqcInspectionModal @register="registerModal" @success="handleSuccess" />
        <FqcInspectionDetailModal @register="registerDetailModal" @success="handleSuccess" />
    </div>
</template>

<script lang="ts" name="fqc-inspection-list" setup>
import { ref, onMounted } from 'vue';
import { BasicTable, TableAction, useTable } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { fqcApi } from '/@/api/warehouse/fqcInspection';
import { useMessage } from '/@/hooks/web/useMessage';
import FqcInspectionModal from './FqcInspectionModal.vue';
import FqcInspectionDetailModal from './FqcInspectionDetailModal.vue';

const { createMessage } = useMessage();
const stats = ref<any>(null);

const statItems = [
    { key: 'totalInspections', label: 'Tổng phiếu' },
    { key: 'draftCount', label: 'Nháp' },
    { key: 'inProgressCount', label: 'Đang KT' },
    { key: 'pendingApprovalCount', label: 'Chờ duyệt' },
    { key: 'passedCount', label: 'Đạt' },
    { key: 'failedCount', label: 'Không đạt' },
];

const [registerTable, { reload }] = useTable({
    title: 'Phiếu kiểm tra chất lượng thành phẩm (FQC)',
    api: fqcApi.list,
    columns: [
        { title: 'Mã phiếu', dataIndex: 'inspectionCode', width: 150 },
        { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 130 },
        { title: 'Sản phẩm', dataIndex: 'productName', width: 180 },
        { title: 'Khách hàng', dataIndex: 'customerName', width: 150 },
        { title: 'SL kiểm tra', dataIndex: 'quantityInspected', width: 110 },
        { title: 'SL đạt', dataIndex: 'quantityPassed', width: 100 },
        { title: 'SL không đạt', dataIndex: 'quantityFailed', width: 110 },
        { title: 'Người KT', dataIndex: 'inspector', width: 130 },
        { title: 'Ngày KT', dataIndex: 'inspectionDate', width: 120 },
        { title: 'Ghi chú', dataIndex: 'notes', width: 200 },
    ],
    formConfig: {
        labelWidth: 100,
        schemas: [
            { field: 'inspectionCode', label: 'Mã phiếu', component: 'Input', colProps: { span: 6 } },
            {
                field: 'status', label: 'Trạng thái', component: 'Select', colProps: { span: 6 },
                componentProps: {
                    options: [
                        { label: 'Nháp', value: 'draft' },
                        { label: 'Đang KT', value: 'in_progress' },
                        { label: 'Chờ duyệt', value: 'pending_approval' },
                        { label: 'Đạt', value: 'passed' },
                        { label: 'Không đạt', value: 'failed' },
                    ],
                },
            },
            { field: 'productName', label: 'Sản phẩm', component: 'Input', colProps: { span: 6 } },
            {
                field: 'inspectionDate_begin', label: 'Từ ngày', component: 'DatePicker', colProps: { span: 3 },
                componentProps: { valueFormat: 'YYYY-MM-DD', style: 'width:100%', placeholder: 'Từ ngày' },
            },
            {
                field: 'inspectionDate_end', label: 'Đến ngày', component: 'DatePicker', colProps: { span: 3 },
                componentProps: { valueFormat: 'YYYY-MM-DD', style: 'width:100%', placeholder: 'Đến ngày' },
            },
        ],
        autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: { width: 200, title: 'Thao tác', dataIndex: 'action', slots: { customRender: 'action' }, fixed: 'right' },
});

const [registerModal, { openModal }] = useModal();
const [registerDetailModal, { openModal: openDetailModal }] = useModal();

function handleAdd() {
    openModal(true, { isUpdate: false });
}

function getActions(record: any) {
    const actions: any[] = [
        { label: 'Chi tiết', onClick: () => openDetailModal(true, { id: record.id }) },
    ];
    if (['draft', 'in_progress'].includes(record.status)) {
        actions.push({ label: 'Sửa', onClick: () => openModal(true, { record, isUpdate: true }) });
    }
    if (record.status === 'in_progress') {
        actions.push({
            label: 'Nộp phê duyệt', type: 'primary',
            popConfirm: {
                title: 'Xác nhận nộp phiếu FQC chờ phê duyệt?',
                confirm: async () => {
                    await fqcApi.submitForApproval(record.id);
                    createMessage.success('Nộp phiếu FQC chờ phê duyệt thành công!');
                    handleSuccess();
                },
            },
        });
    }
    if (record.status === 'pending_approval') {
        actions.push({
            label: 'Duyệt đạt', type: 'primary',
            auth: 'qms:inspection:approve',
            onClick: () => handleApprove(record, 'passed'),
        });
        actions.push({
            label: 'Từ chối', color: 'error',
            auth: 'qms:inspection:approve',
            onClick: () => handleApprove(record, 'failed'),
        });
    }
    if (record.status === 'draft') {
        actions.push({
            label: 'Xóa', color: 'error',
            popConfirm: {
                title: 'Xác nhận xóa phiếu?',
                confirm: async () => {
                    await fqcApi.delete({ id: record.id });
                    createMessage.success('Xóa thành công!');
                    handleSuccess();
                },
            },
        });
    }
    return actions;
}

async function handleApprove(record: any, status: string) {
    await fqcApi.approve(record.id, status);
    createMessage.success(`Phiếu FQC đã được cập nhật: ${statusLabel(status)}`);
    handleSuccess();
}

function handleSuccess() {
    reload();
    loadStats();
}

async function loadStats() {
    try {
        const res: any = await fqcApi.statistics();
        stats.value = res;
    } catch (e) { }
}

onMounted(() => loadStats());

function statusColor(s: string) {
    const map: Record<string, string> = {
        draft: 'default', in_progress: 'blue', pending_approval: 'gold',
        passed: 'green', failed: 'red',
    };
    return map[s] || 'default';
}

function statusLabel(s: string) {
    const map: Record<string, string> = {
        draft: 'Nháp', in_progress: 'Đang KT', pending_approval: 'Chờ duyệt',
        passed: 'Đạt', failed: 'Không đạt',
    };
    return map[s] || s;
}
</script>

<style scoped>
.stat-card {
    background: #fff;
    padding: 16px;
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}
</style>
