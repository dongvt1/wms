<template>
    <div class="bom-manage flex gap-4 p-4 h-full">
        <!-- ══ LEFT PANEL: BOM List ══ -->
        <div class="bom-list-panel w-80 flex-shrink-0 flex flex-col bg-white rounded-lg shadow border border-gray-200">
            <!-- Search -->
            <div class="p-3 border-b border-gray-100">
                <a-input-search v-model:value="searchText" placeholder="Tìm mã / tên BOM..." allow-clear
                    @search="filterBoms" @change="filterBoms" />
                <div class="flex gap-2 mt-2">
                    <a-select v-model:value="filterStatus" style="flex:1" placeholder="Trạng thái" allow-clear
                        @change="filterBoms">
                        <a-select-option value="active">Đang dùng</a-select-option>
                        <a-select-option value="inactive">Ngừng</a-select-option>
                    </a-select>
                    <a-button type="primary" size="small" @click="handleAdd">+ Thêm</a-button>
                </div>
            </div>

            <!-- BOM items -->
            <div class="overflow-y-auto flex-1">
                <a-spin :spinning="listLoading">
                    <div v-if="!filteredBoms.length && !listLoading" class="text-center text-gray-400 text-sm py-8">
                        Không có BOM nào</div>

                    <div v-for="bom in filteredBoms" :key="bom.id"
                        class="bom-list-item px-3 py-2 cursor-pointer border-b border-gray-50 hover:bg-blue-50 transition"
                        :class="{ 'bg-blue-100 border-l-4 border-l-blue-500': selectedBom?.id === bom.id }"
                        @click="selectBom(bom)">
                        <div class="flex items-center justify-between">
                            <span class="font-medium text-sm text-gray-800 truncate">{{ bom.bomName }}</span>
                            <a-tag :color="bom.status === 'active' ? 'green' : 'red'" class="!text-xs !ml-1 !px-1">
                                {{ bom.status === 'active' ? '✓' : '✗' }}
                            </a-tag>
                        </div>
                        <div class="text-xs text-gray-400 mt-0.5 flex gap-2">
                            <span>{{ bom.bomCode }}</span>
                            <span>·</span>
                            <span>v{{ bom.version }}</span>
                            <span v-if="bom.productName">· {{ bom.productName }}</span>
                        </div>
                    </div>
                </a-spin>
            </div>

            <!-- Pagination small -->
            <div class="p-2 border-t text-xs text-gray-400 text-center">
                {{ filteredBoms.length }} / {{ allBoms.length }} BOM
            </div>
        </div>

        <!-- ══ RIGHT PANEL: BOM Detail ══ -->
        <div class="bom-detail-panel flex-1 flex flex-col min-w-0">
            <!-- Empty state -->
            <div v-if="!selectedBom"
                class="flex-1 flex items-center justify-center bg-white rounded-lg shadow border border-gray-200">
                <div class="text-center text-gray-300">
                    <div class="text-5xl mb-3">📋</div>
                    <div class="text-lg font-medium">Chọn BOM để quản lý</div>
                    <div class="text-sm mt-1">Hoặc nhấn <strong>+ Thêm</strong> để tạo BOM mới</div>
                </div>
            </div>

            <template v-else>
                <!-- Header -->
                <div class="bg-white rounded-lg shadow border border-gray-200 p-4 mb-3">
                    <div class="flex items-start justify-between gap-4">
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-2 flex-wrap">
                                <h2 class="text-lg font-bold text-gray-800 m-0">{{ selectedBom.bomName }}</h2>
                                <a-tag :color="selectedBom.status === 'active' ? 'green' : 'red'">
                                    {{ selectedBom.status === 'active' ? 'Đang dùng' : 'Ngừng' }}
                                </a-tag>
                                <a-tag v-if="selectedBom.isDefault" color="gold">Mặc định</a-tag>
                            </div>
                            <div class="flex gap-4 mt-1 text-sm text-gray-500 flex-wrap">
                                <span><strong>Mã:</strong> {{ selectedBom.bomCode }}</span>
                                <span><strong>Phiên bản:</strong> {{ selectedBom.version }}</span>
                                <span v-if="selectedBom.productName"><strong>Thành phẩm:</strong> {{
                                    selectedBom.productName }}</span>
                                <span v-if="selectedBom.outputQuantity">
                                    <strong>SL đầu ra:</strong> {{ selectedBom.outputQuantity }} {{ selectedBom.unit }}
                                </span>
                            </div>
                            <div v-if="selectedBom.notes" class="text-xs text-gray-400 mt-1 italic">{{ selectedBom.notes
                                }}</div>
                        </div>
                        <div class="flex gap-2 flex-shrink-0">
                            <a-button @click="handleEdit(selectedBom)">✏️ Sửa BOM</a-button>
                            <a-popconfirm title="Xác nhận xóa BOM này?" @confirm="handleDelete(selectedBom)">
                                <a-button danger>🗑 Xóa</a-button>
                            </a-popconfirm>
                        </div>
                    </div>
                </div>

                <!-- NVL Table -->
                <div class="bg-white rounded-lg shadow border border-gray-200 flex-1 flex flex-col overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-3 border-b border-gray-100">
                        <span class="font-semibold text-gray-700">
                            📦 Nguyên vật liệu ({{ bomItems.length }} dòng)
                        </span>
                        <a-button type="primary" size="small" @click="handleAddItem">+ Thêm NVL</a-button>
                    </div>

                    <div class="overflow-y-auto flex-1 p-3">
                        <a-spin :spinning="itemsLoading">
                            <div v-if="!bomItems.length && !itemsLoading"
                                class="text-center text-gray-400 text-sm py-8">
                                Chưa có NVL nào. Nhấn <strong>+ Thêm NVL</strong> để bắt đầu.
                            </div>

                            <div v-for="(item, idx) in bomItems" :key="item.id || idx"
                                class="nvl-row mb-3 rounded-lg border overflow-hidden"
                                :class="expandedItems.has(idx) ? 'border-blue-300 shadow-sm' : 'border-gray-200'">

                                <!-- NVL Header Row -->
                                <div class="flex items-center gap-3 px-3 py-2 bg-gray-50 hover:bg-blue-50 transition">
                                    <!-- Index -->
                                    <span class="text-xs text-gray-400 font-mono w-5 text-center">{{ idx + 1 }}</span>

                                    <!-- Expand toggle -->
                                    <button class="text-blue-400 hover:text-blue-600 text-xs font-bold w-5"
                                        @click="toggleExpand(idx)">
                                        {{ expandedItems.has(idx) ? '▼' : '▶' }}
                                    </button>

                                    <!-- Material Select -->
                                    <div class="flex-1 min-w-0">
                                        <a-select v-model:value="item.materialId" show-search :options="materialOptions"
                                            :filter-option="filterOption" placeholder="Chọn vật tư" size="small"
                                            style="width:100%" @change="onMaterialChange(item)" />
                                    </div>

                                    <!-- Quantity -->
                                    <a-input-number v-model:value="item.quantity" :min="0.001" size="small"
                                        style="width:90px" placeholder="SL" />

                                    <!-- Unit -->
                                    <a-input v-model:value="item.unit" size="small" style="width:70px"
                                        placeholder="Đơn vị" />

                                    <!-- Wastage -->
                                    <a-input-number v-model:value="item.wastageRate" :min="0" :max="100" size="small"
                                        style="width:80px" :placeholder="'Hao hụt%'"
                                        :formatter="(v: any) => v ? `${v}%` : ''"
                                        :parser="(v: any) => v?.replace('%', '')" />

                                    <!-- Substitute badge -->
                                    <a-badge :count="(item.substitutes || []).length"
                                        :number-style="{ backgroundColor: '#3b82f6', fontSize: '10px' }">
                                        <a-button size="small" type="text"
                                            :class="expandedItems.has(idx) ? 'text-blue-600' : 'text-gray-400'"
                                            @click="toggleExpand(idx)">TT</a-button>
                                    </a-badge>

                                    <!-- Delete -->
                                    <a-popconfirm title="Xóa dòng NVL này?" @confirm="removeItem(idx)">
                                        <a-button size="small" type="text" danger>✕</a-button>
                                    </a-popconfirm>
                                </div>

                                <!-- Substitute Panel (expandable) -->
                                <div v-if="expandedItems.has(idx)"
                                    class="bg-blue-50/40 border-t border-blue-100 px-4 py-3">
                                    <div class="flex items-center justify-between mb-2">
                                        <span class="text-xs font-semibold text-blue-600">🔄 Linh kiện thay thế</span>
                                        <a-button size="small" type="dashed" @click="addSubstitute(item)">
                                            + Thêm thay thế
                                        </a-button>
                                    </div>

                                    <div v-if="!(item.substitutes || []).length"
                                        class="text-xs text-gray-400 italic mb-1">
                                        Chưa có linh kiện thay thế.
                                    </div>

                                    <div v-for="(sub, si) in item.substitutes" :key="si"
                                        class="flex items-center gap-2 mb-1.5">
                                        <!-- Priority -->
                                        <a-input-number v-model:value="sub.priority" :min="1" :max="99" size="small"
                                            style="width:54px" placeholder="#" />

                                        <!-- Substitute material select -->
                                        <a-select v-model:value="sub.substituteMaterialId" show-search
                                            :options="materialOptions" :filter-option="filterOption"
                                            placeholder="Vật tư thay thế" size="small" style="flex:1" />

                                        <!-- Notes -->
                                        <a-input v-model:value="sub.notes" size="small" style="width:140px"
                                            placeholder="Ghi chú..." />

                                        <!-- Remove substitute -->
                                        <a-button size="small" type="text" danger
                                            @click="removeSubstitute(item, si)">✕</a-button>
                                    </div>
                                </div>
                            </div>
                        </a-spin>
                    </div>

                    <!-- Footer actions -->
                    <div class="border-t border-gray-100 px-4 py-3 flex justify-between items-center bg-gray-50">
                        <span class="text-xs text-gray-400">
                            Tổng {{ bomItems.length }} NVL ·
                            {{bomItems.reduce((s, i) => s + ((i.substitutes || []).length), 0)}} thay thế
                        </span>
                        <div class="flex gap-2">
                            <a-button @click="loadBomDetail">↩ Hoàn tác</a-button>
                            <a-button type="primary" :loading="saving" @click="handleSaveItems">
                                💾 Lưu thay đổi
                            </a-button>
                        </div>
                    </div>
                </div>
            </template>
        </div>

        <!-- BOM Modal (Create / Edit) -->
        <BomModal @register="registerModal" @success="onBomSaved" />
    </div>
