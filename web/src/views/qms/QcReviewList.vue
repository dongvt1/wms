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
                    <a-descriptions-item label="Phiên đạt">{{ reviewDetail.review?.passedSessions
                        }}</a-descriptions-item>
                    <a-descriptions-item label="Phiên không đạt">{{ reviewDetail.review?.failedSessions
                        }}</a-descriptions-item>
                    <a-descriptions-item label="Kết quả tổng" :span="3">
                        <a-tag v-if="reviewDetail.review?.overallResult"
                            :color="reviewDetail.review?.overallResult === 'passed' ? 'green' : reviewDetail.review?.overallResult === 'failed' ? 'red' : 'orange'">
                            {{ reviewDetail.review?.overallResult === 'passed' ? '✅ Đạt' :
                               reviewDetail.review?.overallResult === 'failed' ? '❌ Không đạt' : '⚠️ Có điều kiện' }}
                        </a-tag>
                        <span v-else class="text-gray-400">Chưa phê duyệt</span>
                    </a-descriptions-item>

                    <!-- Suggested Result Section -->
                    <a-descriptions-item label="Kết quả đề xuất" :span="3">
                        <a-spin v-if="suggestLoading" size="small" />
                        <template v-else-if="suggestedResult">
                            <a-tag :color="suggestedResult === 'passed' ? 'green' : suggestedResult === 'failed' ? 'red' : 'orange'">
                                {{ suggestedResult === 'passed' ? '✅ Đề xuất: Đạt' :
                                   suggestedResult === 'failed' ? '❌ Đề xuất: Không đạt' : '⚠️ Đề xuất: Có điều kiện' }}
                            </a-tag>
                        </template>
                        <span v-else class="text-gray-400">Không có đề xuất</span>
                    </a-descriptions-item>

                    <a-descriptions-item label="Ghi chú" :span="3">{{ reviewDetail.review?.rejectionReason
                        }}</a-descriptions-item>
                </a-descriptions>

                <!-- Override Result Button (Quản_lý_QC only) -->
                <div class="mt-3" v-if="reviewDetail.review?.status === 'approved' || reviewDetail.review?.status === 'pending_approval'">
                    <a-button v-auth="'qms:inspection:approve'" type="default" @click="showOverrideModal = true">
                        🔄 Ghi đè kết quả
                    </a-button>
                </div>

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

        <!-- Override Result Modal -->
        <BasicModal v-model:open="showOverrideModal" title="Ghi đè kết quả Review" width="480px"
            @ok="handleOverride" @cancel="resetOverrideForm" :confirmLoading="overrideLoading">
            <a-form layout="vertical">
                <a-form-item label="Kết quả mới" required>
                    <a-select v-model:value="overrideForm.result" placeholder="Chọn kết quả">
                        <a-select-option value="passed">✅ Đạt</a-select-option>
                        <a-select-option value="failed">❌ Không đạt</a-select-option>
                        <a-select-option value="conditional">⚠️ Có điều kiện</a-select-option>
                    </a-select>
                </a-form-item>
                <a-form-item label="Lý do ghi đè" required>
                    <a-textarea v-model:value="overrideForm.reason" :rows="3"
                        placeholder="Nhập lý do ghi đè kết quả..." />
                </a-form-item>
            </a-form>
        </BasicModal>
    </div>
</template>

<script lang="ts" name="qc-review-list" setup>
import { ref, reactive } from 'vue';
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

// Suggestion state
const suggestedResult = ref<string | null>(null);
const suggestLoading = ref(false);

// Override state
const showOverrideModal = ref(false);
const overrideLoading = ref(false);
const overrideForm = reactive({
    result: '' as string,
    reason: '' as string,
});

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
        { title: 'Phiên đạt', dataIndex: 'passedSessions', width: 100 },
        { title: 'Phiên không đạt', dataIndex: 'failedSessions', width: 120 },
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
                await loadSuggestion(record.id);
            },
        },
    ];
}

/** Load the auto-calculated suggestion for the review */
async function loadSuggestion(reviewId: string) {
    suggestedResult.value = null;
    suggestLoading.value = true;
    try {
        const res: any = await qcReviewApi.suggest(reviewId);
        suggestedResult.value = res?.result || res || null;
    } catch (e) {
        suggestedResult.value = null;
    } finally {
        suggestLoading.value = false;
    }
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
        await loadSuggestion(res.id);
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

/** Handle override result submission */
async function handleOverride() {
    if (!overrideForm.result) {
        createMessage.warning('Vui lòng chọn kết quả mới!');
        return;
    }
    if (!overrideForm.reason?.trim()) {
        createMessage.warning('Vui lòng nhập lý do ghi đè!');
        return;
    }
    overrideLoading.value = true;
    try {
        await qcReviewApi.override(reviewDetail.value.review.id, {
            result: overrideForm.result,
            reason: overrideForm.reason.trim(),
        });
        createMessage.success('Ghi đè kết quả thành công!');
        showOverrideModal.value = false;
        resetOverrideForm();
        await refreshDetail();
        reload();
    } catch (e: any) {
        createMessage.error(e?.message || 'Ghi đè kết quả thất bại!');
    } finally {
        overrideLoading.value = false;
    }
}

function resetOverrideForm() {
    overrideForm.result = '';
    overrideForm.reason = '';
    showOverrideModal.value = false;
}

async function refreshDetail() {
    const res: any = await qcReviewApi.queryById(reviewDetail.value.review.id);
    reviewDetail.value = res;
    // Reload suggestion after refresh
    await loadSuggestion(reviewDetail.value.review.id);
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
