<template>
    <BasicModal v-bind="$attrs" @register="registerModal"
        :title="isUpdate ? 'Sửa công đoạn kiểm tra' : 'Tạo công đoạn kiểm tra'" width="1000px" @ok="handleSubmit">
        <BasicForm @register="registerForm" />

        <a-divider>Danh sách tham số input</a-divider>
        <a-table :dataSource="params" :columns="paramColumns" :pagination="false" size="small" bordered>
            <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'paramName'">
                    <a-input v-model:value="record.paramName" placeholder="Tên tham số" />
                </template>
                <template v-if="column.key === 'paramCode'">
                    <a-input v-model:value="record.paramCode" placeholder="Mã tham số" />
                </template>
                <template v-if="column.key === 'inputType'">
                    <a-select v-model:value="record.inputType" style="width:100%">
                        <a-select-option value="text">Văn bản</a-select-option>
                        <a-select-option value="number">Số</a-select-option>
                        <a-select-option value="pass_fail">Đạt/Không đạt</a-select-option>
                        <a-select-option value="select">Lựa chọn</a-select-option>
                        <a-select-option value="date">Ngày</a-select-option>
                        <a-select-option value="list">Danh sách (nhiều lần đo)</a-select-option>
                    </a-select>
                </template>
                <template v-if="column.key === 'unit'">
                    <a-input v-model:value="record.unit" placeholder="mm, kg..." />
                </template>
                <template v-if="column.key === 'minMax'">
                    <span v-if="record.inputType === 'number'" class="flex gap-1">
                        <a-input-number v-model:value="record.minValue" placeholder="Min" style="width:48%" />
                        <a-input-number v-model:value="record.maxValue" placeholder="Max" style="width:48%" />
                    </span>
                    <span v-else-if="record.inputType === 'select'">
                        <a-input v-model:value="record.optionsJson" placeholder='["A","B","C"]' />
                    </span>
                </template>
                <template v-if="column.key === 'isRequired'">
                    <a-checkbox v-model:checked="record.isRequiredBool"
                        @change="record.isRequired = record.isRequiredBool ? 1 : 0" />
                </template>
                <template v-if="column.key === 'action'">
                    <a-button danger size="small" @click="params.splice(params.indexOf(record), 1)">Xóa</a-button>
                </template>
            </template>
        </a-table>
        <a-button type="dashed" block class="mt-2" @click="addParam">+ Thêm tham số</a-button>
    </BasicModal>
</template>

<script lang="ts" name="qc-stage-modal" setup>
import { ref, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { qcStageApi } from '/@/api/warehouse/qcStage';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref(false);
const recordId = ref('');
const params = ref<any[]>([]);

const paramColumns = [
    { title: 'Tên tham số *', key: 'paramName', width: '18%' },
    { title: 'Mã tham số', key: 'paramCode', width: '12%' },
    { title: 'Kiểu nhập', key: 'inputType', width: '18%' },
    { title: 'Đơn vị', key: 'unit', width: '8%' },
    { title: 'Min/Max hoặc Options', key: 'minMax', width: '22%' },
    { title: 'Bắt buộc', key: 'isRequired', width: '8%' },
    { title: '', key: 'action', width: '5%' },
];

const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
    labelWidth: 120,
    schemas: [
        { field: 'stageCode', label: 'Mã công đoạn', component: 'Input', helpMessage: 'Để trống để tự động sinh mã' },
        { field: 'stageName', label: 'Tên công đoạn', component: 'Input', required: true },
        { field: 'description', label: 'Mô tả', component: 'InputTextArea', componentProps: { rows: 2 } },
        { field: 'sortOrder', label: 'Thứ tự', component: 'InputNumber', defaultValue: 0, componentProps: { style: 'width:100%' } },
        {
            field: 'status', label: 'Trạng thái', component: 'Select', defaultValue: 'active',
            componentProps: { options: [{ label: 'Đang dùng', value: 'active' }, { label: 'Ngừng', value: 'inactive' }] }
        },
    ],
    showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    params.value = [];
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate) && data.record) {
        recordId.value = data.record.id;
        setFieldsValue({ ...data.record });
        try {
            const res: any = await qcStageApi.getParams(data.record.id);
            params.value = (res || []).map((p: any) => ({
                ...p,
                isRequiredBool: p.isRequired === 1,
            }));
        } catch (e) { }
    }
});

function addParam() {
    params.value.push({
        paramName: '', paramCode: '', inputType: 'text', unit: '',
        defaultValue: '', minValue: null, maxValue: null,
        optionsJson: '', isRequired: 1, isRequiredBool: true, sortOrder: params.value.length + 1,
    });
}

async function handleSubmit() {
    try {
        const values = await validate();
        setModalProps({ confirmLoading: true });
        const payload = {
            stage: { ...values, id: unref(isUpdate) ? unref(recordId) : undefined },
            params: params.value,
        };
        if (unref(isUpdate)) {
            await qcStageApi.edit(payload);
        } else {
            await qcStageApi.add(payload);
        }
        emit('success');
        closeModal();
    } finally {
        setModalProps({ confirmLoading: false });
    }
}
</script>
