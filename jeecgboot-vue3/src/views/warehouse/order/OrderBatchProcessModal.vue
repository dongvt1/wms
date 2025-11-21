<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="modalTitle" @ok="handleSubmit" :width="600">
    <BasicForm @register="registerForm" />
    <div v-if="selectedOrders.length > 0" class="mt-4">
      <h4>选中的订单 ({{ selectedOrders.length }})</h4>
      <a-table 
        :dataSource="selectedOrders" 
        :columns="orderColumns" 
        :pagination="false" 
        size="small"
        :scroll="{ y: 200 }"
      />
    </div>
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
  const selectedOrders = ref<Recordable[]>([]);
  const modalData = ref<Recordable>({});

  const orderColumns = [
    {
      title: '订单编码',
      dataIndex: 'orderCode',
      width: 120,
    },
    {
      title: '客户名称',
      dataIndex: 'customerName',
      width: 120,
    },
    {
      title: '订单金额',
      dataIndex: 'finalAmount',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
    },
  ];

  const [registerForm, { setFieldsValue, validate, resetFields }] = useForm({
    labelWidth: 100,
    schemas: [
      {
        field: 'action',
        label: '处理动作',
        component: 'Select',
        required: true,
        componentProps: {
          options: [
            { label: '确认订单', value: 'CONFIRM' },
            { label: '取消订单', value: 'CANCEL' },
            { label: '开始配送', value: 'SHIP' },
            { label: '完成订单', value: 'COMPLETE' },
          ],
          placeholder: '请选择处理动作',
        },
      },
      {
        field: 'reason',
        label: '处理原因',
        component: 'InputTextArea',
        required: true,
        componentProps: {
          placeholder: '请输入处理原因',
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
    selectedOrders.value = data.selectedOrders || [];
    modalData.value = {
      title: data.title || '批量处理订单',
    };
    
    // Set form values
    setFieldsValue({
      action: data.action || '',
      reason: data.reason || '',
    });
  });

  const modalTitle = computed(() => unref(modalData).title || '批量处理订单');

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      
      if (selectedOrders.value.length === 0) {
        createMessage.error('请选择要处理的订单');
        return;
      }
      
      const orderIds = selectedOrders.value.map(order => order.id);
      const params = {
        orderIds,
        action: values.action,
        reason: values.reason,
      };
      
      const result = await orderApi.batchProcess(params);
      
      if (result.success) {
        const { successCount, failedCount } = result.result;
        createMessage.success(`批量处理完成，成功: ${successCount}，失败: ${failedCount}`);
        closeModal();
        emit('success');
      } else {
        createMessage.error(result.message || '批量处理失败');
      }
    } catch (error) {
      createMessage.error('批量处理失败');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style scoped>
.mt-4 {
  margin-top: 16px;
}
</style>