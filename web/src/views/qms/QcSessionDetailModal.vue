<template>
    <BasicModal v-bind="$attrs" @register="registerModal" title="Chi tiết phiên kiểm tra" width="860px" :footer="null">
        <template v-if="detail">
            <a-descriptions bordered size="small" :column="2">
                <a-descriptions-item label="Mã phiên">{{ detail.session?.sessionCode }}</a-descriptions-item>
                <a-descriptions-item label="Trạng thái">
                    <a-tag :color="detail.session?.status === 'completed' ? 'green' : 'blue'">
                        {{ detail.session?.status === 'completed' ? 'Hoàn thành' : 'Nháp' }}
                    </a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="Công đoạn">{{ detail.session?.stageName }}</a-descriptions-item>
                <a-descriptions-item label="Mã WO">{{ detail.session?.workOrderId }}</a-descriptions-item>
                <a-descriptions-item label="Người KT">{{ detail.session?.inspector }}</a-descriptions-item>
                <a-descriptions-item label="Ngày KT">{{ detail.session?.inspectionDate }}</a-descriptions-item>
                <a-descriptions-item label="Ghi chú" :span="2">{{ detail.session?.notes }}</a-descriptions-item>
            </a-descriptions>

            <a-divider>Kết quả tham số kiểm tra</a-divider>

            <template v-for="(val, idx) in detail.values" :key="idx">
                <a-card size="small" class="mb-2"
                    :title="`${idx + 1}. ${val.paramName}${val.unit ? ' (' + val.unit + ')' : ''}`"
                    :headStyle="{ background: resultBg(val.result) }">
                    <!-- List type -->
                    <template v-if="val.inputType === 'list'">
                        <a-table :dataSource="val.items || []" :columns="listViewCols" :pagination="false" size="small"
                            bordered>
                            <template #bodyCell="{ column, record: item }">
                                <template v-if="column.key === 'result'">
                                    <a-tag
                                        :color="item.result === 'passed' ? 'green' : item.result === 'failed' ? 'red' : 'default'">
                                        {{ item.result === 'passed' ? '✅ Đạt' : item.result === 'failed' ? '❌ Không đạt'
                                        : 'N/A' }}
                                    </a-tag>
                                </template>
                            </template>
                        </a-table>
                    </template>
                    <!-- Other types -->
                    <template v-else>
                        <a-row :gutter="16">
                            <a-col :span="10">
                                <span class="text-gray-500">Giá trị: </span>
                                <strong>{{ val.actualValue || '—' }}</strong>
                            </a-col>
                            <a-col :span="6">
                                <a-tag
                                    :color="val.result === 'passed' ? 'green' : val.result === 'failed' ? 'red' : 'default'">
                                    {{ val.result === 'passed' ? '✅ Đạt' : val.result === 'failed' ? '❌ Không đạt' :
                                    'N/A' }}
                                </a-tag>
                            </a-col>
                            <a-col :span="8" class="text-gray-500 text-sm">{{ val.notes }}</a-col>
                        </a-row>
                    </template>
                </a-card>
            </template>
        </template>
        <a-spin v-else />
    </BasicModal>
</template>

<script lang="ts" name="qc-session-detail-modal" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { qcSessionApi } from '/@/api/warehouse/qcSession';

const emit = defineEmits(['success', 'register']);
const detail = ref<any>(null);

const listViewCols = [
    { title: 'Lần đo', dataIndex: 'seqNo', width: 70 },
    { title: 'Giá trị đo', dataIndex: 'measuredValue', width: '40%' },
    { title: 'Kết quả', key: 'result', width: '20%' },
    { title: 'Ghi chú', dataIndex: 'notes' },
];

const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    detail.value = null;
    setModalProps({ confirmLoading: false });
    if (data?.id) {
        const res: any = await qcSessionApi.queryById(data.id);
        detail.value = res;
    }
});

function resultBg(result: string) {
    if (result === 'passed') return '#f6ffed';
    if (result === 'failed') return '#fff2f0';
    return '#fafafa';
}
</script>
