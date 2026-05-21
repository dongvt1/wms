<template>
    <div class="p-4">
        <!-- Header -->
        <div class="mb-4 flex items-center justify-between">
            <h2 class="text-lg font-semibold">Báo cáo chất lượng nhà cung cấp</h2>
            <a-button @click="goBack">← Quay lại</a-button>
        </div>

        <!-- Loading -->
        <a-spin :spinning="loading">
            <!-- No supplier ID -->
            <a-empty v-if="!supplierId" description="Không có mã nhà cung cấp. Vui lòng chọn nhà cung cấp từ NCR hoặc Dashboard." />

            <template v-else>
                <!-- Summary Cards -->
                <a-row :gutter="16" class="mb-4">
                    <a-col :span="6">
                        <a-card class="stat-card">
                            <a-statistic title="Tỷ lệ IQC đạt" :value="supplierData?.iqcPassRate ?? 0"
                                suffix="%" :precision="1"
                                :value-style="{ color: passRateColor }" />
                            <div class="text-xs text-gray-400 mt-1">
                                {{ supplierData?.iqcPassed ?? 0 }} / {{ supplierData?.iqcTotal ?? 0 }} phiếu đạt
                            </div>
                        </a-card>
                    </a-col>
                    <a-col :span="6">
                        <a-card class="stat-card">
                            <a-statistic title="Số NCR" :value="supplierData?.ncrCount ?? 0"
                                :value-style="{ color: ncrCountColor }" />
                            <div class="text-xs text-gray-400 mt-1">
                                Tổng số báo cáo không phù hợp
                            </div>
                        </a-card>
                    </a-col>
                    <a-col :span="6">
                        <a-card class="stat-card">
                            <a-statistic title="Xếp hạng" :value="rankingDisplay"
                                :value-style="{ color: '#1890ff' }" />
                            <div class="text-xs text-gray-400 mt-1">
                                Trong tổng {{ supplierData?.totalSuppliers ?? 0 }} nhà cung cấp
                            </div>
                        </a-card>
                    </a-col>
                    <a-col :span="6">
                        <a-card class="stat-card">
                            <a-statistic title="Tổng phiếu IQC" :value="supplierData?.iqcTotal ?? 0" />
                            <div class="text-xs text-gray-400 mt-1">
                                Tổng số phiếu kiểm tra đầu vào
                            </div>
                        </a-card>
                    </a-col>
                </a-row>

                <!-- NCR History Table -->
                <a-card title="Lịch sử NCR" class="mt-4">
                    <a-table :dataSource="ncrList" :columns="ncrColumns" :loading="ncrLoading"
                        :pagination="{ pageSize: 10 }" rowKey="id" size="small" bordered>
                        <template #bodyCell="{ column, record }">
                            <template v-if="column.dataIndex === 'status'">
                                <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
                            </template>
                            <template v-if="column.dataIndex === 'severity'">
                                <a-tag :color="severityColor(record.severity)">{{ severityLabel(record.severity)
                                    }}</a-tag>
                            </template>
                            <template v-if="column.dataIndex === 'proposedAction'">
                                {{ proposedActionLabel(record.proposedAction) }}
                            </template>
                        </template>
                    </a-table>
                </a-card>
            </template>
        </a-spin>
    </div>
</template>

<script lang="ts" name="supplier-quality-report" setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { qmsAnalyticsApi, SupplierReportModel } from '/@/api/wms/qmsAnalytics';
import { wmsNcrApi, NcrModel } from '/@/api/wms/ncr';
import { useMessage } from '/@/hooks/web/useMessage';

const route = useRoute();
const router = useRouter();
const { createMessage } = useMessage();

const loading = ref(false);
const ncrLoading = ref(false);
const supplierData = ref<SupplierReportModel | null>(null);
const ncrList = ref<NcrModel[]>([]);

const supplierId = computed(() => {
    return (route.query.supplierId as string) || (route.params.supplierId as string) || '';
});

const passRateColor = computed(() => {
    const rate = supplierData.value?.iqcPassRate ?? 0;
    if (rate >= 95) return '#52c41a';
    if (rate >= 80) return '#faad14';
    return '#f5222d';
});

const ncrCountColor = computed(() => {
    const count = supplierData.value?.ncrCount ?? 0;
    if (count === 0) return '#52c41a';
    if (count <= 3) return '#faad14';
    return '#f5222d';
});

const rankingDisplay = computed(() => {
    const ranking = supplierData.value?.ranking;
    if (!ranking) return '—';
    return `#${ranking}`;
});

const ncrColumns = [
    { title: 'Mã NCR', dataIndex: 'ncrCode', width: 150 },
    { title: 'Trạng thái', dataIndex: 'status', width: 120 },
    { title: 'Mức độ', dataIndex: 'severity', width: 110 },
    { title: 'Mô tả lỗi', dataIndex: 'description', ellipsis: true },
    { title: 'Hành động đề xuất', dataIndex: 'proposedAction', width: 160 },
    { title: 'Ngày tạo', dataIndex: 'createTime', width: 150 },
];

async function loadSupplierReport() {
    if (!supplierId.value) return;
    loading.value = true;
    try {
        const res = await qmsAnalyticsApi.supplier(supplierId.value);
        supplierData.value = res;
    } catch (e: any) {
        createMessage.error(e?.message || 'Lỗi tải báo cáo nhà cung cấp');
    } finally {
        loading.value = false;
    }
}

async function loadNcrHistory() {
    if (!supplierId.value) return;
    ncrLoading.value = true;
    try {
        const res = await wmsNcrApi.bySupplier(supplierId.value);
        ncrList.value = res || [];
    } catch (e: any) {
        createMessage.error(e?.message || 'Lỗi tải lịch sử NCR');
    } finally {
        ncrLoading.value = false;
    }
}

function goBack() {
    router.back();
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

function proposedActionLabel(s: string) {
    const map: Record<string, string> = {
        return: 'Trả nhà cung cấp', repair: 'Sửa chữa',
        scrap: 'Hủy', accept_conditional: 'Chấp nhận có điều kiện',
    };
    return map[s] || s || '—';
}

onMounted(() => {
    loadSupplierReport();
    loadNcrHistory();
});
</script>

<style scoped>
.stat-card {
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}
</style>
