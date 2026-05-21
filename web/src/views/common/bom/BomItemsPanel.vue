<template>
    <div class="bom-items-panel px-4 py-3 bg-gray-50">
        <a-spin :spinning="loading">
            <div v-if="!items.length && !loading" class="text-gray-400 text-sm text-center py-2">
                BOM chưa có nguyên vật liệu nào.
            </div>

            <template v-for="(item, idx) in items" :key="item.id || idx">
                <div class="bom-item-row mb-2 rounded border border-gray-200 bg-white shadow-sm overflow-hidden">
                    <!-- Header dòng NVL -->
                    <div class="flex items-center gap-3 px-3 py-2 cursor-pointer hover:bg-blue-50 transition"
                        @click="item._expanded = !item._expanded">
                        <span class="text-blue-500 text-xs font-bold w-5 select-none">
                            {{ item._expanded ? '▼' : '▶' }}
                        </span>
                        <span class="text-xs font-semibold text-gray-500 w-6">{{ idx + 1 }}</span>

                        <!-- Tên + Mã NVL -->
                        <span class="flex-1 font-medium text-gray-800">
                            {{ item.materialName || item.materialId }}
                            <span class="ml-1 text-gray-400 text-xs">[{{ item.materialCode || '–' }}]</span>
                        </span>

                        <!-- SL + Đơn vị -->
                        <span class="text-sm text-gray-700 w-28 text-right">
                            {{ item.quantity }} <span class="text-gray-400">{{ item.unit }}</span>
                        </span>

                        <!-- Hao hụt -->
                        <span v-if="item.wastageRate" class="text-orange-500 text-xs w-20 text-right">
                            Hao hụt: {{ item.wastageRate }}%
                        </span>

                        <!-- Badge số thay thế -->
                        <a-tag v-if="(item.substitutes || []).length" color="blue" class="ml-2 text-xs">
                            {{ item.substitutes.length }} thay thế
                        </a-tag>
                        <a-tag v-else color="default" class="ml-2 text-xs opacity-50">Không có TT</a-tag>
                    </div>

                    <!-- Panel substitute (collapsible) -->
                    <div v-if="item._expanded" class="border-t border-gray-100 bg-blue-50/30 px-4 py-2">
                        <div class="text-xs font-semibold text-blue-600 mb-2">
                            🔄 Linh kiện có thể thay thế:
                        </div>

                        <div v-if="!(item.substitutes || []).length" class="text-xs text-gray-400 italic">
                            Chưa khai báo linh kiện thay thế.
                        </div>

                        <div v-for="(sub, si) in item.substitutes" :key="sub.id || si"
                            class="flex items-center gap-3 py-1 border-b border-blue-100 last:border-0">
                            <!-- Ưu tiên -->
                            <a-tag color="geekblue" class="!text-xs !px-1 !py-0 w-8 text-center">
                                #{{ sub.priority || si + 1 }}
                            </a-tag>

                            <!-- Tên + Mã NVL thay thế -->
                            <span class="flex-1 text-sm text-gray-700">
                                {{ sub.substituteName || sub.substituteMaterialId }}
                                <span class="text-gray-400 text-xs ml-1">[{{ sub.substituteCode || '–' }}]</span>
                            </span>

                            <!-- Đơn vị -->
                            <span class="text-gray-500 text-xs w-12">{{ sub.substituteUnit || '' }}</span>

                            <!-- Ghi chú -->
                            <span v-if="sub.notes" class="text-gray-400 text-xs italic truncate max-w-xs">
                                {{ sub.notes }}
                            </span>
                        </div>
                    </div>
                </div>
            </template>
        </a-spin>
    </div>
</template>

<script lang="ts" name="bom-items-panel" setup>
import { ref, watch, onMounted } from 'vue';
import { bomApi } from '/@/api/common/bom';

const props = defineProps<{ bomId: string }>();

const loading = ref(false);
const items = ref<any[]>([]);

async function loadItems() {
    if (!props.bomId) return;
    loading.value = true;
    try {
        const data: any = await bomApi.getItems(props.bomId);
        items.value = (data || []).map((i: any) => ({ ...i, _expanded: false }));
    } catch (e) {
        items.value = [];
    } finally {
        loading.value = false;
    }
}

onMounted(loadItems);
watch(() => props.bomId, loadItems);
</script>

<style scoped>
.bom-items-panel {
    border-radius: 6px;
}

.bom-item-row {
    transition: box-shadow 0.2s;
}

.bom-item-row:hover {
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.12);
}
</style>
