<template>
    <BasicModal v-bind="$attrs" @register="registerModal" :title="`Phiên bản BOM: ${productName}`" width="960px"
        :footer="null">
        <!-- Toolbar -->
        <div class="flex justify-between items-center mb-3">
            <a-space>
                <a-tag color="blue">{{ productCode }}</a-tag>
                <span class="text-gray-500 text-sm">Tổng {{ bomList.length }} phiên bản</span>
            </a-space>
            <a-button type="primary" size="small" @click="handleAdd">+ Thêm phiên bản BOM</a-button>
        </div>

        <!-- BOM version table -->
        <a-table :dataSource="bomList" :columns="columns" :loading="loading" :pagination="false" size="small" bordered
            row-key="id">
            <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'status'">
                    <a-tag :color="record.status === 'active' ? 'green' : 'default'">
                        {{ record.status === 'active' ? 'Đang dùng' : 'Ngừng' }}
                    </a-tag>
                </template>
                <template v-if="column.key === 'isDefault'">
                    <a-tag v-if="record.isDefault" color="gold">⭐ Mặc định</a-tag>
                    <a-button v-else type="link" size="small" @click="setDefault(record)">Đặt mặc định</a-button>
                </template>
                <template v-if="column.key === 'action'">
                    <a-space>
                        <a-button type="link" size="small" @click="handleEdit(record)">Sửa</a-button>
                        <a-button type="link" size="small" @click="handleViewItems(record)">Xem NVL</a-button>
                        <a-popconfirm title="Xác nhận xóa phiên bản BOM?" @confirm="handleDelete(record)">
                            <a-button type="link" size="small" danger>Xóa</a-button>
                        </a-popconfirm>
                    </a-space>
                </template>
            </template>
        </a-table>

        <!-- BOM Items drawer -->
        <a-drawer v-model:open="itemDrawerVisible" :title="`NVL trong ${selectedBom?.bomName}`" width="520"
            placement="right">
            <a-table :dataSource="bomItems" :columns="itemCols" :loading="itemsLoading" :pagination="false" size="small"
                bordered />
        </a-drawer>

        <!-- Edit/Add BOM modal -->
        <BomModal @register="registerBomModal" @success="loadBomList" />
    </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner, useModal } from '/@/components/Modal';
import { bomApi } from '/@/api/warehouse/bom';
import { useMessage } from '/@/hooks/web/useMessage';
import BomModal from '/@/views/common/bom/BomModal.vue';

const emit = defineEmits(['register']);
const { createMessage } = useMessage();

const productId = ref('');
const productName = ref('');
const productCode = ref('');
const bomList = ref<any[]>([]);
const loading = ref(false);
const bomItems = ref<any[]>([]);
const itemsLoading = ref(false);
const itemDrawerVisible = ref(false);
const selectedBom = ref<any>(null);

const columns = [
    { title: 'Mã BOM', dataIndex: 'bomCode', width: 120 },
    { title: 'Tên BOM', dataIndex: 'bomName', width: 200 },
    { title: 'Phiên bản', dataIndex: 'version', width: 90 },
    { title: 'SL TP đầu ra', dataIndex: 'outputQuantity', width: 120 },
    { title: 'Đơn vị', dataIndex: 'unit', width: 80 },
    { title: 'Trạng thái', key: 'status', width: 110 },
    { title: 'Mặc định', key: 'isDefault', width: 140 },
    { title: 'Thao tác', key: 'action', width: 180, fixed: 'right' },
];

const itemCols = [
    { title: 'Mã NVL', dataIndex: 'materialCode', width: 120 },
    { title: 'Tên NVL', dataIndex: 'materialName', width: 200 },
    { title: 'Số lượng', dataIndex: 'quantity', width: 100 },
    { title: 'Đơn vị', dataIndex: 'unit', width: 80 },
    { title: 'Hao hụt %', dataIndex: 'wastagePct', width: 90 },
];

const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    productId.value = data.productId;
    productName.value = data.productName || '';
    productCode.value = data.productCode || '';
    await loadBomList();
});

const [registerBomModal, { openModal: openBomModal }] = useModal();

async function loadBomList() {
    loading.value = true;
    try {
        const res: any = await bomApi.getByProductId(productId.value);
        bomList.value = res || [];
    } catch { bomList.value = []; }
    finally { loading.value = false; }
}

function handleAdd() {
    openBomModal(true, { isUpdate: false, defaultProductId: productId.value });
}

function handleEdit(record: any) {
    openBomModal(true, { isUpdate: true, record });
}

async function handleDelete(record: any) {
    await bomApi.delete({ id: record.id });
    createMessage.success('Đã xóa phiên bản BOM!');
    await loadBomList();
}

async function setDefault(record: any) {
    try {
        await bomApi.setDefault({ bomId: record.id, productId: productId.value });
        createMessage.success('Đã đặt làm phiên bản mặc định!');
        await loadBomList();
    } catch { createMessage.error('Thao tác thất bại!'); }
}

async function handleViewItems(record: any) {
    selectedBom.value = record;
    itemDrawerVisible.value = true;
    itemsLoading.value = true;
    try {
        const items: any = await bomApi.getItems(record.id);
        bomItems.value = items || [];
    } catch { bomItems.value = []; }
    finally { itemsLoading.value = false; }
}
</script>
