<template>
  <div>
    <BasicTable @register="registerTable" :actionColumn="actionColumn">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate"> New Supplier </a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: 'Edit',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                tooltip: 'Delete',
                color: 'error',
                onClick: handleDelete.bind(null, record),
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>

    <SupplierModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { supplierApi } from './stockTransaction.api';
  import { supplierColumns, supplierSearchFormSchema } from './stockTransaction.data';
  import SupplierModal from './SupplierModal.vue';

  // Recordable type definition
  type Recordable<T = any> = Record<string, T>;

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();

  const [registerTable, { reload }] = useTable({
    title: 'Supplier List',
    api: supplierApi.list,
    columns: supplierColumns,
    formConfig: {
      labelWidth: 120,
      schemas: supplierSearchFormSchema,
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: false,
    actionColumn: {
      width: 120,
      title: 'Action',
      dataIndex: 'action',
      fixed: 'right',
    },
  });

  const actionColumn = reactive({
    width: 120,
    title: 'Action',
    dataIndex: 'action',
    fixed: 'right' as const,
  });

  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }

  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  async function handleDelete(record: Recordable) {
    try {
      await supplierApi.delete(record.id);
      createMessage.success('Supplier deleted successfully');
      reload();
    } catch (error) {
      createMessage.error('Failed to delete supplier');
    }
  }

  function handleSuccess() {
    reload();
  }
</script>