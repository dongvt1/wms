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
  import { customerApi } from './customer.api';
  import { customerFormSchema } from './customer.data';

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
      // Reset form for new customer
      resetFields();
    }
  });

  const [registerForm, { setFieldsValue, resetFields, validate, getFieldsValue }] = useForm({
    labelWidth: 120,
    schemas: customerFormSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  const getTitle = computed(() => {
    return isUpdate.value ? 'Edit Customer' : 'New Customer';
  });

  async function handleSubmit() {
    try {
      setModalProps({ confirmLoading: true });
      const values = await validate();
      
      if (isUpdate.value) {
        // Update existing customer
        await customerApi.edit(values);
        createMessage.success('Customer updated successfully');
      } else {
        // Create new customer
        await customerApi.add(values);
        createMessage.success('Customer created successfully');
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