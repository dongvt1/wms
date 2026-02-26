<template>
    <BasicModal v-bind="$attrs" @register="registerModal"
        :title="isUpdate ? 'Sửa phiên kiểm tra' : 'Tạo phiên kiểm tra'" width="1000px" @ok="handleSubmit">
        <BasicForm @register="registerForm" />

        <a-divider>Giá trị tham số kiểm tra</a-divider>
        <template v-if="values.length === 0">
            <a-empty description="Chọn công đoạn để tự động tải tham số" />
        </template>
        <template v-for="(val, idx) in values" :key="idx">
            <a-card size="small" class="mb-2"
                :title="`${idx + 1}. ${val.paramName}${val.unit ? ' (' + val.unit + ')' : ''}`">
                <!-- input_type = list: nhiều lần đo -->
                <template v-if="val.inputType === 'list'">
                    <a-table :dataSource="val.items" :columns="listColumns" :pagination="false" size="small" bordered>
                        <template #bodyCell="{ column, record: item }">
                            <template v-if="column.key === 'measuredValue'">
                                <a-input v-model:value="item.measuredValue" placeholder="Giá trị đo..." />
                            </template>
                            <template v-if="column.key === 'result'">
                                <a-select v-model:value="item.result" style="width:100%">
                                    <a-select-option value="passed">✅ Đạt</a-select-option>
                                    <a-select-option value="failed">❌ Không đạt</a-select-option>
                                    <a-select-option value="na">N/A</a-select-option>
                                </a-select>
                            </template>
                            <template v-if="column.key === 'notes'">
                                <a-input v-model:value="item.notes" placeholder="Ghi chú..." />
                            </template>
                            <template v-if="column.key === 'action'">
                                <a-button danger size="small"
                                    @click="val.items.splice(val.items.indexOf(item), 1)">Xóa</a-button>
                            </template>
                        </template>
                    </a-table>
                    <a-button type="dashed" size="small" class="mt-1" @click="addListItem(val)">+ Thêm lần đo</a-button>
                </template>

                <!-- input các loại khác -->
                <template v-else>
                    <div class="flex gap-2 items-center">
                        <div class="flex-1">
                            <a-input v-if="val.inputType === 'text'" v-model:value="val.actualValue"
                                placeholder="Nhập giá trị..." />
                            <a-input-number v-else-if="val.inputType === 'number'" v-model:value="val.actualValue"
                                style="width:100%" :min="val.minValue" :max="val.maxValue" placeholder="Nhập số..." />
                            <a-select v-else-if="val.inputType === 'pass_fail'" v-model:value="val.actualValue"
                                style="width:100%">
                                <a-select-option value="passed">✅ Đạt</a-select-option>
                                <a-select-option value="failed">❌ Không đạt</a-select-option>
                                <a-select-option value="na">N/A</a-select-option>
                            </a-select>
                            <a-select v-else-if="val.inputType === 'select'" v-model:value="val.actualValue"
                                style="width:100%">
                                <a-select-option v-for="opt in parseOptions(val.optionsJson)" :key="opt" :value="opt">{{
                                    opt }}</a-select-option>
                            </a-select>
                            <a-date-picker v-else-if="val.inputType === 'date'" v-model:value="val.actualValue"
                                format="YYYY-MM-DD" valueFormat="YYYY-MM-DD" style="width:100%" />
                        </div>
                        <a-select v-model:value="val.result" style="width:130px">
                            <a-select-option value="passed">✅ Đạt</a-select-option>
                            <a-select-option value="failed">❌ Không đạt</a-select-option>
                            <a-select-option value="na">N/A</a-select-option>
                        </a-select>
                        <a-input v-model:value="val.notes" placeholder="Ghi chú..." style="width:200px" />
                    </div>
                </template>
            </a-card>
        </template>
    </BasicModal>
</template>

<script lang="ts" name="qc-session-modal" setup>
import { ref, unref, watch } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { qcSessionApi } from '/@/api/warehouse/qcSession';
import { qcStageApi } from '/@/api/warehouse/qcStage';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref(false);
const recordId = ref('');
const values = ref<any[]>([]);
const stageOptions = ref<any[]>([]);

