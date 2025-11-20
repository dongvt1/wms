<template>
  <BasicModal
    v-bind="$attrs"
    title="Customer Statistics"
    :width="600"
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
    
    <Divider>Balance Information</Divider>
    
    <Description
      v-if="balanceData"
      :data="balanceData"
      :schema="balanceSchema"
      :column="2"
    />
    
    <Divider>Order Statistics</Divider>
    
    <Description
      v-if="statisticsData"
      :data="statisticsData"
      :schema="customerStatisticsSchema"
      :column="2"
    />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Description } from '/@/components/Description';
  import { Divider } from 'ant-design-vue';
  import { customerApi } from './customer.api';
  import { customerStatisticsSchema } from './customer.data';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const customerData = ref<Recordable>({});
  const balanceData = ref<Recordable>({});
  const statisticsData = ref<Recordable>({});

  const [registerModal, { setModalProps }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    
    if (data?.record) {
      customerData.value = data.record;
      
      // Load customer balance
      try {
        const balance = await customerApi.getBalance(data.record.id);
        balanceData.value = {
          balance: balance.balance,
          lastUpdated: balance.lastUpdated,
          updatedBy: balance.updatedBy,
        };
      } catch (error) {
        console.error('Failed to load customer balance:', error);
        balanceData.value = {};
      }
      
      // Load customer statistics
      try {
        const statistics = await customerApi.getStatistics(data.record.id);
        statisticsData.value = statistics;
      } catch (error) {
        console.error('Failed to load customer statistics:', error);
        statisticsData.value = {};
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

  const balanceSchema = reactive([
    {
      field: 'balance',
      label: 'Current Balance',
      customRender: ({ text }) => {
        const balance = parseFloat(text);
        if (balance >= 0) {
          return `<span style="color: green; font-weight: bold;">¥${balance.toFixed(2)}</span>`;
        } else {
          return `<span style="color: red; font-weight: bold;">¥${Math.abs(balance).toFixed(2)}</span>`;
        }
      },
    },
    {
      field: 'lastUpdated',
      label: 'Last Updated',
    },
    {
      field: 'updatedBy',
      label: 'Updated By',
    },
  ]);
</script>