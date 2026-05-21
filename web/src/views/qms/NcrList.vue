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
                <a-button type="primary" @click="handleAdd">+ Tạo NCR</a-button>
            </template>
            <template #action="{ record }">
                <TableAction :actions="getActions(record)" />
            </template>
            <template #status="{ record }">
                <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
            </template>
            <template #severity="{ record }">
                <a-tag :color="severityColor(record.severity)">{{ severityLabel(record.severity) }}</a-tag>
            </template>
            <template #sourceType="{ record }">
                <a-tag>{{ sourceTypeLabel(record.sourceType) }}</a-tag>
            </template>
        </BasicTable>

        <NcrModal @register="registerModal" @success="handleSuccess" />
        <NcrDetailModal @register="registerDetailModal" @success="handleSuccess" />
    </div>
</template>

<script lang="ts" name="ncr-list" setup>
import { ref, onMounted } from 'vue';
import { BasicTable, TableAction, useTable } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { ncrApi } from '/@/api/warehouse/ncr';
import { useMessage } from '/@/hooks/web/useMessage';
import NcrModal from './NcrModal.vue';
import NcrDetailModal from './NcrDetailModal.vue';

const { createMessage } = useMessage();
const stats = ref<any>(null);

const statItems = [
    { key: 'totalCount', label: 'Tổng NCR' },
    { key: 'openCount', label: 'Mở' },
    { key: 'investigatingCount', label: 'Đang điều tra' },
    { key: 'actionTakenCount', label: 'Đã xử lý' },
    { key: 'verifiedCount', label: 'Đã xác minh' },
    { key: 'closedCount', label: 'Đã đóng' },
];

const [registerTable, { reload }] = useTable({
    title: 'Báo cáo sự không phù hợp (NCR)',
    api: ncrApi.list,
    columns: [
        { title: 'Mã NCR', dataIndex: 'ncrCode', width: 150 },
        { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 130 },
        { title: 'Mức độ', dataIndex: 'severity', slots: { customRender: 'severity' }, width: 110 },
        { title: 'Nguồn', dataIndex: 'sourceType', slots: { customRender: 'sourceType' }, width: 100 },
        { title: 'Sản phẩm', dataIndex: 'productName', width: 180 },
        { title: 'Nhà cung cấp', dataIndex: 'supplierName', width: 150 },
        { title: 'SL lỗi', dataIndex: 'quantityDefective', width: 100 },
        { title: 'Người phụ trách', dataIndex: 'assignedTo', width: 130 },
        { title: 'Ngày tạo', dataIndex: 'createTime', width: 150 },
    ],
    formConfig: {
        labelWidth: 100,
        schemas: [
            {
                field: 'status', label: 'Trạng thái', component: 'Select', colProps: { span: 5 },
                componentProps: {
                    options: [
                        { label: 'Mở', value: 'open' },
                        { label: 'Đang điều tra', value: 'investigating' },
                        { label: 'Đã xử lý', value: 'action_taken' },
                        { label: 'Đã xác minh', value: 'verified' },
                        { label: 'Đã đóng', value: 'closed' },
                    ],
                    placeholder: 'Trạng thái',
                    allowClear: true,
                },
            },
            {
                field: 'severity', label: 'Mức độ', component: 'Select', colProps: { span: 5 },
                componentProps: {
                    options: [
                        { label: 'Nghiêm trọng', value: 'critical' },
                        { label: 'Lớn', value: 'major' },
                        { label: 'Nhỏ', value: 'minor' },
                    ],
                    placeholder: 'Mức độ',
                    allowClear: true,
                },
            },
            {
                field: 'sourceType', label: 'Nguồn', component: 'Select', colProps: { span: 5 },
                componentProps: {
                    options: [
                        { label: 'IQC', value: 'iqc' },
                        { label: 'PQC', value: 'pqc' },
                        { label: 'FQC', value: 'fqc' },
                        { label: 'Khác', value: 'other' },
                    ],
                    placeholder: 'Nguồn phát hiện',
                    allowClear: true,
                },
            },
            {
                field: 'supplierId', label: 'NCC', component: 'Input', colProps: { span: 5 },
                componentProps: { placeholder: 'ID nhà cung cấp' },
            },
        ],
        autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: { width: 180, title: 'Thao tác', dataIndex: 'action', slots: { customRender: 'action' }, fixed: 'right' },
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
    if (record.status === 'open') {
        actions.push({ label: 'Sửa', onClick: () => openModal(true, { record, isUpdate: true }) });
        actions.push({
            label: 'Xóa', color: 'error',
            popConfirm: {
                title: 'Xác nhận xóa NCR?',
                confirm: async () => {
                    await ncrApi.delete({ id: record.id });
                    createMessage.success('Xóa thành công!');
                    handleSuccess();
                },
            },
        });
    }
    return actions;
}

function handleSuccess() {
    reload();
    loadStats();
}

async function loadStats() {
    try {
        const res: any = await ncrApi.statistics();
        stats.value = res;
    } catch (e) { }
}

onMounted(() => loadStats());

function statusColor(s: string) {
    const map: Record<string, string> = {
        open: 'red', investigating: 'blue', action_taken: 'orange',
        verified: 'cyan', closed: 'green',
    };
    return map[s] || 'default';
}

function statusLabel(s: string) {
    const map: Record<string, string> = {
        open: 'Mở', investigating: 'Đang điều tra', action_taken: 'Đã xử lý',
        verified: 'Đã xác minh', closed: 'Đã đóng',
    };
    return map[s] || s;
}

function severityColor(s: string) {
    const map: Record<string, string> = {
        critical: 'red', major: 'orange', minor: 'blue',
    };
    return map[s] || 'default';
}

function severityLabel(s: string) {
    const map: Record<string, string> = {
        critical: 'Nghiêm trọng', major: 'Lớn', minor: 'Nhỏ',
    };
    return map[s] || s;
}

function sourceTypeLabel(s: string) {
    const map: Record<string, string> = {
        iqc: 'IQC', pqc: 'PQC', fqc: 'FQC', other: 'Khác',
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
