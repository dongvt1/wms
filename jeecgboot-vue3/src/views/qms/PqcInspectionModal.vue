<template>
    <BasicModal v-bind="$attrs" @register="registerModal" :title="isUpdate ? 'Sửa phiếu PQC' : 'Tạo phiếu kiểm tra PQC'"
        width="900px" @ok="handleSubmit">
        <BasicForm @register="registerForm" />

        <a-divider>Kết quả chi tiết tiêu chí kiểm tra</a-divider>
        <a-table :dataSource="resultItems" :columns="resultColumns" :pagination="false" size="small" bordered
            class="mb-2">
            <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'actualValue'">
                    <a-input v-if="record.inputType === 'text' || !record.inputType" v-model:value="record.actualValue"
                        placeholder="Nhập giá trị..." />
                    <a-input-number v-else-if="record.inputType === 'number'" v-model:value="record.actualValue"
                        style="width:100%" />
                    <a-select v-else v-model:value="record.actualValue" style="width:100%">
                        <a-select-option value="passed">✅ Đạt</a-select-option>
                        <a-select-option value="failed">❌ Không đạt</a-select-option>
                        <a-select-option value="na">N/A</a-select-option>
                    </a-select>
                </template>
                <template v-if="column.key === 'result'">
                    <a-select v-model:value="record.result" style="width:100%">
                        <a-select-option value="passed">Đạt</a-select-option>
                        <a-select-option value="failed">Không đạt</a-select-option>
                        <a-select-option value="na">N/A</a-select-option>
                    </a-select>
                </template>
                <template v-if="column.key === 'notes'">
                    <a-input v-model:value="record.notes" placeholder="Ghi chú..." />
                </template>
                <template v-if="column.key === 'action'">
                    <a-button danger size="small" @click="removeResult(resultItems.indexOf(record))">Xóa</a-button>
                </template>
            </template>
        </a-table>
        <a-button type="dashed" block @click="addManualItem">+ Thêm tiêu chí thủ công</a-button>
    </BasicModal>
</template>

<script lang="ts" name="pqc-inspection-modal" setup>
import { ref, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { pqcApi } from '/@/api/warehouse/pqcInspection';
import { qmsChecklistApi } from '/@/api/warehouse/qmsChecklist';
import { workOrderApi } from '/@/api/warehouse/workOrder';
import { productApi } from '/@/api/warehouse/product';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref(false);
const recordId = ref('');
const resultItems = ref<any[]>([]);

const resultColumns = [
    { title: 'Tên tiêu chí', dataIndex: 'criterionName', width: '25%' },
    { title: 'Tiêu chuẩn', dataIndex: 'standardValue', width: '18%' },
    { title: 'Giá trị thực', key: 'actualValue', width: '22%' },
    { title: 'Kết quả', key: 'result', width: '15%' },
    { title: 'Ghi chú', key: 'notes', width: '15%' },
    { title: '', key: 'action', width: '5%' },
];

const [registerForm, { setFieldsValue, resetFields, validate, updateSchema }] = useForm({
    labelWidth: 150,
    schemas: [
        {
            field: 'inspectionCode', label: 'Mã phiếu', component: 'Input',
            helpMessage: 'Để trống để tự động sinh mã'
        },
        {
            field: 'workOrderId', label: 'Lệnh sản xuất', component: 'Select',
            componentProps: { options: [], showSearch: true, placeholder: 'Chọn lệnh SX' }
        },
        {
            field: 'productId', label: 'Thành phẩm', component: 'Select', required: true,
            componentProps: { options: [], showSearch: true, placeholder: 'Chọn sản phẩm' }
        },
        {
            field: 'templateId', label: 'Mẫu checklist', component: 'Select',
            helpMessage: 'Chọn mẫu để tự load tiêu chí',
            componentProps: { options: [], showSearch: true, placeholder: 'Chọn mẫu checklist PQC' }
        },
        {
            field: 'quantityInspected', label: 'SL kiểm tra', component: 'InputNumber', required: true,
            componentProps: { min: 0, style: 'width:100%' }
        },
        { field: 'inspector', label: 'Người kiểm tra', component: 'Input' },
        {
            field: 'inspectionDate', label: 'Ngày kiểm tra', component: 'DatePicker',
            componentProps: { valueFormat: 'YYYY-MM-DD', style: 'width:100%' }
        },
        {
            field: 'status', label: 'Trạng thái', component: 'Select', defaultValue: 'in_progress',
            componentProps: {
                options: [
                    { label: 'Nháp', value: 'draft' },
                    { label: 'Đang kiểm tra', value: 'in_progress' },
                ],
            },
        },
        { field: 'notes', label: 'Ghi chú', component: 'InputTextArea', componentProps: { rows: 2 } },
    ],
    showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    resultItems.value = [];
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    // Load work orders
    try {
        const woRes: any = await workOrderApi.list({ pageSize: 999, status: 'in_progress' });
        const wos = woRes?.records || woRes || [];
        const woOpts = wos.map((w: any) => ({ label: w.orderCode, value: w.id }));
        updateSchema([{ field: 'workOrderId', componentProps: { options: woOpts } }]);
    } catch (e) { }

    // Load products
    try {
        const pRes: any = await productApi.list({ pageSize: 999 });
        const prods = pRes?.records || pRes || [];
        const pOpts = prods.map((p: any) => ({ label: `${p.code} - ${p.name}`, value: p.id }));
        updateSchema([{ field: 'productId', componentProps: { options: pOpts } }]);
    } catch (e) { }

    // Load PQC templates
    try {
        const templates: any = await qmsChecklistApi.listActive('pqc');
        const tOpts = (templates || []).map((t: any) => ({ label: `${t.templateCode} - ${t.templateName}`, value: t.id }));
        updateSchema([{ field: 'templateId', componentProps: { options: tOpts } }]);
    } catch (e) { }

    if (unref(isUpdate) && data.record) {
        recordId.value = data.record.id;
        setFieldsValue({ ...data.record });
        try {
            const res: any = await pqcApi.getResults(data.record.id);
            resultItems.value = (res || []).map((r: any) => ({ ...r }));
        } catch (e) { }
    }
});

async function onTemplateChange(templateId: string) {
    if (!templateId) return;
    try {
        const items: any = await qmsChecklistApi.getItems(templateId);
        resultItems.value = (items || []).map((item: any) => ({
            checklistItemId: item.id,
            criterionName: item.criterionName,
            standardValue: item.standardValue,
            inputType: item.inputType,
            actualValue: '',
            result: '',
            notes: '',
        }));
    } catch (e) { }
}

function addManualItem() {
    resultItems.value.push({ criterionName: '', standardValue: '', inputType: 'pass_fail', actualValue: '', result: '', notes: '' });
}

function removeResult(idx: number) {
    resultItems.value.splice(idx, 1);
}

async function handleSubmit() {
    try {
        const values = await validate();
        setModalProps({ confirmLoading: true });
        if (values.templateId && resultItems.value.length === 0) {
            await onTemplateChange(values.templateId);
        }
        const payload = {
            inspection: { ...values, id: unref(isUpdate) ? unref(recordId) : undefined },
            results: resultItems.value,
        };
        if (unref(isUpdate)) {
            await pqcApi.edit(payload);
        } else {
            await pqcApi.add(payload);
        }
        emit('success');
        closeModal();
    } finally {
        setModalProps({ confirmLoading: false });
    }
}
</script>
