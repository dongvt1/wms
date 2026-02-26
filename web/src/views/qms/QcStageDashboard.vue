<template>
    <div class="p-6">
        <!-- Header: Tìm WO -->
        <div class="mb-6 flex items-center gap-4">
            <div>
                <div class="text-xl font-bold mb-1">🔍 Thực hiện kiểm tra công đoạn</div>
                <div class="text-gray-500 text-sm">Nhập mã lệnh sản xuất (WO) để bắt đầu kiểm tra</div>
            </div>
            <div class="flex-1" />
            <a-input-search v-model:value="workOrderId" placeholder="Nhập mã WO (vd: WO20260226001)..."
                enter-button="Tải công đoạn" size="large" style="max-width: 440px" :loading="loading"
                @search="loadStages" />
        </div>

        <!-- Chưa chọn WO -->
        <div v-if="!workOrderId || !loaded" class="text-center py-20 text-gray-400">
            <div class="text-5xl mb-4">📋</div>
            <div class="text-lg">Nhập mã WO để hiển thị các công đoạn cần kiểm tra</div>
        </div>

        <!-- Đã load: hiển thị cards -->
        <template v-else>
            <!-- Thống kê nhanh -->
            <a-row :gutter="16" class="mb-6">
                <a-col :span="6">
                    <a-statistic title="Tổng công đoạn" :value="stages.length" />
                </a-col>
                <a-col :span="6">
                    <a-statistic title="Đã hoàn thành" :value="doneCount" :value-style="{ color: '#3f8600' }" />
                </a-col>
                <a-col :span="6">
                    <a-statistic title="Chưa kiểm tra" :value="stages.length - doneCount"
                        :value-style="{ color: '#cf1322' }" />
                </a-col>
                <a-col :span="6">
                    <a-button type="primary" ghost size="large" @click="openReview">
                        📝 Xem Review & Phê duyệt
                    </a-button>
                </a-col>
            </a-row>

            <!-- Cards công đoạn -->
            <a-spin :spinning="loading">
                <a-empty v-if="stages.length === 0" description="Không có công đoạn nào đang hoạt động" />
                <a-row v-else :gutter="[16, 16]">
                    <a-col v-for="stage in stages" :key="stage.id" :xs="24" :sm="12" :md="8" :lg="6">
                        <a-card hoverable :class="['stage-card', cardClass(stage)]"
                            @click="handleStartInspection(stage)">
                            <!-- Status badge -->
                            <template #extra>
                                <a-tag :color="sessionStatus(stage).color" class="mb-0">
                                    {{ sessionStatus(stage).label }}
                                </a-tag>
                            </template>
                            <template #title>
                                <div class="flex items-center gap-2">
                                    <span class="text-2xl">{{ stageIcon(stage) }}</span>
                                    <span class="font-semibold text-base">{{ stage.stageName }}</span>
                                </div>
                            </template>

                            <!-- Thông tin công đoạn -->
                            <div class="text-gray-500 text-sm mb-3">
                                <span class="font-mono bg-gray-100 px-1 rounded">{{ stage.stageCode }}</span>
                                <span v-if="stage.description" class="ml-2">{{ stage.description }}</span>
                            </div>

                            <!-- Session info nếu đã kiểm -->
                            <template v-if="getSession(stage.id)">
                                <a-descriptions size="small" :column="1" class="mb-2">
                                    <a-descriptions-item label="Người KT">
                                        {{ getSession(stage.id)?.inspector || '—' }}
                                    </a-descriptions-item>
                                    <a-descriptions-item label="Ngày KT">
                                        {{ formatDate(getSession(stage.id)?.inspectionDate) }}
                                    </a-descriptions-item>
                                </a-descriptions>
                            </template>
                            <template v-else>
                                <div class="text-gray-400 text-sm italic mb-2">Chưa có phiên kiểm tra</div>
                            </template>

                            <!-- Action button -->
                            <div class="mt-2">
                                <a-button v-if="!getSession(stage.id)" type="primary" block
                                    @click.stop="handleStartInspection(stage)">
                                    ▶ Bắt đầu kiểm tra
                                </a-button>
                                <a-button v-else-if="getSession(stage.id)?.status === 'draft'" type="default" block
                                    @click.stop="handleContinue(stage)">
                                    ✏️ Tiếp tục kiểm tra
                                </a-button>
                                <div v-else class="flex gap-2">
                                    <a-button type="default" block @click.stop="handleViewDetail(stage)">
                                        👁 Xem chi tiết
                                    </a-button>
                                    <a-button size="small" @click.stop="handleAddMore(stage)"
                                        title="Thêm phiên kiểm tra mới">＋</a-button>
                                </div>
                            </div>
                        </a-card>
                    </a-col>
                </a-row>
            </a-spin>
        </template>

        <!-- Modals -->
        <QcSessionModal @register="registerModal" @success="loadStages(workOrderId)" />
        <QcSessionDetailModal @register="registerDetailModal" />
    </div>
