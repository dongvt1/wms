<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">Add Product</a-button>
      </template>
    </BasicTable>
    <ProductModal @register="registerModal" @success="handleSuccess" />
    <ProductHistoryModal @register="registerHistoryModal" />
  </div>
</template>

<script lang="ts" name="product-list" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction, useTable, ActionItem } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { columns, searchFormSchema, getCategoryOptions } from './product.data';
  import { productApi } from './product.api';
  import ProductModal from './ProductModal.vue';
  import ProductHistoryModal from './ProductHistoryModal.vue';
  import { onMounted } from 'vue';

  const [registerTable, { reload, setProps }] = useTable({
    title: 'Product Management',
    api: productApi.list,
    columns,
    formConfig: {
      labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: {
      width: 200,
      title: 'Actions',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: 'right',
    },
  });

  const [registerModal, { openModal }] = useModal();
  const [registerHistoryModal, { openModal: openHistoryModal }] = useModal();

  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'Edit',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'View History',
        onClick: handleViewHistory.bind(null, record),
      },
      {
        label: 'Delete',
        popConfirm: {
          title: 'Are you sure to delete this product?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  function handleAdd() {
    openModal(true, {
      isUpdate: false,
    });
  }

  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  function handleViewHistory(record) {
    openHistoryModal(true, {
      productId: record.id,
      productName: record.name,
    });
  }

  async function handleDelete(record) {
    await productApi.delete({ id: record.id });
    reload();
  }

  function handleSuccess() {
    reload();
  }

  // Get category options during initialization
  onMounted(async () => {
    try {
      const categoryOptions = await getCategoryOptions();
      // Update category options in search form
      setProps({
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema.map(schema => {
            if (schema.field === 'categoryId') {
              return {
                ...schema,
                componentProps: {
                  ...schema.componentProps,
                  options: categoryOptions,
                },
              };
            }
            return schema;
          }),
          autoSubmitOnEnter: true,
        },
      });
    } catch (error) {
      console.error('Failed to fetch category list:', error);
    }
  });
</script>