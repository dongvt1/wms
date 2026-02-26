<template>
    <div class="p-4">
        <BasicTable @register="registerTable">
            <template #toolbar>
                <a-input-search v-model:value="woSearch" placeholder="Nhập mã WO để xem review..."
                    enter-button="Xem Review" style="width:320px" @search="handleWoSearch" />
            </template>
            <template #action="{ record }">
                <TableAction :actions="getActions(record)" />
            </template>
            <template #status="{ record }">
                <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
            </template>
            <template #overallResult="{ record }">
                <a-tag v-if="record.overallResult"
                    :color="record.overallResult === 'passed' ? 'green' : record.overallResult === 'failed' ? 'red' : 'orange'">
                    {{ record.overallResult === 'passed' ? '✅ Đạt' : record.overallResult === 'failed' ? '❌ Không đạt' :
                    '⚠️ Có điều kiện' }}
                </a-tag>
                <span v-else class="text-gray-400">—</span>
            </template>
        </BasicTable>

        <!-- Review Detail Modal -->
        <BasicModal v-model:open="showDetail" title="Review & Phê duyệt" width="900px" :footer="null"
            @cancel="showDetail = false">
            <template v-if="reviewDetail">
                <a-descriptions bordered size="small" :column="3">
                    <a-descriptions-item label="Mã Review">{{ reviewDetail.review?.reviewCode }}</a-descriptions-item>
                    <a-descriptions-item label="Mã WO">{{ reviewDetail.review?.workOrderId }}</a-descriptions-item>
                    <a-descriptions-item label="Trạng thái">
                        <a-tag :color="statusColor(reviewDetail.review?.status)">{{
                            statusLabel(reviewDetail.review?.status) }}</a-tag>
                    </a-descriptions-item>
                    <a-descriptions-item label="Tổng phiên KT">{{ reviewDetail.review?.totalSessions
                        }}</a-descriptions-item>
                    <a-descriptions-item label="Hoàn thành">{{ reviewDetail.review?.passedSessions
                        }}</a-descriptions-item>
                    <a-descriptions-item label="Chưa hoàn thành">{{ reviewDetail.review?.failedSessions
                        }}</a-descriptions-item>
                    <a-descriptions-item label="Kết quả tổng" :span="3">
                        <a-tag v-if="reviewDetail.review?.overallResult"
                            :color="reviewDetail.review?.overallResult === 'passed' ? 'green' : 'red'">
                            {{ reviewDetail.review?.overallResult }}
                        </a-tag>
                        <span v-else class="text-gray-400">Chưa phê duyệt</span>
                    </a-descriptions-item>
                    <a-descriptions-item label="Ghi chú" :span="3">{{ reviewDetail.review?.rejectionReason
                        }}</a-descriptions-item>
                </a-descriptions>

                <a-divider>Danh sách phiên kiểm tra</a-divider>
                <a-table :dataSource="reviewDetail.sessions || []" :columns="sessionCols" :pagination="false"
                    size="small" bordered>
                    <template #bodyCell="{ column, record }">
                        <template v-if="column.key === 'status'">
                            <a-tag :color="record.status === 'completed' ? 'green' : 'blue'">
                                {{ record.status === 'completed' ? 'Hoàn thành' : 'Nháp' }}
                            </a-tag>
                        </template>
                    </template>
                </a-table>

                <!-- Action buttons -->
                <div class="mt-4 flex gap-2" v-if="reviewDetail.review?.status === 'draft'">
                    <a-button type="primary" @click="handleSubmitReview">📋 Nộp phê duyệt</a-button>
                </div>
                <div class="mt-4 flex gap-2 items-start" v-if="reviewDetail.review?.status === 'pending_approval'">
                    <a-select v-model:value="approveResult" style="width:180px" placeholder="Kết quả tổng thể">
                        <a-select-option value="passed">✅ Đạt</a-select-option>
                        <a-select-option value="failed">❌ Không đạt</a-select-option>
                        <a-select-option value="conditional">⚠️ Có điều kiện</a-select-option>
                    </a-select>
                    <a-button type="primary" @click="handleApprove">✅ Phê duyệt</a-button>
                    <a-button danger @click="showRejectForm = true">❌ Từ chối</a-button>
                </div>
                <div v-if="showRejectForm" class="mt-2 flex gap-2">
                    <a-input v-model:value="rejectReason" placeholder="Lý do từ chối..." style="flex:1" />
                    <a-button danger @click="handleReject">Xác nhận từ chối</a-button>
                </div>
            </template>
        </BasicModal>
    </div>