</template>

<script lang="ts" name="qc-stage-dashboard" setup>
import { ref, computed } from 'vue';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { qcStageApi } from '/@/api/warehouse/qcStage';
import { qcSessionApi } from '/@/api/warehouse/qcSession';
import { qcReviewApi } from '/@/api/warehouse/qcReview';
import QcSessionModal from './QcSessionModal.vue';
import QcSessionDetailModal from './QcSessionDetailModal.vue';

const { createMessage } = useMessage();
const workOrderId = ref('');
const stages = ref<any[]>([]);
const sessions = ref<any[]>([]); // sessions của WO hiện tại
const loading = ref(false);
const loaded = ref(false);

const [registerModal, { openModal }] = useModal();
const [registerDetailModal, { openModal: openDetailModal }] = useModal();

const doneCount = computed(() =>
    stages.value.filter(s => getSession(s.id)?.status === 'completed').length
);

async function loadStages(wo: string) {
    if (!wo) return;
    loading.value = true;
    loaded.value = false;
    try {
        const [stageRes, sessionRes]: any[] = await Promise.all([
            qcStageApi.listActive(),
            qcSessionApi.listByWorkOrder(wo),
        ]);
        stages.value = stageRes || [];
        // Lấy session mới nhất cho mỗi stage
        sessions.value = sessionRes || [];
        loaded.value = true;
    } catch (e) {
        createMessage.error('Không thể tải dữ liệu');
    } finally {
        loading.value = false;
    }
}

function getSession(stageId: string) {
    // Lấy session mới nhất của stage này trong WO
    const list = sessions.value
        .filter((s: any) => s.stageId === stageId)
        .sort((a: any, b: any) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime());
    return list[0] || null;
}

function sessionStatus(stage: any) {
    const s = getSession(stage.id);
    if (!s) return { color: 'default', label: 'Chưa kiểm tra' };
    if (s.status === 'completed') return { color: 'green', label: '✅ Đã hoàn thành' };
    return { color: 'blue', label: '🔄 Đang thực hiện' };
}

function cardClass(stage: any) {
    const s = getSession(stage.id);
    if (!s) return 'card-pending';
    if (s.status === 'completed') return 'card-done';
    return 'card-inprogress';
}

function stageIcon(stage: any) {
    const s = getSession(stage.id);
    if (!s) return '📋';
    if (s.status === 'completed') return '✅';
    return '🔄';
}

function handleStartInspection(stage: any) {
    openModal(true, {
        isUpdate: false,
        prefill: { workOrderId: workOrderId.value, stageId: stage.id, stageName: stage.stageName },
    });
}

function handleContinue(stage: any) {
    const s = getSession(stage.id);
    if (s) openModal(true, { isUpdate: true, record: s });
}

function handleViewDetail(stage: any) {
    const s = getSession(stage.id);
    if (s) openDetailModal(true, { id: s.id });
}

function handleAddMore(stage: any) {
    openModal(true, {
        isUpdate: false,
        prefill: { workOrderId: workOrderId.value, stageId: stage.id, stageName: stage.stageName },
    });
}

async function openReview() {
    if (!workOrderId.value) return;
    try {
        const res: any = await qcReviewApi.byWorkOrder(workOrderId.value);
        createMessage.success(`Review ${res.reviewCode} – Trạng thái: ${res.status}`);
        // Có thể navigate tới trang review
    } catch (e) {
        createMessage.error('Không thể tải review');
    }
}

function formatDate(d: any) {
    if (!d) return '—';
    return typeof d === 'string' ? d.substring(0, 10) : d;
}
</script>

<style scoped>
.stage-card {
    border-radius: 10px;
    transition: all 0.2s;
    cursor: pointer;
}

.stage-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-pending {
    border-top: 4px solid #d9d9d9;
}

.card-inprogress {
    border-top: 4px solid #1890ff;
    background: #f0f8ff;
}

.card-done {
    border-top: 4px solid #52c41a;
    background: #f6ffed;
}
</style>
