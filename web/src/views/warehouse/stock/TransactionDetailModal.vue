<template>
  <BasicModal
    v-bind="$attrs"
    title="Transaction Details"
    :width="900"
    @register="registerModal"
    :showOkBtn="false"
    :destroyOnClose="true"
  >
    <Description
      v-if="transactionData"
      :data="transactionData"
      :schema="transactionDetailSchema"
      :column="2"
    />
    
    <Divider>Transaction Items</Divider>
    
    <BasicTable
      v-if="itemDataSource.length > 0"
      :columns="stockTransactionItemColumns"
      :dataSource="itemDataSource"
      :pagination="false"
      :bordered="true"
    />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Description } from '/@/components/Description';
  import { BasicTable } from '/@/components/Table';
  import { Divider } from 'ant-design-vue';
  import { stockTransactionItemApi } from './stockTransaction.api';
  import { stockTransactionItemColumns } from './stockTransaction.data';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const transactionData = ref<Recordable>({});
  const itemDataSource = ref<Recordable[]>([]);

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    
    if (data?.record) {
      transactionData.value = data.record;
      
      // Load transaction items
      try {
        const items = await stockTransactionItemApi.getItemsByTransactionId(data.record.id);
        itemDataSource.value = items;
      } catch (error) {
        console.error('Failed to load transaction items:', error);
        itemDataSource.value = [];
      }
    }
  });

  const transactionDetailSchema = reactive([
    {
      field: 'transactionNumber',
      label: 'Transaction Number',
    },
    {
      field: 'transactionType_dictText',
      label: 'Transaction Type',
    },
    {
      field: 'status_dictText',
      label: 'Status',
    },
    {
      field: 'sourceLocationName',
      label: 'Source Location',
    },
    {
      field: 'targetLocationName',
      label: 'Target Location',
    },
    {
      field: 'supplierName',
      label: 'Supplier',
    },
    {
      field: 'transactionDate',
      label: 'Transaction Date',
    },
    {
      field: 'createByName',
      label: 'Created By',
    },
    {
      field: 'createTime',
      label: 'Create Time',
    },
    {
      field: 'approveByName',
      label: 'Approved By',
    },
    {
      field: 'approveTime',
      label: 'Approve Time',
    },
    {
      field: 'remarks',
      label: 'Remarks',
      span: 2,
    },
  ]);
</script>