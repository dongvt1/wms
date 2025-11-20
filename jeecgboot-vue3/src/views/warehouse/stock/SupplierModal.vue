<template>
  <BasicModal
    v-bind="$attrs"
    :title="getTitle"
    :width="600"
    @register="registerModal"
    @ok="handleSubmit"
    :destroyOnClose="true"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { supplierApi } from './stockTransaction.api';
  import { supplierFormSchema } from './stockTransaction.data';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  const isUpdate = ref(false);
  const recordId = ref<string>('');

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    recordId.value = data?.record?.id || '';
    
    if (isUpdate.value) {
      // Set form values for editing
      setFieldsValue(data.record);
    } else {
      // Reset form for new supplier
      resetFields();
    }
  });

  const [registerForm, { setFieldsValue, resetFields, validate, getFieldsValue }] = useForm({
    labelWidth: 120,
    schemas: supplierFormSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  const getTitle = computed(() => {
    return isUpdate.value ? 'Edit Supplier' : 'New Supplier';
  });

  async function handleSubmit() {
    try {
      setModalProps({ confirmLoading: true });
      const values = await validate();
      
      if (isUpdate.value) {
        // Update existing supplier
        await supplierApi.edit(values);
        createMessage.success('Supplier updated successfully');
      } else {
        // Create new supplier
        await supplierApi.add(values);
        createMessage.success('Supplier created successfully');
      }
      
      closeModal();
      emit('success');
    } catch (error) {
      console.error('Submit error:', error);
      createMessage.error('Operation failed');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>