const listColumns = [
    { title: 'Lần đo', dataIndex: 'seqNo', width: 60 },
    { title: 'Giá trị', key: 'measuredValue', width: '35%' },
    { title: 'Kết quả', key: 'result', width: '20%' },
    { title: 'Ghi chú', key: 'notes', width: '30%' },
    { title: '', key: 'action', width: 60 },
];

const [registerForm, { setFieldsValue, resetFields, validate, updateSchema }] = useForm({
    labelWidth: 140,
    schemas: [
        { field: 'sessionCode', label: 'Mã phiên', component: 'Input', helpMessage: 'Để trống để tự động sinh mã' },
        { field: 'workOrderId', label: 'Mã lệnh SX (WO)', component: 'Input', required: true },
        {
            field: 'stageId', label: 'Công đoạn kiểm tra', component: 'Select', required: true,
            componentProps: { options: [], showSearch: true, placeholder: 'Chọn công đoạn' }
        },
        { field: 'inspector', label: 'Người kiểm tra', component: 'Input' },
        { field: 'inspectionDate', label: 'Ngày kiểm tra', component: 'DatePicker', componentProps: { valueFormat: 'YYYY-MM-DD', style: 'width:100%' } },
        { field: 'notes', label: 'Ghi chú', component: 'InputTextArea', componentProps: { rows: 2 } },
    ],
    showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    values.value = [];
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    try {
        const stages: any = await qcStageApi.listActive();
        const opts = (stages || []).map((s: any) => ({ label: `${s.stageCode} - ${s.stageName}`, value: s.id }));
        stageOptions.value = opts;
        updateSchema([{ field: 'stageId', componentProps: { options: opts } }]);
    } catch (e) { }

    if (unref(isUpdate) && data.record) {
        // Chế độ sửa: load data cũ
        recordId.value = data.record.id;
        setFieldsValue({ ...data.record });
        try {
            const res: any = await qcSessionApi.getValues(data.record.id);
            values.value = (res || []).map((v: any) => ({ ...v, items: v.items || [] }));
        } catch (e) { }
    } else if (data?.prefill) {
        // Chế độ tạo mới từ Dashboard: điền sẵn WO + stage, auto-load params
        const { workOrderId, stageId, stageName } = data.prefill;
        setFieldsValue({ workOrderId, stageId, stageName });
        if (stageId) await onStageChange(stageId);
    }
});

async function onStageChange(stageId: string) {
    if (!stageId || unref(isUpdate)) return;
    try {
        const params: any = await qcStageApi.getParams(stageId);
        values.value = (params || []).map((p: any) => ({
            paramId: p.id,
            paramName: p.paramName,
            inputType: p.inputType,
            unit: p.unit,
            optionsJson: p.optionsJson,
            minValue: p.minValue,
            maxValue: p.maxValue,
            actualValue: p.defaultValue || '',
            result: '',
            notes: '',
            items: [],
        }));
    } catch (e) { }
}

function addListItem(val: any) {
    const nextSeq = (val.items?.length || 0) + 1;
    val.items.push({ seqNo: nextSeq, measuredValue: '', result: '', notes: '' });
}

function parseOptions(json: string) {
    if (!json) return [];
    try { return JSON.parse(json); } catch { return json.split(',').map((s: string) => s.trim()); }
}

async function handleSubmit() {
    try {
        const formValues = await validate();
        setModalProps({ confirmLoading: true });
        if (formValues.stageId && values.value.length === 0) {
            await onStageChange(formValues.stageId);
        }
        const payload = {
            session: { ...formValues, id: unref(isUpdate) ? unref(recordId) : undefined },
            values: values.value,
        };
        if (unref(isUpdate)) {
            await qcSessionApi.edit(payload);
        } else {
            await qcSessionApi.add(payload);
        }
        emit('success');
        closeModal();
    } finally {
        setModalProps({ confirmLoading: false });
    }
}
</script>

<style scoped>
.ant-card {
    border-radius: 6px;
}
</style>
