<template>
    <BasicModal v-bind="$attrs" @register="registerModal" title="Chi tiết phiếu FQC" width="860px" :footer="null">
        <template v-if="detail">
            <a-descriptions bordered size="small" :column="2">
                <a-descriptions-item label="Mã phiếu">{{ detail.inspection?.inspectionCode }}</a-descriptions-item>
                <a-descriptions-item label="Trạng thái">
                    <a-tag :color="statusColor(detail.inspection?.status)">{{ statusLabel(detail.inspection?.status)
                        }}</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="Sản phẩm">{{ detail.inspection?.productName }}</a-descriptions-item>
                <a-descriptions-item label="Khách hàng">{{ detail.inspection?.customerName }}</a-descriptions-item>
                <a-descriptions-item label="Đơn hàng xuất">{{ detail.inspection?.outboundOrderId }}</a-descriptions-item>
                <a-descriptions-item label="SL kiểm tra">{{ detail.inspection?.quantityInspected }}</a-descriptions-item>
                <a-descriptions-item label="SL đạt">{{ detail.inspection?.quantityPassed }}</a-descriptions-item>
                <a-descriptions-item label="SL không đạt">{{ detail.inspection?.quantityFailed }}</a-descriptions-item>
                <a-descriptions-item label="Người KT">{{ detail.inspection?.inspector }}</a-descriptions-item>
                <a-descriptions-item label="Ngày KT">{{ detail.inspection?.inspectionDate }}</a-descriptions-item>
                <a-descriptions-item label="Ghi chú" :span="2">{{ detail.inspection?.notes }}</a-descriptions-item>
            </a-descriptions>

            <a-divider>Kết quả tiêu chí kiểm tra</a-divider>
            <a-table :dataSource="detail.results" :columns="resultColumns" :pagination="false" size="small" bordered>
                <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'result'">
                        <a-tag
                            :color="record.result === 'passed' ? 'green' : record.result === 'failed' ? 'red' : 'default'">
                            {{ record.result === 'passed' ? '✅ Đạt' : record.result === 'failed' ? '❌ Không đạt' : 'N/A'
                            }}
                        </a-tag>
                    </template>
                </template>
            </a-table>

            <!-- Attachments section -->
            <a-divider v-if="detail.attachments && detail.attachments.length > 0">Tệp đính kèm</a-divider>
            <a-list v-if="detail.attachments && detail.attachments.length > 0" :dataSource="detail.attachments"
                size="small">
                <template #renderItem="{ item }">
                    <a-list-item>
                        <a-list-item-meta>
                            <template #title>
                                <a :href="item.filePath" target="_blank">{{ item.fileName }}</a>
                            </template>
                            <template #description>
                                {{ formatFileSize(item.fileSize) }} · {{ item.fileType?.toUpperCase() }} · {{ item.uploadTime }}
                            </template>
                        </a-list-item-meta>
                    </a-list-item>
                </template>
            </a-list>

            <div class="mt-4 flex gap-2" v-if="detail.inspection?.status === 'in_progress'">
                <a-button type="primary" @click="submitForApproval">📋 Nộp phê duyệt</a-button>
            </div>
            <div class="mt-4 flex gap-2" v-if="detail.inspection?.status === 'pending_approval'">
                <a-button v-auth="'qms:inspection:approve'" type="primary" @click="approve('passed')">✅ Duyệt đạt</a-button>
                <a-button v-auth="'qms:inspection:approve'" danger @click="approve('failed')">❌ Từ chối</a-button>
            </div>
        </template>
        <a-spin v-else />
    </BasicModal>
</template>

<script lang="ts" name="fqc-detail-modal" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { fqcApi } from '/@/api/warehouse/fqcInspection';
import { useMessage } from '/@/hooks/web/useMessage';

const emit = defineEmits(['success', 'register']);
const { createMessage } = useMessage();
const detail = ref<any>(null);

const resultColumns = [
    { title: 'Tiêu chí', dataIndex: 'criterionName', width: '30%' },
    { title: 'Tiêu chuẩn', dataIndex: 'standardValue', width: '20%' },
    { title: 'Giá trị thực', dataIndex: 'actualValue', width: '20%' },
    { title: 'Kết quả', key: 'result', width: '15%' },
    { title: 'Ghi chú', dataIndex: 'notes', width: '15%' },
];

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    detail.value = null;
    setModalProps({ confirmLoading: false });
    if (data?.id) {
        const res: any = await fqcApi.queryById(data.id);
        detail.value = res;
    }
});

async function submitForApproval() {
    await fqcApi.submitForApproval(detail.value.inspection.id);
    createMessage.success('Nộp phiếu FQC chờ phê duyệt thành công!');
    emit('success');
    closeModal();
}

async function approve(status: string) {
    await fqcApi.approve(detail.value.inspection.id, status);
    createMessage.success(`Đã cập nhật trạng thái: ${statusLabel(status)}`);
    emit('success');
    closeModal();
}

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

function formatFileSize(bytes: number) {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}
</script>
