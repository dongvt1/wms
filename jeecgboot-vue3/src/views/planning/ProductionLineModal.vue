<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="isUpdate ? 'Sửa dây chuyền' : 'Thêm dây chuyền'"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" name="production-line-modal" setup>
  import { ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { productionLineApi } from '/@/api/warehouse/productionLine';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const recordId = ref('');

  const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
    labelWidth: 120,
    schemas: [
      { field: 'lineCode', label: 'Mã dây chuyền', component: 'Input', required: true,
        componentProps: { placeholder: 'Nhập mã dây chuyền' } },
      { field: 'lineName', label: 'Tên dây chuyền', component: 'Input', required: true },
      { field: 'capacityPerDay', label: 'Năng suất/ngày', component: 'InputNumber',
        componentProps: { placeholder: '0', style: 'width: 100%' } },
      { field: 'unit', label: 'Đơn vị', component: 'Input',
        componentProps: { placeholder: 'cái, kg, m...' } },
      { field: 'status', label: 'Trạng thái', component: 'Select', required: true,
        defaultValue: 'active',
        componentProps: {
          options: [
            { label: 'Hoạt động', value: 'active' },
            { label: 'Ngừng hoạt động', value: 'inactive' },
            { label: 'Đang bảo trì', value: 'maintenance' },
          ],
        },
      },
      { field: 'description', label: 'Mô tả', component: 'InputTextArea',
        componentProps: { rows: 3 } },
    ],
    showActionButtonGroup: false,
    actionColOptions: { span: 23 },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
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
        await productionLineApi.edit({ ...values, id: unref(recordId) });
      } else {
        await productionLineApi.add(values);
      }
      emit('success');
      closeModal();
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