</template>

<script lang="ts" name="bom-manage" setup>
import { ref, computed, onMounted } from 'vue';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { bomApi, type BomModel, type BomItemModel } from '/@/api/common/bom';
import { materialApi } from '/@/api/common/material';
import BomModal from './BomModal.vue';

const { createMessage } = useMessage();
const [registerModal, { openModal }] = useModal();

// ── State ──
const allBoms = ref<BomModel[]>([]);
const listLoading = ref(false);
const searchText = ref('');
const filterStatus = ref<string | undefined>(undefined);

const selectedBom = ref<BomModel | null>(null);
const bomItems = ref<any[]>([]);
const itemsLoading = ref(false);
const saving = ref(false);
const expandedItems = ref<Set<number>>(new Set());

const materialOptions = ref<any[]>([]);

// ── Filtered BOM list ──
const filteredBoms = computed(() => {
    const q = searchText.value.toLowerCase();
    return allBoms.value.filter(b => {
        const matchText = !q || b.bomCode?.toLowerCase().includes(q) || b.bomName?.toLowerCase().includes(q);
        const matchStatus = !filterStatus.value || b.status === filterStatus.value;
        return matchText && matchStatus;
    });
});

// ── Load all BOMs ──
async function loadBoms() {
    listLoading.value = true;
    try {
        const res: any = await bomApi.list({ pageSize: 999 });
        allBoms.value = res?.records || res || [];
    } finally {
        listLoading.value = false;
    }
}

