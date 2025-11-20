<template>
  <BasicModal
    v-bind="$attrs"
    title="Customer Order History"
    :width="900"
    @register="registerModal"
    :showOkBtn="false"
    :destroyOnClose="true"
  >
    <Description
      v-if="customerData"
      :data="customerData"
      :schema="customerDetailSchema"
      :column="2"
    />
    
    <Divider>Order History</Divider>
    
    <BasicTable
      v-if="orderHistory.length > 0"
      :columns="customerOrderHistoryColumns"
      :dataSource="orderHistory"
      :pagination="false"
      :bordered="true"
    />
    <Empty v-else description="No order history found" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Description } from '/@/components/Description';
  import { BasicTable } from '/@/components/Table';
  import { Divider, Empty } from 'ant-design-vue';
  import { customerApi } from './customer.api';
  import { customerOrderHistoryColumns } from './customer.data';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const customerData = ref<Recordable>({});
  const orderHistory = ref<Recordable[]>([]);

  const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    
    if (data?.record) {
      customerData.value = data.record;
      
      // Load order history
      try {
        const orders = await customerApi.getOrderHistory(data.record.id);
        orderHistory.value = orders;
      } catch (error) {
        console.error('Failed to load order history:', error);
        orderHistory.value = [];
      }
    }
  });

  const customerDetailSchema = reactive([
    {
      field: 'customerCode',
      label: 'Customer Code',
    },
    {
      field: 'customerName',
      label: 'Customer Name',
    },
    {
      field: 'contactPerson',
      label: 'Contact Person',
    },
    {
      field: 'phone',
      label: 'Phone',
    },
    {
      field: 'email',
      label: 'Email',
    },
    {
      field: 'address',
      label: 'Address',
      span: 2,
    },
  ]);
</script>