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
                <a-button type="primary" @click="handleAdd">+ Tạo phiếu IQC</a-button>
            </template>
            <template #action="{ record }">
                <TableAction :actions="getActions(record)" />
            </template>
            <template #status="{ record }">
                <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
            </template>
        </BasicTable>

        <IqcInspectionModal @register="registerModal" @success="handleSuccess" />
        <IqcInspectionDetailModal @register="registerDetailModal" @success="handleSuccess" />
    </div>
</template>

<script lang="ts" name="iqc-inspection-list" setup>
import { ref, onMounted } from 'vue';
import { BasicTable, TableAction, useTable } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { iqcApi } from '/@/api/warehouse/iqcInspection';
import { useMessage } from '/@/hooks/web/useMessage';
import IqcInspectionModal from './IqcInspectionModal.vue';
import IqcInspectionDetailModal from './IqcInspectionDetailModal.vue';

const { createMessage } = useMessage();
const stats = ref<any>(null);

const statItems = [
    { key: 'totalInspections', label: 'Tổng phiếu' },
    { key: 'draftCount', label: 'Nháp' },
    { key: 'inProgressCount', label: 'Đang KT' },
    { key: 'passedCount', label: 'Đạt' },
    { key: 'failedCount', label: 'Không đạt' },
    { key: 'conditionalCount', label: 'Có điều kiện' },
];

const [registerTable, { reload }] = useTable({
    title: 'Phiếu kiểm tra chất lượng đầu vào (IQC)',
    api: iqcApi.list,
    columns: [
        { title: 'Mã phiếu', dataIndex: 'inspectionCode', width: 150 },
        { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 130 },
        { title: 'SL nhận', dataIndex: 'quantityReceived', width: 100 },
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
                        { label: 'Đạt', value: 'passed' },
                        { label: 'Không đạt', value: 'failed' },
                        { label: 'Có điều kiện', value: 'conditional' },
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
    actionColumn: { width: 200, title: 'Thao tác', dataIndex: 'action', slots: { customRender: 'action' }, fixed: 'right' },
});

const [registerModal, { openModal }] = useModal();
const [registerDetailModal, { openModal: openDetailModal }] = useModal();

function getActions(record: any) {
    const actions: any[] = [
        { label: 'Chi tiết', onClick: () => openDetailModal(true, { id: record.id }) },
    ];
    if (['draft', 'in_progress'].includes(record.status)) {
        actions.push({ label: 'Sửa', onClick: () => openModal(true, { record, isUpdate: true }) });
    }
    if (record.status === 'in_progress') {
        actions.push({
            label: 'Duyệt đạt', type: 'primary',
            onClick: () => handleApprove(record, 'passed'),
        });
        actions.push({
            label: 'Từ chối', color: 'error',
            onClick: () => handleApprove(record, 'failed'),
        });
    }
    if (record.status === 'draft') {
        actions.push({
            label: 'Xóa', color: 'error',
            popConfirm: {
                title: 'Xác nhận xóa phiếu?',
                confirm: async () => {
                    await iqcApi.delete({ id: record.id });
                    createMessage.success('Xóa thành công!');
                    handleSuccess();
                },
            },
        });
    }
    return actions;
}

async function handleApprove(record: any, status: string) {
    await iqcApi.approve(record.id, status);
    createMessage.success(`Phiếu IQC đã được cập nhật: ${statusLabel(status)}`);
    handleSuccess();
}

function handleSuccess() {
    reload();
    loadStats();
}

async function loadStats() {
    try {
        const res: any = await iqcApi.statistics();
        stats.value = res;
    } catch (e) { }
}

onMounted(() => loadStats());

function statusColor(s: string) {
    const map: Record<string, string> = {
        draft: 'default', in_progress: 'blue', passed: 'green', failed: 'red', conditional: 'orange',
    };
    return map[s] || 'default';
}

function statusLabel(s: string) {
    const map: Record<string, string> = {
        draft: 'Nháp', in_progress: 'Đang KT', passed: 'Đạt', failed: 'Không đạt', conditional: 'Có điều kiện',
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
