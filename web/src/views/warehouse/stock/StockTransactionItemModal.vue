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
  import { stockTransactionItemFormSchema } from './stockTransaction.data';
  import { productApi } from '../product/product.api';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  const isUpdate = ref(false);

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    
    if (isUpdate.value) {
      // Set form values for editing
      setFieldsValue(data.record);
    } else {
      // Reset form for new item
      resetFields();
    }
  });

  const [registerForm, { setFieldsValue, resetFields, validate, getFieldsValue }] = useForm({
    labelWidth: 120,
    schemas: stockTransactionItemFormSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  const getTitle = computed(() => {
    return isUpdate.value ? 'Edit Transaction Item' : 'Add Transaction Item';
  });

  async function handleSubmit() {
    try {
      setModalProps({ confirmLoading: true });
      const values = await validate();
      
      // Calculate total amount
      if (values.quantity && values.unitPrice) {
        values.totalAmount = values.quantity * values.unitPrice;
      }
      
      closeModal();
      emit('success', values);
    } catch (error) {
      console.error('Submit error:', error);
      createMessage.error('Operation failed');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>