<template>
    <BasicModal v-bind="$attrs" @register="registerModal" title="Chi tiết NCR" width="900px" :footer="null">
        <template v-if="detail">
            <a-descriptions bordered size="small" :column="2">
                <a-descriptions-item label="Mã NCR">{{ detail.ncrCode }}</a-descriptions-item>
                <a-descriptions-item label="Trạng thái">
                    <a-tag :color="statusColor(detail.status)">{{ statusLabel(detail.status) }}</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="Mức độ">
                    <a-tag :color="severityColor(detail.severity)">{{ severityLabel(detail.severity) }}</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="Nguồn phát hiện">{{ sourceTypeLabel(detail.sourceType) }}
                </a-descriptions-item>
                <a-descriptions-item label="Sản phẩm">{{ detail.productName || detail.productId }}
                </a-descriptions-item>
                <a-descriptions-item label="Nhà cung cấp">
                    <span v-if="detail.supplierId">
                        {{ detail.supplierName || detail.supplierId }}
                        <a-button type="link" size="small" @click="viewSupplierReport(detail.supplierId)">
                            📊 Xem báo cáo
                        </a-button>
                    </span>
                    <span v-else>—</span>
                </a-descriptions-item>
                <a-descriptions-item label="Số lượng lỗi">{{ detail.quantityDefective ?? '—' }}
                </a-descriptions-item>
                <a-descriptions-item label="Hành động đề xuất">{{ proposedActionLabel(detail.proposedAction) }}
                </a-descriptions-item>
                <a-descriptions-item label="Người phụ trách">{{ detail.assignedTo || '—' }}</a-descriptions-item>
                <a-descriptions-item label="Ngày tạo">{{ detail.createTime }}</a-descriptions-item>
                <a-descriptions-item label="Mô tả lỗi" :span="2">{{ detail.description }}</a-descriptions-item>
                <a-descriptions-item label="Ghi chú" :span="2">{{ detail.notes || '—' }}</a-descriptions-item>
            </a-descriptions>

            <!-- Corrective Action Section -->
            <a-divider>Hành động khắc phục</a-divider>
            <div v-if="detail.correctiveAction" class="mb-4">
                <a-alert type="success" :message="detail.correctiveAction" show-icon />
            </div>
            <div v-else-if="['investigating', 'action_taken'].includes(detail.status)" class="mb-4">
                <a-textarea v-model:value="correctiveActionText" :rows="3"
                    placeholder="Nhập hành động khắc phục đã thực hiện..." />
            </div>
            <div v-else class="mb-4 text-gray-400">
                Chưa có hành động khắc phục.
            </div>

            <!-- Closure Info -->
            <div v-if="detail.status === 'closed'" class="mb-4">
                <a-descriptions bordered size="small" :column="2">
                    <a-descriptions-item label="Đóng bởi">{{ detail.closedBy }}</a-descriptions-item>
                    <a-descriptions-item label="Ngày đóng">{{ detail.closedDate }}</a-descriptions-item>
                </a-descriptions>
            </div>

            <!-- Attachment List -->
            <a-divider>Tệp đính kèm</a-divider>
            <div v-if="attachments.length > 0">
                <a-list size="small" :dataSource="attachments" bordered>
                    <template #renderItem="{ item }">
                        <a-list-item>
                            <a-list-item-meta :title="item.fileName"
                                :description="`${item.fileType?.toUpperCase()} — ${formatFileSize(item.fileSize)}`" />
                        </a-list-item>
                    </template>
                </a-list>
            </div>
            <div v-else class="text-gray-400">Không có tệp đính kèm.</div>

            <!-- State Transition Buttons -->
            <a-divider>Thao tác</a-divider>
            <div class="flex gap-2 flex-wrap">
                <a-button v-if="detail.status === 'open'" type="primary"
                    @click="handleTransition('investigating')">
                    🔍 Bắt đầu điều tra
                </a-button>
                <a-button v-if="detail.status === 'investigating'" type="primary"
                    @click="handleTransition('action_taken')">
                    ✅ Ghi nhận hành động
                </a-button>
                <a-button v-if="detail.status === 'action_taken'" type="primary"
                    @click="handleTransition('verified')">
                    🔎 Xác minh hiệu quả
                </a-button>
                <a-button v-if="detail.status === 'verified'" type="primary" v-auth="'qms:ncr:close'"
                    @click="handleClose">
                    🔒 Đóng NCR
                </a-button>
            </div>

            <!-- Transition Notes Input -->
            <div v-if="showTransitionNotes" class="mt-3">
                <a-textarea v-model:value="transitionNotes" :rows="2" placeholder="Ghi chú chuyển trạng thái..." />
                <div class="mt-2 flex gap-2">
                    <a-button type="primary" size="small" @click="confirmTransition">Xác nhận</a-button>
                    <a-button size="small" @click="cancelTransition">Hủy</a-button>
                </div>
            </div>
        </template>
        <a-spin v-else />
    </BasicModal>
