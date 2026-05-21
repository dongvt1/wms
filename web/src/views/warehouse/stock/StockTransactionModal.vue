<template>
  <BasicModal
    v-bind="$attrs"
    :title="getTitle"
    :width="800"
    @register="registerModal"
    @ok="handleSubmit"
    :destroyOnClose="true"
  >
    <BasicForm @register="registerForm" />
    
    <Divider>Transaction Items</Divider>
    
    <BasicTable
      @register="registerItemTable"
      :dataSource="itemDataSource"
      :columns="stockTransactionItemColumns"
      :pagination="false"
      :actionColumn="itemActionColumn"
    >
      <template #toolbar>
        <a-button type="primary" @click="handleAddItem"> Add Item </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: 'Edit',
                onClick: handleEditItem.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                tooltip: 'Delete',
                color: 'error',
                onClick: handleDeleteItem.bind(null, record),
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>

    <StockTransactionItemModal @register="registerItemModal" @success="handleItemSuccess" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { Divider } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import { stockTransactionApi, stockTransactionItemApi } from './stockTransaction.api';
  import { stockTransactionFormSchema, stockTransactionItemColumns } from './stockTransaction.data';
  import StockTransactionItemModal from './StockTransactionItemModal.vue';
  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  const itemDataSource = ref<Recordable[]>([]);
  const currentEditIndex = ref<number>(-1);
  const isUpdate = ref(false);
  const recordId = ref<string>('');

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    recordId.value = data?.record?.id || '';
    
    if (isUpdate.value) {
      // Load transaction data
      const transactionData = data.record;
      setFieldsValue(transactionData);
      
      // Load transaction items
      try {
        const items = await stockTransactionItemApi.getItemsByTransactionId(transactionData.id);
        itemDataSource.value = items;
      } catch (error) {
        console.error('Failed to load transaction items:', error);
      }
    } else {
      // Reset form for new transaction
      resetFields();
      itemDataSource.value = [];
      
      // Set default values based on transaction type
      if (data?.transactionType) {
        setFieldsValue({ transactionType: data.transactionType });
      }
    }
  });

  const [registerForm, { setFieldsValue, resetFields, validate, getFieldsValue }] = useForm({
    labelWidth: 120,
    schemas: stockTransactionFormSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  const [registerItemTable] = useTable({
    columns: stockTransactionItemColumns,
    pagination: false,
    bordered: true,
    showIndexColumn: false,
    actionColumn: {
      width: 100,
      title: 'Action',
      dataIndex: 'action',
      fixed: 'right',
    },
  });

  const [registerItemModal, { openModal: openItemModal }] = useModal();

  const itemActionColumn = reactive({
    width: 100,
    title: 'Action',
    dataIndex: 'action',
    fixed: 'right' as const,
  });

  const getTitle = computed(() => {
    const formData = getFieldsValue();
    if (isUpdate.value) {
      return 'Edit Stock Transaction';
    } else if (formData.transactionType === 'IN') {
      return 'Stock In Transaction';
    } else if (formData.transactionType === 'OUT') {
      return 'Stock Out Transaction';
    } else if (formData.transactionType === 'TRANSFER') {
      return 'Stock Transfer Transaction';
    }
    return 'New Stock Transaction';
  });

  async function handleSubmit() {
    try {
      setModalProps({ confirmLoading: true });
      const values = await validate();
      
      // Validate that at least one item is added
      if (itemDataSource.value.length === 0) {
        createMessage.error('Please add at least one item to the transaction');
        setModalProps({ confirmLoading: false });
        return;
      }
      
      // Prepare transaction data
      const transactionData = {
        ...values,
        items: itemDataSource.value,
      };
      
      if (isUpdate.value) {
        // Update existing transaction
        // Note: Edit functionality for stock transactions is not implemented in the API
        // This would typically be handled by cancelling the old transaction and creating a new one
        createMessage.warning('Editing stock transactions is not supported. Please cancel and create a new transaction.');
        setModalProps({ confirmLoading: false });
        return;
        createMessage.success('Transaction updated successfully');
      } else {
        // Create new transaction based on type
        try {
          if (values.transactionType === 'IN') {
            await stockTransactionApi.createStockIn(transactionData);
          } else if (values.transactionType === 'OUT') {
            await stockTransactionApi.createStockOut(transactionData);
          } else if (values.transactionType === 'TRANSFER') {
            await stockTransactionApi.createStockTransfer(transactionData);
          }
          
          createMessage.success('Transaction created successfully');
        } catch (error) {
          createMessage.error('Failed to create transaction');
          setModalProps({ confirmLoading: false });
          return;
        }
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

  function handleAddItem() {
    currentEditIndex.value = -1;
    openItemModal(true, {
      isUpdate: false,
    });
  }

  function handleEditItem(record: Recordable, index: number) {
    currentEditIndex.value = index;
    openItemModal(true, {
      record,
      isUpdate: true,
    });
  }

  function handleDeleteItem(record: Recordable) {
    const index = itemDataSource.value.findIndex((item) => item.id === record.id);
    if (index !== -1) {
      itemDataSource.value.splice(index, 1);
    }
  }

  function handleItemSuccess(values: Recordable) {
    if (currentEditIndex.value === -1) {
      // Add new item
      itemDataSource.value.push({
        ...values,
        id: Date.now().toString(), // Temporary ID for new items
      });
    } else {
      // Update existing item
      itemDataSource.value[currentEditIndex.value] = {
        ...itemDataSource.value[currentEditIndex.value],
        ...values,
      };
    }
  }
</script>