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
  const modalData = ref<Recordable>({});

  const [registerForm, { setFieldsValue, validate, resetFields }] = useForm({
    labelWidth: 100,
    schemas: [
      {
        field: 'newStatus',
        label: 'New Status',
        component: 'Select',
        required: true,
        componentProps: {
          options: [
            { label: 'Pending', value: 'PENDING' },
            { label: 'Confirmed', value: 'CONFIRMED' },
            { label: 'Shipping', value: 'SHIPPING' },
            { label: 'Completed', value: 'COMPLETED' },
            { label: 'Cancelled', value: 'CANCELLED' },
          ],
          placeholder: 'Please select new status',
        },
      },
      {
        field: 'reason',
        label: 'Reason',
        component: 'InputTextArea',
        required: true,
        componentProps: {
          placeholder: 'Please enter reason for status change',
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
    modalData.value = {
      newStatus: data.newStatus || '',
      title: data.title || 'Update Order Status',
      reasonRequired: data.reasonRequired !== false,
    };
    
    // Set form values
    setFieldsValue({
      newStatus: data.newStatus || '',
    });
  });

  const modalTitle = computed(() => unref(modalData).title || 'Update Order Status');

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      
      const result = await orderApi.updateStatus(
        unref(orderRecord).id, 
        values.newStatus, 
        values.reason
      );
      
      if (result.success) {
        createMessage.success('Order status updated successfully');
        closeModal();
        emit('success');
      } else {
        createMessage.error(result.message || 'Failed to update order status');
      }
    } catch (error) {
      createMessage.error('Failed to update order status');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>