</template>

<script lang="ts" name="qc-review-list" setup>
import { ref } from 'vue';
import { BasicTable, TableAction, useTable } from '/@/components/Table';
import { BasicModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { qcReviewApi } from '/@/api/warehouse/qcReview';

const { createMessage } = useMessage();
const woSearch = ref('');
const showDetail = ref(false);
const reviewDetail = ref<any>(null);
const approveResult = ref('');
const rejectReason = ref('');
const showRejectForm = ref(false);

const sessionCols = [
    { title: 'Mã phiên', dataIndex: 'sessionCode', width: 160 },
    { title: 'Công đoạn', dataIndex: 'stageName', width: 200 },
    { title: 'Người KT', dataIndex: 'inspector', width: 120 },
    { title: 'Ngày KT', dataIndex: 'inspectionDate', width: 110 },
    { title: 'Trạng thái', key: 'status', width: 120 },
];

const [registerTable, { reload }] = useTable({
    title: 'Danh sách Review & Phê duyệt',
    api: qcReviewApi.list,
    columns: [
        { title: 'Mã Review', dataIndex: 'reviewCode', width: 160 },
        { title: 'Mã WO', dataIndex: 'workOrderId', width: 160 },
        { title: 'Tổng phiên', dataIndex: 'totalSessions', width: 100 },
        { title: 'Hoàn thành', dataIndex: 'passedSessions', width: 100 },
        { title: 'Kết quả', key: 'overallResult', slots: { customRender: 'overallResult' }, width: 140 },
        { title: 'Trạng thái', dataIndex: 'status', slots: { customRender: 'status' }, width: 140 },
        { title: 'Người duyệt', dataIndex: 'approver', width: 130 },
        { title: 'Ngày duyệt', dataIndex: 'approvalDate', width: 150 },
    ],
    useSearchForm: false,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: { width: 130, title: 'Thao tác', dataIndex: 'action', slots: { customRender: 'action' }, fixed: 'right' },
});

function getActions(record: any) {
    return [
        {
            label: 'Xem chi tiết',
            onClick: async () => {
                const res: any = await qcReviewApi.queryById(record.id);
                reviewDetail.value = res;
                approveResult.value = '';
                rejectReason.value = '';
                showRejectForm.value = false;
                showDetail.value = true;
            },
        },
    ];
}

async function handleWoSearch(wo: string) {
    if (!wo) return;
    try {
        const res: any = await qcReviewApi.byWorkOrder(wo);
        reviewDetail.value = { review: res, sessions: [] };
        // Load sessions detail
        const detail: any = await qcReviewApi.queryById(res.id);
        reviewDetail.value = detail;
        approveResult.value = '';
        rejectReason.value = '';
        showRejectForm.value = false;
        showDetail.value = true;
    } catch (e) {
        createMessage.error('Không tìm thấy WO hoặc chưa có Review');
    }
}

async function handleSubmitReview() {
    await qcReviewApi.submit(reviewDetail.value.review.id);
    createMessage.success('Nộp review chờ phê duyệt thành công!');
    await refreshDetail();
    reload();
}

async function handleApprove() {
    if (!approveResult.value) { createMessage.warning('Vui lòng chọn kết quả tổng thể!'); return; }
    await qcReviewApi.approve(reviewDetail.value.review.id, undefined, approveResult.value);
    createMessage.success('Phê duyệt thành công!');
    await refreshDetail();
    reload();
}

async function handleReject() {
    await qcReviewApi.reject(reviewDetail.value.review.id, undefined, rejectReason.value);
    createMessage.success('Đã từ chối review!');
    showRejectForm.value = false;
    await refreshDetail();
    reload();
}

async function refreshDetail() {
    const res: any = await qcReviewApi.queryById(reviewDetail.value.review.id);
    reviewDetail.value = res;
}

function statusColor(s: string) {
    const m: Record<string, string> = { draft: 'default', pending_approval: 'gold', approved: 'green', rejected: 'red' };
    return m[s] || 'default';
}
function statusLabel(s: string) {
    const m: Record<string, string> = { draft: 'Nháp', pending_approval: 'Chờ duyệt', approved: 'Đã duyệt', rejected: 'Từ chối' };
    return m[s] || s;
}
</script>