</template>

<script lang="ts" name="ncr-detail-modal" setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { ncrApi } from '/@/api/warehouse/ncr';
import { useMessage } from '/@/hooks/web/useMessage';

const emit = defineEmits(['success', 'register']);
const { createMessage } = useMessage();
const router = useRouter();
const detail = ref<any>(null);
const attachments = ref<any[]>([]);
const correctiveActionText = ref('');
const transitionNotes = ref('');
const showTransitionNotes = ref(false);
const pendingTargetStatus = ref('');

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    detail.value = null;
    attachments.value = [];
    correctiveActionText.value = '';
    transitionNotes.value = '';
    showTransitionNotes.value = false;
    setModalProps({ confirmLoading: false });
    if (data?.id) {
        const res: any = await ncrApi.queryById(data.id);
        // queryById may return the NCR with nested attachments or flat
        if (res?.ncr) {
            detail.value = res.ncr;
            attachments.value = res.attachments || [];
        } else {
            detail.value = res;
            attachments.value = res?.attachments || [];
        }
    }
});

function viewSupplierReport(supplierId: string) {
    router.push({ path: '/qms/supplier-report', query: { supplierId } });
}

function handleTransition(targetStatus: string) {
    pendingTargetStatus.value = targetStatus;
    showTransitionNotes.value = true;
}

async function confirmTransition() {
    try {
        const notes = correctiveActionText.value || transitionNotes.value || undefined;
        await ncrApi.transition(detail.value.id, pendingTargetStatus.value, notes);
        createMessage.success(`Chuyển trạng thái thành công: ${statusLabel(pendingTargetStatus.value)}`);
        showTransitionNotes.value = false;
        transitionNotes.value = '';
        emit('success');
        closeModal();
    } catch (e: any) {
        createMessage.error(e?.message || 'Lỗi chuyển trạng thái');
    }
}

function cancelTransition() {
    showTransitionNotes.value = false;
    transitionNotes.value = '';
    pendingTargetStatus.value = '';
}

async function handleClose() {
    if (!correctiveActionText.value && !detail.value.correctiveAction) {
        createMessage.warning('Vui lòng nhập xác nhận hành động khắc phục đã hoàn tất');
        return;
    }
    try {
        const confirmationNotes = correctiveActionText.value || 'Xác nhận hành động khắc phục đã hoàn tất';
        await ncrApi.close(detail.value.id, confirmationNotes);
        createMessage.success('Đóng NCR thành công!');
        emit('success');
        closeModal();
    } catch (e: any) {
        createMessage.error(e?.message || 'Lỗi đóng NCR');
    }
}

function formatFileSize(bytes: number) {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

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

function proposedActionLabel(s: string) {
    const map: Record<string, string> = {
        return: 'Trả nhà cung cấp', repair: 'Sửa chữa',
        scrap: 'Hủy', accept_conditional: 'Chấp nhận có điều kiện',
    };
    return map[s] || s || '—';
}
</script>
