<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="modalTitle" @ok="handleSubmit" :width="1000">
    <BasicForm @register="registerForm" />
    <Divider>Order Items</Divider>
    <BasicTable @register="registerItemTable">
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              icon: 'ant-design:delete-outlined',
              color: 'error',
              tooltip: 'Delete',
              onClick: handleDeleteItem.bind(null, record),
            },
          ]"
        />
      </template>
    </BasicTable>
    <div class="mt-4">
      <a-button type="dashed" @click="handleAddItem" block>
        <PlusOutlined /> Add Item
      </a-button>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { Divider, Button } from 'ant-design-vue';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { orderFormSchema, orderItemColumns } from './order.data';
  import { orderApi } from './order.api';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  const isUpdate = ref(false);
  const orderRecord = ref<Recordable>({});
  const orderItems = ref<Recordable[]>([]);

  const [registerForm, { setFieldsValue, validate, resetFields }] = useForm({
    labelWidth: 100,
    schemas: orderFormSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  const [registerItemTable, { setTableData }] = useTable({
    title: 'Order Items',
    columns: orderItemColumns,
    bordered: true,
    showIndexColumn: false,
    pagination: false,
    canResize: false,
    actionColumn: {
      width: 80,
      title: 'Action',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    
    if (data?.record) {
      orderRecord.value = data.record;
      setFieldsValue({
        customerId: data.record.customerId,
        notes: data.record.notes,
      });
      
      // Load order items if updating
      if (isUpdate.value) {
        const orderDetail = await orderApi.queryById(data.record.id);
        if (orderDetail.success && orderDetail.result) {
          orderItems.value = orderDetail.result.orderItems || [];
          setTableData(orderItems.value);
        }
      } else {
        orderItems.value = [];
        setTableData([]);
      }
    }
  });

  const modalTitle = computed(() => (unref(isUpdate) ? 'Edit Order' : 'Create Order'));

  function handleAddItem() {
    const newItem = {
      id: `temp_${Date.now()}`,
      productId: '',
      productName: '',
      productCode: '',
      quantity: 1,
      unitPrice: 0,
      totalPrice: 0,
      discountAmount: 0,
      finalAmount: 0,
    };
    orderItems.value.push(newItem);
    setTableData([...orderItems.value]);
  }

  function handleDeleteItem(record: Recordable) {
    const index = orderItems.value.findIndex(item => item.id === record.id);
    if (index !== -1) {
      orderItems.value.splice(index, 1);
      setTableData([...orderItems.value]);
    }
  }

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      
      // Validate order items
      if (orderItems.value.length === 0) {
        createMessage.error('Please add at least one order item');
        return;
      }
      
      // Validate each order item
      for (const item of orderItems.value) {
        if (!item.productId) {
          createMessage.error('Please select product for all items');
          return;
        }
        if (!item.quantity || item.quantity <= 0) {
          createMessage.error('Please enter valid quantity for all items');
          return;
        }
        if (!item.unitPrice || item.unitPrice <= 0) {
          createMessage.error('Please enter valid unit price for all items');
          return;
        }
      }
      
      const params = {
        ...values,
        createdBy: 'admin', // TODO: Get from current user
        orderItems: orderItems.value,
      };
      
      let result;
      if (isUpdate.value) {
        params.id = orderRecord.value.id;
        result = await orderApi.edit(params);
      } else {
        result = await orderApi.add(params);
      }
      
      if (result.success) {
        createMessage.success(`Order ${isUpdate.value ? 'updated' : 'created'} successfully`);
        closeModal();
        emit('success');
      } else {
        createMessage.error(result.message || `Failed to ${isUpdate.value ? 'update' : 'create'} order`);
      }
    } catch (error) {
      createMessage.error(`Failed to ${isUpdate.value ? 'update' : 'create'} order`);
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