// ── Select BOM ──
async function selectBom(bom: BomModel) {
    selectedBom.value = bom;
    expandedItems.value = new Set();
    await loadBomDetail();
}

async function loadBomDetail() {
    if (!selectedBom.value?.id) return;
    itemsLoading.value = true;
    try {
        const items: any = await bomApi.getItems(selectedBom.value.id);
        bomItems.value = (items || []).map((i: any) => ({
            ...i,
            substitutes: i.substitutes || [],
        }));
    } finally {
        itemsLoading.value = false;
    }
}

// ── Expand/Collapse ──
function toggleExpand(idx: number) {
    const s = new Set(expandedItems.value);
    s.has(idx) ? s.delete(idx) : s.add(idx);
    expandedItems.value = s;
}

// ── Add / Remove NVL ──
function handleAddItem() {
    bomItems.value.push({ materialId: undefined, quantity: 1, unit: '', wastageRate: 0, substitutes: [] });
}

function removeItem(idx: number) {
    bomItems.value.splice(idx, 1);
    const s = new Set(expandedItems.value);
    s.delete(idx);
    expandedItems.value = s;
}

// ── Add / Remove Substitute ──
function addSubstitute(item: any) {
    if (!item.substitutes) item.substitutes = [];
    item.substitutes.push({ substituteMaterialId: undefined, priority: item.substitutes.length + 1, notes: '' });
    // Auto-expand
    const idx = bomItems.value.indexOf(item);
    if (idx >= 0) {
        const s = new Set(expandedItems.value);
        s.add(idx);
        expandedItems.value = s;
    }
}

