<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">Add Category</a-button>
      </template>
    </BasicTable>
    <CategoryModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="category-list" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction, useTable, ActionItem } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { categoryColumns, categorySearchFormSchema } from './category.data';
  import { categoryApi } from './category.api';
  import CategoryModal from './CategoryModal.vue';

  const [registerTable, { reload }] = useTable({
    title: 'Category Management',
    api: categoryApi.list,
    columns: categoryColumns,
    formConfig: {
      labelWidth: 120,
      schemas: categorySearchFormSchema,
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: true,
    actionColumn: {
      width: 150,
      title: 'Actions',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: 'right',
    },
  });

  const [registerModal, { openModal }] = useModal();

  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'Edit',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Delete',
        popConfirm: {
          title: 'Are you sure to delete this category?',
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

  async function handleDelete(record) {
    await categoryApi.delete({ id: record.id });
    reload();
  }

  function handleSuccess() {
    reload();
  }
</script>