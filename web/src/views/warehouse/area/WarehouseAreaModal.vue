<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" :width="700">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema } from './area.data';
  import { warehouseAreaApi } from './area.api';

  // statementEmits
  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  const rowId = ref('');

  // Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  // form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;

    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      setFieldsValue({
        ...data.record,
      });
    }
  });

  // Set title
  const getTitle = computed(() => (!unref(isUpdate) ? 'Add Warehouse Area' : 'Edit Warehouse Area'));

  // form submission event
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });

      if (unref(isUpdate)) {
        await warehouseAreaApi.update({ ...values, id: rowId.value });
      } else {
        await warehouseAreaApi.save(values);
      }

      // Close pop-up window
      closeModal();
      // Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  :deep(.ant-input-number){
    width: 100%;
  }
</style>