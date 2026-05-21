<template>
    <BasicModal v-bind="$attrs" @register="registerModal" :title="isUpdate ? 'Sửa NCR' : 'Tạo báo cáo NCR'"
        width="800px" @ok="handleSubmit">
        <BasicForm @register="registerForm" />
    </BasicModal>
</template>

<script lang="ts" name="ncr-modal" setup>
import { ref, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { ncrApi } from '/@/api/warehouse/ncr';
import { wmsSupplierApi } from '/@/api/wms/supplier';
import { useMessage } from '/@/hooks/web/useMessage';

const emit = defineEmits(['success', 'register']);
const { createMessage } = useMessage();
const isUpdate = ref(false);
const recordId = ref('');

const [registerForm, { setFieldsValue, resetFields, validate, updateSchema }] = useForm({
    labelWidth: 150,
    schemas: [
        {
            field: 'sourceType', label: 'Nguồn phát hiện', component: 'Select', required: true,
            componentProps: {
                options: [
                    { label: 'IQC - Kiểm tra đầu vào', value: 'iqc' },
                    { label: 'PQC - Kiểm tra sản xuất', value: 'pqc' },
                    { label: 'FQC - Kiểm tra thành phẩm', value: 'fqc' },
                    { label: 'Khác', value: 'other' },
                ],
                placeholder: 'Chọn nguồn phát hiện',
            },
        },
        {
            field: 'sourceId', label: 'Phiếu kiểm tra liên kết', component: 'Input',
            helpMessage: 'ID phiếu kiểm tra nguồn (IQC/PQC/FQC)',
            componentProps: { placeholder: 'Nhập ID phiếu kiểm tra liên kết' },
        },
        {
            field: 'productId', label: 'Sản phẩm', component: 'Input',
            componentProps: { placeholder: 'ID sản phẩm' },
        },
        {
            field: 'supplierId', label: 'Nhà cung cấp', component: 'Select',
            componentProps: { options: [], showSearch: true, placeholder: 'Chọn nhà cung cấp', allowClear: true },
        },
        {
            field: 'description', label: 'Mô tả lỗi', component: 'InputTextArea', required: true,
            componentProps: { rows: 3, placeholder: 'Mô tả chi tiết sự không phù hợp...' },
        },
        {
            field: 'severity', label: 'Mức độ nghiêm trọng', component: 'Select', required: true,
            componentProps: {
                options: [
                    { label: 'Nghiêm trọng (Critical)', value: 'critical' },
                    { label: 'Lớn (Major)', value: 'major' },
                    { label: 'Nhỏ (Minor)', value: 'minor' },
                ],
                placeholder: 'Chọn mức độ',
            },
        },
        {
            field: 'quantityDefective', label: 'Số lượng lỗi', component: 'InputNumber',
            componentProps: { min: 0, style: 'width:100%', placeholder: 'Nhập số lượng lỗi' },
        },
        {
            field: 'proposedAction', label: 'Hành động đề xuất', component: 'Select',
            componentProps: {
                options: [
                    { label: 'Trả nhà cung cấp', value: 'return' },
                    { label: 'Sửa chữa', value: 'repair' },
                    { label: 'Hủy', value: 'scrap' },
                    { label: 'Chấp nhận có điều kiện', value: 'accept_conditional' },
                ],
                placeholder: 'Chọn hành động đề xuất',
                allowClear: true,
            },
        },
        {
            field: 'assignedTo', label: 'Người phụ trách', component: 'Input',
            componentProps: { placeholder: 'Tên người phụ trách xử lý' },
        },
        {
            field: 'notes', label: 'Ghi chú', component: 'InputTextArea',
            componentProps: { rows: 2, placeholder: 'Ghi chú thêm...' },
        },
    ],
    showActionButtonGroup: false,
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    // Load suppliers for dropdown
    try {
        const suppliers: any = await wmsSupplierApi.getActive();
        const supplierOpts = (suppliers || []).map((s: any) => ({
            label: `${s.supplierCode} - ${s.supplierName}`,
            value: s.id,
        }));
        updateSchema([{ field: 'supplierId', componentProps: { options: supplierOpts } }]);
    } catch (e) { }

    if (unref(isUpdate) && data.record) {
        recordId.value = data.record.id;
        setFieldsValue({ ...data.record });
    }
});

async function handleSubmit() {
    try {
        const values = await validate();
        setModalProps({ confirmLoading: true });

        if (unref(isUpdate)) {
            await ncrApi.edit({ ...values, id: unref(recordId) });
            createMessage.success('Cập nhật NCR thành công!');
        } else {
            await ncrApi.add(values);
            createMessage.success('Tạo NCR thành công!');
        }
        emit('success');
        closeModal();
    } finally {
        setModalProps({ confirmLoading: false });
    }
}
</script>