function removeSubstitute(item: any, si: number) {
    item.substitutes.splice(si, 1);
}

// ── Auto-fill unit from material ──
function onMaterialChange(item: any) {
    const mat = materialOptions.value.find(m => m.value === item.materialId);
    if (mat?.unit && !item.unit) item.unit = mat.unit;
}

// ── Save items ──
async function handleSaveItems() {
    if (!selectedBom.value) return;
    saving.value = true;
    try {
        const payload = {
            bom: { ...selectedBom.value },
            items: bomItems.value
                .filter(i => i.materialId && i.quantity)
                .map(i => ({
                    ...i,
                    substitutes: (i.substitutes || []).filter((s: any) => s.substituteMaterialId),
                })),
        };
        await bomApi.edit(payload);
        createMessage.success('Lưu BOM thành công!');
        await loadBomDetail();
    } finally {
        saving.value = false;
    }
}

// ── CRUD BOM ──
function handleAdd() {
    openModal(true, { isUpdate: false });
}

function handleEdit(bom: BomModel) {
    openModal(true, { record: bom, isUpdate: true });
}

async function handleDelete(bom: BomModel) {
    await bomApi.delete({ id: bom.id! });
    createMessage.success('Đã xóa BOM!');
    selectedBom.value = null;
    bomItems.value = [];
    await loadBoms();
}

async function onBomSaved() {
    await loadBoms();
    createMessage.success('Lưu BOM thành công!');
}

// ── Filter trigger ──
function filterBoms() { /* computed auto-updates */ }

// ── Filter option for Select ──
function filterOption(input: string, option: any) {
    return option.label?.toLowerCase().includes(input.toLowerCase());
}

// ── Init ──
onMounted(async () => {
    await loadBoms();
    try {
        const mats: any = await materialApi.listAll();
        materialOptions.value = (mats || []).map((m: any) => ({
            label: `${m.code} - ${m.name}`,
            value: m.id,
            unit: m.unit,
        }));
    } catch (e) { }
});
</script>

<style scoped>
.bom-manage {
    min-height: calc(100vh - 140px);
}

.bom-list-panel {
    min-height: 0;
}

.bom-detail-panel {
    min-height: 0;
}

.nvl-row {
    transition: box-shadow 0.2s;
}

.nvl-row:hover {
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}
</style>
