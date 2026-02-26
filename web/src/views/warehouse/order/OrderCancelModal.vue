<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="modalTitle" @ok="handleSubmit" :width="500">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { orderApi } from './order.api';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  const orderRecord = ref<Recordable>({});

  const [registerForm, { setFieldsValue, validate, resetFields }] = useForm({
    labelWidth: 100,
    schemas: [
      {
        field: 'reason',
        label: 'Cancel Reason',
        component: 'InputTextArea',
        required: true,
        componentProps: {
          placeholder: 'Please enter cancel reason',
          rows: 4,
        },
      },
    ],
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    orderRecord.value = data.record;
  });

  const modalTitle = computed(() => `Cancel Order: ${unref(orderRecord).orderCode || ''}`);

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      
      const result = await orderApi.cancel(unref(orderRecord).id, values.reason);
      
      if (result.success) {
        createMessage.success('Order cancelled successfully');
        closeModal();
        emit('success');
      } else {
        createMessage.error(result.message || 'Failed to cancel order');
      }
    } catch (error) {
      createMessage.error('Failed to cancel order');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>